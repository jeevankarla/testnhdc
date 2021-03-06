/*******************************************************************************
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
 *******************************************************************************/
package org.ofbiz.accounting.invoice;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.commons.collections.CollectionUtils;
import org.ofbiz.accounting.payment.PaymentGatewayServices;
import org.ofbiz.accounting.payment.PaymentWorker;
import org.ofbiz.accounting.util.UtilAccounting;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilGenerics;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilNumber;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.order.order.OrderReadHelper;
import org.ofbiz.product.product.ProductEvents;
import org.ofbiz.product.product.ProductWorker;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;


/**
 * InvoiceServices - Services for creating invoices
 *
 * Note that throughout this file we use BigDecimal to do arithmetic. It is
 * critical to understand the way BigDecimal works if you wish to modify the
 * computations in this file. The most important things to keep in mind:
 *
 * Critically important: BigDecimal arithmetic methods like add(),
 * multiply(), divide() do not modify the BigDecimal itself. Instead, they
 * return a new BigDecimal. For example, to keep a running total of an
 * amount, make sure you do this:
 *
 *      amount = amount.add(subAmount);
 *
 * and not this,
 *
 *      amount.add(subAmount);
 *
 * Use .setScale(scale, roundingMode) after every computation to scale and
 * round off the decimals. Check the code to see how the scale and
 * roundingMode are obtained and how the function is used.
 *
 * use .compareTo() to compare big decimals
 *
 *      ex.  (amountOne.compareTo(amountTwo) == 1)
 *           checks if amountOne is greater than amountTwo
 *
 * Use .signum() to test if value is negative, zero, or positive
 *
 *      ex.  (amountOne.signum() == 1)
 *           checks if the amount is a positive non-zero number
 *
 * Never use the .equals() function becaues it considers 2.0 not equal to 2.00 (the scale is different)
 * Instead, use .compareTo() or .signum(), which handles scale correctly.
 *
 * For reference, check the official Sun Javadoc on java.math.BigDecimal.
 */
public class InvoiceServices {

    public static String module = InvoiceServices.class.getName();

    // set some BigDecimal properties
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int DECIMALS = UtilNumber.getBigDecimalScale("invoice.decimals");
    private static final int ROUNDING = UtilNumber.getBigDecimalRoundingMode("invoice.rounding");
    private static final int TAX_DECIMALS = UtilNumber.getBigDecimalScale("salestax.calc.decimals");
    private static final int TAX_ROUNDING = UtilNumber.getBigDecimalRoundingMode("salestax.rounding");
    public static final int TAX_CALC_SCALE = UtilNumber.getBigDecimalScale("salestax.calc.decimals");
    private static final int INVOICE_ITEM_SEQUENCE_ID_DIGITS = 5; // this is the number of digits used for invoiceItemSeqId: 00001, 00002...

    public static final String resource = "AccountingUiLabels";
    
    
    private static int decimals;
    private static int rounding;
     
     static {
         decimals = 2;// UtilNumber.getBigDecimalScale("order.decimals");
         rounding = UtilNumber.getBigDecimalRoundingMode("order.rounding");

         // set zero to the proper scale
         //if (decimals != -1) ZERO = ZERO.setScale(decimals);
     }

    // service to create an invoice for a complete order by the system userid
    public static Map<String, Object> createInvoiceForOrderAllItems(DispatchContext dctx, Map<String, Object> context) {
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        try {
            List<GenericValue> orderItems = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", (String) context.get("orderId")));
            if (orderItems.size() > 0) {
                context.put("billItems", orderItems);
            }
            // get the system userid and store in context otherwise the invoice add service does not work
            GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", "system"));
            if (userLogin != null) {
                context.put("userLogin", userLogin);
            }

            Map<String, Object> result = dispatcher.runSync("createInvoiceForOrder", context);
            result.remove("invoiceTypeId");  //remove extra parameter
            return result;
        }
        catch (GenericServiceException e) {
            String errMsg = UtilProperties.getMessage(resource,"AccountingEntityDataProblemCreatingInvoiceFromOrderItems",UtilMisc.toMap("reason",e.toString()),(Locale) context.get("locale"));
            Debug.logError (e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        } catch (GenericEntityException e) {
            String errMsg = UtilProperties.getMessage(resource,"AccountingEntityDataProblemCreatingInvoiceFromOrderItems",UtilMisc.toMap("reason",e.toString()),(Locale) context.get("locale"));
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        }
    }

    /* Service to create an invoice for an order */
    public static Map<String, Object> createInvoiceForOrderOrig(DispatchContext dctx, Map<String, Object> context) {
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");

        if (DECIMALS == -1 || ROUNDING == -1) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingAritmeticPropertiesNotConfigured",locale));
        }

        String orderId = (String) context.get("orderId");
        String purposeTypeId = (String) context.get("purposeTypeId");
        List<GenericValue> billItems = UtilGenerics.checkList(context.get("billItems"));
        String invoiceId = (String) context.get("invoiceId");
        String shipmentId = (String) context.get("shipmentId");
        Map itemAdjMap = (Map) context.get("itemAdjMap");
        List ignoreAdjustmentsList = (List) context.get("ignoreAdjustmentsList");

        String invoicetypeId = "";
        
        if (UtilValidate.isEmpty(billItems)) {
            Debug.logVerbose("No order items to invoice; not creating invoice; returning success", module);
            return ServiceUtil.returnSuccess(UtilProperties.getMessage(resource,"AccountingNoOrderItemsToInvoice",locale));
        }

        try {
            GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
            if (orderHeader == null) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingNoOrderHeader",locale));
            }

            // figure out the invoice type
            String invoiceType = null;

            String orderType = orderHeader.getString("orderTypeId");
            if (orderType.equals("SALES_ORDER")) {
                invoiceType = "SALES_INVOICE";
            } else if (orderType.equals("PURCHASE_ORDER")) {
                invoiceType = "PURCHASE_INVOICE";
            }

            // Set the precision depending on the type of invoice
            int invoiceTypeDecimals = UtilNumber.getBigDecimalScale("invoice." + invoiceType + ".decimals");
            if (invoiceTypeDecimals == -1) invoiceTypeDecimals = DECIMALS;

            // Make an order read helper from the order
            OrderReadHelper orh = new OrderReadHelper(orderHeader);

            // get the product store
            GenericValue productStore = orh.getProductStore();

            // get the shipping adjustment mode (Y = Pro-Rate; N = First-Invoice)
            String prorateShipping = productStore != null ? productStore.getString("prorateShipping") : "Y";
            if (prorateShipping == null) {
                prorateShipping = "Y";
            }

            // get the billing parties
            String billToCustomerPartyId = orh.getBillToParty().getString("partyId");
            String billFromVendorPartyId = orh.getBillFromParty().getString("partyId");

            ////Debug.log("billToCustomerPartyId=================="+billToCustomerPartyId);
            
           // //Debug.log("billFromVendorPartyId=================="+billFromVendorPartyId);
              
            // get some price totals
            BigDecimal shippableAmount = orh.getShippableTotal(null);
            BigDecimal orderSubTotal = orh.getOrderItemsSubTotal();

            // these variables are for pro-rating order amounts across invoices, so they should not be rounded off for maximum accuracy
            BigDecimal invoiceShipProRateAmount = ZERO;
            BigDecimal invoiceSubTotal = ZERO;
            BigDecimal invoiceQuantity = ZERO;

            GenericValue billingAccount = orderHeader.getRelatedOne("BillingAccount");
            String billingAccountId = billingAccount != null ? billingAccount.getString("billingAccountId") : null;

            Timestamp invoiceDate = (Timestamp)context.get("eventDate");
            if (UtilValidate.isEmpty(invoiceDate)) {
                // TODO: ideally this should be the same time as when a shipment is sent and be passed in as a parameter
                invoiceDate = UtilDateTime.nowTimestamp();
            }
            // TODO: perhaps consider billing account net days term as well?
            Long orderTermNetDays = orh.getOrderTermNetDays();
            Timestamp dueDate = null;
            if (orderTermNetDays != null) {
                dueDate = UtilDateTime.getDayEnd(invoiceDate, orderTermNetDays);
            }

            //===========get RO for branch================
            
            String roFroBranch = "";
            List conditionList = FastList.newInstance();
 			conditionList.add(EntityCondition.makeCondition("partyIdTo", EntityOperator.EQUALS, billFromVendorPartyId));
 			conditionList.add(EntityCondition.makeCondition("roleTypeIdTo", EntityOperator.EQUALS, "ORGANIZATION_UNIT" ));
 	        conditionList.add(EntityCondition.makeCondition("roleTypeIdFrom", EntityOperator.EQUALS, "PARENT_ORGANIZATION"));
 			conditionList.add(EntityCondition.makeCondition("fromDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.getDayEnd(invoiceDate)));
			conditionList.add(EntityCondition.makeCondition(EntityCondition.makeCondition("thruDate", EntityOperator.EQUALS, null), EntityOperator.OR, 
			EntityCondition.makeCondition("thruDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.getDayStart(invoiceDate))));
			    EntityCondition condition = EntityCondition.makeCondition(conditionList, EntityOperator.AND);  	
			try{
				List<GenericValue> orgsListS = delegator.findList("PartyRelationship", condition, null, UtilMisc.toList("partyIdFrom"), null, false);
				GenericValue orgsList = EntityUtil.getFirst(orgsListS);
				roFroBranch = orgsList.getString("partyIdFrom");
				
   	    	}catch (GenericEntityException e) {
   				// TODO: handle exception
   	    		Debug.logError(e, module);
   			}
			
            
            // create the invoice record
            if (UtilValidate.isEmpty(invoiceId)) {
                Map<String, Object> createInvoiceContext = FastMap.newInstance();
                createInvoiceContext.put("partyId", billToCustomerPartyId);
                createInvoiceContext.put("shipmentId", shipmentId);
                createInvoiceContext.put("partyIdFrom", roFroBranch);
                createInvoiceContext.put("costCenterId", billFromVendorPartyId);
                createInvoiceContext.put("billingAccountId", billingAccountId);
                createInvoiceContext.put("invoiceDate", invoiceDate);
                createInvoiceContext.put("dueDate", dueDate);
                createInvoiceContext.put("invoiceTypeId", invoiceType);
                // start with INVOICE_IN_PROCESS, in the INVOICE_READY we can't change the invoice (or shouldn't be able to...)
                createInvoiceContext.put("statusId", "INVOICE_IN_PROCESS");
                createInvoiceContext.put("currencyUomId", orderHeader.getString("currencyUom"));
                createInvoiceContext.put("purposeTypeId", purposeTypeId);
                createInvoiceContext.put("userLogin", userLogin);

                // store the invoice first
                Map<String, Object> createInvoiceResult = dispatcher.runSync("createInvoice", createInvoiceContext);
                if (ServiceUtil.isError(createInvoiceResult)) {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceFromOrder",locale), null, null, createInvoiceResult);
                }

                // call service, not direct entity op: delegator.create(invoice);
                invoiceId = (String) createInvoiceResult.get("invoiceId");
                
                GenericValue invoice = delegator.findOne("Invoice", UtilMisc.toMap("invoiceId",invoiceId), true);
                if(UtilValidate.isNotEmpty(invoice)){
                   invoicetypeId = (String) invoice.getString("invoiceTypeId");
                }  
            }

            // order roles to invoice roles
            List<GenericValue> orderRoles = orderHeader.getRelated("OrderRole");
            Map<String, Object> createInvoiceRoleContext = FastMap.newInstance();
            createInvoiceRoleContext.put("invoiceId", invoiceId);
            createInvoiceRoleContext.put("userLogin", userLogin);
            for (GenericValue orderRole : orderRoles) {
            	
            	String roleTypeId = orderRole.getString("roleTypeId");
            	
            	if(roleTypeId.equals("BILL_FROM_VENDOR"))
                  createInvoiceRoleContext.put("partyId", roFroBranch);
            	else
            	  createInvoiceRoleContext.put("partyId", orderRole.getString("partyId"));	
            		
                createInvoiceRoleContext.put("roleTypeId", orderRole.getString("roleTypeId"));
                Map<String, Object> createInvoiceRoleResult = dispatcher.runSync("createInvoiceRole", createInvoiceRoleContext);
                if (ServiceUtil.isError(createInvoiceRoleResult)) {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceFromOrder",locale), null, null, createInvoiceRoleResult);
                }
            }
            
            try{
              	 GenericValue billFromVendorRole = delegator.makeValue("InvoiceRole");
              	 billFromVendorRole.set("invoiceId", invoiceId);
              	 billFromVendorRole.set("partyId", billFromVendorPartyId);
              	 billFromVendorRole.set("roleTypeId", "COST_CENTER_ID");
              	 billFromVendorRole.set("datetimePerformed", UtilDateTime.nowTimestamp());
                   delegator.createOrStore(billFromVendorRole);
               } catch (GenericEntityException e) {
             	  Debug.logError(e, "Failed to Populate Invoice ", module);
               }	
           
            // order terms to invoice terms.
            // TODO: it might be nice to filter OrderTerms to only copy over financial terms.
            List<GenericValue> orderTerms = orh.getOrderTerms();
            createInvoiceTerms(delegator, dispatcher, invoiceId, orderTerms, userLogin, locale);

            // billing accounts
            // List billingAccountTerms = null;
            // for billing accounts we will use related information
            if (billingAccount != null) {
                /*
                 * jacopoc: billing account terms were already copied as order terms
                 *          when the order was created.
                // get the billing account terms
                billingAccountTerms = billingAccount.getRelated("BillingAccountTerm");

                // set the invoice terms as defined for the billing account
                createInvoiceTerms(delegator, dispatcher, invoiceId, billingAccountTerms, userLogin, locale);
                */
                // set the invoice bill_to_customer from the billing account
                List<GenericValue> billToRoles = billingAccount.getRelated("BillingAccountRole", UtilMisc.toMap("roleTypeId", "BILL_TO_CUSTOMER"), null);
                for (GenericValue billToRole : billToRoles) {
                    if (!(billToRole.getString("partyId").equals(billToCustomerPartyId))) {
                        createInvoiceRoleContext = UtilMisc.toMap("invoiceId", invoiceId, "partyId", billToRole.get("partyId"),
                                                                           "roleTypeId", "BILL_TO_CUSTOMER", "userLogin", userLogin);
                        Map<String, Object> createInvoiceRoleResult = dispatcher.runSync("createInvoiceRole", createInvoiceRoleContext);
                        if (ServiceUtil.isError(createInvoiceRoleResult)) {
                            return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceRoleFromOrder",locale), null, null, createInvoiceRoleResult);
                        }
                    }
                }

                // set the bill-to contact mech as the contact mech of the billing account
                if (UtilValidate.isNotEmpty(billingAccount.getString("contactMechId"))) {
                    Map<String, Object> createBillToContactMechContext = UtilMisc.toMap("invoiceId", invoiceId, "contactMechId", billingAccount.getString("contactMechId"),
                                                                       "contactMechPurposeTypeId", "BILLING_LOCATION", "userLogin", userLogin);
                    Map<String, Object> createBillToContactMechResult = dispatcher.runSync("createInvoiceContactMech", createBillToContactMechContext);
                    if (ServiceUtil.isError(createBillToContactMechResult)) {
                        return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceContactMechFromOrder",locale), null, null, createBillToContactMechResult);
                    }
                }
            } else {
                List<GenericValue> billingLocations = orh.getBillingLocations();
                if (UtilValidate.isNotEmpty(billingLocations)) {
                    for (GenericValue ocm : billingLocations) {
                        Map<String, Object> createBillToContactMechContext = UtilMisc.toMap("invoiceId", invoiceId, "contactMechId", ocm.getString("contactMechId"),
                                                                           "contactMechPurposeTypeId", "BILLING_LOCATION", "userLogin", userLogin);
                        Map<String, Object> createBillToContactMechResult = dispatcher.runSync("createInvoiceContactMech", createBillToContactMechContext);
                        if (ServiceUtil.isError(createBillToContactMechResult)) {
                            return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceContactMechFromOrder",locale), null, null, createBillToContactMechResult);
                        }
                    }
                } else {
                    Debug.logWarning("No billing locations found for order [" + orderId +"] and none were created for Invoice [" + invoiceId + "]", module);
                }
            }

            // get a list of the payment method types
            //DEJ20050705 doesn't appear to be used: List paymentPreferences = orderHeader.getRelated("OrderPaymentPreference");

            // create the bill-from (or pay-to) contact mech as the primary PAYMENT_LOCATION of the party from the store
            GenericValue payToAddress = null;
            if (invoiceType.equals("PURCHASE_INVOICE")) {
                // for purchase orders, the pay to address is the BILLING_LOCATION of the vendor
                GenericValue billFromVendor = orh.getPartyFromRole("BILL_FROM_VENDOR");
                if (billFromVendor != null) {
                    List<GenericValue> billingContactMechs = billFromVendor.getRelatedOne("Party").getRelatedByAnd("PartyContactMechPurpose",
                            UtilMisc.toMap("contactMechPurposeTypeId", "BILLING_LOCATION"));
                    if (UtilValidate.isNotEmpty(billingContactMechs)) {
                        payToAddress = EntityUtil.getFirst(billingContactMechs);
                    }
                }
            } else {
                // for sales orders, it is the payment address on file for the store
                payToAddress = PaymentWorker.getPaymentAddress(delegator, productStore.getString("payToPartyId"));
            }
            if (payToAddress != null) {
                Map<String, Object> createPayToContactMechContext = UtilMisc.toMap("invoiceId", invoiceId, "contactMechId", payToAddress.getString("contactMechId"),
                                                                   "contactMechPurposeTypeId", "PAYMENT_LOCATION", "userLogin", userLogin);
                Map<String, Object> createPayToContactMechResult = dispatcher.runSync("createInvoiceContactMech", createPayToContactMechContext);
                if (ServiceUtil.isError(createPayToContactMechResult)) {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceContactMechFromOrder",locale), null, null, createPayToContactMechResult);
                }
            }

            // sequence for items - all OrderItems or InventoryReservations + all Adjustments
            int invoiceItemSeqNum = 1;
            String invoiceItemSeqId = UtilFormatOut.formatPaddedNumber(invoiceItemSeqNum, INVOICE_ITEM_SEQUENCE_ID_DIGITS);
            // create the item records
            for (GenericValue currentValue : billItems) {
            	String orderItemSeqId = (String) currentValue.get("orderItemSeqId");
            	
            	List inputAdjustmentsList = FastList.newInstance();
            	if(UtilValidate.isNotEmpty(itemAdjMap)){
            		inputAdjustmentsList = (List) itemAdjMap.get(orderItemSeqId);
            	}
                GenericValue itemIssuance = null;
                GenericValue orderItem = null;
                GenericValue shipmentReceipt = null;
                if ("ItemIssuance".equals(currentValue.getEntityName())) {
                    itemIssuance = currentValue;
                } else if ("OrderItem".equals(currentValue.getEntityName())) {
                    orderItem = currentValue;
                } else if ("ShipmentReceipt".equals(currentValue.getEntityName())) {
                    shipmentReceipt = currentValue;
                } else {
                    Debug.logError("Unexpected entity " + currentValue + " of type " + currentValue.getEntityName(), module);
                }

                if (orderItem == null && itemIssuance != null) {
                    orderItem = itemIssuance.getRelatedOne("OrderItem");
                } else if ((orderItem == null) && (shipmentReceipt != null)) {
                    orderItem = shipmentReceipt.getRelatedOne("OrderItem");
                } else if ((orderItem == null) && (itemIssuance == null) && (shipmentReceipt == null)) {
                    Debug.logError("Cannot create invoice when orderItem, itemIssuance, and shipmentReceipt are all null", module);
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingIllegalValuesPassedToCreateInvoiceService",locale));
                }
                GenericValue product = null;
                if (orderItem.get("productId") != null) {
                    product = orderItem.getRelatedOne("Product");
                }
                // get some quantities
                BigDecimal billingQuantity = null;
                if (itemIssuance != null) {
                    billingQuantity = itemIssuance.getBigDecimal("quantity");
                    BigDecimal cancelQty = itemIssuance.getBigDecimal("cancelQuantity");
                    if (cancelQty == null) {
                        cancelQty = ZERO;
                    }
                    billingQuantity = billingQuantity.subtract(cancelQty).setScale(DECIMALS, ROUNDING);
                } else if (shipmentReceipt != null) {
                    billingQuantity = shipmentReceipt.getBigDecimal("quantityAccepted");
                } else {
                    BigDecimal orderedQuantity = OrderReadHelper.getOrderItemQuantity(orderItem);
                    BigDecimal invoicedQuantity = OrderReadHelper.getOrderItemInvoicedQuantity(orderItem);
                    billingQuantity = orderedQuantity.subtract(invoicedQuantity);
                    if (billingQuantity.compareTo(ZERO) < 0) {
                        billingQuantity = ZERO;
                    }
                }
                if (billingQuantity == null) billingQuantity = ZERO;

                // check if shipping applies to this item.  Shipping is calculated for sales invoices, not purchase invoices.
                boolean shippingApplies = false;
                if ((product != null) && (ProductWorker.shippingApplies(product)) && (invoiceType.equals("SALES_INVOICE"))) {
                    shippingApplies = true;
                }

                BigDecimal billingAmount = orderItem.getBigDecimal("unitPrice").setScale(invoiceTypeDecimals, ROUNDING);

                Map<String, Object> createInvoiceItemContext = FastMap.newInstance();
                createInvoiceItemContext.put("invoiceId", invoiceId);
                createInvoiceItemContext.put("invoiceItemSeqId", invoiceItemSeqId);
                
              //  //Debug.log("invoicetypeId========================="+invoicetypeId);
                if(!invoicetypeId.equals("PURCHASE_INVOICE")){
                createInvoiceItemContext.put("invoiceItemTypeId", getInvoiceItemType(delegator, (orderItem.getString("orderItemTypeId")), (product == null ? null : product.getString("productTypeId")), invoiceType, "INV_FPROD_ITEM"));
               // //Debug.log("invoicetypeId=============INNN============"+invoicetypeId);
                }
                else{
                createInvoiceItemContext.put("invoiceItemTypeId", "INV_RAWPROD_ITEM");	
              //  //Debug.log("invoicetypeId============OUTTTTT============="+invoicetypeId);
                }
                
                createInvoiceItemContext.put("description", orderItem.get("itemDescription"));
                createInvoiceItemContext.put("quantity", billingQuantity);
                createInvoiceItemContext.put("amount", billingAmount);
                createInvoiceItemContext.put("productId", orderItem.get("productId"));
                createInvoiceItemContext.put("productFeatureId", orderItem.get("productFeatureId"));
                createInvoiceItemContext.put("overrideGlAccountId", orderItem.get("overrideGlAccountId"));
                createInvoiceItemContext.put("costCenterId", billFromVendorPartyId);
                //createInvoiceItemContext.put("uomId", "");
                createInvoiceItemContext.put("userLogin", userLogin);
                String itemIssuanceId = null;
                if (itemIssuance != null && itemIssuance.get("inventoryItemId") != null) {
                    itemIssuanceId = itemIssuance.getString("itemIssuanceId");
                    createInvoiceItemContext.put("inventoryItemId", itemIssuance.get("inventoryItemId"));
                }
                // similarly, tax only for purchase invoices
                if ((product != null) && (invoiceType.equals("SALES_INVOICE"))) {
                    createInvoiceItemContext.put("taxableFlag", product.get("taxable"));
                }

                Map<String, Object> createInvoiceItemResult = dispatcher.runSync("createInvoiceItem", createInvoiceItemContext);
                if (ServiceUtil.isError(createInvoiceItemResult)) {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceItemFromOrder",locale), null, null, createInvoiceItemResult);
                }

                // this item total
                BigDecimal thisAmount = billingAmount.multiply(billingQuantity).setScale(invoiceTypeDecimals, ROUNDING);

                // add to the ship amount only if it applies to this item
                if (shippingApplies) {
                    invoiceShipProRateAmount = invoiceShipProRateAmount.add(thisAmount).setScale(invoiceTypeDecimals, ROUNDING);
                }

                // increment the invoice subtotal
                invoiceSubTotal = invoiceSubTotal.add(thisAmount).setScale(100, ROUNDING);

                // increment the invoice quantity
                invoiceQuantity = invoiceQuantity.add(billingQuantity).setScale(invoiceTypeDecimals, ROUNDING);

                // create the OrderItemBilling record
                Map<String, Object> createOrderItemBillingContext = FastMap.newInstance();
                createOrderItemBillingContext.put("invoiceId", invoiceId);
                createOrderItemBillingContext.put("invoiceItemSeqId", invoiceItemSeqId);
                createOrderItemBillingContext.put("orderId", orderItem.get("orderId"));
                createOrderItemBillingContext.put("orderItemSeqId", orderItem.get("orderItemSeqId"));
                createOrderItemBillingContext.put("itemIssuanceId", itemIssuanceId);
                createOrderItemBillingContext.put("quantity", billingQuantity);
                createOrderItemBillingContext.put("amount", billingAmount);
                createOrderItemBillingContext.put("userLogin", userLogin);
                if ((shipmentReceipt != null) && (shipmentReceipt.getString("receiptId") != null)) {
                    createOrderItemBillingContext.put("shipmentReceiptId", shipmentReceipt.getString("receiptId"));
                }
                Map<String, Object> createOrderItemBillingResult = dispatcher.runSync("createOrderItemBilling", createOrderItemBillingContext);
                if (ServiceUtil.isError(createOrderItemBillingResult)) {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingOrderItemBillingFromOrder",locale), null, null, createOrderItemBillingResult);
                }

                if ("ItemIssuance".equals(currentValue.getEntityName())) {
                    List<GenericValue> shipmentItemBillings = delegator.findByAnd("ShipmentItemBilling", UtilMisc.toMap("shipmentId", currentValue.get("shipmentId")));
                    if (UtilValidate.isEmpty(shipmentItemBillings)) {

                        // create the ShipmentItemBilling record
                        GenericValue shipmentItemBilling = delegator.makeValue("ShipmentItemBilling", UtilMisc.toMap("invoiceId", invoiceId, "invoiceItemSeqId", invoiceItemSeqId));
                        shipmentItemBilling.put("shipmentId", currentValue.get("shipmentId"));
                        shipmentItemBilling.put("shipmentItemSeqId", currentValue.get("shipmentItemSeqId"));
                        shipmentItemBilling.create();
                    }
                }

                String parentInvoiceItemSeqId = invoiceItemSeqId;
                // increment the counter
                invoiceItemSeqNum++;
                invoiceItemSeqId = UtilFormatOut.formatPaddedNumber(invoiceItemSeqNum, INVOICE_ITEM_SEQUENCE_ID_DIGITS);

                // Get the original order item from the DB, in case the quantity has been overridden
                GenericValue originalOrderItem = delegator.findByPrimaryKey("OrderItem", UtilMisc.toMap("orderId", orderId, "orderItemSeqId", orderItem.getString("orderItemSeqId")));

                // create the item adjustment as line items
                List<GenericValue> itemAdjustments = OrderReadHelper.getOrderItemAdjustmentList(orderItem, orh.getAdjustments());
                
               // Debug.log("inputAdjustmentsList==================="+inputAdjustmentsList);
               // Debug.log("ignoreAdjustmentsList==================="+ignoreAdjustmentsList);
                
                
                
                if(UtilValidate.isNotEmpty(inputAdjustmentsList)){
                	itemAdjustments = inputAdjustmentsList;
                }
                //inputAdjustmentsList
                
                for (GenericValue adj : itemAdjustments) {
                	
                	
                    // Check against OrderAdjustmentBilling to see how much of this adjustment has already been invoiced
                	BigDecimal adjAlreadyInvoicedQty = BigDecimal.ZERO;
                    BigDecimal adjAlreadyInvoicedAmount = null;
                    try {
                        Map<String, Object> checkResult = dispatcher.runSync("calculateInvoicedAdjustmentTotal", UtilMisc.toMap("orderAdjustment", adj));
                        adjAlreadyInvoicedAmount = (BigDecimal) checkResult.get("invoicedTotal");
                        if(UtilValidate.isNotEmpty(checkResult.get("invoicedQty"))){
                        	adjAlreadyInvoicedQty = (BigDecimal) checkResult.get("invoicedQty");
                        }
                        
                    } catch (GenericServiceException e) {
                        String errMsg = UtilProperties.getMessage(resource, "AccountingTroubleCallingCalculateInvoicedAdjustmentTotalService", locale);
                        Debug.logError(e, errMsg, module);
                        return ServiceUtil.returnError(errMsg);
                    }
                    
                    ////Debug.log("adjAlreadyInvoicedAmount ==============="+adjAlreadyInvoicedAmount);
                   // //Debug.log("adjAlreadyInvoicedQty ==============="+adjAlreadyInvoicedQty);
                    
                    // If the absolute invoiced amount >= the abs of the adjustment amount, the full amount has already been invoiced,
                    //  so skip this adjustment
                    if (adj.get("amount") == null) { // JLR 17/4/7 : fix a bug coming from POS in case of use of a discount (on item(s) or sale, item(s) here) and a cash amount higher than total (hence issuing change)
                        continue;
                    }
                    if(UtilValidate.isNotEmpty(ignoreAdjustmentsList)){
	                    if(!ignoreAdjustmentsList.contains(adj.getString("orderAdjustmentTypeId"))){
	                    	if (adjAlreadyInvoicedAmount.abs().compareTo(adj.getBigDecimal("amount").setScale(invoiceTypeDecimals, ROUNDING).abs()) > 0) {
	                            continue;
	                        }
	                    }
                    }    
                    
                    BigDecimal amount = ZERO;
                    BigDecimal sourcePercentage = ZERO;
                    BigDecimal totalQuota =BigDecimal.ZERO;
                    BigDecimal tenPercentAdjQty =BigDecimal.ZERO;
                    if( (UtilValidate.isNotEmpty(adj.get("orderAdjustmentTypeId") )) &&    ( (adj.get("orderAdjustmentTypeId")).equals("TEN_PERCENT_SUBSIDY")  )  ){
                    	
                    	// Get Already Billed Qty
                    	
                    	
                    	// Get Quota Quantity
                    	
                    	List detCondsList = FastList.newInstance();
                    	detCondsList.add(EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId));
						detCondsList.add(EntityCondition.makeCondition("orderItemSeqId", EntityOperator.EQUALS, orderItem.getString("orderItemSeqId")));
					  	
						BigDecimal quota =BigDecimal.ZERO;
						
					  	try {
							List<GenericValue> OrderItemDetailList = delegator.findList("OrderItemDetail", EntityCondition.makeCondition(detCondsList,EntityOperator.AND), UtilMisc.toSet("partyId","quotaQuantity","productId"), null, null, true);
							
							if(UtilValidate.isNotEmpty(OrderItemDetailList)){
								for(GenericValue OrderItemDetailValue : OrderItemDetailList){
									if(UtilValidate.isNotEmpty(OrderItemDetailValue.get("quotaQuantity"))){
										quota = OrderItemDetailValue.getBigDecimal("quotaQuantity");
										totalQuota = totalQuota.add(quota);
									}
								}
							}
						  	
					  	} catch (GenericEntityException e) {
							Debug.logError(e, "Failed to retrive ProductPriceType ", module);
							return ServiceUtil.returnError("Failed to retrive ProductPriceType " + e);
						}
					  	//Debug.log("totalQuota ==============="+totalQuota);
					  	
					  	tenPercentAdjQty = totalQuota.subtract(adjAlreadyInvoicedQty);
					  	if(billingQuantity.compareTo(tenPercentAdjQty) < 0){
                        	tenPercentAdjQty = billingQuantity;
                        }
                    	                    	
                    	if((UtilValidate.isNotEmpty(ignoreAdjustmentsList)) && (ignoreAdjustmentsList.contains(adj.getString("orderAdjustmentTypeId")))){
                    		if ( (adj.get("amount") != null) && (tenPercentAdjQty.compareTo(ZERO) > 0) ) {
                                // pro-rate the amount
                                // set decimals = 100 means we don't round this intermediate value, which is very important
                                amount = adj.getBigDecimal("amount"); //.divide(totalQuota, 100, ROUNDING);
                                //amount = amount.multiply(tenPercentAdjQty);
                                // Tax needs to be rounded differently from other order adjustments
                                if (adj.getString("orderAdjustmentTypeId").equals("SALES_TAX")) {
                                    amount = amount.setScale(TAX_DECIMALS, TAX_ROUNDING);
                                } else {
                                    amount = amount.setScale(invoiceTypeDecimals, ROUNDING);
                                }
                            }
                    	}
                    	else{
                    		if ( (adj.get("amount") != null) && (tenPercentAdjQty.compareTo(ZERO) > 0) ) {
                                // pro-rate the amount
                                // set decimals = 100 means we don't round this intermediate value, which is very important
                                amount = adj.getBigDecimal("amount").divide(totalQuota, 100, ROUNDING);
                                amount = amount.multiply(tenPercentAdjQty);
                                // Tax needs to be rounded differently from other order adjustments
                                if (adj.getString("orderAdjustmentTypeId").equals("SALES_TAX")) {
                                    amount = amount.setScale(TAX_DECIMALS, TAX_ROUNDING);
                                } else {
                                    amount = amount.setScale(invoiceTypeDecimals, ROUNDING);
                                }
                            }
                    	}
                    	
                    	
                    }
                    else{
                    	
                    	if(ignoreAdjustmentsList.contains(adj.getString("orderAdjustmentTypeId"))){
                    		if (adj.get("amount") != null) {
                                // pro-rate the amount
                                // set decimals = 100 means we don't round this intermediate value, which is very important
                                amount = adj.getBigDecimal("amount"); //.divide(originalOrderItem.getBigDecimal("quantity"), 100, ROUNDING);
                                //amount = amount.multiply(billingQuantity);
                                // Tax needs to be rounded differently from other order adjustments
                                if (adj.getString("orderAdjustmentTypeId").equals("SALES_TAX")) {
                                    amount = amount.setScale(TAX_DECIMALS, TAX_ROUNDING);
                                } else {
                                    amount = amount.setScale(invoiceTypeDecimals, ROUNDING);
                                    
                                     sourcePercentage = adj.getBigDecimal("sourcePercentage");
                                    
                                   // Debug.log("amount=======INNNNN3232323========"+amount);
                                    
                                   // Debug.log("sourcePercentage=======INNNNN3232323========"+sourcePercentage);
                                    
                                }
                            } else if (adj.get("sourcePercentage") != null) {
                                // pro-rate the amount
                                // set decimals = 100 means we don't round this intermediate value, which is very important
                                BigDecimal percent = adj.getBigDecimal("sourcePercentage");
        					  	//Debug.log("percent ==============="+percent);

                                percent = percent.divide(new BigDecimal(100), 100, ROUNDING);
                                amount = billingAmount.multiply(percent);
                                amount = amount.divide(originalOrderItem.getBigDecimal("quantity"), 100, ROUNDING);
                                amount = amount.multiply(billingQuantity);
                                amount = amount.setScale(invoiceTypeDecimals, ROUNDING);
                            }
                    	}
                    	else{
                    		if (adj.get("amount") != null) {
                                // pro-rate the amount
                                // set decimals = 100 means we don't round this intermediate value, which is very important
                                amount = adj.getBigDecimal("amount").divide(originalOrderItem.getBigDecimal("quantity"), 100, ROUNDING);
                                amount = amount.multiply(billingQuantity);
                                // Tax needs to be rounded differently from other order adjustments
                                if (adj.getString("orderAdjustmentTypeId").equals("SALES_TAX")) {
                                    amount = amount.setScale(TAX_DECIMALS, TAX_ROUNDING);
                                } else {
                                    amount = amount.setScale(invoiceTypeDecimals, ROUNDING);
                                }
                            } else if (adj.get("sourcePercentage") != null) {
                                // pro-rate the amount
                                // set decimals = 100 means we don't round this intermediate value, which is very important
                                BigDecimal percent = adj.getBigDecimal("sourcePercentage");
                                percent = percent.divide(new BigDecimal(100), 100, ROUNDING);
                                amount = billingAmount.multiply(percent);
                                amount = amount.divide(originalOrderItem.getBigDecimal("quantity"), 100, ROUNDING);
                                amount = amount.multiply(billingQuantity);
                                amount = amount.setScale(invoiceTypeDecimals, ROUNDING);
                            }
                    	}
                    	
                    	
                    	
                    }
                    
                    if (amount.signum() != 0) {
                        Map<String, Object> createInvoiceItemAdjContext = FastMap.newInstance();
                        createInvoiceItemAdjContext.put("invoiceId", invoiceId);
                        createInvoiceItemAdjContext.put("invoiceItemSeqId", invoiceItemSeqId);
                        createInvoiceItemAdjContext.put("invoiceItemTypeId", getInvoiceItemType(delegator, adj.getString("orderAdjustmentTypeId"), null, invoiceType, "INVOICE_ITM_ADJ"));
                        createInvoiceItemAdjContext.put("quantity", BigDecimal.ONE);
                        createInvoiceItemAdjContext.put("amount", amount);
                       // createInvoiceItemAdjContext.put("productId", orderItem.get("productId"));
                        createInvoiceItemAdjContext.put("productFeatureId", orderItem.get("productFeatureId"));
                        createInvoiceItemAdjContext.put("overrideGlAccountId", adj.get("overrideGlAccountId"));
                        createInvoiceItemAdjContext.put("parentInvoiceId", invoiceId);
                        createInvoiceItemAdjContext.put("sourcePercentage", sourcePercentage);
                        createInvoiceItemAdjContext.put("costCenterId", billFromVendorPartyId);
                        
                        createInvoiceItemAdjContext.put("parentInvoiceItemSeqId", parentInvoiceItemSeqId);
                        if(UtilValidate.isNotEmpty(adj.get("isAssessableValue")) && (adj.get("isAssessableValue").equals("Y")) ){
                        	createInvoiceItemAdjContext.put("isAssessableValue", "Y");
                        }
                        //createInvoiceItemAdjContext.put("uomId", "");
                        createInvoiceItemAdjContext.put("userLogin", userLogin);
                        createInvoiceItemAdjContext.put("taxAuthPartyId", adj.get("taxAuthPartyId"));
                        createInvoiceItemAdjContext.put("taxAuthGeoId", adj.get("taxAuthGeoId"));
                        createInvoiceItemAdjContext.put("taxAuthorityRateSeqId", adj.get("taxAuthorityRateSeqId"));

                        // some adjustments fill out the comments field instead
                        String description = (UtilValidate.isEmpty(adj.getString("description")) ? adj.getString("comments") : adj.getString("description"));
                        createInvoiceItemAdjContext.put("description", description);

                        // invoice items for sales tax are not taxable themselves
                        // TODO: This is not an ideal solution. Instead, we need to use OrderAdjustment.includeInTax when it is implemented
                        if (!(adj.getString("orderAdjustmentTypeId").equals("SALES_TAX"))) {
                            createInvoiceItemAdjContext.put("taxableFlag", product.get("taxable"));
                        }

                        // If the OrderAdjustment is associated to a ProductPromo,
                        // and the field ProductPromo.overrideOrgPartyId is set,
                        // copy the value to InvoiceItem.overrideOrgPartyId: this
                        // represent an organization override for the payToPartyId
                        if (UtilValidate.isNotEmpty(adj.getString("productPromoId"))) {
                            try {
                                GenericValue productPromo = adj.getRelatedOne("ProductPromo");
                                if (UtilValidate.isNotEmpty(productPromo.getString("overrideOrgPartyId"))) {
                                    createInvoiceItemAdjContext.put("overrideOrgPartyId", productPromo.getString("overrideOrgPartyId"));
                                }
                            } catch (GenericEntityException e) {
                                Debug.logError(e, "Error looking up ProductPromo with id [" + adj.getString("productPromoId") + "]", module);
                            }
                        }

                        Map<String, Object> createInvoiceItemAdjResult = dispatcher.runSync("createInvoiceItem", createInvoiceItemAdjContext);
                        if (ServiceUtil.isError(createInvoiceItemAdjResult)) {
                            return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceItemFromOrder",locale), null, null, createInvoiceItemAdjResult);
                        }
                       
                        // Create the OrderAdjustmentBilling record
                        
                        if(UtilValidate.isNotEmpty(adj.getString("orderAdjustmentId"))){
                        	Map<String, Object> createOrderAdjustmentBillingContext = FastMap.newInstance();
                            createOrderAdjustmentBillingContext.put("orderAdjustmentId", adj.getString("orderAdjustmentId"));
                            createOrderAdjustmentBillingContext.put("invoiceId", invoiceId);
                            createOrderAdjustmentBillingContext.put("invoiceItemSeqId", invoiceItemSeqId);
                            createOrderAdjustmentBillingContext.put("amount", amount);
                            createOrderAdjustmentBillingContext.put("quantity", tenPercentAdjQty);
                            createOrderAdjustmentBillingContext.put("userLogin", userLogin);

                            Map<String, Object> createOrderAdjustmentBillingResult = dispatcher.runSync("createOrderAdjustmentBilling", createOrderAdjustmentBillingContext);
                            if (ServiceUtil.isError(createOrderAdjustmentBillingResult)) {
                                return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingOrderAdjustmentBillingFromOrder",locale), null, null, createOrderAdjustmentBillingContext);
                            }
                        }
                        
                        // this adjustment amount
                        BigDecimal thisAdjAmount = amount;

                        // adjustments only apply to totals when they are not tax or shipping adjustments
                        if (!"SALES_TAX".equals(adj.getString("orderAdjustmentTypeId")) &&
                                !"SHIPPING_ADJUSTMENT".equals(adj.getString("orderAdjustmentTypeId"))) {
                            // increment the invoice subtotal
                            invoiceSubTotal = invoiceSubTotal.add(thisAdjAmount).setScale(100, ROUNDING);

                            // add to the ship amount only if it applies to this item
                            if (shippingApplies) {
                                invoiceShipProRateAmount = invoiceShipProRateAmount.add(thisAdjAmount).setScale(invoiceTypeDecimals, ROUNDING);
                            }
                        }

                        // increment the counter
                        invoiceItemSeqNum++;
                        invoiceItemSeqId = UtilFormatOut.formatPaddedNumber(invoiceItemSeqNum, INVOICE_ITEM_SEQUENCE_ID_DIGITS);
                    }
                }
                
                
                
            }

            // create header adjustments as line items -- always to tax/shipping last
            Map<GenericValue, BigDecimal> shipAdjustments = FastMap.newInstance();
            Map<GenericValue, BigDecimal> taxAdjustments = FastMap.newInstance();

            List<GenericValue> headerAdjustments = orh.getOrderHeaderAdjustments();
            for (GenericValue adj : headerAdjustments) {
            	
            	/*if(ignoreAdjustmentsList.contains(adj.getString("orderAdjustmentTypeId"))){
            		continue;
            	}*/
            	
                // Check against OrderAdjustmentBilling to see how much of this adjustment has already been invoiced
                BigDecimal adjAlreadyInvoicedAmount = null;
                try {
                    Map<String, Object> checkResult = dispatcher.runSync("calculateInvoicedAdjustmentTotal", UtilMisc.toMap("orderAdjustment", adj));
                    adjAlreadyInvoicedAmount = ((BigDecimal) checkResult.get("invoicedTotal")).setScale(invoiceTypeDecimals, ROUNDING);
                } catch (GenericServiceException e) {
                    String errMsg = UtilProperties.getMessage(resource, "AccountingTroubleCallingCalculateInvoicedAdjustmentTotalService", locale);
                    Debug.logError(e, errMsg, module);
                    return ServiceUtil.returnError(errMsg);
                }

                // If the absolute invoiced amount >= the abs of the adjustment amount, the full amount has already been invoiced,
                //  so skip this adjustment
                if (null == adj.get("amount")) { // JLR 17/4/7 : fix a bug coming from POS in case of use of a discount (on item(s) or sale, sale here) and a cash amount higher than total (hence issuing change)
                    continue;
                }
                if (adjAlreadyInvoicedAmount.abs().compareTo(adj.getBigDecimal("amount").setScale(invoiceTypeDecimals, ROUNDING).abs()) > 0) {
                    continue;
                }

                if ("SHIPPING_CHARGES".equals(adj.getString("orderAdjustmentTypeId"))) {
                    shipAdjustments.put(adj, adjAlreadyInvoicedAmount);
                } else if ("SALES_TAX".equals(adj.getString("orderAdjustmentTypeId"))) {
                    taxAdjustments.put(adj, adjAlreadyInvoicedAmount);
                } else {
                    // these will effect the shipping pro-rate (unless commented)
                    // other adjustment type
                    BigDecimal adjAmount = calcHeaderAdj(delegator, adj, invoiceType, invoiceId, invoiceItemSeqId,
                            orderSubTotal, invoiceSubTotal, adj.getBigDecimal("amount").setScale(invoiceTypeDecimals, ROUNDING), invoiceTypeDecimals, ROUNDING, userLogin, dispatcher, locale);
                    // invoiceShipProRateAmount += adjAmount;
                    // do adjustments compound or are they based off subtotal? Here we will (unless commented)
                    // invoiceSubTotal += adjAmount;

                    // increment the counter
                    invoiceItemSeqNum++;
                    invoiceItemSeqId = UtilFormatOut.formatPaddedNumber(invoiceItemSeqNum, INVOICE_ITEM_SEQUENCE_ID_DIGITS);
                }
                
            }

            // next do the shipping adjustments.  Note that we do not want to add these to the invoiceSubTotal or orderSubTotal for pro-rating tax later, as that would cause
            // numerator/denominator problems when the shipping is not pro-rated but rather charged all on the first invoice
            for (GenericValue adj : shipAdjustments.keySet()) {
                BigDecimal adjAlreadyInvoicedAmount = shipAdjustments.get(adj);

                if ("N".equalsIgnoreCase(prorateShipping)) {

                    // Set the divisor and multiplier to 1 to avoid prorating
                    BigDecimal divisor = BigDecimal.ONE;
                    BigDecimal multiplier = BigDecimal.ONE;

                    // The base amount in this case is the adjustment amount minus the total already invoiced for that adjustment, since
                    //  it won't be prorated
                    BigDecimal baseAmount = adj.getBigDecimal("amount").setScale(invoiceTypeDecimals, ROUNDING).subtract(adjAlreadyInvoicedAmount);
                    BigDecimal adjAmount = calcHeaderAdj(delegator, adj, invoiceType, invoiceId, invoiceItemSeqId,
                            divisor, multiplier, baseAmount, invoiceTypeDecimals, ROUNDING, userLogin, dispatcher, locale);
                } else {

                    // Pro-rate the shipping amount based on shippable information
                    BigDecimal divisor = shippableAmount;
                    BigDecimal multiplier = invoiceShipProRateAmount;

                    // The base amount in this case is the adjustment amount, since we want to prorate based on the full amount
                    BigDecimal baseAmount = adj.getBigDecimal("amount").setScale(invoiceTypeDecimals, ROUNDING);
                    BigDecimal adjAmount = calcHeaderAdj(delegator, adj, invoiceType, invoiceId, invoiceItemSeqId,
                            divisor, multiplier, baseAmount, invoiceTypeDecimals, ROUNDING, userLogin, dispatcher, locale);
                }

                // Increment the counter
                invoiceItemSeqNum++;
                invoiceItemSeqId = UtilFormatOut.formatPaddedNumber(invoiceItemSeqNum, INVOICE_ITEM_SEQUENCE_ID_DIGITS);
            }

            // last do the tax adjustments
            String prorateTaxes = productStore != null ? productStore.getString("prorateTaxes") : "Y";
            if (prorateTaxes == null) {
                prorateTaxes = "Y";
            }
            for (GenericValue adj : taxAdjustments.keySet()) {
                BigDecimal adjAlreadyInvoicedAmount = taxAdjustments.get(adj);
                BigDecimal adjAmount = null;

                if ("N".equalsIgnoreCase(prorateTaxes)) {

                    // Set the divisor and multiplier to 1 to avoid prorating
                    BigDecimal divisor = BigDecimal.ONE;
                    BigDecimal multiplier = BigDecimal.ONE;

                    // The base amount in this case is the adjustment amount minus the total already invoiced for that adjustment, since
                    //  it won't be prorated
                    BigDecimal baseAmount = adj.getBigDecimal("amount").setScale(TAX_DECIMALS, TAX_ROUNDING).subtract(adjAlreadyInvoicedAmount);
                    adjAmount = calcHeaderAdj(delegator, adj, invoiceType, invoiceId, invoiceItemSeqId,
                             divisor, multiplier, baseAmount, TAX_DECIMALS, TAX_ROUNDING, userLogin, dispatcher, locale);
                } else {

                    // Pro-rate the tax amount based on shippable information
                    BigDecimal divisor = orderSubTotal;
                    BigDecimal multiplier = invoiceSubTotal;

                    // The base amount in this case is the adjustment amount, since we want to prorate based on the full amount
                    BigDecimal baseAmount = adj.getBigDecimal("amount");
                    adjAmount = calcHeaderAdj(delegator, adj, invoiceType, invoiceId, invoiceItemSeqId,
                            divisor, multiplier, baseAmount, TAX_DECIMALS, TAX_ROUNDING, userLogin, dispatcher, locale);
                }
                invoiceSubTotal = invoiceSubTotal.add(adjAmount).setScale(invoiceTypeDecimals, ROUNDING);

                // Increment the counter
                invoiceItemSeqNum++;
                invoiceItemSeqId = UtilFormatOut.formatPaddedNumber(invoiceItemSeqNum, INVOICE_ITEM_SEQUENCE_ID_DIGITS);
            }

            // check for previous order payments
            List<GenericValue> orderPaymentPrefs = delegator.findByAnd("OrderPaymentPreference", UtilMisc.toMap("orderId", orderId));
            List<GenericValue> currentPayments = FastList.newInstance();
            for (GenericValue paymentPref : orderPaymentPrefs) {
                List<GenericValue> payments = paymentPref.getRelated("Payment");
                currentPayments.addAll(payments);
            }
            // apply these payments to the invoice if they have any remaining amount to apply
            for (GenericValue payment : currentPayments) {
                BigDecimal notApplied = PaymentWorker.getPaymentNotApplied(payment);
                if (notApplied.signum() > 0) {
                    Map<String, Object> appl = FastMap.newInstance();
                    appl.put("paymentId", payment.get("paymentId"));
                    appl.put("invoiceId", invoiceId);
                    appl.put("billingAccountId", billingAccountId);
                    appl.put("amountApplied", notApplied);
                    appl.put("userLogin", userLogin);
                    Map<String, Object> createPayApplResult = dispatcher.runSync("createPaymentApplication", appl);
                    if (ServiceUtil.isError(createPayApplResult)) {
                        return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceFromOrder",locale), null, null, createPayApplResult);
                    }
                }
            }

            // Should all be in place now. Depending on the ProductStore.autoApproveInvoice setting, set status to INVOICE_READY (unless it's a purchase invoice, which we set to INVOICE_IN_PROCESS)
            /*String autoApproveInvoice = productStore != null ? productStore.getString("autoApproveInvoice") : "Y";
            if (!"N".equals(autoApproveInvoice)) {
                String nextStatusId = "PURCHASE_INVOICE".equals(invoiceType) ? "INVOICE_IN_PROCESS" : "INVOICE_READY";
                Map<String, Object> setInvoiceStatusResult = dispatcher.runSync("setInvoiceStatus", UtilMisc.<String, Object>toMap("invoiceId", invoiceId, "statusId", nextStatusId, "userLogin", userLogin));
                if (ServiceUtil.isError(setInvoiceStatusResult)) {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceFromOrder",locale), null, null, setInvoiceStatusResult);
                }
            }*/

            Map<String, Object> resp = ServiceUtil.returnSuccess();
            
            resp.put("invoiceId", invoiceId);
            resp.put("invoiceTypeId", invoiceType);
            return resp;
        } catch (GenericEntityException e) {
            String errMsg = UtilProperties.getMessage(resource, "AccountingEntityDataProblemCreatingInvoiceFromOrderItems", UtilMisc.toMap("reason", e.toString()), locale);
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        } catch (GenericServiceException e) {
            String errMsg = UtilProperties.getMessage(resource, "AccountingServiceOtherProblemCreatingInvoiceFromOrderItems", UtilMisc.toMap("reason", e.toString()), locale);
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        }
    }

    
    /* Service to create an invoice for an order */
    public static Map<String, Object> createInvoiceForOrder(DispatchContext dctx, Map<String, Object> context) {
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");

        if (DECIMALS == -1 || ROUNDING == -1) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingAritmeticPropertiesNotConfigured",locale));
        }
        String purposeTypeId = (String) context.get("purposeTypeId");
        String billOfSaleTypeId = (String) context.get("billOfSaleTypeId");
        String orderId = (String) context.get("orderId");
        List<GenericValue> billItems = UtilGenerics.checkList(context.get("billItems"));
        String invoiceId = (String) context.get("invoiceId");

        if (UtilValidate.isEmpty(billItems)) {
            Debug.logVerbose("No order items to invoice; not creating invoice; returning success", module);
            return ServiceUtil.returnSuccess(UtilProperties.getMessage(resource,"AccountingNoOrderItemsToInvoice",locale));
        }

        try {
            GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
            if (orderHeader == null) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingNoOrderHeader",locale));
            }
            
            // figure out the invoice type
            String invoiceType = null;

            String orderType = orderHeader.getString("orderTypeId");
            if (orderType.equals("SALES_ORDER")) {
                invoiceType = "SALES_INVOICE";
            } else if (orderType.equals("PURCHASE_ORDER")) {
                invoiceType = "PURCHASE_INVOICE";
            }

            // Set the precision depending on the type of invoice
            int invoiceTypeDecimals = UtilNumber.getBigDecimalScale("invoice." + invoiceType + ".decimals");
            if (invoiceTypeDecimals == -1) invoiceTypeDecimals = DECIMALS;

            // Make an order read helper from the order
            OrderReadHelper orh = new OrderReadHelper(orderHeader);

            // get the product store
            GenericValue productStore = orh.getProductStore();

            // get the shipping adjustment mode (Y = Pro-Rate; N = First-Invoice)
            String prorateShipping = productStore != null ? productStore.getString("prorateShipping") : "Y";
            if (prorateShipping == null) {
                prorateShipping = "Y";
            }

            // get the billing parties
            String billToCustomerPartyId = orh.getBillToParty().getString("partyId");
            String billFromVendorPartyId = orh.getBillFromParty().getString("partyId");
           
            // get some price totals
            BigDecimal shippableAmount = orh.getShippableTotal(null);
            BigDecimal orderSubTotal = orh.getOrderItemsSubTotal();

            // these variables are for pro-rating order amounts across invoices, so they should not be rounded off for maximum accuracy
            BigDecimal invoiceShipProRateAmount = ZERO;
            BigDecimal invoiceSubTotal = ZERO;
            BigDecimal invoiceQuantity = ZERO;

            GenericValue billingAccount = orderHeader.getRelatedOne("BillingAccount");
            String billingAccountId = billingAccount != null ? billingAccount.getString("billingAccountId") : null;

            Timestamp invoiceDate = (Timestamp)context.get("eventDate");
            if (UtilValidate.isEmpty(invoiceDate)) {
                // TODO: ideally this should be the same time as when a shipment is sent and be passed in as a parameter
                invoiceDate = UtilDateTime.nowTimestamp();
            }
            // TODO: perhaps consider billing account net days term as well?
            Long orderTermNetDays = orh.getOrderTermNetDays();
            //Timestamp dueDate = null;
            Timestamp dueDate = invoiceDate;
            if (orderTermNetDays != null) {
                dueDate = UtilDateTime.getDayEnd(invoiceDate, orderTermNetDays);
            } 

            // See if estimated delivery date is present and use that as the due date
            Timestamp estimatedDeliveryDate = orderHeader.getTimestamp("estimatedDeliveryDate");            
            if (dueDate == null && estimatedDeliveryDate != null) {
            	dueDate = estimatedDeliveryDate;
            }
            List orderIds = EntityUtil.getFieldListFromEntityList(billItems, "orderId", true);
            if(orderIds.size()>1){
            	dueDate = invoiceDate;
            }
            // create the invoice record
            if (UtilValidate.isEmpty(invoiceId)) {
                Map<String, Object> createInvoiceContext = FastMap.newInstance();
                createInvoiceContext.put("partyId", billToCustomerPartyId);
                createInvoiceContext.put("partyIdFrom", billFromVendorPartyId);
                createInvoiceContext.put("billingAccountId", billingAccountId);
                createInvoiceContext.put("facilityId", orderHeader.getString("originFacilityId"));
                createInvoiceContext.put("description", orderHeader.getString("productSubscriptionTypeId"));
                createInvoiceContext.put("invoiceDate", invoiceDate);
                createInvoiceContext.put("dueDate", dueDate);
                if(UtilValidate.isNotEmpty(purposeTypeId)){
                	createInvoiceContext.put("purposeTypeId", purposeTypeId);
                }
                createInvoiceContext.put("billOfSaleTypeId", billOfSaleTypeId);
                createInvoiceContext.put("isEnableAcctg", orderHeader.getString("isEnableAcctg"));
                createInvoiceContext.put("invoiceTypeId", invoiceType);
                // start with INVOICE_IN_PROCESS, in the INVOICE_READY we can't change the invoice (or shouldn't be able to...)
                createInvoiceContext.put("statusId", "INVOICE_IN_PROCESS");
                createInvoiceContext.put("currencyUomId", orderHeader.getString("currencyUom"));
                createInvoiceContext.put("userLogin", userLogin);
                createInvoiceContext.put("invoiceMessage", orderHeader.getString("orderMessage"));

                // store the invoice first
                Map<String, Object> createInvoiceResult = dispatcher.runSync("createInvoice", createInvoiceContext);
                if (ServiceUtil.isError(createInvoiceResult)) {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceFromOrder",locale), null, null, createInvoiceResult);
                }

                // call service, not direct entity op: delegator.create(invoice);
                invoiceId = (String) createInvoiceResult.get("invoiceId");
            }

            // order roles to invoice roles
            //only get when invoice type for purchase
		       List<GenericValue> orderRoles = orderHeader.getRelated("OrderRole");
		        Map<String, Object> createInvoiceRoleContext = FastMap.newInstance();
		         createInvoiceRoleContext.put("invoiceId", invoiceId);
		        createInvoiceRoleContext.put("userLogin", userLogin);
		        
		       // if(invoiceType.equals("PURCHASE_INVOICE")) { 
		         if( UtilValidate.isNotEmpty(orderHeader.getString("salesChannelEnumId")) && (!"BYPROD_SALES_CHANNEL".equalsIgnoreCase(orderHeader.getString("salesChannelEnumId")))){     
		        	 for (GenericValue orderRole : orderRoles) {
			            createInvoiceRoleContext.put("partyId", orderRole.getString("partyId"));
			            createInvoiceRoleContext.put("roleTypeId", orderRole.getString("roleTypeId"));
			            Map<String, Object> createInvoiceRoleResult = dispatcher.runSync("createInvoiceRole", createInvoiceRoleContext);
			            if (ServiceUtil.isError(createInvoiceRoleResult)) {
			                return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceFromOrder",locale), null, null, createInvoiceRoleResult);
			            }
			        }
                  }//end of salesChannel if
               // }//end of PurchaseInvoice check

            // order terms to invoice terms.
            // TODO: it might be nice to filter OrderTerms to only copy over financial terms.
            List<GenericValue> orderTerms = orh.getOrderTerms();
            createInvoiceTerms(delegator, dispatcher, invoiceId, orderTerms, userLogin, locale);

            // billing accounts
            // List billingAccountTerms = null;
            // for billing accounts we will use related information
            if (billingAccount != null) {
                /*
                 * jacopoc: billing account terms were already copied as order terms
                 *          when the order was created.
                // get the billing account terms
                billingAccountTerms = billingAccount.getRelated("BillingAccountTerm");

                // set the invoice terms as defined for the billing account
                createInvoiceTerms(delegator, dispatcher, invoiceId, billingAccountTerms, userLogin, locale);
                */
                // set the invoice bill_to_customer from the billing account
                List<GenericValue> billToRoles = billingAccount.getRelated("BillingAccountRole", UtilMisc.toMap("roleTypeId", "BILL_TO_CUSTOMER"), null);
                for (GenericValue billToRole : billToRoles) {
                    if (!(billToRole.getString("partyId").equals(billToCustomerPartyId))) {
                        createInvoiceRoleContext = UtilMisc.toMap("invoiceId", invoiceId, "partyId", billToRole.get("partyId"),
                                                                           "roleTypeId", "BILL_TO_CUSTOMER", "userLogin", userLogin);
                        Map<String, Object> createInvoiceRoleResult = dispatcher.runSync("createInvoiceRole", createInvoiceRoleContext);
                        if (ServiceUtil.isError(createInvoiceRoleResult)) {
                            return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceRoleFromOrder",locale), null, null, createInvoiceRoleResult);
                        }
                    }
                }

                // set the bill-to contact mech as the contact mech of the billing account
                if (UtilValidate.isNotEmpty(billingAccount.getString("contactMechId"))) {
                    Map<String, Object> createBillToContactMechContext = UtilMisc.toMap("invoiceId", invoiceId, "contactMechId", billingAccount.getString("contactMechId"),
                                                                       "contactMechPurposeTypeId", "BILLING_LOCATION", "userLogin", userLogin);
                    Map<String, Object> createBillToContactMechResult = dispatcher.runSync("createInvoiceContactMech", createBillToContactMechContext);
                    if (ServiceUtil.isError(createBillToContactMechResult)) {
                        return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceContactMechFromOrder",locale), null, null, createBillToContactMechResult);
                    }
                }
            } else {
                List<GenericValue> billingLocations = orh.getBillingLocations();
                if (UtilValidate.isNotEmpty(billingLocations)) {
                    for (GenericValue ocm : billingLocations) {
                        Map<String, Object> createBillToContactMechContext = UtilMisc.toMap("invoiceId", invoiceId, "contactMechId", ocm.getString("contactMechId"),
                                                                           "contactMechPurposeTypeId", "BILLING_LOCATION", "userLogin", userLogin);
                        Map<String, Object> createBillToContactMechResult = dispatcher.runSync("createInvoiceContactMech", createBillToContactMechContext);
                        if (ServiceUtil.isError(createBillToContactMechResult)) {
                            return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceContactMechFromOrder",locale), null, null, createBillToContactMechResult);
                        }
                    }
                } else {
                    Debug.logInfo("No billing locations found for order [" + orderId +"] and none were created for Invoice [" + invoiceId + "]", module);
                }
            }

            // get a list of the payment method types
            //DEJ20050705 doesn't appear to be used: List paymentPreferences = orderHeader.getRelated("OrderPaymentPreference");

            // create the bill-from (or pay-to) contact mech as the primary PAYMENT_LOCATION of the party from the store
            GenericValue payToAddress = null;
            if (invoiceType.equals("PURCHASE_INVOICE")) {
                // for purchase orders, the pay to address is the BILLING_LOCATION of the vendor
                GenericValue billFromVendor = orh.getPartyFromRole("BILL_FROM_VENDOR");
                if (billFromVendor != null) {
                    List<GenericValue> billingContactMechs = billFromVendor.getRelatedOne("Party").getRelatedByAnd("PartyContactMechPurpose",
                            UtilMisc.toMap("contactMechPurposeTypeId", "BILLING_LOCATION"));
                    if (UtilValidate.isNotEmpty(billingContactMechs)) {
                        payToAddress = EntityUtil.getFirst(billingContactMechs);
                    }
                }
            } else {
                // for sales orders, it is the payment address on file for the store
                payToAddress = PaymentWorker.getPaymentAddress(delegator, productStore.getString("payToPartyId"));
            }
            if (payToAddress != null) {
                Map<String, Object> createPayToContactMechContext = UtilMisc.toMap("invoiceId", invoiceId, "contactMechId", payToAddress.getString("contactMechId"),
                                                                   "contactMechPurposeTypeId", "PAYMENT_LOCATION", "userLogin", userLogin);
                Map<String, Object> createPayToContactMechResult = dispatcher.runSync("createInvoiceContactMech", createPayToContactMechContext);
                if (ServiceUtil.isError(createPayToContactMechResult)) {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceContactMechFromOrder",locale), null, null, createPayToContactMechResult);
                }
            }

            // sequence for items - all OrderItems or InventoryReservations + all Adjustments
            int invoiceItemSeqNum = 1;
            String invoiceItemSeqId = UtilFormatOut.formatPaddedNumber(invoiceItemSeqNum, INVOICE_ITEM_SEQUENCE_ID_DIGITS);

            // create the item records
            for (GenericValue currentValue : billItems) {
                GenericValue itemIssuance = null;
                GenericValue orderItem = null;
                GenericValue shipmentReceipt = null;
                if ("ItemIssuance".equals(currentValue.getEntityName())) {
                    itemIssuance = currentValue;
                } else if ("OrderItem".equals(currentValue.getEntityName())) {
                    orderItem = currentValue;
                } else if ("ShipmentReceipt".equals(currentValue.getEntityName())) {
                    shipmentReceipt = currentValue;
                } else {
                    Debug.logError("Unexpected entity " + currentValue + " of type " + currentValue.getEntityName(), module);
                }
                if (orderItem == null && itemIssuance != null) {
                    orderItem = itemIssuance.getRelatedOne("OrderItem");
                } else if ((orderItem == null) && (shipmentReceipt != null)) {
                    orderItem = shipmentReceipt.getRelatedOne("OrderItem");
                } else if ((orderItem == null) && (itemIssuance == null) && (shipmentReceipt == null)) {
                    Debug.logError("Cannot create invoice when orderItem, itemIssuance, and shipmentReceipt are all null", module);
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingIllegalValuesPassedToCreateInvoiceService",locale));
                }
                GenericValue product = null;
                if (orderItem.get("productId") != null) {
                    product = orderItem.getRelatedOne("Product");
                }
                // get some quantities
                BigDecimal billingQuantity = null;
                if (itemIssuance != null) {
                    billingQuantity = itemIssuance.getBigDecimal("quantity");
                    BigDecimal cancelQty = itemIssuance.getBigDecimal("cancelQuantity");
                    if (cancelQty == null) {
                        cancelQty = ZERO;
                    }
                    billingQuantity = billingQuantity.subtract(cancelQty).setScale(DECIMALS, ROUNDING);
                } else if (shipmentReceipt != null) {
                    billingQuantity = shipmentReceipt.getBigDecimal("quantityAccepted");
                } else {
                    BigDecimal orderedQuantity = OrderReadHelper.getOrderItemQuantity(orderItem);
                    BigDecimal invoicedQuantity = OrderReadHelper.getOrderItemInvoicedQuantity(orderItem);
                    billingQuantity = orderedQuantity.subtract(invoicedQuantity);
                    if (billingQuantity.compareTo(ZERO) < 0) {
                        billingQuantity = ZERO;
                    }
                }
                if (billingQuantity == null) billingQuantity = ZERO;
                // check if shipping applies to this item.  Shipping is calculated for sales invoices, not purchase invoices.
                boolean shippingApplies = false;
                if ((product != null) && (ProductWorker.shippingApplies(product)) && (invoiceType.equals("SALES_INVOICE"))) {
                    shippingApplies = true;
                }

                BigDecimal billingAmount = orderItem.getBigDecimal("unitPrice").setScale(invoiceTypeDecimals, ROUNDING);
                Map<String, Object> createInvoiceItemContext = FastMap.newInstance();
                createInvoiceItemContext.put("invoiceId", invoiceId);
                createInvoiceItemContext.put("invoiceItemSeqId", invoiceItemSeqId);
                createInvoiceItemContext.put("invoiceItemTypeId", getInvoiceItemType(delegator, (orderItem.getString("orderItemTypeId")), (product == null ? null : product.getString("productTypeId")), invoiceType, "INV_FPROD_ITEM"));
                createInvoiceItemContext.put("description", orderItem.get("itemDescription"));
                createInvoiceItemContext.put("quantity", billingQuantity);
                createInvoiceItemContext.put("amount", billingAmount);
                createInvoiceItemContext.put("productId", orderItem.get("productId"));
                createInvoiceItemContext.put("unitPrice", orderItem.get("unitPrice"));
                createInvoiceItemContext.put("unitListPrice", orderItem.get("unitListPrice"));
                createInvoiceItemContext.put("bedAmount", orderItem.get("bedAmount"));
                createInvoiceItemContext.put("bedPercent", orderItem.get("bedPercent"));
                createInvoiceItemContext.put("bedcessPercent", orderItem.get("bedcessPercent"));
                createInvoiceItemContext.put("bedcessAmount", orderItem.get("bedcessAmount"));
                createInvoiceItemContext.put("bedseccessPercent", orderItem.get("bedseccessPercent"));
                createInvoiceItemContext.put("bedseccessAmount", orderItem.get("bedseccessAmount"));
                createInvoiceItemContext.put("vatAmount", orderItem.get("vatAmount"));
                createInvoiceItemContext.put("vatPercent", orderItem.get("vatPercent"));
                createInvoiceItemContext.put("cstAmount", orderItem.get("cstAmount"));
                createInvoiceItemContext.put("cstPercent", orderItem.get("cstPercent"));
                createInvoiceItemContext.put("tcsAmount", orderItem.get("tcsAmount"));
                createInvoiceItemContext.put("tcsPercent", orderItem.get("tcsPercent"));
                createInvoiceItemContext.put("serviceTaxAmount", orderItem.get("serviceTaxAmount"));
                createInvoiceItemContext.put("serviceTaxPercent", orderItem.get("serviceTaxPercent"));
                createInvoiceItemContext.put("productFeatureId", orderItem.get("productFeatureId"));
                createInvoiceItemContext.put("overrideGlAccountId", orderItem.get("overrideGlAccountId"));
                //createInvoiceItemContext.put("uomId", "");
                createInvoiceItemContext.put("userLogin", userLogin);
                
                String itemIssuanceId = null;
                if (itemIssuance != null && itemIssuance.get("inventoryItemId") != null) {
                    itemIssuanceId = itemIssuance.getString("itemIssuanceId");
                    createInvoiceItemContext.put("inventoryItemId", itemIssuance.get("inventoryItemId"));
                }
                // similarly, tax only for purchase invoices
                if ((product != null) && (invoiceType.equals("SALES_INVOICE"))) {
                    createInvoiceItemContext.put("taxableFlag", product.get("taxable"));
                }
                Map<String, Object> createInvoiceItemResult = dispatcher.runSync("createInvoiceItem", createInvoiceItemContext);
                if (ServiceUtil.isError(createInvoiceItemResult)) {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceItemFromOrder",locale), null, null, createInvoiceItemResult);
                }

                // this item total
                BigDecimal thisAmount = billingAmount.multiply(billingQuantity).setScale(invoiceTypeDecimals, ROUNDING);

                // add to the ship amount only if it applies to this item
                if (shippingApplies) {
                    invoiceShipProRateAmount = invoiceShipProRateAmount.add(thisAmount).setScale(invoiceTypeDecimals, ROUNDING);
                }

                // increment the invoice subtotal
                invoiceSubTotal = invoiceSubTotal.add(thisAmount).setScale(100, ROUNDING);

                // increment the invoice quantity
                invoiceQuantity = invoiceQuantity.add(billingQuantity).setScale(invoiceTypeDecimals, ROUNDING);

                // create the OrderItemBilling record
                Map<String, Object> createOrderItemBillingContext = FastMap.newInstance();
                createOrderItemBillingContext.put("invoiceId", invoiceId);
                createOrderItemBillingContext.put("invoiceItemSeqId", invoiceItemSeqId);
                createOrderItemBillingContext.put("orderId", orderItem.get("orderId"));
                createOrderItemBillingContext.put("orderItemSeqId", orderItem.get("orderItemSeqId"));
                createOrderItemBillingContext.put("itemIssuanceId", itemIssuanceId);
                createOrderItemBillingContext.put("quantity", billingQuantity);
                createOrderItemBillingContext.put("amount", billingAmount);
                createOrderItemBillingContext.put("userLogin", userLogin);
                if ((shipmentReceipt != null) && (shipmentReceipt.getString("receiptId") != null)) {
                    createOrderItemBillingContext.put("shipmentReceiptId", shipmentReceipt.getString("receiptId"));
                }

                Map<String, Object> createOrderItemBillingResult = dispatcher.runSync("createOrderItemBilling", createOrderItemBillingContext);
                if (ServiceUtil.isError(createOrderItemBillingResult)) {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingOrderItemBillingFromOrder",locale), null, null, createOrderItemBillingResult);
                }

                if ("ItemIssuance".equals(currentValue.getEntityName())) {
                    List<GenericValue> shipmentItemBillings = delegator.findByAnd("ShipmentItemBilling", UtilMisc.toMap("shipmentId", currentValue.get("shipmentId")));
                    if (UtilValidate.isEmpty(shipmentItemBillings)) {

                        // create the ShipmentItemBilling record
                        GenericValue shipmentItemBilling = delegator.makeValue("ShipmentItemBilling", UtilMisc.toMap("invoiceId", invoiceId, "invoiceItemSeqId", invoiceItemSeqId));
                        shipmentItemBilling.put("shipmentId", currentValue.get("shipmentId"));
                        shipmentItemBilling.put("shipmentItemSeqId", currentValue.get("shipmentItemSeqId"));
                        shipmentItemBilling.create();
                    }
                }

                String parentInvoiceItemSeqId = invoiceItemSeqId;
                // increment the counter
                invoiceItemSeqNum++;
                invoiceItemSeqId = UtilFormatOut.formatPaddedNumber(invoiceItemSeqNum, INVOICE_ITEM_SEQUENCE_ID_DIGITS);

                // Get the original order item from the DB, in case the quantity has been overridden
                GenericValue originalOrderItem = delegator.findByPrimaryKey("OrderItem", UtilMisc.toMap("orderId", orderId, "orderItemSeqId", orderItem.getString("orderItemSeqId")));

                // create the item adjustment as line items
                List<GenericValue> itemAdjustments = OrderReadHelper.getOrderItemAdjustmentList(orderItem, orh.getAdjustments());
                for (GenericValue adj : itemAdjustments) {

                    // Check against OrderAdjustmentBilling to see how much of this adjustment has already been invoiced
                    BigDecimal adjAlreadyInvoicedAmount = null;
                    try {
                        Map<String, Object> checkResult = dispatcher.runSync("calculateInvoicedAdjustmentTotal", UtilMisc.toMap("orderAdjustment", adj));
                        adjAlreadyInvoicedAmount = (BigDecimal) checkResult.get("invoicedTotal");
                    } catch (GenericServiceException e) {
                        String errMsg = UtilProperties.getMessage(resource, "AccountingTroubleCallingCalculateInvoicedAdjustmentTotalService", locale);
                        Debug.logError(e, errMsg, module);
                        return ServiceUtil.returnError(errMsg);
                    }
                    // If the absolute invoiced amount >= the abs of the adjustment amount, the full amount has already been invoiced,
                    //  so skip this adjustment
                    if (adj.get("amount") == null) { // JLR 17/4/7 : fix a bug coming from POS in case of use of a discount (on item(s) or sale, item(s) here) and a cash amount higher than total (hence issuing change)
                        continue;
                    }
                    if (adjAlreadyInvoicedAmount.abs().compareTo(adj.getBigDecimal("amount").setScale(invoiceTypeDecimals, ROUNDING).abs()) > 0) {
                        continue;
                    }

                    BigDecimal amount = ZERO;
                    if (adj.get("amount") != null) {
                        // pro-rate the amount
                        // set decimals = 100 means we don't round this intermediate value, which is very important
                        amount = adj.getBigDecimal("amount").divide(originalOrderItem.getBigDecimal("quantity"), 100, ROUNDING);
                        amount = amount.multiply(billingQuantity);
                        // Tax needs to be rounded differently from other order adjustments
                        if (adj.getString("orderAdjustmentTypeId").equals("SALES_TAX")) {
                            amount = amount.setScale(TAX_DECIMALS, TAX_ROUNDING);
                        } else {
                            amount = amount.setScale(invoiceTypeDecimals, ROUNDING);
                        }
                    } else if (adj.get("sourcePercentage") != null) {
                        // pro-rate the amount
                        // set decimals = 100 means we don't round this intermediate value, which is very important
                        BigDecimal percent = adj.getBigDecimal("sourcePercentage");
                        percent = percent.divide(new BigDecimal(100), 100, ROUNDING);
                        amount = billingAmount.multiply(percent);
                        amount = amount.divide(originalOrderItem.getBigDecimal("quantity"), 100, ROUNDING);
                        amount = amount.multiply(billingQuantity);
                        amount = amount.setScale(invoiceTypeDecimals, ROUNDING);
                    }
                    if (amount.signum() != 0) {
                        Map<String, Object> createInvoiceItemAdjContext = FastMap.newInstance();
                		GenericValue orderAdjustmentType = adj.getRelatedOne("OrderAdjustmentType");                        
                        createInvoiceItemAdjContext.put("invoiceId", invoiceId);
                        createInvoiceItemAdjContext.put("invoiceItemSeqId", invoiceItemSeqId);
                        createInvoiceItemAdjContext.put("invoiceItemTypeId", getInvoiceItemType(delegator, adj.getString("orderAdjustmentTypeId"), null, invoiceType, "INVOICE_ITM_ADJ"));
                        if (("SALES_TAX".equals(orderAdjustmentType.getString("parentTypeId")))) {
                            createInvoiceItemAdjContext.put("invoiceItemTypeId", adj.getString("orderAdjustmentTypeId"));
                        }
                        if (("PURCHASE_TAX".equals(orderAdjustmentType.getString("parentTypeId")))) {
                            createInvoiceItemAdjContext.put("invoiceItemTypeId", adj.getString("orderAdjustmentTypeId"));
                        }
                        createInvoiceItemAdjContext.put("quantity", BigDecimal.ONE);
                        createInvoiceItemAdjContext.put("amount", amount);
                        createInvoiceItemAdjContext.put("overrideGlAccountId", adj.get("overrideGlAccountId"));
                        if ((!"SALES_TAX".equals(orderAdjustmentType.getString("parentTypeId"))) &&(!"PURCHASE_TAX".equals(orderAdjustmentType.getString("parentTypeId")))) {
                        //if (!("SALES_TAX".equals(orderAdjustmentType.getString("parentTypeId")))) {   
                            createInvoiceItemAdjContext.put("productId", orderItem.get("productId"));
                            createInvoiceItemAdjContext.put("productFeatureId", orderItem.get("productFeatureId"));                        	
                        	createInvoiceItemAdjContext.put("parentInvoiceId", invoiceId);
                        	createInvoiceItemAdjContext.put("parentInvoiceItemSeqId", parentInvoiceItemSeqId);
                        }
                        //createInvoiceItemAdjContext.put("uomId", "");
                        createInvoiceItemAdjContext.put("userLogin", userLogin);
                        createInvoiceItemAdjContext.put("taxAuthPartyId", adj.get("taxAuthPartyId"));
                        createInvoiceItemAdjContext.put("taxAuthGeoId", adj.get("taxAuthGeoId"));
                        createInvoiceItemAdjContext.put("taxAuthorityRateSeqId", adj.get("taxAuthorityRateSeqId"));

                        // some adjustments fill out the comments field instead
                        if ((!"SALES_TAX".equals(orderAdjustmentType.getString("parentTypeId"))) &&(!"PURCHASE_TAX".equals(orderAdjustmentType.getString("parentTypeId")))) {
                       // if (!("SALES_TAX".equals(orderAdjustmentType.getString("parentTypeId")))) {
                        	String description = (UtilValidate.isEmpty(adj.getString("description")) ? adj.getString("comments") : adj.getString("description"));
                        	createInvoiceItemAdjContext.put("description", description);
                        }
                        // invoice items for sales tax are not taxable themselves
                        // TODO: This is not an ideal solution. Instead, we need to use OrderAdjustment.includeInTax when it is implemented
                        if ((!"SALES_TAX".equals(orderAdjustmentType.getString("parentTypeId"))) &&(!"PURCHASE_TAX".equals(orderAdjustmentType.getString("parentTypeId")))) {
                        //if (!("SALES_TAX".equals(orderAdjustmentType.getString("parentTypeId")))) {
                            createInvoiceItemAdjContext.put("taxableFlag", product.get("taxable"));
                        }

                        // If the OrderAdjustment is associated to a ProductPromo,
                        // and the field ProductPromo.overrideOrgPartyId is set,
                        // copy the value to InvoiceItem.overrideOrgPartyId: this
                        // represent an organization override for the payToPartyId
                        if (UtilValidate.isNotEmpty(adj.getString("productPromoId"))) {
                            try {
                                GenericValue productPromo = adj.getRelatedOne("ProductPromo");
                                if (UtilValidate.isNotEmpty(productPromo.getString("overrideOrgPartyId"))) {
                                    createInvoiceItemAdjContext.put("overrideOrgPartyId", productPromo.getString("overrideOrgPartyId"));
                                }
                            } catch (GenericEntityException e) {
                                Debug.logError(e, "Error looking up ProductPromo with id [" + adj.getString("productPromoId") + "]", module);
                            }
                        }

                        Map<String, Object> createInvoiceItemAdjResult = dispatcher.runSync("createInvoiceItem", createInvoiceItemAdjContext);
                        if (ServiceUtil.isError(createInvoiceItemAdjResult)) {
                            return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceItemFromOrder",locale), null, null, createInvoiceItemAdjResult);
                        }

                        // Create the OrderAdjustmentBilling record
                        Map<String, Object> createOrderAdjustmentBillingContext = FastMap.newInstance();
                        createOrderAdjustmentBillingContext.put("orderAdjustmentId", adj.getString("orderAdjustmentId"));
                        createOrderAdjustmentBillingContext.put("invoiceId", invoiceId);
                        createOrderAdjustmentBillingContext.put("invoiceItemSeqId", invoiceItemSeqId);
                        createOrderAdjustmentBillingContext.put("amount", amount);
                        createOrderAdjustmentBillingContext.put("userLogin", userLogin);

                        Map<String, Object> createOrderAdjustmentBillingResult = dispatcher.runSync("createOrderAdjustmentBilling", createOrderAdjustmentBillingContext);
                        if (ServiceUtil.isError(createOrderAdjustmentBillingResult)) {
                            return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingOrderAdjustmentBillingFromOrder",locale), null, null, createOrderAdjustmentBillingContext);
                        }

                        // this adjustment amount
                        BigDecimal thisAdjAmount = amount;

                        // adjustments only apply to totals when they are not tax or shipping adjustments
                       // if (!"SALES_TAX".equals(orderAdjustmentType.getString("parentTypeId")) &&!"SHIPPING_ADJUSTMENT".equals(adj.getString("orderAdjustmentTypeId"))) {
                       if ((!"SALES_TAX".equals(orderAdjustmentType.getString("parentTypeId"))) &&(!"PURCHASE_TAX".equals(orderAdjustmentType.getString("parentTypeId")))&&(!"SHIPPING_ADJUSTMENT".equals(orderAdjustmentType.getString("parentTypeId")))) { 	
                            // increment the invoice subtotal
                            invoiceSubTotal = invoiceSubTotal.add(thisAdjAmount).setScale(100, ROUNDING);
                           
                            // add to the ship amount only if it applies to this item
                            if (shippingApplies) {
                                invoiceShipProRateAmount = invoiceShipProRateAmount.add(thisAdjAmount).setScale(invoiceTypeDecimals, ROUNDING);
                            }
                        }

                        // increment the counter
                        invoiceItemSeqNum++;
                        invoiceItemSeqId = UtilFormatOut.formatPaddedNumber(invoiceItemSeqNum, INVOICE_ITEM_SEQUENCE_ID_DIGITS);
                    }
                }
            }

            // create header adjustments as line items -- always to tax/shipping last
            Map<GenericValue, BigDecimal> shipAdjustments = FastMap.newInstance();
            Map<GenericValue, BigDecimal> taxAdjustments = FastMap.newInstance();

            List<GenericValue> headerAdjustments = orh.getOrderHeaderAdjustments();
            for (GenericValue adj : headerAdjustments) {

                // Check against OrderAdjustmentBilling to see how much of this adjustment has already been invoiced
                BigDecimal adjAlreadyInvoicedAmount = null;
                try {
                    Map<String, Object> checkResult = dispatcher.runSync("calculateInvoicedAdjustmentTotal", UtilMisc.toMap("orderAdjustment", adj));
                    adjAlreadyInvoicedAmount = ((BigDecimal) checkResult.get("invoicedTotal")).setScale(invoiceTypeDecimals, ROUNDING);
                } catch (GenericServiceException e) {
                    String errMsg = UtilProperties.getMessage(resource, "AccountingTroubleCallingCalculateInvoicedAdjustmentTotalService", locale);
                    Debug.logError(e, errMsg, module);
                    return ServiceUtil.returnError(errMsg);
                }

                // If the absolute invoiced amount >= the abs of the adjustment amount, the full amount has already been invoiced,
                //  so skip this adjustment
                if (null == adj.get("amount")) { // JLR 17/4/7 : fix a bug coming from POS in case of use of a discount (on item(s) or sale, sale here) and a cash amount higher than total (hence issuing change)
                    continue;
                }
                if (adjAlreadyInvoicedAmount.abs().compareTo(adj.getBigDecimal("amount").setScale(invoiceTypeDecimals, ROUNDING).abs()) > 0) {
                    continue;
                }

                if ("SHIPPING_CHARGES".equals(adj.getString("orderAdjustmentTypeId"))) {
                    shipAdjustments.put(adj, adjAlreadyInvoicedAmount);
                } else if ("SALES_TAX".equals(adj.getString("orderAdjustmentTypeId"))) {
                    taxAdjustments.put(adj, adjAlreadyInvoicedAmount);
                } else {
                    // these will effect the shipping pro-rate (unless commented)
                    // other adjustment type
                	 //Debug.log("=adjAmount=BEFOREE=ELSE=TYPE=>"+adj.getString("orderAdjustmentTypeId")+"==");
                	 //Debug.log("=orderSubTotal=>"+orderSubTotal+"=invoiceSubTotal="+invoiceSubTotal+"adj.amount="+adj.getBigDecimal("amount")+"=adj.amount=ROUNDING="+adj.getBigDecimal("amount").setScale(invoiceTypeDecimals, ROUNDING)+"=invoiceTypeDecimals="+invoiceTypeDecimals+"=ROUNDING="+ROUNDING);
                    BigDecimal adjAmount = calcHeaderAdj(delegator, adj, invoiceType, invoiceId, invoiceItemSeqId,
                            orderSubTotal, invoiceSubTotal, adj.getBigDecimal("amount").setScale(invoiceTypeDecimals, ROUNDING), invoiceTypeDecimals, ROUNDING, userLogin, dispatcher, locale);
                    // invoiceShipProRateAmount += adjAmount;
                    // do adjustments compound or are they based off subtotal? Here we will (unless commented)
                    // invoiceSubTotal += adjAmount;
                    //Debug.log("=adjAmount==In==ELSEEE==>"+adjAmount);
                    // increment the counter
                    invoiceItemSeqNum++;
                    invoiceItemSeqId = UtilFormatOut.formatPaddedNumber(invoiceItemSeqNum, INVOICE_ITEM_SEQUENCE_ID_DIGITS);
                }
            }

            // next do the shipping adjustments.  Note that we do not want to add these to the invoiceSubTotal or orderSubTotal for pro-rating tax later, as that would cause
            // numerator/denominator problems when the shipping is not pro-rated but rather charged all on the first invoice
            for (GenericValue adj : shipAdjustments.keySet()) {
                BigDecimal adjAlreadyInvoicedAmount = shipAdjustments.get(adj);
                
                if ("N".equalsIgnoreCase(prorateShipping)) {

                    // Set the divisor and multiplier to 1 to avoid prorating
                    BigDecimal divisor = BigDecimal.ONE;
                    BigDecimal multiplier = BigDecimal.ONE;

                    // The base amount in this case is the adjustment amount minus the total already invoiced for that adjustment, since
                    //  it won't be prorated
                    BigDecimal baseAmount = adj.getBigDecimal("amount").setScale(invoiceTypeDecimals, ROUNDING).subtract(adjAlreadyInvoicedAmount);
                    BigDecimal adjAmount = calcHeaderAdj(delegator, adj, invoiceType, invoiceId, invoiceItemSeqId,
                            divisor, multiplier, baseAmount, invoiceTypeDecimals, ROUNDING, userLogin, dispatcher, locale);
                } else {

                    // Pro-rate the shipping amount based on shippable information
                    BigDecimal divisor = shippableAmount;
                    BigDecimal multiplier = invoiceShipProRateAmount;

                    // The base amount in this case is the adjustment amount, since we want to prorate based on the full amount
                    BigDecimal baseAmount = adj.getBigDecimal("amount").setScale(invoiceTypeDecimals, ROUNDING);
                    BigDecimal adjAmount = calcHeaderAdj(delegator, adj, invoiceType, invoiceId, invoiceItemSeqId,
                            divisor, multiplier, baseAmount, invoiceTypeDecimals, ROUNDING, userLogin, dispatcher, locale);
                }

                // Increment the counter
                invoiceItemSeqNum++;
                invoiceItemSeqId = UtilFormatOut.formatPaddedNumber(invoiceItemSeqNum, INVOICE_ITEM_SEQUENCE_ID_DIGITS);
            }

            // last do the tax adjustments
            String prorateTaxes = productStore != null ? productStore.getString("prorateTaxes") : "Y";
            if (prorateTaxes == null) {
                prorateTaxes = "Y";
            }
            for (GenericValue adj : taxAdjustments.keySet()) {
                BigDecimal adjAlreadyInvoicedAmount = taxAdjustments.get(adj);
                BigDecimal adjAmount = null;

                if ("N".equalsIgnoreCase(prorateTaxes)) {

                    // Set the divisor and multiplier to 1 to avoid prorating
                    BigDecimal divisor = BigDecimal.ONE;
                    BigDecimal multiplier = BigDecimal.ONE;

                    // The base amount in this case is the adjustment amount minus the total already invoiced for that adjustment, since
                    //  it won't be prorated
                    BigDecimal baseAmount = adj.getBigDecimal("amount").setScale(TAX_DECIMALS, TAX_ROUNDING).subtract(adjAlreadyInvoicedAmount);
                    adjAmount = calcHeaderAdj(delegator, adj, invoiceType, invoiceId, invoiceItemSeqId,
                             divisor, multiplier, baseAmount, TAX_DECIMALS, TAX_ROUNDING, userLogin, dispatcher, locale);
                } else {

                    // Pro-rate the tax amount based on shippable information
                    BigDecimal divisor = orderSubTotal;
                    BigDecimal multiplier = invoiceSubTotal;

                    // The base amount in this case is the adjustment amount, since we want to prorate based on the full amount
                    BigDecimal baseAmount = adj.getBigDecimal("amount");
                    adjAmount = calcHeaderAdj(delegator, adj, invoiceType, invoiceId, invoiceItemSeqId,
                            divisor, multiplier, baseAmount, TAX_DECIMALS, TAX_ROUNDING, userLogin, dispatcher, locale);
                }
                invoiceSubTotal = invoiceSubTotal.add(adjAmount).setScale(invoiceTypeDecimals, ROUNDING);

                // Increment the counter
                invoiceItemSeqNum++;
                invoiceItemSeqId = UtilFormatOut.formatPaddedNumber(invoiceItemSeqNum, INVOICE_ITEM_SEQUENCE_ID_DIGITS);
            }

            // check for previous order payments
            List<GenericValue> orderPaymentPrefs = delegator.findByAnd("OrderPaymentPreference", UtilMisc.toMap("orderId", orderId));
            List<GenericValue> currentPayments = FastList.newInstance();
            for (GenericValue paymentPref : orderPaymentPrefs) {
                List<GenericValue> payments = paymentPref.getRelated("Payment");
                currentPayments.addAll(payments);
            }
            // apply these payments to the invoice if they have any remaining amount to apply
            for (GenericValue payment : currentPayments) {
                BigDecimal notApplied = PaymentWorker.getPaymentNotApplied(payment);
                if (notApplied.signum() > 0) {
                    Map<String, Object> appl = FastMap.newInstance();
                    appl.put("paymentId", payment.get("paymentId"));
                    appl.put("invoiceId", invoiceId);
                    appl.put("billingAccountId", billingAccountId);
                    appl.put("amountApplied", notApplied);
                    appl.put("userLogin", userLogin);
                    Map<String, Object> createPayApplResult = dispatcher.runSync("createPaymentApplication", appl);
                    if (ServiceUtil.isError(createPayApplResult)) {
                        return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceFromOrder",locale), null, null, createPayApplResult);
                    }
                }
            }

            // Should all be in place now. Depending on the ProductStore.autoApproveInvoice setting, set status to INVOICE_READY (unless it's a purchase invoice, which we set to INVOICE_IN_PROCESS)
            String autoApproveInvoice = productStore != null ? productStore.getString("autoApproveInvoice") : "Y";
            if (!"N".equals(autoApproveInvoice)) {
                String nextStatusId = "PURCHASE_INVOICE".equals(invoiceType) ? "INVOICE_IN_PROCESS" : "INVOICE_APPROVED";
                Map<String, Object> setInvoiceStatusResult = dispatcher.runSync("setInvoiceStatus", UtilMisc.<String, Object>toMap("invoiceId", invoiceId, "statusId", nextStatusId, "userLogin", userLogin));
                if (ServiceUtil.isError(setInvoiceStatusResult)) {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceFromOrder",locale), null, null, setInvoiceStatusResult);
                }
                /*  :TODO:: For now we'll leave the invoice in APROVED STATUS
                nextStatusId = "INVOICE_READY";
                setInvoiceStatusResult = dispatcher.runSync("setInvoiceStatus", UtilMisc.<String, Object>toMap("invoiceId", invoiceId, "statusId", nextStatusId, "userLogin", userLogin));
                if (ServiceUtil.isError(setInvoiceStatusResult)) {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceFromOrder",locale), null, null, setInvoiceStatusResult);
                }    
                */            
            }

            Map<String, Object> resp = ServiceUtil.returnSuccess();
            resp.put("invoiceId", invoiceId);
            resp.put("invoiceTypeId", invoiceType);
            return resp;
        } catch (GenericEntityException e) {
            String errMsg = UtilProperties.getMessage(resource, "AccountingEntityDataProblemCreatingInvoiceFromOrderItems", UtilMisc.toMap("reason", e.toString()), locale);
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        } catch (GenericServiceException e) {
            String errMsg = UtilProperties.getMessage(resource, "AccountingServiceOtherProblemCreatingInvoiceFromOrderItems", UtilMisc.toMap("reason", e.toString()), locale);
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        }
    }
    
    
    // Service for creating commission invoices
    public static Map<String, Object> createCommissionInvoices(DispatchContext dctx, Map<String, Object> context) {
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");
        List<String> salesInvoiceIds = UtilGenerics.checkList(context.get("invoiceIds"));
        List<Map<String, String>> invoicesCreated = FastList.newInstance();
        Map<String, List<Map<String, Object>>> commissionParties = FastMap.newInstance();
        for (String salesInvoiceId : salesInvoiceIds) {
            List<String> salesRepPartyIds = UtilGenerics.checkList(context.get("partyIds"));
            BigDecimal amountTotal =  InvoiceWorker.getInvoiceTotal(delegator, salesInvoiceId);
            if (amountTotal.signum() == 0) {
                Debug.logWarning("Invoice [" + salesInvoiceId + "] has an amount total of [" + amountTotal + "], so no commission invoice will be created", module);
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingInvoiceCommissionZeroInvoiceAmount",locale));
            }
            BigDecimal appliedFraction = amountTotal.divide(amountTotal, 12, ROUNDING);
            GenericValue invoice = null;
            boolean isReturn = false;
            List<String> billFromVendorInvoiceRoles = new ArrayList<String>();
            List<GenericValue> invoiceItems = new ArrayList<GenericValue>();
            try {
                List<EntityExpr> invoiceRoleConds = UtilMisc.toList(
                        EntityCondition.makeCondition("invoiceId", EntityOperator.EQUALS, salesInvoiceId),
                        EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "BILL_FROM_VENDOR"));
                billFromVendorInvoiceRoles = EntityUtil.getFieldListFromEntityList(delegator.findList("InvoiceRole", EntityCondition.makeCondition(invoiceRoleConds, EntityOperator.AND), null, null, null, false), "partyId", true);
                invoiceRoleConds = UtilMisc.toList(
                        EntityCondition.makeCondition("invoiceId", EntityOperator.EQUALS, salesInvoiceId),
                        EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "SALES_REP"));
                // if the receiving parties is empty then we will create commission invoices for all sales agent associated to sales invoice.
                if (UtilValidate.isEmpty(salesRepPartyIds)) {
                    salesRepPartyIds = EntityUtil.getFieldListFromEntityList(delegator.findList("InvoiceRole", EntityCondition.makeCondition(invoiceRoleConds, EntityOperator.AND), null, null, null, false), "partyId", true);
                    if (UtilValidate.isEmpty(salesRepPartyIds)) {
                        return ServiceUtil.returnError(UtilProperties.getMessage(resource,"No party found with role sales representative for sales invoice "+ salesInvoiceId,locale));
                    }
                } else {
                    List<String> salesInvoiceRolePartyIds = EntityUtil.getFieldListFromEntityList(delegator.findList("InvoiceRole", EntityCondition.makeCondition(invoiceRoleConds, EntityOperator.AND), null, null, null, false), "partyId", true);
                    if (UtilValidate.isNotEmpty(salesInvoiceRolePartyIds)) {
                        salesRepPartyIds = UtilGenerics.checkList(CollectionUtils.intersection(salesRepPartyIds, salesInvoiceRolePartyIds));
                    }
                }
                invoice = delegator.findOne("Invoice", UtilMisc.toMap("invoiceId", salesInvoiceId), false);
                String invoiceTypeId = invoice.getString("invoiceTypeId");
                if ("CUST_RTN_INVOICE".equals(invoiceTypeId)) {
                    isReturn = true;
                } else if (!"SALES_INVOICE".equals(invoiceTypeId)) {
                    Debug.logWarning("This type of invoice has no commission; returning success", module);
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingInvoiceCommissionInvalid",locale));
                }
                invoiceItems = delegator.findList("InvoiceItem", EntityCondition.makeCondition("invoiceId", EntityOperator.EQUALS, salesInvoiceId), null, null, null, false);
            } catch (GenericEntityException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
            // Map of commission Lists (of Maps) for each party.
            // Determine commissions for various parties.
            for (GenericValue invoiceItem : invoiceItems) {
                BigDecimal amount = ZERO;
                BigDecimal quantity = ZERO;
                quantity = invoiceItem.getBigDecimal("quantity");
                amount = invoiceItem.getBigDecimal("amount");
                amount = isReturn ? amount.negate() : amount;
                String productId = invoiceItem.getString("productId");
                String invoiceItemSeqId = invoiceItem.getString("invoiceItemSeqId");
                String invoiceId = invoiceItem.getString("invoiceId");
                // Determine commission parties for this invoiceItem
                if (UtilValidate.isNotEmpty(productId)) {
                    Map<String, Object> resultMap = null;
                    try {
                        resultMap = dispatcher.runSync("getCommissionForProduct", UtilMisc.<String, Object>toMap(
                                "productId", productId,
                                "invoiceId", invoiceId,
                                "invoiceItemSeqId", invoiceItemSeqId,
                                "invoiceItemTypeId", invoiceItem.getString("invoiceItemTypeId"),
                                "amount", amount,
                                "quantity", quantity,
                                "userLogin", userLogin));
                    } catch (GenericServiceException e) {
                        return ServiceUtil.returnError(e.getMessage());
                    }
                    // build a Map of partyIds (both to and from) in a commission and the amounts
                    // Note that getCommissionForProduct returns a List of Maps with a lot values.  See services.xml definition for reference.
                    List<Map<String, Object>> itemCommissions = UtilGenerics.checkList(resultMap.get("commissions"));
                    if (UtilValidate.isNotEmpty(itemCommissions)) {
                        for (Map<String, Object> commissionMap : itemCommissions) {
                            commissionMap.put("invoice", invoice);
                            commissionMap.put("appliedFraction", appliedFraction);
                            if (!billFromVendorInvoiceRoles.contains(commissionMap.get("partyIdFrom")) || !salesRepPartyIds.contains(commissionMap.get("partyIdTo"))) {
                                continue;
                            }
                            String partyIdFromTo = (String) commissionMap.get("partyIdFrom") + (String) commissionMap.get("partyIdTo");
                            if (!commissionParties.containsKey(partyIdFromTo)) {
                                commissionParties.put(partyIdFromTo, UtilMisc.toList(commissionMap));
                            } else {
                                (commissionParties.get(partyIdFromTo)).add(commissionMap);
                            }
                        }
                    }
                }
            }
        }
        Timestamp now = UtilDateTime.nowTimestamp();
        // Create invoice for each commission receiving party
        for (Map.Entry<String, List<Map<String, Object>>> commissionParty : commissionParties.entrySet()) {
            List<GenericValue> toStore = FastList.newInstance();
            List<Map<String, Object>> commList = commissionParty.getValue();
            // get the billing parties
            if (UtilValidate.isEmpty(commList)) {
                continue;
            }
            // From and To are reversed between commission and invoice
            String partyIdBillTo = (String) (commList.get(0)).get("partyIdFrom");
            String partyIdBillFrom = (String) (commList.get(0)).get("partyIdTo");
            GenericValue invoice = (GenericValue) (commList.get(0)).get("invoice");
            BigDecimal appliedFraction = (BigDecimal) (commList.get(0)).get("appliedFraction");
            Long days = (Long) (commList.get(0)).get("days");
            // create the invoice record
            // To and From are in commission's sense, opposite for invoice
            Map<String, Object> createInvoiceMap = FastMap.newInstance();
            createInvoiceMap.put("partyId", partyIdBillTo);
            createInvoiceMap.put("partyIdFrom", partyIdBillFrom);
            createInvoiceMap.put("invoiceDate", now);
            // if there were days associated with the commission agreement, then set a dueDate for the invoice.
            if (days != null) {
                createInvoiceMap.put("dueDate", UtilDateTime.getDayEnd(now, days));
            }
            createInvoiceMap.put("invoiceTypeId", "COMMISSION_INVOICE");
            // start with INVOICE_IN_PROCESS, in the INVOICE_READY we can't change the invoice (or shouldn't be able to...)
            createInvoiceMap.put("statusId", "INVOICE_IN_PROCESS");
            createInvoiceMap.put("currencyUomId", invoice.getString("currencyUomId"));
            createInvoiceMap.put("userLogin", userLogin);
            // store the invoice first
            Map<String, Object> createInvoiceResult = null;
            try {
                createInvoiceResult = dispatcher.runSync("createInvoice", createInvoiceMap);
            } catch (GenericServiceException e) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingInvoiceCommissionError",locale), null, null, createInvoiceResult);
            }
            String invoiceId = (String) createInvoiceResult.get("invoiceId");
            // create the bill-from (or pay-to) contact mech as the primary PAYMENT_LOCATION of the party from the store
            List<EntityExpr> partyContactMechPurposeConds = UtilMisc.toList(
                    EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyIdBillTo),
                    EntityCondition.makeCondition("contactMechPurposeTypeId", EntityOperator.EQUALS, "BILLING_LOCATION"));
            List<GenericValue> partyContactMechPurposes = new ArrayList<GenericValue>();
            try {
                partyContactMechPurposes = delegator.findList("PartyContactMechPurpose",
                        EntityCondition.makeCondition(partyContactMechPurposeConds, EntityOperator.AND), null, null, null, false);
            } catch (GenericEntityException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
            if (partyContactMechPurposes.size() > 0) {
                GenericValue address = partyContactMechPurposes.get(0);
                GenericValue invoiceContactMech = delegator.makeValue("InvoiceContactMech", UtilMisc.toMap(
                        "invoiceId", invoiceId,
                        "contactMechId", address.getString("contactMechId"),
                        "contactMechPurposeTypeId", "BILLING_LOCATION"));
                toStore.add(invoiceContactMech);
            }
            partyContactMechPurposeConds = UtilMisc.toList(
                    EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyIdBillTo),
                    EntityCondition.makeCondition("contactMechPurposeTypeId", EntityOperator.EQUALS, "PAYMENT_LOCATION"));
            try {
                partyContactMechPurposes = delegator.findList("PartyContactMechPurpose",
                        EntityCondition.makeCondition(partyContactMechPurposeConds, EntityOperator.AND), null, null, null, false);
            } catch (GenericEntityException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
            if (partyContactMechPurposes.size() > 0) {
                GenericValue address = partyContactMechPurposes.get(0);
                GenericValue invoiceContactMech = delegator.makeValue("InvoiceContactMech", UtilMisc.toMap(
                        "invoiceId", invoiceId,
                        "contactMechId", address.getString("contactMechId"),
                        "contactMechPurposeTypeId", "PAYMENT_LOCATION"));
                toStore.add(invoiceContactMech);
            }
            // create the item records
            for (Map<String, Object> commissionMap : commList) {
                BigDecimal elemAmount = ((BigDecimal)commissionMap.get("commission")).multiply(appliedFraction);
                BigDecimal quantity = (BigDecimal)commissionMap.get("quantity");
                String invoiceIdFrom = (String)commissionMap.get("invoiceId");
                String invoiceItemSeqIdFrom = (String)commissionMap.get("invoiceItemSeqId");
                elemAmount = elemAmount.setScale(DECIMALS, ROUNDING);
                Map<String, Object> resMap = null;
                Map<String, Object> invoiceItemAssocResultMap = null;
                try {
                    resMap = dispatcher.runSync("createInvoiceItem", UtilMisc.toMap(
                            "invoiceId", invoiceId,
                            "productId", commissionMap.get("productId"),
                            "invoiceItemTypeId", "COMM_INV_ITEM",
                            "quantity",quantity,
                            "amount", elemAmount,
                            "userLogin", userLogin));
                    invoiceItemAssocResultMap = dispatcher.runSync("createInvoiceItemAssoc", UtilMisc.toMap(
                            "invoiceIdFrom", invoiceIdFrom,
                            "invoiceItemSeqIdFrom", invoiceItemSeqIdFrom,
                            "invoiceIdTo", invoiceId,
                            "invoiceItemSeqIdTo", resMap.get("invoiceItemSeqId"),
                            "invoiceItemAssocTypeId", "COMMISSION_INVOICE",
                            "partyIdFrom", partyIdBillFrom,
                            "partyIdTo", partyIdBillTo,
                            "quantity", quantity,
                            "amount", elemAmount,
                            "userLogin", userLogin));
                } catch (GenericServiceException e) {
                    return ServiceUtil.returnError(e.getMessage());
                }
                if (ServiceUtil.isError(resMap)) {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingInvoiceCommissionErrorItem",locale), null, null, resMap);
                }
            }
            // store value objects
            try {
                delegator.storeAll(toStore);
            } catch (GenericEntityException e) {
                String errMsg = UtilProperties.getMessage(resource,"AccountingInvoiceCommissionEntityDataProblem",UtilMisc.toMap("reason",e.toString()),locale);
                Debug.logError(e, errMsg, module);
                return ServiceUtil.returnError(errMsg);
            }
            invoicesCreated.add(UtilMisc.toMap("commissionInvoiceId",invoiceId, "salesRepresentative ",partyIdBillFrom));
        }
        Map<String, Object> result = ServiceUtil.returnSuccess("Created Commission invoices for each commission receiving parties " + invoicesCreated);
        Debug.logInfo("Created Commission invoices for each commission receiving parties " + invoicesCreated, module);
        result.put("invoicesCreated", invoicesCreated);
        return result;
    }

    public static Map<String, Object> readyInvoices(DispatchContext dctx, Map<String, Object> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");
        // Get invoices to make ready
        List<String> invoicesCreated = UtilGenerics.checkList(context.get("invoicesCreated"));
        String nextStatusId = "INVOICE_READY";
        try {
            for (String invoiceId : invoicesCreated) {
                Map<String, Object> setInvoiceStatusResult = dispatcher.runSync("setInvoiceStatus", UtilMisc.<String, Object>toMap("invoiceId", invoiceId, "statusId", nextStatusId, "userLogin", userLogin));
                if (ServiceUtil.isError(setInvoiceStatusResult)) {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingInvoiceCommissionError",locale), null, null, setInvoiceStatusResult);
                }
            }
        } catch (GenericServiceException e) {
            String errMsg = UtilProperties.getMessage(resource,"AccountingInvoiceCommissionEntityDataProblem",UtilMisc.toMap("reason",e.toString()),locale);
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        }
        return ServiceUtil.returnSuccess();
    }

    public static Map<String, Object> createInvoicesFromShipment(DispatchContext dctx, Map<String, Object> context) {
        //Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        String shipmentId = (String) context.get("shipmentId");
        Locale locale = (Locale) context.get("locale");
        List<String> invoicesCreated = FastList.newInstance();

        Map<String, Object> serviceContext = UtilMisc.toMap("shipmentIds", UtilMisc.toList(shipmentId), "eventDate", context.get("eventDate"), "userLogin", context.get("userLogin"));
        try {
            Map<String, Object> result = dispatcher.runSync("createInvoicesFromShipments", serviceContext);
            invoicesCreated = UtilGenerics.checkList(result.get("invoicesCreated"));
        } catch (GenericServiceException e) {
            Debug.logError(e, "Trouble calling createInvoicesFromShipment service; invoice not created for shipment [" + shipmentId + "]", module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingTroubleCallingCreateInvoicesFromShipmentService",UtilMisc.toMap("shipmentId",shipmentId),locale));
        }
        Map<String, Object> response = ServiceUtil.returnSuccess();
        response.put("invoicesCreated", invoicesCreated);
        return response;
    }

    public static Map<String, Object> setInvoicesToReadyFromShipment(DispatchContext dctx, Map<String, Object> context) {
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        String shipmentId = (String) context.get("shipmentId");
        Locale locale = (Locale) context.get("locale");
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        // 1. Find all the orders for this shipment
        // 2. For every order check the invoice
        // 2.a If the invoice is in In-Process status, then move its status to ready and capture the payment.
        // 2.b If the invoice is in status other then IN-Process, skip this. These would be already paid and captured.

        GenericValue shipment = null;
        try {
            shipment = delegator.findByPrimaryKey("Shipment", UtilMisc.toMap("shipmentId", shipmentId));
        } catch (GenericEntityException e) {
            String errMsg = UtilProperties.getMessage(resource, "AccountingTroubleGettingShipmentEntity", UtilMisc.toMap("shipmentId",shipmentId), locale);
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        }
        List<GenericValue> itemIssuances = FastList.newInstance();
        try {
            EntityFindOptions findOptions = new EntityFindOptions();
            findOptions.setDistinct(true);
            Set<String> fieldsToSelect = UtilMisc.toSet("orderId", "shipmentId");
            itemIssuances = delegator.findList("ItemIssuance", EntityCondition.makeCondition("shipmentId", shipmentId), fieldsToSelect, UtilMisc.toList("orderId"), findOptions, false);
        } catch (GenericEntityException e) {
            String errMsg = UtilProperties.getMessage(resource, "AccountingProblemGettingItemsFromShipments", locale);
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        }
        if (itemIssuances.size() == 0) {
            Debug.logInfo("No items issued for shipments", module);
            return ServiceUtil.returnSuccess();
        }
        // The orders can now be placed in separate groups, each for
        // 1. The group of orders for which payment is already captured. No grouping and action required.
        // 2. The group of orders for which invoice is IN-Process status.
        Map<String, Object> ordersWithInProcessInvoice = FastMap.newInstance();

        for (GenericValue itemIssuance : itemIssuances) {
            String orderId = itemIssuance.getString("orderId");
            Map<String, Object> billFields = FastMap.newInstance();
            billFields.put("orderId", orderId);

            List<GenericValue> orderItemBillings = FastList.newInstance();
            try {
                orderItemBillings = delegator.findByAnd("OrderItemBilling", billFields);
            } catch (GenericEntityException e) {
                String errMsg = UtilProperties.getMessage(resource, "AccountingProblemLookingUpOrderItemBilling", UtilMisc.toMap("billFields", billFields), locale);
                Debug.logError(e, errMsg, module);
                return ServiceUtil.returnError(errMsg);
            }
            // if none found, the order does not have any invoice
            if (orderItemBillings.size() != 0) {
                // orders already have an invoice
                GenericValue orderItemBilling = EntityUtil.getFirst(orderItemBillings);
                GenericValue invoice = null;
                try {
                    invoice = orderItemBilling.getRelatedOne("Invoice");
                } catch (GenericEntityException e) {
                    Debug.logError(e, module);
                    return ServiceUtil.returnError(e.getMessage());
                }
                if (invoice != null) {
                    if ("INVOICE_IN_PROCESS".equals(invoice.getString("statusId"))) {
                        ordersWithInProcessInvoice.put(orderId, invoice);
                    }
                }
            }
        }

     // For In-Process invoice, move the status to ready and capture the payment
        Set<String> invoicesInProcess = ordersWithInProcessInvoice.keySet();
        for (String orderId : invoicesInProcess) {
            GenericValue invoice = (GenericValue) ordersWithInProcessInvoice.get(orderId);
            String invoiceId = invoice.getString("invoiceId");
            Map<String, Object> setInvoiceStatusResult = FastMap.newInstance();
            try {
                setInvoiceStatusResult = dispatcher.runSync("setInvoiceStatus", UtilMisc.<String, Object>toMap("invoiceId", invoiceId, "statusId", "INVOICE_READY", "userLogin", userLogin));
            } catch (GenericServiceException e) {
                Debug.logError(e, module);
                return ServiceUtil.returnError(e.getMessage());
            }
            if (ServiceUtil.isError(setInvoiceStatusResult)) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceFromOrder",locale), null, null, setInvoiceStatusResult);
            }
        }
        return ServiceUtil.returnSuccess();
    }

    public static Map<String, Object> createSalesInvoicesFromDropShipment(DispatchContext dctx, Map<String, Object> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        String shipmentId = (String) context.get("shipmentId");
        Locale locale = (Locale) context.get("locale");

        Map<String, Object> serviceContext = UtilMisc.toMap("shipmentIds", UtilMisc.toList(shipmentId), "createSalesInvoicesForDropShipments", Boolean.TRUE, "userLogin", context.get("userLogin"));

        Map<String, Object> serviceResult;
        try {
            serviceResult = dispatcher.runSync("createInvoicesFromShipments", serviceContext);
        } catch (GenericServiceException e) {
            String errorMessage = UtilProperties.getMessage(resource, "AccountingTroubleCallingCreateInvoicesFromShipmentService", UtilMisc.toMap("shipmentId", shipmentId), locale);
            Debug.logError(e, errorMessage, module);
            return ServiceUtil.returnError(errorMessage);
        }

        return serviceResult;
    }

    public static Map<String, Object> createInvoicesFromShipments(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        List<String> shipmentIds = UtilGenerics.checkList(context.get("shipmentIds"));
        Locale locale = (Locale) context.get("locale");
        Boolean createSalesInvoicesForDropShipments = (Boolean) context.get("createSalesInvoicesForDropShipments");
        if (UtilValidate.isEmpty(createSalesInvoicesForDropShipments)) createSalesInvoicesForDropShipments = Boolean.FALSE;

        boolean salesShipmentFound = false;
        boolean purchaseShipmentFound = false;
        boolean dropShipmentFound = false;

        List<String> invoicesCreated = FastList.newInstance();

        //DEJ20060520: not used? planned to be used? List shipmentIdList = new LinkedList();
        for (String tmpShipmentId : shipmentIds) {
            try {
                GenericValue shipment = delegator.findByPrimaryKey("Shipment", UtilMisc.toMap("shipmentId", tmpShipmentId));
                String shipmentTypeId = shipment.getString("shipmentTypeId");
                GenericValue shipmentType = delegator.findByPrimaryKey("ShipmentType", UtilMisc.toMap("shipmentTypeId", shipmentTypeId));
                if ((shipment.getString("shipmentTypeId") != null) && ((shipment.getString("shipmentTypeId").equals("PURCHASE_SHIPMENT")) || (shipmentType.getString("parentTypeId").equals("PURCHASE_SHIPMENT")))) {
                    purchaseShipmentFound = true;
                } else if ((shipment.getString("shipmentTypeId") != null) && (shipment.getString("shipmentTypeId").equals("DROP_SHIPMENT"))) {
                    dropShipmentFound = true;
                } else {
                    salesShipmentFound = true;
                }
                if (purchaseShipmentFound && salesShipmentFound && dropShipmentFound) {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingShipmentsOfDifferentTypes",UtilMisc.toMap("tmpShipmentId",tmpShipmentId,"shipmentTypeId",shipment.getString("shipmentTypeId")),locale));
                }
            } catch (GenericEntityException e) {
                String errMsg = UtilProperties.getMessage(resource, "AccountingTroubleGettingShipmentEntity",UtilMisc.toMap("tmpShipmentId",tmpShipmentId), locale);
                Debug.logError(e, errMsg, module);
                return ServiceUtil.returnError(errMsg);
            }
        }
        EntityCondition shipmentIdsCond = EntityCondition.makeCondition("shipmentId", EntityOperator.IN, shipmentIds);
        // check the status of the shipment
        // get the items of the shipment.  They can come from ItemIssuance if the shipment were from a sales order, ShipmentReceipt
        // if it were a purchase order or from the order items of the (possibly linked) orders if the shipment is a drop shipment
        List<GenericValue> items = null;
        List<GenericValue> orderItemAssocs = null;
        try {
            if (purchaseShipmentFound) {
                items = delegator.findList("ShipmentReceipt", shipmentIdsCond, null, UtilMisc.toList("shipmentId"), null, false);
                // filter out items which have been received but are not actually owned by an internal organization, so they should not be on a purchase invoice
                Iterator<GenericValue> itemsIter = items.iterator();
                while (itemsIter.hasNext()) {
                    GenericValue item = itemsIter.next();
                    GenericValue inventoryItem = item.getRelatedOne("InventoryItem");
                    GenericValue ownerPartyRole = delegator.findByPrimaryKeyCache("PartyRole", UtilMisc.toMap("partyId", inventoryItem.getString("ownerPartyId"), "roleTypeId", "INTERNAL_ORGANIZATIO"));
                    if (UtilValidate.isEmpty(ownerPartyRole)) {
                        itemsIter.remove();
                    }
                }
            } else if (dropShipmentFound) {

                List<GenericValue> shipments = delegator.findList("Shipment", shipmentIdsCond, null, null, null, false);

                // Get the list of purchase order IDs related to the shipments
                List<String> purchaseOrderIds = EntityUtil.getFieldListFromEntityList(shipments, "primaryOrderId", true);

                if (createSalesInvoicesForDropShipments) {

                    // If a sales invoice is being created for a drop shipment, we have to reference the original sales order items
                    // Get the list of the linked orderIds (original sales orders)
                    orderItemAssocs = delegator.findList("OrderItemAssoc", EntityCondition.makeCondition("toOrderId", EntityOperator.IN, purchaseOrderIds), null, null, null, false);

                    // Get only the order items which are indirectly related to the purchase order - this limits the list to the drop ship group(s)
                    items = EntityUtil.getRelated("FromOrderItem", orderItemAssocs);
                } else {

                    // If it's a purchase invoice being created, the order items for that purchase orders can be used directly
                    items = delegator.findList("OrderItem", EntityCondition.makeCondition("orderId", EntityOperator.IN, purchaseOrderIds), null, null, null, false);
                }
            } else {
                items = delegator.findList("ItemIssuance", shipmentIdsCond, null, UtilMisc.toList("shipmentId"), null, false);
            }
        } catch (GenericEntityException e) {
            String errMsg = UtilProperties.getMessage(resource, "AccountingProblemGettingItemsFromShipments", locale);
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        }
        if (items.size() == 0) {
            Debug.logInfo("No items issued for shipments", module);
            return ServiceUtil.returnSuccess();
        }

        // group items by order
        Map<String, List<GenericValue>> shippedOrderItems = FastMap.newInstance();
        for (GenericValue item : items) {
            String orderId = item.getString("orderId");
            String orderItemSeqId = item.getString("orderItemSeqId");
            List<GenericValue> itemsByOrder = shippedOrderItems.get(orderId);
            if (itemsByOrder == null) {
                itemsByOrder = FastList.newInstance();
            }

            // check and make sure we haven't already billed for this issuance or shipment receipt
            List<EntityCondition> billFields = FastList.newInstance();
            billFields.add(EntityCondition.makeCondition("orderId", orderId));
            billFields.add(EntityCondition.makeCondition("orderItemSeqId", orderItemSeqId));
            billFields.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "INVOICE_CANCELLED"));

            if (dropShipmentFound) {

                // Drop shipments have neither issuances nor receipts, so this check is meaningless
                itemsByOrder.add(item);
                shippedOrderItems.put(orderId, itemsByOrder);
                continue;
            } else if (item.getEntityName().equals("ItemIssuance")) {
                billFields.add(EntityCondition.makeCondition("itemIssuanceId", item.get("itemIssuanceId")));
            } else if (item.getEntityName().equals("ShipmentReceipt")) {
                billFields.add(EntityCondition.makeCondition("shipmentReceiptId", item.getString("receiptId")));
            }
            List<GenericValue> itemBillings = null;
            try {
                itemBillings = delegator.findList("OrderItemBillingAndInvoiceAndItem", EntityCondition.makeCondition(billFields, EntityOperator.AND), null, null, null, false);
            } catch (GenericEntityException e) {
                String errMsg = UtilProperties.getMessage(resource, "AccountingProblemLookingUpOrderItemBilling",UtilMisc.toMap("billFields",billFields), locale);
                Debug.logError(e, errMsg, module);
                return ServiceUtil.returnError(errMsg);
            }

            // if none found, then okay to bill
            if (itemBillings.size() == 0) {
                itemsByOrder.add(item);
            }

            // update the map with modified list
            shippedOrderItems.put(orderId, itemsByOrder);
        }
        // make sure we aren't billing items already invoiced i.e. items billed as digital (FINDIG)
        Set<String> orders = shippedOrderItems.keySet();
        for (String orderId : orders) {

            // we'll only use this list to figure out which ones to send
            List<GenericValue> billItems = shippedOrderItems.get(orderId);

            // a new list to be used to pass to the create invoice service
            List<GenericValue> toBillItems = FastList.newInstance();

            // map of available quantities so we only have to calc once
            Map<String, BigDecimal> itemQtyAvail = FastMap.newInstance();

            // now we will check each issuance and make sure it hasn't already been billed
            for (GenericValue issue : billItems) {
                BigDecimal issueQty = ZERO;

                if (issue.getEntityName().equals("ShipmentReceipt")) {
                    issueQty = issue.getBigDecimal("quantityAccepted");
                } else {
                    issueQty = issue.getBigDecimal("quantity");
                }

                BigDecimal billAvail = itemQtyAvail.get(issue.getString("orderItemSeqId"));
                if (billAvail == null) {
                    List<EntityCondition> lookup = FastList.newInstance();
                    lookup.add(EntityCondition.makeCondition("orderId", orderId));
                    lookup.add(EntityCondition.makeCondition("orderItemSeqId", issue.get("orderItemSeqId")));
                    lookup.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "INVOICE_CANCELLED"));
                    GenericValue orderItem = null;
                    List<GenericValue> billed = null;
                    BigDecimal orderedQty = null;
                    try {
                        orderItem = issue.getEntityName().equals("OrderItem") ? issue : issue.getRelatedOne("OrderItem");

                        // total ordered
                        orderedQty = orderItem.getBigDecimal("quantity");

                        if (dropShipmentFound && createSalesInvoicesForDropShipments.booleanValue()) {

                            // Override the issueQty with the quantity from the purchase order item
                            GenericValue orderItemAssoc = EntityUtil.getFirst(EntityUtil.filterByAnd(orderItemAssocs, UtilMisc.toMap("orderId", issue.getString("orderId"), "orderItemSeqId", issue.getString("orderItemSeqId"))));
                            GenericValue purchaseOrderItem = orderItemAssoc.getRelatedOne("ToOrderItem");
                            orderItem.set("quantity", purchaseOrderItem.getBigDecimal("quantity"));
                            issueQty = purchaseOrderItem.getBigDecimal("quantity");
                        }
                        billed = delegator.findList("OrderItemBillingAndInvoiceAndItem", EntityCondition.makeCondition(lookup, EntityOperator.AND), null, null, null, false);
                    } catch (GenericEntityException e) {
                        String errMsg = UtilProperties.getMessage(resource, "AccountingProblemGettingOrderItemOrderItemBilling",UtilMisc.toMap("lookup",lookup), locale);
                        Debug.logError(e, errMsg, module);
                        return ServiceUtil.returnError(errMsg);
                    }


                    // add up the already billed total
                    if (billed.size() > 0) {
                        BigDecimal billedQuantity = ZERO;
                        for (GenericValue oib : billed) {
                            BigDecimal qty = oib.getBigDecimal("quantity");
                            if (qty != null) {
                                billedQuantity = billedQuantity.add(qty).setScale(DECIMALS, ROUNDING);
                            }
                        }
                        BigDecimal leftToBill = orderedQty.subtract(billedQuantity).setScale(DECIMALS, ROUNDING);
                        billAvail = leftToBill;
                    } else {
                        billAvail = orderedQty;
                    }
                }

                // no available means we cannot bill anymore
                if (billAvail != null && billAvail.signum() == 1) { // this checks if billAvail is a positive non-zero number
                    if (issueQty != null && issueQty.compareTo(billAvail) > 0) {
                        // can only bill some of the issuance; others have been billed already
                        if ("ShipmentReceipt".equals(issue.getEntityName())) {
                            issue.set("quantityAccepted", billAvail);
                        } else {
                            issue.set("quantity", billAvail);
                        }
                        billAvail = ZERO;
                    } else {
                        // now have been billed
                        billAvail = billAvail.subtract(issueQty).setScale(DECIMALS, ROUNDING);
                    }

                    // okay to bill these items; but none else
                    toBillItems.add(issue);
                }

                // update the available to bill quantity for the next pass
                itemQtyAvail.put(issue.getString("orderItemSeqId"), billAvail);
            }

            OrderReadHelper orh = new OrderReadHelper(delegator, orderId);

            GenericValue productStore = orh.getProductStore();
            String prorateShipping = productStore != null ? productStore.getString("prorateShipping") : "N";
            // If shipping charges are not prorated, the shipments need to be examined for additional shipping charges
            if ("N".equalsIgnoreCase(prorateShipping)) {

                // Get the set of filtered shipments
                List<GenericValue> invoiceableShipments = null;
                try {
                    if (dropShipmentFound) {

                        List<String> invoiceablePrimaryOrderIds = null;
                        if (createSalesInvoicesForDropShipments) {

                            // If a sales invoice is being created for the drop shipment, we need to reference back to the original purchase order IDs

                            // Get the IDs for orders which have billable items
                            List<String> invoiceableLinkedOrderIds = EntityUtil.getFieldListFromEntityList(toBillItems, "orderId", true);

                            // Get back the IDs of the purchase orders - this will be a list of the purchase order items which are billable by virtue of not having been
                            //  invoiced in a previous sales invoice
                            List<GenericValue> reverseOrderItemAssocs = EntityUtil.filterByCondition(orderItemAssocs, EntityCondition.makeCondition("orderId", EntityOperator.IN, invoiceableLinkedOrderIds));
                            invoiceablePrimaryOrderIds = EntityUtil.getFieldListFromEntityList(reverseOrderItemAssocs, "toOrderId", true);

                        } else {

                            // If a purchase order is being created for a drop shipment, the purchase order IDs can be used directly
                            invoiceablePrimaryOrderIds = EntityUtil.getFieldListFromEntityList(toBillItems, "orderId", true);

                        }

                        // Get the list of shipments which are associated with the filtered purchase orders
                        if (! UtilValidate.isEmpty(invoiceablePrimaryOrderIds)) {
                            List<EntityExpr> invoiceableShipmentConds = UtilMisc.toList(
                                    EntityCondition.makeCondition("primaryOrderId", EntityOperator.IN, invoiceablePrimaryOrderIds),
                                    EntityCondition.makeCondition("shipmentId", EntityOperator.IN, shipmentIds));
                            invoiceableShipments = delegator.findList("Shipment", EntityCondition.makeCondition(invoiceableShipmentConds, EntityOperator.AND), null, null, null, false);
                        }
                    } else {
                        List<String> invoiceableShipmentIds = EntityUtil.getFieldListFromEntityList(toBillItems, "shipmentId", true);
                        if (UtilValidate.isNotEmpty(invoiceableShipmentIds)) {
                            invoiceableShipments = delegator.findList("Shipment", EntityCondition.makeCondition("shipmentId", EntityOperator.IN, invoiceableShipmentIds), null, null, null, false);
                        }
                    }
                } catch (GenericEntityException e) {
                    String errMsg = UtilProperties.getMessage(resource, "AccountingTroubleCallingCreateInvoicesFromShipmentsService", locale);
                    Debug.logError(e, errMsg, module);
                    return ServiceUtil.returnError(errMsg);
                }
                // Total the additional shipping charges for the shipments
                Map<GenericValue, BigDecimal> additionalShippingCharges = FastMap.newInstance();
                BigDecimal totalAdditionalShippingCharges = ZERO;
                if (UtilValidate.isNotEmpty(invoiceableShipments)) {
                    for (GenericValue shipment : invoiceableShipments) {
                        if (shipment.get("additionalShippingCharge") == null) continue;
                        BigDecimal shipmentAdditionalShippingCharges = shipment.getBigDecimal("additionalShippingCharge").setScale(DECIMALS, ROUNDING);
                        additionalShippingCharges.put(shipment, shipmentAdditionalShippingCharges);
                        totalAdditionalShippingCharges = totalAdditionalShippingCharges.add(shipmentAdditionalShippingCharges);
                    }
                }
                // If the additional shipping charges are greater than zero, process them
                if (totalAdditionalShippingCharges.signum() == 1) {

                    // Add an OrderAdjustment to the order for each additional shipping charge
                    for (GenericValue shipment : additionalShippingCharges.keySet()) {
                        String shipmentId = shipment.getString("shipmentId");
                        BigDecimal additionalShippingCharge = additionalShippingCharges.get(shipment);
                        Map<String, Object> createOrderAdjustmentContext = FastMap.newInstance();
                        createOrderAdjustmentContext.put("orderId", orderId);
                        createOrderAdjustmentContext.put("orderAdjustmentTypeId", "SHIPPING_CHARGES");
                        String addtlChargeDescription = shipment.getString("addtlShippingChargeDesc");
                        if (UtilValidate.isEmpty(addtlChargeDescription)) {
                            addtlChargeDescription = UtilProperties.getMessage(resource, "AccountingAdditionalShippingChargeForShipment", UtilMisc.toMap("shipmentId", shipmentId), locale);
                        }
                        createOrderAdjustmentContext.put("description", addtlChargeDescription);
                        createOrderAdjustmentContext.put("sourceReferenceId", shipmentId);
                        createOrderAdjustmentContext.put("amount", additionalShippingCharge);
                        createOrderAdjustmentContext.put("userLogin", context.get("userLogin"));
                        String shippingOrderAdjustmentId = null;
                        try {
                            Map<String, Object> createOrderAdjustmentResult = dispatcher.runSync("createOrderAdjustment", createOrderAdjustmentContext);
                            shippingOrderAdjustmentId = (String) createOrderAdjustmentResult.get("orderAdjustmentId");
                        } catch (GenericServiceException e) {
                            String errMsg = UtilProperties.getMessage(resource, "AccountingTroubleCallingCreateOrderAdjustmentService", locale);
                            Debug.logError(e, errMsg, module);
                            return ServiceUtil.returnError(errMsg);
                        }
                        // Obtain a list of OrderAdjustments due to tax on the shipping charges, if any
                        GenericValue billToParty = orh.getBillToParty();
                        GenericValue payToParty = orh.getBillFromParty();
                        GenericValue destinationContactMech = null;
                        try {
                            destinationContactMech = shipment.getRelatedOne("DestinationPostalAddress");
                        } catch (GenericEntityException e) {
                            String errMsg = UtilProperties.getMessage(resource, "AccountingTroubleCallingCreateInvoicesFromShipmentService", locale);
                            Debug.logError(e, errMsg, module);
                            return ServiceUtil.returnError(errMsg);
                        }

                        List<Object> emptyList = FastList.newInstance();
                        Map<String, Object> calcTaxContext = FastMap.newInstance();
                        calcTaxContext.put("productStoreId", orh.getProductStoreId());
                        calcTaxContext.put("payToPartyId", payToParty.getString("partyId"));
                        calcTaxContext.put("billToPartyId", billToParty.getString("partyId"));
                        calcTaxContext.put("orderShippingAmount", totalAdditionalShippingCharges);
                        calcTaxContext.put("shippingAddress", destinationContactMech);

                        // These parameters don't matter if we're only worried about adjustments on the shipping charges
                        calcTaxContext.put("itemProductList", emptyList);
                        calcTaxContext.put("itemAmountList", emptyList);
                        calcTaxContext.put("itemPriceList", emptyList);
                        calcTaxContext.put("itemShippingList", emptyList);

                        Map<String, Object> calcTaxResult = null;
                        try {
                            calcTaxResult = dispatcher.runSync("calcTax", calcTaxContext);
                        } catch (GenericServiceException e) {
                            String errMsg = UtilProperties.getMessage(resource, "AccountingTroubleCallingCalcTaxService", locale);
                            Debug.logError(e, errMsg, module);
                            return ServiceUtil.returnError(errMsg);
                        }
                        List<GenericValue> orderAdjustments = UtilGenerics.checkList(calcTaxResult.get("orderAdjustments"));
                        // If we have any OrderAdjustments due to tax on shipping, store them and add them to the total
                        if (orderAdjustments != null) {
                            for (GenericValue orderAdjustment : orderAdjustments) {
                                totalAdditionalShippingCharges = totalAdditionalShippingCharges.add(orderAdjustment.getBigDecimal("amount").setScale(DECIMALS, ROUNDING));
                                orderAdjustment.set("orderAdjustmentId", delegator.getNextSeqId("OrderAdjustment"));
                                orderAdjustment.set("orderId", orderId);
                                orderAdjustment.set("orderItemSeqId", "_NA_");
                                orderAdjustment.set("shipGroupSeqId", shipment.getString("primaryShipGroupSeqId"));
                                orderAdjustment.set("originalAdjustmentId", shippingOrderAdjustmentId);
                            }
                            try {
                                delegator.storeAll(orderAdjustments);
                            } catch (GenericEntityException e) {
                                String errMsg = UtilProperties.getMessage(resource, "AccountingProblemStoringOrderAdjustments", UtilMisc.toMap("orderAdjustments", orderAdjustments), locale);
                                Debug.logError(e, errMsg, module);
                                return ServiceUtil.returnError(errMsg);
                            }
                        }
                        // If part of the order was paid via credit card, try to charge it for the additional shipping
                        List<GenericValue> orderPaymentPreferences = null;
                        try {
                            orderPaymentPreferences = delegator.findByAnd("OrderPaymentPreference", UtilMisc.toMap("orderId", orderId, "paymentMethodTypeId", "CREDIT_CARD"));
                        } catch (GenericEntityException e) {
                            String errMsg = UtilProperties.getMessage(resource, "AccountingProblemGettingOrderPaymentPreferences", locale);
                            Debug.logError(e, errMsg, module);
                            return ServiceUtil.returnError(errMsg);
                        }

                        //  Use the first credit card we find, for the sake of simplicity
                        String paymentMethodId = null;
                        GenericValue cardOrderPaymentPref = EntityUtil.getFirst(orderPaymentPreferences);
                        if (cardOrderPaymentPref != null) {
                            paymentMethodId = cardOrderPaymentPref.getString("paymentMethodId");
                        }
                        if (paymentMethodId != null) {

                            // Release all outstanding (not settled or cancelled) authorizations, while keeping a running
                            //  total of their amounts so that the total plus the additional shipping charges can be authorized again
                            //  all at once.
                            BigDecimal totalNewAuthAmount = totalAdditionalShippingCharges.setScale(DECIMALS, ROUNDING);
                            for (GenericValue orderPaymentPreference : orderPaymentPreferences) {
                                if (! (orderPaymentPreference.getString("statusId").equals("PAYMENT_SETTLED") || orderPaymentPreference.getString("statusId").equals("PAYMENT_CANCELLED"))) {
                                    GenericValue authTransaction = PaymentGatewayServices.getAuthTransaction(orderPaymentPreference);
                                    if (authTransaction != null && authTransaction.get("amount") != null) {

                                        // Update the total authorized amount
                                        totalNewAuthAmount = totalNewAuthAmount.add(authTransaction.getBigDecimal("amount").setScale(DECIMALS, ROUNDING));

                                        // Release the authorization for the OrderPaymentPreference
                                        Map<String, Object> prefReleaseResult = null;
                                        try {
                                            prefReleaseResult = dispatcher.runSync("releaseOrderPaymentPreference", UtilMisc.toMap("orderPaymentPreferenceId", orderPaymentPreference.getString("orderPaymentPreferenceId"), "userLogin", context.get("userLogin")));
                                        } catch (GenericServiceException e) {
                                            String errMsg = UtilProperties.getMessage(resource, "AccountingTroubleCallingReleaseOrderPaymentPreferenceService", locale);
                                            Debug.logError(e, errMsg, module);
                                            return ServiceUtil.returnError(errMsg);
                                        }
                                        if (ServiceUtil.isError(prefReleaseResult) || ServiceUtil.isFailure(prefReleaseResult)) {
                                            String errMsg = ServiceUtil.getErrorMessage(prefReleaseResult);
                                            Debug.logError(errMsg, module);
                                            return ServiceUtil.returnError(errMsg);
                                        }
                                    }
                                }
                            }

                            // Create a new OrderPaymentPreference for the order to handle the new (totalled) charge. Don't
                            //  set the maxAmount so that it doesn't interfere with other authorizations
                            Map<String, Object> serviceContext = UtilMisc.toMap("orderId", orderId, "paymentMethodId", paymentMethodId, "paymentMethodTypeId", "CREDIT_CARD", "userLogin", context.get("userLogin"));
                            String orderPaymentPreferenceId = null;
                            try {
                                Map<String, Object> result = dispatcher.runSync("createOrderPaymentPreference", serviceContext);
                                orderPaymentPreferenceId = (String) result.get("orderPaymentPreferenceId");
                            } catch (GenericServiceException e) {
                                String errMsg = UtilProperties.getMessage(resource, "AccountingTroubleCallingCreateOrderPaymentPreferenceService", locale);
                                Debug.logError(e, errMsg, module);
                                return ServiceUtil.returnError(errMsg);
                            }

                            // Attempt to authorize the new orderPaymentPreference
                            Map<String, Object> authResult = null;
                            try {
                                // Use an overrideAmount because the maxAmount wasn't set on the OrderPaymentPreference
                                authResult = dispatcher.runSync("authOrderPaymentPreference", UtilMisc.toMap("orderPaymentPreferenceId", orderPaymentPreferenceId, "overrideAmount", totalNewAuthAmount, "userLogin", context.get("userLogin")));
                            } catch (GenericServiceException e) {
                                String errMsg = UtilProperties.getMessage(resource, "AccountingTroubleCallingAuthOrderPaymentPreferenceService", locale);
                                Debug.logError(e, errMsg, module);
                                return ServiceUtil.returnError(errMsg);
                            }

                            // If the authorization fails, create the invoice anyway, but make a note of it
                            boolean authFinished = ((Boolean) authResult.get("finished")).booleanValue();
                            boolean authErrors = ((Boolean) authResult.get("errors")).booleanValue();
                            if (authErrors || ! authFinished) {
                                String errMsg = UtilProperties.getMessage(resource, "AccountingUnableToAuthAdditionalShipCharges", UtilMisc.toMap("shipmentId", shipmentId, "paymentMethodId", paymentMethodId, "orderPaymentPreferenceId", orderPaymentPreferenceId), locale);
                                Debug.logError(errMsg, module);
                            }

                        }
                    }
                }
            } else {
                Debug.logInfo(UtilProperties.getMessage(resource, "AccountingIgnoringAdditionalShipCharges", UtilMisc.toMap("productStoreId", orh.getProductStoreId()), locale), module);
            }

            String invoiceId = null;
            List<GenericValue> shipmentItemBillings = null;
            String shipmentId = shipmentIds.get(0);
            try {
                shipmentItemBillings = delegator.findByAnd("ShipmentItemBilling", UtilMisc.toMap("shipmentId", shipmentId));
            } catch (GenericEntityException e) {
                String errMsg = UtilProperties.getMessage(resource, "AccountingProblemGettingShipmentItemBilling", locale);
                return ServiceUtil.returnError(errMsg);
            }
            if (UtilValidate.isNotEmpty(shipmentItemBillings)) {
                GenericValue shipmentItemBilling = EntityUtil.getFirst(shipmentItemBillings);
                invoiceId = shipmentItemBilling.getString("invoiceId");
            }
            
            // call the createInvoiceForOrder service for each order
            Map<String, Object> serviceContext = UtilMisc.toMap("orderId", orderId, "billItems", toBillItems, "invoiceId", invoiceId, "eventDate", context.get("eventDate"), "userLogin", context.get("userLogin"));
            try {
                Map<String, Object> result = dispatcher.runSync("createInvoiceForOrder", serviceContext);
                invoicesCreated.add((String) result.get("invoiceId"));
            } catch (GenericServiceException e) {
                String errMsg = UtilProperties.getMessage(resource, "AccountingTroubleCallingCreateInvoiceForOrderService", locale);
                Debug.logError(e, errMsg, module);
                return ServiceUtil.returnError(errMsg);
            }
        }

        Map<String, Object> response = ServiceUtil.returnSuccess();
        response.put("invoicesCreated", invoicesCreated);
        return response;
    }

    private static String getInvoiceItemType(Delegator delegator, String key1, String key2, String invoiceTypeId, String defaultValue) {
        GenericValue itemMap = null;
        try {
            if (UtilValidate.isNotEmpty(key1)) {
                itemMap = delegator.findByPrimaryKeyCache("InvoiceItemTypeMap", UtilMisc.toMap("invoiceItemMapKey", key1, "invoiceTypeId", invoiceTypeId));
            }
            if (itemMap == null && UtilValidate.isNotEmpty(key2)) {
                itemMap = delegator.findByPrimaryKeyCache("InvoiceItemTypeMap", UtilMisc.toMap("invoiceItemMapKey", key2, "invoiceTypeId", invoiceTypeId));
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, "Trouble getting InvoiceItemTypeMap entity record", module);
            return defaultValue;
        }
        if (itemMap != null) {
            return itemMap.getString("invoiceItemTypeId");
        } else {
            return defaultValue;
        }
    }

    public static Map<String, Object> createInvoicesFromReturnShipment(DispatchContext dctx, Map<String, Object> context) {
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Locale locale = (Locale) context.get("locale");

        String shipmentId = (String) context.get("shipmentId");
        String errorMsg = UtilProperties.getMessage(resource, "AccountingErrorCreatingInvoiceForShipment",UtilMisc.toMap("shipmentId",shipmentId), locale);
        boolean salesReturnFound = false;
        boolean purchaseReturnFound = false;

        List<String> invoicesCreated = FastList.newInstance();
        try {

            // get the shipment and validate that it is a sales return
            GenericValue shipment = delegator.findByPrimaryKey("Shipment", UtilMisc.toMap("shipmentId", shipmentId));
            if (shipment == null) {
                return ServiceUtil.returnError(errorMsg + UtilProperties.getMessage(resource, "AccountingShipmentNotFound",locale));
            }
            if (shipment.getString("shipmentTypeId").equals("SALES_RETURN")) {
                salesReturnFound = true;
            } else if ("PURCHASE_RETURN".equals(shipment.getString("shipmentTypeId"))) {
                purchaseReturnFound = true;
            }
            if (!(salesReturnFound || purchaseReturnFound)) {
                 return ServiceUtil.returnError(errorMsg + UtilProperties.getMessage(resource, "AccountingShipmentNotSalesReturnAndPurchaseReturn",locale));
            }
            // get the items of the shipment. They can come from ItemIssuance if the shipment were from a purchase return, ShipmentReceipt if it were from a sales return
            List<GenericValue> shippedItems = null;
            if (salesReturnFound) {
                shippedItems = shipment.getRelated("ShipmentReceipt");
            } else if (purchaseReturnFound) {
                shippedItems = shipment.getRelated("ItemIssuance");
            }
            if (shippedItems == null) {
                Debug.logInfo("No items issued for shipments", module);
                return ServiceUtil.returnSuccess();
            }

            // group the shipments by returnId (because we want a seperate itemized invoice for each return)
            Map<String, List<GenericValue>> itemsShippedGroupedByReturn = FastMap.newInstance();

            for (GenericValue item : shippedItems) {
                String returnId = null;
                String returnItemSeqId = null;
                if (item.getEntityName().equals("ShipmentReceipt")) {
                    returnId = item.getString("returnId");
                } else if (item.getEntityName().equals("ItemIssuance")) {
                    GenericValue returnItemShipment = EntityUtil.getFirst(delegator.findByAnd("ReturnItemShipment", UtilMisc.toMap("shipmentId", item.getString("shipmentId"), "shipmentItemSeqId", item.getString("shipmentItemSeqId"))));
                    returnId = returnItemShipment.getString("returnId");
                    returnItemSeqId = returnItemShipment.getString("returnItemSeqId");
                }

                // see if there are ReturnItemBillings for this item
                List<GenericValue> billings = null;
                if (item.getEntityName().equals("ShipmentReceipt")) {
                    billings = delegator.findByAnd("ReturnItemBilling", UtilMisc.toMap("shipmentReceiptId", item.getString("receiptId"), "returnId", returnId,
                                "returnItemSeqId", item.get("returnItemSeqId")));
                } else if (item.getEntityName().equals("ItemIssuance")) {
                    billings = delegator.findByAnd("ReturnItemBilling", UtilMisc.toMap("returnId", returnId, "returnItemSeqId", returnItemSeqId));
                }
                // if there are billings, we have already billed the item, so skip it
                if (UtilValidate.isNotEmpty(billings)) continue;

                // get the List of items shipped to/from this returnId
                List<GenericValue> billItems = itemsShippedGroupedByReturn.get(returnId);
                if (billItems == null) {
                    billItems = FastList.newInstance();
                }

                // add our item to the group and put it back in the map
                billItems.add(item);
                itemsShippedGroupedByReturn.put(returnId, billItems);
            }

            // loop through the returnId keys in the map and invoke the createInvoiceFromReturn service for each
            for (String returnId : itemsShippedGroupedByReturn.keySet()) {
                List<GenericValue> billItems = itemsShippedGroupedByReturn.get(returnId);
                if (Debug.verboseOn()) {
                    Debug.logVerbose("Creating invoice for return [" + returnId + "] with items: " + billItems.toString(), module);
                }
                Map<String, Object> input = UtilMisc.toMap("returnId", returnId, "billItems", billItems, "userLogin", context.get("userLogin"));
                Map<String, Object> serviceResults = dispatcher.runSync("createInvoiceFromReturn", input);
                if (ServiceUtil.isError(serviceResults)) {
                    return ServiceUtil.returnError(errorMsg, null, null, serviceResults);
                }

                // put the resulting invoiceId in the return list
                invoicesCreated.add((String) serviceResults.get("invoiceId"));
            }
        } catch (GenericServiceException e) {
            Debug.logError(e, errorMsg + e.getMessage(), module);
            return ServiceUtil.returnError(errorMsg + e.getMessage());
        } catch (GenericEntityException e) {
            Debug.logError(e, errorMsg + e.getMessage(), module);
            return ServiceUtil.returnError(errorMsg + e.getMessage());
        }

        Map<String, Object> result = ServiceUtil.returnSuccess();
        result.put("invoicesCreated", invoicesCreated);
        return result;
    }

    public static Map<String, Object> createInvoiceFromReturn(DispatchContext dctx, Map<String, Object> context) {
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");

        String returnId= (String) context.get("returnId");
        List<GenericValue> billItems = UtilGenerics.checkList(context.get("billItems"));
        String errorMsg = UtilProperties.getMessage(resource, "AccountingErrorCreatingInvoiceForReturn",UtilMisc.toMap("returnId",returnId),locale);
        // List invoicesCreated = new ArrayList();
        try {
            String invoiceTypeId;
            String description;
            // get the return header
            GenericValue returnHeader = delegator.findByPrimaryKey("ReturnHeader", UtilMisc.toMap("returnId", returnId));
            if ("CUSTOMER_RETURN".equals(returnHeader.getString("returnHeaderTypeId"))) {
                invoiceTypeId = "CUST_RTN_INVOICE";
                description = "Return Invoice for Customer Return #" + returnId;
            } else {
                invoiceTypeId = "PURC_RTN_INVOICE";
                description = "Return Invoice for Vendor Return #" + returnId;
            }
            // set the invoice data
            Map<String, Object> input = UtilMisc.<String, Object>toMap("invoiceTypeId", invoiceTypeId, "statusId", "INVOICE_IN_PROCESS");
            input.put("partyId", returnHeader.get("toPartyId"));
            input.put("partyIdFrom", returnHeader.get("fromPartyId"));
            input.put("currencyUomId", returnHeader.get("currencyUomId"));
            input.put("invoiceDate", UtilDateTime.nowTimestamp());
            input.put("description", description);
            input.put("billingAccountId", returnHeader.get("billingAccountId"));
            input.put("userLogin", userLogin);

            // call the service to create the invoice
            Map<String, Object> serviceResults = dispatcher.runSync("createInvoice", input);
            if (ServiceUtil.isError(serviceResults)) {
                return ServiceUtil.returnError(errorMsg, null, null, serviceResults);
            }
            String invoiceId = (String) serviceResults.get("invoiceId");

            // keep track of the invoice total vs the promised return total (how much the customer promised to return)
            BigDecimal invoiceTotal = ZERO;
            BigDecimal promisedTotal = ZERO;

            // loop through shipment receipts to create invoice items and return item billings for each item and adjustment
            int invoiceItemSeqNum = 1;
            String invoiceItemSeqId = UtilFormatOut.formatPaddedNumber(invoiceItemSeqNum, INVOICE_ITEM_SEQUENCE_ID_DIGITS);

            for (GenericValue item : billItems) {
                boolean shipmentReceiptFound = false;
                boolean itemIssuanceFound = false;
                if ("ShipmentReceipt".equals(item.getEntityName())) {
                    shipmentReceiptFound = true;
                } else if ("ItemIssuance".equals(item.getEntityName())) {
                    itemIssuanceFound = true;
                } else {
                    Debug.logError("Unexpected entity " + item + " of type " + item.getEntityName(), module);
                }
                // we need the related return item and product
                GenericValue returnItem = null;
                if (shipmentReceiptFound) {
                    returnItem = item.getRelatedOneCache("ReturnItem");
                } else if (itemIssuanceFound) {
                    GenericValue shipmentItem = item.getRelatedOneCache("ShipmentItem");
                    GenericValue returnItemShipment = EntityUtil.getFirst(shipmentItem.getRelated("ReturnItemShipment"));
                    returnItem = returnItemShipment.getRelatedOneCache("ReturnItem");
                }
                if (returnItem == null) continue; // Just to prevent NPE
                GenericValue product = returnItem.getRelatedOneCache("Product");

                // extract the return price as a big decimal for convenience
                BigDecimal returnPrice = returnItem.getBigDecimal("returnPrice");

                // determine invoice item type from the return item type
                String invoiceItemTypeId = getInvoiceItemType(delegator, returnItem.getString("returnItemTypeId"), null, invoiceTypeId, null);
                if (invoiceItemTypeId == null) {
                    return ServiceUtil.returnError(errorMsg + UtilProperties.getMessage(resource, "AccountingNoKnownInvoiceItemTypeReturnItemType",UtilMisc.toMap("returnItemTypeId",returnItem.getString("returnItemTypeId")),locale));
                }
                BigDecimal quantity = BigDecimal.ZERO;
                if (shipmentReceiptFound) {
                    quantity = item.getBigDecimal("quantityAccepted");
                } else if (itemIssuanceFound) {
                    quantity = item.getBigDecimal("quantity");
                }

                // create the invoice item for this shipment receipt
                input = UtilMisc.toMap("invoiceId", invoiceId, "invoiceItemTypeId", invoiceItemTypeId, "quantity", quantity);
                input.put("invoiceItemSeqId", "" + invoiceItemSeqId); // turn the int into a string with ("" + int) hack
                input.put("amount", returnItem.get("returnPrice"));
                input.put("productId", returnItem.get("productId"));
                input.put("taxableFlag", product.get("taxable"));
                input.put("description", returnItem.get("description"));
                // TODO: what about the productFeatureId?
                input.put("userLogin", userLogin);
                serviceResults = dispatcher.runSync("createInvoiceItem", input);
                if (ServiceUtil.isError(serviceResults)) {
                    return ServiceUtil.returnError(errorMsg, null, null, serviceResults);
                }

                // copy the return item information into ReturnItemBilling
                input = UtilMisc.toMap("returnId", returnId, "returnItemSeqId", returnItem.get("returnItemSeqId"),
                        "invoiceId", invoiceId);
                input.put("invoiceItemSeqId", "" + invoiceItemSeqId); // turn the int into a string with ("" + int) hack
                input.put("quantity", quantity);
                input.put("amount", returnItem.get("returnPrice"));
                input.put("userLogin", userLogin);
                if (shipmentReceiptFound) {
                    input.put("shipmentReceiptId", item.get("receiptId"));
                }
                serviceResults = dispatcher.runSync("createReturnItemBilling", input);
                if (ServiceUtil.isError(serviceResults)) {
                    return ServiceUtil.returnError(errorMsg, null, null, serviceResults);
                }
                if (Debug.verboseOn()) {
                    Debug.logVerbose("Creating Invoice Item with amount " + returnPrice + " and quantity " + quantity
                            + " for shipment [" + item.getString("shipmentId") + ":" + item.getString("shipmentItemSeqId") + "]", module);
                }

                String parentInvoiceItemSeqId = invoiceItemSeqId;
                // increment the seqId counter after creating the invoice item and return item billing
                invoiceItemSeqNum += 1;
                invoiceItemSeqId = UtilFormatOut.formatPaddedNumber(invoiceItemSeqNum, INVOICE_ITEM_SEQUENCE_ID_DIGITS);

                // keep a running total (note: a returnItem may have many receipts. hence, the promised total quantity is the receipt quantityAccepted + quantityRejected)
                BigDecimal cancelQuantity = ZERO;
                if (shipmentReceiptFound) {
                    cancelQuantity = item.getBigDecimal("quantityRejected");
                } else if (itemIssuanceFound) {
                    cancelQuantity = item.getBigDecimal("cancelQuantity");
                }
                if (cancelQuantity == null) cancelQuantity = ZERO;
                BigDecimal actualAmount = returnPrice.multiply(quantity).setScale(DECIMALS, ROUNDING);
                BigDecimal promisedAmount = returnPrice.multiply(quantity.add(cancelQuantity)).setScale(DECIMALS, ROUNDING);
                invoiceTotal = invoiceTotal.add(actualAmount).setScale(DECIMALS, ROUNDING);
                promisedTotal = promisedTotal.add(promisedAmount).setScale(DECIMALS, ROUNDING);

                // for each adjustment related to this ReturnItem, create a separate invoice item
                List<GenericValue> adjustments = returnItem.getRelatedCache("ReturnAdjustment");
                for (GenericValue adjustment : adjustments) {

                    if (adjustment.get("amount") == null) {
                         Debug.logWarning("Return adjustment [" + adjustment.get("returnAdjustmentId") + "] has null amount and will be skipped", module);
                         continue;
                    }

                    // determine invoice item type from the return item type
                    invoiceItemTypeId = getInvoiceItemType(delegator, adjustment.getString("returnAdjustmentTypeId"), null, invoiceTypeId, null);
                    if (invoiceItemTypeId == null) {
                        return ServiceUtil.returnError(errorMsg + "No known invoice item type for the return adjustment type ["
                                +  adjustment.getString("returnAdjustmentTypeId") + "]");
                    }

                    // prorate the adjustment amount by the returned amount; do not round ratio
                    BigDecimal ratio = quantity.divide(returnItem.getBigDecimal("returnQuantity"), 100, ROUNDING);
                    BigDecimal amount = adjustment.getBigDecimal("amount");
                    amount = amount.multiply(ratio).setScale(DECIMALS, ROUNDING);
                    if (Debug.verboseOn()) {
                        Debug.logVerbose("Creating Invoice Item with amount " + adjustment.getBigDecimal("amount") + " prorated to " + amount
                                + " for return adjustment [" + adjustment.getString("returnAdjustmentId") + "]", module);
                    }

                    // prepare invoice item data for this adjustment
                    input = UtilMisc.toMap("invoiceId", invoiceId, "invoiceItemTypeId", invoiceItemTypeId, "quantity", BigDecimal.ONE);
                    input.put("amount", amount);
                    input.put("invoiceItemSeqId", "" + invoiceItemSeqId); // turn the int into a string with ("" + int) hack
                    input.put("productId", returnItem.get("productId"));
                    input.put("description", adjustment.get("description"));
                    input.put("overrideGlAccountId", adjustment.get("overrideGlAccountId"));
                    input.put("parentInvoiceId", invoiceId);
                    input.put("parentInvoiceItemSeqId", parentInvoiceItemSeqId);
                    input.put("taxAuthPartyId", adjustment.get("taxAuthPartyId"));
                    input.put("taxAuthGeoId", adjustment.get("taxAuthGeoId"));
                    input.put("userLogin", userLogin);

                    // only set taxable flag when the adjustment is not a tax
                    // TODO: Note that we use the value of Product.taxable here. This is not an ideal solution. Instead, use returnAdjustment.includeInTax
                    if (adjustment.get("returnAdjustmentTypeId").equals("RET_SALES_TAX_ADJ")) {
                        input.put("taxableFlag", "N");
                    }

                    // create the invoice item
                    serviceResults = dispatcher.runSync("createInvoiceItem", input);
                    if (ServiceUtil.isError(serviceResults)) {
                        return ServiceUtil.returnError(errorMsg, null, null, serviceResults);
                    }

                    // increment the seqId counter
                    invoiceItemSeqNum += 1;
                    invoiceItemSeqId = UtilFormatOut.formatPaddedNumber(invoiceItemSeqNum, INVOICE_ITEM_SEQUENCE_ID_DIGITS);

                    // keep a running total (promised adjustment in this case is the same as the invoice adjustment)
                    invoiceTotal = invoiceTotal.add(amount).setScale(DECIMALS, ROUNDING);
                    promisedTotal = promisedTotal.add(amount).setScale(DECIMALS, ROUNDING);
                }
            }

            // ratio of the invoice total to the promised total so far or zero if the amounts were zero
            BigDecimal actualToPromisedRatio = ZERO;
            if (invoiceTotal.signum() != 0) {
                actualToPromisedRatio = invoiceTotal.divide(promisedTotal, 100, ROUNDING);  // do not round ratio
            }

            // loop through return-wide adjustments and create invoice items for each
            List<GenericValue> adjustments = returnHeader.getRelatedByAndCache("ReturnAdjustment", UtilMisc.toMap("returnItemSeqId", "_NA_"));
            for (GenericValue adjustment : adjustments) {

                // determine invoice item type from the return item type
                String invoiceItemTypeId = getInvoiceItemType(delegator, adjustment.getString("returnAdjustmentTypeId"), null, invoiceTypeId, null);
                if (invoiceItemTypeId == null) {
                    return ServiceUtil.returnError(errorMsg + UtilProperties.getMessage(resource, "AccountingNoKnownInvoiceItemTypeReturnAdjustmentType",
                            UtilMisc.toMap("returnAdjustmentTypeId",adjustment.getString("returnAdjustmentTypeId")),locale));
                }

                // prorate the adjustment amount by the actual to promised ratio
                BigDecimal amount = adjustment.getBigDecimal("amount").multiply(actualToPromisedRatio).setScale(DECIMALS, ROUNDING);
                if (Debug.verboseOn()) {
                    Debug.logVerbose("Creating Invoice Item with amount " + adjustment.getBigDecimal("amount") + " prorated to " + amount
                            + " for return adjustment [" + adjustment.getString("returnAdjustmentId") + "]", module);
                }

                // prepare the invoice item for the return-wide adjustment
                input = UtilMisc.toMap("invoiceId", invoiceId, "invoiceItemTypeId", invoiceItemTypeId, "quantity", BigDecimal.ONE);
                input.put("amount", amount);
                input.put("invoiceItemSeqId", "" + invoiceItemSeqId); // turn the int into a string with ("" + int) hack
                input.put("description", adjustment.get("description"));
                input.put("overrideGlAccountId", adjustment.get("overrideGlAccountId"));
                input.put("taxAuthPartyId", adjustment.get("taxAuthPartyId"));
                input.put("taxAuthGeoId", adjustment.get("taxAuthGeoId"));
                input.put("userLogin", userLogin);

                // XXX TODO Note: we need to implement ReturnAdjustment.includeInTax for this to work properly
                input.put("taxableFlag", adjustment.get("includeInTax"));

                // create the invoice item
                serviceResults = dispatcher.runSync("createInvoiceItem", input);
                if (ServiceUtil.isError(serviceResults)) {
                    return ServiceUtil.returnError(errorMsg, null, null, serviceResults);
                }

                // increment the seqId counter
                invoiceItemSeqNum += 1;
                invoiceItemSeqId = UtilFormatOut.formatPaddedNumber(invoiceItemSeqNum, INVOICE_ITEM_SEQUENCE_ID_DIGITS);
            }

            // Set the invoice to READY
            serviceResults = dispatcher.runSync("setInvoiceStatus", UtilMisc.<String, Object>toMap("invoiceId", invoiceId, "statusId", "INVOICE_READY", "userLogin", userLogin));
            if (ServiceUtil.isError(serviceResults)) {
                return ServiceUtil.returnError(errorMsg, null, null, serviceResults);
            }

            // return the invoiceId
            Map<String, Object> results = ServiceUtil.returnSuccess();
            results.put("invoiceId", invoiceId);
            return results;
        } catch (GenericServiceException e) {
            Debug.logError(e, errorMsg + e.getMessage(), module);
            return ServiceUtil.returnError(errorMsg + e.getMessage());
        } catch (GenericEntityException e) {
            Debug.logError(e, errorMsg + e.getMessage(), module);
            return ServiceUtil.returnError(errorMsg + e.getMessage());
        }
    }

    public static Map<String, Object> checkInvoicePaymentApplications(DispatchContext ctx, Map<String, Object> context) {
        Delegator delegator = ctx.getDelegator();
        LocalDispatcher dispatcher = ctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");

        if (DECIMALS == -1 || ROUNDING == -1) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingAritmeticPropertiesNotConfigured",locale));
        }

        String invoiceId = (String) context.get("invoiceId");
        GenericValue invoice = null ;
        Timestamp invoiceDate = null;
        try {
            invoice = delegator.findByPrimaryKey("Invoice", UtilMisc.toMap("invoiceId", invoiceId));
            invoiceDate = invoice.getTimestamp("invoiceDate");
        } catch (GenericEntityException e) {
            Debug.logError(e, "Problem getting Invoice for Invoice ID" + invoiceId, module);
            return ServiceUtil.returnError("Problem getting Invoice for Invoice ID" + invoiceId);
        }

        // Ignore invoices that aren't ready yet
        if (! invoice.getString("statusId").equals("INVOICE_READY")) {
            return ServiceUtil.returnSuccess();
        }

        // Get the payment applications that can be used to pay the invoice
        List<GenericValue> paymentAppl = null;
        try {
            paymentAppl = delegator.findByAnd("PaymentAndApplication", UtilMisc.toMap("invoiceId", invoiceId));
            paymentAppl = EntityUtil.orderBy(paymentAppl, UtilMisc.toList("amountApplied"));
            // For each payment application, select only those that are RECEIVED or SENT based on whether the payment is a RECEIPT or DISBURSEMENT respectively
            for (Iterator<GenericValue> iter = paymentAppl.iterator(); iter.hasNext();) {
                GenericValue payment = iter.next();
                if ("PMNT_CONFIRMED".equals(payment.get("statusId"))) {
                    continue; // keep
                }
                if ("PMNT_RECEIVED".equals(payment.get("statusId")) && UtilAccounting.isReceipt(payment)) {
                    continue; // keep
                }
                if ("PMNT_SENT".equals(payment.get("statusId")) && UtilAccounting.isDisbursement(payment)) {
                    continue; // keep
                }
                // all other cases, remove the payment application
                iter.remove();
            }
        } catch (GenericEntityException e) {
            String errMsg = UtilProperties.getMessage(resource, "AccountingProblemGettingPaymentApplication",UtilMisc.toMap("invoiceId",invoiceId), locale);
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        }

        Map<String, BigDecimal> payments = FastMap.newInstance();
        Timestamp paidDate = null;
        for (GenericValue payAppl : paymentAppl) {
            payments.put(payAppl.getString("paymentId"), payAppl.getBigDecimal("amountApplied"));

            // paidDate will be the last date (chronologically) of all the Payments applied to this invoice
           /* Timestamp paymentDate = payAppl.getTimestamp("effectiveDate");
            if (paymentDate != null) {
                if ((paidDate == null) || (paidDate.before(paymentDate))) {
                    paidDate = paymentDate;
                }
                // invoice date is greater than the payment date then paiddate as invoice date
                if ((paidDate.before(invoiceDate))) {
                    paidDate = invoiceDate;
                }
            }*/
        }
       // lets get the paid date for the invoice 
        paidDate = getInvoicePaidDate(delegator,invoice.getString("invoiceId"));
        BigDecimal totalPayments = ZERO;
        for (String paymentId : payments.keySet()) {
            BigDecimal amount = payments.get(paymentId);
            if (amount == null) amount = ZERO;
            totalPayments = totalPayments.add(amount).setScale(DECIMALS, ROUNDING);
        }

        if (totalPayments.signum() == 1) {
            BigDecimal invoiceTotal = InvoiceWorker.getInvoiceTotal(delegator, invoiceId);
            if (Debug.verboseOn()) {
                Debug.logVerbose("Invoice #" + invoiceId + " total: " + invoiceTotal, module);
                Debug.logVerbose("Total payments : " + totalPayments, module);
            }
            if (totalPayments.compareTo(invoiceTotal) >= 0) { // this checks that totalPayments is greater than or equal to invoiceTotal
                // this invoice is paid
                Map<String, Object> svcCtx = UtilMisc.toMap("statusId", "INVOICE_PAID", "invoiceId", invoiceId,
                        "paidDate", paidDate, "userLogin", userLogin);
                try {
                    dispatcher.runSync("setInvoiceStatus", svcCtx);
                } catch (GenericServiceException e) {
                    String errMsg = UtilProperties.getMessage(resource, "AccountingProblemChangingInvoiceStatusTo",UtilMisc.toMap("newStatus","INVOICE_PAID"), locale);
                    Debug.logError(e, errMsg + svcCtx, module);
                    return ServiceUtil.returnError(errMsg);
                }
            }
        } else {
            Debug.logInfo("No payments found for Invoice #" + invoiceId, module);
        }

        return ServiceUtil.returnSuccess();
    }
 public static Timestamp getInvoicePaidDate(Delegator delegator, String invoiceId) {   
        
        Timestamp paidDate = null;
        try {
        	List<GenericValue> paymentAppl = delegator.findByAnd("PaymentAndApplication", UtilMisc.toMap("invoiceId", invoiceId));
        	GenericValue invoice = delegator.findOne("Invoice", UtilMisc.toMap("invoiceId",invoiceId), true);
        	Timestamp invoiceDate = invoice.getTimestamp("invoiceDate");
        	 for (GenericValue payAppl : paymentAppl) {
                 // paidDate will be the last date (chronologically) of all the Payments applied to this invoice
                 Timestamp paymentDate = payAppl.getTimestamp("effectiveDate");
                 if (paymentDate != null) {
                     if ((paidDate == null) || (paidDate.before(paymentDate))) {
                         paidDate = paymentDate;
                     }
                     // invoice date is greater than the payment date then paiddate as invoice date
                     if ((paidDate.before(invoiceDate))) {
                         paidDate = invoiceDate;
                     }
                 }
                 
             }
        }catch (Exception e) {
        	 String errMsg = "unable to get invoice paid date";
             Debug.logError(e, errMsg, module);
		}  
        return paidDate;
    }
    private static BigDecimal calcHeaderAdj(Delegator delegator, GenericValue adj, String invoiceTypeId, String invoiceId, String invoiceItemSeqId,
            BigDecimal divisor, BigDecimal multiplier, BigDecimal baseAmount, int decimals, int rounding, GenericValue userLogin, LocalDispatcher dispatcher, Locale locale) {
        BigDecimal adjAmount = ZERO;
        if (adj.get("amount") != null) {

            // pro-rate the amount
            BigDecimal amount = ZERO;
            // make sure the divisor is not 0 to avoid NaN problems; just leave the amount as 0 and skip it in essense
            if (divisor.signum() != 0) {
                // multiply first then divide to avoid rounding errors
                amount = baseAmount.multiply(multiplier).divide(divisor, decimals, rounding);
            }
            if ((invoiceTypeId != null) && (invoiceTypeId.equals("PURCHASE_INVOICE"))) {//  for Purchase Invoice we are skipping above for fixing Freight and Discount issues
            	amount=baseAmount; 
            }
            if (amount.signum() != 0) {
                Map<String, Object> createInvoiceItemContext = FastMap.newInstance();
                createInvoiceItemContext.put("invoiceId", invoiceId);
                createInvoiceItemContext.put("invoiceItemSeqId", invoiceItemSeqId);
                createInvoiceItemContext.put("invoiceItemTypeId", getInvoiceItemType(delegator, adj.getString("orderAdjustmentTypeId"), null, invoiceTypeId, "INVOICE_ADJ"));
                createInvoiceItemContext.put("description", adj.get("description"));
                createInvoiceItemContext.put("quantity", BigDecimal.ONE);
                createInvoiceItemContext.put("amount", amount);
                createInvoiceItemContext.put("overrideGlAccountId", adj.get("overrideGlAccountId"));
                //createInvoiceItemContext.put("productId", orderItem.get("productId"));
                //createInvoiceItemContext.put("productFeatureId", orderItem.get("productFeatureId"));
                //createInvoiceItemContext.put("uomId", "");
                //createInvoiceItemContext.put("taxableFlag", product.get("taxable"));
                createInvoiceItemContext.put("taxAuthPartyId", adj.get("taxAuthPartyId"));
                createInvoiceItemContext.put("taxAuthGeoId", adj.get("taxAuthGeoId"));
                createInvoiceItemContext.put("taxAuthorityRateSeqId", adj.get("taxAuthorityRateSeqId"));
                createInvoiceItemContext.put("userLogin", userLogin);
                Map<String, Object> createInvoiceItemResult = null;
                try {
                    createInvoiceItemResult = dispatcher.runSync("createInvoiceItem", createInvoiceItemContext);
                } catch (GenericServiceException e) {
                    String errMsg = UtilProperties.getMessage(resource,"AccountingServiceErrorCreatingInvoiceItemFromOrder",locale) + ": " + e.toString();
                    Debug.logError(e, errMsg, module);
                    ServiceUtil.returnError(errMsg);
                }
                if (ServiceUtil.isError(createInvoiceItemResult)) {
                    ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceItemFromOrder",locale), null, null, createInvoiceItemResult);
                }

                // Create the OrderAdjustmentBilling record
                Map<String, Object> createOrderAdjustmentBillingContext = FastMap.newInstance();
                createOrderAdjustmentBillingContext.put("orderAdjustmentId", adj.getString("orderAdjustmentId"));
                createOrderAdjustmentBillingContext.put("invoiceId", invoiceId);
                createOrderAdjustmentBillingContext.put("invoiceItemSeqId", invoiceItemSeqId);
                createOrderAdjustmentBillingContext.put("amount", amount);
                createOrderAdjustmentBillingContext.put("userLogin", userLogin);

                try {
                    Map<String, Object> createOrderAdjustmentBillingResult = dispatcher.runSync("createOrderAdjustmentBilling", createOrderAdjustmentBillingContext);
                } catch (GenericServiceException e) {
                    ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingOrderAdjustmentBillingFromOrder",locale), null, null, createOrderAdjustmentBillingContext);
                }

            }
            amount = amount.setScale(decimals, rounding);
            adjAmount = amount;
        }
        else if (adj.get("sourcePercentage") != null) {
            // pro-rate the amount
            BigDecimal percent = adj.getBigDecimal("sourcePercentage");
            percent = percent.divide(new BigDecimal(100), 100, rounding);
            BigDecimal amount = ZERO;
            // make sure the divisor is not 0 to avoid NaN problems; just leave the amount as 0 and skip it in essense
            if (divisor.signum() != 0) {
                // multiply first then divide to avoid rounding errors
                amount = percent.multiply(divisor);
            }
            if (amount.signum() != 0) {
                Map<String, Object> createInvoiceItemContext = FastMap.newInstance();
                createInvoiceItemContext.put("invoiceId", invoiceId);
                createInvoiceItemContext.put("invoiceItemSeqId", invoiceItemSeqId);
                createInvoiceItemContext.put("invoiceItemTypeId", getInvoiceItemType(delegator, adj.getString("orderAdjustmentTypeId"), null, invoiceTypeId, "INVOICE_ADJ"));
                createInvoiceItemContext.put("description", adj.get("description"));
                createInvoiceItemContext.put("quantity", BigDecimal.ONE);
                createInvoiceItemContext.put("amount", amount);
                createInvoiceItemContext.put("overrideGlAccountId", adj.get("overrideGlAccountId"));
                //createInvoiceItemContext.put("productId", orderItem.get("productId"));
                //createInvoiceItemContext.put("productFeatureId", orderItem.get("productFeatureId"));
                //createInvoiceItemContext.put("uomId", "");
                //createInvoiceItemContext.put("taxableFlag", product.get("taxable"));
                createInvoiceItemContext.put("taxAuthPartyId", adj.get("taxAuthPartyId"));
                createInvoiceItemContext.put("taxAuthGeoId", adj.get("taxAuthGeoId"));
                createInvoiceItemContext.put("taxAuthorityRateSeqId", adj.get("taxAuthorityRateSeqId"));
                createInvoiceItemContext.put("userLogin", userLogin);

                Map<String, Object> createInvoiceItemResult = null;
                try {
                    createInvoiceItemResult = dispatcher.runSync("createInvoiceItem", createInvoiceItemContext);
                } catch (GenericServiceException e) {
                    String errMsg = UtilProperties.getMessage(resource,"AccountingServiceErrorCreatingInvoiceItemFromOrder",locale) + ": " + e.toString();
                    Debug.logError(e, errMsg, module);
                    ServiceUtil.returnError(errMsg);
                }
                if (ServiceUtil.isError(createInvoiceItemResult)) {
                    ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceItemFromOrder",locale), null, null, createInvoiceItemResult);
                }

                // Create the OrderAdjustmentBilling record
                Map<String, Object> createOrderAdjustmentBillingContext = FastMap.newInstance();
                createOrderAdjustmentBillingContext.put("orderAdjustmentId", adj.getString("orderAdjustmentId"));
                createOrderAdjustmentBillingContext.put("invoiceId", invoiceId);
                createOrderAdjustmentBillingContext.put("invoiceItemSeqId", invoiceItemSeqId);
                createOrderAdjustmentBillingContext.put("amount", amount);
                createOrderAdjustmentBillingContext.put("userLogin", userLogin);

                try {
                    Map<String, Object> createOrderAdjustmentBillingResult = dispatcher.runSync("createOrderAdjustmentBilling", createOrderAdjustmentBillingContext);
                } catch (GenericServiceException e) {
                    ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingOrderAdjustmentBillingFromOrder",locale), null, null, createOrderAdjustmentBillingContext);
                }

            }
            amount = amount.setScale(decimals, rounding);
            adjAmount = amount;
        }

        Debug.logInfo("adjAmount: " + adjAmount + ", divisor: " + divisor + ", multiplier: " + multiplier +
                ", invoiceTypeId: " + invoiceTypeId + ", invoiceId: " + invoiceId + ", itemSeqId: " + invoiceItemSeqId +
                ", decimals: " + decimals + ", rounding: " + rounding + ", adj: " + adj, module);
        return adjAmount;
    }
    
    
    
    
    /* Creates InvoiceTerm entries for a list of terms, which can be BillingAccountTerms, OrderTerms, etc. */
    private static void createInvoiceTerms(Delegator delegator, LocalDispatcher dispatcher, String invoiceId, List<GenericValue> terms, GenericValue userLogin, Locale locale) {
        if (terms != null) {
            for (GenericValue term : terms) {

                Map<String, Object> createInvoiceTermContext = FastMap.newInstance();
                createInvoiceTermContext.put("invoiceId", invoiceId);
                createInvoiceTermContext.put("invoiceItemSeqId", "_NA_");
                createInvoiceTermContext.put("termTypeId", term.get("termTypeId"));
                createInvoiceTermContext.put("termValue", term.get("termValue"));
                createInvoiceTermContext.put("termDays", term.get("termDays"));
                if (!"BillingAccountTerm".equals(term.getEntityName())) {
                    createInvoiceTermContext.put("textValue", term.get("textValue"));
                    createInvoiceTermContext.put("description", term.get("description"));
                }
                createInvoiceTermContext.put("uomId", term.get("uomId"));
                createInvoiceTermContext.put("userLogin", userLogin);

                Map<String, Object> createInvoiceTermResult = null;
                try {
                    createInvoiceTermResult = dispatcher.runSync("createInvoiceTerm", createInvoiceTermContext);
                } catch (GenericServiceException e) {
                    String errMsg = UtilProperties.getMessage(resource,"AccountingServiceErrorCreatingInvoiceTermFromOrder",locale) + ": " + e.toString();
                    Debug.logError(e, errMsg, module);
                    ServiceUtil.returnError(errMsg);
                }
                if (ServiceUtil.isError(createInvoiceTermResult)) {
                    ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceTermFromOrder",locale), null, null, createInvoiceTermResult);
                }
            }
        }
    }

    /**
     * Service to add payment application records to indicate which invoices
     * have been paid/received. For invoice processing, this service works on
     * the invoice level when 'invoiceProcessing' parameter is set to "Y" else
     * it works on the invoice item level.
     */
    public static Map<String, Object> updatePaymentApplication(DispatchContext dctx, Map<String, Object> context) {
        if (!context.containsKey("useHighestAmount")) {
            context.put("useHighestAmount","N");
        }
        BigDecimal amountApplied = (BigDecimal) context.get("amountApplied");
        if (amountApplied != null) {
            context.put("amountApplied", amountApplied);
        } else {
            amountApplied = ZERO;
            context.put("amountApplied", ZERO);
        }

        return updatePaymentApplicationDefBd(dctx, context);
    }

    /**
     * Service to add payment application records to indicate which invoices
     * have been paid/received. For invoice processing, this service works on
     * the invoice level when 'invoiceProcessing' parameter is set to "Y" else
     * it works on the invoice item level.
     *
     * This version will apply as much as possible when no amountApplied is provided.
     */
    public static Map<String, Object> updatePaymentApplicationDef(DispatchContext dctx, Map<String, Object> context) {
        if (!context.containsKey("useHighestAmount")) {
            context.put("useHighestAmount","Y");
        }
        return updatePaymentApplication(dctx, context);
    }

    public static Map<String, Object> updatePaymentApplicationDefBd(DispatchContext dctx, Map<String, Object> context) {
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");

        if (DECIMALS == -1 || ROUNDING == -1) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingAritmeticPropertiesNotConfigured",locale));
        }

        if (!context.containsKey("useHighestAmount")) {
            context.put("useHighestAmount","Y");
        }

        String defaultInvoiceProcessing = UtilProperties.getPropertyValue("AccountingConfig","invoiceProcessing");

        boolean debug = true; // show processing messages in the log..or not....

        // a 'y' in invoiceProssesing will reverse the default processing
        String changeProcessing = (String) context.get("invoiceProcessing");
        String invoiceId = (String) context.get("invoiceId");
        String invoiceItemSeqId = (String) context.get("invoiceItemSeqId");
        String paymentId = (String) context.get("paymentId");
        String toPaymentId = (String) context.get("toPaymentId");
        String paymentApplicationId = (String) context.get("paymentApplicationId");
        BigDecimal amountApplied = (BigDecimal) context.get("amountApplied");
        String billingAccountId = (String) context.get("billingAccountId");
        String taxAuthGeoId = (String) context.get("taxAuthGeoId");
        String useHighestAmount = (String) context.get("useHighestAmount");

        List<String> errorMessageList = FastList.newInstance();

        if (debug) Debug.logInfo("updatePaymentApplicationDefBd input parameters..." +
                " defaultInvoiceProcessing: " + defaultInvoiceProcessing +
                " changeDefaultInvoiceProcessing: " + changeProcessing +
                " useHighestAmount: " + useHighestAmount +
                " paymentApplicationId: " + paymentApplicationId +
                " PaymentId: " + paymentId +
                " InvoiceId: " + invoiceId +
                " InvoiceItemSeqId: " + invoiceItemSeqId +
                " BillingAccountId: " + billingAccountId +
                " toPaymentId: " + toPaymentId +
                " amountApplied: " + amountApplied +
                " TaxAuthGeoId: " + taxAuthGeoId, module);

        if (changeProcessing == null) {
            changeProcessing = "N";    // not provided, so no change
        }

        boolean invoiceProcessing = true;
        if (defaultInvoiceProcessing.equals("YY")) {
            invoiceProcessing = true;
        } else if (defaultInvoiceProcessing.equals("NN")) {
            invoiceProcessing = false;
        } else if (defaultInvoiceProcessing.equals("Y")) {
            invoiceProcessing = !"Y".equals(changeProcessing);
        } else if (defaultInvoiceProcessing.equals("N")) {
            invoiceProcessing = "Y".equals(changeProcessing);
        }

        // on a new paymentApplication check if only billing or invoice or tax
        // id is provided not 2,3... BUT a combination of billingAccountId and invoiceId is permitted - that's how you use a
        // Billing Account to pay for an Invoice
        if (paymentApplicationId == null) {
            int count = 0;
            if (invoiceId != null) count++;
            if (toPaymentId != null) count++;
            if (billingAccountId != null) count++;
            if (taxAuthGeoId != null) count++;
            if ((billingAccountId != null) && (invoiceId != null)) count--;
            if (count != 1) {
                errorMessageList.add(UtilProperties.getMessage(resource, "AccountingSpecifyInvoiceToPaymentBillingAccountTaxGeoId", locale));
            }
        }

        // avoid null pointer exceptions.
        if (amountApplied == null) amountApplied = ZERO;
        // makes no sense to have an item numer without an invoice number
        if (invoiceId == null) invoiceItemSeqId = null;

        // retrieve all information and perform checking on the retrieved info.....

        // Payment.....
        BigDecimal paymentApplyAvailable = ZERO;
        // amount available on the payment reduced by the already applied amounts
        BigDecimal amountAppliedMax = ZERO;
        // the maximum that can be applied taking payment,invoice,invoiceitem,billing account in concideration
        // if maxApplied is missing, this value can be used,
        // Payment this should be checked after the invoice checking because it is possible the currency is changed
        GenericValue payment = null;
        String currencyUomId = null;
        if (paymentId == null || paymentId.equals("")) {
            errorMessageList.add(UtilProperties.getMessage(resource, "AccountingPaymentIdBlankNotSupplied",locale));
        } else {
            try {
                payment = delegator.findByPrimaryKey("Payment", UtilMisc.toMap("paymentId", paymentId));
            } catch (GenericEntityException e) {
                ServiceUtil.returnError(e.getMessage());
            }
            if (payment == null) {
                errorMessageList.add(UtilProperties.getMessage(resource, "AccountingPaymentRecordNotFound",UtilMisc.toMap("paymentId",paymentId),locale));
            }
            paymentApplyAvailable = payment.getBigDecimal("amount").subtract(PaymentWorker.getPaymentApplied(payment)).setScale(DECIMALS,ROUNDING);

            if (payment.getString("statusId").equals("PMNT_CANCELLED")) {
                errorMessageList.add(UtilProperties.getMessage(resource, "AccountingPaymentCancelled", UtilMisc.toMap("paymentId",paymentId), locale));
            }
            //If PaymentType is advance and payment status is confirmed  it will allow application
            try {
            	GenericValue paymentType = delegator.findByPrimaryKey("PaymentType", UtilMisc.toMap("paymentTypeId", payment.getString("paymentTypeId")));
                if(UtilValidate.isNotEmpty(paymentType)){
                	String parentTypeId = (String) paymentType.get("parentTypeId");
                	if(UtilValidate.isNotEmpty(parentTypeId) && !parentTypeId.equals("ADVANCES_PAYOUT")){
                		if (payment.getString("statusId").equals("PMNT_CONFIRMED")) {
                            errorMessageList.add(UtilProperties.getMessage(resource, "AccountingPaymentConfirmed", UtilMisc.toMap("paymentId",paymentId), locale));
                        }
                	}
                }else{
                	if (payment.getString("statusId").equals("PMNT_CONFIRMED")) {
                        errorMessageList.add(UtilProperties.getMessage(resource, "AccountingPaymentConfirmed", UtilMisc.toMap("paymentId",paymentId), locale));
                    }
                }
            }catch (GenericEntityException e) {
                ServiceUtil.returnError(e.getMessage());
            }
            
            currencyUomId = payment.getString("currencyUomId");

            // if the amount to apply is 0 give it amount the payment still need
            // to apply
            if (amountApplied.signum() == 0) {
                amountAppliedMax = paymentApplyAvailable;
            }

        }

        // the "TO" Payment.....
        BigDecimal toPaymentApplyAvailable = ZERO;
        GenericValue toPayment = null;
        if (toPaymentId != null && !toPaymentId.equals("")) {
            try {
                toPayment = delegator.findByPrimaryKey("Payment", UtilMisc.toMap("paymentId", toPaymentId));
            } catch (GenericEntityException e) {
                ServiceUtil.returnError(e.getMessage());
            }
            if (toPayment == null) {
                errorMessageList.add(UtilProperties.getMessage(resource, "AccountingPaymentRecordNotFound",UtilMisc.toMap("paymentId",toPaymentId),locale));
            }
            toPaymentApplyAvailable = toPayment.getBigDecimal("amount").subtract(PaymentWorker.getPaymentApplied(toPayment)).setScale(DECIMALS,ROUNDING);

            if (toPayment.getString("statusId").equals("PMNT_CANCELLED")) {
                errorMessageList.add(UtilProperties.getMessage(resource, "AccountingPaymentCancelled", UtilMisc.toMap("paymentId",paymentId), locale));
            }
            //If PaymentType is advance and payment status is confirmed  it will allow application
            try {
                GenericValue toPaymentType = delegator.findByPrimaryKey("PaymentType", UtilMisc.toMap("paymentTypeId", toPayment.getString("paymentTypeId")));
	            if(UtilValidate.isNotEmpty(toPaymentType)){
	            	String toParentTypeId = (String) toPaymentType.get("parentTypeId");
	            	if(UtilValidate.isNotEmpty(toParentTypeId) && !toParentTypeId.equals("REFUND_PAYIN")){
	            		 if (toPayment.getString("statusId").equals("PMNT_CONFIRMED")) {
	                         errorMessageList.add(UtilProperties.getMessage(resource, "AccountingPaymentConfirmed", UtilMisc.toMap("paymentId",paymentId), locale));
	                     }
	            	}
	            }else{
	            	if (toPayment.getString("statusId").equals("PMNT_CONFIRMED")) {
                        errorMessageList.add(UtilProperties.getMessage(resource, "AccountingPaymentConfirmed", UtilMisc.toMap("paymentId",paymentId), locale));
                    }
	            }
            } catch (GenericEntityException e) {
                ServiceUtil.returnError(e.getMessage());
            }
            // if the amount to apply is less then required by the payment reduce it
            if (amountAppliedMax.compareTo(toPaymentApplyAvailable) > 0) {
                amountAppliedMax = toPaymentApplyAvailable;
            }

            if (paymentApplicationId == null) {
                // only check for new application records, update on existing records is checked in the paymentApplication section
                if (toPaymentApplyAvailable.signum() == 0) {
                    errorMessageList.add(UtilProperties.getMessage(resource, "AccountingPaymentAlreadyApplied",UtilMisc.toMap("paymentId",toPaymentId), locale));
                } else {
                    // check here for too much application if a new record is
                    // added (paymentApplicationId == null)
                    if (amountApplied.compareTo(toPaymentApplyAvailable) > 0) {
                            errorMessageList.add(UtilProperties.getMessage(resource, "AccountingPaymentLessRequested",
                                    UtilMisc.<String, Object>toMap("paymentId",toPaymentId,
                                                "paymentApplyAvailable",toPaymentApplyAvailable,
                                                "amountApplied",amountApplied,"isoCode", currencyUomId),locale));
                    }
                }
            }

            // check if at least one send is the same as one receiver on the other payment
            if (!payment.getString("partyIdFrom").equals(toPayment.getString("partyIdTo")) &&
                    !payment.getString("partyIdTo").equals(toPayment.getString("partyIdFrom")))    {
                errorMessageList.add(UtilProperties.getMessage(resource, "AccountingFromPartySameToParty", locale));
            }

            if (debug) Debug.logInfo("toPayment info retrieved and checked...", module);
        }

        // assign payment to billing account if the invoice is assigned to this billing account
        if (invoiceId != null) {
            GenericValue invoice = null;
            try {
                invoice = delegator.findByPrimaryKey("Invoice", UtilMisc.toMap("invoiceId", invoiceId));
            } catch (GenericEntityException e) {
                ServiceUtil.returnError(e.getMessage());
            }

            if (invoice == null) {
                errorMessageList.add(UtilProperties.getMessage(resource, "AccountingInvoiceNotFound",UtilMisc.toMap("invoiceId",invoiceId),locale));
            } else {
                if (invoice.getString("billingAccountId") != null) {
                    billingAccountId = invoice.getString("billingAccountId");
                }
            }
        }

        // billing account
        GenericValue billingAccount = null;
        if (billingAccountId != null && !billingAccountId.equals("")) {
            try {
                billingAccount = delegator.findByPrimaryKey("BillingAccount", UtilMisc.toMap("billingAccountId", billingAccountId));
            } catch (GenericEntityException e) {
                ServiceUtil.returnError(e.getMessage());
            }
            if (billingAccount == null) {
                errorMessageList.add(UtilProperties.getMessage(resource, "AccountingBillingAccountNotFound",UtilMisc.toMap("billingAccountId",billingAccountId), locale));
            }
            // check the currency
            if (billingAccount.get("accountCurrencyUomId") != null && currencyUomId != null &&
                    !billingAccount.getString("accountCurrencyUomId").equals(currencyUomId)) {
                errorMessageList.add(UtilProperties.getMessage(resource, "AccountingBillingAccountCurrencyProblem",
                        UtilMisc.toMap("billingAccountId",billingAccountId,"accountCurrencyUomId",billingAccount.getString("accountCurrencyUomId"),
                                "paymentId",paymentId,"paymentCurrencyUomId", currencyUomId),locale));
            }

            if (debug) Debug.logInfo("Billing Account info retrieved and checked...", module);
        }

        // get the invoice (item) information
        BigDecimal invoiceApplyAvailable = ZERO;
        // amount available on the invoice reduced by the already applied amounts
        BigDecimal invoiceItemApplyAvailable = ZERO;
        // amount available on the invoiceItem reduced by the already applied amounts
        GenericValue invoice = null;
        GenericValue invoiceItem = null;
        if (invoiceId != null) {
            try {
                invoice = delegator.findByPrimaryKey("Invoice", UtilMisc.toMap("invoiceId", invoiceId));
            } catch (GenericEntityException e) {
                ServiceUtil.returnError(e.getMessage());
            }

            if (invoice == null) {
                errorMessageList.add(UtilProperties.getMessage(resource, "AccountingInvoiceNotFound",UtilMisc.toMap("invoiceId",invoiceId),locale));
            } else { // check the invoice and when supplied the invoice item...

                if (invoice.getString("statusId").equals("INVOICE_CANCELLED")) {
                    errorMessageList.add(UtilProperties.getMessage(resource,"AccountingInvoiceCancelledCannotApplyTo",UtilMisc.toMap("invoiceId",invoiceId),locale));
                }

                // check the currency
                if (currencyUomId != null && invoice.get("currencyUomId") != null &&
                        !currencyUomId.equals(invoice.getString("currencyUomId"))) {
                    Debug.logInfo(UtilProperties.getMessage(resource, "AccountingInvoicePaymentCurrencyProblem",
                            UtilMisc.toMap("invoiceCurrency", invoice.getString("currencyUomId"), "paymentCurrency", payment.getString("currencyUomId")),locale), module);
                    Debug.logInfo("will try to apply payment on the actualCurrency amount on payment", module);

                    if (payment.get("actualCurrencyAmount") == null || payment.get("actualCurrencyUomId") == null) {
                        errorMessageList.add("Actual amounts are required in the currency of the invoice to make this work....");
                    } else {
                        currencyUomId = payment.getString("actualCurrencyUomId");
                        if (!currencyUomId.equals(invoice.getString("currencyUomId"))) {
                            errorMessageList.add("actual currency on payment (" + currencyUomId + ") not the same as original invoice currency (" + invoice.getString("currencyUomId") + ")");
                        }
                    }
                    paymentApplyAvailable = payment.getBigDecimal("actualCurrencyAmount").subtract(PaymentWorker.getPaymentApplied(payment)).setScale(DECIMALS,ROUNDING);
                    if (amountApplied.signum() == 0) {
                        amountAppliedMax = paymentApplyAvailable;
                    }
                }

                // check if the invoice already covered by payments
                BigDecimal invoiceTotal = InvoiceWorker.getInvoiceTotal(invoice);
                invoiceApplyAvailable = InvoiceWorker.getInvoiceNotApplied(invoice);

                // adjust the amountAppliedMax value if required....
                if (invoiceApplyAvailable.compareTo(amountAppliedMax) < 0) {
                    amountAppliedMax = invoiceApplyAvailable;
                }

                if (invoiceTotal.signum() == 0) {
                    errorMessageList.add(UtilProperties.getMessage(resource,"AccountingInvoiceTotalZero",UtilMisc.toMap("invoiceId",invoiceId),locale));
                } else if (paymentApplicationId == null) {
                    // only check for new records here...updates are checked in the paymentApplication section
                    if (invoiceApplyAvailable.signum() == 0) {
                        errorMessageList.add(UtilProperties.getMessage(resource,"AccountingInvoiceCompletelyApplied",UtilMisc.toMap("invoiceId",invoiceId),locale));
                    }
                    // check here for too much application if a new record(s) are
                    // added (paymentApplicationId == null)
                    else if (amountApplied.compareTo(invoiceApplyAvailable) > 0) {
                        errorMessageList.add(UtilProperties.getMessage(resource, "AccountingInvoiceLessRequested",
                                UtilMisc.<String, Object>toMap("invoiceId",invoiceId,
                                            "invoiceApplyAvailable",invoiceApplyAvailable,
                                            "amountApplied",amountApplied,"isoCode",invoice.getString("currencyUomId")),locale));
                    }
                }

                // check if at least one sender is the same as one receiver on the invoice
                if (!payment.getString("partyIdFrom").equals(invoice.getString("partyId")) &&
                        !payment.getString("partyIdTo").equals(invoice.getString("partyIdFrom")))    {
                    errorMessageList.add(UtilProperties.getMessage(resource, "AccountingFromPartySameToParty", locale));
                }

                if (debug) Debug.logInfo("Invoice info retrieved and checked ...", module);
            }

            // if provided check the invoice item.
            if (invoiceItemSeqId != null) {
                // when itemSeqNr not provided delay checking on invoiceItemSeqId
                try {
                    invoiceItem = delegator.findByPrimaryKey("InvoiceItem", UtilMisc.toMap("invoiceId", invoiceId, "invoiceItemSeqId", invoiceItemSeqId));
                } catch (GenericEntityException e) {
                    ServiceUtil.returnError(e.getMessage());
                }

                if (invoiceItem == null) {
                    errorMessageList.add(UtilProperties.getMessage(resource,"AccountingInvoiceItemNotFound",UtilMisc.toMap("invoiceId",invoiceId,"invoiceItemSeqId",invoiceItemSeqId),locale));
                } else {
                    if (invoice.get("currencyUomId") != null && currencyUomId != null && !invoice.getString("currencyUomId").equals(currencyUomId)) {
                        errorMessageList.add(UtilProperties.getMessage(resource,"AccountingInvoicePaymentCurrencyProblem",UtilMisc.toMap("paymentCurrencyId", currencyUomId,"itemCurrency",invoice.getString("currencyUomId")) ,locale));
                    }

                    // get the invoice item applied value
                    BigDecimal quantity = null;
                    if (invoiceItem.get("quantity") == null) {
                        quantity = BigDecimal.ONE;
                    } else {
                        quantity = invoiceItem.getBigDecimal("quantity").setScale(DECIMALS,ROUNDING);
                    }
                    invoiceItemApplyAvailable = invoiceItem.getBigDecimal("amount").multiply(quantity).setScale(DECIMALS,ROUNDING).subtract(InvoiceWorker.getInvoiceItemApplied(invoiceItem));
                    // check here for too much application if a new record is added
                    // (paymentApplicationId == null)
                    if (paymentApplicationId == null && amountApplied.compareTo(invoiceItemApplyAvailable) > 0) {
                        // new record
                        errorMessageList.add("Invoice(" + invoiceId + ") item(" + invoiceItemSeqId + ") has  " + invoiceItemApplyAvailable + " to apply but " + amountApplied + " is requested\n");
                        String uomId = invoice.getString("currencyUomId");
                        errorMessageList.add(UtilProperties.getMessage(resource, "AccountingInvoiceItemLessRequested",
                                UtilMisc.<String, Object>toMap("invoiceId",invoiceId, "invoiceItemSeqId", invoiceItemSeqId,
                                            "invoiceItemApplyAvailable",invoiceItemApplyAvailable,
                                            "amountApplied",amountApplied,"isoCode",uomId),locale));
                    }
                }
                if (debug) Debug.logInfo("InvoiceItem info retrieved and checked against the Invoice (currency and amounts) ...", module);
            }
        }

        // check this at the end because the invoice can change the currency.......
        if (paymentApplicationId == null) {
            // only check for new application records, update on existing records is checked in the paymentApplication section
            if (paymentApplyAvailable.signum() == 0) {
                errorMessageList.add(UtilProperties.getMessage(resource, "AccountingPaymentAlreadyApplied",UtilMisc.toMap("paymentId",paymentId), locale));
            } else {
                // check here for too much application if a new record is
                // added (paymentApplicationId == null)
                if (amountApplied.compareTo(paymentApplyAvailable) > 0) {
                    errorMessageList.add(UtilProperties.getMessage(resource, "AccountingPaymentLessRequested",
                            UtilMisc.<String, Object>toMap("paymentId",paymentId,
                                        "paymentApplyAvailable",paymentApplyAvailable,
                                        "amountApplied",amountApplied,"isoCode", currencyUomId),locale));
                }
            }
        }


        // get the application record if the applicationId is supplied if not
        // create empty record.
        BigDecimal newInvoiceApplyAvailable = invoiceApplyAvailable;
        // amount available on the invoice taking into account if the invoiceItemnumber has changed
        BigDecimal newInvoiceItemApplyAvailable = invoiceItemApplyAvailable;
        // amount available on the invoiceItem taking into account if the itemnumber has changed
        BigDecimal newToPaymentApplyAvailable = toPaymentApplyAvailable;
        BigDecimal newPaymentApplyAvailable = paymentApplyAvailable;
        GenericValue paymentApplication = null;
        if (paymentApplicationId == null) {
            paymentApplication = delegator.makeValue("PaymentApplication");
            // prepare for creation
        } else { // retrieve existing paymentApplication
            try {
                paymentApplication = delegator.findByPrimaryKey("PaymentApplication", UtilMisc.toMap("paymentApplicationId", paymentApplicationId));
            } catch (GenericEntityException e) {
                ServiceUtil.returnError(e.getMessage());
            }

            if (paymentApplication == null) {
                errorMessageList.add(UtilProperties.getMessage(resource, "AccountingPaymentApplicationNotFound", UtilMisc.toMap("paymentApplicationId",paymentApplicationId), locale));
                paymentApplicationId = null;
            } else {

                // if both invoiceId and BillingId is entered there was
                // obviously a change
                // only take the newly entered item, same for tax authority and toPayment
                if (paymentApplication.get("invoiceId") == null && invoiceId != null) {
                    billingAccountId = null;
                    taxAuthGeoId = null;
                    toPaymentId = null;
                } else if (paymentApplication.get("toPaymentId") == null && toPaymentId != null) {
                    invoiceId = null;
                    invoiceItemSeqId = null;
                    taxAuthGeoId = null;
                    billingAccountId = null;
                } else if (paymentApplication.get("billingAccountId") == null && billingAccountId != null) {
                    invoiceId = null;
                    invoiceItemSeqId = null;
                    toPaymentId = null;
                    taxAuthGeoId = null;
                } else if (paymentApplication.get("taxAuthGeoId") == null && taxAuthGeoId != null) {
                    invoiceId = null;
                    invoiceItemSeqId = null;
                    toPaymentId = null;
                    billingAccountId = null;
                }

                // check if the payment for too much application if an existing
                // application record is changed
                if (paymentApplyAvailable.compareTo(ZERO) == 0) {
                    newPaymentApplyAvailable = paymentApplyAvailable.add(paymentApplication.getBigDecimal("amountApplied")).subtract(amountApplied).setScale(DECIMALS, ROUNDING);
                } else {
                    newPaymentApplyAvailable = paymentApplyAvailable.add(paymentApplyAvailable).subtract(amountApplied).setScale(DECIMALS, ROUNDING);
                }
                if (newPaymentApplyAvailable.compareTo(ZERO) < 0) {
                    errorMessageList.add(UtilProperties.getMessage(resource, "AccountingPaymentNotEnough", UtilMisc.<String, Object>toMap("paymentId",paymentId,"paymentApplyAvailable",paymentApplyAvailable.add(paymentApplication.getBigDecimal("amountApplied")),"amountApplied",amountApplied),locale));
                }

                if (invoiceId != null) {
                    // only when we are processing an invoice on existing paymentApplication check invoice item for to much application if the invoice
                    // number did not change
                    if (invoiceId.equals(paymentApplication .getString("invoiceId"))) {
                        // check if both the itemNumbers are null then this is a
                        // record for the whole invoice
                        if (invoiceItemSeqId == null && paymentApplication.get("invoiceItemSeqId") == null) {
                            newInvoiceApplyAvailable = invoiceApplyAvailable.add(paymentApplication.getBigDecimal("amountApplied")).subtract(amountApplied).setScale(DECIMALS, ROUNDING);
                            if (invoiceApplyAvailable.compareTo(ZERO) < 0) {
                                errorMessageList.add(UtilProperties.getMessage(resource, "AccountingInvoiceNotEnough",UtilMisc.<String, Object>toMap("tooMuch",newInvoiceApplyAvailable.negate(),"invoiceId",invoiceId),locale));
                            }
                        } else if (invoiceItemSeqId == null && paymentApplication.get("invoiceItemSeqId") != null) {
                            // check if the item number changed from a real Item number to a null value
                            newInvoiceApplyAvailable = invoiceApplyAvailable.add(paymentApplication.getBigDecimal("amountApplied")).subtract(amountApplied).setScale(DECIMALS, ROUNDING);
                            if (invoiceApplyAvailable.compareTo(ZERO) < 0) {
                                errorMessageList.add(UtilProperties.getMessage(resource, "AccountingInvoiceNotEnough",UtilMisc.<String, Object>toMap("tooMuch",newInvoiceApplyAvailable.negate(),"invoiceId",invoiceId),locale));
                            }
                        } else if (invoiceItemSeqId != null && paymentApplication.get("invoiceItemSeqId") == null) {
                            // check if the item number changed from a null value to
                            // a real Item number
                            newInvoiceItemApplyAvailable = invoiceItemApplyAvailable.subtract(amountApplied).setScale(DECIMALS, ROUNDING);
                            if (newInvoiceItemApplyAvailable.compareTo(ZERO) < 0) {
                                errorMessageList.add(UtilProperties.getMessage(resource, "AccountingItemInvoiceNotEnough",UtilMisc.<String, Object>toMap("tooMuch",newInvoiceItemApplyAvailable.negate(),"invoiceId",invoiceId,"invoiceItemSeqId",invoiceItemSeqId),locale));
                            }
                        } else if (invoiceItemSeqId.equals(paymentApplication.getString("invoiceItemSeqId"))) {
                            // check if the real item numbers the same
                            // item number the same numeric value
                            newInvoiceItemApplyAvailable = invoiceItemApplyAvailable.add(paymentApplication.getBigDecimal("amountApplied")).subtract(amountApplied).setScale(DECIMALS, ROUNDING);
                            if (newInvoiceItemApplyAvailable.compareTo(ZERO) < 0) {
                                errorMessageList.add(UtilProperties.getMessage(resource, "AccountingItemInvoiceNotEnough",UtilMisc.<String, Object>toMap("tooMuch",newInvoiceItemApplyAvailable.negate(),"invoiceId",invoiceId,"invoiceItemSeqId",invoiceItemSeqId),locale));
                            }
                        } else {
                            // item number changed only check new item
                            newInvoiceItemApplyAvailable = invoiceItemApplyAvailable.add(amountApplied).setScale(DECIMALS, ROUNDING);
                            if (newInvoiceItemApplyAvailable.compareTo(ZERO) < 0) {
                                errorMessageList.add(UtilProperties.getMessage(resource, "AccountingItemInvoiceNotEnough",UtilMisc.<String, Object>toMap("tooMuch",newInvoiceItemApplyAvailable.negate(),"invoiceId",invoiceId,"invoiceItemSeqId",invoiceItemSeqId),locale));
                            }
                        }

                        // if the amountApplied = 0 give it the higest possible
                        // value
                        if (amountApplied.signum() == 0) {
                            if (newInvoiceItemApplyAvailable.compareTo(newPaymentApplyAvailable) < 0) {
                                amountApplied = newInvoiceItemApplyAvailable;
                                // from the item number
                            } else {
                                amountApplied = newPaymentApplyAvailable;
                                // from the payment
                            }
                        }

                        // check the invoice
                        newInvoiceApplyAvailable = invoiceApplyAvailable.add(paymentApplication.getBigDecimal("amountApplied").subtract(amountApplied)).setScale(DECIMALS, ROUNDING);
                        if (newInvoiceApplyAvailable.compareTo(ZERO) < 0) {
                            errorMessageList.add(UtilProperties.getMessage(resource, "AccountingInvoiceNotEnough",UtilMisc.<String, Object>toMap("tooMuch",invoiceApplyAvailable.add(paymentApplication.getBigDecimal("amountApplied")).subtract(amountApplied),"invoiceId",invoiceId),locale));
                        }
                    }
                }

                // check the toPayment account when only the amountApplied has
                // changed,
                if (toPaymentId != null && toPaymentId.equals(paymentApplication.getString("toPaymentId"))) {
                    newToPaymentApplyAvailable = toPaymentApplyAvailable.subtract(paymentApplication.getBigDecimal("amountApplied")).add(amountApplied).setScale(DECIMALS, ROUNDING);
                    if (newToPaymentApplyAvailable.compareTo(ZERO) < 0) {
                        errorMessageList.add(UtilProperties.getMessage(resource, "AccountingPaymentNotEnough", UtilMisc.<String, Object>toMap("paymentId",toPaymentId,"paymentApplyAvailable",newToPaymentApplyAvailable,"amountApplied",amountApplied),locale));
                    }
                } else if (toPaymentId != null) {
                    // billing account entered number has changed so we have to
                    // check the new billing account number.
                    newToPaymentApplyAvailable = toPaymentApplyAvailable.add(amountApplied).setScale(DECIMALS, ROUNDING);
                    if (newToPaymentApplyAvailable.compareTo(ZERO) < 0) {
                        errorMessageList.add(UtilProperties.getMessage(resource, "AccountingPaymentNotEnough", UtilMisc.<String, Object>toMap("paymentId",toPaymentId,"paymentApplyAvailable",newToPaymentApplyAvailable,"amountApplied",amountApplied),locale));
                    }

                }
            }
            if (debug) Debug.logInfo("paymentApplication record info retrieved and checked...", module);
        }

        // show the maximumus what can be added in the payment application file.
        String toMessage = null;  // prepare for success message
        if (debug) {
            String extra = "";
            if (invoiceItemSeqId != null) {
                extra = " Invoice item(" + invoiceItemSeqId + ") amount not yet applied: " + newInvoiceItemApplyAvailable;
            }
            Debug.logInfo("checking finished, start processing with the following data... ", module);
            if (invoiceId != null) {
                Debug.logInfo(" Invoice(" + invoiceId + ") amount not yet applied: " + newInvoiceApplyAvailable + extra + " Payment(" + paymentId + ") amount not yet applied: " + newPaymentApplyAvailable +  " Requested amount to apply:" + amountApplied, module);
                toMessage = UtilProperties.getMessage(resource, "AccountingApplicationToInvoice",UtilMisc.toMap("invoiceId",invoiceId),locale);
                if (extra.length() > 0) toMessage = UtilProperties.getMessage(resource, "AccountingApplicationToInvoiceItem",UtilMisc.toMap("invoiceId",invoiceId,"invoiceItemSeqId",invoiceItemSeqId),locale);
            }
            if (toPaymentId != null) {
                Debug.logInfo(" toPayment(" + toPaymentId + ") amount not yet applied: " + newToPaymentApplyAvailable + " Payment(" + paymentId + ") amount not yet applied: " + newPaymentApplyAvailable + " Requested amount to apply:" + amountApplied, module);
                toMessage = UtilProperties.getMessage(resource, "AccountingApplicationToPayment",UtilMisc.toMap("paymentId",toPaymentId),locale);
            }
            if (taxAuthGeoId != null) {
                Debug.logInfo(" taxAuthGeoId(" + taxAuthGeoId + ")  Payment(" + paymentId + ") amount not yet applied: " + newPaymentApplyAvailable + " Requested amount to apply:" + amountApplied, module);
                toMessage = UtilProperties.getMessage(resource, "AccountingApplicationToTax",UtilMisc.toMap("taxAuthGeoId",taxAuthGeoId),locale);
            }
        }
        // if the amount to apply was not provided or was zero fill it with the maximum possible and provide information to the user
        if (amountApplied.signum() == 0 &&  useHighestAmount.equals("Y")) {
            amountApplied = newPaymentApplyAvailable;
            if (invoiceId != null && newInvoiceApplyAvailable.compareTo(amountApplied) < 0) {
                amountApplied = newInvoiceApplyAvailable;
                toMessage = UtilProperties.getMessage(resource, "AccountingApplicationToInvoice",UtilMisc.toMap("invoiceId",invoiceId),locale);
            }
            if (toPaymentId != null && newToPaymentApplyAvailable.compareTo(amountApplied) < 0) {
                amountApplied = newToPaymentApplyAvailable;
                toMessage = UtilProperties.getMessage(resource, "AccountingApplicationToPayment",UtilMisc.toMap("paymentId",toPaymentId),locale);
            }
        }

        String successMessage = null;
        if (amountApplied.signum() == 0) {
            errorMessageList.add(UtilProperties.getMessage(resource, "AccountingNoAmount",locale));
        } else {
            successMessage = UtilProperties.getMessage(resource, "AccountingApplicationSuccess",UtilMisc.<String, Object>toMap("amountApplied",amountApplied,"paymentId",paymentId,"isoCode", currencyUomId, "toMessage", toMessage),locale);
        }
        // report error messages if any
        if (errorMessageList.size() > 0) {
            return ServiceUtil.returnError(errorMessageList);
        }

        // ============ start processing ======================
           // if the application is specified it is easy, update the existing record only
        if (paymentApplicationId != null) {
            // record is already retrieved previously
            if (debug) Debug.logInfo("Process an existing paymentApplication record: " + paymentApplicationId, module);
            // update the current record
            paymentApplication.set("invoiceId", invoiceId);
            paymentApplication.set("invoiceItemSeqId", invoiceItemSeqId);
            paymentApplication.set("paymentId", paymentId);
            paymentApplication.set("toPaymentId", toPaymentId);
            paymentApplication.set("amountApplied", amountApplied);
            paymentApplication.set("billingAccountId", billingAccountId);
            paymentApplication.set("taxAuthGeoId", taxAuthGeoId);
            return storePaymentApplication(delegator, paymentApplication,locale);
        }

        // if no invoice sequence number is provided it assumed the requested paymentAmount will be
        // spread over the invoice starting with the lowest sequence number if
        // itemprocessing is on otherwise create one record
        if (invoiceId != null && paymentId != null && (invoiceItemSeqId == null)) {
            if (invoiceProcessing) {
                // create only a single record with a null seqId
                if (debug) Debug.logInfo("Try to allocate the payment to the invoice as a whole", module);
                paymentApplication.set("paymentId", paymentId);
                paymentApplication.set("toPaymentId",null);
                paymentApplication.set("invoiceId", invoiceId);
                paymentApplication.set("invoiceItemSeqId", null);
                paymentApplication.set("toPaymentId", null);
                paymentApplication.set("amountApplied", amountApplied);
                paymentApplication.set("billingAccountId", billingAccountId);
                paymentApplication.set("taxAuthGeoId", null);
                if (debug) Debug.logInfo("creating new paymentapplication", module);
                return storePaymentApplication(delegator, paymentApplication,locale);
            } else { // spread the amount over every single item number
                if (debug) Debug.logInfo("Try to allocate the payment to the itemnumbers of the invoice", module);
                // get the invoice items
                List<GenericValue> invoiceItems = null;
                try {
                    invoiceItems = delegator.findByAnd("InvoiceItem", UtilMisc.toMap("invoiceId", invoiceId));
                } catch (GenericEntityException e) {
                    return ServiceUtil.returnError(e.getMessage());
                }
                if (invoiceItems.size() == 0) {
                    errorMessageList.add("No invoice items found for invoice " + invoiceId + " to match payment against...\n");
                    return ServiceUtil.returnError(errorMessageList);
                } else { // we found some invoice items, start processing....
                    // check if the user want to apply a smaller amount than the maximum possible on the payment
                    if (amountApplied.signum() != 0 && amountApplied.compareTo(paymentApplyAvailable) < 0)    {
                        paymentApplyAvailable = amountApplied;
                    }
                    for (GenericValue currentInvoiceItem : invoiceItems) {
                        if (paymentApplyAvailable.compareTo(ZERO) > 0) {
                            break;
                        }
                        if (debug) Debug.logInfo("Start processing item: " + currentInvoiceItem.getString("invoiceItemSeqId"), module);
                        BigDecimal itemQuantity = BigDecimal.ONE;
                        if (currentInvoiceItem.get("quantity") != null && currentInvoiceItem.getBigDecimal("quantity").signum() != 0) {
                            itemQuantity = new BigDecimal(currentInvoiceItem.getString("quantity")).setScale(DECIMALS,ROUNDING);
                        }
                        BigDecimal itemAmount = currentInvoiceItem.getBigDecimal("amount").setScale(DECIMALS,ROUNDING);
                        BigDecimal itemTotal = itemAmount.multiply(itemQuantity).setScale(DECIMALS,ROUNDING);

                        // get the application(s) already allocated to this
                        // item, if available
                        List<GenericValue> paymentApplications = null;
                        try {
                            paymentApplications = currentInvoiceItem.getRelated("PaymentApplication");
                        } catch (GenericEntityException e) {
                            ServiceUtil.returnError(e.getMessage());
                        }
                        BigDecimal tobeApplied = ZERO;
                        // item total amount - already applied (if any)
                        BigDecimal alreadyApplied = ZERO;
                        if (UtilValidate.isNotEmpty(paymentApplications)) {
                            // application(s) found, add them all together
                            Iterator<GenericValue> p = paymentApplications.iterator();
                            while (p.hasNext()) {
                                paymentApplication = p.next();
                                alreadyApplied = alreadyApplied.add(paymentApplication.getBigDecimal("amountApplied").setScale(DECIMALS,ROUNDING));
                            }
                            tobeApplied = itemTotal.subtract(alreadyApplied).setScale(DECIMALS,ROUNDING);
                        } else {
                            // no application connected yet
                            tobeApplied = itemTotal;
                        }
                        if (debug) Debug.logInfo("tobeApplied:(" + tobeApplied + ") = " + "itemTotal(" + itemTotal + ") - alreadyApplied(" + alreadyApplied + ") but not more then (nonapplied) paymentAmount(" + paymentApplyAvailable + ")", module);

                        if (tobeApplied.signum() == 0) {
                            // invoiceItem already fully applied so look at the next one....
                            continue;
                        }

                        if (paymentApplyAvailable.compareTo(tobeApplied) > 0) {
                            paymentApplyAvailable = paymentApplyAvailable.subtract(tobeApplied);
                        } else {
                            tobeApplied = paymentApplyAvailable;
                            paymentApplyAvailable = ZERO;
                        }

                        // create application payment record but check currency
                        // first if supplied
                        if (invoice.get("currencyUomId") != null && currencyUomId != null && !invoice.getString("currencyUomId").equals(currencyUomId)) {
                            errorMessageList.add("Payment currency (" + currencyUomId + ") and invoice currency(" + invoice.getString("currencyUomId") + ") not the same\n");
                        } else {
                            paymentApplication.set("paymentApplicationId", null);
                            // make sure we get a new record
                            paymentApplication.set("invoiceId", invoiceId);
                            paymentApplication.set("invoiceItemSeqId", currentInvoiceItem.getString("invoiceItemSeqId"));
                            paymentApplication.set("paymentId", paymentId);
                            paymentApplication.set("toPaymentId", toPaymentId);
                            paymentApplication.set("amountApplied", tobeApplied);
                            paymentApplication.set("billingAccountId", billingAccountId);
                            paymentApplication.set("taxAuthGeoId", taxAuthGeoId);
                            storePaymentApplication(delegator, paymentApplication,locale);
                        }

                        // check if either the invoice or the payment is fully
                        // applied, when yes change the status to paid
                        // which triggers the ledger routines....
                        /*
                         * if
                         * (InvoiceWorker.getInvoiceTotal(invoice).equals(InvoiceWorker.getInvoiceApplied(invoice))) {
                         * try { dispatcher.runSync("setInvoiceStatus",
                         * UtilMisc.toMap("invoiceId",invoiceId,"statusId","INVOICE_PAID")); }
                         * catch (GenericServiceException e1) {
                         * Debug.logError(e1, "Error updating invoice status",
                         * module); } }
                         *
                         * if
                         * (payment.getBigDecimal("amount").equals(PaymentWorker.getPaymentApplied(payment))) {
                         * GenericValue appliedPayment = (GenericValue)
                         * delegator.makeValue("Payment",
                         * UtilMisc.toMap("paymentId",paymentId,"statusId","INVOICE_PAID"));
                         * try { appliedPayment.store(); } catch
                         * (GenericEntityException e) {
                         * ServiceUtil.returnError(e.getMessage()); } }
                         */
                    }

                    if (errorMessageList.size() > 0) {
                        return ServiceUtil.returnError(errorMessageList);
                    } else {
                        if (successMessage != null) {
                            return ServiceUtil.returnSuccess(successMessage);
                        }
                        else {
                            return ServiceUtil.returnSuccess();
                        }
                    }
                }
            }
        }

        // if no paymentApplicationId supplied create a new record with the data
        // supplied...
        if (paymentApplicationId == null && amountApplied != null) {
            paymentApplication.set("paymentApplicationId", paymentApplicationId);
            paymentApplication.set("invoiceId", invoiceId);
            paymentApplication.set("invoiceItemSeqId", invoiceItemSeqId);
            paymentApplication.set("paymentId", paymentId);
            paymentApplication.set("toPaymentId", toPaymentId);
            paymentApplication.set("amountApplied", amountApplied);
            paymentApplication.set("billingAccountId", billingAccountId);
            paymentApplication.set("taxAuthGeoId", taxAuthGeoId);
            return storePaymentApplication(delegator, paymentApplication,locale);
        }

        // should never come here...
        errorMessageList.add("??unsuitable parameters passed...?? This message.... should never be shown\n");
        errorMessageList.add("--Input parameters...InvoiceId:" + invoiceId + " invoiceItemSeqId:" + invoiceItemSeqId + " PaymentId:" + paymentId + " toPaymentId:" + toPaymentId + "\n  paymentApplicationId:" + paymentApplicationId + " amountApplied:" + amountApplied);
        return ServiceUtil.returnError(errorMessageList);
    }

    public static Map<String, Object> calculateInvoicedAdjustmentTotal(DispatchContext dctx, Map<String, Object> context) {
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        GenericValue orderAdjustment = (GenericValue) context.get("orderAdjustment");
        Map<String, Object> result = ServiceUtil.returnSuccess();

        BigDecimal invoicedTotal = ZERO;
        BigDecimal invoicedQty = ZERO;
        List<GenericValue> invoicedAdjustments = null;
        try {
           // invoicedAdjustments = delegator.findByAnd("OrderAdjustmentBilling", UtilMisc.toMap("orderAdjustmentId", orderAdjustment.getString("orderAdjustmentId")));
            invoicedAdjustments = delegator.findByAnd("OrderAdjustmentBillingAndInvoice", UtilMisc.toMap("orderAdjustmentId", orderAdjustment.getString("orderAdjustmentId")));
            //To skipping any adjustment in canceled status
            invoicedAdjustments = EntityUtil.filterByCondition(invoicedAdjustments, EntityCondition.makeCondition("invoiceStatusId", EntityOperator.NOT_EQUAL, "INVOICE_CANCELLED"));
        } catch (GenericEntityException e) {
            String errMsg = UtilProperties.getMessage(resource, "AccountingTroubleCallingCalculateInvoicedAdjustmentTotalService" + ": " + e.getMessage(), locale);
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        }
        for (GenericValue invoicedAdjustment : invoicedAdjustments) {
        	if(UtilValidate.isNotEmpty(invoicedAdjustment.getBigDecimal("amount"))){
        		invoicedTotal = invoicedTotal.add(invoicedAdjustment.getBigDecimal("amount").setScale(DECIMALS, ROUNDING));
        	}
        	if(UtilValidate.isNotEmpty(invoicedAdjustment.getBigDecimal("quantity"))){
        		invoicedQty = invoicedQty.add(invoicedAdjustment.getBigDecimal("quantity").setScale(DECIMALS, ROUNDING));
        	}
        }
        result.put("invoicedTotal", invoicedTotal);
        result.put("invoicedQty", invoicedQty);
        return result;
    }
    
    public static Map<String, Object> createInvoiceItemAndTax(DispatchContext dctx, Map<String, Object> context) {
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");
        String invoiceId = (String)context.get("invoiceId");
        BigDecimal cstPercent = (BigDecimal)context.get("cstPercent");
        BigDecimal bedPercent = (BigDecimal)context.get("bedPercent");
        BigDecimal vatPercent = (BigDecimal)context.get("vatPercent");
        BigDecimal vatAmountVal=(BigDecimal)context.get("vatAmount");
        BigDecimal cstAmount = (BigDecimal)context.get("cstAmount");
        BigDecimal tdsAmount = (BigDecimal)context.get("tdsAmount");
        BigDecimal servPercent = (BigDecimal)context.get("serviceTaxPercent");
        BigDecimal servAmount=(BigDecimal)context.get("serviceTaxAmount");
        BigDecimal bedAmount = (BigDecimal)context.get("bedAmount");
        String vatType = (String)context.get("vatType");
        String cstType = (String)context.get("cstType");
        String tdsType=(String)context.get("tdsType");
        String serviceTaxType = (String)context.get("serviceTaxType");
        String bedTaxType = (String)context.get("bedTaxType");
        //
        BigDecimal amount = (BigDecimal)context.get("amount");
        String productId = (String)context.get("productId");
        BigDecimal quantity = (BigDecimal)context.get("quantity");
        String costCenterId=(String)context.get("costCenterId");
        
        if(UtilValidate.isEmpty(amount)){
        	return ServiceUtil.returnError("Amount cannot be empty or ZERO");
        }
        // similarly, tax only for purchase invoices
        try{
        	Map<String, Object> createInvoiceItemResult = dispatcher.runSync("createInvoiceItem", context);
            if (ServiceUtil.isError(createInvoiceItemResult)) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceItemFromOrder",locale), null, null, createInvoiceItemResult);
            }
            String parentInvoiceItemSeq=(String)createInvoiceItemResult.get("invoiceItemSeqId"); 
            BigDecimal totalAmt = BigDecimal.ZERO;
    		if(UtilValidate.isNotEmpty(quantity) && quantity.compareTo(BigDecimal.ZERO)>0){
    			totalAmt = amount.multiply(quantity);
    		}
    		else{
    			totalAmt = amount;
    		}
    		GenericValue invoice = delegator.findOne("Invoice", UtilMisc.toMap("invoiceId", invoiceId), false);
    		String invoiceTypeId = invoice.getString("invoiceTypeId");
    		List<GenericValue> invoiceTypeMap = delegator.findList("InvoiceItemTypeMap", EntityCondition.makeCondition("invoiceTypeId", EntityOperator.EQUALS, invoiceTypeId), null, null, null, false);
    		List<GenericValue> invoiceItems = delegator.findList("InvoiceItem", EntityCondition.makeCondition("invoiceId", EntityOperator.EQUALS, invoiceId), null, null, null, false);
        	if(UtilValidate.isNotEmpty(bedPercent) && bedPercent.compareTo(BigDecimal.ZERO)>0){
        		List<GenericValue> bedItemTypes = EntityUtil.filterByCondition(invoiceTypeMap, EntityCondition.makeCondition("invoiceItemTypeId", EntityOperator.LIKE, "BED_%"));
        		if(UtilValidate.isNotEmpty(bedItemTypes)){
        			String invoiceTaxItemType = (EntityUtil.getFirst(bedItemTypes)).getString("invoiceItemTypeId");
        			if(UtilValidate.isNotEmpty(bedTaxType)){
        				invoiceTaxItemType=bedTaxType;
        			}
        			List<GenericValue> bedInvItems = EntityUtil.filterByCondition(invoiceItems, EntityCondition.makeCondition(EntityCondition.makeCondition("invoiceItemTypeId", EntityOperator.EQUALS, invoiceTaxItemType),EntityOperator.AND,EntityCondition.makeCondition("parentInvoiceItemSeqId", EntityOperator.EQUALS, parentInvoiceItemSeq)));
            		BigDecimal bedItemAmt = BigDecimal.ZERO;
            		bedItemAmt = (totalAmt.multiply(bedPercent)).divide(new BigDecimal(100), TAX_DECIMALS, TAX_ROUNDING);
            		if(UtilValidate.isNotEmpty(bedAmount)){
            			bedItemAmt=bedAmount;
            		}
            		//totalAmt = totalAmt.add(bedItemAmt);
            		if(UtilValidate.isEmpty(bedInvItems)){
            			Map<String, Object> createTaxItemContext = FastMap.newInstance();
            			createTaxItemContext.put("invoiceId", invoiceId);
            			createTaxItemContext.put("invoiceItemTypeId", invoiceTaxItemType);
            			createTaxItemContext.put("amount", bedItemAmt);
            			createTaxItemContext.put("parentInvoiceItemSeq", parentInvoiceItemSeq);
            			createTaxItemContext.put("quantity", BigDecimal.ONE);
            			createTaxItemContext.put("userLogin", userLogin);
            			if(UtilValidate.isNotEmpty(costCenterId)){
            				createTaxItemContext.put("costCenterId", costCenterId);
            			}
                        createInvoiceItemResult = dispatcher.runSync("createInvoiceItem", createTaxItemContext);
            		}
            		else{
            			GenericValue bedInvItem = EntityUtil.getFirst(bedInvItems);
            			BigDecimal extAmt = bedInvItem.getBigDecimal("amount");
            			BigDecimal totalBEDAmt = bedItemAmt.add(extAmt);
            			bedInvItem.set("amount", totalBEDAmt);
            			if(UtilValidate.isNotEmpty(costCenterId)){
            				bedInvItem.put("costCenterId", costCenterId);
            			}
            			bedInvItem.store();
            		}
        		}
        	}
        	
        	if(UtilValidate.isNotEmpty(cstPercent) && cstPercent.compareTo(BigDecimal.ZERO)>0){
        		
        		List<GenericValue> cstItemTypes = EntityUtil.filterByCondition(invoiceTypeMap, EntityCondition.makeCondition("invoiceItemTypeId", EntityOperator.LIKE, "CST_%"));
        		if(UtilValidate.isNotEmpty(cstItemTypes)){
        			String invoiceTaxItemType = (EntityUtil.getFirst(cstItemTypes)).getString("invoiceItemTypeId");
        			if(UtilValidate.isNotEmpty(cstType)){
        				invoiceTaxItemType=cstType;
        			}
        			List<GenericValue> cstInvItems = EntityUtil.filterByCondition(invoiceItems, EntityCondition.makeCondition(EntityCondition.makeCondition("invoiceItemTypeId", EntityOperator.EQUALS, invoiceTaxItemType),EntityOperator.AND,EntityCondition.makeCondition("parentInvoiceItemSeqId", EntityOperator.EQUALS, parentInvoiceItemSeq)));
        			BigDecimal cstItemAmt = BigDecimal.ZERO;
        		
        			cstItemAmt = (totalAmt.multiply(cstPercent)).divide(new BigDecimal(100), TAX_DECIMALS, TAX_ROUNDING);
        			if(UtilValidate.isEmpty(cstInvItems)){
        			
	        			Map<String, Object> createTaxItemContext = FastMap.newInstance();
	        			createTaxItemContext.put("invoiceId", invoiceId);
	        			createTaxItemContext.put("invoiceItemTypeId", invoiceTaxItemType);
	        			createTaxItemContext.put("amount", cstItemAmt);
	        			createTaxItemContext.put("parentInvoiceItemSeq", parentInvoiceItemSeq);
	        			createTaxItemContext.put("quantity", BigDecimal.ONE);
	        			createTaxItemContext.put("userLogin", userLogin);
	        			if(UtilValidate.isNotEmpty(costCenterId)){
	        				createTaxItemContext.put("costCenterId", costCenterId);
            			}
	                    createInvoiceItemResult = dispatcher.runSync("createInvoiceItem", createTaxItemContext);
	        		}
	        		else{
	        			GenericValue cstInvItem = EntityUtil.getFirst(cstInvItems);
	        			BigDecimal extAmt = cstInvItem.getBigDecimal("amount");
	        			BigDecimal totalCSTAmt = cstItemAmt.add(extAmt);
	        			cstInvItem.set("amount", totalCSTAmt);
	        			if(UtilValidate.isNotEmpty(costCenterId)){
	        				cstInvItem.put("costCenterId", costCenterId);
            			}
	        			cstInvItem.store();
	        		}
        		}
        	}
        	if(UtilValidate.isNotEmpty(tdsAmount)){
        		List<GenericValue> tdsItemTypes = EntityUtil.filterByCondition(invoiceTypeMap, EntityCondition.makeCondition("invoiceItemTypeId", EntityOperator.LIKE, "TDS_%"));
        		if(UtilValidate.isNotEmpty(tdsItemTypes)){
        			String invoiceTaxItemType = (EntityUtil.getFirst(tdsItemTypes)).getString("invoiceItemTypeId");
        			if(UtilValidate.isNotEmpty(tdsType)){
        				invoiceTaxItemType=tdsType;
        			}
        			List<GenericValue> tdsInvItems = EntityUtil.filterByCondition(invoiceItems, EntityCondition.makeCondition(EntityCondition.makeCondition("invoiceItemTypeId", EntityOperator.EQUALS, invoiceTaxItemType),EntityOperator.AND,EntityCondition.makeCondition("parentInvoiceItemSeqId", EntityOperator.EQUALS, parentInvoiceItemSeq)));
        			BigDecimal tdsItemAmt = BigDecimal.ZERO;
        			//tdsItemAmt = (totalAmt.multiply(cstPercent)).divide(new BigDecimal(100), TAX_DECIMALS, TAX_ROUNDING);
        			if(UtilValidate.isNotEmpty(tdsAmount)){
        				tdsItemAmt=tdsAmount;
        			}
        			if(UtilValidate.isEmpty(tdsInvItems)){
        			
	        			Map<String, Object> createTaxItemContext = FastMap.newInstance();
	        			createTaxItemContext.put("invoiceId", invoiceId);
	        			createTaxItemContext.put("invoiceItemTypeId", invoiceTaxItemType);
	        			createTaxItemContext.put("parentInvoiceItemSeq", parentInvoiceItemSeq);
	        			createTaxItemContext.put("amount", tdsItemAmt);
	        			createTaxItemContext.put("quantity", BigDecimal.ONE);
	        			createTaxItemContext.put("userLogin", userLogin);
	        			if(UtilValidate.isNotEmpty(costCenterId)){
	        				createTaxItemContext.put("costCenterId", costCenterId);
            			}
	                    createInvoiceItemResult = dispatcher.runSync("createInvoiceItem", createTaxItemContext);
	        		}
	        		else{
	        			GenericValue tdsInvItem = EntityUtil.getFirst(tdsInvItems);
	        			BigDecimal extAmt = tdsInvItem.getBigDecimal("amount");
	        			BigDecimal totalTDSAmt = tdsItemAmt.add(extAmt);
	        			tdsInvItem.set("amount", totalTDSAmt);
	        			if(UtilValidate.isNotEmpty(costCenterId)){
	        				tdsInvItem.put("costCenterId", costCenterId);
            			}
	        			tdsInvItem.store();
	        		}
        		}
        	}
        	if(UtilValidate.isNotEmpty(vatPercent) && vatPercent.compareTo(BigDecimal.ZERO)>0){
        		
        		List<GenericValue> vatItemTypes = EntityUtil.filterByCondition(invoiceTypeMap, EntityCondition.makeCondition("invoiceItemTypeId", EntityOperator.LIKE, "VAT_%"));
        		if(UtilValidate.isNotEmpty(vatItemTypes)){
        			String invoiceTaxItemType = (EntityUtil.getFirst(vatItemTypes)).getString("invoiceItemTypeId");
        			if(UtilValidate.isNotEmpty(vatType)){
        				invoiceTaxItemType=vatType;
        			}
	        		List<GenericValue> vatInvItems = EntityUtil.filterByCondition(invoiceItems, EntityCondition.makeCondition(EntityCondition.makeCondition("invoiceItemTypeId", EntityOperator.EQUALS, invoiceTaxItemType),EntityOperator.AND,EntityCondition.makeCondition("parentInvoiceItemSeqId", EntityOperator.EQUALS, parentInvoiceItemSeq)));
	        		BigDecimal vatItemAmt = BigDecimal.ZERO;
	    			vatItemAmt = (totalAmt.multiply(vatPercent)).divide(new BigDecimal(100), TAX_DECIMALS, TAX_ROUNDING);
	        		if(UtilValidate.isEmpty(vatInvItems)){
	        			
	        			Map<String, Object> createTaxItemContext = FastMap.newInstance();
	        			createTaxItemContext.put("invoiceId", invoiceId);
	        			createTaxItemContext.put("invoiceItemTypeId", invoiceTaxItemType);
	        			createTaxItemContext.put("parentInvoiceItemSeq", parentInvoiceItemSeq);
	        			createTaxItemContext.put("amount", vatItemAmt);
	        			createTaxItemContext.put("quantity", BigDecimal.ONE);
	        			createTaxItemContext.put("parentInvoiceItemSeq", parentInvoiceItemSeq);
	        			createTaxItemContext.put("quantity", BigDecimal.ONE);
	        			createTaxItemContext.put("userLogin", userLogin);
	        			if(UtilValidate.isNotEmpty(costCenterId)){
	        				createTaxItemContext.put("costCenterId", costCenterId);
            			}
	                    createInvoiceItemResult = dispatcher.runSync("createInvoiceItem", createTaxItemContext);
	        		}
	        		else{
	        			GenericValue vatInvItem = EntityUtil.getFirst(vatInvItems);
	        			BigDecimal extAmt = vatInvItem.getBigDecimal("amount");
	        			BigDecimal totalVATAmt = vatItemAmt.add(extAmt);
	        			vatInvItem.set("amount", totalVATAmt);
	        			if(UtilValidate.isNotEmpty(costCenterId)){
	        				vatInvItem.put("costCenterId", costCenterId);
            			}
	        			vatInvItem.store();
	        		}
        		}
        	}
        	
        	if(UtilValidate.isNotEmpty(servPercent) && servPercent.compareTo(BigDecimal.ZERO)>0){
        		
        		List<GenericValue> servItemTypes = EntityUtil.filterByCondition(invoiceTypeMap, EntityCondition.makeCondition("invoiceItemTypeId", EntityOperator.LIKE, "SERTAX_%"));
        		if(UtilValidate.isNotEmpty(servItemTypes)){
        			String invoiceTaxItemType = (EntityUtil.getFirst(servItemTypes)).getString("invoiceItemTypeId");
        			if(UtilValidate.isNotEmpty(serviceTaxType)){
        				invoiceTaxItemType=serviceTaxType;
        			}
        			List<GenericValue> servTaxInvItems = EntityUtil.filterByCondition(invoiceItems, EntityCondition.makeCondition(EntityCondition.makeCondition("invoiceItemTypeId", EntityOperator.EQUALS, invoiceTaxItemType),EntityOperator.AND,EntityCondition.makeCondition("parentInvoiceItemSeqId", EntityOperator.EQUALS, parentInvoiceItemSeq)));
	        		BigDecimal servTaxItemAmt = BigDecimal.ZERO;
	        		servTaxItemAmt = (totalAmt.multiply(servPercent)).divide(new BigDecimal(100), TAX_DECIMALS, TAX_ROUNDING);
	    			
	    			if(UtilValidate.isNotEmpty(servAmount)){
	    				servTaxItemAmt=servAmount;
	    			}
	        		if(UtilValidate.isEmpty(servTaxInvItems)){
	        			Map<String, Object> createTaxItemContext = FastMap.newInstance();
	        			createTaxItemContext.put("invoiceId", invoiceId);
	        			createTaxItemContext.put("invoiceItemTypeId", invoiceTaxItemType);
	        			createTaxItemContext.put("parentInvoiceItemSeq", parentInvoiceItemSeq);
	        			createTaxItemContext.put("amount", servTaxItemAmt);
	        			createTaxItemContext.put("quantity", BigDecimal.ONE);
	        			createTaxItemContext.put("userLogin", userLogin);
	        			if(UtilValidate.isNotEmpty(costCenterId)){
	        				createTaxItemContext.put("costCenterId", costCenterId);
            			}
	                    createInvoiceItemResult = dispatcher.runSync("createInvoiceItem", createTaxItemContext);
	        		}
	        		else{
	        			GenericValue servTaxInvItem = EntityUtil.getFirst(servTaxInvItems);
	        			BigDecimal extAmt = servTaxInvItem.getBigDecimal("amount");
	        			BigDecimal totalSerTaxAmt = servTaxItemAmt.add(extAmt);
	        			servTaxInvItem.set("amount", totalSerTaxAmt);
	        			if(UtilValidate.isNotEmpty(costCenterId)){
	        				servTaxInvItem.put("costCenterId", costCenterId);
            			}
	        			servTaxInvItem.store();
	        		}
        		}
        	}
        	// let's handle invoice rounding here
	        try{   
	        	    Map roundAdjCtx = UtilMisc.toMap("userLogin",userLogin);	  	
	        	    roundAdjCtx.put("invoiceId", invoiceId);
		  	 		Map roundingResult = dispatcher.runSync("adjustRoundingDiffForInvoice",roundAdjCtx);
		  	 		if (ServiceUtil.isError(roundingResult)) {
		  	 			String errMsg =  ServiceUtil.getErrorMessage(roundingResult);
		  	 			Debug.logError(errMsg , module);
	  	      		  	return ServiceUtil.returnError(errMsg+"==Error While  Rounding Invoice !");
		  	 		}
		  	 	}catch (Exception e) {
		  			  Debug.logError(e, "Error while Creating Rounding Diff For Invoice", module);
		              return ServiceUtil.returnError(e+"==Error While  Rounding Invoice !");
		  	 }
	        
        	
        }catch(GenericEntityException e){
        	Debug.logError(e, e.getMessage(), module);
            return ServiceUtil.returnError(e.getMessage());
        }
        catch(GenericServiceException e){
        	Debug.logError(e, e.getMessage(), module);
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }
    public static Map<String, Object> adjustRoundingDiffForInvoice(DispatchContext dctx, Map<String, ? extends Object> context){
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String invoiceId = (String) context.get("invoiceId");
        Locale locale = (Locale) context.get("locale");     
        Map result = ServiceUtil.returnSuccess();
        BigDecimal roundingAmount = BigDecimal.ZERO;
		try {
			GenericValue invoice = delegator.findOne("Invoice", UtilMisc.toMap("invoiceId", invoiceId), false);
			    BigDecimal invoiceTotal = ZERO;
		        List<GenericValue> invoiceItems = null;
		        try {
		            invoiceItems = invoice.getRelated("InvoiceItem");
		            invoiceItems = EntityUtil.filterByAnd(
		                    invoiceItems, UtilMisc.toList(
		                            EntityCondition.makeCondition("invoiceItemTypeId", EntityOperator.NOT_IN, UtilMisc.toList("ROUNDING_ADJUSTMENT"))
		                          ));
		        } catch (GenericEntityException e) {
		            Debug.logError(e, "Trouble getting InvoiceItem list", module);
		        }
		        if (invoiceItems != null) {
		            for (GenericValue invoiceItem : invoiceItems) {
		                invoiceTotal = invoiceTotal.add(InvoiceWorker.getInvoiceItemTotal(invoiceItem)).setScale(DECIMALS,ROUNDING);
		            }
		        }
			roundingAmount = (invoiceTotal.setScale(0, ROUNDING)).subtract(invoiceTotal);
			// add rounding  adjustment "ROUNDING_ADJUSTMENT"
			if(!(roundingAmount.compareTo(BigDecimal.ZERO) == 0)){
				//finding rounding adjustmentItem if any then reset with new roundingamount
				List<GenericValue> roundingInvoiceItems = invoice.getRelated("InvoiceItem");
				roundingInvoiceItems = EntityUtil.filterByAnd(
						roundingInvoiceItems, UtilMisc.toList(
		                            EntityCondition.makeCondition("invoiceItemTypeId", EntityOperator.EQUALS,"ROUNDING_ADJUSTMENT")
		                          ));
		            if(UtilValidate.isEmpty(roundingInvoiceItems)){
		            	Map invoiceItemCtx = FastMap.newInstance();
		            	invoiceItemCtx.put("invoiceId", invoiceId);
						invoiceItemCtx.put("amount", roundingAmount);
						invoiceItemCtx.put("invoiceItemTypeId", "ROUNDING_ADJUSTMENT");
						invoiceItemCtx.put("quantity", BigDecimal.ONE);
						invoiceItemCtx.put("userLogin", userLogin);
						result = dispatcher.runSync("createInvoiceItem", invoiceItemCtx);
						String invItemSeqId = (String) result.get("invoiceItemSeqId");
						if (ServiceUtil.isError(result)) {
							Debug.logError("Error creating ROUNDING ADJUSTMENT item for Invoice : ", module);	
							return ServiceUtil.returnError("Error creating ROUNDING ADJUSTMENT item for Invoice : ");
						}
	        		}else{
	        			GenericValue roundigInvItem = EntityUtil.getFirst(roundingInvoiceItems);
	        			roundigInvItem.set("amount", roundingAmount);
	        			roundigInvItem.store();
	        		}
			}//rounding creation end
		} catch (Exception e) {
			Debug.logError(e, module);
			return ServiceUtil.returnError(e.toString());
		}
        result = ServiceUtil.returnSuccess("Successfully Created Adjustment For Rounding!!");
        result.put("invoiceId", invoiceId);
        return result;
    }
    /*public static Map<String, Object> updateInvoiceItemAndTax(DispatchContext dctx, Map<String, Object> context) {
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");
        String invoiceId = (String)context.get("invoiceId");
        String invoiceItemSeqId = (String)context.get("invoiceItemSeqId");

        BigDecimal cstCurrPercent = BigDecimal.ZERO;
    	BigDecimal vatCurrPercent = BigDecimal.ZERO;
    	BigDecimal bedCurrPercent = BigDecimal.ZERO;
        BigDecimal currQuantity = BigDecimal.ZERO;
        BigDecimal currAmount = BigDecimal.ZERO;
        BigDecimal currTotalAmt = BigDecimal.ZERO;
        currTotalAmt = currAmount;
    	// similarly, tax only for purchase invoices
    	if(UtilValidate.isNotEmpty(context.get("cstPercent"))){
    		cstCurrPercent = (BigDecimal)context.get("cstPercent");
    	}
    	if(UtilValidate.isNotEmpty(context.get("vatPercent"))){
    		varCurrPercent = (BigDecimal)context.get("cstPercent");
    	}
    	if(UtilValidate.isNotEmpty(context.get("bedPercent"))){
    		bedCurrPercent = (BigDecimal)context.get("bedPercent");
    	}
    	if(UtilValidate.isNotEmpty(context.get("quantity"))){
    		currQuantity = (BigDecimal)context.get("quantity");
    		currTotalAmt = currAmount.multiply(currQuantity);
    	}
        try{
        	
        	Map<String, Object> updateInvoiceItemResult = dispatcher.runSync("updateInvoiceItem", context);
            if (ServiceUtil.isError(updateInvoiceItemResult)) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorUpdatingInvoiceItemFromOrder",locale), null, null, createInvoiceItemResult);
            }
            List<GenericValue> invoiceItems = delegator.findList("InvoiceItem", EntityCondition.makeCondition("invoiceId", EntityOperator.EQUALS, invoiceId), null, null, null, false);
            
        	GenericValue invoiceItem = delegator.findOne("InvoiceItem", UtilMisc.toMap("invoiceId", invoiceId, "invoiceItemSeqId", invoiceItemSeqId), false);
        	BigDecimal prevAmount = invoiceItem.getBigDecimal("amount");
        	BigDecimal prevQuantity = BigDecimal.ZERO;
        	BigDecimal cstPrevPercent = BigDecimal.ZERO;
        	BigDecimal vatPrevPercent = BigDecimal.ZERO;
        	BigDecimal bedPrevPercent = BigDecimal.ZERO;
        	BigDecimal prevTotalAmt = prevAmount;
        	if(UtilValidate.isNotEmpty(invoiceItem.get("quantity"))){
        		prevQuantity = invoiceItem.getBigDecimal("quantity");
        		prevTotalAmt = prevAmount.multiply(prevQuantity);
        	}
        	if(UtilValidate.isNotEmpty(invoiceItem.get("cstPercent"))){
        		cstPrevPercent = invoiceItem.getBigDecimal("cstPercent");
        	}
        	if(UtilValidate.isNotEmpty(invoiceItem.get("vatPercent"))){
        		varPrevPercent = invoiceItem.getBigDecimal("vatPercent");
        	}
        	if(UtilValidate.isNotEmpty(invoiceItem.get("bedPercent"))){
        		bedPrevPercent = invoiceItem.getBigDecimal("bedPercent");
        	}
        	
        	BigDecimal diffCSTPercent = cstCurrPercent.subtract(cstPrevPercent);
        	BigDecimal diffVATPercent = vatCurrPercent.subtract(vatPrevPercent);
        	BigDecimal diffBEDPercent = bedCurrPercent.subtract(bedrevPercent);
        	
        	if(bedPercent.compareTo(BigDecimal.ZERO)>0){
        		List<GenericValue> bedInvItems = EntityUtil.filterByCondition(invoiceItems, EntityCondition.makeCondition("invoiceItemTypeId", EntityOperator.EQUALS, "BED_SALE"));
        		BigDecimal bedItemAmt = BigDecimal.ZERO;
        		bedItemAmt = (amount.multiply(bedPercent)).divide(new BigDecimal(100), TAX_DECIMALS, TAX_ROUNDING);
    			
    			GenericValue bedInvItem = EntityUtil.getFirst(bedInvItems);
    			BigDecimal extAmt = bedInvItem.getBigDecimal("amount");
    			BigDecimal totalBEDAmt = bedItemAmt.add(extAmt);
    			bedInvItem.set("amount", totalBEDAmt);
    			bedInvItem.store();
        	}
        	
        	if(cstPercent.compareTo(BigDecimal.ZERO)>0){
        		List<GenericValue> cstInvItems = EntityUtil.filterByCondition(invoiceItems, EntityCondition.makeCondition("invoiceItemTypeId", EntityOperator.EQUALS, "CST_SALE"));
        		
        		BigDecimal cstItemAmt = BigDecimal.ZERO;
    			cstItemAmt = (amount.multiply(cstPercent)).divide(new BigDecimal(100), TAX_DECIMALS, TAX_ROUNDING);
    			
        		if(UtilValidate.isEmpty(cstInvItems)){
        			
        			Map<String, Object> createTaxItemContext = FastMap.newInstance();
        			createTaxItemContext.put("invoiceId", invoiceId);
        			createTaxItemContext.put("invoiceItemTypeId", "CST_SALE");
        			createTaxItemContext.put("amount", cstItemAmt);
        			createTaxItemContext.put("userLogin", userLogin);
                    createInvoiceItemResult = dispatcher.runSync("createInvoiceItem", createTaxItemContext);
                    
        		}
        		else{
        			GenericValue cstInvItem = EntityUtil.getFirst(cstInvItems);
        			BigDecimal extAmt = cstInvItem.getBigDecimal("amount");
        			BigDecimal totalCSTAmt = cstItemAmt.add(extAmt);
        			cstInvItem.set("amount", totalCSTAmt);
        			cstInvItem.store();
        		}
        	}
        	if(UtilValidate.isNotEmpty(vatPercent) && vatPercent.compareTo(BigDecimal.ZERO)>0){
        		List<GenericValue> vatInvItems = EntityUtil.filterByCondition(invoiceItems, EntityCondition.makeCondition("invoiceItemTypeId", EntityOperator.EQUALS, "VAT_SALE"));
        		BigDecimal vatItemAmt = BigDecimal.ZERO;
    			vatItemAmt = (amount.multiply(vatPercent)).divide(new BigDecimal(100), TAX_DECIMALS, TAX_ROUNDING);
    			
        		if(UtilValidate.isEmpty(vatInvItems)){
        			
        			Map<String, Object> createTaxItemContext = FastMap.newInstance();
        			createTaxItemContext.put("invoiceId", invoiceId);
        			createTaxItemContext.put("invoiceItemTypeId", "VAT_SALE");
        			createTaxItemContext.put("amount", vatItemAmt);
        			createTaxItemContext.put("userLogin", userLogin);
                    createInvoiceItemResult = dispatcher.runSync("createInvoiceItem", createTaxItemContext);
                    
        		}
        		else{
        			GenericValue vatInvItem = EntityUtil.getFirst(vatInvItems);
        			BigDecimal extAmt = vatInvItem.getBigDecimal("amount");
        			BigDecimal totalVATAmt = vatItemAmt.add(extAmt);
        			vatInvItem.set("amount", totalVATAmt);
        			vatInvItem.store();
        		}
        	}
        	
        	
        }catch(GenericEntityException e){
        	Debug.logError(e, e.getMessage(), module);
            return ServiceUtil.returnError(e.getMessage());
        }
        catch(GenericServiceException e){
        	Debug.logError(e, e.getMessage(), module);
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }*/
    
    public static Map<String, Object> removeInvoiceItemAndTax(DispatchContext dctx, Map<String, Object> context) {
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");
        String invoiceId = (String)context.get("invoiceId");
        String invoiceItemSeqId = (String)context.get("invoiceItemSeqId");

        BigDecimal cstPercent = BigDecimal.ZERO;
    	BigDecimal vatPercent = BigDecimal.ZERO;
    	BigDecimal bedPercent = BigDecimal.ZERO;
        // similarly, tax only for purchase invoices
    	
        try{
        	
            List<GenericValue> invoiceItems = delegator.findList("InvoiceItem", EntityCondition.makeCondition("invoiceId", EntityOperator.EQUALS, invoiceId), null, null, null, false);
            
        	GenericValue invoiceItem = delegator.findOne("InvoiceItem", UtilMisc.toMap("invoiceId", invoiceId, "invoiceItemSeqId", invoiceItemSeqId), false);
        	
        	GenericValue invoice = delegator.findOne("Invoice", UtilMisc.toMap("invoiceId", invoiceId), false);
        	
        	String invoiceTypeId = invoice.getString("invoiceTypeId");
    		List<GenericValue> invoiceTypeMap = delegator.findList("InvoiceItemTypeMap", EntityCondition.makeCondition("invoiceTypeId", EntityOperator.EQUALS, invoiceTypeId), null, null, null, false);
    		
        	BigDecimal amount = invoiceItem.getBigDecimal("amount");
        	BigDecimal quantity = BigDecimal.ZERO;
        	BigDecimal totalAmt = BigDecimal.ZERO;
        	totalAmt = amount;
        	if(UtilValidate.isNotEmpty(invoiceItem.get("quantity"))){
        		quantity = invoiceItem.getBigDecimal("quantity");
        		totalAmt = amount.multiply(quantity);
        	}
        	if(UtilValidate.isNotEmpty(invoiceItem.get("cstPercent"))){
        		cstPercent = invoiceItem.getBigDecimal("cstPercent");
        	}
        	if(UtilValidate.isNotEmpty(invoiceItem.get("vatPercent"))){
        		vatPercent = invoiceItem.getBigDecimal("vatPercent");
        	}
        	if(UtilValidate.isNotEmpty(invoiceItem.get("bedPercent"))){
        		bedPercent = invoiceItem.getBigDecimal("bedPercent");
        	}
        	
        	if(bedPercent.compareTo(BigDecimal.ZERO)>0){
        		
        		List<GenericValue> bedItemTypes = EntityUtil.filterByCondition(invoiceTypeMap, EntityCondition.makeCondition("invoiceItemTypeId", EntityOperator.LIKE, "BED_%"));
        		if(UtilValidate.isNotEmpty(bedItemTypes)){
        			String invoiceTaxItemType = (EntityUtil.getFirst(bedItemTypes)).getString("invoiceItemTypeId");
        			
        			List<GenericValue> bedInvItems = EntityUtil.filterByCondition(invoiceItems, EntityCondition.makeCondition("invoiceItemTypeId", EntityOperator.EQUALS, invoiceTaxItemType));
	        		if(UtilValidate.isNotEmpty(bedInvItems)){
	        			BigDecimal bedItemAmt = BigDecimal.ZERO;
		        		bedItemAmt = (totalAmt.multiply(bedPercent)).divide(new BigDecimal(100), TAX_DECIMALS, TAX_ROUNDING);
		    			totalAmt = totalAmt.add(bedItemAmt);
		    			GenericValue bedInvItem = EntityUtil.getFirst(bedInvItems);
		    			BigDecimal extAmt = bedInvItem.getBigDecimal("amount");
		    			BigDecimal totalBEDAmt = extAmt.subtract(bedItemAmt);
		    			bedInvItem.set("amount", totalBEDAmt);
		    			bedInvItem.store();
	        		}
        			
        		}
        	}
        	
        	if(cstPercent.compareTo(BigDecimal.ZERO)>0){
        		
        		List<GenericValue> cstItemTypes = EntityUtil.filterByCondition(invoiceTypeMap, EntityCondition.makeCondition("invoiceItemTypeId", EntityOperator.LIKE, "CST_%"));
        		if(UtilValidate.isNotEmpty(cstItemTypes)){
        			String invoiceTaxItemType = (EntityUtil.getFirst(cstItemTypes)).getString("invoiceItemTypeId");
        		
	        		List<GenericValue> cstInvItems = EntityUtil.filterByCondition(invoiceItems, EntityCondition.makeCondition("invoiceItemTypeId", EntityOperator.EQUALS, invoiceTaxItemType));
	        		if(UtilValidate.isNotEmpty(cstInvItems)){
	        			BigDecimal cstItemAmt = BigDecimal.ZERO;
		    			cstItemAmt = (totalAmt.multiply(cstPercent)).divide(new BigDecimal(100), TAX_DECIMALS, TAX_ROUNDING);
		    			
		    			GenericValue cstInvItem = EntityUtil.getFirst(cstInvItems);
		    			BigDecimal extAmt = cstInvItem.getBigDecimal("amount");
		    			BigDecimal totalCSTAmt = extAmt.subtract(cstItemAmt);
		    			cstInvItem.set("amount", totalCSTAmt);
		    			cstInvItem.store();
	        		}
        		}
        	}
        	if(UtilValidate.isNotEmpty(vatPercent) && vatPercent.compareTo(BigDecimal.ZERO)>0){
        		
        		List<GenericValue> vatItemTypes = EntityUtil.filterByCondition(invoiceTypeMap, EntityCondition.makeCondition("invoiceItemTypeId", EntityOperator.LIKE, "VAT_%"));
        		if(UtilValidate.isNotEmpty(vatItemTypes)){
        			String invoiceTaxItemType = (EntityUtil.getFirst(vatItemTypes)).getString("invoiceItemTypeId");
        			
	        		List<GenericValue> vatInvItems = EntityUtil.filterByCondition(invoiceItems, EntityCondition.makeCondition("invoiceItemTypeId", EntityOperator.EQUALS, invoiceTaxItemType));
	        		if(UtilValidate.isNotEmpty(vatInvItems)){
	        			BigDecimal vatItemAmt = BigDecimal.ZERO;
		    			vatItemAmt = (totalAmt.multiply(vatPercent)).divide(new BigDecimal(100), TAX_DECIMALS, TAX_ROUNDING);
		    			
		    			GenericValue vatInvItem = EntityUtil.getFirst(vatInvItems);
		    			BigDecimal extAmt = vatInvItem.getBigDecimal("amount");
		    			BigDecimal totalVATAmt = vatItemAmt.subtract(extAmt);
		    			vatInvItem.set("amount", totalVATAmt);
		    			vatInvItem.store();
	        		}
        		}
        	}
        	
        	Map<String, Object> removeInvoiceItemResult = dispatcher.runSync("removeInvoiceItem", context);
            if (ServiceUtil.isError(removeInvoiceItemResult)) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorRemovingInvoiceItemFromOrder",locale), null, null, removeInvoiceItemResult);
            }
        	
        	
        }catch(GenericEntityException e){
        	Debug.logError(e, e.getMessage(), module);
            return ServiceUtil.returnError(e.getMessage());
        }
        catch(GenericServiceException e){
        	Debug.logError(e, e.getMessage(), module);
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }
    
    /*Create invoice for List of orders    */
    public static Map<String, Object> createInvoiceForCRInstOrder(DispatchContext dctx, Map<String, Object> context) {
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");

        if (DECIMALS == -1 || ROUNDING == -1) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingAritmeticPropertiesNotConfigured",locale));
        }
        String billOfSaleTypeId = (String) context.get("billOfSaleTypeId");
        String orderId = (String) context.get("orderId");
        List<GenericValue> billItems = UtilGenerics.checkList(context.get("billItems"));
        String invoiceId = (String) context.get("invoiceId");

        if (UtilValidate.isEmpty(billItems)) {
            Debug.logVerbose("No order items to invoice; not creating invoice; returning success", module);
            return ServiceUtil.returnSuccess(UtilProperties.getMessage(resource,"AccountingNoOrderItemsToInvoice",locale));
        }

        try {
            GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
            if (orderHeader == null) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingNoOrderHeader",locale));
            }
            
            // figure out the invoice type
            String invoiceType = null;

            String orderType = orderHeader.getString("orderTypeId");
            if (orderType.equals("SALES_ORDER")) {
                invoiceType = "SALES_INVOICE";
            } else if (orderType.equals("PURCHASE_ORDER")) {
                invoiceType = "PURCHASE_INVOICE";
            }

            // Set the precision depending on the type of invoice
            int invoiceTypeDecimals = UtilNumber.getBigDecimalScale("invoice." + invoiceType + ".decimals");
            if (invoiceTypeDecimals == -1) invoiceTypeDecimals = DECIMALS;

            // Make an order read helper from the order
            OrderReadHelper orh = new OrderReadHelper(orderHeader);

            // get the product store
            GenericValue productStore = orh.getProductStore();

            // get the shipping adjustment mode (Y = Pro-Rate; N = First-Invoice)
            String prorateShipping = productStore != null ? productStore.getString("prorateShipping") : "Y";
            if (prorateShipping == null) {
                prorateShipping = "Y";
            }

            // get the billing parties
            String billToCustomerPartyId = orh.getBillToParty().getString("partyId");
            String billFromVendorPartyId = orh.getBillFromParty().getString("partyId");

            // get some price totals
            BigDecimal shippableAmount = orh.getShippableTotal(null);
            BigDecimal orderSubTotal = orh.getOrderItemsSubTotal();

            // these variables are for pro-rating order amounts across invoices, so they should not be rounded off for maximum accuracy
            BigDecimal invoiceShipProRateAmount = ZERO;
            BigDecimal invoiceSubTotal = ZERO;
            BigDecimal invoiceQuantity = ZERO;

            GenericValue billingAccount = orderHeader.getRelatedOne("BillingAccount");
            String billingAccountId = billingAccount != null ? billingAccount.getString("billingAccountId") : null;

            Timestamp invoiceDate = (Timestamp)context.get("eventDate");
            if (UtilValidate.isEmpty(invoiceDate)) {
                // TODO: ideally this should be the same time as when a shipment is sent and be passed in as a parameter
                invoiceDate = UtilDateTime.nowTimestamp();
            }
            // TODO: perhaps consider billing account net days term as well?
            Long orderTermNetDays = orh.getOrderTermNetDays();
            Timestamp dueDate = null;
            if (orderTermNetDays != null) {
                dueDate = UtilDateTime.getDayEnd(invoiceDate, orderTermNetDays);
            } 

            // See if estimated delivery date is present and use that as the due date
            Timestamp estimatedDeliveryDate = orderHeader.getTimestamp("estimatedDeliveryDate");            
            if (dueDate == null && estimatedDeliveryDate != null) {
            	dueDate = estimatedDeliveryDate;
            }
            List orderIds = EntityUtil.getFieldListFromEntityList(billItems, "orderId", true);
            if(orderIds.size()>1){
            	dueDate = invoiceDate;
            }
            
            // create the invoice record
            if (UtilValidate.isEmpty(invoiceId)) {
                Map<String, Object> createInvoiceContext = FastMap.newInstance();
                createInvoiceContext.put("partyId", billToCustomerPartyId);
                createInvoiceContext.put("partyIdFrom", billFromVendorPartyId);
                createInvoiceContext.put("billingAccountId", billingAccountId);
                createInvoiceContext.put("facilityId", orderHeader.getString("originFacilityId"));
                createInvoiceContext.put("description", orderHeader.getString("productSubscriptionTypeId"));
                createInvoiceContext.put("invoiceDate", invoiceDate);
                createInvoiceContext.put("dueDate", dueDate);
                createInvoiceContext.put("billOfSaleTypeId", billOfSaleTypeId);
                createInvoiceContext.put("isEnableAcctg", orderHeader.getString("isEnableAcctg"));
                createInvoiceContext.put("invoiceTypeId", invoiceType);
                // start with INVOICE_IN_PROCESS, in the INVOICE_READY we can't change the invoice (or shouldn't be able to...)
                createInvoiceContext.put("statusId", "INVOICE_IN_PROCESS");
                createInvoiceContext.put("currencyUomId", orderHeader.getString("currencyUom"));
                createInvoiceContext.put("userLogin", userLogin);

                // store the invoice first
                Map<String, Object> createInvoiceResult = dispatcher.runSync("createInvoice", createInvoiceContext);
                if (ServiceUtil.isError(createInvoiceResult)) {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceFromOrder",locale), null, null, createInvoiceResult);
                }

                // call service, not direct entity op: delegator.create(invoice);
                invoiceId = (String) createInvoiceResult.get("invoiceId");
            }

            // order roles to invoice roles
           List<GenericValue> orderRoles = orderHeader.getRelated("OrderRole");
            Map<String, Object> createInvoiceRoleContext = FastMap.newInstance();

            // order terms to invoice terms.
            // TODO: it might be nice to filter OrderTerms to only copy over financial terms.
            List<GenericValue> orderTerms = orh.getOrderTerms();
            createInvoiceTerms(delegator, dispatcher, invoiceId, orderTerms, userLogin, locale);

            // billing accounts
            // List billingAccountTerms = null;
            // for billing accounts we will use related information
            if (billingAccount != null) {
                /*
                 * jacopoc: billing account terms were already copied as order terms
                 *          when the order was created.
                // get the billing account terms
                billingAccountTerms = billingAccount.getRelated("BillingAccountTerm");

                // set the invoice terms as defined for the billing account
                createInvoiceTerms(delegator, dispatcher, invoiceId, billingAccountTerms, userLogin, locale);
                */
                // set the invoice bill_to_customer from the billing account
                List<GenericValue> billToRoles = billingAccount.getRelated("BillingAccountRole", UtilMisc.toMap("roleTypeId", "BILL_TO_CUSTOMER"), null);
                for (GenericValue billToRole : billToRoles) {
                    if (!(billToRole.getString("partyId").equals(billToCustomerPartyId))) {
                        createInvoiceRoleContext = UtilMisc.toMap("invoiceId", invoiceId, "partyId", billToRole.get("partyId"),
                                                                           "roleTypeId", "BILL_TO_CUSTOMER", "userLogin", userLogin);
                        Map<String, Object> createInvoiceRoleResult = dispatcher.runSync("createInvoiceRole", createInvoiceRoleContext);
                        if (ServiceUtil.isError(createInvoiceRoleResult)) {
                            return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceRoleFromOrder",locale), null, null, createInvoiceRoleResult);
                        }
                    }
                }

                // set the bill-to contact mech as the contact mech of the billing account
                if (UtilValidate.isNotEmpty(billingAccount.getString("contactMechId"))) {
                    Map<String, Object> createBillToContactMechContext = UtilMisc.toMap("invoiceId", invoiceId, "contactMechId", billingAccount.getString("contactMechId"),
                                                                       "contactMechPurposeTypeId", "BILLING_LOCATION", "userLogin", userLogin);
                    Map<String, Object> createBillToContactMechResult = dispatcher.runSync("createInvoiceContactMech", createBillToContactMechContext);
                    if (ServiceUtil.isError(createBillToContactMechResult)) {
                        return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceContactMechFromOrder",locale), null, null, createBillToContactMechResult);
                    }
                }
            } else {
                List<GenericValue> billingLocations = orh.getBillingLocations();
                if (UtilValidate.isNotEmpty(billingLocations)) {
                    for (GenericValue ocm : billingLocations) {
                        Map<String, Object> createBillToContactMechContext = UtilMisc.toMap("invoiceId", invoiceId, "contactMechId", ocm.getString("contactMechId"),
                                                                           "contactMechPurposeTypeId", "BILLING_LOCATION", "userLogin", userLogin);
                        Map<String, Object> createBillToContactMechResult = dispatcher.runSync("createInvoiceContactMech", createBillToContactMechContext);
                        if (ServiceUtil.isError(createBillToContactMechResult)) {
                            return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceContactMechFromOrder",locale), null, null, createBillToContactMechResult);
                        }
                    }
                } else {
                    Debug.logInfo("No billing locations found for order [" + orderId +"] and none were created for Invoice [" + invoiceId + "]", module);
                }
            }

            // get a list of the payment method types
            //DEJ20050705 doesn't appear to be used: List paymentPreferences = orderHeader.getRelated("OrderPaymentPreference");

            // create the bill-from (or pay-to) contact mech as the primary PAYMENT_LOCATION of the party from the store
            GenericValue payToAddress = null;
            if (invoiceType.equals("PURCHASE_INVOICE")) {
                // for purchase orders, the pay to address is the BILLING_LOCATION of the vendor
                GenericValue billFromVendor = orh.getPartyFromRole("BILL_FROM_VENDOR");
                if (billFromVendor != null) {
                    List<GenericValue> billingContactMechs = billFromVendor.getRelatedOne("Party").getRelatedByAnd("PartyContactMechPurpose",
                            UtilMisc.toMap("contactMechPurposeTypeId", "BILLING_LOCATION"));
                    if (UtilValidate.isNotEmpty(billingContactMechs)) {
                        payToAddress = EntityUtil.getFirst(billingContactMechs);
                    }
                }
            } else {
                // for sales orders, it is the payment address on file for the store
                payToAddress = PaymentWorker.getPaymentAddress(delegator, productStore.getString("payToPartyId"));
            }
            if (payToAddress != null) {
                Map<String, Object> createPayToContactMechContext = UtilMisc.toMap("invoiceId", invoiceId, "contactMechId", payToAddress.getString("contactMechId"),
                                                                   "contactMechPurposeTypeId", "PAYMENT_LOCATION", "userLogin", userLogin);
                Map<String, Object> createPayToContactMechResult = dispatcher.runSync("createInvoiceContactMech", createPayToContactMechContext);
                if (ServiceUtil.isError(createPayToContactMechResult)) {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceContactMechFromOrder",locale), null, null, createPayToContactMechResult);
                }
            }

            // sequence for items - all OrderItems or InventoryReservations + all Adjustments
            int invoiceItemSeqNum = 1;
            String invoiceItemSeqId = UtilFormatOut.formatPaddedNumber(invoiceItemSeqNum, INVOICE_ITEM_SEQUENCE_ID_DIGITS);
            
            // create the item records
            for (GenericValue currentValue : billItems) {
                GenericValue itemIssuance = null;
                GenericValue orderItem = null;
                GenericValue shipmentReceipt = null;
                if ("ItemIssuance".equals(currentValue.getEntityName())) {
                    itemIssuance = currentValue;
                } else if ("OrderItem".equals(currentValue.getEntityName())) {
                    orderItem = currentValue;
                } else if ("ShipmentReceipt".equals(currentValue.getEntityName())) {
                    shipmentReceipt = currentValue;
                } else {
                    Debug.logError("Unexpected entity " + currentValue + " of type " + currentValue.getEntityName(), module);
                }
                if (orderItem == null && itemIssuance != null) {
                    orderItem = itemIssuance.getRelatedOne("OrderItem");
                } else if ((orderItem == null) && (shipmentReceipt != null)) {
                    orderItem = shipmentReceipt.getRelatedOne("OrderItem");
                } else if ((orderItem == null) && (itemIssuance == null) && (shipmentReceipt == null)) {
                    Debug.logError("Cannot create invoice when orderItem, itemIssuance, and shipmentReceipt are all null", module);
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingIllegalValuesPassedToCreateInvoiceService",locale));
                }
                GenericValue product = null;
                if (orderItem.get("productId") != null) {
                    product = orderItem.getRelatedOne("Product");
                }
                // get some quantities
                BigDecimal billingQuantity = null;
                if (itemIssuance != null) {
                    billingQuantity = itemIssuance.getBigDecimal("quantity");
                    BigDecimal cancelQty = itemIssuance.getBigDecimal("cancelQuantity");
                    if (cancelQty == null) {
                        cancelQty = ZERO;
                    }
                    billingQuantity = billingQuantity.subtract(cancelQty).setScale(DECIMALS, ROUNDING);
                } else if (shipmentReceipt != null) {
                    billingQuantity = shipmentReceipt.getBigDecimal("quantityAccepted");
                } else {
                    BigDecimal orderedQuantity = OrderReadHelper.getOrderItemQuantity(orderItem);
                    BigDecimal invoicedQuantity = OrderReadHelper.getOrderItemInvoicedQuantity(orderItem);
                    billingQuantity = orderedQuantity.subtract(invoicedQuantity);
                    if (billingQuantity.compareTo(ZERO) < 0) {
                        billingQuantity = ZERO;
                    }
                }
                if (billingQuantity == null) billingQuantity = ZERO;
                // check if shipping applies to this item.  Shipping is calculated for sales invoices, not purchase invoices.
                boolean shippingApplies = false;
                if ((product != null) && (ProductWorker.shippingApplies(product)) && (invoiceType.equals("SALES_INVOICE"))) {
                    shippingApplies = true;
                }

                BigDecimal billingAmount = orderItem.getBigDecimal("unitPrice").setScale(invoiceTypeDecimals, ROUNDING);
                Map<String, Object> createInvoiceItemContext = FastMap.newInstance();
                createInvoiceItemContext.put("invoiceId", invoiceId);
                createInvoiceItemContext.put("invoiceItemSeqId", invoiceItemSeqId);
                createInvoiceItemContext.put("invoiceItemTypeId", getInvoiceItemType(delegator, (orderItem.getString("orderItemTypeId")), (product == null ? null : product.getString("productTypeId")), invoiceType, "INV_FPROD_ITEM"));
                createInvoiceItemContext.put("description", orderItem.get("itemDescription"));
                createInvoiceItemContext.put("quantity", billingQuantity);
                createInvoiceItemContext.put("amount", billingAmount);
                createInvoiceItemContext.put("productId", orderItem.get("productId"));
                createInvoiceItemContext.put("productFeatureId", orderItem.get("productFeatureId"));
                createInvoiceItemContext.put("overrideGlAccountId", orderItem.get("overrideGlAccountId"));
                //createInvoiceItemContext.put("uomId", "");
                createInvoiceItemContext.put("userLogin", userLogin);
                String itemIssuanceId = null;
                if (itemIssuance != null && itemIssuance.get("inventoryItemId") != null) {
                    itemIssuanceId = itemIssuance.getString("itemIssuanceId");
                    createInvoiceItemContext.put("inventoryItemId", itemIssuance.get("inventoryItemId"));
                }
                // similarly, tax only for purchase invoices
                if ((product != null) && (invoiceType.equals("SALES_INVOICE"))) {
                    createInvoiceItemContext.put("taxableFlag", product.get("taxable"));
                }
                Map<String, Object> createInvoiceItemResult = dispatcher.runSync("createInvoiceItem", createInvoiceItemContext);
                if (ServiceUtil.isError(createInvoiceItemResult)) {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceItemFromOrder",locale), null, null, createInvoiceItemResult);
                }

                // this item total
                BigDecimal thisAmount = billingAmount.multiply(billingQuantity).setScale(invoiceTypeDecimals, ROUNDING);

                // add to the ship amount only if it applies to this item
                if (shippingApplies) {
                    invoiceShipProRateAmount = invoiceShipProRateAmount.add(thisAmount).setScale(invoiceTypeDecimals, ROUNDING);
                }

                // increment the invoice subtotal
                invoiceSubTotal = invoiceSubTotal.add(thisAmount).setScale(100, ROUNDING);

                // increment the invoice quantity
                invoiceQuantity = invoiceQuantity.add(billingQuantity).setScale(invoiceTypeDecimals, ROUNDING);

                // create the OrderItemBilling record
                Map<String, Object> createOrderItemBillingContext = FastMap.newInstance();
                createOrderItemBillingContext.put("invoiceId", invoiceId);
                createOrderItemBillingContext.put("invoiceItemSeqId", invoiceItemSeqId);
                createOrderItemBillingContext.put("orderId", orderItem.get("orderId"));
                createOrderItemBillingContext.put("orderItemSeqId", orderItem.get("orderItemSeqId"));
                createOrderItemBillingContext.put("itemIssuanceId", itemIssuanceId);
                createOrderItemBillingContext.put("quantity", billingQuantity);
                createOrderItemBillingContext.put("amount", billingAmount);
                createOrderItemBillingContext.put("userLogin", userLogin);
                if ((shipmentReceipt != null) && (shipmentReceipt.getString("receiptId") != null)) {
                    createOrderItemBillingContext.put("shipmentReceiptId", shipmentReceipt.getString("receiptId"));
                }

                Map<String, Object> createOrderItemBillingResult = dispatcher.runSync("createOrderItemBilling", createOrderItemBillingContext);
                if (ServiceUtil.isError(createOrderItemBillingResult)) {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingOrderItemBillingFromOrder",locale), null, null, createOrderItemBillingResult);
                }

                if ("ItemIssuance".equals(currentValue.getEntityName())) {
                    List<GenericValue> shipmentItemBillings = delegator.findByAnd("ShipmentItemBilling", UtilMisc.toMap("shipmentId", currentValue.get("shipmentId")));
                    if (UtilValidate.isEmpty(shipmentItemBillings)) {

                        // create the ShipmentItemBilling record
                        GenericValue shipmentItemBilling = delegator.makeValue("ShipmentItemBilling", UtilMisc.toMap("invoiceId", invoiceId, "invoiceItemSeqId", invoiceItemSeqId));
                        shipmentItemBilling.put("shipmentId", currentValue.get("shipmentId"));
                        shipmentItemBilling.put("shipmentItemSeqId", currentValue.get("shipmentItemSeqId"));
                        shipmentItemBilling.create();
                    }
                }

                String parentInvoiceItemSeqId = invoiceItemSeqId;
                // increment the counter
                invoiceItemSeqNum++;
                invoiceItemSeqId = UtilFormatOut.formatPaddedNumber(invoiceItemSeqNum, INVOICE_ITEM_SEQUENCE_ID_DIGITS);

                // Get the original order item from the DB, in case the quantity has been overridden
                GenericValue originalOrderItem = delegator.findByPrimaryKey("OrderItem", UtilMisc.toMap("orderId", orderId, "orderItemSeqId", orderItem.getString("orderItemSeqId")));

            }

            // create header adjustments as line items -- always to tax/shipping last
            Map<GenericValue, BigDecimal> shipAdjustments = FastMap.newInstance();
            Map<GenericValue, BigDecimal> taxAdjustments = FastMap.newInstance();

            List<GenericValue> headerAdjustments = FastList.newInstance();
            if(UtilValidate.isNotEmpty(billItems)){
            	List<String> billOrderIds = EntityUtil.getFieldListFromEntityList(billItems, "orderId", true);
            	headerAdjustments = delegator.findList("OrderAdjustment", EntityCondition.makeCondition("orderId", EntityOperator.IN, billOrderIds), null, null, null, false);
            }
            for (GenericValue adj : headerAdjustments) {

                  if (adj.get("amount") == null) { // JLR 17/4/7 : fix a bug coming from POS in case of use of a discount (on item(s) or sale, item(s) here) and a cash amount higher than total (hence issuing change)
                      continue;
                  }
                  BigDecimal amount = ZERO;
                  if (adj.get("amount") != null) {
                      // pro-rate the amount
                      // set decimals = 100 means we don't round this intermediate value, which is very important
                      amount = adj.getBigDecimal("amount");
                      //amount = amount.multiply(billingQuantity);
                      // Tax needs to be rounded differently from other order adjustments
                      if (adj.getString("orderAdjustmentTypeId").equals("SALES_TAX")) {
                          amount = amount.setScale(TAX_DECIMALS, TAX_ROUNDING);
                      } else {
                          amount = amount.setScale(invoiceTypeDecimals, ROUNDING);
                      }
                  } else if (adj.get("sourcePercentage") != null) {
                      // pro-rate the amount
                      // set decimals = 100 means we don't round this intermediate value, which is very important
                	  return ServiceUtil.returnError("Case not supported");
                  }
                  if (amount.signum() != 0) {
                      Map<String, Object> createInvoiceItemAdjContext = FastMap.newInstance();
              		GenericValue orderAdjustmentType = adj.getRelatedOne("OrderAdjustmentType");                        
                      createInvoiceItemAdjContext.put("invoiceId", invoiceId);
                      createInvoiceItemAdjContext.put("invoiceItemSeqId", invoiceItemSeqId);
                      createInvoiceItemAdjContext.put("invoiceItemTypeId", getInvoiceItemType(delegator, adj.getString("orderAdjustmentTypeId"), null, invoiceType, "INVOICE_ITM_ADJ"));
                    // Debug.log("=invoiceType="+invoiceType+"====NowKey=InSerVICEE=="+getInvoiceItemType(delegator, adj.getString("orderAdjustmentTypeId"), null, invoiceType, "INVOICE_ITM_ADJ"));
                      if (("SALES_TAX".equals(orderAdjustmentType.getString("parentTypeId")))) {
                          createInvoiceItemAdjContext.put("invoiceItemTypeId", adj.getString("orderAdjustmentTypeId"));
                      }
                      createInvoiceItemAdjContext.put("quantity", BigDecimal.ONE);
                      createInvoiceItemAdjContext.put("amount", amount);
                      createInvoiceItemAdjContext.put("overrideGlAccountId", adj.get("overrideGlAccountId"));
                      if (!("SALES_TAX".equals(orderAdjustmentType.getString("parentTypeId")))) {   
                      	createInvoiceItemAdjContext.put("parentInvoiceId", invoiceId);
                      }
                      createInvoiceItemAdjContext.put("userLogin", userLogin);
                      createInvoiceItemAdjContext.put("taxAuthPartyId", adj.get("taxAuthPartyId"));
                      createInvoiceItemAdjContext.put("taxAuthGeoId", adj.get("taxAuthGeoId"));
                      createInvoiceItemAdjContext.put("taxAuthorityRateSeqId", adj.get("taxAuthorityRateSeqId"));

                      // some adjustments fill out the comments field instead
                      if (!("SALES_TAX".equals(orderAdjustmentType.getString("parentTypeId")))) {
                      	String description = (UtilValidate.isEmpty(adj.getString("description")) ? adj.getString("comments") : adj.getString("description"));
                      	createInvoiceItemAdjContext.put("description", description);
                      }
                      // invoice items for sales tax are not taxable themselves
                      // TODO: This is not an ideal solution. Instead, we need to use OrderAdjustment.includeInTax when it is implemented
                      if (!("SALES_TAX".equals(orderAdjustmentType.getString("parentTypeId")))) {
                         // createInvoiceItemAdjContext.put("taxableFlag", product.get("taxable"));
                      }

                      // If the OrderAdjustment is associated to a ProductPromo,
                      // and the field ProductPromo.overrideOrgPartyId is set,
                      // copy the value to InvoiceItem.overrideOrgPartyId: this
                      // represent an organization override for the payToPartyId
                      if (UtilValidate.isNotEmpty(adj.getString("productPromoId"))) {
                          try {
                              GenericValue productPromo = adj.getRelatedOne("ProductPromo");
                              if (UtilValidate.isNotEmpty(productPromo.getString("overrideOrgPartyId"))) {
                                  createInvoiceItemAdjContext.put("overrideOrgPartyId", productPromo.getString("overrideOrgPartyId"));
                              }
                          } catch (GenericEntityException e) {
                              Debug.logError(e, "Error looking up ProductPromo with id [" + adj.getString("productPromoId") + "]", module);
                          }
                      }

                      Map<String, Object> createInvoiceItemAdjResult = dispatcher.runSync("createInvoiceItem", createInvoiceItemAdjContext);
                      if (ServiceUtil.isError(createInvoiceItemAdjResult)) {
                          return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceItemFromOrder",locale), null, null, createInvoiceItemAdjResult);
                      }

                      // Create the OrderAdjustmentBilling record
                      Map<String, Object> createOrderAdjustmentBillingContext = FastMap.newInstance();
                      createOrderAdjustmentBillingContext.put("orderAdjustmentId", adj.getString("orderAdjustmentId"));
                      createOrderAdjustmentBillingContext.put("invoiceId", invoiceId);
                      createOrderAdjustmentBillingContext.put("invoiceItemSeqId", invoiceItemSeqId);
                      createOrderAdjustmentBillingContext.put("amount", amount);
                      createOrderAdjustmentBillingContext.put("userLogin", userLogin);

                      Map<String, Object> createOrderAdjustmentBillingResult = dispatcher.runSync("createOrderAdjustmentBilling", createOrderAdjustmentBillingContext);
                      if (ServiceUtil.isError(createOrderAdjustmentBillingResult)) {
                          return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingOrderAdjustmentBillingFromOrder",locale), null, null, createOrderAdjustmentBillingContext);
                      }

                      // this adjustment amount
                      BigDecimal thisAdjAmount = amount;

                      // increment the counter
                      invoiceItemSeqNum++;
                      invoiceItemSeqId = UtilFormatOut.formatPaddedNumber(invoiceItemSeqNum, INVOICE_ITEM_SEQUENCE_ID_DIGITS);
                  }
              }

            // next do the shipping adjustments.  Note that we do not want to add these to the invoiceSubTotal or orderSubTotal for pro-rating tax later, as that would cause
            // numerator/denominator problems when the shipping is not pro-rated but rather charged all on the first invoice
            // check for previous order payments
            List<GenericValue> orderPaymentPrefs = delegator.findByAnd("OrderPaymentPreference", UtilMisc.toMap("orderId", orderId));
            List<GenericValue> currentPayments = FastList.newInstance();
            for (GenericValue paymentPref : orderPaymentPrefs) {
                List<GenericValue> payments = paymentPref.getRelated("Payment");
                currentPayments.addAll(payments);
            }
            // apply these payments to the invoice if they have any remaining amount to apply
            for (GenericValue payment : currentPayments) {
                BigDecimal notApplied = PaymentWorker.getPaymentNotApplied(payment);
                if (notApplied.signum() > 0) {
                    Map<String, Object> appl = FastMap.newInstance();
                    appl.put("paymentId", payment.get("paymentId"));
                    appl.put("invoiceId", invoiceId);
                    appl.put("billingAccountId", billingAccountId);
                    appl.put("amountApplied", notApplied);
                    appl.put("userLogin", userLogin);
                    Map<String, Object> createPayApplResult = dispatcher.runSync("createPaymentApplication", appl);
                    if (ServiceUtil.isError(createPayApplResult)) {
                        return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceFromOrder",locale), null, null, createPayApplResult);
                    }
                }
            }

            // Should all be in place now. Depending on the ProductStore.autoApproveInvoice setting, set status to INVOICE_READY (unless it's a purchase invoice, which we set to INVOICE_IN_PROCESS)
            String autoApproveInvoice = productStore != null ? productStore.getString("autoApproveInvoice") : "Y";
            if (!"N".equals(autoApproveInvoice)) {
                String nextStatusId = "PURCHASE_INVOICE".equals(invoiceType) ? "INVOICE_IN_PROCESS" : "INVOICE_APPROVED";
                Map<String, Object> setInvoiceStatusResult = dispatcher.runSync("setInvoiceStatus", UtilMisc.<String, Object>toMap("invoiceId", invoiceId, "statusId", nextStatusId, "userLogin", userLogin));
                if (ServiceUtil.isError(setInvoiceStatusResult)) {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceFromOrder",locale), null, null, setInvoiceStatusResult);
                }
                /*  :TODO:: For now we'll leave the invoice in APROVED STATUS
                nextStatusId = "INVOICE_READY";
                setInvoiceStatusResult = dispatcher.runSync("setInvoiceStatus", UtilMisc.<String, Object>toMap("invoiceId", invoiceId, "statusId", nextStatusId, "userLogin", userLogin));
                */            
            }

            Map<String, Object> resp = ServiceUtil.returnSuccess();
            resp.put("invoiceId", invoiceId);
            resp.put("invoiceTypeId", invoiceType);
            return resp;
        } catch (GenericEntityException e) {
            String errMsg = UtilProperties.getMessage(resource, "AccountingEntityDataProblemCreatingInvoiceFromOrderItems", UtilMisc.toMap("reason", e.toString()), locale);
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        } catch (GenericServiceException e) {
            String errMsg = UtilProperties.getMessage(resource, "AccountingServiceOtherProblemCreatingInvoiceFromOrderItems", UtilMisc.toMap("reason", e.toString()), locale);
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        }
    }
    /**
     * Update/add to the paymentApplication table and making sure no duplicate
     * record exist
     *
     * @param delegator
     * @param paymentApplication
     * @return map results
     */
    private static Map<String, Object> storePaymentApplication(Delegator delegator, GenericValue paymentApplication,Locale locale) {
        Map<String, Object> results = ServiceUtil.returnSuccess(UtilProperties.getMessage(resource, "AccountingSuccessFull",locale));
        boolean debug = true;
        if (debug) Debug.logInfo("Start updating the paymentApplication table ", module);

        if (DECIMALS == -1 || ROUNDING == -1) {
            return ServiceUtil.returnError("Arithmetic properties for Invoice services not configured properly. Cannot proceed.");
        }

        // check if a record already exists with this data
        List<GenericValue> checkAppls = null;
        try {
            checkAppls = delegator.findByAnd("PaymentApplication", UtilMisc.toMap(
                    "invoiceId", paymentApplication.get("invoiceId"),
                    "invoiceItemSeqId", paymentApplication.get("invoiceItemSeqId"),
                    "billingAccountId", paymentApplication.get("billingAccountId"),
                    "paymentId", paymentApplication.get("paymentId"),
                    "toPaymentId", paymentApplication.get("toPaymentId"),
                    "taxAuthGeoId", paymentApplication.get("taxAuthGeoId")));
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        if (checkAppls.size() > 0) {
            if (debug) Debug.logInfo(checkAppls.size() + " records already exist", module);
            // 1 record exists just update and if different ID delete other record and add together.
            GenericValue checkAppl = checkAppls.get(0);
            // if new record  add to the already existing one.
            if (paymentApplication.get("paymentApplicationId") == null)    {
                // add 2 amounts together
                checkAppl.set("amountApplied", paymentApplication.getBigDecimal("amountApplied").
                        add(checkAppl.getBigDecimal("amountApplied")).setScale(DECIMALS,ROUNDING));
                if (debug)     Debug.logInfo("Update paymentApplication record: " + checkAppl.getString("paymentApplicationId") + " with appliedAmount:" + checkAppl.getBigDecimal("amountApplied"), module);
                try {
                    checkAppl.store();
                } catch (GenericEntityException e) {
                    ServiceUtil.returnError(e.getMessage());
                }
            } else if (paymentApplication.getString("paymentApplicationId").equals(checkAppl.getString("paymentApplicationId"))) {
                // update existing record in-place
                checkAppl.set("amountApplied", paymentApplication.getBigDecimal("amountApplied"));
                if (debug)     Debug.logInfo("Update paymentApplication record: " + checkAppl.getString("paymentApplicationId") + " with appliedAmount:" + checkAppl.getBigDecimal("amountApplied"), module);
                try {
                    checkAppl.store();
                } catch (GenericEntityException e) {
                    ServiceUtil.returnError(e.getMessage());
                }
            } else    { // two existing records, an updated one added to the existing one
                // add 2 amounts together
                checkAppl.set("amountApplied", paymentApplication.getBigDecimal("amountApplied").
                        add(checkAppl.getBigDecimal("amountApplied")).setScale(DECIMALS,ROUNDING));
                // delete paymentApplication record and update the checkAppls one.
                if (debug) Debug.logInfo("Delete paymentApplication record: " + paymentApplication.getString("paymentApplicationId") + " with appliedAmount:" + paymentApplication.getBigDecimal("amountApplied"), module);
                try {
                    paymentApplication.remove();
                } catch (GenericEntityException e) {
                    ServiceUtil.returnError(e.getMessage());
                }
                // update amount existing record
                if (debug)     Debug.logInfo("Update paymentApplication record: " + checkAppl.getString("paymentApplicationId") + " with appliedAmount:" + checkAppl.getBigDecimal("amountApplied"), module);
                try {
                    checkAppl.store();
                } catch (GenericEntityException e) {
                    ServiceUtil.returnError(e.getMessage());
                }
            }
        } else {
            if (debug) Debug.logInfo("No records found with paymentId,invoiceid..etc probaly changed one of them...", module);
            // create record if ID null;
            if (paymentApplication.get("paymentApplicationId") == null) {
                paymentApplication.set("paymentApplicationId", delegator.getNextSeqId("PaymentApplication"));
                    if (debug) Debug.logInfo("Create new paymentAppication record: " + paymentApplication.getString("paymentApplicationId") + " with appliedAmount:" + paymentApplication.getBigDecimal("amountApplied"), module);
                try {
                    paymentApplication.create();
                } catch (GenericEntityException e) {
                    ServiceUtil.returnError(e.getMessage());
                }
            } else {
                // update existing record (could not be found because a non existing combination of paymentId/invoiceId/invoiceSeqId/ etc... was provided
                if (debug) Debug.logInfo("Update existing paymentApplication record: " + paymentApplication.getString("paymentApplicationId") + " with appliedAmount:" + paymentApplication.getBigDecimal("amountApplied"), module);
                try {
                    paymentApplication.store();
                } catch (GenericEntityException e) {
                    ServiceUtil.returnError(e.getMessage());
                }
            }
        }
        return results;
    }

    public static Map<String, Object> checkPaymentInvoices(DispatchContext dctx, Map<String, Object> context) {
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        String paymentId = (String) context.get("paymentId");
        try {
            GenericValue payment = delegator.findByPrimaryKey("Payment", UtilMisc.toMap("paymentId", paymentId));
            if (payment == null) throw new GenericServiceException("Payment with ID [" + paymentId  + "] not found!");

            List<GenericValue> paymentApplications = payment.getRelated("PaymentApplication");
            if (UtilValidate.isEmpty(paymentApplications)) return ServiceUtil.returnSuccess();

            // TODO: this is inefficient -- instead use HashSet to construct a distinct Set of invoiceIds, then iterate over it and call checkInvoicePaymentAppls
            for (GenericValue paymentApplication : paymentApplications) {
                String invoiceId = paymentApplication.getString("invoiceId");
                if (invoiceId != null) {
                    Map<String, Object> serviceResult = dispatcher.runSync("checkInvoicePaymentApplications", UtilMisc.<String, Object>toMap("invoiceId", invoiceId, "userLogin", userLogin));
                    if (ServiceUtil.isError(serviceResult)) return serviceResult;
                }
            }
            return ServiceUtil.returnSuccess();
        } catch (GenericServiceException se) {
            Debug.logError(se, se.getMessage(), module);
            return ServiceUtil.returnError(se.getMessage());
        } catch (GenericEntityException ee) {
            Debug.logError(ee, ee.getMessage(), module);
            return ServiceUtil.returnError(ee.getMessage());
        }
    }
    public static Map<String, Object> getInvoiceTotal(DispatchContext dctx, Map<String, Object> context) {
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        String invoiceId = (String) context.get("invoiceId");
        Map<String, Object> serviceResult = ServiceUtil.returnSuccess();
        BigDecimal amountTotal =  InvoiceWorker.getInvoiceTotal(delegator, invoiceId);
        serviceResult.put("amountTotal", amountTotal);
        return serviceResult;
    }

	public static Map<String, Object>  sendInvoiceSms(DispatchContext dctx, Map<String, Object> context)  {
        String invoiceId = (String) context.get("invoiceId");
        GenericValue userLogin = (GenericValue) context.get("userLogin");      
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();        
        Map<String, Object> serviceResult;
		try {
			GenericValue invoice = delegator.findByPrimaryKey("Invoice", UtilMisc.toMap("invoiceId", invoiceId));
            if (invoice == null) {
                Debug.logError("Invalid invoice id  " + invoiceId, module);
                return ServiceUtil.returnError("Invalid invoice id  " + invoiceId);            	
            }
            GenericValue invoiceType=delegator.findOne("InvoiceType",UtilMisc.toMap("invoiceTypeId",invoice.getString("invoiceTypeId")) , false);
        	String destinationPartyId = "";
            if ((invoice.getString("invoiceTypeId").equals("SALES_INVOICE") )|| (invoiceType.getString("parentTypeId").equals("SALES_INVOICE") )) {
                destinationPartyId = invoice.getString("partyId");
            }
            if ((invoice.getString("invoiceTypeId").equals("PURCHASE_INVOICE"))|| (invoiceType.getString("parentTypeId").equals("PURCHASE_INVOICE") )) {
                destinationPartyId = invoice.getString("partyIdFrom");
            }
            if (UtilValidate.isEmpty(destinationPartyId)) {
                Debug.logError("Invalid destination party id for invoice " + invoiceId, module);
                return ServiceUtil.returnError("Invalid destination party id for invoice  " + invoiceId);             
            }
            Map<String, Object> getTelParams = FastMap.newInstance();
        	getTelParams.put("partyId", destinationPartyId);
            getTelParams.put("userLogin", userLogin);                    	
            serviceResult = dispatcher.runSync("getPartyTelephone", getTelParams);
            if (ServiceUtil.isError(serviceResult)) {
                return ServiceUtil.returnError(ServiceUtil.getErrorMessage(serviceResult));
            } 
            String contactNumberTo = (String) serviceResult.get("countryCode") + (String) serviceResult.get("contactNumber");            
            Map<String, Object> sendSmsParams = FastMap.newInstance();      
            sendSmsParams.put("contactNumberTo", contactNumberTo);
            if (UtilValidate.isEmpty(invoice.getString("description"))) {
                Debug.logError("Empty description for invoice " + invoiceId, module);
                return ServiceUtil.returnError("Empty description for invoice  " + invoiceId);             
            }            
            sendSmsParams.put("text", invoice.getString("description"));            
            serviceResult  = dispatcher.runSync("sendSms", sendSmsParams);       
            if (ServiceUtil.isError(serviceResult)) {
                Debug.logError(ServiceUtil.getErrorMessage(serviceResult), module);
                return serviceResult;
            }             
        } catch (Exception e) {
            Debug.logError(e, "Problem getting Invoice", module);
            return ServiceUtil.returnError(e.getMessage());
        }
        
        return ServiceUtil.returnSuccess();
    }
	
	public static Map<String, Object> createPaymentAndApplicationForInvoices(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();              
        BigDecimal paymentAmount = (BigDecimal) context.get("amount");
        Locale locale = (Locale) context.get("locale");     
        String paymentMethodType = (String) context.get("paymentMethodTypeId");
        String paymentMethodId = (String) context.get("paymentMethodId");
        String paymentLocationId = (String) context.get("paymentLocationId");                
        String paymentRefNum = (String) context.get("paymentRefNum");        
        Timestamp effectiveDate = (Timestamp) context.get("effectiveDate");
        Timestamp paymentDate = (Timestamp) context.get("paymentDate");
        String paymentPurposeType = (String) context.get("paymentPurposeType");
        String issuingAuthority = (String) context.get("issuingAuthority");
        String issuingAuthorityBranch = (String) context.get("issuingAuthorityBranch");
        String paymentTypeId = (String) context.get("paymentTypeId");
        String finAccountId = (String) context.get("finAccountId");
        //String isDepositWithDrawPayment =(String) context.get("isDepositWithDrawPayment");
       // String finAccountTransTypeId =(String) context.get("finAccountTransTypeId");
        String organizationPartyId =(String) context.get("organizationPartyId");
        String statusId = (String) context.get("statusId");
        String partyId =(String) context.get("partyId");
        String facilityId =(String) context.get("facilityId");
        String comments=(String) context.get("comments");
        Timestamp instrumentDate = (Timestamp) context.get("instrumentDate");
        List invoiceIds =(List) context.get("invoices");
        Debug.log("invoiceIds========="+invoiceIds);
        Map invoiceAmountMap = (Map) context.get("invoiceAmountMap");
        Debug.log("invoiceAmountMap========="+invoiceAmountMap);
        String paymentId = "";
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        try {       	
        	
        	Map<String, Object> paymentCtx = UtilMisc.<String, Object>toMap("paymentTypeId", paymentTypeId);
            paymentCtx.put("paymentMethodTypeId", paymentMethodType);
            paymentCtx.put("partyIdTo", organizationPartyId);
            paymentCtx.put("partyIdFrom", partyId);
            if (!UtilValidate.isEmpty(paymentMethodId)) {
         	   paymentCtx.put("paymentMethodId", paymentMethodId);                       	
            }   
           
            paymentCtx.put("isEnableAcctg", context.get("isEnableAcctg"));
            if (!UtilValidate.isEmpty(paymentLocationId) ) {
                paymentCtx.put("paymentLocation", paymentLocationId);                        	
            } 
            if (!UtilValidate.isEmpty(paymentRefNum) ) {
                paymentCtx.put("paymentRefNum", paymentRefNum);                        	
            }
            if (!UtilValidate.isEmpty(issuingAuthority) ) {
                paymentCtx.put("issuingAuthority", issuingAuthority);                        	
            }
            if (!UtilValidate.isEmpty(effectiveDate) ) {
                paymentCtx.put("effectiveDate", effectiveDate);                        	
            }
            if (!UtilValidate.isEmpty(issuingAuthorityBranch) ) {
                paymentCtx.put("issuingAuthorityBranch", issuingAuthorityBranch);                        	
            }
            if (!UtilValidate.isEmpty(paymentPurposeType)) {
                paymentCtx.put("paymentPurposeType", paymentPurposeType);                        	
            }
            if (UtilValidate.isNotEmpty(paymentDate) ) {
                paymentCtx.put("paymentDate", paymentDate);                        	
            }
            else{
            	paymentCtx.put("paymentDate", UtilDateTime.nowTimestamp());
            }
            paymentCtx.put("instrumentDate",instrumentDate);
            paymentCtx.put("statusId", statusId);            
            paymentCtx.put("amount", paymentAmount);
            paymentCtx.put("userLogin", userLogin);
            paymentCtx.put("comments", comments);
            paymentCtx.put("facilityId", facilityId);
            if (UtilValidate.isNotEmpty(finAccountId)) {
				paymentCtx.put("finAccountId", finAccountId);
			}
            paymentCtx.put("createdByUserLogin", userLogin.getString("userLoginId"));
            paymentCtx.put("lastModifiedByUserLogin",  userLogin.getString("userLoginId"));
            paymentCtx.put("createdDate", UtilDateTime.nowTimestamp());
            paymentCtx.put("lastModifiedDate", UtilDateTime.nowTimestamp());
            Map<String, Object> paymentResult = dispatcher.runSync("createPayment", paymentCtx);
            if (ServiceUtil.isError(paymentResult)) {
            	Debug.logError(paymentResult.toString(), module);    			
                return ServiceUtil.returnError(null, null, null, paymentResult);
            }
            paymentId = (String)paymentResult.get("paymentId");
            }catch (Exception e) {
            Debug.logError(e, e.toString(), module);
            return ServiceUtil.returnError(e.toString());
        }
        Debug.log("paymentAmount============="+paymentAmount);
        BigDecimal amountAppliedRunningTotal = paymentAmount;
        for( int i=0; i< invoiceIds.size(); i++){
        	String invoiceId = (String)invoiceIds.get(i);
        	Map invoicePaymentInfoMap =FastMap.newInstance();
			BigDecimal outstandingAmount =BigDecimal.ZERO;
			invoicePaymentInfoMap.put("invoiceId", invoiceId);
			invoicePaymentInfoMap.put("userLogin",userLogin);
			try{
				Map<String, Object> getInvoicePaymentInfoListResult = dispatcher.runSync("getInvoicePaymentInfoList", invoicePaymentInfoMap);
				if (ServiceUtil.isError(getInvoicePaymentInfoListResult)) {
		            	Debug.logError(getInvoicePaymentInfoListResult.toString(), module);    			
		                return ServiceUtil.returnError(null, null, null, getInvoicePaymentInfoListResult);
		            }
				Map invoicePaymentInfo = (Map)((List)getInvoicePaymentInfoListResult.get("invoicePaymentInfoList")).get(0);
				outstandingAmount = (BigDecimal)invoicePaymentInfo.get("outstandingAmount");
			}catch (GenericServiceException e) {
				// TODO: handle exception
				 Debug.logError(e, e.toString(), module);
		         return ServiceUtil.returnError(e.toString());
				
			}
        	 Map<String, Object> invoiceCtx = UtilMisc.<String, Object>toMap("invoiceId", invoiceId);
             invoiceCtx.put("userLogin", userLogin);
             invoiceCtx.put("statusId","INVOICE_READY");
             
             try{
             	Map<String, Object> invoiceResult = dispatcher.runSync("setInvoiceStatus",invoiceCtx);
             	if (ServiceUtil.isError(invoiceResult)) {
             		Debug.logError(invoiceResult.toString(), module);
                     return ServiceUtil.returnError(null, null, null, invoiceResult);
                 }
             	
             }catch(GenericServiceException e){
             	 Debug.logError(e, e.toString(), module);
                 return ServiceUtil.returnError(e.toString());
             }
             Map<String, Object> invoiceApplCtx = UtilMisc.<String, Object>toMap("invoiceId", invoiceId);
             invoiceApplCtx.put("userLogin", userLogin);
             invoiceApplCtx.put("paymentId",paymentId);                       
             try{
            	
            	 Map<String, Object> result =dispatcher.runSync("getInvoiceTotal", UtilMisc.toMap("invoiceId",invoiceId ,"userLogin" ,userLogin));
            	 if (ServiceUtil.isError(result)) {
            		 Debug.logError(result.toString(), module);
                     return ServiceUtil.returnError(null, null, null, result);
                 }
            	 Debug.log("invoiceId================="+invoiceId);
            	 invoiceApplCtx.put("amountApplied", amountAppliedRunningTotal);
                 if(UtilValidate.isNotEmpty(invoiceAmountMap)){
                	 Debug.log("ivAmount================="+invoiceAmountMap.get(invoiceId));
                	 invoiceApplCtx.put("amountApplied", (BigDecimal) invoiceAmountMap.get(invoiceId));
            	 }
             	Map<String, Object> invoiceApplResult = dispatcher.runSync("createPaymentApplication",invoiceApplCtx);
             	
             	if (ServiceUtil.isError(invoiceApplResult)) {
             		 Debug.logError(invoiceApplResult.toString(), module);
                     return ServiceUtil.returnError(null, null, null, invoiceApplResult);
                 }
             	/*amountAppliedRunningTotal = amountAppliedRunningTotal.subtract(outstandingAmount);
             	if( amountAppliedRunningTotal.compareTo(BigDecimal.ZERO) <= 0){
             		break;
             		
             	}*/
             }catch(GenericServiceException e){
             	Debug.logError(e, e.toString(), module);
                 return ServiceUtil.returnError(e.toString());
             }
        	
        }  
        Map<String, Object> result = ServiceUtil.returnSuccess();        
        result.put("paymentId", paymentId);
     return result;
	}
	public static Map<String, Object> settleInvoiceAndPayments(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();              
        String invoiceId = (String) context.get("invoiceId");
        Locale locale = (Locale) context.get("locale");        
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        GenericValue invoiceDetails = null;
        Map<String, Object> result = ServiceUtil.returnSuccess();
        try {
        	
        	if(!UtilValidate.isEmpty(invoiceId)){
        		invoiceDetails = delegator.findOne("Invoice", UtilMisc.toMap("invoiceId",invoiceId), false);
        	}
        	if(UtilValidate.isEmpty(invoiceDetails)){
        		Debug.logError("Not found the invoice with id :"+invoiceId, module);
                return ServiceUtil.returnError("Not found the invoice with id :"+invoiceId);
        	}
        	result =dispatcher.runSync("getInvoiceTotal", UtilMisc.toMap("invoiceId",invoiceId ,"userLogin" ,userLogin));
        	BigDecimal amountAppliedRunningTotal = (BigDecimal)result.get("amountTotal");        	
        	List<GenericValue> payments = PaymentWorker.getNotAppliedPaymentsForParty(dctx ,UtilMisc.toMap("partyIdFrom",invoiceDetails.get("partyId"),"partyIdTo",invoiceDetails.get("partyIdFrom")));
        	//filter out the SecurityDeposit  payment  from not applied payments list
        	EntityConditionList<EntityExpr> dateCondition = EntityCondition.makeCondition(UtilMisc.toList(
                    EntityCondition.makeCondition("effectiveDate", EntityOperator.EQUALS, null),
                    EntityCondition.makeCondition("effectiveDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp())), EntityOperator.OR);
        	payments = EntityUtil.filterByCondition(payments, dateCondition);
        	String securityDepositPmtType = "SECURITYDEPSIT_PAYIN";
        	payments = EntityUtil.filterByCondition(payments, EntityCondition.makeCondition("paymentTypeId",EntityOperator.NOT_EQUAL , securityDepositPmtType));
        	if (UtilValidate.isNotEmpty(payments)) {	
	        	Map<String, Object> invoiceCtx = UtilMisc.<String, Object>toMap("invoiceId", invoiceId);
	             invoiceCtx.put("userLogin", userLogin);
	             invoiceCtx.put("statusId","INVOICE_READY");
	             try{
	             	Map<String, Object> invoiceResult = dispatcher.runSync("setInvoiceStatus",invoiceCtx);
	             	if (ServiceUtil.isError(invoiceResult)) {
	             		Debug.logError(invoiceResult.toString(), module);
	                     return ServiceUtil.returnError(null, null, null, invoiceResult);
	                 }	             	
	             }catch(GenericServiceException e){
	             	 Debug.logError(e, e.toString(), module);
	                 return ServiceUtil.returnError(e.toString());
	             }        		
        	}
        	for( GenericValue payment : payments){
	        	 String paymentId = payment.getString("paymentId");	        	
	        	 Map invoicePaymentInfoMap =FastMap.newInstance();	             
	             Map<String, Object> invoiceApplCtx = UtilMisc.<String, Object>toMap("invoiceId", invoiceId);
	             invoiceApplCtx.put("userLogin", userLogin);
	             invoiceApplCtx.put("paymentId",paymentId);                       
	             try{	
	            	 invoicePaymentInfoMap.put("userLogin", userLogin);
	            	 invoicePaymentInfoMap.put("invoiceId",invoiceId); 
	            	 Map<String, Object> getInvoicePaymentInfoListResult = dispatcher.runSync("getInvoicePaymentInfoList", invoicePaymentInfoMap);
					 if (ServiceUtil.isError(getInvoicePaymentInfoListResult)) {
			            	Debug.logError(getInvoicePaymentInfoListResult.toString(), module);    			
			                return ServiceUtil.returnError(null, null, null, getInvoicePaymentInfoListResult);
			            }
					Map invoicePaymentInfo = (Map)((List)getInvoicePaymentInfoListResult.get("invoicePaymentInfoList")).get(0);
					BigDecimal outstandingAmount = (BigDecimal)invoicePaymentInfo.get("outstandingAmount");
					if(outstandingAmount.compareTo(BigDecimal.ZERO) == 0){
						break;
					}
					 invoiceApplCtx.put("invoiceId", invoiceId);
					 invoiceApplCtx.put("amountApplied", PaymentWorker.getPaymentNotApplied(payment));
	            	 Map<String, Object> invoiceApplResult = dispatcher.runSync("createPaymentApplication",invoiceApplCtx);	             	
	             	if (ServiceUtil.isError(invoiceApplResult)) {
	             		 Debug.logError(invoiceApplResult.toString(), module);
	                     return ServiceUtil.returnError(null, null, null, invoiceApplResult);
	                 }
	             	Map<String, Object> checkApplResult = dispatcher.runSync("checkPaymentInvoices",UtilMisc.toMap("userLogin", userLogin,"paymentId", paymentId));		             	
	              	if (ServiceUtil.isError(checkApplResult)) {
	              		 Debug.logError(checkApplResult.toString(), module);
	                      return ServiceUtil.returnError(null, null, null, checkApplResult);
	                  } 		             	
	             }catch(GenericServiceException e){
	             	Debug.logError(e, e.toString(), module);
	                 return ServiceUtil.returnError(e.toString());
	             }        	
        } 
         
     }catch(Exception e){
      	Debug.logError(e, e.toString(), module);
        return ServiceUtil.returnError(e.toString());
    }
     result = ServiceUtil.returnSuccess();        
     result.put("invoiceId", invoiceId);
     return result;
	}
	

	
	public static Map<String, Object> createTaxInvoiceSequence(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();              
        String invoiceId = (String) context.get("invoiceId");
        Locale locale = (Locale) context.get("locale");        
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        GenericValue invoiceDetails = null;
        Map<String, Object> result = ServiceUtil.returnSuccess();
        GenericValue tenantConfigEnableTaxInvSeq;
        
       // Debug.log("invoiceId============="+invoiceId);
        
        try {
        	
        	Boolean enableTaxInvSeq  = Boolean.FALSE;
    	    tenantConfigEnableTaxInvSeq = delegator.findOne("TenantConfiguration", UtilMisc.toMap("propertyTypeEnumId","LMS", "propertyName","enableTaxInvSeq"), false);
       		if (UtilValidate.isNotEmpty(tenantConfigEnableTaxInvSeq) && (tenantConfigEnableTaxInvSeq.getString("propertyValue")).equals("Y")) {
       			enableTaxInvSeq = Boolean.TRUE;
       		}
       		else{
       			tenantConfigEnableTaxInvSeq = delegator.findOne("TenantConfiguration", UtilMisc.toMap("propertyTypeEnumId","INVOICE_SQUENCE", "propertyName","enableInvoiceSequence"), false);
	       		if (UtilValidate.isNotEmpty(tenantConfigEnableTaxInvSeq) && (tenantConfigEnableTaxInvSeq.getString("propertyValue")).equals("Y")) {
	       			enableTaxInvSeq = Boolean.TRUE;
	       		}
       		}
       		
       	 //Debug.log("enableTaxInvSeq============="+enableTaxInvSeq);
       		Timestamp invDate=null;
       		String purPoseTypeId = "";
       		if(enableTaxInvSeq && UtilValidate.isNotEmpty(invoiceId)){
       			List<GenericValue> invoiceItems = delegator.findList("Invoice", EntityCondition.makeCondition("invoiceId", EntityOperator.EQUALS, invoiceId), null, null, null, false);
       			List invoiceItemTypeIds = EntityUtil.getFieldListFromEntityList(invoiceItems, "invoiceTypeId", true);
       			if(UtilValidate.isNotEmpty((EntityUtil.getFirst(invoiceItems)).getTimestamp("dueDate"))){
       			    invDate = (EntityUtil.getFirst(invoiceItems)).getTimestamp("dueDate");
       			}    
       			if(UtilValidate.isEmpty(invDate)){
       				invDate = (EntityUtil.getFirst(invoiceItems)).getTimestamp("invoiceDate");
       			}
       			if(UtilValidate.isNotEmpty((EntityUtil.getFirst(invoiceItems)).getString("purposeTypeId"))){
       				purPoseTypeId = (EntityUtil.getFirst(invoiceItems)).getString("purposeTypeId");
       				purPoseTypeId = purPoseTypeId.trim();
       			}
       			
       		   // Debug.log("invDate============="+invDate);
       			
       			String partyId ="";
       			String prefix ="";
       			String orderId = "";
       			String shipmentId = "";
       			GenericValue shipments = null;
       			List<GenericValue> orderAssoc = FastList.newInstance();
       			String invoiceParty = (EntityUtil.getFirst(invoiceItems)).getString("partyIdFrom");
       			GenericValue obPartyDetails = delegator.findOne("Party", UtilMisc.toMap("partyId", invoiceParty), false);
                String externalId = obPartyDetails.getString("externalId");
       			shipmentId = (EntityUtil.getFirst(invoiceItems)).getString("shipmentId");
       			String indentTypeId = "D";
       			String boSequnce = "";
       			String roSequnce ="";
       			//if(UtilValidate.isNotEmpty(shipmentId)){
	       			if(((EntityUtil.getFirst(invoiceItems)).getString("invoiceTypeId")).equals("PURCHASE_INVOICE")){
	       				partyId = (EntityUtil.getFirst(invoiceItems)).getString("costCenterId");
	       				
	       			/*//=======================New Sequence from Role========================
		       			 try {
		 					List conditions = FastList.newInstance();
		 					conditions.add(EntityCondition.makeCondition("invoiceId", EntityOperator.EQUALS, invoiceId));
		 					conditions.add(EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "SUPPLIER_AGENT"));
		 			    	List <GenericValue> InvoiceRole = delegator.findList("InvoiceRole", EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, false);
		 			    	if(UtilValidate.isNotEmpty(InvoiceRole)){
		 			    		partyId = (EntityUtil.getFirst(InvoiceRole)).getString("partyId");
		 			    		
		 			    	}
		 			 } catch (Exception e) {
		 				Debug.logError("Problem in Invoice Role", module);
		 			 }*/
      				//=========================================================================
	       				
	        			shipments= delegator.findOne("Shipment",UtilMisc.toMap("shipmentId", shipmentId), true);
	        			if(UtilValidate.isNotEmpty(shipments)){
	        				orderId = shipments.getString("primaryOrderId");
	        			}
	        			//Debug.log("invDate============="+invDate);
	        			try{
	           			orderAssoc = delegator.findList("OrderAssoc", EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId), UtilMisc.toSet("toOrderId"), null, null, false);
		           			if(UtilValidate.isNotEmpty(orderAssoc)){
		           				orderId = EntityUtil.getFirst(orderAssoc).getString("toOrderId");
		           			}
	        			}catch(Exception e){
	        				Debug.logError("Problem in OrderAssoc", module);
	        			}
	           			
	                	prefix="PI";
	       			}
	       			
	       			//Debug.log("prefix============="+prefix);
	       			
	       			if(((EntityUtil.getFirst(invoiceItems)).getString("invoiceTypeId")).equals("SALES_INVOICE")){
	       				partyId = (EntityUtil.getFirst(invoiceItems)).getString("costCenterId");
	       				
	       				/*
	       				//=======================New Sequence from Role========================
	       				
			       			 try {
			 					List conditions = FastList.newInstance();
			 					conditions.add(EntityCondition.makeCondition("invoiceId", EntityOperator.EQUALS, invoiceId));
			 					conditions.add(EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "CUSTOMER_AGENT"));
			 			    	List <GenericValue> InvoiceRole = delegator.findList("InvoiceRole", EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, false);
			 			    	if(UtilValidate.isNotEmpty(InvoiceRole)){
			 			    		partyId = (EntityUtil.getFirst(InvoiceRole)).getString("partyId");
			 			    		
			 			    	}
			 			 } catch (Exception e) {
			 				Debug.logError("Problem in Invoice Role", module);
			 			 }
	       				
	       				
	       				//=========================================================================
	       				*/
	       				shipments= delegator.findOne("Shipment",UtilMisc.toMap("shipmentId", shipmentId), true);
	       				if(UtilValidate.isNotEmpty(shipments)){
	       					orderId = shipments.getString("primaryOrderId");
	       				}
	        			//Debug.log("orderId============="+orderId);
	        			try{
	        				orderAssoc = delegator.findList("OrderAssoc", EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId), UtilMisc.toSet("toOrderId"), null, null, false);
		                	if(UtilValidate.isNotEmpty(orderAssoc)){
		                		orderId = EntityUtil.getFirst(orderAssoc).getString("toOrderId");
		       				}
	        			}catch(Exception e){
	        				Debug.logError("Problem in OrderAssoc", module);
	        			}
	                	prefix="SI";
	       			}
	       			
	       			//Debug.log("prefix============="+prefix);
	       			if(UtilValidate.isNotEmpty(shipmentId)){
	       				
	      				if(UtilValidate.isEmpty(orderAssoc) && ((EntityUtil.getFirst(invoiceItems)).getString("invoiceTypeId")).equals("SALES_INVOICE")){
	       					
	       				    List condList = FastList.newInstance();
			                condList.add(EntityCondition.makeCondition("shipmentId", EntityOperator.EQUALS, shipmentId));
							EntityCondition condExpr1 = EntityCondition.makeCondition(condList, EntityOperator.AND);
							List<GenericValue> OrderHeader = delegator.findList("OrderHeader", condExpr1, null, null, null, false);
	       					
							orderId = (EntityUtil.getFirst(OrderHeader)).getString("orderId");
							
						    condList.clear();
			                condList.add(EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId));
			                condList.add(EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "ON_BEHALF_OF"));
							EntityCondition condExpr2 = EntityCondition.makeCondition(condList, EntityOperator.AND);
							List<GenericValue> orderRolesDetails = delegator.findList("OrderRole", condExpr2, null, null, null, false);
			                if(UtilValidate.isNotEmpty(orderRolesDetails)){
			                	indentTypeId = "O";
			                }
	       					
	       				}else{
			       		    List condList = FastList.newInstance();
			                condList.add(EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId));
			                condList.add(EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "ON_BEHALF_OF"));
							EntityCondition condExpr1 = EntityCondition.makeCondition(condList, EntityOperator.AND);
							List<GenericValue> orderRolesDetails = delegator.findList("OrderRole", condExpr1, null, null, null, false);
			                if(UtilValidate.isNotEmpty(orderRolesDetails)){
			                	indentTypeId = "O";
			                }
	       				}
	       				
	       			   /* List condList = FastList.newInstance();
		                condList.add(EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId));
		                condList.add(EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "ON_BEHALF_OF"));
						EntityCondition condExpr1 = EntityCondition.makeCondition(condList, EntityOperator.AND);
						List<GenericValue> orderRolesDetails = delegator.findList("OrderRole", condExpr1, null, null, null, false);
		                if(UtilValidate.isNotEmpty(orderRolesDetails)){
		                	indentTypeId = "O";
		                }*/
	                
	                //Debug.log("indentTypeId============="+indentTypeId);
	       			GenericValue partyBOs = delegator.findOne("Party", UtilMisc.toMap("partyId", partyId), false);
	                boSequnce = partyBOs.getString("externalId");
	                
	                List conditionList = FastList.newInstance();
	     			conditionList.add(EntityCondition.makeCondition("partyIdTo", EntityOperator.EQUALS, partyId));
	     			conditionList.add(EntityCondition.makeCondition("roleTypeIdTo", EntityOperator.EQUALS, "ORGANIZATION_UNIT" ));
	     	        conditionList.add(EntityCondition.makeCondition("roleTypeIdFrom", EntityOperator.EQUALS, "PARENT_ORGANIZATION"));
	     			conditionList.add(EntityCondition.makeCondition("fromDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.getDayEnd(invDate)));
	    			conditionList.add(EntityCondition.makeCondition(EntityCondition.makeCondition("thruDate", EntityOperator.EQUALS, null), EntityOperator.OR, 
	    			EntityCondition.makeCondition("thruDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.getDayStart(invDate))));
	    			    EntityCondition condition = EntityCondition.makeCondition(conditionList, EntityOperator.AND);  	
	                
	       			List<GenericValue> partyRelations = delegator.findList("PartyRelationship", condition, null, null, null, false);
	       		    String partyIdFrom = EntityUtil.getFirst(partyRelations).getString("partyIdFrom");
	    			GenericValue partyROs = delegator.findOne("Party", UtilMisc.toMap("partyId", partyIdFrom), false);
	                roSequnce = partyROs.getString("externalId");
       			}
	       			
	       			//Debug.log("roSequnce============="+roSequnce);
       			Map finYearContext = FastMap.newInstance();
   				finYearContext.put("onlyIncludePeriodTypeIdList", UtilMisc.toList("FISCAL_YEAR"));
   				finYearContext.put("organizationPartyId", "Company");
   				finYearContext.put("userLogin", userLogin);
   				finYearContext.put("findDate", invDate);
   				finYearContext.put("excludeNoOrganizationPeriods", "Y");
   				List customTimePeriodList = FastList.newInstance();
   				Map resultCtx = FastMap.newInstance();
   				try{
   					resultCtx = dispatcher.runSync("findCustomTimePeriods", finYearContext);
   					if(ServiceUtil.isError(resultCtx)){
   						Debug.logError("Problem in fetching financial year ", module);
   						return ServiceUtil.returnError("Problem in fetching financial year ");
   					}
   				}catch(GenericServiceException e){
   					Debug.logError(e, module);
   					return ServiceUtil.returnError(e.getMessage());
   				}
   				customTimePeriodList = (List)resultCtx.get("customTimePeriodList");
   				String finYearId = "";
   				GenericValue customTimePeriod =null;
   				if(UtilValidate.isNotEmpty(customTimePeriodList)){
   					customTimePeriod = EntityUtil.getFirst(customTimePeriodList);
   					finYearId = (String)customTimePeriod.get("customTimePeriodId");
   				}
       			/*if(invoiceItemTypeIds.contains("BED_SALE")){
       				GenericValue billOfSale = delegator.makeValue("BillOfSaleInvoiceSequence");
    				billOfSale.put("billOfSaleTypeId", "EXCISE_INV");
    				billOfSale.put("invoiceId", invoiceId);
    				billOfSale.put("finYearId", finYearId);
    				billOfSale.put("invoiceDueDate", invDate);
    				delegator.setNextSubSeqId(billOfSale, "sequenceId", 10, 1);
    	            delegator.create(billOfSale);

       			}
       			else if(invoiceItemTypeIds.contains("INV_RAWPROD_ITEM") || invoiceItemTypeIds.contains("CST_SALE")){
       				GenericValue billOfSale = delegator.makeValue("BillOfSaleInvoiceSequence");
    				billOfSale.put("billOfSaleTypeId", "VAT_INV");
    				billOfSale.put("invoiceId", invoiceId);
    				billOfSale.put("finYearId", finYearId);
    				billOfSale.put("invoiceDueDate", invDate);
    				delegator.setNextSubSeqId(billOfSale, "sequenceId", 10, 1);
    	            delegator.create(billOfSale);
       			}*/
       			if(invoiceItemTypeIds.contains("PURCHASE_INVOICE")){
       				GenericValue billOfSale = delegator.makeValue("BillOfSaleInvoiceSequence");
       				
       				//Debug.log("billOfSale============="+billOfSale);
       				
    				billOfSale.put("billOfSaleTypeId", "PUR_INV_SQUENCE");
    				billOfSale.put("invoiceId", invoiceId);
    				billOfSale.put("finYearId", finYearId);
    				billOfSale.put("partyId", partyId);
    				billOfSale.put("invoiceDueDate", invDate);
                    if(UtilValidate.isNotEmpty(purPoseTypeId) && (purPoseTypeId.equals("DIES_AND_CHEM_SALE") || purPoseTypeId.equals("DEPOT_DIES_CHEM_SALE")))
                     billOfSale.put("productCategoryId", "DC");
                    else	
                     billOfSale.put("productCategoryId", "Y");
    				delegator.setNextSubSeqId(billOfSale, "sequenceId", 6, 1);
    				billOfSale.put("indentTypeId",indentTypeId);
    	            delegator.create(billOfSale);
		            String sequenceId = (String) billOfSale.get("sequenceId");
		            String productCategoryId = (String) billOfSale.get("productCategoryId");
    				billOfSale.put("invoiceSequence", prefix+"/"+roSequnce+"/"+boSequnce+"/"+productCategoryId+"/"+indentTypeId+"/"+UtilDateTime.toDateString(customTimePeriod.getDate("fromDate"),"yy")+"-"+UtilDateTime.toDateString(customTimePeriod.getDate("thruDate"),"yy"+"/"+sequenceId));
    				delegator.createOrStore(billOfSale); 
   			    }
   			    if(invoiceItemTypeIds.contains("SALES_INVOICE")){
       				GenericValue billOfSale = delegator.makeValue("BillOfSaleInvoiceSequence");
       				
       				//Debug.log("billOfSale===sale=========="+billOfSale);
    				billOfSale.put("billOfSaleTypeId", "SALE_INV_SQUENCE");
    				billOfSale.put("invoiceId", invoiceId);
    				billOfSale.put("partyId", partyId);
    				billOfSale.put("finYearId", finYearId);
    				billOfSale.put("invoiceDueDate", invDate);
    				if(UtilValidate.isNotEmpty(purPoseTypeId) && (purPoseTypeId.equals("DIES_AND_CHEM_SALE") || purPoseTypeId.equals("DEPOT_DIES_CHEM_SALE")))
                    billOfSale.put("productCategoryId", "DC");
                    else	
                    billOfSale.put("productCategoryId", "Y");
    				delegator.setNextSubSeqId(billOfSale, "sequenceId", 6, 1);
    				billOfSale.put("indentTypeId",indentTypeId);
    	            delegator.create(billOfSale);
		            String sequenceId = (String) billOfSale.get("sequenceId");
		            String productCategoryId = (String) billOfSale.get("productCategoryId");
    				billOfSale.put("invoiceSequence", prefix+"/"+roSequnce+"/"+boSequnce+"/"+productCategoryId+"/"+indentTypeId+"/"+UtilDateTime.toDateString(customTimePeriod.getDate("fromDate"),"yy")+"-"+UtilDateTime.toDateString(customTimePeriod.getDate("thruDate"),"yy"+"/"+sequenceId));
    				delegator.createOrStore(billOfSale);
   			   }
   			   if(invoiceItemTypeIds.contains("OBINVOICE_IN")){
    				GenericValue billOfSale = delegator.makeValue("BillOfSaleInvoiceSequence");
 			    	billOfSale.put("billOfSaleTypeId", "OB_INV_SQUENCE");
 				    billOfSale.put("invoiceId", invoiceId);
 				    billOfSale.put("partyId", invoiceParty);
 				    billOfSale.put("finYearId", finYearId);
 				    billOfSale.put("invoiceDueDate", invDate);
 				    billOfSale.put("productCategoryId", "Y");
 				    delegator.setNextSubSeqId(billOfSale, "sequenceId", 6, 1);
 				    billOfSale.put("indentTypeId",indentTypeId);
 	                delegator.create(billOfSale);
 	                billOfSale.put("invoiceSequence", "OB"+externalId+"_"+invoiceId);
   				    delegator.createOrStore(billOfSale);
    			}
       			
       		}
        }catch(Exception e){
        	Debug.logError(e, e.toString(), module);
        	return ServiceUtil.returnError(e.toString());
        }
        return result;
	}
	
	
	
	public static Map<String, Object> createCreditNoteOrDebitNoteForInvoice(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();              
        Locale locale = (Locale) context.get("locale");     
        String paymentTypeId = (String) context.get("paymentTypeId");
        String paymentMethodTypeId = (String) context.get("paymentMethodTypeId");
        String paymentMethodId = (String) context.get("paymentMethodId");
        String paymentPurposeType = (String) context.get("paymentPurposeType");
        String paymentLocationId = (String) context.get("paymentLocationId");                
        String paymentRefNum = (String) context.get("paymentRefNum");        
        Timestamp effectiveDate = (Timestamp) context.get("effectiveDate");
        Timestamp paymentDate = (Timestamp) context.get("paymentDate");
        String issuingAuthority = (String) context.get("issuingAuthority");
        String issuingAuthorityBranch = (String) context.get("issuingAuthorityBranch");
        String organizationPartyId =(String) context.get("organizationPartyId");
        String statusId = (String) context.get("statusId");
        String partyId =(String) context.get("partyId");
        String facilityId =(String) context.get("facilityId");
        String finAccountId =(String) context.get("finAccountId");
        String comments=(String) context.get("comments");
        Timestamp instrumentDate = (Timestamp) context.get("instrumentDate");
        List invoiceIds =(List) context.get("invoiceIds");
        
        String paymentId = "";
        String partyIdFrom="";
        String partyIdTo="";
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Map<String, Object> result = ServiceUtil.returnSuccess(); 
        List paymentsList =FastList.newInstance();
       
        for( int i=0; i< invoiceIds.size(); i++){
        	String invoiceId = (String)invoiceIds.get(i);
        	Map invoicePaymentInfoMap =FastMap.newInstance();
			BigDecimal outstandingAmount =BigDecimal.ZERO;
			BigDecimal amountAppliedRunningTotal = BigDecimal.ZERO;
			  BigDecimal paymentAmount=BigDecimal.ZERO;
			invoicePaymentInfoMap.put("invoiceId", invoiceId);
			invoicePaymentInfoMap.put("userLogin",userLogin);
			try{
				Map<String, Object> getInvoicePaymentInfoListResult = dispatcher.runSync("getInvoicePaymentInfoList", invoicePaymentInfoMap);
				if (ServiceUtil.isError(getInvoicePaymentInfoListResult)) {
		            	Debug.logError(getInvoicePaymentInfoListResult.toString(), module);    			
		                return ServiceUtil.returnError(null, null, null, getInvoicePaymentInfoListResult);
		            }
				Map invoicePaymentInfo = (Map)((List)getInvoicePaymentInfoListResult.get("invoicePaymentInfoList")).get(0);
				outstandingAmount = (BigDecimal)invoicePaymentInfo.get("outstandingAmount");
				if(outstandingAmount.compareTo(BigDecimal.ZERO) == 0){
					break;
				}
			  paymentAmount=outstandingAmount;
		      amountAppliedRunningTotal = paymentAmount;
			}catch (GenericServiceException e) {
				// TODO: handle exception
				 Debug.logError(e, e.toString(), module);
		         return ServiceUtil.returnError(e.toString());
				
			}
		try{ 
			GenericValue invoice= delegator.findOne("Invoice",UtilMisc.toMap("invoiceId", invoiceId), true);
			if (UtilValidate.isEmpty(invoice)) {
				Debug.logError("Invoice doesn't exists with Id: " + invoiceId,module);
				return ServiceUtil.returnError("Invoice doesn't exists with Id: "	+ invoiceId);
			}
			Map<String, Object> paymentCtx = UtilMisc.<String, Object>toMap("paymentTypeId", paymentTypeId);
            paymentCtx.put("paymentMethodTypeId", paymentMethodTypeId);
			if (UtilValidate.isNotEmpty(invoice)) {
				partyIdFrom = invoice.getString("partyId");
				partyIdTo=invoice.getString("partyIdFrom");
				if (!UtilValidate.isEmpty(effectiveDate) ) {
	                paymentCtx.put("effectiveDate",invoice.getTimestamp("dueDate"));                        	
	            }
				if (UtilValidate.isNotEmpty(paymentDate) ) {
	                paymentCtx.put("paymentDate",invoice.getTimestamp("invoiceDate"));    
	            }
			   }
            paymentCtx.put("partyIdTo", partyIdTo);
            paymentCtx.put("partyIdFrom", partyIdFrom);
            paymentRefNum=invoiceId;
            if (!UtilValidate.isEmpty(paymentMethodId)) {
         	   paymentCtx.put("paymentMethodId", paymentMethodId);                       	
            }   
            paymentCtx.put("isEnableAcctg", context.get("isEnableAcctg"));
            if (!UtilValidate.isEmpty(paymentLocationId) ) {
                paymentCtx.put("paymentLocation", paymentLocationId);                        	
            } 
            if (!UtilValidate.isEmpty(paymentRefNum) ) {
                paymentCtx.put("paymentRefNum", paymentRefNum);                        	
            }
            if (!UtilValidate.isEmpty(issuingAuthority) ) {
                paymentCtx.put("issuingAuthority", issuingAuthority);                        	
            }
            if (!UtilValidate.isEmpty(issuingAuthorityBranch) ) {
                paymentCtx.put("issuingAuthorityBranch", issuingAuthorityBranch);                        	
            }
            if (!UtilValidate.isEmpty(paymentPurposeType)) {
                paymentCtx.put("paymentPurposeType", paymentPurposeType);                        	
            }
            paymentCtx.put("instrumentDate",instrumentDate);
            paymentCtx.put("statusId", statusId);            
            paymentCtx.put("amount", paymentAmount);
            paymentCtx.put("userLogin", userLogin);
            paymentCtx.put("comments", comments);
            paymentCtx.put("facilityId", facilityId);
            paymentCtx.put("createdByUserLogin", userLogin.getString("userLoginId"));
            paymentCtx.put("lastModifiedByUserLogin",  userLogin.getString("userLoginId"));
            paymentCtx.put("createdDate", UtilDateTime.nowTimestamp());
            paymentCtx.put("lastModifiedDate", UtilDateTime.nowTimestamp());
			if(UtilValidate.isNotEmpty(finAccountId)){
				paymentCtx.put("finAccountId", finAccountId);
			}
            Map<String, Object> paymentResult = dispatcher.runSync("createPayment", paymentCtx);
            if (ServiceUtil.isError(paymentResult)) {
            	Debug.logError(paymentResult.toString(), module);    			
                return ServiceUtil.returnError(null, null, null, paymentResult);
            }
            paymentId = (String)paymentResult.get("paymentId");
            try {
            	GenericValue payment = delegator.findOne("Payment",UtilMisc.toMap("paymentId",paymentId) , false);
            	 statusId = null;
            	if(UtilValidate.isNotEmpty(payment)){
            		if(UtilAccounting.isReceipt(payment)){
            			statusId = "PMNT_RECEIVED";
            		}
            		if(UtilAccounting.isDisbursement(payment)){
            			statusId = "PMNT_SENT";
            		}
            	}
            	Map<String, Object> setPaymentStatusMap = UtilMisc.<String, Object>toMap("userLogin", userLogin);
	        	setPaymentStatusMap.put("paymentId", paymentId);
	        	setPaymentStatusMap.put("statusId", statusId);
	        	if(UtilValidate.isNotEmpty(finAccountId)){
	        		setPaymentStatusMap.put("finAccountId", finAccountId);
	        	}
	            Map<String, Object> pmntResults = dispatcher.runSync("setPaymentStatus", setPaymentStatusMap);
	            if (ServiceUtil.isError(pmntResults)) {
  	            	Debug.logError(pmntResults.toString(), module);
  	            	return ServiceUtil.returnError(null, null, null, pmntResults);
  	            }
            } catch (Exception e) {
                Debug.logError(e, "Unable to change Payment Status", module);
            }
            paymentsList.add(paymentId);
            }catch (Exception e) {
            Debug.logError(e, e.toString(), module);
            return ServiceUtil.returnError(e.toString());
            }
        	 Map<String, Object> invoiceCtx = UtilMisc.<String, Object>toMap("invoiceId", invoiceId);
             invoiceCtx.put("userLogin", userLogin);
             invoiceCtx.put("statusId","INVOICE_READY");
             try{
             	Map<String, Object> invoiceResult = dispatcher.runSync("setInvoiceStatus",invoiceCtx);
             	if (ServiceUtil.isError(invoiceResult)) {
             		Debug.logError(invoiceResult.toString(), module);
                     return ServiceUtil.returnError(null, null, null, invoiceResult);
                 }
             }catch(GenericServiceException e){
             	 Debug.logError(e, e.toString(), module);
                 return ServiceUtil.returnError(e.toString());
             }
             Map<String, Object> invoiceApplCtx = UtilMisc.<String, Object>toMap("invoiceId", invoiceId);
             invoiceApplCtx.put("userLogin", userLogin);
             invoiceApplCtx.put("paymentId",paymentId);                       
             try{
            	 Map<String, Object> invoiceTotalResult =dispatcher.runSync("getInvoiceTotal", UtilMisc.toMap("invoiceId",invoiceId ,"userLogin" ,userLogin));
            	 if (ServiceUtil.isError(invoiceTotalResult)) {
            		 Debug.logError(invoiceTotalResult.toString(), module);
                     return ServiceUtil.returnError(null, null, null, invoiceTotalResult);
                 }
                 invoiceApplCtx.put("amountApplied", amountAppliedRunningTotal); 
             	Map<String, Object> invoiceApplResult = dispatcher.runSync("createPaymentApplication",invoiceApplCtx);
             	
             	if (ServiceUtil.isError(invoiceApplResult)) {
             		 Debug.logError(invoiceApplResult.toString(), module);
                     return ServiceUtil.returnError(null, null, null, invoiceApplResult);
                 }
             	amountAppliedRunningTotal = amountAppliedRunningTotal.subtract(outstandingAmount);
             	if( amountAppliedRunningTotal.compareTo(BigDecimal.ZERO) <= 0){
             		break;
             	}
             }catch(GenericServiceException e){
             	Debug.logError(e, e.toString(), module);
                 return ServiceUtil.returnError(e.toString());
             }
        }
        result.put("paymentsList", paymentsList);
     return result;
	}
	/*public static Map<String, Object> autoPaymentApplicationByPaymentType(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();              
        BigDecimal paymentAmount = (BigDecimal) context.get("amount");
        Locale locale = (Locale) context.get("locale");     
        
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String parentTypeId = (String) context.get("parentTypeId");
        String paymentTypeId = (String) context.get("paymentTypeId");
        String roleTypeId = (String) context.get("roleTypeId");
        String paymentId = (String) context.get("paymentId");
        
        List paymentTypesList = FastList.newInstance();
        if(UtilValidate.isNotEmpty(paymentTypeId)){
        	paymentTypesList = UtilMisc.toList(paymentTypeId);
        }
        if(UtilValidate.isNotEmpty(roleTypeId)){
        	try{
        		List<GenericValue> paymentTypeRoleType = delegator.findList("PaymentTypeRoleType", EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, roleTypeId), null, null, null, false);
            	paymentTypesList = EntityUtil.getFieldListFromEntityList(paymentTypeRoleType, "paymentTypeId", true);
        	}catch (GenericEntityException e) {
	        	Debug.logError(e, e.toString(), module);
		        return ServiceUtil.returnError(e.toString());
	        }
        }
        
        List condList = FastList.newInstance();
    	if(UtilValidate.isNotEmpty(paymentTypesList)){
    		condList.add(EntityCondition.makeCondition("paymentTypeId", EntityOperator.IN, paymentTypesList));
    	}
    	if(UtilValidate.isNotEmpty(paymentId)){
    		condList.add(EntityCondition.makeCondition("paymentId", EntityOperator.EQUALS, paymentId));  
    	}
    	condList.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "PMNT_VOID"));
    	EntityCondition pmntCond = EntityCondition.makeCondition(condList, EntityOperator.AND);
        List<GenericValue> paymentsList = null;
        try {
        	paymentsList = delegator.findList("Payment", pmntCond, null, null, null, false);
        } catch (GenericEntityException e) {
        	Debug.logError(e, e.toString(), module);
	        return ServiceUtil.returnError(e.toString());
        }
        List partyIdsList = FastList.newInstance();
        if(parentTypeId == "RECEIPT"){
        	partyIdsList =  EntityUtil.getFieldListFromEntityList(paymentsList, "partyIdFrom", true);  
        }
        else{
        	partyIdsList =  EntityUtil.getFieldListFromEntityList(paymentsList, "partyIdTo", true);  
        }
        for(int ptyCount=0; ptyCount<partyIdsList.size(); ptyCount++){
        	String partyId = (String) partyIdsList.get(ptyCount);
        	// Get Party Related Payments
        	List<GenericValue> partyPayments = null;
        	if(parentTypeId == "RECEIPT"){
        		partyPayments = EntityUtil.filterByCondition(paymentsList, EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, partyId));
            }
            else{
            	partyPayments = EntityUtil.filterByCondition(paymentsList, EntityCondition.makeCondition("partyIdTo", EntityOperator.EQUALS, partyId));  
            }
        	
        	// Get Party Related Invoices
        	List conditionList = FastList.newInstance();
        	if(parentTypeId == "RECEIPT"){
        		conditionList = UtilMisc.toList(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId));  
        	}
            else{
            	conditionList = UtilMisc.toList(EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, partyId));
            }
        	conditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "INVOICE_READY"));
        	EntityCondition condition = EntityCondition.makeCondition(conditionList, EntityOperator.AND);
            List invoices = null;
            try{
            	List invoiceIdList = delegator.findList("Invoice", condition, UtilMisc.toSet("invoiceId"), null, null, false);
            	invoices = EntityUtil.getFieldListFromEntityList(invoiceIdList,"invoiceId",true);
            }catch(GenericEntityException e){
            	e.printStackTrace();
            }
            for (GenericValue payment : partyPayments) {
	            BigDecimal notApplied = PaymentWorker.getPaymentNotApplied(payment);
	            if (notApplied.signum() > 0) {
	            	BigDecimal amountAppliedRunningTotal = notApplied;
	            	for( int i=0; i< invoices.size(); i++){
	                	String invoiceId = (String)invoices.get(i);
	                	Map invoicePaymentInfoMap =FastMap.newInstance();
	        			BigDecimal outstandingAmount =BigDecimal.ZERO;
	        			invoicePaymentInfoMap.put("invoiceId", invoiceId);
	        			invoicePaymentInfoMap.put("userLogin",userLogin);
	        			try{
	        				Map<String, Object> getInvoicePaymentInfoListResult = dispatcher.runSync("getInvoicePaymentInfoList", invoicePaymentInfoMap);
	        				if (ServiceUtil.isError(getInvoicePaymentInfoListResult)) {
	        		            	Debug.logError(getInvoicePaymentInfoListResult.toString(), module);    			
	        		                return ServiceUtil.returnError(null, null, null, getInvoicePaymentInfoListResult);
	        		        }
	        				Map invoicePaymentInfo = (Map)((List)getInvoicePaymentInfoListResult.get("invoicePaymentInfoList")).get(0);
	        				outstandingAmount = (BigDecimal)invoicePaymentInfo.get("outstandingAmount");					
	        			}catch (GenericServiceException e) {
	        				// TODO: handle exception
	        				 Debug.logError(e, e.toString(), module);
	        		         return ServiceUtil.returnError(e.toString());
	        			}
	        			Map<String, Object> appl = FastMap.newInstance();
		                appl.put("paymentId", payment.get("paymentId"));
		                appl.put("amountApplied", amountAppliedRunningTotal);
		                appl.put("invoiceId", invoiceId);
		                appl.put("userLogin", userLogin);
		                try {
			                Map<String, Object> createPayApplResult = dispatcher.runSync("createPaymentApplication", appl);
			                if (ServiceUtil.isError(createPayApplResult)) {
			                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceFromOrder",locale), null, null, createPayApplResult);
			                }
		                } catch (GenericServiceException e) {
		                	// TODO: handle exception
	        				 Debug.logError(e, e.toString(), module);
	        		         return ServiceUtil.returnError(e.toString());
	                    }
		                amountAppliedRunningTotal = amountAppliedRunningTotal.subtract(outstandingAmount);
	                 	if( amountAppliedRunningTotal.compareTo(BigDecimal.ZERO) <= 0){
	                 		break;
	                 	}
	                } 
	            }
	        }
        }
        
        Map<String, Object> result = ServiceUtil.returnSuccess("Payments Has Been Successfully Applied");        
        return result;
	}*/
	
	public static Map<String, Object> autoPaymentApplicationByPaymentType(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();              
        Locale locale = (Locale) context.get("locale");     
        
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String paymentTypeId = (String) context.get("paymentTypeId");
        String partyId = (String) context.get("partyId");
        Timestamp fromDate = (Timestamp) context.get("fromDate");
        Timestamp thruDate = (Timestamp) context.get("thruDate");
        //String roleTypeId = (String) context.get("roleTypeId");
        List appliedInvoiceIdList = FastList.newInstance();
        List appliedPaymentIdList = FastList.newInstance();
        String invoiceParentTypeId = "";
        List paymentTypesList = FastList.newInstance();
        if(UtilValidate.isNotEmpty(paymentTypeId)){
        	paymentTypesList = UtilMisc.toList(paymentTypeId);
        }
        
    	try{
    		/*List<GenericValue> paymentTypeRoleType = delegator.findList("PaymentType", EntityCondition.makeCondition("paymentTypeId", EntityOperator.EQUALS, paymentTypeId), null, null, null, false);
        	paymentTypesList = EntityUtil.getFieldListFromEntityList(paymentTypeRoleType, "paymentTypeId", true);*/
    		GenericValue paymentType = delegator.findOne("PaymentType", UtilMisc.toMap("paymentTypeId",paymentTypeId), false);
    		if(UtilValidate.isEmpty(paymentType)){
    			Debug.logError("inValid PaymentTypeId ::"+paymentTypeId, module);
    	        return ServiceUtil.returnError("inValid PaymentTypeId ::"+paymentTypeId);
    		}
    	}catch (GenericEntityException e) {
        	Debug.logError(e, e.toString(), module);
	        return ServiceUtil.returnError(e.toString());
        }
        List invoiceTypeIds = FastList.newInstance();
		try {
			invoiceTypeIds = EntityUtil.getFieldListFromEntityList(delegator.findList("InvoiceType", null, null, null, null, false), "invoiceTypeId", true);
		} catch (GenericEntityException e) {
			Debug.logError(e, module);
			return ServiceUtil.returnError(e.toString());
		}
		
        int i=0;
        
        	EntityListIterator paymentListItr = null;
            List exprListForParameters = FastList.newInstance();
            if(UtilValidate.isNotEmpty(fromDate) && UtilValidate.isNotEmpty(thruDate)){
            	exprListForParameters.add(EntityCondition.makeCondition("paymentDate", EntityOperator.BETWEEN, UtilMisc.toList(UtilDateTime.getDayStart(fromDate),UtilDateTime.getDayEnd(thruDate)))); 
            }
            if(UtilValidate.isNotEmpty(partyId)){
            	exprListForParameters.add(EntityCondition.makeCondition(EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS,partyId),EntityOperator.OR,
            			EntityCondition.makeCondition("partyIdTo", EntityOperator.EQUALS,partyId))); 
            }
        	exprListForParameters.add(EntityCondition.makeCondition("paymentTypeId", EntityOperator.EQUALS, paymentTypeId));
        	exprListForParameters.add(EntityCondition.makeCondition(EntityCondition.makeCondition("isFullyApplied", EntityOperator.NOT_EQUAL, "Y"),EntityOperator.OR,
        			EntityCondition.makeCondition("isFullyApplied", EntityOperator.EQUALS, null))); 
        	exprListForParameters.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_IN, UtilMisc.toList("PMNT_NOT_PAID","PMNT_VOID")));
        	EntityCondition pmntCond = EntityCondition.makeCondition(exprListForParameters, EntityOperator.AND);
    		Debug.log("pmntCond=========="+pmntCond);
        	try {
    			paymentListItr = delegator.find("Payment", pmntCond ,null, null, UtilMisc.toList("paymentDate"), null);
    		} catch (GenericEntityException e) {
    			Debug.logError(e, module);
    			return ServiceUtil.returnError(e.toString());
    		}
        	 Debug.log("Completed Payment's :======"+0);
    		GenericValue paymentEntry;
    		while(paymentListItr != null && (paymentEntry = paymentListItr.next()) != null) {
    			BigDecimal notApplied = PaymentWorker.getPaymentNotApplied(paymentEntry);
    			 ++i;
    			 
    			 if(i%20 ==0){
    				 Debug.log("Completed Payment's :======"+i);
    			 }
    			 appliedPaymentIdList.add(paymentEntry.getString("paymentId"));
    			if (notApplied.signum() > 0) {
    				BigDecimal amountAppliedRunningTotal = notApplied;
    				String paymentId = paymentEntry.getString("paymentId");
        			String partyIdFrom = paymentEntry.getString("partyIdFrom");
        			String partyIdTo = paymentEntry.getString("partyIdTo");
        			//Debug.log("paymentEntry=========="+paymentEntry);
        			
        			EntityListIterator invoicesListItr = null;
        			exprListForParameters.clear();
        			if(UtilValidate.isNotEmpty(fromDate) && UtilValidate.isNotEmpty(thruDate)){
                    	exprListForParameters.add(EntityCondition.makeCondition("invoiceDate", EntityOperator.BETWEEN, UtilMisc.toList(UtilDateTime.getDayStart(fromDate),UtilDateTime.getDayEnd(thruDate)))); 
                    }
        			exprListForParameters.add(EntityCondition.makeCondition("invoiceTypeId",EntityOperator.IN, invoiceTypeIds));
        			exprListForParameters.add(EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, partyIdTo)); 
        			exprListForParameters.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyIdFrom)); 
        			exprListForParameters.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "INVOICE_READY"));
        			EntityCondition paramCond = EntityCondition.makeCondition(exprListForParameters, EntityOperator.AND);
        			//Debug.log("paramCond======"+paramCond);
        			
        			try {
        				invoicesListItr = delegator.find("Invoice", paramCond,null, UtilMisc.toSet("invoiceId","partyIdFrom","partyId"), UtilMisc.toList("invoiceDate"), null);
        			} catch (GenericEntityException e) {
        				Debug.logError(e, module);
        				return ServiceUtil.returnError(e.toString());
        			}
    				
        			GenericValue invoiceEntry;
            		while(invoicesListItr != null && (invoiceEntry = invoicesListItr.next()) != null) {
            			String invoiceId = (String)invoiceEntry.get("invoiceId");
	                	Map invoicePaymentInfoMap =FastMap.newInstance();
	        			BigDecimal outstandingAmount =BigDecimal.ZERO;
	        			invoicePaymentInfoMap.put("invoiceId", invoiceId);
	        			invoicePaymentInfoMap.put("userLogin",userLogin);
	        			appliedInvoiceIdList.add(invoiceId);
	        			try{
	        				Map<String, Object> getInvoicePaymentInfoListResult = dispatcher.runSync("getInvoicePaymentInfoList", invoicePaymentInfoMap);
	        				if (ServiceUtil.isError(getInvoicePaymentInfoListResult)) {
	        		            	Debug.logError(getInvoicePaymentInfoListResult.toString(), module);    			
	        		                return ServiceUtil.returnError(null, null, null, getInvoicePaymentInfoListResult);
	        		        }
	        				Map invoicePaymentInfo = (Map)((List)getInvoicePaymentInfoListResult.get("invoicePaymentInfoList")).get(0);
	        				outstandingAmount = (BigDecimal)invoicePaymentInfo.get("outstandingAmount");	
	        			}catch (GenericServiceException e) {
	        				// TODO: handle exception
	        				 Debug.logError(e, e.toString(), module);
	        		         return ServiceUtil.returnError(e.toString());
	        			}
	        			BigDecimal amountApplied = outstandingAmount;
	        			if(amountAppliedRunningTotal.compareTo(outstandingAmount) <0){
	        				Debug.log("amountAppliedRunningTotal====="+amountAppliedRunningTotal+"===outstandingAmount===="+outstandingAmount);
	        				amountApplied = amountAppliedRunningTotal;
	        			}
	        			Map<String, Object> appl = FastMap.newInstance();
		                appl.put("paymentId", paymentId);
		                appl.put("amountApplied", amountAppliedRunningTotal);
		                appl.put("invoiceId", invoiceId);
		                appl.put("userLogin", userLogin);
		                try {
			                Map<String, Object> createPayApplResult = dispatcher.runSync("createPaymentApplication", appl);
			                if (ServiceUtil.isError(createPayApplResult)) {
			                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,"AccountingErrorCreatingInvoiceFromOrder",locale), null, null, createPayApplResult);
			                }
		                } catch (GenericServiceException e) {
		                	// TODO: handle exception
	        				 Debug.logError(e, e.toString(), module);
	        		         return ServiceUtil.returnError(e.toString());
	                    }
		                amountAppliedRunningTotal = amountAppliedRunningTotal.subtract(outstandingAmount);
	                 	if( amountAppliedRunningTotal.compareTo(BigDecimal.ZERO) <= 0){
	                 		try {
	                 			paymentEntry.set("isFullyApplied", "Y");
		                 		delegator.store(paymentEntry);
	                 		}catch (Exception e) {
			                	// TODO: handle exception
		        				 Debug.logError(e, e.toString(), module);
		        		         return ServiceUtil.returnError(e.toString());
		                    }
	                 		break;
	                 	}
            			
            		}
            		try {
            			invoicesListItr.close();
            		} catch (GenericEntityException e) {
            			Debug.logError(e, module);
            			return ServiceUtil.returnError(e.toString());
            		}
            		
    			}
    			
    		}
    		try {
    			paymentListItr.close();
    		} catch (GenericEntityException e) {
    			Debug.logError(e, module);
    			return ServiceUtil.returnError(e.toString());
    		}
        
        Map<String, Object> result = ServiceUtil.returnSuccess("Payments Has Been Successfully Applied"); 
        result.put("appliedInvoiceIdList", appliedInvoiceIdList);
        result.put("appliedPaymentIdList", appliedPaymentIdList);
        return result;
	}
	
	public static Map<String, Object>  getAcctTransEnries(DispatchContext dctx, Map<String, Object> context)  {
        String invoiceId = (String) context.get("invoiceId");
        String paymentId = (String) context.get("paymentId");
        GenericValue userLogin = (GenericValue) context.get("userLogin");      
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();        
        Map<String, Object> serviceResult;
        List<GenericValue> acctgTransEntries = new ArrayList<GenericValue>();
        List  AcctgTransAndEntries = FastList.newInstance();
		Map<String, Object> result = ServiceUtil.returnSuccess(); 
		Map<String, Object> AcctgTransEntriesMap = FastMap.newInstance();

		try {
	            if(UtilValidate.isNotEmpty(invoiceId)){
				    acctgTransEntries = delegator.findList("AcctgTransAndEntries", EntityCondition.makeCondition("invoiceId", EntityOperator.EQUALS, invoiceId), null, UtilMisc.toList("acctgTransId"), null, false);
	            }
	            if(UtilValidate.isNotEmpty(paymentId)){
	    			acctgTransEntries = delegator.findList("AcctgTransAndEntries", EntityCondition.makeCondition("paymentId", EntityOperator.EQUALS, paymentId), null, UtilMisc.toList("acctgTransId"), null, false);	
	            }
				if (UtilValidate.isEmpty(acctgTransEntries)) {
	                Debug.logError("NO account Entries for InvoiceId ", module);
	                AcctgTransAndEntries.add(AcctgTransEntriesMap);
	                result.put("AcctgTransAndEntries", AcctgTransAndEntries);
	                return result;
	            }
				for (GenericValue acctTransEntry : acctgTransEntries) {
					  String accountCode ="";
					  BigDecimal amount = ZERO;
		              BigDecimal origAmount = ZERO;
		              String partyAcctCode = ""; 
		              String orgPartyAcctCode = "";
		              String externalId ="";
		              AcctgTransEntriesMap = FastMap.newInstance();
					  String partyId = acctTransEntry.getString("partyId");
					  if(UtilValidate.isNotEmpty(acctTransEntry.getString("externalId"))){
					      externalId = acctTransEntry.getString("externalId");
					  }    
					  String organizationPartyId = acctTransEntry.getString("organizationPartyId");
					  amount = acctTransEntry.getBigDecimal("amount");
					  origAmount = acctTransEntry.getBigDecimal("origAmount");
					  GenericValue partyAttributes = delegator.findOne("PartyAttribute", UtilMisc.toMap("partyId", partyId,"attrName","ACCOUNT_CODE"), false);
		              if ((partyAttributes != null) && (partyAttributes.getString("attrValue") != null)) {
					        partyAcctCode = partyAttributes.getString("attrValue");
	                  }
					  GenericValue orgPartyAttributes = delegator.findOne("PartyAttribute", UtilMisc.toMap("partyId", organizationPartyId,"attrName","ACCOUNT_CODE"), false);
		              if ((orgPartyAttributes != null) && (orgPartyAttributes.getString("attrValue") != null)) {
					      orgPartyAcctCode = orgPartyAttributes.getString("attrValue");
	                  }    
					  accountCode =externalId+orgPartyAcctCode+partyAcctCode;
					  AcctgTransEntriesMap.put("acctgTransId",acctTransEntry.getString("acctgTransId"));
					  AcctgTransEntriesMap.put("acctgTransEntrySeqId",acctTransEntry.getString("acctgTransEntrySeqId"));
					  AcctgTransEntriesMap.put("accountCode",accountCode);
					  AcctgTransEntriesMap.put("isPosted",acctTransEntry.getString("isPosted"));
					  AcctgTransEntriesMap.put("glFiscalTypeId",acctTransEntry.getString("glFiscalTypeId"));
					  AcctgTransEntriesMap.put("acctgTransTypeId",acctTransEntry.getString("acctgTransTypeId"));
					  AcctgTransEntriesMap.put("transactionDate",acctTransEntry.getTimestamp("transactionDate"));
					  AcctgTransEntriesMap.put("postedDate",acctTransEntry.getTimestamp("postedDate"));
					  AcctgTransEntriesMap.put("glJournalId",acctTransEntry.getString("glJournalId"));
					  AcctgTransEntriesMap.put("transTypeDescription",acctTransEntry.getString("transTypeDescription"));
					  AcctgTransEntriesMap.put("paymentId",acctTransEntry.getString("paymentId"));
					  AcctgTransEntriesMap.put("fixedAssetId",acctTransEntry.getString("fixedAssetId"));
					  AcctgTransEntriesMap.put("finAccountTransId",acctTransEntry.getString("finAccountTransId"));
					  AcctgTransEntriesMap.put("glAccountId",acctTransEntry.getString("glAccountId"));
					  AcctgTransEntriesMap.put("productId",acctTransEntry.getString("productId"));
					  AcctgTransEntriesMap.put("debitCreditFlag",acctTransEntry.get("debitCreditFlag"));
					  AcctgTransEntriesMap.put("amount",amount);
					  AcctgTransEntriesMap.put("origAmount",origAmount);
					  AcctgTransEntriesMap.put("organizationPartyId",organizationPartyId);
					  AcctgTransEntriesMap.put("glAccountTypeId",acctTransEntry.getString("glAccountTypeId"));
					  AcctgTransEntriesMap.put("accountName",acctTransEntry.getString("accountName"));
					  AcctgTransEntriesMap.put("glAccountClassId",acctTransEntry.getString("glAccountClassId"));
					  AcctgTransEntriesMap.put("reconcileStatusId",acctTransEntry.getString("reconcileStatusId"));
					  AcctgTransEntriesMap.put("acctgTransEntryTypeId",acctTransEntry.getString("acctgTransEntryTypeId"));	
					  AcctgTransAndEntries.add(AcctgTransEntriesMap);
			}
        } catch (Exception e) {
            Debug.logError(e, "Problem getting AcctgTransAndEntries", module);
            return ServiceUtil.returnError(e.getMessage());
        }
        
	        result.put("AcctgTransAndEntries", AcctgTransAndEntries);
	        return result;
    }
	
	public static String getInvoiceSequence(Delegator delegator, String invoiceId) {
        GenericValue invSeqObject = null;
        try {
			List<GenericValue> invoiceSeqList = delegator.findList("BillOfSaleInvoiceSequence", EntityCondition.makeCondition("invoiceId", EntityOperator.EQUALS, invoiceId), null, null, null, false);
			if(UtilValidate.isNotEmpty(invoiceSeqList)){
				invSeqObject = EntityUtil.getFirst(invoiceSeqList);
			}
        } catch (GenericEntityException e) {
            Debug.logError(e, "Error finding BillOfSaleInvoiceSequence in getInvoiceSequence", module);
        }
        if (invSeqObject == null || invSeqObject.getString("invoiceSequence") == null) {
            return invoiceId;
        } else {
            return invSeqObject.getString("invoiceSequence");
        }
    }
	public static BigDecimal getInvoiceBasicValue(Delegator delegator, String invoiceId) {
		BigDecimal invoiceItemBasicVal = BigDecimal.ZERO;
        try {
        	GenericValue invoice = delegator.findOne("Invoice", UtilMisc.toMap("invoiceId", invoiceId), false);
        	String invoicetypeId = (String) invoice.getString("invoiceTypeId");
    		List conditionList = FastList.newInstance();
    		List<GenericValue> invoiceItemList = FastList.newInstance();
        	if(invoicetypeId.equals("PURCHASE_INVOICE")){
        		conditionList.add(EntityCondition.makeCondition("invoiceId", EntityOperator.EQUALS, invoiceId));
        		conditionList.add(EntityCondition.makeCondition("invoiceItemTypeId", EntityOperator.EQUALS, "INV_RAWPROD_ITEM"));
            	invoiceItemList = delegator.findList("InvoiceItem", EntityCondition.makeCondition(conditionList,EntityOperator.AND), UtilMisc.toSet("productId","quantity","amount"), null, null, true);
        	}
        	if(invoicetypeId.equals("SALES_INVOICE")){
        		conditionList.add(EntityCondition.makeCondition("invoiceId", EntityOperator.EQUALS, invoiceId));
        		conditionList.add(EntityCondition.makeCondition("invoiceItemTypeId", EntityOperator.EQUALS, "INV_FPROD_ITEM"));
            	invoiceItemList = delegator.findList("InvoiceItem", EntityCondition.makeCondition(conditionList,EntityOperator.AND), UtilMisc.toSet("productId","quantity","amount"), null, null, true);
        	}
        	for (GenericValue invoiceItem : invoiceItemList) {
	            BigDecimal quantity = invoiceItem.getBigDecimal("quantity");
	            BigDecimal unitpPce = invoiceItem.getBigDecimal("amount");
	            BigDecimal amount = quantity.multiply(unitpPce);
	            
	            amount = (amount.setScale(0, rounding));
	            
	            invoiceItemBasicVal = invoiceItemBasicVal.add(amount);
        	}
        } catch (GenericEntityException e) {
            Debug.logError(e, "Error,while getting Basic Amount of Invoice", module);
        }
        return invoiceItemBasicVal; 
    }
	
	public static String invoicetypeinfo(Delegator delegator, String shipmentId,String invoiceTypeId) {
		String invoicess = "";
		try {     							
	    		List conditionList = FastList.newInstance();
	    		List<GenericValue> Invoice = FastList.newInstance();
	    		conditionList.add(EntityCondition.makeCondition("shipmentId", EntityOperator.EQUALS, shipmentId));
	    		conditionList.add(EntityCondition.makeCondition("invoiceTypeId", EntityOperator.EQUALS, invoiceTypeId));
    			conditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "INVOICE_CANCELLED"));
    			Invoice = delegator.findList("Invoice", EntityCondition.makeCondition(conditionList,EntityOperator.AND), UtilMisc.toSet("shipmentId","invoiceTypeId","statusId","invoiceId"), null, null, true);    			
    			if (UtilValidate.isNotEmpty(Invoice)) {           		
            		GenericValue invoiceFirst = EntityUtil.getFirst(Invoice);
            		invoicess = (String) invoiceFirst.getString("invoiceId");  			
    			}
		}catch (GenericEntityException e) {
            Debug.logError(e, "Error,while getting Invoice", module);
        }
        return invoicess; 
	}
	
	public static String getInvoiceFromShipment(Delegator delegator, String invoiceId) {
		BigDecimal invoiceItemBasicVal = BigDecimal.ZERO;
		String oppositeInvoice = "";
        try {
        	GenericValue invoice = delegator.findOne("Invoice", UtilMisc.toMap("invoiceId", invoiceId), false);
        	String invoicetypeId = (String) invoice.getString("invoiceTypeId");
        	
        	String shipmentId = (String) invoice.getString("shipmentId");
        	
    		List conditionList = FastList.newInstance();
    		List<GenericValue> Invoice = FastList.newInstance();
        	if(invoicetypeId.equals("PURCHASE_INVOICE")){
        		//conditionList.add(EntityCondition.makeCondition("invoiceId", EntityOperator.EQUALS, invoiceId));
        		conditionList.add(EntityCondition.makeCondition("shipmentId", EntityOperator.EQUALS, shipmentId));
        		conditionList.add(EntityCondition.makeCondition("invoiceTypeId", EntityOperator.EQUALS, "SALES_INVOICE"));
        		conditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "INVOICE_CANCELLED"));
        		Invoice = delegator.findList("Invoice", EntityCondition.makeCondition(conditionList,EntityOperator.AND), UtilMisc.toSet("invoiceId"), null, null, true);
        	}
        	if(invoicetypeId.equals("SALES_INVOICE")){
        		//conditionList.add(EntityCondition.makeCondition("invoiceId", EntityOperator.EQUALS, invoiceId));
        		conditionList.add(EntityCondition.makeCondition("shipmentId", EntityOperator.EQUALS, shipmentId));
        		conditionList.add(EntityCondition.makeCondition("invoiceTypeId", EntityOperator.EQUALS, "PURCHASE_INVOICE"));
        		conditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "INVOICE_CANCELLED"));
        		Invoice = delegator.findList("Invoice", EntityCondition.makeCondition(conditionList,EntityOperator.AND), UtilMisc.toSet("invoiceId"), null, null, true);
        	}
        	
        	
        	if (UtilValidate.isNotEmpty(Invoice)) {
        		
        		GenericValue invoiceFirst = EntityUtil.getFirst(Invoice);
        		oppositeInvoice = (String) invoiceFirst.getString("invoiceId");
        		
        	}
        	
        	
        } catch (GenericEntityException e) {
            Debug.logError(e, "Error,while getting Basic Amount of Invoice", module);
        }
        return oppositeInvoice; 
    }
	
	
	public static Map<String, Object> autoPaymentApplicationInLIFO(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();              
        Locale locale = (Locale) context.get("locale");     
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String paymentTypeId = (String) context.get("paymentTypeId");
        String partyId = (String) context.get("partyId");
        String paymentId = (String) context.get("paymentId");
        //String roleTypeId = (String) context.get("roleTypeId");
        List appliedInvoiceIdList = FastList.newInstance();
        List appliedPaymentIdList = FastList.newInstance();
        List exprList = FastList.newInstance();
        List invoicesList = FastList.newInstance();
	    String partyIdFrom = "";
	    String partyIdTo = "";
	    boolean autoPaymentApplication = Boolean.FALSE;
	    boolean useFifo = Boolean.FALSE;
		if (UtilValidate.isNotEmpty(context.get("useFifo"))) {
			useFifo = (Boolean) context.get("useFifo");
		}
		boolean sendSMS = Boolean.TRUE;
		if (UtilValidate.isNotEmpty(context.get("sendSMS"))) {
			sendSMS = (Boolean) context.get("sendSMS");
		}
	   Timestamp paymentTimestamp = UtilDateTime.nowTimestamp();
  	   String statusId = null;
  	   BigDecimal  paymentAmount=BigDecimal.ZERO;
	  	 try{
	  	  GenericValue payment = delegator.findOne("Payment",UtilMisc.toMap("paymentId",paymentId) , false);
		  if(UtilValidate.isNotEmpty(payment)){
			  paymentAmount=payment.getBigDecimal("amount");
			  paymentTimestamp=payment.getTimestamp("paymentDate");
		  		if(UtilAccounting.isReceipt(payment)){
		  			partyIdFrom=payment.getString("partyIdFrom");
		  			autoPaymentApplication=Boolean.TRUE;
		  		}
		  		if(UtilAccounting.isDisbursement(payment)){
		  			partyIdTo=payment.getString("partyIdTo");
		  			autoPaymentApplication=Boolean.TRUE;
		  		}
		  	}
	  	 }catch (GenericEntityException e) {
				// TODO: handle exception
				Debug.logError(e, module);
		 }
      BigDecimal amountAppliedRunningTotal = paymentAmount;
      
      String enablePaymentApplication = "N";
		try{
			 GenericValue tenantConfigEnablePaymentApplication = delegator.findOne("TenantConfiguration", UtilMisc.toMap("propertyTypeEnumId","GLOBAL", "propertyName","enableGlobalPaymntAutoApplication"), true);
			 if (UtilValidate.isNotEmpty(tenantConfigEnablePaymentApplication)) {
				 enablePaymentApplication = tenantConfigEnablePaymentApplication.getString("propertyValue");
			 } 
		}catch (GenericEntityException e) {
				// TODO: handle exception
				Debug.logError(e, module);
		 }
		Debug.log("====paymentId===AutoPaymentApplication ==Starts=====>HERE="+paymentId);
      if((UtilValidate.isNotEmpty(enablePaymentApplication) && enablePaymentApplication.equals("Y")) && (autoPaymentApplication)){
    	  Debug.log("====inside==PaymentApplication===>>>>"+autoPaymentApplication+"===partyIdFrom==="+partyIdFrom+"====paymentTimestamp===="+paymentTimestamp);
    	  if (UtilValidate.isNotEmpty(partyIdFrom)) {
        	  exprList.add(EntityCondition.makeCondition("partyId",EntityOperator.EQUALS, partyIdFrom));
    	  }
    	  if (UtilValidate.isNotEmpty(partyIdTo)) {
        	  exprList.add(EntityCondition.makeCondition("partyIdFrom",EntityOperator.EQUALS, partyIdTo));
    	  }
  		
    	  if (UtilValidate.isNotEmpty(context.get("invoiceId"))) {
  			exprList.add(EntityCondition.makeCondition("invoiceId",EntityOperator.EQUALS, context.get("invoiceId")));
  		}else{
  			if (UtilValidate.isNotEmpty(paymentTimestamp)) {
  	  			exprList.add(EntityCondition.makeCondition("invoiceDate",EntityOperator.LESS_THAN_EQUAL_TO,	UtilDateTime.getDayEnd(paymentTimestamp)));
  	  		}
  		}
  		//exprList.add(EntityCondition.makeCondition("invoiceTypeId",EntityOperator.IN,UtilMisc.toList(obInvoiceType, "SHOPEE_RENT", "MIS_INCOME_IN")));
  		List invoiceStatusList = UtilMisc.toList("INVOICE_PAID","INVOICE_CANCELLED","INVOICE_WRITEOFF");
  		exprList.add(EntityCondition.makeCondition("statusId",EntityOperator.NOT_IN, invoiceStatusList));
  		//exprList.add(EntityCondition.makeCondition("purposeTypeId",EntityOperator.NOT_EQUAL, "LMS_SALES_CHANNEL"));
  		//exprList.add(EntityCondition.makeCondition("purposeTypeId", EntityOperator.NOT_IN, UtilMisc.toList("LMS_SALES_CHANNEL","BYPROD_SALES_CHANNEL")));
  		exprList.add(EntityCondition.makeCondition(EntityCondition.makeCondition("purposeTypeId", EntityOperator.EQUALS, null), EntityOperator.OR, 
         		EntityCondition.makeCondition("purposeTypeId", EntityOperator.NOT_IN, UtilMisc.toList("LMS_SALES_CHANNEL","BYPROD_SALES_CHANNEL"))));
  		EntityCondition paramCond = EntityCondition.makeCondition(exprList,EntityOperator.AND);
  		
  		EntityFindOptions findOptions = new EntityFindOptions();
		findOptions.setDistinct(true);
		List<String> orderBy = UtilMisc.toList("-invoiceDate");
		try {
			// Here we are trying change the invoice order to apply (LIFO OR FIFO)
			if (useFifo) {
				orderBy = UtilMisc.toList("invoiceDate");
			}
			invoicesList = delegator.findList("Invoice", paramCond, null,orderBy, findOptions, false);
		} catch (Exception e) {
  			// TODO: handle exception

  		}
		
		if (UtilValidate.isNotEmpty(context.get("invoiceId"))) {
			try {
				Map<String, Object> result =dispatcher.runSync("getInvoiceTotal", UtilMisc.toMap("invoiceId",context.get("invoiceId") ,"userLogin" ,userLogin));
			   	 if (ServiceUtil.isError(result)) {
			   		 Debug.logError(result.toString(), module);
			            return ServiceUtil.returnError(null, null, null, result);
			        }
			   	amountAppliedRunningTotal = (BigDecimal)result.get("amountTotal");
			}catch (Exception e) {
	  			// TODO: handle exception

	  		}
		}

		List invoiceIds = EntityUtil.getFieldListFromEntityList(invoicesList, "invoiceId", true);
        for( int i=0; i< invoiceIds.size(); i++){
        	String invoiceId = (String)invoiceIds.get(i);
        	Map invoicePaymentInfoMap =FastMap.newInstance();
			BigDecimal outstandingAmount =BigDecimal.ZERO;
			invoicePaymentInfoMap.put("invoiceId", invoiceId);
			invoicePaymentInfoMap.put("userLogin",userLogin);
			try{
				Map<String, Object> getInvoicePaymentInfoListResult = dispatcher.runSync("getInvoicePaymentInfoList", invoicePaymentInfoMap);
				if (ServiceUtil.isError(getInvoicePaymentInfoListResult)) {
		            	Debug.logError(getInvoicePaymentInfoListResult.toString(), module);    			
		                return ServiceUtil.returnError(null, null, null, getInvoicePaymentInfoListResult);
		            }
				Map invoicePaymentInfo = (Map)((List)getInvoicePaymentInfoListResult.get("invoicePaymentInfoList")).get(0);
				outstandingAmount = (BigDecimal)invoicePaymentInfo.get("outstandingAmount");
			}catch (GenericServiceException e) {
				// TODO: handle exception
				 Debug.logError(e, e.toString(), module);
		         return ServiceUtil.returnError(e.toString());
				
			}
        	 Map<String, Object> invoiceCtx = UtilMisc.<String, Object>toMap("invoiceId", invoiceId);
             invoiceCtx.put("userLogin", userLogin);
             invoiceCtx.put("statusId","INVOICE_READY");
             
             try{
             	Map<String, Object> invoiceResult = dispatcher.runSync("setInvoiceStatus",invoiceCtx);
             	if (ServiceUtil.isError(invoiceResult)) {
             		Debug.logError(invoiceResult.toString(), module);
                     return ServiceUtil.returnError(null, null, null, invoiceResult);
                 }
             	
             }catch(GenericServiceException e){
             	 Debug.logError(e, e.toString(), module);
                 return ServiceUtil.returnError(e.toString());
             }
             Map<String, Object> invoiceApplCtx = UtilMisc.<String, Object>toMap("invoiceId", invoiceId);
             invoiceApplCtx.put("userLogin", userLogin);
             invoiceApplCtx.put("paymentId",paymentId);                       
             try{
            	
            	 Map<String, Object> result =dispatcher.runSync("getInvoiceTotal", UtilMisc.toMap("invoiceId",invoiceId ,"userLogin" ,userLogin));
            	 if (ServiceUtil.isError(result)) {
            		 Debug.logError(result.toString(), module);
                     return ServiceUtil.returnError(null, null, null, result);
                 }
            	 invoiceApplCtx.put("amountApplied", amountAppliedRunningTotal);
                
             	Map<String, Object> invoiceApplResult = dispatcher.runSync("createPaymentApplication",invoiceApplCtx);
             	
             	if (ServiceUtil.isError(invoiceApplResult)) {
             		 Debug.logError(invoiceApplResult.toString(), module);
                     return ServiceUtil.returnError(null, null, null, invoiceApplResult);
                 }
             	amountAppliedRunningTotal = amountAppliedRunningTotal.subtract(outstandingAmount);
             	if( amountAppliedRunningTotal.compareTo(BigDecimal.ZERO) <= 0){
             		break;
             		
             	}
             }catch(GenericServiceException e){
             	Debug.logError(e, e.toString(), module);
                 return ServiceUtil.returnError(e.toString());
             }
        	
        } 
		}       
		        
      Map<String, Object> result = ServiceUtil.returnSuccess();      
      if (UtilValidate.isNotEmpty(context.get("invoiceId"))) {
    	  result.put("invoiceId", context.get("invoiceId"));
      }
      result.put("paymentId", paymentId);
	 
      return result;
	}
	/*public static Map<String, Object> createAccountingRole(DispatchContext dctx, Map<String, Object> context) {
    Delegator delegator = dctx.getDelegator();
    LocalDispatcher dispatcher = dctx.getDispatcher();  
	String invoiceId = (String) context.get("invoiceId");	
	String paymentId = (String) context.get("paymentId");	
	String acctgTransId = (String) context.get("acctgTransId");	
	String finAccountTransId=(String) context.get("finAccountTransId");
	Debug.log("finAccountTransId --------"+finAccountTransId);
	 Debug.log("acctgTransId --------"+acctgTransId);
	 Debug.log("paymentId --------"+paymentId);
	 Debug.log("invoiceId --------"+invoiceId);
	 GenericValue userLogin = (GenericValue) context.get("userLogin");
	Map<String, Object> createRoleContext = FastMap.newInstance();
	try{
		createRoleContext.put("userLogin", userLogin);
		createRoleContext.put("partyId", "Company");
		createRoleContext.put("roleTypeId", "ACCOUNTING");
        String partyId=null;
        //Let's populate InvoiceRole
        if(UtilValidate.isNotEmpty(invoiceId)&& UtilValidate.isEmpty(acctgTransId)&& UtilValidate.isEmpty(finAccountTransId)){
        	createRoleContext.put("invoiceId", invoiceId);
        	 GenericValue invoice = delegator.findOne("Invoice", UtilMisc.toMap("invoiceId",invoiceId), true);
                if(UtilValidate.isNotEmpty(invoice)){
                   String invoicetypeId = (String) invoice.getString("invoiceTypeId");
                   GenericValue invoiceTypeDetails = delegator.findOne("InvoiceType", UtilMisc.toMap("invoiceTypeId",invoicetypeId), true);
                   if(UtilValidate.isNotEmpty(invoiceTypeDetails)){
                	   String invoiceTypeParent=(String)invoiceTypeDetails.getString("parentTypeId");
                	   if(UtilValidate.isNotEmpty(invoiceTypeParent)&&"PURCHASE_INVOICE".equals(invoiceTypeParent)){
                		   partyId=(String) invoice.getString("partyId");
                	   }else if(UtilValidate.isNotEmpty(invoiceTypeParent)&&"SALES_INVOICE".equals(invoiceTypeParent)){
                		   partyId=(String) invoice.getString("partyIdFrom");
                	   }
                   }
                }  
                Map<String, Object> createInvoiceRoleResult = dispatcher.runSync("createInvoiceRole", createRoleContext);
    	        if (ServiceUtil.isError(createInvoiceRoleResult)) {
    	            return ServiceUtil.returnError("Problem in creation of Accounting role", null, null, createRoleContext);
    	        }
    	        if(UtilValidate.isNotEmpty(partyId)){
    	        	createRoleContext.put("partyId",partyId);
    	        	createRoleContext.put("roleTypeId","INTERNAL_ORGANIZATIO");
    	        	createInvoiceRoleResult = dispatcher.runSync("createInvoiceRole", createRoleContext);
    		        if (ServiceUtil.isError(createInvoiceRoleResult)) {
    		            return ServiceUtil.returnError("Problem in creation of Accounting role", null, null, createInvoiceRoleResult);
    		        }
    	        }
        }
        //Let's populate PaymentRole
        if(UtilValidate.isNotEmpty(paymentId)&&UtilValidate.isEmpty(acctgTransId)&&UtilValidate.isEmpty(finAccountTransId)){
        	createRoleContext.put("paymentId", paymentId);
        	 GenericValue payment = delegator.findOne("Payment", UtilMisc.toMap("paymentId",paymentId), true);	        	
                if(UtilValidate.isNotEmpty(payment)){
                   String paymentTypeId = (String) payment.getString("paymentTypeId");
                   String parentTypeId =(String) PaymentWorker.getPaymentTypeParent(delegator, paymentTypeId);
                   Debug.log("parentTypeId --------"+parentTypeId);
                   //GenericValue paymentTypeDetails = delegator.findOne("PaymentType", UtilMisc.toMap("paymentTypeId",paymentTypeId), true);
                   if(UtilValidate.isNotEmpty(parentTypeId)){
                	   if("RECEIPT".equals(parentTypeId)){
                		   partyId=(String) payment.getString("partyIdTo");
                	   }else if("DISBURSEMENT".equals(parentTypeId)){
                		   partyId=(String) payment.getString("partyIdFrom");
                	   }
                   }
                }  
                Map<String, Object> createPaymentRoleResult = dispatcher.runSync("createPaymentRole", createRoleContext);
    	        if (ServiceUtil.isError(createPaymentRoleResult)) {
    	            return ServiceUtil.returnError("Problem in creation of Accounting role", null, null, createRoleContext);
    	        }
    	        if(UtilValidate.isNotEmpty(partyId)){
    	        	createRoleContext.put("partyId",partyId);
    	        	Debug.log("payment Party--------"+partyId);
    	        	createRoleContext.put("roleTypeId","INTERNAL_ORGANIZATIO");
    	        	createPaymentRoleResult = dispatcher.runSync("createPaymentRole", createRoleContext);
    		        if (ServiceUtil.isError(createPaymentRoleResult)) {
    		            return ServiceUtil.returnError("Problem in creation of Accounting role", null, null, createPaymentRoleResult);
    		        }
    	        }
        }
Debug.log("acctgTransId========before========="+acctgTransId);
        //Let's populate AcctgTransRole
        if(UtilValidate.isNotEmpty(acctgTransId) && UtilValidate.isEmpty(finAccountTransId)&&UtilValidate.isEmpty(finAccountTransId)){
        	createRoleContext.put("acctgTransId", acctgTransId);
        	Debug.log("acctgTransId======loop======"+acctgTransId);
        	GenericValue AcctgTrans = delegator.findOne("AcctgTrans", UtilMisc.toMap("acctgTransId",acctgTransId), true);
        	Debug.log("AcctgTrans============"+AcctgTrans);
        	String acctgPaymentId=null;
        	String acctgInvoiceId=null;
        	if(UtilValidate.isNotEmpty(AcctgTrans)){
        		acctgPaymentId=(String) AcctgTrans.getString("paymentId");
        		acctgInvoiceId=(String) AcctgTrans.getString("invoiceId");
        		
        	}
        	if(UtilValidate.isNotEmpty(acctgPaymentId)){
	        	 GenericValue paymentDetail = delegator.findOne("Payment", UtilMisc.toMap("paymentId",acctgPaymentId), true);
	        	 Debug.log("paymentDetail --------"+paymentDetail);
	                if(UtilValidate.isNotEmpty(paymentDetail)){
	                   String paymentTypeId = (String) paymentDetail.getString("paymentTypeId");
	                   String parentTypeId =(String) PaymentWorker.getPaymentTypeParent(delegator, paymentTypeId);
	                   Debug.log("parentTypeId --------"+parentTypeId);
	                   //GenericValue paymentTypeDetails = delegator.findOne("PaymentType", UtilMisc.toMap("paymentTypeId",paymentTypeId), true);
	                   if(UtilValidate.isNotEmpty(parentTypeId)){
	                	   if("RECEIPT".equals(parentTypeId)){
	                		   partyId=(String) paymentDetail.getString("partyIdTo");
	                	   }else if("DISBURSEMENT".equals(parentTypeId)){
	                		   partyId=(String) paymentDetail.getString("partyIdFrom");
	                	   }
	                   }
	                }  
        	}
        	if(UtilValidate.isNotEmpty(acctgInvoiceId)){
        		 Debug.log("acctgTransId ---invoiceId-----"+acctgInvoiceId);
        		 GenericValue invoiceDetails = delegator.findOne("Invoice", UtilMisc.toMap("invoiceId",acctgInvoiceId), true);
        	
                if(UtilValidate.isNotEmpty(invoiceDetails)){
 	                   String invoicetypeId = (String) invoiceDetails.getString("invoiceTypeId");
 	                   GenericValue invoiceTypeDetails = delegator.findOne("InvoiceType", UtilMisc.toMap("invoiceTypeId",invoicetypeId), true);
 	                   if(UtilValidate.isNotEmpty(invoiceTypeDetails)){
 	                	   String invoiceTypeParent=(String)invoiceTypeDetails.getString("parentTypeId");
 	                	   if(UtilValidate.isNotEmpty(invoiceTypeParent)&&"PURCHASE_INVOICE".equals(invoiceTypeParent)){
 	                		   partyId=(String) invoiceDetails.getString("partyId");
 	                	   }else if(UtilValidate.isNotEmpty(invoiceTypeParent)&&"SALES_INVOICE".equals(invoiceTypeParent)){
 	                		   partyId=(String) invoiceDetails.getString("partyIdFrom");
 	                	   }
 	                   }
 	                
                }
        	}
        	 Debug.log("acctgTransId ---partyId-----"+partyId);
                Map<String, Object> createAcctgRoleResult = dispatcher.runSync("createAcctgTransRole", createRoleContext);
    	        if (ServiceUtil.isError(createAcctgRoleResult)) {
    	            return ServiceUtil.returnError("Problem in creation of Accounting role", null, null, createRoleContext);
    	        }
    	        Debug.log("acctgTransId ---for Party-----"+partyId);
    	        if(UtilValidate.isNotEmpty(partyId)){
    	        	createRoleContext.put("partyId",partyId);
    	        	Debug.log(" Party is===--------"+partyId);
    	        	createRoleContext.put("roleTypeId","INTERNAL_ORGANIZATIO");
    	        	createAcctgRoleResult = dispatcher.runSync("createAcctgTransRole", createRoleContext);
    		        if (ServiceUtil.isError(createAcctgRoleResult)) {
    		            return ServiceUtil.returnError("Problem in creation of Accounting role", null, null, createAcctgRoleResult);
    		        }
    	        }
        }
        
      //Let's populate FinAccountTransRole
        if(UtilValidate.isNotEmpty(finAccountTransId)){
        	createRoleContext.put("finAccountTransId", finAccountTransId);
        	Debug.log("acctgTransId======loop======"+acctgTransId);
        	GenericValue finAcctTrans = delegator.findOne("FinAccountTrans", UtilMisc.toMap("finAccountTransId",finAccountTransId), true);
        	Debug.log("AcctgTrans============"+finAcctTrans);
        	String finAcctPaymentId=null;
        	String finAcctInvoiceId=null;
        	if(UtilValidate.isNotEmpty(finAcctTrans)){
        		finAcctPaymentId=(String) finAcctTrans.getString("paymentId");
        		finAcctInvoiceId=(String) finAcctTrans.getString("invoiceId");
        		
        	}
        	if(UtilValidate.isNotEmpty(finAcctPaymentId)){
	        	 GenericValue paymentDetail = delegator.findOne("Payment", UtilMisc.toMap("paymentId",finAcctPaymentId), true);
	        	 Debug.log("paymentDetail ----finAccount----"+paymentDetail);
	                if(UtilValidate.isNotEmpty(paymentDetail)){
	                   String paymentTypeId = (String) paymentDetail.getString("paymentTypeId");
	                   String parentTypeId =(String) PaymentWorker.getPaymentTypeParent(delegator, paymentTypeId);
	                   Debug.log("parentTypeId --------"+parentTypeId);
	                   //GenericValue paymentTypeDetails = delegator.findOne("PaymentType", UtilMisc.toMap("paymentTypeId",paymentTypeId), true);
	                   if(UtilValidate.isNotEmpty(parentTypeId)){
	                	   if("RECEIPT".equals(parentTypeId)){
	                		   partyId=(String) paymentDetail.getString("partyIdTo");
	                	   }else if("DISBURSEMENT".equals(parentTypeId)){
	                		   partyId=(String) paymentDetail.getString("partyIdFrom");
	                	   }
	                   }
	                }  
        	}
        	if(UtilValidate.isNotEmpty(finAcctInvoiceId)){
        		 Debug.log("finAccount trans ---invoiceId-----"+finAcctInvoiceId);
        		 GenericValue invoiceDetails = delegator.findOne("Invoice", UtilMisc.toMap("invoiceId",finAcctInvoiceId), true);
        	
                if(UtilValidate.isNotEmpty(invoiceDetails)){
 	                   String invoicetypeId = (String) invoiceDetails.getString("invoiceTypeId");
 	                   GenericValue invoiceTypeDetails = delegator.findOne("InvoiceType", UtilMisc.toMap("invoiceTypeId",invoicetypeId), true);
 	                   if(UtilValidate.isNotEmpty(invoiceTypeDetails)){
 	                	   String invoiceTypeParent=(String)invoiceTypeDetails.getString("parentTypeId");
 	                	   if(UtilValidate.isNotEmpty(invoiceTypeParent)&&"PURCHASE_INVOICE".equals(invoiceTypeParent)){
 	                		   partyId=(String) invoiceDetails.getString("partyId");
 	                	   }else if(UtilValidate.isNotEmpty(invoiceTypeParent)&&"SALES_INVOICE".equals(invoiceTypeParent)){
 	                		   partyId=(String) invoiceDetails.getString("partyIdFrom");
 	                	   }
 	                   }
 	                
                }
        	}
        	 Debug.log("acctgTransId ---partyId-----"+partyId);
                Map<String, Object> createAcctgRoleResult = dispatcher.runSync("createFinAcctTransRole", createRoleContext);
    	        if (ServiceUtil.isError(createAcctgRoleResult)) {
    	            return ServiceUtil.returnError("Problem in creation of Accounting role", null, null, createRoleContext);
    	        }
    	        Debug.log("acctgTransId ---for Party-----"+partyId);
    	        if(UtilValidate.isNotEmpty(partyId)){
    	        	createRoleContext.put("partyId",partyId);
    	        	Debug.log(" Party is===--------"+partyId);
    	        	createRoleContext.put("roleTypeId","INTERNAL_ORGANIZATIO");
    	        	createAcctgRoleResult = dispatcher.runSync("createFinAcctTransRole", createRoleContext);
    		        if (ServiceUtil.isError(createAcctgRoleResult)) {
    		            return ServiceUtil.returnError("Problem in creation of Accounting role", null, null, createAcctgRoleResult);
    		        }
    	        }
        }
        
	   
	}catch (GenericServiceException e) {
		 String errorMsg = "create Invoice Role failed"; 
        Debug.logError(e, module);
        return ServiceUtil.returnError(errorMsg + e.getMessage());
    }catch (GenericEntityException e) {
		Debug.logError(e, "Failed to create accounting role ", module);
		return ServiceUtil.returnError("Failed to create accounting role" + e);
	}       
    
    return ServiceUtil.returnSuccess();	    
}*/



public static Map<String, Object> createAccountingRoleForInvoice(DispatchContext dctx, Map<String, Object> context) {
    Delegator delegator = dctx.getDelegator();
    LocalDispatcher dispatcher = dctx.getDispatcher();  
	String invoiceId = (String) context.get("invoiceId");			
	 GenericValue userLogin = (GenericValue) context.get("userLogin");
	Map<String, Object> createRoleContext = FastMap.newInstance();
	try{
		createRoleContext.put("userLogin", userLogin);
		createRoleContext.put("partyId", "Company");
		createRoleContext.put("roleTypeId", "ACCOUNTING");
        String partyId=null;
        //Let's populate InvoiceRole
        if(UtilValidate.isNotEmpty(invoiceId)){
        	createRoleContext.put("invoiceId", invoiceId);
        	 GenericValue invoice = delegator.findOne("Invoice", UtilMisc.toMap("invoiceId",invoiceId), true);
	        	/* GenericValue invoice = delegator.findOne("Invoice", UtilMisc.toMap("invoiceId",invoiceId), true);
             if(UtilValidate.isNotEmpty(invoice)){
                String invoicetypeId = (String) invoice.getString("invoiceTypeId");
                GenericValue invoiceTypeDetails = delegator.findOne("InvoiceType", UtilMisc.toMap("invoiceTypeId",invoicetypeId), true);
                if(UtilValidate.isNotEmpty(invoiceTypeDetails)){
             	   String invoiceTypeParent=(String)invoiceTypeDetails.getString("parentTypeId");
             	   if(UtilValidate.isNotEmpty(invoiceTypeParent)&&"PURCHASE_INVOICE".equals(invoiceTypeParent)){
             		   partyId=(String) invoice.getString("partyId");
             	   }else if(UtilValidate.isNotEmpty(invoiceTypeParent)&&"SALES_INVOICE".equals(invoiceTypeParent)){
             		   partyId=(String) invoice.getString("partyIdFrom");
             	   }
                }
             } */ 
                Map<String, Object> createInvoiceRoleResult = dispatcher.runSync("createInvoiceRole", createRoleContext);
    	        if (ServiceUtil.isError(createInvoiceRoleResult)) {
    	            return ServiceUtil.returnError("Problem in creation of Accounting role", null, null, createRoleContext);
    	        }
    	        /*if(UtilValidate.isNotEmpty(partyId)){
    	        	createRoleContext.put("partyId",partyId);
    	        	createRoleContext.put("roleTypeId","INTERNAL_ORGANIZATIO");
    	        	createInvoiceRoleResult = dispatcher.runSync("createInvoiceRole", createRoleContext);
    		        if (ServiceUtil.isError(createInvoiceRoleResult)) {
    		            return ServiceUtil.returnError("Problem in creation of Accounting role", null, null, createInvoiceRoleResult);
    		        }
    	        }*/
        }
        
        
	   
	}catch (GenericServiceException e) {
		 String errorMsg = "create Invoice Role failed"; 
        Debug.logError(e, module);
        return ServiceUtil.returnError(errorMsg + e.getMessage());
    }catch (GenericEntityException e) {
		Debug.logError(e, "Failed to create accounting role ", module);
		return ServiceUtil.returnError("Failed to create accounting role" + e);
	}       
    
    return ServiceUtil.returnSuccess();	    
}


public static Map<String, Object> createAccountingRoleForPayment(DispatchContext dctx, Map<String, Object> context) {
    Delegator delegator = dctx.getDelegator();
    LocalDispatcher dispatcher = dctx.getDispatcher();  
	String paymentId = (String) context.get("paymentId");	
		
	 GenericValue userLogin = (GenericValue) context.get("userLogin");
	Map<String, Object> createRoleContext = FastMap.newInstance();
	try{
		createRoleContext.put("userLogin", userLogin);
		createRoleContext.put("partyId", "Company");
		createRoleContext.put("roleTypeId", "ACCOUNTING");
        String partyId=null;
        //Let's populate PaymentRole
        if(UtilValidate.isNotEmpty(paymentId)){
        	createRoleContext.put("paymentId", paymentId);
        	 GenericValue payment = delegator.findOne("Payment", UtilMisc.toMap("paymentId",paymentId), true);	        	
                if(UtilValidate.isNotEmpty(payment)){
                   String paymentTypeId = (String) payment.getString("paymentTypeId");
                   String parentTypeId =(String) PaymentWorker.getPaymentTypeParent(delegator, paymentTypeId);
                   //GenericValue paymentTypeDetails = delegator.findOne("PaymentType", UtilMisc.toMap("paymentTypeId",paymentTypeId), true);
                   if(UtilValidate.isNotEmpty(parentTypeId)){
                	   if("RECEIPT".equals(parentTypeId)){
                		   partyId=(String) payment.getString("partyIdTo");
                	   }else if("DISBURSEMENT".equals(parentTypeId)){
                		   partyId=(String) payment.getString("partyIdFrom");
                	   }
                   }
                }  
                Map<String, Object> createPaymentRoleResult = dispatcher.runSync("createPaymentRole", createRoleContext);
    	        if (ServiceUtil.isError(createPaymentRoleResult)) {
    	            return ServiceUtil.returnError("Problem in creation of Accounting role", null, null, createRoleContext);
    	        }
    	        if(UtilValidate.isNotEmpty(partyId)){
    	        	createRoleContext.put("partyId",partyId);	    	        	
    	        	createRoleContext.put("roleTypeId","INTERNAL_ORGANIZATIO");
    	        	createPaymentRoleResult = dispatcher.runSync("createPaymentRole", createRoleContext);
    		        if (ServiceUtil.isError(createPaymentRoleResult)) {
    		            return ServiceUtil.returnError("Problem in creation of Accounting role", null, null, createPaymentRoleResult);
    		        }
    	        }
        }
	   
	}catch (GenericServiceException e) {
		 String errorMsg = "create Invoice Role failed"; 
        Debug.logError(e, module);
        return ServiceUtil.returnError(errorMsg + e.getMessage());
    }catch (GenericEntityException e) {
		Debug.logError(e, "Failed to create accounting role ", module);
		return ServiceUtil.returnError("Failed to create accounting role" + e);
	}       
    
    return ServiceUtil.returnSuccess();	    
}

public static Map<String, Object> createAccountingRoleForAcctgTrans(DispatchContext dctx, Map<String, Object> context) {
    Delegator delegator = dctx.getDelegator();
    LocalDispatcher dispatcher = dctx.getDispatcher();  		
	String acctgTransId = (String) context.get("acctgTransId");		
	String orgPartyId=(String)context.get("organizationPartyId");
	 GenericValue userLogin = (GenericValue) context.get("userLogin");
	Map<String, Object> createRoleContext = FastMap.newInstance();
	try{
		createRoleContext.put("userLogin", userLogin);
		createRoleContext.put("partyId", "Company");
		createRoleContext.put("roleTypeId", "ACCOUNTING");
        String partyId=null;
        //Let's populate AcctgTransRole
        if(UtilValidate.isNotEmpty(acctgTransId)){
        	createRoleContext.put("acctgTransId", acctgTransId);
        	GenericValue AcctgTrans = delegator.findOne("AcctgTrans", UtilMisc.toMap("acctgTransId",acctgTransId), true);
        	String finAccountTransId=(String) AcctgTrans.getString("finAccountTransId");
        	String acctgTransType=(String) AcctgTrans.getString("acctgTransTypeId");
        	String acctgPaymentId=null;
        	String acctgInvoiceId=null;
        	if(UtilValidate.isNotEmpty(AcctgTrans)){
        		acctgPaymentId=(String) AcctgTrans.getString("paymentId");
        		acctgInvoiceId=(String) AcctgTrans.getString("invoiceId");
        		
        	}
        	if(UtilValidate.isNotEmpty(acctgPaymentId)){
	        	 GenericValue paymentDetail = delegator.findOne("Payment", UtilMisc.toMap("paymentId",acctgPaymentId), true);
	                if(UtilValidate.isNotEmpty(paymentDetail)){
	                   String paymentTypeId = (String) paymentDetail.getString("paymentTypeId");
	                   String parentTypeId =(String) PaymentWorker.getPaymentTypeParent(delegator, paymentTypeId);
	                   //GenericValue paymentTypeDetails = delegator.findOne("PaymentType", UtilMisc.toMap("paymentTypeId",paymentTypeId), true);
	                   if(UtilValidate.isNotEmpty(parentTypeId)){
	                	   if("RECEIPT".equals(parentTypeId)){
	                		   partyId=(String) paymentDetail.getString("partyIdTo");
	                	   }else if("DISBURSEMENT".equals(parentTypeId)){
	                		   partyId=(String) paymentDetail.getString("partyIdFrom");
	                	   }
	                   }
	                }  
        	}
        	if(UtilValidate.isNotEmpty(acctgInvoiceId)){
        		 GenericValue invoiceDetails = delegator.findOne("Invoice", UtilMisc.toMap("invoiceId",acctgInvoiceId), true);
        		 List andExprs1=FastList.newInstance();
        		 andExprs1.add(EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "COST_CENTER_ID"));
    		     andExprs1.add(EntityCondition.makeCondition("invoiceId", EntityOperator.EQUALS, acctgInvoiceId));
    		     List invRoleList = delegator.findList("InvoiceRole", EntityCondition.makeCondition(andExprs1,EntityOperator.AND), null, null, null, false);

                if(UtilValidate.isNotEmpty(invRoleList)){
                	  GenericValue invRoleVal = EntityUtil.getFirst(invRoleList);
                	  partyId=(String) invRoleVal.get("partyId");
 	                  /* String invoicetypeId = (String) invoiceDetails.getString("invoiceTypeId");
 	                   GenericValue invoiceTypeDetails = delegator.findOne("InvoiceType", UtilMisc.toMap("invoiceTypeId",invoicetypeId), true);
 	                   if(UtilValidate.isNotEmpty(invoiceTypeDetails)){
 	                	   String invoiceTypeParent=(String)invoiceTypeDetails.getString("parentTypeId");
 	                	   if(UtilValidate.isNotEmpty(invoiceTypeParent)&&"PURCHASE_INVOICE".equals(invoiceTypeParent)){
 	                		   partyId=(String) invoiceDetails.getString("partyId");
 	                	   }else if(UtilValidate.isNotEmpty(invoiceTypeParent)&&"SALES_INVOICE".equals(invoiceTypeParent)){
 	                		   partyId=(String) invoiceDetails.getString("partyIdFrom");
 	                	   }
 	                   }*/
 	                
                }
        	}
                Map<String, Object> createAcctgRoleResult = dispatcher.runSync("createAcctgTransRole", createRoleContext);
    	        if (ServiceUtil.isError(createAcctgRoleResult)) {
    	            return ServiceUtil.returnError("Problem in creation of Accounting role", null, null, createRoleContext);
    	        }
    	        Debug.log("partyId==============="+partyId);
    	        Debug.log("orgPartyId==============="+orgPartyId);
    	        if(UtilValidate.isNotEmpty(finAccountTransId)){
    	        	GenericValue finAcctTrans = delegator.findOne("FinAccountTrans", UtilMisc.toMap("finAccountTransId",finAccountTransId), true);
    	        	partyId=(String)finAcctTrans.getString("costCenterId");
    	        	  Debug.log("coset==============="+partyId);
    	        }
    	        if(UtilValidate.isEmpty(partyId)&&"JOURNAL".equals(acctgTransType)&&!"Company".equals(orgPartyId)){
    	        	partyId=orgPartyId;
    	        }
    	        if(UtilValidate.isNotEmpty(partyId)){
    	        	createRoleContext.put("partyId",partyId);
    	        	createRoleContext.put("roleTypeId","INTERNAL_ORGANIZATIO");
    	        	createAcctgRoleResult = dispatcher.runSync("createAcctgTransRole", createRoleContext);
    		        if (ServiceUtil.isError(createAcctgRoleResult)) {
    		            return ServiceUtil.returnError("Problem in creation of Accounting role", null, null, createAcctgRoleResult);
    		        }
    	        }
        }        
	   
	}catch (GenericServiceException e) {
		 String errorMsg = "create Invoice Role failed"; 
        Debug.logError(e, module);
        return ServiceUtil.returnError(errorMsg + e.getMessage());
    }catch (GenericEntityException e) {
		Debug.logError(e, "Failed to create accounting role ", module);
		return ServiceUtil.returnError("Failed to create accounting role" + e);
	}  
	Map<String, Object> resultVal =FastMap.newInstance();
	resultVal.put("acctgTransId", acctgTransId);
    return resultVal;
  //  return ServiceUtil.returnSuccess();	    
}
public static Map<String, Object> createAccountingRoleForFinTrans(DispatchContext dctx, Map<String, Object> context) {
    Delegator delegator = dctx.getDelegator();
    LocalDispatcher dispatcher = dctx.getDispatcher();  
	String finAccountTransId=(String) context.get("finAccountTransId");
    GenericValue userLogin = (GenericValue) context.get("userLogin");
	Map<String, Object> createRoleContext = FastMap.newInstance();
	try{
		createRoleContext.put("userLogin", userLogin);
		createRoleContext.put("partyId", "Company");
		createRoleContext.put("roleTypeId", "ACCOUNTING");
        String partyId=null;
        //Let's populate AcctgTransRole
        
      //Let's populate FinAccountTransRole
        if(UtilValidate.isNotEmpty(finAccountTransId)){
        	createRoleContext.put("finAccountTransId", finAccountTransId);
        	GenericValue finAcctTrans = delegator.findOne("FinAccountTrans", UtilMisc.toMap("finAccountTransId",finAccountTransId), true);
        	GenericValue finAcctTransAttb = delegator.findOne("FinAccountTransAttribute", UtilMisc.toMap("finAccountTransId",finAccountTransId,"attrName","FATR_CONTRA"), true);
        	Debug.log("finAcctTransAttb==========="+finAcctTransAttb);
        	String finAcctPaymentId=null;
        	if(UtilValidate.isNotEmpty(finAcctTrans)){
        		finAcctPaymentId=(String) finAcctTrans.getString("paymentId");	       
        		//GenericValue finAccnt = delegator.findOne("FinAccount", UtilMisc.toMap("finAccountId",finAcctTrans.getString("finAccountId")), true);	        		
        		//String finAcctParty=(String) finAccnt.get("ownerPartyId");
        		String finAcctParty=(String) finAcctTrans.get("costCenterId");
        		Debug.log("costCenter==========="+finAcctParty);
        		/*if(UtilValidate.isNotEmpty(finAcctParty)){
        			GenericValue partyRole = delegator.findOne("PartyRole", UtilMisc.toMap("partyId",finAcctParty,"roleTypeId","INTERNAL_ORGANIZATIO"), true);
        			if(UtilValidate.isEmpty(partyRole)){
        				if(UtilValidate.isNotEmpty(finAcctTransAttb)){
	        				String finTransAttributeId=(String)finAcctTransAttb.get("attrValue");
	        				GenericValue finTransAtt = delegator.findOne("FinAccountTrans", UtilMisc.toMap("finAccountTransId",finTransAttributeId), true);
	        				String finAttributeId=(String)finTransAtt.get("finAccountId");
	        				GenericValue finTransAttFinAccount = delegator.findOne("FinAccount", UtilMisc.toMap("finAccountId",finAttributeId), true);
	        				finAcctParty=(String)finTransAttFinAccount.get("costCenterId");
        				}
        			}
        		}*/
        		Debug.log("finAcctParty====after======="+finAcctParty);
        		partyId=finAcctParty;
        		
        	}	        	
        	if(UtilValidate.isNotEmpty(finAcctPaymentId)){
	        	 GenericValue paymentDetail = delegator.findOne("Payment", UtilMisc.toMap("paymentId",finAcctPaymentId), true);
	        	
	                if(UtilValidate.isNotEmpty(paymentDetail)){
	                   String paymentTypeId = (String) paymentDetail.getString("paymentTypeId");
	                   String parentTypeId =(String) PaymentWorker.getPaymentTypeParent(delegator, paymentTypeId);		                 
	                   if(UtilValidate.isNotEmpty(parentTypeId)){
	                	   if("RECEIPT".equals(parentTypeId)){
	                		   partyId=(String) paymentDetail.getString("partyIdTo");
	                	   }else if("DISBURSEMENT".equals(parentTypeId)){
	                		   partyId=(String) paymentDetail.getString("partyIdFrom");
	                	   }
	                	   Debug.log("partyId====paymebnts======="+partyId);
	                   }
	                }  
        	}
        	
            Map<String, Object> createAcctgRoleResult = dispatcher.runSync("createFinAcctTransRole", createRoleContext);
	        if (ServiceUtil.isError(createAcctgRoleResult)) {
	            return ServiceUtil.returnError("Problem in creation of Accounting role", null, null, createRoleContext);
	        }
	        Debug.log("partyId============"+partyId);
	        if(UtilValidate.isNotEmpty(partyId)){
	        	createRoleContext.put("partyId",partyId);	    	        	
	        	createRoleContext.put("roleTypeId","INTERNAL_ORGANIZATIO");
	        	createAcctgRoleResult = dispatcher.runSync("createFinAcctTransRole", createRoleContext);
		        if (ServiceUtil.isError(createAcctgRoleResult)) {
		            return ServiceUtil.returnError("Problem in creation of Accounting role", null, null, createAcctgRoleResult);
		        }
	        }
        }
        
	   
	}catch (GenericServiceException e) {
		 String errorMsg = "create Invoice Role failed"; 
        Debug.logError(e, module);
        return ServiceUtil.returnError(errorMsg + e.getMessage());
    }catch (GenericEntityException e) {
		Debug.logError(e, "Failed to create accounting role ", module);
		return ServiceUtil.returnError("Failed to create accounting role" + e);
	}       
    
    return ServiceUtil.returnSuccess();	    
}
	
	//createPaymentApplicationForInvoiceEmp
	public static Map<String, Object> createPaymentApplicationForInvoiceEmp(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();              
        Locale locale = (Locale) context.get("locale");     
        String paymentMethodType = (String) context.get("paymentMethodTypeId");
        String paymentMethodId = (String) context.get("paymentMethodId");
        Timestamp effectiveDate = (Timestamp) context.get("effectiveDate");
        Timestamp paymentDate = (Timestamp) context.get("paymentDate");
        String paymentTypeId = (String) context.get("paymentTypeId");
        String finAccountId = (String) context.get("finAccountId");
        String purposeTypeId = (String) context.get("purposeTypeId");
        String invoicePartyIdFrom =(String) context.get("invoicePartyIdFrom");
        String invoicePartyIdTo = (String) context.get("invoicePartyIdTo");
        String invoiceId =(String) context.get("invoiceId");
        String headerCostCenterId =(String) context.get("headerCostCenterId");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Map<String, Object> invoiceResult = ServiceUtil.returnSuccess();
        BigDecimal amount;
        String paymentId = "";
        try{
        	if(UtilValidate.isNotEmpty(headerCostCenterId) && UtilValidate.isNotEmpty(invoiceId)){
        		GenericValue invoice = delegator.findOne("Invoice", UtilMisc.toMap("invoiceId",invoiceId), false);
        		if(UtilValidate.isNotEmpty(invoice)){
        			invoice.set("costCenterId", headerCostCenterId);
        			invoice.store();
        		}
        	}
        	invoiceResult = dispatcher.runSync("getInvoiceTotal", UtilMisc.toMap("invoiceId",invoiceId ,"userLogin" ,userLogin));
        	amount = (BigDecimal)invoiceResult.get("amountTotal");
        	if (amount.compareTo(ZERO) > 0) {
        		Map<String, Object> paymentCtx = UtilMisc.<String, Object>toMap("paymentTypeId", paymentTypeId);
      	        paymentCtx.put("paymentMethodId", paymentMethodId);//from AP mandatory
      	        paymentCtx.put("organizationPartyId", invoicePartyIdFrom);
                paymentCtx.put("partyId", invoicePartyIdTo);
                paymentCtx.put("paymentPurposeType", purposeTypeId);
      	        paymentCtx.put("paymentDate", UtilDateTime.nowTimestamp());
      	        paymentCtx.put("statusId", "PMNT_NOT_PAID");
      	        if (UtilValidate.isNotEmpty(finAccountId) ) {
      	            paymentCtx.put("finAccountId", finAccountId);                        	
      	        }
      	        paymentCtx.put("userLogin", userLogin);
      	        paymentCtx.put("amount", amount);
      	        paymentCtx.put("invoices", UtilMisc.toList(invoiceId));
      	        Map<String, Object> paymentResult = dispatcher.runSync("createPaymentAndApplicationForInvoices", paymentCtx);
	  	        if (ServiceUtil.isError(paymentResult)) {
	  	            Debug.logError("Problems in service createPaymentAndApplicationForInvoices", module);
		  			//request.setAttribute("_ERROR_MESSAGE_", "Error in service createPaymentAndApplicationForInvoices");
		  			return ServiceUtil.returnError("Problem calling 'createPaymentAndApplicationForInvoices'");
	  	        }
	  	        paymentId = (String)paymentResult.get("paymentId");
        	}
        	
        	Map<String, Object> setPaymentStatusMap = UtilMisc.<String, Object>toMap("userLogin", userLogin);
        	setPaymentStatusMap.put("paymentId", paymentId);
        	setPaymentStatusMap.put("statusId", "PMNT_SENT");
        	if(UtilValidate.isNotEmpty(finAccountId)){
        		setPaymentStatusMap.put("finAccountId", finAccountId);
        	}
        	setPaymentStatusMap.put("depositReceiptFlag", "Y");
            Map<String, Object> pmntResults = dispatcher.runSync("setPaymentStatus", setPaymentStatusMap);
        }
        catch (GenericServiceException e) {
            return ServiceUtil.returnError("Problem calling 'createPaymentAndApplicationForInvoices'");
        }
        catch (GenericEntityException e) {
            return ServiceUtil.returnError("Problem updating  'invoice'");
        }
        result.put("paymentId",paymentId);
        return ServiceUtil.returnSuccess("Payment and Application Successful. PaymentId: "+paymentId+", InvoiceId: "+invoiceId);
	}

}