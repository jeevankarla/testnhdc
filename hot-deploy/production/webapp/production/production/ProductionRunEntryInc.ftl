
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
		background: #F3F3F3;
	}
	
	#errorSpan {
		color: red;
		font-size: 1.7em;
		font-weight: bold;
	}
	
	#successSpan {
		color: green;
		font-size: 1.7em;
		font-weight: bold;
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
	var dataView3;
	var issueMaterialCol;
	var issuedMaterialCol;
	var grid;
	var grid2;
	var grid3;
	var data2 = ${StringUtil.wrapString(additionalChargesJSON)!'[]'};
	var data3;
	var productLabelIdGrid1;
	var productIdLabelGrid1;
	var availableTags = ${StringUtil.wrapString(productItemsJSON)!'[]'};
	var availProdTagsGrid1;
	var availFacilityTagsGrid1 = [];
	var availFacilityTag = [];
	var productDetails = [];
	var productNameObj= ${StringUtil.wrapString(productNameObj)!'[]'};
    var workEffortObj= ${StringUtil.wrapString(workEffortObj)!'[]'};
    var facilityWorkEffObj=${StringUtil.wrapString(facilityWorkEffObj)!'[]'};
    var availFacTagsGrid2 = [];
	var facIdLabelGrid1;
	var facLabelIdGrid1;
	var facIdLabelGrid2;
	var facLabelIdGrid2;
	var availFacTagsGrid3 = [];
	var facIdLabelGrid3;
	var facLabelIdGrid3;
	var data = [];
	var conversionData ={};
	var taskEffortId;
	var gridEditable = 'Y';
	var gridReturnEditable='Y';
	var gridDeclareEditable ='Y';
	function requiredFieldValidator(value) {
		if (value == null || value == undefined || !value.length)
			return {valid:false, msg:"This is a required field"};
		else
			return {valid:true, msg:null};
	}
	function prepareIssueGrid(resultParam, workEffortId, displayBtn){
		if(displayBtn == 'N'){
			data = resultParam['issuedProductItemsJSON'];
		}
		availProdTagsGrid1 = resultParam['productItemsJSON'];
		productIdLabelGrid1 = resultParam['productIdLabelJSON'];
		productLabelIdGrid1 = resultParam['productLabelIdJSON'];
		productDetails = resultParam['productDetailJSON'];
		taskEffortId = workEffortId;
		gridEditable = displayBtn;
		setupGrid1();
	}
	
	function prepareReturnGrid(resultReturnData, workEffortId, displayBtn){
		data3 = resultReturnData['returnProductItemsJSON'];
		availFacTagsGrid3 = resultReturnData['returnToFacilityJSON'];
		facIdLabelGrid3 = resultReturnData['returnToFacilityIdLabelJSON'];
		facLabelIdGrid3 = resultReturnData['returnToFacilityLabelIdJSON'];
		taskEffortId = workEffortId;
		gridReturnEditable = displayBtn;
		setupGrid3();
	}
	
	function prepareDeclareGrid(resultDate, workEffortId, displayBtn){
		data2 = resultDate['declareProductItemsJSON'];
		conversionData = resultDate['conversionJSON'];
		availFacTagsGrid2 = resultDate['moveToFacilityJSON'];
		facIdLabelGrid2 = resultDate['moveToFacilityIdLabelJSON'];
		facLabelIdGrid2 = resultDate['moveToFacilityLabelIdJSON'];
		taskEffortId = workEffortId;
		gridDeclareEditable = displayBtn;
		setupGrid2();
	}
	
	
	var enableSubmit = true;
	function processDeclareComponentEntry(){
		if (Slick.GlobalEditorLock.isActive() && !Slick.GlobalEditorLock.commitCurrentEdit()) {
			return false;		
		}
		var dataJson = {};
		var index = 0;		
		$("#declareDiv").hide();
		for (var rowCount=0; rowCount < data2.length; ++rowCount)
		{ 
			var productId = data2[rowCount]["cDeclareProductId"];
			var qty = parseFloat(data2[rowCount]["declareQuantity"]);
			var toFacilityId = data2[rowCount]["toFacilityId"];
	 		if (!isNaN(qty) && qty>0) {
	 			dataJson['productId_o_'+index] = productId;
	 			dataJson['quantity_o_'+index] = qty;
	 			dataJson['toFacilityId_o_'+index] = toFacilityId;
				index += 1;
   			}
		}
		dataJson['workEffortId'] = taskEffortId;
		var action = "declareRoutingTaskMaterial";
		$.ajax({
			 type: "POST",
             url: action,
             data: dataJson,
             dataType: 'json',
			success:function(result){
				if(result["_ERROR_MESSAGE_"] || result["_ERROR_MESSAGE_LIST_"]){
					msg = result["_ERROR_MESSAGE_"];
					if(result["_ERROR_MESSAGE_LIST_"] =! undefined){
						msg =msg+result["_ERROR_MESSAGE_LIST_"] ;
					}
					var formattedMsg = "<div style='background-color:#E7E5E5'><span id='errorSpan'>"+msg+"</span></div>";
					$('div#displayDeclareMessage').html(formattedMsg);
              	    $('div#displayDeclareMessage').delay(8000).fadeOut('slow');
				}else{
					$('#declareMaterialDiv').hide();
					$("#declareSave").hide();
					$('div#displayDeclareMessage').html("<div style='background-color:#E7E5E5'><span id='successSpan'>Successfully Updated</span></div>"); 
					$('div#displayDeclareMessage').delay(8000).fadeOut('slow');
					$('#declareTask').trigger("click");
				}					
			},
			error: function (xhr, textStatus, thrownError){
				alert("record not found :: Error code:-  "+xhr.status);
			}							
		});
		
	}
	function processReturnComponentEntry(){
		
		if (Slick.GlobalEditorLock.isActive() && !Slick.GlobalEditorLock.commitCurrentEdit()) {
			return false;		
		}
		var dataJson = {};
		var index = 0;		
		$("#returnSubmitDiv").hide();
		for (var rowCount=0; rowCount < data3.length; ++rowCount)
		{ 
			var productId = data3[rowCount]["cReturnProductId"];
			var qty = parseFloat(data3[rowCount]["returnQuantity"]);
			var returnFacilityId = data3[rowCount]["returnFacilityId"];
			var description = data3[rowCount]["description"];
	 		if (!isNaN(qty) && qty>0) {
	 			dataJson['productId_o_'+index] = productId;
	 			dataJson['quantity_o_'+index] = qty;
	 			dataJson['toFacilityId_o_'+index] = returnFacilityId;
	 			dataJson['description_o_'+index] = description;
				index += 1;
   			}
		}
		dataJson['workEffortId'] = taskEffortId;
		dataJson['transferGroupTypeId'] = "RETURN_XFER";
		var action = "returnUnusedMaterialOfRoutingTask";
		
		$.ajax({
			 type: "POST",
             url: action,
             data: dataJson,
             dataType: 'json',
			success:function(result){
				if(result["_ERROR_MESSAGE_"] || result["_ERROR_MESSAGE_LIST_"]){
					msg = result["_ERROR_MESSAGE_"];
					if(result["_ERROR_MESSAGE_LIST_"] =! undefined){
						msg =msg+result["_ERROR_MESSAGE_LIST_"] ;
					}
					var formattedMsg = "<div style='background-color:#E7E5E5'><span id='errorSpan'>"+msg+"</span></div>";
					$('div#displayDeclareMessage').html(formattedMsg);
              	    $('div#displayDeclareMessage').delay(8000).fadeOut('slow');
				}else{
					//$('#returnMaterialDiv').hide();
					$('#returnMaterialSave').hide();
					var formattedMsg = "<div style='background-color:#E7E5E5'><span id='successSpan'>Successfully Updated</span></div>";
					$('div#displayDeclareMessage').html(formattedMsg);
              	    $('div#displayDeclareMessage').delay(8000).fadeOut('slow');
              	    $('#declareTask').trigger("click");
				}					
			},
			error: function (xhr, textStatus, thrownError){
				alert("record not found :: Error code:-  "+xhr.status);
			}							
		});
	}
	
	function processIssueComponentEntry() {
		
		if (Slick.GlobalEditorLock.isActive() && !Slick.GlobalEditorLock.commitCurrentEdit()) {
			return false;		
		}
		var dataJson = {};
		var index = 0;		
		$("#issueSubmitDiv").hide();
		for (var rowCount=0; rowCount < data.length; ++rowCount)
		{ 
			var productId = data[rowCount]["cIssueProductId"];
			var qty = parseFloat(data[rowCount]["issueQuantity"]);
			var facilityId = data[rowCount]["issueFacilityId"];
	 		if (!isNaN(qty) && qty>0 && facilityId) {
	 			dataJson['productId_o_'+index] = productId;
	 			dataJson['quantity_o_'+index] = qty;
	 			dataJson['facilityId_o_'+index] = facilityId;
				index += 1;
   			}
		}
		dataJson['workEffortId'] = taskEffortId;
		var action = "issueRoutingTaskNeededMaterial";
		
		$.ajax({
			 type: "POST",
             url: action,
             data: dataJson,
             dataType: 'json',
			success:function(result){
				if(result["_ERROR_MESSAGE_"] || result["_ERROR_MESSAGE_LIST_"]){
					msg = result["_ERROR_MESSAGE_"];
					if(result["_ERROR_MESSAGE_LIST_"] =! undefined){
						msg =msg+result["_ERROR_MESSAGE_LIST_"] ;
					}
					var formattedMsg = "<div style='background-color:#E7E5E5'><span id='errorSpan'>"+msg+"</span></div>";
					$('#displayMessage').html(formattedMsg);
              	    $('div#displayMessage').delay(8000).fadeOut('slow');
				}else{
					$('div#displayMessage').html("<div style='background-color:#E7E5E5'><span id='successSpan'>Successfully Updated</span></div>"); 
					$('div#displayMessage').delay(8000).fadeOut('slow');
					$('#addMaterialDiv').hide();
					$('#issueMaterialBtn').hide();
				}					
			},
			error: function (xhr, textStatus, thrownError){
				alert("record not found :: Error code:-  "+xhr.status);
			}							
		});
	}
	
    function issueProductFormatter(row, cell, value, columnDef, dataContext) {
    	return productIdLabelGrid1[value];
    }
	function issueFacilityFormatter(row, cell, value, columnDef, dataContext){
		if(value){
			if(facLabelIdGrid1 && facLabelIdGrid1[value]){
				var entryVal = facLabelIdGrid1[value];
				data[row]['issueFacilityId'] = entryVal;
				return facLabelIdGrid1[value];
			}
			if(facIdLabelGrid1 && facIdLabelGrid1[value]){
				return facIdLabelGrid1[value];
			}
		}
		return "";
	}
	
	function facilityFormatter(row, cell, value, columnDef, dataContext) {
		if(value){
			if(facLabelIdGrid2 && facLabelIdGrid2[value]){
				var entryVal = facLabelIdGrid2[value];
				data2[row]['toFacilityId'] = entryVal;
				return facLabelIdGrid2[value];
			}
			if(facIdLabelGrid2 && facIdLabelGrid2[value]){
				return facIdLabelGrid2[value];
			}
		}
		return "";
    }
    
    function facilityValidator(value,item) {
    	
    	var invalid = 1;
    	var valueId;
    	if(value){
			if(facLabelIdGrid1 && facLabelIdGrid1[value]){
				valueId = facLabelIdGrid1[value];
			}
			else if(facLabelIdGrid1 && facLabelIdGrid1[value]){
				valueId = facIdLabelGrid2[value];
			}
		}
	  	for (var rowCount=0; rowCount < data.length; ++rowCount)
	  	{ 
			var prod = data[rowCount]["cIssueProductId"];
			if(productDetails){
				var prodDetMap = {};
				prodDetMap = productDetails[prod];
				var facilityAvl = prodDetMap['productFacilityJSON'];
				var errorMsg = 0;
				
				for(var index=0; index < facilityAvl.length; ++index){
					var allowedFacility = facilityAvl[index]["value"];
					if(allowedFacility == valueId){
						errorMsg = 1;
					}
				}
				if(errorMsg == 1){
					invalid = 0;
				}
			}
	  	}
	  	if(invalid == 1){
	  		return {valid: false, msg: "Facility Not Allowed" + value};
	  	}
	    return {valid: true, msg: null};
    }
	function returnFacilityFormatter(row, cell, value, columnDef, dataContext) {
		if(value){
			/*if(facLabelIdGrid3 && facLabelIdGrid3[value]){
				return facLabelIdGrid3[value];
			}*/
			if(facIdLabelGrid3 && facIdLabelGrid3[value]){
				return facIdLabelGrid3[value];
			}
		}
		return "";
    }
    
    //quantity validator
	function quantityFormatter(row, cell, value, columnDef, dataContext) { 
		if(value == null){
			return "";
		}
        return  value;
    }
    //crates validator
	function cratesFormatter(row, cell, value, columnDef, dataContext) { 
		if(value == null){
			return "";
		}
        return  value;
    }
	
	function returnQuantityValidator(value ,item) {
		for (var rowCount=0; rowCount < data2.length; ++rowCount)
  		{ 
			if (data2[rowCount]['declareQuantity'] != null && data2[rowCount]['declareQuantity'] != undefined) {
				
				if(value && value <= 0){
					return {valid: false, msg: "Cannot Return quantity of value less than 1 "};
				}
			}
  		}
		return {valid: true, msg: null};
	}
	
	
	function quantityValidator(value ,item) {
		var quarterVal = value*4;
		var floorValue = Math.floor(quarterVal);
		var remainder = quarterVal - floorValue;
		var remainderVal =  Math.floor(value) - value;
		 if(parseInt(value) <0 ){
			return {valid: false, msg: "required quantity Should not be less than or equals to zero" + value};
		 }
	     
      return {valid: true, msg: null};
    }
   function cratesValidator(value ,item) {
		var quarterVal = value*4;
		var floorValue = Math.floor(quarterVal);
		var remainder = quarterVal - floorValue;
		var remainderVal =  Math.floor(value) - value;
		 if(parseInt(value) <0 ){
			return {valid: false, msg: "required quantity Should not be less than or equals to zero" + value};
		 }
	     
      return {valid: true, msg: null};
    }
    
    
	var mainGrid;		
	function setupGrid1() {
		    issueMaterialCol = [
					{id:"cIssueProductName", name:"Material", field:"cIssueProductName", width:240, minWidth:240, cssClass:"cell-title", regexMatcher:"contains", availableTags: availProdTagsGrid1, editor: AutoCompleteEditor,formatter: issueProductFormatter, sortable:false, toolTip:""},
					{id:"issueQuantity", name:"Quantity", field:"issueQuantity", width:80, minWidth:80, editor:FloatCellEditor, cssClass:"cell-title", formatter: quantityFormatter,validator: quantityValidator, sortable:false},
					{id:"UOM", name:"UOM", field:"uomDescription", width:80, minWidth:80, cssClass:"readOnlyColumnClass", focusable :false,editor:FloatCellEditor, sortable:false},
					{id:"issueFacilityId", name:"From Store", field:"issueFacilityId", width:140, minWidth:140, cssClass:"cell-title", regexMatcher:"contains", availableTags: availFacilityTagsGrid1, editor: AutoCompleteEditor,formatter: issueFacilityFormatter, validator: facilityValidator, sortable:false},
					{id:"inventoryAvl", name:"Stock Avl. to Issue[ATP]", field:"inventoryAvl", width:140, minWidth:140, cssClass:"readOnlyColumnClass",editor:FloatCellEditor, focusable :false, sortable:false}
			];
			
			issuedMaterialCol = [
					{id:"cIssueProductName", name:"Material", field:"cIssueProductName", width:240, minWidth:240, cssClass:"readOnlyColumnClass", sortable:false, focusable :false,toolTip:""},
					{id:"issueQuantity", name:"Quantity", field:"issueQuantity", width:80, minWidth:80, editor:FloatCellEditor, cssClass:"readOnlyColumnClass", focusable :false,sortable:false},
					{id:"UOM", name:"UOM", field:"uomDescription", width:80, minWidth:80, cssClass:"readOnlyColumnClass", focusable :false,editor:FloatCellEditor, focusable :false,sortable:false},
					{id:"issueFacilityId", name:"From Store", field:"issueFacilityId", width:140, minWidth:140, cssClass:"readOnlyColumnClass", focusable :false,sortable:false}
			];
			var columns;
			if(gridEditable == "Y"){
				columns = issueMaterialCol;
			}
			else{
				columns = issuedMaterialCol;
			}
			
			var options = {
			editable: true,		
			forceFitColumns: false,			
			enableCellNavigation: true,
			enableColumnReorder: false,
			enableAddRow: true,
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
      		var productLabel = item['cIssueProductName'];
      		item['cIssueProductName'] = productLabelIdGrid1[productLabel];
      		item['cIssueProductId'] = productLabelIdGrid1[productLabel];
      		if(typeof(productNameObj)!= "undefined"){
      				var productId = productLabelIdGrid1[productLabel]; 
					var productDetObj=productNameObj[productId];
	      			if(typeof(productDetObj)!= "undefined"){	
						item['uomDescription'] =productDetObj['uomId'];
					}
				}
      		
      		
      		grid.invalidateRow(data.length);
      		data.push(item);
      		grid.updateRowCount();
      		grid.render();
    	});
    	
        grid.onCellChange.subscribe(function(e,args) {
			if (args.cell == 0 || args.cell == 1) {
				var prod = data[args.row]["cIssueProductId"];
				if(productDetails){
					var prodDetMap = {};
					prodDetMap = productDetails[prod];
					availFacilityTagsGrid1 = prodDetMap['productFacilityJSON'];
					facIdLabelGrid1 = prodDetMap['facilityIdLabelJSON'];
					facLabelIdGrid1 = prodDetMap['facilityLabelIdJSON'];
					var setData = grid.getColumns();
					setData[3].availableTags = availFacilityTagsGrid1;
					grid.setColumns(setData);
				}
				grid.updateRow(args.row);
			}
			if(args.cell == 3){
				
				var prod = data[args.row]["cIssueProductId"];
				if(productDetails){
					var prodDetMap = {};
					prodDetMap = productDetails[prod];
					var productInventoryMap = prodDetMap['facilityInventoryJSON'];
					var facilityId = data[args.row]['issueFacilityId'];
					data[args.row]['inventoryAvl'] = productInventoryMap[facilityId];
					data[args.row]["issueFacilityId"] = facilityId;
					
				}
				grid.updateRow(args.row);
			}
			
		}); 
		
		grid.onActiveCellChanged.subscribe(function(e,args) {
        	if (args.cell == 1 && data[args.row] != null) {
				var prod = data[args.row]["cIssueProductId"];
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
	
	function setupGrid2() {
	
	
		   var declareColumn = [
		   			{id:"cDeclareProductName", name:"Product", field:"cDeclareProductName", width:240, minWidth:240, cssClass:"readOnlyColumnClass", focusable :false, editor:FloatCellEditor, sortable:false, toolTip:""},
					{id:"declareCrates", name:"Crates", field:"declareCrates", width:80, minWidth:80, editor:FloatCellEditor, cssClass:"cell-title", formatter: cratesFormatter, validator: cratesValidator, sortable:false},
					{id:"declareQuantity", name:"Quantity", field:"declareQuantity", width:80, minWidth:80, editor:FloatCellEditor, cssClass:"cell-title", formatter: quantityFormatter, validator: quantityValidator, sortable:false},
					{id:"declareUom", name:"UOM", field:"declareUom", width:80, minWidth:80, cssClass:"readOnlyColumnClass", focusable :false,editor:FloatCellEditor, sortable:false},
					{id:"toFacilityId", name:"Move To", field:"toFacilityId", width:120, minWidth:120, cssClass:"cell-title", regexMatcher:"contains", availableTags: availFacTagsGrid2, editor: AutoCompleteEditor, formatter: facilityFormatter, sortable:false}
			];
			
			var declaredColumn = [
		   			{id:"cDeclareProductName", name:"Product", field:"cDeclareProductName", width:240, minWidth:240, cssClass:"readOnlyColumnClass", focusable :false, editor:FloatCellEditor, sortable:false, toolTip:""},
					{id:"declareCrates", name:"Crates", field:"declareCrates", width:80, minWidth:80, editor:FloatCellEditor, cssClass:"readOnlyColumnClass", focusable :false, sortable:false},
					{id:"declareQuantity", name:"Quantity", field:"declareQuantity", width:80, minWidth:80, editor:FloatCellEditor, cssClass:"readOnlyColumnClass", focusable :false, sortable:false},
					{id:"declareUom", name:"UOM", field:"declareUom", width:80, minWidth:80, cssClass:"readOnlyColumnClass", focusable :false,editor:FloatCellEditor, sortable:false},
					{id:"toFacilityId", name:"Move To", field:"toFacilityId", width:120, minWidth:120, cssClass:"readOnlyColumnClass", focusable :false, sortable:false}
			];

			if(gridDeclareEditable == "Y"){
				columns = declareColumn;
			}
			else{
				columns = declaredColumn;
			}
			
			var options = {
				editable: true,		
				forceFitColumns: false,			
				enableCellNavigation: true,
				enableColumnReorder: false,
				enableAddRow: true,
				asyncEditorLoading: false,			
				autoEdit: true,
	            secondaryHeaderRowHeight: 25
			};
		

		grid2 = new Slick.Grid("#myGrid2", data2, columns, options);
        grid2.setSelectionModel(new Slick.CellSelectionModel());        
		var columnpicker = new Slick.Controls.ColumnPicker(columns, grid2, options);
		
		// wire up model events to drive the grid
        if (data2.length > 0) {			
			$(grid2.getCellNode(0, 2)).click();
		}else{
			$(grid2.getCellNode(0,0)).click();
		}
         grid2.onKeyDown.subscribe(function(e) {
			var cellNav = 2;
			var cell = grid2.getCellFromEvent(e);		
			if(e.which == $.ui.keyCode.UP && cell.row == 0){
				grid2.getEditController().commitCurrentEdit();	
				$(grid2.getCellNode(cell.row+1, 0)).click();
				e.stopPropagation();
			}
			else if((e.which == $.ui.keyCode.DOWN || e.which == $.ui.keyCode.ENTER) && cell.row == data2.length && cell.cell == cellNav){
				grid2.getEditController().commitCurrentEdit();	
				$(grid2.getCellNode(0, 2)).click();
				e.stopPropagation();
			}else if((e.which == $.ui.keyCode.DOWN || e.which == $.ui.keyCode.ENTER) && cell.row == (data2.length-1) && cell.cell == cellNav){
				grid2.getEditController().commitCurrentEdit();
				grid2.gotoCell(data2.length, 0, true);
				$(grid2.getCellNode(data2.length, 0)).edit();
				
				e.stopPropagation();
			}
			
			else if((e.which == $.ui.keyCode.DOWN || e.which == $.ui.keyCode.RIGHT) && cell 
				&& cell.row == data.length && cell.cell == cellNav){
  				grid2.getEditController().commitCurrentEdit();	
				$(grid2.getCellNode(cell.row, 0)).click();
				e.stopPropagation();
			
			}else if (e.which == $.ui.keyCode.RIGHT &&
				cell && (cell.cell == cellNav) && 
				cell.row != data2.length) {
				grid2.getEditController().commitCurrentEdit();	
				$(grid2.getCellNode(cell.row+1, 0)).click();
				e.stopPropagation();	
			}
			else if (e.which == $.ui.keyCode.LEFT &&
				cell && (cell.cell == 0) && 
				cell.row != data2.length) {
				grid2.getEditController().commitCurrentEdit();	
				$(grid2.getCellNode(cell.row, cellNav)).click();
				e.stopPropagation();	
			}else if (e.which == $.ui.keyCode.ENTER) {
        		grid2.getEditController().commitCurrentEdit();
				if(cell.cell == 1 || cell.cell == 2){
					jQuery("#changeSave").click();
				}
            	e.stopPropagation();
            	e.preventDefault();        	
            }else if (e.keyCode == 27) {
            //here ESC to Save grid
        		if (cell && cell.cell == 0) {
        			$(grid2.getCellNode(cell.row - 1, cellNav)).click();
        			return false;
        		}  
        		grid2.getEditController().commitCurrentEdit();
				   
            	e.stopPropagation();
            	e.preventDefault();        	
            }
            
            else {
            	return false;
            }
        });
        
                
    	grid2.onAddNewRow.subscribe(function (e, args) {
      		var item = args.item;
      		grid2.invalidateRow(data2.length);
      		data2.push(item);
      		grid2.updateRowCount();
      		grid2.render();
    	});
        grid2.onCellChange.subscribe(function(e,args) {
        
        if (args.cell == 0 || args.cell == 2) {
				var prod = data2[args.row]["cDeclareProductId"];
				var prodConversionData = conversionData[prod];
				var calcQty = 0;
					calcQty = parseFloat(data2[args.row]["declareQuantity"]);
				var convValue = 1;
				if(prodConversionData['CRATE']){
                 	convValue = prodConversionData['CRATE'];
				}
				if(prodConversionData['LtrKg']){
                 	ltrKg = prodConversionData['LtrKg'];
				}
				var calculateQty = 0;
				if(ltrKg != 'undefined' && ltrKg != null && ltrKg>0){
					declareboxes =convValue*ltrKg;
					declareCrates = parseFloat(Math.round((calcQty/declareboxes)*1000)/1000);
					data2[args.row]["declareCrates"] = declareCrates;
				}
				if(isNaN(declareCrates)){
					declareCrates = 0;
				}
				grid2.updateRow(args.row);

			} 
        if (args.cell == 1) {
				var prod = data2[args.row]["cDeclareProductId"];
                var prodConversionData = conversionData[prod];
				
				var calcQty = 0;
					calcQty = parseFloat(data2[args.row]["declareCrates"]);
				var convValue = 1;
				if(prodConversionData['CRATE']){
					convValue = prodConversionData['CRATE'];
					LtrKg = prodConversionData['LtrKg'];
                }	
                var totBoxes = parseInt(calcQty*convValue);
               	var calculateQty = 0;
                if(prodConversionData['LtrKg']){
                	ltrKg = prodConversionData['LtrKg'];
                	calculateQty = parseFloat(Math.round((ltrKg*totBoxes)*100)/100);
					data2[args.row]["declareQuantity"] = calculateQty;
				}
				if(isNaN(calculateQty)){
					calculateQty = 0;
				}
				grid2.updateRow(args.row);

			} 
			if(args.cell == 3){
				var facValue = data2[args.row]["toFacilityId"];
				if(facValue){
					var facId = facLabelIdGrid2[facValue];
					data2[args.row]["toFacilityId"] = facId;
					grid2.updateRow(args.row);
				}
			}
		}); 
		
		grid2.onActiveCellChanged.subscribe(function(e,args) {
			
		});
		
		grid2.onValidationError.subscribe(function(e, args) {
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
	    mainGrid = grid2;
	}
	
	function setupGrid3() {
		    var returnColumns = [
					{id:"cReturnProductName", name:"Material", field:"cReturnProductName", width:180, minWidth:180, cssClass:"readOnlyColumnClass", focusable :false,editor:FloatCellEditor, sortable:false, toolTip:""},
					{id:"returnQuantity", name:"Qty", field:"returnQuantity", width:50, minWidth:50, editor:FloatCellEditor, cssClass:"cell-title", formatter: quantityFormatter, validator: returnQuantityValidator, sortable:false},
					{id:"returnUOM", name:"UOM", field:"returnUom", width:50, minWidth:50, cssClass:"readOnlyColumnClass", focusable :false,editor:FloatCellEditor, sortable:false},
					//{id:"returnReasonId", name:"Reason", field:"returnReasonId", width:100, minWidth:100, cssClass:"cell-title", options: dropDownOption, editor: SelectCellEditor,sortable:false},
					{id:"returnFacilityId", name:"Move To", field:"returnFacilityId", width:120, minWidth:120, cssClass:"cell-title", regexMatcher:"contains", availableTags: availFacTagsGrid3, editor: AutoCompleteEditor, formatter: returnFacilityFormatter, sortable:false},
					{id:"description", name:"Comment", field:"description", width:150, minWidth:150, editor:LongTextCellEditor,	sortable:false, align:"right", toolTip:"Return Reason"}
			];
			
			var returnedColumn = [
		   			{id:"cReturnProductName", name:"Material", field:"cReturnProductName", width:180, minWidth:180, cssClass:"readOnlyColumnClass", focusable :false,editor:FloatCellEditor, sortable:false, toolTip:""},
					{id:"returnQuantity", name:"Qty", field:"returnQuantity", width:50, minWidth:50, editor:FloatCellEditor, cssClass:"readOnlyColumnClass", focusable :false, sortable:false},
					{id:"returnUOM", name:"UOM", field:"returnUom", width:50, minWidth:50, cssClass:"readOnlyColumnClass", focusable :false,editor:FloatCellEditor, sortable:false},
					{id:"returnFacilityId", name:"Move To", field:"returnFacilityId", width:120, minWidth:120, cssClass:"readOnlyColumnClass", focusable :false, sortable:false},
					{id:"description", name:"Comment", field:"description", width:150, minWidth:150, editor:LongTextCellEditor,	cssClass:"readOnlyColumnClass", focusable :false, sortable:false, toolTip:"Return Reason"}
			];
			if(gridReturnEditable == "Y"){
				columns = returnColumns;
			}
			else{
				columns = returnedColumn;
			}
			
			
			var options = {
			editable: true,		
			forceFitColumns: false,			
			enableCellNavigation: true,
			enableColumnReorder: false,
			enableAddRow: true,
			asyncEditorLoading: false,			
			autoEdit: true,
            secondaryHeaderRowHeight: 25
            
		};
		

		grid3 = new Slick.Grid("#myGrid3", data3, columns, options);
        grid3.setSelectionModel(new Slick.CellSelectionModel());        
		var columnpicker = new Slick.Controls.ColumnPicker(columns, grid3, options);
		
		// wire up model events to drive the grid
        if (data3.length > 0) {			
			$(grid3.getCellNode(0, 1)).click();
		}else{
			$(grid3.getCellNode(0,0)).click();	
		}
		
		grid3.onValidationError.subscribe(function(e, args) {
	        var validationResult = args.validationResults;
	        var activeCellNode = args.cellNode;
	        var editor = args.editor;
	        var errorMessage = validationResult.msg;
	        alert(errorMessage);
	        /*var valid_result = validationResult.valid;
	        if (!valid_result) {
	           $(activeCellNode).attr("title", errorMessage);
	            }
	        else {
	           $(activeCellNode).attr("title", "");
	        }*/
	    });
         grid3.onKeyDown.subscribe(function(e) {
			var cellNav = 2;
			var cell = grid3.getCellFromEvent(e);		
			if(e.which == $.ui.keyCode.UP && cell.row == 0){
				grid3.getEditController().commitCurrentEdit();	
				$(grid3.getCellNode(cell.row+1, 0)).click();
				e.stopPropagation();
			}
			else if((e.which == $.ui.keyCode.DOWN || e.which == $.ui.keyCode.ENTER) && cell.row == data3.length && cell.cell == cellNav){
				grid3.getEditController().commitCurrentEdit();	
				$(grid3.getCellNode(0, 2)).click();
				e.stopPropagation();
			}else if((e.which == $.ui.keyCode.DOWN || e.which == $.ui.keyCode.ENTER) && cell.row == (data3.length-1) && cell.cell == cellNav){
				grid3.getEditController().commitCurrentEdit();
				grid3.gotoCell(data3.length, 0, true);
				$(grid3.getCellNode(data3.length, 0)).edit();
				
				e.stopPropagation();
			}
			
			else if((e.which == $.ui.keyCode.DOWN || e.which == $.ui.keyCode.RIGHT) && cell 
				&& cell.row == data.length && cell.cell == cellNav){
  				grid3.getEditController().commitCurrentEdit();	
				$(grid3.getCellNode(cell.row, 0)).click();
				e.stopPropagation();
			
			}else if (e.which == $.ui.keyCode.RIGHT &&
				cell && (cell.cell == cellNav) && 
				cell.row != data3.length) {
				grid3.getEditController().commitCurrentEdit();	
				$(grid3.getCellNode(cell.row+1, 0)).click();
				e.stopPropagation();	
			}
			else if (e.which == $.ui.keyCode.LEFT &&
				cell && (cell.cell == 0) && 
				cell.row != data3.length) {
				grid3.getEditController().commitCurrentEdit();	
				$(grid3.getCellNode(cell.row, cellNav)).click();
				e.stopPropagation();	
			}else if (e.which == $.ui.keyCode.ENTER) {
        		grid3.getEditController().commitCurrentEdit();
				if(cell.cell == 1 || cell.cell == 2){
					jQuery("#changeSave").click();
				}
            	e.stopPropagation();
            	e.preventDefault();        	
            }else if (e.keyCode == 27) {
            //here ESC to Save grid
        		if (cell && cell.cell == 0) {
        			$(grid3.getCellNode(cell.row - 1, cellNav)).click();
        			return false;
        		}  
        		grid3.getEditController().commitCurrentEdit();
				   
            	e.stopPropagation();
            	e.preventDefault();        	
            }
            
            else {
            	return false;
            }
        });
        
                
    	grid3.onAddNewRow.subscribe(function (e, args) {
      		var item = args.item;   
      		var productLabel = item['cProductName']; 
      		item['cProductId'] = productLabelIdMap[productLabel];     		 		
      		grid3.invalidateRow(data3.length);
      		data3.push(item);
      		grid3.updateRowCount();
      		grid3.render();
    	});
        grid3.onCellChange.subscribe(function(e,args) {
			if (args.cell == 0 || args.cell == 1) {
				//var prod = data3[args.row]["cProductId"];
				grid3.updateRow(args.row);
			}
			if(args.cell == 3){
				var facValue = data3[args.row]["returnFacilityId"];
				if(facValue){
					var facId = facLabelIdGrid3[facValue];
					data3[args.row]["returnFacilityId"] = facId;
					grid3.updateRow(args.row);
				}
			}
		}); 
		
		grid3.onActiveCellChanged.subscribe(function(e,args) {
        	if (args.cell == 1 && data3[args.row] != null) {
				var prod = data3[args.row]["cProductId"];
			}
			
		});
		
		grid3.onValidationError.subscribe(function(e, args) {
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
		
		mainGrid = grid3;
	}
// to show special related fields in form			
		 
</script>			