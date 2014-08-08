

<style type="text/css">
		div.graph
		{
			width: 300px;
			height: 400px;
		}
		label
		{
			display: block;
			margin-left: 400px;
			padding-left: 1em;
		}
		
	.dataTables_filter input {
  		border-style: groove;    
	}
	 #datatable1_filter,  #datatable1_length, #datatable1_info, #datatable1_paginate { 

	 }
	 .myRightAlignClass{ text-align: right;font-weight:bold } 
 
</style>	


<script type="text/javascript" language="javascript" class="init">	

$(document).ready(function() {
	var numPayheads = 0;
	var salaryTableData = ${StringUtil.wrapString(employeesPayrollTableJSON!'[]')};
	var tfooter = '<tfoot><tr><td></td><td></td><td></td>';
	<#if benefitTypes?exists> 
		<#assign len = benefitTypes?size>
		numPayheads = numPayheads + ${len};
		<#list benefitTypes as benefitType>
			tfooter = tfooter + '<td></td>';
		</#list>
	</#if> 
alert("numPayheads="+numPayheads);
	tfooter = tfooter + '</tr></tfoot>';
	$('#salaryAnalysisTable').html( '<table cellpadding="0" cellspacing="0" border="0" class="display" id="datatable1">' + tfooter + '</table>' );

	var datatable1 = $('#datatable1').dataTable( {
		"data": salaryTableData,
		"columns": [
			{ "title": "Id" },	
			{ "title": "Name" },										
			{ "title": "Dept" },	
        <#if benefitTypes?exists> 
	        <#list benefitTypes as benefitType>				
				{"title":"${benefitType}", sClass: "myRightAlignClass", sType: "numeric"  },
			</#list>			
		</#if>											
		],
		"iDisplayLength" : 100,
        "sDom": 'lfTrtip',		
		"tableTools": {
                "sSwfPath": "<@ofbizContentUrl>/images/jquery/plugins/datatables/1.10.0/extensions/TableTools/swf/copy_csv_xls_pdf.swf</@ofbizContentUrl>",
                "aButtons": [ 
                { 
                	"sExtends": "copy", 
                	"oSelectorOpts": { filter: 'applied', order: 'current' }
                },
                { 
                	"sExtends": "csv", 
                	"oSelectorOpts": { filter: 'applied', order: 'current' },
                	"sFileName": "EmployeeWisePayroll.csv"                 	
                },
                { 
                	"sExtends": "pdf", 
                	"oSelectorOpts": { filter: 'applied', order: 'current' },
                	"sFileName": "EmployeeWisePayroll.pdf"   
                } 
                ]
        },
        "footerCallback": function ( row, data, start, end, display ) {
			var api = this.api();
            $( api.column( 0).footer() ).html("Total");                      
            // Total over all pages
            for (i = 0; i < numPayheads; ++i) {
            	var index = 3 + i;
            	columnData = api.column( index ).data();
            	total = 0;
				$.each(columnData,function(){total+=parseFloat(this) || 0;});
            	// Update footer
            	$( api.column( index).footer() ).html(total);  
            }          
        }		
	 });
	 
	 datatable1.fnSort( [ [0,'asc'] ] );	 
	 
 	 
});
</script>

		
<div class="container">
<div class="screenlet">
	<div class="screenlet-title-bar">
      	<h3>Employe Wise Salary Analysis</h3>	
     </div>
    <div class="screenlet-body">
    	<div id="salaryAnalysisTable"/>
    </div>    
</div>    
</div>
