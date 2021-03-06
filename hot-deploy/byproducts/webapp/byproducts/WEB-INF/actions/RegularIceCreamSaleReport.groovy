import org.ofbiz.base.util.*;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.util.EntityFindOptions;
import java.util.*;
import java.lang.*;
import org.ofbiz.entity.*;
import org.ofbiz.entity.condition.*;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityListIterator;
import java.sql.*;
import javolution.util.FastList;
import javolution.util.FastMap;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.party.party.PartyHelper;
import java.math.BigDecimal;
import java.math.MathContext;
import org.ofbiz.base.util.UtilNumber;
import in.vasista.vbiz.byproducts.ByProductNetworkServices;
import org.ofbiz.accounting.invoice.InvoiceWorker;
import in.vasista.vbiz.byproducts.SalesInvoiceServices;
dctx = dispatcher.getDispatchContext();
context.put("dctx",dctx);
fromDate=parameters.fromDate;
thruDate=parameters.thruDate;
categoryType=parameters.categoryType;
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
boolean isPurchaseInvoice = Boolean.FALSE;
if(UtilValidate.isNotEmpty(parameters.isPurchaseInvoice) && (parameters.isPurchaseInvoice=="Y")){
	isPurchaseInvoice = Boolean.TRUE;
}
fromDateTime = UtilDateTime.getDayStart(fromDateTime);
dayBegin = UtilDateTime.getDayStart(fromDateTime);
dayEnd = UtilDateTime.getDayEnd(thruDateTime);
context.fromDate = fromDateTime;
context.thruDate = thruDateTime;
totalDays=UtilDateTime.getIntervalInDays(fromDateTime,thruDateTime);
isByParty = Boolean.TRUE;
/*if(totalDays > 32){
	Debug.logError("You Cannot Choose More Than 31 Days.","");
	context.errorMessage = "You Cannot Choose More Than 31 Days";
	return;
}
*/partyIds=[];
if(UtilValidate.isEmpty(parameters.partyId)){
	if(categoryType.equals("ICE_CREAM_NANDINI")||categoryType.equals("All")){
	nandiniPartyIds = ByProductNetworkServices.getPartyByRoleType(dctx, [userLogin: userLogin, roleTypeId: "IC_WHOLESALE"]).get("partyIds");
	partyIds.addAll(nandiniPartyIds);
	}
	if(categoryType.equals("ICE_CREAM_AMUL")||categoryType.equals("All")){
	amulPartyIds = ByProductNetworkServices.getPartyByRoleType(dctx, [userLogin: userLogin, roleTypeId: "EXCLUSIVE_CUSTOMER"]).get("partyIds");
	partyIds.addAll(amulPartyIds);
	}
	if(categoryType.equals("UNITS")||categoryType.equals("All")){
		unitPartyIds = ByProductNetworkServices.getPartyByRoleType(dctx, [userLogin: userLogin, roleTypeId: "UNITS"]).get("partyIds");
		partyIds.addAll(unitPartyIds);
	}
	if(categoryType.equals("UNION")||categoryType.equals("All")){
		unionPartyIds = ByProductNetworkServices.getPartyByRoleType(dctx, [userLogin: userLogin, roleTypeId: "UNION"]).get("partyIds");
		partyIds.addAll(unionPartyIds);
	}
	if(categoryType.equals("DEPOT_CUSTOMER")||categoryType.equals("All")){
		depotPartyIds = ByProductNetworkServices.getPartyByRoleType(dctx, [userLogin: userLogin, roleTypeId: "DEPOT_CUSTOMER"]).get("partyIds");
		partyIds.addAll(depotPartyIds);
	}
	if(UtilValidate.isEmpty(categoryType)){
		EntityFindOptions efo = new EntityFindOptions();
		efo.setDistinct(true);
		fieldToSelect = UtilMisc.toSet("partyId");
		EntityListIterator partyRoleList = delegator.find("PartyRole",null,null,fieldToSelect,null,efo);
		partyIds=EntityUtil.getFieldListFromEntityListIterator(partyRoleList, "partyId", true);
	}
}else{
	partyIds.add(parameters.partyId);
}
//dayWiseTotals = SalesInvoiceServices.getPeriodSalesInvoiceTotals(dctx, [partyIds:partyIds, isQuantityLtrs:true,fromDate:dayBegin, thruDate:dayEnd]).get("invoiceIdTotals");
dayWiseInvoice=FastMap.newInstance();
shippingDetails=[:];
prodTempMap=[:];
ppdMap=[:];
vatAdjMap=[:];
invAdjMap=[:];
vatMap=[:];
// Invoice Sales Abstract
	dayWisePpdMap = [:];
	productDetailsList=[];
	for( i=0 ; i <= (totalDays); i++){
		currentDay =UtilDateTime.addDaysToTimestamp(fromDateTime, i);
		dayBegin=UtilDateTime.getDayStart(currentDay);
		dayEnd=UtilDateTime.getDayEnd(currentDay);
		invoiceMap = [:];
		if(UtilValidate.isNotEmpty(partyIds)){
			invoiceTaxMap = SalesInvoiceServices.getInvoiceSalesTaxItems(dctx, [partyIds:partyIds,fromDate:dayBegin, thruDate:dayEnd]).get("invoiceTaxMap");
			inputMap=[:];
			inputMap.put("partyIds", partyIds);
			inputMap.put("isQuantityLtrs", true);
			inputMap.put("fromDate", dayBegin);
			inputMap.put("thruDate", dayEnd);
			inputMap.put("isPurchaseInvoice", isPurchaseInvoice);
			salesInvoiceTotals = SalesInvoiceServices.getPeriodSalesInvoiceTotals(dctx, inputMap);
			if(UtilValidate.isNotEmpty(salesInvoiceTotals)){
				invoiceTotals = salesInvoiceTotals.get("invoiceIdTotals");
				if(UtilValidate.isNotEmpty(invoiceTotals)){
					invoiceTotals.each { invoice ->
						if(UtilValidate.isNotEmpty(invoice)){
							invoiceId = "";
							partyName = "";
							idValue = "";
							basicRevenue=0;
							bedRevenue=0;
							vatRevenue=0;
							cstRevenue=0;
							totalRevenue=0;
							ppd=0;
							vatAdj=0;
							finalMap=FastMap.newInstance();
							invoiceId = invoice.getKey();
									if(UtilValidate.isNotEmpty(invoiceTaxMap) && invoiceTaxMap.containsKey(invoiceId)){
							          if(invoiceTaxMap.get(invoiceId).containsKey("PPD_PROMO_ADJ") ){
										  ppd=invoiceTaxMap.get(invoiceId).get("PPD_PROMO_ADJ");
										  vatAdj=invoiceTaxMap.get(invoiceId).get("VAT_SALE_ADJ");
										  ppdMap[invoiceId]=ppd;
										  vatAdjMap[invoiceId]=vatAdj;
							          }
									}
									if(UtilValidate.isNotEmpty(invoice.getValue().invoiceDateStr)){
										invoiceDate = invoice.getValue().invoiceDateStr;
									}
									invoiceDetails = delegator.findOne("Invoice",[invoiceId : invoiceId] , false);
									invoicePartyId = invoiceDetails.partyId;
									prodTotals = invoice.getValue().get("productTotals");
									
									invQuantity=0;
									invBasicRevenue=0;
									invBedRevenue=0;
									invVatRevenue=0;
									invCstRevenue=0;
									InvTotal=0;
									totalMap=[:];
									quantity = invoice.getValue().get("total");
									basicRevenue = invoice.getValue().get("basicRevenue");
									bedRevenue = invoice.getValue().get("bedRevenue");
									vatRevenue =invoice.getValue().get("vatRevenue");
									cstRevenue = invoice.getValue().get("cstRevenue");
									totalRevenue = invoice.getValue().get("totalRevenue");
									bedRevenue = bedRevenue+invoice.getValue().get("bedCessRevenue");
									bedRevenue = bedRevenue+invoice.getValue().get("bedSecCessRevenue");
									totalMap["quantity"]=quantity;
									totalMap["basicRevenue"]=basicRevenue;
									totalMap["bedRevenue"]=bedRevenue;
									totalMap["vatRevenue"]=vatRevenue;
									totalMap["cstRevenue"]=cstRevenue;
									totalMap["totalRevenue"]=basicRevenue+cstRevenue+vatRevenue+bedRevenue;
									invAdjMap[invoiceId]=totalRevenue+vatAdj+ppd;
									vatMap[invoiceId]=vatAdj+vatRevenue;
									tempVariantMap =FastMap.newInstance();
									if(UtilValidate.isNotEmpty(prodTotals)){
										prodTotals.each{productValue ->
											virtualProductId="";
											if(UtilValidate.isNotEmpty(productValue)){
												currentProduct = productValue.getKey();
												virtualProductId = currentProduct;
												product = delegator.findOne("Product", [productId : currentProduct], false);
												productId = productValue.getKey();
													exprList=[];
													if(product.productTypeId == "FINISHED_GOOD" && UtilValidate.isNotEmpty(categoryType) && categoryType.equalsIgnoreCase("UNITS")){
														
															exprList.add(EntityCondition.makeCondition("productCategoryTypeId", EntityOperator.EQUALS, "SALES_ACANLY"));
														
													}else{
														exprList.add(EntityCondition.makeCondition("productCategoryTypeId", EntityOperator.EQUALS, "IC_CAT_RPT"));
													}
													exprList.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId));
													condition = EntityCondition.makeCondition(exprList, EntityOperator.AND);
													productList = delegator.findList("ProductCategoryAndMember", condition, null, null, null, false);
													GenericValue productEntry = null;
													 if(UtilValidate.isNotEmpty(productList)){
														productEntry=EntityUtil.getFirst(productList);
													    virtualProductId = productEntry.get("productCategoryId");
													 }
													 virtualProductId = "";
													 if(UtilValidate.isNotEmpty(productEntry) && productEntry.categoryName && product.productTypeId == "FINISHED_GOOD"){
														 virtualProductId = productEntry.categoryName;
													 }
													 else if(product.brandName){
														 virtualProductId = product.brandName;
													 }
													  if(categoryType.equals("UNITS")){
														    if(UtilValidate.isEmpty(prodTempMap[product.brandName])){
															  temp =[:]
															  temp.put("qtyLtrs", productValue.getValue().get("total"));
															  temp.put("amount" , productValue.getValue().get("totalRevenue"));
															  temp.put("productType", product.productTypeId);
															  temp.put("productCategory", product.primaryProductCategoryId);
															  if(product.productTypeId == "FINISHED_GOOD" && UtilValidate.isNotEmpty(productEntry) && productEntry.categoryName){
																  temp.put("brandName", productEntry.categoryName);
															  }
															  else{
															  temp.put("brandName", product.brandName);
															  }
															  prodTempMap.put(product.brandName, temp);
															}else{
																totProd =0;
																totProd=prodTempMap.get(product.brandName);
																totProd["qtyLtrs"]+= productValue.getValue().get("total");
																totProd["amount"]+= productValue.getValue().get("totalRevenue");
																prodTempMap.put(product.brandName,totProd);
															}
															productDetailsList = UtilMisc.sortMaps(prodTempMap.values().asList(), UtilMisc.toList("productCategory"));
														}
														if(UtilValidate.isEmpty(tempVariantMap[virtualProductId])){
															quantity =productValue.getValue().get("total");
															basicRevenue = productValue.getValue().get("basicRevenue");
															bedRevenue=productValue.getValue().get("bedRevenue");
															vatRevenue = productValue.getValue().get("vatRevenue");
															cstRevenue = productValue.getValue().get("cstRevenue");
															totalRevenue = productValue.getValue().get("totalRevenue");
															bedRevenue=bedRevenue+productValue.getValue().get("bedCessRevenue");
															bedRevenue=bedRevenue+productValue.getValue().get("bedSecCessRevenue");
															tempProdMap = FastMap.newInstance();
															tempProdMap["quantity"] = quantity;
															tempProdMap["invoiceId"]=invoiceId;
															if(basicRevenue>0){
															 tempProdMap["basicRevenue"] = basicRevenue;
															}else{
															 basicRevenue=0;
															 tempProdMap["basicRevenue"] = 0;
															}
															if(bedRevenue>0){
																tempProdMap["bedRevenue"] = bedRevenue;
															}else{
																bedRevenue=0;
																tempProdMap["bedRevenue"] = 0;
															}
															if(vatRevenue>0){
																tempProdMap["vatRevenue"] = vatRevenue;
															}else{
																vatRevenue=0;
																tempProdMap["vatRevenue"] = 0;
															}
															if(cstRevenue>0){
															   tempProdMap["cstRevenue"] = cstRevenue;
															}else{
															   cstRevenue=0;
															   tempProdMap["cstRevenue"] = 0;
															}
															if(totalRevenue>0){
															 tempProdMap["totalRevenue"] = basicRevenue+bedRevenue+vatRevenue+cstRevenue;
															}else{
															 tempProdMap["totalRevenue"] = 0;
															}
															temp=FastMap.newInstance();
															temp.putAll(tempProdMap);
															tempVariantMap[virtualProductId] = temp;
														}else{
															tempMap = [:];
															productMap = [:];
															tempMap.putAll(tempVariantMap.get(virtualProductId));
															productMap.putAll(tempMap);
															
															quantity =productValue.getValue().get("total");
															basicRevenue = productValue.getValue().get("basicRevenue");
															bedRevenue=productValue.getValue().get("bedRevenue");
															vatRevenue = productValue.getValue().get("vatRevenue");
															cstRevenue = productValue.getValue().get("cstRevenue");
															totalRevenue = productValue.getValue().get("totalRevenue");
															productMap["quantity"] += productValue.getValue().get("total");
															productMap["basicRevenue"] += productValue.getValue().get("basicRevenue");
															productMap["bedRevenue"] += productValue.getValue().get("bedRevenue");
															productMap["vatRevenue"] += productValue.getValue().get("vatRevenue");
															productMap["cstRevenue"] += productValue.getValue().get("cstRevenue");
															productMap["totalRevenue"] =productMap["basicRevenue"]+productMap["bedRevenue"]+productMap["vatRevenue"]+productMap["cstRevenue"] ;
															temp=FastMap.newInstance();
															temp.putAll(productMap);
															tempVariantMap[virtualProductId] = temp;
														}
														//tempVariantMap["InvoiceId"]=invoiceId;
												finalMap.put("productTotals",tempVariantMap);
												finalMap.put("invTotals",totalMap);
												//finalMap.put("ppdTotals",ppdMap);
										}
									   }
									}
								
								invoiceList = [];
								if(UtilValidate.isNotEmpty(invoiceMap[invoiceId])){
									invoiceList = invoiceMap.get(invoiceId);
								}
								invoiceList.add(finalMap);
								invoiceMap[invoiceId] = invoiceList;
							 //}
							//}
						}
					}
				}
			}
		}
		if(UtilValidate.isNotEmpty(invoiceMap)){
			tempMap = [:];
			tempMap.putAll(invoiceMap);
			for(Map.Entry entry : tempMap.entrySet()){
				invoice = delegator.findOne("Invoice", [invoiceId : entry.getKey()], false);
				shippingtemp=InvoiceWorker.getInvoiceShippingParty(invoice);
				shippingDetails.put(entry.getKey(),shippingtemp);
			}
			dayWiseInvoice.put(dayBegin,tempMap);
			//dayWisePpdMap.put(dayBegin, ppdMap);
		}
	}
	categoryCheck = "";
	categoryTotal = [:];
	if(productDetailsList){
	productDetailsList = UtilMisc.sortMaps(productDetailsList, UtilMisc.toList("productType"));
	if(productDetailsList){
	productDetailsList.each{ productDetail ->
		if(categoryCheck == productDetail.productType){
			categoryTotal[productDetail.productType] += productDetail.amount;
		}
		else{
			categoryCheck = productDetail.productType;
			categoryTotal[categoryCheck] = productDetail.amount;
		}
	}
	}
}
context.categoryTotal = categoryTotal;	
context.productDetailsList=productDetailsList;
context.shippingDetails=shippingDetails;	
context.categoryType=categoryType;
context.ppdMap=ppdMap;
context.vatAdjMap=vatAdjMap;
context.invAdjMap=invAdjMap;
context.vatMap=vatMap;
context.dayWiseInvoice=dayWiseInvoice;


