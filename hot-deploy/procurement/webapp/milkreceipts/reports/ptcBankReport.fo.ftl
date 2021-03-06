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

<#-- do not display columns associated with values specified in the request, ie constraint values -->
<fo:layout-master-set>
	<fo:simple-page-master master-name="main" page-height="12in" page-width="10in"
            margin-top=".3in" margin-bottom=".3in" margin-left=".2in" margin-right=".1in">
        <fo:region-body margin-top="0.01in"/>
        <fo:region-before extent="1in"/>
        <fo:region-after extent="1in"/>        
    </fo:simple-page-master>   
</fo:layout-master-set>
${setRequestAttribute("OUTPUT_FILENAME", "DTCBankReport.pdf")}
 <#if ptcBankMap?has_content> 
<fo:page-sequence master-reference="main" force-page-count="no-force" font-family="Courier,monospace">					
			<fo:static-content flow-name="xsl-region-before">
			</fo:static-content>		
            <fo:flow flow-name="xsl-region-body"   font-family="Courier,monospace">	
					<#--<#assign finAccount = delegator.findOne("FinAccount", {"finAccountId" : parameters.finAccountId}, true)?if_exists/>-->
					<fo:block  keep-together="always" text-align="right" font-family="Courier,monospace" font-size="10pt" white-space-collapse="false">&#160;${uiLabelMap.CommonPage}- <fo:page-number/> </fo:block>
					<fo:block  keep-together="always" text-align="right" font-family="Courier,monospace" white-space-collapse="false">    UserLogin : <#if userLogin?exists>${userLogin.userLoginId?if_exists}</#if></fo:block>
					<fo:block  keep-together="always" text-align="right" font-family="Courier,monospace" white-space-collapse="false">&#160;      Date:${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(nowTimestamp, "dd/MM/yy HH:mm:ss")}</fo:block>
					<fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-weight="bold">&#160;      ${uiLabelMap.KMFDairyHeader}</fo:block>
					<fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-weight="bold">&#160;      ${uiLabelMap.KMFDairySubHeader}</fo:block>
					<fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-weight="bold">&#160;STATEMENT SHOWING THE PAYMENT TOWARDS TRANSPORTATION CHARGES</fo:block>
              		<fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-weight="bold">&#160;TO BE CREDITED TO PTC CONTRACTORS AS PER DETAILS BELOW</fo:block>
              		<fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="5pt">&#160;  </fo:block>
              		<#assign finAcctDetails = delegator.findOne("FinAccount", {"finAccountId" :finAccountId}, true)>
		   		    <#if finAcctDetails?has_content>
              			<fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-weight="bold">&#160;${finAcctDetails.finAccountName?if_exists}         &#160;&#160;&#160;Period: ${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(fromDate, "dd-MMM-yyyy")} TO ${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(thruDate, "dd-MMM-yyyy")}  </fo:block>
              		</#if>
             		<fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="3pt">&#160;  </fo:block>
              		<fo:block>
	                 	<fo:table border-style="solid">
	                    <fo:table-column column-width="30pt"/>
	                    <fo:table-column column-width="60pt"/>
	                    <fo:table-column column-width="170pt"/>  
	               	    <fo:table-column column-width="130pt"/>
	            		<fo:table-column column-width="130pt"/>
	            		<fo:table-column column-width="140pt"/> 		
	                    <fo:table-body>
	                    <fo:table-row >
	                    		<fo:table-cell border-style="solid">
                            		<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false" font-weight="bold">S.No</fo:block>  
                       			</fo:table-cell>
                       			<fo:table-cell border-style="solid">
                            		<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false" font-weight="bold">Code</fo:block>
                       			</fo:table-cell>
                       			<fo:table-cell border-style="solid">
                            		<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false" font-weight="bold">Contractor Name</fo:block> 
                       			</fo:table-cell>
                       			<fo:table-cell border-style="solid">
                            		<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false" font-weight="bold">Pan Number</fo:block> 
                       			</fo:table-cell>
                       			<fo:table-cell border-style="solid">
                            		<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false" font-weight="bold">Account No.</fo:block>  
                       			</fo:table-cell>
                       			<fo:table-cell border-style="solid">
                            		<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false" font-weight="bold">Net Amount</fo:block>  
                        		</fo:table-cell>
                			</fo:table-row>
                    </fo:table-body>
                </fo:table>
               </fo:block> 		
            	<fo:block>
                 	<fo:table border-style="solid">
                    <fo:table-column column-width="30pt"/>
                    <fo:table-column column-width="60pt"/>
                    <fo:table-column column-width="170pt"/>  
               	    <fo:table-column column-width="130pt"/>
            		<fo:table-column column-width="130pt"/>
            		<fo:table-column column-width="140pt"/> 		
                    <fo:table-body>
                    <#assign sno=1>
                   <#assign ptcBankMapDetails = ptcBankMap.entrySet()?if_exists>											
                    	<#list ptcBankMapDetails as ptcBankList>
								<fo:table-row border-style="solid">
									<fo:table-cell border-style="solid">
	                            		<fo:block  text-align="left" font-size="11pt" white-space-collapse="false"> 
                                             ${sno?if_exists}
                                      </fo:block>  
	                       			</fo:table-cell>
									<fo:table-cell border-style="solid">
	                            		<fo:block  text-align="left" font-size="11pt" white-space-collapse="false"> 
                                            ${ptcBankList.getKey()?if_exists}
                                      </fo:block>  
	                       			</fo:table-cell>
	                       			<fo:table-cell border-style="solid">
                                    <fo:block text-align="left">
                                            ${ptcBankList.getValue().get("partyName")?if_exists}
                                    </fo:block>
	                                </fo:table-cell>
	                                <fo:table-cell border-style="solid">
	                                    <fo:block text-align="left">
                                            ${ptcBankList.getValue().get("partyPan")?if_exists}
	                                    </fo:block>
	                                </fo:table-cell>
	                                <fo:table-cell border-style="solid">
	                                    <fo:block text-align="left">
                                            ${ptcBankList.getValue().get("finAccountCode")?if_exists}
	                                    </fo:block>
	                                </fo:table-cell>
	                                  <fo:table-cell border-style="solid">
	                                    <fo:block text-align="right">
                                            ${ptcBankList.getValue().get("amount")?if_exists?string("#0.00")}
	                                    </fo:block>
	                                </fo:table-cell>
                              </fo:table-row>
                              <#assign sno=sno+1>
                          </#list>
                          	<fo:table-row border-style="solid">
									<fo:table-cell border-style="solid" number-columns-spanned="5">
	                            		<fo:block  text-align="center" font-size="11pt" font-weight="bold"> 
	                            		Grand Total
                                      </fo:block>  
	                       			</fo:table-cell>
	                                  <fo:table-cell border-style="solid">
	                                    <fo:block text-align="right" font-weight="bold">
                                           ${totAmount?if_exists?string("#0.00")}
	                                    </fo:block>
	                                </fo:table-cell>
                              </fo:table-row>
                              
                               <fo:table-row>
                          		<#assign amountWords = Static["org.ofbiz.base.util.UtilNumber"].formatRuleBasedAmount(totAmount, "%indRupees-and-paise", locale).toUpperCase()>
			                   <fo:table-cell>
			                        	<fo:block keep-together="always" font-size="12pt" font-weight="bold">Amount Payable:(${StringUtil.wrapString(amountWords?default(""))}  ONLY)</fo:block>
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
			            		<fo:block linefeed-treatment="preserve">&#xA;</fo:block>  
			       			</fo:table-cell>
						</fo:table-row>
						<fo:table-row>
							<fo:table-cell>
			            		<fo:block  keep-together="always" font-weight="bold">Prepared By</fo:block>  
			       			</fo:table-cell>
			       			<fo:table-cell>
			            		<fo:block  keep-together="always" font-weight="bold">&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;Pre-Audit</fo:block>  
			       			</fo:table-cell>
			       			<fo:table-cell>
			            		<fo:block  keep-together="always" font-weight="bold">&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;Manager(Finance)</fo:block>  
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
			            		<fo:block  keep-together="always"></fo:block>  
			       			</fo:table-cell>
			       			<fo:table-cell>
			            		<fo:block  keep-together="always" font-weight="bold">&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;Director</fo:block>  
			       			</fo:table-cell>
			       			<fo:table-cell>
			            		<fo:block  keep-together="always"></fo:block>  
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