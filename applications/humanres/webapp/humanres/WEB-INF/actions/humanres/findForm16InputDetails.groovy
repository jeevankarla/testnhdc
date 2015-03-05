import org.ofbiz.base.util.*;
import org.ofbiz.entity.condition.*;
import org.ofbiz.entity.util.EntityUtil;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.ofbiz.base.util.UtilDateTime;
import net.sf.json.JSONObject;
import net.sf.json.JSONArray;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.UtilDateTime;
import in.vasista.vbiz.humanres.PayrollService;
import in.vasista.vbiz.humanres.HumanresService;
import in.vasista.vbiz.byproducts.ByProductServices;

JSONArray headItemsJson = new JSONArray();
JSONObject newObj = new JSONObject();

partyId = "";
customTimePeriodId = "";
if(UtilValidate.isNotEmpty(parameters.partyIdTo)){
	partyId = parameters.partyIdTo;
}
if(UtilValidate.isNotEmpty(parameters.customTimePeriodId)){
	customTimePeriodId = parameters.customTimePeriodId;
}

form16InputTypes = delegator.findList("Enumeration",EntityCondition.makeCondition("enumTypeId", EntityOperator.EQUALS, "FORM16_INPUT_TYPE") , null, null, null, false);
sectionTypesList = EntityUtil.getFieldListFromEntityList(form16InputTypes, "enumId", true);



if(UtilValidate.isNotEmpty(sectionTypesList)){
	sectionTypesList.each{ sectionType->
		List employeeSectionList=[];
		employeeSectionList.add(EntityCondition.makeCondition("employeeId", EntityOperator.EQUALS, partyId));
		employeeSectionList.add(EntityCondition.makeCondition("sectionTypeId", EntityOperator.EQUALS, sectionType));
		employeeSectionList.add(EntityCondition.makeCondition("customTimePeriodId", EntityOperator.EQUALS, customTimePeriodId));
		sectionCondition=EntityCondition.makeCondition(employeeSectionList,EntityOperator.AND);
		sectionList = delegator.findList("EmployeeForm16Detail", sectionCondition , null, null, null, false );
		if(UtilValidate.isNotEmpty(sectionList)){
			sectionList.each{ section->
				sectionDesc = "";
				sectionTypeId = section.get("sectionTypeId");
				grossAmount = section.get("grossAmount");
				qualifyingAmount = section.get("qualifyingAmount");
				deductableAmount = section.get("deductableAmount");
				GenericValue sectionDetails = delegator.findOne("Enumeration", [enumId : sectionType], false);
				if(UtilValidate.isNotEmpty(sectionDetails)){
					sectionDesc = sectionDetails.get("description");
				}
				newObj.put("id",sectionTypeId+"["+sectionDesc+"]");
				newObj.put("inputTypeId",sectionType);
				newObj.put("sectionTypeId",sectionDesc);
				newObj.put("grossAmount",grossAmount);
				newObj.put("qualifyingAmount",qualifyingAmount);
				newObj.put("deductableAmount",deductableAmount);
				headItemsJson.add(newObj);
			}
		}else{
			GenericValue sectionTypeDetails = delegator.findOne("Enumeration", [enumId : sectionType], false);
			if(UtilValidate.isNotEmpty(sectionTypeDetails)){
				sectionDesc = sectionTypeDetails.get("description");
				newObj.put("id",sectionType+"["+sectionDesc+"]");
				newObj.put("inputTypeId",sectionType);
				newObj.put("sectionTypeId",sectionDesc);
				newObj.put("grossAmount","");
				newObj.put("qualifyingAmount","");
				newObj.put("deductableAmount","");
				headItemsJson.add(newObj);
			}
		}
	}
}
context.headItemsJson=headItemsJson;

