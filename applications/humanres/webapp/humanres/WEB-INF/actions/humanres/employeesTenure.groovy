
import org.ofbiz.base.util.*;
import org.ofbiz.entity.condition.*;
import org.ofbiz.entity.util.EntityUtil;
import net.sf.json.JSONObject;
import net.sf.json.JSONArray;

def populateChildren(org, employeeList) {
	internalOrgs = EntityUtil.filterByDate(delegator.findByAnd("PartyRelationshipAndDetail", [partyIdFrom : org.partyId, partyRelationshipTypeId : "GROUP_ROLLUP"],["groupName"]));
	internalOrgs.each { internalOrg ->
		populateChildren(internalOrg, employeeList);
	}
	
	employments = EntityUtil.filterByDate(delegator.findByAnd("EmploymentAndPerson", [partyIdFrom : org.partyId, roleTypeIdTo : "EMPLOYEE"],["firstName"]));
	employments.each { employment ->
		employee = [:];
		employee.put("department", org.groupName);
		employeePosition = "";
		emplPositionAndFulfillments = EntityUtil.filterByDate(delegator.findByAnd("EmplPositionAndFulfillment", ["employeePartyId" : employment.partyId]));
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
		employee.put("position", employeePosition);
		String lastName="";
		if(employment.lastName!=null){
			lastName=employment.lastName;
		}
		employee.put("name", employment.firstName + " " + lastName);
		employee.put("employeeId", employment.partyId);
		employee.put("birthDate", employment.birthDate);
		joinDate = UtilDateTime.toDateString(employment.appointmentDate, "dd/MM/yyyy");
		employee.put("joinDate", joinDate)
		
		partyTelephone= dispatcher.runSync("getPartyTelephone", [partyId: employment.partyId, userLogin: userLogin]);
		phoneNumber = "";
		if (partyTelephone != null && partyTelephone.contactNumber != null) {
			phoneNumber = partyTelephone.contactNumber;
		}
		employee.put("phoneNumber", phoneNumber);
		address = "";
		//partyPostalAddress= dispatcher.runSync("getPartyPostalAddress", [partyId: employment.partyId, userLogin: userLogin]);
		//if (partyPostalAddress != null) {
		//	address = partyPostalAddress.address1+partyPostalAddress.address2+partyPostalAddress.city+"-"+partyPostalAddress.postalCode;
		//}
		//employee.put("address", address);
		employeeList.add(employee);
	}
}

employeeList = [];
company = delegator.findByPrimaryKey("PartyAndGroup", [partyId : "Company"]);
populateChildren(company, employeeList);
//Debug.logError("employeeList="+employeeList,"");
departmentMap = [:];
positionMap = [:];
upcomingRetirements = [];
thruDate=UtilDateTime.addDaysToTimestamp(UtilDateTime.nowTimestamp(), 365);

JSONArray employeesJSON = new JSONArray();

employeeList.each {employee ->
	if (departmentMap.containsKey(employee.department)) {
		departmentMap[employee.department] += 1;
	}
	else {
		departmentMap[employee.department] = 1;
	}
	if (positionMap.containsKey(employee.position)) {
		positionMap[employee.position] += 1;
	}
	else {
		positionMap[employee.position] = 1;
	}	
	if (employee.birthDate) {
		int day =  UtilDateTime.getDayOfMonth(UtilDateTime.toTimestamp(employee.birthDate), timeZone, locale);
		int month = UtilDateTime.getMonth(UtilDateTime.toTimestamp(employee.birthDate), timeZone, locale) + 1;
		if (day == 1) { // need to take the prev month last date
			month--;
		}
		int year = UtilDateTime.getYear(UtilDateTime.toTimestamp(employee.birthDate), timeZone, locale) + 60;	
		retirementDate = UtilDateTime.toTimestamp(month, day, year, 0, 0, 0);
		retirementDate = UtilDateTime.getMonthEnd(UtilDateTime.toTimestamp(retirementDate), timeZone, locale);
//Debug.logError("employee.birthDate="+employee.birthDate,"");
//Debug.logError("retirementDate= "+retirementDate,"");

		if (retirementDate < thruDate) {
//Debug.logError("deptPieDataJSON="+deptPieDataJSON,"");
			
			JSONArray employeeJSON = new JSONArray();
			employeeJSON.add(employee.name);
			employeeJSON.add(employee.employeeId);
			employeeJSON.add(employee.department);
			employeeJSON.add(employee.position);
			employeeJSON.add(employee.joinDate);
			employeeJSON.add(UtilDateTime.toDateString(retirementDate, "dd/MM/yyyy"));
			employeesJSON.add(employeeJSON);
		}
	}
}
JSONArray deptPieDataJSON = new JSONArray();

JSONArray deptEmployeesJSON = new JSONArray();
departmentMap.each { dept ->
	JSONArray deptJSON = new JSONArray();
	deptJSON.add(dept.getKey());
	deptJSON.add(dept.getValue());
	deptEmployeesJSON.add(deptJSON);
	
	JSONObject deptPie = new JSONObject();
	deptPie.put("label", dept.getKey());
	deptPie.put("data", dept.getValue());
	deptPieDataJSON.add(deptPie);
}

JSONArray positionPieDataJSON = new JSONArray();
JSONArray positionEmployeesJSON = new JSONArray();
positionMap.each { position ->
	JSONArray positionJSON = new JSONArray();
	positionJSON.add(position.getKey());
	positionJSON.add(position.getValue());
	positionEmployeesJSON.add(positionJSON);
	
	JSONObject positionPie = new JSONObject();
	positionPie.put("label", position.getKey());
	positionPie.put("data", position.getValue());
	positionPieDataJSON.add(positionPie);
}
//Debug.logError("departmentMap="+departmentMap,"");
//Debug.logError("positionMap="+positionMap,"");
//Debug.logError("deptEmployeesJSON="+deptEmployeesJSON,"");
//Debug.logError("deptPieDataJSON="+deptPieDataJSON,"");
Debug.logError("positionEmployeesJSON="+positionEmployeesJSON,"");
Debug.logError("positionPieDataJSON="+positionPieDataJSON,"");
//Debug.logError("employeesJSON="+employeesJSON,"");
context.deptEmployeesJSON = deptEmployeesJSON;
context.deptPieDataJSON = deptPieDataJSON;
context.positionEmployeesJSON = positionEmployeesJSON;
context.positionPieDataJSON = positionPieDataJSON;
context.employeesJSON = employeesJSON;



