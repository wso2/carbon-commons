/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.identity.entitlement.proxy;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Iterator;


public class XACMLRequetBuilder {

    private static Log log = LogFactory.getLog(XACMLRequetBuilder.class);

    public static String buildXACML3Request(Attribute[] attributes) {

        OMFactory factory = OMAbstractFactory.getOMFactory();

        OMElement requestXML = factory.createOMElement("Request", null);
        requestXML.addAttribute("xlmns", "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17", null);
        requestXML.addAttribute("CombinedDecision", "false", null);
        requestXML.addAttribute("ReturnPolicyIdList", "false", null);

        HashSet<String> catagorySet = new HashSet<String>();
        for (Attribute attribute : attributes) {
            if (!catagorySet.contains(attribute.getCategory())) {
                catagorySet.add(attribute.getCategory());
                OMElement attributesXML = factory.createOMElement("Attributes", null);
                attributesXML.addAttribute("Category", attribute.getCategory(), null);

                HashSet<String> attributeSet = new HashSet<String>();
                if (!attributeSet.contains(attribute.getId())) {
                    attributeSet.add(attribute.getId());
                    OMElement attributeXML = factory.createOMElement("Attribute", null);
                    attributeXML.addAttribute("AttributeId", attribute.getId(), null);
                    attributeXML.addAttribute("IncludeInResult", "false", null);

                    OMElement attributeValueXML = factory.createOMElement("AttributeValue", null);
                    attributeValueXML.addAttribute("DataType", "http://www.w3.org/2001/XMLSchema#" + attribute.getType(), null);
                    attributeValueXML.setText(attribute.getValue());
                    attributeXML.addChild(attributeValueXML);
                    attributesXML.addChild(attributeXML);
                } else {
                    Iterator itr = attributesXML.getChildElements();
                    while (itr.hasNext()) {
                        OMElement attributeXML = (OMElement) itr.next();
                        if (attribute.getId().equals(attributeXML.getAttributeValue(new QName("AttributeId")))) {
                            OMElement attributeValueXML = factory.createOMElement("AttributeValue", null);
                            attributeValueXML.addAttribute("DataType", "http://www.w3.org/2001/XMLSchema#" + attribute.getType(), null);
                            attributeValueXML.setText(attribute.getValue());
                            attributeXML.addChild(attributeValueXML);
                            break;
                        }
                    }
                }
                requestXML.addChild(attributesXML);
            } else {
                Iterator itr = requestXML.getChildElements();
                while (itr.hasNext()) {
                    OMElement attributesXML = (OMElement) itr.next();
                    if (attribute.getCategory().equals(attributesXML.getAttributeValue(new QName("Category")))) {
                        HashSet<String> attributeSet = new HashSet<String>();
                        Iterator itr1 = attributesXML.getChildElements();
                        while (itr1.hasNext()) {
                            attributeSet.add(((OMElement) itr1.next()).getAttributeValue(new QName("AttributeId")));
                        }

                        if (!attributeSet.contains(attribute.getId())) {
                            attributeSet.add(attribute.getId());
                            OMElement attributeXML = factory.createOMElement("Attribute", null);
                            attributeXML.addAttribute("AttributeId", attribute.getId(), null);
                            attributeXML.addAttribute("IncludeInResult", "false", null);

                            OMElement attributeValueXML = factory.createOMElement("AttributeValue", null);
                            attributeValueXML.addAttribute("DataType", "http://www.w3.org/2001/XMLSchema#" + attribute.getType(), null);
                            attributeValueXML.setText(attribute.getValue());
                            attributeXML.addChild(attributeValueXML);
                            attributesXML.addChild(attributeXML);
                        } else {
                            Iterator itr2 = attributesXML.getChildElements();
                            while (itr2.hasNext()) {
                                OMElement attributeXML = (OMElement) itr2.next();
                                if (attribute.getId().equals(attributeXML.getAttributeValue(new QName("AttributeId")))) {
                                    OMElement attributeValueXML = factory.createOMElement("AttributeValue", null);
                                    attributeValueXML.addAttribute("DataType", "http://www.w3.org/2001/XMLSchema#" + attribute.getType(), null);
                                    attributeValueXML.setText(attribute.getValue());
                                    attributeXML.addChild(attributeValueXML);
                                    break;
                                }
                            }
                        }
                        break;
                    }

                }
            }

        }
        return requestXML.toString();
    }

}
