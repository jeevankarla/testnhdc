/*******************************************************************************
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
 *******************************************************************************/
package in.vasista.vbiz.production;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.NullPointerException;
import java.lang.SecurityException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.rmi.server.ServerCloneException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;
import javax.xml.transform.stream.StreamSource;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;

import java.util.Random;
import java.util.Map.Entry;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.MimeConstants;
import org.jdom.JDOMException;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilGenerics;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.collections.MapComparator;
import org.ofbiz.base.util.collections.MapStack;
import org.ofbiz.base.util.string.FlexibleStringExpander;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityJoinOperator;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.webapp.view.ApacheFopWorker;
import org.ofbiz.widget.fo.FoScreenRenderer;
import org.ofbiz.widget.html.HtmlScreenRenderer;
import org.ofbiz.widget.screen.ScreenRenderer;

import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFReader;

import org.xml.sax.SAXException;

import in.vasista.vbiz.procurement.ProcurementNetworkServices;
import org.ofbiz.entity.transaction.TransactionUtil;

/**
 * Procurement Services
 */
public class ProductionServices {

	public static final String module = ProductionServices.class.getName();
	public static final String resource = "CommonUiLabels";
	protected static final HtmlScreenRenderer htmlScreenRenderer = new HtmlScreenRenderer();
    protected static final FoScreenRenderer foScreenRenderer = new FoScreenRenderer();
    
    /**
     * returns Opening Balance map for the given Silo(Facility) as on that Date
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getSiloInventoryOpeningBalance(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Timestamp effectiveDate = (Timestamp)context.get("effectiveDate");
        String facilityId = (String)context.get("facilityId");
        String productId = (String)context.get("productId");
        Map<String, Object> result = FastMap.newInstance();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        GenericValue facility = null;
        try {
            facility = delegator.findByPrimaryKey("Facility", UtilMisc.toMap("facilityId", facilityId));
            
            if(UtilValidate.isEmpty(productId)){
            	List<GenericValue> productFacility = delegator.findList("ProductFacility", EntityCondition.makeCondition("facilityId", EntityOperator.EQUALS, facilityId), null, null, null, false);
            	if(UtilValidate.isNotEmpty(productFacility)){
            		productId = (EntityUtil.getFirst(productFacility)).getString("productId");
            	}
            	
            }
            
        } catch (GenericEntityException e) {
        	Debug.logError(e, module);
            return ServiceUtil.returnError("Error fetching data " + e);
        }
        
        if(UtilValidate.isEmpty(effectiveDate)){
        	effectiveDate = UtilDateTime.nowTimestamp();
        }
        
        if(UtilValidate.isEmpty(facility)){
        	Debug.logError("Silo with code "+facilityId+" doesn't exists", module);
        	return ServiceUtil.returnError("Silo with code "+facilityId+" doesn't exists");
        }
        
        
        List conditionList = FastList.newInstance();
        conditionList.add(EntityCondition.makeCondition("effectiveDate", EntityOperator.LESS_THAN, effectiveDate));
        conditionList.add(EntityCondition.makeCondition("facilityId", EntityOperator.EQUALS, facilityId));
        conditionList.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId));
        EntityCondition condition = EntityCondition.makeCondition(conditionList, EntityOperator.AND);
        

        BigDecimal inventoryCount = BigDecimal.ZERO;
        BigDecimal inventoryKgFat = BigDecimal.ZERO;
        BigDecimal inventoryKgSnf = BigDecimal.ZERO;
        EntityListIterator eli = null;
        Map openingBalanceMap = FastMap.newInstance();
        
        try {
            eli = delegator.find("InventoryItemAndDetail", condition, null, null, UtilMisc.toList("effectiveDate"), null);
            
            GenericValue inventoryTrans;
            while ((inventoryTrans = eli.next()) != null) {
            	BigDecimal quantityOnHandDiff = inventoryTrans.getBigDecimal("quantityOnHandDiff");
                BigDecimal fatPercent = inventoryTrans.getBigDecimal("fatPercent");
                BigDecimal snfPercent = inventoryTrans.getBigDecimal("snfPercent");
                
                BigDecimal kgFat =BigDecimal.ZERO;
                BigDecimal kgSnf =BigDecimal.ZERO;
                kgFat = ProcurementNetworkServices.calculateKgFatOrKgSnf(inventoryCount, fatPercent);
                kgSnf = ProcurementNetworkServices.calculateKgFatOrKgSnf(inventoryCount, fatPercent);
               
                inventoryCount = inventoryCount.add(quantityOnHandDiff);
                inventoryKgFat = inventoryKgFat.add(kgFat);
                inventoryKgSnf = inventoryKgSnf.add(kgSnf);
            }
            BigDecimal fatPercent = BigDecimal.ZERO;
            BigDecimal snfPercent = BigDecimal.ZERO;
            
            fatPercent = ProcurementNetworkServices.calculateFatOrSnf(inventoryKgFat, inventoryCount);
            snfPercent = ProcurementNetworkServices.calculateFatOrSnf(inventoryKgSnf, inventoryCount);
            
            openingBalanceMap.put("fat",fatPercent);
            openingBalanceMap.put("snf",snfPercent);
            openingBalanceMap.put("kgFat",inventoryKgFat);
            openingBalanceMap.put("kgSnf",inventoryKgSnf);
            openingBalanceMap.put("productId",productId);
            openingBalanceMap.put("quantityKgs",inventoryCount);
            eli.close();
        }
        catch(GenericEntityException e){
        	Debug.logError(e, module);
        	return ServiceUtil.returnError(e.toString());
        }
        result.put("openingBalance",openingBalanceMap);
        return result;
    }// End of the Service
    
    public static Map<String, Object> getProductionDetails(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        String workEffortId = (String)context.get("workEffortId");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Map<String, Object> result = FastMap.newInstance();
            EntityListIterator returnItems = null;
            EntityListIterator eli = null;
            List issuedProductsList = FastList.newInstance();
            List declareProductsList = FastList.newInstance();
            List returnProductsList = FastList.newInstance();
            try {
            	List<GenericValue> workEffort = delegator.findList("WorkEffort", EntityCondition.makeCondition("workEffortParentId", EntityOperator.EQUALS, workEffortId), null, UtilMisc.toList("workEffortId"), null, false);
            	if(UtilValidate.isNotEmpty(workEffort)){
		        	List<String> workEffortIds = EntityUtil.getFieldListFromEntityList(workEffort, "workEffortId", true);
		            eli = delegator.find("InventoryItemAndDetail", EntityCondition.makeCondition("workEffortId", EntityOperator.IN, workEffortIds), null, null, null, null);
		            GenericValue productionTrans;
		            while ((productionTrans = eli.next()) != null) {
		                Map issuedProductsMap = FastMap.newInstance();
		                Map declareProductsMap = FastMap.newInstance();
		                BigDecimal fatPercent = BigDecimal.ZERO;
		                BigDecimal snfPercent = BigDecimal.ZERO;
		                String productId = productionTrans.getString("productId");
		            	BigDecimal quantityOnHandDiff = productionTrans.getBigDecimal("quantityOnHandDiff");
		            	if(UtilValidate.isNotEmpty(productionTrans.getBigDecimal("fatPercent"))){
		            	fatPercent = productionTrans.getBigDecimal("fatPercent");
		            	}
		            	if(UtilValidate.isNotEmpty(productionTrans.getBigDecimal("snfPercent"))){
		            	snfPercent = productionTrans.getBigDecimal("snfPercent");
		            	}
		            	String returnId = productionTrans.getString("returnId");
		             	if((quantityOnHandDiff.compareTo(BigDecimal.ZERO) <= 0) && (UtilValidate.isEmpty(returnId))){
		             		issuedProductsMap.put("issuedProdId",productId);
		             		issuedProductsMap.put("issuedQty",quantityOnHandDiff);
		             		issuedProductsMap.put("fatPercent",fatPercent);
		             		issuedProductsMap.put("snfPercent",snfPercent);
		             		issuedProductsList.add(issuedProductsMap);
		                } else if((quantityOnHandDiff.compareTo(BigDecimal.ZERO) >= 0) && (UtilValidate.isEmpty(returnId))){
		                    String productBatchId = productionTrans.getString("productBatchId");
		                 	if(UtilValidate.isNotEmpty(productBatchId)){
		                    	List<GenericValue> productBatchSequence = delegator.findList("ProductBatchSequence", EntityCondition.makeCondition("productBatchId", EntityOperator.EQUALS, productBatchId), null, UtilMisc.toList("sequenceId"), null, false);
		                     	if(UtilValidate.isNotEmpty(productBatchSequence)){
		                     		String batchNo = (EntityUtil.getFirst(productBatchSequence)).getString("sequenceId");
		                 		    declareProductsMap.put("batchNo",batchNo);
		                     	}
		                 	}
		             		declareProductsMap.put("declareProdId",productId);
		             		declareProductsMap.put("declareQty",quantityOnHandDiff);
		             		declareProductsMap.put("fatPercent",fatPercent);
		             		declareProductsMap.put("snfPercent",snfPercent);
		             		declareProductsList.add(declareProductsMap);
		             	} 
		            }
		            eli.close();
		            
		            //List<String> orderBy = UtilMisc.toList("returnId","returnItemSeqId");
	            	returnItems = delegator.find("ReturnHeaderAndItem", EntityCondition.makeCondition("workEffortId", EntityOperator.IN, workEffortIds), null, null, null, null);
	            	if(UtilValidate.isNotEmpty(returnItems)){
			            GenericValue returnTrans;
			            while ((returnTrans = returnItems.next()) != null) {
			                Map returnProductsMap = FastMap.newInstance();
			            	String returnId = returnTrans.getString("returnId");
			            	String returnItemSeqId = returnTrans.getString("returnItemSeqId");
			                String returnProdId = returnTrans.getString("productId");
			            	BigDecimal returnQty = returnTrans.getBigDecimal("returnQuantity");
			            	if(UtilValidate.isNotEmpty(returnId)){
			             		returnProductsMap.put("returnId",returnId);
			             		returnProductsMap.put("returnItemSeqId",returnItemSeqId);
			             		returnProductsMap.put("returnProdId",returnProdId);
			             		returnProductsMap.put("returnQty",returnQty);
			             		returnProductsList.add(returnProductsMap);
			             	}
			            }
	            	}
	            	returnItems.close();

            	}
            }
            catch(GenericEntityException e){
            	Debug.logError(e, module);
            	return ServiceUtil.returnError(e.toString());
            }
            result.put("issuedProductsList",issuedProductsList);
            result.put("declareProductsList",declareProductsList);
            result.put("returnProductsList",returnProductsList);
        	
        return result;
    }// End of the Service
    
    public static String issueRoutingTaskNeededMaterial(HttpServletRequest request, HttpServletResponse response) {
	  	  Delegator delegator = (Delegator) request.getAttribute("delegator");
	  	  LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
	  	  Locale locale = UtilHttp.getLocale(request);
	  	  Map<String, Object> result = ServiceUtil.returnSuccess();
	  	  HttpSession session = request.getSession();
	  	  GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");
	      Timestamp nowTimeStamp = UtilDateTime.nowTimestamp();	 
	  	  Map<String, Object> paramMap = UtilHttp.getParameterMap(request);
	  	  int rowCount = UtilHttp.getMultiFormRowCount(paramMap);
	  	  if (rowCount < 1) {
	  		  Debug.logError("No rows to process, as rowCount = " + rowCount, module);
			  request.setAttribute("_ERROR_MESSAGE_", "No rows to process");	  		  
	  		  return "error";
	  	  }
	  	  String workEffortId = (String) request.getParameter("workEffortId");
	  	  boolean beginTransaction = false;
	  	  try{
	  		  beginTransaction = TransactionUtil.begin();
		  	  for (int i = 0; i < rowCount; i++){
		  		  
		  		  String facilityId = "";
		  		  String productId = "";
		  		  String thisSuffix = UtilHttp.MULTI_ROW_DELIMITER + i;
		  		  BigDecimal quantity = BigDecimal.ZERO;
		  		  String quantityStr = "";
		  		  if (paramMap.containsKey("facilityId" + thisSuffix)) {
		  			  facilityId = (String) paramMap.get("facilityId" + thisSuffix);
		  		  }
		  		  if (paramMap.containsKey("productId" + thisSuffix)) {
		  			  productId = (String) paramMap.get("productId"+thisSuffix);
		  		  }
		  		  if (paramMap.containsKey("quantity" + thisSuffix)) {
		  			quantityStr = (String) paramMap.get("quantity"+thisSuffix);
		  		  }
		  		  
		  		  if(UtilValidate.isNotEmpty(quantityStr)){
		  			  quantity = new BigDecimal(quantityStr);
		  		  }
		  		  
			  		GenericValue productFacility = delegator.findOne("ProductFacility", UtilMisc.toMap("productId", productId, "facilityId", facilityId), false);
	                if(UtilValidate.isEmpty(productFacility)){
	                	Debug.logError("Material not associated to the Plant/Silo: "+facilityId, module);
	                	TransactionUtil.rollback();
	            		return "error";
	                }
	                
	                Map inputCtx = FastMap.newInstance();
	                inputCtx.put("productId", productId);
	                inputCtx.put("facilityId", facilityId);
	                inputCtx.put("quantity", quantity);
	                inputCtx.put("workEffortId", workEffortId);
	                inputCtx.put("userLogin", userLogin);
	                Map resultCtx = dispatcher.runSync("issueProductionRunTaskComponent", inputCtx);
	                if(ServiceUtil.isError(resultCtx)){
	                	Debug.logError("Error issuing material for routing task : "+workEffortId, module);
	                	TransactionUtil.rollback();
	            		return "error";
	                }
		  	  }
	  	  }
	  	catch (GenericEntityException e) {
			try {
				TransactionUtil.rollback(beginTransaction, "Error Fetching data", e);
	  		} catch (GenericEntityException e2) {
	  			Debug.logError(e2, "Could not rollback transaction: " + e2.toString(), module);
	  		}
	  		Debug.logError("An entity engine error occurred while fetching data", module);
	  	}
  	  	catch (GenericServiceException e) {
  	  		try {
  			  TransactionUtil.rollback(beginTransaction, "Error while calling services", e);
  	  		} catch (GenericEntityException e2) {
  			  Debug.logError(e2, "Could not rollback transaction: " + e2.toString(), module);
  	  		}
  	  		Debug.logError("An entity engine error occurred while calling services", module);
  	  	}
	  	finally {
	  		try {
	  			TransactionUtil.commit(beginTransaction);
	  		} catch (GenericEntityException e) {
	  			Debug.logError(e, "Could not commit transaction for entity engine error occurred while fetching data", module);
	  		}
	  	}
	  	request.setAttribute("_EVENT_MESSAGE_", "Successfully made issue of material for Task :"+workEffortId);
		return "success";  
    }
    
    public static String returnUnusedMaterialOfRoutingTask(HttpServletRequest request, HttpServletResponse response) {
	  	  Delegator delegator = (Delegator) request.getAttribute("delegator");
	  	  LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
	  	  Locale locale = UtilHttp.getLocale(request);
	  	  Map<String, Object> result = ServiceUtil.returnSuccess();
	  	  HttpSession session = request.getSession();
	  	  GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");
	      Timestamp nowTimestamp = UtilDateTime.nowTimestamp();	 
	  	  Map<String, Object> paramMap = UtilHttp.getParameterMap(request);
	  	  int rowCount = UtilHttp.getMultiFormRowCount(paramMap);
	  	  if (rowCount < 1) {
	  		  Debug.logError("No rows to process, as rowCount = " + rowCount, module);
			  request.setAttribute("_ERROR_MESSAGE_", "No rows to process");	  		  
	  		  return "error";
	  	  }
	  	  String workEffortId = (String) request.getParameter("workEffortId");
	  	  
	  	  boolean beginTransaction = false;
	  	  try{
	  		  beginTransaction = TransactionUtil.begin();
	  		  
	  		  GenericValue workEffort = delegator.findOne("WorkEffort", UtilMisc.toMap("workEffortId", workEffortId), false);
	  		  if(UtilValidate.isEmpty(workEffort)){
	  			  Debug.logError("Not a valid production run : "+workEffortId, module);
	  			  TransactionUtil.rollback();
	  			  return "error";
	  		  }
	  		  String facilityId = workEffort.getString("facilityId");
	  		  
	  		  GenericValue returnHeader = delegator.makeValue("ReturnHeader");
	  		  returnHeader.set("returnHeaderTypeId", "PRODUCTION_RETURN");
	  		  returnHeader.set("statusId", "RTN_INITIATED");
	  		  returnHeader.set("originFacilityId", facilityId);
	  		  returnHeader.set("entryDate", nowTimestamp);
	  		  returnHeader.set("createdBy", userLogin.getString("userLoginId"));
	  		  returnHeader.set("returnDate", nowTimestamp);
	  		  returnHeader.set("workEffortId", workEffortId);
	  		  delegator.createSetNextSeqId(returnHeader);
    		
	  		  String returnId = (String)returnHeader.get("returnId");
	  		  
	  		  for (int i = 0; i < rowCount; i++){
		  		  
		  		  String productId = "";
		  		  String returnReasonId = "";
		  		  String description = "";
		  		  String thisSuffix = UtilHttp.MULTI_ROW_DELIMITER + i;
		  		  BigDecimal quantity = BigDecimal.ZERO;
		  		  String quantityStr = "";
		  		  if (paramMap.containsKey("productId" + thisSuffix)) {
		  			  productId = (String) paramMap.get("productId"+thisSuffix);
		  		  }
		  		  if (paramMap.containsKey("quantity" + thisSuffix)) {
		  			quantityStr = (String) paramMap.get("quantity"+thisSuffix);
		  		  }
		  		  
		  		  if(UtilValidate.isNotEmpty(quantityStr)){
		  			  quantity = new BigDecimal(quantityStr);
		  		  }
		  		  if (paramMap.containsKey("returnReasonId" + thisSuffix)) {
		  			  returnReasonId = (String) paramMap.get("returnReasonId"+thisSuffix);
		  		  }
		  			if (paramMap.containsKey("description" + thisSuffix)) {
		  			  description = (String) paramMap.get("description"+thisSuffix);
		  		  }
		  			
		  			GenericValue returnItem = delegator.makeValue("ReturnItem");
		    		returnItem.set("returnReasonId", "RTN_DEFECTIVE_ITEM");
		    		if(UtilValidate.isNotEmpty(returnReasonId)){
		    			returnItem.set("returnReasonId", returnReasonId);
		    		}
		    		returnItem.set("statusId", "RTN_INITIATED");
		    		returnItem.set("returnId", returnId);
		    		returnItem.set("productId", productId);
		    		returnItem.set("returnQuantity", quantity);
		    		returnItem.set("description", description);
		    		returnItem.set("returnTypeId", "RTN_REFUND");
		    		returnItem.set("returnItemTypeId", "RET_FPROD_ITEM");
	    		    delegator.setNextSubSeqId(returnItem, "returnItemSeqId", 5, 1);
		    		delegator.create(returnItem);
		    		
		  		  /*Map inputCtx = FastMap.newInstance();
		  		  inputCtx.put("productId", productId);
		  		  inputCtx.put("quantity", quantity);
		  		  inputCtx.put("workEffortId", workEffortId);
		  		  inputCtx.put("userLogin", userLogin);
		  		  Map resultCtx = dispatcher.runSync("productionRunTaskReturnMaterial", inputCtx);
		  		  if(ServiceUtil.isError(resultCtx)){
		  			  Debug.logError("Error issuing material for routing task : "+workEffortId, module);
		  			  TransactionUtil.rollback();
		  			  return "error";
		  		  }*/
		  	  }
	  	  }
	  	catch (GenericEntityException e) {
			try {
				TransactionUtil.rollback(beginTransaction, "Error Fetching data", e);
	  		} catch (GenericEntityException e2) {
	  			Debug.logError(e2, "Could not rollback transaction: " + e2.toString(), module);
	  		}
	  		Debug.logError("An entity engine error occurred while fetching data", module);
	  	}
	  	finally {
	  		try {
	  			TransactionUtil.commit(beginTransaction);
	  		} catch (GenericEntityException e) {
	  			Debug.logError(e, "Could not commit transaction for entity engine error occurred while fetching data", module);
	  		}
	  	}
	  	request.setAttribute("_EVENT_MESSAGE_", "Successfully made return of material for Task :"+workEffortId);
		return "success";  
    }
    
    public static String declareRoutingTaskMaterial(HttpServletRequest request, HttpServletResponse response) {
	  	  Delegator delegator = (Delegator) request.getAttribute("delegator");
	  	  LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
	  	  Locale locale = UtilHttp.getLocale(request);
	  	  Map<String, Object> result = ServiceUtil.returnSuccess();
	  	  HttpSession session = request.getSession();
	  	  GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");
	      Timestamp nowTimeStamp = UtilDateTime.nowTimestamp();	 
	  	  Map<String, Object> paramMap = UtilHttp.getParameterMap(request);
	  	  int rowCount = UtilHttp.getMultiFormRowCount(paramMap);
	  	  if (rowCount < 1) {
	  		  Debug.logError("No rows to process, as rowCount = " + rowCount, module);
			  request.setAttribute("_ERROR_MESSAGE_", "No rows to process");	  		  
	  		  return "error";
	  	  }
	  	  String workEffortId = (String) request.getParameter("workEffortId");
	  	  String productionRunId = (String) request.getParameter("productionRunId");
	  	  boolean beginTransaction = false;
	  	  try{
	  		  beginTransaction = TransactionUtil.begin();
		  	  for (int i = 0; i < rowCount; i++){
		  		  
		  		  String facilityId = "";
		  		  String productId = "";
		  		  String thisSuffix = UtilHttp.MULTI_ROW_DELIMITER + i;
		  		  BigDecimal quantity = BigDecimal.ZERO;
		  		  String quantityStr = "";
		  		  if (paramMap.containsKey("productId" + thisSuffix)) {
		  			  productId = (String) paramMap.get("productId"+thisSuffix);
		  		  }
		  		  if (paramMap.containsKey("quantity" + thisSuffix)) {
		  			quantityStr = (String) paramMap.get("quantity"+thisSuffix);
		  		  }
		  		  
		  		  if(UtilValidate.isNotEmpty(quantityStr)){
		  			  quantity = new BigDecimal(quantityStr);
		  		  }
		  		  Map inputCtx = FastMap.newInstance();
		  		  inputCtx.put("productId", productId);
		  		  inputCtx.put("quantity", quantity);
		  		  inputCtx.put("inventoryItemTypeId", "NON_SERIAL_INV_ITEM");
		  		  inputCtx.put("workEffortId", workEffortId);
		  		  inputCtx.put("userLogin", userLogin);
		  		  Debug.log("inputCtx #############"+inputCtx);
		  		  Map resultCtx = dispatcher.runSync("productionRunTaskProduce", inputCtx);
		  		  if(ServiceUtil.isError(resultCtx)){
		  			  Debug.logError("Error in declaring material of routing task : "+workEffortId, module);
		  			  TransactionUtil.rollback();
		  			  return "error";
		  		  }
		  		  List inventoryItemIds = (List)resultCtx.get("inventoryItemIds");
		  		  Debug.log("inventoryItemIds ###################"+inventoryItemIds);
		  	  }
	  	  }
	  	catch (GenericEntityException e) {
			try {
				TransactionUtil.rollback(beginTransaction, "Error Fetching data", e);
	  		} catch (GenericEntityException e2) {
	  			Debug.logError(e2, "Could not rollback transaction: " + e2.toString(), module);
	  		}
	  		Debug.logError("An entity engine error occurred while fetching data", module);
	  	}
	  	catch (GenericServiceException e) {
	  		try {
			  TransactionUtil.rollback(beginTransaction, "Error while calling services", e);
	  		} catch (GenericEntityException e2) {
			  Debug.logError(e2, "Could not rollback transaction: " + e2.toString(), module);
	  		}
	  		Debug.logError("An entity engine error occurred while calling services", module);
	  	}
	  	finally {
	  		try {
	  			TransactionUtil.commit(beginTransaction);
	  		} catch (GenericEntityException e) {
	  			Debug.logError(e, "Could not commit transaction for entity engine error occurred while fetching data", module);
	  		}
	  	}
	  	request.setAttribute("_EVENT_MESSAGE_", "Successfully made issue of material for Task :"+workEffortId);
		return "success";  
    }
    
    public static Map<String, Object> changeRoutingTaskStatus(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
	  	Map<String, Object> result = ServiceUtil.returnSuccess();
	    GenericValue userLogin = (GenericValue) context.get("userLogin");
	  	String workEffortId = (String) context.get("workEffortId");
	  	String productionRunId = (String) context.get("productionRunId");
	  	String statusId = (String) context.get("statusId");
	  	try{
	  		  Map inputStatusCtx = FastMap.newInstance();
	  		  inputStatusCtx.put("productionRunId", productionRunId);
	  		  inputStatusCtx.put("workEffortId", workEffortId);
	  		  inputStatusCtx.put("statusId", statusId);
	  		  inputStatusCtx.put("userLogin", userLogin);
	  		  Map resultCtx = dispatcher.runSync("changeProductionRunTaskStatus", inputStatusCtx);
	  		  if(ServiceUtil.isError(resultCtx)){
	  			  Debug.logError("Error changing routing task status : "+workEffortId, module);
	  			  return ServiceUtil.returnError("Error changing routing task status : "+workEffortId);
	  		  }
	  		  String newStatusId = (String)resultCtx.get("newStatusId");
			  String oldStatusId = (String)resultCtx.get("oldStatusId");
	  	 
	  	}catch(GenericServiceException e){
        	Debug.logError(e, module);
        	return ServiceUtil.returnError(e.toString());
	  	}
	  	result.put("productionRunId",productionRunId);
	  	return result;
     }
    
     public static Map<String, Object> confirmProductionRunStatus(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        String productionRunId = (String)context.get("productionRunId");
        Map<String, Object> result = FastMap.newInstance();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        try {
            Map inputCtx = FastMap.newInstance();
            inputCtx.put("productionRunId", productionRunId);
            inputCtx.put("statusId", "PRUN_DOC_PRINTED");
            inputCtx.put("userLogin", userLogin);
            Map resultCtx = dispatcher.runSync("changeProductionRunStatus", inputCtx);
	  		if(ServiceUtil.isError(resultCtx)){
	  			Debug.logError("Error changing production run status to confirmed: "+productionRunId, module);
	  			return ServiceUtil.returnError("Error changing production run status to confirmed: "+productionRunId);
	  		}
	  		String newStatusId = (String)resultCtx.get("newStatusId");
        }
        catch(GenericServiceException e){
        	Debug.logError(e, module);
        	return ServiceUtil.returnError(e.toString());
        }
        result.put("productionRunId",productionRunId);
        return result;
    }// End of the Service
     
     public static Map<String, Object> createStockXferRequest(DispatchContext dctx, Map<String, ? extends Object> context) {
         Delegator delegator = dctx.getDelegator();
         LocalDispatcher dispatcher = dctx.getDispatcher();
         String transferDate = (String)context.get("transferDate");
         String fromFacilityId = (String)context.get("fromFacilityId");
         String toFacilityId = (String)context.get("toFacilityId");
         BigDecimal xferQty = (BigDecimal)context.get("xferQty");
         String productId = (String)context.get("productId");
         String statusId = (String)context.get("statusId");
         String inventoryItemId = (String)context.get("inventoryItemId");
         String comments = (String)context.get("comment");
         Map<String, Object> result = ServiceUtil.returnSuccess();
         GenericValue userLogin = (GenericValue) context.get("userLogin");
         Timestamp xferDate = null;
         try {
        	 
        	 SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM, yyyy");
        	 if(UtilValidate.isNotEmpty(transferDate)){
        		 try {
        			 xferDate = new java.sql.Timestamp(sdf.parse(transferDate).getTime());
          		 } catch (ParseException e) {
          			Debug.logError(e, "Cannot parse date string: "+ transferDate, module);			
          			return ServiceUtil.returnError("Cannot parse date string: "+ transferDate);
          		 }
        	 }else{
        		 xferDate = UtilDateTime.nowTimestamp();
        	 }
     		 
        	 if(xferQty.compareTo(BigDecimal.ZERO)<1){
        		 Debug.logError("Transfer Qty cannot be less than 1", module);
   	  			return ServiceUtil.returnError("Transfer Qty cannot be less than 1");
        	 }
     		 
        	 List conditionList = FastList.newInstance();
        	 
        	 if(UtilValidate.isEmpty(toFacilityId)){
        		 conditionList.add(EntityCondition.makeCondition("facilityId", EntityOperator.EQUALS, toFacilityId));
        		 conditionList.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId));
        		 EntityCondition cond = EntityCondition.makeCondition(conditionList, EntityOperator.AND);
             	 List<GenericValue> productFacility = delegator.findList("ProductFacility", cond, null, null, null, false);
             	 if(UtilValidate.isEmpty(productFacility)){
             		Debug.logError("ProductId[ "+productId+" ] is not associated to store :"+toFacilityId, module);	
             		return ServiceUtil.returnError("ProductId[ "+productId+" ] is not associated to store :"+toFacilityId);
             	 }
             }
             
             Map<String, ? extends Object> findCurrInventoryParams =  UtilMisc.toMap("productId", productId, "facilityId", fromFacilityId);
             
             Map<String, Object> resultCtx = dispatcher.runSync("getInventoryAvailableByFacility", findCurrInventoryParams);
             if (ServiceUtil.isError(resultCtx)) {
             	Debug.logError("Problem getting inventory level of the request for product Id :"+productId, module);
                 return ServiceUtil.returnError("Problem getting inventory level of the request for product Id :"+productId);
             }
             Object qohObj = resultCtx.get("quantityOnHandTotal");
             BigDecimal qoh = BigDecimal.ZERO;
             if (qohObj != null) {
             	qoh = new BigDecimal(qohObj.toString());
             }
             
             if (xferQty.compareTo(qoh) > 0) {
             	Debug.logError("Available Inventory level for productId : "+productId + " is "+qoh, module);
                 return ServiceUtil.returnError("Available Inventory level for productId : "+productId + " is "+qoh);
             }
        	 
             conditionList.clear();
        	 conditionList.add(EntityCondition.makeCondition("facilityId", EntityOperator.EQUALS, fromFacilityId));
        	 conditionList.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId));
        	 if(UtilValidate.isNotEmpty(inventoryItemId)){	
        		 conditionList.add(EntityCondition.makeCondition("inventoryItemId", EntityOperator.EQUALS, inventoryItemId));
        	 }
        	 conditionList.add(EntityCondition.makeCondition("quantityOnHandTotal", EntityOperator.GREATER_THAN, BigDecimal.ZERO));
        	 EntityCondition condition = EntityCondition.makeCondition(conditionList, EntityOperator.AND);
        	 List<GenericValue> inventoryItems = delegator.findList("InventoryItem", condition, null, UtilMisc.toList("datetimeReceived"), null, false);
        	 BigDecimal requestedQty = xferQty;
        	 
        	 GenericValue newEntity = delegator.makeValue("InventoryTransferGroup");
        	 newEntity.set("transferGroupTypeId", "_NA_");
 	         newEntity.set("statusId", "IXF_REQUESTED");
 	         delegator.createSetNextSeqId(newEntity);
 	         
 	         String transferGroupId = (String) newEntity.get("transferGroupId");
        	 List<GenericValue> inventoryItemDetail = null;
        	 Iterator<GenericValue> itr = inventoryItems.iterator();
        	 
        	 while ((requestedQty.compareTo(BigDecimal.ZERO) > 0) && itr.hasNext()) {
                 GenericValue inventoryItem = itr.next();
                 String invItemId = inventoryItem.getString("inventoryItemId");
                 BigDecimal itemQOH = inventoryItem.getBigDecimal("quantityOnHandTotal");
                 BigDecimal xferQuantity = null;
                 if (requestedQty.compareTo(itemQOH) >= 0) {	
                	 xferQuantity = itemQOH;
                 } else {
                	 xferQuantity = requestedQty;
                 }
                 
                 Map inputCtx = FastMap.newInstance();
                 inputCtx.put("statusId", statusId);
                 inputCtx.put("comments", comments);
                 inputCtx.put("facilityId", fromFacilityId);
                 inputCtx.put("facilityIdTo", toFacilityId);
                 inputCtx.put("inventoryItemId", invItemId);
                 inputCtx.put("xferQty", xferQuantity);
                 inputCtx.put("userLogin", userLogin);
                 Map resultMap = dispatcher.runSync("createInventoryTransfer", inputCtx);
     	  		 if(ServiceUtil.isError(resultMap)){
     	  			Debug.logError("Error in processing transfer entry ", module);
     	  			return ServiceUtil.returnError("Error in processing transfer entry ");
     	  		 }
     	  		 
     	  		 requestedQty = requestedQty.subtract(xferQuantity);
     	  		 
     	  		 String inventoryTransferId = (String)resultMap.get("inventoryTransferId");
     	  		 
     	  		inventoryItemDetail = delegator.findList("InventoryItemDetail", EntityCondition.makeCondition("inventoryItemId", EntityOperator.EQUALS, invItemId), null, null, null, false);
     	  		
     	  		if(UtilValidate.isNotEmpty(inventoryItemDetail)){
     	  			for(GenericValue invDetail : inventoryItemDetail){
     	  				invDetail.set("inventoryTransferId", inventoryTransferId);
     	  				invDetail.store();
     	  			}
     	  		}
     	  		
     	  		GenericValue newEntityMember = delegator.makeValue("InventoryTransferGroupMember");
     	  		newEntityMember.set("transferGroupId", transferGroupId);
     	  		newEntityMember.set("inventoryTransferId", inventoryTransferId);
     	  		newEntityMember.set("inventoryItemId", invItemId);
     	  		newEntityMember.set("productId", productId);
    	        newEntityMember.set("xferQty", xferQuantity);
    	        newEntityMember.set("createdDate", UtilDateTime.nowTimestamp());
    	        newEntityMember.set("createdByUserLogin", userLogin.get("userLoginId"));
    	        newEntityMember.set("lastModifiedDate", UtilDateTime.nowTimestamp());
    	        newEntityMember.set("lastModifiedByUserLogin", userLogin.get("userLoginId"));
    	        newEntityMember.create();
             }
         }
         catch(Exception e){
         	Debug.logError(e, module);
         	return ServiceUtil.returnError(e.toString());
         }
         return result;
     }
     public static String updateTransferGroupStatus(HttpServletRequest request, HttpServletResponse response) {
	  	  Delegator delegator = (Delegator) request.getAttribute("delegator");
	  	  LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
	  	  Locale locale = UtilHttp.getLocale(request);
	  	  Map<String, Object> result = ServiceUtil.returnSuccess();
	  	  HttpSession session = request.getSession();
	  	  GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");
	      Timestamp nowTimeStamp = UtilDateTime.nowTimestamp();	 
	  	  Map<String, Object> paramMap = UtilHttp.getParameterMap(request);
	  	  int rowCount = UtilHttp.getMultiFormRowCount(paramMap);
	  	  if (rowCount < 1) {
	  		  Debug.logError("No rows to process, as rowCount = " + rowCount, module);
			  request.setAttribute("_ERROR_MESSAGE_", "No rows to process");	  		  
	  		  return "error";
	  	  }
	  	  boolean beginTransaction = false;
	  	  try{
	  		  beginTransaction = TransactionUtil.begin();
		  	  for (int i = 0; i < rowCount; i++){
		  		  
		  		  String transferGroupId = "";
		  		  String statusId = "";
		  		  String thisSuffix = UtilHttp.MULTI_ROW_DELIMITER + i;
		  		  if (paramMap.containsKey("transferGroupId" + thisSuffix)) {
		  			  transferGroupId = (String) paramMap.get("transferGroupId" + thisSuffix);
		  		  }else{
		  			Debug.logError("Transfer Group Id cannot be empty", module);
                	TransactionUtil.rollback();
            		return "error";
		  		  }
		  		  
		  		  if (paramMap.containsKey("statusId" + thisSuffix)) {
		  			  statusId = (String) paramMap.get("statusId"+thisSuffix);
		  		  }
		  		  else{
		  			Debug.logError("status cannot be empty", module);
                	TransactionUtil.rollback();
            		return "error";
		  		  }
		  		  
		  		  GenericValue transferGroup = delegator.findOne("InventoryTransferGroup", UtilMisc.toMap("transferGroupId", transferGroupId), false);
		  		  
		  		  String oldStatusId = transferGroup.getString("statusId");
		  		  
		  		  GenericValue statusItem = delegator.findOne("StatusValidChange", UtilMisc.toMap("statusId", oldStatusId, "statusIdTo", statusId), false);
		  		  
		  		  if(UtilValidate.isEmpty(statusItem)){
		  			  Debug.logError("Not a valid status change", module);
		  			  TransactionUtil.rollback();
		  			  return "error";
		  		  }
		  		  
		  		  transferGroup.set("statusId", statusId);
		  		  transferGroup.store();
		  		  
		  		  List<GenericValue> transferGroupMembers = delegator.findList("InventoryTransferGroupMember", EntityCondition.makeCondition("transferGroupId", EntityOperator.EQUALS, transferGroupId), null, null, null, false);
		  		  
		  		  List<String> inventoryTransferIds = EntityUtil.getFieldListFromEntityList(transferGroupMembers, "inventoryTransferId", true);
		  		  
		  		  List<GenericValue> inventoryTransfers = delegator.findList("InventoryTransfer", EntityCondition.makeCondition("inventoryTransferId", EntityOperator.IN, inventoryTransferIds), null, null, null, false);
		  		  
		  		  for(GenericValue eachTransfer : inventoryTransfers){
		  			  Map inputCtx = FastMap.newInstance();
		              inputCtx.put("inventoryTransferId", eachTransfer.getString("inventoryTransferId"));
		              inputCtx.put("inventoryItemId", eachTransfer.getString("inventoryItemId"));
		              inputCtx.put("statusId", statusId);
		              inputCtx.put("userLogin", userLogin);
		              Map resultCtx = dispatcher.runSync("updateInventoryTransfer", inputCtx);
		              if(ServiceUtil.isError(resultCtx)){
		            	  Debug.logError("Error updating inventory transfer status : "+eachTransfer.getString("inventoryTransferId"), module);
		            	  TransactionUtil.rollback();
		            	  return "error";
		              }
		  		  }
		  	  }
	  	  }
	  	catch (GenericEntityException e) {
			try {
				TransactionUtil.rollback(beginTransaction, "Error Fetching data", e);
	  		} catch (GenericEntityException e2) {
	  			Debug.logError(e2, "Could not rollback transaction: " + e2.toString(), module);
	  		}
	  		Debug.logError("An entity engine error occurred while fetching data", module);
	  	}
 	  	catch (GenericServiceException e) {
 	  		try {
 			  TransactionUtil.rollback(beginTransaction, "Error while calling services", e);
 	  		} catch (GenericEntityException e2) {
 			  Debug.logError(e2, "Could not rollback transaction: " + e2.toString(), module);
 	  		}
 	  		Debug.logError("An entity engine error occurred while calling services", module);
 	  	}
	  	finally {
	  		try {
	  			TransactionUtil.commit(beginTransaction);
	  		} catch (GenericEntityException e) {
	  			Debug.logError(e, "Could not commit transaction for entity engine error occurred while fetching data", module);
	  		}
	  	}
	  	request.setAttribute("_EVENT_MESSAGE_", "Entry Successfully");
		return "success";  
     }
     
}