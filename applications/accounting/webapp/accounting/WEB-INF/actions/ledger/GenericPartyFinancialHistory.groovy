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
import org.ofbiz.entity.*;
import org.ofbiz.base.util.*;
import org.ofbiz.common.*;
import org.ofbiz.webapp.control.*;
import org.ofbiz.accounting.invoice.*;
import org.ofbiz.accounting.payment.*;
import org.ofbiz.entity.*;
import org.ofbiz.entity.condition.*;
import org.ofbiz.entity.util.*;
import javolution.util.FastMap;
import java.util.Calendar;
import java.util.List;

import javolution.util.FastList;
import javolution.util.FastMap;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.ServiceUtil;

fromDate=parameters.fromDate;
thruDate=parameters.thruDate;
partyCode = parameters.partyId;
dctx = dispatcher.getDispatchContext();
fromDateTime = null;
thruDateTime = null;
if(UtilValidate.isNotEmpty(fromDate)&& UtilValidate.isNotEmpty(thruDate)){
def sdf = new SimpleDateFormat("MMMM dd, yyyy");
try {
	fromDateTime = new java.sql.Timestamp(sdf.parse(fromDate).getTime());
	thruDateTime = new java.sql.Timestamp(sdf.parse(thruDate).getTime());
} catch (ParseException e) {
	Debug.logError(e, "Cannot parse date string: "+fromDate, "");
}
}
Timestamp invoiceFirstDate=null;
Timestamp invoiceLastDate=null;
Timestamp paymentFirstDate=null;
Timestamp paymentLastDate=null;


partyFinHistoryMap=[:];

Boolean actualCurrency = new Boolean(context.actualCurrency);
if (actualCurrency == null) {
    actualCurrency = true;
}
actualCurrencyUomId = context.actualCurrencyUomId;
if (!actualCurrencyUomId) {
    actualCurrencyUomId = context.defaultOrganizationPartyCurrencyUomId;
}
findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
//get total/unapplied/applied invoices separated by sales/purch amount:
/*
for Company

AR---->invoice   D...and...Payment C
AP----->invoice  C....and...Payment D

OB = arOB()- apOB()  //for 

if OB > 0  then Ar is big( put in Debit)
else       then is Ap  big(put in Credit)

clsoing Bal=Ob+total D -  total C
*/
totalInvSaApplied         = BigDecimal.ZERO;
totalInvSaNotApplied     = BigDecimal.ZERO;
totalInvPuApplied         = BigDecimal.ZERO;
totalInvPuNotApplied     = BigDecimal.ZERO;

conditionList=[];

conditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "INVOICE_IN_PROCESS"));
conditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "INVOICE_WRITEOFF"));
conditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "INVOICE_CANCELLED"));
if(UtilValidate.isNotEmpty(fromDate)&& UtilValidate.isNotEmpty(thruDate)){
conditionList.add(EntityCondition.makeCondition("invoiceDate", EntityOperator.GREATER_THAN_EQUAL_TO,UtilDateTime.getDayStart(fromDateTime)))
conditionList.add(EntityCondition.makeCondition("invoiceDate",EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.getDayEnd(thruDateTime)))
}
conditionList.add( EntityCondition.makeCondition([
            EntityCondition.makeCondition([
                EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, parameters.partyId),
                EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, "Company")
                ],EntityOperator.AND),
            EntityCondition.makeCondition([
                EntityCondition.makeCondition("partyId", EntityOperator.EQUALS,"Company"),
                EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, parameters.partyId)
                ],EntityOperator.AND)
            ],EntityOperator.OR));

newInvCondition=EntityCondition.makeCondition(conditionList,EntityOperator.AND);



	List<String> payOrderBy = UtilMisc.toList("invoiceDate");
tempInvIterator=null;
invIterator = delegator.find("InvoiceAndType", newInvCondition, null, null, payOrderBy, findOpts);
//Debug.log("==invIterator===="+invIterator+"=invExprs==="+newInvCondition);
tempInvIterator=invIterator;

/*invoiceIdsList=EntityUtil.getFieldListFromEntityListIterator(tempInvIterator, "invoiceId", true);

Debug.log("==invoiceIdsList**************=="+invoiceIdsList+"====");

invoiceDateList=EntityUtil.getFieldListFromEntityListIterator(tempInvIterator, "invoiceDate", true);*/
/*invoiceFirstDate=invoiceDateList.getFirst();
invoiceLastDate=invoiceDateList.getLast();
Debug.log("==invoiceDateList**************=="+invoiceDateList+"====");
Debug.log("==invoiceDate=FF=="+invoiceDateList.getFirst());
Debug.log("==invoiceDate=EE=="+invoiceDateList.getLast());*/


arInvoiceDetailsMap=[:];
arInvoiceDetailsMap.put("invTotal", BigDecimal.ZERO);
apInvoiceDetailsMap=[:];
apInvoiceDetailsMap.put("invTotal", BigDecimal.ZERO);
invCounter=0;
while (invoice = invIterator.next()) {
	invoiceDate=invoice.invoiceDate;
	if(invCounter==0){
		invoiceFirstDate=invoice.invoiceDate;
	}
	invCounter=invCounter+1;
	innerMap=[:];
	curntDay=UtilDateTime.toDateString(invoiceDate ,"dd-MM-yyyy");
	//Debug.log("=curntDay=="+curntDay);
	//Debug.log("=curntDay=="+curntDay+"==invId=="+invoice.invoiceId);
	innerMap["partyId"]="";
	innerMap["date"]=curntDay;
	innerMap["purposeTypeId"]=invoice.purposeTypeId;
	innerMap["description"]=invoice.invoiceMessage;
	innerMap["invoiceDate"]=invoice.invoiceDate;
	innerMap["invoiceId"]=invoice.invoiceId;
	innerMap["tinNumber"]="";
	innerMap["vchrType"]="";
	innerMap["vchrCode"]="";
	innerMap["crOrDbId"]="";
	invTotalVal=org.ofbiz.accounting.invoice.InvoiceWorker.getInvoiceTotal(delegator,invoice.invoiceId);
	//invTotalVal=invTotalVal-vatRevenue;
	innerMap["invTotal"]=invTotalVal;
	innerMap["debitValue"]=0;
	innerMap["creditValue"]=0;
    if ("PURCHASE_INVOICE".equals(invoice.parentTypeId)) {
		innerMap["partyId"]=invoice.partyIdFrom;
		innerMap["vchrType"]="PURCHASE";
		innerMap["vchrCode"]="Purchase";
		if(UtilValidate.isEmpty(invoice.invoiceMessage)){
			if(UtilValidate.isEmpty(invoice.description)){
				innerMap["description"]="Purchase Invoice";
			}else{
			innerMap["description"]=invoice.description;
			}
		}
		innerMap["crOrDbId"]="C";
		innerMap["creditValue"]=invTotalVal;
		//preparing Map here
		dayInvoiceList=[];
		dayInvoiceList=apInvoiceDetailsMap[curntDay];
		//Debug.log("=curntDay=="+curntDay+"==dayInvoiceList=="+dayInvoiceList);
		if(UtilValidate.isEmpty(dayInvoiceList)){
			dayTempInvoiceList=[];
			dayTempInvoiceList.addAll(innerMap);
			dayInvoiceList=dayTempInvoiceList;
			apInvoiceDetailsMap.put(curntDay, dayInvoiceList);
		}else{
		     dayInvoiceList.addAll(innerMap);
			apInvoiceDetailsMap.put(curntDay, dayInvoiceList);
		}
		apInvoiceDetailsMap["invTotal"]+=invTotalVal;
    }
    else if ("SALES_INVOICE".equals(invoice.parentTypeId)) {
		innerMap["vchrType"]="SALES";
		innerMap["vchrCode"]="Sales";
		innerMap["partyId"]=invoice.partyId;
		innerMap["crOrDbId"]="D";
		innerMap["debitValue"]=invTotalVal;
		if(UtilValidate.isEmpty(invoice.invoiceMessage)){
			innerMap["description"]="Sales Invoice";
		}
		//preparing Map here
		dayInvoiceList=[];
		dayInvoiceList=arInvoiceDetailsMap[curntDay];
		//Debug.log("=curntDay=="+curntDay+"==dayInvoiceList=="+dayInvoiceList);
		if(UtilValidate.isEmpty(dayInvoiceList)){
			dayTempInvoiceList=[];
			dayTempInvoiceList.addAll(innerMap);
			dayInvoiceList=dayTempInvoiceList;
			arInvoiceDetailsMap.put(curntDay, dayInvoiceList);
		}else{
			 dayInvoiceList.addAll(innerMap);
			arInvoiceDetailsMap.put(curntDay, dayInvoiceList);
		}
		arInvoiceDetailsMap["invTotal"]+=invTotalVal;
    }
    else {
        Debug.logError("InvoiceType: " + invoice.invoiceTypeId + " without a valid parentTypeId: " + invoice.parentTypeId + " !!!! Should be either PURCHASE_INVOICE or SALES_INVOICE", "");
    }
}

invIterator.close();

//get total/unapplied/applied payment in/out total amount:

arPaymentDetailsMap=[:];
arPaymentDetailsMap.put("amount",BigDecimal.ZERO);
apPaymentDetailsMap=[:];
apPaymentDetailsMap.put("amount",BigDecimal.ZERO);

totalPayInApplied         = BigDecimal.ZERO;
totalPayInNotApplied     = BigDecimal.ZERO;
totalPayOutApplied         = BigDecimal.ZERO;
totalPayOutNotApplied     = BigDecimal.ZERO;


payCounter=0;
conditionList.clear();
conditionList.add( EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "PMNT_NOTPAID"));
conditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "PMNT_CANCELLED"));
conditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "PMNT_VOID"));
if(UtilValidate.isNotEmpty(fromDate)&& UtilValidate.isNotEmpty(thruDate)){
conditionList.add(EntityCondition.makeCondition("paymentDate", EntityOperator.GREATER_THAN_EQUAL_TO,UtilDateTime.getDayStart(fromDateTime)))
conditionList.add(EntityCondition.makeCondition("paymentDate",EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.getDayEnd(thruDateTime)))
}
conditionList.add(EntityCondition.makeCondition([
               EntityCondition.makeCondition([
                EntityCondition.makeCondition("partyIdTo", EntityOperator.EQUALS, parameters.partyId),
                EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, "Company")
                ], EntityOperator.AND),
            EntityCondition.makeCondition([
                EntityCondition.makeCondition("partyIdTo", EntityOperator.EQUALS, "Company"),
                EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, parameters.partyId)
                ], EntityOperator.AND)
            ], EntityOperator.OR));

newPayCondition=EntityCondition.makeCondition(conditionList,EntityOperator.AND);


	List<String> orderBy = UtilMisc.toList("paymentDate");
payIterator = delegator.find("PaymentAndType", newPayCondition, null, null, orderBy, findOpts);

while (payment = payIterator.next()) {
	
	paymentDate=payment.paymentDate;
	if(payCounter==0){
		paymentFirstDate=payment.paymentDate;
	}
	payCounter=payCounter+1;
	innerMap=[:];
	curntDay=UtilDateTime.toDateString(paymentDate ,"dd-MM-yyyy");
	//Debug.log("=curntDay===in=Payment=="+curntDay+"==paymentId=");
	innerMap["partyId"]="";
	innerMap["date"]=curntDay;
	innerMap["paymentDate"]=payment.paymentDate;
	innerMap["paymentId"]=payment.paymentId;
	innerMap["vchrType"]="";
	innerMap["vchrCode"]="";
	innerMap["paymentMethodTypeId"]=payment.paymentMethodTypeId;
	innerMap["description"]=payment.comments;
	innerMap["amount"]=payment.amount;
	innerMap["crOrDbId"]="";
	innerMap["debitValue"]=0;
	innerMap["creditValue"]=0;
	
    if ("DISBURSEMENT".equals(payment.parentTypeId) || "TAX_PAYMENT".equals(payment.parentTypeId)) {
		innerMap["partyId"]=payment.partyIdTo;
		innerMap["vchrType"]="DISBURSEMENT";
		innerMap["crOrDbId"]="D";
		innerMap["vchrCode"]="Payment";
		innerMap["debitValue"]=payment.amount;
		if(UtilValidate.isEmpty(payment.comments)){
			innerMap["description"]="Payment Amount";
		}
		//preparing Map here
		dayPaymentList=[];
		dayPaymentList=apPaymentDetailsMap[curntDay];
		//Debug.log("=curntDay=="+curntDay+"==dayInvoiceList=="+dayPaymentList);
		if(UtilValidate.isEmpty(dayPaymentList)){
			dayTempPaymentList=[];
			dayTempPaymentList.addAll(innerMap);
			dayPaymentList=dayTempPaymentList;
			apPaymentDetailsMap.put(curntDay, dayPaymentList);
		}else{
			 dayPaymentList.addAll(innerMap);
			apPaymentDetailsMap.put(curntDay, dayPaymentList);
		}
		apPaymentDetailsMap.put("amount",apPaymentDetailsMap.get("amount").add(payment.amount));
    }
    else if ("RECEIPT".equals(payment.parentTypeId)) {
		innerMap["partyId"]=payment.partyIdFrom;
		innerMap["vchrType"]="RECEIPT";
		innerMap["vchrCode"]="Receipt";
		innerMap["crOrDbId"]="C";
		innerMap["creditValue"]=payment.amount;
		if(UtilValidate.isEmpty(payment.comments)){
			innerMap["description"]="Receipt Amount";
		}
		//preparing Map here
		dayPaymentList=[];
		dayPaymentList=arPaymentDetailsMap[curntDay];
		//Debug.log("=curntDay=="+curntDay+"==dayInvoiceList=="+dayPaymentList);
		if(UtilValidate.isEmpty(dayPaymentList)){
			dayTempPaymentList=[];
			dayTempPaymentList.addAll(innerMap);
			dayPaymentList=dayTempPaymentList;
			arPaymentDetailsMap.put(curntDay, dayPaymentList);
		}else{
			 dayPaymentList.addAll(innerMap);
			arPaymentDetailsMap.put(curntDay, dayPaymentList);
		}
		arPaymentDetailsMap.put("amount",arPaymentDetailsMap.get("amount").add(payment.amount));
		
    }
    else {
        Debug.logError("PaymentTypeId: " + payment.paymentTypeId + " without a valid parentTypeId: " + payment.parentTypeId + " !!!! Should be either DISBURSEMENT, TAX_PAYMENT or RECEIPT", "");
    }
}
payIterator.close();
//finalMap prepration
if(UtilValidate.isEmpty(fromDate)){
	if(paymentFirstDate<invoiceFirstDate){
		fromDateTime=paymentFirstDate;
	}else{
	fromDateTime=invoiceFirstDate;
	}
}
dayBegin = UtilDateTime.getDayStart(fromDateTime);
dayEnd = UtilDateTime.getDayEnd(thruDateTime);
context.fromDate=dayBegin;
context.thruDate=dayEnd;
//Debug.log("=curntDay===in=dayBegin=="+dayBegin+"==dayEnd==="+dayEnd);
//Debug.log("=======arInvoiceDetailsMap==>"+arInvoiceDetailsMap);
//Debug.log("=======apInvoiceDetailsMap==>"+apInvoiceDetailsMap);
totalDays= UtilDateTime.getIntervalInDays(dayBegin,dayEnd)+1;
dayWiseArDetailMap=[:];
dayWiseApDetailMap=[:];
partyDayWiseDetailMap=[:];
for(int j=0 ; j < (totalDays); j++){
	Timestamp saleDate = UtilDateTime.addDaysToTimestamp(dayBegin, j);
	reciepts = BigDecimal.ZERO;
	totalValue = BigDecimal.ZERO;
	curntDay=UtilDateTime.toDateString(saleDate ,"dd-MM-yyyy");
	
	dayDetailMap=[:]
	dayInvoiceList=[];
	dayPaymentList=[];
	//ar map preparation
	newTempMap=[:];
	arInvList=arInvoiceDetailsMap.get(curntDay);
	arPayList=arPaymentDetailsMap.get(curntDay);
	if(UtilValidate.isNotEmpty(arInvList) || UtilValidate.isNotEmpty(arPayList)){
		newTempMap.put("invoiceList", arInvList);
		newTempMap.put("paymentList", arPayList);
		dayWiseArDetailMap[curntDay]=newTempMap;
		//preparing Anothor List 
		if(UtilValidate.isNotEmpty(arInvList)){
			dayInvoiceList.addAll(arInvList);
		}
		if(UtilValidate.isNotEmpty(arPayList)){
			dayPaymentList.addAll(arPayList);
		}
	}
	//AP map preparation
	newApTempMap=[:];
	invList=apInvoiceDetailsMap.get(curntDay);
	payList=apPaymentDetailsMap.get(curntDay);
	if(UtilValidate.isNotEmpty(invList) || UtilValidate.isNotEmpty(payList)){
		newApTempMap.put("invoiceList", invList);
		newApTempMap.put("paymentList", payList);
		dayWiseApDetailMap[curntDay]=newApTempMap;
		//preparing Anothor List
		if(UtilValidate.isNotEmpty(invList)){
			dayInvoiceList.addAll(invList);
		}
		if(UtilValidate.isNotEmpty(payList)){
			dayPaymentList.addAll(payList);
		}
	}
	//dayWise PartyMap prepartion
	if(UtilValidate.isNotEmpty(dayInvoiceList) || UtilValidate.isNotEmpty(dayPaymentList)){
		dayDetailMap.put("invoiceList", dayInvoiceList);
		dayDetailMap.put("paymentList", dayPaymentList);
		partyDayWiseDetailMap[curntDay]=dayDetailMap;
	}
}
/*dayWiseArDetailMap["totInvoiceValue"]=arInvoiceDetailsMap.get("invTotal");
dayWiseArDetailMap["totPaymentValue"]=arPaymentDetailsMap.get("amount");

dayWiseApDetailMap["totInvoiceValue"]=apInvoiceDetailsMap.get("invTotal");
dayWiseApDetailMap["totPaymentValue"]=apInvoiceDetailsMap.get("amount");*/
context.arInvoiceDetailsMap=arInvoiceDetailsMap;
context.apInvoiceDetailsMap=apInvoiceDetailsMap;

context.arPaymentDetailsMap=arPaymentDetailsMap;
context.apPaymentDetailsMap=apPaymentDetailsMap;

context.dayWiseArDetailMap=dayWiseArDetailMap;
context.dayWiseApDetailMap=dayWiseApDetailMap;

//Debug.log("=======dayWiseArDetailMap==>"+dayWiseArDetailMap);
//Debug.log("=======dayWiseApDetailMap==>"+dayWiseApDetailMap);

arOpeningBalance  =BigDecimal.ZERO;
arClosingBalance  =BigDecimal.ZERO;

apOpeningBalance  =BigDecimal.ZERO;
apClosingBalance  =BigDecimal.ZERO;

if(UtilValidate.isNotEmpty(parameters.partyId)){
	arOpeningBalance = (org.ofbiz.accounting.ledger.GeneralLedgerServices.getGenericOpeningBalanceForParty( dctx , [userLogin: userLogin, tillDate: dayBegin, partyId:parameters.partyId])).get("openingBalance");
	//openingBalance2 = (in.vasista.vbiz.byproducts.ByProductNetworkServices.getOpeningBalanceForParty( dctx , [userLogin: userLogin, saleDate: dayBegin, partyId:parameters.partyId])).get("openingBalance");
	//Debug.log("=======openingBalance====openingBalance2===>"+openingBalance2);
	apOpeningBalance = (org.ofbiz.accounting.ledger.GeneralLedgerServices.getGenericOpeningBalanceForParty( dctx , [userLogin: userLogin, tillDate: dayBegin, partyId:parameters.partyId,isOBCallForAP:Boolean.TRUE])).get("openingBalance");
	}
	
partyTrTotalMap=[:];
partyTrTotalMap["debitValue"]=BigDecimal.ZERO;
partyTrTotalMap["creditValue"]=BigDecimal.ZERO;
//so PartyTotal Debit means Ar_Invoice+Ap_Payment+(Ar OB with Minus)+ ap OB with Plus
//             Credit means Ap_Invoice+Ar_Payment+(Ar OB with Plus)+ ap  OB with Plus
partyTrTotalMap["debitValue"]+=(arInvoiceDetailsMap.get("invTotal")+apPaymentDetailsMap.get("amount"));

partyTrTotalMap["creditValue"]+=(apInvoiceDetailsMap.get("invTotal")+arPaymentDetailsMap.get("amount"));

partyTotalMap=[:];
partyTotalMap["debitValue"]=BigDecimal.ZERO;
partyTotalMap["creditValue"]=BigDecimal.ZERO;

//Debug.log("====partyTrTotalMap==First====>"+partyTrTotalMap);

//compute OB logic for Credit or Debit
partyOBMap=[:];
partyOBMap["debitValue"]=BigDecimal.ZERO;
partyOBMap["creditValue"]=BigDecimal.ZERO;

oB=arOpeningBalance-apOpeningBalance;
if(oB>0){
	partyOBMap["debitValue"]=oB;
	partyTotalMap["debitValue"]+=(oB);
}else{
  partyOBMap["creditValue"]=(-1*oB);
  partyTotalMap["creditValue"]+=(-1*oB) ;
}
partyTotalMap["debitValue"]+=(partyTrTotalMap["debitValue"]) ;
partyTotalMap["creditValue"]+=(partyTrTotalMap["creditValue"]) ;
//compute CB logic for Credit or Debit
partyCBMap=[:];
partyCBMap["debitValue"]=BigDecimal.ZERO;
partyCBMap["creditValue"]=BigDecimal.ZERO;
cB=partyTotalMap["debitValue"]-partyTotalMap["creditValue"];
if(cB>0){
	partyCBMap["debitValue"]=cB;
}else{
  partyCBMap["creditValue"]= (-1*cB);
}

context.partyOBMap=partyOBMap;
context.partyTrTotalMap=partyTrTotalMap;
context.partyCBMap=partyCBMap;

	arClosingBalance=(arOpeningBalance+arInvoiceDetailsMap.get("invTotal"))-(arPaymentDetailsMap.get("amount"));
	
	apClosingBalance=(apOpeningBalance+apInvoiceDetailsMap.get("invTotal"))-(apPaymentDetailsMap.get("amount"));
	
	//Debug.log("====FINALLL==ARRRR=arClosingBalance==>"+arClosingBalance+"=arOpeningBalance==="+arOpeningBalance);
	//Debug.log("====FINALLL=APPPP==apClosingBalance==>"+apClosingBalance+"=apOpeningBalance==="+apOpeningBalance);
	
	context.partyDayWiseDetailMap=partyDayWiseDetailMap;
	
	context.arOpeningBalance=arOpeningBalance;
	context.arClosingBalance=arClosingBalance;
	
	context.apOpeningBalance=apOpeningBalance;
	context.apClosingBalance=apClosingBalance;
	
//transferAmount = totalInvSaApplied.add(totalInvSaNotApplied).subtract(totalInvPuApplied.add(totalInvPuNotApplied)).subtract(totalPayInApplied.add(totalPayInNotApplied).add(totalPayOutApplied.add(totalPayOutNotApplied)));


