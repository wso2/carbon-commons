package org.wso2.carbon.deployment.synchronizer.git.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * String Utility methods
 */
public class FileUtilities {

    private static final Log log = LogFactory.getLog(FileUtilities.class);

    /**
     * Filter and clean files in the directory
     *
     * @param direcotry directory to clean
     */
    public static void filterFiles (File direcotry) {

        if(direcotry.isDirectory()) {
            Collection<File> files = org.apache.commons.io.FileUtils.listFiles(direcotry, new String[]{".svn"}, true);
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolderStructure(file);
                }
            }
        }
    }

    /**
     * Deletes a folder structure recursively
     *
     * @param existingDir folder to delete
     */
    public static void deleteFolderStructure (File existingDir) {

        try {
            org.apache.commons.io.FileUtils.deleteDirectory(existingDir);

        } catch (IOException e) {
            log.error("Deletion of existing non-git repository structure failed");
            e.printStackTrace();
        }
    }
}
