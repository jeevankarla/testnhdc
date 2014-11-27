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
	<fo:simple-page-master master-name="main" page-height="12in" page-width="15in"
            margin-top="0.1in" margin-bottom=".7in" margin-left=".5in" margin-right=".5in">
        <fo:region-body margin-top="2.15in"/>
        <fo:region-before extent="1.in"/>
        <fo:region-after extent="1.5in"/>        
    </fo:simple-page-master>   
</fo:layout-master-set>
${setRequestAttribute("OUTPUT_FILENAME", "LoanAvailedReport.pdf")}

 <#if loanTypeList?has_content> 
<fo:page-sequence master-reference="main" force-page-count="no-force" font-family="Courier,monospace">					
			<fo:static-content flow-name="xsl-region-before">
					<fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false">&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;UserLogin : <#if userLogin?exists>${userLogin.userLoginId?if_exists}</#if></fo:block>
					<fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false">&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;Date:${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(nowTimestamp, "dd/MM/yy HH:mm:ss")}</fo:block>
					<fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="15pt" font-weight="bold">  MOTHER DAIRY A UNIT OF K.M.F </fo:block>
					<fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="15pt" font-weight="bold">  G.K.V.K POST, BANGALORE, KARNATAKA - 560065 </fo:block>
			
					<#assign loanTypeId=parameters.loanTypeId>
		          <#assign loanTypeName = delegator.findOne("LoanType", {"loanTypeId" : loanTypeId}, true)>
		           <#if loanTypeName?has_content> 
		          
                    <fo:block text-align="center" font-size="13pt" keep-together="always"  white-space-collapse="false" font-weight="bold">&#160;&#160;LOAN AND ADVANCES REPORT PRINCIPLE AND INTEREST FOR: ${(loanTypeName.description)?if_exists}  </fo:block>
              		   </#if>
              		 <fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="13pt" font-weight="bold">  For The Month Of :${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(fromDate?if_exists, "dd/MM/yy ")} TO ${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(thruDate?if_exists, "dd/MM/yy")} </fo:block>
              		<fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="13pt" font-weight="bold"> &#160;&#160;  </fo:block>
              		   
              		 <fo:block>
	                 	<fo:table border-style="solid">
	                    <fo:table-column column-width="30pt"/>
	                    <fo:table-column column-width="60pt"/>
	                    <fo:table-column column-width="140pt"/>  
	               	    <fo:table-column column-width="70pt"/>
	               	    <fo:table-column column-width="80pt"/>
	            		<fo:table-column column-width="80pt"/> 		
	            		<fo:table-column column-width="50pt"/>
	            		<fo:table-column column-width="80pt"/>
	            		<fo:table-column column-width="50pt"/>
	            		<fo:table-column column-width="70pt"/>
	            		<fo:table-column column-width="70pt"/>
	            		<fo:table-column column-width="80pt"/>
	            		<fo:table-column column-width="80pt"/>
	            		<fo:table-column column-width="50pt"/>
	            		<fo:table-column column-width="50pt"/>
	                    <fo:table-body>
	                    <fo:table-row >
	                    		<fo:table-cell border-style="solid">
                            		<fo:block   text-align="left" font-size="12pt" white-space-collapse="false" font-weight="bold">SNO</fo:block>  
                       			</fo:table-cell>                     
                       			<fo:table-cell border-style="solid">
                            		<fo:block   text-align="left" font-size="12pt" white-space-collapse="false" font-weight="bold">Employee Code </fo:block>
                       			</fo:table-cell>
                       			<fo:table-cell border-style="solid">
                            		<fo:block   text-align="left" font-size="12pt" white-space-collapse="false" font-weight="bold">Employee Name</fo:block> 
                       			</fo:table-cell>
                       			<fo:table-cell border-style="solid">
                            		<fo:block   text-align="left" font-size="12pt" white-space-collapse="false" font-weight="bold">Principal Amount</fo:block>  
                       			</fo:table-cell>
                       			<fo:table-cell border-style="solid">
                            		<fo:block   text-align="left" font-size="12pt" white-space-collapse="false" font-weight="bold">Interest Amount</fo:block>  
                       			</fo:table-cell>
                       			<fo:table-cell border-style="solid">
                            		<fo:block   text-align="left" font-size="12pt" white-space-collapse="false" font-weight="bold">PRN INST  EMI Rs </fo:block>  
                       			</fo:table-cell>
                        		<fo:table-cell border-style="solid">
                            		<fo:block   text-align="left" font-size="12pt" white-space-collapse="false" font-weight="bold">PRN INST NO</fo:block>   
                        		</fo:table-cell>
                        		<fo:table-cell border-style="solid">
                            		<fo:block   text-align="left" font-size="12pt" white-space-collapse="false" font-weight="bold">INT  INST  EMI Rs </fo:block>  
                        		</fo:table-cell>
                        		<fo:table-cell border-style="solid">
                            		<fo:block  text-align="left" font-size="12pt" white-space-collapse="false" font-weight="bold">INT INST NO</fo:block>  
                        		</fo:table-cell>
                        		<fo:table-cell border-style="solid">
                            		<fo:block   text-align="left" font-size="12pt" white-space-collapse="false" font-weight="bold">TOT DEDN PRN </fo:block>  
                       			</fo:table-cell>
                       			<fo:table-cell border-style="solid">
                            		<fo:block   text-align="left" font-size="12pt" white-space-collapse="false" font-weight="bold">TOT DEDN INT </fo:block>  
                       			</fo:table-cell>
                        		<fo:table-cell border-style="solid">
                            		<fo:block   text-align="left" font-size="12pt" white-space-collapse="false" font-weight="bold">BAL AMOUNT PRN </fo:block>   
                        		</fo:table-cell>
                        		<fo:table-cell border-style="solid">
                            		<fo:block   text-align="left" font-size="12pt" white-space-collapse="false" font-weight="bold">BAL AMOUNT INT </fo:block>  
                        		</fo:table-cell>
                        		<fo:table-cell border-style="solid">
                            		<fo:block  text-align="left" font-size="12pt" white-space-collapse="false" font-weight="bold">BAL EMI PRN</fo:block>  
                        		</fo:table-cell>
                        		<fo:table-cell border-style="solid">
                            		<fo:block   text-align="left" font-size="12pt" white-space-collapse="false" font-weight="bold">BAL EMI INT</fo:block>  
                       			</fo:table-cell>
                			</fo:table-row>
                    </fo:table-body>
                </fo:table>
              </fo:block> 		                
            </fo:static-content>		
           <fo:flow flow-name="xsl-region-body"   font-family="Courier,monospace">		
            	<fo:block>
                 	<fo:table border-style="solid">
                    <fo:table-column column-width="30pt"/>
                    <fo:table-column column-width="60pt"/>
                    <fo:table-column column-width="140pt"/>  
               	    <fo:table-column column-width="70pt"/>
               	    <fo:table-column column-width="80pt"/>
            		<fo:table-column column-width="80pt"/> 		
            		<fo:table-column column-width="50pt"/>
            		<fo:table-column column-width="80pt"/>
            		<fo:table-column column-width="50pt"/>
            		<fo:table-column column-width="70pt"/>
            		<fo:table-column column-width="70pt"/> 		
            		<fo:table-column column-width="80pt"/>
            		<fo:table-column column-width="80pt"/>
            		<fo:table-column column-width="50pt"/>
            		<fo:table-column column-width="50pt"/>
                    <fo:table-body>
                		<#assign sNo=1>
                		<#assign totalPRNAMT=0>
                		<#assign totalINTAMT=0>
                		<#assign totalprinAmtEmi=0>
                		<#assign totalintrstAmtEmi=0>
                		<#assign totaloftotalRecPrinAmount=0>
                		<#assign totaloftotalRecIntAmount=0>
                		<#assign totalnetPrinAmount=0>
                		<#assign totalnetIntAmount=0>
                		<#assign totalnetPrinInst=0>
                		<#assign totalnetIntInst=0>
                		
                		
                    	<#list loanTypeList as loanAcctngDetails>
                          <#-- <#assign disbDate = (loanAcctngDetails.get("disbDate")?if_exists)/> -->
	                		<#assign employeeId = (loanAcctngDetails.get("employeeId")?if_exists)/>
	                		<#assign employeeName = (loanAcctngDetails.get("employeeName")?if_exists)/>
	                		<#assign principalAmount = (loanAcctngDetails.get("principalAmount")?if_exists)/>
	                		<#assign interestAmount = (loanAcctngDetails.get("interestAmount")?if_exists)/>
	                		<#assign prinAmtEmi = (loanAcctngDetails.get("prinAmtEmi")?if_exists)/>
	                		<#assign numPrincipalInst = (loanAcctngDetails.get("numPrincipalInst")?if_exists)/>
	                		<#assign intrstAmtEmi = (loanAcctngDetails.get("intrstAmtEmi")?if_exists)/>
	                		<#assign numInterestInst = (loanAcctngDetails.get("numInterestInst")?if_exists)/>
	                		<#assign totalRecPrinAmount = (loanAcctngDetails.get("totalRecPrinAmount")?if_exists)/>
							<#assign totalRecIntAmount = (loanAcctngDetails.get("totalRecIntAmount")?if_exists)/>
				     		<#assign netPrinAmount = (loanAcctngDetails.get("netPrinAmount")?if_exists)/>
						    <#assign netIntAmount = (loanAcctngDetails.get("netIntAmount")?if_exists)/>
							<#assign netPrinInst = (loanAcctngDetails.get("netPrinInst")?if_exists)/>
						    <#assign netIntInst = (loanAcctngDetails.get("netIntInst")?if_exists)/>
						                		
						    <#assign totalPRNAMT=totalPRNAMT+principalAmount> 
						    <#assign totalINTAMT=totalINTAMT+interestAmount>       		
						    <#assign totalprinAmtEmi=totalprinAmtEmi+prinAmtEmi>
                		    <#assign totalintrstAmtEmi=totalintrstAmtEmi+intrstAmtEmi>
                		    <#assign totaloftotalRecPrinAmount=totaloftotalRecPrinAmount+totalRecPrinAmount>
                		    <#assign totaloftotalRecIntAmount=totaloftotalRecIntAmount+totalRecIntAmount>
                		    <#assign totalnetPrinAmount=totalnetPrinAmount+netPrinAmount>
                		    <#assign totalnetIntAmount=totalnetIntAmount+netIntAmount>
                		    <#assign totalnetPrinInst=totalnetPrinInst+netPrinInst>
                		    <#assign totalnetIntInst=totalnetIntInst+netIntInst>
						                     		
								<fo:table-row border-style="solid">
								
									<fo:table-cell border-style="solid">
	                            	 <fo:block  text-align="left"  font-size="13pt" > 	 ${sNo}</fo:block>                 			  
	                       			</fo:table-cell>
                                	<fo:table-cell border-style="solid" >
	                                    <fo:block font-size="13pt" text-align="left">${employeeId?if_exists}</fo:block>
	                                </fo:table-cell>
                                 	<fo:table-cell border-style="solid">
	                                    <fo:block text-align="left">${employeeName?if_exists}</fo:block>
	                                </fo:table-cell>
	                       			<fo:table-cell border-style="solid">
                                    <fo:block text-align="left" font-size="13pt" >
                                            ${principalAmount?if_exists}
                                    </fo:block>
	                                </fo:table-cell>
	                                <fo:table-cell border-style="solid">
                                    <fo:block text-align="left" font-size="13pt" >
                                            ${interestAmount?if_exists}
                                    </fo:block>
	                                </fo:table-cell>
	                                <fo:table-cell border-style="solid">
	                                    <fo:block text-align="left" font-size="13pt">
	                                       ${prinAmtEmi?if_exists}
	                                    </fo:block>
	                                </fo:table-cell>
	                                <fo:table-cell border-style="solid">
	                                    <fo:block text-align="left" font-size="13pt">
	                                     ${numPrincipalInst?if_exists}
	                                    </fo:block>
	                                </fo:table-cell>
	                                <fo:table-cell border-style="solid">
	                                    <fo:block text-align="left" font-size="13pt">
	                                       ${interestAmount?if_exists}
	                                    </fo:block>
	                                </fo:table-cell>
	                                
	                                <fo:table-cell border-style="solid">
	                                    <fo:block text-align="left" font-size="13pt">
	                                     ${numInterestInst?if_exists}
	                                    </fo:block>
	                                </fo:table-cell>
	                               <fo:table-cell border-style="solid">
	                                    <fo:block text-align="left" font-size="13pt">
	                                     ${totalRecPrinAmount?if_exists}
	                                    </fo:block>
	                                </fo:table-cell>
	                               <fo:table-cell border-style="solid">
	                                    <fo:block text-align="left" font-size="13pt">
	                                     ${totalRecIntAmount?if_exists}
	                                    </fo:block>
	                                </fo:table-cell>
	                               <fo:table-cell border-style="solid">
	                                    <fo:block text-align="left" font-size="13pt">
	                                     ${netPrinAmount?if_exists}
	                                    </fo:block>
	                                </fo:table-cell>
	                               <fo:table-cell border-style="solid">
	                                    <fo:block text-align="left" font-size="13pt">
	                                     ${netIntAmount?if_exists}
	                                    </fo:block>
	                                </fo:table-cell>
	                               <fo:table-cell border-style="solid">
	                                    <fo:block text-align="left" font-size="13pt">
	                                     ${netPrinInst?if_exists}
	                                    </fo:block>
	                                </fo:table-cell>
	                               <fo:table-cell border-style="solid">
	                                    <fo:block text-align="left" font-size="13pt">
	                                     ${netIntInst?if_exists}
	                                    </fo:block>
	                                </fo:table-cell>
	                              	<#assign sNo=sNo+1>
                              </fo:table-row>
                          </#list>
                          <fo:table-row >
		                   		<fo:table-cell >	
                            		<fo:block keep-together="always" font-weight="bold">&#160;&#160;</fo:block>
                            	</fo:table-cell>
                            </fo:table-row>
                          <fo:table-row border-style="solid">
								
									<fo:table-cell border-style="solid">
	                            	 <fo:block  text-align="left"  font-size="13pt" > 	</fo:block>                 			  
	                       			</fo:table-cell>
                                	<fo:table-cell border-style="solid" font-weight="bold">
	                                    <fo:block font-size="13pt" text-align="left"></fo:block>
	                                </fo:table-cell>
                                 	<fo:table-cell border-style="solid">
	                                    <fo:block text-align="left" font-weight="bold">TOTAL</fo:block>
	                                </fo:table-cell>
	                       			<fo:table-cell border-style="solid">
                                    <fo:block text-align="left" font-size="13pt" font-weight="bold">
                                       ${totalPRNAMT}    
                                    </fo:block>
	                                </fo:table-cell>
	                                <fo:table-cell border-style="solid">
                                    <fo:block text-align="left" font-size="13pt" font-weight="bold">
                                       ${totalINTAMT}    
                                    </fo:block>
	                                </fo:table-cell>
	                                <fo:table-cell border-style="solid">
	                                 <fo:block text-align="left" font-size="13pt" font-weight="bold">
	                                    ${totalprinAmtEmi}    
	                                  </fo:block>
	                                </fo:table-cell>
	                                <fo:table-cell border-style="solid">
	                                 <fo:block text-align="left" font-size="13pt" font-weight="bold">
	                                </fo:block>
	                                </fo:table-cell>
	                                <fo:table-cell border-style="solid">
	                                    <fo:block text-align="left" font-size="13pt" font-weight="bold">
	                                     ${totalintrstAmtEmi}    
	                                    </fo:block>
	                                </fo:table-cell>
	                                <fo:table-cell border-style="solid">
	                                    <fo:block text-align="left" font-size="13pt" font-weight="bold">
	                                    </fo:block>
	                                </fo:table-cell>
	                               <fo:table-cell border-style="solid">
	                                    <fo:block text-align="left" font-size="13pt" font-weight="bold">
	                                    ${totaloftotalRecPrinAmount}    
	                                    </fo:block>
	                                </fo:table-cell>
	                               <fo:table-cell border-style="solid">
	                                    <fo:block text-align="left" font-size="13pt" font-weight="bold">
	                                     ${totaloftotalRecIntAmount}    
	                                    </fo:block>
	                                </fo:table-cell>
	                               <fo:table-cell border-style="solid">
	                                    <fo:block text-align="left" font-size="13pt" font-weight="bold">
	                                      ${totalnetPrinAmount}    
	                                    </fo:block>
	                                </fo:table-cell>
	                               <fo:table-cell border-style="solid">
	                                    <fo:block text-align="left" font-size="13pt" font-weight="bold">
	                                     ${totalnetIntAmount}    
	                                    </fo:block>
	                                </fo:table-cell>
	                               <fo:table-cell border-style="solid">
	                                    <fo:block text-align="left" font-size="13pt" font-weight="bold">
                                         ${totalnetPrinInst}    
	                                    </fo:block>
	                                </fo:table-cell>
	                               <fo:table-cell border-style="solid">
	                                    <fo:block text-align="left" font-size="13pt" font-weight="bold">
	                                     ${totalnetIntInst}    
	                                    </fo:block>
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
				                      NO DATA FOUND      	
	       		 </fo:block>
		</fo:flow>
	</fo:page-sequence>	
    </#if>  
</fo:root>
</#escape>