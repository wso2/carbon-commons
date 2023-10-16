package org.wso2.carbon.logging.appender.http.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class MetadataFileHandler {
    private final File jsonFile;
    private JsonObject metadata;

    public MetadataFileHandler(String filePath) {
        jsonFile = new File(filePath);
        metadata = new JsonObject();
        loadMetadataFromFile();
    }

    public void writeMetadata(String key, String value) {
        metadata.addProperty(key, value);
        saveMetadataToFile();
    }

    public String readMetadata(String key) {
        if (metadata.has(key)) {
            return metadata.get(key).getAsString();
        }
        return null;
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

    private void loadMetadataFromFile() {
        if (jsonFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(jsonFile, StandardCharsets.UTF_8))) {
                String line;
                StringBuilder jsonContent = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line);
                }
                JsonParser parser = new JsonParser();
                metadata = parser.parse(jsonContent.toString()).getAsJsonObject();
            } catch (IOException e) {
                e.printStackTrace();
                // Handle exceptions appropriately
            }
        }
    }
}

