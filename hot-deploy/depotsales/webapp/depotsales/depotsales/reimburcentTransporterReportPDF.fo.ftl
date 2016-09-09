
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


<#escape x as x?xml>
	<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
        <fo:layout-master-set>
            <fo:simple-page-master master-name="main" page-height="12in" page-width="10in"  margin-left=".3in" margin-right=".3in" margin-top=".1in">
                <fo:region-body margin-top=".1"/>
                <fo:region-before extent="1in"/>
                <fo:region-after extent="1.5in"/>
            </fo:simple-page-master>
        </fo:layout-master-set>
        ${setRequestAttribute("OUTPUT_FILENAME", "IndentReport.pdf")}
        <#if finalMap?has_content>
        
         <#assign  PartyList = finalMap.keySet()>
         <#list PartyList as eachParty>
        
        <fo:page-sequence master-reference="main" font-size="10pt">	
        	<fo:static-content flow-name="xsl-region-before" font-family="Courier,monospace">
        	</fo:static-content>
        	<fo:flow flow-name="xsl-region-body"   font-family="Courier,monospace">	
				<#assign reportHeader = delegator.findOne("TenantConfiguration", {"propertyTypeEnumId" : "COMPANY_HEADER","propertyName" : "reportHeaderLable"}, true)>
                <#assign reportSubHeader = delegator.findOne("TenantConfiguration", {"propertyTypeEnumId" : "COMPANY_HEADER","propertyName" : "reportSubHeaderLable"}, true)>
				<fo:block  keep-together="always" text-align="center"  font-weight="bold"   font-size="12pt" white-space-collapse="false">${reportHeader.description?if_exists} </fo:block>
				<fo:block  text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="12pt" font-weight="bold">${BOAddress?if_exists}</fo:block>
				<fo:block  keep-together="always" text-align="center"  font-weight="bold"  font-size="10pt" white-space-collapse="false"> ${eachParty?if_exists} </fo:block>
        		<fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="5pt">--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------</fo:block>
            
                 
                <#-->  <fo:block>     
    <fo:table width="100%"   align="right" table-layout="fixed"  font-size="10pt"> 
	<fo:table-column column-width="50%"/>
	<fo:table-column column-width="50%"/>

     

		<fo:table-body>
			<fo:table-row white-space-collapse="false">
				<fo:table-cell >
				<fo:block text-align="left"  font-weight="bold"  font-size="10pt" >${Static["org.ofbiz.party.party.PartyHelper"].getPartyName(delegator, soceity, true)}</fo:block>
				<#list finalAddresList as eachDetail>
				<fo:block text-align="left"    font-size="10pt" >${eachDetail.key2?if_exists}</fo:block>
				</#list>
				<fo:block text-align="left" font-weight="bold" font-size="10pt" >PassBook No : ${passNo?if_exists}</fo:block>
				</fo:table-cell>
				<fo:table-cell >
				<fo:block text-align="right"    font-size="10pt" keep-together="always" white-space-collapse="false">&#160;&#160;&#160;&#160;NHDC BILL NO   :${invoiceId?if_exists}</fo:block>
				<fo:block text-align="right"    font-size="10pt" keep-together="always" white-space-collapse="false">&#160;&#160;&#160;&#160;NHDC Indent No :${indentNo?if_exists}</fo:block>
				<fo:block text-align="right"    font-size="10pt" keep-together="always" white-space-collapse="false">&#160;&#160;&#160;&#160;NHDC PO No     :${poNumber?if_exists}</fo:block>
				<fo:block text-align="right"    font-size="10pt" keep-together="always" white-space-collapse="false">&#160;&#160;&#160;&#160;User Agency Indent No/Date  :${externalOrderId?if_exists}</fo:block>
				</fo:table-cell>
				<fo:table-cell >
				<fo:block text-align="right"     font-size="10pt" >DATE :<#if invoiceDate?has_content>${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(invoiceDate, "dd-MMM-yyyy")}</#if></fo:block>
				<fo:block text-align="right"     font-size="10pt" >DATE :<#if indentDate?has_content>${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(indentDate, "dd-MMM-yyyy")}</#if></fo:block>
				<fo:block text-align="right"     font-size="10pt" >DATE :<#if poDate?has_content>${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(poDate, "dd-MMM-yyyy")}</#if></fo:block>
				<fo:block text-align="right"     font-size="10pt" >Tally Sale No : ${tallyRefNo?if_exists}</fo:block>
				</fo:table-cell>
			</fo:table-row>
		</fo:table-body>
	</fo:table>
	</fo:block>-->
            
            
        		<fo:block linefeed-treatment="preserve">&#xA;</fo:block> 
             	 
        		<fo:block>
             		<fo:table border-style="solid">
             		    <fo:table-column column-width="4%"/>
			            <fo:table-column column-width="25%"/>
			            <fo:table-column column-width="10%"/>
			            <fo:table-column column-width="10%"/>
			            <fo:table-column column-width="17%"/>
	                    <fo:table-column column-width="10%"/>
			            <fo:table-column column-width="10%"/>
			            <fo:table-column column-width="15%"/>
			             <fo:table-column column-width="10%"/>
			              <fo:table-column column-width="10%"/>
			               <fo:table-column column-width="10%"/>
			                <fo:table-column column-width="10%"/>
			                <fo:table-column column-width="10%"/>
			            
			            <fo:table-body>
			                <fo:table-row>
			                    <fo:table-cell border-style="solid">
					            	<fo:block  keep-together="always" text-align="center" font-size="11pt" white-space-collapse="false">SNo</fo:block>
					            </fo:table-cell>
					            <fo:table-cell border-style="solid">
					            	<fo:block  keep-together="always" text-align="center" font-size="11pt" white-space-collapse="false">Sale Invo No.</fo:block>
					            </fo:table-cell >
					            <fo:table-cell border-style="solid">
					            	<fo:block  keep-together="always" text-align="center" font-size="11pt" white-space-collapse="false">Date</fo:block>
					            </fo:table-cell >
					           
					            <fo:table-cell border-style="solid">
					            	<fo:block   text-align="center" font-size="11pt" white-space-collapse="false">Sale Quantity</fo:block>
					            	<fo:block   text-align="center" font-size="11pt" white-space-collapse="false">(KGS)</fo:block>
					            </fo:table-cell>
					            <fo:table-cell border-style="solid">
					            	<fo:block   text-align="center" font-size="11pt" white-space-collapse="false">Amount</fo:block>
					            	<fo:block   text-align="center" font-size="11pt" white-space-collapse="false">(Rs)</fo:block>
					            </fo:table-cell>
					            <fo:table-cell border-style="solid">
					            	<fo:block   text-align="center" font-size="11pt" white-space-collapse="false">Name of Supplier</fo:block>
					            </fo:table-cell>
					             <fo:table-cell border-style="solid">
					            	<fo:block  text-align="center" font-size="11pt" white-space-collapse="false">Destination</fo:block>
					            </fo:table-cell>
					             <fo:table-cell border-style="solid">
					            	<fo:block   text-align="left" font-size="11pt" white-space-collapse="false">Transporter</fo:block>
					            </fo:table-cell>
					            <fo:table-cell border-style="solid">
					            	<fo:block   text-align="left" font-size="11pt" white-space-collapse="false">Lr No</fo:block>
					            </fo:table-cell>
					            <fo:table-cell border-style="solid">
					            	<fo:block   text-align="left" font-size="11pt" white-space-collapse="false">Date</fo:block>
					            </fo:table-cell>
					            <fo:table-cell border-style="solid">
					            	<fo:block   text-align="left" font-size="11pt" white-space-collapse="false">Claim Amount</fo:block>
					            </fo:table-cell>
					            <fo:table-cell border-style="solid">
					            	<fo:block   text-align="left" font-size="11pt" white-space-collapse="false">Eligibility Claim Amount</fo:block>
					            </fo:table-cell>
							</fo:table-row>
							
							 <#assign  eachPartyList = finalMap.get(eachParty)>
			                     <#assign sr=1>
			                  <#list eachPartyList as eachPartyDet>
			                <fo:table-row>
			                    <fo:table-cell border-style="solid">
					            	<fo:block  keep-together="always" text-align="left" font-size="10pt" white-space-collapse="false">${sr} </fo:block>
					            </fo:table-cell>
					            <fo:table-cell border-style="solid">
					            	<fo:block text-align="left" font-size="11pt" white-space-collapse="false">${eachPartyDet.get("billno")} </fo:block>
					            </fo:table-cell >
					            <fo:table-cell border-style="solid">
					            	<fo:block  text-align="center" font-size="11pt" white-space-collapse="false">${eachPartyDet.get("invoiceDate")}</fo:block>
					            </fo:table-cell>  
					             <fo:table-cell border-style="solid">
					            	<fo:block  keep-together="always" text-align="right" font-size="11pt" white-space-collapse="false">${eachPartyDet.get("invoiceQTY")}</fo:block>
						         </fo:table-cell>
					            <fo:table-cell border-style="solid">
					            	<fo:block  keep-together="always" text-align="left" font-size="11pt" white-space-collapse="false">${eachPartyDet.get("invoiceAmount")}</fo:block>
					            </fo:table-cell>
					            <fo:table-cell border-style="solid">
					            	<fo:block  keep-together="always" text-align="left" font-size="11pt" white-space-collapse="false">${eachPartyDet.get("supplierName")}</fo:block>
					            </fo:table-cell>
					             <fo:table-cell border-style="solid">
					            	<fo:block  keep-together="always" text-align="left" font-size="11pt" white-space-collapse="false">${eachPartyDet.get("destAddr")}</fo:block>
					            </fo:table-cell>
					             <fo:table-cell border-style="solid">
					            	<fo:block  keep-together="always" text-align="left" font-size="11pt" white-space-collapse="false">${eachPartyDet.get("transporter")}</fo:block>
					            </fo:table-cell>
					             <fo:table-cell border-style="solid">
					            	<fo:block  keep-together="always" text-align="left" font-size="11pt" white-space-collapse="false">${eachPartyDet.get("destAddr")}</fo:block>
					            </fo:table-cell>
					             <fo:table-cell border-style="solid">
					            	<fo:block  keep-together="always" text-align="left" font-size="11pt" white-space-collapse="false">${eachPartyDet.get("lrNumber")}</fo:block>
					            </fo:table-cell>
					             <fo:table-cell border-style="solid">
					            	<fo:block  keep-together="always" text-align="left" font-size="11pt" white-space-collapse="false">${eachPartyDet.get("lrDate")}</fo:block>
					            </fo:table-cell>
					             <fo:table-cell border-style="solid">
					            	<fo:block  keep-together="always" text-align="left" font-size="11pt" white-space-collapse="false">${eachPartyDet.get("claim")}</fo:block>
					            </fo:table-cell>
					            <fo:table-cell border-style="solid">
					            	<fo:block  keep-together="always" text-align="left" font-size="11pt" white-space-collapse="false">${eachPartyDet.get("eligibleAMT")}</fo:block>
					            </fo:table-cell>
							</fo:table-row>
							<#assign sr=sr+1>
							</#list>
						</fo:table-body>
					</fo:table>
				</fo:block>
				<fo:block>&#160;&#160;&#160;&#160;&#160;</fo:block>
			</fo:flow>
		</fo:page-sequence>
		</#list>
		<#else>
    	<fo:page-sequence master-reference="main">
	    	<fo:flow flow-name="xsl-region-body" font-family="Helvetica">
	       		 <fo:block font-size="14pt">
 	            	${uiLabelMap.NoOrdersFound}.
	       		 </fo:block>
	    	</fo:flow>
		</fo:page-sequence>	
    </#if> 
 </fo:root>
</#escape>