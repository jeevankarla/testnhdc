
<link rel="stylesheet" href="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/slick.grid.css</@ofbizContentUrl>" type="text/css" media="screen" charset="utf-8" />
<link rel="stylesheet" href="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/controls/slick.pager.css</@ofbizContentUrl>" type="text/css" media="screen" charset="utf-8" />
<link rel="stylesheet" href="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/css/smoothness/jquery-ui-1.8.5.custom.css</@ofbizContentUrl>" type="text/css" media="screen" charset="utf-8" />
<link rel="stylesheet" href="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/examples/examples.css</@ofbizContentUrl>" type="text/css" media="screen" charset="utf-8" />
<link rel="stylesheet" href="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/controls/slick.columnpicker.css</@ofbizContentUrl>" type="text/css" media="screen" charset="utf-8" />
<style type="text/css">
	.cell-title {
		font-weight: normal;
		font-color: green;
	}
	.cell-effort-driven {
		text-align: center;
	}
	.readOnlyColumnClass {
		font-weight: normal;
		background: mistyrose;
	}
	.readOnlyColumnAndWarningClass {
		font-weight: bold;
		color: red;
		background: white;
		animation: blinker 1.7s cubic-bezier(.5, 0, 1, 1) infinite alternate; 
	}
	@keyframes blinker {  
	  from { opacity: 1; }
	  to { opacity: 0; }
	}
	
});
	
	
	.righthalf {
	    float: right;
	    height: 1%;
	    margin: 0 0 1% 1%;
	    right: 0;
	    width: 69%;
	}
	
	.lefthalf {
	    float: left;
	    height: 1%;
	    left: 0;
	    margin: 0% 1% 1% 0%;
	    width: 29%;
	}
	
	.btn {
	    color:#08233e;
	    font:8em Futura, ‘Century Gothic’, AppleGothic, sans-serif;
	    font-size:100%;
	    font-weight: bold;
	    padding:14px;
	    background:url(overlay.png) repeat-x center #ffcc00;
	    background-color:rgba(900,204,0,1);
	    border:1px solid #ffcc00;
	    -moz-border-radius:10px;
	    -webkit-border-radius:10px;
	    border-radius:10px;
	    border-bottom:1px solid #9f9f9f;
	    -moz-box-shadow:inset 0 1px 0 rgba(255,255,255,0.5);
	    -webkit-box-shadow:inset 0 1px 0 rgba(255,255,255,0.5);
	    box-shadow:inset 0 1px 0 rgba(255,255,255,0.5);
	    cursor:pointer;
	    display:inline;
	}
	
	
	.btn:hover {
    	background-color:rgba(900,204,0,0.8);
	}
	.btn {
	    background-color:green;
	    cursor:pointer;
	}
	
</style>			
			
<script language="javascript" type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/lib/firebugx.js</@ofbizContentUrl>"></script>
<script language="javascript" type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/lib/jquery-1.4.3.min.js</@ofbizContentUrl>"></script>
<script language="javascript" type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/lib/jquery-ui-1.8.5.custom.min.js</@ofbizContentUrl>"></script>
<script language="javascript" type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/lib/jquery.event.drag-2.0.min.js</@ofbizContentUrl>"></script>
<script language="javascript" type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/slick.core.js</@ofbizContentUrl>"></script>
<script language="javascript" type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/slick.editors.js</@ofbizContentUrl>"></script>
<script language="javascript" type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/plugins/slick.cellrangedecorator.js</@ofbizContentUrl>"></script>
<script language="javascript" type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/plugins/slick.cellrangeselector.js</@ofbizContentUrl>"></script>
<script language="javascript" type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/plugins/slick.cellselectionmodel.js</@ofbizContentUrl>"></script>		
<script language="javascript" type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/slick.grid.js</@ofbizContentUrl>"></script>
<script language="javascript" type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/slick.groupitemmetadataprovider.js</@ofbizContentUrl>"></script>
<script language="javascript" type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/slick.dataview.js</@ofbizContentUrl>"></script>
<script language="javascript" type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/controls/slick.pager.js</@ofbizContentUrl>"></script>
<script language="javascript" type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/controls/slick.columnpicker.js</@ofbizContentUrl>"></script>
<script language="javascript" type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/validate/jquery.validate.js</@ofbizContentUrl>"></script>
<script language="javascript" type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/multiSelect/jquery.multiselect.js</@ofbizContentUrl>"></script>
<script type="application/javascript">
	var dataView;
	var dataView2;
	var grid;
	var data2 = [];
	var grid2;
	var withAdjColumns;
	
	var productQuotaJSON = ${StringUtil.wrapString(productQuotaJSON)!'{}'};
	var productCategoryJSON = ${StringUtil.wrapString(productCategoryJSON)!'{}'};
	
	var productLabelIdMap = ${StringUtil.wrapString(productLabelIdJSON)!'{}'};
	var productIdLabelMap = ${StringUtil.wrapString(productIdLabelJSON)!'{}'};
	var availableTags = ${StringUtil.wrapString(productItemsJSON)!'[]'};
	var featureAvailableTags = ${StringUtil.wrapString(featuresJSON)!'[]'};
	var priceTags = ${StringUtil.wrapString(productCostJSON)!'[]'};
	var conversionData = ${StringUtil.wrapString(conversionJSON)!'{}'};
	var data = ${StringUtil.wrapString(dataJSON)!'[]'};
	data2=${StringUtil.wrapString(data2JSON)!'[]'};
	var userDefPriceObj = ${StringUtil.wrapString(userDefPriceObj)!'[]'};
    var productQtyInc = ${StringUtil.wrapString(productQtyIncJSON)!'{}'};
	var boothAutoJson = ${StringUtil.wrapString(boothsJSON)!'[]'};
	var partyAutoJson = ${StringUtil.wrapString(partyJSON)!'[]'};	
	var branchAutoJson = ${StringUtil.wrapString(branchJSON)!'[]'};	
	var partyNameObj = ${StringUtil.wrapString(partyNameObj)!'[]'};
	var routeAutoJson = ${StringUtil.wrapString(routesJSON)!'[]'};
	var prodIndentQtyCat=${StringUtil.wrapString(prodIndentQtyCat)!'[]'};
	var qtyInPieces=${StringUtil.wrapString(qtyInPieces)!'[]'};
	
	var availableAdjTags = ${StringUtil.wrapString(orderAdjItemsJSON)!'[]'};
	var orderAdjLabelJSON = ${StringUtil.wrapString(orderAdjLabelJSON)!'{}'};
	var orderAdjLabelIdMap = ${StringUtil.wrapString(orderAdjLabelIdJSON)!'{}'};
	
	var productUOMMap = ${StringUtil.wrapString(productUOMJSON)!'{}'};
	var uomLabelMap = ${StringUtil.wrapString(uomLabelJSON)!'{}'};
	
	function requiredFieldValidator(value) {
		if (value == null || value == undefined || !value.length)
			return {valid:false, msg:"This is a required field"};
		else
			return {valid:true, msg:null};
	}
	
	function processIndentEntryInternal(formName, action) {
		if (Slick.GlobalEditorLock.isActive() && !Slick.GlobalEditorLock.commitCurrentEdit()) {
			return false;		
		}
		var formId = "#" + formName;
		var inputRowSubmit = jQuery("<input>").attr("type", "hidden").attr("name", "_useRowSubmit").val("Y");
		jQuery(formId).append(jQuery(inputRowSubmit));
		var orderId = $("#orderId").val();			
		for (var rowCount=0; rowCount < data.length; ++rowCount)
		{ 
			var productId = data[rowCount]["cProductId"];
			var prodId="";
			if(typeof(productId)!= "undefined"){ 	  
				var prodId = productId.toUpperCase();
			}
			var qty = parseFloat(data[rowCount]["quantity"]);
			var orderItemSeqId = data[rowCount]["orderItemSeqId"];
			var balqty = parseFloat(data[rowCount]["baleQuantity"]);
			var yarnUOM = data[rowCount]["cottonUom"];
			var bundleWeight = data[rowCount]["bundleWeight"];
			var batchNo = data[rowCount]["batchNo"];
			var days = data[rowCount]["daysToStore"];
			var unitPrice = data[rowCount]["unitPrice"];
			var bundleUnitPrice = data[rowCount]["KgunitPrice"];			
			var remarks = data[rowCount]["remarks"];		
			var serviceCharge = data[rowCount]["SERVICE_CHARGE"];
			var serviceChargeAmt = data[rowCount]["SERVICE_CHARGE_AMT"];			
			
			<#if changeFlag?exists && changeFlag != "EditDepotSales">
			 if(qty>0){
			</#if>
	 		if (!isNaN(prodId)) {
				var inputOrder = jQuery("<input>").attr("type", "hidden").attr("name", "orderId_o_" + rowCount).val(orderId);
				var inputProd = jQuery("<input>").attr("type", "hidden").attr("name", "productId_o_" + rowCount).val(prodId);
				var inputorderItemSeqId = jQuery("<input>").attr("type", "hidden").attr("name", "orderItemSeqId_o_" + rowCount).val(orderItemSeqId);
				var inputBaleQty = jQuery("<input>").attr("type", "hidden").attr("name", "baleQuantity_o_" + rowCount).val(balqty);
				var inputQty = jQuery("<input>").attr("type", "hidden").attr("name", "amendedQuantity_o_" + rowCount).val(qty);
				var inputYarnUOM = jQuery("<input>").attr("type", "hidden").attr("name", "yarnUOM_o_" + rowCount).val(yarnUOM);
				var inputBundleWeight = jQuery("<input>").attr("type", "hidden").attr("name", "bundleWeight_o_" + rowCount).val(bundleWeight);
				var inputUnitPrice = jQuery("<input>").attr("type", "hidden").attr("name", "amendedPrice_o_" + rowCount).val(unitPrice);
				var inputbundleUnitPrice = jQuery("<input>").attr("type", "hidden").attr("name", "bundleUnitPrice_o_" + rowCount).val(bundleUnitPrice);			
				var inputRemarks = jQuery("<input>").attr("type", "hidden").attr("name", "remarks_o_" + rowCount).val(remarks);
				var inputServChgAmt = jQuery("<input>").attr("type", "hidden").attr("name", "serviceChargeAmt_o_" + rowCount).val(serviceChargeAmt);
				var inputServChg = jQuery("<input>").attr("type", "hidden").attr("name", "serviceCharge_o_" + rowCount).val(serviceCharge);
				
				jQuery(formId).append(jQuery(inputOrder));
				jQuery(formId).append(jQuery(inputRemarks));
				jQuery(formId).append(jQuery(inputorderItemSeqId));
				jQuery(formId).append(jQuery(inputProd));				
				jQuery(formId).append(jQuery(inputBaleQty));
				jQuery(formId).append(jQuery(inputYarnUOM));
				jQuery(formId).append(jQuery(inputBundleWeight));
				jQuery(formId).append(jQuery(inputQty));
				jQuery(formId).append(jQuery(inputUnitPrice));
				jQuery(formId).append(jQuery(inputbundleUnitPrice));			
				
				jQuery(formId).append(jQuery(inputServChgAmt));
				jQuery(formId).append(jQuery(inputServChg));
				
				<#if changeFlag?exists && changeFlag != "AdhocSaleNew">
					var batchNum = jQuery("<input>").attr("type", "hidden").attr("name", "batchNo_o_" + rowCount).val(batchNo);
					jQuery(formId).append(jQuery(batchNum));
					var days = jQuery("<input>").attr("type", "hidden").attr("name", "daysToStore_o_" + rowCount).val(days);
					jQuery(formId).append(jQuery(days));
				</#if>
				var taxList = [];
				taxList = data[rowCount]["taxList"]
				
				var taxListItem = jQuery("<input>").attr("type", "hidden").attr("name", "taxList_o_" + rowCount).val(taxList);
				jQuery(formId).append(jQuery(taxListItem));	
				
   			}
			
   			<#if changeFlag?exists && changeFlag != "EditDepotSales">
   			 }
   			</#if>
		}
		for (var rowCount=0; rowCount < data2.length; ++rowCount)
		{ 
			var ordetAdjTypeId = data2[rowCount]["orderAdjTypeId"];
			var adjAmt = parseFloat(data2[rowCount]["adjAmount"]);
	 		if (!isNaN(adjAmt)) {	 		
				var inputInv = jQuery("<input>").attr("type", "hidden").attr("name", "orderAdjTypeId_o_" + rowCount).val(ordetAdjTypeId);
				var inputAmt = jQuery("<input>").attr("type", "hidden").attr("name", "adjAmt_o_" + rowCount).val(adjAmt);
				jQuery(formId).append(jQuery(inputInv));				
				jQuery(formId).append(jQuery(inputAmt));
			}
		}
		var dataString = $("#indententryinit").serializeArray();
		$.each(dataString , function(i, fd) {
   			if(fd.name === "routeId"){
   				var route = jQuery("<input>").attr("type", "hidden").attr("name", "routeId").val(fd.value);
   				jQuery(formId).append(jQuery(route));
   			 }
		});
		
		//calculateTaxApplicability();
		
		<#if changeFlag?exists && changeFlag != "AdhocSaleNew">
			var partyId = $("#partyId").val();
			var suplierPartyId = $("#suplierPartyId").val();
			var societyPartyId="";
			 	societyPartyId = $("#societyPartyId").val();
			 	
			var partyGeoId = $("#partyGeoId").val(); 	
			var billingType = $("#billingType").val();
			var orderTaxType= $("#orderTaxType").val();
			var poNumber = $("#PONumber").val();
			var acctgFlag = $("#disableAcctgFlag").val();
			var promoAdj = $("#promotionAdj").val();
			var productStoreId = $("#productStoreId").val();
			var cfcId = $("#cfcs").val();
			var schemeCategory = $("#schemeCategory").val();
			
			
			
			var orderMessage = $("#orderMessage").val();
			var party = jQuery("<input>").attr("type", "hidden").attr("name", "partyId").val(partyId);
			var suplierParty = jQuery("<input>").attr("type", "hidden").attr("name", "suplierPartyId").val(suplierPartyId);
			var societyParty = jQuery("<input>").attr("type", "hidden").attr("name", "societyPartyId").val(societyPartyId);
			var POField = jQuery("<input>").attr("type", "hidden").attr("name", "PONumber").val(poNumber);
			var promoField = jQuery("<input>").attr("type", "hidden").attr("name", "promotionAdjAmt").val(promoAdj);
			var productStore = jQuery("<input>").attr("type", "hidden").attr("name", "productStoreId").val(productStoreId);
			var cfc = jQuery("<input>").attr("type", "hidden").attr("name", "cfcId").val(cfcId);
			
			var tax = jQuery("<input>").attr("type", "hidden").attr("name", "orderTaxType").val(orderTaxType);
			var bilngType = jQuery("<input>").attr("type", "hidden").attr("name", "billingType").val(billingType);
			var orderMessageInPut = jQuery("<input>").attr("type", "hidden").attr("name", "orderMessage").val(orderMessage);
			var disableAcctgFlag = jQuery("<input>").attr("type", "hidden").attr("name", "disableAcctgFlag").val(acctgFlag);
			var schemeCategoryObj = jQuery("<input>").attr("type", "hidden").attr("name", "schemeCategory").val(schemeCategory);
			var partyGeo = jQuery("<input>").attr("type", "hidden").attr("name", "partyGeoId").val(partyGeoId);
			<#if orderId?exists>
				var order = '${orderId?if_exists}';
				var extOrder = jQuery("<input>").attr("type", "hidden").attr("name", "orderId").val(order);		
				jQuery(formId).append(jQuery(extOrder));
			</#if>
			
			jQuery(formId).append(jQuery(party));
			jQuery(formId).append(jQuery(suplierParty));
			jQuery(formId).append(jQuery(societyParty));
			jQuery(formId).append(jQuery(bilngType));
			jQuery(formId).append(jQuery(POField));
			jQuery(formId).append(jQuery(promoField));
			jQuery(formId).append(jQuery(tax));
			jQuery(formId).append(jQuery(productStore));
			jQuery(formId).append(jQuery(cfc));
			jQuery(formId).append(jQuery(orderMessageInPut));
			jQuery(formId).append(jQuery(disableAcctgFlag));
			jQuery(formId).append(jQuery(schemeCategoryObj));
			jQuery(formId).append(jQuery(partyGeo));
		</#if>
		
		jQuery(formId).attr("action", action);	
		jQuery(formId).submit();
	}
	var enableSubmit = true;
	<#assign editClickHandlerAction =''>	
	<#--<#if changeFlag?exists && changeFlag=='supplDeliverySchedule'>
		 <#assign editClickHandlerAction='processSupplDeleverySchdule'>
	<#elseif changeFlag?exists && changeFlag=='ByProdGatePass'>
	      <#assign editClickHandlerAction='processSupplDeleverySchdule'>
	<#else>
		 <#assign editClickHandlerAction='processIndentEntryNew'>		 	
	</#if>-->
		function editClickHandlerEvent(row){
			showUDPPriceToolTip(data[row], row, userDefPriceObj);
			
		}
	
	<#--
	function editClickHandler(row) {
		if(enableSubmit){						
			enableSubmit = false;
			processChangeIndentInternal('indententry', '<@ofbizUrl>${editClickHandlerAction}</@ofbizUrl>', row);		
		}
		
	}
	-->
	
	function processIndentEntry(formName, action) {
		jQuery("#changeSave").attr( "disabled", "disabled");
		processIndentEntryInternal(formName, action);
		
	}
	
    function productFormatter(row, cell, value, columnDef, dataContext) {   
        return productIdLabelMap[value];
    }
    function productValidator(value,item) {
      
    	var currProdCnt = 1;
	  	for (var rowCount=0; rowCount < data.length; ++rowCount)
	  	{ 
			if (data[rowCount]['cProductName'] != null && data[rowCount]['cProductName'] != undefined && value == data[rowCount]['cProductName']) {
				++currProdCnt;
			}
	  	}
	  
	  	var invalidProdCheck = 0;
	  	for (var rowCount=0; rowCount < availableTags.length; ++rowCount)
	  	{  
			if (value == availableTags[rowCount]["label"]) {
				invalidProdCheck = 1;
			}
	  	}
      	//if (currProdCnt > 1) {
        	//return {valid: false, msg: "Duplicate Product " + value};      				
      	//}
      	if(invalidProdCheck == 0){
      		return {valid: false, msg: "Invalid Product " + value};
      	}
      
      	if (item != null && item != undefined ) {
      		item['cProductId'] = productLabelIdMap[value];
	  	}      
      	return {valid: true, msg: null};
    }
    
    //quantity validator
	function quantityFormatter(row, cell, value, columnDef, dataContext) { 
		if(value == null){
			return "";
		}
        return  value;
    }
	
	function rateFormatter(row, cell, value, columnDef, dataContext) { 
		var formatValue = parseFloat(value).toFixed(2);
        return formatValue;
    }
	
	 function adustmentFormatter(row, cell, value, columnDef, dataContext) {
        return orderAdjLabelJSON[value];
    }
	function orderAdjFormatter(value,item) {
      
      	var valueId = orderAdjLabelIdMap[value];
    	var currItemCnt = 1;
	  	for (var rowCount=0; rowCount < data2.length; ++rowCount)
	  	{ 
			if (data2[rowCount]['orderAdjTypeId'] != null && data2[rowCount]['orderAdjTypeId'] != undefined && valueId == data2[rowCount]['orderAdjTypeId']) {
				++currItemCnt;
			}
	  	}
	  	
	  	var invalidItemCheck = 0;
	  	for (var rowCount=0; rowCount < availableAdjTags.length; ++rowCount)
	  	{  
			if (valueId == availableAdjTags[rowCount]["value"]) {
				invalidItemCheck = 1;
			}
	  	}
      	if (currItemCnt > 1) {
        	return {valid: false, msg: "Duplicate Item " + value};      				
      	}
      	if(invalidItemCheck == 0){
      		return {valid: false, msg: "Invalid Item " + value};
      	}
      
      	if (item != null && item != undefined ) {
      		item['invoiceItemTypeId'] = invoiceAdjLabelIdMap[value];
	  	}      
      	return {valid: true, msg: null};
    }
	
	
	function quantityValidator(value ,item) {
		var quarterVal = value*4;
		var floorValue = Math.floor(quarterVal);
		var remainder = quarterVal - floorValue;
		var remainderVal =  Math.floor(value) - value;
	     if(remainder !=0 ){
			return {valid: false, msg: "packets should not be in decimals " + value};
		}
      return {valid: true, msg: null};
    }
	var mainGrid;		
	function setupGrid1() {
		var columns = [
			{id:"cProductName", name:"${uiLabelMap.Product}", field:"cProductName", width:300, minWidth:300, cssClass:"cell-title", availableTags: availableTags, regexMatcher:"contains" ,editor: AutoCompleteEditor, validator: productValidator, sortable:false ,toolTip:""},
			{id:"remarks", name:"Specifications", field:"remarks", width:150, minWidth:150, sortable:false, cssClass:"cell-title", focusable :true,editor:TextCellEditor},
			{id:"baleQuantity", name:"Qty(Nos)", field:"baleQuantity", width:50, minWidth:50, sortable:false, editor:FloatCellEditor},
			{id:"cottonUom", name:"${uiLabelMap.cottonUom}", field:"cottonUom", width:50, minWidth:50, cssClass:"cell-title",editor: SelectCellEditor, sortable:false, options: "KGs,Bale,Half-Bale,Bundle"},
			{id:"bundleWeight", name:"${uiLabelMap.BundleWtKgs}", field:"bundleWeight", width:110, minWidth:110, sortable:false, editor:FloatCellEditor},
			{id:"quantity", name:"Qty(Kgs)", field:"quantity", width:50, minWidth:50, sortable:false, editor:FloatCellEditor},
			{id:"unitPrice", name:"${uiLabelMap.UnitPrice}", field:"unitPrice", width:60, minWidth:60, sortable:false, formatter: rateFormatter, align:"right", editor:FloatCellEditor},
			{id:"KgunitPrice", name:"${uiLabelMap.UnitPrice}", field:"KgunitPrice", width:60, minWidth:60, sortable:false, formatter: rateFormatter, align:"right", editor:FloatCellEditor},
			<#--{id:"schemeApplicability", name:"10% Scheme", field:"schemeApplicability", width:150, minWidth:150, cssClass:"cell-title",editor: SelectCellEditor, sortable:false, options: "Applicable,Not-Applicable"},-->
			{id:"amount", name:"Amt(Rs)", field:"amount", width:75, minWidth:75, sortable:false, formatter: rateFormatter,editor:FloatCellEditor},	
			<#--{id:"warning", name:"Warning", field:"warning", width:230, minWidth:230, sortable:false, cssClass:"readOnlyColumnAndWarningClass", focusable :false},-->
			{id:"taxAmt", name:"VAT/CST", field:"taxAmt", width:65, minWidth:65, sortable:false, formatter: rateFormatter, align:"right", cssClass:"readOnlyColumnClass" , focusable :false},
			{id:"SERVICE_CHARGE_AMT", name:"Serv Chgs", field:"SERVICE_CHARGE_AMT", width:65, minWidth:65, sortable:false, formatter: rateFormatter, align:"right", cssClass:"readOnlyColumnClass" , focusable :false},
			{id:"totPayable", name:"Total Payable", field:"totPayable", width:75, minWidth:75, sortable:false, formatter: rateFormatter, align:"right", cssClass:"readOnlyColumnClass" , focusable :false},
			{id:"button", name:"Edit Tax", field:"button", width:60, minWidth:60, cssClass:"cell-title", focusable :false,
 				formatter: function (row, cell, id, def, datactx) { 
					return '<a href="#" class="button" onclick="editClickHandlerEvent('+row+')" value="Edit">Edit</a>'; 
 				}
 			},
 			{id:"orderItemSeqId", name:"orderItemSeqId", field:"orderItemSeqId", width:150, minWidth:150, sortable:false, cssClass:"cell-title", focusable :true,editor:FloatCellEditor},
 			{id:"quotaAvbl", name:"${uiLabelMap.QuotaAvailable}", field:"quota", width:110, minWidth:110, sortable:false, cssClass:"readOnlyColumnClass", focusable :false},
			{id:"warning", name:"Warning", field:"warning", width:130, minWidth:130, sortable:false, cssClass:"readOnlyColumnAndWarningClass", focusable :false}
		];
		hiddencolumns = [
			{id:"cProductName", name:"${uiLabelMap.Product}", field:"cProductName", width:300, minWidth:300, cssClass:"readOnlyColumnClass", sortable:false ,toolTip:""},
			{id:"remarks", name:"Specifications", field:"remarks", width:150, minWidth:150, sortable:false, cssClass:"cell-title", focusable :true,editor:TextCellEditor},
			{id:"cottonUom", name:"${uiLabelMap.cottonUom}", field:"cottonUom", width:50, minWidth:50,  cssClass:"readOnlyColumnClass"},
			{id:"bundleWeight", name:"${uiLabelMap.BundleWtKgs}", field:"bundleWeight", width:110, minWidth:110,  cssClass:"readOnlyColumnClass"},
			{id:"quantity", name:"Qty(Kgs)", field:"quantity", width:50, minWidth:50, sortable:false, editor:FloatCellEditor},
			{id:"unitPrice", name:"${uiLabelMap.UnitPrice}", field:"unitPrice", width:60, minWidth:60, sortable:false, formatter: rateFormatter, align:"right", editor:FloatCellEditor},
			<#--{id:"schemeApplicability", name:"10% Scheme", field:"schemeApplicability", width:150, minWidth:150, cssClass:"cell-title",editor: SelectCellEditor, sortable:false, options: "Applicable,Not-Applicable"},-->
			{id:"amount", name:"Amt(Rs)", field:"amount", width:75, minWidth:75, sortable:false, formatter: rateFormatter,editor:FloatCellEditor},	
			<#--{id:"warning", name:"Warning", field:"warning", width:230, minWidth:230, sortable:false, cssClass:"readOnlyColumnAndWarningClass", focusable :false},-->
		];


		var data_view = new Slick.Data.DataView();
		grid = new Slick.Grid("#myGrid1", data, hiddencolumns,options);
		var columnPicker= new Slick.Controls.ColumnPicker(columns, grid,options);
		
		
		
		var options = {
			editable: true,		
			forceFitColumns: false,			
			enableCellNavigation: true,
			enableAddRow: true,
			asyncEditorLoading: false,			
			autoEdit: true,
            secondaryHeaderRowHeight: 25
		};
		

		grid = new Slick.Grid("#myGrid1", data,hiddencolumns, options);
        grid.setSelectionModel(new Slick.CellSelectionModel());        
		var columnpicker = new Slick.Controls.ColumnPicker(columns, grid, options);

		
		// wire up model events to drive the grid
        if (data.length > 0) {			
			$(grid.getCellNode(0, 1)).click();
		}else{
			$(grid.getCellNode(0,0)).click();
		}
         grid.onKeyDown.subscribe(function(e) {
			var cellNav = 0;
			<#if changeFlag?exists && changeFlag != "AdhocSaleNew">
				cellNav = 3;
			<#else>
				cellNav = 2;
			</#if>
			var cell = grid.getCellFromEvent(e);		
			if(e.which == $.ui.keyCode.UP && cell.row == 0){
				grid.getEditController().commitCurrentEdit();	
				$(grid.getCellNode(cell.row+1, 0)).click();
				e.stopPropagation();
			}
			else if((e.which == $.ui.keyCode.DOWN || e.which == $.ui.keyCode.ENTER) && cell.row == data.length && cell.cell == cellNav){
				grid.getEditController().commitCurrentEdit();	
				$(grid.getCellNode(0, 2)).click();
				e.stopPropagation();
			}else if((e.which == $.ui.keyCode.DOWN || e.which == $.ui.keyCode.ENTER) && cell.row == (data.length-1) && cell.cell == cellNav){
				grid.getEditController().commitCurrentEdit();
				grid.gotoCell(data.length, 0, true);
				$(grid.getCellNode(data.length, 0)).edit();
				
				e.stopPropagation();
			}
			
			else if((e.which == $.ui.keyCode.DOWN || e.which == $.ui.keyCode.RIGHT) && cell 
				&& cell.row == data.length && cell.cell == cellNav){
  				grid.getEditController().commitCurrentEdit();	
				$(grid.getCellNode(cell.row, 0)).click();
				e.stopPropagation();
			
			}else if (e.which == $.ui.keyCode.RIGHT &&
				cell && (cell.cell == cellNav) && 
				cell.row != data.length) {
				grid.getEditController().commitCurrentEdit();	
				$(grid.getCellNode(cell.row+1, 0)).click();
				e.stopPropagation();	
			}
			else if (e.which == $.ui.keyCode.LEFT &&
				cell && (cell.cell == 0) && 
				cell.row != data.length) {
				grid.getEditController().commitCurrentEdit();	
				$(grid.getCellNode(cell.row, cellNav)).click();
				e.stopPropagation();	
			}else if (e.which == $.ui.keyCode.ENTER) {
        		grid.getEditController().commitCurrentEdit();
				if(cell.cell == 1 || cell.cell == 2){
					jQuery("#changeSave").click();
				}
            	e.stopPropagation();
            	e.preventDefault();        	
            }else if (e.keyCode == 27) {
            //here ESC to Save grid
        		if (cell && cell.cell == 0) {
        			$(grid.getCellNode(cell.row - 1, cellNav)).click();
        			return false;
        		}  
        		grid.getEditController().commitCurrentEdit();
				   
            	e.stopPropagation();
            	e.preventDefault();        	
            }
            
            else {
            	return false;
            }
        });
        
                
    	grid.onAddNewRow.subscribe(function (e, args) {
      		var item = args.item;   
      		var productLabel = item['cProductName']; 
      		item['productNameStr'] = productLabel;
      		item['cProductId'] = productLabelIdMap[productLabel];  
      		grid.invalidateRow(data.length);
      		data.push(item);
      		grid.updateRowCount();
      		grid.render();
    	});
    	grid.onBeforeEditCell.subscribe(function(e,args) {
	      	
	      	if (args.cell == 1) {
				var row = args.row;
				updateCurrentQuota(row);
			}
			
			if ( (args.cell == 2) || (args.cell == 3) || ((args.cell == 4)) ) {
	      		var productName = data[args.row]["productNameStr"];
      			<#if changeFlag?exists && changeFlag != "EditDepotSales">
      				if (productName.toLowerCase().indexOf("cotton") <= 0){
      					grid.updateRow(args.row);
      					return false;
      				}
      			</#if>
      		}
	      	
	      	
	    });
       	  grid.onCellChange.subscribe(function(e,args) {
		  
	     	setUndefinedValues(data[args.row]);
		 	grid.updateRow(args.row);
			var prod = data[args.row]["cProductId"];
			var quantity = parseFloat(data[args.row]["quantity"]);
			var bundleunitPrice = data[args.row]['bundleunitPrice'];
			var uom = data[args.row]["cottonUom"];
			var balQuty = parseFloat(data[args.row]["baleQuantity"]);
			var bundleWeight = parseFloat(data[args.row]["bundleWeight"]);	
			var price = parseFloat(data[args.row]["unitPrice"]);
			var amt = parseFloat(data[args.row]["amount"]);
			if( uom == "KGs" ){
			 	roundedAmount =calculateBundlePrice(balQuty,uom,bundleunitPrice);
			}else{
			 	roundedAmount =calculateBundlePrice(balQuty,uom,price);
			}
			if (args.cell == 3) {
		  		if(isNaN(roundedAmount)){
					roundedAmount = 0;
		  		}
		  		if(isNaN(bundleunitPrice)){
					bundleunitPrice = 0;
		  		}	
		  		quantity=calculateBundleWeight(balQuty,uom,bundleWeight);
		  		data[args.row]["quantity"]=quantity;
		  		data[args.row]["bundleunitPrice"] = bundleunitPrice;
		  		data[args.row]["amount"] = roundedAmount;
		  		data[args.row]["totPayable"] = roundedAmount;
			}
		else if(args.cell == 4 || args.cell == 5){
		
		       var minqty = parseFloat(data[args.row]["minQuantity"]);
		       
				
				if(quantity<minqty){
				alert("Sorry,Please give the quantity greater than the (Shipped Quantity) : "+minqty);
				data[args.row]["quantity"] = data[args.row]["actualQuantity"];
				quantity = data[args.row]["quantity"];
				}
				
				
				if(minqty>0){
				  alert("Sorry, already shipment done for this order");
				  data[args.row]['unitPrice'] = data[args.row]['actualUnitPrice'];
				  
				  price = data[args.row]['unitPrice'];
				  
				}
		
		
			data[args.row]["amount"] = Math.round(quantity*price);
		}
		else if(args.cell == 5){
		  data[args.row]["amount"] = Math.round(quantity*price);
		}
		else if(args.cell == 6){
		  data[args.row]["unitPrice"] = Math.round(amt/quantity);
		}
		else if(args.cell == 7){
		  baleQty=calculateKgs(quantity,uom,bundleWeight);
		  data[args.row]["baleQuantity"]=baleQty;
		   if( uom != "KGs" ){
		  data[args.row]["amount"] = Math.round(quantity*bundleunitPrice);
		  upb=reverseCalculationKGToBundle(bundleWeight,bundleunitPrice);
		  }else{
		   data[args.row]["amount"] = Math.round(quantity*price);
		   upb=reverseCalculationKGToBundle(bundleWeight,price);
		  }
		  data[args.row]["bundleunitPrice"] = upb;
		  data[args.row]["totPayable"] = Math.round(quantity*bundleunitPrice);
		}
		
		else if(args.cell == 8){
		    var upb=0;
			var bundle=0;
				if(uom == "Bale"){
				bundle=balQuty*40;
				}
				if(uom == "Half-Bale"){
				bundle=balQuty*20;
				}
		if(uom == "Bundle" || uom == "KGs" ){
				bundle=balQuty;
				}
				upb=amt/bundle;
				kgprice=amt/quantity;
			data[args.row]["bundleunitPrice"] =upb;
			data[args.row]["unitPrice"] = kgprice;
		}
	 grid.updateRow(args.row);
	 });
		
		
		grid.onValidationError.subscribe(function(e, args) {
        var validationResult = args.validationResults;
        var activeCellNode = args.cellNode;
        var editor = args.editor;
        var errorMessage = validationResult.msg;
        var valid_result = validationResult.valid;
        
        if (!valid_result) {
           $(activeCellNode).attr("tittle", errorMessage);
            }else {
           $(activeCellNode).attr("tittle", "");
        }

    });
    	//updateInlineTotalAmount();
		//updateProductTotalAmount();
		
		mainGrid = grid;
	}
		
	jQuery(function(){
	     // only setupGrid when BoothId exists
	     var boothId=$('[name=boothId]').val();
	     var partyId=$('[name=partyId]').val();
		 
		 	setupGrid1();
		 	//setupGrid2();
	    
	    
			//  alert("=After==Setup==partyId==="+partyId+"==boothId=="+boothId);	
        jQuery(".grid-header .ui-icon")
            .addClass("ui-state-default ui-corner-all")
            .mouseover(function(e) {
                jQuery(e.target).addClass("ui-state-hover")
            })
            .mouseout(function(e) {
                jQuery(e.target).removeClass("ui-state-hover")
            });		
		jQuery("#gridContainer").resizable();	   			
    	var tabindex = 1;
    	jQuery('input,select').each(function() {
        	if (this.type != "hidden") {
            	var $input = $(this);
            	$input.attr("tabindex", tabindex);
            	tabindex++;
        	}
    	});

    	var rowCount = jQuery('#myGrid1 .slick-row').length;
		if (rowCount > 0) {			
			$(mainGrid.getCellNode(rowCount-1, 0)).click();		   
    	}
    	else { 
			$("#boothId").focus();      
		}  		
	});
	
	
// to show special related fields in form			
	
	$(document).ready(function(){
		(function blink() { 
		    $('.readOnlyColumnAndWarningClass').fadeOut(500).fadeIn(500, blink); 
		})();
		$(function() {
			$( "#indententryinit" ).validate();
		});	
		
	});	
	 
	
	
	
	function populateContactDetails(contactInfo){
		jQuery("#name").val(contactInfo["name"]);		
		jQuery("#address1").val(contactInfo["address1"]);		
		jQuery("#address2").val(contactInfo["address2"]);		
		jQuery("#contactNumber").val(contactInfo["contactNumber"]); 
		jQuery("#pinNumber").val(contactInfo["postalCode"]);
	}
	
	function updatePayablePrice(row){
		var amount = data[row]['amount'];
		var taxAmt = data[row]['taxAmt'];
		if(isNaN(amount)){
			amount = 0;
		}
		if(isNaN(taxAmt)){
			taxAmt = 0;
		}
		
        data[row]["totPayable"] = amount + taxAmt; 
        grid.updateRow(row);
        updateTotalIndentAmount();
	}
	
	function updateTotalIndentAmount(){
		var totalAmount = 0;
		for (i = 0; i < data.length; i++) {
			totalAmount += data[i]["totPayable"];
		}
		var amt = parseFloat(Math.round((totalAmount) * 100) / 100);
		var dispText = "";
		if(amt > 0 ){
			dispText = "<b>  [Indent Amt: Rs " +  amt + "]</b>";
		}
		else{
			dispText = "<b>  [Indent Amt: Rs 0 ]</b>";
		}
		
		jQuery("#totalAmount").html(dispText);
		
	}
	
	 function calculateBundlePrice(balQuty,uom,org2){
	  var result=0;
	    if(uom == "Bale"){
			result = Math.round(balQuty*org2*40);
		}
		else if(uom == "Half-Bale"){
		    result = Math.round(balQuty*org2*20);
		}
		else if(uom == "Bundle"||uom == "KGs"){				
		    result = Math.round(balQuty*org2);
		}
		
		return result;
	 }
	 function reverseCalculationBundleToKG(bundleWeight,bundlePrice){
	  var result=0;
		result=bundlePrice/bundleWeight;
		return result;
	 }
	 function reverseCalculationKGToBundle(bundleWeight,kgPrice){
	  var result=0;
		result=kgPrice*bundleWeight;
		return result;
	 }
	 
	function calculateBundleWeight(balQuty,uom,org2){
	  var result=0;
	    if(uom == "Bale"){
			result = balQuty*org2*40;
		}
		else if(uom == "Half-Bale"){
		    result = balQuty*org2*20;
		}
		else if(uom == "Bundle"){				
		    result = balQuty*org2;
		}
		else if(uom == "KGs"){				
		    result = balQuty;
		}
		return result;
	 }
	function calculateKgs(quantity,uom,org2){
	  var result=0;
	    if(uom == "Bale"){
			 result=quantity/(org2*40);
			}
		else if(uom == "Half-Bale"){
				result=quantity/(org2*20);
			}
		else if(uom == "Bundle"){
				result = quantity/org2;
			}
		else if(uom == "KGs"){				
			result = quantity;
		    }
		return result;
	 }
	 function setUndefinedValues(row){
	 if ((row["bundleWeight"]) == undefined   ) {
			row["bundleWeight"] = 4.54;
		}
        if ((row["cottonUom"]) == undefined   ) {
			row["cottonUom"] = "KGs";
		}
		if(isNaN(row['unitPrice'])){
			row['unitPrice'] = 0;
		 }
		if(isNaN(row['bundleunitPrice'])){
			row['bundleunitPrice'] = 0;
		 }
		if(isNaN(row["quantity"])){
			row["quantity"] = 0;
		 }
		 if(isNaN(row["amount"])){
			row["amount"] = 0;
		 }
		  if(isNaN(row["totPayable"])){
			row["totPayable"] = 0;
		 }
		 if(isNaN(row["taxAmt"])){
			row["taxAmt"] = 0;
		 }
		 if(isNaN(row["SERVICE_CHARGE_AMT"])){
			row["SERVICE_CHARGE_AMT"] = 0;
		 }
	 }
	function setCustomerId(item){
      	var custId= item['customerName'];
      	var splited = (((custId.split("["))[1]).split("]"))[0];
      	var productLabel = item['cProductName']; 
      	item['productNameStr'] = productLabel;
      	var custmerID=indcustomerLabelPsbNumMap[custId];
      	item['customerId'] = splited;
      	item['psbNumber'] = partyPsbNumber[custmerID];
      	item['cProductId'] = productLabelIdMap[productLabel];  
      	grid.invalidateRow(data.length);
      	data.push(item);
      	grid.updateRowCount();
      	grid.render();
	}

 	function setCustomerIdOnCustomerChange(item,orgs){
		if(orgs.cell==1 && item!= undefined){
 			var custId= item['customerName'];
      		var splited = (((custId.split("["))[1]).split("]"))[0];
      		data[orgs.row]['customerId'] = splited;
 		}
    }
	
</script>		