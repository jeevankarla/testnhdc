/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import org.ofbiz.base.util.*;
import org.ofbiz.entity.*;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import net.sf.json.JSONObject;
import net.sf.json.JSONArray;
import org.ofbiz.entity.util.EntityUtil;

resultCtx = dispatcher.runSync("getCustomerBranch",UtilMisc.toMap("userLogin",userLogin));



roleTypeId = parameters.roleTypeId;

List roleTypeAttr=delegator.findList("RoleTypeAttr",EntityCondition.makeCondition("attrName",EntityOperator.EQUALS,"ACCOUNTING_ROLE"),null,null,null,false);
roleTypeAttrList=[];
if(roleTypeAttr){
	roleTypeAttr.each{roleType->
		tempMap=[:];
		if(roleType.roleTypeId != "EMPLOYEE" && roleType.roleTypeId != "MILK_SUPPLIER"){
			GenericValue roleTypeDes=delegator.findOne("RoleType",[roleTypeId:roleType.roleTypeId],false);
			if(UtilValidate.isNotEmpty(roleTypeDes)){
				tempMap["roleTypeId"]=roleType.roleTypeId;
				tempMap["description"]=roleTypeDes.description;
				roleTypeAttrList.add(tempMap);
			}
		}
	}
}
context.roleTypeAttrList=roleTypeAttrList;
context.roleTypeId = roleTypeId;

Map formatMap = [:];
List formatList = [];
List branchIds = [];
	for (eachList in resultCtx.get("productStoreList")) {
		
		formatMap = [:];
		formatMap.put("storeName",eachList.get("storeName"));
		formatMap.put("payToPartyId",eachList.get("payToPartyId"));
		formatList.addAll(formatMap);
		branchIds.add(eachList.get("payToPartyId"));
		
	}

roList = dispatcher.runSync("getRegionalOffices",UtilMisc.toMap("userLogin",userLogin));
roPartyList = roList.get("partyList");

for(eachRO in roPartyList){
	formatMap = [:];
	formatMap.put("storeName",eachRO.get("groupName"));
	formatMap.put("payToPartyId",eachRO.get("partyId"));
	formatList.addAll(formatMap);
}
context.branchList = formatList;


finCondList = [];
finCondList.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "FNACT_ACTIVE"));
finCondList.add(EntityCondition.makeCondition("finAccountTypeId", EntityOperator.IN, ["BANK_ACCOUNT","CASH"]));
if(UtilValidate.isNotEmpty(parameters.ownerPartyId) && parameters.ownerPartyId!=null){
	finCondList.add(EntityCondition.makeCondition("ownerPartyId", EntityOperator.EQUALS, parameters.ownerPartyId));
}
cond = EntityCondition.makeCondition(finCondList, EntityOperator.AND);

finAccounts = delegator.findList("FinAccount", cond, null, null, null, false);

context.finAccounts1=finAccounts;

paymentCondList = [];
if(parameters.ownerPartyId && parameters.ownerPartyId!=null){
	paymentCondList.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, parameters.ownerPartyId));
}
paymentCondList.add(EntityCondition.makeCondition("paymentMethodTypeId", EntityOperator.IN, ["CASH_PAYOUT","CHEQUE_PAYOUT"]));
cond = EntityCondition.makeCondition(paymentCondList, EntityOperator.AND);
paymentMethods = [];
paymentMethods = delegator.findList("PaymentMethodAndType", cond, null, null, null, false);
context.paymentMethods = paymentMethods;
partyId=""
if(parameters.ownerPartyId && parameters.ownerPartyId!=null){
	partyId = parameters.ownerPartyId 
}
context.partyId = partyId;

uiLabelMap = UtilProperties.getResourceBundleMap("AccountingUiLabels", locale);
if(UtilValidate.isEmpty(parameters.noConditionFind)){
return "";
}
condList = [];
//reset purposeType If Empty before search
if(UtilValidate.isEmpty(parameters.purposeTypeIdField)){
	condList.add(EntityCondition.makeCondition("enumTypeId", EntityOperator.EQUALS, "ORDER_SALES_CHANNEL"));
	condList.add(EntityCondition.makeCondition("enumId", EntityOperator.NOT_IN, UtilMisc.toList("BYPROD_SALES_CHANNEL","RM_DIRECT_CHANNEL")));
	enumCond = EntityCondition.makeCondition(condList, EntityOperator.AND);
	salesChannelList = delegator.findList("Enumeration",enumCond, null, null, null, false);
	if(!UtilValidate.isEmpty(salesChannelList)){
		parameters.purposeTypeId = EntityUtil.getFieldListFromEntityList(salesChannelList,"enumId",true);
		//parameters.purposeTypeId=UtilMisc.toList("BYPROD_SALES_CHANNEL","RM_DIRECT_CHANNEL")
		parameters.purposeTypeId_op = "in";
	}
}
else {
	parameters.purposeTypeId = parameters.purposeTypeIdField;
}
//Debug.log("====parameters.purposeTypeIdField===="+parameters.purposeTypeIdField+"=====purposeTypeId="+parameters.purposeTypeId);

condtList = [];
condtList.add(EntityCondition.makeCondition("parentTypeId" ,EntityOperator.EQUALS, "MONEY"));
cond = EntityCondition.makeCondition(condtList, EntityOperator.AND);
PaymentMethodType = delegator.findList("PaymentMethodType", cond, UtilMisc.toSet("paymentMethodTypeId","description"), null, null ,false);

context.PaymentMethodType = PaymentMethodType;

condList.clear();
parentTypeId=parameters.parentTypeId;
actionName="createVoucherPayment";

//AP PaymentTypes
condList.add(EntityCondition.makeCondition("parentTypeId", EntityOperator.IN, UtilMisc.toList("DISBURSEMENT","TAX_PAYMENT")));
cond = EntityCondition.makeCondition(condList, EntityOperator.AND);
apPaymentTypes = delegator.findList("PaymentType", cond, null, ["description"], null, false);

condList.clear();
condList.add(EntityCondition.makeCondition("paymentTypeId", EntityOperator.IN, UtilMisc.toList("AGNSTINV_PAYOUT","STATUTORY_PAYOUT")));
cond = EntityCondition.makeCondition(condList, EntityOperator.AND);
apTreasurerPaymentTypes = delegator.findList("PaymentType", cond, null, ["description"], null, false);
context.put("apTreasurerPaymentTypes",apTreasurerPaymentTypes);

condList.clear();
condList.add(EntityCondition.makeCondition("paymentTypeId", EntityOperator.EQUALS, "MIS_INCOME_PAYIN"));
cond = EntityCondition.makeCondition(condList, EntityOperator.AND);
arTreasurerPaymentTypes = delegator.findList("PaymentType", cond, null, ["description"], null, false);
context.put("arTreasurerPaymentTypes",arTreasurerPaymentTypes);

//AR PaymentTypes
condList.clear();
if(UtilValidate.isNotEmpty(parameters.paymentMethodSearchFlag)){
	condList.add(EntityCondition.makeCondition("paymentTypeId", EntityOperator.LIKE, "%SALES_PAYIN%"));
}
condList.add(EntityCondition.makeCondition("parentTypeId", EntityOperator.IN, UtilMisc.toList("RECEIPT")));
cond = EntityCondition.makeCondition(condList, EntityOperator.AND);
arPaymentTypes = delegator.findList("PaymentType", cond, null, ["description"], null, false);
paymentTypes=[];
paymentMethodLike="";
if("SALES_INVOICE"==parentTypeId){
	context.paymentTypes=arPaymentTypes;
	paymentMethodLike="%_PAYIN%";
	actionName="createArVoucherPayment";
}else if("PURCHASE_INVOICE"==parentTypeId){
	context.paymentTypes=apPaymentTypes;
	paymentMethodLike="%_PAYOUT%";
	actionName="createApVoucherPayment";
}else{
	context.paymentTypes=paymentTypes;
}
if(UtilValidate.isNotEmpty(parameters.paymentMethodSearchFlag)){
actionName="createOtherVoucherPayment";
}
context.parentTypeId=parentTypeId;
context.actionName=actionName;


condList.clear();
condList.add(EntityCondition.makeCondition("finAccountTypeId", EntityOperator.EQUALS, "BANK_ACCOUNT"));
condList.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "FNACT_ACTIVE"));
//condList.add(EntityCondition.makeCondition("ownerPartyId", EntityOperator.EQUALS, "Company"));
cond = EntityCondition.makeCondition(condList, EntityOperator.AND);
finAccountList = delegator.findList("FinAccount", cond, null, ["finAccountName"], null, false);
context.finAccountList = finAccountList;
BankList = delegator.findList("Bank", null, null, null, null, false);
JSONArray BankListJSON = new JSONArray();

if(BankList){
	BankList.each{ eachBank ->
		JSONObject newObj = new JSONObject();
			newObj.put("value",eachBank.description);
			BankListJSON.add(newObj);
	}
}
context.BankListJSON=BankListJSON;

//Debug.log("====paymentMethodSearchFlag=HECKKKKK="+parameters.paymentMethodSearchFlag);
JSONObject voucherPaymentMethodJSON = new JSONObject();
JSONArray cashMethodItemsJSON = new JSONArray();
JSONArray bankMethodItemsJSON = new JSONArray();
JSONArray allMethodItemsJSON = new JSONArray();
bankPaymentMethodList=[];
cashPaymentMethodList=[];
condList.clear();
if(UtilValidate.isNotEmpty(parameters.paymentMethodSearchFlag)){
	condList.add(EntityCondition.makeCondition("paymentMethodTypeId", EntityOperator.NOT_LIKE, "%_PAYOUT%"));
	condList.add(EntityCondition.makeCondition("parentTypeId", EntityOperator.EQUALS,"MONEY"));
	cond = EntityCondition.makeCondition(condList, EntityOperator.AND);
	bankPaymentMethodList = delegator.findList("PaymentMethodType",cond, null, null, null, false);
}else{

condList.add(
	EntityCondition.makeCondition(
			EntityCondition.makeCondition("paymentMethodTypeId", EntityOperator.LIKE, paymentMethodLike), 
			EntityOperator.OR, 
			EntityCondition.makeCondition("paymentMethodTypeId", EntityOperator.EQUALS, "CHEQUE")));


condList.add(EntityCondition.makeCondition("parentTypeId", EntityOperator.EQUALS,"MONEY"));
cond = EntityCondition.makeCondition(condList, EntityOperator.AND);
bankPaymentMethodList = delegator.findList("PaymentMethodType",cond, null, null, null, false);
condList.clear();
//CASH_PAYIN
if("SALES_INVOICE"==parentTypeId){
//          if(parameters.prefPaymentMethodTypeId=="CASH"){
	condList.add(EntityCondition.makeCondition("paymentMethodTypeId", EntityOperator.LIKE, "%CASH_PAYIN%"));
		/* }else
	  {
		  condList.add(EntityCondition.makeCondition("paymentMethodTypeId", EntityOperator.LIKE, paymentMethodLike));
	  }*/
}
else
{

condList.add(EntityCondition.makeCondition("paymentMethodTypeId", EntityOperator.LIKE, paymentMethodLike));
}
//condList.add(EntityCondition.makeCondition("parentTypeId", EntityOperator.EQUALS,"CASH"));
condList.add(EntityCondition.makeCondition("parentTypeId", EntityOperator.EQUALS,"MONEY"));
cond = EntityCondition.makeCondition(condList, EntityOperator.AND);
cashPaymentMethodList = delegator.findList("PaymentMethodType", cond, null, null, null, false);
}

bankPaymentMethodIdsList=EntityUtil.getFieldListFromEntityList(bankPaymentMethodList, "paymentMethodTypeId", false);
cashPaymentMethodIdsList=EntityUtil.getFieldListFromEntityList(cashPaymentMethodList, "paymentMethodTypeId", false);


allPaymentMethods = []; 

if("SALES_INVOICE"==parentTypeId){
	cashPaymentMethodList.each{ methodTypeEach->
		JSONObject newPMethodObj = new JSONObject();
		newPMethodObj.put("value",methodTypeEach.paymentMethodTypeId);
		newPMethodObj.put("text", methodTypeEach.description);
		cashMethodItemsJSON.add(newPMethodObj);
		if(!allPaymentMethods.contains(methodTypeEach.paymentMethodTypeId)){
			allMethodItemsJSON.add(newPMethodObj);
			allPaymentMethods.add(methodTypeEach.paymentMethodTypeId);
		}
		
	}
	JSONArray finalJSON = new JSONArray();
	bankPaymentMethodList.each{ methodTypeEach->
		JSONObject newPMethodObj = new JSONObject();
		newPMethodObj.put("value",methodTypeEach.paymentMethodTypeId);
		newPMethodObj.put("text", methodTypeEach.description);
		if("FT_PAYIN".equals(methodTypeEach.paymentMethodTypeId)){
			finalJSON.add(newPMethodObj);
		}else{
			bankMethodItemsJSON.add(newPMethodObj);
		}
			
		
		if(!allPaymentMethods.contains(methodTypeEach.paymentMethodTypeId)){
			allMethodItemsJSON.add(newPMethodObj);
			allPaymentMethods.add(methodTypeEach.paymentMethodTypeId);
		}
	}
	finalJSON.addAll(bankMethodItemsJSON);	
	bankMethodItemsJSON=finalJSON;	
}else if("PURCHASE_INVOICE"==parentTypeId){
		cList = [];
		cList.add(EntityCondition.makeCondition("paymentMethodTypeId", EntityOperator.IN,bankPaymentMethodIdsList));
		if(UtilValidate.isNotEmpty(parameters.ownerPartyId) && parameters.ownerPartyId!=null){
			cList.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, parameters.ownerPartyId));
		}
		
		cond = EntityCondition.makeCondition(cList, EntityOperator.AND);
		
		bankPaymentMethodList = delegator.findList("PaymentMethod", cond, null, null, null, false);
		
		cList.clear();
		cList.add(EntityCondition.makeCondition("paymentMethodTypeId", EntityOperator.IN,cashPaymentMethodIdsList));
		if(UtilValidate.isNotEmpty(parameters.ownerPartyId) && parameters.ownerPartyId!=null){
			cList.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, parameters.ownerPartyId));
		}
		cond = EntityCondition.makeCondition(cList, EntityOperator.AND);
		
		cashPaymentMethodList = delegator.findList("PaymentMethod", cond, null, null, null, false);
		cashPaymentMethodList.each{ methodTypeEach->
			JSONObject newPMethodObj = new JSONObject();
			newPMethodObj.put("value",methodTypeEach.paymentMethodId);
			newPMethodObj.put("text", methodTypeEach.description);
			cashMethodItemsJSON.add(newPMethodObj);
			allMethodItemsJSON.add(newPMethodObj);
		}
		// for payment method id in ap payments
		condList.clear();
//		condList.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, "Company"));
		if(UtilValidate.isNotEmpty(parameters.ownerPartyId) && parameters.ownerPartyId!=null){
			condList.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, parameters.ownerPartyId));
		}
		
		condList.add(EntityCondition.makeCondition("paymentMethodId", EntityOperator.NOT_EQUAL, "PAYMENTMETHOD2"));
		cond = EntityCondition.makeCondition(condList, EntityOperator.AND);
		bankPaymentMethodList = delegator.findList("PaymentMethod", cond, null, ["description"], null, false);
		bankPaymentMethodList.each{ methodTypeEach->
			JSONObject newPMethodObj = new JSONObject();
			newPMethodObj.put("value",methodTypeEach.paymentMethodId);
			newPMethodObj.put("text", methodTypeEach.description);
			bankMethodItemsJSON.add(newPMethodObj);
			allMethodItemsJSON.add(newPMethodObj);
		}
}
	voucherPaymentMethodJSON.put("CASH",cashMethodItemsJSON);
	voucherPaymentMethodJSON.put("BANK",bankMethodItemsJSON);
	voucherPaymentMethodJSON.put("ALL",allMethodItemsJSON);
	
	//Debug.log("allMethodItemsJSON=======>"+allMethodItemsJSON);
context.voucherPaymentMethodJSON=voucherPaymentMethodJSON;

voucherType=parameters.prefPaymentMethodTypeId;


invoiceCreateScreenTitle="";
if(UtilValidate.isEmpty(voucherType)){
	invoiceCreateScreenTitle = uiLabelMap.AccountingCreateNewPurchaseInvoice;
}else if(voucherType=="CASH"){
invoiceCreateScreenTitle = uiLabelMap.AccountingCreateNewCashPurchaseInvoice;
}else if(voucherType=="BANK"){
invoiceCreateScreenTitle = uiLabelMap.AccountingCreateNewBankPurchaseInvoice;
}
context.invoiceCreateScreenTitle=invoiceCreateScreenTitle;
context.voucherType = voucherType;


prefPaymentMethodTypeId=voucherType;
context.invoiceCreateScreenTitle=invoiceCreateScreenTitle;

arScreenTitle="";
arVoucherType=parameters.arVoucherType;
if(UtilValidate.isEmpty(arVoucherType)){
	arScreenTitle = uiLabelMap.AccountingCreateNewSalesInvoice;
}else if(arVoucherType=="CASH"){
arScreenTitle = uiLabelMap.AccountingCreateNewCashSalesInvoice;
}else if(arVoucherType=="BANK"){
arScreenTitle = uiLabelMap.AccountingCreateNewBankSalesInvoice;
}
prefPaymentMethodTypeId=arVoucherType;
context.arScreenTitle=arScreenTitle;


