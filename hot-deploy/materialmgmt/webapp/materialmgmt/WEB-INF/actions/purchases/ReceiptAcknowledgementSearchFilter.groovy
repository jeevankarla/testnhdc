import org.ofbiz.base.util.*;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import javolution.util.FastMap;
import java.text.ParseException;
import org.ofbiz.service.ServiceUtil;
import in.vasista.vbiz.facility.util.FacilityUtil;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
productId=parameters.productId;
custRequestId = parameters.custRequestId;
custRequestDate = parameters.custRequestDate;
custRequestTypeId=parameters.custRequestTypeId;
context.custRequestTypeId=custRequestTypeId;
partyId = parameters.fromPartyId;
fromPartyId=context.get("partyId");
conditionList=[];
if(UtilValidate.isNotEmpty(parameters.productId)){
	conditionList.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId));
}
if(UtilValidate.isNotEmpty(parameters.custRequestId)){
	conditionList.add(EntityCondition.makeCondition("custRequestId", EntityOperator.EQUALS, custRequestId));
}
if(UtilValidate.isNotEmpty(partyId)){
	conditionList.add(EntityCondition.makeCondition("fromPartyId", EntityOperator.EQUALS, partyId));
}

if(UtilValidate.isEmpty(partyId) && UtilValidate.isNotEmpty(fromPartyId)){
	conditionList.add(EntityCondition.makeCondition("fromPartyId", EntityOperator.IN, fromPartyId));
}
if(UtilValidate.isNotEmpty(custRequestDate)){
    def sdf = new SimpleDateFormat("yyyy-MM-dd");
	try {
		custRequestDateNew = new java.sql.Timestamp(sdf.parse(custRequestDate+" 00:00:00").getTime());
	}catch (Exception e) {
		Debug.logError(e, "Cannot parse date string: " + custRequestDate, "");
	}
	conditionList.add(EntityCondition.makeCondition("custRequestDate", EntityOperator.EQUALS, custRequestDateNew));
}
orderBy = UtilMisc.toList("lastModifiedDate");	
conditionList.add(EntityCondition.makeCondition("itemStatusId", EntityOperator.EQUALS, "CRQ_ISSUED"));
conditionList.add(EntityCondition.makeCondition("custRequestTypeId", EntityOperator.EQUALS, custRequestTypeId));
condition = EntityCondition.makeCondition(conditionList, EntityOperator.AND);
CustRequestAndItemAndAttribute = delegator.findList("CustRequestAndItemAndAttribute", condition, null, orderBy, null, false);
if(UtilValidate.isNotEmpty(CustRequestAndItemAndAttribute)){
	if(custRequestTypeId=="INTERNAL_INDENT"){
		tempList=[];
		CustRequestAndItemAndAttribute.each{items->
			tempMap = [:];
			tempMap.custRequestId=items.custRequestId;
			tempMap.custRequestItemSeqId=items.custRequestItemSeqId;
			tempMap.custRequestDate=items.custRequestDate;
			tempMap.custRequestName=items.custRequestName;
			tempMap.productId = items.productId;
			tempMap.fromPartyId=items.fromPartyId;
			tempMap.quantity = items.quantity;
			productFacility = delegator.findList("ProductFacility",EntityCondition.makeCondition("productId",EntityOperator.EQUALS,items.productId),UtilMisc.toSet("facilityId"),null,null,false);
			prodFacilityIds = EntityUtil.getFieldListFromEntityList(productFacility, "facilityId", true);
			ecl = EntityCondition.makeCondition([EntityCondition.makeCondition("facilityId",EntityOperator.IN,prodFacilityIds),
				                                 EntityCondition.makeCondition("ownerPartyId",EntityOperator.EQUALS,items.fromPartyId)],EntityOperator.AND);               
			facilityList = delegator.findList("Facility",ecl,null,null,null,false);
			tempMap.put("facility",facilityList);
			tempList.add(tempMap);
		}
		context.indentItems = tempList;
	}else{
		context.indentItems = CustRequestAndItemAndAttribute;
	}
}

	