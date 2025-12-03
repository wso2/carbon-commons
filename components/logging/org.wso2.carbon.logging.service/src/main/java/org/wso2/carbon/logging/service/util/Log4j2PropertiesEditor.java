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
        return "Property not found";
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

        // Record original block position before any edits
        int originalFirstIdx = -1;
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
                if (originalFirstIdx == -1) {
                    originalFirstIdx = i;
                }
            }
        }

        if (!merge) {
            // Replace: remove the whole block; we will insert back at originalFirstIdx
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
            // Merge: update existing keys in place, remove ones marked to be removed
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

            List<Integer> linesToRemove = new ArrayList<>();
            for (Map.Entry<String, String> entry : newProps.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (existingKeys.containsKey(key)) {
                    int lineIndex = existingKeys.get(key);
                    if (value == null || "__REMOVE__".equals(value)) {
                        linesToRemove.add(lineIndex);
                    } else {
                        lines.set(lineIndex, key + " = " + safeToString(value));
                    }
                }
            }
            linesToRemove.sort(Collections.reverseOrder());
            for (int index : linesToRemove) {
                lines.remove(index);
            }

            // Only insert truly new keys
            newProps.entrySet().removeIf(e ->
                    e.getValue() == null ||
                            "__REMOVE__".equals(e.getValue()) ||
                            existingKeys.containsKey(e.getKey())
            );
        }

        // Recompute current last index of the block after edits (for merge)
        int currentLastIdx = -1;
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
                currentLastIdx = i;
            }
        }

        // Decide insertion index to keep the block in-place
        int insertionIndex;
        if (merge) {
            if (currentLastIdx != -1) {
                insertionIndex = currentLastIdx + 1; // append after existing keys of this appender
            } else {
                insertionIndex = getInsertionIndex(lines, originalFirstIdx);
            }
        } else {
            // Replace: insert exactly where the block originally started if we had one
            insertionIndex = getInsertionIndex(lines, originalFirstIdx);
        }

        List<String> toInsert = new ArrayList<>();
        for (Map.Entry<String, String> e : newProps.entrySet()) {
            toInsert.add(e.getKey() + " = " + safeToString(e.getValue()));
        }

        lines.addAll(insertionIndex, toInsert);
        writeLinesAtomically(file.toPath(), lines);
    }

    private static int getInsertionIndex(ArrayList<String> lines, int originalFirstIdx) {
        int insertionIndex;
        if (originalFirstIdx != -1) {
            insertionIndex = originalFirstIdx;   // original spot where the block used to be
        } else {
            // Fallback: after last appender.* line
            insertionIndex = lines.size();
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
        }
        return insertionIndex;
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