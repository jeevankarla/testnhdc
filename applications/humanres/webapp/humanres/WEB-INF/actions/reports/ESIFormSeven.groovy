import org.apache.avalon.framework.parameters.Parameters;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.condition.*;
import org.ofbiz.entity.util.EntityUtil;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.ofbiz.base.util.UtilDateTime;
import net.sf.json.JSONObject;
import net.sf.json.JSONArray;
import in.vasista.vbiz.humanres.PayrollService;
import in.vasista.vbiz.humanres.HumanresService;
import in.vasista.vbiz.byproducts.ByProductServices;
import org.ofbiz.party.party.PartyHelper;


dctx = dispatcher.getDispatchContext();

fromDate = parameters.fromDate;
thruDate = parameters.thruDate;


def sdf = new SimpleDateFormat("MMMM dd,yyyy");
try {
	if (fromDate) {
		fromDate = UtilDateTime.getDayStart(new java.sql.Timestamp(sdf.parse(fromDate).getTime()));
	}
	if (thruDate) {
		thruDate = UtilDateTime.getDayEnd(new java.sql.Timestamp(sdf.parse(thruDate).getTime()));
	}
} catch (ParseException e) {
	Debug.logError(e, "Cannot parse date string: " + e, "");
	context.errorMessage = "Cannot parse date string: " + e;
	return;
}

periodList=[];
context.putAt("fromDate", fromDate);
context.putAt("thruDate", thruDate);
totalDays=UtilDateTime.getIntervalInDays(fromDate,thruDate);
totalDays=totalDays+1;

/*if(totalDays>92){
	Debug.logError("We can't select more than 3 months..","");
	context.errorMessage = "We can't select more than 3 months..";
}*/

List conList = [];
conList.add(EntityCondition.makeCondition("fromDate",EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.toSqlDate(fromDate)));
conList.add(EntityCondition.makeCondition("thruDate",EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.toSqlDate(thruDate)));
conList.add(EntityCondition.makeCondition("periodTypeId",EntityOperator.EQUALS, "HR_MONTH"));
List customTimePeriodList = delegator.findList("CustomTimePeriod",EntityCondition.makeCondition(conList,EntityOperator.AND),null,null,null,false);

PeriodIdsList=[];
if(UtilValidate.isNotEmpty(customTimePeriodList)){
	PeriodIdsList = EntityUtil.getFieldListFromEntityList(customTimePeriodList, "customTimePeriodId", true);
}
context.put("PeriodIdsList",PeriodIdsList);

emplInputMap = [:];
emplInputMap.put("userLogin", userLogin);
emplInputMap.put("orgPartyId", "Company");
emplInputMap.put("fromDate", fromDate);
emplInputMap.put("thruDate", thruDate);
Map EmploymentsMap = HumanresService.getActiveEmployements(dctx,emplInputMap);
employments=EmploymentsMap.get("employementList");
employments = UtilMisc.sortMaps(employments, UtilMisc.toList("partyIdTo"));
if(UtilValidate.isNotEmpty(employments)){
	employmentsList = EntityUtil.getFieldListFromEntityList(employments, "partyIdTo", true);
}

periodBillingIdMap=[:];
PeriodIdsList.each{ Period ->
	List periodbillingConditionList=[];
	periodbillingConditionList.add(EntityCondition.makeCondition("customTimePeriodId", EntityOperator.EQUALS, Period));
	periodbillingConditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.IN , UtilMisc.toList("GENERATED","APPROVED")));
	periodbillingCondition = EntityCondition.makeCondition(periodbillingConditionList,EntityOperator.AND);
	BillingList = delegator.findList("PeriodBilling", periodbillingCondition, null, null, null, false);
	if(UtilValidate.isNotEmpty(BillingList)){
		BillingList = EntityUtil.getFirst(BillingList);
		BillingId = BillingList.periodBillingId;
		periodBillingIdMap.put(Period,BillingId);
	}
}

EmplWiseDetailsMap=[:];
EmplNameMap=[:];
if(UtilValidate.isNotEmpty(employmentsList)){
	employmentsList.each{ employeeId ->
		customTimePeriodTotals = PayrollService.getEmployeeSalaryTotalsForPeriod(dctx,UtilMisc.toMap("partyId",employeeId,"fromDate",fromDate,"thruDate",thruDate,"userLogin",userLogin)).get("periodTotalsForParty");
		periodDetailsMap=[:];
		if(UtilValidate.isNotEmpty(customTimePeriodTotals)){
			Iterator customTimePeriodIter = customTimePeriodTotals.entrySet().iterator();
			while(customTimePeriodIter.hasNext()){
				Map.Entry customTimePeriodEntry = customTimePeriodIter.next();
				if(customTimePeriodEntry.getKey() != "customTimePeriodTotals"){
					PeriodIdsList.each{ Period ->
						if(Period.equals(customTimePeriodEntry.getKey())){
							Periodid=Period;
						}
					}
					Map periodTotalsMap = [:];
					periodTotals = customTimePeriodEntry.getValue().get("periodTotals");
					basic = 0;
					dearnessAllowance =0;
					houseRentAllowance =0;
					cityComp = 0;
					HeatAllowance=0;
					CashAllowance = 0;
					coldAllowance = 0;
					convey = 0;
					ShiftAllowance = 0;
					CanteenAllowance = 0;
					attendanceBonus =0;
					FieldAllowance = 0;
					SpecialPay = 0;
					GeneralHolidayWages = 0;
					SecondSaturdayWages = 0;
					
					personalPay =0;
					secndSatDay =0;
					shift =0;
					washing =0;
					others = 0;
					totalBenefits = 0;
					EmployeeStateInsurance = periodTotals.get("PAYROL_DD_ESI_DED");
					if(UtilValidate.isNotEmpty(EmployeeStateInsurance)){
						basic = periodTotals.get("PAYROL_BEN_SALARY");
						if(UtilValidate.isEmpty(basic)){
							basic = 0;
						}
						if(basic>=15000){
							detailsMap=[:];
							dearnessAllowance = periodTotals.get("PAYROL_BEN_DA");
							if(UtilValidate.isEmpty(dearnessAllowance)){
								dearnessAllowance = 0;
							}
							houseRentAllowance = periodTotals.get("PAYROL_BEN_HRA");
							if(UtilValidate.isEmpty(houseRentAllowance)){
								houseRentAllowance = 0;
							}
							cityComp = periodTotals.get("PAYROL_BEN_CITYCOMP");
							if(UtilValidate.isEmpty(cityComp)){
								cityComp = 0;
							}
							HeatAllowance = periodTotals.get("PAYROL_BEN_HEATALLOW");
							if(UtilValidate.isEmpty(HeatAllowance)){
								HeatAllowance = 0;
							}
							CashAllowance = periodTotals.get("PAYROL_BEN_CASH");
							if(UtilValidate.isEmpty(CashAllowance)){
								CashAllowance = 0;
							}
							coldAllowance = periodTotals.get("PAYROL_BEN_COLDALLOW");
							if(UtilValidate.isEmpty(coldAllowance)){
								coldAllowance = 0;
							}
							convey = periodTotals.get("PAYROL_BEN_CONVEY");
							if(UtilValidate.isEmpty(convey)){
								convey = 0;
							}
							ShiftAllowance = periodTotals.get("PAYROL_BEN_SHIFT");
							if(UtilValidate.isEmpty(ShiftAllowance)){
								ShiftAllowance = 0;
							}
							CanteenAllowance = periodTotals.get("PAYROL_BEN_CANTN");
							if(UtilValidate.isEmpty(CanteenAllowance)){
								CanteenAllowance = 0;
							}
							AttendanceBonus = periodTotals.get("PAYROL_BEN_ATNDBON");
							if(UtilValidate.isEmpty(AttendanceBonus)){
								AttendanceBonus = 0;
							}
							FieldAllowance = periodTotals.get("PAYROL_BEN_FIELD");
							if(UtilValidate.isEmpty(FieldAllowance)){
								FieldAllowance = 0;
							}
							SpecialPay = periodTotals.get("PAYROL_BEN_SPELPAY");
							if(UtilValidate.isEmpty(SpecialPay)){
								SpecialPay = 0;
							}
							GeneralHolidayWages = periodTotals.get("PAYROL_BEN_GEN_HOL_W");
							if(UtilValidate.isEmpty(GeneralHolidayWages)){
								GeneralHolidayWages = 0;
							}
							SecondSaturdayWages = periodTotals.get("PAYROL_BEN_SECSATDAY");
							if(UtilValidate.isEmpty(SecondSaturdayWages)){
								SecondSaturdayWages = 0;
							}
							EmployeeStateInsurance = periodTotals.get("PAYROL_DD_ESI");
							if(UtilValidate.isEmpty(EmployeeStateInsurance)){
								MedicalAllowance = periodTotals.get("PAYROL_BEN_MED_ALLOW");
								if(UtilValidate.isEmpty(MedicalAllowance)){
									MedicalAllowance = 0;
								}
							}
							others = basic+dearnessAllowance+houseRentAllowance+cityComp+HeatAllowance+CashAllowance+coldAllowance+convey+ShiftAllowance;
							Wages = others+CanteenAllowance+attendanceBonus+FieldAllowance+SpecialPay+GeneralHolidayWages+SecondSaturdayWages+MedicalAllowance;
							
							
							if(UtilValidate.isNotEmpty(periodBillingIdMap)){
								Iterator periodBillingIdMapIter = periodBillingIdMap.entrySet().iterator();
								while(periodBillingIdMapIter.hasNext()){
									Map.Entry periodBillingIdMapEntry = periodBillingIdMapIter.next();
									if((periodBillingIdMapEntry.getKey()).equals(Periodid)){
										periodBillingId=periodBillingIdMapEntry.getValue();
									}
								}
							}
							List headerConditionList=[];
							headerConditionList.add(EntityCondition.makeCondition("periodBillingId", EntityOperator.EQUALS, periodBillingId));
							headerConditionList.add(EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, employeeId));
							headerCondition = EntityCondition.makeCondition(headerConditionList,EntityOperator.AND);
							headerIdsList = delegator.findList("PayrollHeader", headerCondition, null, null, null, false);
							if(UtilValidate.isNotEmpty(headerIdsList)){
								headerIdsList = EntityUtil.getFirst(headerIdsList);
								headerId = headerIdsList.payrollHeaderId;
							}
							
							emplyerContrbtn=0;
							List emplyrConditionList=[];
							emplyrConditionList.add(EntityCondition.makeCondition("payrollHeaderId", EntityOperator.EQUALS, headerId));
							emplyrConditionList.add(EntityCondition.makeCondition("payrollHeaderItemTypeId", EntityOperator.EQUALS, "PAYROL_DD_ESI_DED"));
							emplyrCondition = EntityCondition.makeCondition(emplyrConditionList,EntityOperator.AND);
							emplyrList = delegator.findList("PayrollHeaderItemEc", emplyrCondition, null, null, null, false);
							if(UtilValidate.isNotEmpty(emplyrList)){
								emplyrList = EntityUtil.getFirst(emplyrList);
								emplyerContrbtn = emplyrList.amount;
							}
							
							List conditionList=[];
							conditionList.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, employeeId));
							conditionList.add(EntityCondition.makeCondition(EntityCondition.makeCondition("noOfPayableDays", EntityOperator.NOT_EQUAL, BigDecimal.ZERO)));
							conditionList.add(EntityCondition.makeCondition("customTimePeriodId", EntityOperator.EQUALS, Periodid));
							condition=EntityCondition.makeCondition(conditionList,EntityOperator.AND);
							attendanceDetails = delegator.findList("PayrollAttendance", condition , null, null, null, false);
							if(UtilValidate.isNotEmpty(attendanceDetails)){
								attendanceDetails.each{ emplAttendance ->
									payableDays = emplAttendance.get("noOfPayableDays");
									detailsMap.put("payableDays",payableDays);
								}
							}
							detailsMap.put("Wages",Wages);
							detailsMap.put("Contribution",emplyerContrbtn);
							detailsMap.put("employeeId",employeeId);
							periodDetailsMap.put(Periodid,detailsMap);
							periodList.add(Periodid);
						}
					}
				}
			}
		}
		String partyName = PartyHelper.getPartyName(delegator, employeeId, false);
		EmplNameMap.put(employeeId,partyName);
		if(UtilValidate.isNotEmpty(periodDetailsMap)){
			EmplWiseDetailsMap.put(employeeId,periodDetailsMap);
		}
	}
}

periodList = (new HashSet(periodList)).toList();
context.put("periodList",periodList);

context.put("EmplNameMap",EmplNameMap);
context.put("EmplWiseDetailsMap",EmplWiseDetailsMap);


