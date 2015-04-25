import org.apache.derby.impl.sql.compile.OrderByColumn;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.condition.*;
import org.ofbiz.entity.util.EntityUtil;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.ofbiz.base.util.UtilDateTime;

import freemarker.core.SequenceBuiltins.sort_byBI;
import in.vasista.vbiz.humanres.EmplLeaveService;
import in.vasista.vbiz.humanres.PayrollService;
import in.vasista.vbiz.humanres.HumanresApiService;
import in.vasista.vbiz.humanres.HumanresService;
import org.ofbiz.party.party.PartyHelper;
import javolution.util.FastList;
import javolution.util.FastMap;

dctx = dispatcher.getDispatchContext();
orderDate=UtilDateTime.nowTimestamp();
context.orderDate=orderDate;
timePeriodId=parameters.customTimePeriodId;
employeeId=parameters.employeeId;
fromDate = UtilDateTime.getDayStart(UtilDateTime.nowTimestamp());
thruDate = UtilDateTime.getDayEnd(UtilDateTime.nowTimestamp());

condList=[];
condList.add(EntityCondition.makeCondition("customTimePeriodId",EntityOperator.EQUALS,timePeriodId));
condList.add(EntityCondition.makeCondition("periodTypeId",EntityOperator.EQUALS,"HR_LEAVEENCASH"));
cond=EntityCondition.makeCondition(condList,EntityOperator.AND);
dateList=delegator.findList("CustomTimePeriod",cond,null,null,null,false);
if(dateList){
	dates=EntityUtil.getFirst(dateList);
	fromDate=UtilDateTime.toTimestamp(dates.get("fromDate"));
	thruDate=UtilDateTime.toTimestamp(dates.get("thruDate"));
}

context.fromlerDate=UtilDateTime.toDateString(fromDate,"dd-MMM-yy");
context.thrulerDate=UtilDateTime.toDateString(thruDate,"dd-MMM-yy");
periodBillingId="";
basicSalDate=UtilDateTime.getDayStart(UtilDateTime.nowTimestamp());
conList=[];
conList.add(EntityCondition.makeCondition("customTimePeriodId",EntityOperator.EQUALS,timePeriodId));
conList.add(EntityCondition.makeCondition("billingTypeId",EntityOperator.EQUALS,"SP_LEAVE_ENCASH"));
conList.add(EntityCondition.makeCondition("statusId",EntityOperator.IN,UtilMisc.toList("GENERATED","APPROVED")));
con=EntityCondition.makeCondition(conList,EntityOperator.AND);
periodList=delegator.findList("PeriodBilling",con,null,["-basicSalDate"],null,false);
//periodBillingIds=EntityUtil.getFieldListFromEntityList(periodList,"periodBillingId",true);

periodList = EntityUtil.getFirst(periodList);
periodBillingIds = periodList.get("periodBillingId");

periodBillingMap=[:];
if(periodList){
	periodBillingId=periodList.get("periodBillingId");
	basicSalDate=periodList.get("basicSalDate");
	if(UtilValidate.isEmpty(periodBillingMap[periodBillingId])){
		periodBillingMap[periodBillingId]=basicSalDate;
	}
}

employementIds=[];
conditionList=[];
conditionList.add(EntityCondition.makeCondition("periodBillingId",EntityOperator.EQUALS,periodBillingIds));
conditionList.add(EntityCondition.makeCondition("partyId",EntityOperator.EQUALS,"Company"));
 if(UtilValidate.isNotEmpty(employeeId))
 {
   conditionList.add(EntityCondition.makeCondition("partyIdFrom",EntityOperator.EQUALS,employeeId));
 }
condition=EntityCondition.makeCondition(conditionList,EntityOperator.AND);
payrollHeaderList=delegator.findList("PayrollHeader",condition,null,null,null,false);
employementIds = EntityUtil.getFieldListFromEntityList(payrollHeaderList, "partyIdFrom", true);

/*emplInputMap = [:];
emplInputMap.put("userLogin", userLogin);
emplInputMap.put("orgPartyId", "Company");
emplInputMap.put("fromDate", fromDate);
emplInputMap.put("thruDate", thruDate);
Map EmploymentsMap = HumanresService.getActiveEmployements(dctx,emplInputMap);
List<GenericValue> employementList = (List<GenericValue>)EmploymentsMap.get("employementList");
employementList = EntityUtil.orderBy(employementList, UtilMisc.toList("partyIdTo"));
*/finalList=[];
emplbalList=[];
emplResult=[:];

if(UtilValidate.isNotEmpty(payrollHeaderList)){
	payrollHeaderList.each{payrolHead->
	     //if(employementIds.contains(employment.partyId)){
	        employee=[:];
			employee["partyId"]="";
			//name
			partyName=PartyHelper.getPartyName(delegator, payrolHead.partyIdFrom, false);
			/*if(employment.lastName!=null){
				lastName=employment.lastName;
			}*/
			//possition
			employeePosition = "";
			emplPositionAndFulfillments = EntityUtil.filterByDate(delegator.findByAnd("EmplPositionAndFulfillment", ["employeePartyId" : payrolHead.partyIdFrom]));
			emplPositionAndFulfillment = EntityUtil.getFirst(emplPositionAndFulfillments);
			if(UtilValidate.isNotEmpty(emplPositionAndFulfillment) && emplPositionAndFulfillment.getString("emplPositionTypeId") != null){
				emplPositionType = delegator.findOne("EmplPositionType",[emplPositionTypeId : emplPositionAndFulfillment.getString("emplPositionTypeId")], true);
				if (emplPositionType != null) {
					employeePosition = emplPositionType.getString("description");
				}
				else {
					employeePosition = emplPositionAndFulfillment.getString("emplPositionId");
				}
			}
			
			//Leave Balance
			int appDays=15;
			String leaveTypeId="EL";
			billingId=payrolHead.periodBillingId;
			int balance=0;
			emplbalList.add(payrolHead.partyIdFrom)
			inputMap = [:];
			inputMap.put("balanceDate", UtilDateTime.toSqlDate(periodBillingMap.get(billingId)));
			inputMap.put("employeeId", payrolHead.partyIdFrom);
			inputMap.put("leaveTypeId", leaveTypeId);
			Map EmplLeaveBalanceMap = EmplLeaveService.getEmployeeLeaveBalance(dctx,inputMap);
			if(UtilValidate.isNotEmpty(EmplLeaveBalanceMap.get("leaveBalances").get(leaveTypeId))){
				balance=EmplLeaveBalanceMap.get("leaveBalances").get(leaveTypeId);
			}
			
		     for (each in emplbalList) {
				int i=Collections.frequency(emplbalList,each)
				if(i>1){
					balance=balance-appDays;
				}
				break;
			}
            employee.put("balance", balance);
			employee.put("name", partyName);
			employee.put("partyId",payrolHead.partyIdFrom);
			employee.put("position", employeePosition);
			employee.put("appDays", appDays);
			employee.put("leaveTypeId", leaveTypeId);
			employee.put("appDate", UtilDateTime.toDateString(periodBillingMap.get(billingId),"dd-MMM-yy"));
			
			finalList.add(employee);
	   }
}
if(UtilValidate.isEmpty(finalList)){
	Debug.logError("No Leaves Found.","");
	context.errorMessage = "No Leaves Found.......!";
	return;
}
context.finalList=finalList;
