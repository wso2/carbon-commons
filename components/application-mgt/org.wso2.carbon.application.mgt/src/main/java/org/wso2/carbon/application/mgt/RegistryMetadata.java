package org.wso2.carbon.application.mgt;


public class RegistryMetadata {

    String artifactName;
    String[] resources;
    String[] dumps;
    String[] collections;
    Association[] associations;

    public String[] getResources() {
        return resources;
    }

    public void setResources(String[] resources) {
        this.resources = resources;
    }

    public String[] getDumps() {
        return dumps;
    }

    public void setDumps(String[] dumps) {
        this.dumps = dumps;
    }

    public String[] getCollections() {
        return collections;
    }

    public void setCollections(String[] collections) {
        this.collections = collections;
    }

    public Association[] getAssociations() {
        return associations;
    }

    public void setAssociations(Association[] associations) {
        this.associations = associations;
    }

    public String getArtifactName() {
        return artifactName;
    }

    public void setArtifactName(String artifactName) {
        this.artifactName = artifactName;
    }
}
