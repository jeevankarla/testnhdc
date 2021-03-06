import org.ofbiz.base.util.UtilDateTime;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javolution.util.FastList;
import org.ofbiz.entity.Delegator;
import org.ofbiz.base.util.*;
import net.sf.json.JSONObject;
import org.ofbiz.entity.util.*;
import net.sf.json.JSONArray;
import org.ofbiz.entity.*;
import org.ofbiz.entity.condition.*;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import java.sql.*;
import in.vasista.vbiz.byproducts.ByProductNetworkServices;
import in.vasista.vbiz.purchase.MaterialHelperServices;
import org.ofbiz.party.party.PartyHelper;

JSONArray partyJSON = new JSONArray();
JSONArray associatePartyJSON = new JSONArray();
JSONObject partyNameObj = new JSONObject();
dctx = dispatcher.getDispatchContext();
subDivDeptFlag=parameters.subDivDeptFlag;
subDivDeptFlag=context.subDivDeptFlag;
if(UtilValidate.isNotEmpty(parameters.roleTypeId)){//to handle IceCream Parties
	roleTypeId =parameters.roleTypeId;
	inputMap = [:];
	inputMap.put("userLogin", userLogin);
	inputMap.put("roleTypeId", roleTypeId);
	if(UtilValidate.isNotEmpty(parameters.partyStatusId)){
			inputMap.put("statusId", parameters.partyStatusId);
	}
	Map partyDetailsMap = ByProductNetworkServices.getPartyByRoleType(dctx, inputMap);
	if(UtilValidate.isNotEmpty(partyDetailsMap)){
		partyDetailsList = partyDetailsMap.get("partyDetails");
	   //To handle KMFUnions along with OtherSuppliers in PurchaseOrder tab.
		if(("MAINSTORE_VENDOR"==parameters.roleTypeId) || "FGSProductSaleMm".equals(subDivDeptFlag)){
			inputMap = [:];
			inputMap.put("userLogin", userLogin);
			inputMap.put("roleTypeId", "UNION");
			if(UtilValidate.isNotEmpty(parameters.partyStatusId)){
					inputMap.put("statusId", parameters.partyStatusId);
			}
			Map unionPartyDetailsMap = ByProductNetworkServices.getPartyByRoleType(dctx,inputMap);
			unionPartyDetailsList = unionPartyDetailsMap.get("partyDetails");
			//Debug.log("======unionPartyDetailsList=="+unionPartyDetailsList.size());
			if(UtilValidate.isNotEmpty(unionPartyDetailsList)&&(UtilValidate.isNotEmpty(partyDetailsList))){
				partyDetailsList.addAll(unionPartyDetailsList);
			}
			if(UtilValidate.isNotEmpty(unionPartyDetailsList)&&(UtilValidate.isEmpty(partyDetailsList))){
				partyDetailsList=unionPartyDetailsList;
			}
		}
		if("FGSProductSaleMm".equals(subDivDeptFlag)){
			inputMap = [:];
			inputMap.put("userLogin", userLogin);
			inputMap.put("roleTypeId", "Retailer");
			if(UtilValidate.isNotEmpty(parameters.partyStatusId)){
					inputMap.put("statusId", parameters.partyStatusId);
			}
			Map retailerPartyDetailsMap = ByProductNetworkServices.getPartyByRoleType(dctx,inputMap);
			retailerPartyDetailsList = retailerPartyDetailsMap.get("partyDetails");
			//Debug.log("======unionPartyDetailsList=="+unionPartyDetailsList.size());
			if(UtilValidate.isNotEmpty(retailerPartyDetailsList)&&(UtilValidate.isNotEmpty(partyDetailsList))){
				partyDetailsList.addAll(retailerPartyDetailsList);
			}
			if(UtilValidate.isNotEmpty(unionPartyDetailsList)&&(UtilValidate.isEmpty(partyDetailsList))){
				partyDetailsList=retailerPartyDetailsList;
			}
		}
	}
	if(roleTypeId && roleTypeId == "EXCLUSIVE_CUSTOMER"){
		inputMap = [:];
		inputMap.put("userLogin", userLogin);
		inputMap.put("roleTypeId", "GCMMF_GODOWN");
		if(UtilValidate.isNotEmpty(parameters.partyStatusId)){
				inputMap.put("statusId", parameters.partyStatusId);
		}
		associatePartyDetails = ByProductNetworkServices.getPartyByRoleType(dctx, inputMap);
		partyAssocDetailsList = [];
		if(UtilValidate.isNotEmpty(associatePartyDetails)){
			partyAssocDetailsList = associatePartyDetails.get("partyDetails");
		}
		partyAssocDetailsList.each{eachParty ->
			JSONObject newPartyObj = new JSONObject();
			partyName=PartyHelper.getPartyName(delegator, eachParty.partyId, false);
			newPartyObj.put("value",eachParty.partyId);
			newPartyObj.put("label",partyName+" ["+eachParty.partyId+"]");
			//partyNameObj.put(eachParty.partyId,partyName);
			associatePartyJSON.add(newPartyObj);
		}
		context.associatePartyJSON = associatePartyJSON;
	}
		// filter OUT depratments if this flag
		if(UtilValidate.isEmpty(parameters.changeFlag)){
			// sub division here
			inputMap = [:];
			inputMap.put("userLogin", userLogin);
			inputMap.put("fromDate", UtilDateTime.addDaysToTimestamp(UtilDateTime.nowTimestamp(), -730));
			if(UtilValidate.isNotEmpty(parameters.partyStatusId)){
				inputMap.put("statusId", parameters.partyStatusId);
			}
			Map departmentsMap = MaterialHelperServices.getDivisionDepartments(dctx,inputMap);
			if(UtilValidate.isNotEmpty(departmentsMap)){
				departmentList=departmentsMap.get("subDivisionDepartmentList");
				partyDetailsList.addAll(departmentList);
			}
		}
	partyDetailsList.each{eachParty ->
		JSONObject newPartyObj = new JSONObject();
		partyName=PartyHelper.getPartyName(delegator, eachParty.partyId, false);
		newPartyObj.put("value",eachParty.partyId);
		newPartyObj.put("label",partyName+" ["+eachParty.partyId+"]");
		partyNameObj.put(eachParty.partyId,partyName);
		partyJSON.add(newPartyObj);
	}
}
//supplier json for supplier lookup.
JSONArray supplierJSON = new JSONArray();

Condition = EntityCondition.makeCondition([EntityCondition.makeCondition("roleTypeId", "SUPPLIER")],EntityOperator.AND);
supplierList=delegator.findList("PartyRole",Condition,null,null,null,false);
if(supplierList){
	supplierList.each{ supplier ->
		JSONObject newObj = new JSONObject();
		conditionList =[];
		conditionList.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS , supplier.partyId));
		conditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS , "PARTY_ENABLED"));
		condition = EntityCondition.makeCondition(conditionList,EntityOperator.AND);
		partyList = delegator.findList("Party", condition, null, null, null, false);
		partyDetails = EntityUtil.getFirst(partyList);
		if(UtilValidate.isNotEmpty(partyDetails)){
			newObj.put("value",partyDetails.partyId);
			partyName=PartyHelper.getPartyName(delegator, partyDetails.partyId, false);
			newObj.put("label",partyName+"["+partyDetails.partyId+"]");
			supplierJSON.add(newObj);
			partyNameObj.put(partyDetails.partyId,partyName);
		}
	}
}
context.supplierJSON=supplierJSON;
context.partyNameObj = partyNameObj;
context.partyJSON = partyJSON;