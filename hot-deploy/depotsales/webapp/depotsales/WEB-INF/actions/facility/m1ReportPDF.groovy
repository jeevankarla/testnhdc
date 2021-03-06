import java.math.BigDecimal;
import java.util.*;
import java.sql.Timestamp;
import org.ofbiz.entity.*;
import org.ofbiz.entity.condition.*;
import org.ofbiz.entity.util.*;
import org.ofbiz.base.util.*;
import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.SortedMap;
 
import javolution.util.FastMap;
import javolution.util.FastList;
import org.ofbiz.entity.util.EntityTypeUtil;
import org.ofbiz.party.party.PartyHelper;
import in.vasista.vbiz.byproducts.ByProductNetworkServices;
import java.math.BigDecimal;
import java.math.MathContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.ServiceUtil;
import java.util.Map.Entry;

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
DateList=[];
DateMap = [:];
partyId=parameters.partyId;
DateMap.put("partyfromDate", partyfromDate);
DateMap.put("partythruDate", partythruDate);

DateList.add(DateMap);
context.DateList=DateList;

branchId = parameters.branchId;
//Debug.log("branchId=================="+branchId);
branchName = "";

branchContext=[:];
branchContext.put("branchId",branchId);
if(branchId){
	branch = delegator.findOne("PartyGroup",[partyId : branchId] , false);
	//Debug.log("branch=================="+branch);
	branchName = branch.get("groupName");
	DateMap.put("branchName", branchName);
}
//Debug.log("branchName=================="+branchName);
branchIdForAdd = "";
	branchList = [];
	
	condListb = [];
	if(branchId){
	condListb.add(EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, branchId));
	condListb.add(EntityCondition.makeCondition("roleTypeIdFrom", EntityOperator.EQUALS, "PARENT_ORGANIZATION"));
	condListb = EntityCondition.makeCondition(condListb, EntityOperator.AND);
	
	PartyRelationship = delegator.findList("PartyRelationship", condListb,UtilMisc.toSet("partyIdTo"), null, null, false);
	
	branchList=EntityUtil.getFieldListFromEntityList(PartyRelationship, "partyIdTo", true);
	
	if(!branchList){
		condListb2 = [];
		//condListb2.add(EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS,"%"));
		condListb2.add(EntityCondition.makeCondition("partyIdTo", EntityOperator.EQUALS, branchId));
		condListb2.add(EntityCondition.makeCondition("roleTypeIdFrom", EntityOperator.EQUALS, "PARENT_ORGANIZATION"));
		condListb2.add(EntityCondition.makeCondition("roleTypeIdTo", EntityOperator.EQUALS, "ORGANIZATION_UNIT"));
		cond = EntityCondition.makeCondition(condListb2, EntityOperator.AND);
		
		PartyRelationship1 = delegator.findList("PartyRelationship", cond,UtilMisc.toSet("partyIdFrom"), null, null, false);
		branchDetails = EntityUtil.getFirst(PartyRelationship1);
		branchIdForAdd=branchDetails.partyIdFrom;
	}
	else{
		branchIdForAdd=branchId;
	}
	
	//if(!branchList)
	branchList.add(branchId);
	}
	branchBasedWeaversList = [];
	condListb = [];
	if(branchList){
		
	condListb.add(EntityCondition.makeCondition("partyIdFrom", EntityOperator.IN, branchList));
	condListb.add(EntityCondition.makeCondition("roleTypeIdFrom", EntityOperator.EQUALS, "ORGANIZATION_UNIT"));
	condListb = EntityCondition.makeCondition(condListb, EntityOperator.AND);
	
	PartyRelationship = delegator.findList("PartyRelationship", condListb,UtilMisc.toSet("partyIdTo"), null, null, false);
	branchBasedWeaversList=EntityUtil.getFieldListFromEntityList(PartyRelationship, "partyIdTo", true);
	
	}
	
	//Debug.log("branchBasedWeaversList=================="+branchBasedWeaversList);
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
conditionList = [];
productIds = [];
purchaseOrderIds =[];
productCategoryIds = [];
conditionList.clear();
if(productCategory != "OTHER" && productCategory != "ALL"){
	conditionList.add(EntityCondition.makeCondition("primaryParentCategoryId", EntityOperator.EQUALS, productCategory));
	condition1 = EntityCondition.makeCondition(conditionList, EntityOperator.AND);
	ProductCategory = delegator.findList("ProductCategory", condition1,UtilMisc.toSet("productCategoryId"), null, null, false);
	productCategoryIds = EntityUtil.getFieldListFromEntityList(ProductCategory, "productCategoryId", true);
}else if(productCategory == "OTHER"){
	conditionList.add(EntityCondition.makeCondition("primaryParentCategoryId", EntityOperator.NOT_IN, ["SILK","COTTON"]));
	condition1 = EntityCondition.makeCondition(conditionList, EntityOperator.AND);
	ProductCategory = delegator.findList("ProductCategory", condition1,UtilMisc.toSet("productCategoryId"), null, null, false);
	productCategoryIds = EntityUtil.getFieldListFromEntityList(ProductCategory, "productCategoryId", true);
}else{
	conditionList.add(EntityCondition.makeCondition("productCategoryTypeId", EntityOperator.EQUALS, "NATURAL_FIBERS"));
	condition1 = EntityCondition.makeCondition(conditionList, EntityOperator.AND);
	ProductCategory = delegator.findList("ProductCategory", condition1,UtilMisc.toSet("productCategoryId"), null, null, false);
	productCategoryIds = EntityUtil.getFieldListFromEntityList(ProductCategory, "productCategoryId", true);
	ProductCategory = delegator.findList("ProductCategory", EntityCondition.makeCondition("primaryParentCategoryId", EntityOperator.IN,productCategoryIds),UtilMisc.toSet("productCategoryId"), null, null, false);
	productCategoryIds = EntityUtil.getFieldListFromEntityList(ProductCategory, "productCategoryId", true);
}
conditionList.clear();
conditionList.add(EntityCondition.makeCondition("productCategoryId", EntityOperator.IN, productCategoryIds));
produtCategorieMember = delegator.findList("ProductCategoryMember", EntityCondition.makeCondition(conditionList, EntityOperator.AND),UtilMisc.toSet("productId","productCategoryId"), null, null, false);
productIds=EntityUtil.getFieldListFromEntityList(produtCategorieMember, "productId", true);
//Debug.log("produtCategorieMember=================="+produtCategorieMember);

//Debug.log("branchBasedOrderIds=================="+branchBasedOrderIds);
conditionList.clear();  
conditionList.add(EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "SHIP_TO_CUSTOMER"));
conditionList.add(EntityCondition.makeCondition("orderTypeId", EntityOperator.EQUALS, "PURCHASE_ORDER"));
conditionList.add(EntityCondition.makeCondition("orderDate", EntityOperator.GREATER_THAN_EQUAL_TO, daystart));
conditionList.add(EntityCondition.makeCondition("orderDate", EntityOperator.LESS_THAN_EQUAL_TO, dayend));
conditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "ORDER_CANCELLED"));
conditionList.add(EntityCondition.makeCondition("productId", EntityOperator.IN, productIds));
conditionList.add(EntityCondition.makeCondition("partyId", EntityOperator.IN, branchBasedWeaversList));
orderHeaderItemAndRoles = delegator.findList("OrderHeaderItemAndRoles", EntityCondition.makeCondition(conditionList, EntityOperator.AND),UtilMisc.toSet("productId","quantity","unitPrice","itemDescription","partyId","orderId"), null, null, false);
//Debug.log("orderHeaderItemAndRoles=================="+orderHeaderItemAndRoles);

branchContextForAdd=[:];
branchContextForAdd.put("branchId",branchIdForAdd);
BOAddress="";
BOEmail="";
try{
	resultCtx = dispatcher.runSync("getBoHeader", branchContextForAdd);
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

finalCSVList=[];
totalQty=0
totalValue=0;
totalRate=0;
prodCatMap=[:];
totalsMap=[:];

stylesMap=[:];
if(branchId){
	stylesMap.put("mainHeader1", "NATIONAL HANDLOOM DEVELOPMENT CORPORATION LTD.");
	stylesMap.put("mainHeader2", BOAddress);
	stylesMap.put("mainHeader3", "M1 Report");
}
else{
	stylesMap.put("mainHeader1", "NATIONAL HANDLOOM DEVELOPMENT CORPORATION LTD.");
	stylesMap.put("mainHeader2", "M1 Report");
}
stylesMap.put("mainHeaderFontName","Arial");
stylesMap.put("mainHeadercellHeight",300);
stylesMap.put("mainHeaderFontSize",10);
stylesMap.put("mainHeadingCell",1);
stylesMap.put("mainHeaderBold",true);
stylesMap.put("columnHeaderBgColor",false);
stylesMap.put("columnHeaderFontName","Arial");
stylesMap.put("columnHeaderFontSize",10);
stylesMap.put("autoSizeCell",true);
stylesMap.put("columnHeaderCellHeight",300);
request.setAttribute("stylesMap", stylesMap);
request.setAttribute("enableStyles", true);
finalCSVList.add(stylesMap);
headerData=[:];
headerData.put("prodcatName", "Product Category");
headerData.put("productName", "Product Count");
headerData.put("partyName", "Party Name");
headerData.put("orderQty", "Order Qty(kgs)");
/*headerData.put("BdlWt", "BdlWt");*/
headerData.put("rate", "Rate");
headerData.put("orderValue", "Order Value");
finalCSVList.add(headerData);

for(productCategoryId in productCategoryIds){
	//tempCSVMap1=[:];
	prodCatList=[];
	prodCatName="";
	productCategoryDetails = delegator.findOne("ProductCategory",[productCategoryId : productCategoryId] , false);
	if(UtilValidate.isNotEmpty(productCategoryDetails)){
		prodCatName=productCategoryDetails.description
	}
	/*tempCSVMap1.put("partyName", "");
	tempCSVMap1.put("orderQty", "");
	//tempCSVMap1.put("BdlWt", "");
	tempCSVMap1.put("rate", "");
	tempCSVMap1.put("orderValue", "");
	finalCSVList.add(tempCSVMap1);*/
	singleCatProducts = EntityUtil.filterByCondition(produtCategorieMember, EntityCondition.makeCondition("productCategoryId", EntityOperator.EQUALS, productCategoryId));
	singleProductIds=EntityUtil.getFieldListFromEntityList(singleCatProducts, "productId", true);
	//singleProductIds=EntityUtil.getFieldListFromEntityList(produtCategorieMember, "productId", true);
	//Debug.log("singleProductIds=================="+singleProductIds);
	for(singleProductId in singleProductIds){
		prodMap=[:];
		tempTotMap=[:];
		//tempCSVMap2=[:];
		prodPartiesList=[];
		totOrderQty =0;
		totOrderValue =0;
		totRate=0;
		singleCatProductsOrdersDetails = EntityUtil.filterByCondition(orderHeaderItemAndRoles, EntityCondition.makeCondition("productId", EntityOperator.EQUALS, singleProductId));
		singleCatProductsOrdersDetail = EntityUtil.getFirst(singleCatProductsOrdersDetails);
		partyIds=EntityUtil.getFieldListFromEntityList(singleCatProductsOrdersDetails, "partyId", true);
		//Debug.log("singleCatProductsOrdersDetail=================="+singleCatProductsOrdersDetail);
		/*if(UtilValidate.isNotEmpty(singleCatProductsOrdersDetail)){
			tempCSVMap2.put("partyName", "");
			tempCSVMap2.put("orderQty", "");
			//tempCSVMap2.put("BdlWt", "");
			tempCSVMap2.put("rate", "");
			tempCSVMap2.put("orderValue", "");
			finalCSVList.add(tempCSVMap2);
			
		}*/
		
		for(partyId in partyIds){
			//Debug.log("partyId=================="+partyId);
			tempMap=[:];
			orderQty =0;
			orderValue =0;
			rate =0;
			unitPrice=0;
			
			singleCatProductsForEachParty = EntityUtil.filterByCondition(singleCatProductsOrdersDetails, EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId));
			//Debug.log("singleCatProductsForEachParty=================="+singleCatProductsForEachParty);
			singleorderIds=singleCatProductsForEachParty.orderId;
			for(eachItem in singleCatProductsForEachParty){
				orderQty=orderQty+eachItem.quantity;
				unitPrice= unitPrice+eachItem.unitPrice;
			}
			
			if(UtilValidate.isNotEmpty(singleCatProductsForEachParty)){
				rate=unitPrice/singleCatProductsForEachParty.size();
			}
			orderValue = orderQty*rate;
			totOrderQty=totOrderQty+orderQty;
			totRate=totRate+rate;
			totOrderValue=totOrderValue+orderValue;
			//Debug.log("partyId=================="+partyId);
			for(orderId in singleorderIds){
				//Debug.log("orderId=================="+orderId);
				exprList=[];
				exprList.add(EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId));
				exprList.add(EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "BILL_FROM_VENDOR"));
				Condition = EntityCondition.makeCondition(exprList, EntityOperator.AND);
				supplierPartyId="";
				productStoreId="";
				supplierDetails = EntityUtil.getFirst(delegator.findList("OrderRole", Condition, null,null,null, false));
				//Debug.log("supplierDetails=================="+supplierDetails);
				if(supplierDetails){
					supplierPartyId=supplierDetails.get("partyId");
					//Debug.log("supplierPartyId=================="+supplierPartyId);
				}
				supplierpartyName="";
				if(supplierPartyId){
					supplierpartyName = PartyHelper.getPartyName(delegator, supplierPartyId, false);
				}
				
				//Debug.log("supplierpartyName=================="+supplierpartyName);
				//Debug.log("orderId=================="+orderId);
		}
			String partyName = PartyHelper.getPartyName(delegator,partyId,false);
			
			//Debug.log("partyId=================="+partyId);
			//Debug.log("supplierpartyName=================="+supplierpartyName);
			//Debug.log("partyName=================="+partyName);
			tempMap.put("productName", singleCatProductsOrdersDetail.itemDescription);
			tempMap.put("prodcatName", prodCatName);
			tempMap.put("partyName", supplierpartyName);
			tempMap.put("orderQty", orderQty);
			/*tempMap.put("BdlWt", "");*/
			tempMap.put("rate", rate);
			tempMap.put("orderValue", orderValue);
			totalQty=totalQty+orderQty
			totalValue=totalValue+orderValue;
			totalRate=totalRate+rate;
			if(orderValue>0){
				prodPartiesList.add(tempMap);
				finalCSVList.add(tempMap)
			}
		}
		if(UtilValidate.isNotEmpty(singleCatProductsOrdersDetail) && UtilValidate.isNotEmpty(prodPartiesList)){
			
			
			tempTotMap.put("partyName", "SUB-TOTAL");
			tempTotMap.put("orderQty", totOrderQty);
			/*tempTotMap.put("BdlWt", "");*/
			tempTotMap.put("rate", totRate);
			tempTotMap.put("orderValue", totOrderValue);
			prodPartiesList.add(tempTotMap); 
			finalCSVList.add(tempTotMap);
			
			prodMap.put(singleCatProductsOrdersDetail.itemDescription, prodPartiesList);
			prodCatList.add(prodMap);
			
		}
	}
	if(UtilValidate.isNotEmpty(prodCatList)){
		prodCatMap.put(prodCatName, prodCatList)
	}

}
totalsMap.put("prodcatName", "TOTAL");
totalsMap.put("orderQty", totalQty);
totalsMap.put("orderValue", totalValue);
totalsMap.put("rate", totalRate);
finalCSVList.add(totalsMap);
context.totalsMap=totalsMap;
context.prodCatMap=prodCatMap;
context.finalCSVList=finalCSVList;









