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
	package in.vasista.vbiz.byproducts;
	


	import java.math.BigDecimal;
	import java.math.RoundingMode;
	import java.text.ParseException;
	import java.util.*;
	import java.util.Map.Entry;
	import java.sql.Date;
	import java.sql.Timestamp;

	import javolution.util.FastList;
	import javolution.util.FastMap;
	import javolution.util.FastSet;

	import org.apache.tools.ant.filters.TokenFilter.ContainsString;
	import org.ofbiz.entity.*;
	import org.ofbiz.entity.condition.*;
	import org.ofbiz.entity.util.*;
	import org.ofbiz.base.util.*;
	import org.ofbiz.service.DispatchContext;
	import org.ofbiz.service.GenericServiceException;
	import org.ofbiz.service.LocalDispatcher;
	import org.ofbiz.service.ModelService;
	import org.ofbiz.service.ServiceUtil;

	import java.util.List;
	import java.util.Map;
	import java.util.TimeZone;
	import java.util.Locale;
	
	import org.ofbiz.network.LmsServices;
	import org.ofbiz.product.product.ProductEvents;
	import in.vasista.vbiz.byproducts.SalesHistoryServices;
import java.text.SimpleDateFormat;

 	public class TransporterServices {
		
		public static final String module = TransporterServices.class.getName();

		
		
		public static void populateTotals(DispatchContext dctx, Map context,
				
			String salesDate, Map<String, Object> salesTotals, String shipmentTypeId  ) {

			GenericDelegator delegator = (GenericDelegator) dctx.getDelegator();
			LocalDispatcher dispatcher = dctx.getDispatcher();
			Timestamp sqlTimestamp = null;
			java.sql.Date saleDate = null;
			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			try {
				sqlTimestamp = new java.sql.Timestamp(formatter.parse(salesDate).getTime());
			} catch (ParseException e) {
			}
			saleDate = new java.sql.Date(sqlTimestamp.getTime());
			
			String productId = null;
			Object quantity = null;
			Object revenue = null;
			Object productSubscriptionTypeId = null;
			Map productsMap = FastMap.newInstance();
			List productSubscriptionTypeList = new ArrayList();
			List productSubscriptionFieldList = new ArrayList();
			
			try {
				productSubscriptionTypeList = delegator.findList("Enumeration", EntityCondition.makeCondition("enumTypeId", EntityOperator.EQUALS , "SUB_PROD_TYPE"), UtilMisc.toSet("enumId"), UtilMisc.toList("sequenceId"), null, false);
				productSubscriptionFieldList = EntityUtil.getFieldListFromEntityList(productSubscriptionTypeList, "enumId", false);
			} catch (GenericEntityException e1) {
				Debug.logError(e1, module);
				e1.printStackTrace();
			}
			HashSet productSubscriptionTypeSet = new HashSet(productSubscriptionFieldList);
			
			productsMap = (Map)salesTotals.get("productTotals");
				Iterator mapIterator = productsMap.entrySet().iterator();
				while (mapIterator.hasNext()) {
					Map.Entry entry = (Entry) mapIterator.next();
					List productsSalesList = new ArrayList();
					productsSalesList.clear();
					productsSalesList.add(entry.getValue());
					Map productsSalesMap = FastMap.newInstance();
					productsSalesMap.clear();
					productsSalesMap = (Map) productsSalesList.get(0);
					productId = (String) entry.getKey();
					Map tempMap = FastMap.newInstance();
					tempMap = (Map) productsSalesMap.get("supplyTypeTotals");
					
					Iterator supplyTypeIter = tempMap.entrySet().iterator(); 
					while (supplyTypeIter.hasNext()) {
						Map.Entry entry2 = (Entry) supplyTypeIter.next();
						Map supplyTypeMap = FastMap.newInstance();
						supplyTypeMap = (Map) (entry2.getValue());
						productSubscriptionTypeId = supplyTypeMap.get("name");
						quantity = supplyTypeMap.get("total");
						revenue = supplyTypeMap.get("totalRevenue");
						
						    try {
					    		GenericValue salesSummary = delegator.findOne("LMSSalesHistorySummaryDetail", UtilMisc.toMap("salesDate", saleDate, "shipmentTypeId", shipmentTypeId, "productSubscriptionTypeId",productSubscriptionTypeId, "productId", productId), false);
					    		if (salesSummary == null) {
					    			salesSummary = delegator.makeValue("LMSSalesHistorySummaryDetail");
					                salesSummary.put("salesDate", saleDate );
					                salesSummary.put("totalQuantity", quantity);
					                salesSummary.put("totalRevenue", revenue); 
					                salesSummary.put("productId", productId);
					                salesSummary.put("productSubscriptionTypeId", productSubscriptionTypeId);
					                salesSummary.put("shipmentTypeId", shipmentTypeId);
					                salesSummary.create();    
					            }
					    		else {  
					    			salesSummary.clear();
				    			    List conditionList= FastList.newInstance(); 
				    	        	conditionList.add(EntityCondition.makeCondition("salesDate", EntityOperator.EQUALS, saleDate));
				    	        	conditionList.add(EntityCondition.makeCondition("shipmentTypeId", EntityOperator.EQUALS, shipmentTypeId));    	
				    	        	EntityCondition condition = EntityCondition.makeCondition(conditionList,EntityOperator.AND);
				    	        	
				    				List<GenericValue> salesSummaryList = delegator.findList("LMSSalesHistorySummaryDetail", condition, null, null, null, false);
				                    delegator.removeAll(salesSummaryList);
				                	salesSummary = delegator.makeValue("LMSSalesHistorySummaryDetail");
				    			    salesSummary.put("salesDate", saleDate);
				                    salesSummary.put("totalQuantity", quantity);
				                    salesSummary.put("totalRevenue", revenue); 
				                    salesSummary.put("productId", productId);
				                    salesSummary.put("productSubscriptionTypeId", productSubscriptionTypeId); 
				                    salesSummary.put("shipmentTypeId", shipmentTypeId);
				                    salesSummary.create();
					            }
					    	} catch (GenericEntityException e) {
					            Debug.logError(e, module);
					        }
						}
				}
		}
		
		 
		public static Map<String, Object> generateTranporterMargin(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericServiceException {
			Delegator delegator = dctx.getDelegator();
			LocalDispatcher dispatcher = dctx.getDispatcher();
			Map<String, Object> result = ServiceUtil.returnSuccess();
			TimeZone timeZone = TimeZone.getDefault();
			List masterList = FastList.newInstance();
			Locale locale = Locale.getDefault();
			List conditionList= FastList.newInstance(); 
			GenericValue userLogin = (GenericValue) context.get("userLogin");
			String customTimePeriodId = (String) context.get("customTimePeriodId");
			String periodBillingId = (String) context.get("periodBillingId");
			String billingTypeId = (String) context.get("billingTypeId");
			RoundingMode rounding = RoundingMode.HALF_UP;
			Map resultValue = ServiceUtil.returnSuccess();
			boolean generationFailed = false;		
			GenericValue tenantConfiguration = null;
			
			GenericValue periodBilling = null;
			try{
				try {
					periodBilling =delegator.findOne("PeriodBilling", UtilMisc.toMap("periodBillingId", periodBillingId), false);
				} catch (GenericEntityException e1) {
					Debug.logError(e1,"Error While Finding PeriodBilling");
					return ServiceUtil.returnError("Error While Finding PeriodBilling" + e1);
				}
				try{        
			    	tenantConfiguration = delegator.findOne("TenantConfiguration",UtilMisc.toMap("propertyName", "enableTranporterDuePayment", "propertyTypeEnumId", "LMS"),false);
			    	tenantConfiguration.set("propertyValue", "N");
			    	tenantConfiguration.set("lastUpdatedStamp", UtilDateTime.nowTimestamp());
			    	tenantConfiguration.store();   
			    }catch(GenericEntityException e){
			    	Debug.logError("Unable to set tenant configuration  record in database"+e, module);
			    	periodBilling.set("statusId", "GENERATION_FAIL");
					periodBilling.store();
					return ServiceUtil.returnError("Unable to set tenant configuration  record in database "); 
			    }
				GenericValue customTimePeriod;
				try {
					customTimePeriod = delegator.findOne("CustomTimePeriod",UtilMisc.toMap("customTimePeriodId", customTimePeriodId), false);
				} catch (GenericEntityException e1) {
					Debug.logError(e1,"Error While Finding Customtime Period");
			    	periodBilling.set("statusId", "GENERATION_FAIL");
					periodBilling.store();
					return ServiceUtil.returnError("Error While Finding Customtime Period" + e1);
				}
				if(customTimePeriod == null){
					generationFailed = true;
				}
				Map<String, Object> updateRecvoryRes=updateFineRecvoryWithBilling(dctx , UtilMisc.toMap("customTimePeriodId", customTimePeriodId,"periodBillingId",periodBillingId,"userLogin",userLogin));
				if (ServiceUtil.isError(updateRecvoryRes)) {
		    		generationFailed = true;
	                Debug.logWarning("There was an error while populating updateRecvory: " + ServiceUtil.getErrorMessage(updateRecvoryRes), module);
	        		return ServiceUtil.returnError("There was an error while populating updateRecvory: " + ServiceUtil.getErrorMessage(updateRecvoryRes));          	            
	            } 
				Timestamp fromDateTime=UtilDateTime.toTimestamp(customTimePeriod.getDate("fromDate"));
				Timestamp thruDateTime=UtilDateTime.toTimestamp(customTimePeriod.getDate("thruDate"));
				
				Timestamp monthBegin = UtilDateTime.getDayStart(fromDateTime, timeZone, locale);
				Timestamp monthEnd = UtilDateTime.getDayEnd(thruDateTime, timeZone, locale);
				
				int totalDays=UtilDateTime.getIntervalInDays(monthBegin,monthEnd);	
				Map routes = ByProductNetworkServices.getRoutes(dctx , UtilMisc.toMap("facilityTypeId", "ROUTE"));		
				List routesList = (List) routes.get("routesList");
				Map transporterMap =FastMap.newInstance();
				Map<String, Object> populateTripRes=populateVehicleTripParty(dctx , UtilMisc.toMap("fromDate", monthBegin,"thruDate",monthEnd,"userLogin",userLogin));
				if (ServiceUtil.isError(populateTripRes)) {
		    		generationFailed = true;
	                Debug.logWarning("There was an error while populating vehicleTripUpdate: " + ServiceUtil.getErrorMessage(populateTripRes), module);
	        		return ServiceUtil.returnError("There was an error while populating vehicleTripUpdate: " + ServiceUtil.getErrorMessage(populateTripRes));          	            
	            } 
				Debug.log("=====populateTripRes====AfterServiceRun==="+populateTripRes);
				try {
					for (int i = 0; i < routesList.size(); i++) {
						String route = (String) routesList.get(i);
						List boothsList=ByProductNetworkServices.getRouteBooths(delegator ,route);//getting list of Booths
						
						GenericValue facilityDetail = delegator.findOne("Facility",UtilMisc.toMap("facilityId", route) ,false);
						String isUpCountry = facilityDetail.getString("isUpcountry");
						BigDecimal facilitySize=new BigDecimal(1);
						
						/*if(UtilValidate.isNotEmpty(facilityDetail.getString("facilitySize"))){
							facilitySize=(BigDecimal)facilityDetail.getBigDecimal("facilitySize");
						}*/
						if(UtilValidate.isEmpty(isUpCountry)){
							isUpCountry="N";
						}
						BigDecimal narmalMargin = BigDecimal.ZERO;
						 BigDecimal totalPeriodComission = BigDecimal.ZERO;
						 //to get normalMargin for each date
						 Map inputRateAmt = UtilMisc.toMap("userLogin", userLogin);
							inputRateAmt.put("rateCurrencyUomId", context.get("currencyUomId"));
							inputRateAmt.put("facilityId", facilityDetail.get("facilityId"));
							inputRateAmt.put("fromDate",monthBegin );
							inputRateAmt.put("rateTypeId", "TRANSPORTER_MRGN");
							Map<String, Object> facilityRateResult = dispatcher.runSync("getFacilityRateAmount", inputRateAmt);
							
							if (ServiceUtil.isError(facilityRateResult)) {
				    			generationFailed = true;
				    			Debug.logWarning("There was an error while creating  the TransporterCommission: " + ServiceUtil.getErrorMessage(result), module);
				        		return ServiceUtil.returnError("There was an error while creating the TransporterCommission: " + ServiceUtil.getErrorMessage(result));          	            
				            }
							String uomId="";
							BigDecimal monthBeginMargin = BigDecimal.ZERO;
				    		if(UtilValidate.isNotEmpty(facilityRateResult)){
				    			monthBeginMargin = (BigDecimal) facilityRateResult.get("rateAmount");
				    			 uomId=(String)facilityRateResult.get("uomId");
				    		}		
				    		inputRateAmt.put("fromDate",monthEnd );
	                        facilityRateResult = dispatcher.runSync("getFacilityRateAmount", inputRateAmt);
							if (ServiceUtil.isError(facilityRateResult)) {
				    			generationFailed = true;
				    			Debug.logWarning("There was an error while creating  the TransporterCommission: " + ServiceUtil.getErrorMessage(result), module);
				        		return ServiceUtil.returnError("There was an error while creating the TransporterCommission: " + ServiceUtil.getErrorMessage(result));          	            
				            }
							
							BigDecimal monthEndMargin = BigDecimal.ZERO;
				    		if(UtilValidate.isNotEmpty(facilityRateResult)){
				    			monthEndMargin = (BigDecimal) facilityRateResult.get("rateAmount");
				    			 uomId=(String)facilityRateResult.get("uomId");
				    		}	
										
			    		BigDecimal dayTotQty = BigDecimal.ZERO;
			    		BigDecimal tripSize = BigDecimal.ZERO;
						
						Map dayTotalsMap = FastMap.newInstance();
						Map dayWiseTotalsMap = FastMap.newInstance();
						//getting
					/*	Map dayTotals = ByProductNetworkServices.getPeriodTotals(dispatcher.getDispatchContext(), UtilMisc.toMap("facilityIds",boothsList,"fromDate",monthBegin, "thruDate",monthEnd));
						dayWiseTotalsMap=(Map)dayTotals.get("dayWiseTotals");*/
						//input map for FacilitySize
						 Map inputDistMap = UtilMisc.toMap("userLogin", userLogin);
						 inputDistMap.put("rateCurrencyUomId", "LEN_km");
						 inputDistMap.put("facilityId", facilityDetail.get("facilityId"));
						 inputDistMap.put("rateTypeId", "FACILITY_SIZE");
							
						Timestamp supplyDate = monthBegin;
				        for (int k = 0; k <= (totalDays); k++) {
							Map routeMarginMap = FastMap.newInstance();
							routeMarginMap.put("partyId","");
							routeMarginMap.put("quantity",BigDecimal.ZERO );
							routeMarginMap.put("saleAmount", BigDecimal.ZERO);
							routeMarginMap.put("cashQty",BigDecimal.ZERO );
							routeMarginMap.put("cashAmount", BigDecimal.ZERO);
							routeMarginMap.put("commision", BigDecimal.ZERO);
							
							supplyDate = UtilDateTime.addDaysToTimestamp(monthBegin, k);
							
							//List shipmentIds =ByProductNetworkServices.getShipmentIds(delegator , UtilDateTime.toDateString(supplyDate, "yyyy-MM-dd HH:mm:ss"),null,route);	//route Specific shipments.
							List shipmentIds = ByProductNetworkServices.getByProdShipmentIds(delegator, supplyDate, supplyDate,UtilMisc.toList(route));//this will give Today AM+PM as SALE
							conditionList.clear();
							conditionList.add(EntityCondition.makeCondition("shipmentId", EntityOperator.IN, shipmentIds));
							conditionList.add(EntityCondition.makeCondition("originFacilityId", EntityOperator.EQUALS, route));
							EntityCondition vhCondition = EntityCondition.makeCondition(conditionList, EntityOperator.AND);
							List<GenericValue> vehicleTrpList = delegator.findList("VehicleTrip", vhCondition, null, UtilMisc.toList("originFacilityId"), null, false);
							 GenericValue vehicleTrip=EntityUtil.getFirst(vehicleTrpList);
							//List shipmentIds =ByProductNetworkServices.getShipmentIds(delegator,supplyDate,supplyDate);
							 String curntDay=UtilDateTime.toDateString(supplyDate ,"yyyy-MM-dd");
							  narmalMargin = monthBeginMargin;
				    			//normal Margin for Each Day if monthStart and MonthEnd is Different
				    			if(monthBeginMargin.compareTo(monthEndMargin) != 0){
				    				inputRateAmt.put("fromDate",supplyDate );
				    				facilityRateResult = dispatcher.runSync("getFacilityRateAmount", inputRateAmt);
						    	    if (ServiceUtil.isError(facilityRateResult)) {
						    			generationFailed = true;
						    			Debug.logWarning("There was an error while creating  the TransporterCommission: " + ServiceUtil.getErrorMessage(result), module);
						        		return ServiceUtil.returnError("There was an error while creating the TransporterCommission: " + ServiceUtil.getErrorMessage(result));          	            
						            }
						    	    if(UtilValidate.isNotEmpty(facilityRateResult)){
						    	    	narmalMargin = (BigDecimal) facilityRateResult.get("rateAmount");
						    			 uomId=(String)facilityRateResult.get("uomId");
						    		}	
				    			}
							 if(UtilValidate.isNotEmpty(shipmentIds)){
								 tripSize=new BigDecimal(shipmentIds.size());
							 }
							 if(UtilValidate.isNotEmpty(vehicleTrpList)){
								 tripSize=new BigDecimal(vehicleTrpList.size());
								 routeMarginMap.put("partyId",vehicleTrip.getString("partyId"));
							 }
							 if(UtilValidate.isEmpty(shipmentIds)){//filled with zero
								 tripSize=new BigDecimal(shipmentIds.size());
								}
							 BigDecimal actualCommision = tripSize.multiply(narmalMargin);
	                         if(uomId.equals("LEN_km")){
	                        	 inputDistMap.put("fromDate",supplyDate );
	                        	 Map<String, Object> facilitySizeResult = dispatcher.runSync("getRouteDistance", inputDistMap);
						    	    if (ServiceUtil.isError(facilitySizeResult)) {
						    			generationFailed = true;
						    			Debug.logWarning("There was an error while getting FacilitySize !: " + ServiceUtil.getErrorMessage(facilitySizeResult), module);
						        		return ServiceUtil.returnError("There was an error while getting FacilitySize !: " + ServiceUtil.getErrorMessage(facilitySizeResult));          	            
						            }
						    	    if(UtilValidate.isNotEmpty(facilitySizeResult)){
						    	    	 BigDecimal tempFacilitySize=(BigDecimal) facilitySizeResult.get("facilitySize");
										   if (tempFacilitySize.compareTo(BigDecimal.ZERO) > 0){
								   			facilitySize = (BigDecimal) facilitySizeResult.get("facilitySize");
								   		}	
						    		}	
	                      	   actualCommision = actualCommision.multiply(facilitySize);
				    		}
	                         /*if(uomId.equals("PER_LTR")){
	                        	   actualCommision = actualCommision.multiply(facilitySize);
	  			    		}*/
							Timestamp lastDay = UtilDateTime.getDayEnd(supplyDate, timeZone, locale);
							routeMarginMap.put("dueAmount", BigDecimal.ZERO);
							routeMarginMap.put("commision",actualCommision);
							totalPeriodComission=totalPeriodComission.add(actualCommision);
							int Days=UtilDateTime.getIntervalInDays(lastDay,monthEnd);
							if(Days==0){						
								Map transporterDuesMap = LmsServices.getTransporterDues(dctx, UtilMisc.toMap("userLogin", userLogin, "fromDate", supplyDate, "thruDate", supplyDate, "facilityId", route));
								BigDecimal transporterDueAmt = BigDecimal.ZERO;
								if(UtilValidate.isNotEmpty(transporterDuesMap)){
									transporterDueAmt =(BigDecimal) transporterDuesMap.get("invoicesTotalAmount");
								}						
								routeMarginMap.put("dueAmount", transporterDueAmt);
							}
							Map tempMap = FastMap.newInstance();
							tempMap.putAll(routeMarginMap);
							dayTotalsMap.put(supplyDate, tempMap);					
						}
				       
						 if(totalPeriodComission.compareTo(BigDecimal.ZERO) !=0){//for whole period if commission is zero dont add
								transporterMap.put(route, dayTotalsMap);
						   }
					}
					masterList.add(transporterMap);
					result =SalesHistoryServices.populateFacilityCommissiions(dctx, context, masterList,periodBillingId);	
					if (ServiceUtil.isError(result)) {
			    		generationFailed = true;
		                Debug.logWarning("There was an error while creating  the TransporterCommission: " + ServiceUtil.getErrorMessage(result), module);
		        		return ServiceUtil.returnError("There was an error while creating the TransporterCommission: " + ServiceUtil.getErrorMessage(result));          	            
		            } 
					if (generationFailed) {
						periodBilling.set("statusId", "GENERATION_FAIL");
					} 
					periodBilling.store();	
				}catch (GenericEntityException e) {
					Debug.logError(e, module);
					
				}
				if (generationFailed) {
					periodBilling.set("statusId", "GENERATION_FAIL");
				} else {
					periodBilling.set("statusId", "GENERATED");
					periodBilling.set("lastModifiedDate", UtilDateTime.nowTimestamp());
				}
				periodBilling.store();	
				
		}catch (GenericEntityException e) {
				Debug.logError(e, module);
		}
	return resultValue;		
	}
			
			public static Map<String, Object> createDTCInvoice (DispatchContext dctx, Map<String, ? extends Object> context) {
		    	Delegator delegator = dctx.getDelegator();
		    	TimeZone timeZone = TimeZone.getDefault();
		        LocalDispatcher dispatcher = dctx.getDispatcher();       
		        GenericValue userLogin = (GenericValue) context.get("userLogin");
		    	Locale locale = (Locale) context.get("locale");
		        Map<String, Object> result = new HashMap<String, Object>();
		        String periodBillingId = (String) context.get("periodBillingId");
		        GenericValue periodBilling = null;
		        String customTimePeriodId="";
		        try{
		        	periodBilling = delegator.findOne("PeriodBilling",UtilMisc.toMap("periodBillingId", periodBillingId), false);
		        	 customTimePeriodId =  periodBilling.getString("customTimePeriodId");
		        }catch (GenericEntityException e) {
		    		Debug.logError("Unable to get PeriodBilling record from DataBase"+e, module);
		    		return ServiceUtil.returnError("Unable to get PeriodBilling record from DataBase "); 
				}   
		        GenericValue customTimePeriod;
				try {
					customTimePeriod = delegator.findOne("CustomTimePeriod",UtilMisc.toMap("customTimePeriodId", customTimePeriodId), false);
				} catch (GenericEntityException e1) {
					Debug.logError(e1,"Error While Finding Customtime Period");
			    /*	periodBilling.set("statusId", "GENERATION_FAIL");
					periodBilling.store();*/
					return ServiceUtil.returnError("Error While Finding Customtime Period" + e1);
				}
				
				Timestamp fromDateTime=UtilDateTime.toTimestamp(customTimePeriod.getDate("fromDate"));
				Timestamp thruDateTime=UtilDateTime.toTimestamp(customTimePeriod.getDate("thruDate"));
				
				Timestamp monthBegin = UtilDateTime.getDayStart(fromDateTime, timeZone, locale);
				Timestamp monthEnd = UtilDateTime.getDayEnd(thruDateTime, timeZone, locale);
			Map facilityRecoveryResultMap =(Map) getFacilityRecvoryForPeriodBilling(dctx,UtilMisc.toMap("periodBillingId",periodBillingId,"fromDate",monthBegin,"userLogin",userLogin));
			Map partyWiseRecvoryMap=(Map)facilityRecoveryResultMap.get("partyRecoveryInfoMap");
			Map transporterTradeResultMap=getTransporterTotalsForPeriodBilling(dctx, UtilMisc.toMap("periodBillingId",(Object)periodBillingId));
			//Map totalRouteTradingTotalMap=(Map)transporterTradeResultMap.get("routeTradingMap");
			//Map totalPartyTradingTotalMap=(Map)transporterTradeResultMap.get("partyTradingMap");
			Map totalPartyTradingTotalMap=(Map)transporterTradeResultMap.get("roundedPartyTradingMap");
			
			
			if(UtilValidate.isNotEmpty(totalPartyTradingTotalMap)){
			Iterator partyMarginsIter = totalPartyTradingTotalMap.entrySet().iterator();
			try{
			while (partyMarginsIter.hasNext()) {
				Map.Entry partyEntry = (Entry) partyMarginsIter.next();	
				String invoiceId="";
				//String facilityId = (String) routeEntry.getKey();	
				
				String partyIdTo = (String) partyEntry.getKey();	
				Map partyWiseValues = (Map) partyEntry.getValue();
				BigDecimal totalMargin = (BigDecimal) partyWiseValues.get("totalMargin");
				BigDecimal quantity = BigDecimal.ONE;
				BigDecimal totalFine = BigDecimal.ZERO;
				BigDecimal totalHikeAmount = BigDecimal.ZERO;
				BigDecimal cratesFine = BigDecimal.ZERO;
				
				if(UtilValidate.isNotEmpty(partyWiseRecvoryMap) && UtilValidate.isNotEmpty(partyWiseRecvoryMap.get(partyIdTo))){
					Map partyFinesMap=(Map)partyWiseRecvoryMap.get(partyIdTo);
					totalFine=(BigDecimal)partyFinesMap.get("totalFine");
					//get CratesFine separately
					cratesFine=(BigDecimal)partyFinesMap.get("cratesFine");
					totalFine=totalFine.subtract(cratesFine);
					
					totalHikeAmount=(BigDecimal)partyFinesMap.get("totalHikeAmount");
				}
				Debug.log("===partyIdTo==="+partyIdTo+"=====Fine="+totalFine+"===Commission="+totalMargin+"==totalHikeAmount="+totalHikeAmount+"==cratesFine="+cratesFine);
				/*GenericValue facilityDetail = delegator.findOne("Facility",UtilMisc.toMap("facilityId", facilityId) ,false);
				String partyIdTo = facilityDetail.getString("ownerPartyId");*/
				// heree Invoice Raised by Transporter
				 if (UtilValidate.isEmpty(invoiceId)&& UtilValidate.isNotEmpty(partyIdTo)) {
		                Map<String, Object> createInvoiceContext = FastMap.newInstance();
		                createInvoiceContext.put("partyId", "Company");
		                createInvoiceContext.put("partyIdFrom", partyIdTo);
		                //createInvoiceContext.put("billingAccountId", billingAccountId);
		               // createInvoiceContext.put("invoiceDate", UtilDateTime.nowTimestamp());
		                createInvoiceContext.put("dueDate", monthEnd);
		                createInvoiceContext.put("invoiceDate", monthEnd);
		                //createInvoiceContext.put("invoiceTypeId", "TRANSPORTER_OUT");
		                createInvoiceContext.put("invoiceTypeId", "SALES_DIS_OUT");
		                createInvoiceContext.put("referenceNumber", "TRSPT_MRGN_"+periodBillingId);
		                // start with INVOICE_IN_PROCESS, in the INVOICE_READY we can't change the invoice (or shouldn't be able to...)
		                createInvoiceContext.put("statusId", "INVOICE_IN_PROCESS");
		                createInvoiceContext.put("currencyUomId", "INR");
		                createInvoiceContext.put("userLogin", userLogin);

		                // store the invoice first
		                Map<String, Object> createInvoiceResult = dispatcher.runSync("createInvoice", createInvoiceContext);
		                	if (ServiceUtil.isError(result)) {
		    		    		//generationFailed = true;
		    	                Debug.logWarning("There was an error while creating  Invoice For TransporterCommission: " + ServiceUtil.getErrorMessage(result), module);
		    	        		return ServiceUtil.returnError("There was an error while creating Invoice for  TransporterCommission: " + ServiceUtil.getErrorMessage(result));          	            
		    	            } 
		    				/*if (generationFailed) {
		    					periodBilling.set("statusId", "GENERATION_FAIL");
		    					periodBilling.store();	
		    				} */
		                // call service for creation invoice);
		                invoiceId = (String) createInvoiceResult.get("invoiceId");
		                Map<String, Object> resMap = null;
		                Map<String, Object> invoiceItemAssocResultMap = null;
		                    resMap = dispatcher.runSync("createInvoiceItem", UtilMisc.toMap("invoiceId", invoiceId,"invoiceItemTypeId", "DTC_ROUTE","quantity",quantity,"amount", totalMargin,"userLogin", userLogin));
		                    if (ServiceUtil.isError(result)) {
		                    	//generationFailed = true;
		    	                Debug.logWarning("There was an error while creating  the InvoiceItem: " + ServiceUtil.getErrorMessage(result), module);
		                    }
		                    //Fines Item
		                    if(totalFine.compareTo(BigDecimal.ZERO) !=0){//if fine is zero dont add
		                    	 resMap = dispatcher.runSync("createInvoiceItem", UtilMisc.toMap("invoiceId", invoiceId,"invoiceItemTypeId", "DTC_RECOVERY_ITEM","quantity",quantity,"amount", totalFine.negate(),"userLogin", userLogin));
				                    if (ServiceUtil.isError(result)) {
				                    	//generationFailed = true;
				    	                Debug.logWarning("There was an error while creating  the InvoiceItem For Recovery: " + ServiceUtil.getErrorMessage(result), module);
				                    }
						   }
		                  //separate Item for CrateFines ItemType "INCOME_ITEM62"
		                    if(cratesFine.compareTo(BigDecimal.ZERO) !=0){//if fine is zero dont add
		                    	 resMap = dispatcher.runSync("createInvoiceItem", UtilMisc.toMap("invoiceId", invoiceId,"invoiceItemTypeId", "INCOME_ITEM62","quantity",quantity,"amount", cratesFine.negate(),"userLogin", userLogin));
				                    if (ServiceUtil.isError(result)) {
				                    	//generationFailed = true;
				    	                Debug.logWarning("There was an error while creating  the InvoiceItem For CrateRecovery: " + ServiceUtil.getErrorMessage(result), module);
				                    }
						   }
		                   //for Hikes inserting New item for Same invoice
		                    if(totalHikeAmount.compareTo(BigDecimal.ZERO) !=0){//if fine is zero dont add
		                    	 resMap = dispatcher.runSync("createInvoiceItem", UtilMisc.toMap("invoiceId", invoiceId,"invoiceItemTypeId", "DTC_HIKE_ITEM","quantity",quantity,"amount", totalHikeAmount,"userLogin", userLogin));
				                    if (ServiceUtil.isError(result)) {
				                    	//generationFailed = true;
				    	                Debug.logWarning("There was an error while creating  InvoiceItem For Hikes: " + ServiceUtil.getErrorMessage(result), module);
				                    }
						   }
		                    
		                    //for TDS Commission.
		                   /* BigDecimal tdsMargin= totalMargin.divide(new BigDecimal(10));//for tax
		                    resMap = dispatcher.runSync("createInvoiceItem", UtilMisc.toMap("invoiceId", invoiceId,"invoiceItemTypeId", "TDS_194H","quantity",quantity,"amount", tdsMargin,"userLogin", userLogin));
		                    if (ServiceUtil.isError(result)) {
		                    	generationFailed = true;
		    	                Debug.logWarning("There was an error while creating  the TDS InvoiceItem: " + ServiceUtil.getErrorMessage(result), module);
		                    }*/
		                    Map<String, Object> invoiceCtx = UtilMisc.<String, Object>toMap("invoiceId", invoiceId);
		    	             invoiceCtx.put("userLogin", userLogin);
		    	             invoiceCtx.put("statusId","INVOICE_APPROVED");
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
		    	             //set to Ready for Posting
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
				
			}//end of while
			}catch(GenericServiceException e){
            	 Debug.logError(e, e.toString(), module);
                return ServiceUtil.returnError(e.toString());
			}
		}//end of if
			return result;
		}
		
		public static Map<String, Object> cancelTransporterMarginReport(DispatchContext dctx, Map<String, ? extends Object> context) {
	    	Delegator delegator = dctx.getDelegator();
	        LocalDispatcher dispatcher = dctx.getDispatcher();       
	        GenericValue userLogin = (GenericValue) context.get("userLogin");
	    	Locale locale = (Locale) context.get("locale");
	        //Map<String, Object> result = ServiceUtil.returnSuccess();
	        
	        Map<String, Object> result = new HashMap<String, Object>();
	        
	        String periodBillingId = (String) context.get("periodBillingId");
	        GenericValue periodBilling = null;
	        String customTimePeriodId="";
	        try{
	        	periodBilling = delegator.findOne("PeriodBilling",UtilMisc.toMap("periodBillingId", periodBillingId), false);
	        	 customTimePeriodId =  periodBilling.getString("customTimePeriodId");
	        }catch (GenericEntityException e) {
	    		Debug.logError("Unable to get PeriodBilling record from DataBase"+e, module);
	    		return ServiceUtil.returnError("Unable to get PeriodBilling record from DataBase "); 
			}        
	    	periodBilling.set("statusId", "CANCEL_INPROCESS");
	        boolean cancelationFailed = false;	
	    	try{
	    		periodBilling.store();    		
	    	}catch (Exception e) {
	    		Debug.logError("Unable to Store PeriodBilling Status"+e, module);
	    		return ServiceUtil.returnError("Unable to Store PeriodBilling Status"); 
			}
	    	//first Cancel DTC payments
    		Map dtcPaymentCancelResult=cancelTransporterPayment(dctx, UtilMisc.toMap("periodBillingId",periodBillingId ,"userLogin",userLogin));
    		if (ServiceUtil.isError(dtcPaymentCancelResult)) {
    			cancelationFailed = true;
    			periodBilling.set("statusId", "CANCEL_FAILED");
    			try{
    	    		periodBilling.store();    		
    	    	}catch (Exception e) {
    	    		Debug.logError("Unable to Store PeriodBilling Status"+e, module);
    	    		return ServiceUtil.returnError("Unable to Store PeriodBilling Status"); 
    			}
                Debug.logWarning("There was an error while Canceling Payment: " + ServiceUtil.getErrorMessage(dtcPaymentCancelResult), module);
        		return ServiceUtil.returnError("There was an error while Canceling Payment: " + ServiceUtil.getErrorMessage(dtcPaymentCancelResult));          	            
            } 
	           
	    	try{
	    		Map<String,  Object> inputContext = UtilMisc.<String, Object>toMap("periodBillingId", periodBillingId,"userLogin", userLogin);
				dispatcher.runAsync("cancelTransporterMarginInvoice", inputContext);
	    	} catch (GenericServiceException e) {
	            Debug.logError(e, "Error in canceling 'transporterMargin' service", module);
	            return ServiceUtil.returnError(e.getMessage());
	        } 
	    	
	        return result;
	    }
		   
		 public static Map<String, Object> cancelTransporterMarginInvoice(DispatchContext dctx, Map<String, ? extends Object> context) {
		    	Delegator delegator = dctx.getDelegator();
		        LocalDispatcher dispatcher = dctx.getDispatcher();       
		        GenericValue userLogin = (GenericValue) context.get("userLogin");
		        Map<String, Object> result = new HashMap<String, Object>();
		        Locale locale = (Locale) context.get("locale");
		        boolean cancelationFailed = false;	
		        String periodBillingId = (String) context.get("periodBillingId");
		        GenericValue periodBilling = null;
		        String customTimePeriodId="";
		        try{
		        	periodBilling = delegator.findOne("PeriodBilling",UtilMisc.toMap("periodBillingId", periodBillingId), false);
		        	 customTimePeriodId =  periodBilling.getString("customTimePeriodId");
		        }catch (GenericEntityException e) {
		    		Debug.logError("Unable to get PeriodBilling record from DataBase"+e, module);
		    		return ServiceUtil.returnError("Unable to get PeriodBilling record from DataBase "); 
				}        
		    	GenericValue customTimePeriod;
				try {
					customTimePeriod = delegator.findOne("CustomTimePeriod",UtilMisc.toMap("customTimePeriodId", customTimePeriodId), false);
				} catch (GenericEntityException e1) {
					Debug.logError(e1, e1.getMessage());
					return ServiceUtil.returnError("Error in customTimePeriod" + e1);
				}
				Timestamp fromDateTime=UtilDateTime.toTimestamp(customTimePeriod.getDate("fromDate"));
				Timestamp thruDateTime=UtilDateTime.toTimestamp(customTimePeriod.getDate("thruDate"));
				
				Timestamp monthBegin = UtilDateTime.getDayStart(fromDateTime, TimeZone.getDefault(), locale);
				Timestamp monthEnd = UtilDateTime.getDayEnd(thruDateTime, TimeZone.getDefault(), locale);
				
				if(customTimePeriod == null){
					cancelationFailed = true;
				}
				
			try{
				List conditionList = UtilMisc.toList(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, "Company"));
				  conditionList.add(EntityCondition.makeCondition("referenceNumber", EntityOperator.EQUALS,  "TRSPT_MRGN_"+periodBillingId));
				  
			     conditionList.add(EntityCondition.makeCondition("invoiceTypeId", EntityOperator.EQUALS, "SALES_DIS_OUT"));
			    /* conditionList.add(EntityCondition.makeCondition("dueDate", EntityOperator.GREATER_THAN_EQUAL_TO ,monthBegin));
			     conditionList.add(EntityCondition.makeCondition("dueDate", EntityOperator.LESS_THAN_EQUAL_TO ,monthEnd));*/
	             conditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "INVOICE_CANCELLED")); 
	            
	        	EntityCondition condition = EntityCondition.makeCondition(conditionList, EntityOperator.AND);      	
	        	List<GenericValue> invoiceRows = delegator.findList("Invoice", condition, null, null, null, false);
		        List invoiceIdsList = EntityUtil.getFieldListFromEntityList(invoiceRows, "invoiceId", false);
		        Debug.log("==invoiceIdsList======Before==Cacnelation==="+invoiceIdsList);
		        	//cancel mass invoice
		        	Map<String, Object> cancelInvoiceInput = UtilMisc.<String, Object>toMap("invoiceIds",invoiceIdsList);
	         	    cancelInvoiceInput.put("userLogin", userLogin);
	                cancelInvoiceInput.put("statusId", "INVOICE_CANCELLED");
	             	Map<String, Object> invoiceResult = dispatcher.runSync("massChangeInvoiceStatus",cancelInvoiceInput);
	             	if (ServiceUtil.isError(invoiceResult)) {
	             		Debug.logError("There was an error while cancel Invoice: " + ServiceUtil.getErrorMessage(invoiceResult), module);
	             		  try{
	          	        	periodBilling = delegator.findOne("PeriodBilling",UtilMisc.toMap("periodBillingId", periodBillingId), false);
	          	        }catch (GenericEntityException e) {
	          	    		Debug.logError("Unable to get PeriodBilling record from DataBase"+e, module);
	          	    		return ServiceUtil.returnError("Unable to get PeriodBilling record from DataBase "); 
	          			}     
	             		periodBilling.set("statusId", "CANCEL_FAILED");
	    		    	try{
	    		    		periodBilling.store();    		
	    		    	}catch (Exception e) {
	    		    		Debug.logError("Unable to Store PeriodBilling Status"+e, module);
	    		    		return ServiceUtil.returnError("Unable to Store PeriodBilling Status"); 
	    				}
	    		    	return ServiceUtil.returnError("There was an error while cancel Invoice:");
	                 }	
	             	
		    	    GenericValue tenantConfiguration = null;
		        	tenantConfiguration = delegator.findOne("TenantConfiguration",UtilMisc.toMap("propertyName", "enableTranporterDuePayment", "propertyTypeEnumId", "LMS"),false);
		        	tenantConfiguration.set("propertyValue", "Y");
		        	tenantConfiguration.set("lastUpdatedStamp", UtilDateTime.nowTimestamp());
		        	tenantConfiguration.store(); 
		        	
		        	if (cancelationFailed) {
						periodBilling.set("statusId", "CANCEL_FAILED");
					} else {
						periodBilling.set("statusId", "COM_CANCELLED");
						periodBilling.set("lastModifiedDate", UtilDateTime.nowTimestamp());
					}
					periodBilling.store();	
		        
		        }catch(GenericServiceException e){
			        	Debug.logError("Unable to Cancel transporter margin"+e, module);
			    		return ServiceUtil.returnError("Unable to Cancel transporter margin "); 
			    }catch(GenericEntityException e){
		        	Debug.logError("Unable to Cancel transporter margin"+e, module);
		    		return ServiceUtil.returnError("Unable to Cancel transporter margin "); 
		        }
		        return result;
		    }
		    
		    public static Map<String, Object> cancelVendorMarginReport(DispatchContext dctx, Map<String, ? extends Object> context) {
		    	Delegator delegator = dctx.getDelegator();
		        LocalDispatcher dispatcher = dctx.getDispatcher();       
		        GenericValue userLogin = (GenericValue) context.get("userLogin");
		        Map<String, Object> result = new HashMap<String, Object>();
		        String periodBillingId = (String) context.get("periodBillingId");
		        GenericValue periodBilling = null;
		        try{
		        	periodBilling = delegator.findOne("PeriodBilling",UtilMisc.toMap("periodBillingId", periodBillingId), false);
		        }catch (GenericEntityException e) {
		    		Debug.logError("Unable to get PeriodBilling record from DataBase"+e, module);
		    		return ServiceUtil.returnError("Unable to get PeriodBilling record from DataBase "); 
				}        
		    	periodBilling.set("statusId", "COM_CANCELLED");
		    	try{
		    		periodBilling.store();    		
		    	}catch (Exception e) {
		    		Debug.logError("Unable to Store PeriodBilling Status"+e, module);
		    		return ServiceUtil.returnError("Unable to Store PeriodBilling Status"); 
				}
		        return result;
		    }
		    
		     
		    
		    
		    public static Map<String, Object> getTransporterMarginDuesForPeriodBilling(DispatchContext dctx, Map<String, Object> context) {
				List conditionList= FastList.newInstance(); 
				LocalDispatcher dispatcher = dctx.getDispatcher();
		        Delegator delegator = dctx.getDelegator();
		        Map<String, Object> result = ServiceUtil.returnSuccess();
				String periodBillingId = (String) context.get("periodBillingId");
				GenericValue userLogin = (GenericValue) context.get("userLogin");
				Map<String, Object> routeTradingMap = new HashMap<String, Object>();
	        	String routeId = null;
				if(!UtilValidate.isEmpty(periodBillingId)){
					conditionList.add(EntityCondition.makeCondition("periodBillingId", EntityOperator.EQUALS, periodBillingId));
		        	EntityCondition condition = EntityCondition.makeCondition(conditionList,EntityOperator.AND);
		        	try{
		        		List<GenericValue> dayWiseRouteMargins = FastList.newInstance();
		        		dayWiseRouteMargins = delegator.findList("FacilityCommission", condition, null,null, null, false);	        		
	                	if(!UtilValidate.isEmpty(dayWiseRouteMargins)){
	                		for(GenericValue eachRoute : dayWiseRouteMargins){
	                			BigDecimal totalMargin = BigDecimal.ZERO;  
	                			BigDecimal totalDue = BigDecimal.ZERO;
	                			Map<String, Object> marginDuesMap = FastMap.newInstance();
	                			BigDecimal totalMarginAmount = BigDecimal.ZERO; 
	                			BigDecimal totalDueAmount = BigDecimal.ZERO;
	                			routeId = (String)eachRoute.get("facilityId");
	                			if(!UtilValidate.isEmpty(eachRoute.get("totalAmount"))){
	                				totalMargin = (BigDecimal)eachRoute.get("totalAmount");
	                			}    
	                			if(!UtilValidate.isEmpty(eachRoute.get("dues"))){
	                				totalDue = (BigDecimal)eachRoute.get("dues");
	                			}                			
	                			if(routeTradingMap.containsKey(routeId)){
	                				BigDecimal margin = BigDecimal.ZERO; 
	                				BigDecimal due = BigDecimal.ZERO;
	                				marginDuesMap = (Map)routeTradingMap.get(routeId);
	                				margin = (BigDecimal)marginDuesMap.get("totalMargin");
	                				due = (BigDecimal)marginDuesMap.get("totalDue");
	                				totalMarginAmount = margin.add(totalMargin);
	                				totalDueAmount = due.add(totalDue);
	                				marginDuesMap.put("totalMargin",totalMarginAmount); 
	                				marginDuesMap.put("totalDue",totalDueAmount);
	                				routeTradingMap.put(routeId,marginDuesMap);              				
	                			}
	                			else{
	                				marginDuesMap.put("totalMargin",totalMargin); 
	                				marginDuesMap.put("totalDue", totalDue);
	                				routeTradingMap.put(routeId, marginDuesMap);
	                			}
	                		}
	                	}
		        	}catch(GenericEntityException e){
		        		Debug.logError(e, module);
		        	}
				}			
				result.put("routeTradingMap",routeTradingMap);
	        	return result;
			}
		    public static Map<String, Object> makeVendorMarginPaymentAdjustments(DispatchContext dctx, Map<String, ? extends Object> context) {
		    	GenericDelegator delegator = (GenericDelegator) dctx.getDelegator();
				LocalDispatcher dispatcher = dctx.getDispatcher();
				Map<String, Object> result = ServiceUtil.returnSuccess();
				String periodBillingId = (String) context.get("periodBillingId");
				GenericValue userLogin = (GenericValue) context.get("userLogin");
				String userLoginId = (String)userLogin.get("userLoginId");
				String boothId = null;
				BigDecimal marginAmount = BigDecimal.ZERO;
				BigDecimal marginAmt = BigDecimal.ZERO;
				BigDecimal dueAmount = BigDecimal.ZERO;
				BigDecimal payAmount = BigDecimal.ZERO;
				RoundingMode rounding = RoundingMode.HALF_UP;
				Map<String, Object> paymentMap = FastMap.newInstance();
				boolean adjustmentFailed = false;
				Map<String, Object> boothTradingMap = FastMap.newInstance();
				List<GenericValue> dayWiseBoothDues = FastList.newInstance();
				Map<String, Object> duesMap = FastMap.newInstance();
				Map<String, Object> boothDuesMargin = FastMap.newInstance();
				Map marginDuesMap = FastMap.newInstance();
				GenericValue periodBilling = null;
		        Map<String, Object> inMap = FastMap.newInstance();
				inMap.put("periodBillingId",periodBillingId);
				inMap.put("userLogin",userLogin);
				try{
					boothTradingMap = dispatcher.runSync("getBoothMarginDuesForPeriodBilling", inMap);
					if(ServiceUtil.isError(boothTradingMap)){
						Debug.logError("Unable to fetch booth margins and dues", module);
						return ServiceUtil.returnError("Unable to fetch booth margins and dues");
					}
					boothDuesMargin = (Map)boothTradingMap.get("boothTradingMap");
				}catch(GenericServiceException e){
					Debug.logError(e, module);
				}
				try{
	    	       	periodBilling = delegator.findOne("PeriodBilling",UtilMisc.toMap("periodBillingId", periodBillingId), false);
	    	       	for(Map.Entry<String, Object> entry : boothDuesMargin.entrySet()){
	    	       		Map<String, Object> paymentInputMap = FastMap.newInstance();
	    	       		boothId = (String)entry.getKey();
	    	       		marginDuesMap = (Map)entry.getValue();
	    	       		marginAmount = ((BigDecimal)marginDuesMap.get("totalMargin")).setScale(0,rounding);
	    	       		dueAmount = (BigDecimal)marginDuesMap.get("totalDue");
	    	       		if(marginAmount.compareTo(BigDecimal.ZERO) == 0 || dueAmount.compareTo(BigDecimal.ZERO) == 0){
	    	       			continue;
	    	       		}
	    	       		else{
	    	       			if(marginAmount.compareTo(dueAmount) > 0){
	        	       			payAmount = dueAmount;
	        	       		}
	        	       		else if(marginAmount.compareTo(dueAmount) < 0){
	        	       			payAmount = marginAmount;
	        	       		}
	        	       		else{
	        	       			payAmount = dueAmount;
	        	       		}
	        	       		String paymentRef = "MRG_ADJUST_"+periodBillingId;
	        	       		paymentInputMap.put("userLogin", userLogin);
	        	       		paymentInputMap.put("facilityId",boothId);
	        	       		paymentInputMap.put("supplyDate",UtilDateTime.toDateString(UtilDateTime.nowTimestamp(), "yyyy-MM-dd HH:mm:ss"));
	        	       		paymentInputMap.put("paymentMethodTypeId","LMS_CREDITNOTE_PAYIN");
	        	       		paymentInputMap.put("amount",payAmount.toString());
	        	       		paymentInputMap.put("paymentRefNum",paymentRef);
	        	       		paymentInputMap.put("useFifo",true);
	        	       		if(payAmount.compareTo(BigDecimal.ZERO) > 0){
	        	       			try{
	        	       				paymentMap = dispatcher.runSync("createPaymentForBooth", paymentInputMap);
	        	       				if(ServiceUtil.isError(paymentMap)){
	        	       					adjustmentFailed = true;
	        	       					periodBilling.set("statusId", "ADJUSTMENT_FAIL");
	        	       					periodBilling.store();
	        	       					return ServiceUtil.returnError("Adjustment Failed!");
	        	       				}
	        	       			}catch(GenericServiceException e){
	        	       				Debug.logError(e, module);
	        	       				adjustmentFailed = true;
	        	       			}
	        	       		}
	    	       		}
	    	       	}
	    	       	if (!adjustmentFailed) {
	    	    		periodBilling.set("statusId", "ADJUSTED");
	    	    		periodBilling.set("lastModifiedByUserLogin",userLoginId);
	        	       	periodBilling.set("lastModifiedDate", UtilDateTime.nowTimestamp());
	    	    	}else{
	    	       		periodBilling.set("statusId", "ADJUSTMENT_FAIL");
	   					
	    	       	}
	    	       	periodBilling.store();
	    	    }catch (GenericEntityException e) {
	    	    	Debug.logError("Unable to get PeriodBilling record from DataBase"+e, module);
	    	    	return ServiceUtil.returnError("Unable to get PeriodBilling record from DataBase "); 
	    		}
	        	result = ServiceUtil.returnSuccess("Margins Adjusted Successfully");
	        	return result;
			}	    
		    public static Map<String, Object> makeTransporterMarginPaymentAdjustments(DispatchContext dctx, Map<String, ? extends Object> context) {
		    	GenericDelegator delegator = (GenericDelegator) dctx.getDelegator();
				LocalDispatcher dispatcher = dctx.getDispatcher();
				Map<String, Object> result = ServiceUtil.returnSuccess();
				String periodBillingId = (String) context.get("periodBillingId");
				GenericValue userLogin = (GenericValue) context.get("userLogin");
				String userLoginId = (String)userLogin.get("userLoginId");
				String routeId = null;
				BigDecimal marginAmount = BigDecimal.ZERO;
				BigDecimal marginAmt = BigDecimal.ZERO;
				BigDecimal dueAmount = BigDecimal.ZERO;
				BigDecimal payAmount = BigDecimal.ZERO;
				RoundingMode rounding = RoundingMode.HALF_UP;
				Map<String, Object> paymentMap = FastMap.newInstance();
				boolean adjustmentFailed = false;
				Map<String, Object> routeTradingMap = FastMap.newInstance();
				List<GenericValue> dayWiseBoothDues = FastList.newInstance();
				Map<String, Object> duesMap = FastMap.newInstance();
				Map<String, Object> routeMargin = FastMap.newInstance();
				Map marginDuesMap = FastMap.newInstance();
				GenericValue periodBilling = null;
		        Map<String, Object> inMap = FastMap.newInstance();
				inMap.put("periodBillingId",periodBillingId);
				inMap.put("userLogin",userLogin);
				try{
					routeTradingMap = dispatcher.runSync("getTransporterMarginDuesForPeriodBilling", inMap);				
					if(ServiceUtil.isError(routeTradingMap)){
						Debug.logError("Unable to fetch route margins and dues", module);
						return ServiceUtil.returnError("Unable to fetch route margins and dues");
					}
					routeMargin = (Map)routeTradingMap.get("routeTradingMap");	
				}catch(GenericServiceException e){
					Debug.logError(e, module);
				}
				GenericValue customTimePeriod;
				try{
	    	       	periodBilling = delegator.findOne("PeriodBilling",UtilMisc.toMap("periodBillingId", periodBillingId), false);   
	    	       	if(UtilValidate.isEmpty(periodBilling)){
		        		return ServiceUtil.returnError("Error in getting periodBilling record from Database "); 
		        	}	       
		        	for(Map.Entry<String, Object> entry : routeMargin.entrySet()){
	    	       		Map<String, Object> paymentInputMap = FastMap.newInstance();
	    	       		routeId = (String)entry.getKey();
	    	       		marginDuesMap = (Map)entry.getValue();
	    	       		marginAmount = ((BigDecimal)marginDuesMap.get("totalMargin")).setScale(0,rounding);  
	    	       		dueAmount = (BigDecimal)marginDuesMap.get("totalDue");
	    	       		if(marginAmount.compareTo(BigDecimal.ZERO) == 0 || dueAmount.compareTo(BigDecimal.ZERO) == 0){
	    	       			continue;
	    	       		}
	    	       		else{
	    	       				if(marginAmount != null && dueAmount !=null){  	       				
			    	       			if(marginAmount.compareTo(dueAmount) > 0){
			        	       			payAmount = dueAmount;
			        	       		}
			        	       		else if(marginAmount.compareTo(dueAmount) < 0){
			        	       			payAmount = marginAmount;
			        	       		}
			        	       		else{
			        	       			payAmount = dueAmount;
			        	       		}	    	       			
			    	       			if(payAmount.compareTo(BigDecimal.ZERO) > 0){		    	       					
			        	       			paymentInputMap.put("facilityId",routeId);
			        	       			paymentInputMap.put("amount",payAmount);  
			        	       			paymentInputMap.put("userLogin", userLogin);
			        	       			paymentInputMap.put("periodBillingId", periodBillingId);
			        	       			try{
			        	       				paymentMap = dispatcher.runSync("createTransporterMarginDuePayment", paymentInputMap);		        	       						        	       				
			        	       				if(ServiceUtil.isError(paymentMap)){
			        	       					adjustmentFailed = true;
			        	       					periodBilling.set("statusId", "ADJUSTMENT_FAIL");
			        	       					periodBilling.store();
			        	       					return ServiceUtil.returnError("Adjustment Failed!");
			        	       				}
			        	       			}catch(GenericServiceException e){
			        	       				Debug.logError(e, module);
			        	       				adjustmentFailed = true;
			        	       			}
			    	       			}
	    	       				}
	    	       			}
	    	       		}
	    	       	if (!adjustmentFailed) {
	    	    		periodBilling.set("statusId", "ADJUSTED");
	    	    		periodBilling.set("lastModifiedByUserLogin",userLoginId);
	        	       	periodBilling.set("lastModifiedDate", UtilDateTime.nowTimestamp());
	    	    	}else{
	    	       		periodBilling.set("statusId", "ADJUSTMENT_FAIL");   					
	    	       	}
	    	       	periodBilling.store();
	    	    }catch (GenericEntityException e) {
	    	    	Debug.logError("Unable to get PeriodBilling record from DataBase"+e, module);
	    	    	return ServiceUtil.returnError("Unable to get PeriodBilling record from DataBase "); 
	    		}
	        	result = ServiceUtil.returnSuccess("Margins Adjusted Successfully");
	        	GenericValue tenantConfiguration = null;
	    		try{        
	            	tenantConfiguration = delegator.findOne("TenantConfiguration",UtilMisc.toMap("propertyName", "enableTranporterDuePayment", "propertyTypeEnumId", "LMS"),false);
	            	tenantConfiguration.set("propertyValue", "Y");
	            	tenantConfiguration.set("lastUpdatedStamp", UtilDateTime.nowTimestamp());
	            	tenantConfiguration.store();   
	            }catch(GenericEntityException e){
	            	Debug.logError("Unable to set tenant configuration  record in database"+e, module);
	        		return ServiceUtil.returnError("Unable to set tenant configuration  record in database "); 
	            }
	        	return result;
			}
		    public static Map<String, Object> cancelTransporterMarginAdjustment(DispatchContext dctx, Map<String, ? extends Object> context) {
		    	Delegator delegator = dctx.getDelegator();
		        LocalDispatcher dispatcher = dctx.getDispatcher();       
		        GenericValue userLogin = (GenericValue) context.get("userLogin");
		        String userLoginId = (String)userLogin.get("userLoginId");
		        Map<String, Object> result = new HashMap<String, Object>();
		        String periodBillingId = (String) context.get("periodBillingId");
		        GenericValue periodBilling = null;
		        try{
		        	periodBilling = delegator.findOne("PeriodBilling",UtilMisc.toMap("periodBillingId", periodBillingId), false);
		        	periodBilling.set("statusId", "CANCEL_ADJ_INPROCESS");
		        	periodBilling.store();
		        }catch(GenericEntityException e){
		        	Debug.logError("Unable to get PeriodBilling record from DataBase"+e, module);
	        		return ServiceUtil.returnError("Unable to get PeriodBilling record from DataBase "); 
		        }
		        Map<String, Object> inMap = FastMap.newInstance();
				inMap.put("periodBillingId",periodBillingId);
				inMap.put("userLogin",userLogin);
		        try{
		        	dispatcher.runAsync("cancelTransporterMarginAdjustmentPayment",inMap);
		        }catch(GenericServiceException e){
		        	Debug.logError("Unable to cancel the adjustment"+e, module);
		    		return ServiceUtil.returnError("Unable to cancel margin adjustment in Payments");
		        }
		        return result;
		    }
		    public static Map<String, Object> cancelTransporterMarginAdjustmentPayment(DispatchContext dctx, Map<String, ? extends Object> context) {
		    	Delegator delegator = dctx.getDelegator();
		        LocalDispatcher dispatcher = dctx.getDispatcher();       
		        GenericValue userLogin = (GenericValue) context.get("userLogin");
		        String userLoginId = (String)userLogin.get("userLoginId");
		        Map<String, Object> result = new HashMap<String, Object>();
		        String periodBillingId = (String) context.get("periodBillingId");
		        String paymentRefNum = "MRG_ADJUST_"+periodBillingId;
		        Map<String, Object> cancelResults = FastMap.newInstance();
		        GenericValue periodBilling = null;
		        List<GenericValue> paymentAdjustmentList = null;
		        
		        List conditionList= FastList.newInstance();        	
	        	conditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "PMNT_RECEIVED"));
	        	conditionList.add(EntityCondition.makeCondition("paymentRefNum", EntityOperator.EQUALS, paymentRefNum));
	        	EntityCondition condition = EntityCondition.makeCondition(conditionList,EntityOperator.AND);
	        	try{
	        		paymentAdjustmentList = delegator.findList("Payment",condition,null,null,null,false);
	        	}catch(GenericEntityException e){
	        		Debug.logError("Unable to get Payment record from DataBase"+e, module);
	        		return ServiceUtil.returnError("Unable to get Payment record from DataBase "); 
	        	}
	        	String paymentId = null;
	        	if(paymentAdjustmentList.size()>0){
	        		for(GenericValue eachPayment : paymentAdjustmentList){
	        			Map<String, Object> inMap = FastMap.newInstance();
	        			paymentId = (String)eachPayment.get("paymentId");
	        			inMap.put("paymentId", paymentId);
	        			inMap.put("userLogin", userLogin);
	        			try{
	        				cancelResults = dispatcher.runSync("voidPayment",inMap);
	        				if (ServiceUtil.isError(cancelResults)) {
	        					throw new GenericServiceException(ServiceUtil.getErrorMessage(cancelResults));
	        					//return ServiceUtil.returnError("Cancel Margin Adjustment Failed");
	                        }
	        			}catch(GenericServiceException e){
	        				Debug.logError("Unable to cancel the adjustment"+e, module);
	        	    		return ServiceUtil.returnError("Unable to cancel margin adjustment in Payments");
	        			}
	        		}
	        	}
	        	try{
		        	periodBilling = delegator.findOne("PeriodBilling",UtilMisc.toMap("periodBillingId", periodBillingId), false);
		        	periodBilling.set("statusId", "GENERATED");
		        	periodBilling.store();
		        }catch(GenericEntityException e){
		        	Debug.logError("Unable to get PeriodBilling record from DataBase"+e, module);
	        		return ServiceUtil.returnError("Unable to get PeriodBilling record from DataBase "); 
		        }
		        result = ServiceUtil.returnSuccess("Transporter Margins Adjustment Cancelled Sucessfully");
		        GenericValue tenantConfiguration = null;
				try{        
		        	tenantConfiguration = delegator.findOne("TenantConfiguration",UtilMisc.toMap("propertyName", "enableTranporterDuePayment", "propertyTypeEnumId", "LMS"),false);
		        	tenantConfiguration.set("propertyValue", "N");
		        	tenantConfiguration.set("lastUpdatedStamp", UtilDateTime.nowTimestamp());
		        	tenantConfiguration.store();        	
		        
		        }catch(GenericEntityException e){
		        	Debug.logError("Unable to set tenant configuration  record in database"+e, module);
		    		return ServiceUtil.returnError("Unable to set tenant configuration  record in database "); 
		        }
		        return result;
		    }
		    public static Map<String, Object> CancelMarginAdjustment(DispatchContext dctx, Map<String, ? extends Object> context) {
		    	Delegator delegator = dctx.getDelegator();
		        LocalDispatcher dispatcher = dctx.getDispatcher();       
		        GenericValue userLogin = (GenericValue) context.get("userLogin");
		        String userLoginId = (String)userLogin.get("userLoginId");
		        Map<String, Object> result = new HashMap<String, Object>();
		        String periodBillingId = (String) context.get("periodBillingId");
		        GenericValue periodBilling = null;
		        try{
		        	periodBilling = delegator.findOne("PeriodBilling",UtilMisc.toMap("periodBillingId", periodBillingId), false);
		        	periodBilling.set("statusId", "CANCEL_ADJ_INPROCESS");
		        	periodBilling.store();
		        }catch(GenericEntityException e){
		        	Debug.logError("Unable to get PeriodBilling record from DataBase"+e, module);
	        		return ServiceUtil.returnError("Unable to get PeriodBilling record from DataBase "); 
		        }
		        Map<String, Object> inMap = FastMap.newInstance();
				inMap.put("periodBillingId",periodBillingId);
				inMap.put("userLogin",userLogin);
		        try{
		        	dispatcher.runAsync("cancelMarginAdjustmentPayment",inMap);
		        }catch(GenericServiceException e){
		        	Debug.logError("Unable to cancel the adjustment"+e, module);
		    		return ServiceUtil.returnError("Unable to cancel margin adjustment in Payments");
		        }
		        return result;
		    }
		    public static Map<String, Object> cancelMarginAdjustmentPayment(DispatchContext dctx, Map<String, ? extends Object> context) {
		    	Delegator delegator = dctx.getDelegator();
		        LocalDispatcher dispatcher = dctx.getDispatcher();       
		        GenericValue userLogin = (GenericValue) context.get("userLogin");
		        String userLoginId = (String)userLogin.get("userLoginId");
		        Map<String, Object> result = new HashMap<String, Object>();
		        String periodBillingId = (String) context.get("periodBillingId");
		        String paymentRefNum = "MRG_ADJUST_"+periodBillingId;
		        Map<String, Object> cancelResults = FastMap.newInstance();
		        GenericValue periodBilling = null;
		        List<GenericValue> paymentAdjustmentList = null;
		        
		        List conditionList= FastList.newInstance();
		        conditionList.add(EntityCondition.makeCondition("paymentMethodTypeId", EntityOperator.EQUALS, "LMS_CREDITNOTE_PAYIN"));
	        	conditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "PMNT_RECEIVED"));
	        	conditionList.add(EntityCondition.makeCondition("paymentRefNum", EntityOperator.EQUALS, paymentRefNum));
	        	EntityCondition condition = EntityCondition.makeCondition(conditionList,EntityOperator.AND);
	        	try{
	        		paymentAdjustmentList = delegator.findList("Payment",condition,null,null,null,false);
	        	}catch(GenericEntityException e){
	        		Debug.logError("Unable to get Payment record from DataBase"+e, module);
	        		return ServiceUtil.returnError("Unable to get Payment record from DataBase "); 
	        	}
	        	String paymentId = null;
	        	if(paymentAdjustmentList.size()>0){
	        		for(GenericValue eachPayment : paymentAdjustmentList){
	        			Map<String, Object> inMap = FastMap.newInstance();
	        			paymentId = (String)eachPayment.get("paymentId");
	        			inMap.put("paymentId", paymentId);
	        			inMap.put("userLogin", userLogin);
	        			try{
	        				cancelResults = dispatcher.runSync("voidPayment",inMap);
	        				if (ServiceUtil.isError(cancelResults)) {
	        					throw new GenericServiceException(ServiceUtil.getErrorMessage(cancelResults));
	        					//return ServiceUtil.returnError("Cancel Margin Adjustment Failed");
	                        }
	        			}catch(GenericServiceException e){
	        				Debug.logError("Unable to cancel the adjustment"+e, module);
	        	    		return ServiceUtil.returnError("Unable to cancel margin adjustment in Payments");
	        			}
	        		}
	        	}
	        	try{
		        	periodBilling = delegator.findOne("PeriodBilling",UtilMisc.toMap("periodBillingId", periodBillingId), false);
		        	periodBilling.set("statusId", "GENERATED");
		        	periodBilling.store();
		        }catch(GenericEntityException e){
		        	Debug.logError("Unable to get PeriodBilling record from DataBase"+e, module);
	        		return ServiceUtil.returnError("Unable to get PeriodBilling record from DataBase "); 
		        }
		        result = ServiceUtil.returnSuccess("Vendor Margins Adjustment Cancelled Sucessfully");	        
		        return result;
		    }
		    
		    
		    public static Map<String, Object> transporterMarginAdjustments(DispatchContext dctx, Map<String, ? extends Object> context) {
		    	Delegator delegator = dctx.getDelegator();
		    	LocalDispatcher dispatcher = dctx.getDispatcher();
		    	Map<String, Object> result = ServiceUtil.returnSuccess();
		    	TimeZone timeZone = TimeZone.getDefault();
				Locale locale = Locale.getDefault();
				GenericValue userLogin = (GenericValue) context.get("userLogin");
				String customTimePeriodId = (String) context.get("customTimePeriodId");
				String periodBillingId = (String) context.get("periodBillingId");			
				GenericValue periodBilling = null;
				String userLoginId = (String)userLogin.get("userLoginId");
				String statusId = null;
				try{
		        	periodBilling = delegator.findOne("PeriodBilling",UtilMisc.toMap("periodBillingId", periodBillingId), false);       	
		        	
		        	if(UtilValidate.isEmpty(periodBilling)){
		        		return ServiceUtil.returnError("Unable to get PeriodBilling record from DataBase "); 
		        	}
		        	if(!(periodBilling.get("statusId")).equals("GENERATED")){
			        		Debug.logError("PeriodBilling should be in Generated State", module);
		        			return ServiceUtil.returnError("PeriodBilling should be in Generated Status");
		        	}
	        		periodBilling.set("statusId", "ADJUST_IN_PROCESS");
		        	periodBilling.set("lastModifiedByUserLogin",userLoginId);
		        	periodBilling.set("lastModifiedDate", UtilDateTime.nowTimestamp());
		        	periodBilling.store();
		        }catch (GenericEntityException e) {
		    		Debug.logError("Unable to get PeriodBilling record from DataBase"+e, module);
		    		return ServiceUtil.returnError("Unable to get PeriodBilling record from DataBase "); 
				}
				try{
					Map<String,  Object> adjustInputMap = UtilMisc.<String, Object>toMap("periodBillingId", periodBillingId, "userLogin", userLogin);
					dispatcher.runAsync("makeTransporterMarginPaymentAdjustments", adjustInputMap);
				}
				catch (GenericServiceException e) {
					Debug.logError(e, module);
					return ServiceUtil.returnError(e.getMessage());			
				}	 
				return result;	
		    }

		    public static Map<String, Object> getTransporterTotalsForPeriodBilling(DispatchContext dctx, Map<String, Object> context) {
				List conditionList= FastList.newInstance(); 
				LocalDispatcher dispatcher = dctx.getDispatcher();
		        Delegator delegator = dctx.getDelegator();
		        Map<String, Object> result = ServiceUtil.returnSuccess();
				String periodBillingId = (String) context.get("periodBillingId");
				GenericValue userLogin = (GenericValue) context.get("userLogin");
				Map<String, Object> routeTradingMap = new HashMap<String, Object>();
				Map<String, Object> partyTradingMap = new HashMap<String, Object>();
				Map<String, List> partyAndFacilityList = FastMap.newInstance();
	        	String routeId = null;
	        	String partyId = "";
				if(!UtilValidate.isEmpty(periodBillingId)){
					conditionList.add(EntityCondition.makeCondition("periodBillingId", EntityOperator.EQUALS, periodBillingId));
		        	EntityCondition condition = EntityCondition.makeCondition(conditionList,EntityOperator.AND);
		        	try{
		        		List<GenericValue> dayWiseRouteMargins = FastList.newInstance();
		        		dayWiseRouteMargins = delegator.findList("FacilityCommission", condition, null,null, null, false);	        		
	                	if(!UtilValidate.isEmpty(dayWiseRouteMargins)){
	                		for(GenericValue eachRoute : dayWiseRouteMargins){
	                			BigDecimal totalMargin = BigDecimal.ZERO;  
	                			BigDecimal totalDue = BigDecimal.ZERO;
	                			Map<String, Object> marginDuesMap = FastMap.newInstance();
	                			Map<String, Object> partyTempMap = FastMap.newInstance();
	                			BigDecimal totalMarginAmount = BigDecimal.ZERO; 
	                			BigDecimal totalDueAmount = BigDecimal.ZERO;
	                			BigDecimal partyCommAmount = BigDecimal.ZERO; 
	                			BigDecimal partyDueAmount = BigDecimal.ZERO;
	                			routeId = (String)eachRoute.get("facilityId");
	                			partyId = (String)eachRoute.get("partyId");
	                			if (!UtilValidate.isEmpty(partyId)) {//preparing facilityList for party
		                			if (UtilValidate.isNotEmpty(partyAndFacilityList.get(partyId))) {
		            					List facilityList =FastList.newInstance();
		            					Set tempFacilityList=new HashSet(partyAndFacilityList.get(partyId));
		            					tempFacilityList.add(routeId);
		            					facilityList.addAll(tempFacilityList);
		            					partyAndFacilityList.put(partyId, facilityList);
		            				} else {
		            					partyAndFacilityList.put(partyId,UtilMisc.toList(routeId));
		            				}
	                			}
	                			if(!UtilValidate.isEmpty(eachRoute.get("totalAmount"))){
	                				totalMargin = (BigDecimal)eachRoute.get("totalAmount");
	                				partyCommAmount=totalMargin;
	                			}    
	                			if(!UtilValidate.isEmpty(eachRoute.get("dues"))){
	                				totalDue = (BigDecimal)eachRoute.get("dues");
	                				partyDueAmount=totalDue;
	                			}                			
	                			if(routeTradingMap.containsKey(routeId)){
	                				BigDecimal margin = BigDecimal.ZERO; 
	                				BigDecimal due = BigDecimal.ZERO;
	                				marginDuesMap = (Map)routeTradingMap.get(routeId);
	                				margin = (BigDecimal)marginDuesMap.get("totalMargin");
	                				due = (BigDecimal)marginDuesMap.get("totalDue");
	                				totalMarginAmount = margin.add(totalMargin);
	                				totalDueAmount = due.add(totalDue);
	                				marginDuesMap.put("totalMargin",totalMarginAmount); 
	                				marginDuesMap.put("totalDue",totalDueAmount);
	                				routeTradingMap.put(routeId,marginDuesMap);              				
	                			}
	                			else{
	                				marginDuesMap.put("totalMargin",totalMargin); 
	                				marginDuesMap.put("totalDue", totalDue);
	                				routeTradingMap.put(routeId, marginDuesMap);
	                			}
	                			//populating partyCommission Map
	                			if(partyTradingMap.containsKey(partyId)){
	                				BigDecimal partyMargin = BigDecimal.ZERO; 
	                				BigDecimal partyDue = BigDecimal.ZERO;
	                				partyTempMap = (Map)partyTradingMap.get(partyId);
	                				partyMargin = (BigDecimal)partyTempMap.get("totalMargin");
	                				partyDue = (BigDecimal)partyTempMap.get("totalDue");
	                				partyCommAmount = partyCommAmount.add(partyMargin);
	                				partyDueAmount = partyDueAmount.add(partyDue);
	                				partyTempMap.put("totalMargin",partyCommAmount); 
	                				partyTempMap.put("totalDue",partyDueAmount);
	                				partyTradingMap.put(partyId,partyTempMap);              				
	                			}else{
	                				partyTempMap.put("totalMargin",partyCommAmount); 
	                				partyTempMap.put("totalDue", partyDueAmount);
	                				partyTradingMap.put(partyId, partyTempMap);
	                			}
	                			
	                		}
	                	}
		        	}catch(GenericEntityException e){
		        		Debug.logError(e, module);
		        	}
				}			
				Map<String, Object> roundedPartyTradingMap = new HashMap<String, Object>();
				for (Map.Entry<String, Object> entry : routeTradingMap.entrySet()) {//rounding by Routewise
					Map<String, Object> routeValue = (Map<String, Object>) entry.getValue();
					BigDecimal tempVal = (BigDecimal) routeValue.get("totalMargin");
					tempVal = tempVal.setScale(0,BigDecimal.ROUND_HALF_UP);
					tempVal = tempVal.setScale(2,BigDecimal.ROUND_HALF_UP);//making decimals
					routeValue.put("totalMargin", tempVal);

					tempVal = (BigDecimal) routeValue.get("totalDue");
					tempVal = tempVal.setScale(0,BigDecimal.ROUND_HALF_UP);
					tempVal = tempVal.setScale(2,BigDecimal.ROUND_HALF_UP);//making decimals
					routeValue.put("totalDue", tempVal);
				}
				for (Map.Entry<String, List> entry : partyAndFacilityList.entrySet()) {
						List<String> routesList = (List)entry.getValue();
						Map tempTradeMap=FastMap.newInstance();
						String tempPartyId= entry.getKey();
						BigDecimal partyMargin = BigDecimal.ZERO; 
        				BigDecimal partyDue = BigDecimal.ZERO;
						for (String partyRouteId :routesList) {
							Map routeValueMap=(Map)routeTradingMap.get(partyRouteId);
							if(!UtilValidate.isEmpty(routeValueMap)){
								partyMargin=partyMargin.add((BigDecimal)routeValueMap.get("totalMargin"));
								partyDue=partyDue.add((BigDecimal)routeValueMap.get("totalDue"));
							}
						}
						tempTradeMap.put("totalMargin", partyMargin);
						tempTradeMap.put("totalDue", partyDue);
						roundedPartyTradingMap.put(tempPartyId, tempTradeMap);
				}
				result.put("routeTradingMap",routeTradingMap);
				result.put("partyTradingMap",partyTradingMap);
				result.put("roundedPartyTradingMap",roundedPartyTradingMap);
	        	return result;
			}
		    
		    
		    
		    public static BigDecimal sum(List<BigDecimal> list) {
		    	BigDecimal sum = BigDecimal.ZERO; 
		        for (BigDecimal i:list)
		        	sum = sum.add(i);
		        return sum;
		    }
		    
		    public static Map<String, Object> populateVehicleTripParty(DispatchContext ctx, Map<String, ? extends Object> context ) {
		    	Delegator delegator = ctx.getDelegator();
		    	LocalDispatcher dispatcher = ctx.getDispatcher();
		    	Timestamp nowTimeStamp=UtilDateTime.nowTimestamp();
		        List<String> facilityIds = (List<String>) context.get("facilityIds");
		        List<String> shipmentIds = FastList.newInstance();
		        GenericValue userLogin = (GenericValue) context.get("userLogin");
		    	TimeZone timeZone = TimeZone.getDefault();
		    	Locale locale = Locale.getDefault();
		        Map<String, Object> vehicleTripResult = new HashMap<String, Object>();
		        Map<String, Object> vehicleTripStatusResult = new HashMap<String, Object>();
		        Map<String, Object> vehicleTripEntity = FastMap.newInstance(); 
		        Map<String, Object> vehicleTripStatusEntity = FastMap.newInstance();
		        Map<String, Object> result = FastMap.newInstance(); 
		        List conditionList= FastList.newInstance(); 
		        Timestamp fromDate = (Timestamp) context.get("fromDate");
		        if (UtilValidate.isEmpty(fromDate)) {
		            Debug.logError("fromDate cannot be empty", module);
		            return ServiceUtil.returnError("fromDate cannot be empty");        	
		        }        
		        Timestamp thruDate = (Timestamp) context.get("thruDate");  
		        if (UtilValidate.isEmpty(thruDate)) {
		            Debug.logError("thruDate cannot be empty", module);
		            return ServiceUtil.returnError("thruDate cannot be empty");        	
		        }     
		        String  seqId="";
		        Debug.log("=====FromDate=="+fromDate+"=====thruDate===="+thruDate);
		        boolean setVehicleStatusFinal = Boolean.FALSE;
				try{  //getting configuration for vehcileStsus Set
					 GenericValue tenantConfigVehicleStatusSet = delegator.findOne("TenantConfiguration", UtilMisc.toMap("propertyTypeEnumId","LMS", "propertyName","setVehicleStatusFinal"), true);
					 if (UtilValidate.isNotEmpty(tenantConfigVehicleStatusSet) && (tenantConfigVehicleStatusSet.getString("propertyValue")).equals("Y")) {
						 setVehicleStatusFinal = Boolean.TRUE;
					 	} 
		    	Timestamp monthBegin = UtilDateTime.getDayStart(fromDate, timeZone, locale);
				Timestamp monthEnd = UtilDateTime.getDayEnd(thruDate, timeZone, locale);
				
				int totalDays=UtilDateTime.getIntervalInDays(monthBegin,monthEnd);
				Timestamp supplyDate = monthBegin;
		        for (int k = 0; k <= (totalDays); k++) {
					supplyDate = UtilDateTime.addDaysToTimestamp(monthBegin, k);
					Map facilityParty=(Map)ByProductNetworkServices.getFacilityPartyContractor(ctx, UtilMisc.toMap("saleDate",supplyDate)).get("facilityPartyMap");
					 //we have to change this shipmentIds helper
				     shipmentIds = ByProductNetworkServices.getByProdShipmentIds(delegator, supplyDate, supplyDate);//this will give Today AM+PM as SALE
					//shipmentIds =ByProductNetworkServices.getShipmentIds(delegator , UtilDateTime.toDateString(supplyDate, "yyyy-MM-dd HH:mm:ss"),null,null);	//get Day Shipments
					 for (int i = 0; i <(shipmentIds.size()); i++) {
						  String shipmentId=shipmentIds.get(i);
							conditionList.clear();
							conditionList.add(EntityCondition.makeCondition("shipmentId", EntityOperator.EQUALS, shipmentId));
							EntityCondition vhCondition = EntityCondition.makeCondition(conditionList, EntityOperator.AND);
							List<GenericValue> vehicleTrpList = delegator.findList("VehicleTrip", vhCondition, null, UtilMisc.toList("originFacilityId"), null, false);
							if(UtilValidate.isEmpty(vehicleTrpList)){//if empty create vehicleTrip this time.
								GenericValue shipment=delegator.findOne("Shipment",UtilMisc.toMap("shipmentId", shipmentId), false);
								 String routeId = shipment.getString("routeId");
					           	if (shipment == null) {
					           		Debug.logError("Shipment does not exist " + shipmentId, module);
					           		return ServiceUtil.returnError("Shipment does not exist " + shipmentId);                    	
					           	}
								Map vehicleCtx = UtilMisc.toMap("facilityId",routeId);
						        vehicleCtx.put("supplyDate", supplyDate);
						        Map vehicleRoleResult =  (Map) ByProductNetworkServices.getVehicleRole(ctx,vehicleCtx);
						        if(UtilValidate.isNotEmpty(vehicleRoleResult.get("vehicleRole"))){
						        	 GenericValue vehicleRole= (GenericValue) vehicleRoleResult.get("vehicleRole");
									String  vehicleId=vehicleRole.getString("vehicleId");
							        if(UtilValidate.isNotEmpty(vehicleId)){
							        	vehicleTripEntity.put("vehicleId", vehicleId);
							        	vehicleTripStatusEntity.put("vehicleId", vehicleId);
							        }
							        vehicleTripEntity.put("originFacilityId", routeId);
							        vehicleTripEntity.put("partyId", facilityParty.get(routeId));
							        vehicleTripEntity.put("userLogin", userLogin);
							        vehicleTripEntity.put("shipmentId", shipmentId);
							        vehicleTripEntity.put("createdDate", nowTimeStamp);
							        vehicleTripEntity.put("createdByUserLogin", userLogin.get("userLoginId"));
							        vehicleTripEntity.put("lastModifiedByUserLogin", userLogin.get("userLoginId"));
							        try {
							        	vehicleTripResult=dispatcher.runSync("createVehicleTrip", vehicleTripEntity);
							            seqId =(String) vehicleTripResult.get("sequenceNum");
							            if (ServiceUtil.isError(vehicleTripResult)){
							  		  		String errMsg =  ServiceUtil.getErrorMessage(vehicleTripResult);
							  		  		Debug.logError(errMsg , module);
							  		  	    return ServiceUtil.returnError("createVehicleTrip service" + vehicleId);    
							  		  	}
							          
							        } catch (GenericServiceException e) {
							            Debug.logError(e, "Error calling createVehicleTrip service", module);
							            return ServiceUtil.returnError(e.getMessage());
							        } 
							        vehicleTripStatusEntity.put("facilityId",routeId);
							        vehicleTripStatusEntity.put("sequenceNum", seqId);
							        vehicleTripStatusEntity.put("userLogin", userLogin);
							        vehicleTripStatusEntity.put("statusId", "VEHICLE_OUT");
							        if(setVehicleStatusFinal){//if configuration set StatusFinal Y then stsus is Returned
							        	 vehicleTripStatusEntity.put("statusId", "VEHICLE_RETURNED");
							        }
							        vehicleTripStatusEntity.put("createdDate", nowTimeStamp);
							        vehicleTripStatusEntity.put("createdByUserLogin", userLogin.get("userLoginId"));
							        vehicleTripStatusEntity.put("lastModifiedByUserLogin", userLogin.get("userLoginId"));
							        try {
							        	vehicleTripStatusResult= dispatcher.runSync("createVehicleTripStatus", vehicleTripStatusEntity);
							            if (ServiceUtil.isError(vehicleTripStatusResult)) {
							  		  		String errMsg =  ServiceUtil.getErrorMessage(vehicleTripStatusResult);
							  		  		Debug.logError(errMsg , module);
							  		  	    return ServiceUtil.returnError("createVehicleTripStatus service" + vehicleId);   
							  		  	}
							        }catch (GenericServiceException e) {
							            Debug.logError(e, "Error calling createVehicleTripStatus service", module);
							            return ServiceUtil.returnError(e.getMessage());
							        }
							      }
							}else{
								 GenericValue updateVehicleTrip=EntityUtil.getFirst(vehicleTrpList);
								 String routeId=updateVehicleTrip.getString("originFacilityId");
								 if(UtilValidate.isNotEmpty(routeId)){//rewrite with upperCase
									 routeId=routeId.toUpperCase(locale);
								 }
								 try{ updateVehicleTrip.set("partyId",facilityParty.get(routeId));
								      updateVehicleTrip.set("originFacilityId",routeId);
									  updateVehicleTrip.set("lastUpdatedStamp", UtilDateTime.nowTimestamp());
								      updateVehicleTrip.set("lastModifiedDate", UtilDateTime.nowTimestamp());
									   updateVehicleTrip.store(); 
								    }catch(GenericEntityException e){
								    	Debug.logError("Unable to set vehicleTrip  record in database"+e, module);
										return ServiceUtil.returnError("Unable to set vehicleTrip   record in database "); 
								    }
								 
							}
							
					 }
		        }
				}catch (GenericEntityException e) {
					// TODO: handle exception
					Debug.logError(e, module);
				}	
				result = ServiceUtil.returnSuccess("Service For UpdateVehicleStatus Runs Sucessfully");	        
		        return result;
		      }
		    public static Map<String, Object> createTransporterRecovery(DispatchContext dctx, Map context) {
		    	Map<String, Object> result = ServiceUtil.returnSuccess();
		    	String facilityId = (String) context.get("facilityId");
		    	String customTimePeriodId = (String) context.get("customTimePeriodId");
		    	String recoveryTypeId = (String)context.get("recoveryTypeId");
		    	Timestamp incidentDate = (Timestamp)context.get("incidentDate");
		    	BigDecimal amount = (BigDecimal)context.get("amount");
				GenericValue userLogin = (GenericValue) context.get("userLogin");
				String description=(String)context.get("description");
		    	GenericDelegator delegator = (GenericDelegator) dctx.getDelegator();
				LocalDispatcher dispatcher = dctx.getDispatcher();
				List conditionList=FastList.newInstance();
				try {
	                conditionList.add(EntityCondition.makeCondition("billingTypeId", EntityOperator.EQUALS, "PB_LMS_TRSPT_MRGN"));
	                conditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "GENERATED"));
	                conditionList.add(EntityCondition.makeCondition("customTimePeriodId", EntityOperator.EQUALS, customTimePeriodId));
		        	EntityCondition condition = EntityCondition.makeCondition(conditionList,EntityOperator.AND);
		        		List<GenericValue> periodBillingList = FastList.newInstance();
		        		periodBillingList = delegator.findList("PeriodBilling", condition, null,null, null, false);	 
		        		if(UtilValidate.isNotEmpty(periodBillingList)){
		        			 Debug.logError("Billing Is Already Generated For This Period", module);
		        			 return ServiceUtil.returnError("Billing Is Already Generated For This Period  and You Can Not Create Recovery!");   
		        		}
					GenericValue facilityRecovery = delegator.makeValue("FineRecovery");
	    			facilityRecovery.put("facilityId", facilityId );
	    			facilityRecovery.put("customTimePeriodId", customTimePeriodId);
	    			facilityRecovery.put("incidentDate", incidentDate);
	    			facilityRecovery.put("recoveryTypeId", recoveryTypeId); 
	    			facilityRecovery.put("amount", amount);
	    			facilityRecovery.put("description", description);
	    			facilityRecovery.put("createdDate", UtilDateTime.nowTimestamp());
	    			facilityRecovery.put("lastModifiedDate", UtilDateTime.nowTimestamp());
	    			facilityRecovery.put("createdByUserLogin", userLogin.get("userLoginId"));
	    			delegator.createSetNextSeqId(facilityRecovery);            
	    			String recoveryId = (String) facilityRecovery.get("recoveryId");
					
		    		/*GenericValue facilityRecovery = delegator.findOne("FacilityRecovery", UtilMisc.toMap("facilityId", facilityId, "customTimePeriodId", customTimePeriodId, "recoveryTypeId",recoveryTypeId), false);
		    		if (facilityRecovery == null) {
		    			facilityRecovery = delegator.makeValue("FacilityRecovery");
		    			facilityRecovery.put("facilityId", facilityId );
		    			facilityRecovery.put("customTimePeriodId", customTimePeriodId);
		    			facilityRecovery.put("incidentDate", incidentDate);
		    			facilityRecovery.put("recoveryTypeId", recoveryTypeId); 
		    			facilityRecovery.put("amount", amount);
		    			facilityRecovery.put("createdDate", UtilDateTime.nowTimestamp());
		    			facilityRecovery.put("lastModifiedDate", UtilDateTime.nowTimestamp());
		    			facilityRecovery.put("createdByUserLogin", userLogin.get("userLoginId"));
		    			facilityRecovery.create();    
		            }
		    		else {  
		    			facilityRecovery.set("amount", amount);
		    			//facilityRecovery.set("createdDate", UtilDateTime.nowTimestamp());
		    			//facilityRecovery.set("createdByUserLogin", userLogin.get("userLoginId"));
		    			if(UtilValidate.isNotEmpty(incidentDate)){
			    			facilityRecovery.set("incidentDate", incidentDate);
			    			}
		    			facilityRecovery.set("lastModifiedDate", UtilDateTime.nowTimestamp());
		    			facilityRecovery.set("lastModifiedByUserLogin", userLogin.get("userLoginId"));
	    			    facilityRecovery.store();
		            }*/
		        }catch(GenericEntityException e){
					Debug.logError("Error while creating Transporter Recovery"+e.getMessage(), module);
				}
		        result = ServiceUtil.returnSuccess("Transporter Recovery Created Sucessfully");
		        result.put("createdDate",UtilDateTime.nowTimestamp());
		        result.put("incidentDate",incidentDate);
		        return result;
		    }  
		    
		    public static Map<String, Object> getFacilityRecvoryForPeriodBilling(DispatchContext dctx, Map<String, Object> context) {
				List conditionList= FastList.newInstance(); 
				LocalDispatcher dispatcher = dctx.getDispatcher();
		        Delegator delegator = dctx.getDelegator();
		        Map<String, Object> result = ServiceUtil.returnSuccess();
				String periodBillingId = (String) context.get("periodBillingId");
				
				GenericValue userLogin = (GenericValue) context.get("userLogin");
				Map<String, Object> facilityRecoveryInfoMap = new HashMap<String, Object>();
				//Map<String, Object> facilityRecoveryInfoMap = new HashMap<String, Object>();
				Map<String, Object> partyRecoveryInfoMap = new HashMap<String, Object>();
				Map<String, Object> partyHikesInfoMap = new HashMap<String, Object>();//for Extra Payments
				Timestamp monthBegin =UtilDateTime.getDayStart(UtilDateTime.nowTimestamp());
				if(UtilValidate.isNotEmpty((Timestamp) context.get("fromDate"))){
					monthBegin=(Timestamp) context.get("fromDate");
				}
	        	//String facilityId= null;
				List<String> facilityIdsList=FastList.newInstance();
				if(!UtilValidate.isEmpty(periodBillingId)){
					conditionList.add(EntityCondition.makeCondition("periodBillingId", EntityOperator.EQUALS, periodBillingId));
		        	EntityCondition condition = EntityCondition.makeCondition(conditionList,EntityOperator.AND);
		        	try{
		        		List<GenericValue> facilityRecoveryList = FastList.newInstance();
		        		facilityRecoveryList = delegator.findList("FineRecovery", condition, null,null, null, false);	 
			        	facilityIdsList = EntityUtil.getFieldListFromEntityList(facilityRecoveryList, "facilityId", false);
			        		
			        	List<GenericValue>	allFaclityCrateFinesList = EntityUtil.filterByCondition(facilityRecoveryList, EntityCondition.makeCondition("recoveryTypeId", EntityOperator.EQUALS, "CRATES"));
			        	List<GenericValue>	allFaclityCanFinesList = EntityUtil.filterByCondition(facilityRecoveryList, EntityCondition.makeCondition("recoveryTypeId", EntityOperator.EQUALS, "CANS"));
			        	List<GenericValue>	allFaclityOtherFinesList = EntityUtil.filterByCondition(facilityRecoveryList, EntityCondition.makeCondition("recoveryTypeId", EntityOperator.LIKE, "FINES_%"));
			        	//for Hikes also Using Same but instead of Minus we will do plus in where ever we need
			        	List<GenericValue>	allFaclityHikesList = EntityUtil.filterByCondition(facilityRecoveryList, EntityCondition.makeCondition("recoveryTypeId", EntityOperator.LIKE, "HIKES_%"));
                        //Debug.log("====allFaclityHikesList="+allFaclityHikesList);
	                	if(!UtilValidate.isEmpty(facilityIdsList)){
	                		for(String facilityId : facilityIdsList){
	                			Map<String, Object> facilityFineTempMap = FastMap.newInstance();
	                			BigDecimal crateFineAmount = BigDecimal.ZERO;  
	                			BigDecimal canFineAmount = BigDecimal.ZERO;
	                			BigDecimal otherFinesAmount=BigDecimal.ZERO;
	                			BigDecimal totalFineAmount = BigDecimal.ZERO;
	                			BigDecimal totalHikeAmount = BigDecimal.ZERO;
	                			List<GenericValue>	faclityCrateFinesList=EntityUtil.filterByCondition(allFaclityCrateFinesList, EntityCondition.makeCondition("facilityId", EntityOperator.EQUALS, facilityId));
	                			List<GenericValue>	faclityCanFinesList=EntityUtil.filterByCondition(allFaclityCanFinesList, EntityCondition.makeCondition("facilityId", EntityOperator.EQUALS, facilityId));
	                			List<GenericValue>	faclityOtherFinesList=EntityUtil.filterByCondition(allFaclityOtherFinesList, EntityCondition.makeCondition("facilityId", EntityOperator.EQUALS, facilityId));
	                			
	                			List<GenericValue>	faclityAllHikesList=EntityUtil.filterByCondition(allFaclityHikesList, EntityCondition.makeCondition("facilityId", EntityOperator.EQUALS, facilityId));
	                			for(GenericValue facilityRecCrate : faclityCrateFinesList){
	                				crateFineAmount=crateFineAmount.add(facilityRecCrate.getBigDecimal("amount"));
	                			}
	                			for(GenericValue facilityRecCan : faclityCanFinesList){
	                				canFineAmount=canFineAmount.add(facilityRecCan.getBigDecimal("amount"));
	                			}
	                			for(GenericValue facilityRecOther : faclityOtherFinesList){
	                				otherFinesAmount=otherFinesAmount.add(facilityRecOther.getBigDecimal("amount"));
	                			}
	                			totalFineAmount=totalFineAmount.add(crateFineAmount);
	                			totalFineAmount=totalFineAmount.add(canFineAmount);
	                			totalFineAmount=totalFineAmount.add(otherFinesAmount);
	                			
	                			facilityFineTempMap.put("cratesFine",crateFineAmount); 
	                			facilityFineTempMap.put("cansFine",canFineAmount);
	                			facilityFineTempMap.put("othersFine",otherFinesAmount);
	                			facilityFineTempMap.put("totalFine",totalFineAmount);
	                			for(GenericValue facilityHikeOther : faclityAllHikesList){
	                				totalHikeAmount=totalHikeAmount.add(facilityHikeOther.getBigDecimal("amount"));
	                			}
	                			facilityFineTempMap.put("totalHikeAmount",totalHikeAmount);
	                			
	                			facilityRecoveryInfoMap.put(facilityId, facilityFineTempMap);
	                			
	                		}
	                	}
		        	}catch(GenericEntityException e){
		        		Debug.logError(e, module);
		        	}
				}			
				result.put("facilityRecoveryInfoMap",facilityRecoveryInfoMap);
				Map<String, Object> partyFacilityMap=(Map<String, Object>)ByProductNetworkServices.getFacilityPartyContractor(dctx, UtilMisc.toMap("saleDate",monthBegin)).get("partyAndFacilityList");
				  for ( Map.Entry<String, Object> entry : partyFacilityMap.entrySet() ) {
					     String  partyId=(String)entry.getKey();
			        	List<String> partyFacilityList = (List<String>)entry.getValue();
			        	Map<String, Object> partyFineTempMap = FastMap.newInstance();
			        	BigDecimal crateFineAmount = BigDecimal.ZERO;  
            			BigDecimal canFineAmount = BigDecimal.ZERO;
            			BigDecimal otherFinesAmount=BigDecimal.ZERO;
            			BigDecimal totalFineAmount = BigDecimal.ZERO;
            			BigDecimal totalHikeAmount = BigDecimal.ZERO;
            			
			        	 for(String faclityId:partyFacilityList){//adding each party Total fines which are having Routes
			        		 if(UtilValidate.isNotEmpty(facilityRecoveryInfoMap.get(faclityId))){
			        			Map facilityFineMap=(Map)facilityRecoveryInfoMap.get(faclityId);
			        			crateFineAmount=crateFineAmount.add((BigDecimal)facilityFineMap.get("cratesFine"));
			        			canFineAmount=canFineAmount.add((BigDecimal)facilityFineMap.get("cansFine"));
			        			otherFinesAmount=otherFinesAmount.add((BigDecimal)facilityFineMap.get("othersFine"));
			        			totalFineAmount=totalFineAmount.add((BigDecimal)facilityFineMap.get("totalFine"));
			        			totalHikeAmount=totalHikeAmount.add((BigDecimal)facilityFineMap.get("totalHikeAmount"));
			        		 }
			        	 }
		        	    partyFineTempMap.put("cratesFine",crateFineAmount); 
            			partyFineTempMap.put("cansFine",canFineAmount);
            			partyFineTempMap.put("othersFine",otherFinesAmount);
            			partyFineTempMap.put("totalFine",totalFineAmount);
            			partyFineTempMap.put("totalHikeAmount",totalHikeAmount);// newly added for Diesel Hike
            			
            			partyRecoveryInfoMap.put(partyId, partyFineTempMap);
				  }
				  result.put("partyRecoveryInfoMap",partyRecoveryInfoMap);
	        	return result;
			}
		    public static Map<String, Object> updateFineRecvoryWithBilling(DispatchContext dctx, Map<String, Object> context) {
				List conditionList= FastList.newInstance(); 
				LocalDispatcher dispatcher = dctx.getDispatcher();
		        Delegator delegator = dctx.getDelegator();
		        Map<String, Object> result = ServiceUtil.returnSuccess();
				String periodBillingId = (String) context.get("periodBillingId");
				String customTimePeriodId = (String) context.get("customTimePeriodId");
				GenericValue userLogin = (GenericValue) context.get("userLogin");
	        	//String facilityId= null;
				List<String> facilityIdsList=FastList.newInstance();
				if(UtilValidate.isNotEmpty(periodBillingId) &&(UtilValidate.isNotEmpty(customTimePeriodId))){
					conditionList.add(EntityCondition.makeCondition("customTimePeriodId", EntityOperator.EQUALS, customTimePeriodId));
		        	EntityCondition condition = EntityCondition.makeCondition(conditionList,EntityOperator.AND);
		        	try{
		        		List<GenericValue> facilityRecoveryList = FastList.newInstance();
		        		facilityRecoveryList = delegator.findList("FineRecovery", condition, null,null, null, false);	 

	                	if(!UtilValidate.isEmpty(facilityRecoveryList)){
	                		for(GenericValue facilityRecovery : facilityRecoveryList){
	                			facilityRecovery.set("periodBillingId",periodBillingId);
	                			facilityRecovery.set("lastModifiedDate", UtilDateTime.nowTimestamp());
	    		    			facilityRecovery.set("lastModifiedByUserLogin", userLogin.get("userLoginId"));
	                			facilityRecovery.store();
	                		}
	                	}
		        	}catch(GenericEntityException e){
		        		Debug.logError(e, module);
		        	}
				}			
	        	return result;
			}
		    public static Map<String, Object>  cancelTranporterRecovery(DispatchContext dctx, Map<String, ? extends Object> context)  {
		    	GenericDelegator delegator = (GenericDelegator) dctx.getDelegator();
				LocalDispatcher dispatcher = dctx.getDispatcher();
				Map<String, Object> result = FastMap.newInstance();	
				GenericValue userLogin = (GenericValue) context.get("userLogin");
				String facilityId = (String) context.get("facilityId");
		    	String customTimePeriodId = (String) context.get("customTimePeriodId");
		    	String recoveryTypeId = (String)context.get("recoveryTypeId");
		    	String recoveryId = (String)context.get("recoveryId");
		    	GenericValue customTimePeriod = null;
				List conditionList = FastList.newInstance();
				List periodBillingList = FastList.newInstance();


		    	try {
		    		try {
						conditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.IN , UtilMisc.toList("GENERATED","IN_PROCESS","APPROVED","APPROVED_PAYMENT")));
				        conditionList.add(EntityCondition.makeCondition("customTimePeriodId", EntityOperator.EQUALS ,customTimePeriodId));
				    	EntityCondition condition=EntityCondition.makeCondition(conditionList,EntityOperator.AND);
				    	periodBillingList = delegator.findList("PeriodBilling", condition, null,null, null, false);

					} catch (GenericEntityException e1) {
						Debug.logError(e1,"Error While Finding PeriodBilling");
						return ServiceUtil.returnError("Error While Finding PeriodBilling" + e1);
					}
					if(UtilValidate.isNotEmpty(periodBillingList)){
						 return ServiceUtil.returnError("Failed to cancel Recovery. Billing already generated! ");
					}
					GenericValue facilityRecovery = delegator.findOne("FineRecovery", UtilMisc.toMap("recoveryId",recoveryId), false);
					if(UtilValidate.isNotEmpty(facilityRecovery)){
						delegator.removeValue(facilityRecovery);    
		            }
						
		    	}catch (GenericEntityException e) {
		    		 Debug.logError(e, module);
		             return ServiceUtil.returnError("Failed to find FineRecovery " + e);
				} 
				result = ServiceUtil.returnSuccess("Recovery Successfully Cancelled..");
				return result;
		}// end of service
		    
		    public static Map<String, Object>  sendTransporterMarginSMS(DispatchContext dctx, Map<String, Object> context)  {
		        LocalDispatcher dispatcher = dctx.getDispatcher();	
		        Delegator delegator = dctx.getDelegator();
		        GenericValue userLogin = (GenericValue) context.get("userLogin");
		        String facilityId = (String) context.get("facilityId");
		        String partyId = (String) context.get("partyId");
		        String customTimePeriodId = (String) context.get("customTimePeriodId");
		        BigDecimal routeAmount = (BigDecimal)context.get("routeAmount");
		        BigDecimal totalFine = (BigDecimal)context.get("totalFine");
		        BigDecimal netAmount = (BigDecimal)context.get("netAmount");
		        String fromDate = null;
		        String thruDate = null;
		        Map<String, Object> serviceResult;
		        Map<String, Object> userServiceResult;
		        String countryCode = "91";
		        String contactNumberTo = null;
		        Timestamp fromDateTime  = null;
		        try {
			        GenericValue customTimePeriod = delegator.findOne("CustomTimePeriod", UtilMisc.toMap("customTimePeriodId", customTimePeriodId),false);
		        	if (UtilValidate.isNotEmpty(customTimePeriod)) {
		        		fromDateTime=UtilDateTime.toTimestamp(customTimePeriod.getDate("fromDate"));
		        		Timestamp thruDateTime=UtilDateTime.toTimestamp(customTimePeriod.getDate("thruDate"));
					    fromDate = (UtilDateTime.toDateString(fromDateTime, "MMMdd")).toString();
					    thruDate = (UtilDateTime.toDateString(thruDateTime, "MMMdd yyyy")).toString();
		        	}
		        }catch (GenericEntityException e) {
	               Debug.logError(e, module);
	               return ServiceUtil.returnError(e.getMessage());
				}
		        try {
		        	// Send SMS notification to contractor
		        	Map facilityParty=(Map)ByProductNetworkServices.getFacilityPartyContractor(dctx, UtilMisc.toMap("saleDate",fromDateTime ,"facilityId",facilityId)).get("facilityPartyMap");
		        	Map<String, Object> getTelParams = FastMap.newInstance();
		        	getTelParams.put("partyId", facilityParty.get(facilityId));
		        	if(UtilValidate.isNotEmpty(partyId)){
		        		 getTelParams.put("partyId", partyId);
		        	 }
		             getTelParams.put("userLogin", userLogin);                    	
		             serviceResult = dispatcher.runSync("getPartyTelephone", getTelParams);
		             if (ServiceUtil.isError(serviceResult)) {
		             	 Debug.logError(ServiceUtil.getErrorMessage(serviceResult), module);
		                 return ServiceUtil.returnSuccess();
		             } 
		             if(UtilValidate.isNotEmpty(serviceResult.get("contactNumber"))){
		             	contactNumberTo = (String) serviceResult.get("contactNumber");
		             	if(!UtilValidate.isEmpty(serviceResult.get("countryCode"))){
		             		contactNumberTo = (String) serviceResult.get("countryCode") + (String) serviceResult.get("contactNumber");
		             	}
		             }	
		        	String text = "DTC Bill generated for route " + facilityId+" ("+fromDate+"-"+thruDate+")."+ "  Gross Amount: Rs" +
		        	routeAmount.setScale(1, UtilNumber.getBigDecimalRoundingMode("order.rounding")) + 
					"; Deductions: Rs" +
					totalFine.setScale(1, UtilNumber.getBigDecimalRoundingMode("order.rounding")) +
					"; Net Amount: Rs" +  
					netAmount.setScale(1, UtilNumber.getBigDecimalRoundingMode("order.rounding")) + 
		        	". Automated message from Mother Dairy.";
					Debug.logInfo("Sms text: " + text, module);
					 Map<String, Object> sendSmsParams = FastMap.newInstance();
					 if(UtilValidate.isNotEmpty(contactNumberTo)){
						 sendSmsParams.put("contactNumberTo", contactNumberTo);                     
				         sendSmsParams.put("text",text);  
				         dispatcher.runAsync("sendSms", sendSmsParams,false); 
					 }
			           
				}
				catch (Exception e) {
					Debug.logError(e, "Error calling sendSmsToContactListNoCommEvent service", module);
					return ServiceUtil.returnError(e.getMessage());			
				} 
		        return ServiceUtil.returnSuccess("Sms successfully sent!");		
		 }
		    
		    public static Map<String, Object>  sendFacilityRecoverySms(DispatchContext dctx, Map<String, Object> context)  {
		        LocalDispatcher dispatcher = dctx.getDispatcher();	
		        Delegator delegator = dctx.getDelegator();
		        GenericValue userLogin = (GenericValue) context.get("userLogin");
		        String facilityId = (String) context.get("facilityId");
		        String partyId = (String) context.get("partyId");
		        Timestamp createdDate = (Timestamp) context.get("incidentDate");
		        String fromDate = (UtilDateTime.toDateString(createdDate, "MMMM dd,yyyy")).toString();
		        String recoveryTypeId = (String)context.get("recoveryTypeId");
		        BigDecimal amount = (BigDecimal)context.get("amount");
		        Map<String, Object> serviceResult;
		        Map<String, Object> userServiceResult;
		        String countryCode = "91";
		        String contactNumberTo = null;
		        String description = null;
		        try {
		        	GenericValue enumeration = delegator.findOne("Enumeration", UtilMisc.toMap("enumId", recoveryTypeId),false);
		        	if (UtilValidate.isNotEmpty(enumeration)) {
		        		description = enumeration.getString("description");
		        	}
		        }catch (GenericEntityException e) {
	               Debug.logError(e, module);
	               return ServiceUtil.returnError(e.getMessage());
				}
		        try {
		        	// Send SMS notification to contractor
		        	Map facilityParty=(Map)ByProductNetworkServices.getFacilityPartyContractor(dctx, UtilMisc.toMap("saleDate",createdDate ,"facilityId",facilityId)).get("facilityPartyMap");
		        	if(UtilValidate.isEmpty(facilityParty.get(facilityId))){
	        			 Debug.logError("'Route "+facilityId+" Expired or No Contarctor Assigned To This Route....!'Entry Not Possible", module);
	        			 return ServiceUtil.returnError("'Route "+facilityId+" Expired or No Contarctor Assigned To This Route....!'Entry Not Possible");   
	        		}
		        	Map<String, Object> getTelParams = FastMap.newInstance();
		        	 getTelParams.put("partyId", facilityParty.get(facilityId));
		        	 if(UtilValidate.isNotEmpty(partyId)){
		        		 getTelParams.put("partyId", partyId);
		        	 }
		             getTelParams.put("userLogin", userLogin);                    	
		             serviceResult = dispatcher.runSync("getPartyTelephone", getTelParams);
		             if (ServiceUtil.isError(serviceResult)) {
		             	 Debug.logError(ServiceUtil.getErrorMessage(serviceResult), module);
		                 return ServiceUtil.returnSuccess();
		             } 
		             if(!UtilValidate.isEmpty(serviceResult.get("contactNumber"))){
		             	contactNumberTo = (String) serviceResult.get("contactNumber");
		             	if(!UtilValidate.isEmpty(serviceResult.get("countryCode"))){
		             		contactNumberTo = (String) serviceResult.get("countryCode") + (String) serviceResult.get("contactNumber");
		             	}
		             }	
		        	String text = " A penalty("+description+")" +" of " + " Rs."+amount.setScale(1, UtilNumber.getBigDecimalRoundingMode("order.rounding"))
		        	+" has been levied for route("+facilityId+")"+" on "+fromDate+"."+
					" Automated message from Mother Dairy.";
					Debug.logInfo("Sms text: " + text, module);
					Map<String, Object> sendSmsParams = FastMap.newInstance();      
				   if(UtilValidate.isNotEmpty(contactNumberTo)){
						 sendSmsParams.put("contactNumberTo", contactNumberTo);                     
				         sendSmsParams.put("text",text);  
				         dispatcher.runAsync("sendSms", sendSmsParams,false); 
					 }
					
				}catch (Exception e) {
					Debug.logError(e, "Error calling sendSmsToContactListNoCommEvent service", module);
					return ServiceUtil.returnError(e.getMessage());			
				} 
		        return ServiceUtil.returnSuccess("Sms successfully sent!");		
		 }   
		 
		    public static Map<String, Object> getFacilityByFinAccount(DispatchContext ctx, Map<String, ? extends Object> context) {
		    	Delegator delegator = ctx.getDelegator();
		    	String finAccountId = (String)context.get("finAccountId");
		    	Timestamp fromDate = (Timestamp)context.get("fromDate");
		    	Timestamp thruDate = (Timestamp)context.get("thruDate");
		    	String reportflag = (String)context.get("reportflag");
		    	GenericValue userLogin = (GenericValue) context.get("userLogin");
		    	Map<String, Object> result = ServiceUtil.returnSuccess();
		    	List partyList= FastList.newInstance();
		    	try {
		    		List conditionList= FastList.newInstance();
		    		conditionList.add(EntityCondition.makeCondition("finAccountId", EntityOperator.EQUALS, finAccountId));
		    		conditionList.add(EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "DTC_BILL"));
		    		 if((UtilValidate.isNotEmpty(reportflag))&&("DTCReport".equals(reportflag))){
		    		conditionList.add(EntityCondition.makeCondition("fromDate", EntityOperator.LESS_THAN_EQUAL_TO, fromDate));

		    		conditionList.add(EntityCondition.makeCondition(EntityCondition.makeCondition("thruDate", EntityOperator.EQUALS, null), EntityOperator.OR,
		    				EntityCondition.makeCondition("thruDate", EntityOperator.GREATER_THAN_EQUAL_TO, fromDate)));
		    		 }
		        	EntityCondition condition = EntityCondition.makeCondition(conditionList,EntityOperator.AND); 
		        	List<GenericValue> finAccountRoleList = delegator.findList("FinAccountRole", condition, null, null, null, false);
		        	partyList = EntityUtil.getFieldListFromEntityList(finAccountRoleList, "partyId", false);
		    		
		    	} catch (GenericEntityException e) {
		            Debug.logError(e, module);
		            return ServiceUtil.returnError(e.getMessage());
		        }
		        result.put("partyList", partyList);
		        return result;
		    }
		    public static Map<String, Object> updateDTCStatus (DispatchContext dctx, Map<String, ? extends Object> context) {
		    	Delegator delegator = dctx.getDelegator();
		    	TimeZone timeZone = TimeZone.getDefault();
		        LocalDispatcher dispatcher = dctx.getDispatcher();       
		        GenericValue userLogin = (GenericValue) context.get("userLogin");
		    	Locale locale = (Locale) context.get("locale");
		        Map<String, Object> result = new HashMap<String, Object>();
		        String periodBillingId = (String) context.get("periodBillingId");
		        String statusId = (String) context.get("statusId");
		        if(UtilValidate.isEmpty(statusId)){
		        	statusId="GENERATED";
		        }
		        GenericValue periodBilling = null;
		        String customTimePeriodId="";
		        try{
		        	periodBilling = delegator.findOne("PeriodBilling",UtilMisc.toMap("periodBillingId", periodBillingId), false);
		        	 customTimePeriodId =  periodBilling.getString("customTimePeriodId");
		        	  periodBilling.set("statusId", statusId);
					  periodBilling.store();
		        }catch (GenericEntityException e) {
		    		Debug.logError("Unable to get PeriodBilling record from DataBase"+e, module);
		    		return ServiceUtil.returnError("Unable to get PeriodBilling record from DataBase "); 
				}   
		        GenericValue customTimePeriod;
				try {
					customTimePeriod = delegator.findOne("CustomTimePeriod",UtilMisc.toMap("customTimePeriodId", customTimePeriodId), false);
				} catch (GenericEntityException e1) {
					Debug.logError(e1,"Error While Finding Customtime Period");
					return ServiceUtil.returnError("Error While Finding Customtime Period" + e1);
				}
				Debug.log("===================statusId=="+statusId);
				if("APPROVED".equalsIgnoreCase(statusId)){
				Map dtcInvoiceResult=createDTCInvoice(dctx, UtilMisc.toMap("periodBillingId",periodBillingId ,"userLogin",userLogin));
				result.putAll(dtcInvoiceResult);
		       }
				if("APPROVED_PAYMENT".equalsIgnoreCase(statusId)){
					Map dtcPaymentResult=createTransporterPayment(dctx, UtilMisc.toMap("periodBillingId",periodBillingId ,"userLogin",userLogin));
					try{
			        	  periodBilling.set("statusId", "APPROVED_PAYMENT");
						  periodBilling.store();
			        }catch (GenericEntityException e) {
			    		Debug.logError("Unable to Make Payment Process For DTC Billing"+e, module);
			    		return ServiceUtil.returnError("Unable to Make Payment Process For DTC Billing! "); 
					}   
					result.putAll(dtcPaymentResult);
			       }
				if("REJECT_PAYMENT".equalsIgnoreCase(statusId)){
					Map dtcPaymentCancelResult=cancelTransporterPayment(dctx, UtilMisc.toMap("periodBillingId",periodBillingId ,"userLogin",userLogin));
					try{
			        	  periodBilling.set("statusId", "REJECT_PAYMENT");
						  periodBilling.store();
			        }catch (GenericEntityException e) {
			    		Debug.logError("Unable To Cancel DTC Bill Payment"+e, module);
			    		return ServiceUtil.returnError("Unable To Cancel DTC Bill Payment.. "); 
					}   
					result.putAll(dtcPaymentCancelResult);
			       }
				
				return result;
		    }
		    public static Map<String, Object> getDTCBillingInvoices(DispatchContext dctx, Map<String, ? extends Object> context) {
		    	Delegator delegator = dctx.getDelegator();
		        LocalDispatcher dispatcher = dctx.getDispatcher();       
		        GenericValue userLogin = (GenericValue) context.get("userLogin");
		        Map<String, Object> result = new HashMap<String, Object>();
		        Locale locale = (Locale) context.get("locale");
		        boolean cancelationFailed = false;	
		        String periodBillingId = (String) context.get("periodBillingId");
		        List invoiceIdsList=FastList.newInstance();
		        GenericValue periodBilling = null;
		        String customTimePeriodId="";
		        try{
		        	periodBilling = delegator.findOne("PeriodBilling",UtilMisc.toMap("periodBillingId", periodBillingId), false);
		        	 customTimePeriodId =  periodBilling.getString("customTimePeriodId");
		        }catch (GenericEntityException e) {
		    		Debug.logError("Unable to get PeriodBilling record from DataBase"+e, module);
		    		return ServiceUtil.returnError("Unable to get PeriodBilling record from DataBase "); 
				}        
		    	GenericValue customTimePeriod;
				try {
					customTimePeriod = delegator.findOne("CustomTimePeriod",UtilMisc.toMap("customTimePeriodId", customTimePeriodId), false);
				} catch (GenericEntityException e1) {
					Debug.logError(e1, e1.getMessage());
					return ServiceUtil.returnError("Error in customTimePeriod" + e1);
				}
				Timestamp fromDateTime=UtilDateTime.toTimestamp(customTimePeriod.getDate("fromDate"));
				Timestamp thruDateTime=UtilDateTime.toTimestamp(customTimePeriod.getDate("thruDate"));
				
				Timestamp monthBegin = UtilDateTime.getDayStart(fromDateTime, TimeZone.getDefault(), locale);
				Timestamp monthEnd = UtilDateTime.getDayEnd(thruDateTime, TimeZone.getDefault(), locale);
				
				try{
					List conditionList = UtilMisc.toList(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, "Company"));
					  conditionList.add(EntityCondition.makeCondition("referenceNumber", EntityOperator.EQUALS,  "TRSPT_MRGN_"+periodBillingId));
					  
				     conditionList.add(EntityCondition.makeCondition("invoiceTypeId", EntityOperator.EQUALS, "SALES_DIS_OUT"));
				    /* conditionList.add(EntityCondition.makeCondition("dueDate", EntityOperator.GREATER_THAN_EQUAL_TO ,monthBegin));
				     conditionList.add(EntityCondition.makeCondition("dueDate", EntityOperator.LESS_THAN_EQUAL_TO ,monthEnd));*/
		             conditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "INVOICE_CANCELLED")); 
		            
		        	EntityCondition condition = EntityCondition.makeCondition(conditionList, EntityOperator.AND);      	
		        	List<GenericValue> invoiceRows = delegator.findList("Invoice", condition, null, null, null, false);
			        invoiceIdsList = EntityUtil.getFieldListFromEntityList(invoiceRows, "invoiceId", false);
			        
				  }catch(GenericEntityException e){
			        	Debug.logError("Unable to get DTC invoices"+e, module);
			    		return ServiceUtil.returnError("Unable to get DTC invoices "); 
			      }
			     result.put("invoiceIdsList", invoiceIdsList);
		        return result;
		    }
		    public static Map<String, Object> createTransporterPayment(DispatchContext dctx, Map<String, ? extends Object> context) {
				Delegator delegator = dctx.getDelegator();
				LocalDispatcher dispatcher = dctx.getDispatcher();
				 GenericValue userLogin = (GenericValue) context.get("userLogin");
				 String periodBillingId = (String) context.get("periodBillingId");
				Map<String, Object> result = ServiceUtil.returnSuccess();
				
				List<String> billingInvoiceIdsList=(List<String>)getDTCBillingInvoices(dctx, UtilMisc.toMap("periodBillingId", periodBillingId,"userLogin", userLogin)).get("invoiceIdsList");
				Debug.log("==========billingInvoiceIdsList===="+billingInvoiceIdsList);
				boolean useFifo = Boolean.FALSE;
				if (UtilValidate.isNotEmpty(context.get("useFifo"))) {
					useFifo = (Boolean) context.get("useFifo");
				}
				Locale locale = (Locale) context.get("locale");
				String paymentMethodType = (String) context.get("paymentMethodTypeId");
				
				String facilityId = (String) context.get("facilityId");
			
				String orderId = (String) context.get("orderId");
				BigDecimal paymentAmount = ProductEvents.parseBigDecimalForEntity((String) context.get("amount"));
				String paymentRef = "TRSPT_MRGN_"+periodBillingId;
				String paymentId = "";
				boolean roundingAdjustmentFlag = Boolean.TRUE;
				List exprListForParameters = FastList.newInstance();
				List boothOrdersList = FastList.newInstance();
				Timestamp paymentTimestamp = UtilDateTime.nowTimestamp();
				Timestamp instrumentDate = UtilDateTime.nowTimestamp();
				
				Map<String, Object> paymentCtx = UtilMisc.<String, Object>toMap("paymentTypeId", "EXPENSE_PAYOUT");
				paymentCtx.put("paymentMethodTypeId", "CHEQUE_PAYIN");
				paymentCtx.put("paymentMethodId", "");
				paymentCtx.put("partyId","Company");	
				/*paymentCtx.put("finAccountTransTypeId","WITHDRAWAL");
				paymentCtx.put("isDepositWithDrawPayment","Y");*/
			 try { 
				for(String invoiceId:billingInvoiceIdsList){
						
					Map<String, Object> getInvoicePaymentInfoListResult = dispatcher.runSync("getInvoicePaymentInfoList", UtilMisc.toMap("userLogin", userLogin, "invoiceId",invoiceId));
					if (ServiceUtil.isError(getInvoicePaymentInfoListResult)) {
						Debug.logError(getInvoicePaymentInfoListResult.toString(),module);
						return ServiceUtil.returnError(null, null, null,getInvoicePaymentInfoListResult);
					}
					Map invoicePaymentInfo = (Map) ((List) getInvoicePaymentInfoListResult.get("invoicePaymentInfoList")).get(0);
					BigDecimal outStandingAmount = (BigDecimal) invoicePaymentInfo.get("outstandingAmount");
					  
					if(UtilValidate.isNotEmpty(invoicePaymentInfo)){
						 if(outStandingAmount.compareTo(BigDecimal.ZERO)>0){
						 GenericValue invoice = delegator.findOne("Invoice", UtilMisc.toMap("invoiceId",invoiceId), false);
						           // paymentCtx.put("partyIdFrom","Company");
						            paymentCtx.put("organizationPartyId",invoice.getString("partyIdFrom"));
						            paymentCtx.put("facilityId", invoice.getString("facilityId"));
						            paymentCtx.put("paymentPurposeType", "");
						            paymentCtx.put("paymentRefNum", paymentRef); 
						            paymentCtx.put("instrumentDate", invoice.getTimestamp("dueDate"));
									paymentCtx.put("paymentDate", invoice.getTimestamp("dueDate"));
									paymentCtx.put("effectiveDate", invoice.getTimestamp("dueDate"));
						            //paymentCtx.put("statusId", "PMNT_RECEIVED");
						            paymentCtx.put("statusId", "PMNT_NOT_PAID");
						            paymentCtx.put("isEnableAcctg", "Y");
						            paymentCtx.put("amount", outStandingAmount);
						            paymentCtx.put("userLogin", userLogin); 
						            paymentCtx.put("invoices", UtilMisc.toList(invoiceId));
						    		try{
						            Map<String, Object> paymentResult = dispatcher.runSync("createPaymentAndApplicationForInvoices", paymentCtx);
						            if (ServiceUtil.isError(paymentResult)) {
						            	Debug.logError(paymentResult.toString(), module);
						                return ServiceUtil.returnError(null, null, null, paymentResult);
						            }
						            paymentId = (String)paymentResult.get("paymentId");
						            }catch (Exception e) {
						            Debug.logError(e, e.toString(), module);
						            return ServiceUtil.returnError(e.toString());
							        }
						 }
					 }
				}
			 }catch (GenericEntityException e) {
					Debug.logError("Error while Creating Payment for DTC"+ e.getMessage(), module);
					return ServiceUtil.returnError("Error while Creating Payment for DTC");
				} catch (GenericServiceException e) {
					Debug.logError("Error while Creating Payment for DTC" + e.getMessage(),module);
					return ServiceUtil.returnError("Error while Creating Payment for DTC");
				}
				result = ServiceUtil.returnSuccess("Payment successfully done for This Billing ..!");
				return result;
			}
		    
		    public static Map<String, Object> getDTCBillingPayments(DispatchContext dctx, Map<String, ? extends Object> context) {
		    	Delegator delegator = dctx.getDelegator();
		        LocalDispatcher dispatcher = dctx.getDispatcher();       
		        GenericValue userLogin = (GenericValue) context.get("userLogin");
		        Map<String, Object> result = new HashMap<String, Object>();
		        Locale locale = (Locale) context.get("locale");
		        boolean cancelationFailed = false;	
		        String periodBillingId = (String) context.get("periodBillingId");
		        List paymentIdsList=FastList.newInstance();
		    	List dtcPaymentsList = FastList.newInstance();
		        GenericValue periodBilling = null;
		        String customTimePeriodId="";
		        try{
		        	periodBilling = delegator.findOne("PeriodBilling",UtilMisc.toMap("periodBillingId", periodBillingId), false);
		        	 customTimePeriodId =  periodBilling.getString("customTimePeriodId");
		        }catch (GenericEntityException e) {
		    		Debug.logError("Unable to get PeriodBilling record from DataBase"+e, module);
		    		return ServiceUtil.returnError("Unable to get PeriodBilling record from DataBase "); 
				}        
		    	/*GenericValue customTimePeriod;
				try {
					customTimePeriod = delegator.findOne("CustomTimePeriod",UtilMisc.toMap("customTimePeriodId", customTimePeriodId), false);
				} catch (GenericEntityException e1) {
					Debug.logError(e1, e1.getMessage());
					return ServiceUtil.returnError("Error in customTimePeriod" + e1);
				}
				Timestamp fromDateTime=UtilDateTime.toTimestamp(customTimePeriod.getDate("fromDate"));
				Timestamp thruDateTime=UtilDateTime.toTimestamp(customTimePeriod.getDate("thruDate"));
				
				Timestamp monthBegin = UtilDateTime.getDayStart(fromDateTime, TimeZone.getDefault(), locale);
				Timestamp monthEnd = UtilDateTime.getDayEnd(thruDateTime, TimeZone.getDefault(), locale);*/
				
				try{
					List conditionList = UtilMisc.toList(EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, "Company"));
					  conditionList.add(EntityCondition.makeCondition("paymentRefNum", EntityOperator.EQUALS,  "TRSPT_MRGN_"+periodBillingId));
				     conditionList.add(EntityCondition.makeCondition("paymentTypeId", EntityOperator.EQUALS, "EXPENSE_PAYOUT"));
		             conditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_IN, UtilMisc.toList("PMNT_VOID","PMNT_CANCELLED")));
		            
		        	EntityCondition condition = EntityCondition.makeCondition(conditionList, EntityOperator.AND);      	
		        	List<GenericValue> paymentsList = delegator.findList("Payment", condition, null, null, null, false);
			        paymentIdsList = EntityUtil.getFieldListFromEntityList(paymentsList, "paymentId", false);
			        dtcPaymentsList.addAll(paymentsList);
				  }catch(GenericEntityException e){
			        	Debug.logError("Unable to get DTC Payments"+e, module);
			    		return ServiceUtil.returnError("Unable to get DTC Payments "); 
			      }
			     result.put("paymentIdsList", paymentIdsList);
			     result.put("dtcPaymentsList", dtcPaymentsList);
		        return result;
		    }    
		    public static Map<String, Object> cancelTransporterPayment(DispatchContext dctx, Map<String, ? extends Object> context) {
				Delegator delegator = dctx.getDelegator();
				LocalDispatcher dispatcher = dctx.getDispatcher();
				 GenericValue userLogin = (GenericValue) context.get("userLogin");
				 String periodBillingId = (String) context.get("periodBillingId");
				Map<String, Object> result = ServiceUtil.returnSuccess();
				List<String> billingPaymentIdsList=(List<String>)getDTCBillingPayments(dctx, UtilMisc.toMap("periodBillingId", periodBillingId,"userLogin", userLogin)).get("paymentIdsList");
				Debug.log("==========billingPaymentIdsList===="+billingPaymentIdsList);
				Locale locale = (Locale) context.get("locale");
				if(UtilValidate.isEmpty(billingPaymentIdsList)){
					result = ServiceUtil.returnSuccess("No Payments Found To Cancel ..!");
					return result;
				}
			 try { 
				for(String paymentId:billingPaymentIdsList){
				            	 Map<String, Object> removePaymentApplResult = dispatcher.runSync("voidPayment", UtilMisc.toMap("userLogin" ,userLogin ,"paymentId", paymentId));
								 if (ServiceUtil.isError(removePaymentApplResult)) {
						            	Debug.logError(removePaymentApplResult.toString(), module);    			
						                return ServiceUtil.returnError(null, null, null, removePaymentApplResult);
						         }
				    }
			    }catch (GenericServiceException e) {
					Debug.logError("Error while Cancel Payment for DTC" + e.getMessage(),module);
					return ServiceUtil.returnError("Error while Cancel Payment for DTC");
				}
				result = ServiceUtil.returnSuccess("Payment  Cancelled For Billing ..!");
				return result;
			}
}


