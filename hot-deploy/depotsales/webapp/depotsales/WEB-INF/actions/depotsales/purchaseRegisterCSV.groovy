import org.ofbiz.base.util.UtilDateTime
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
import org.ofbiz.service.GenericServiceException;

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
	
	SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy");
	dayend = null;
	daystart = null;
	Timestamp fromDate;
	Timestamp thruDate;
	
	partyfromDate=parameters.fromDate;
	partythruDate=parameters.thruDate;
	
	productCategory=parameters.categoryId;
	context.partyfromDate=partyfromDate;
	context.partythruDate=partythruDate;
	daystart = null;
	dayend = null;
	if(UtilValidate.isNotEmpty(partyfromDate)){
		try {
			fromDate = new java.sql.Timestamp(sdf.parse(partyfromDate).getTime());
			daystart = UtilDateTime.getDayStart(fromDate);
			 } catch (ParseException e) {
				 //////Debug.logError(e, "Cannot parse date string: " + parameters.partyfromDate, "");
			}
	}
	if(UtilValidate.isNotEmpty(partythruDate)){
	   try {
		   thruDate = new java.sql.Timestamp(sdf.parse(partythruDate).getTime());
		   dayend = UtilDateTime.getDayEnd(thruDate);
	   } catch (ParseException e) {
		   //////Debug.logError(e, "Cannot parse date string: " + parameters.partythruDate, "");
			}
	}
	context.daystart=daystart
	context.dayend=dayend
	
	JSONArray orderList=new JSONArray();
	 List condList = [];
	
	inputFields = [:];
	inputFields.put("noConditionFind", "Y");
	inputFields.put("hideSearch","Y");
	branchIdForAdd="";
	branchList=[];
	condListb = [];
	if(UtilValidate.isNotEmpty(branchId)){
	condListb.add(EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, branchId));
	condListb.add(EntityCondition.makeCondition("roleTypeIdFrom", EntityOperator.EQUALS, "PARENT_ORGANIZATION"));
	condListb = EntityCondition.makeCondition(condListb, EntityOperator.AND);
	PartyRelationship = delegator.findList("PartyRelationship", condListb,UtilMisc.toSet("partyIdTo"), null, null, false);
	branchList=EntityUtil.getFieldListFromEntityList(PartyRelationship, "partyIdTo", true);
	}
	if(!branchList){
		condListb2 = [];
		//condListb2.add(EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS,"%"));
		condListb2.add(EntityCondition.makeCondition("partyIdTo", EntityOperator.EQUALS, branchId));
		condListb2.add(EntityCondition.makeCondition("roleTypeIdFrom", EntityOperator.EQUALS, "PARENT_ORGANIZATION"));
		condListb2.add(EntityCondition.makeCondition("roleTypeIdTo", EntityOperator.EQUALS, "ORGANIZATION_UNIT"));
		cond = EntityCondition.makeCondition(condListb2, EntityOperator.AND);
		
		PartyRelationship1 = delegator.findList("PartyRelationship", cond,UtilMisc.toSet("partyIdFrom"), null, null, false);
		if(PartyRelationship1){
		branchDetails = EntityUtil.getFirst(PartyRelationship1);
		branchIdForAdd=branchDetails.partyIdFrom;
		}
	}
	else{
		if(branchId){
		branchIdForAdd=branchId;
		}
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
		condList.add(EntityCondition.makeCondition("entryDate", EntityOperator.GREATER_THAN_EQUAL_TO, daystart));
		condList.add(EntityCondition.makeCondition("entryDate", EntityOperator.LESS_THAN_EQUAL_TO, dayend));
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
	BOAddress="";
	BOEmail="";
	if(branchIdForAdd){
	branchContextForADD=[:];
	branchContextForADD.put("branchId",branchIdForAdd);
	try{
		resultCtx = dispatcher.runSync("getBoHeader", branchContextForADD);
		if(ServiceUtil.isError(resultCtx)){
			Debug.logError("Problem in BO Header ", module);
			return ServiceUtil.returnError("Problem in fetching financial year ");
		}
		if(resultCtx.get("boHeaderMap")){
			boHeaderMap=resultCtx.get("boHeaderMap");
			
			if(boHeaderMap.get("header0")){
				BOAddress=boHeaderMap.get("header0");
			}
			if(boHeaderMap.get("header1")){
				BOEmail=boHeaderMap.get("header1");
			}
		}
	}catch(GenericServiceException e){
	Debug.logError(e, module);
	return ServiceUtil.returnError(e.getMessage());
	}
	context.BOAddress=BOAddress;
	context.BOEmail=BOEmail;
	}
	/*
	headerData2=[:];
	headerData2.put("orderDate", "INDENT DATE");
	//headerData2.put("orderId", "_");
	headerData2.put("orderNo", "SEQUENCE ID");
	headerData2.put("Qty", "QTY");
	headerData2.put("unit", "UNIT");
	headerData2.put("poQty", "PO QTY");
	headerData2.put("salInv", "SAL INVOICE");
	headerData2.put("salDate", "SAL DATE");
	headerData2.put("salVal", "SAL VALUE");
	headerData2.put("transporter", "TRANSPORTER");
	headerData2.put("milInv", "MIL INVOICE");
	headerData2.put("value", "VALUE");
	headerData2.put("paymentReceipt", "PAYMENT RECEIPT");
	headerData2.put("amount", "AMOUNT");
	headerData2.put("weaverName", "WEAVER NAME");
	headerData2.put("poNo", "PO NO");
	headerData2.put("poDate", "PO DATE");
	headerData2.put("supplierName", "SUPPLIER NAME");
	orderList.add(headerData2);
	*/
	
	stylesMap=[:];
	if(branchId){
		stylesMap.put("mainHeader1", "NATIONAL HANDLOOM DEVELOPMENT CORPORATION LTD.");
		stylesMap.put("mainHeader2", BOAddress);
		stylesMap.put("mainHeader3", "Purchase Register Report");	
	}
	else{
		stylesMap.put("mainHeader1", "NATIONAL HANDLOOM DEVELOPMENT CORPORATION LTD.");
		stylesMap.put("mainHeader2", "Purchase Register Report");	
	}
	stylesMap.put("mainHeaderFontName","Arial");
	stylesMap.put("mainHeadercellHeight",300);
	stylesMap.put("mainHeadingCell",5);
	stylesMap.put("mainHeaderFontSize",10);
	stylesMap.put("mainHeaderBold",true);
	stylesMap.put("columnHeaderBgColor",false);
	stylesMap.put("columnHeaderFontName","Arial");
	stylesMap.put("columnHeaderFontSize",10);
	stylesMap.put("autoSizeCell",true);
	stylesMap.put("columnHeaderCellHeight",300);
	request.setAttribute("stylesMap", stylesMap);
	request.setAttribute("enableStyles", true);
	orderList.add(stylesMap);
	headerData=[:];
	headerData.put("orderDate", "Indent Date");
	//headerData2.put("orderId", "_");
	headerData.put("orderNo", "Sequence Id");
	headerData.put("Qty", "Qty");
	headerData.put("unit", "Unit");
	headerData.put("poQty", "PO Qty");
	headerData.put("salInv", "Sale Invoice");
	headerData.put("salDate", "Sale Date");
	headerData.put("salVal", "Sale Value");
	headerData.put("transporter", "Transporter");
	headerData.put("milInv", "Mil Invoice");
	headerData.put("value", "Value");
	headerData.put("paymentReceipt", "Payment Receipt");
	headerData.put("amount", "Amount");
	headerData.put("weaverName", "Weaver Name");
	headerData.put("poSquenceNo", "PO Sequence No");
	headerData.put("poDate", "PO Date");
	headerData.put("supplierName", "Supplier Name");
	headerData.put("salquantity", "Sale Quantity");
	
	orderList.add(headerData);
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
		billFromVendor = PartyHelper.getPartyName(delegator, billFromVendorPartyId, false);
		JSONObject tempData = new JSONObject();
		tempData.put("partyId", partyId);
		tempData.put("weaverName", partyName);
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
		/*exprCondList=[];
		exprCondList.add(EntityCondition.makeCondition("toOrderId", EntityOperator.EQUALS, orderId));
		exprCondList.add(EntityCondition.makeCondition("orderAssocTypeId", EntityOperator.EQUALS, "BackToBackOrder"));
		EntityCondition disCondition = EntityCondition.makeCondition(exprCondList, EntityOperator.AND);
		OrderAss = EntityUtil.getFirst(delegator.findList("OrderAssoc", disCondition, null,null,null, false));
		*/
		resultCtx = dispatcher.runSync("getAssociateOrder",UtilMisc.toMap("userLogin",userLogin, "orderId", orderId));
		POorder="NA";
		isgeneratedPO="N";
		if(resultCtx.orderId){
			POorder=resultCtx.orderId;
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
		tempData.put("supplierName", supplierPartyName);
		tempData.put("orderNo", orderNo);
		tempData.put("orderId", eachHeader.orderId);
		tempData.put("orderDate", UtilDateTime.toDateString(eachHeader.estimatedDeliveryDate, "dd/MM/yyyy"));
		//tempData.put("orderDate", String.valueOf(eachHeader.estimatedDeliveryDate).substring(0,10));
		tempData.put("statusId", eachHeader.statusId);
		if(UtilValidate.isNotEmpty(eachHeader.getBigDecimal("grandTotal"))){
			tempData.put("orderTotal", eachHeader.getBigDecimal("grandTotal"));
			}
		ordQty=0;
		poId="";
		salValue=0;
		poQty=0;
	unitPrice=0;
		unitpricetotalamount=0;
		totalamount1=0;
		salquantity1=0;
		custCondList.clear();
		custCondList.add(EntityCondition.makeCondition("toOrderId",  EntityOperator.EQUALS, orderId));
		custCondList.add(EntityCondition.makeCondition("orderAssocTypeId", EntityOperator.EQUALS, "BackToBackOrder"));
		custCond1 = EntityCondition.makeCondition(custCondList, EntityOperator.AND);
		orderAssocList = delegator.findList("OrderAssoc", custCond1, null, null, null, false);
		if(UtilValidate.isNotEmpty(orderAssocList)){
			orderAssoc = EntityUtil.getFirst(orderAssocList);
			poId=orderAssoc.get("orderId");
			custCondList.clear();
			custCondList.add(EntityCondition.makeCondition("orderId",  EntityOperator.IN, UtilMisc.toList(orderId,poId)));
			custCond2 = EntityCondition.makeCondition(custCondList, EntityOperator.AND);
			orderItemList = delegator.findList("OrderItem", custCond2, null, null, null, false);
			if(UtilValidate.isNotEmpty(orderItemList)){
				orderItemList1 = EntityUtil.filterByCondition(orderItemList, EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId));
				if(UtilValidate.isNotEmpty(orderItemList)){
					for(orderItem in orderItemList1){
						ordQty=ordQty+orderItem.quantity;
						
						
						
						unitpricetotalamount=unitpricetotalamount+orderItem.unitPrice;
					}
					
					
					unitPrice=(unitpricetotalamount)/(orderItemList1.size());
					unitPrice=Math.round(unitPrice * 100) / 100;
					totalamount1=totalamount1+((unitPrice)*(ordQty));
					}
				
				orderItemList2 = EntityUtil.filterByCondition(orderItemList, EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, poId));
				if(UtilValidate.isNotEmpty(orderItemList)){
					for(orderItem in orderItemList2){
						poQty=poQty+orderItem.quantity;
					}
				}
			}
			tempData.put("poNo", poId);
			tempData.put("poSquenceNo", poSquenceNo);
			tempData.put("poQty",poQty);
			tempData.put("Qty", ordQty);
		   // tempData.put("unit", indentPrice);
			//salinvoice=0;
			orderHeader = delegator.findOne("OrderHeader",[orderId : poId] , false);
			if(orderHeader){
				tempData.put("poDate", UtilDateTime.toDateString(orderHeader.orderDate, "dd/MM/yyyy"));
			}
			shipments = delegator.findList("Shipment", EntityCondition.makeCondition("primaryOrderId",  EntityOperator.EQUALS, poId), null, null, null, false);
			if(shipments){
				shipment = EntityUtil.getFirst(shipments);
				tempData.put("milInv", shipment.supplierInvoiceId);
				tempData.put("transporter", shipment.carrierName);
			}
			conditionList=[];
			conditionList.add(EntityCondition.makeCondition("orderId",  EntityOperator.EQUALS, orderId));
			conditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "INVOICE_CANCELLED"));
			cond3 = EntityCondition.makeCondition(conditionList, EntityOperator.AND);
			OrderItemBillingList = delegator.findList("OrderItemBillingAndInvoiceAndInvoiceItem", cond3, UtilMisc.toSet("quantity","invoiceId"), null, null, false);
			if(OrderItemBillingList){
				for(OrderItemBilling in OrderItemBillingList){
					conditionList.clear();
					conditionList.add(EntityCondition.makeCondition("invoiceId",  EntityOperator.EQUALS, OrderItemBilling.invoiceId));
					cond1 = EntityCondition.makeCondition(conditionList, EntityOperator.AND);
					invoiceItemList = delegator.findList("InvoiceItem", cond1, null, null, null, false);
					for(invoiceItem in invoiceItemList){
						if(UtilValidate.isNotEmpty(invoiceItem.itemValue)){
							salValue=salValue+invoiceItem.itemValue;
							//salinvoice=OrderItemBilling.invoiceId
							salquantity1=salquantity1+invoiceItem.quantity
							
							
							
						}
					}
			
					Invoice = delegator.findOne("Invoice",[invoiceId : OrderItemBilling.invoiceId] , false);
					tempData.put("salInv", OrderItemBilling.invoiceId);
					tempData.put("salDate", UtilDateTime.toDateString(Invoice.invoiceDate, "dd/MM/yyyy"));
					tempData.put("salquantity",salquantity1 );
						}
			}
			
			tempData.put("salVal", salValue);
			tempData.put("value", "-");
			tempData.put("paymentReceipt", "-");
			tempData.put("amount", "-");
			
			
			tempData.put("unit", unitPrice);
			tempData.put("amount",totalamount1);
			
			
			
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
