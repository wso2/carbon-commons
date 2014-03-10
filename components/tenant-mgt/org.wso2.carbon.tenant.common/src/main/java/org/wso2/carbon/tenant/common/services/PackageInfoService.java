package org.wso2.carbon.tenant.common.services;

import java.util.List;

import org.wso2.carbon.tenant.common.internal.CloudCommonServiceComponent;
import org.wso2.carbon.tenant.common.packages.PackageInfo;

public class PackageInfoService {

	public PackageInfo[] getPackageInfos() throws Exception {
		List<PackageInfo> list = CloudCommonServiceComponent.getPackageInfos().
		                                                     getMultitenancyPackages();
		PackageInfo[] packageInfos = list.toArray(new PackageInfo[list.size()]);
		return packageInfos;
	}
}
