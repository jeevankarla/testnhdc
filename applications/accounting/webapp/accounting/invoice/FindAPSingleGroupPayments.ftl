<script type="application/javascript">
function makeDatePicker(paymentDate){
	$( "#"+paymentDate ).datepicker({
			dateFormat:'dd MM, yy',
			changeMonth: true,
			numberOfMonths: 1,
			onSelect: function( selectedDate ) {
				$( "#"+paymentDate ).datepicker();
			}
		});
}
		
$(document).ready(function(){
	makeDatePicker("paymentDate");
	$('#ui-datepicker-div').css('clip', 'auto');
});
	
</script>	
<form method="post" name="findAPSingleGroupPayment" action="<@ofbizUrl>findAPSingleGroupPayment</@ofbizUrl>">  
      <input type="hidden" name="paymentGroupTypeId" id="paymentGroupTypeId" value="SINGLE_PAYMENT"/>
      <input type="hidden" name="statusId" id="statusId" value="PAYGRP_CREATED"/>
      <input type="hidden" name="isFormSubmitted" id="isFormSubmitted" value="Y"/>
      
      <table width="60%" border="0" cellspacing="0" cellpadding="0">     
        <tr>
          <td>&nbsp;</td>
          <td align='left' valign='middle' nowrap="nowrap"><div class='h3'>PaymentDate:</div></td>
          <td>&nbsp;</td>
          <td valign='middle'>          
             <input class='h2' type="text" size="17" name="paymentDate" id="paymentDate" readonly/>          
          </td>
        </tr>
        <tr>
          <td>&nbsp;</td>
          <td align='left' valign='middle' nowrap="nowrap"><div class='h3'>Issuing Authority:</div></td>
          <td>&nbsp;</td>
          <td valign='middle'>          
             <select name="finAccountId" id="finAccountId">
	             <option value=""></option>   			     			
	                <#list finAccountList as finAccount>    
	      					<option  value="${finAccount.finAccountId}" >${finAccount.finAccountName}</option>
	      			</#list>            
			</select>            
          </td>
        </tr>
        <tr>
          <td>&nbsp;</td>
          <td align='left' valign='middle' nowrap="nowrap"><div class='h3'>To Party:</div></td>
          <td>&nbsp;</td>
         <td><@htmlTemplate.lookupField size="10" maxlength="22" formName="findAPSingleGroupPayment" size="12"  name="partyIdTo" id="partyIdTo" fieldFormName="LookupPartyName"/></td>
        </tr>      
        
        <tr>
          <td>&nbsp;</td>
          <td align='left' valign='middle' nowrap="nowrap"><div class='h3'>Role Type:</div></td>
          <td>&nbsp;</td>
          <td valign='middle'>          
             <select name="roleTypeId" id="roleTypeId">
	             <option value=""></option>   			     			
	                <#list roleTypeAttrList as roleTypeEntry>    
	      					<option  value="${roleTypeEntry.roleTypeId}" >${roleTypeEntry.description}</option>
	      			</#list>            
			</select>            
          </td>
        </tr> 
         <tr><td><br/></td></tr>
        <tr>
        <td>&nbsp;</td>
          <td align='left' valign='middle' nowrap="nowrap"><div class='h3'>	</div></td>
           <td>&nbsp;</td>
           <td valign='middle'>
             <input type="submit" style="padding:.3em" name="submit" value="Find"/>
           </td>
         </tr>        
      </table>
</form>