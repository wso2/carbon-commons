package org.wso2.carbon.logging.config;

public class SyslogConfiguration {

	private String syslogHostURL = "";
	private String port = "";
	private String realm = "";
	private String userName = "";
	private String password = "";
	private String syslogLogPattern = "";
	private boolean isSyslogOn = true;

	public String getSyslogHostURL() {
		return syslogHostURL;
	}

	public void setSyslogHostURL(String syslogHostURL) {
		this.syslogHostURL = syslogHostURL;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getRealm() {
		return realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSyslogLogPattern() {
		return syslogLogPattern;
	}

	public void setSyslogLogPattern(String syslogLogPattern) {
		this.syslogLogPattern = syslogLogPattern;
	}

	public boolean isSyslogOn() {
		return isSyslogOn;
	}

	public void setSyslogOn(boolean isSyslogOn) {
		this.isSyslogOn = isSyslogOn;
	}

}
