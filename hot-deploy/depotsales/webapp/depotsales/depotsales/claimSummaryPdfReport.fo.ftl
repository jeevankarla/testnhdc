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
			<fo:simple-page-master master-name="main" page-height="12in" page-width="10in"  margin-bottom=".1in" margin-left="0.1in" margin-right=".3in">
		        <fo:region-body margin-top="1.3in"/>
		        <fo:region-before extent="1in"/>
		        <fo:region-after extent="1in"/>        
		    </fo:simple-page-master>   
		</fo:layout-master-set>
        ${setRequestAttribute("OUTPUT_FILENAME", "claimReportDetails.pdf")}
        <#if errorMessage?has_content>
	<fo:page-sequence master-reference="main">
	<fo:flow flow-name="xsl-region-body" font-family="Helvetica">
	   <fo:block font-size="14pt">
	           ${errorMessage}.
        </fo:block>
	</fo:flow>
	</fo:page-sequence>	
	<#else>
       <#if DistrictWiseList?has_content>
         <#assign claimTotal=0>
	        <fo:page-sequence master-reference="main" font-size="12pt">	
	        	<fo:static-content flow-name="xsl-region-before" font-family="Arial">
                    <#assign reportHeader = delegator.findOne("TenantConfiguration", {"propertyTypeEnumId" : "COMPANY_HEADER","propertyName" : "reportHeaderLable"}, true)>
 			        <#assign reportSubHeader = delegator.findOne("TenantConfiguration", {"propertyTypeEnumId" : "COMPANY_HEADER","propertyName" : "reportSubHeaderLable"}, true)>
 			        <fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="12pt" font-weight="bold" >${reportHeader.description?if_exists} </fo:block>
        			<fo:block  text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="12pt" font-weight="bold" >${BOAddress?if_exists}</fo:block>
                	<fo:block text-align="center" white-space-collapse="false" font-size = "12pt" font-weight="bold">Statement for Claiming Reimbursement against Yarn Subsidy allowed to the Handloom Weavers towards the Supply of Indian Silk and Cotton Hank Yarn</fo:block>
          			<fo:block text-align="center" keep-together="always"  white-space-collapse="false" font-size = "10pt"> From ${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(fromDate, "dd/MM/yyyy")} To ${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(thruDate, "dd/MM/yyyy")} </fo:block>
            	</fo:static-content>	        	
	        	<fo:flow flow-name="xsl-region-body" font-family="Arial">	
	        	   <fo:block text-align="left">
		        	<fo:table>
		               <fo:table-column column-width="6%"/>
	                    <fo:table-column column-width="15%"/>
	                    <fo:table-column column-width="15%"/>
	                    <fo:table-column column-width="16%"/>
	                    <fo:table-column column-width="16%"/>
	                    <fo:table-column column-width="16%"/>
	                    <fo:table-column column-width="16%"/>
		                    <fo:table-body>
		                       <fo:table-row>
				                    <fo:table-cell border-style="solid">
						            	<fo:block  text-align="center" font-size="10pt" white-space-collapse="false" font-weight="bold">SI. No.</fo:block>  
						            </fo:table-cell>
						             <fo:table-cell border-style="solid">
						            	<fo:block  text-align="center" font-size="10pt" white-space-collapse="false" font-weight="bold">Name of State</fo:block>  
						            </fo:table-cell>
						            <fo:table-cell border-style="solid">
						            	<fo:block text-align="center" font-size="10pt" white-space-collapse="false" font-weight="bold">Yarn Supplied during the quarter(in Kgs)</fo:block>  
						            </fo:table-cell>
						            <fo:table-cell border-style="solid">
						            	<fo:block text-align="center" font-size="10pt" white-space-collapse="false" font-weight="bold">Value Of Yarn Before yarn subsidy(in Rs)</fo:block>  
						            </fo:table-cell>
						            <fo:table-cell border-style="solid">
						            	<fo:block text-align="center" font-size="10pt" white-space-collapse="false" font-weight="bold">Yarn Subsidy @10% on yarn value Before Subsidy(in Rs)</fo:block>  
						            </fo:table-cell>
						            <fo:table-cell border-style="solid">
						            	<fo:block text-align="center" font-size="10pt" white-space-collapse="false" font-weight="bold">Service Charges @0.5% Of yarn value Before Subsidy(in Rs)</fo:block>  
						            </fo:table-cell>
						             <fo:table-cell border-style="solid">
						            	<fo:block text-align="center" font-size="10pt" white-space-collapse="false" font-weight="bold">Total Claim For Yarn Subsidy And Claim Charges(in Rs)</fo:block>  
						            </fo:table-cell>
							  </fo:table-row>
		                       <#list DistrictWiseList as eachEntry>
		                       	<#assign claimAmt=0>
		                        <fo:table-row>
				                    <fo:table-cell border-style="solid">
						            	<fo:block  text-align="left" font-size="10pt" white-space-collapse="false">${eachEntry.sNo}</fo:block>  
						            </fo:table-cell>
						             <fo:table-cell border-style="solid">
						            	<fo:block  text-align="left" font-size="10pt" white-space-collapse="false">${eachEntry.districtName}</fo:block>  
						            </fo:table-cell>
						             <fo:table-cell border-style="solid">
						            	<fo:block  text-align="right" font-size="10pt" white-space-collapse="false">${eachEntry.quantity}</fo:block>  
						            </fo:table-cell>
						             <fo:table-cell border-style="solid">
						            	<fo:block  text-align="right" font-size="10pt" white-space-collapse="false">${eachEntry.value}</fo:block>  
						            </fo:table-cell>
						             <fo:table-cell border-style="solid">
						            	<fo:block  text-align="right" font-size="10pt" white-space-collapse="false">${eachEntry.subsidyAmt}</fo:block>  
						            </fo:table-cell>
						             <fo:table-cell border-style="solid">
						            	<fo:block  text-align="right" font-size="10pt" white-space-collapse="false">${eachEntry.serviceCharg}</fo:block>  
						            </fo:table-cell>
						            <#assign claimAmt= eachEntry.claimTotal>
						            <#assign claimTotal=claimTotal+claimAmt>
						             <fo:table-cell border-style="solid">
						            	<fo:block  text-align="right" font-size="10pt" white-space-collapse="false">${eachEntry.claimTotal?if_exists?string("##0.00")}</fo:block>  
						            </fo:table-cell>
							     </fo:table-row>
							   </#list>
							   <fo:table-row>
				                    <fo:table-cell border-style="solid">
						            	<fo:block  text-align="left" font-size="10pt" white-space-collapse="false" font-weight="bold">TOTAL</fo:block>  
						            </fo:table-cell>
						             <fo:table-cell border-style="solid">
						            	<fo:block  text-align="left" font-size="10pt" white-space-collapse="false" font-weight="bold"></fo:block>  
						            </fo:table-cell>
						             <fo:table-cell border-style="solid">
						            	<fo:block  text-align="right" font-size="10pt" white-space-collapse="false" font-weight="bold">${tempToMap.quantity}</fo:block>  
						            </fo:table-cell>
						             <fo:table-cell border-style="solid">
						            	<fo:block  text-align="right" font-size="10pt" white-space-collapse="false" font-weight="bold">${tempToMap.value}</fo:block>  
						            </fo:table-cell>
						             <fo:table-cell border-style="solid">
						            	<fo:block  text-align="right" font-size="10pt" white-space-collapse="false" font-weight="bold">${tempToMap.subsidyAmt}</fo:block>  
						            </fo:table-cell>
						             <fo:table-cell border-style="solid">
						            	<fo:block  text-align="right" font-size="10pt" white-space-collapse="false" font-weight="bold">${tempToMap.serviceCharg}</fo:block>  
						            </fo:table-cell>
						             <fo:table-cell border-style="solid">
						            	<fo:block  text-align="right" font-size="10pt" white-space-collapse="false" font-weight="bold">${tempToMap.claimTotal?if_exists?string("##0.00")}</fo:block>  
						            </fo:table-cell>
							     </fo:table-row>
							 </fo:table-body>
						</fo:table>
						<fo:block linefeed-treatment="preserve">&#xA;</fo:block>	
						<fo:block linefeed-treatment="preserve">&#xA;</fo:block>	
						<fo:block linefeed-treatment="preserve">&#xA;</fo:block>
                	    <fo:block text-align="left" white-space-collapse="false" font-weight="bold" font-size = "12pt">FUNDS POSITION WITH THE IMPLEMENTING AGENCY:-</fo:block>
						<fo:table>
	                    <fo:table-column column-width="5%"/>
	                    <fo:table-column column-width="55%"/>
	                    <fo:table-column column-width="16%"/>
		                    <fo:table-body>
		                        <fo:table-row>
				                    <fo:table-cell border-style="solid">
						            	<fo:block  text-align="center" font-size="10pt" white-space-collapse="false" font-weight="bold"></fo:block>  
						            </fo:table-cell>
						             <fo:table-cell border-style="solid">
						            	<fo:block  text-align="center" font-size="10pt" white-space-collapse="false" font-weight="bold">Particulars</fo:block>  
						            </fo:table-cell>
						             <fo:table-cell border-style="solid">
						            	<fo:block text-align="center" font-size="10pt" white-space-collapse="false" font-weight="bold">Amount (in Rs.)</fo:block>  
						            </fo:table-cell>
							    </fo:table-row>
		                        <fo:table-row>
				                    <fo:table-cell border-style="solid">
						            	<fo:block  text-align="left" font-size="10pt" white-space-collapse="false">i</fo:block>  
						            </fo:table-cell>
						             <fo:table-cell border-style="solid">
						            	<fo:block  text-align="left" font-size="10pt" white-space-collapse="false">Amount of Reimbursement claimed</fo:block>  
						            </fo:table-cell>
						            <#assign totalClaim=totalsubsidyAmt+totalserviceCharg>
						             <fo:table-cell border-style="solid">
						            	<fo:block  text-align="right" font-size="10pt" white-space-collapse="false">${totalClaim?if_exists?string("##0.00")}</fo:block>  
						            </fo:table-cell>
							     </fo:table-row>
							     <fo:table-row>
				                    <fo:table-cell border-style="solid">
						            	<fo:block  text-align="left" font-size="10pt" white-space-collapse="false">ii</fo:block>  
						            </fo:table-cell>
						             <fo:table-cell border-style="solid">
						            	<fo:block  text-align="left" font-size="10pt" white-space-collapse="false">Less:- Advance amount already claimed</fo:block>  
						            </fo:table-cell>
						             <fo:table-cell border-style="solid">
						            	<fo:block  text-align="right" font-size="10pt" white-space-collapse="false"></fo:block>  
						            </fo:table-cell>
							     </fo:table-row>
							     <fo:table-row>
				                    <fo:table-cell border-style="solid">
						            	<fo:block  text-align="left" font-size="10pt" white-space-collapse="false">iii</fo:block>  
						            </fo:table-cell>
						             <fo:table-cell border-style="solid">
						            	<fo:block  text-align="left" font-size="10pt" white-space-collapse="false">Balance amount (i-ii)</fo:block>  
						            </fo:table-cell>
						             <fo:table-cell border-style="solid">
						            	<fo:block  text-align="right" font-size="10pt" white-space-collapse="false"></fo:block>  
						            </fo:table-cell>
							     </fo:table-row>
							 </fo:table-body>
						</fo:table>
						<fo:block linefeed-treatment="preserve">&#xA;</fo:block>	
						<fo:block linefeed-treatment="preserve">&#xA;</fo:block>
						<fo:block  text-align="left" font-size="10pt" white-space-collapse="false">Place: ${branchName}</fo:block>
						<fo:block  text-align="left" font-size="10pt" white-space-collapse="false">Date: </fo:block>  
						</fo:block>
        		</fo:flow>
        	</fo:page-sequence>
        	<#else>
			<fo:page-sequence master-reference="main">
				<fo:flow flow-name="xsl-region-body" font-family="Helvetica">
			   		 <fo:block font-size="14pt">
			        	${uiLabelMap.NoOrdersFound}.
			   		 </fo:block>
				</fo:flow>
			</fo:page-sequence>	
		</#if>   
 </#if>
 </fo:root>
</#escape>