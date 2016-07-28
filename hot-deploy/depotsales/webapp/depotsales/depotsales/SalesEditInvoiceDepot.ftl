<link type="text/css" href="<@ofbizContentUrl>/images/jquery/ui/css/ui-lightness/jquery-ui-1.8.13.custom.css</@ofbizContentUrl>" rel="Stylesheet" />	

<script type="text/javascript">
	
$(document).ready(function(){
		

		$( "#effectiveDate" ).datepicker({
			dateFormat:'d MM, yy',
			changeMonth: true,
			numberOfMonths: 1,
			onSelect: function( selectedDate ) {
				$( "#effectiveDate" ).datepicker("option", selectedDate);
			}
		});
		$( "#suppInvoiceDate" ).datepicker({
			dateFormat:'d MM, yy',
			changeMonth: true,
			numberOfMonths: 1,
			onSelect: function( selectedDate ) {
				$( "#SInvoiceDate" ).datepicker(selectedDate);
			}
		});
		$('#ui-datepicker-div').css('clip', 'auto');
			$("#partyId").autocomplete({ source: partyAutoJson }).keydown(function(e){
			if (e.keyCode === 13){
		      	 $('#partyId').autocomplete('close');
	    			$('#purchaseEntryInit').submit();
	    			return false;   
			}
		});
		//prepareApplicableOptions();
    	//setupGrid2();
	});
</script>
<#assign changeRowTitle = "Changes">                

<#include "SalesEditInvoiceDepotInc.ftl"/>

<div class="full">
	
		<div class="screenlet-title-bar">
	         <div class="grid-header" style="width:100%">
				<label>Sales Invoice Entry </label>
			</div>
	     </div>
	      
	    <div class="screenlet-body">
	    <form method="post" name="purchaseEntryInit" action="<@ofbizUrl>MaterialInvoiceInit</@ofbizUrl>" id="purchaseEntryInit" class="form-style-8">  
	      <table width="60%"  border="0" cellspacing="0" cellpadding="0">  
	        <tr>
	          <input type="hidden" name="isFormSubmitted"  value="YES" />
	          
	          
	          <input type="hidden" id="invoiceId" name="invoiceId"  value="${invoiceId}" />
	          
	          
	          <td align='left' valign='middle' nowrap="nowrap"><div class='h3'>Invoice Date :</div></td>
	          <#if invoDate?exists && invoDate?has_content>  
		  	  	<input type="hidden" name="effectiveDate" id="effectiveDate" value="${invoDate}"/>  
	          	<td valign='middle'>
	            	<div class='tabletext h3'>${invoDate}         
	            	</div>
	          	</td>       
	       	  <#else> 
	          	  	<td valign='middle'>          
	            		<input class='h3' type="text" name="effectiveDate" id="effectiveDate" value="${defaultEffectiveDate}"/>           		
	            	</td>
	       	  </#if>
	        </tr>
	        <tr>
	          <td align='left' valign='middle' nowrap="nowrap"><div class='h3'>Shipment Date:</div></td>
				<#if shipmentDate?exists && shipmentDate?has_content>  
		  	  		<input type="hidden" name="estimatedShipDate" id="estimatedShipDate" value="${shipmentDate?if_exists}"/>  
	          		<td valign='middle'>
	            		<div class='tabletext h3'>
	            			${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(shipmentDate, "dd MMMM, yyyy")?if_exists}
	            		</div>
	          		</td>       
	          	</#if>
	        </tr> 
	        <tr><td><br/></td></tr>
	        <tr>
	          <td align='left' valign='middle' nowrap="nowrap"><div class='h3'>Supplier:</div></td>
				<#if partyId?exists && partyId?has_content>  
		  	  		  <#--  <input type="hidden" name="partyId" id="partyId" value="${partyId?if_exists}"/>  -->
	          		<td valign='middle'>
	            		<div class='tabletext h3'>
	            			<#assign supplierName = delegator.findOne("PartyNameView", {"partyId" : partyId}, true) />
	               			${partyId?if_exists} [ ${supplierName.groupName?if_exists} ${supplierName.firstName?if_exists} ${supplierName.lastName?if_exists}]             
	            		</div>
	          		</td>       
	          	</#if>
	        </tr>
	         <#-- Showing BillToParty: -->
	          <td align='left' valign='middle' nowrap="nowrap"><div class='h3'>BillToParty:</div></td>
				<#if billToPartyId?exists && billToPartyId?has_content>  
		  	  	  <#--	<input type="hidden" name="partyId" id="partyId" value="${billToPartyId?if_exists}"/>  -->
	          		<td valign='middle'>
	            		<div class='tabletext h3'>
	            			<#assign supplierName = delegator.findOne("PartyNameView", {"partyId" : billToPartyId}, true) />
	               			${billToPartyId?if_exists} [ ${supplierName.groupName?if_exists} ${supplierName.firstName?if_exists} ${supplierName.lastName?if_exists}]             
	            		</div>
	          		</td>       
	          	</#if>
	        </tr>
	        <tr><td><br/></td></tr>
	        <tr>
	          <input type="hidden" name="isFormSubmitted"  value="YES" />
	          <td align='left' valign='middle' nowrap="nowrap"><div class='h3'>Tally Reference No :</div></td>
            <#-->	<#if tallyRefNo?exists && tallyRefNo?has_content>  
		  	  	<input type="hidden" name="tallyrefNo" id="tallyrefNo" value="${tallyRefNo?if_exists}"/>  
	          	<td valign='middle'>
	            	<div class='tabletext h3'>${tallyRefNo?if_exists}         
	            	</div>
	          	</td>       
	       	  <#else> 
	          	  	<td valign='middle'>          
	            		<input class='h3' type="text" name="tallyrefNo" id="tallyrefNo" value="${tallyRefNo}"/>           		
	            	</td>
	       	  </#if> -->
	       	  <#if tallyRefNo?exists && tallyRefNo?has_content>  
	       	  <td valign='middle'>          
	            		<input class='h3' type="text" name="tallyrefNo" id="tallyrefNo" value="${tallyRefNo}"/>           		
	            	</td>
	            	<#else>
	            	<td valign='middle'>          
	            		<input class='h3' type="text" name="tallyrefNo" id="tallyrefNo" />           		
	            	</td>
	            	</#if>
	       	  
	        </tr>
	        <tr><td><br/></td></tr>
	       	<tr>
	            <td align='left' valign='middle' nowrap="nowrap"><div class='h3'>invoiceId</div></td>
				<#if invoiceId?exists && invoiceId?has_content>  
		  	  		<input type="hidden" name="invoiceId" id="invoiceId" value="${invoiceId?if_exists}"/>  
	          		<td valign='middle'>
	            		<div class='tabletext h3'>${invoiceId?if_exists}</div> 
	          		</td>       
	          	</#if>
	        </tr> 
	       	<tr>
	            <td align='left' valign='middle' nowrap="nowrap"><div class='h3'>Sales Order No:</div></td>
				<#if orderId?exists && orderId?has_content>  
		  	  		<input type="hidden" name="orderId" id="orderId" value="${orderId?if_exists}"/>  
	          		<td valign='middle'>
	            		<div class='tabletext h3'>${orderId?if_exists}</div> 
	          		</td>       
	          	</#if>
	        </tr>
	                  
	      </table>
	      <div id="sOFieldsDiv" >
	      </div> 
	</form>
	<br/>
    <form method="post" id="indententry" action="<@ofbizUrl>purchaseEntryInit</@ofbizUrl>">  
	<#-- passing BillToPartyId: -->
       	<#if billToPartyId?exists>
				<input type="hidden" name="partyId" id="partyId" value="${billToPartyId?if_exists}"/>
		 <#else> 
		 		<input type="hidden" name="partyId" id="partyId" value="${partyId?if_exists}"/>
		 </#if>
		<input type="hidden" name="shipmentId" id="billToPartyId" value="${parameters.shipmentId?if_exists}"/>
		<input type="hidden" name="vehicleId" id="vehicleId" value="${vehicleId?if_exists}"/>
		<input type="hidden" name="orderId" id="orderId" value="${orderId?if_exists}"/>
		<input type="hidden" name="isDisableAcctg" id="isDisableAcctg" value="N"/>
		<br>
	</form>
	</div>

<div class="full">
	<div class="screenlet">
	    <div class="screenlet-title-bar">
	 		<div class="grid-header" style="width:100%">
				<label>Sales Items</label><span id="totalAmount"></span>
			</div>
			 <div class="screenlet-body" id="FieldsDIV" >
				<div id="myGrid1" style="width:100%;height:200px;">
					<div class="grid-header" style="width:100%">
					</div>
				</div>
				<div class="lefthalf" >
				<div class="screenlet-title-bar">
					<div class="grid-header" style="width:100%">
						<label>Discounts</label><span id="totalAmount"></span>
					</div>
					<div id="myGrid3" style="width:100%;height:150px;">
						<div class="grid-header" style="width:100%">
						</div>
					</div>
				</div>
				</div>
				<div class="righthalf">
				<div class="screenlet-title-bar">
					<div class="grid-header" style="width:100%">
						<label>Additional Charges</label><span id="totalAmount"></span>
					</div>
					<div id="myGrid2" style="width:100%;height:150px;">
						<div class="grid-header" style="width:100%">
						</div>
					</div>
				</div>
				</div>
				<#assign formAction ='processEditSalesInvoice'>	
				<#if invoiceId?exists>
			    	<div align="center">
			    		<h3>
			    		<input type="submit" style="padding:.4em" id="changeSave" value="Submit" onclick="javascript:processIndentEntry('indententry','<@ofbizUrl>${formAction}</@ofbizUrl>');"/>
			    		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			    		<input type="submit" style="padding:.4em" id="changeCancel" value="Cancel" onclick="javascript:processIndentEntry('indententry','<@ofbizUrl>FindGRNShipmentsDepot</@ofbizUrl>');"/>
			    		</h3>   	
			    	</div>     
				</#if>  	
			</div>
			</br>
		</div>
	</div>     
</div>
 	

</div>
 