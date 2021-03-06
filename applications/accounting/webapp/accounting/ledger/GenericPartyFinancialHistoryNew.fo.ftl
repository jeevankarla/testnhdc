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
            <#--><fo:simple-page-master master-name="main" page-height="12in" page-width="10in"  margin-left=".3in" margin-right=".1in" margin-top=".5in"> -->
              <fo:simple-page-master master-name="main" page-height="12in" page-width="10.5in"
                     margin-left=".3in" margin-right=".1in">
                <fo:region-body margin-top="1.7in"/>
                <fo:region-before extent="1in"/>
                <fo:region-after extent="1in"/>
            </fo:simple-page-master>
        </fo:layout-master-set>
        ${setRequestAttribute("OUTPUT_FILENAME", "PartywiseLedgerAbstract.pdf")}
        <#if errorMessage?has_content>
	<fo:page-sequence master-reference="main">
	<fo:flow flow-name="xsl-region-body" font-family="Helvetica">
	   <fo:block font-size="14pt">
	           ${errorMessage}.
        </fo:block>
	</fo:flow>
	</fo:page-sequence>	
	<#else>
       <#if partyTotalCBMap?has_content>
       <#assign partyLedgerList = partyDayWiseDetailMap.entrySet()>
      
		        <fo:page-sequence master-reference="main" font-size="12pt">	
		        	<fo:static-content flow-name="xsl-region-before" font-family="Courier,monospace">
		        	<#assign roId = parameters.division>
              	 		<#assign roHeader = roId+"_HEADER">
              	 		<#assign roSubheader = roId+"_HEADER01">
		        	    <#assign reportHeader = delegator.findOne("TenantConfiguration", {"propertyTypeEnumId" : "COMPANY_HEADER","propertyName" : roHeader}, true)>
                        <#assign reportSubHeader = delegator.findOne("TenantConfiguration", {"propertyTypeEnumId" : "COMPANY_HEADER","propertyName" : roSubheader}, true)>
		        		<fo:block  text-align="center"  keep-together="always"  white-space-collapse="false" font-weight="bold">NATIONAL HANDLOOM DEVELOPMENT CORPORATION LTD.</fo:block>
		        		<fo:block text-align="center" font-size="13pt" white-space-collapse="false" font-weight="bold">&#160;${reportHeader.description?if_exists}</fo:block>
                    	<fo:block text-align="center" font-size="12pt" keep-together="always"  white-space-collapse="false" font-weight="bold">&#160;${reportSubHeader.description?if_exists}</fo:block>
                    	<fo:block text-align="center" font-size="12pt" keep-together="always"  white-space-collapse="false" font-weight="bold">&#160;SUBLEDGER REPORT FOR THE PERIOD FROM: ${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(fromDate, "dd/MM/yyyy")} to ${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(thruDate, "dd/MM/yyyy")} </fo:block>
              			<fo:block font-size="11pt" text-align="left">============================================================================================================</fo:block> 
	                    <#assign  partyName="">
            			<#if parameters.partyId?exists>
            			<#assign partyName = Static["org.ofbiz.party.party.PartyHelper"].getPartyName(delegator, parameters.partyId, false)>
            			</#if>
            			<fo:block text-align="left" font-size="12pt" keep-together="always" font-family="Courier,monospace" white-space-collapse="false" font-weight="bold">&#160; PARTY CODE: ${parameters.partyId?if_exists}                  PARTY NAME:  ${partyName?if_exists}                  </fo:block>
              			<fo:block font-size="11pt" text-align="left">------------------------------------------------------------------------------------------------------------</fo:block>  
            			<#--><fo:block text-align="left" font-size="12pt" keep-together="always" font-family="Courier,monospace" white-space-collapse="false">&#160;                INVOICE-INFO                          PAYMENT-INFO       </fo:block> -->
		        	</fo:static-content>
		            <fo:flow flow-name="xsl-region-body"   font-family="Courier,monospace">		
					<fo:block>
                 	<fo:table>
                    <fo:table-column column-width="90pt"/>
            		<fo:table-column column-width="150pt"/> 
            		<fo:table-column column-width="130pt"/>	
            		<fo:table-column column-width="70pt"/>
            		<fo:table-column column-width="70pt"/>
            		<fo:table-column column-width="100pt"/>
            		<fo:table-column column-width="100pt"/>
            		<fo:table-column column-width="90pt"/>	
            		<fo:table-column column-width="90pt"/>	
                    <fo:table-body>
                    <fo:table-row>
		                    <fo:table-cell>
				            	<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false">DATE</fo:block>  
				            </fo:table-cell>
				            <fo:table-cell>
				            	<fo:block   text-align="left" font-size="12pt" white-space-collapse="false">Business Transactions</fo:block> 
				            </fo:table-cell>
				             <fo:table-cell>
				            	<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false">Transaction</fo:block> 
				            	<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false">&#160;&#160;Type</fo:block>   
				            </fo:table-cell>
				             <fo:table-cell>
				            	<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false">Invoice</fo:block> 
				            	<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false">&#160;&#160;Id</fo:block>   
				            </fo:table-cell>
				            <#-->
				            <fo:table-cell>
		                    	<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false">Type</fo:block>  
		                    </fo:table-cell> -->
				             <fo:table-cell>
				            	<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false">Payment</fo:block> 
				            	<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false">&#160;&#160;Id</fo:block>  
				            </fo:table-cell>
				             <fo:table-cell>
				            	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">Debit</fo:block>  
				            </fo:table-cell>
				            <fo:table-cell>
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">Credit</fo:block>  
		                    </fo:table-cell>
                        </fo:table-row>
                       </fo:table-body>
                </fo:table>
              </fo:block> 
              <fo:block font-size="11pt" text-align="left">-------------------------------------------------------------------------------------------------------------</fo:block>		        	
            	<fo:block>
                 	<fo:table>
                     <fo:table-column column-width="90pt"/>
            		<fo:table-column column-width="150pt"/> 	
            		<fo:table-column column-width="100pt"/>
            		<fo:table-column column-width="70pt"/>
            		<fo:table-column column-width="100pt"/>
            		<fo:table-column column-width="100pt"/>
            		<fo:table-column column-width="90pt"/>	
            		<fo:table-column column-width="90pt"/>
                    <fo:table-body>
                     <fo:table-row>
                     <fo:table-cell font-weight="bold">
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false"></fo:block>  
		                    </fo:table-cell>
		                    <fo:table-cell number-columns-spanned="4" font-weight="bold">
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">***Opening Balance:</fo:block>  
		                    </fo:table-cell>
				             <fo:table-cell>
				            	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">${partyOBMap.get("debitValue")?if_exists?string("#0.00")}</fo:block>  
				            </fo:table-cell>
				            <fo:table-cell>
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">${partyOBMap.get("creditValue")?if_exists?string("#0.00")}</fo:block>  
		                    </fo:table-cell>
                      </fo:table-row>
                      <fo:table-row>
	                    	<fo:table-cell>
	                    	   <fo:block font-size="11pt" text-align="left">&#160;</fo:block>
		                	</fo:table-cell>
					  </fo:table-row>
                     <#list partyLedgerList as eachDayPartyLedger>
                     <#assign dateStr = eachDayPartyLedger.getKey()>  
                      <#assign invoiceList = eachDayPartyLedger.getValue().get("invoiceList")?if_exists>
                        <#assign paymentList = eachDayPartyLedger.getValue().get("paymentList")?if_exists>
                        <#if invoiceList?has_content>
                        <#list invoiceList as eachDateDetail>
                        <fo:table-row>
		                    <fo:table-cell>
				            	<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false">${eachDateDetail.get("date")?if_exists}</fo:block>  
				            </fo:table-cell>
				             <fo:table-cell>
				            	<fo:block   text-align="left"  >${eachDateDetail.get("description")?if_exists}</fo:block>  
				            </fo:table-cell>
				            <#assign Invoice = delegator.findOne("Invoice", {"invoiceId" : eachDateDetail.get("invoiceId")}, true)?if_exists/> 
				            <#assign InvoiceType = "">
				            <#if Invoice.get("invoiceTypeId")?has_content>
				            <#assign InvoiceType = delegator.findOne("InvoiceType", {"invoiceTypeId" : Invoice.get("invoiceTypeId")}, true)?if_exists/> 
				            </#if>
				             <fo:table-cell>
				            	<fo:block   text-align="right"  ><#if InvoiceType?has_content>${InvoiceType.get("description")?if_exists}<#else>&#160;</#if></fo:block>  
				            </fo:table-cell>
				             <fo:table-cell>
				            	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">${eachDateDetail.get("invoiceId")?if_exists}</fo:block>  
				            </fo:table-cell>
				            <#-->
				            <fo:table-cell>
				            	<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false">${eachDateDetail.get("vchrCode")?if_exists}</fo:block>  
				            </fo:table-cell> -->
				             <fo:table-cell>
				            	<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false">&#160;</fo:block>  
				            </fo:table-cell>
				            <fo:table-cell>
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">${eachDateDetail.get("debitValue")?if_exists?string("#0.00")}</fo:block>  
		                    </fo:table-cell>
		                     <fo:table-cell>
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">${eachDateDetail.get("creditValue")?if_exists?string("#0.00")}</fo:block>  
		                    </fo:table-cell>
                        </fo:table-row>
                         </#list>
                        </#if>
                        <#-- payment Population  -->
                       <#if paymentList?has_content>
                        <#list paymentList as eachDateDetail>
                        <fo:table-row>
		                    <fo:table-cell>
				            	<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false">${eachDateDetail.get("date")?if_exists}</fo:block>  
				            </fo:table-cell>
				             <fo:table-cell>
				            	<fo:block   text-align="left"  >${eachDateDetail.get("description")?if_exists}</fo:block>  
				            </fo:table-cell>
				              <#assign payment = delegator.findOne("Payment", {"paymentId" : eachDateDetail.get("paymentId")}, true)?if_exists/> 
				             
				               <#assign paymentMethodType = "">
				               <#if payment.get("paymentMethodTypeId")?has_content>
				               <#assign paymentMethodType = delegator.findOne("PaymentMethodType", {"paymentMethodTypeId" : payment.get("paymentMethodTypeId")}, true)?if_exists/> 
				              </#if>
				             <fo:table-cell>
				            	<fo:block   text-align="right"  ><#if paymentMethodType?has_content>${paymentMethodType.get("description")?if_exists}<#else>&#160;</#if></fo:block>  
				            </fo:table-cell>
				             <fo:table-cell>
				            	<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false">${eachDateDetail.get("instrumentNo")?if_exists}</fo:block>  
				            </fo:table-cell>
				            <#-->
				            <fo:table-cell>
				            	<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false">${eachDateDetail.get("vchrCode")?if_exists}</fo:block>  
				            </fo:table-cell> -->
		                    <fo:table-cell>
				            	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">${eachDateDetail.get("paymentId")?if_exists}</fo:block>  
				            </fo:table-cell>
				              <fo:table-cell>
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">${eachDateDetail.get("debitValue")?if_exists?string("#0.00")}</fo:block>  
		                    </fo:table-cell>
		                     <fo:table-cell>
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">${eachDateDetail.get("creditValue")?if_exists?string("#0.00")}</fo:block>  
		                    </fo:table-cell>
                        </fo:table-row>
                         </#list>
                        </#if>
                       
                        </#list>
 						<fo:table-row>
	                    	<fo:table-cell>
	                    	   <fo:block font-size="11pt" text-align="left">&#160;</fo:block>
		                	</fo:table-cell>
						</fo:table-row>
                        <fo:table-row>
                        <fo:table-cell font-weight="bold">
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false"></fo:block>  
		                    </fo:table-cell>
		                    <fo:table-cell number-columns-spanned="4" font-weight="bold">
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">***Transaction Total:</fo:block>  
		                    </fo:table-cell>
		                    <#assign partyDebitTrans=0>
		                    <#assign partyCreditTrans=0>
							<#assign partyDebitTrans=partyTotalTrasnsMap.get("debitValue")?if_exists>
							<#assign partyCreditTrans=partyTotalTrasnsMap.get("creditValue")?if_exists>
							 <#assign partyDebitTrans=partyDebitTrans+partyOBMap.get("debitValue")?if_exists>
		                    <#assign partyCreditTrans=partyCreditTrans+partyOBMap.get("creditValue")?if_exists>
				             <fo:table-cell>
				            	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">${partyDebitTrans?string("#0.00")}</fo:block>  
				            </fo:table-cell>
				            <fo:table-cell>
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">${partyCreditTrans?string("#0.00")}</fo:block>  
		                    </fo:table-cell>
                         </fo:table-row>
                          <fo:table-row>
	                    	<fo:table-cell>
	                    	   <fo:block font-size="11pt" text-align="left">&#160;</fo:block>
		                	</fo:table-cell>
						</fo:table-row>
                         <fo:table-row>
                         <fo:table-cell font-weight="bold">
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false"></fo:block>  
		                    </fo:table-cell>
		                    <fo:table-cell number-columns-spanned="4" font-weight="bold">
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">***Closing Balance:</fo:block>  
		                    </fo:table-cell>
				             <fo:table-cell>
				            	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">${partyTotalCBMap.get("debitValue")?if_exists?string("#0.00")}</fo:block>  
				            </fo:table-cell>
				            <fo:table-cell>
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">${partyTotalCBMap.get("creditValue")?if_exists?string("#0.00")}</fo:block>  
		                    </fo:table-cell>
                         </fo:table-row>
						<fo:table-row>
	                    	<fo:table-cell>
	                    	    <fo:block font-size="11pt" text-align="left">================================================================================================================</fo:block>
		                	</fo:table-cell>
						</fo:table-row>
						
					 <#if totalPartyDayWiseFinHistryOpeningBal?has_content>	
						<fo:table-row>
                            <fo:table-cell >        
                                     <fo:block page-break-after="always"></fo:block>
                             </fo:table-cell>
                        </fo:table-row>
					 <#-- <fo:table-row>
							  <fo:table-cell>
									 <fo:block text-align="center"  keep-together="always"  font-size="12pt" white-space-collapse="false" font-weight="bold">&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;Financial Account Transaction Details</fo:block>  
							  </fo:table-cell>
					</fo:table-row> -->	
					 <fo:table-row>
                            <fo:table-cell >
											<fo:block>
											<fo:table>
						                    <fo:table-column column-width="90pt"/>
						            		<fo:table-column column-width="100pt"/>
						            		<fo:table-column column-width="70pt"/>
						            		<fo:table-column column-width="100pt"/> 	
							            	<fo:table-column column-width="90pt"/>
						            		<fo:table-column column-width="75pt"/>
						            		<fo:table-column column-width="70pt"/>
						            		<fo:table-column column-width="90pt"/>
						            		<fo:table-column column-width="90pt"/>
						            		<fo:table-column column-width="90pt"/>	
						            		<fo:table-column column-width="90pt"/>
						                    <fo:table-body>
						                    <fo:table-row>
								                    <fo:table-cell>
										            	<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false">DATE</fo:block>  
										            </fo:table-cell>
										            <fo:table-cell number-columns-spanned="2">
										            	<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false">Financial </fo:block>  
										            	<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false">&#160;Transactions</fo:block>   
										            </fo:table-cell>
										            
										             <fo:table-cell>
										            	<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false">Transaction </fo:block>  
										            	<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false">&#160;Type</fo:block>   
										            </fo:table-cell>
										           
										             <fo:table-cell>
										            	<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false">FinAccount</fo:block> 
										            	<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false">&#160;&#160;TransId</fo:block>   
										            </fo:table-cell>
										            <#-->
									            <fo:table-cell>
							                    	<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false">Type</fo:block>  
							                    </fo:table-cell> -->
									             <fo:table-cell>
									            	<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false">Payment</fo:block> 
									            	<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false">&#160;&#160;Id</fo:block>  
									            </fo:table-cell>
									             <fo:table-cell>
									            	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">Debit</fo:block>  
									            </fo:table-cell>
									            <fo:table-cell>
							                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">Credit</fo:block>  
							                    </fo:table-cell>
					                        </fo:table-row>
					                       </fo:table-body>
					                </fo:table>
					              </fo:block> 
					              	  </fo:table-cell>
					</fo:table-row>
					<fo:table-row>
                            <fo:table-cell>
					
					              <fo:block font-size="11pt" text-align="left">-------------------------------------------------------------------------------------------------------------------------</fo:block>		        	
					            	<fo:block>
					                 	<fo:table>
					                     <fo:table-column column-width="90pt"/>
					            		<fo:table-column column-width="100pt"/> 
					            		<fo:table-column column-width="70pt"/>	
						            	<fo:table-column column-width="90pt"/>
					            		<fo:table-column column-width="75pt"/>
					            		<fo:table-column column-width="70pt"/>
					            		<fo:table-column column-width="90pt"/>
					            		<fo:table-column column-width="90pt"/>
					            		<fo:table-column column-width="90pt"/>	
					            		<fo:table-column column-width="90pt"/>
					                    <fo:table-body>		
					                    	<#assign partyDayWiseFin = totalPartyDayWiseFinHistryOpeningBal.entrySet()>
					                    	<#assign FinTransDebitTotal = 0>
					                    	<#assign FinTransCreditTotal = 0>
											<#list partyDayWiseFin as partyDayWiseFinTrans>
											<#assign FinOpenDebit=0>
											<#assign FinOpenCredit=0>
											<#assign FinTransDebit=0>
											<#assign FinTransCredit=0>
											<#assign FinOpenBal = partyDayWiseFinTrans.getValue().get("openingBal")>
											<#assign FinCloseBal = partyDayWiseFinTrans.getValue().get("FinClsBalMap")>	
											<#assign FinTransTotal = partyDayWiseFinTrans.getValue().get("FinTransMap")>	
											<#assign FinTransDebit =FinTransTotal.get("debitValue")+FinOpenBal.debitValue?if_exists>
											<#assign FinTransDebitTotal = FinTransDebitTotal+FinCloseBal.get("debitValue")>												
											<#assign FinTransCredit =FinTransTotal.get("creditValue")+FinOpenBal.creditValue?if_exists>
											<#assign FinTransCreditTotal = FinTransCreditTotal+FinCloseBal.get("creditValue")>
											<#assign FinDayWiseTrans = partyDayWiseFinTrans.getValue().get("partyDayWiseFinHistryMap").entrySet()>
												
										
					                     <fo:table-row>
					                         <fo:table-cell font-weight="bold">
		                    	                <fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false"></fo:block>  
		                                        </fo:table-cell>
												 <fo:table-cell font-weight="bold">
							                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false"></fo:block>  
							                    </fo:table-cell>
							                    <fo:table-cell number-columns-spanned="4" font-weight="bold">
							                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">***Opening Balance:</fo:block>  
							                    </fo:table-cell>
									             <fo:table-cell>
									            	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">${FinOpenBal.debitValue?if_exists?string("#0.00")} </fo:block></fo:table-cell>
									            <fo:table-cell>
							                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">${FinOpenBal.creditValue?if_exists?string("#0.00")} </fo:block>  
							                    </fo:table-cell>
					                      </fo:table-row>
					                      <fo:table-row>
						                    	<fo:table-cell>
						                    	   <fo:block font-size="11pt" text-align="left">&#160;</fo:block>
							                	</fo:table-cell>
										  </fo:table-row>
											
											<#assign finAccount = delegator.findOne("FinAccount", {"finAccountId" : partyDayWiseFinTrans.getKey()}, true)?if_exists/> 
											<#assign finAccountType = delegator.findOne("FinAccountType", {"finAccountTypeId" : finAccount.finAccountTypeId}, true)?if_exists/> 
											<fo:table-row>
							                    <fo:table-cell>
									            	<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false" font-weight="bold"> ${finAccountType.description?if_exists}[${partyDayWiseFinTrans.getKey()}]</fo:block>  
									            	<fo:block   text-align="left"  ></fo:block>  
									            </fo:table-cell>
											</fo:table-row>	
												<#list FinDayWiseTrans as FinTransDaywise>
												<#assign FinAccTrans = FinTransDaywise.getValue()>
										<#list FinAccTrans as FinTransDay>
					                        	<fo:table-row>
							                    <fo:table-cell>
									            	<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false">${FinTransDaywise.getKey()}</fo:block>  
									            </fo:table-cell>
									             
												<#assign FinAccTransId ="">
												<#assign FinAccTransId =FinTransDay.get("instrumentNo")?if_exists>
									            <#assign finAccountTrns = delegator.findOne("FinAccountTrans", {"finAccountTransId" : FinAccTransId}, true)?if_exists/> 
  													<fo:table-cell number-columns-spanned="2">
										            	<fo:block  text-align="left" font-size="12pt" white-space-collapse="false">${finAccountTrns.comments?if_exists}</fo:block> 
										            </fo:table-cell>
										            
										            <#assign FinAccountTransType = "">
										            <#if finAccountTrns.get("finAccountTransTypeId")?has_content>
				                                    <#assign FinAccountTransType = delegator.findOne("FinAccountTransType", {"finAccountTransTypeId" : finAccountTrns.get("finAccountTransTypeId")}, true)?if_exists/> 
									                </#if>
									            
									             <fo:table-cell>
									            	<fo:block   text-align="right"  ><#if FinAccountTransType?has_content>${FinAccountTransType.get("description")?if_exists}<#else>&#160;</#if></fo:block>  
									            </fo:table-cell>
									             <fo:table-cell>
									            	<fo:block   text-align="right" font-size="12pt" white-space-collapse="false">${FinTransDay.get("instrumentNo")?if_exists}</fo:block>  
									            </fo:table-cell>
									            <#-->
									            <fo:table-cell>
									            	<fo:block  keep-together="always" text-align="left" font-size="12pt" white-space-collapse="false">${eachDateDetail.get("vchrCode")?if_exists}</fo:block>  
									            </fo:table-cell> -->
							                    <fo:table-cell>
									            	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">${FinTransDay.get("paymentId")?if_exists}</fo:block>  
									            </fo:table-cell>
									              <fo:table-cell>
							                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">${FinTransDay.get("debitValue")?if_exists?string("#0.00")}</fo:block>  
							                    </fo:table-cell>
							                     <fo:table-cell>
							                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">${FinTransDay.get("creditValue")?if_exists?string("#0.00")}</fo:block>  
							                    </fo:table-cell>
					                        </fo:table-row>
											</#list>
										</#list>
					 <fo:table-row>
	                    	<fo:table-cell>
	                    	   <fo:block font-size="11pt" text-align="left">&#160;</fo:block>
		                	</fo:table-cell>
						</fo:table-row>
                        <fo:table-row>
                            <fo:table-cell font-weight="bold">
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false"></fo:block>  
		                    </fo:table-cell>
		                    <fo:table-cell>
				            	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false"></fo:block>  
				            </fo:table-cell>
		                    <fo:table-cell number-columns-spanned="4" font-weight="bold">
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">***Financial Transaction Total:</fo:block>  
		                    </fo:table-cell>
				             <fo:table-cell>
				            	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">${FinTransDebit?string("#0.00")}</fo:block>  
				            </fo:table-cell>
				            <fo:table-cell>
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">${FinTransCredit?string("#0.00")}</fo:block>  
		                    </fo:table-cell>
                         </fo:table-row>
                          <fo:table-row>
	                    	<fo:table-cell>
	                    	   <fo:block font-size="11pt" text-align="left">&#160;</fo:block>
		                	</fo:table-cell>
						</fo:table-row>
                         <fo:table-row>
                         <fo:table-cell font-weight="bold">
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false"></fo:block>  
		                    </fo:table-cell>
							 <fo:table-cell>
				            	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false"></fo:block>  
				            </fo:table-cell>
		                    <fo:table-cell number-columns-spanned="4" font-weight="bold">
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">***Closing Balance:</fo:block>  
		                    </fo:table-cell>
				             <fo:table-cell>
				            	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">${FinCloseBal.get("debitValue")?if_exists?string("#0.00")}</fo:block>  
				            </fo:table-cell>
				            <fo:table-cell>
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">${FinCloseBal.get("creditValue")?if_exists?string("#0.00")}</fo:block>  
		                    </fo:table-cell>
                         </fo:table-row>
						 <fo:table-row>
	                    	<fo:table-cell>
	                    	    <fo:block font-size="11pt" text-align="left">======================================================111111111====================================================</fo:block>
		                	</fo:table-cell>
						 </fo:table-row>
							</#list>
					    </fo:table-body>
					    </fo:table>
					 </fo:block>
             	  </fo:table-cell>
                </fo:table-row>
                        
                        <#--
                        <fo:table-row>
	                    	<fo:table-cell>
	                    	   <fo:block font-size="11pt" text-align="left">=======================================================================================================</fo:block>
		                	</fo:table-cell>
						</fo:table-row> -->

						 <fo:table-row>
	                    	<fo:table-cell>
	                    	   <fo:block font-size="11pt" text-align="left">&#160;</fo:block>
		                	</fo:table-cell>
						</fo:table-row>
                        <fo:table-row>
                        <fo:table-cell font-weight="bold">
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false"></fo:block>  
		                    </fo:table-cell>
		                    <fo:table-cell number-columns-spanned="4" font-weight="bold">
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">***Total Business Transaction Total:</fo:block>  
		                    </fo:table-cell>
				             <fo:table-cell>
				            	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">${partyDebitTrans?string("#0.00")}</fo:block>  
				            </fo:table-cell>
				            <fo:table-cell>
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">${partyCreditTrans?string("#0.00")}</fo:block>  
		                    </fo:table-cell>
                         </fo:table-row>
						 <fo:table-row>
						 <fo:table-cell font-weight="bold">
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false"></fo:block>  
		                    </fo:table-cell>
		                    <fo:table-cell number-columns-spanned="4" font-weight="bold">
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">***Total Financial Transaction Total:</fo:block>  
		                    </fo:table-cell>
				             <fo:table-cell>
				            	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">${FinTransDebitTotal?string("#0.00")}</fo:block>  
				            </fo:table-cell>
				            <fo:table-cell>
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">${FinTransCreditTotal?string("#0.00")}</fo:block>  
		                    </fo:table-cell>
                         </fo:table-row>
                          <fo:table-row>
	                    	<fo:table-cell>
	                    	   <fo:block font-size="11pt" text-align="left">&#160;</fo:block>
		                	</fo:table-cell>
						</fo:table-row> 
						<fo:table-row>
	                    	<fo:table-cell>
	                    	    <fo:block font-size="11pt" text-align="left">----------------------------------------------------------------------------------------------------------</fo:block>
		                	</fo:table-cell>
						</fo:table-row>
						<#assign TotalDebitTrans=partyDebitTrans+FinTransDebitTotal>
						<#assign TotalCreditTrans=partyCreditTrans+FinTransCreditTotal>

                          <fo:table-row>
                          <fo:table-cell font-weight="bold">
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false"></fo:block>  
		                    </fo:table-cell>
		                    <fo:table-cell number-columns-spanned="4" font-weight="bold">
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">***Total Transaction Total:</fo:block>  
		                    </fo:table-cell>
				             <fo:table-cell>
				            	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">${TotalDebitTrans?if_exists?string("#0.00")}</fo:block>  
				            </fo:table-cell>
				            <fo:table-cell>
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">${TotalCreditTrans?if_exists?string("#0.00")}</fo:block>  
		                    </fo:table-cell>
                         </fo:table-row>
                          <fo:table-row>
	                    	<fo:table-cell>
	                    	   <fo:block font-size="11pt" text-align="left">&#160;</fo:block>
		                	</fo:table-cell>
						</fo:table-row>
						<#assign cB=0>
						<#assign cB=TotalDebitTrans-TotalCreditTrans>
						<#assign partyCBDebit=0>
						<#assign partyCBCredit=0>
						<#if 0 < cB >
							<#assign partyCBDebit=cB>
						<#else>
						<#assign partyCBCredit=(-1*cB)>
						</#if>
                         <fo:table-row>
                         <fo:table-cell font-weight="bold">
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false"></fo:block>  
		                    </fo:table-cell>
		                    <fo:table-cell number-columns-spanned="4" font-weight="bold">
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">***ToTal Closing Balance:</fo:block>  
		                    </fo:table-cell>
				             <fo:table-cell>
				            	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">${partyCBDebit?if_exists?string("#0.00")}</fo:block>  
				            </fo:table-cell>
				            <fo:table-cell>
		                    	<fo:block  keep-together="always" text-align="right" font-size="12pt" white-space-collapse="false">${partyCBCredit?if_exists?string("#0.00")}</fo:block>  
		                    </fo:table-cell>
                         </fo:table-row>
						<fo:table-row>
	                    	<fo:table-cell>
	                    	    <fo:block font-size="11pt" text-align="left">==========================================================================================================</fo:block>
		                	</fo:table-cell>
						</fo:table-row>
    					</#if>
						<fo:table-row>
	                    	<fo:table-cell>
	                    	    <fo:block font-size="11pt" text-align="right" keep-together="always" >&#160;</fo:block>
		                	</fo:table-cell>
						</fo:table-row>
						<fo:table-row>
	                    	<fo:table-cell>
	                    	    <fo:block font-size="11pt" text-align="right" keep-together="always" >&#160;</fo:block>
		                	</fo:table-cell>
						</fo:table-row>
						<fo:table-row>
	                    	<fo:table-cell>
	                    	    <fo:block font-size="11pt" text-align="right" keep-together="always" >&#160;</fo:block>
		                	</fo:table-cell>
						</fo:table-row>
						<fo:table-row>
	                    	<fo:table-cell>
	                    	    <fo:block font-size="11pt" text-align="right" keep-together="always" >&#160;</fo:block>
		                	</fo:table-cell>
						</fo:table-row>
						<fo:table-row>
	                    	<fo:table-cell>
	                    	    <fo:block font-size="11pt" text-align="right" keep-together="always" >&#160;</fo:block>
		                	</fo:table-cell>
						</fo:table-row>
						<fo:table-row>
						
						<fo:table-cell>
	                    	    <fo:block font-size="11pt" text-align="right" keep-together="always" >&#160;Prepared by </fo:block>
		                	</fo:table-cell>
		                	<fo:table-cell>
	                    	    <fo:block font-size="11pt" text-align="right" keep-together="always" >&#160;</fo:block>
		                	</fo:table-cell>
		                	<fo:table-cell>
	                    	    <fo:block font-size="11pt" text-align="right" keep-together="always" >&#160; Pre Auditor</fo:block>
		                	</fo:table-cell>
		                	<fo:table-cell>
	                    	    <fo:block font-size="11pt" text-align="right" keep-together="always" >&#160;</fo:block>
		                	</fo:table-cell>
		                	<fo:table-cell>
	                    	    <fo:block font-size="11pt" text-align="right" keep-together="always" >&#160;</fo:block>
		                	</fo:table-cell>
	                    	<fo:table-cell>
	                    	    <fo:block font-size="11pt" text-align="right" keep-together="always" > &#160;Manager (Finance)</fo:block>
		                	</fo:table-cell>
						</fo:table-row>
                    </fo:table-body>
                </fo:table>
              </fo:block> 		
			</fo:flow>
		</fo:page-sequence>
		</#if>
		
			<#if !(partyTotalCBMap?has_content) >
			<fo:page-sequence master-reference="main">
				<fo:flow flow-name="xsl-region-body" font-family="Helvetica">
			   		 <fo:block font-size="14pt">
			        	${uiLabelMap.NoOrdersFound}
			   		 </fo:block>
				</fo:flow>
			</fo:page-sequence>	
		</#if>   
 </#if>
 </fo:root>
</#escape>