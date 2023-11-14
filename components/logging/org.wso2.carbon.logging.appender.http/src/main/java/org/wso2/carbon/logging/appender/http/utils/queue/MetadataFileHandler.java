/*
 *
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.logging.appender.http.utils.queue;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.wso2.carbon.logging.appender.http.utils.queue.exception.PersistentQueueException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * This class is used to handle the metadata file of the queue.
 */
public class MetadataFileHandler {

    private final File jsonFile;
    private JsonObject metadata;
    private boolean isInitialized;

    /**
     * Constructor of the MetadataFileHandler class.
     *
     * @param filePath path of the metadata file.
     * @throws PersistentQueueException if an error occurs while creating the metadata file.
     */
    public MetadataFileHandler(String filePath) throws PersistentQueueException {

        jsonFile = new File(filePath);
        metadata = new JsonObject();
        loadMetadataFromFile();
    }

    /**
     * Returns the value of the given key.
     *
     * @param key key of the value.
     * @return value of the given key.
     */
    public Optional<JsonArray> getAsJsonArray(String key) {

        if (metadata.has(key) && metadata.get(key).isJsonArray()) {
            return Optional.of(metadata.get(key).getAsJsonArray());
        }
        return Optional.empty();
    }

    /**
     * Adds an array to the metadata file.
     *
     * @param key key of the array.
     * @param value array to be added to metadata file.
     * @throws PersistentQueueException if an error occurs while adding the array to the metadata file.
     */
    public void addJsonArray(String key, JsonArray value) throws PersistentQueueException {

        metadata.add(key, value);
        saveMetadataToFile();
    }

    /**
     * Returns if the metadata file is initialized.
     *
     * @return boolean if the metadata file is initialized.
     */
    public boolean isInitialized() {

        return isInitialized;
    }

    /**
     * Sets if the metadata file is initialized.
     * @param isInitialized boolean if the metadata file is initialized.
     */
    public void setIsInitialized(boolean isInitialized) {

        this.isInitialized = isInitialized;
    }

    /**
     * Closes the metadata file.
     *
     * @throws PersistentQueueException if an error occurs while closing the metadata file.
     */
    public void close() throws PersistentQueueException {

        saveMetadataToFile();
    }

    private void saveMetadataToFile() throws PersistentQueueException {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile, StandardCharsets.UTF_8))) {
            Gson gson = new Gson();
            String json = gson.toJson(metadata);
            writer.write(json);
        } catch (IOException e) {
            throw new PersistentQueueException(
                    PersistentQueueException.PersistentQueueErrorTypes.QUEUE_META_DATA_FILE_READING_FAILED,
                    "Unable to read metadata file", e);
        }
    }

    private void loadMetadataFromFile() throws PersistentQueueException {

        if (jsonFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(jsonFile, StandardCharsets.UTF_8))) {
                String line;
                StringBuilder jsonContent = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line);
                }
                metadata = new JsonParser().parse(jsonContent.toString()).getAsJsonObject();
                isInitialized = true;
            } catch (IOException e) {
                throw new PersistentQueueException(
                        PersistentQueueException.PersistentQueueErrorTypes.QUEUE_META_DATA_FILE_READING_FAILED,
                        "Unable to read metadata file", e);
            }
        } else {
            try {
                Files.createDirectories(Paths.get(jsonFile.getParent()));
                if (!jsonFile.createNewFile()) {
                    throw new IOException("Unable to create metadata file");
                }
            } catch (IOException e) {
                throw new PersistentQueueException(
                        PersistentQueueException.PersistentQueueErrorTypes.QUEUE_META_DATA_FILE_CREATION_FAILED,
                        "Unable to create metadata file", e);
            }
        }
    }
}
