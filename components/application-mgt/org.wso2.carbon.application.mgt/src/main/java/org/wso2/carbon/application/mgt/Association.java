package org.wso2.carbon.application.mgt;

public class Association {

    private String sourcePath;
    private String targetPath;

    public Association(String sourcePath, String targetPath) {
        this.sourcePath = sourcePath;
        this.targetPath = targetPath;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public String getTargetPath() {
        return targetPath;
    }

}
