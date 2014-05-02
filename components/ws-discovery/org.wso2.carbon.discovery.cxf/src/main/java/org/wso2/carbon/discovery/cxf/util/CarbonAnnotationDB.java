package org.wso2.carbon.discovery.cxf.util;

import org.scannotation.AnnotationDB;

import java.util.Map;
import java.util.Set;

public class CarbonAnnotationDB extends AnnotationDB {

    public Map<String, Set<String>> getImplementsIndex() {
        return implementsIndex;
    }

}
