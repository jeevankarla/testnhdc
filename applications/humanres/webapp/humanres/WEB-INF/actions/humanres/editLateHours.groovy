import org.ofbiz.base.util.*;
import org.ofbiz.entity.condition.*;
import org.ofbiz.entity.util.EntityUtil;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.swing.RowFilter.NotFilter;

import org.ofbiz.base.util.UtilDateTime;
import net.sf.json.JSONObject;
import net.sf.json.JSONArray;
import in.vasista.vbiz.humanres.PayrollService;
import in.vasista.vbiz.humanres.HumanresService;
import in.vasista.vbiz.byproducts.ByProductServices;

dctx = dispatcher.getDispatchContext();
emplList=[];
holidaysList=[];
workedHolidaysList=[];
List EncashmentList=[];
employments=[];
holidays=[];
empIds=[];
empName=[:];
orderDate=UtilDateTime.nowTimestamp();
context.orderDate=orderDate;
partyId=parameters.partyId;
timePeriodId=parameters.customTimePeriodId;
employeeList = [];
internalOrgs=[];
context.internalOrgs=internalOrgs;
context.employeeList=employeeList;
company = delegator.findByPrimaryKey("PartyAndGroup", [partyId : "Company"]);
populateChildren(company, employeeList);
def populateChildren(org, employeeList) {
	EmploymentsMap=HumanresService.getActiveEmployements(dctx,[userLogin:userLogin,orgPartyId:"company"]);
	employments=EmploymentsMap.get("employementList");
	employments.each{ employment->
		empIds.add(employment.partyId);
		String lastName="";
		if(employment.lastName!=null){
			lastName=employment.lastName;
		}
		name=employment.firstName+" "+lastName;
		empName.put(employment.partyId,name);
		
	}
}
if(UtilValidate.isNotEmpty(timePeriodId)){
	context.timePeriodId=timePeriodId;
		dates=delegator.findOne("CustomTimePeriod", [customTimePeriodId:timePeriodId], false);
		fromDate=UtilDateTime.toDateString(dates.get("fromDate"), "MMM dd, yyyy");
		thruDate=UtilDateTime.toDateString(dates.get("thruDate"), "MMM dd, yyyy");
		
		fromDateStart = UtilDateTime.getDayStart(UtilDateTime.nowTimestamp());
		thruDateEnd= UtilDateTime.getDayEnd(UtilDateTime.nowTimestamp());
		def sdf = new SimpleDateFormat("MMMM dd, yyyy");
		try {
			if (fromDate) {
				fromDateStart = UtilDateTime.getDayStart(new java.sql.Timestamp(sdf.parse(fromDate).getTime()));
			}
			if (thruDate) {
				thruDateEnd = UtilDateTime.getDayEnd(new java.sql.Timestamp(sdf.parse(thruDate).getTime()));
			}
		} catch (ParseException e) {
		Debug.logError(e, "Cannot parse date string: " + e, "");
		context.errorMessage = "Cannot parse date string: " + e;
		return;
		}
		context.fromlarDate=UtilDateTime.toDateString(fromDateStart,"dd-MM-yyyy");
		context.thrularDate=UtilDateTime.toDateString(thruDateEnd,"dd-MM-yyyy");
	partyId=parameters.partyId;
	
	resultMap=PayrollService.getPayrollAttedancePeriod(dctx,[userLogin:userLogin,timePeriodStart:fromDateStart,timePeriodEnd:thruDateEnd,timePeriodId:timePeriodId,locale:locale]);
	lastClosePeriod=resultMap.get("lastCloseAttedancePeriod");
	if(UtilValidate.isNotEmpty(lastClosePeriod)){
		customTimePeriodId=lastClosePeriod.get("customTimePeriodId");
		context.customTimePeriodId=customTimePeriodId;
		attenDates=delegator.findOne("CustomTimePeriod", [customTimePeriodId:customTimePeriodId], false);
		fromDate=UtilDateTime.toDateString(attenDates.get("fromDate"), "MMM dd, yyyy");
		thruDate=UtilDateTime.toDateString(attenDates.get("thruDate"), "MMM dd, yyyy");
		fromDateStart = UtilDateTime.getDayStart(UtilDateTime.nowTimestamp());
		thruDateEnd= UtilDateTime.getDayEnd(UtilDateTime.nowTimestamp());
		def sdfn = new SimpleDateFormat("MMMM dd, yyyy");
		try {
			if (fromDate) {
				fromDateStart = UtilDateTime.getDayStart(new java.sql.Timestamp(sdfn.parse(fromDate).getTime()));
			}
			if (thruDate) {
				thruDateEnd = UtilDateTime.getDayEnd(new java.sql.Timestamp(sdfn.parse(thruDate).getTime()));
			}
		} catch (ParseException e) {
		Debug.logError(e, "Cannot parse date string: " + e, "");
		context.errorMessage = "Cannot parse date string: " + e;
		return;
		}
		List conditionList=[];
		conditionList.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS,partyId));
		conditionList.add(EntityCondition.makeCondition("date",EntityOperator.GREATER_THAN_EQUAL_TO,UtilDateTime.toSqlDate(fromDateStart)));
		conditionList.add(EntityCondition.makeCondition("date",EntityOperator.LESS_THAN_EQUAL_TO,UtilDateTime.toSqlDate(thruDateEnd)));
		condition=EntityCondition.makeCondition(conditionList,EntityOperator.AND);
		emplList = delegator.findList("EmplDailyAttendanceDetail", condition ,null,null, null, false );
		
		//Approval Holiday
		List holidayconditionList=[];
		holidayconditionList.add(EntityCondition.makeCondition("holiDayDate",EntityOperator.GREATER_THAN_EQUAL_TO,fromDateStart));
		holidayconditionList.add(EntityCondition.makeCondition("holiDayDate",EntityOperator.LESS_THAN_EQUAL_TO,thruDateEnd));
		holidaycondition=EntityCondition.makeCondition(holidayconditionList,EntityOperator.AND);
		holidaysList = delegator.findList("HolidayCalendar", holidaycondition ,null,null, null, false );
		
		secondSaturDay = UtilDateTime.addDaysToTimestamp(UtilDateTime.getWeekStart(UtilDateTime.getMonthStart(thruDateEnd),0,2,timeZone,locale), -1);
		if(UtilValidate.isNotEmpty(holidaysList)){
			holidaysList.each{ days ->
				holidays.add(UtilDateTime.toSqlDate(days.get("holiDayDate")));
			}
		}
		holidays.add(UtilDateTime.toSqlDate(secondSaturDay));
		List conList=[];
		conList.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS,partyId));
		conList.add(EntityCondition.makeCondition("date",EntityOperator.IN,holidays));
		con=EntityCondition.makeCondition(conList,EntityOperator.AND);
		workedHolidaysList = delegator.findList("EmplDailyAttendanceDetail", con ,null,null, null, false );
		
		
		//GH and SS Encashment
		
		List conGHSSList=[];
		conGHSSList.add(EntityCondition.makeCondition("partyId", EntityOperator.IN,empIds));
		conGHSSList.add(EntityCondition.makeCondition("date",EntityOperator.IN,holidays));
		conGHSSList.add(EntityCondition.makeCondition("encashmentStatus",EntityOperator.EQUALS,"CASH_ENCASHMENT"));
		conGHSS=EntityCondition.makeCondition(conGHSSList,EntityOperator.AND);
		workedGHSSList = delegator.findList("EmplDailyAttendanceDetail", conGHSS ,UtilMisc.toSet("partyId","date","shiftType","encashmentStatus"),null, null, false );
		leaveType="";
		workedGHSSList.each{ workedGHSS->
			employee=[:];
			if(workedGHSS.date==UtilDateTime.toSqlDate(secondSaturDay))
				leaveType="SS";
			else
				leaveType="GH";
				
			employee.put("partyId",workedGHSS.partyId);
			employee.put("date",workedGHSS.date);
			employee.put("leaveType",leaveType);
			employee.put("name",empName.get(workedGHSS.partyId));
			employee.put("encashmentStatus",workedGHSS.encashmentStatus);
			employee.put("shiftType",workedGHSS.shiftType);
			EncashmentList.add(employee);
			
		}
	}
}

context.emplList=emplList;
context.holidaysList=workedHolidaysList;
context.EncashmentList=EncashmentList;
