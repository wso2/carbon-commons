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

/**
 * Behavior:
 *  - preserves unrelated lines and comments
 *  - detects keys of the form "appender.<APPENDER_NAME>.*"
 *  - replaces existing keys' values
 *  - inserts missing keys contiguously inside the appender block (or after last appender.*)
 *  - can remove all existing appender.<APPENDER>.* keys (useful for reset)
 *  - writes atomically using a temp file and ATOMIC_MOVE (falls back to non-atomic rename when not supported)
 */
public final class Log4j2PropertiesEditor {

    private static final String APPENDER_PREFIX = "appender.";
    private static final String KV_SEPARATOR = "=";

    private Log4j2PropertiesEditor() {
        throw new AssertionError("No instances of Log4j2PropertiesEditor");
    }

    /**
     * Read the file as raw lines using UTF-8.
     */
    public static List<String> readAllLines(File file) throws IOException {
        if (!file.exists()) {
            return new ArrayList<>();
        }
        return Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
    }

    /**
     * Return a list of keys present for the given appender
     */
    public static ArrayList<String> getKeysOfAppender(File file, String appenderName) throws IOException {
        List<String> lines = readAllLines(file);
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
        List<String> lines = readAllLines(file);
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
        List<String> lines = readAllLines(file);
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
     *
     * If removeExistingAppenderKeys == true, all existing lines that belong to this appender
     * will be removed before inserting newProps.
     *
     * If false, existing keys that match newProps are updated, missing ones are inserted,
     * and unrelated lines are preserved.
     */
    public static void writeUpdatedAppender(File file,
                                            String appenderName,
                                            Map<String, String> newProps,
                                            boolean removeExistingAppenderKeys) throws IOException {

        List<String> lines = readAllLines(file);
        if (lines == null) {
            lines = new ArrayList<>();
        }

        String targetPrefix = APPENDER_PREFIX + appenderName + ".";
        // 1) build index maps for existing appender-related keys and for all appender.* keys
        Map<String, Integer> existingAppenderKeyLineIdx = new LinkedHashMap<>();
        Map<String, Integer> allAppenderKeyLineIdx = new LinkedHashMap<>();
        for (int i = 0; i < lines.size(); i++) {
            String raw = lines.get(i);
            String trimmed = raw.trim();
            if (trimmed.isEmpty() || isCommentLine(trimmed)) {
                continue;
            }
            int sep = findKeyValueSeparator(trimmed);
            if (sep <= 0) {
                continue;
            }
            String key = trimmed.substring(0, sep).trim();
            if (key.startsWith(APPENDER_PREFIX)) {
                allAppenderKeyLineIdx.put(key, i);
                if (key.startsWith(targetPrefix)) {
                    existingAppenderKeyLineIdx.put(key, i);
                }
            }
        }

        // 2) compute insertion index
        int insertionIndex = -1;
        if (!existingAppenderKeyLineIdx.isEmpty()) {
            // Insert after last existing key for this appender
            insertionIndex = Collections.max(existingAppenderKeyLineIdx.values()) + 1;
        } else if (!allAppenderKeyLineIdx.isEmpty()) {
            // no keys for this appender; insert after last "appender.*" line
            insertionIndex = Collections.max(allAppenderKeyLineIdx.values()) + 1;
        } else {
            // no appender.* keys at all; append at end of file
            insertionIndex = lines.size();
        }

        // 3) If removeExistingAppenderKeys, remove all existing lines for this appender
        if (removeExistingAppenderKeys && !existingAppenderKeyLineIdx.isEmpty()) {
            // Remove by building a new list skipping those indices
            List<String> newLines = new ArrayList<>();
            for (int i = 0; i < lines.size(); i++) {
                if (!existingAppenderKeyLineIdx.containsValue(i)) {
                    newLines.add(lines.get(i));
                }
            }
            lines = newLines;
            // Recompute insertionIndex: if any earlier lines removed before old insertionIndex, adjust.
            // It's simpler to recompute insertionIndex: find last appender.* now
            int newLastAppenderIdx = -1;
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
                if (key.startsWith(APPENDER_PREFIX)) {
                    newLastAppenderIdx = i;
                }
            }
            insertionIndex = (newLastAppenderIdx >= 0) ? newLastAppenderIdx + 1 : lines.size();
        }

        // 4) Update existing keys where applicable (only if not removing them)
        // Note: when removeExistingAppenderKeys==true we already removed old lines; updates are not needed.
        if (!removeExistingAppenderKeys && !existingAppenderKeyLineIdx.isEmpty()) {
            for (Map.Entry<String, String> e : newProps.entrySet()) {
                String key = e.getKey();
                String value = safeToString(e.getValue());
                Integer idx = existingAppenderKeyLineIdx.get(key);
                if (idx != null && idx >= 0 && idx < lines.size()) {
                    // Replace the entire line with "key = value" (preserve key exactly)
                    lines.set(idx, key + " = " + value);
                }
            }
        }

        // 5) Determine which props are missing and prepare insertion list
        List<String> toInsert = new ArrayList<>();
        for (Map.Entry<String, String> e : newProps.entrySet()) {
            String key = e.getKey();
            String value = safeToString(e.getValue());
            boolean exists = (!removeExistingAppenderKeys && existingAppenderKeyLineIdx.containsKey(key))
                    || (removeExistingAppenderKeys && false); // removed earlier
            if (!exists) {
                toInsert.add(key + " = " + value);
            }
        }

        // 6) Insert missing lines at insertionIndex preserving order given by newProps iteration order
        if (!toInsert.isEmpty()) {
            // Insert in batch
            List<String> before = new ArrayList<>();
            List<String> after = new ArrayList<>();
            for (int i = 0; i < insertionIndex && i < lines.size(); i++) {
                before.add(lines.get(i));
            }
            for (int i = insertionIndex; i < lines.size(); i++) {
                after.add(lines.get(i));
            }
            List<String> merged = new ArrayList<>(before.size() + toInsert.size() + after.size());
            merged.addAll(before);
            merged.addAll(toInsert);
            merged.addAll(after);
            lines = merged;
        }

        // 7) Write lines atomically
        writeLinesAtomically(file.toPath(), lines);
    }

    private static int findKeyValueSeparator(String line) {
        // support both '=' and ':'? In properties typically '=' or ':' but original code used '='
        int eq = line.indexOf(KV_SEPARATOR);
        if (eq >= 0) {
            return eq;
        }
        return -1;
    }

    private static boolean isCommentLine(String trimmed) {
        return trimmed.startsWith("#") || trimmed.startsWith("!");
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
