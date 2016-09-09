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

BranchList=[];
	branchMap = [:];
	branchName="";
	 branchId = parameters.branchId;
	 if(branchId){
	branch = delegator.findOne("PartyGroup",[partyId : branchId] , false);
	branchName = branch.get("groupName");
	 }
	branchMap.put("branchName", branchName);
	BranchList.add(branchMap);
	context.BranchList=BranchList;
	dctx = dispatcher.getDispatchContext();

	salesChannel = parameters.salesChannelEnumId;
	searchOrderId = parameters.orderId;
	facilityOrderId = parameters.orderId;
	facilityDeliveryDate = parameters.estimatedDeliveryDate;
	facilityDeliveryThruDate = parameters.estimatedDeliveryThruDate;
	productId = parameters.productId;
	facilityStatusId = parameters.statusId;
	facilityPartyId = parameters.partyId;
	screenFlag = parameters.screenFlag;
	tallyRefNO = parameters.tallyRefNO;
	
	facilityDateStart = null;
	facilityDateEnd = null;
	if(UtilValidate.isNotEmpty(facilityDeliveryDate)){
		def sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			transDate = new java.sql.Timestamp(sdf.parse(facilityDeliveryDate+" 00:00:00").getTime());
		} catch (ParseException e) {
			Debug.logError(e, "Cannot parse date string: " + facilityDeliveryDate, "");
		}
		facilityDateStart = UtilDateTime.getDayStart(transDate);
		facilityDateEnd = UtilDateTime.getDayEnd(transDate);
	}
	transThruDate = null;
	if(UtilValidate.isNotEmpty(facilityDeliveryThruDate)){
		def sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			transThruDate = new java.sql.Timestamp(sdf.parse(facilityDeliveryThruDate+" 00:00:00").getTime());
		} catch (ParseException e) {
			Debug.logError(e, "Cannot parse date string: " + facilityDeliveryThruDate, "");
		}
		facilityDateEnd = UtilDateTime.getDayEnd(transThruDate);
	}
	
	JSONArray orderList=new JSONArray();
	 List condList = [];
	
	inputFields = [:];
	inputFields.put("noConditionFind", "Y");
	inputFields.put("hideSearch","Y");
	
	branchList=[];
	condListb = [];
	if(UtilValidate.isNotEmpty(branchId)){
	condListb.add(EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, branchId));
	condListb.add(EntityCondition.makeCondition("roleTypeIdFrom", EntityOperator.EQUALS, "PARENT_ORGANIZATION"));
	condListb = EntityCondition.makeCondition(condListb, EntityOperator.AND);
	PartyRelationship = delegator.findList("PartyRelationship", condListb,UtilMisc.toSet("partyIdTo"), null, null, false);
	branchList=EntityUtil.getFieldListFromEntityList(PartyRelationship, "partyIdTo", true);
	}
	
	if(!branchList)
	branchList.add(branchId);
	
	orderRoles = [];
	branchBasedOrderIds=[];
	if(UtilValidate.isNotEmpty(branchList)){
		custCondList = [];
		if((UtilValidate.isNotEmpty(branchList))){
			custCondList.add(EntityCondition.makeCondition("partyId", EntityOperator.IN, branchList));
		}
		custCondList.add(EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "BILL_FROM_VENDOR"));
		custCond = EntityCondition.makeCondition(custCondList, EntityOperator.AND);
		orderRoles = delegator.findList("OrderRole", custCond, null, null, null, false);
		branchBasedOrderIds = EntityUtil.getFieldListFromEntityList(orderRoles, "orderId", true);
	}
	
	
	custOrderRoles =[];
	custBasededOrderIds=[];
	if(UtilValidate.isNotEmpty(facilityPartyId)){
		custCondList = [];
		custCondList.add(EntityCondition.makeCondition("partyId",  EntityOperator.EQUALS, facilityPartyId));
		custCondList.add(EntityCondition.makeCondition("orderId",  EntityOperator.IN, branchBasedOrderIds));
		custCondList.add(EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "SHIP_TO_CUSTOMER"));
		custCond = EntityCondition.makeCondition(custCondList, EntityOperator.AND);
		custOrderRoles = delegator.findList("OrderRole", custCond, null, null, null, false);
		branchBasedOrderIds = EntityUtil.getFieldListFromEntityList(custOrderRoles, "orderId", true);
	}
	
		condList.add(EntityCondition.makeCondition("orderId" ,EntityOperator.IN, branchBasedOrderIds));
		condList.add(EntityCondition.makeCondition("statusId" ,EntityOperator.NOT_EQUAL,"ORDER_CANCELLED"));
		condList.add(EntityCondition.makeCondition("purposeTypeId" ,EntityOperator.EQUALS, "BRANCH_SALES"));
		condList.add(EntityCondition.makeCondition("shipmentId" ,EntityOperator.EQUALS, null)); // Review

	if(parameters.indentDateSort)
	dateSort = parameters.indentDateSort;
	else
	dateSort = "-orderDate";
	
	cond = EntityCondition.makeCondition(condList, EntityOperator.AND);
	List<String> payOrderBy = UtilMisc.toList(dateSort,"-orderId");
	
	resultList = [];
	forIndentsCount = [];
	int totalIndents = 0
	orderHeader = delegator.findList("OrderHeader", cond, null, payOrderBy, null, false);
	orderIds=EntityUtil.getFieldListFromEntityList(orderHeader, "orderId", true);
	
	orderHeader.each{ eachHeader ->
		orderId = eachHeader.orderId;
		orderParty = EntityUtil.filterByCondition(orderRoles, EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId));
		
		custOrderRoles.clear();
		partyId = "";
		if(UtilValidate.isEmpty(partyId)){
			custCondList = [];
			custCondList.add(EntityCondition.makeCondition("orderId",  EntityOperator.EQUALS, orderId));
			custCondList.add(EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "SHIP_TO_CUSTOMER"));
			custCond = EntityCondition.makeCondition(custCondList, EntityOperator.AND);
			custOrderRoles = delegator.findList("OrderRole", custCond, null, null, null, false);
		}
			custBasededOrderIds = EntityUtil.getFieldListFromEntityList(custOrderRoles, "partyId", true);
			partyId = EntityUtil.getFirst(custOrderRoles).get("partyId");
		
		billFromOrderParty = EntityUtil.filterByCondition(orderRoles, EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId));
		billFromVendorPartyId = "";
		if(billFromOrderParty){
			billFromVendorPartyId = billFromOrderParty.get(0).get("partyId");
		}
		
		partyName = PartyHelper.getPartyName(delegator, partyId, false);
		JSONObject tempData = new JSONObject();
		tempData.put("partyId", partyId);
		tempData.put("billFromVendorPartyId", billFromVendorPartyId);
		tempData.put("partyName", partyName);
		
		if(eachHeader.tallyRefNo)
		 tempData.put("tallyRefNo", eachHeader.tallyRefNo);
		else
		 tempData.put("tallyRefNo", "NA");
		
		orderNo ="NA";
		orderHeaderSequences = delegator.findList("OrderHeaderSequence",EntityCondition.makeCondition("orderId", EntityOperator.EQUALS , eachHeader.orderId)  , null, null, null, false );
		if(UtilValidate.isNotEmpty(orderHeaderSequences)){
			orderSeqDetails = EntityUtil.getFirst(orderHeaderSequences);
			orderNo = orderSeqDetails.orderNo;
		}
		exprCondList=[];
		exprCondList.add(EntityCondition.makeCondition("toOrderId", EntityOperator.EQUALS, orderId));
		exprCondList.add(EntityCondition.makeCondition("orderAssocTypeId", EntityOperator.EQUALS, "BackToBackOrder"));
		EntityCondition disCondition = EntityCondition.makeCondition(exprCondList, EntityOperator.AND);
		OrderAss = EntityUtil.getFirst(delegator.findList("OrderAssoc", disCondition, null,null,null, false));
		
		POorder="NA";
		isgeneratedPO="N";
		if(OrderAss){
			POorder=OrderAss.get("orderId");
			isgeneratedPO = "Y";
		}
		poSquenceNo="NA";
		poOrderHeaderSequences = delegator.findList("OrderHeaderSequence",EntityCondition.makeCondition("orderId", EntityOperator.EQUALS , POorder)  , null, null, null, false );
		if(UtilValidate.isNotEmpty(poOrderHeaderSequences)){
			poOrderSeqDetails = EntityUtil.getFirst(poOrderHeaderSequences);
			poSquenceNo = poOrderSeqDetails.orderNo;
		}
		tempData.put("POorder", POorder);
		tempData.put("poSquenceNo", poSquenceNo);
		tempData.put("isgeneratedPO", isgeneratedPO);
	
		exprList=[];
		exprList.add(EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId));
		exprList.add(EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "SUPPLIER"));
		EntityCondition discontinuationDateCondition = EntityCondition.makeCondition(exprList, EntityOperator.AND);
		supplierPartyId="";
		productStoreId="";
		supplierDetails = EntityUtil.getFirst(delegator.findList("OrderRole", discontinuationDateCondition, null,null,null, false));
		if(supplierDetails){
			supplierPartyId=supplierDetails.get("partyId");
		}
		supplierPartyName="";
		if(supplierPartyId){
			supplierPartyName = PartyHelper.getPartyName(delegator, supplierPartyId, false);
		}
		productStoreId=eachHeader.productStoreId;
		tempData.put("supplierPartyId", supplierPartyId);
		tempData.put("totalIndents", totalIndents);
		tempData.put("storeName", productStoreId);
		tempData.put("supplierPartyName", supplierPartyName);
		tempData.put("orderNo", orderNo);
		tempData.put("orderId", eachHeader.orderId);
		tempData.put("orderDate", String.valueOf(eachHeader.estimatedDeliveryDate).substring(0,10));
		tempData.put("statusId", eachHeader.statusId);
		
		if(UtilValidate.isNotEmpty(eachHeader.getBigDecimal("grandTotal"))){
			tempData.put("orderTotal", eachHeader.getBigDecimal("grandTotal"));
		}
		productStoreId=eachHeader.productStoreId;
		
		conditonList = [];
		conditonList.add(EntityCondition.makeCondition("orderId" ,EntityOperator.EQUALS, orderId));
		cond = EntityCondition.makeCondition(conditonList, EntityOperator.AND);
		OrderPaymentPreference = delegator.findList("OrderPaymentPreference", cond, null, null, null ,false);
		double paidAmt = 0;
		
		paymentIdsOfIndentPayment = [];
		if(OrderPaymentPreference){
		orderPreferenceIds = EntityUtil.getFieldListFromEntityList(OrderPaymentPreference,"orderPaymentPreferenceId", true);
		conditonList.clear();
		conditonList.add(EntityCondition.makeCondition("paymentPreferenceId" ,EntityOperator.IN,orderPreferenceIds));
		conditonList.add(EntityCondition.makeCondition("statusId" ,EntityOperator.NOT_EQUAL, "PMNT_VOID"));
		cond = EntityCondition.makeCondition(conditonList, EntityOperator.AND);
		PaymentList = delegator.findList("Payment", cond, null, null, null ,false);
		paymentIdsOfIndentPayment = EntityUtil.getFieldListFromEntityList(PaymentList,"paymentId", true);
		
		for (eachPayment in PaymentList) {
			paidAmt = paidAmt+eachPayment.get("amount");
		}
		
	  }
		conditonList.clear();
		conditonList.add(EntityCondition.makeCondition("orderId" ,EntityOperator.EQUALS,orderId));
		cond = EntityCondition.makeCondition(conditonList, EntityOperator.AND);
		OrderItemBillingList = delegator.findList("OrderItemBilling", cond, null, null, null ,false);
		invoiceIds = EntityUtil.getFieldListFromEntityList(OrderItemBillingList,"invoiceId", true);
		
		if(invoiceIds){
		conditonList.clear();
		conditonList.add(EntityCondition.makeCondition("invoiceId" ,EntityOperator.IN,invoiceIds));
		cond = EntityCondition.makeCondition(conditonList, EntityOperator.AND);
		PaymentApplicationList = delegator.findList("PaymentApplication", cond, null, null, null ,false);
		
			for (eachList in PaymentApplicationList) {
				 if(!paymentIdsOfIndentPayment.contains(eachList.paymentId))
					paidAmt = paidAmt+eachList.amountApplied;
			}
		}
		tempData.put("paidAmt", paidAmt);
		grandTOT = eachHeader.getBigDecimal("grandTotal");
		balance = grandTOT-paidAmt;
		tempData.put("balance", balance);
		orderList.add(tempData);
	}
	
	if (UtilValidate.isNotEmpty(resultList)) {
		try {
			resultList.close();
		} catch (Exception e) {
			Debug.logWarning(e, module);
		}
	}
	
	context.orderList=orderList;
	