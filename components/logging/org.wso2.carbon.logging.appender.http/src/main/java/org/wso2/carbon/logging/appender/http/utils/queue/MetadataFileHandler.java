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

    public void addProperty(String key, String value) {

       metadata.addProperty(key, value);
        saveMetadataToFile();
    }

    public String readMetadata(String key) {

       if (metadata.has(key)) {
           return metadata.get(key).getAsString();
        }
        return null;
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

    public void addLong(String key, long value) {

       metadata.addProperty(key, value);
        saveMetadataToFile();
    }

    public boolean isInitialized() {

       return isInitialized;
    }

    public void close() {

       saveMetadataToFile();
    }

    public void deleteFile() throws PersistentQueueException {

       if(!jsonFile.delete()) {
           throw new PersistentQueueException("Error: Unable to delete metadata file");
        }
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

               e.printStackTrace();
                //todo: Handle exceptions appropriately
            }
        }
       else{
           try {
               Files.createDirectories(Paths.get(jsonFile.getParent()));
               if(!jsonFile.createNewFile()){
                   throw new PersistentQueueException("Error: Unable to create metadata file");
               }
           } catch (IOException e) {
               throw new PersistentQueueException("Error: Unable to create metadata file", e);
           }
       }
    }
}
