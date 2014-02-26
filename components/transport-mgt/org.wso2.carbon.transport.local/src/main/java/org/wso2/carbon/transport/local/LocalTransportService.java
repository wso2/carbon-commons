package org.wso2.carbon.transport.local;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.core.transports.AbstractTransportService;
import org.wso2.carbon.core.transports.util.TransportParameter;

public class LocalTransportService extends AbstractTransportService {
    public static final String TRANSPORT_NAME = "local";

	/**
	 * Instantiates LocalTransportService with a reference to the AxisConfiguration.
	 */
	public LocalTransportService() {
		super(TRANSPORT_NAME);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isEnableAtStartup() {
		return true;
	}

    public boolean dependenciesAvailable(TransportParameter[] params) {
        return true;
    }
}
