<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

<style type="text/css">

.myButton {
	border-radius:3px;
	border:2px solid #BAD0EE;
	display:inline-block;
	cursor:pointer;
	color:#ffffff;
	font-family:arial;
	font-size:12px;
	padding:5px 10px;
	text-decoration:none;
	width: 100px;
}
.myButton:hover {
	background-color:#BAD0EE;
}
</style>
<script type="text/javascript">
//<![CDATA[
	function checkFacilityAvalableOrNot(current){
		var domObj = $(current).parent().parent();
		var transferObj = $(domObj).find("#transferGroupId");
        var toFacilityObj = $(domObj).find("#toFacilityId");
    	var transId = $(transferObj).val();
		var toFacilityId = $(toFacilityObj).val();
		if(typeof(toFacilityId)!= "undefined" && toFacilityId!='' && toFacilityId != null ){
		$.ajax({
		     type: "POST",
		     url: 'checkFacilityAvalableOrNot',
		     data: {
		            toFacilityId : toFacilityId,
		            transId : transId,
		            },
		     dataType: 'json',
		     success: function(result) {
		       if(result["_ERROR_MESSAGE_"] || result["_ERROR_MESSAGE_LIST_"]){
				  alert(''+result['_ERROR_MESSAGE_']);
		    	   //populateError(result["_ERROR_MESSAGE_"]+result["_ERROR_MESSAGE_LIST_"]);
		    	   location.reload(true);
		       }else{              
		    	    
		       }
		     } 
		});
    }
	}
	
	function toggleTransferGroupId(master) {
        var transfers = jQuery("#listInXfer :checkbox[name='transferGroupIds']");
        jQuery.each(transfers, function() {
            this.checked = master.checked;
        });
    }
        
    function massTransferAcknowledgementSubmit(current, statusId){
    	//jQuery(current).attr( "disabled", "disabled");
    //	var transferGroup = jQuery("#listInXfer :checkbox[name='transferGroupIds']");
    	var transferGroup = $('input[name=transferGroupIds]:checked');
	 	if(transferGroup.size() <=0) {
	 		alert("Please Select at least One Request..!")
		 	return false;
	 	}
        var index = 0;
        jQuery.each(transferGroup, function() {
            if (jQuery(this).is(':checked')) {
            	var domObj = $(this).parent().parent();
            	var transferObj = $(domObj).find("#transferGroupId");
                var toFacilityObj = $(domObj).find("#toFacilityId");
				var transferQtyObj = $(domObj).find("#transferQty");
            	var transId = $(transferObj).val();
				var toFacilityId = $(toFacilityObj).val();
				var transferQty = $(transferQtyObj).val();
            	var appendStr = "";
            	appendStr += "<input type=hidden name=transferGroupId_o_"+index+" value="+transId+" />";
            	appendStr += "<input type=hidden name=statusId_o_"+index+" value="+statusId+" />";
            	if(typeof(toFacilityId)!= "undefined" && toFacilityId!='' && toFacilityId != null ){
            		appendStr += "<input type=hidden name=toFacilityId_o_"+index+" value="+toFacilityId+" />";
            	}
            	if(typeof(transferQty)!= "undefined" && transferQty!='' && transferQty != null ){
            		appendStr += "<input type=hidden name=transferQty_o_"+index+" value="+transferQty+" />";
            	}
   				$("#updateTransferGroupForm").append(appendStr);
            }
            index = index+1;
        });
       	jQuery('#updateTransferGroupForm').submit();
    }
    
        
//]]>
</script>

<form name="updateTransferGroupForm" id="updateTransferGroupForm" method="post" action="updateTransferGroupStatus">
</form>
<#if incommingTransfer?has_content>
  <div id="incomingXfer">

  </div>
  <form name="listInXfer" id="listInXfer"  method="post">
    <#if security.hasEntityPermission("DPT", "_ACCEPT_XFER", session)>
    <div align="right">
        <table>
            <tr>
                <td><input class="myButton" type="button" name="acceptXfer" id="acceptXfer" value="Accept" onclick="javascript:massTransferAcknowledgementSubmit(this, 'IXF_COMPLETE');"/></td>
                <td>&nbsp;</td>
                <td><input class="myButton" type="button" name="rejectXfer" id="rejectXfer" value="Reject" onclick="javascript:massTransferAcknowledgementSubmit(this, 'IXF_CANCELLED');"/></td>
            </tr>
        </table>
    </div>
    </#if>
	
    <table class="basic-table hover-bar" cellspacing="0">
      <thead>
        <tr class="header-row-2">
          <td>Transfer Id</td>
          <td>From Plant/Silo</td>
          <td>To Plant/Silo</td>
          <td>Product</td>
          <td>Status</td>
         <td>Quantity</td> 
         <#-- <td>QC Details</td> -->
          <td align='center'>${uiLabelMap.CommonSelect} <input type="checkbox" id="checkAllTransfers" name="checkAllTransfers" onchange="javascript:toggleTransferGroupId(this);"/></td>
        </tr>
      </thead>
      <tbody>
        <#assign alt_row = false>
        <#list incommingTransfer as eachXfer>
        	<#assign fromFacility = delegator.findOne("Facility", {"facilityId" : eachXfer.fromFacilityId}, false)> 
        	<#assign toFacility = delegator.findOne("Facility", {"facilityId" : eachXfer.toFacilityId}, false)>
        	<#assign product = delegator.findOne("Product", {"productId" : eachXfer.productId}, false)>
        	<#assign status = delegator.findOne("StatusItem", {"statusId" : eachXfer.statusId}, false)>   
        	<#assign facilityList = eachXfer.facilityList>
            <#assign toFacilityId = eachXfer.toFacilityId>
            <tr valign="middle"<#if alt_row> class="alternate-row"</#if>>
            	<input type="hidden" name="transferGroupId" id="transferGroupId" value="${eachXfer.transferGroupId?if_exists}">
	            <td>${(eachXfer.transferGroupId)?if_exists}</td>
              	<td>${fromFacility.facilityName?if_exists} [${eachXfer.fromFacilityId?if_exists}]</td>
              	<#if security.hasEntityPermission("PRO", "_EDIT_XFER_FAC", session) && facilityList?has_content>
				<td><select id="toFacilityId" name="toFacilityId" onchange="javascript:checkFacilityAvalableOrNot(this);">
					<#list facilityList as facility>
                      <#if toFacilityId?has_content  && toFacilityId == facility.facilityId >
			        	<option value='${facility.facilityId}' selected='selected'>${facility.facilityName?if_exists}</option> 	
			         <#else>
			           <option value='${facility.facilityId?if_exists}'>${facility.facilityName?if_exists}</option>
				      </#if>
                    </#list>
				</td>
                <#else>
              	<td>${toFacility.facilityName?if_exists} [${eachXfer.toFacilityId?if_exists}]</td>
                </#if>
              	<td>${(product.productName)?if_exists} [${eachXfer.productId}]</td>
              	<td>${status.description?if_exists}</td>
              	<#if security.hasEntityPermission("PRO", "_EDIT_XFER_QTY", session)>
				<td><input type="text" size="6" name="transferQty" id="transferQty" value="${eachXfer.xferQtySum?if_exists}"></td>
                <#else>
              	<td>${eachXfer.xferQtySum?if_exists}</td>
                </#if>
               <#--	<#if eachXfer.qcStatusId=="QC_NOT_ACCEPT"> 
              	<td><input type="button" name="QCDetails" onclick="javascript:showQcForm('${eachXfer.productId}','${eachXfer.toFacilityId}','CreateIncomingInvTransQcDetails', 'transferGroupId', '${eachXfer.transferGroupId}');" style="buttontext" value="QCDetails" /></td>
              	<#else> 
              	</#if> 
              	<td>&#160;</td> -->
              	<td align='center'><input type="checkbox" id="transferGroupIds" name="transferGroupIds" value="${eachXfer.transferGroupId}"/></td>
              	
            </tr>
            <#assign alt_row = !alt_row>
        </#list>
      </tbody>
    </table>
  </form>
<#else>
  <h3>No Transfers Found</h3>
</#if>
