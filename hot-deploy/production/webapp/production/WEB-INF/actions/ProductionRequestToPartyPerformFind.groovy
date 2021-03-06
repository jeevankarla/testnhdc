
import java.math.BigDecimal;
import java.util.*;
import java.sql.Timestamp;
import org.ofbiz.entity.*;
import org.ofbiz.entity.condition.*;
import org.ofbiz.entity.util.*;
import org.ofbiz.base.util.*;
import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import net.sf.json.JSONArray;
import java.util.SortedMap;
import java.math.RoundingMode;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.entity.util.EntityTypeUtil;


if(UtilValidate.isNotEmpty(result.listIt)){
list=result.listIt;
if(UtilValidate.isNotEmpty(parameters.partyIdFromDept)){
	parameters.partyIdFrom=parameters.partyIdFromDept;
}
List resultList=FastList.newInstance();
   GenericValue custRequest=null;
	while(custRequest=list.next()){
	   List	custRequstParty=delegator.findList("CustRequestParty",EntityCondition.makeCondition("custRequestId",EntityOperator.EQUALS,custRequest.custRequestId),null,null,null,false);
		custReqParty=EntityUtil.getFirst(custRequstParty);
		Map tempMap = FastMap.newInstance();
		    tempMap.custRequestId=custRequest.custRequestId;
			tempMap.custRequestTypeId=custRequest.custRequestTypeId;
			tempMap.statusId = custRequest.statusId;
			tempMap.fromPartyId = custRequest.fromPartyId;
			tempMap.custRequestDate = custRequest.custRequestDate;
			tempMap.responseRequiredDate = custRequest.responseRequiredDate;
			tempMap.custRequestName = custRequest.custRequestName;
			tempMap.description = custRequest.description;
			tempMap.createdByUserLogin = custRequest.createdByUserLogin;
			tempMap.lastModifiedDate = custRequest.lastModifiedDate;
			if(UtilValidate.isNotEmpty(custRequest.custRequestItemSeqId)){
				tempMap.custRequestItemSeqId=custRequest.custRequestItemSeqId;
			}
			if(UtilValidate.isNotEmpty(custRequest.productId)){
				tempMap.productId=custRequest.productId;
			}
			if(UtilValidate.isNotEmpty(custRequest.quantity)){
				tempMap.quantity=custRequest.quantity;
			}
			if(UtilValidate.isNotEmpty(custRequest.quantity)){
				tempMap.quantity=custRequest.quantity;
			}
			if(UtilValidate.isNotEmpty(custReqParty.partyId)){
			tempMap.partyIdFrom = custReqParty.partyId;
			}
			if(UtilValidate.isEmpty(parameters.partyIdFrom)){
			 resultList.add(tempMap);
			}else if(UtilValidate.isNotEmpty(parameters.partyIdFrom) && parameters.partyIdFrom==custReqParty.partyId){
			 resultList.add(tempMap);
			}
	}
	context.listIt=resultList;
}