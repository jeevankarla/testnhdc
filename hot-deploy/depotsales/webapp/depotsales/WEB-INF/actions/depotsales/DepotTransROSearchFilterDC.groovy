import org.ofbiz.base.util.*;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;

import net.sf.json.JSONObject;
import net.sf.json.JSONArray;
import javolution.util.FastMap;
import javolution.util.FastList;
import java.sql.Timestamp;

import org.ofbiz.base.util.UtilDateTime;

import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.ofbiz.service.ServiceUtil;

if(UtilValidate.isNotEmpty(parameters.partyId_op)){
	partyId = parameters.partyId;
	partyId = partyId.replaceAll("[{}]", "");
	List tempPartyIdList = StringUtil.split(partyId,",");
	List<String> newPartyList = FastList.newInstance();
	for(int i=0;i<tempPartyIdList.size();i++)
	{
		tPartyId = tempPartyIdList.get(i).replaceAll("\\[", "").replaceAll("\\]","");
		newPartyList.add(tPartyId.trim());
	}
	parameters.partyId = newPartyList;
	condList = [];
	partyIdList = newPartyList;
	condList.add(EntityCondition.makeCondition("partyIdTo" ,EntityOperator.IN,partyIdList));
	condList.add(EntityCondition.makeCondition("roleTypeIdFrom" ,EntityOperator.EQUALS,"PARENT_ORGANIZATION"));
	condList.add(EntityCondition.makeCondition("roleTypeIdTo" ,EntityOperator.EQUALS,"ORGANIZATION_UNIT"));
	PartyRelationshipList = delegator.findList("PartyRelationship",EntityCondition.makeCondition(condList,EntityOperator.AND), null, null, null, false );
	if(UtilValidate.isNotEmpty(PartyRelationshipList)){
		List partyIdFromList = EntityUtil.getFieldListFromEntityList(PartyRelationshipList, "partyIdFrom", true);
		if(partyIdFromList.size() == 1){
			context.partyId = EntityUtil.getFirst(PartyRelationshipList).get("partyIdFrom");
		}
		else{
			context.partyId = "";
		}
	}
}
else if(UtilValidate.isNotEmpty(parameters.partyId)){
	partyIdFrom = parameters.partyId;
	parameters.tempPartyId = partyIdFrom;
	context.tempPartyId = partyIdFrom;
	condList = [];
	condList.add(EntityCondition.makeCondition("partyId" ,EntityOperator.EQUALS,partyIdFrom));
	condList.add(EntityCondition.makeCondition("partyClassificationGroupId" ,EntityOperator.EQUALS,"REGIONAL_OFFICE"));
	PartyClassification = delegator.findList("PartyClassification",EntityCondition.makeCondition(condList,EntityOperator.AND), null, null, null, false );
	if(UtilValidate.isNotEmpty(PartyClassification)){
		condList.clear();
		condList.add(EntityCondition.makeCondition("partyIdFrom" ,EntityOperator.EQUALS,partyIdFrom));
		condList.add(EntityCondition.makeCondition("partyRelationshipTypeId" ,EntityOperator.EQUALS,"BRANCH_CUSTOMER"));
		PartyRelationship = delegator.findList("PartyRelationship",EntityCondition.makeCondition(condList,EntityOperator.AND), null, null, null, false );
		if(UtilValidate.isNotEmpty(PartyRelationship)){
			partyIdList = EntityUtil.getFieldListFromEntityList(PartyRelationship, "partyIdTo", true);
			parameters.partyId = partyIdList;
			parameters.partyId_op = "in";
		}
	}
}
else{
	if(parameters.hideSearch=="Y"){
		resultCtx = dispatcher.runSync("getCustomerBranch",UtilMisc.toMap("userLogin",userLogin));
		List branchIds = [];
		for (eachList in resultCtx.get("productStoreList")) {
			branchIds.add(eachList.get("payToPartyId"));
		}
		parameters.partyId = branchIds;
		parameters.partyId_op = "in";
		context.partyId = "";
	}
	
}
