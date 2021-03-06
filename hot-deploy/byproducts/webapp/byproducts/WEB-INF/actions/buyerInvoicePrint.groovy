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

import java.text.DateFormat;
import org.ofbiz.accounting.invoice.InvoiceWorker;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.base.util.Debug;
import java.util.*;

import org.ofbiz.entity.*;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.util.EntityUtil;
import javolution.util.FastMap;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.party.party.PartyWorker;
import org.ofbiz.product.product.ProductWorker;
import in.vasista.vbiz.byproducts.ByProductNetworkServices;
invoiceDetailList = [];
vatMap = [:];
reportTitle = [:];

today = UtilDateTime.nowTimestamp();
/*
cxt = [:];
cxt.put("userLogin", userLogin);
cxt.put("productId", "518");
cxt.put("partyId", "ABDUL");
cxt.put("priceDate", UtilDateTime.nowTimestamp());
cxt.put("productStoreId", "1006");
cxt.put("productPriceTypeId", "MRP_IS");
cxt.put("geoTax", "VAT");

result = ByProductNetworkServices.calculateStoreProductPrices(delegator, dispatcher, cxt);
Debug.log("result############"+result);
*/
/*finalProdList = [];
prodList= ProductWorker.getProductsByCategory(delegator ,"ICE_CREAM_AMUL" ,null);
finalProdList.addAll(prodList);
prodList= ProductWorker.getProductsByCategory(delegator ,"ICE_CREAM_NANDINI" ,null);
finalProdList.addAll(prodList);
finalProdIdsList = EntityUtil.getFieldListFromEntityList(finalProdList, "productId", true);
productPrices = delegator.findList("ProductPrice", EntityCondition.makeCondition("productId", EntityOperator.IN, finalProdIdsList), null, null, null, false);
duplicateProdList = [];
finalProdIdsList.each{ eachProdId ->
	prodPriceList = EntityUtil.filterByCondition(productPrices, EntityCondition.makeCondition("productId", EntityOperator.EQUALS, eachProdId));
	if(prodPriceList.size()>5){
		duplicateProdList.add(eachProdId);
	}
	
}*/
//Debug.log("##########duplicateProdList######"+duplicateProdList);
String salesChannelEnumId="";
invoiceSlipsMap = [:];
totVatsPercesntMap = [:];
orderMsg="";
shipmentId = parameters.shipmentId;
conditionList = [];
conditionList.add(EntityCondition.makeCondition("shipmentId", EntityOperator.EQUALS, shipmentId));
conditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "ORDER_CANCELLED"));
condition = EntityCondition.makeCondition(conditionList, EntityOperator.AND);
orderHeaders = delegator.findList("OrderHeader", condition, UtilMisc.toSet("orderId", "salesChannelEnumId","orderMessage"), null, null, false);
if(orderHeaders){
	orderMessage = orderHeaders.orderMessage;
	orderMessage.each{ eachorderMSg ->
		if(UtilValidate.isNotEmpty(eachorderMSg)){
		orderMsg=eachorderMSg;
		}
	}
}
context.orderMsg=orderMsg;
orderIds = EntityUtil.getFieldListFromEntityList(orderHeaders, "orderId", true);

taxLabelFlag = "otherChannel";
if(orderHeaders){
	ordHdr = EntityUtil.getFirst(orderHeaders);
	salesChannelEnumId=ordHdr.salesChannelEnumId;
	if(ordHdr.salesChannelEnumId && (ordHdr.salesChannelEnumId == "ICP_BELLARY_CHANNEL" || ordHdr.salesChannelEnumId == "ICP_NANDINI_CHANNEL" || ordHdr.salesChannelEnumId == "ICP_AMUL_CHANNEL")){
		taxLabelFlag = "icpChannel";
	}
}


conditionList.clear();
conditionList.add(EntityCondition.makeCondition("orderId", EntityOperator.IN, orderIds));
conditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "INVOICE_CANCELLED"));
cond = EntityCondition.makeCondition(conditionList, EntityOperator.AND);
invoices = delegator.findList("OrderItemBillingAndInvoiceAndInvoiceItem", cond, UtilMisc.toSet("invoiceId"), null, null, false);
invoiceIds = EntityUtil.getFieldListFromEntityList(invoices, "invoiceId", true);

invoiceIds.each { invoiceId ->
	invoiceDetailMap = [:];
	invoice = delegator.findOne("Invoice", [invoiceId : invoiceId], false);
	invoiceOrderItemList = delegator.findByAnd("OrderItemBillingAndInvoiceAndInvoiceItem", [invoiceId : invoiceId], null);
	orderIds = EntityUtil.getFieldListFromEntityList(invoiceOrderItemList, "orderId", true);
	orderId = "";
	if(orderIds){
		orderId = orderIds.get(0);
	}
	partyIdFrom = invoice.partyIdFrom;
	partyIdTo = invoice.partyId;
	fromPartyDetail = (Map)(PartyWorker.getPartyIdentificationDetails(delegator, partyIdFrom)).get("partyDetails");
	//give prefrence to ShipToCustomer
	conditionList.clear();
	conditionList.add(EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId));
	conditionList.add(EntityCondition.makeCondition("roleTypeId", EntityOperator.IN, UtilMisc.toList("SHIP_TO_CUSTOMER", "PLACING_CUSTOMER")));
	shipCond = EntityCondition.makeCondition(conditionList, EntityOperator.AND);
	orderShipRole = delegator.findList("OrderRole", shipCond, null, null, null, false);
	placingPartyId = "";
	shipTopartyId = "";
	if(UtilValidate.isNotEmpty(orderShipRole)){
		placingParty = EntityUtil.filterByCondition(orderShipRole, EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "PLACING_CUSTOMER"));
		if(placingParty){
			placingPartyId = placingParty.get(0).get("partyId");
		}
		shipingParty = EntityUtil.filterByCondition(orderShipRole, EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "SHIP_TO_CUSTOMER"));
		if(shipingParty){
			shipTopartyId = shipingParty.get(0).get("partyId");
		}
		//shipTopartyId = (EntityUtil.getFirst(orderShipRole)).getString("partyId");
	 }
	//if shipToCustomer NotEmpty
	toPartyDetail = [:];
	toPartyId = "";
	if(placingPartyId){
		toPartyId = placingPartyId;
	}
	else if(shipTopartyId){
		toPartyId = shipTopartyId;
	}
	else{
		toPartyId=partyIdTo;
	}
	toPartyDetail = (Map)(PartyWorker.getPartyIdentificationDetails(delegator, toPartyId)).get("partyDetails");
	invoiceDetailMap.put("fromPartyDetail", fromPartyDetail);
	invoiceDetailMap.put("toPartyDetail", toPartyDetail);
	invoiceSequence = delegator.findList("BillOfSaleInvoiceSequence", EntityCondition.makeCondition("invoiceId", EntityOperator.EQUALS, invoiceId), null, null,null, false);
	
	OrderHeader = delegator.findOne("OrderHeader", [orderId: orderId], false);
	shipmentId = OrderHeader.shipmentId;
	
	shipment = delegator.findOne("Shipment", [shipmentId : shipmentId], false);
	billingAddress = [:];
	if ("PURCHASE_INVOICE".equals(invoice.invoiceTypeId)) {
		billingAddress = InvoiceWorker.getSendFromAddress(invoice);
	} else {
		//if shipRole not empty then assign to OrderRole
		if(UtilValidate.isNotEmpty(orderShipRole)){
			if(placingPartyId && shipTopartyId && !(placingPartyId.equals(shipTopartyId))){
				partyAddress = dispatcher.runSync("getPartyPostalAddress", [partyId: placingPartyId, userLogin: userLogin]);
				billingTempMap=[:];
				billingTempMap["toName"]=""
				billingTempMap["address1"]="";
				billingTempMap["address2"]="";
				billingTempMap["city"]="";
				billingTempMap["postalCode"]="";
				if(UtilValidate.isNotEmpty(partyAddress)){
					if(UtilValidate.isNotEmpty(partyAddress)){
						billingTempMap["toName"]=org.ofbiz.party.party.PartyHelper.getPartyName(delegator,placingPartyId, false);
						billingTempMap["address1"]=partyAddress.get("address1");
						billingTempMap["address2"]=partyAddress.get("address2");
						billingTempMap["city"]=partyAddress.get("city");
						billingTempMap["postalCode"]=partyAddress.get("postalCode");
					}
				}
				billingAddress.put("orderedPartyAddress", billingTempMap);
			}
			if(shipTopartyId){
				partyAddress = dispatcher.runSync("getPartyPostalAddress", [partyId: shipTopartyId, userLogin: userLogin]);
				billingTempMap=[:];
				billingTempMap["toName"]=""
				billingTempMap["address1"]="";
				billingTempMap["address2"]="";
				billingTempMap["city"]="";
				billingTempMap["postalCode"]="";
				if(UtilValidate.isNotEmpty(partyAddress)){
					if(UtilValidate.isNotEmpty(partyAddress)){
						billingTempMap["toName"]=org.ofbiz.party.party.PartyHelper.getPartyName(delegator,shipTopartyId, false);
						billingTempMap["address1"]=partyAddress.get("address1");
						billingTempMap["address2"]=partyAddress.get("address2");
						billingTempMap["city"]=partyAddress.get("city");
						billingTempMap["postalCode"]=partyAddress.get("postalCode");
					}
				}
				billingAddress.put("shippingAddress", billingTempMap);
			}
		}else{
			billingAddress = InvoiceWorker.getBillToAddress(invoice);
		}
	}
	invoiceDetailMap.put("invoice", invoice);
	invoiceDetailMap.put("shipment", shipment);
	invoiceDetailMap.put("billingAddress", billingAddress);
	invoiceItems = delegator.findList("InvoiceItem", EntityCondition.makeCondition("invoiceId", EntityOperator.EQUALS, invoiceId), null, null, null, false);
	vatPercentMap=[:];
	vatPercent=null;	bedPercent=null;	cstPercent=null;
	invoiceItems.each{ eachinvoicePercent ->
		if(UtilValidate.isNotEmpty(eachinvoicePercent.vatPercent)){
			vatPercent=eachinvoicePercent.vatPercent;
		}
		if(UtilValidate.isNotEmpty(eachinvoicePercent.cstPercent)){
		cstPercent=eachinvoicePercent.cstPercent;
		}
		if(UtilValidate.isNotEmpty(eachinvoicePercent.bedPercent)){
		bedPercent=eachinvoicePercent.bedPercent;
		}
		vatPercentMap.put("vatPercent",vatPercent);
		vatPercentMap.put("cstPercent",cstPercent);
		vatPercentMap.put("bedPercent",bedPercent);
	}
	totVatsPercesntMap.put(invoiceId,vatPercentMap);
	
	productIds = EntityUtil.getFieldListFromEntityList(invoiceItems, "productId", true);
	products = delegator.findList("Product", EntityCondition.makeCondition("productId", EntityOperator.IN, productIds), null, null, null, false);
	
	prodCategories = ["ICE_CREAM_NANDINI", "ICE_CREAM_AMUL", "MILK_POWDER", "FG_STORE"];
	condList = [];
	condList.add(EntityCondition.makeCondition("productCategoryId", EntityOperator.IN, prodCategories));
	condList.add(EntityCondition.makeCondition("productId", EntityOperator.IN, productIds));
	condExpr = EntityCondition.makeCondition(condList, EntityOperator.AND);
	productCategories = delegator.findList("ProductCategoryAndMember", condExpr, null, null, null, false);
	
	chapters = EntityUtil.getFieldListFromEntityList(productCategories, "chapterNum", true);
	chapterMap = [:];
	chapters.each{ eachChap ->
		chapterDetails = EntityUtil.filterByCondition(productCategories, EntityCondition.makeCondition("chapterNum", EntityOperator.EQUALS, eachChap));
		chapDetail = EntityUtil.getFirst(chapterDetails);
		chapter = "";
		subHeading = "";
		displayStr = "";
		description = chapDetail.productCategoryId;
		if(chapDetail.description){
			description = chapDetail.description;
		}
		if(chapDetail.chapterNum){
			chapter = chapDetail.chapterNum;
			displayStr += chapter;
		}
		if(chapDetail.subHeading){
			subHeading = chapDetail.subHeading;
			displayStr += "/"+ subHeading;
		}
		chapterMap.put(description, displayStr)
	}
	
	
	orderItemAttributes = delegator.findList("OrderItemAttribute", EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId), null, null, null, false);
	invoiceItemDetail = [];
	invoiceTaxItems = [:];
	taxList = [];
	invoiceItems.each{ eachItem ->
		
		if(eachItem.productId){
			
			cxt = [:];
			cxt.put("userLogin", userLogin);
			cxt.put("productId", eachItem.productId);
			cxt.put("partyId", partyIdTo);
			cxt.put("priceDate", UtilDateTime.nowTimestamp());
			cxt.put("productStoreId", "_NA_");
			cxt.put("productPriceTypeId", "MRP_IS");
			cxt.put("geoTax", "VAT");
			
			result = ByProductNetworkServices.calculateStoreProductPrices(delegator, dispatcher, cxt);
			mrpPrice = result.get("totalPrice");
		
			orderSeqList = EntityUtil.filterByAnd(invoiceOrderItemList, UtilMisc.toMap("invoiceId", eachItem.invoiceId, "invoiceItemSeqId", eachItem.invoiceItemSeqId));
			tempMap = [:];
			List prodDetails = EntityUtil.filterByCondition(products, EntityCondition.makeCondition("productId", EntityOperator.EQUALS, eachItem.productId));
			prodDetail = EntityUtil.getFirst(prodDetails);
			tempMap.put("productId", eachItem.productId);
			tempMap.put("itemDescription", prodDetail.description);
			prodUomDescription="Kg/Ltr";
			//ge UOM description for UomId
			if(UtilValidate.isNotEmpty(prodDetail.quantityUomId)){
			uomIdValue = delegator.findOne("Uom",[uomId:prodDetail.quantityUomId], false);
			if(UtilValidate.isNotEmpty(uomIdValue)){
				prodUomDescription=uomIdValue.description;
				}
			}
			tempMap.put("quantityUomId", prodUomDescription);
			if(UtilValidate.isNotEmpty(prodDetail.quantityIncluded)){
				tempMap.put("quantityLtr", (eachItem.quantity)*prodDetail.quantityIncluded);
			}else{
	 	      tempMap.put("quantityLtr", (eachItem.quantity));
		    }
			tempMap.put("quantity", eachItem.quantity);
			tempMap.put("mrpPrice", mrpPrice);
			tempMap.put("defaultPrice", eachItem.unitPrice);
			if(orderSeqList){
				orderItemSeqId = (EntityUtil.getFirst(orderSeqList)).get("orderItemSeqId");
				if(UtilValidate.isNotEmpty(salesChannelEnumId) && salesChannelEnumId=="SCRAP_PROD_CHANNEL"){
					ecl=EntityCondition.makeCondition([EntityCondition.makeCondition("orderId",EntityOperator.EQUALS,orderId),EntityCondition.makeCondition("orderItemSeqId",EntityOperator.EQUALS,orderItemSeqId)],EntityOperator.AND);
					List orderItemList= delegator.findList("OrderItem",ecl,null,null,null,false);
					GenericValue orderItem = EntityUtil.getFirst(orderItemList);
					recurringFreqUomId="";
					comments="";
					if(UtilValidate.isNotEmpty(orderItem.getString("recurringFreqUomId"))){
						recurringFreqUomId=orderItem.getString("recurringFreqUomId");
						uomIdValue = delegator.findOne("Uom",[uomId:recurringFreqUomId], false);
						if(UtilValidate.isNotEmpty(uomIdValue)){
							recurringFreqUomId=uomIdValue.description;
							}
					}
					if(UtilValidate.isNotEmpty(orderItem.getString("comments"))){
						comments=orderItem.getString("comments");
					}
					tempMap.put("recurringFreqUomId",recurringFreqUomId);
					tempMap.put("comments",comments);
				}
				
				batchList = EntityUtil.filterByAnd(orderItemAttributes, UtilMisc.toMap("orderId", orderId, "orderItemSeqId", orderItemSeqId));
				if(batchList){
					batchItem = batchList.get(0);
					tempMap.put("batchNo", batchItem.getString("attrValue"));
				}else{
					tempMap.put("batchNo", "");
				}
			}
			invoiceItemDetail.add(tempMap);
		}
		else{
			tempTaxMap = [:];
			tempTaxMap.put(eachItem.invoiceItemTypeId, eachItem.amount);
			taxList.add(tempTaxMap);
		}
	}
	if(invoiceSequence){
		invoiceNo = (invoiceSequence.get(0)).get("sequenceId");
	}
	else{
		invoiceNo = invoiceId
	}
	invoiceDetailMap.put("invoiceNo", invoiceNo);
	invoiceDetailMap.put("chapterMap", chapterMap);
	invoiceDetailMap.put("invoiceItems", invoiceItemDetail);
	invoiceDetailMap.put("invoiceTaxItems", taxList);
	//invoiceSlipsMap.put(partyIdTo, invoiceDetailMap);
	invoiceSlipsMap.put(shipTopartyId, invoiceDetailMap);
	
}
taxParty = delegator.findOne("Party", UtilMisc.toMap("partyId", "TAX10"), false);
taxAuthority = delegator.findOne("TaxAuthority", UtilMisc.toMap("taxAuthGeoId","IND", "taxAuthPartyId","TAX10"), false);
context.invoiceSlipsMap = invoiceSlipsMap;
context.taxParty = taxParty;
context.taxAuthority = taxAuthority;
context.taxLabelFlag = taxLabelFlag;
context.totVatsPercesntMap = totVatsPercesntMap;
