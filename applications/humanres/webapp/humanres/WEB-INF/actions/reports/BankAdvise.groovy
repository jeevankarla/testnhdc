/*
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
*/

import org.ofbiz.base.util.Debug;
import org.ofbiz.entity.condition.*;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.Delegator;
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
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.base.util.UtilNumber;
import org.ofbiz.party.contact.ContactMechWorker;
import java.lang.Integer;
/*List employeeBankList=[];
List conditionList =[];
addresses=null;
customTimePeriod=delegator.findList("CustomTimePeriod", EntityCondition.makeCondition("customTimePeriodId", parameters.customTimePeriodId), null,null, null, false);
	fromDate=UtilDateTime.toTimestamp(customTimePeriod[0].fromDate);
	thruDate=UtilDateTime.toTimestamp(customTimePeriod[0].thruDate);
	context.put("fromDate", fromDate);
	context.put("thruDate", thruDate);
conditionList=UtilMisc.toList(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, parameters.partyId),
      	EntityCondition.makeCondition("invoiceAttrName", EntityOperator.EQUALS, "TIME_PERIOD_ID"),
	    EntityCondition.makeCondition("invoiceAttrValue", EntityOperator.EQUALS,parameters.customTimePeriodId),
		EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "INVOICE_CANCELLED"));
		EntityCondition condition = EntityCondition.makeCondition(conditionList, EntityOperator.AND);
	    depInvoiceList= delegator.findList("InvoiceAndAttribute",condition,null,null,null,false);
		if (context.request) {
			addresses = ContactMechWorker.getPartyPostalAddresses(request, parameters.partyId, "PAYROL_BANK_ADRESS");
		}
		context.addresses = addresses;
depInvoiceList.each{ invoice ->	  
					   Map BankDetailsMap=[:];
					   empDetailsList=delegator.findList("PartyPersonAndEmployeeDetail", EntityCondition.makeCondition("partyId", invoice.partyIdFrom), null,null, null, false);
					   BankDetailsMap["partyId"]=invoice.partyIdFrom;
					   if(empDetailsList){						  
						   BankDetailsMap["accountNumber"]=empDetailsList[0].employeeBankAccNo;
						   BankDetailsMap["empName"]=empDetailsList[0].firstName+empDetailsList[0].lastName;						   
					   }
					   BankDetailsMap["amount"]= InvoiceWorker.getInvoiceTotal(delegator,invoice.invoiceId);
					   employeeBankList.add(BankDetailsMap);
					   
}

context.employeeBankList=employeeBankList;		*/
reportTypeFlag = parameters.reportTypeFlag;
List conditionList =[];
if(UtilValidate.isNotEmpty(parameters.partyIdFrom)){
	parameters.partyIdFrom=parameters.partyIdFrom;
}else{
	parameters.partyIdFrom= "Company";
}
fromDate= context.getAt("timePeriodStart");
thruDate= context.getAt("timePeriodEnd");
bankAdvPayrollMap= context.get("BankAdvicePayRollMap");
	conditionList.add(EntityCondition.makeCondition("ownerPartyId", EntityOperator.EQUALS ,"Company"));
	conditionList.add(EntityCondition.makeCondition("finAccountTypeId", EntityOperator.EQUALS ,"BANK_ACCOUNT"));
	conditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS ,"FNACT_ACTIVE"));
	if(UtilValidate.isNotEmpty(parameters.finAccountId) && (!"All".equals(parameters.finAccountId))){
		conditionList.add(EntityCondition.makeCondition("finAccountId", EntityOperator.EQUALS ,parameters.finAccountId));
	}
  EntityCondition condition = EntityCondition.makeCondition(conditionList, EntityOperator.AND);
  companyBankAccountList= delegator.findList("EmpFinAccount",condition,null,null,null,false);
  Map bankWiseEmplDetailsMap=FastMap.newInstance();
Map CanaraBankMap=FastMap.newInstance();  
AllPartyList = [];
if(UtilValidate.isNotEmpty(companyBankAccountList)){
	companyBankAccountList.each{ bankDetails->		
		finAccountId= bankDetails.finAccountId;
		List conList=FastList.newInstance();
		billingTypeId = "";
		if(UtilValidate.isNotEmpty(parameters.billingTypeId)){
			billingTypeId = parameters.billingTypeId;
		}
		
		conList.add(EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "EMPLOYEE"));
		if(billingTypeId.equals("SP_BONUS")){
			conList.add(EntityCondition.makeCondition("fromDate", EntityOperator.GREATER_THAN_EQUAL_TO, fromDate));
			conList.add(EntityCondition.makeCondition("thruDate", EntityOperator.EQUALS, null));
		}else{
		  	conList.add(EntityCondition.makeCondition("fromDate", EntityOperator.LESS_THAN_EQUAL_TO, thruDate));
			conList.add(EntityCondition.makeCondition(EntityCondition.makeCondition("thruDate", EntityOperator.EQUALS, null), EntityOperator.OR, EntityCondition.makeCondition("thruDate", EntityOperator.GREATER_THAN_EQUAL_TO, fromDate)));
		}
		//BankRequestFlag comes from BankRequestLetterPdf screen 
		if(!BankRequestFlag){
			conList.add(EntityCondition.makeCondition("finAccountId", EntityOperator.EQUALS, finAccountId));
		}
		EntityCondition cond = EntityCondition.makeCondition(conList, EntityOperator.AND);
		finAccountRoleList=delegator.findList("EmpFinAccountRole",cond, null,null, null, false);
		if(UtilValidate.isNotEmpty(finAccountRoleList)){
			partyIds = EntityUtil.getFieldListFromEntityList(finAccountRoleList, "partyId", true);
			AllPartyList.addAll(partyIds);
			emplPartyIds=[];
			for(String partyId : partyIds){
				if(UtilValidate.isNotEmpty(bankAdvPayrollMap.get(partyId)) && (bankAdvPayrollMap.get(partyId).get("netAmt") !=0)){
					emplPartyIds.add(partyId);
				}
			}
			
			if(UtilValidate.isNotEmpty(emplPartyIds)){
				if(UtilValidate.isNotEmpty(bankWiseEmplDetailsMap)){
					bankWiseEmplDetails=bankWiseEmplDetailsMap.entrySet();
					bankWiseEmplDetails.each { bank ->
						employeeList = bank.getValue();
						for(employee in employeeList){
							if(emplPartyIds.contains(employee)){
								emplPartyIds.remove(employee);
							}
						}
					}
				}
				bankWiseEmplDetailsMap.put(finAccountId,emplPartyIds);
			}
			
			/*finAccountIds = EntityUtil.getFieldListFromEntityList(finAccountRoleList, "finAccountId", true);
			AllPartyList.addAll(partyIds);*/
			//Debug.log("partyIds============="+partyIds);
			/*if(UtilValidate.isNotEmpty(partyIds)){
				emplPartyIds=[];
				List finAccConList=FastList.newInstance();
					 finAccConList.add(EntityCondition.makeCondition("finAccountId", EntityOperator.IN ,finAccountIds));
					 finAccConList.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS ,"FNACT_ACTIVE"));
					 finAccConList.add(EntityCondition.makeCondition("finAccountTypeId", EntityOperator.EQUALS ,"BANK_ACCOUNT"));
					 EntityCondition finAccCond = EntityCondition.makeCondition(finAccConList, EntityOperator.AND);
				List<GenericValue> finAccountDetailsList = delegator.findList("FinAccount", finAccCond, null, ["finAccountCode"], null, false);
				List tempfinAccountList = FastList.newInstance();
				partiesFinAccList = UtilMisc.sortMaps(finAccountDetailsList, UtilMisc.toList("finAccountCode"));
				for(finAccount in partiesFinAccList){
					Map tempAccNoMap = FastMap.newInstance();
					tempAccNoMap.put("ownerPartyId", finAccount.get("ownerPartyId"));
					tempAccNoMap.put("gbCode", finAccount.get("gbCode"));
					tempAccNoMap.put("finAccountCode", finAccount.get("finAccountCode"));
					if(UtilValidate.isNotEmpty(finAccount.get("finAccountCode"))){
						BigDecimal accCodeBig = new BigDecimal(finAccount.get("finAccountCode"));
						tempAccNoMap.put("finAccountCode", accCodeBig.intValue());
					}
					tempfinAccountList.add(tempAccNoMap);
				}
				tempfinAccountList = UtilMisc.sortMaps(tempfinAccountList, UtilMisc.toList("ownerPartyId"));
				canraBankPartyIds=[];
				//handling canara bank 
				if(UtilValidate.isNotEmpty(tempfinAccountList)){
					tempfinAccountList.each{ finAcc->
						gbCode = finAcc.get("gbCode");
						ownerPartyId= finAcc.get("ownerPartyId");
						//Debug.log("ownerPartyId============="+ownerPartyId);
						if(reportTypeFlag.equals("disbursedLoanBankReport")){
							if(UtilValidate.isNotEmpty(gbCode) && "CNRB".equals(gbCode)){
    							canraBankPartyIds.add(ownerPartyId);
    						}else{
    							emplPartyIds.add(ownerPartyId);
							}
						}else{
							if(UtilValidate.isNotEmpty(gbCode) && "CNRB".equals(gbCode) &&(UtilValidate.isNotEmpty(bankAdvPayrollMap.get(ownerPartyId)) && (bankAdvPayrollMap.get(ownerPartyId).get("netAmt") !=0))){
								canraBankPartyIds.add(ownerPartyId);
							}else{
								// Adding whose employee NetAmount is not equal to zero
								if(UtilValidate.isNotEmpty(bankAdvPayrollMap.get(ownerPartyId)) && (bankAdvPayrollMap.get(ownerPartyId).get("netAmt") !=0)){
									//Debug.log("netAmt=========="+ownerPartyId+"======"+bankAdvPayrollMap.get(ownerPartyId).get("netAmt"));
									emplPartyIds.add(ownerPartyId);
								}
							}
						}
					}
					if(UtilValidate.isNotEmpty(canraBankPartyIds)){
						if(UtilValidate.isNotEmpty(CanaraBankMap)){
							CanaraBankEmplDetails=CanaraBankMap.entrySet();
							CanaraBankEmplDetails.each { canaraBank ->
								canaraemplList = canaraBank.getValue();
								for(emply in canaraemplList){
									if(emplPartyIds.contains(emply)){
										emplPartyIds.remove(emply);
									}
									if(canraBankPartyIds.contains(emply)){
										canraBankPartyIds.remove(emply);
									}
								}
							}
						}
						CanaraBankMap.put(finAccountId,canraBankPartyIds);
					}
				}
				partyIds = EntityUtil.getFieldListFromEntityList(finAccountDetailsList, "ownerPartyId", true);
				if(UtilValidate.isNotEmpty(emplPartyIds)){
					if(UtilValidate.isNotEmpty(bankWiseEmplDetailsMap)){
						bankWiseEmplDetails=bankWiseEmplDetailsMap.entrySet();
						bankWiseEmplDetails.each { bank ->
							employeeList = bank.getValue();
							for(employee in employeeList){
								if(emplPartyIds.contains(employee)){
									emplPartyIds.remove(employee);
								}
								if(canraBankPartyIds.contains(employee)){
									canraBankPartyIds.remove(employee);
								}
							}
						}
					}
					bankWiseEmplDetailsMap.put(finAccountId,emplPartyIds);
				}
			}*/
		}
		
	}
}
context.put("bankWiseEmplDetailsMap",bankWiseEmplDetailsMap);
context.put("CanaraBankMap",CanaraBankMap);
context.put("AllPartyList",AllPartyList);

	