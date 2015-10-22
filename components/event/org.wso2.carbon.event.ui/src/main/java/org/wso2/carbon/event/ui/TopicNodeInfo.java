package org.wso2.carbon.event.ui;

import org.wso2.carbon.event.stub.internal.xsd.TopicNode;


@Deprecated
public class TopicNodeInfo {
    private TopicNode topicNode;
    private String path;
    public TopicNodeInfo(TopicNode topicNode, String path) {
        super();
        this.topicNode = topicNode;
        this.path = path;
    }
    public TopicNode getTopicNode() {
        return topicNode;
    }
    public void setTopicNode(TopicNode topicNode) {
        this.topicNode = topicNode;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    
    
}
