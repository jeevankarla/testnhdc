<link type="text/css" href="<@ofbizContentUrl>/images/jquery/ui/css/ui-lightness/jquery-ui-1.8.13.custom.css</@ofbizContentUrl>" rel="Stylesheet" />	

<!--[if lte IE 8]><script language="javascript" type="text/javascript" src="../excanvas.min.js"></script><![endif]-->

<script  language="javascript" type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/flot/jquery.flot.js</@ofbizContentUrl>"></script>
<script  language="javascript" type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/flot/jquery.flot.axislabels.js"</@ofbizContentUrl>></script>
 
<#if ajaxUrl != "LMSChartsDayCashReceivablesInternal">
<script  language="javascript" type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/flot/jquery.flot.pie.js"</@ofbizContentUrl>></script>
<script  language="javascript" type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/flot/jquery.flot.tooltip.js"</@ofbizContentUrl>></script>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/slick.grid.css</@ofbizContentUrl>" type="text/css" media="screen" charset="utf-8" />
<link rel="stylesheet" href="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/controls/slick.pager.css</@ofbizContentUrl>" type="text/css" media="screen" charset="utf-8" />
<link rel="stylesheet" href="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/examples/examples.css</@ofbizContentUrl>" type="text/css" media="screen" charset="utf-8" />
<link rel="stylesheet" href="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/controls/slick.columnpicker.css</@ofbizContentUrl>" type="text/css" media="screen" charset="utf-8" />


<script language="javascript" type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/slickgrid/lib/firebugx.js</@ofbizContentUrl>"></script>
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
</#if>

<style>
.button1 {
    background-color: grey;
    border: none;
    color: white;
    padding: 5px 5px;
    text-align: center;
    text-decoration: none;
    display: inline-block;
    font-size: 10px;
    margin: 4px 2px;
    cursor: pointer;
}
input[type=button] {
	color: white;
    padding: .5x 7px;
    background:#008CBA;
    border: .8px solid green;
    border:0 none;
    cursor:pointer;
    -webkit-border-radius: 5px;
    border-radius: 5px; 
}
input[type=button]:hover {
    background-color: #3e8e41;
}
</style>

<input type = "hidden" id="isFind" value=${isFind} >

<script type="text/javascript">

$(document).ready(function(){

$("#counter").hide();

var isFind = $("#isFind").val();

 if(isFind == "Y"){
  $(".screenlet").show();
 }else{
  $(".screenlet").hide(); 
  
}




 var n = localStorage.getItem('on_load_counter');
    if (n === null) {
        n = 0;
    }
    n++;
    localStorage.setItem("on_load_counter", n);
    document.getElementById('counter').innerHTML = n;
    $( ".user" ).click(function() {
        localStorage.setItem("on_load_counter", 0);
});


if(n==1){

  $('div#orderSpinn1').html('<img src="/images/welcome nhdc3.jpg" height="500" width="800">');

}  

// enter event handle
	$("input").keypress(function(e){
		if (e.which == 13 && e.target.name =="facilityId") {
				$("#getTreeGrid").click();
		}
	});  
	
    $('#loader').hide();
	jQuery.ajaxSetup({
  		beforeSend: function() {
     		$('#loader').show();
     		$('#result').hide();
  		},
  		complete: function(){
     		$('#loader').hide();
     		$('#result').show();     
  		}
	});

	$( "#fromDate" ).datepicker({
			dateFormat:'MM d, yy',
			changeMonth: true,
			onSelect: function( selectedDate ) {					
			    date = $(this).datepicker('getDate');
		        y = date.getFullYear(),
		        m = date.getMonth();
		        d = date.getDate();
		        var maxDate = new Date(y+1, m, d);
				$( "#thruDate" ).datepicker( "option", {minDate: selectedDate, maxDate: maxDate}).datepicker('setDate', date);
			}
		});
		$( "#thruDate" ).datepicker({
			dateFormat:'MM d, yy',
			changeMonth: true,
			changeYear: true,
			onSelect: function( selectedDate ) {
				$( "#fromDate" ).datepicker( "option", "maxDate", selectedDate );
			}
		});
		$('#ui-datepicker-div').css('clip', 'auto');

	// fetch the charts for today
	        $.get(  
            "${ajaxUrl}",  
            { fromDate: $("#fromDate").val()},  
            function(responseText){  
                $("#result").html(responseText); 
				var reponse = jQuery(responseText);
       			var reponseScript = reponse.filter("script");
       			// flot does not work well with hidden elements, so we unhide here itself       			
       			$('#loader').hide();
     			$('#result').show(); 
            },  
            "html"  
        );  
        
	// also set the click handler
  	$("#getTreeGrid").click(function(){  
        $.get(  
            "${ajaxUrl}",  
            { fromDate: $("#fromDate").val() ,thruDate: $("#thruDate").val()},  
            function(responseText){  
                $("#result").html(responseText); 
				var reponse = jQuery(responseText);
       			var reponseScript = reponse.filter("script");
       			// flot does not work well with hidden elements, so we unhide here itself
       			$('#loader').hide();
     			$('#result').show();        			
            },  
            "html"  
        );  
    });
    	$("#getTreeGridCsv").click(function(){  
          var fromDate= $("#fromDate").val();
          var thruDate= $("#thruDate").val();
          $("#fromDateCsv").val(fromDate);
          $("#thruDateCsv").val(thruDate);
         var screenFlag="${parameters.screenflag}";
          $("#screenFlag").val(screenFlag);
          $('#csvForm').submit();
    });
});




</script>
  <div align='center' name ='displayMsg' id='orderSpinn1'/></div>

<div id="counter"></div>

<#--

<div class="button-bar tab-bar" >

<pre>
<ul>
<li>
 <ul>
  <li class="selected "><a href="/myportal/control/IndentAnalyticsTab">Indent Analytics</a></li>
  <li><a href="/myportal/control/SupplierAnalyticsSubTabBar">Supply Analytics</a></li>
  <li><a href="/myportal/control/QuotaDashboardAuth">Quota Dashboard</a></li>
 </ul>
</li>
</ul>
 <br class="clear">
</div>
 -->