/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.deployment.synchronizer.s3;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.deployment.synchronizer.ArtifactRepository;
import org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerException;

import java.io.File;
import java.util.*;

/**
 * S3 based artifact repository can be used in conjunction with the
 * DeploymentSynchronizer to synchronize a local repository against
 * a remote S3 bucket in the Amazon cloud (http://aws.amazon.com/s3/).
 * This implementation is particularly suited for Stratos based cloud
 * deployments.
 */
public class S3BasedArtifactRepository implements ArtifactRepository {

    private static final Log log = LogFactory.getLog(S3BasedArtifactRepository.class);

    private Bucket bucket;
    private AmazonS3Client s3Client;

    private Map<String,String> checksumTable = new HashMap<String,String>();
    private Map<String,Long> checkoutTimeTable = new HashMap<String,Long>();
    private Map<String,Long> commitTimeTable = new HashMap<String,Long>();

    public void init(int tenantId) throws DeploymentSynchronizerException {
        ServerConfiguration serverConfig = ServerConfiguration.getInstance();
        String accessKeyId = serverConfig.getFirstProperty(S3Constants.AWS_ACCESS_KEY_ID);
        if (accessKeyId == null) {
            handleException("AWS access key ID must be specified for the S3 based synchronizer");
        }

        String secretKey = serverConfig.getFirstProperty(S3Constants.AWS_SECRET_KEY);
        if (secretKey == null) {
            handleException("AWS secret key must be specified for the S3 based synchronizer");
        }

        AWSCredentials credentials = new BasicAWSCredentials(accessKeyId, secretKey);
        ClientConfiguration clientConfig = S3Utils.getClientConfiguration();
        s3Client = new AmazonS3Client(credentials, clientConfig);

        String bucketNamePrefix = S3Utils.getBucketNamePrefix();
        List<Bucket> buckets = s3Client.listBuckets();
        for (Bucket b : buckets) {
            if (b.getName().startsWith(bucketNamePrefix)) {
                if (log.isDebugEnabled()) {
                    log.debug("Found previously created S3 bucket: " + b.getName());
                }
                bucket = b;
                break;
            }
        }

        if (bucket == null) {
            String bucketName = bucketNamePrefix + tenantId + "-" + System.currentTimeMillis();
            log.info("Creating new S3 bucket: " + bucketName);
            bucket = s3Client.createBucket(bucketName);
        }
    }

    public void commit(String filePath) throws DeploymentSynchronizerException {
        File root = new File(filePath);
        Set<String> existingFiles = new HashSet<String>();
        Set<String> deletableFiles = new HashSet<String>();

        Collection<File> children = FileUtils.listFiles(root, HiddenFileFilter.VISIBLE,
                HiddenFileFilter.VISIBLE);
        for (File child : children) {
            String key = S3Utils.getKeyFromFile(root, child);
            String checksum = checksumTable.get(key);
            if (checksum == null) {
                // This file does not exist in the S3 repository yet
                commitFile(key, child);
            } else {
                if (commitTimeTable.containsKey(key) && child.lastModified() > commitTimeTable.get(key)) {
                    // Local copy has been updated since the last commit
                    commitFile(key, child);
                } else {
                    commitTimeTable.put(key, child.lastModified());
                }
            }

            existingFiles.add(key);
        }

        // Remove any objects which are no longer used (Mark & Sweep method)
        if (log.isTraceEnabled()) {
            log.trace("Retrieving object list for deletable file identification");
        }
        ObjectListing objectListing = s3Client.listObjects(bucket.getName(),
                S3Constants.OBJECT_KEY_PREFIX);
        while (true) {
            for (S3ObjectSummary summary : objectListing.getObjectSummaries()) {
                String key = summary.getKey();
                if (!existingFiles.contains(key)) {
                    deletableFiles.add(key);
                }
            }

            if (objectListing.isTruncated()) {
                if (log.isTraceEnabled()) {
                    log.trace("Retrieving next batch of object list");
                }
                objectListing = s3Client.listNextBatchOfObjects(objectListing);
            } else {
                break;
            }
        }

        for (String key : deletableFiles) {
            if (log.isTraceEnabled()) {
                log.trace("Deleting object: " + key);
            }
            s3Client.deleteObject(bucket.getName(), key);
            checksumTable.remove(key);
            checkoutTimeTable.remove(key);
            commitTimeTable.remove(key);
        }
    }

    public void checkout(String filePath) throws DeploymentSynchronizerException {
        if (log.isTraceEnabled()) {
            log.trace("Retrieving object list for checkout");
        }
        File root = new File(filePath);
        ObjectListing objectListing = s3Client.listObjects(bucket.getName(),
                S3Constants.OBJECT_KEY_PREFIX);
        List<String> existingKeys = new ArrayList<String>();
        List<String> deletableKeys = new ArrayList<String>();

        while (true) {
            for (S3ObjectSummary summary : objectListing.getObjectSummaries()) {
                String key = summary.getKey();
                existingKeys.add(key);
                String checksum = checksumTable.get(key);
                if (checksum == null) {
                    // we haven't heard of this file before - let's check it out
                    checkoutFile(root, key);
                } else if (!checksum.equals(summary.getETag())) {
                    File file = S3Utils.getFileFromKey(root, key);
                    if (file.exists() && file.lastModified() == checkoutTimeTable.get(key)) {
                        // Copy in S3 has been updated - Need to checkout the changes
                        checkoutFile(root, key);
                    }
                    // If the file does not exist locally, it will be removed from S3 during
                    // the next commit
                }
            }

            if (objectListing.isTruncated()) {
                if (log.isTraceEnabled()) {
                    log.trace("Retrieving next batch of object list");
                }
                objectListing = s3Client.listNextBatchOfObjects(objectListing);
            } else {
                break;
            }
        }

        for (String key : checksumTable.keySet()) {
            if (!existingKeys.contains(key)) {
                deletableKeys.add(key);
            }
        }

        for (String key : deletableKeys) {
            File file = S3Utils.getFileFromKey(root, key);
            if (file.exists()) {
                if (log.isDebugEnabled()) {
                    log.debug("Deleting the file: " + file.getPath() + " from the local copy");
                }
                FileUtils.deleteQuietly(file);
                checksumTable.remove(key);
                checkoutTimeTable.remove(key);
                commitTimeTable.remove(key);
            }
        }
    }

    public void initAutoCheckout(boolean useEventing) throws DeploymentSynchronizerException {

    }

    public void cleanupAutoCheckout() {

    }

    private void commitFile(String key, File file) {
        if (log.isDebugEnabled()) {
            log.debug("Committing the file: " + file.getPath() + " to the S3 store");
        }
        PutObjectResult result = s3Client.putObject(bucket.getName(), key, file);
        checksumTable.put(key, result.getETag());
        commitTimeTable.put(key, file.lastModified());
    }

    private void checkoutFile(File root, String key) {
        if (log.isDebugEnabled()) {
            log.debug("Checking out the object: " + key + " from the S3 store");
        }
        File targetFile = S3Utils.getFileFromKey(root, key);
        ObjectMetadata metadata = s3Client.getObject(new GetObjectRequest(
                bucket.getName(), key), targetFile);
        checksumTable.put(key, metadata.getETag());
        checkoutTimeTable.put(key, targetFile.lastModified());
    }

    private void handleException(String msg) throws DeploymentSynchronizerException {
        log.error(msg);
        throw new DeploymentSynchronizerException(msg);
    }

}
