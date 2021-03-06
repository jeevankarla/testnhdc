import org.ofbiz.base.util.UtilDateTime;


import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import net.sf.json.JSONObject;
import javolution.util.FastList;
import org.ofbiz.base.util.*;
import net.sf.json.JSONObject;
import net.sf.json.JSONArray;
import in.vasista.vbiz.byproducts.ByProductNetworkServices;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.party.party.PartyHelper;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.party.contact.ContactMechWorker;
import javolution.util.FastMap;
import java.text.ParseException;
import org.ofbiz.service.ServiceUtil;
import in.vasista.vbiz.facility.util.FacilityUtil;
import org.ofbiz.party.contact.ContactMechWorker;



JSONArray OrderAddress = new JSONArray();

partyId = parameters.partyId;

contactMechId = parameters.contactMechId;

//Debug.log("contactMechId==============="+contactMechId);



conditionListAddress = [];
if(contactMechId)
conditionListAddress.add(EntityCondition.makeCondition("contactMechId", EntityOperator.EQUALS, contactMechId));
if(partyId)
conditionListAddress.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId));
conditionListAddress.add(EntityCondition.makeCondition("contactMechPurposeTypeId", EntityOperator.EQUALS, "SHIPPING_LOCATION"));
conditionAddress = EntityCondition.makeCondition(conditionListAddress,EntityOperator.AND);

 List<String> contactMechOrder = UtilMisc.toList("-contactMechId");

listAddressList = delegator.findList("PartyContactDetailByPurpose", conditionAddress, null, contactMechOrder, null, false);

listAddress = EntityUtil.getFirst(listAddressList);



JSONObject tempMap = new JSONObject();

if(listAddress){
	
	if(listAddress.address1)
      tempMap.put("address1",listAddress.address1);
	else
	  tempMap.put("address1","");
	  
   if(listAddress.address2)
	  tempMap.put("address2",listAddress.address2);
	else
	  tempMap.put("address2","");
	  
	  if(listAddress.countryGeoName)
	  //tempMap.put("country",listAddress.country);
	  tempMap.put("country",listAddress.countryGeoName);
	else
	  tempMap.put("country","");
	  
	  if(listAddress.stateGeoName)
	  //tempMap.put("state",listAddress.state);
	  tempMap.put("state",listAddress.stateGeoName);
	else
	  tempMap.put("state","TamilNadu");
	  
	  if(listAddress.city)
	  tempMap.put("city",listAddress.city);
	else
	  tempMap.put("city","");
	  
	  if(listAddress.postalCode)
	  tempMap.put("postalCode",listAddress.postalCode);
	else
	  tempMap.put("postalCode","");
	
	  OrderAddress.add(tempMap);
	  
}

//Debug.log("tempMap==============="+tempMap);


request.setAttribute("OrderAddress", tempMap);
return "success";



//context.partyOBMap = partyOBMap;