import org.ofbiz.base.util.*;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.LocalDispatcher;
import java.text.ParseException;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.party.party.PartyHelper;
import org.ofbiz.base.util.UtilMisc;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import javax.swing.text.html.parser.Entity;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.network.LmsServices;
dctx = dispatcher.getDispatchContext();

if (UtilValidate.isEmpty(parameters.fromDate)) {
		fromDate = UtilDateTime.nowTimestamp();
}
else{
	def sdf = new SimpleDateFormat("MMMM dd, yyyy");
	try {
		fromDate = new java.sql.Timestamp(sdf.parse(parameters.fromDate+" 00:00:00").getTime());
	} catch (ParseException e) {
		Debug.logError(e, "Cannot parse date string: " + parameters.fromDate, "");
	}
}
if (UtilValidate.isEmpty(parameters.thruDate)) {
	thruDate = UtilDateTime.nowTimestamp();
}
else{
def sdf = new SimpleDateFormat("MMMM dd, yyyy");
try {
	thruDate = new java.sql.Timestamp(sdf.parse(parameters.thruDate+" 00:00:00").getTime());
} catch (ParseException e) {
	Debug.logError(e, "Cannot parse date string: " + parameters.thruDate, "");
}
}
openedDate = UtilDateTime.getDayStart(fromDate);
closeDate = UtilDateTime.getDayEnd(thruDate);
context.newOrTerminateDate = openedDate;
printDate = UtilDateTime.toDateString(UtilDateTime.nowTimestamp(), "dd/MM/yyyy");
context.printDate = printDate;
conditionList=[];
TerminatedReportFlag=parameters.TerminatedReportFlag;
if(UtilValidate.isEmpty(TerminatedReportFlag)){
	conditionList.add(EntityCondition.makeCondition("openedDate", EntityOperator.GREATER_THAN_EQUAL_TO ,openedDate));
	conditionList.add(EntityCondition.makeCondition("openedDate", EntityOperator.LESS_THAN_EQUAL_TO ,closeDate));
	conditionList.add(EntityCondition.makeCondition("facilityTypeId",  EntityOperator.EQUALS,"BOOTH"));
	condition = EntityCondition.makeCondition(conditionList,EntityOperator.AND);
	List<GenericValue> facilityList = delegator.findList("Facility", condition, null, null, null, false);
	facilitySaleMap = [:];
	Map SCTMap=FastMap.newInstance();
	if(UtilValidate.isNotEmpty(facilityList)){
		for(i=0;i<facilityList.size();i++){
			categoryType=facilityList.get(i).get("categoryTypeEnum");
			facilityId=facilityList.get(i).get("facilityId");
			if(UtilValidate.isNotEmpty(categoryType)){
				if(UtilValidate.isEmpty(facilitySaleMap[categoryType])){
					tempMap=[:];
					Map finalMap=FastMap.newInstance();
					tempMap.putAt("name",facilityList.get(i).get("facilityName"));
					tempMap.putAt("date",facilityList.get(i).get("openedDate"));
					if(UtilValidate.isEmpty(finalMap[facilityId])){
						finalMap.put(facilityId,tempMap);
						facilitySaleMap.put(categoryType,finalMap);
					}
				 }else{
					 tempMap = [:];
					 Map finalMap=FastMap.newInstance();
					 finalMap.putAll(facilitySaleMap.get(categoryType));
					 tempMap.putAt("name",facilityList.get(i).get("facilityName"));
					 tempMap.putAt("date",facilityList.get(i).get("openedDate"));
					 finalMap.put(facilityId,tempMap);
					 facilitySaleMap[categoryType] = finalMap;
				 }
			   }
		}
	}
	context.facilitySaleMap = facilitySaleMap;
}else{
		conditionList.add(EntityCondition.makeCondition("closedDate", EntityOperator.GREATER_THAN_EQUAL_TO ,openedDate));
		conditionList.add(EntityCondition.makeCondition("closedDate", EntityOperator.LESS_THAN_EQUAL_TO ,closeDate));
		conditionList.add(EntityCondition.makeCondition("closedReason", EntityOperator.EQUALS,"PERMANENT"));
		condition = EntityCondition.makeCondition(conditionList,EntityOperator.AND);
		List<GenericValue> facilityList = delegator.findList("Facility", condition, null, null, null, false);
		
		facilitySaleMap = [:];
		Map SCTMap=FastMap.newInstance();
		if(UtilValidate.isNotEmpty(facilityList)){
			for(i=0;i<facilityList.size();i++){
				categoryType=facilityList.get(i).get("categoryTypeEnum");
				facilityId=facilityList.get(i).get("facilityId");
				if(UtilValidate.isNotEmpty(categoryType)){
					if(UtilValidate.isEmpty(facilitySaleMap[categoryType])){
						tempMap=[:];
						Map finalMap=FastMap.newInstance();
						tempMap.putAt("name",facilityList.get(i).get("facilityName"));
						tempMap.putAt("opndate",facilityList.get(i).get("openedDate"));
						tempMap.putAt("closeddate",facilityList.get(i).get("closedDate"));
						tempMap.putAt("closedReason",facilityList.get(i).get("closedReason"));
						if(UtilValidate.isEmpty(finalMap[facilityId])){
							finalMap.put(facilityId,tempMap);
							facilitySaleMap.put(categoryType,finalMap);
						}
					 }else{
						 tempMap = [:];
						 Map finalMap=FastMap.newInstance();
						 finalMap.putAll(facilitySaleMap.get(categoryType));
						 tempMap.putAt("name",facilityList.get(i).get("facilityName"));
						 tempMap.putAt("opndate",facilityList.get(i).get("openedDate"));
						 tempMap.putAt("closeddate",facilityList.get(i).get("closedDate"));
						 tempMap.putAt("closedReason",facilityList.get(i).get("closedReason"));
	//					 tempMap.putAt("date",facilityList.get(i).get("openedDate"));
						 finalMap.put(facilityId,tempMap);
						 facilitySaleMap[categoryType] = finalMap;
					 }
				   }
			}
		}
		context.facilitySaleMap = facilitySaleMap;
	}
