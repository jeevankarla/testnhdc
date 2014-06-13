
$(function() {
       $('input[name=boothId]').autocomplete({ source: boothsList }).keydown(function(e){
    	   $.ui.autocomplete.filter = function (array, term) { 
               var matcher = new RegExp($.ui.autocomplete.escapeRegex(term), "i");
               return $.grep(array, function (value) {
                   return matcher.test(value.label || value.value || value);
               });
           };

			if (e.keyCode === 13){
		      	 $('input[name=boothId]').autocomplete('close');
		      	 if( typeof $("input[name=boothId]").val() != "undefined"   && $("input[name=boothId]").val() != ""){ 
		      	 }
				return false;
			}
		});
       // booth auto Complete
     $('input[name=boothId]').keypress(function (e) {
			$('input[name=boothId]').autocomplete({ source: boothsList ,filterOpt : ""});	
			$.ui.autocomplete.filter = function (array, term) { 
                var matcher = new RegExp($.ui.autocomplete.escapeRegex(term), "i");
                return $.grep(array, function (value) {
                    return matcher.test(value.label || value.value || value);
                });
            };
	 });
     
  
     // route auto Complete
     $('#routeId').blur(function (e) {    	 	
			//$("#routeId").autocomplete({ source: routesList , onblur: function( event, ui ) {
		var rt = $("#routeId").val();		
 	 $('span#routeTooltip').html('<label>'+rt+'</label>');
 	 
			//} });	
	 });
  // route auto Complete
  $('#routeId').keypress(function (e) {    	 	
			$("#routeId").autocomplete({ source: routesList , select: function( event, ui ) {
				$('span#routeTooltip').html('<label>'+ui.item.label+'</label>');
			} });	
	 });
  
     $('#routeId').focus(function (e) {    	 	
			$("#routeId").autocomplete({ source: routesList });	
	 }); 
     
     changeRowColor();
});

//we have to use color scheme for vehcileStatus List
function changeRowColor(){
	   /* var cell;
	    var result = $('tr').find("td:contains('VEHICLE_RETURNED')");
	    alert("==text="+$(result).text()+"=afterTrim="+$.trim($(result).text()));
	    var compText=$.trim($(result).text());
	    if(compText=="VEHICLE_RETURNED"){
	    	var resParent = $(result).parent();
	    	$(resParent).css("background-color", "#D76871");
	   // $(this).css("background-color", "#FFCC88");
        }else{
        	$(resParent).css("background-color", "green");
        }
	    */
	 var chkBox=$('#bulkCheckBox'); 
	 $(chkBox).css("text-align","right");
	
	var tableObj = $('#_col table tr');
	$(tableObj).each( function( index, element ){
	    var result = $(this).find("td:contains('Finalized')");
	    var compText=$.trim($(result).text());
	    if (compText==("Finalized")){
	    	 $(this).css("background-color", "#77BA72");
	    }else if(compText==("CratesReturned")){
	    	 $(this).css("background-color", "#98928F");
	    }else{
	    	$(this).css("background-color", "#FFCC88");
	    }
	});
	
   // 
}
 function checkAllRouteStatus(master){
	 var shipIds = $('input[name="bulkStatusChange"]');
    jQuery.each(shipIds, function() {
         this.checked = master.checked;
     });
	
 }
 function getAllRouteShipments(){
	 var shipIdsList = $('input[name=bulkStatusChange]:checked');
	 if(shipIdsList.size() <=0) {
	 alert("Please Select at least One Vehicle..!")
		 return false;
	 }
      jQuery.each(shipIdsList, function() {
    	 var shipmentId=$(this).val();
    	  $('#ListSubmitBulk').append('<input type="hidden" name="shipmentIds" value="'+shipmentId+'" />');
     });
      jQuery('#ListSubmitBulk').submit();
 }






