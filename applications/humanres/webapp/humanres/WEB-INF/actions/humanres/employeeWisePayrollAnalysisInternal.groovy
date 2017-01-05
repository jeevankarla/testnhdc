import org.ofbiz.base.util.Debug;
import net.sf.json.JSONObject;
import net.sf.json.JSONArray;
import org.ofbiz.entity.condition.*;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.base.util.*;
import org.ofbiz.party.party.PartyHelper;


// NOTE: This groovy assumes EmployeePayRollReport.groovy has been run before this


//Debug.logError("parameters.customTimePeriodId="+parameters.customTimePeriodId, "");
//Debug.logError("payRollSummaryMap="+payRollSummaryMap, "");
//Debug.logError("payRollMap="+payRollMap, "");
payRollSummaryMap = context.payRollSummaryMap;
Debug.logError("payRollSummaryMap="+payRollSummaryMap, "");

screenFlag=context.ajaxUrl1;
employementIds = [];
if (security.hasEntityPermission("HUMANRES", "_ADMIN", session)) {
	if(payRollEmployeeMap!=null){
		employementIds = payRollEmployeeMap.keySet();
	}
}
if(screenFlag.equals("EmployeeWisePayrollAnalysisInternal")){
	regionalOfficeId = parameters.regionalOfficeId;
	if(regionalOfficeId.equals("Company")){
		
	}else{	
		conditionList = [];
		conditionList.add(EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, regionalOfficeId));
		conditionList.add(EntityCondition.makeCondition("thruDate", EntityOperator.EQUALS, null));
		employementList = delegator.findList("Employment", EntityCondition.makeCondition(conditionList,EntityOperator.AND), null, null, null, false);
		if(UtilValidate.isNotEmpty(employementList)){
			employementIds = EntityUtil.getFieldListFromEntityList(employementList, "partyIdTo", true);
		}
	}
}
benefitDescMap=context.benefitDescMap;
benefitTypeIds=context.benefitTypeIds;
dedTypeIds=context.dedTypeIds;
dedDescMap=context.dedDescMap;

payheadTypeIds = [];
payheadTypeNames = [];

benefitTypeIds.each{ benefitTypeId->
	if (payRollSummaryMap.containsKey(benefitTypeId)) {
		payheadTypeIds.add(benefitTypeId);
		payheadTypeNames.add(benefitDescMap.get(benefitTypeId));
	}
}
payheadTypeNames.add("Total Benifits");
dedTypeIds.each{ dedTypeId->
	if (payRollSummaryMap.containsKey(dedTypeId)) {
		payheadTypeIds.add(dedTypeId);
		payheadTypeNames.add(dedDescMap.get(dedTypeId));
	}
}
payheadTypeNames.add("Total Deductions");
payheadTypeNames.add("Net Amount");
payheadTypeNames.add("Empl Contribution to CPF");
payheadTypeNames.add("Gross Total");
JSONArray benefitsTableJSON = new JSONArray();
employeeDeptMap = [:];
employments = [];
employments = EntityUtil.filterByDate(delegator.findList("Employment",null, null, null, null, false), context.timePeriodEnd);
employments.each { employment ->
	dept = delegator.findByPrimaryKey("PartyGroup", [partyId : employment.partyIdFrom]);
	employeeDeptMap[employment.partyIdTo] = dept.groupName;
}
//Debug.logError("payRollEmployeeMap="+payRollEmployeeMap,"");
JSONArray employeesPayrollTableJSON = new JSONArray();

if("Y".equals(parameters.isRegionalOfficeTotals)){
	finalMap = [:];
	if (payRollEmployeeMap != null) {
		payRollEmployeeMap.each { employeePayroll ->
			netAmount = 0.0;
			tempMap = [:];
			tempList = [];
			partyId = employeePayroll.getKey();
			partyName = PartyHelper.getPartyName(delegator, partyId, false);
			grpName = employeeDeptMap.get(partyId);
			employeePayrollItems = employeePayroll.getValue();
			tempMap.put("partyId",partyId);
			tempMap.put("partyName",partyName);
			payheadTypeIds.each{ payheadTypeId->
				amount = 0;
				if (employeePayrollItems.containsKey(payheadTypeId)) {
					amount = employeePayrollItems.get(payheadTypeId);
					netAmount = netAmount + amount;
				}
				tempMap.put(payheadTypeId,amount);
			}
			tempMap.put("netAmount",netAmount);
			tempList.add(tempMap);
			
			if(UtilValidate.isNotEmpty(finalMap[grpName])){
				finalList = finalMap[grpName];
				finalList.addAll(tempList);
				finalMap.put(grpName, finalList);
			}
			else{
				finalMap.put(grpName, tempList);
			}
		}
	}
	
	regionPaySheetMap = [:];
	for (Map.Entry entry : finalMap.entrySet()) {
		region = entry.getKey();
		regionPaySheetList = entry.getValue();
		tempMap = [:];
		totalEmpPerRegion = 0;
		for(Map regionPaySheetEntry : regionPaySheetList){
			totalBenifit = 0;
			totalDeduction = 0;
			empCpf = 0;
			benefitDescMap.each { benefitMap ->
				amount = 0;
				if(tempMap.containsKey(benefitMap.getKey())){
					amount = tempMap[benefitMap.getKey()];
					if(regionPaySheetEntry.get(benefitMap.getKey()) != null){
						tempAmt = regionPaySheetEntry.get(benefitMap.getKey());
						if(tempAmt < 0){
							tempAmt *= -1;
						}
						amount += tempAmt;
					}
				}else{
					amount = regionPaySheetEntry.get(benefitMap.getKey());
					if(amount == null){
						amount = 0;
					}
				}
				if(amount < 0){
					amount *= -1;
				}
				tempMap.put(benefitMap.getKey(), amount);
				totalBenifit += amount;
				if("PAYROL_BEN_SALARY".equals(benefitMap.getKey())){
					if(regionPaySheetEntry.get(benefitMap.getKey()) != null){
						empCpf += regionPaySheetEntry.get(benefitMap.getKey());
					}
				}
				if("PAYROL_BEN_DA".equals(benefitMap.getKey())){
					if(regionPaySheetEntry.get(benefitMap.getKey()) != null){
						empCpf += regionPaySheetEntry.get(benefitMap.getKey());
					}
				}
			}
			tempMap.put("totalBenifit", totalBenifit);
			empFpf = empCpf;
			empFpf = Math.min(empFpf, new Double(15000));
			empFpf*=0.0833;
			
			if(tempMap.containsKey("empFpf")){
				empFpf += tempMap["empFpf"]; 
			}
			tempMap.put("empFpf", empFpf);
			
			dedDescMap.each { dedMap ->
				amount = 0;
				if(tempMap.containsKey(dedMap.getKey())){
					amount = tempMap[dedMap.getKey()];
					if(regionPaySheetEntry.get(dedMap.getKey()) != null){
						tempAmt = regionPaySheetEntry.get(dedMap.getKey());
						if(tempAmt < 0){
							tempAmt *= -1;
						}
						amount += tempAmt;
					}
				}else{
					amount = regionPaySheetEntry.get(dedMap.getKey());
					if(amount == null){
						amount = 0;
					}
				}
				if(amount < 0){
					amount *= -1;
				}
				tempMap.put(dedMap.getKey(), amount);
				totalDeduction += amount;
			}
			tempMap.put("totalDeduction", totalDeduction);
			totalEmpPerRegion += 1;
		}
		tempMap.put("totalEmpPerRegion", totalEmpPerRegion);
		
		regionPaySheetMap.put(region, tempMap);
	}
	
	sumMap = [:];
	for(Map.Entry regionPaySheetEntry : regionPaySheetMap.entrySet()){
		regionPayMap = regionPaySheetEntry.getValue();
		regionPayMap.each { regionPayEntry ->
			amount = 0;
			if(sumMap.containsKey(regionPayEntry.getKey())){
				amount = sumMap[regionPayEntry.getKey()];
				amount += regionPayEntry.getValue();
			}
			else{
				amount = regionPayEntry.getValue();
			}
			sumMap.put(regionPayEntry.getKey(), amount);
		}
	}
	
	finalRegionPaySheetMap = [:];
	Set columnKeys = [];
	Map columnMap = [:];
	for(Map.Entry regionPaySheetEntry : regionPaySheetMap.entrySet()){
		regionKey = regionPaySheetEntry.getKey();
		regionPayMap = regionPaySheetEntry.getValue();
		Set zeroValueSet = [];
		regionPayMap.each { regionPayEntry ->
			if(sumMap[regionPayEntry.getKey()] == 0){
				zeroValueSet.add(regionPayEntry.getKey());
				//regionPayMap.remove(regionPayEntry.getKey());
			}
		}
		empCpf = 0;
		if(regionPayMap["PAYROL_BEN_SALARY"]){
			empCpf = regionPayMap["PAYROL_BEN_SALARY"];
		}
		if(regionPayMap["PAYROL_BEN_DA"]){
			empCpf += regionPayMap["PAYROL_BEN_DA"];
		}
		empCpf*=0.12;
		regionPayMap.put("empCpf",empCpf);
		grossTotal = regionPayMap["totalBenifit"]+empCpf;
		regionPayMap.put("grossTotal",grossTotal);
		regionPayMap.keySet().removeAll(zeroValueSet);
		finalRegionPaySheetMap.put(regionKey, regionPayMap);
		columnKeys = regionPayMap.keySet();
	}
	columnKeys.each { columnKey ->
		if(benefitDescMap.containsKey(columnKey)){
			columnMap.put(columnKey, benefitDescMap[columnKey]);
		}
		if(dedDescMap.containsKey(columnKey)){
			columnMap.put(columnKey, dedDescMap[columnKey]);
		}
	}
	//context.put("regionPaySheetMap",regionPaySheetMap);
	context.put("finalRegionPaySheetMap",finalRegionPaySheetMap);
	context.put("columnMap",columnMap);
	context.put("sumMap",sumMap);
	context.put("payheadTypeIds",payheadTypeIds);
	
}
else{
	if (payRollEmployeeMap != null) {
		payRollEmployeeMap.each { employeePayroll ->
			netAmount = 0.0;
			partyId = employeePayroll.getKey();
			if(employementIds.contains(partyId)){
				partyName = PartyHelper.getPartyName(delegator, partyId, false);
				JSONArray employeePayrollJSON = new JSONArray();
				employeePayrollJSON.add(partyId);
				employeePayrollJSON.add(partyName);
				employeePayrollJSON.add(employeeDeptMap.get(partyId));
				employeePayrollItems = employeePayroll.getValue();
				
				totBenifit = 0;
				benefitTypeIds.each{ benefitTypeId->
					amount = 0;
					if (payRollSummaryMap.containsKey(benefitTypeId)) {
						if (employeePayrollItems.containsKey(benefitTypeId)) {
							amount = employeePayrollItems.get(benefitTypeId);//.setScale(0, BigDecimal.ROUND_HALF_UP);
							netAmount = netAmount + amount;
							totBenifit=totBenifit+amount;
						}
						employeePayrollJSON.add(amount);
					}
				}
				employeePayrollJSON.add(totBenifit);
				totDeduction = 0;
				dedTypeIds.each{ dedTypeId->
					amount = 0;
					if (payRollSummaryMap.containsKey(dedTypeId)) {
						if (employeePayrollItems.containsKey(dedTypeId)) {
							amount = employeePayrollItems.get(dedTypeId);//.setScale(0, BigDecimal.ROUND_HALF_UP);
							netAmount = netAmount + amount;
							totDeduction=totDeduction+amount;
						}
						employeePayrollJSON.add(amount);
					}
				}
				employeePayrollJSON.add(totDeduction);
				/*payheadTypeIds.each{ payheadTypeId->
					amount = 0;
					if (employeePayrollItems.containsKey(payheadTypeId)) {
						amount = employeePayrollItems.get(payheadTypeId);//.setScale(0, BigDecimal.ROUND_HALF_UP);
						netAmount = netAmount + amount;
					}
					employeePayrollJSON.add(amount);
				}*/
				employeePayrollJSON.add(netAmount);
				empCPF = 0;
				if(employeePayrollItems.get("PAYROL_BEN_SALARY")){
					empCPF = employeePayrollItems.get("PAYROL_BEN_SALARY");
				}
				if(employeePayrollItems.get("PAYROL_BEN_DA")){
					empCPF+=employeePayrollItems.get("PAYROL_BEN_DA");
				}
				empCPF*=0.12;
				employeePayrollJSON.add(empCPF);
				employeePayrollJSON.add(totBenifit+empCPF);
				employeesPayrollTableJSON.add(employeePayrollJSON);
		  }
		}
	}
}


//Debug.logError("employeesPayrollTableJSON="+employeesPayrollTableJSON,"");

context.payheadTypes = payheadTypeNames;
context.employeesPayrollTableJSON = employeesPayrollTableJSON;

