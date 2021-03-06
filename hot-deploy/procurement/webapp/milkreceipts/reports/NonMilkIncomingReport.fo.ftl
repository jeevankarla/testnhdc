
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
specific language governing permissions and limitationsborder-style="solid"border-style="solid"
under the License.
-->

<#escape x as x?xml>
<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">

<#-- do not display columns associated with values specified in the request, ie constraint values -->
<fo:layout-master-set>
	<fo:simple-page-master master-name="main" page-height="12in" page-width="8in"
            margin-top="0in" margin-bottom=".7in" margin-left=".5in" margin-right=".5in">
        <fo:region-body margin-top="0.2in"/>
        <fo:region-before extent="1.5in"/>
        <fo:region-after extent="1.5in"/>        
    </fo:simple-page-master>   
</fo:layout-master-set>
 <#if weighmentAndItemDetailsList?has_content> 

<fo:page-sequence master-reference="main" force-page-count="no-force" font-family="Courier,monospace">	
		<#assign pageNumber = 0>				
		<fo:static-content flow-name="xsl-region-before">
		       	 
            </fo:static-content>		
            <fo:flow flow-name="xsl-region-body"   font-family="Courier,monospace">	
		        <fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="10pt">&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;         Date: ${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(nowTimestamp, "dd/MM/yy HH:mm:ss")}</fo:block>
		        <fo:block keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="10pt">&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;         UserLogin:<#if userLogin?exists>${userLogin.userLoginId?if_exists}</#if></fo:block> 
		        <fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="10pt" > &#160;&#160;  </fo:block>
				<fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="11pt" font-weight="bold" >${uiLabelMap.KMFDairyHeader}</fo:block>
				<fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="11pt" font-weight="bold" >${uiLabelMap.KMFDairySubHeader}</fo:block>
			    <fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="5pt" > ----------------------------------------------------------------------------------------------------------------------------------------------------------------</fo:block>
			    <#if partyIdTo?has_content && partyIdTo=="MD">
             	<fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="11pt" font-weight="bold">ACKNOWLEDGEMENT </fo:block>
             	<#else>
				<fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="11pt" font-weight="bold">DISPATCH REPORT </fo:block>
                </#if>
			    
			    <fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="3pt" >&#160; </fo:block> 
			   
	             <#if partyIdTo?has_content && partyIdTo=="MD">
                <fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="10pt" font-weight="bold" >DISPATCH FROM:      </fo:block>
                <#else>
				 <fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="10pt" font-weight="bold" >DISPATCH TO:    </fo:block>
                </#if>
                <#if weighmentPartyDetails?has_content>
                    <#assign index=1>    
                	 <#list weighmentPartyDetails as partyDetailsMap>
              			<fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="10pt" >&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;${index?if_exists} .  <#if partyDetailsMap.get("partyName")?has_content>${partyDetailsMap.get("partyName")} <#else> </#if><#if partyDetailsMap.get("partyId")?has_content>[${partyDetailsMap.get("partyId")}] <#else> </#if>        </fo:block>
              			<fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="10pt" ><#if partyDetailsMap.get("address1")?has_content>&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;     ${partyDetailsMap.get("address1")}   <#else> </#if>     </fo:block>
                		<fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="10pt" ><#if partyDetailsMap.get("address2")?has_content>&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;     ${partyDetailsMap.get("address2")?if_exists} <#else> </#if>     </fo:block>
                		<fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="10pt" ><#if partyDetailsMap.get("city")?has_content>&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;     ${partyDetailsMap.get("city")?if_exists}-${partyDetailsMap.get("postalCode")?if_exists}. <#else> </#if>                          </fo:block>
              			<fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="10pt" >&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;     ${partyDetailsMap.get("phoneNumber")?if_exists}         </fo:block>
                		<fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="10pt" >&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;     ${partyDetailsMap.get("emailAddress")?if_exists}        </fo:block>
                		<fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="10pt" >&#160;&#160; </fo:block>
                		<#assign index=index+1>
					</#list>
               </#if>
             
              <fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="10pt">DC NO :         ${dcNo?if_exists}</fo:block>
              <fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="10pt">TANKER NO :     ${vehicleId?if_exists} </fo:block>
              <#if partyIdTo?has_content && partyIdTo=="MD">
              <fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="10pt">DISPATCH DATE : ${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(sendDate, "dd MMM yyyy")}                                   DISPATCH TIME : ${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(sendDate, "HH:mm")}</fo:block>
              <fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="10pt">RECEIVED DATE : ${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(receiveDate, "dd MMM yyyy")}                                   RECEIVED TIME : ${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(receiveDate, "HH:mm")}     </fo:block>
              <#else>
     	  		<fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="10pt">DISPATCH DATE : ${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(receiveDate, "dd MMM yyyy")}                                   DISPATCH TIME : ${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(receiveDate, "HH:mm")}     </fo:block>
              </#if>  
      	    <fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="5pt" > -------- -------- ------- -------- -------- -------- ------- ------- -------- ------- ------- -------- ------- ------- ------- ------- ------- ------- ------- ------ ------ ------</fo:block>
            <fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="10pt" font-weight="bold">S.NO       PRODUCT     					DISPATCH WEIGHT   GROSS WEIGHT		TARE WEIGHT	   QUANTITY	</fo:block>
            <fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="5pt" > -------- -------- ------- -------- -------- -------- ------- ------- -------- ------- ------- -------- ------- ------- ------- ------- ------- ------- ------- ------ ------ ------</fo:block>
       <fo:block >
		 <fo:table width="100%" align="right" table-layout="fixed"  font-size="10pt">
           	<fo:table-column column-width="30pt"/>               
            <fo:table-column column-width="150pt"/>               
            <fo:table-column column-width="100pt"/>
            <fo:table-column column-width="90pt"/>
            <fo:table-column column-width="90pt"/>
            <fo:table-column column-width="100pt"/>
           	<fo:table-body>
           	<#assign totGrsWeight = 0>
            <#assign totTareWeight = 0>
           	<#assign sno=1>
           	<#if weighmentAndItemDetailsList?has_content>
                <#list weighmentAndItemDetailsList as weighmentDetails>
            	<fo:table-row>
            		<fo:table-cell>
            			<fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="10pt" >${sno?if_exists}</fo:block>
            		</fo:table-cell>
            		<#if weighmentDetails.get("productId")?has_content>
            			<#assign productId = weighmentDetails.get("productId")>
                  		<#assign product = delegator.findOne("Product",{"productId":productId},false)>
                    <fo:table-cell>
            			<fo:block   text-align="left" font-family="Courier,monospace"  font-size="10pt" >${product.getString("productName")?if_exists}</fo:block>
            		</fo:table-cell>
            		<#else>
					<fo:table-cell>
            			<fo:block   text-align="left" font-family="Courier,monospace"  font-size="10pt" ></fo:block>
            		</fo:table-cell>
                    </#if>
					<fo:table-cell>
            			<fo:block   text-align="left" font-family="Courier,monospace"  font-size="10pt" >${weighmentDetails.get("dispatchWeight")?if_exists}</fo:block>
            		</fo:table-cell>
            		<#if weighmentDetails.get("grossWeight")?has_content>
                    	<#assign grossWeight = weighmentDetails.get("grossWeight")>
                         <#if partyIdTo?has_content && partyIdTo=="MD">
                         	<#if sno == 1>
                          		<#assign totGrsWeight = grossWeight>
                         	</#if>
                         <#else>
                            <#if sno == listSize>
                         		<#assign totGrsWeight = grossWeight>
                        	</#if>
                         </#if>
                        <fo:table-cell>
            				<fo:block   text-align="left" font-family="Courier,monospace"  font-size="10pt" >${grossWeight?if_exists}</fo:block>
            			</fo:table-cell>
                    <#else>
						<fo:table-cell>
            				<fo:block   text-align="left" font-family="Courier,monospace"  font-size="10pt" ></fo:block>
            			</fo:table-cell>
                    </#if> 
            		
                    <#if weighmentDetails.get("tareWeight")?has_content>
                    	<#assign tareWeight = weighmentDetails.get("tareWeight")>
 						<#if partyIdTo?has_content && partyIdTo=="MD">
							<#if sno == listSize>
                         		<#assign totTareWeight = tareWeight>
                        	</#if>
                        <#else>
                            <#if sno == 1>
                          		<#assign totTareWeight = tareWeight>
                         </#if>
                        </#if>
						<fo:table-cell>
	            			<fo:block   text-align="left" font-family="Courier,monospace"  font-size="10pt" >${tareWeight?if_exists}</fo:block>
            			</fo:table-cell>
                     <#else>
                         <fo:table-cell>
            				<fo:block   text-align="left" font-family="Courier,monospace"  font-size="10pt" ></fo:block>
            			</fo:table-cell>
                    </#if> 
					
            		<#assign qty = grossWeight - tareWeight>
            		<fo:table-cell>
            			<fo:block   text-align="left" font-family="Courier,monospace"  font-size="10pt" >${qty?if_exists}</fo:block>
            		</fo:table-cell>
            	</fo:table-row>
            	<#assign sno=sno+1>	
           		</#list>
            </#if>
    		</fo:table-body>
    	</fo:table>
     </fo:block>	
        <fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="5pt" > -------- -------- ------- -------- -------- -------- ------- ------- -------- ------- ------- -------- ------- ------- ------- ------- ------- ------- ------- ------ ------ ------ ---</fo:block>
     
        <fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="10pt" >&#160;&#160; </fo:block>
       <fo:block  keep-together="always" font-weight="bold" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="10pt">&#160;GROSS WT:  ${totGrsWeight?if_exists}      TARE WT: ${totTareWeight?if_exists}       NET WT: ${(totGrsWeight-totTareWeight)?if_exists}</fo:block>
        <fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="10pt" >&#160;&#160; </fo:block>
        <fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="10pt" >&#160;&#160; </fo:block>
        <fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="10pt" >&#160;&#160; </fo:block>
       <fo:block  text-align="left"  font-weight="bold" keep-together="always" font-size="10pt" >&#160;&#160;&#160;SECURITY &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;   &#160;&#160;&#160;&#160;&#160; SHIFT INCHARGE   </fo:block>       		
       <fo:block  text-align="right"  font-weight="bold" keep-together="always" font-size="10pt" > &#160;</fo:block>       		
       <fo:block  text-align="right"  font-weight="bold" keep-together="always" font-size="10pt" >(Weighbridge) &#160;</fo:block>       		
			 </fo:flow>
			 </fo:page-sequence>
			 
			 <#else>
				<fo:page-sequence master-reference="main">
    			<fo:flow flow-name="xsl-region-body" font-family="Helvetica">
       		 		<fo:block font-size="14pt">
       	<#-->	 	<#if partyId?has_content> <#if dcNo?has_content> No Records Found....!</#if><#else> ${errorMessage} </#if>-->
       		 	Please First Enter The Details...!   			   
       		 		</fo:block>
    			</fo:flow>
			</fo:page-sequence>
			</#if>  
</fo:root>
</#escape>

