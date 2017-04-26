
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
import in.vasista.vbiz.byproducts.icp.ICPServices;

billingId = parameters.billingId;
billingType = parameters.billingType; 
isInvoiceFind = parameters.isInvoiceFind;
JSONArray openbillingPeriodsList = new JSONArray();
JSONArray closebillingPeriodsList = new JSONArray();
JSONArray rembInvoicesList = new JSONArray();

conditionList=[];
conditionList.add(EntityCondition.makeCondition("periodTypeId", EntityOperator.IN,["DEP_SHIP_REIMB_PRD","TEN_SUB_REIMB_PRD"]));
schemeTimePeriod = delegator.findList("SchemeTimePeriod",EntityCondition.makeCondition(conditionList, EntityOperator.AND), null, null, null, false);

List<GenericValue> openschemeTimePeriod = EntityUtil.filterByCondition(schemeTimePeriod, EntityCondition.makeCondition("isClosed", EntityOperator.EQUALS, "N"));
List<GenericValue> closeschemeTimePeriod = EntityUtil.filterByCondition(schemeTimePeriod, EntityCondition.makeCondition("isClosed", EntityOperator.EQUALS, "Y"));
JSONObject openPeriodBillings = new JSONObject();
JSONObject closePeriodBillings = new JSONObject();
JSONArray  newList1 = new JSONArray();
JSONArray  newList2 = new JSONArray();
JSONArray  newList3 = new JSONArray();
JSONArray  newList4 = new JSONArray();
for(eachId in openschemeTimePeriod)
{
	JSONObject newObj = new JSONObject();
	fromDateStr=eachId.getString("fromDate");
	thruDateStr=eachId.getString("thruDate");
	newObj.put("label", fromDateStr + "-" + thruDateStr + "[" + eachId.periodName +"]")
	newObj.put("value", eachId.schemeTimePeriodId)
	openbillingPeriodsList.add(newObj);
	if("TEN_SUB_REIMB_PRD".equals(eachId.getString("periodTypeId"))){
		newList1.add(newObj);
	}else{
		newList2.add(newObj);
	}
}
openPeriodBillings.put("TEN_SUB_REIMB_PRD", newList1)
openPeriodBillings.put("DEP_SHIP_REIMB_PRD", newList2)
for(eachId in closeschemeTimePeriod)
{
	JSONObject newObj = new JSONObject();
	fromDateStr=eachId.getString("fromDate");
	thruDateStr=eachId.getString("thruDate");
	newObj.put("label", fromDateStr + "-" + thruDateStr + "[" + eachId.periodName +"]")
	newObj.put("value", eachId.schemeTimePeriodId)
	closebillingPeriodsList.add(newObj);
	if("TEN_SUB_REIMB_PRD".equals(eachId.getString("periodTypeId"))){
		newList3.add(newObj);
	}else{
		newList4.add(newObj);
	}
}
closePeriodBillings.put("TEN_SUB_REIMB_PRD", newList3)
closePeriodBillings.put("DEP_SHIP_REIMB_PRD", newList4)
if(UtilValidate.isNotEmpty(isInvoiceFind)){
	fromDate=null;
	thruDate=null;
	schemeTimePeriod = delegator.findOne("SchemeTimePeriod",[schemeTimePeriodId : billingId] , false);
	frmDateStr=schemeTimePeriod.getString("fromDate")
	toDateStr=schemeTimePeriod.getString("thruDate")
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	if(UtilValidate.isNotEmpty(frmDateStr)){
		try {
			fromDate = new java.sql.Timestamp(sdf.parse(frmDateStr).getTime());
		}catch (ParseException e) {
			Debug.logError(e, "Cannot parse date string: " + frmDateStr, "");
			request.setAttribute("_ERROR_MESSAGE_","Cannot parse date string: "+ frmDateStr);
		}
		fromDate = UtilDateTime.getDayStart(fromDate);
	}
	if(UtilValidate.isNotEmpty(toDateStr)){
		try {
			thruDate = new java.sql.Timestamp(sdf.parse(toDateStr).getTime());
		}catch (ParseException e) {
			Debug.logError(e, "Cannot parse date string: " + toDateStr, "");
			request.setAttribute("_ERROR_MESSAGE_","Cannot parse date string: "+ toDateStr);
		}
		thruDate = UtilDateTime.getDayEnd(thruDate);
	}
	/*conditionList.clear();
	conditionList.add(EntityCondition.makeCondition("periodTypeId", EntityOperator.EQUALS, "TEN_SUB_REIMB_PERIOD"));
	conditionList.add(EntityCondition.makeCondition("fromDate", EntityOperator.GREATER_THAN_EQUAL_TO, new java.sql.Date(fromDate.getTime())));
	conditionList.add(EntityCondition.makeCondition("thruDate", EntityOperator.LESS_THAN_EQUAL_TO, new java.sql.Date(thruDate.getTime())));
	schemePeriodList = delegator.findList("SchemeTimePeriod",EntityCondition.makeCondition(conditionList, EntityOperator.AND), null, null, null, false);
	schemePeriod=EntityUtil.getFirst(schemePeriodList);
	shemeId="";
	if(UtilValidate.isNotEmpty(schemePeriod)){
		shemeId=schemePeriod.schemeTimePeriodId;
	}*/ 
	conditionList.clear();
	conditionList.add(EntityCondition.makeCondition("invoiceTypeId", EntityOperator.EQUALS, "SALES_INVOICE"));
	if("TEN_SUB_REIMB_PRD".equals(billingType)){
		conditionList.add(EntityCondition.makeCondition("invoiceItemTypeId", EntityOperator.IN, ["SER_CHRG_REMB","TEN_PER_SUB_REMB"]));
		conditionList.add(EntityCondition.makeCondition("purposeTypeId", EntityOperator.EQUALS, "TEN_PER_SUB_SER_CHRG"));
	}else{
		conditionList.add(EntityCondition.makeCondition("invoiceItemTypeId", EntityOperator.IN, ["SHIP_CHARG_REMB","DEPO_CHRG_REMB"]));
		conditionList.add(EntityCondition.makeCondition("purposeTypeId", EntityOperator.EQUALS, "DEP_SHIP_REMB_CHARG"));
	}
	conditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "INVOICE_CANCELLED"));
	if(UtilValidate.isNotEmpty(billingId)){
		conditionList.add(EntityCondition.makeCondition("periodBillingId", EntityOperator.EQUALS, billingId));
	}
	invoicesListing = delegator.findList("InvoiceAndItem",EntityCondition.makeCondition(conditionList, EntityOperator.AND), null, null, null, false);
	
	for(eachInvoice in invoicesListing)
	{
		schemeTimePeriod = delegator.findOne("SchemeTimePeriod",[schemeTimePeriodId : eachInvoice.periodBillingId] , false);
		invoiceItemType = delegator.findOne("InvoiceItemType",[invoiceItemTypeId : eachInvoice.invoiceItemTypeId] , false);
		JSONObject newObj = new JSONObject();
		newObj.put("invoiceId", eachInvoice.invoiceId);
		newObj.put("invoiceItemTypeId", invoiceItemType.description);
		if(UtilValidate.isNotEmpty(schemeTimePeriod)){
			fromDatestr=UtilDateTime.toDateString(schemeTimePeriod.fromDate, "dd/MM/yyyy")
			thruDatestr=UtilDateTime.toDateString(schemeTimePeriod.thruDate, "dd/MM/yyyy")
			newObj.put("billingPeriod",  fromDatestr + "-" + thruDatestr);
			newObj.put("periodName", schemeTimePeriod.periodName);
		}else{
			newObj.put("billingPeriod",  "");
			newObj.put("periodName", "");
		}
		newObj.put("invoiceDate", UtilDateTime.toDateString(eachInvoice.invoiceDate, "dd/MM/yyyy"));
		newObj.put("itemValue", eachInvoice.itemValue);
		rembInvoicesList.add(newObj)
	}
	context.rembInvoicesList=rembInvoicesList;
	request.setAttribute("rembInvoicesList",rembInvoicesList);
	return "success";
}
context.closePeriodBillings=closePeriodBillings;
context.openPeriodBillings=openPeriodBillings;
context.billingPeriodsList=openbillingPeriodsList;
context.closebillingPeriodsList=closebillingPeriodsList;



