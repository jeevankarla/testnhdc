import org.ofbiz.base.util.*;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.LocalDispatcher;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.UtilMisc;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.List;
import java.text.SimpleDateFormat;
import javax.swing.text.html.parser.Entity;
import in.vasista.vbiz.byproducts.ByProductNetworkServices;
import org.ofbiz.product.product.ProductWorker;

dctx = dispatcher.getDispatchContext();
routeIdsList =[];
shipmentIds = [];
Timestamp estimatedDeliveryDateTime = null;
if(parameters.estimatedShipDate){
	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	try {
		estimatedDeliveryDateTime = new java.sql.Timestamp(formatter.parse(parameters.estimatedShipDate).getTime());
		
	} catch (ParseException e) {
	}
}
context.put("estimatedDeliveryDate", estimatedDeliveryDateTime);
productQuantityIncluded = [:];

allProductsList = ByProductNetworkServices.getAllProducts(dispatcher.getDispatchContext(), UtilMisc.toMap("salesDate",estimatedDeliveryDateTime));

allProductsList.each{ eachProd ->
	quantityInc = 1;
	if(eachProd.quantityIncluded){
		quantityInc = eachProd.quantityIncluded;
	}
	productQuantityIncluded.put(eachProd.productId, quantityInc);
	
}
context.productQuantityIncluded = productQuantityIncluded;
lmsProductsList=ProductWorker.getProductsByCategory(delegator ,"LMS" ,null);
byProductsList=  EntityUtil.filterByCondition(allProductsList, EntityCondition.makeCondition("productId",EntityOperator.NOT_IN , lmsProductsList.productId));

lmsProductsIdsList=EntityUtil.getFieldListFromEntityList(lmsProductsList, "productId", false);
byProductsIdsList=EntityUtil.getFieldListFromEntityList(byProductsList, "productId", false);

if(parameters.shipmentId){
	if(parameters.shipmentId == "allRoutes"){
		shipments = delegator.findByAnd("Shipment", [estimatedShipDate : estimatedDeliveryDateTime , shipmentTypeId : parameters.shipmentTypeId ],["routeId"]);
		shipmentIds.addAll(EntityUtil.getFieldListFromEntityList(shipments, "shipmentId", false));
		routeIdsList.addAll(EntityUtil.getFieldListFromEntityList(shipments, "routeId", false))
	}else{
		shipmentId = parameters.shipmentId;
		shipment = delegator.findOne("Shipment", [shipmentId : shipmentId], false);
		shipmentIds.add(shipmentId);
		routeIdsList.add(shipment.routeId);
	}
	
}
conditionList = [];

routeWiseMap =[:];
routeWiseTotalCrates = [:];
if(UtilValidate.isNotEmpty(routeIdsList)){
	routeIdsList.each{ routeId ->
		 Set lmsProductList =new HashSet();
		 Set byProdList =new HashSet();
		lmsProdSeqList=[];
		byProdSeqList=[];
		
		orderHeader = delegator.findList("OrderHeader", EntityCondition.makeCondition("shipmentId", EntityOperator.IN, shipmentIds), UtilMisc.toSet("originFacilityId"), null, null, false);
		boothsList = EntityUtil.getFieldListFromEntityList(orderHeader, "originFacilityId", true);
		//prepare boothsLsit
		//boothsList = (ByProductNetworkServices.getRouteBooths(delegator , routeId));
		if(UtilValidate.isNotEmpty(boothsList)){
			ownerParty = delegator.findOne("Facility", UtilMisc.toMap("facilityId", routeId), false);
			contractorName = "";
			
			
			if(UtilValidate.isNotEmpty(shipmentIds)){
				dayBegin = UtilDateTime.getDayStart(estimatedDeliveryDateTime);
				context.putAt("dayBegin", dayBegin);
				dayEnd = UtilDateTime.getDayEnd(estimatedDeliveryDateTime);
				Map boothWiseMap =[:];
				boothsList.each{ boothId ->
					dayTotals = ByProductNetworkServices.getPeriodTotals(dispatcher.getDispatchContext(), [shipmentIds:shipmentIds, facilityIds:UtilMisc.toList(boothId),fromDate:dayBegin, thruDate:dayEnd]);
					if(UtilValidate.isNotEmpty(dayTotals)){
						productTotals = dayTotals.get("productTotals");		
						Map boothWiseProd= FastMap.newInstance();						
						if(UtilValidate.isNotEmpty(productTotals)){
							//Calulating Crates
							totalQuantity =0;
							Iterator prodIter = productTotals.entrySet().iterator();
							while (prodIter.hasNext()) {
								Map.Entry entry = prodIter.next();
								itrProductId=entry.getKey();
								if(lmsProductsIdsList.contains(itrProductId)){
									qty=productTotals.get(entry.getKey()).get("total");
									totalQuantity=totalQuantity+qty;
								}
							}
							cratesTotalSub =(totalQuantity/(12));
							noPacketsexc = (totalQuantity.intValue()%12);
							amount=dayTotals.get("totalRevenue");
							boothWiseProd.put("prodDetails", productTotals);
							boothWiseProd.put("amount", amount);
							boothWiseProd.put("crates", cratesTotalSub.intValue());
							boothWiseProd.put("excess", noPacketsexc);
							
							String paymentMethodType="";
							partyProfileDafault=ByProductNetworkServices.getPartyProfileDafult(dispatcher.getDispatchContext(),[boothId:boothId,supplyDate:estimatedDeliveryDateTime]).get("partyProfileDafault");
							if(UtilValidate.isNotEmpty(partyProfileDafault)){
							paymentMethodType=partyProfileDafault.getString("defaultPayMeth");
							}
							boothWiseProd.put("paymentMode",paymentMethodType)
							boothWiseMap.put(boothId, boothWiseProd);							
						}
					
					}
					
				}
				routeTotals = ByProductNetworkServices.getPeriodTotals(dispatcher.getDispatchContext(), [shipmentIds:shipmentIds, facilityIds:boothsList,fromDate:dayBegin, thruDate:dayEnd]);
				routeAmount=0;
				
				productWiseTotalCratesMap =[:];
				routeTotQty=0;
				if(UtilValidate.isNotEmpty(routeTotals)){
					routeProdTotals = routeTotals.get("productTotals");
					routeAmount= routeTotals.get("totalRevenue");
					if(UtilValidate.isNotEmpty(routeProdTotals)){
						Iterator mapIter = routeProdTotals.entrySet().iterator();	
						while (mapIter.hasNext()) {
							Map.Entry entry = mapIter.next();
							productId=entry.getKey();
							
							
							conditionList=[];
							conditionList.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS,entry.getKey()));
							condition = EntityCondition.makeCondition(conditionList,EntityOperator.AND);
							productCategoryList = delegator.findList("ProductCategoryAndMember",condition,null,null,null,false);
							prodCategoryIds= EntityUtil.getFieldListFromEntityList(productCategoryList, "productCategoryId", true);
							
							//populating grand total crates and excess packets
							cratesDetailMap =[:];
							cratesDetailMap["prodCrates"]=0;
							cratesDetailMap["packetsExces"]=0;
							prodCategoryIds.each{ prodCategory->
								if("LMS".equals(prodCategory)){
									lmsProductList.add(entry.getKey());
									rtQty =entry.getValue().get("total");
									routeTotQty=routeTotQty+rtQty;
								}
								if("BYPROD".equals(prodCategory)){
									byProdList.add(entry.getKey());
								}
								if("CRATE_INDENT".equals(prodCategory)){
									qtyValue=entry.getValue().get("total");
									prodCrates =(qtyValue/(12)).intValue();
									packetsExces = (qtyValue.intValue()%12);
									cratesDetailMap.put("prodCrates", prodCrates);
									cratesDetailMap.put("packetsExces", packetsExces);
								}
							}					
							productWiseTotalCratesMap.put(entry.getKey(), cratesDetailMap);
						}						
					}			
					
					lmsProdSeqList = delegator.findList("Product",EntityCondition.makeCondition("productId", EntityOperator.IN, lmsProductList) , null, ["sequenceNum"], null, false);
					lmsProdIdsList= EntityUtil.getFieldListFromEntityList(lmsProdSeqList, "productId", true);
					byProdSeqList = delegator.findList("Product",EntityCondition.makeCondition("productId", EntityOperator.IN, byProdList) , null, ["sequenceNum"], null, false);
					byProdIdsList= EntityUtil.getFieldListFromEntityList(byProdSeqList, "productId", true);
					 //getting vehicle role for vehcileId
					String vehicleId="";
					vehicleRole=ByProductNetworkServices.getVehicleRole(dispatcher.getDispatchContext(),[facilityId:routeId,supplyDate:estimatedDeliveryDateTime]).get("vehicleRole");
					
					if(UtilValidate.isNotEmpty(vehicleRole)){
					vehicleId=vehicleRole.getString("vehicleId");
					}
					//route wise crates
					Debug.log("==lmsProdIdsList="+lmsProdIdsList);
					Debug.log("==byProdIdsList="+byProdIdsList);
					
					rtCrates = (routeTotQty/(12)).intValue();
					rtExcessPkts=(routeTotQty.intValue()%12);
					Map boothDetailsMap=FastMap.newInstance();
					boothDetailsMap.put("boothWiseMap", boothWiseMap);
					boothDetailsMap.put("lmsProdList", lmsProdIdsList);
					boothDetailsMap.put("byProdList", byProdIdsList);
					boothDetailsMap.put("routeWiseTotals", routeProdTotals);
					boothDetailsMap.put("routeWiseCrates", productWiseTotalCratesMap);
					boothDetailsMap.put("routeAmount", routeAmount);
					boothDetailsMap.put("contractorName", contractorName);
					boothDetailsMap.put("vehicleId", vehicleId);
					boothDetailsMap.put("rtCrates", rtCrates);
					boothDetailsMap.put("rtExcessPkts", rtExcessPkts);
					if(UtilValidate.isNotEmpty(boothWiseMap)){
						routeWiseMap.put(routeId, boothDetailsMap);
					}
					routeWiseTotalCrates.put(routeId, productWiseTotalCratesMap);
				}				
			}
		}		
	}
}
context.put("routeWiseMap",routeWiseMap);
context.putAt("routeWiseTotalCrates", routeWiseTotalCrates);
