package com.ca.mas.masusermanagementsample.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExpandableListDataPump {
    public static HashMap<String, List<String>> getData() {
        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();

        List<String> masObject = new ArrayList<String>();
        masObject.add("MAS Init");
        masObject.add("MAS Login");
        masObject.add("MAS Logout");
        masObject.add("MAS Set Custom AuthCallbackHandler");
        masObject.add("MAS Remove Custom AuthCallbackHandler");
        masObject.add("MAS GrantFlow:password");
        masObject.add("MAS GrantFlow: client credential");
        masObject.add("MAS set configuration file nameMAS set configuration file name");
        masObject.add("JWKS Settings");
        masObject.add("MAS start");
        masObject.add("MAS start with new configuration discarding local storage");
        masObject.add("MAS start with JSON");
        masObject.add("MAS start with URL");
        masObject.add("MAS stop");
        masObject.add("MAS enableBrowserBasedAuthentication");

        List<String> masCustomOject = new ArrayList<String>();
        masCustomOject.add("Use Native MASUI");
        masCustomOject.add("MAS set custom login page");
        masCustomOject.add("MAS set custom OTP Channels page");
        masCustomOject.add("MAS set custom OTP page");

        List<String> masUser = new ArrayList<String>();
        masUser.add("MASUser getCurrentUser");
        masUser.add("MASUser getUsername");
        masUser.add("MASUser getFullName");
        masUser.add("MASUser isSessionLocked");

        expandableListDetail.put("MAS Lifecycle", masObject);
        expandableListDetail.put("MAS UI Customization", masCustomOject);
        expandableListDetail.put("MAS User", masUser);
        return expandableListDetail;
    }
}