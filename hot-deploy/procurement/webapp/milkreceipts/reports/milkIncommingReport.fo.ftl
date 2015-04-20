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
	<fo:simple-page-master master-name="main" page-height="12in" page-width="10in"
            margin-top="0in" margin-bottom=".7in" margin-left=".5in" margin-right=".5in">
        <fo:region-body margin-top="0.2in"/>
        <fo:region-before extent="1.5in"/>
        <fo:region-after extent="1.5in"/>        
    </fo:simple-page-master>   
</fo:layout-master-set>
${setRequestAttribute("OUTPUT_FILENAME", "arcOrder.pdf")}
 <#if milkTransferMap?has_content> 

<fo:page-sequence master-reference="main" force-page-count="no-force" font-family="Courier,monospace">	
		<#assign pageNumber = 0>				
		<fo:static-content flow-name="xsl-region-before">
		       	 
            </fo:static-content>		
            <fo:flow flow-name="xsl-region-body"   font-family="Courier,monospace">	
		        <fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false">&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;         Date: ${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(nowTimestamp, "dd/MM/yy HH:mm:ss")}</fo:block>
		        <fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="12pt" > &#160;&#160;  </fo:block>
				<fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="13pt" font-weight="bold" >${uiLabelMap.KMFDairyHeader}</fo:block>
				<fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="14pt" font-weight="bold" >${uiLabelMap.KMFDairySubHeader}</fo:block>
			    <fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="5pt" > ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------</fo:block>
			    <fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="12pt" > &#160;&#160;  </fo:block>
			    <fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="12pt" >&#160;&#160; </fo:block>
             <fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="13pt" font-weight="bold">ACKNOWLEDGEMENT </fo:block>
             <fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="12pt" >&#160;&#160; </fo:block>
 
			    
			    <fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="3pt" >&#160; </fo:block> 
                <fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="12pt" font-weight="bold" >ADDRESS:      </fo:block>
              	<fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="12pt" ><#if milkTransferMap.get("partyId")?has_content>Id: ${milkTransferMap.get("partyId")}, <#else> </#if><#if milkTransferMap.get("partyName")?has_content>${milkTransferMap.get("partyName")} <#else> </#if>        </fo:block>
              	<fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="12pt" ><#if milkTransferMap.get("address1")?has_content>${milkTransferMap.get("address1")}   <#else> </#if>     </fo:block>
                <fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="12pt" ><#if milkTransferMap.get("address2")?has_content>${milkTransferMap.get("address2")?if_exists} <#else> </#if>     </fo:block>
                <fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="12pt" ><#if milkTransferMap.get("city")?has_content>${milkTransferMap.get("city")?if_exists}-${milkTransferMap.get("postalCode")?if_exists}. <#else> </#if>                          </fo:block>
                
              	<fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="12pt" >    Ph No: ${milkTransferMap.get("phoneNumber")?if_exists}         </fo:block>
                <fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="12pt" >    Mail : ${milkTransferMap.get("emailAddress")?if_exists}        </fo:block>
                            <fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="12pt" >&#160;&#160; </fo:block>
               
             
              <fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="12pt">DC NO :         ${dcNo?if_exists}</fo:block>
              <fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="12pt">TANKER NO :     ${milkTransferMap.get("containerId")?if_exists} </fo:block>
              <fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="12pt">DISPATCH DATE : ${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(milkTransferMap.get("sendDate"), "dd MMM yyyy")}                                   DISPATCH TIME : ${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(milkTransferMap.get("sendDate"), "HH:mm")}</fo:block>
              <fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="12pt">RECEIVED DATE : ${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(milkTransferMap.get("receiveDate"), "dd MMM yyyy")}                                   RECEIVED TIME : ${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(milkTransferMap.get("receiveDate"), "HH:mm")}     </fo:block>
              <fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="12pt">DRIVER NAME :   ${milkTransferMap.get("receivedQuantity")?if_exists}</fo:block>
     	  
     	    <fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="12pt" >&#160;&#160; </fo:block>
      	    <fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="5pt" > -------- -------- ------- -------- -------- -------- ------- ------- -------- ------- ------- -------- ------- ------- ------- ------- ------- ------- ------- ------ ------ ------ ------ ------ ------ ------ ------ --------</fo:block>
            <fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="10pt" font-weight="bold">&#160;&#160;                         MILK           QUANTITY (KG)       TEMP     ACIDITY        CLR       FAT          SNF </fo:block>
            <fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="5pt" > -------- -------- ------- -------- -------- -------- ------- ------- -------- ------- ------- -------- ------- ------- ------- ------- ------- ------- ------- ------ ------ ------ ------ ------ ------ ------ ------ --------</fo:block>
       <fo:block >
		 <fo:table width="100%" align="right" table-layout="fixed"  font-size="12pt">
           <fo:table-column column-width="120pt"/>               
            <fo:table-column column-width="100pt"/>               
            <fo:table-column column-width="100pt"/>
            <fo:table-column column-width="70pt"/>
            <fo:table-column column-width="70pt"/>
            <fo:table-column column-width="70pt"/>
            <fo:table-column column-width="70pt"/>
            <fo:table-column column-width="70pt"/>
           	<fo:table-body>
             <fo:table-row>
              <fo:table-cell ><fo:block text-align="left"  font-weight="bold" keep-together="always" font-size="12pt" >DISPATCH</fo:block></fo:table-cell>       		
             <fo:table-cell  ><fo:block text-align="center"  font-weight="bold" keep-together="always" font-size="12pt">${milkTransferMap.get("milkType")?if_exists}</fo:block></fo:table-cell>       		
              <fo:table-cell ><fo:block text-align="right"  font-weight="bold" keep-together="always" font-size="12pt">${milkTransferMap.get("sendQtyKgs")?if_exists}</fo:block></fo:table-cell>       		
              <fo:table-cell ><fo:block text-align="right"  font-weight="bold" keep-together="always" font-size="12pt">${milkTransferMap.get("sendTemparature")?if_exists}</fo:block></fo:table-cell>       		
              <fo:table-cell ><fo:block text-align="right"  font-weight="bold" keep-together="always" font-size="12pt">${milkTransferMap.get("sendAcidity")?if_exists}</fo:block></fo:table-cell>       		
              <fo:table-cell ><fo:block text-align="right"  font-weight="bold" keep-together="always" font-size="12pt">${milkTransferMap.get("sendLR")?if_exists}</fo:block></fo:table-cell>       		
              <fo:table-cell ><fo:block text-align="right"  font-weight="bold" keep-together="always" font-size="12pt">${milkTransferMap.get("fat")?if_exists}</fo:block></fo:table-cell>       		
              <fo:table-cell ><fo:block text-align="right"  font-weight="bold" keep-together="always" font-size="12pt">${milkTransferMap.get("snf")?if_exists}</fo:block></fo:table-cell>       		
             </fo:table-row>
            <fo:table-row>
              <fo:table-cell ><fo:block text-align="left"  font-weight="bold" keep-together="always" font-size="12pt" >ACKNOWLEDGED</fo:block></fo:table-cell>       		
             <fo:table-cell  ><fo:block text-align="center"  font-weight="bold" keep-together="always" font-size="12pt"></fo:block></fo:table-cell>       		
              <fo:table-cell ><fo:block text-align="right"  font-weight="bold" keep-together="always" font-size="12pt">${milkTransferMap.get("receivedQuantity")?if_exists}</fo:block></fo:table-cell>       		
              <fo:table-cell ><fo:block text-align="right"  font-weight="bold" keep-together="always" font-size="12pt">${milkTransferMap.get("receivedTemparature")?if_exists}</fo:block></fo:table-cell>       		
              <fo:table-cell ><fo:block text-align="right"  font-weight="bold" keep-together="always" font-size="12pt">${milkTransferMap.get("receivedAcidity")?if_exists}</fo:block></fo:table-cell>       		
              <fo:table-cell ><fo:block text-align="right"  font-weight="bold" keep-together="always" font-size="12pt">${milkTransferMap.get("receivedLR")?if_exists}</fo:block></fo:table-cell>       		
              <fo:table-cell ><fo:block text-align="right"  font-weight="bold" keep-together="always" font-size="12pt">${milkTransferMap.get("receivedFat")?if_exists}</fo:block></fo:table-cell>       		
              <fo:table-cell ><fo:block text-align="right"  font-weight="bold" keep-together="always" font-size="12pt">${milkTransferMap.get("receivedSnf")?if_exists}</fo:block></fo:table-cell>       		
             </fo:table-row>
    	</fo:table-body>
    		</fo:table>
     </fo:block>	
        <fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="5pt" > -------- -------- ------- -------- -------- -------- ------- ------- -------- ------- ------- -------- ------- ------- ------- ------- ------- ------- ------- ------ ------ ------ ------ ------ ------ ------ ------ --------</fo:block>
     
        <fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="12pt" >&#160;&#160; </fo:block>
     
       <fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-size="12pt">&#160;GROSS WT: ${milkTransferMap.get("grossWeight")?if_exists}                  TARE WT: ${milkTransferMap.get("tareWeight")?if_exists}                  NET WT: ${milkTransferMap.get("grossWeight")-milkTransferMap.get("tareWeight")?if_exists}</fo:block>
        <fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="12pt" >&#160;&#160; </fo:block>
        <fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="12pt" >&#160;&#160; </fo:block>
        <fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="12pt" >&#160;&#160; </fo:block>
       <fo:block  text-align="right"  font-weight="bold" keep-together="always" font-size="12pt" >SHIFT INCHARGE   &#160;</fo:block>       		
 
			 </fo:flow>
			 </fo:page-sequence>
			 
			 <#else>
				<fo:page-sequence master-reference="main">
    			<fo:flow flow-name="xsl-region-body" font-family="Helvetica">
       		 		<fo:block font-size="14pt">
            			NO RECORDS FOUND
       		 		</fo:block>
    			</fo:flow>
			</fo:page-sequence>
			</#if>  
</fo:root>
</#escape>

