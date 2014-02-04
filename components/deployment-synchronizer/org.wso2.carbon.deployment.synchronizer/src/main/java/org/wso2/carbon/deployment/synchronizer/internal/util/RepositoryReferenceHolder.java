/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.deployment.synchronizer.internal.util;

import org.wso2.carbon.deployment.synchronizer.ArtifactRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RepositoryReferenceHolder {

    private static RepositoryReferenceHolder instance = new RepositoryReferenceHolder();

    private Map<ArtifactRepository, List<RepositoryConfigParameter>> repositories;

    private RepositoryReferenceHolder(){
          repositories = new HashMap<ArtifactRepository, List<RepositoryConfigParameter>>();
    }

    public static synchronized RepositoryReferenceHolder getInstance(){
        return instance;
    }
    
    public Map<ArtifactRepository, List<RepositoryConfigParameter>> getRepositories(){
        return repositories;
    }
    
    public void addRepository(ArtifactRepository repositoryType, List<RepositoryConfigParameter> parameters){
        repositories.put(repositoryType, parameters);
    }

    public void removeRepository(ArtifactRepository repositoryType){
        repositories.remove(repositoryType);
    }
    
    public ArtifactRepository getRepositoryByType(String repositoryType){
        if(repositoryType == null){
            return null;
        }
        
        for(ArtifactRepository repo : repositories.keySet()){
             if(repositoryType.equals(repo.getRepositoryType())){
                 return repo;
             }
        }
        return null;
    }
            
}
