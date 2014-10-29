/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.viewflows.service;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.Phase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.viewflows.AxisConfigData;
import org.wso2.carbon.viewflows.HandlerData;
import org.wso2.carbon.viewflows.PhaseData;
import org.wso2.carbon.viewflows.PhaseOrderData;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * this class produces the information about the flows and phases
 */
@SuppressWarnings("unused")
public class AxisConfigAdminService extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(AxisConfigAdminService.class);

    /**
     * @return the global axisConfig data for running server
     */
    public AxisConfigData getAxisConfigData() {
        AxisConfigData axisConfigData = new AxisConfigData();
        AxisConfiguration axisConfiguration = getAxisConfig();

        // setting the in-phase order data
        axisConfigData.setInflowPhaseOrder(getPhaseOrderData(axisConfiguration.getInFlowPhases(),
                                                             null, false));
        axisConfigData.setOutflowPhaseOrder(getPhaseOrderData(
                axisConfiguration.getOutFlowPhases(), null, true));
        axisConfigData.
                setInfaultflowPhaseOrder(getPhaseOrderData(axisConfiguration.getInFaultFlowPhases(),
                                                           null, false));
        axisConfigData.
                setOutfaultPhaseOrder(getPhaseOrderData(axisConfiguration.getOutFaultFlowPhases(),
                                                        null, true));

        return axisConfigData;
    }

    private PhaseOrderData getPhaseOrderData(List globalPhaseList, List localPhaseList,
                                             boolean invert) {
        PhaseOrderData phaseOrderData = new PhaseOrderData();

        int globalPhases = (globalPhaseList == null) ? 0 : globalPhaseList.size();
        int localPhases = (localPhaseList == null) ? 0 : localPhaseList.size();

        PhaseData[] phaseData = new PhaseData[globalPhases + localPhases];
        if (globalPhaseList != null) {
            setPhaseDataArray(globalPhaseList, phaseData, true, 0, invert);
        }
        if (localPhaseList != null) {
            setPhaseDataArray(localPhaseList, phaseData, false, globalPhases, invert);
        }
        phaseOrderData.setPhases(phaseData);
        return phaseOrderData;
    }

    private void setPhaseDataArray(List phaseList, PhaseData[] phaseData, boolean isGlobalPhase,
                                   int offset, boolean invert) {
        Phase phase;
        for (int i = 0; i < phaseList.size(); i++) {
            phase = (Phase) phaseList.get(i);
            phaseData[offset + i] = getPhaseData(phase, isGlobalPhase, invert);
        }
    }

    private PhaseData getPhaseData(Phase phase, boolean isGlobalPhase, boolean invert) {
        PhaseData phaseData = new PhaseData(phase.getPhaseName());
        phaseData.setIsGlobalPhase(isGlobalPhase);
        HandlerData[] handlers = new HandlerData[phase.getHandlerCount()];

        // populate the phase handlers
        Handler handler;
        if (invert) {
            for (int i = phase.getHandlers().size() - 1, j = 0; i >= 0; i--, j++) {
                handler = phase.getHandlers().get(i);
                handlers[j] = getHandlerData(handler);
            }
        } else {
            for (int i = 0; i < phase.getHandlers().size(); i++) {
                handler = phase.getHandlers().get(i);
                handlers[i] = getHandlerData(handler);
            }
        }

        phaseData.setHandlers(handlers);
        log.debug("Setting phase ==> " + phaseData.getName());
        return phaseData;
    }

    private HandlerData getHandlerData(Handler handler) {
        HandlerData handlerData = new HandlerData(handler.getName());
        handlerData.setClassName(handler.getHandlerDesc().getClassName());
        handlerData.setPhaseLast(handler.getHandlerDesc().getRules().isPhaseLast());
        return handlerData;
    }

    /**
     * @param serviceId
     * @param operationId
     * @return Axis config data for the given axis operation
     */
    public AxisConfigData getOperationAxisConfigData(String serviceId,
                                                     String operationId) throws AxisFault {
        log.debug("Getting handler details for service " + serviceId +
                  " operation " + operationId);
        AxisConfigData axisConfigData = new AxisConfigData();
        AxisConfiguration axisConfiguration = getAxisConfig();

        AxisService axisService = axisConfiguration.getService(serviceId);
        AxisOperation axisOperation = axisService.getOperation(new QName(operationId));

        // adding phases to axis config data object
        axisConfigData.
                setInflowPhaseOrder(getPhaseOrderData(axisConfiguration.getInFlowPhases(),
                                                      axisOperation.getRemainingPhasesInFlow(),
                                                      false));
        axisConfigData.
                setOutflowPhaseOrder(getPhaseOrderData(axisOperation.getPhasesOutFlow(),
                                                       axisConfiguration.getOutFlowPhases(),
                                                       true));
        axisConfigData.
                setInfaultflowPhaseOrder(getPhaseOrderData(axisConfiguration.getInFaultFlowPhases(),
                                                           axisOperation.getPhasesInFaultFlow(),
                                                           false));
        axisConfigData.
                setOutfaultPhaseOrder(getPhaseOrderData(axisOperation.getPhasesOutFaultFlow(),
                                                        axisConfiguration.getOutFaultFlowPhases(),
                                                        true));

        return axisConfigData;
    }

    /**
     * Returns the list of all handlers found in the particular phase
     * @param flow - specifies the flow of the phase
     * @param phase - phase name
     * @return - list of handler names
     */
    public ArrayList getHandlerNamesForPhase(String flow, String phase) {
        ArrayList handlerNames = new ArrayList();
        

        return handlerNames;
    }
}
