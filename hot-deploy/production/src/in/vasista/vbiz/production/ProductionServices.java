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
    
	
}