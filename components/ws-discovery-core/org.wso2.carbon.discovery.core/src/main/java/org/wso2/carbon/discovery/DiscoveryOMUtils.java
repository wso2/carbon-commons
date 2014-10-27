/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.discovery;

import org.wso2.carbon.discovery.messages.*;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMAttribute;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utilitiy methods for manipulating WS-Discovery message payloads and
 * data access objects
 */
public class DiscoveryOMUtils {

    private static final Log log = LogFactory.getLog(DiscoveryOMUtils.class);

    public static Notification getHelloFromOM(OMElement helloElement)
            throws DiscoveryException {

        validateTopElement(DiscoveryConstants.HELLO, helloElement);

        TargetService targetService = createService(helloElement, true);
        return new Notification(DiscoveryConstants.NOTIFICATION_TYPE_HELLO, targetService);
    }

    public static Notification getByeFromOM(OMElement byeElement)
            throws DiscoveryException {

        validateTopElement(DiscoveryConstants.BYE, byeElement);

        TargetService targetService = createService(byeElement, false);
        return new Notification(DiscoveryConstants.NOTIFICATION_TYPE_BYE, targetService);
    }

    public static Probe getProbeFromOM(OMElement probeElement) throws DiscoveryException {
        validateTopElement(DiscoveryConstants.PROBE, probeElement);

        Probe probe = new Probe();
        probe.setTypes(getTypes(probeElement));
        probe.setScopes(getScopes(probeElement));

        OMElement scopesElement = probeElement.getFirstChildWithName(DiscoveryConstants.SCOPES);
        if (scopesElement != null) {
            OMAttribute attr = scopesElement.getAttribute(DiscoveryConstants.ATTR_MATCH_BY);
            if (attr != null && attr.getAttributeValue() != null) {
                try {
                    probe.setMatchBy(new URI(attr.getAttributeValue()));
                } catch (URISyntaxException e) {
                    throw new DiscoveryException("Invalid URI value for the MatchBy attribute", e);
                }
            }
        }
        return probe;
    }

    public static Resolve getResolveFromOM(OMElement resolveElement) throws DiscoveryException {
        validateTopElement(DiscoveryConstants.RESOLVE, resolveElement);

        OMElement eprElement = getRequiredElement(resolveElement,
                AddressingConstants.Final.WSA_ENDPOINT_REFERENCE);
        EndpointReference epr = null;
        try {
            epr = EndpointReferenceHelper.fromOM(eprElement);
        } catch (AxisFault axisFault) {
            handleFault("Error while processing the endpoint reference", axisFault);
        }
        return new Resolve(epr);
    }

    public static QueryMatch getProbeMatchFromOM(OMElement matchElement) throws DiscoveryException {
        validateTopElement(DiscoveryConstants.PROBE_MATCHES, matchElement);

        Iterator matches = matchElement.getChildrenWithName(
                DiscoveryConstants.PROBE_MATCH);
        List<TargetService> services = new ArrayList<TargetService>();

        while (matches.hasNext()) {
            services.add(createService((OMElement) matches.next(), true));
        }

        return new QueryMatch(DiscoveryConstants.RESULT_TYPE_PROBE_MATCH, 
                services.toArray(new TargetService[services.size()]));
    }

    public static QueryMatch getResolveMatchFromOM(OMElement matchElement) throws DiscoveryException {
        validateTopElement(DiscoveryConstants.RESOLVE_MATCHES, matchElement);

        Iterator matches = matchElement.getChildrenWithName(
                DiscoveryConstants.RESOLVE_MATCH);
        List<TargetService> services = new ArrayList<TargetService>();

        while (matches.hasNext()) {
            if (services.size() == 0) {
                services.add(createService((OMElement) matches.next(), true));
            } else {
                throw new DiscoveryException("A ResolveMatch must not contain more than " +
                        "one target service descriprion");
            }
        }

        return new QueryMatch(DiscoveryConstants.RESULT_TYPE_RESOLVE_MATCH,
                services.toArray(new TargetService[1]));
    }

    private static TargetService createService(OMElement parent,
                                               boolean requireMetaVersion) throws DiscoveryException {
        OMElement eprElement = getRequiredElement(parent,
                AddressingConstants.Final.WSA_ENDPOINT_REFERENCE);
        EndpointReference epr = null;
        try {
            epr = EndpointReferenceHelper.fromOM(eprElement);
        } catch (AxisFault axisFault) {
            handleFault("Error while processing the endpoint reference in the " +
                    parent.getLocalName() + " message", axisFault);
        }

        TargetService targetService = new TargetService(epr);
        targetService.setTypes(getTypes(parent));
        targetService.setScopes(getScopes(parent));
        targetService.setXAddresses(getXAddresses(parent));

        OMElement metaVersionElement;
        if (requireMetaVersion) {
            metaVersionElement = getRequiredElement(parent, DiscoveryConstants.METADATA_VERSION);
        } else {
            metaVersionElement = parent.getFirstChildWithName(DiscoveryConstants.METADATA_VERSION);
        }

        if (metaVersionElement != null && metaVersionElement.getText() != null) {
            targetService.setMetadataVersion(Integer.parseInt(metaVersionElement.getText()));
        }
        return targetService;
    }

    public static OMElement toOM(Notification notification, OMFactory factory)
            throws DiscoveryException {

        OMElement topElement;
        if (DiscoveryConstants.NOTIFICATION_TYPE_HELLO == notification.getType()) {
            topElement = factory.createOMElement(DiscoveryConstants.HELLO);
        } else {
            topElement = factory.createOMElement(DiscoveryConstants.BYE);
        }

        return toOM(notification.getTargetService(), factory, topElement);
    }

    public static OMElement toOM(Probe probe, OMFactory factory) {
        OMElement topElement = factory.createOMElement(DiscoveryConstants.PROBE);
        if (probe.getTypes() != null && probe.getTypes().length > 0) {
            topElement.addChild(serializeQNamesList(probe.getTypes(), DiscoveryConstants.TYPES,
                    factory));
        }

        if (probe.getScopes() != null && probe.getScopes().length > 0) {
            OMElement scopesElement = serializeURIList(probe.getScopes(), DiscoveryConstants.SCOPES,
                    factory);
            if (!DiscoveryConstants.SCOPE_MATCH_RULE_DEAULT.equals(probe.getMatchBy().toString())) {
                scopesElement.addAttribute(factory.createOMAttribute(
                        DiscoveryConstants.ATTR_MATCH_BY.getLocalPart(), null,
                        probe.getMatchBy().toString()));
            }
            topElement.addChild(scopesElement);
        }

        return topElement;
    }

    public static OMElement toOM(Resolve resolve, OMFactory factory) throws DiscoveryException {
        if (resolve.getEpr() == null) {
            throw new DiscoveryException("The EPR of a Resolve request must not be null");
        }

        OMElement topElement = factory.createOMElement(DiscoveryConstants.RESOLVE);
        try {
            OMElement epr = EndpointReferenceHelper.toOM(factory, resolve.getEpr(),
                        AddressingConstants.Final.WSA_ENDPOINT_REFERENCE,
                        AddressingConstants.Final.WSA_NAMESPACE);
            topElement.addChild(epr);
        } catch (AxisFault axisFault) {
            handleFault("Error while serializing the Resolve request", axisFault);
        }

        return topElement;
    }

    public static OMElement toOM(QueryMatch match, OMFactory factory) throws DiscoveryException {
        OMElement topElement;
        QName childQName;
        if (DiscoveryConstants.RESULT_TYPE_PROBE_MATCH == match.getResultType()) {
            topElement = factory.createOMElement(DiscoveryConstants.PROBE_MATCHES);
            childQName = DiscoveryConstants.PROBE_MATCH;
        } else {
            topElement = factory.createOMElement(DiscoveryConstants.RESOLVE_MATCHES);
            childQName = DiscoveryConstants.RESOLVE_MATCH;
        }

        if (match.getTargetServices() != null) {
            for (TargetService service : match.getTargetServices()) {
                if (service != null) {
                    OMElement matchElement = factory.createOMElement(childQName);
                    topElement.addChild(toOM(service, factory, matchElement));
                }
            }
        }
        return topElement;
    }

    /**
     * Serializes the given TargetService instance into XML using the provided OMFactory.
     * The serialized XML content is attached to the provided parent element. Therefore
     * the parent element must not be null when invoking this method.
     *
     * @param service TargetService to be serialized
     * @param factory OMFactory to be used for creating XML objects
     * @param parent The parent element to which the TargetService will be attached
     * @return The parent element with the serialized TargetService content
     * @throws DiscoveryException If an error occurs while serializing XML content
     */
    public static OMElement toOM(TargetService service, OMFactory factory,
                                 OMElement parent) throws DiscoveryException {
        try {
            OMElement epr = EndpointReferenceHelper.toOM(factory, service.getEpr(),
                    AddressingConstants.Final.WSA_ENDPOINT_REFERENCE,
                    AddressingConstants.Final.WSA_NAMESPACE);
            parent.addChild(epr);

            if (service.getTypes() != null && service.getTypes().length > 0) {
                parent.addChild(serializeQNamesList(service.getTypes(), DiscoveryConstants.TYPES,
                        factory));
            }

            if (service.getScopes() != null && service.getScopes().length > 0) {
                parent.addChild(serializeURIList(service.getScopes(), DiscoveryConstants.SCOPES,
                        factory));
            }

            if (service.getXAddresses() != null && service.getXAddresses().length > 0) {
                parent.addChild(serializeURIList(service.getXAddresses(),
                        DiscoveryConstants.XADDRESSES, factory));
            }

            if (service.getMetadataVersion() != -1) {
                OMElement metaVersion = factory.createOMElement(DiscoveryConstants.METADATA_VERSION);
                metaVersion.setText(String.valueOf(service.getMetadataVersion()));
                parent.addChild(metaVersion);
            }

        } catch (AxisFault axisFault) {
            handleFault("Error while serializing the target service description " +
                    "into XML", axisFault);
        }

        return parent;
    }

    private static OMElement getRequiredElement(OMElement parent,
                                                QName qname) throws DiscoveryException {

        OMElement child = parent.getFirstChildWithName(qname);
        if (child == null) {
            String msg = "Required element " + qname + " was not found";
            log.error(msg);
            throw new DiscoveryException(msg);
        }
        return child;
    }

    private static void validateTopElement(QName expected,
                                           OMElement elt) throws DiscoveryException {

        QName found = elt.getQName();
        if (!expected.equals(found)) {
            String msg = "Invalid top level element in the " + expected.getLocalPart() + " " +
                    "message. Expected: " + expected + " but found: " + found;
            log.error(msg);
            throw new DiscoveryException(msg);
        }
    }

    private static QName[] getTypes(OMElement parent) throws DiscoveryException {
        OMElement typesElement = parent.getFirstChildWithName(DiscoveryConstants.TYPES);
        if (typesElement == null) {
            return null;
        }

        String typesList = typesElement.getText();
        if (typesList == null || "".equals(typesList)) {
            return null;
        }

        StringTokenizer st = new StringTokenizer(typesList.trim());
        List<QName> types = new ArrayList<QName>();
        while (st.hasMoreTokens()) {
            String type = st.nextToken();
            QName typeQName = typesElement.resolveQName(type);
            if (typeQName == null) {
                String msg = "Type " + type + " cannot be resolved to a valid QName";
                log.error(msg);
                throw new DiscoveryException(msg);
            }
            types.add(typeQName);
        }

        if (types.size() == 0) {
            return null;
        }

        return types.toArray(new QName[types.size()]);
    }

    private static URI[] getScopes(OMElement parent) throws DiscoveryException {
        OMElement scopesElement = parent.getFirstChildWithName(DiscoveryConstants.SCOPES);
        if (scopesElement == null) {
            return null;
        }

        String scopesList = scopesElement.getText();
        if (scopesList == null || "".equals(scopesList)) {
            return null;
        }

        return parseURIList(scopesList);
    }

    private static OMElement serializeURIList(URI[] scopes, QName topElement, OMFactory factory) {
        OMElement scopesElement = factory.createOMElement(topElement);
        StringBuffer scopesTxt = new StringBuffer();
        boolean firstEntry = true;
        for (URI scope : scopes) {
            if (!firstEntry) {
                scopesTxt.append(" ");
            }
            scopesTxt.append(scope.toString());
            firstEntry = false;
        }
        scopesElement.setText(scopesTxt.toString());
        return scopesElement;
    }

    private static OMElement serializeQNamesList(QName[] qnames, QName topElement,
                                                OMFactory factory) {

        OMElement wrapperElement = factory.createOMElement(topElement);
        StringBuffer qnamesTxt = new StringBuffer();
        boolean firstEntry = true;
        for (QName qname : qnames) {
            if (!firstEntry) {
                qnamesTxt.append(" ");
            }

            OMNamespace ns = wrapperElement.declareNamespace(qname.getNamespaceURI(),
                    qname.getPrefix());
            qnamesTxt.append(ns.getPrefix() + ":" + qname.getLocalPart());
            firstEntry = false;
        }
        wrapperElement.setText(qnamesTxt.toString());
        return wrapperElement;
    }

    private static URI[] getXAddresses(OMElement parent) throws DiscoveryException {
        OMElement xAddrsElement = parent.getFirstChildWithName(DiscoveryConstants.XADDRESSES);
        if (xAddrsElement == null) {
            return null;
        }

        String xAddrsList = xAddrsElement.getText();
        if (xAddrsList == null || "".equals(xAddrsList)) {
            return null;
        }

        return parseURIList(xAddrsList);
    }

    private static URI[] parseURIList(String uriListStr) throws DiscoveryException {
        StringTokenizer st = new StringTokenizer(uriListStr.trim());
        List<URI> uriList = new ArrayList<URI>();

        while (st.hasMoreTokens()) {
            String scope = st.nextToken();
            try {
                URI uri = new URI(scope);
                uriList.add(uri);
            } catch (URISyntaxException e) {
                String msg = "Error while parsing the scope URI: " + scope;
                log.error(msg, e);
                throw new DiscoveryException(msg, e);
            }
        }

        if (uriList.size() == 0) {
            return null;
        }

        return uriList.toArray(new URI[uriList.size()]);
    }

    private static void handleFault(String msg, Throwable t) throws DiscoveryException {
        log.error(msg, t);
        throw new DiscoveryException(msg, t);
    }
}
