package in.vasista.vbiz.depotsales;
import java.text.DateFormat;

import in.vasista.vbiz.depotsales.DepotPurchaseServices;
import in.vasista.vbiz.depotsales.DepotHelperServices;
import in.vasista.vbiz.depotsales.DepotSalesServices;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.HashMap;
import java.util.Calendar;

import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.order.order.OrderChangeHelper;
import org.ofbiz.order.shoppingcart.CheckOutHelper;
import org.ofbiz.order.shoppingcart.product.ProductPromoWorker;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.ofbiz.entity.GenericDelegator;

import javolution.util.FastList;
import javolution.util.FastMap;

import java.util.Iterator;

import org.ofbiz.order.shoppingcart.ShoppingCart;
import org.ofbiz.order.shoppingcart.ShoppingCartEvents;
import org.ofbiz.order.shoppingcart.ShoppingCartItem;
import org.ofbiz.base.conversion.NumberConverters.BigDecimalToString;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilNumber;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.security.Security;
import org.ofbiz.party.contact.ContactMechWorker;
import org.ofbiz.order.order.OrderReadHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.party.party.PartyHelper;

import java.util.Map.Entry;

import in.vasista.vbiz.byproducts.ByProductNetworkServices;

public class paymentApiServices {
	
	
	public static final String module = paymentApiServices.class.getName();
	private static int decimals;
	private static int rounding;
    public static final String resource = "AccountingUiLabels";
    
    static {
        decimals = 2;// UtilNumber.getBigDecimalScale("order.decimals");
        rounding = UtilNumber.getBigDecimalRoundingMode("order.rounding");

        // set zero to the proper scale
        //if (decimals != -1) ZERO = ZERO.setScale(decimals);
    }
    
    /*
     * Security check to make userLogin partyId must equal facility owner party Id if the user
     * is a retailer (has MOB_RTLR_DB_VIEW). If user is a sales rep (MOB_SREP_DB_VIEW permission), 
     * then we just return true.
     */
    static boolean hasFacilityAccess(DispatchContext dctx, Map<String, ? extends Object> context) {  
        Security security = dctx.getSecurity();
    	GenericValue userLogin = (GenericValue) context.get("userLogin");
    	GenericValue party = (GenericValue) context.get("party");
        if (security.hasEntityPermission("MOB_SREP_DB", "_VIEW", userLogin)) {
            return true;
        } 		
        if (security.hasEntityPermission("MOB_RTLR_DB", "_VIEW", userLogin)) {
        	if (userLogin != null && userLogin.get("partyId") != null) {
        		String userLoginParty = (String)userLogin.get("partyId");
        		String ownerParty = (String)party.get("partyId");
        		if (userLoginParty.equals(ownerParty)) {
        			return true;
        		}
        	}
        }
    	return false;
    }
    
    
    public static Map<String, Object> makeOrderPayment(DispatchContext dctx,Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		try {
			Map<String, Object> resultResult = dispatcher.runSync("createOrderPayment", context);
			if (ServiceUtil.isError(resultResult)) {
				Debug.logWarning("There was an error making Payment: "
						+ ServiceUtil.getErrorMessage(resultResult), module);
				return ServiceUtil
						.returnError("There was an error making Payment:"
								+ ServiceUtil.getErrorMessage(resultResult));
			}
			result = resultResult;
		} catch (GenericServiceException e) {
			Debug.logError(e, "Error calling payment service",
					module);
		}
		return result;
	}
    
    public static Map<String, Object> makeInvoicePayment(DispatchContext dctx,Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		try {
			Map<String, Object> resultResult = dispatcher.runSync("createInvoiceApplyPayment", context);
			if (ServiceUtil.isError(resultResult)) {
				Debug.logWarning("There was an error making Payment: "
						+ ServiceUtil.getErrorMessage(resultResult), module);
				return ServiceUtil
						.returnError("There was an error making Payment:"
								+ ServiceUtil.getErrorMessage(resultResult));
			}
			result = resultResult;
		} catch (GenericServiceException e) {
			Debug.logError(e, "Error calling payment service",
					module);
		}
		return result;
	}
    
    
    
    
    public static Map<String, Object> makeWeaverPayment(DispatchContext dctx,Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Map<String, Object> result = ServiceUtil.returnSuccess();
    
		 String paymentDate = (String) context.get("paymentDate");
		 String partyIdFrom = (String) context.get("partyId");
		 String partyIdTo = "";
		 String amount = (String) context.get("amount");
		 String paymentRefNum = (String) context.get("transactionId");
		 GenericValue userLogin = (GenericValue) context.get("userLogin");
		 Timestamp eventDate = null;
		 Timestamp nowTimeStamp=UtilDateTime.nowTimestamp();
		  	
	      if (UtilValidate.isNotEmpty(paymentDate)) {
		      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");  
				try {
					eventDate = new java.sql.Timestamp(sdf.parse(paymentDate).getTime());
				} catch (ParseException e) {
					Debug.logError(e, "Cannot parse date string: " + paymentDate, module);
				} catch (NullPointerException e) {
					Debug.logError(e, "Cannot parse date string: " + paymentDate, module);
				}
			}
	      
	      
	      
	      if (UtilValidate.isEmpty(paymentDate)) {
	    	  eventDate = UtilDateTime.nowTimestamp();
	      }
	      GenericValue partyList = null;
	        if(UtilValidate.isNotEmpty(partyIdFrom)){
	        	List condList =FastList.newInstance();
	   	    	condList.add(EntityCondition.makeCondition("partyIdTo", EntityOperator.EQUALS, partyIdFrom));
	   	    	
	   	    	condList.add(EntityCondition.makeCondition("fromDate", EntityOperator.LESS_THAN_EQUAL_TO, nowTimeStamp));
	   	    	condList.add(EntityCondition.makeCondition(EntityCondition.makeCondition("thruDate", EntityOperator.EQUALS, null),EntityOperator.OR,
	   	    			 EntityCondition.makeCondition("thruDate", EntityOperator.GREATER_THAN, nowTimeStamp)));
	   	    	EntityCondition prodStrFacCondition = EntityCondition.makeCondition(condList, EntityOperator.AND);
	   	    	try{
	   	    		partyList =EntityUtil.getFirst( delegator.findList("PartyRelationship", prodStrFacCondition, null, null, null, false));
	   	    	}catch (GenericEntityException e) {
	   				// TODO: handle exception
	   	    		Debug.logError(e, module);
	   			}
	   	    }
	        if(UtilValidate.isNotEmpty(partyIdFrom)){
	        partyIdTo= partyList.getString("partyIdFrom");
	        }
		    Map<String, Object> paymentParams = new HashMap<String, Object>();
		    paymentParams.put("paymentTypeId", "ONACCOUNT_PAYIN");
		    paymentParams.put("paymentMethodTypeId", "MOBILE_PAYIN");
//		    if(UtilValidate.isNotEmpty(paymentPurposeType)){
//		  	  paymentParams.put("paymentPurposeType", paymentPurposeType);
//		    }
		   // paymentParams.put("paymentPreferenceId", orderPaymentPreference.getString("orderPaymentPreferenceId"));
		    paymentParams.put("amount", new BigDecimal(amount));
		    paymentParams.put("statusId", "PMNT_RECEIVED");
		    paymentParams.put("paymentDate", eventDate);
		    paymentParams.put("instrumentDate", eventDate);
		    paymentParams.put("partyIdFrom", partyIdFrom);
		 //   paymentParams.put("currencyUomId", productStore.getString("defaultCurrencyUomId"));
		    paymentParams.put("partyIdTo", partyIdTo);
		
		if (paymentRefNum != null) {
		    paymentParams.put("paymentRefNum", paymentRefNum);
		}
	/*	if (issuingAuthority != null) {
		    paymentParams.put("issuingAuthority", issuingAuthority);
		}
		if (comments != null) {
		    paymentParams.put("comments", comments);
		}
*/		paymentParams.put("userLogin", userLogin);
		Map<String, Object> resultResult = FastMap.newInstance();
		try{
			resultResult = dispatcher.runSync("createPayment", paymentParams);
		   if (ServiceUtil.isError(resultResult)) {
				Debug.logWarning("There was an error making Payment: "
						+ ServiceUtil.getErrorMessage(resultResult), module);
				return ServiceUtil
						.returnError("There was an error making Payment:"
								+ ServiceUtil.getErrorMessage(resultResult));
			}
		   result=resultResult;
		}catch (Exception e) {
			 Debug.logError(e, e.toString(), module);
			  return ServiceUtil.returnError("Error While Creating Payment");	
	  	   }
    
       return result;
	}
	
	
}