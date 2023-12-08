package org.olf.rs.circ.client;

import org.apache.commons.lang.NotImplementedException;
import org.extensiblecatalog.ncip.v2.service.NCIPInitiationData;
import org.extensiblecatalog.ncip.v2.service.NCIPResponseData;
import org.json.JSONObject;

public class CancelRequestItem extends NCIPService implements NCIPCircTransaction {
    @Override
    public NCIPInitiationData generateNCIP2Object() {
        throw new NotImplementedException();
    }

    @Override
    public NCIPInitiationData modifyForWMS(NCIPInitiationData initData) {
        throw new NotImplementedException();
    }

    @Override
    public JSONObject constructResponseNcip2Response(NCIPResponseData responseData) {
        throw new NotImplementedException();
    }

    @Override
    public String generateNCIP1Object() {
        return generateNCIP1Object("/templates/cancelRequestItem.hbs");
    }

    @Override
    public JSONObject constructResponseNcip1Response(String responseData) {
        return null;
    }

    @Override
    public JSONObject validateRequest() {
        return null;
    }

    @Override
    public JSONObject constructWMSResponse(JSONObject responseJson) {
        throw new NotImplementedException();
    }
}
