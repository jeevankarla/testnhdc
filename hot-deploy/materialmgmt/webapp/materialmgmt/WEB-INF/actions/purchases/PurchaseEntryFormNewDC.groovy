
import org.ofbiz.base.util.*;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import net.sf.json.JSONObject;
import net.sf.json.JSONArray;
import javolution.util.FastMap;
import java.sql.Timestamp;
import org.ofbiz.base.util.UtilDateTime;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import org.ofbiz.service.ServiceUtil;
import in.vasista.vbiz.byproducts.ByProductNetworkServices;
import in.vasista.vbiz.byproducts.ByProductServices;
import org.ofbiz.product.product.ProductWorker;
import in.vasista.vbiz.facility.util.FacilityUtil;

import in.vasista.vbiz.purchase.PurchaseStoreServices;
import in.vasista.vbiz.purchase.MaterialHelperServices;

changeFlag=parameters.changeFlag;
customerName=parameters.partyName;
context.customerName=customerName;
productCatageoryId=parameters.productCatageoryId;

if(changeFlag=="PurchaseOrder"){
	productCatageoryId="PACKING_PRODUCT";
}

if(parameters.POId){
	orderHeader = delegator.findOne("OrderHeader", UtilMisc.toMap("orderId", parameters.POId), false);
	orderTypeId = orderHeader.orderTypeId;
	orderType = delegator.findOne("OrderType", UtilMisc.toMap("orderTypeId", orderTypeId), false);
		
	if(orderType){
		parentOrderTypeId = orderType.parentTypeId;
		if(parentOrderTypeId == "PURCHASE_CONTRACT"){
			parameters.purchaseTypeFlag = "contractPurchase";
		}
	}
}

//Debug.log("========changeFlagAFTERR NEWAPPLICATIONNNNNN=="+changeFlag+"=====in OrderINDesdff");
subscriptionProdList = [];
displayGrid = true;
effDateDayBegin="";
effDateDayEnd="";
effectiveDate = parameters.effectiveDate;
SimpleDateFormat sdf = new SimpleDateFormat("dd MMMMM, yyyy");
if(UtilValidate.isNotEmpty(effectiveDate)){
try {
	effectiveDate = new java.sql.Timestamp(sdf.parse(effectiveDate).getTime());
}catch (ParseException e) {
	Debug.logError(e, "Cannot parse date string: " + effDate, "");
	displayGrid = false;
}
	effDateDayBegin = UtilDateTime.getDayStart(effectiveDate);
	effDateDayEnd = UtilDateTime.getDayEnd(effectiveDate);
}else{
	effDateDayBegin = UtilDateTime.getDayStart(UtilDateTime.nowTimestamp());
	effDateDayEnd = UtilDateTime.getDayEnd(UtilDateTime.nowTimestamp());
}
conditionList = [];
exprList = [];
result = [:];
partyId="";
billToParty="";
billToPartyId = parameters.billToPartyId;

issueToDeptId = parameters.issueToDeptId;
facility = null;
prodPriceMap = [:];

if(UtilValidate.isNotEmpty(billToPartyId)){
	context.billToPartyId = billToPartyId;
	context.billToPartyName = org.ofbiz.party.party.PartyHelper.getPartyName(delegator,billToPartyId, false);;
	
}

prodList=[];

	/*exprList.clear();
	exprList.add(EntityCondition.makeCondition("productId", EntityOperator.NOT_EQUAL, "_NA_"));
	exprList.add(EntityCondition.makeCondition("productTypeId", EntityOperator.EQUALS, "RAW_MATERIAL"));
	exprList.add(EntityCondition.makeCondition(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.EQUALS, null),EntityOperator.OR,
			 EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN, effDateDayBegin)));
	  EntityCondition discontinuationDateCondition = EntityCondition.makeCondition(exprList, EntityOperator.AND);
	prodList =delegator.findList("Product", discontinuationDateCondition,null, null, null, false);


if(UtilValidate.isNotEmpty(changeFlag) &&( (changeFlag == "InterUnitPurchase")||(changeFlag == "PurchaseOrder"))){
	tempProdList=ProductWorker.getProductsByCategory(delegator,"GEL_STATIONERY",null);
	
	prodList.addAll(tempProdList);
}*/
dctx = dispatcher.getDispatchContext();
//exprList.add(EntityCondition.makeCondition("productId", EntityOperator.NOT_EQUAL, "_NA_"));
//exprList.add(EntityCondition.makeCondition("isVirtual", EntityOperator.NOT_EQUAL, "Y"));


conditionList.clear();
conditionList.add(EntityCondition.makeCondition("primaryParentCategoryId", EntityOperator.IN,["DYES","CHEMICALS"]));
condition = EntityCondition.makeCondition(conditionList,EntityOperator.AND);
	
productIdsList = EntityUtil.getFieldListFromEntityList(delegator.findList("ProductCategoryAndMember",condition, UtilMisc.toSet("productId"), null, null, false), "productId", true);

prodList = delegator.findList("Product", EntityCondition.makeCondition("productId", EntityOperator.IN, productIdsList),null, null, null, false);



//prodList =delegator.findList("Product", null,null, null, null, false);
/*resultMap = MaterialHelperServices.getMaterialProducts(dctx, context);
prodList.addAll(resultMap.get("productList"));*/
JSONArray productItemJSON = new JSONArray();
JSONObject productIdItemLabelsJSON = new JSONObject();
JSONObject productLabelIdsJSON=new JSONObject();
context.productList = prodList;

productIds = EntityUtil.getFieldListFromEntityList(prodList, "productId", true);
Map result = (Map)MaterialHelperServices.getProductUOM(delegator, productIds);
uomLabelMap = result.get("uomLabel");
productUomMap = result.get("productUom");
JSONObject productUOMJSON = new JSONObject();
JSONObject uomLabelJSON=new JSONObject();

prodList.each{eachItem ->
	JSONObject newObj = new JSONObject();
	newObj.put("value",eachItem.productId);
	newObj.put("label",eachItem.description);
	productItemJSON.add(newObj);
	if(eachItem.description){
		productIdItemLabelsJSON.put(eachItem.productId, eachItem.description);
		productLabelIdsJSON.put(eachItem.description, eachItem.productId);
	}
	
	
	
	productUOMJSON.put(eachItem.productId, "WT_kg");
	uomLabelJSON.put("WT_kg", "kg");
}

context.productUOMJSON = productUOMJSON;
context.uomLabelJSON = uomLabelJSON;
context.productItemJSON = productItemJSON;
context.productIdItemLabelsJSON = productIdItemLabelsJSON;
context.productLabelIdsJSON = productLabelIdsJSON;
if(displayGrid){
	context.partyCode = facility;
}

paymentTerms = delegator.findByAnd("TermType",UtilMisc.toMap("parentTypeId","FEE_PAYMENT_TERM"));
deliveryTerms = delegator.findByAnd("TermType",UtilMisc.toMap("parentTypeId","DELIVERY_TERM"));
//otherTerms = delegator.findByAnd("TermType",UtilMisc.toMap("parentTypeId","OTHERS"));
otherTerms = delegator.findByAnd("OrderAdjustmentType",UtilMisc.toMap("parentTypeId","ADDITIONAL_CHARGES"));

JSONArray paymentTermsJSON = new JSONArray();
JSONArray deliveryTermsJSON = new JSONArray();
JSONArray otherTermsJSON = new JSONArray();

JSONObject newObj = new JSONObject();
newObj.put("value","");
newObj.put("label","");
paymentTermsJSON.add(newObj);
deliveryTermsJSON.add(newObj);
otherTermsJSON.add(newObj);

paymentTerms.each{ eachTerm ->
	newObj.put("value",eachTerm.termTypeId);
	newObj.put("label",eachTerm.description);
	paymentTermsJSON.add(newObj);
}
deliveryTerms.each{ eachTerm ->
	newObj.put("value",eachTerm.termTypeId);
	newObj.put("label",eachTerm.description);
	deliveryTermsJSON.add(newObj);
}
JSONObject otherTermsLabelJSON = new JSONObject();
JSONObject otherTermsLabelIdJSON=new JSONObject();

otherTerms.each{eachItem ->
	newObj = new JSONObject();
	newObj.put("value",eachItem.orderAdjustmentTypeId);
	newObj.put("label",eachItem.orderAdjustmentTypeId +" [ " +eachItem.description+"]");
	otherTermsJSON.add(newObj);
	
	otherTermsLabelJSON.put(eachItem.orderAdjustmentTypeId, eachItem.description);
	otherTermsLabelIdJSON.put(eachItem.orderAdjustmentTypeId +" [ " +eachItem.description+"]", eachItem.orderAdjustmentTypeId);
	
}

context.paymentTermsJSON =paymentTermsJSON;
context.deliveryTermsJSON =deliveryTermsJSON;
context.otherTermsJSON =otherTermsJSON;
context.otherTermsLabelJSON =otherTermsLabelJSON;
context.otherTermsLabelIdJSON =otherTermsLabelIdJSON;

// orderTypes here
purchaseTypeFlag = parameters.purchaseTypeFlag;
if(UtilValidate.isNotEmpty(purchaseTypeFlag) && purchaseTypeFlag == "contractPurchase"){
	orderTypes = delegator.findByAnd("OrderType",UtilMisc.toMap("parentTypeId","PURCHASE_CONTRACT"));
	context.orderTypes =orderTypes;
}else{
	orderTypes = delegator.findByAnd("OrderType",UtilMisc.toMap("parentTypeId","PURCHASE_ORDER"));
	orderTypes = EntityUtil.orderBy(orderTypes,UtilMisc.toList("-description"));
	context.orderTypes =orderTypes;
}

//productStoreId =PurchaseStoreServices.getPurchaseFactoryStore(delegator).get("factoryStoreId");
//context.productStoreId = productStoreId;


JSONArray cstJSON = new JSONArray();
JSONArray vatJSON = new JSONArray();
JSONArray excJSON = new JSONArray();
orderTaxTypeList=delegator.findList("OrderTaxType",null,UtilMisc.toSet("taxType","taxRate"),null,null,false);
if(UtilValidate.isNotEmpty(orderTaxTypeList)){
	orderTaxTypeList.each{ orderTax ->
		JSONObject newObjt = new JSONObject();
		newObjt.put("value",orderTax.taxRate);
		newObjt.put("label",orderTax.taxRate);
		if(orderTax.taxType=="EXCISE_DUTY_PUR"){
			excJSON.add(newObjt);
		}
		if(orderTax.taxType=="CST_PUR"){
			cstJSON.add(newObjt);
		}
		if(orderTax.taxType=="VAT_PUR"){
			vatJSON.add(newObjt);
		}
	}
}
context.cstJSON=cstJSON;
context.excJSON=excJSON;
context.vatJSON=vatJSON;

titleTransferEnumIdsList = [];
taxAuthorityTypeTitleTransferList = delegator.findList("TaxAuthorityTypeTitleTransfer", null, null, null, null, false);
titleTransferEnumIdsList = EntityUtil.getFieldListFromEntityList(taxAuthorityTypeTitleTransferList, "titleTransferEnumId", true);

JSONObject transactionTypeTaxMap = new JSONObject();
for(int i=0; i<titleTransferEnumIdsList.size(); i++){
	titleTransferEnumId = titleTransferEnumIdsList.get(i);
	
	filteredTitleTransfer = EntityUtil.filterByCondition(taxAuthorityTypeTitleTransferList, EntityCondition.makeCondition("titleTransferEnumId", EntityOperator.EQUALS, titleTransferEnumId));
	taxIdsList = EntityUtil.getFieldListFromEntityList(filteredTitleTransfer, "taxAuthorityRateTypeId", true);
	
	JSONArray applicableTaxList = new JSONArray();
	for(int j=0; j<taxIdsList.size(); j++){
		applicableTaxList.add(taxIdsList.get(j));
	}
	transactionTypeTaxMap.putAt(titleTransferEnumId, applicableTaxList);
}
Debug.log("transactionTypeTaxMap =================="+transactionTypeTaxMap);

context.transactionTypeTaxMap = transactionTypeTaxMap;