import org.ofbiz.base.util.Debug;
import org.ofbiz.entity.condition.*;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.util.EntityUtil;
import net.sf.json.JSONObject;
import net.sf.json.JSONArray;
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
import org.ofbiz.party.party.PartyHelper;
import in.vasista.vbiz.humanres.HumanresService;

dctx = dispatcher.getDispatchContext();
def sdf = new SimpleDateFormat("MMMM dd, yyyy");
try {
	if (parameters.CadrefromDate) {
		fromDate = UtilDateTime.getDayStart(new java.sql.Timestamp(sdf.parse(parameters.CadrefromDate).getTime()));
	}
	if (parameters.CadrethruDate) {
		thruDate = UtilDateTime.getDayEnd(new java.sql.Timestamp(sdf.parse(parameters.CadrethruDate).getTime()));
	}
} catch (ParseException e) {
	Debug.logError(e, "Cannot parse date string: " + e, "");
	context.errorMessage = "Cannot parse date string: " + e;
	return;
}
context.put("fromDate",fromDate);
context.put("thruDate",thruDate);
Map emplInputMap = FastMap.newInstance();
emplInputMap.put("userLogin", userLogin);
emplInputMap.put("fromDate", fromDate);
emplInputMap.put("thruDate", thruDate);
emplInputMap.put("orgPartyId", "Company");
cadreEmployeeList = [];
employementIds = [];
partyIdsList = [];
Map resultMap = HumanresService.getActiveEmployements(dctx,emplInputMap);
List<GenericValue> employementList = (List<GenericValue>)resultMap.get("employementList");
if(parameters.departmentFlag){
	employementIds = EntityUtil.getFieldListFromEntityList(employementList, "partyIdTo", true);
}else{
	employeList = EntityUtil.getFieldListFromEntityList(employementList, "partyIdTo", true);
	if(UtilValidate.isNotEmpty(employeList)){
		employeList.each{ partyId->
			employementId = partyId;
			tempMap =[:];
			tempMap.employementId=employementId;
			String paddedemployementId = String.format("%15s", employementId).replace(' ', '0');
			tempMap.paddedemployementId=paddedemployementId;
			partyIdsList.addAll(tempMap);
		}
	}
	if(UtilValidate.isNotEmpty(partyIdsList)){
		partyIdsList =UtilMisc.sortMaps(partyIdsList, UtilMisc.toList("paddedemployementId"));
	}
	partyIdsList.each{ party->
		partyId=party.get("employementId");
		employementIds.addAll(partyId);
	}
}

if(UtilValidate.isNotEmpty(employementIds)){
	sNo = 0;
	employementIds.each { partyId ->
		sNo = sNo+1;
		cadreMap = [:];
		partyDetails = delegator.findOne("Person",[ partyId : partyId ], false);
		String partyName = partyDetails.get("nickname");
		if(UtilValidate.isEmpty(partyName)){
			partyName = PartyHelper.getPartyName(delegator, partyId, false);
		}else{
			partyName  = "-";
		}
		deptName  = "-";
		employmentConditionList=[];
		employmentConditionList.add(EntityCondition.makeCondition("partyIdTo", EntityOperator.EQUALS , partyId));
		employmentConditionList.add(EntityCondition.makeCondition("roleTypeIdTo", EntityOperator.EQUALS , "EMPLOYEE"));
		employmentConditionList.add(EntityCondition.makeCondition("thruDate", EntityOperator.EQUALS ,null));
		employCondition = EntityCondition.makeCondition(employmentConditionList,EntityOperator.AND);
		employeeDepartmentList = delegator.findList("Employment", employCondition, null, null, null, false);
		if(UtilValidate.isNotEmpty(employeeDepartmentList)){
			employeeDepartmentList.each { departmentDet ->
				departmentId = departmentDet.get("partyIdFrom");
				deptName =  PartyHelper.getPartyName(delegator, departmentId, false);
			}
		}else{
			deptName  = "-";
		}
		//Debug.log("deptName====="+partyId+"=========="+deptName);
		emplPositionAndFulfillments = EntityUtil.filterByDate(delegator.findByAnd("EmplPositionAndFulfillment", ["employeePartyId" : partyId]));
		designationId = "";
		gradeLevel = "";
		if(UtilValidate.isNotEmpty(emplPositionAndFulfillments)){
			designationId = emplPositionAndFulfillments[0].emplPositionTypeId;
			if(UtilValidate.isNotEmpty(designationId)){
				emplPositionType = delegator.findOne("EmplPositionType",[emplPositionTypeId : designationId], true);
				gradeLevel = 0;
				if(UtilValidate.isNotEmpty(emplPositionType.get("gradeLevel"))){
					gradeLevel=emplPositionType.get("gradeLevel");
				}
				if(UtilValidate.isNotEmpty(emplPositionType.get("description"))){
					designationId = emplPositionType.description;
				}
			}
		}else{
			designationId  = "-";
			gradeLevel = 0;
		}
		cadreMap.put("sNo",sNo);
		cadreMap.put("partyId",partyId);
		cadreMap.put("Name",partyName);
		cadreMap.put("designation",designationId);
		cadreMap.put("gradeLevel",gradeLevel);
		cadreMap.put("deptName",deptName);
		cadreEmployeeList.addAll(cadreMap);
	}
}
context.put("cadreEmployeeList",cadreEmployeeList);
