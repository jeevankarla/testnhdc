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
import java.util.SortedMap;
import javolution.util.FastMap;
import javolution.util.FastList;
import org.ofbiz.entity.util.EntityTypeUtil;
import org.ofbiz.product.inventory.InventoryWorker;
import org.ofbiz.product.inventory.InventoryServices;
import in.vasista.vbiz.purchase.MaterialHelperServices;

fromDate = parameters.ivdFromDate;
thruDate = parameters.ivdThruDate;

context.fromDate=fromDate;
context.thruDate=thruDate;

def sdf = new SimpleDateFormat("MMMM dd, yyyy");
try {
	if (UtilValidate.isNotEmpty(fromDate)) {
		fromDate = UtilDateTime.getDayStart(new java.sql.Timestamp(sdf.parse(fromDate).getTime()));
		thruDate = UtilDateTime.getDayEnd(new java.sql.Timestamp(sdf.parse(thruDate).getTime()));
	}
} catch (ParseException e) {
	Debug.logError(e, "Cannot parse date string: " + e, "");
context.errorMessage = "Cannot parse date string: " + e;
	return;
}

dayStart = UtilDateTime.getDayStart(fromDate);
dayEnd = UtilDateTime.getDayEnd(thruDate);

Map finalMap = FastMap.newInstance();

conditionList=[];
conditionList.add(EntityCondition.makeCondition("estimatedDeliveryDate", EntityOperator.GREATER_THAN_EQUAL_TO, dayStart));
			conditionList.add(EntityCondition.makeCondition("estimatedDeliveryDate", EntityOperator.LESS_THAN_EQUAL_TO, dayEnd));
			conditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "ORDER_CANCELLED"));
			condition = EntityCondition.makeCondition(conditionList, EntityOperator.AND);
			OrderHeaderList = delegator.findList("OrderHeaderAndItems", condition, null, null, null, false); 
			OrderIdList = EntityUtil.getFieldListFromEntityList(OrderHeaderList, "orderId", true);
			
						
	if(UtilValidate.isNotEmpty(OrderIdList)){
			
			OrderIdList.each{orderId->
				
				Map ProductWiseMap = FastMap.newInstance(); 
				List productList = EntityUtil.filterByAnd(OrderHeaderList, UtilMisc.toMap("orderId", orderId));
				if(UtilValidate.isNotEmpty(productList)){
					productList.each{productEntry->
						conditionList.clear();
						tempMap =[:];
						 initial="";
						tempMap["productCode"]="";
						tempMap["productName"]="";
						tempMap["initialQty"]=BigDecimal.ZERO;
						tempMap["finalQty"]=BigDecimal.ZERO;
						tempMap["diffQty"]=BigDecimal.ZERO;
						product = delegator.findOne("Product",UtilMisc.toMap("productId", productEntry.productId), false);
						if(UtilValidate.isNotEmpty(product)){
							tempMap["productName"]=product.productName;
							tempMap["productCode"]=(product.internalName).substring(0, 8);
						}
						tempMap["finalQty"]=productEntry.quantity;
						OrderItemAttr = EntityUtil.getFirst(delegator.findByAnd("OrderItemAttribute", UtilMisc.toMap("orderId",orderId,"attrName","INDENTQTY_FOR:"+productEntry.productId)));
						if(UtilValidate.isNotEmpty(OrderItemAttr)){
							initial=OrderItemAttr.attrValue;
							tempMap["initialQty"]=new BigDecimal(initial);
						}
						if(tempMap["initialQty"]!=null && tempMap["finalQty"]!=null){
							
						tempMap["diffQty"] = (tempMap["initialQty"]).subtract(tempMap["finalQty"]);
						}
						
						ProductWiseMap.put(productEntry.productId, tempMap);
						
					}
					finalMap.put(orderId, ProductWiseMap);
				}
			}
	}
	context.IndentVsDispatchMap=finalMap;
