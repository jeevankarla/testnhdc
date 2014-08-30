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
import javolution.util.FastList;
import javolution.util.FastMap;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.ServiceUtil;

import java.math.BigDecimal;
import java.math.MathContext;
import org.ofbiz.base.util.UtilNumber;
import in.vasista.vbiz.byproducts.ByProductNetworkServices;
import org.ofbiz.accounting.invoice.InvoiceWorker;
import in.vasista.vbiz.byproducts.SalesInvoiceServices;
import org.ofbiz.party.party.PartyHelper;



dctx = dispatcher.getDispatchContext();
context.put("dctx",dctx);
fromDate=parameters.fromDate;
thruDate=parameters.thruDate;
categoryType=parameters.categoryType;
context.put("categoryType",categoryType);
dctx = dispatcher.getDispatchContext();
fromDateTime = null;
thruDateTime = null;
def sdf = new SimpleDateFormat("MMMM dd, yyyy");
try {
	fromDateTime = new java.sql.Timestamp(sdf.parse(fromDate).getTime());
	thruDateTime = new java.sql.Timestamp(sdf.parse(thruDate).getTime());
} catch (ParseException e) {
	Debug.logError(e, "Cannot parse date string: "+fromDate, "");
}

fromDateTime = UtilDateTime.getDayStart(fromDateTime);
dayBegin = UtilDateTime.getDayStart(fromDateTime);
dayEnd = UtilDateTime.getDayEnd(thruDateTime);
context.fromDate = fromDateTime;
context.thruDate = thruDateTime;
maxIntervalDays=UtilDateTime.getIntervalInDays(fromDateTime,thruDateTime);
isByParty = Boolean.TRUE;
if(maxIntervalDays > 32){
	Debug.logError("You Cannot Choose More Than 31 Days.","");
	context.errorMessage = "You Cannot Choose More Than 31 Days";
	return;
}
partyIds=[];
if(categoryType.equals("ICE_CREAM_NANDINI")||categoryType.equals("All")){
   nandiniPartyIds = ByProductNetworkServices.getPartyByRoleType(dctx, [userLogin: userLogin, roleTypeId: "IC_WHOLESALE"]).get("partyIds");
   partyIds.addAll(nandiniPartyIds);
}
if(categoryType.equals("ICE_CREAM_AMUL")||categoryType.equals("All")){
   amulPartyIds = ByProductNetworkServices.getPartyByRoleType(dctx, [userLogin: userLogin, roleTypeId: "EXCLUSIVE_CUSTOMER"]).get("partyIds");
   partyIds.addAll(amulPartyIds);
}
invoiceMap = [:];
if(UtilValidate.isNotEmpty(partyIds)){
	invoiceTotals = SalesInvoiceServices.getPeriodSalesInvoiceTotals(dctx, [partyIds:partyIds, isQuantityLtrs:true,fromDate:dayBegin, thruDate:dayEnd]).get("invoiceIdTotals");
	if(UtilValidate.isNotEmpty(invoiceTotals)){
		invoiceTotals.each { invoice ->
			if(UtilValidate.isNotEmpty(invoice)){
				invoiceId = "";
				partyName = "";
				basicRevenue=0;
				bedRevenue=0;
				vatRevenue=0;
				cstRevenue=0;
				totalRevenue=0;
				
				invoiceId = invoice.getKey();
				invoiceDate = invoice.getValue().invoiceDateStr;
				invoiceDetails = delegator.findOne("Invoice",[invoiceId : invoiceId] , false);
				invoicePartyId = invoiceDetails.partyId;
				if(UtilValidate.isNotEmpty(invoicePartyId)){
					partyName = PartyHelper.getPartyName(delegator, invoicePartyId, false);
				}
				if(UtilValidate.isNotEmpty(invoice.getValue().basicRevenue)){
					basicRevenue = invoice.getValue().basicRevenue;
				}
				if(UtilValidate.isNotEmpty(invoice.getValue().vatRevenue)){
					vatRevenue = invoice.getValue().vatRevenue;
				}
				if(UtilValidate.isNotEmpty(invoice.getValue().bedRevenue)){
					bedRevenue = invoice.getValue().bedRevenue;
				}
				if(UtilValidate.isNotEmpty(invoice.getValue().cstRevenue)){
					cstRevenue = invoice.getValue().cstRevenue;
				}
				if(UtilValidate.isNotEmpty(invoice.getValue().totalRevenue)){
					totalRevenue = invoice.getValue().totalRevenue;
				}
				totalMap = [:];
				totalMap["invoiceDate"]=invoiceDate;
				totalMap["basicRevenue"]=basicRevenue;
				totalMap["partyName"]=partyName;
				totalMap["bedRevenue"]=bedRevenue;
				totalMap["vatRevenue"]=vatRevenue;
				totalMap["cstRevenue"]=cstRevenue;
				totalMap["totalRevenue"]=totalRevenue;
				tempMap = [:];
				tempMap.putAll(totalMap);
				if(UtilValidate.isNotEmpty(tempMap)){
					invoiceMap.put(invoiceId,tempMap);
				}
			}
		}
	}
	context.put("invoiceMap",invoiceMap);
}















