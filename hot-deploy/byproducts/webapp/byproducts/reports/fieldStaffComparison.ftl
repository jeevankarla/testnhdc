
<!--[if lte IE 8]><script language="javascript" type="text/javascript" src="../excanvas.min.js"></script><![endif]-->

<script  language="javascript" type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/flot/jquery.flot.js</@ofbizContentUrl>"></script>
<script  language="javascript" type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/flot/jquery.flot.tickrotor.js"</@ofbizContentUrl>></script>
<script  language="javascript" type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/flot/jquery.flot.axislabels.js"</@ofbizContentUrl>></script>
		
			
<script type="application/javascript">  


$(document).ready(function(){
		jQuery.plot($("#graph"), [{data: ${StringUtil.wrapString(fieldStaffDataListJSON)},
			points: { show: false },
									bars: {show: true,
                   						   	barWidth: 0.15,
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
         				max : ${fieldStaffDataListJSON.size()} + 1,
         				ticks:${StringUtil.wrapString(labelsJSON)},
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
                var x = item.datapoint[0].toFixed(2),
                    y = item.datapoint[1].toFixed(2);
                    noofAgents = item.datapoint[2].toFixed(0);
                var content ="<br> Rs. "+y; 
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
			width: 950px;
			height: 500px;
		}
	</style>


<div class="screenlet">
    <div class="screenlet-title-bar">
  
      	<h3>Comparison Data of Field Staff [${froDate?date} - ${toDate?date}]</h3>

    </div>
    <div class="screenlet-body">
   		<div id="graph" class="graph"></div>  
        <br><br><br><br>   		   		
    </div>   
</div>

