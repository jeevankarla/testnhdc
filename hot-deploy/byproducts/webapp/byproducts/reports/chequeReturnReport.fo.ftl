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
            <fo:simple-page-master master-name="main" page-height="12in" page-width="8in"  margin-left=".3in" margin-right=".3in" margin-top=".5in" margin-bottom="0.5in">
                <fo:region-body margin-top="1.5in"/>
                <fo:region-before extent="1in"/>
                <fo:region-after extent="1in"/>
            </fo:simple-page-master>
        </fo:layout-master-set>
        
        ${setRequestAttribute("OUTPUT_FILENAME", "chequeReturnReport.txt")}
        <#if chequeReturnMap?has_content>
        <fo:page-sequence master-reference="main" force-page-count="no-force" font-family="Courier,monospace">		
        		<fo:static-content flow-name="xsl-region-before">
              		<fo:block  keep-together="always" text-align="right" font-family="Courier,monospace" white-space-collapse="false"></fo:block>
              		<fo:block text-align="left"  keep-together="always"  white-space-collapse="false" linefeed-treatment="preserve">&#xA;</fo:block>
              		<#assign reportHeader = delegator.findOne("TenantConfiguration", {"propertyTypeEnumId" : "COMPANY_HEADER","propertyName" : "reportHeaderLable"}, true)>
                    <#assign reportSubHeader = delegator.findOne("TenantConfiguration", {"propertyTypeEnumId" : "COMPANY_HEADER","propertyName" : "reportSubHeaderLable"}, true)>
              		 
		        	<fo:block text-align="center" font-weight="bold" font-size="12pt">${reportHeader.description?if_exists}.</fo:block>
                    <fo:block text-align="center" font-size="12pt" font-weight="bold">&#160;       ${reportSubHeader.description?if_exists}.</fo:block>
                    <fo:block text-align="left"  keep-together="always"  white-space-collapse="false" linefeed-treatment="preserve">&#xA;</fo:block>
                    <fo:block text-align="center" font-size="12pt" font-weight="bold">&#160;       STATEMENT OF CHEQUES DISHONOURED FOR THE PERIOD: ${periodFromDate?if_exists} - ${periodThruDate?if_exists}</fo:block>
              		<fo:block text-align="left"  keep-together="always"  white-space-collapse="false">============================================================================</fo:block> 
		        	<fo:block text-align="left"  keep-together="always"  white-space-collapse="false" font-size="10pt" font-weight="bold">CODE      AGENT-NAME           BANK             CHQ-NUM    CHQ-DATE      AMOUNT   DISHONOUR</fo:block> 
		        	<fo:block text-align="left"  keep-together="always"  white-space-collapse="false">============================================================================</fo:block> 
		        </fo:static-content>
		        <fo:flow flow-name="xsl-region-body"  font-family="Courier,monospace">	
            	<fo:block>
                 	<fo:table>
                    <fo:table-column column-width="55pt"/> 
               	    <fo:table-column column-width="100pt"/>
            		<fo:table-column column-width="130pt"/>
            		<fo:table-column column-width="70pt"/> 	
            		<fo:table-column column-width="40pt"/>	
            		<fo:table-column column-width="80pt"/>
            		<fo:table-column column-width="65pt"/>
            		<fo:table-column column-width="55pt"/>
                    <fo:table-body>
                    <#assign totalReturn = 0>
                    <#assign facilityReturns = chequeReturnMap.entrySet()>
                    <#list facilityReturns as eachFacilityReturn>
                    	<#assign returnEntries = eachFacilityReturn.getValue()>
                    	<#assign subTotal = 0>
                    	<#list returnEntries as eachReturn>
            				<fo:table-row>
               					<fo:table-cell>
                           			<fo:block  keep-together="always" text-align="left" white-space-collapse="false" font-size="10pt" font-weight="bold">&#160;${eachReturn.get("facilityId")?if_exists}</fo:block>  
                       			</fo:table-cell>
                       			<#assign facilityDetails = delegator.findOne("Facility", {"facilityId" : eachReturn.get("facilityId")}, false)>
                       			<fo:table-cell>
                           			<fo:block  keep-together="always" text-align="left" white-space-collapse="false" font-size="10pt">${Static["org.ofbiz.order.order.OrderServices"].nameTrim((StringUtil.wrapString(facilityDetails.get("facilityName")?if_exists)),15)}</fo:block>  
                       			</fo:table-cell>
                       			<fo:table-cell>
                           			<fo:block  keep-together="always" text-align="left" white-space-collapse="false" font-size="10pt">${Static["org.ofbiz.order.order.OrderServices"].nameTrim((StringUtil.wrapString(finTransMap.get(eachReturn.get("paymentId"))?if_exists)),4)}</fo:block>  
                       			</fo:table-cell>
                       			<fo:table-cell>
                           			<fo:block  keep-together="always" text-align="left" white-space-collapse="false" font-size="10pt">${eachReturn.get("referenceNum")?if_exists}</fo:block>  
                       			</fo:table-cell>
                       			<fo:table-cell>
                           			<fo:block  keep-together="always" text-align="left" white-space-collapse="false" font-size="10pt">${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(eachReturn.get("paymentDate"), "dd/MM/yy")}</fo:block>  
                       			</fo:table-cell>
                       			<fo:table-cell>
                           			<fo:block  keep-together="always" text-align="right" white-space-collapse="false" font-size="10pt">${eachReturn.get("amount")?if_exists?string("#0.00")}</fo:block>  
                       			</fo:table-cell>
                       			<fo:table-cell>
                           			<fo:block  keep-together="always" text-align="right" white-space-collapse="false" font-size="10pt">${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(eachReturn.get("cancelDate"), "dd/MM/yy")}</fo:block>  
                       			</fo:table-cell>
                       			
                       			<#assign subTotal = subTotal+eachReturn.get("amount")>
                       			<#assign totalReturn = totalReturn+eachReturn.get("amount")>
							</fo:table-row>
						</#list>
						<fo:table-row>
							<fo:table-cell>
		            			<fo:block  keep-together="always">----------------------------------------------------------------------------</fo:block>  
		            		</fo:table-cell>
				    	</fo:table-row>
				    	<fo:table-row>
			            	<fo:table-cell>
	                   			<fo:block  keep-together="always" text-align="left" white-space-collapse="false"></fo:block>  
	               			</fo:table-cell>
			           		<fo:table-cell>
			               		<fo:block  keep-together="always" text-align="left"  white-space-collapse="false"></fo:block>  
			           		</fo:table-cell>
			           		<fo:table-cell>
			            		<fo:block  keep-together="always" text-align="left"  white-space-collapse="false"></fo:block>  
			            	</fo:table-cell>
			           		<fo:table-cell>
			               		<fo:block  keep-together="always" text-align="left"  white-space-collapse="false" font-weight="bold" font-size="10pt">Sub Total</fo:block>  
			           		</fo:table-cell>
			    			<fo:table-cell>
			    				<fo:block  keep-together="always" text-align="left"  white-space-collapse="false"></fo:block>  
			    			</fo:table-cell>
			    			<fo:table-cell>
			            		<fo:block  keep-together="always" text-align="right"  white-space-collapse="false" font-weight="bold" font-size="10pt">${subTotal?if_exists?string("#0.00")}</fo:block>  
			            	</fo:table-cell>
			            	<fo:table-cell>
			            		<fo:block  keep-together="always" text-align="left"  white-space-collapse="false"></fo:block>  
			            	</fo:table-cell>
			            	<fo:table-cell>
			            		<fo:block  keep-together="always" text-align="left"  white-space-collapse="false"></fo:block>  
			            	</fo:table-cell>
		        		</fo:table-row>
				    	<fo:table-row>
							<fo:table-cell>
		            			<fo:block  keep-together="always">----------------------------------------------------------------------------</fo:block>  
		            		</fo:table-cell>
				    	</fo:table-row>
					</#list>
					<fo:table-row>
						<fo:table-cell>
		            		<fo:block  keep-together="always">----------------------------------------------------------------------------</fo:block>  
		            	</fo:table-cell>
				    </fo:table-row>
		            <fo:table-row>
		            	<fo:table-cell>
                   			<fo:block  keep-together="always" text-align="left" white-space-collapse="false"></fo:block>  
               			</fo:table-cell>
		           		<fo:table-cell>
		               		<fo:block  keep-together="always" text-align="left"  white-space-collapse="false"></fo:block>  
		           		</fo:table-cell>
		           		<fo:table-cell>
		               		<fo:block  keep-together="always" text-align="left"  white-space-collapse="false" font-weight="bold" font-size="10pt">Grand Total</fo:block>  
		           		</fo:table-cell>
		    			<fo:table-cell>
		            		<fo:block  keep-together="always" text-align="right"  white-space-collapse="false"></fo:block>  
		            	</fo:table-cell>
		    			<fo:table-cell>
		    				<fo:block  keep-together="always" text-align="right"  white-space-collapse="false"></fo:block>  
		    			</fo:table-cell>
		    			<fo:table-cell>
		            		<fo:block  keep-together="always" text-align="right"  white-space-collapse="false" font-weight="bold" font-size="10pt">${totalReturn?if_exists?string("#0.00")}</fo:block>  
		            	</fo:table-cell>
		            	<fo:table-cell>
		            		<fo:block  keep-together="always" text-align="left"  white-space-collapse="false"></fo:block>  
		            	</fo:table-cell>
		            	<fo:table-cell>
		            		<fo:block  keep-together="always" text-align="left"  white-space-collapse="false"></fo:block>  
		            	</fo:table-cell>
		        	</fo:table-row>
			        <fo:table-row>
					<fo:table-cell>
	            		<fo:block  keep-together="always">----------------------------------------------------------------------------</fo:block>  
	            	</fo:table-cell>
			        </fo:table-row>
			        
			        <fo:table-row>	
			            <fo:table-cell>
			            		<fo:block  keep-together="always" text-align="left" font-size="10pt" white-space-collapse="false">NOTE: The collection of above should be made and remitted to cashier only</fo:block> 
			            		<fo:block  keep-together="always" text-align="left" font-size="10pt" white-space-collapse="false">in form of D.D or Cash</fo:block> 
			            		<fo:block  keep-together="always" text-align="left" font-size="10pt" white-space-collapse="false"></fo:block>  
			            	</fo:table-cell>
			        </fo:table-row>
			       <fo:table-row>	
			            	<fo:table-cell>
			            		<fo:block linefeed-treatment="preserve">&#xA;</fo:block> 
			            	</fo:table-cell>
			        </fo:table-row>
					<fo:table-row>	
			            	<fo:table-cell>
			            		<fo:block linefeed-treatment="preserve">&#xA;</fo:block> 
			            	</fo:table-cell>
			        </fo:table-row>
					
					<fo:table-row>	
			            	<fo:table-cell>
			            		<fo:block linefeed-treatment="preserve">&#xA;</fo:block> 
			            	</fo:table-cell>
			        </fo:table-row>
			        <fo:table-row>
			            	<fo:table-cell>
			            		<fo:block text-align="center"  keep-together="always"  white-space-collapse="false" font-size="10pt">&#160;                                                                                                                                        MF/GMF</fo:block>
			            	</fo:table-cell>
			         </fo:table-row>
			         <fo:table-row>
			            	<fo:table-cell>
			            		<fo:block text-align="center"  keep-together="always"  white-space-collapse="false" font-size="10pt">&#160;                                                                                                                                           MOTHERDAIRY</fo:block>  
			            	</fo:table-cell>
			         </fo:table-row>
	                </fo:table-body>
                </fo:table>
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
 </fo:root>
</#escape>