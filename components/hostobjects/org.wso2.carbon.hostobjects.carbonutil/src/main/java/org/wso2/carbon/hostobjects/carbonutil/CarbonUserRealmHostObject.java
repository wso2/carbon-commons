package org.wso2.carbon.hostobjects.carbonutil;

import javax.script.ScriptException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.hostobjects.carbonutil.internal.ServiceHodler;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class CarbonUserRealmHostObject extends ScriptableObject {

	private static final long serialVersionUID = 66283547299256304L;

	@Override
	public String getClassName() {
		return "CarbonUserRealm";
	}

	public static Scriptable jsConstructor(Context cx, Object[] args,
			Function ctorObj, boolean inNewExpr) throws Exception {
		return new CarbonUserRealmHostObject();
	}
	 
	// user, resource, action
	public static boolean jsFunction_isUserAuthorized(Context cx,
			Scriptable thisObj, Object[] args, Function funObj) throws Exception {
		boolean isAuthorized = false;
		int argLength = args.length;
		if (argLength != 3) {
			throw new ScriptException("Invalid arguments.");
		}
		String user = (String) args[0];
		String userName = MultitenantUtils.getTenantAwareUsername(user);
		String domainName = MultitenantUtils.getTenantDomain(user);
		RealmService service = ServiceHodler.getRealmService();
		int tenantId = service.getTenantManager().getTenantId(domainName);
		UserRealm realm = service.getTenantUserRealm(tenantId);
		isAuthorized = realm.getAuthorizationManager().isUserAuthorized(userName, (String) args[1], (String) args[2]);
		return isAuthorized;
	}

}
