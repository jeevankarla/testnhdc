import org.ofbiz.base.util.*;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import java.util.*;
import java.lang.*;
import org.ofbiz.entity.*;
import org.ofbiz.entity.condition.*;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import java.sql.*;
import javolution.util.FastList;
import javolution.util.FastMap;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.ServiceUtil;

import java.math.BigDecimal;
import java.math.MathContext;
import org.ofbiz.base.util.UtilNumber;
import in.vasista.vbiz.byproducts.ByProductNetworkServices;
import org.ofbiz.accounting.invoice.InvoiceWorker;
import in.vasista.vbiz.byproducts.SalesInvoiceServices;
import org.ofbiz.party.party.PartyHelper;



dctx = dispatcher.getDispatchContext();
context.put("dctx",dctx);
fromDate=parameters.fromDate;
thruDate=parameters.thruDate;
reportTypeFlag = parameters.reportTypeFlag;

dctx = dispatcher.getDispatchContext();
fromDateTime = null;
thruDateTime = null;
def sdf = new SimpleDateFormat("MMMM dd, yyyy");
try {
	fromDateTime = new java.sql.Timestamp(sdf.parse(fromDate).getTime());
	thruDateTime = new java.sql.Timestamp(sdf.parse(thruDate).getTime());
} catch (ParseException e) {
	Debug.logError(e, "Cannot parse date string: "+fromDate, "");
}
fromDateTime = UtilDateTime.getDayStart(fromDateTime);
dayBegin = UtilDateTime.getDayStart(fromDateTime);
dayEnd = UtilDateTime.getDayEnd(thruDateTime);
context.fromDate = dayBegin;
context.thruDate = dayEnd;
totalDays=UtilDateTime.getIntervalInDays(fromDateTime,thruDateTime);
isByParty = Boolean.TRUE;
if(totalDays > 32){
	Debug.logError("You Cannot Choose More Than 31 Days.","");
	context.errorMessage = "You Cannot Choose More Than 31 Days";
	return;
}
exprList=[];
conditionList=[];
if(reportTypeFlag=="ELIGIBLE"){
 exprList.add(EntityCondition.makeCondition("productCategoryId", EntityOperator.EQUALS, "ELIGIBLE"));
}else{
 exprList.add(EntityCondition.makeCondition("productCategoryId", EntityOperator.EQUALS, "INELIGIBLE"));	
}
exprList.add(EntityCondition.makeCondition("productCategoryTypeId", EntityOperator.EQUALS, "PUR_ANLS_CODE"));
condition = EntityCondition.makeCondition(exprList, EntityOperator.AND);


//get product from ProductCategory
productCatDetails = delegator.findList("ProductCategoryAndMember", condition, null, null, null, false);
productIds = EntityUtil.getFieldListFromEntityList(productCatDetails, "productId", true);
conditionList.add(EntityCondition.makeCondition("productCategoryTypeId", EntityOperator.EQUALS, "PUR_ANLS_CODE"));
conditionList.add(EntityCondition.makeCondition("primaryParentCategoryId", EntityOperator.NOT_EQUAL, null));
conditionList.add(EntityCondition.makeCondition("productId", EntityOperator.IN, productIds));
condition1 = EntityCondition.makeCondition(conditionList, EntityOperator.AND);

productCategoryMember = delegator.findList("ProductCategoryAndMember",condition1, null, null, null, false );

productCatMap=[:]
productPrimaryCatMap=[:]
productCategoryMember.each{prodCatMember ->
	prodCatMember.productId
	productCatMap[prodCatMember.productId] = prodCatMember.productCategoryId;
	productPrimaryCatMap[prodCatMember.productCategoryId] = prodCatMember.primaryParentCategoryId;
}
invoiceTotals = SalesInvoiceServices.getPeriodSalesInvoiceTotals(dctx, [isPurchaseInvoice:true, isQuantityLtrs:true,isQuantityLtrs:true,fromDate:dayBegin, thruDate:dayEnd]).get("invoiceIdTotals");
// Purchase abstract Sales report
reportTypeFlag = parameters.reportTypeFlag;
				prodCatMap =FastMap.newInstance();
				prodPrimaryCategoryMap=FastMap.newInstance();
				if(UtilValidate.isNotEmpty(invoiceTotals)){
					invoiceTotals.each { invoice ->
						if(UtilValidate.isNotEmpty(invoice)){
							invoiceId = invoice.getKey();
							prodTotals = invoice.getValue().get("productTotals");
							invoiceItemList = delegator.findList("InvoiceAndItem",EntityCondition.makeCondition("invoiceId", EntityOperator.EQUALS , invoiceId)  , null, null, null, false );
							if(UtilValidate.isNotEmpty(prodTotals)){
								prodTotals.each{productValue ->
									prodCategoryId="";
									if(UtilValidate.isNotEmpty(productValue)){
										currentProduct = productValue.getKey();
										product = delegator.findOne("Product", [productId : currentProduct], false);
										productId = productValue.getKey();
										GenericValue prodInvoiceItem=null;
										List<GenericValue> prodInvoiceItemList = EntityUtil.filterByCondition(invoiceItemList, EntityCondition.makeCondition(EntityCondition.makeCondition("invoiceId", EntityOperator.EQUALS, invoiceId),EntityOperator.AND,
											EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId)));
										if(UtilValidate.isNotEmpty(prodInvoiceItemList)){
											prodInvoiceItem=prodInvoiceItemList.getFirst();
										}
										//to Exlude Tax Total call with Parameter False 
										invItemVal=org.ofbiz.accounting.invoice.InvoiceWorker.getPurchaseInvoiceItemTotal(prodInvoiceItem,false);
										if(UtilValidate.isNotEmpty(productCatMap)&& productCatMap.get(productId)){
											// get category
											prodCategoryId=productCatMap.get(productId);
											prodPrimaryCategoryId=productPrimaryCatMap.get(prodCategoryId);
											if(UtilValidate.isEmpty(prodPrimaryCategoryMap[prodPrimaryCategoryId])){
												prodCatMap=[:];
												cstRevenue = productValue.getValue().get("cstRevenue");
												tempProdMap = FastMap.newInstance();
												tempProdMap["totalRevenue"] = invItemVal+cstRevenue;
												prodCatMap[prodCategoryId] = tempProdMap;
												prodPrimaryCategoryMap[prodPrimaryCategoryId]=prodCatMap;
											 }else{
												prodCatMap = [:];
												prodCatMap.putAll(prodPrimaryCategoryMap.get(prodPrimaryCategoryId));
													if(UtilValidate.isEmpty(prodCatMap[prodCategoryId])){
														cstRevenue = productValue.getValue().get("cstRevenue");
														tempProdMap = FastMap.newInstance();
														tempProdMap["totalRevenue"] = invItemVal+cstRevenue;
														prodCatMap[prodCategoryId] = tempProdMap;
													}else{
														tempMap = [:];
														productMap = [:];
														tempMap.putAll(prodCatMap.get(prodCategoryId));
														totalRevenue = productValue.getValue().get("totalRevenue");
														tempMap["totalRevenue"]+= invItemVal+productValue.getValue().get("cstRevenue");
														prodCatMap[prodCategoryId] = tempMap;
													}
													prodPrimaryCategoryMap[prodPrimaryCategoryId]=prodCatMap;
										    }
										} 
									}
								}
							}
						}
					}
				}
			//end if of salesInvoiceTotals}
	context.put("reportTypeFlag",reportTypeFlag);
	context.put("prodMap",prodPrimaryCategoryMap);

