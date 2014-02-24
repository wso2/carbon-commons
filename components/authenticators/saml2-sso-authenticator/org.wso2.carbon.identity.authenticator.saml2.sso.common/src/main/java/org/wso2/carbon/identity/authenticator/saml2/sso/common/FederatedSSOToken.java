package org.wso2.carbon.identity.authenticator.saml2.sso.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FederatedSSOToken {

	private HttpServletRequest httpServletRequest;
	private HttpServletResponse httpServletResponse;
	
	public HttpServletRequest getHttpServletRequest() {
		return httpServletRequest;
	}
	public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
		this.httpServletRequest = httpServletRequest;
	}
	public HttpServletResponse getHttpServletResponse() {
		return httpServletResponse;
	}
	public void setHttpServletResponse(HttpServletResponse httpServletResponse) {
		this.httpServletResponse = httpServletResponse;
	}
		
}
