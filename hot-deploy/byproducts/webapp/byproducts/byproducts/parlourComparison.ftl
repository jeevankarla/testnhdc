
<!--[if lte IE 8]><script language="javascript" type="text/javascript" src="../excanvas.min.js"></script><![endif]-->

<script  language="javascript" type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/flot/jquery.flot.js</@ofbizContentUrl>"></script>
<script  language="javascript" type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/flot/jquery.flot.tickrotor.js"</@ofbizContentUrl>></script>
<script  language="javascript" type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/flot/jquery.flot.axislabels.js"</@ofbizContentUrl>></script>
		
			
<script type="application/javascript">  


$(document).ready(function(){
		var labels = ${StringUtil.wrapString(labelsJSON)};
		jQuery.plot($("#graph"), [{data: ${StringUtil.wrapString(parlourDataListJSON)},
			points: { show: false },
									bars: {show: true,
                   						   	barWidth: 0.7,
                   							points: { show: false }, 
                   							align: 'center'}}],
				{ 
					series: {
                   		points: { show: true }                   				
                	}, 
               		grid: { hoverable: true},                 	 
                  	yaxes: [
                      { position: 'left',
                      	min: 0,
 						axisLabel : 'Revenue (Rs)' }
                  	],                	 
     				xaxis: {
         				min: 0,
         				max : ${parlourDataListJSON.size()} + 1,
         				ticks:labels,
         				rotateTicks: 140
     				},                   	         		             		            		
				});


    function showTooltip(x, y, contents) {
        $('<div id="tooltip">' + contents + '</div>').css( {
            position: 'absolute',
            display: 'none',
            top: y + 5,
            left: x + 5,
            border: '1px solid #fdd',
            padding: '2px',
            'background-color': '#fee',
            opacity: 0.80
        }).appendTo("body").fadeIn(200);
    }

    var previousPoint = null;
    $("#graph").bind("plothover", function (event, pos, item) {
        $("#x").text(pos.x.toFixed(2));
        $("#y").text(pos.y.toFixed(2));
        if (item) {
            if (previousPoint != item.dataIndex) {
                previousPoint = item.dataIndex;
                
                $("#tooltip").remove();
                var x = item.datapoint[0],
                    y = item.datapoint[1].toFixed(2);
                var content = labels[x-1][1] + " <br> Rs. "+y; 
                showTooltip(item.pageX, item.pageY,
                            content);
            }
        }
        else {
            $("#tooltip").remove();
            previousPoint = null;            
        }
    });       				
                     
});
</script>
	<style type="text/css">
		div.graph
		{
			width: 2500px;
			height: 500px;
		}
	</style>


<div class="screenlet">
    <div class="screenlet-title-bar">
  
      	<h3><#if reportTarget=="RouteComparison" > Comparison Data of Routes Trading  <#elseif reportTarget=="ShopeeComparison" > Comparison Data of Shopees <#else> Comparison </#if>   [${froDate?date} - ${toDate?date}]</h3>

    </div>
    <div class="screenlet-body">
   		<div id="graph" class="graph"></div>  
        <br><br><br><br>   		   		
    </div>   
</div>

