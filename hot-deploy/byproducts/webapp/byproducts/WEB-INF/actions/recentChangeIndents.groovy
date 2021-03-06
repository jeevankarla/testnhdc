import java.util.*;

import org.ofbiz.base.util.*;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import javolution.util.FastList;
import java.text.SimpleDateFormat;
import net.sf.json.JSONObject;
import net.sf.json.JSONArray;
import in.vasista.vbiz.byproducts.ByProductNetworkServices;
uiLabelMap = UtilProperties.getResourceBundleMap("OrderUiLabels", locale);

lastChangeSubProdMap=[:];
orderId = null;
boothId = null;
subscriptionId = null;
productSubscriptionTypeId = null;
fromDate = null;
modifiedBy = null;
modificationTime = null;
prodList = [];
SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
dayBegin = UtilDateTime.getDayStart(UtilDateTime.nowTimestamp(), timeZone, locale);
dayEnd = UtilDateTime.getDayEnd(UtilDateTime.nowTimestamp(), timeZone, locale);
SubProdList = [];
conditionList = [];
boothId = null;
if(("newIndent".equals(changeFlag)) ){
    subscriptionId = null;
	List exprList = [];
	exprList.add(EntityCondition.makeCondition([
		 EntityCondition.makeCondition("createdDate", EntityOperator.LESS_THAN_EQUAL_TO, dayEnd),
		 EntityCondition.makeCondition("lastUpdatedStamp", EntityOperator.LESS_THAN_EQUAL_TO, dayEnd)
		], EntityOperator.OR));
	exprList.add(EntityCondition.makeCondition("lastModifiedByUserLogin", EntityOperator.EQUALS, userLogin.userLoginId));
	condition = EntityCondition.makeCondition(exprList, EntityOperator.AND);
	lastSubscriptionProdList = delegator.findList("SubscriptionProduct", condition, null, ["-lastUpdatedStamp"], null, false);
	if(lastSubscriptionProdList){
		lastIndent = EntityUtil.getFirst(lastSubscriptionProdList);
		fromDate = lastIndent.fromDate;
		subscriptionId = lastIndent.subscriptionId;
		productSubscriptionTypeId = lastIndent.productSubscriptionTypeId;
		modifiedBy = lastIndent.lastModifiedByUserLogin;
		modTime = lastIndent.lastUpdatedStamp;
		modificationTime = dateFormat.format(modTime);
	}	
	
	if(subscriptionId){		
		conditionList.clear();
		conditionList.add(EntityCondition.makeCondition("subscriptionId", EntityOperator.EQUALS, subscriptionId));
		conditionList.add(EntityCondition.makeCondition("fromDate", EntityOperator.EQUALS, fromDate));
		conditionList.add(EntityCondition.makeCondition("productSubscriptionTypeId", EntityOperator.EQUALS, productSubscriptionTypeId));
		conditionList.add(EntityCondition.makeCondition("lastModifiedByUserLogin", EntityOperator.EQUALS, modifiedBy));
		conditionList.add(EntityCondition.makeCondition("subscriptionTypeId", EntityOperator.EQUALS, "BYPRODUCTS"));
		condition = EntityCondition.makeCondition(conditionList,EntityOperator.AND);
		SubProdList = delegator.findList("SubscriptionFacilityAndSubscriptionProduct", condition, null,["-createdDate"], null, false);
		if(SubProdList){
			latestDate = SubProdList.get(0).get("createdDate");
			SubProdList = EntityUtil.filterByCondition(SubProdList, EntityCondition.makeCondition("createdDate", EntityOperator.EQUALS, latestDate));
		}
	}
	
	if (SubProdList.size() > 0) {
		boothIndent = EntityUtil.getFirst(SubProdList);
		boothId = boothIndent.facilityId;
	}
	
}

if(("AdhocSaleNew".equals(changeFlag)) ){
	
	List shipmentIds = ByProductNetworkServices.getShipmentIds(delegator , UtilDateTime.toDateString(dayBegin, "yyyy-MM-dd HH:mm:ss"),"RM_DIRECT_SHIPMENT");
	
	List exprList = [];
	
	exprList.add(EntityCondition.makeCondition("shipmentId", EntityOperator.IN ,shipmentIds));
	
	exprList.add(EntityCondition.makeCondition("shipmentStatusId", EntityOperator.EQUALS , "GENERATED"));
	exprList.add(EntityCondition.makeCondition("orderStatusId", EntityOperator.NOT_EQUAL  , "ORDER_CANCELLED"));
	exprList.add(EntityCondition.makeCondition("changeDatetime", EntityOperator.GREATER_THAN_EQUAL_TO, dayBegin));
	exprList.add(EntityCondition.makeCondition("changeDatetime", EntityOperator.LESS_THAN_EQUAL_TO ,dayEnd));
	exprList.add(EntityCondition.makeCondition("changeByUserLoginId", EntityOperator.EQUALS, userLogin.userLoginId));
	
	
	condition = EntityCondition.makeCondition(exprList, EntityOperator.AND);
	
	SubProdList = delegator.findList("OrderHeaderItemProductShipmentAndFacility", condition, null, ["-changeDatetime"], null, false);
	
	if (SubProdList.size() > 0) {
		boothIndent = EntityUtil.getFirst(SubProdList);
		boothId = boothIndent.originFacilityId;
		SubProdList = EntityUtil.filterByAnd(SubProdList, UtilMisc.toMap("orderId", boothIndent.orderId));
	}	
	
	if(SubProdList){
		lastIndent = EntityUtil.getFirst(SubProdList);
		fromDate = lastIndent.fromDate;		
		productSubscriptionTypeId = lastIndent.productSubscriptionTypeId;
		modifiedBy = lastIndent.changeByUserLoginId;
		modTime = lastIndent.changeDatetime;
		modificationTime = dateFormat.format(modTime);
	}
}




SubProdList.each{ eachItem ->
	productMap = [:];
	prodDetails = delegator.findOne("Product", UtilMisc.toMap("productId", eachItem.productId),false);
	productMap.productId = eachItem.productId;
	productMap.productName = prodDetails.brandName;
	prodList.add(productMap);
	lastChangeSubProdMap[eachItem.productId] = eachItem.quantity;
	facilityParty = delegator.findOne("Facility", UtilMisc.toMap("facilityId", boothId),false);
	lastChangeSubProdMap["boothId"] = boothId+" ["+facilityParty.facilityName+" ]";
	lastChangeSubProdMap["modifiedBy"] = modifiedBy;
	lastChangeSubProdMap["modificationTime"] = modificationTime;//dateFormat.format(eachItem.changeDatetime);
}

context.prodList = prodList;
context.lastChangeSubProdMap = lastChangeSubProdMap;



//lets prepare So Booths and customers json for auto complete
facilities = delegator.findList("Facility", null, null, null, null, false);
JSONArray facilityJSON = new JSONArray();
JSONObject productIdLabelJSON = new JSONObject();
facilities.each{eachItem ->
	JSONObject newObj = new JSONObject();
	newObj.put("value",eachItem.facilityId);
	newObj.put("label",eachItem.facilityId + " [" + eachItem.facilityName + "]");
	facilityJSON.add(newObj);
}
context.facilityJSON = facilityJSON;


routes = delegator.findByAnd("Facility", ["facilityTypeId" : "ROUTE" ,"categoryTypeEnum" : "BYPROD_ROUTE" ],null);
JSONArray routeJSON = new JSONArray();

routes.each{eachItem ->
	JSONObject newObj = new JSONObject();
	newObj.put("value",eachItem.facilityId);
	newObj.put("label",eachItem.facilityId + " [" + eachItem.facilityName + "]");
	routeJSON.add(newObj);
	//productIdLabelJSON.put(eachItem.productId, eachItem.productId + " [" + eachItem.productName + "]")
}
context.routeJSON = routeJSON;
//context.productIdLabelJSON = productIdLabelJSON;

conditionList.clear();
conditionList.add(EntityCondition.makeCondition("productCategoryTypeId", EntityOperator.IN,UtilMisc.toList("Milk","Milk powder","Curd","Ghee","Butter","Chocolate","INDENT","Sweets","Flavoured Milk")));
condition = EntityCondition.makeCondition(conditionList,EntityOperator.AND);
productCategory = delegator.findList("ProductCategory", condition, UtilMisc.toSet("productCategoryId"), null, null, false);
List productCategoryIds = FastList.newInstance();
if("scrapSales".equals(changeFlag)){
	productCategoryIds.add("SCRAP_MATERIAL");
}else{
productCategoryIds = EntityUtil.getFieldListFromEntityList(productCategory, "productCategoryId", true);
}
context.productCategoryIds=productCategoryIds;

