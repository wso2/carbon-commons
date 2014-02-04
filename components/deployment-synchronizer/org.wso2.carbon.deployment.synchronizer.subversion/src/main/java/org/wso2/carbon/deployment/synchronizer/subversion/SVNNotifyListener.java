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

package org.wso2.carbon.deployment.synchronizer.subversion;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.ISVNProgressListener;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNProgressEvent;
import org.wso2.carbon.deployment.synchronizer.subversion.util.SVNFileChecksumResolverUtil;

import java.io.File;

/**
 * Logs and traces the SVN notifications, messages and other runtime debug information
 */
public class SVNNotifyListener implements ISVNNotifyListener, ISVNProgressListener {

    private static final Log log = LogFactory.getLog(SVNNotifyListener.class);

    public void setCommand(int i) {

    }

    public void logCommandLine(String s) {
        log.debug(s);
    }

    public void logMessage(String s) {
        log.trace(s);
    }

    public void logError(String s) {
        log.debug(s);
        SVNFileChecksumResolverUtil.resolveChecksum(s);
    }

    public void logRevision(long l, String s) {

    }

    public void logCompleted(String s) {
        log.debug(s);
    }

    public void onNotify(File file, SVNNodeKind svnNodeKind) {
        if (log.isTraceEnabled()) {
            log.trace("Status of the file: " + file.getPath() + " has changed");
        }
    }

    public void onProgress(SVNProgressEvent svnProgressEvent) {
        if (log.isTraceEnabled()) {
            log.trace("Bytes transferred: " + svnProgressEvent.getProgress());
        }
    }
}
