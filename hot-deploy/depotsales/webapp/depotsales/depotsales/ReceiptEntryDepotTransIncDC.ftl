
<link rel="stylesheet" href="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/slick.grid.css</@ofbizContentUrl>" type="text/css" media="screen" charset="utf-8" />
<link rel="stylesheet" href="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/controls/slick.pager.css</@ofbizContentUrl>" type="text/css" media="screen" charset="utf-8" />
<link rel="stylesheet" href="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/css/smoothness/jquery-ui-1.8.5.custom.css</@ofbizContentUrl>" type="text/css" media="screen" charset="utf-8" />
<link rel="stylesheet" href="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/examples/examples.css</@ofbizContentUrl>" type="text/css" media="screen" charset="utf-8" />
<link rel="stylesheet" href="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/controls/slick.columnpicker.css</@ofbizContentUrl>" type="text/css" media="screen" charset="utf-8" />
<style type="text/css">
	.cell-title {
		font-weight: normal;
	}
	.cell-effort-driven {
		text-align: center;
	}
	.readOnlyColumnClass {
		font-weight: normal;
		background: mistyrose;
	}
	
	.btn {
	    color:#08233e;
	    font:8em Futura, ‘Century Gothic’, AppleGothic, sans-serif;
	    font-size:100%;
	    font-weight: bold;
	    padding:14px;
	    background:url(overlay.png) repeat-x center #ffcc00;
	    background-color:rgba(255,204,0,1);
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
    	background-color:rgba(255,204,0,0.8);
	}
	.btn {
	    background-color:orange;
	    cursor:pointer;
	}
	
</style>			
			
<script language="javascript" type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/lib/firebugx.js</@ofbizContentUrl>"></script>
<#--
<script language="javascript" type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/lib/jquery-1.4.3.min.js</@ofbizContentUrl>"></script>
-->
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
<script type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/datetimepicker/jquery-ui-timepicker-addon-0.9.3.min.js</@ofbizContentUrl>"></script>
<script type="application/javascript">
    
    	
	var dataView;
	var dataView2;
	var grid;
	var productLabelIdMap = ${StringUtil.wrapString(productLabelIdJSON)!'{}'};
	var productIdLabelMap = ${StringUtil.wrapString(productIdLabelJSON)!'{}'};
	var availableTags = ${StringUtil.wrapString(productItemsJSON)!'[]'};
	var priceTags = ${StringUtil.wrapString(productCostJSON)!'[]'};
	var conversionData = ${StringUtil.wrapString(conversionJSON)!'{}'};
	var productUOMMap = ${StringUtil.wrapString(productUOMJSON)!'{}'};
	var uomLabelMap = ${StringUtil.wrapString(uomLabelJSON)!'{}'};
	
	var data = ${StringUtil.wrapString(dataJSON)!'[]'};
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
		
		var rowCountIndex=0;
		var grandQty = 0; 
		for (var rowCount=0; rowCount < data.length; ++rowCount)
		{ 
			var productId = data[rowCount]["cProductId"];
			var orderItemSeqId  = data[rowCount]["orderItemSeqId"];		
			var remarks = data[rowCount]["remarks"];			
			var prodId="";
			if(typeof(productId)!= "undefined"){ 	  
			var prodId = productId.toUpperCase();
			}
			var qty = parseFloat(data[rowCount]["quantity"]);
			grandQty = grandQty+qty;
			 
	 		if (!isNaN(qty) && qty>0 ) {	 		
				var inputProd = jQuery("<input>").attr("type", "hidden").attr("name", "productId_o_" + rowCountIndex).val(prodId);
				var inputQty = jQuery("<input>").attr("type", "hidden").attr("name", "quantity_o_" + rowCountIndex).val(qty);
				var orderItemSeqId = jQuery("<input>").attr("type", "hidden").attr("name", "orderItemSeqId_o_" + rowCountIndex).val(orderItemSeqId);
				jQuery(formId).append(jQuery(inputProd));				
				jQuery(formId).append(jQuery(inputQty));
				jQuery(formId).append(jQuery(orderItemSeqId));
				
				var dcQty = parseFloat(data[rowCount]["deliveryChallanQty"]);
		 		if (!isNaN(dcQty) && dcQty>0 ) {	 	
		 			if( ((!isNaN(qty)) && (qty>dcQty) )){
			 			alert("Received Quantity should be less than Delivery Challan Quantity");
			 			return false;
			 		}	
					var inputDCQty = jQuery("<input>").attr("type", "hidden").attr("name", "deliveryChallanQty_o_" + rowCountIndex).val(dcQty);
					jQuery(formId).append(jQuery(inputDCQty));
	   			}
	   			
	   			var oldRecvdQty = parseFloat(data[rowCount]["oldRecvdQty"]);
		 		if (!isNaN(oldRecvdQty) && oldRecvdQty>0 ) {	 		
					var inputOldQty = jQuery("<input>").attr("type", "hidden").attr("name", "oldRecvdQty_o_"+ rowCountIndex).val(oldRecvdQty);
					jQuery(formId).append(jQuery(inputOldQty));
	   			}
				rowCountIndex++;
   			}
   			
   			  
		}
		if( (grandQty==0) || (isNaN(grandQty))) {
			alert("please give atleast one product Qty greater than Zero..");
			return false;
		}
		var dataString = $("#indententryinit").serializeArray();
		var orderId = $("#orderId").val();
		var vehicleId = $("#vehicleId").val();
		var order = jQuery("<input>").attr("type", "hidden").attr("name", "orderId").val(orderId);
		var vehicle = jQuery("<input>").attr("type", "hidden").attr("name", "vehicleId").val(vehicleId);
		
		//if(vehicleId == '')
		//{
		//alert("Vehilce Id Missing..!");
		//window.location.reload(true);
   		//return false;	 
   		//}
		var supplierId = $("#supplierId").val();
		var supplier = jQuery("<input>").attr("type", "hidden").attr("name", "supplierId").val(supplierId);
		var suppInvoiceId = $("#suppInvoiceId").val();
		
		if(suppInvoiceId == '')
		{
		alert("Supplier Invoice Id missing..!");
		window.location.reload(true);
   		return false;	 
   		}
		var suppInvoice = jQuery("<input>").attr("type", "hidden").attr("name", "suppInvoiceId").val(suppInvoiceId);
		var suppInvoiceDate = $("#suppInvoiceDate").val();
		if(suppInvoiceDate == '')
		{
		alert("Supplier Invoice Date is missing..!");
		window.location.reload(true);
   		return false;	 
   		}
		var suppInvDate = jQuery("<input>").attr("type", "hidden").attr("name", "suppInvoiceDate").val(suppInvoiceDate);
		var withoutPO =  jQuery("<input>").attr("type", "hidden").attr("name", "withoutPO").val($("#withoutPO").val());
	
		var deliveryChallanNo = $("#deliveryChallanNo").val();
		var dcNo = jQuery("<input>").attr("type", "hidden").attr("name", "deliveryChallanNo").val(deliveryChallanNo);
		var deliveryChallanDate = $("#deliveryChallanDate").val();
		var dcDate = jQuery("<input>").attr("type", "hidden").attr("name", "deliveryChallanDate").val(deliveryChallanDate);
		var effectiveDate = $("#effectiveDate").val();
		var remarks = $("#remarks").val();
		var remarksOption = jQuery("<input>").attr("type", "hidden").attr("name", "remarks").val(remarks);
		
		var effDate = jQuery("<input>").attr("type", "hidden").attr("name", "receiptDate").val(effectiveDate);
		$("#receiptDate").val(effectiveDate);
		
		var hideQCflow = $("#hideQCflow").val();
		var hideQCflag = jQuery("<input>").attr("type", "hidden").attr("name", "hideQCflow").val(hideQCflow);
		var allowedGraterthanTheOrdered = $("#allowedGraterthanTheOrdered").val();
		var flag = jQuery("<input>").attr("type", "hidden").attr("name", "allowedGraterthanTheOrdered").val(allowedGraterthanTheOrdered);
		
        var lrNUm = $("#lrNumber").val();
        var lrNumber = jQuery("<input>").attr("type", "hidden").attr("name", "lrNumber").val(lrNUm);
        var lrDate = $("#lrDate").val();
        var lrDate = jQuery("<input>").attr("type", "hidden").attr("name", "lrDate").val(lrDate);
        var freightCharges = $("#freightCharges").val();
        var freightChar = jQuery("<input>").attr("type", "hidden").attr("name", "freightCharges").val(freightCharges);
        var carrier = $("#carrierName").val();
        var carrierName = jQuery("<input>").attr("type", "hidden").attr("name", "carrierName").val(carrier);
        
        var destina = $("#destination").val();
        var destination = jQuery("<input>").attr("type", "hidden").attr("name", "destination").val(destina);
			
		jQuery(formId).append(jQuery(effDate));
		jQuery(formId).append(jQuery(suppInvoice));
		jQuery(formId).append(jQuery(suppInvDate));
		jQuery(formId).append(jQuery(order));
		jQuery(formId).append(jQuery(vehicle));
		jQuery(formId).append(jQuery(withoutPO));
		jQuery(formId).append(jQuery(supplier));
		jQuery(formId).append(jQuery(dcNo));
		jQuery(formId).append(jQuery(dcDate));
		jQuery(formId).append(jQuery(remarksOption));
		jQuery(formId).append(jQuery(hideQCflag));
		jQuery(formId).append(jQuery(flag));
		jQuery(formId).append(jQuery(lrNumber));
		jQuery(formId).append(jQuery(lrDate));
		jQuery(formId).append(jQuery(freightChar));
		jQuery(formId).append(jQuery(carrierName));
		jQuery(formId).append(jQuery(destination));
		
		
		jQuery(formId).attr("action", action);
		
		jQuery(formId).submit();
	}
	var enableSubmit = true;
	
	
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
      	if (currProdCnt > 1) {
        	return {valid: false, msg: "Duplicate Product " + value};      				
      	}
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
     //deliveryChallanQty validator
	function deliveryChallanQtyFormatter(row, cell, value, columnDef, dataContext) { 
		if(value == null){
			return "";
		}
        return  value;
    }
	
	function rateFormatter(row, cell, value, columnDef, dataContext) { 
		var formatValue = parseFloat(value).toFixed(2);
        return formatValue;
    }
	
	
	
	function quantityValidator(value ,item) {
		var quarterVal = value*4;
		var qty = item['orderedQty'];
		var balQty = item['balanceQty'];  
		if(value>balQty){
			alert("Required quantity Should not be greater than indent Qty")
			return {valid: false, msg: null};
		}
		var floorValue = Math.floor(quarterVal);
		var remainder = quarterVal - floorValue;
		var remainderVal =  Math.floor(value) - value;
		 if(parseInt(value) <0 ){
			return {valid: false, msg: "required quantity Should not be less than or equals to zero" + value};
		 }
	     
      return {valid: true, msg: null};
    }
    function deliveryChallanQtyValidator(value ,item) {
		var quarterVal = value*4;
		var floorValue = Math.floor(quarterVal);
		var remainder = quarterVal - floorValue;
		var remainderVal =  Math.floor(value) - value;
	     
      return {valid: true, msg: null};
    }
    
	var mainGrid;		
	function setupGrid1() {
		<#if (withoutPO?exists && withoutPO?has_content)> 
		    var columns = [
					{id:"cProductName", name:"Item", field:"cProductName", width:220, minWidth:220, cssClass:"cell-title", editor: AutoCompleteEditor,availableTags:availableTags,regexMatcher:"contains", validator: productValidator, sortable:false, toolTip:""},
					{id:"ordQuantity", name:"Order Qty", field:"orderedQty", width:60, minWidth:60, cssClass:"readOnlyColumnClass", sortable:false, focusable :false,},
				<#--	{id:"deliveryChallanQty", name:"DC Qty", field:"deliveryChallanQty", width:80, minWidth:80, cssClass:"cell-title",editor:FloatCellEditor, sortable:false , formatter: deliveryChallanQtyFormatter,  validator: deliveryChallanQtyValidator},-->
					{id:"quantity", name:"Dispatch Qty", field:"quantity", width:80, minWidth:80, cssClass:"cell-title",editor:FloatCellEditor, sortable:false , formatter: quantityFormatter,  validator: quantityValidator},
					{id:"UOM", name:"BalanceQty(Kgs)", field:"uomDescription", width:80, minWidth:80, cssClass:"readOnlyColumnClass", focusable :false,editor:FloatCellEditor, sortable:false}
			];
			<#else>
				var columns = [
					{id:"cProductName", name:"Item", field:"cProductName", width:220, minWidth:220, cssClass:"readOnlyColumnClass", sortable:false, focusable :false, validator: productValidator, toolTip:""},
					{id:"remarks", name:"Specification", field:"remarks", width:120, minWidth:220, cssClass:"readOnlyColumnClass", sortable:false, focusable :false},
					{id:"ordQuantity", name:"Indent Qty", field:"orderedQty", width:120, minWidth:120, cssClass:"readOnlyColumnClass", sortable:false, focusable :false,},
					<#--{id:"deliveryChallanQty", name:"DC Qty", field:"deliveryChallanQty", width:80, minWidth:80, cssClass:"cell-title",editor:FloatCellEditor, sortable:false , formatter: deliveryChallanQtyFormatter,  validator: deliveryChallanQtyValidator}, -->
					{id:"quantity", name:"Dispatch Qty", field:"quantity", width:120, minWidth:120, editor:FloatCellEditor, cssClass:"cell-title",sortable:false , formatter: quantityFormatter,  validator: quantityValidator},
					{id:"oldRecvdQty", name:"Dispatched Qty", field:"oldRecvdQty", width:120, minWidth:120, cssClass:"readOnlyColumnClass", sortable:false, focusable :false,},
					{id:"balanceQty", name:"Balance Qty(Kgs)", field:"balanceQty", width:120, minWidth:120, cssClass:"readOnlyColumnClass", focusable :false,editor:FloatCellEditor, sortable:false},
					{id:"unitPrice", name:"unitPrice(Kgs)", field:"unitPrice", width:90, minWidth:90, cssClass:"readOnlyColumnClass", focusable :false,editor:FloatCellEditor,formatter: rateFormatter, sortable:false,},
					{id:"ItemValue", name:"Item Amount(Rs)", field:"ItemValue", width:100, minWidth:100, cssClass:"readOnlyColumnClass", focusable :false,editor:FloatCellEditor,formatter: rateFormatter, sortable:false}
				];
		</#if>
		
			var options = {
			editable: true,		
			forceFitColumns: false,			
			enableCellNavigation: true,
			enableColumnReorder: false,
			//enableAddRow: true,
			asyncEditorLoading: false,			
			autoEdit: true,
            secondaryHeaderRowHeight: 25
            
		};
		

		grid = new Slick.Grid("#myGrid1", data, columns, options);
        grid.setSelectionModel(new Slick.CellSelectionModel());        
		var columnpicker = new Slick.Controls.ColumnPicker(columns, grid, options);
		
		// wire up model events to drive the grid
        if (data.length > 0) {			
			$(grid.getCellNode(0, 1)).click();
		}else{
			$(grid.getCellNode(0,0)).click();
		}
         grid.onKeyDown.subscribe(function(e) {
			var cellNav = 2;
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
				$(grid.getCellNode(cell.row+2, 0)).click();
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
      		item['cProductId'] = productLabelIdMap[productLabel];     		 		
      		grid.invalidateRow(data.length);
      		data.push(item);
      		grid.updateRowCount();
      		grid.render();
    	});
    	    	
    	var grandTOT = 0;
    	
    	 for (var rowCount=0; rowCount < data.length; ++rowCount)
		{
				var balanceQty = data[rowCount]["balanceQty"];
				if(isNaN(balanceQty)){
					balanceQty = 0;
				}
				if(balanceQty <= 0){
				var obj=[];
		    	obj[rowCount]={quantity:"readOnlyColumnClass"};		    	
				grid.setCellCssStyles("readOnlyColumnClass",obj);
				grid.updateRow(rowCount);				
				}
 		}
    	
        grid.onCellChange.subscribe(function(e,args) {
        
        for (var rowCount=0; rowCount < data.length; ++rowCount)
		{ 
			var quantity = data[rowCount]["quantity"];
			var unitPrice = data[rowCount]["unitPrice"];
			grandTOT = grandTOT+quantity*unitPrice;
			
		}
        
        $("#grandTot").html("<font color=green>Dispatch Items Entry   &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160; Order Value:</font> &#160;&#160;<font color=blue>"+grandTOT+"</font>");
        
        grandTOT = 0;
        
           
			if (args.cell == 1 || args.cell == 2) {
				var prod = data[args.row]["cProductId"];
				var uomId = productUOMMap[prod];
				if(uomId != 'undefined' && uomId != null ){
				 var uomLabel = uomLabelMap[uomId];
				 data[args.row]['uomDescription'] = uomLabel;
				}
				grid.updateRow(args.row);
			}
			if (args.cell == 3) {
				var ordQuantity = data[args.row]["orderedQty"];
				var oldRecvdQty = data[args.row]["oldRecvdQty"];
				var quantity=data[args.row]["quantity"];
				if(isNaN(oldRecvdQty)){
					oldRecvdQty = 0;
				}
				
				if(isNaN(quantity)){
					quantity = 0;
				}
				var balanceQty=ordQuantity-oldRecvdQty;
				balanceQty=balanceQty-quantity;
				//if(balqty<0){
				//balqty=0;
				//}
					data[args.row]["balanceQty"]=balanceQty;
				//if(balanceQty <= 0){
				//	data[args.row]["balanceQty"]=0;
					//data[args.row]["quantity"]=0;
				//}
				grid.updateRow(args.row);
			}
			
		}); 
		
		grid.onActiveCellChanged.subscribe(function(e,args) {
        	if (args.cell == 1 && data[args.row] != null) {
				var prod = data[args.row]["cProductId"];
			}
			
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
		
		mainGrid = grid;
	}
	
	jQuery(function(){
	     var orderId=$('[name=orderId]').val();
	     var withoutPO = "${withoutPO?if_exists}";
	    
		 if(orderId || withoutPO){
		 	setupGrid1();
	     }
				
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
			$("#orderId").focus();      
		}  		
	});
	
	
// to show special related fields in form			
	
	$(document).ready(function(){
	 /*
		$('#suppInvoiceId').keypress(function (e) {
	  			if (e.which == $.ui.keyCode.ENTER) {
	    			$('#indententryinit').submit();
	    			return false;   
	  			}
		});
		*/
		     $(function() {
				$( "#indententryinit" ).validate();
			});	
			$("#orderId").autocomplete({ disabled: false });
			$("#suppInvoiceId").autocomplete({ disabled: false });	
		
	});	
	 
</script>			