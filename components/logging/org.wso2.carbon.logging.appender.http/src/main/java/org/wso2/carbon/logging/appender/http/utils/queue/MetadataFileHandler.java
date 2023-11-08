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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MetadataFileHandler {

    private final File jsonFile;
    private JsonObject metadata;
    private boolean isInitialized;

    public MetadataFileHandler(String filePath) throws PersistentQueueException {

        jsonFile = new File(filePath);
        metadata = new JsonObject();
        loadMetadataFromFile();
    }

    public JsonArray getAsJsonArray(String key) {

        if (metadata.has(key)) {
            return metadata.get(key).getAsJsonArray();
        }
        return null;
    }

    public void addJsonArray(String key, JsonArray value) {

        metadata.add(key, value);
        saveMetadataToFile();
    }

    public boolean isInitialized() {

        return isInitialized;
    }

    public void close() {

        saveMetadataToFile();
    }

    private void saveMetadataToFile() {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile, StandardCharsets.UTF_8))) {
            Gson gson = new Gson();
            String json = gson.toJson(metadata);
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle exceptions appropriately
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
                        "Error: Unable to read metadata file", e);
            }
        }
        else{
            try {
                Files.createDirectories(Paths.get(jsonFile.getParent()));
                if(!jsonFile.createNewFile()){
                    throw new PersistentQueueException(
                            PersistentQueueException.PersistentQueueErrorTypes.QUEUE_META_DATA_FILE_CREATION_FAILED, "Meta data file creation failed.");
                }
            } catch (IOException e) {
                throw new PersistentQueueException(
                        PersistentQueueException.PersistentQueueErrorTypes.QUEUE_META_DATA_FILE_CREATION_FAILED,
                        "Error: Unable to create metadata file", e);
            }
        }
    }
}
