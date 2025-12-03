package org.wso2.carbon.logging.service.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.apache.catalina.ha.tcp.SimpleTcpCluster.log;

public final class Log4j2PropertiesEditor {

    private static final String APPENDER_PREFIX = "appender.";
    private static final String KV_SEPARATOR = "=";

    private Log4j2PropertiesEditor() {
        throw new AssertionError("No instances of Log4j2PropertiesEditor");
    }

    /**
     * Read the file as raw lines using UTF-8.
     */
    public static ArrayList<String> readAllLines(File file) throws IOException {
        return (ArrayList<String>) Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
    }

    /**
     * Return a list of keys present for the given appender
     */
    public static ArrayList<String> getKeysOfAppender(File file, String appenderName) throws IOException {
        ArrayList<String> lines = readAllLines(file);
        ArrayList<String> keys = new ArrayList<>();
        String prefix = APPENDER_PREFIX + appenderName + ".";
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (isCommentLine(line)) {
                continue;
            }
            int idx = findKeyValueSeparator(line);
            if (idx <= 0) {
                continue;
            }
            String key = line.substring(0, idx).trim();
            if (key.startsWith(prefix)) {
                keys.add(key);
            }
        }
        return keys;
    }

    /**
     * Return a map of key -> value for all keys belonging to the given appender.
     */
    public static Map<String, String> getKeyValuesOfAppender(File file, String appenderName) throws IOException {
        ArrayList<String> lines = readAllLines(file);
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        String prefix = APPENDER_PREFIX + appenderName + ".";
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || isCommentLine(line)) {
                continue;
            }
            int idx = findKeyValueSeparator(line);
            if (idx <= 0) {
                continue;
            }
            String key = line.substring(0, idx).trim();
            if (key.startsWith(prefix)) {
                String value = line.substring(idx + 1).trim();
                map.put(key, value);
            }
        }
        return map;
    }

    /**
     * Return the property value for a given key (first occurrence) or null if not found.
     */
    public static String getProperty(File file, String key) throws IOException {
        ArrayList<String> lines = readAllLines(file);
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || isCommentLine(line)) {
                continue;
            }
            int idx = findKeyValueSeparator(line);
            if (idx <= 0) {
                continue;
            }
            String k = line.substring(0, idx).trim();
            if (k.equals(key)) {
                return line.substring(idx + 1).trim();
            }
        }
        return null;
    }

    /**
     * Update the properties for the given appenderName using newProps.
     */
    public static void writeUpdatedAppender(File file,
                                            String appenderName,
                                            Map<String, String> newProps,
                                            boolean merge) throws IOException {

        ArrayList<String> lines = readAllLines(file);
        String targetPrefix = APPENDER_PREFIX + appenderName + ".";

        if (!merge) {
            // Original behavior: remove all existing lines for this appender
            lines.removeIf(line -> {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || isCommentLine(trimmed)) {
                    return false;
                }
                int sep = findKeyValueSeparator(trimmed);
                if (sep <= 0) {
                    return false;
                }
                String key = trimmed.substring(0, sep).trim();
                return key.startsWith(targetPrefix);
            });
        } else {
            // Merge behavior: update existing keys, keep others, handle removals
            Map<String, Integer> existingKeys = new LinkedHashMap<>();
            for (int i = 0; i < lines.size(); i++) {
                String trimmed = lines.get(i).trim();
                if (trimmed.isEmpty() || isCommentLine(trimmed)) {
                    continue;
                }
                int sep = findKeyValueSeparator(trimmed);
                if (sep <= 0) {
                    continue;
                }
                String key = trimmed.substring(0, sep).trim();
                if (key.startsWith(targetPrefix)) {
                    existingKeys.put(key, i);
                }
            }

            // Track lines to remove (for null values or properties to delete)
            List<Integer> linesToRemove = new ArrayList<>();

            // Update existing properties or mark for deletion
            for (Map.Entry<String, String> entry : newProps.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (existingKeys.containsKey(key)) {
                    int lineIndex = existingKeys.get(key);
                    if (value == null || "__REMOVE__".equals(value)) {
                        // Mark line for removal
                        linesToRemove.add(lineIndex);
                    } else {
                        lines.set(lineIndex, key + " = " + safeToString(value));
                    }
                }
            }

            // Remove lines in reverse order to maintain indices
            Collections.sort(linesToRemove, Collections.reverseOrder());
            for (int index : linesToRemove) {
                lines.remove(index);
            }

            // Remove updated/deleted keys from newProps so we only insert new ones
            newProps.entrySet().removeIf(e ->
                    e.getValue() == null ||
                            "__REMOVE__".equals(e.getValue()) ||
                            existingKeys.containsKey(e.getKey())
            );
        }

        // Find insertion point (after last appender.* line)
        int insertionIndex = -1;
        for (int i = lines.size() - 1; i >= 0; i--) {
            String trimmed = lines.get(i).trim();
            if (trimmed.isEmpty() || isCommentLine(trimmed)) {
                continue;
            }
            int sep = findKeyValueSeparator(trimmed);
            if (sep <= 0) {
                continue;
            }
            String key = trimmed.substring(0, sep).trim();
            if (key.startsWith(APPENDER_PREFIX)) {
                insertionIndex = i + 1;
                break;
            }
        }

        if (insertionIndex < 0) {
            insertionIndex = lines.size();
        }

        // Insert new properties
        List<String> toInsert = new ArrayList<>();
        for (Map.Entry<String, String> e : newProps.entrySet()) {
            toInsert.add(e.getKey() + " = " + safeToString(e.getValue()));
        }

        lines.addAll(insertionIndex, toInsert);

        // Write atomically
        writeLinesAtomically(file.toPath(), lines);
    }




    private static int findKeyValueSeparator(String line) {
        int eq = line.indexOf(KV_SEPARATOR);
        if (eq >= 0) {
            return eq;
        }
        return -1;
    }

    private static boolean isCommentLine(String trimmed) {
        return trimmed.startsWith("#");
    }

    private static String safeToString(Object o) {
        return o == null ? "" : o.toString();
    }

    /**
     * Write lines to a temporary file and move into place using ATOMIC_MOVE, with fallback.
     */
    private static void writeLinesAtomically(Path target, List<String> lines) throws IOException {
        Path parent = target.getParent();
        if (parent == null) {
            throw new IOException("Target path has no parent: " + target);
        }
        Path tmp = Files.createTempFile(parent, "log4j2.properties.", ".tmp");
        try (BufferedWriter w = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
            for (int i = 0; i < lines.size(); i++) {
                w.write(lines.get(i));
                // keep unix newline
                if (i < lines.size() - 1) {
                    w.newLine();
                }
            }
        }

        try {
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException amnse) {
            // fallback to non-atomic move
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}