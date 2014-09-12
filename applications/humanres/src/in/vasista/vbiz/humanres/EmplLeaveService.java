package in.vasista.vbiz.humanres;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;


import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.accounting.invoice.InvoiceWorker;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.TimeDuration;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilGenerics;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.party.party.PartyWorker;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;
import org.ofbiz.base.util.StringUtil;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityUtil;


public class EmplLeaveService {
    public static final String module = EmplLeaveService.class.getName();
	
	public static Map<String, Object> getEmployeeLeaveBalance(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Delegator delegator = dctx.getDelegator();

        Map<String, Object> result = FastMap.newInstance();
        String employeeId = (String) context.get("employeeId");
        try{		
        	List conditionList = UtilMisc.toList(
				EntityCondition.makeCondition("billingTypeId", EntityOperator.EQUALS ,"PAYROLL_BILL"));		
        	conditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS ,"APPROVED"));
        	EntityCondition condition = EntityCondition.makeCondition(conditionList,EntityOperator.AND);
        	List<GenericValue> periodBillingList = delegator.findList("PeriodBilling", condition, null, null, null, false);

        	List periodIds = EntityUtil.getFieldListFromEntityList(periodBillingList, "customTimePeriodId", true);
        	conditionList.clear();
        	conditionList.add(EntityCondition.makeCondition("customTimePeriodId", EntityOperator.IN, periodIds));
        	condition = EntityCondition.makeCondition(conditionList, EntityOperator.AND);
        	List customTimePeriods = delegator.findList("CustomTimePeriod", condition, null, UtilMisc.toList("-thruDate"), null, true);
        	if (customTimePeriods != null && customTimePeriods.size() > 0) {
        		GenericValue latestHRPeriod = EntityUtil.getFirst(customTimePeriods);
        		result.put("leaveBalanceDate", latestHRPeriod.get("fromDate"));
        		//result.put("leaveBalanceDateTime", UtilDateTime.(latestHRPeriod.get("fromDate")));
                Map<String, Object> leaveBalancesMap = FastMap.newInstance();
            	conditionList.clear();
            	conditionList.add(EntityCondition.makeCondition("customTimePeriodId", EntityOperator.EQUALS, latestHRPeriod.getString("customTimePeriodId")));
            	conditionList.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, employeeId));
            	if(UtilValidate.isNotEmpty(context.get("leaveTypeId"))){
            		conditionList.add(EntityCondition.makeCondition("leaveTypeId", EntityOperator.EQUALS, context.get("leaveTypeId")));
            	}
            	condition = EntityCondition.makeCondition(conditionList, EntityOperator.AND);
	            List<GenericValue> leaveBalances = delegator.findList("EmplLeaveBalanceStatus", condition, null, null, null, false);
				for (int i = 0; i < leaveBalances.size(); ++i) {		
					GenericValue leaveBalance = leaveBalances.get(i);
					String leaveTypeId = leaveBalance.getString("leaveTypeId");
					BigDecimal openingBalance = BigDecimal.ZERO;					
					BigDecimal closingBalance = BigDecimal.ZERO;
					
					if (UtilValidate.isNotEmpty(leaveBalance.getBigDecimal("openingBalance"))) {
						openingBalance = leaveBalance.getBigDecimal("openingBalance");
						closingBalance = closingBalance.add(openingBalance);
					}
					if (UtilValidate.isNotEmpty(leaveBalance.getBigDecimal("allotedDays"))) {
						closingBalance = closingBalance.add(leaveBalance.getBigDecimal("allotedDays"));
					}
					if (UtilValidate.isNotEmpty(leaveBalance.getBigDecimal("availedDays"))) {
						closingBalance = closingBalance.subtract(leaveBalance.getBigDecimal("availedDays"));
					}	
					if (UtilValidate.isNotEmpty(leaveBalance.getBigDecimal("encashedDays"))) {
						closingBalance = closingBalance.subtract(leaveBalance.getBigDecimal("encashedDays"));
					}
					if (UtilValidate.isNotEmpty(leaveBalance.getBigDecimal("lapsedDays"))) {
						closingBalance = closingBalance.subtract(leaveBalance.getBigDecimal("lapsedDays"));
					}
					if (UtilValidate.isNotEmpty(leaveBalance.getBigDecimal("adjustedDays"))) {
						closingBalance = closingBalance.add(leaveBalance.getBigDecimal("adjustedDays"));
					}
					if(closingBalance.compareTo(BigDecimal.ZERO) != 0){
						Map leaveCtx = FastMap.newInstance();
						leaveCtx.put("timePeriodStart", UtilDateTime.toTimestamp(latestHRPeriod.getDate("fromDate")));
						leaveCtx.put("partyId", employeeId);
						leaveCtx.put("leaveTypeId", leaveTypeId);
						Map leaveResult = fetchLeaveDaysForPeriod(dctx,leaveCtx);
						if(!ServiceUtil.isError(leaveResult)){
							result.put("leaveBalanceDate", latestHRPeriod.get("thruDate"));
							Map leaveDetailmap = (Map)leaveResult.get("leaveDetailmap");
							if(UtilValidate.isNotEmpty(leaveDetailmap)){
								closingBalance = closingBalance.subtract((BigDecimal)leaveDetailmap.get(leaveTypeId));
							}
						}
						
					}
					leaveBalancesMap.put(leaveTypeId, closingBalance);
				}
				result.put("leaveBalances", leaveBalancesMap);
				//this is to return date for json request
				result.put("leaveBalanceDateStr",UtilDateTime.toDateString((java.sql.Date)result.get("leaveBalanceDate"),"dd-MM-yyyy"));
        		
        	}
        } catch (Exception e) {
        	Debug.logError(e, "Error fetching leaves", module);
        	return ServiceUtil.returnError(e.toString());
        }
        //Debug.log("result============"+result);
        return result;        
	}
	
	public static Map<String, Object> fetchLeaveDaysForPeriod(DispatchContext dctx, Map<String, Object> context) {
				Delegator delegator = dctx.getDelegator();
				LocalDispatcher dispatcher = dctx.getDispatcher();
				String errorMsg = "fetchLeaveDaysForPeriod failed";
				String partyId = (String) context.get("partyId");	
				GenericValue userLogin = (GenericValue) context.get("userLogin");
				Timestamp timePeriodStart = (Timestamp)context.get("timePeriodStart");
				Timestamp timePeriodEnd = (Timestamp)context.get("timePeriodEnd");
				String leaveTypeId = (String)context.get("leaveTypeId");
				Map<String, Object> serviceResults = ServiceUtil.returnSuccess();
				BigDecimal noOfLeaveDays = BigDecimal.ZERO;
				Map leaveDetailmap = FastMap.newInstance();
				BigDecimal lossOfPayDays = BigDecimal.ZERO;
				try{
					
					List conditionList = UtilMisc.toList(
			            EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId));
					if(UtilValidate.isNotEmpty(leaveTypeId)){
						conditionList.add(EntityCondition.makeCondition("leaveTypeId", EntityOperator.EQUALS, leaveTypeId));
					}
					conditionList.add(EntityCondition.makeCondition("leaveStatus", EntityOperator.EQUALS, "LEAVE_APPROVED"));
					if(UtilValidate.isNotEmpty(timePeriodEnd)){
						conditionList.add(EntityCondition.makeCondition("fromDate", EntityOperator.LESS_THAN_EQUAL_TO, timePeriodEnd));
					}
					
					conditionList.add(EntityCondition.makeCondition(EntityCondition.makeCondition("thruDate", EntityOperator.EQUALS, null), EntityOperator.OR, 
			    	EntityCondition.makeCondition("thruDate", EntityOperator.GREATER_THAN_EQUAL_TO, timePeriodStart)));
					EntityCondition condition = EntityCondition.makeCondition(conditionList, EntityOperator.AND);  		
					List<GenericValue> leaves = delegator.findList("EmplLeave", condition, null, null, null, false);
					serviceResults.put("leaves", leaves);
					for (int i = 0; i < leaves.size(); ++i) {		
						GenericValue leave = leaves.get(i);
						String leaveType =leave.getString("leaveTypeId");
			            Timestamp from = leave.getTimestamp("fromDate");
			            Timestamp thru = UtilDateTime.getDayEnd(leave.getTimestamp("thruDate"));			
						if ( UtilValidate.isNotEmpty(timePeriodEnd) && (from.compareTo(timePeriodStart) < 0 || thru.compareTo(timePeriodEnd) > 0)) {
							Debug.logError("leave entry ========"+leave, module);
							Debug.logError( errorMsg + ": leave cannot span multiple payroll periods ,employeeId:"+partyId, module);
			            	/*return ServiceUtil.returnError(errorMsg + ": leave cannot span multiple payroll periods, employeeId:"+partyId, 
			            			null, null, null);	*/			
					 	}
						int intv = (UtilDateTime.getIntervalInDays(from, thru)+1);
						BigDecimal temp = new BigDecimal(intv);
						if(UtilValidate.isNotEmpty(leave.getString("dayFractionId"))){
							temp = temp.divide(new BigDecimal(2), 1, BigDecimal.ROUND_HALF_UP);
						}
						
						if(UtilValidate.isEmpty(leaveDetailmap.get(leaveType))){
							leaveDetailmap.put(leaveType,BigDecimal.ZERO);
						}
						if(UtilValidate.isNotEmpty(leave.getBigDecimal("lossOfPayDays"))){
							lossOfPayDays = lossOfPayDays.add(leave.getBigDecimal("lossOfPayDays"));
						}
						leaveDetailmap.put(leaveType, ((BigDecimal)leaveDetailmap.get(leaveType)).add(temp));
						noOfLeaveDays = noOfLeaveDays.add(temp);
					} 
				}catch(Exception e){
					Debug.logError(e, "Error fetching leave days", module);
		        	return ServiceUtil.returnError(e.toString());
				}
				
				serviceResults.put("lossOfPayDays", lossOfPayDays);
				serviceResults.put("leaveDetailmap", leaveDetailmap);
				serviceResults.put("noOfLeaveDays", noOfLeaveDays);
		        return serviceResults; 
			}
	
	public static Map<String, Object> updateGhSsAvailedDetails(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();    	
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String partyId =  (String)context.get("partyId");
        List chDateStringList =  (List)context.get("chDate");
        String emplLeaveApplId =  (String)context.get("emplLeaveApplId");
        String leaveStatus =  (String)context.get("leaveStatus");
        String serviceName = (String)context.get("serviceName");
		List<GenericValue> holiDayList = FastList.newInstance(); 
		Map result = FastMap.newInstance();
		List<Date> chDateList = FastList.newInstance();
		try {
				List<Date> holidays = FastList.newInstance();
				
				Debug.log("context======="+context);
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
				if(UtilValidate.isNotEmpty(chDateStringList)){
					for(int i=0;i< chDateStringList.size();i++){
						String chDate = (String)chDateStringList.get(i);
						chDateList.add(UtilDateTime.toSqlDate(sdf.parse(chDate)));
					}
				}
				
				List conList=UtilMisc.toList(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS,partyId));
				
				if(UtilValidate.isNotEmpty(emplLeaveApplId) && UtilValidate.isEmpty(chDateList)){
					conList.add(EntityCondition.makeCondition("emplLeaveApplId",EntityOperator.EQUALS,emplLeaveApplId));
				}else{
					
					conList.add(EntityCondition.makeCondition("date",EntityOperator.IN,chDateList));
				}
				
				EntityCondition con= EntityCondition.makeCondition(conList,EntityOperator.AND);
				List<GenericValue> tempWorkedHolidaysList = delegator.findList("EmplDailyAttendanceDetail", con ,null,UtilMisc.toList("date" ,"partyId"), null, false );
				for(GenericValue workedHoliday : tempWorkedHolidaysList){
					if((UtilValidate.isNotEmpty(leaveStatus) && leaveStatus.equals("LEAVE_REJECTED")) || (UtilValidate.isNotEmpty(serviceName) && serviceName.equals("deleteEmplLeave"))){
						workedHoliday.set("encashmentStatus", null);
						workedHoliday.set("emplLeaveApplId", null);
						
					}else{
						
						workedHoliday.set("encashmentStatus", "LEAVE_ENCASHMENT");
						workedHoliday.set("emplLeaveApplId", emplLeaveApplId);
					}
					
					workedHoliday.store();
				}
		}catch(Exception e){
  			Debug.logError("Error while updating EmplDailyAttendanceDetail " + e.getMessage(), module);
  			return ServiceUtil.returnError("Error while updating EmplDailyAttendanceDetail ");
  		}
    	
    	//Debug.log("result:" + result, module);		 
    	return result;
    }
	
	public static Map<String, Object> getEmployLeaveValidStatusChange(DispatchContext ctx, Map<String, ? extends Object> context) {
    	Delegator delegator = ctx.getDelegator();
    	GenericValue userLogin = (GenericValue) context.get("userLogin");
    	Map<String, Object> result = ServiceUtil.returnSuccess();
        String leaveTypeId = (String)context.get("leaveTypeId");
        String statusId = (String)context.get("leaveStatus");
		String approveLevels = null;
		List validStatusChangeList = FastList.newInstance();
		try {
			GenericValue emplLeaveTypeDetails = delegator.findOne("EmplLeaveType",UtilMisc.toMap("leaveTypeId", leaveTypeId), true);
			if(UtilValidate.isNotEmpty(emplLeaveTypeDetails)){
				approveLevels = (String) emplLeaveTypeDetails.get("approveLevels");
			}
			if(UtilValidate.isEmpty(approveLevels)){
				approveLevels = "01";
			}
			List conditionList = FastList.newInstance();
			conditionList.add(EntityCondition.makeCondition("statusTypeId", EntityOperator.EQUALS ,"LEAVE_STATUS"));
			conditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS ,statusId));
     		EntityCondition condition = EntityCondition.makeCondition(conditionList,EntityOperator.AND);
     		List<GenericValue> StatusValidChangeList = delegator.findList("StatusValidChangeToDetail", condition, null,null, null, false);
     		for(GenericValue statusValidChange:StatusValidChangeList){
				String conditionExpression = statusValidChange.getString("conditionExpression");
				List<String> condExpSplitStr = StringUtil.split(conditionExpression, "|");
				if(condExpSplitStr.contains(approveLevels)){
					validStatusChangeList.add(statusValidChange);
				}
     		}
		}catch(Exception e){
  			Debug.logError("Error while retrieving status list" + e.getMessage(), module);
  			return ServiceUtil.returnError("Error while getting Employee Leave valid status");
  		}
		result.put("validStatusChangeList",validStatusChangeList);
        return result;
    }
	
	
	public static Map<String, Object> updateEmplLeaveStatus(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();    	
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String userLoginId = (String)userLogin.get("userLoginId");
        String emplLeaveApplId =  (String)context.get("emplLeaveApplId");
        String leaveStatus =  (String)context.get("leaveStatus");
        String approverPartyId = (String)context.get("approverPartyId");
        String leaveTypeId = (String)context.get("leaveTypeId");
        Map<String, Object> result = ServiceUtil.returnSuccess(" "+ leaveStatus + " Sucessfully..!");
		GenericValue emplLeaveDetails = null;
		try {
			if(UtilValidate.isEmpty(leaveStatus.trim())){
				return ServiceUtil.returnError("Leave Status Cannot be Empty "); 
			}
			if(UtilValidate.isEmpty(approverPartyId)){
				return ServiceUtil.returnError("Approver Party Cannot be Empty "); 
			}
			emplLeaveDetails = delegator.findOne("EmplLeave",UtilMisc.toMap("emplLeaveApplId", emplLeaveApplId), false);
			if(UtilValidate.isNotEmpty(emplLeaveDetails)){
				emplLeaveDetails.set("leaveStatus", leaveStatus);
				emplLeaveDetails.set("approverPartyId", approverPartyId);
				emplLeaveDetails.store();
			}
		}catch(Exception e){
  			Debug.logError("Error while updating emplLeaveStatus " + e.getMessage(), module);
  			return ServiceUtil.returnError("Error while updating EmplLeaveStatus ");
  		}
		result.put("emplLeaveApplId",emplLeaveDetails.getString("emplLeaveApplId"));
		result.put("partyId",emplLeaveDetails.getString("partyId"));
		result.put("leaveTypeId",emplLeaveDetails.getString("leaveTypeId"));
		result.put("emplLeaveReasonTypeId",emplLeaveDetails.getString("emplLeaveReasonTypeId"));
		result.put("fromDate",emplLeaveDetails.getTimestamp("fromDate"));
		result.put("thruDate",emplLeaveDetails.getTimestamp("thruDate"));
		result.put("effectedCreditDays",emplLeaveDetails.getBigDecimal("effectedCreditDays"));
		result.put("dayFractionId",emplLeaveDetails.getString("dayFractionId"));
		result.put("appliedBy",emplLeaveDetails.getString("appliedBy"));
		result.put("lossOfPayDays",emplLeaveDetails.getBigDecimal("lossOfPayDays"));
		result.put("approverPartyId",emplLeaveDetails.getString("approverPartyId"));
		result.put("leaveStatus",emplLeaveDetails.getString("leaveStatus"));
		result.put("documentsProduced",emplLeaveDetails.getString("documentsProduced"));
		result.put("description",emplLeaveDetails.getString("description"));
		result.put("comment",emplLeaveDetails.getString("comment"));
    	return result;
    }
	
	
	public static Map<String, Object> storeEmplLeaveStatus(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();    	
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String userLoginId = (String)userLogin.get("userLoginId");
        String emplLeaveApplId =  (String)context.get("emplLeaveApplId");
        String leaveStatus =  (String)context.get("leaveStatus");
        String approverPartyId = (String)context.get("approverPartyId");
		Map result = FastMap.newInstance();
		GenericValue emplLeaveStatusDetails = null; 
		try {
			emplLeaveStatusDetails = delegator.findOne("EmplLeaveStatus",UtilMisc.toMap("emplLeaveApplId", emplLeaveApplId,"leaveStatus",leaveStatus), true);
			GenericValue emplLeaveStatus = delegator.makeValue("EmplLeaveStatus");
		    emplLeaveStatus.set("emplLeaveApplId", emplLeaveApplId );
		    emplLeaveStatus.set("leaveStatus", leaveStatus);
		    emplLeaveStatus.set("approverPartyId", approverPartyId);
		    emplLeaveStatus.set("changedByUserLogin", userLoginId); 
		    emplLeaveStatus.set("changedDate", UtilDateTime.nowTimestamp());
		    delegator.createOrStore(emplLeaveStatus);    
		}catch(Exception e){
  			Debug.logError("Error while updating emplLeaveStatus " + e.getMessage(), module);
  			return ServiceUtil.returnError("Error while updating EmplLeaveStatus ");
  		}
    	return result;
    }
	
}
