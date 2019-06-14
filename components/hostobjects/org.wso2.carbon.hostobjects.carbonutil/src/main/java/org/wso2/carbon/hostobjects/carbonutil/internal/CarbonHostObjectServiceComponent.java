package org.wso2.carbon.hostobjects.carbonutil.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.user.core.service.RealmService;

@Component(
        name = "org.wso2.carbon.hostobject.carbonutil",
        immediate = true)
public class CarbonHostObjectServiceComponent {

    @Reference(
            name = "user.realmservice.default",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {

        ServiceHodler.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

        ServiceHodler.setRealmService(null);
    }
}
