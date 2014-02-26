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

package org.wso2.carbon.databridge.commons.utils;


import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;

import java.util.ArrayList;
import java.util.List;

/**
 * the util class that converts Events and its definitions in to various forms
 */
public final class EventDefinitionConverterUtils {
    public final static String nullString = "_null";
    private static Gson gson = new Gson();

    private EventDefinitionConverterUtils() {

    }

    public static AttributeType[] generateAttributeTypeArray(List<Attribute> attributes) {
        if (attributes != null) {
            AttributeType[] attributeTypes = new AttributeType[attributes.size()];
            for (int i = 0, metaDataSize = attributes.size(); i < metaDataSize; i++) {
                Attribute attribute = attributes.get(i);
                attributeTypes[i] = attribute.getType();
            }
            return attributeTypes;
        } else {
            return null;  //to improve performance
        }
    }


    public static StreamDefinition convertFromJson(String streamDefinition)
            throws MalformedStreamDefinitionException {
        try {
            StreamDefinition tempStreamDefinition = gson.fromJson(streamDefinition.
                    replaceAll("('|\")type('|\")\\W*:\\W*('|\")(?i)int('|\")", "'type':'INT'").replaceAll("('|\")type('|\")\\W*:\\W*('|\")(?i)long('|\")", "'type':'LONG'").
                    replaceAll("('|\")type('|\")\\W*:\\W*('|\")(?i)float('|\")", "'type':'FLOAT'").replaceAll("('|\")type('|\")\\W*:\\W*('|\")(?i)double('|\")", "'type':'DOUBLE'").
                    replaceAll("('|\")type('|\")\\W*:\\W*('|\")(?i)bool('|\")", "'type':'BOOL'").replaceAll("('|\")type('|\")\\W*:\\W*('|\")(?i)string('|\")", "'type':'STRING'"), StreamDefinition.class);

            String name = tempStreamDefinition.getName();
            String version = tempStreamDefinition.getVersion();


            if (version == null) {
                version = "1.0.0";  //when populating the object using google gson the defaults are getting null values
            }
            if (name == null) {
                throw new MalformedStreamDefinitionException("stream name is null");
            }

            StreamDefinition newStreamDefinition = new StreamDefinition(name, version);

            newStreamDefinition.setTags(tempStreamDefinition.getTags());
            List<Attribute> metaList = tempStreamDefinition.getMetaData();
            if (metaList != null && metaList.size() > 0) {
                newStreamDefinition.setMetaData(metaList);
            }
            List<Attribute> correlationList = tempStreamDefinition.getCorrelationData();
            if (correlationList != null && correlationList.size() > 0) {
                newStreamDefinition.setCorrelationData(correlationList);
            }
            List<Attribute> payloadList = tempStreamDefinition.getPayloadData();
            if (payloadList != null && payloadList.size() > 0) {
                newStreamDefinition.setPayloadData(payloadList);
            }

            newStreamDefinition.setNickName(tempStreamDefinition.getNickName());
            newStreamDefinition.setDescription(tempStreamDefinition.getDescription());
            newStreamDefinition.setDescription(tempStreamDefinition.getDescription());
            newStreamDefinition.setTags(tempStreamDefinition.getTags());
            return newStreamDefinition;
        } catch (RuntimeException e) {
            throw new MalformedStreamDefinitionException(" Malformed stream definition " + streamDefinition, e);
        }
    }

    public static String convertToJson(List<StreamDefinition> existingDefinitions) {
        JSONArray jsonDefnArray = new JSONArray();
        for (StreamDefinition existingDefinition : existingDefinitions) {
            jsonDefnArray.put(convertToJson(existingDefinition));
        }

        return gson.toJson(existingDefinitions);
    }

    public static List<StreamDefinition> convertMultipleEventDefns(String jsonArrayOfEventDefns)
            throws MalformedStreamDefinitionException {
        try {
            JSONArray jsonArray = new JSONArray(jsonArrayOfEventDefns);
            List<StreamDefinition> streamDefinitions = new ArrayList<StreamDefinition>();
            for (int i = 0; i < jsonArray.length(); i++) {
                streamDefinitions.add(convertFromJson(jsonArray.getString(i)));
            }
            return streamDefinitions;
        } catch (JSONException e) {
            throw new MalformedStreamDefinitionException(" Malformed stream definition " + jsonArrayOfEventDefns, e);
        }

    }

    public static String convertToJson(StreamDefinition existingDefinition) {
        return gson.toJson(existingDefinition);
    }
}
