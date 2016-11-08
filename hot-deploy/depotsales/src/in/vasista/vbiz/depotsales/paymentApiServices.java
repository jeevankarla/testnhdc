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
	
	
}
