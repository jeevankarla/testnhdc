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
import java.util.Calendar;
import javolution.util.FastList;
import javolution.util.FastMap;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.ofbiz.base.util.UtilNumber;
import org.ofbiz.party.party.PartyHelper;
import java.math.RoundingMode;
import java.util.Map;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.network.LmsServices;
import in.vasista.vbiz.byproducts.TransporterServices;

import in.vasista.vbiz.byproducts.ByProductNetworkServices;

rounding = UtilNumber.getBigDecimalRoundingMode("order.rounding");
context.rounding = rounding;

dctx = dispatcher.getDispatchContext();
periodBillingId = null;
if(parameters.periodBillingId){
	periodBillingId = parameters.periodBillingId;
}else{
	context.errorMessage = "No PeriodBillingId Found";
	return;
}
facilityCommissionList = [];
customTimePeriod=delegator.findOne("CustomTimePeriod",[customTimePeriodId : parameters.customTimePeriodId], false);
fromDateTime=UtilDateTime.toTimestamp(customTimePeriod.getDate("fromDate"));
thruDateTime=UtilDateTime.toTimestamp(customTimePeriod.getDate("thruDate"));
fromDateStr = UtilDateTime.toDateString(fromDateTime,"MMM(dd-");
thruDateStr = UtilDateTime.toDateString(thruDateTime,"dd),yyyy");
context.put("fromDateStr",fromDateStr);
context.put("thruDateStr",thruDateStr);
context.put("fromDateTime",fromDateTime);
context.put("thruDateTime",thruDateTime);
dctx = dispatcher.getDispatchContext();
monthBegin = UtilDateTime.getDayStart(fromDateTime, timeZone, locale);
monthEnd = UtilDateTime.getDayEnd(thruDateTime, timeZone, locale);
Map partyFacilityMap=(Map)ByProductNetworkServices.getFacilityPartyContractor(dctx, UtilMisc.toMap("saleDate",monthBegin)).get("partyAndFacilityList");
facilityWorkOrdrNumMap = [:];
facilityWorkOrderNoList = delegator.findList("FacilityAttribute", EntityCondition.makeCondition("attrName", EntityOperator.EQUALS, "WORK_ORDER_NO"), null, null, null, false);
facilityWorkOrderNoList.each{eachWork ->
	facilityWorkOrdrNumMap.put(eachWork.facilityId, eachWork.attrValue);
}
context.facilityWorkOrdrNumMap = facilityWorkOrdrNumMap;//workOrder Numbers

conditionList = [];
conditionList.add(EntityCondition.makeCondition("periodBillingId", EntityOperator.EQUALS , periodBillingId));
conditionList.add(EntityCondition.makeCondition("commissionDate", EntityOperator.EQUALS , monthBegin));
//conditionList.add(EntityCondition.makeCondition("facilityId", EntityOperator.IN ,UtilMisc.toList("S01","S02","S03")));
condition = EntityCondition.makeCondition(conditionList,EntityOperator.AND);
EntityFindOptions findOptions = new EntityFindOptions();
routesList = delegator.findList("FacilityAndCommission",condition,["facilityId"]as Set, UtilMisc.toList("parentFacilityId","facilityId"),findOptions,false);
routeIdsList = EntityUtil.getFieldListFromEntityList(routesList, "facilityId", false);

routeMarginMap =[:];
masterList=[];
grTotalMap =[:];
supplyDate = monthBegin;
Map transporterMargins= new LinkedHashMap();
routesList.each{ route ->
	TransporterMarginReportList =[];
	dayTotalsMap = [:];
	grTotalMap["grTotQty"]=BigDecimal.ZERO;
	grTotalMap["grTotRtAmount"]=BigDecimal.ZERO;
	grTotalMap["grTotpendingDue"]=BigDecimal.ZERO;
	grTotalMap["monthBill"]=BigDecimal.ZERO;
	
	for(int i=0 ; i <= (UtilDateTime.getIntervalInDays(monthBegin,monthEnd)); i++){
		currentDate=UtilDateTime.addDaysToTimestamp(monthBegin,i);
		dayOfMonth=UtilDateTime.getDayOfMonth(currentDate ,timeZone, locale);
		dayTotalsMap[(String)dayOfMonth] = [:];
		dayTotalsMap[(String)dayOfMonth].putAll(routeMarginMap);
	
	}
	routesRateAmount =[:];
	dayTotalsMap["Tot"] =[:];
	dayTotalsMap["Tot"].putAll(grTotalMap);
	TransporterMarginReportList.add(dayTotalsMap);
	transporterMargins[route.facilityId]=TransporterMarginReportList;
}
conditionList.clear();
conditionList.add(EntityCondition.makeCondition("periodBillingId", EntityOperator.EQUALS , periodBillingId));
//conditionList.add(EntityCondition.makeCondition("commissionDate", EntityOperator.EQUALS , monthBegin));
//conditionList.add(EntityCondition.makeCondition("facilityId", EntityOperator.IN ,UtilMisc.toList("S01","S02","S03")));
condition = EntityCondition.makeCondition(conditionList,EntityOperator.AND);
facilityCommissionList = delegator.findList("FacilityCommission",condition , null, ["commissionDate"], null, false);

routeSmsMap=[:];
routePartyMap=[:];
if(UtilValidate.isNotEmpty(facilityCommissionList)){
	facilityCommissionList.each { facilityCommission ->
		facilityId = facilityCommission.facilityId;
		partyId = facilityCommission.partyId;
		if(UtilValidate.isEmpty(routePartyMap.get(facilityId))){
			routePartyMap[facilityId]=partyId;
		 }
		
		facilityRateResult=[:];
		rateMap =[:];
		rateList =[];
		partyIdentification=[];
		facilityParty=[];
		conditionList.clear();
		conditionList.add(EntityCondition.makeCondition("ownerPartyId", EntityOperator.EQUALS, partyId));
		conditionList.add(EntityCondition.makeCondition("ownerPartyId", EntityOperator.EQUALS, "FNACT_ACTIVE"));
		fincondition = EntityCondition.makeCondition(conditionList,EntityOperator.AND);
		List<GenericValue> facilities = delegator.findList("Facility", EntityCondition.makeCondition("facilityId", EntityOperator.EQUALS, facilityId), null, null, null, false);
		
		List<GenericValue> finAccountDetails = delegator.findList("FinAccount", fincondition, null, null, null, false);
		if(UtilValidate.isNotEmpty(finAccountDetails)){
			finAccount = EntityUtil.getFirst(finAccountDetails);
		}
		List<GenericValue> partyIdentificationDetails = delegator.findList("PartyIdentification", EntityCondition.makeCondition([partyId: partyId, partyIdentificationTypeId: "PAN_NUMBER"]), null, null, null, false);
		if(UtilValidate.isNotEmpty(partyIdentificationDetails)){
			partyIdentification = EntityUtil.getFirst(partyIdentificationDetails);
		}
		List<GenericValue> facilityPartyDtls = delegator.findList("FacilityParty", EntityCondition.makeCondition([partyId: partyId]), null, null, null, false);
		if(UtilValidate.isNotEmpty(facilityPartyDtls)){
			facilityParty = EntityUtil.getFirst(facilityPartyDtls);
		}
		
		String partyName = PartyHelper.getPartyName(delegator, partyId, true);
		
		facility = EntityUtil.getFirst(facilities);

		Map inputRateAmt =  UtilMisc.toMap("userLogin", userLogin);
		inputRateAmt.put("rateCurrencyUomId", "INR");
		inputRateAmt.put("facilityId", facilityId);
		inputRateAmt.put("fromDate",monthBegin );
		inputRateAmt.put("rateTypeId", "TRANSPORTER_MRGN");
		facilityRateResult = dispatcher.runSync("getFacilityRateAmount", inputRateAmt);
		
		Map inputFacilitySize =  UtilMisc.toMap("userLogin", userLogin);
		inputFacilitySize.put("rateCurrencyUomId", "LEN_km");
		inputFacilitySize.put("facilityId", facilityId);
		inputFacilitySize.put("fromDate",monthBegin);
		inputFacilitySize.put("rateTypeId", "FACILITY_SIZE");
		facilitySizeResult = dispatcher.runSync("getRouteDistance", inputFacilitySize);
			
		
		List dayTotalsList =  transporterMargins[facilityId];
		dayValuesMap = dayTotalsList.get(0);
		routeValueMap =[:];
		totalsMap =[:];
		totalsMap = dayValuesMap["Tot"];
		
			if(UtilValidate.isNotEmpty(partyId)){
				totalsMap.put("partyCode",partyId);
			}
			if(UtilValidate.isNotEmpty(facilitySizeResult)){
				distance = (BigDecimal) facilitySizeResult.get("facilitySize");
				totalsMap.put("distance", distance);
			}
		
			if(UtilValidate.isNotEmpty(partyName)){
				totalsMap.put("partyName",partyName);
			}
			if(UtilValidate.isNotEmpty(partyIdentification)){
			    totalsMap.put("panId",partyIdentification.idValue);
			}
			if(UtilValidate.isNotEmpty(facilityParty)){
				closedDate=facilityParty.thruDate;
				if(UtilValidate.isNotEmpty(closedDate)){
				  closedDate=UtilDateTime.toDateString(closedDate, "dd-MMM-yyyy");
				}
				totalsMap.put("closedDate", closedDate);
			}
			if(UtilValidate.isNotEmpty(finAccountDetails)){
				totalsMap.put("accNo", finAccount.finAccountCode);
			}
		
		if(UtilValidate.isNotEmpty(facilityRateResult)){
			monthBeginMargin = (BigDecimal) facilityRateResult.get("rateAmount");
			uomId=(String)facilityRateResult.get("uomId");
			totalsMap.put("uomId", uomId);
			totalsMap.put("margin", monthBeginMargin);
		}
		int dayInteger = UtilDateTime.getDayOfMonth((facilityCommission.commissionDate),timeZone, locale);
		routeValueMap = dayValuesMap[(String)dayInteger];
		routeValueMap["totalQuantity"]=BigDecimal.ZERO;
		if(UtilValidate.isNotEmpty(facilityCommission.totalQty)){
			routeValueMap["totalQuantity"] = ((new BigDecimal(facilityCommission.totalQty)).setScale(1,BigDecimal.ROUND_HALF_UP));
			totalsMap["grTotQty"] += ((new BigDecimal(facilityCommission.totalQty)).setScale(1,BigDecimal.ROUND_HALF_UP));
		}
		routeValueMap["rtAmount"] = BigDecimal.ZERO;
		if(UtilValidate.isNotEmpty(facilityCommission.totalAmount)){
			routeValueMap["rtAmount"] = ((new BigDecimal(facilityCommission.totalAmount)).setScale(2,BigDecimal.ROUND_HALF_UP));
		
			totalsMap["grTotRtAmount"] += ((new BigDecimal(facilityCommission.totalAmount)).setScale(2,BigDecimal.ROUND_HALF_UP));
		}
		routeValueMap["pendingDue"] =BigDecimal.ZERO;
		if(UtilValidate.isNotEmpty(facilityCommission.dues)){
			routeValueMap["pendingDue"] = ((new BigDecimal(facilityCommission.dues)).setScale(2,BigDecimal.ROUND_HALF_UP));
		totalsMap["grTotpendingDue"] += ((new BigDecimal(facilityCommission.dues)).setScale(2,BigDecimal.ROUND_HALF_UP));
		}		
		// for transporter SMS
		if(UtilValidate.isEmpty(routeSmsMap[facilityId])){
			routeSmsMap[facilityId] = ((new BigDecimal(facilityCommission.totalAmount)).setScale(2,BigDecimal.ROUND_HALF_UP));
		}else{
			routeSmsMap[facilityId] += ((new BigDecimal(facilityCommission.totalAmount)).setScale(2,BigDecimal.ROUND_HALF_UP));
		}
	}
}
if(UtilValidate.isNotEmpty(transporterMargins)){
	masterList.add(transporterMargins);
}
//rounding	total commission						
masterList.each{eachMargin->
	eachMargin.each{eachRouteMargin->
		dayWiseList=eachRouteMargin.getValue();
		dayWiseList.each{eachDayMargin->
			totalsMap =[:];
			totalsMap = eachDayMargin.getAt("Tot");
			totalMargin=BigDecimal.ZERO;
			totalMargin=totalsMap.getAt("grTotRtAmount");
			totalMargin=new BigDecimal(totalMargin).setScale(0,BigDecimal.ROUND_HALF_UP);//rounding
			totalMargin=new BigDecimal(totalMargin).setScale(2,BigDecimal.ROUND_HALF_UP);
			totalsMap.putAt("grTotRtAmount",totalMargin);
			eachDayMargin.putAt("Tot",totalsMap);
		}
	}
}


context.put("masterList", masterList);
context.put("routePartyMap",routePartyMap);
facRecoveryMap=[:];

facilityRecoveryResult = TransporterServices.getFacilityRecvoryForPeriodBilling(dctx,UtilMisc.toMap("periodBillingId",periodBillingId,"fromDate",monthBegin,"userLogin",userLogin));
facRecoveryMap=facilityRecoveryResult.get("facilityRecoveryInfoMap");
partyRecoveryInfoMap=facilityRecoveryResult.get("partyRecoveryInfoMap");

// for transporter SMS 
finalMap = [:];
if(UtilValidate.isNotEmpty(routeSmsMap)){
	Iterator mapIter = routeSmsMap.entrySet().iterator();
	while (mapIter.hasNext()) {
		Map.Entry entry = mapIter.next();
		 netAmount = BigDecimal.ZERO;
		 totalFine = BigDecimal.ZERO;
		 routeAmount = BigDecimal.ZERO;
		 routeId = entry.getKey();
		 routeAmount = entry.getValue();
		 if(UtilValidate.isNotEmpty(facRecoveryMap.get(routeId))){
			 facilityRecvry = facRecoveryMap.get(routeId);
			 if(UtilValidate.isNotEmpty(facilityRecvry.totalFine)){
				 totalFine = facilityRecvry.totalFine;
				 netAmount = (routeAmount-totalFine);
			 }
		 }else{
		 	netAmount = routeAmount;
		 }
		 if(netAmount!=0){
			 tempMap = [:];
			 tempMap["routeAmount"] =new BigDecimal(routeAmount).setScale(2,BigDecimal.ROUND_HALF_UP) ;
			 tempMap["totalFine"] =new BigDecimal(totalFine).setScale(2,BigDecimal.ROUND_HALF_UP) ; 
			 tempMap["netAmount"] = new BigDecimal(netAmount).setScale(0,BigDecimal.ROUND_HALF_UP) ;
			 tempTempMap = [:];
			 tempTempMap.putAll(tempMap);
			 finalMap.put(routeId,tempTempMap);
		 }
	}
}
context.put("finalMap",finalMap);
//facilityRecoveryResultRes = TransporterServices.getTransporterTotalsForPeriodBilling(dctx,UtilMisc.toMap("periodBillingId",periodBillingId));
//Debug.log("=====partyTradingMap===="+facilityRecoveryResultRes.get("partyTradingMap"));
context.put("facilityRecoveryInfoMap", facRecoveryMap);
context.put("partyFacilityMap", partyFacilityMap);
