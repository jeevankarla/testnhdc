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
	<fo:simple-page-master master-name="main" page-height="10in" page-width="12in"  margin-left="1in" margin-right=".3in" margin-bottom=".3in" margin-top=".5in">
        <fo:region-body margin-top="1.3in"/>
        <fo:region-before extent="1in"/>
        <fo:region-after extent="1in"/>        
    </fo:simple-page-master>   
</fo:layout-master-set>
${setRequestAttribute("OUTPUT_FILENAME", "prdctRetrnReport.txt")}
 <#if productCategoryMap?has_content> 
 <#assign grandTotalNetValue = 0>
<fo:page-sequence master-reference="main" force-page-count="no-force" font-family="Courier,monospace">					
			<fo:static-content flow-name="xsl-region-before">
					<fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" font-weight="bold" white-space-collapse="false">${uiLabelMap.KMFDairyHeader}</fo:block>
					<fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" font-weight="bold" white-space-collapse="false">${uiLabelMap.KMFDairySubHeader}</fo:block>
                    <fo:block  text-align="center"  keep-together="always"  white-space-collapse="false" font-weight="bold">CATEGORY WISE SALES STATEMENT From :: ${effectiveDateStr?if_exists}  To:: ${thruEffectiveDateStr?if_exists}</fo:block>
              		<fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false">UserLogin:<#if userLogin?exists>${userLogin.userLoginId?if_exists}</#if>&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;Print Date : ${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(nowTimestamp, "dd/MM/yy HH:mm:ss")}</fo:block>
              		<fo:block font-size="10pt">---------------------------------------------------------------------------------------------------------------------------------</fo:block>
              		<fo:block font-weight="bold" font-size="10pt">ProductCode			&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;ProductName   								&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;Total Amount          &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;Average Amount</fo:block>
            		<fo:block font-size="10pt">---------------------------------------------------------------------------------------------------------------------------------</fo:block>
            </fo:static-content>		
            <fo:flow flow-name="xsl-region-body"   font-family="Courier,monospace">	
            	<fo:block>
                    <fo:table>
				    <fo:table-column column-width="10%"/>
			        <fo:table-column column-width="12%"/>
			        <fo:table-column column-width="35%"/>
			        <fo:table-column column-width="18%"/>
			        <fo:table-column column-width="18%"/>
                    <fo:table-body>
                    <#assign serialNo = 1>
		                    	<#assign prodCategoryDetails = productCategoryMap.entrySet()>
		                    	<#list prodCategoryDetails as prodCategory>
		                    	<#if prodCategory.getKey()=="Milk">
		                    		<fo:table-row>
	                        			<fo:table-cell>
	                            			<fo:block  keep-together="always" text-align="left" font-weight="bold"  font-size="12pt" white-space-collapse="false">${prodCategory.getKey()}</fo:block>  
	                        			</fo:table-cell>
		                        	</fo:table-row>
		                        	<fo:table-row>
	                        			<fo:table-cell>
	                            			<fo:block font-size="10pt">---------------------------------------------------------------------------------------------------------------------------------</fo:block>
	                        			</fo:table-cell>
		                        	</fo:table-row>
		                        	</#if>
		                    		<#assign productDetails = prodCategory.getValue().entrySet()>
		                    			<#list productDetails as prod>
		                    				<#assign catProdDetails = prod.getValue().entrySet()>
	                    			<fo:table-row>
	                        			<fo:table-cell>
	                            			<fo:block  keep-together="always" text-align="left" font-weight="bold"  font-size="12pt" white-space-collapse="false">${prod.getKey()}</fo:block>  
	                        			</fo:table-cell>
		                        	</fo:table-row>
		                        	<fo:table-row>
	                        			<fo:table-cell>
	                            			<fo:block font-size="10pt">---------------------------------------------------------------------------------------------------------------------------------</fo:block>
	                        			</fo:table-cell>
		                        	</fo:table-row>
		                        	<#assign totalSaleValue = 0>
						 			<#assign totalReturnValue = 0>
									<#assign totalNetValue = 0>
						 			<#assign totalVatValue = 0>
						 					<#list catProdDetails as catProd>
		                    				 <#assign product = delegator.findOne("Product", {"productId" : catProd.getKey()}, true)?if_exists/>
		                    				 <#assign saleValue = (catProd.getValue())?if_exists>
		                    				 <#assign returnValue = (productReturnMap[product.productId].get("returnPrice"))?if_exists>
		                    				  <#if returnValue?has_content>
		                    				 	<#assign netValue = (saleValue-returnValue)?if_exists>
		                    				 <#else>
		                    				 	<#assign netValue = (saleValue?if_exists)>
		                    				 </#if>
											<fo:table-row>
												<fo:table-cell>
			                            			<fo:block  keep-together="always" text-align="left" font-size="10pt" white-space-collapse="false">${product.brandName?if_exists}</fo:block>  
			                        			</fo:table-cell>
			                        			<fo:table-cell>
			                            			<fo:block  keep-together="always" text-align="left" font-size="10pt" white-space-collapse="false">${product.description?if_exists}</fo:block>  
			                        			</fo:table-cell>
			                        			<#assign totalNetValue = (totalNetValue + netValue?if_exists)>
			                        			<fo:table-cell>
			                            			<fo:block  keep-together="always" text-align="right" font-size="10pt" white-space-collapse="false">${netValue?if_exists?string("#0.00")}</fo:block>  
			                        			</fo:table-cell>
			                        			<fo:table-cell>
			                            			<fo:block  keep-together="always" text-align="right" font-size="10pt" white-space-collapse="false"><#if (totalDays!=0)>${(netValue/totalDays)?if_exists?string("#0.00")}</#if></fo:block>  
			                        			</fo:table-cell>
			                        		</fo:table-row>
			                        		<#assign serialNo = serialNo+1>
			                        	</#list>
			                        	<fo:table-row>
		                        			<fo:table-cell>
		                            			<fo:block font-size="10pt">---------------------------------------------------------------------------------------------------------------------------------</fo:block>
		                        			</fo:table-cell>
		                        	</fo:table-row>
	                    			<fo:table-row>
	                        			<fo:table-cell>
	                            			<fo:block  keep-together="always" text-align="left" font-size="10pt" white-space-collapse="false"></fo:block>  
	                        			</fo:table-cell>
	                        			<fo:table-cell>
	                            			<fo:block  keep-together="always" font-weight="bold" text-align="left" font-size="10pt" white-space-collapse="false">&lt;SUB TOTAL&gt;</fo:block>  
	                        			</fo:table-cell>
	                        			<#assign grandTotalNetValue = (grandTotalNetValue + totalNetValue?if_exists)>
	                        			<fo:table-cell>
	                            			<fo:block  keep-together="always" font-weight="bold" text-align="right" font-size="10pt" white-space-collapse="false">${totalNetValue?if_exists?string("#0.00")}</fo:block>  
	                        			</fo:table-cell>
	                        			<fo:table-cell>
	                            			<fo:block  keep-together="always" font-weight="bold" text-align="right" font-size="10pt" white-space-collapse="false"><#if (totalDays!=0)>${(totalNetValue/totalDays)?if_exists?string("#0.00")}</#if></fo:block>  
	                        			</fo:table-cell>
		                        	</fo:table-row>
		                        	<fo:table-row>
	                        			<fo:table-cell>
	                            			<fo:block font-size="10pt">---------------------------------------------------------------------------------------------------------------------------------</fo:block>
	                        			</fo:table-cell>
		                        </fo:table-row>
		                        </#list>
			                    </#list>
		                        <fo:table-row>
                        			<fo:table-cell>
	                            			<fo:block  keep-together="always" text-align="left" font-size="10pt" white-space-collapse="false"></fo:block>  
	                        			</fo:table-cell>
                        			<fo:table-cell>
                            			<fo:block  keep-together="always" font-weight="bold" text-align="left" font-size="12pt" white-space-collapse="false">GRAND TOTAL</fo:block>  
                        			</fo:table-cell>
                        			<fo:table-cell>
                            			<fo:block  keep-together="always" font-weight="bold" text-align="right" font-size="12pt" white-space-collapse="false">${grandTotalNetValue?if_exists?string("#0.00")}</fo:block>  
                        			</fo:table-cell>
                        			<fo:table-cell>
                            			<fo:block  keep-together="always" font-weight="bold" text-align="right" font-size="12pt" white-space-collapse="false"><#if (totalDays!=0)>${(grandTotalNetValue/totalDays)?if_exists?string("#0.00")}</#if></fo:block>  
                        			</fo:table-cell>
		                        </fo:table-row>
		                        <fo:table-row>
                        			<fo:table-cell>
                            			<fo:block font-size="10pt">---------------------------------------------------------------------------------------------------------------------------------</fo:block>
                        			</fo:table-cell>
		                        </fo:table-row>
		                        <fo:table-row>
                        			<fo:table-cell>
	                            			<fo:block  keep-together="always" text-align="left" font-size="10pt" white-space-collapse="false"></fo:block>  
	                        			</fo:table-cell>
                        			<fo:table-cell>
                            			<fo:block  keep-together="always" font-weight="bold" text-align="left" font-size="12pt" white-space-collapse="false">Average Milk Sales Per Day</fo:block>  
                        			</fo:table-cell>
                        			<fo:table-cell>
                            			<fo:block  keep-together="always" font-weight="bold" text-align="right" font-size="12pt" white-space-collapse="false"></fo:block>  
                        			</fo:table-cell>
                        			<fo:table-cell>
                            			<fo:block  keep-together="always" font-weight="bold" text-align="right" font-size="12pt" white-space-collapse="false"><#if (totalDays!=0)>${(milkSaleTotal/totalDays)?if_exists?string("#0.00")}</#if></fo:block>  
                        			</fo:table-cell>
		                        </fo:table-row>
		                        <fo:table-row>
                        			<fo:table-cell>
	                            			<fo:block  keep-together="always" text-align="left" font-size="10pt" white-space-collapse="false"></fo:block>  
	                        			</fo:table-cell>
                        			<fo:table-cell>
                            			<fo:block  keep-together="always" font-weight="bold" text-align="left" font-size="12pt" white-space-collapse="false">Average Curd Sales Per Day</fo:block>  
                        			</fo:table-cell>
                        			<fo:table-cell>
                            			<fo:block  keep-together="always" font-weight="bold" text-align="right" font-size="12pt" white-space-collapse="false"></fo:block>  
                        			</fo:table-cell>
                        			<fo:table-cell>
                            			<fo:block  keep-together="always" font-weight="bold" text-align="right" font-size="12pt" white-space-collapse="false"><#if (totalDays!=0)>${(curdSaleTotal/totalDays)?if_exists?string("#0.00")}</#if></fo:block>  
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
	                            			<fo:block  keep-together="always" text-align="left" font-size="10pt" white-space-collapse="false"></fo:block>  
	                        			</fo:table-cell>
                        			<fo:table-cell>
                            			<fo:block  keep-together="always" font-weight="bold" text-align="left" font-size="12pt" white-space-collapse="false"></fo:block>  
                        			</fo:table-cell>
                        			<fo:table-cell>
                            			<fo:block  keep-together="always" font-weight="bold" text-align="right" font-size="12pt" white-space-collapse="false"></fo:block>  
                        			</fo:table-cell>
                        			<fo:table-cell>
                            			<fo:block  keep-together="always" font-weight="bold" text-align="right" font-size="12pt" white-space-collapse="false">Authorised Signatory</fo:block>    
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