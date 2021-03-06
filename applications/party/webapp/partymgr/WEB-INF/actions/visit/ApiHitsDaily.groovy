
import org.ofbiz.base.util.*;
import org.ofbiz.entity.condition.*;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.party.party.PartyHelper;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import net.sf.json.JSONObject;
import net.sf.json.JSONArray;

dctx = dispatcher.getDispatchContext();
//SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

def sdf = new SimpleDateFormat("dd/MM/yyyy");
Timestamp fromDate = null;
try {
	if (parameters.fromDate) {
		context.fromDate = parameters.fromDate;
		fromDate = UtilDateTime.getDayStart(new java.sql.Timestamp(sdf.parse(parameters.fromDate).getTime()));
	}
} catch (ParseException e) {
	Debug.logError(e, "Cannot parse date string: " + e, "");
}
if (fromDate == null) {
	fromDate = UtilDateTime.nowTimestamp();
}

Timestamp thruDate = null;
try {
	if (parameters.thruDate) {
		context.thruDate = parameters.thruDate;
		thruDate = UtilDateTime.getDayStart(new java.sql.Timestamp(sdf.parse(parameters.thruDate).getTime()));
	}
} catch (ParseException e) {
	Debug.logError(e, "Cannot parse date string: " + e, "");
}
if (thruDate == null) {
	thruDate = UtilDateTime.nowTimestamp();
}

//String hitsDateStr = UtilDateTime.toDateString(hitsDate, "dd/MM/yyyy");			

Timestamp timePeriodStart = UtilDateTime.getDayStart(fromDate);
Timestamp timePeriodEnd = UtilDateTime.getDayEnd(thruDate);
JSONArray hitsListJSON = new JSONArray();
conditionList=[];
conditionList.add(EntityCondition.makeCondition("startDateTime", EntityOperator.GREATER_THAN_EQUAL_TO , timePeriodStart));
conditionList.add(EntityCondition.makeCondition("startDateTime", EntityOperator.LESS_THAN_EQUAL_TO , timePeriodEnd));
conditionList.add(EntityCondition.makeCondition("userLoginId", EntityOperator.NOT_EQUAL, 'hrmsapi'));
condition = EntityCondition.makeCondition(conditionList,EntityOperator.AND);

hitList = delegator.findList("ApiHit", condition, null, null, null, false);

for (GenericValue hit : hitList) {
	//String startDateTime = hit.get("startDateTime").toString(); 
	String hitDate = UtilDateTime.toDateString(hit.get("startDateTime"), "dd/MM/yyyy");
    String hitTime = UtilDateTime.toDateString(hit.get("startDateTime"), "HH:mm:ss");

	String userLoginId= hit.getString("userLoginId");
	String partyId = "";
	String role = "";
	if (userLoginId) {
		userLogin = delegator.findByPrimaryKey("UserLogin", [userLoginId : userLoginId]);
		if (userLogin) {
			partyId = userLogin.partyId;
			if (partyId) {
				partyRoles = delegator.findByAnd("PartyRole", [partyId : partyId]);
				partyRoles.each { partyRole ->
					if (partyRole.roleTypeId.equals("Retailer")) {
						role = "Retailer";				
					}
					else if (partyRole.roleTypeId.equals("EMPLOYEE")) {
						role = "Employee";
					}
				}
			}
		}
	}
	String contentId= hit.getString("contentId");
	String totalTimeMillis = hit.getLong("totalTimeMillis");
	
	JSONArray hitJSON = new JSONArray();	
	hitJSON.add(hitDate);
	hitJSON.add(hitTime);
	hitJSON.add(userLoginId);
	hitJSON.add(partyId);
	hitJSON.add(role);
	hitJSON.add(contentId);
	hitJSON.add(totalTimeMillis);
	
	hitsListJSON.add(hitJSON);
}

//Debug.logError("hitsListJSON="+hitsListJSON,"");
context.fromDate = fromDate;
context.thruDate = thruDate;
context.hitsListJSON = hitsListJSON;