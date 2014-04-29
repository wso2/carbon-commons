/*
* Copyright 2004,2013 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.discovery.cxf;

import org.apache.catalina.core.StandardContext;
import org.wso2.carbon.discovery.cxf.util.CarbonAnnotationDB;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class APIScanner {
/*
    public static void scan(StandardContext context) {

        Set<String> entityClasses = getAnnotatedClasses(context, Path.class);
        for (String className : entityClasses) {
            try {
                Class<?> clazz = Class.forName(className);
                showAPIinfo(context, clazz);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    */
    public static CarbonAnnotationDB getAnnotatedClasses(StandardContext context,
                                            Class<?> annotation) {

        CarbonAnnotationDB db = new CarbonAnnotationDB();
        db.addIgnoredPackages("org.apache");
        db.addIgnoredPackages("org.codehaus");
        db.addIgnoredPackages("org.springframework");

        final String path = context.getRealPath("/WEB-INF/classes");
        URL resourceUrl = null;
        URL[] urls = null;

        if (path != null) {
            final File fp = new File(path);
            if (fp.exists()) {
                try {
                    resourceUrl = fp.toURI().toURL();
                    urls = new URL[] { new URL(resourceUrl.toExternalForm()) };
                    db.scanArchives(urls);
                    return db;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return db;
    }

}