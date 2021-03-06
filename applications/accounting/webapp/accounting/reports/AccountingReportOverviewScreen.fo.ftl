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
            margin-top="0.2in" margin-bottom=".3in" margin-left=".5in" margin-right=".1in">
        <fo:region-body margin-top="1in"/>
        <fo:region-before extent="1in"/>
        <fo:region-after extent="1in"/>        
    </fo:simple-page-master>   
</fo:layout-master-set>
   ${setRequestAttribute("OUTPUT_FILENAME", "accountingTrans.pdf")}
        <#if accountingTransEntries?has_content> 
        	<#assign acctgTransId = accountingTransEntries.acctgTransId?if_exists>
        	<#assign acctgTransTypeId = accountingTransEntries.acctgTransTypeId?if_exists>
        	<#assign description = accountingTransEntries.description?if_exists>
        	<#assign transactionDate = accountingTransEntries.transactionDate?if_exists>
        	<#assign isPosted = accountingTransEntries.isPosted?if_exists>
        	<#assign postedDate = accountingTransEntries.postedDate?if_exists>
        	<#assign scheduledPostingDate = accountingTransEntries.scheduledPostingDate?if_exists>
        	<#assign glJournalId = accountingTransEntries.glJournalId?if_exists>
        	<#assign glFiscalTypeId = accountingTransEntries.glFiscalTypeId?if_exists>
        	<#assign voucherRef = accountingTransEntries.voucherRef?if_exists>
        	<#assign voucherDate = accountingTransEntries.voucherDate?if_exists>
        	<#assign fixedAssetId = accountingTransEntries.fixedAssetId?if_exists>
        	<#assign groupStatusId = accountingTransEntries.groupStatusId?if_exists>
        	<#assign inventoryItemId = accountingTransEntries.inventoryItemId?if_exists>
        	<#assign physicalInventoryId = accountingTransEntries.physicalInventoryId?if_exists>
        	<#assign partyId = accountingTransEntries.partyId?if_exists>
        	<#assign roleTypeId = accountingTransEntries.roleTypeId?if_exists>
        	<#assign invoiceId = accountingTransEntries.invoiceId?if_exists>
        	<#assign paymentId = accountingTransEntries.paymentId?if_exists>
        	<#assign finAccountTransId = accountingTransEntries.finAccountTransId?if_exists>
        	<#assign shipmentId = accountingTransEntries.shipmentId?if_exists>
        	<#assign receiptId = accountingTransEntries.receiptId?if_exists>
        	<#assign workEffortId = accountingTransEntries.workEffortId?if_exists>
        	<#assign theirAcctgTransId = accountingTransEntries.theirAcctgTransId?if_exists>
        	<#assign createdByUserLogin = accountingTransEntries.createdByUserLogin?if_exists>
        	<#assign lastModifiedByUserLogin = accountingTransEntries.lastModifiedByUserLogin?if_exists>
        	
        	
           <fo:page-sequence master-reference="main" force-page-count="no-force" font-size="12pt" font-family="Courier,monospace">					
		    	<fo:static-content flow-name="xsl-region-before">
			    	<#assign roHeader = partyIdForAdd+"_HEADER">
              	 	<#assign roSubheader = partyIdForAdd+"_HEADER01">
		    	    <#assign reportHeader = delegator.findOne("TenantConfiguration", {"propertyTypeEnumId" : "COMPANY_HEADER","propertyName" : roHeader}, true)>
                    <#assign reportSubHeader = delegator.findOne("TenantConfiguration", {"propertyTypeEnumId" : "COMPANY_HEADER","propertyName" : roSubheader}, true)>   
                    <fo:block  keep-together="always" text-align="center" font-weight = "bold" font-family="Courier,monospace" white-space-collapse="false">NATIONAL HANDLOOM DEVELOPMENT CORPORATION LTD.</fo:block>
                    <fo:block  text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="12pt" >  ${reportHeader.description?if_exists} </fo:block>
					<fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-size="12pt" >  ${reportSubHeader.description?if_exists}  </fo:block>

                    <fo:block text-align="right" linefeed-treatment="preserve"></fo:block>
                    <#assign finAccountTransDetails = delegator.findOne("FinAccountTrans", {"finAccountTransId" : finAccountTransId}, false)?if_exists/>
                    <fo:block  keep-together="always" text-align="center" font-weight = "bold" font-family="Courier,monospace" white-space-collapse="false"><#--<#if finAccountTransDetails?has_content>${(finAccountTransDetails.finAccountTransTypeId)?replace("_"," ")}<#else>${acctgTransTypeId?if_exists?replace("_"," ")}</#if>--></fo:block>
                    <#--<fo:block text-align="left"  keep-together="always"  font-weight = "bold" white-space-collapse="false">Date:${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(nowTimestamp, "MMMM dd,yyyy")}&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;UserLogin : <#if userLogin?exists>${userLogin.userLoginId?if_exists}</#if>   </fo:block>-->
                    <fo:block>
                        <fo:table>
	                    <fo:table-column column-width="50%"/>
		                    <fo:table-body>
			                    <fo:table-row>	                    	
		                   			<fo:table-cell>
		                        		<fo:block text-align="right" font-weight="bold"><#if invSequenceNum?has_content>Sequence Number:<fo:inline font-weight="bold">${invSequenceNum?if_exists}</fo:inline><#elseif finAccntTransSequence?has_content>Sequence Number:<fo:inline font-weight="bold">${finAccntTransSequence?if_exists}</fo:inline><#else></#if></fo:block>  
		                   			</fo:table-cell>
			                    </fo:table-row>
		                    </fo:table-body>
	                    </fo:table>
                    </fo:block>
              		</fo:static-content>		
            		<fo:flow flow-name="xsl-region-body"   font-family="Courier,monospace">	
		            <fo:block text-align="left" font-weight="bold">Date:${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(nowTimestamp, "MMMM dd,yyyy")}</fo:block>  
              		<fo:block>---------------------------------------------------------------------------------------------</fo:block>
            		<fo:block><fo:table>
                    <fo:table-column column-width="50%"/>
                    <fo:table-column column-width="70%"/>
                    <fo:table-body>
                    <fo:table-row>
            				<#if acctgTransId?has_content>
            				<fo:table-cell>
                        		<fo:block  text-align="left">Acctg Trans Id:${acctgTransId?if_exists}</fo:block>  
                   			</fo:table-cell>
                   			<#else>
                   			<fo:table-cell>
                        		<fo:block  text-align="left"  font-weight = "bold"></fo:block>  
                   			</fo:table-cell>
                   			</#if>
                   			<#if reportTypeFlag?has_content>
                    				<#if finAccountTransDetails?has_content>
	            				<fo:table-cell>
	                        		<fo:block  keep-together="always" text-align="left">Acctg Trans Type Id:${finAccountTransDetails.finAccountTransTypeId?if_exists}</fo:block>  
	                   			</fo:table-cell>
	                   			<#else>
	                   			<fo:table-cell>
	                        		<fo:block  text-align="left"  font-weight = "bold"></fo:block>  
	                   			</fo:table-cell>
	                   			</#if>
	                   		<#else>		
	                   			<#if acctgTransTypeId?has_content>
	            				<fo:table-cell>
	                        		<fo:block  keep-together="always" text-align="left"><!--Acctg Trans Type Id:${acctgTransTypeId?if_exists}--></fo:block>  
	                   			</fo:table-cell>
	                   			<#else>
	                   			<fo:table-cell>
	                        		<fo:block  text-align="left"  font-weight = "bold"></fo:block>  
	                   			</fo:table-cell>
	                   			</#if>
	                   		</#if>
                    </fo:table-row>	
                    <fo:table-row>	
                     <#if invoiceId?has_content>
                    		<fo:table-cell>
                            		<fo:block  keep-together="always" text-align="left">Invoice Id:${invoiceId?if_exists}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  font-weight = "bold"></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                       		<#if paymentId?has_content>
                       		<fo:table-cell>
                            		<fo:block  text-align="left" keep-together="always">Payment Id:${paymentId?if_exists}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  font-weight = "bold"></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                     </fo:table-row>
                     <fo:table-row>
                     			
                       			<#if finAccountTransId?has_content>
                    		<fo:table-cell>
                            		<fo:block  keep-together="always" text-align="left">Fin Account Trans Id:${finAccountTransId?if_exists}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  font-weight = "bold"></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                       			<#if description?has_content>
                				<fo:table-cell>
                            		<fo:block  text-align="left" wrap-option="wrap" >Description:${description?if_exists}</fo:block>  
                       			</fo:table-cell>
                       			<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  font-weight = "bold"></fo:block>  
                       			</fo:table-cell>
                       			</#if>
                    </fo:table-row>
                    <fo:table-row>
                    		<#if transactionDate?has_content>
                				<fo:table-cell>
                            		<fo:block  keep-together="always" text-align="left" >Transaction Date:${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(transactionDate, "dd-MM-yyyy")}</fo:block>  
                       			</fo:table-cell>
                       			<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  font-weight = "bold"></fo:block>  
                       			</fo:table-cell>
                       			</#if>
                    
                    		<#if isPosted?has_content>
                				<fo:table-cell>
                            		<fo:block  text-align="left"  keep-together="always"><#--Is Posted:${isPosted?if_exists}--></fo:block>  
                       			</fo:table-cell>
                       			<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       			</#if>
                    </fo:table-row>
                    
                    <#--<#if postedDate?has_content>
                                <fo:table-row>
                				<fo:table-cell>
                            		<fo:block  text-align="left"  keep-together="always">Posted Date:${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(postedDate, "dd-MM-yyyy HH:mm:ss")}</fo:block>  
                       			</fo:table-cell>
                       			<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       			</fo:table-row>
                       			</#if>-->
                    <#if scheduledPostingDate?has_content>
                   		<fo:table-row>	
                    		<fo:table-cell>
                            		<fo:block  keep-together="always" text-align="left" >Schd Posting Date:${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(scheduledPostingDate, "dd-MM-yyyy HH:mm:ss")}</fo:block>  
                       		</fo:table-cell>
                       	</fo:table-row>
                    </#if>
                       		
                     
                     <fo:table-row>	
                     <#if glJournalId?has_content>
                       		<fo:table-cell>
                            		<fo:block  text-align="left"  keep-together="always"><#--GL Journal Id:${glJournalId?if_exists}--></fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                     
                     <#--<#if glFiscalTypeId?has_content>
                    		<fo:table-cell>
                            		<fo:block  keep-together="always" text-align="left" >Fiscal GL Type Id:${glFiscalTypeId?if_exists}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>-->
                       		
                     </fo:table-row>
                     <fo:table-row>	
                     <#if voucherRef?has_content>
                       		<fo:table-cell>
                            		<fo:block  text-align="left"  keep-together="always">Voucher Ref:${voucherRef?if_exists}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                     <#if voucherDate?has_content>
                    		<fo:table-cell>
                            		<fo:block  keep-together="always" text-align="left" >Voucher Date:${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(voucherDate, "dd-MM-yyyy")}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                     </fo:table-row>
                     <fo:table-row>	
                     		<#if groupStatusId?has_content>
                    		<fo:table-cell>
                            		<fo:block  keep-together="always" text-align="left" >Group Status Id:${groupStatusId?if_exists}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                       		<#if fixedAssetId?has_content>
                       		<fo:table-cell>
                            		<fo:block  text-align="left"  keep-together="always">Fixed Asset Id:${fixedAssetId?if_exists}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                     </fo:table-row>
                     <fo:table-row>	
                     <#if inventoryItemId?has_content>
                    		<fo:table-cell>
                            		<fo:block  keep-together="always" text-align="left" >Inventory Item Id:${inventoryItemId?if_exists}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                       		<#if physicalInventoryId?has_content>
                       		<fo:table-cell>
                            		<fo:block  text-align="left"  keep-together="always">Physical Inventory Id:${physicalInventoryId?if_exists}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                     </fo:table-row>
                     <fo:table-row>	
                     <#if partyId?has_content>
                     		<#assign partyFullName = Static["org.ofbiz.party.party.PartyHelper"].getPartyName(delegator, partyId?if_exists, false)>
                    		<fo:table-cell>
                            		<fo:block text-align="left" wrap-option="wrap"><#if parameters.reportFlag?has_content && parameters.reportFlag =="Y">&#160;<#else>Party: <fo:inline font-weight="bold">${partyFullName?if_exists}[${partyId?if_exists}]</fo:inline></#if></fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                       		<#--<#if roleTypeId?has_content>
                       		<fo:table-cell>
                            		<fo:block  text-align="left"  keep-together="always">Role Type Id:${roleTypeId?if_exists}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>-->
                       		<#assign invoiceDetails = delegator.findOne("Invoice", {"invoiceId" : invoiceId}, false)?if_exists/>
                       		<#if invoiceDetails?has_content && invoiceDetails.referenceNumber?has_content>
                     		<fo:table-cell>
                            		<fo:block  text-align="left"  keep-together="always"><#--Party Invoice No:<fo:inline font-weight="bold">${invoiceDetails.referenceNumber?if_exists}</fo:inline>--></fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                     </fo:table-row>
                     
                     <fo:table-row>	
                       		<#if shipmentId?has_content>
                       		<fo:table-cell>
                            		<fo:block  text-align="left"  keep-together="always">Shipment Id:${shipmentId?if_exists}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                       		<#if receiptId?has_content>
                    		<fo:table-cell>
                            		<fo:block  keep-together="always" text-align="left" >Receipt Id:${receiptId?if_exists}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                       		
                     </fo:table-row>
                     <fo:table-row>	
                     
                       		<#if workEffortId?has_content>
                       		<fo:table-cell>
                            		<fo:block  text-align="left"  keep-together="always">Work Effort Id:${workEffortId?if_exists}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                       		<#if theirAcctgTransId?has_content>
                    		<fo:table-cell>
                            		<fo:block  keep-together="always" text-align="left" >Their Acctg Trans Id:${theirAcctgTransId?if_exists}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                     </fo:table-row>
                     
                     <fo:table-row>	
                   			<fo:table-cell>
                        		<fo:block  text-align="left"  ></fo:block>  
                   			</fo:table-cell>
                     		<#if invoiceDetails?has_content && invoiceDetails.referenceNumberDate?has_content>
                    		<fo:table-cell>
                            		<fo:block  keep-together="always" text-align="left" >Party Invoice Date:<fo:inline font-weight="bold">${Static["org.ofbiz.base.util.UtilDateTime"].toDateString((invoiceDetails.referenceNumberDate), "dd-MM-yyyy")?if_exists}</fo:inline></fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                     </fo:table-row>
                     
                     <#if reportTypeFlag?has_content>
                     	<#assign finAccountTransDetails = delegator.findOne("FinAccountTrans", {"finAccountTransId" : finAccountTransId}, false)?if_exists/>
                    	<#if finAccountTransDetails?has_content>
		                    <fo:table-row>	
		                     		<#if finAccountTransDetails.contraRefNum?has_content>
		                     		<fo:table-cell>
		                            		<fo:block  text-align="left"  keep-together="always">Instrument Number:${finAccountTransDetails.contraRefNum?if_exists}</fo:block>  
		                       		</fo:table-cell>
		                       		<#else>
		                       			<fo:table-cell>
		                            		<fo:block  text-align="left"  ></fo:block>  
		                       			</fo:table-cell>
		                       		</#if>
		                     		<#if finAccountTransDetails.comments?has_content>
		                    		<fo:table-cell>
		                            		<fo:block  keep-together="always" text-align="left" >Cheque in favour:${finAccountTransDetails.comments?if_exists}</fo:block>  
		                       		</fo:table-cell>
		                       		<#else>
		                       			<fo:table-cell>
		                            		<fo:block  text-align="left"  ></fo:block>  
		                       			</fo:table-cell>
		                       		</#if>
		                     </fo:table-row>
                      	</#if> 
                      </#if> 	   
                     </fo:table-body>
                      </fo:table>
            		</fo:block>
            		<fo:block>--------------------------------------------------------------------------------------------</fo:block>
            		<fo:block font-weight = "bold" font-size = "12pt">Acct Name 		        &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;  Party  &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;Debit Amt    &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;Credit Amt</fo:block>
            		<fo:block>--------------------------------------------------------------------------------------------</fo:block>
            	<fo:block>
                 	<fo:table>
                    <fo:table-column column-width="210pt"/>
                    <fo:table-column column-width="100pt"/>
                    <fo:table-column column-width="90pt"/>
                    <fo:table-column column-width="170pt"/>
                    <fo:table-column column-width="20pt"/> 
                    <fo:table-body>
							<#if accountingTransEntryList?has_content>
							<#assign crTotal = 0>
							<#assign drTotal = 0>
							<#list accountingTransEntryList as accntngTransEntry>
							<fo:table-row>
                				
                       			<#if accntngTransEntry.glAccountId?has_content>  
        						<#assign glAccntDetails = delegator.findOne("GlAccount", {"glAccountId" :accntngTransEntry.glAccountId}, true)>
                				<fo:table-cell>
                            		<fo:block text-align="left" wrap-option="wrap"> ${Static["org.ofbiz.order.order.OrderServices"].nameTrim((StringUtil.wrapString(glAccntDetails.accountName?if_exists)),50)}</fo:block>
                            		<#if glAccntDetails.accountName=="ACCOUNTS PAYABLE"||glAccntDetails.accountName=="ACCOUNTS RECEIVABLE">
                            		<fo:block text-align="left" font-size="10pt" >(${Static["org.ofbiz.party.party.PartyHelper"].getPartyName(delegator,accntngTransEntry.partyId, false)})</fo:block>
                            		</#if>  
                       			</fo:table-cell>
                       			</#if>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  white-space-collapse="false">${accntngTransEntry.partyId?if_exists}</fo:block>  
                       			</fo:table-cell>
                       			<#if accntngTransEntry.debitCreditFlag?has_content && accntngTransEntry.debitCreditFlag == "D">
                       				<fo:table-cell>
                            			<fo:block  text-align="right"  white-space-collapse="false">${accntngTransEntry.amount?if_exists?string("#0.00")}</fo:block>  
                       				</fo:table-cell>
                       				<#if accntngTransEntry.amount?has_content>
                       					<#assign drTotal = drTotal+accntngTransEntry.amount>
                       				</#if>
                       			<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="right"  white-space-collapse="false">0.00</fo:block>  
                       			</fo:table-cell>
                       			</#if>
                       			<#if accntngTransEntry.debitCreditFlag?has_content && accntngTransEntry.debitCreditFlag == "C">
	                       			<fo:table-cell>
	                            		<fo:block  text-align="right"  white-space-collapse="false">${accntngTransEntry.amount?if_exists?string("#0.00")}</fo:block>  
	                       			</fo:table-cell>
                       				<#if accntngTransEntry.amount?has_content>
                       					<#assign crTotal = crTotal+accntngTransEntry.amount>
                       				</#if>
                       			<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="right"  white-space-collapse="false">0.00</fo:block>  
                       			</fo:table-cell>
                       			</#if>
            				</fo:table-row>
		  					</#list>
		  					<fo:table-row>
                				<fo:table-cell>
                            		<fo:block  text-align="left"  white-space-collapse="false">---------------------------------------------------------------------------------------------</fo:block>  
                       			</fo:table-cell>
                				
            				</fo:table-row>
		  					<fo:table-row>
                				<fo:table-cell>
                            		<fo:block  text-align="left"  white-space-collapse="false"></fo:block>  
                       			</fo:table-cell>
                				<fo:table-cell>
                            		<fo:block text-align="left" wrap-option="wrap" font-weight="bold"> Totals: </fo:block>  
                       			</fo:table-cell>
                       			<fo:table-cell>
                            		<fo:block  text-align="right"  font-weight="bold" white-space-collapse="false">${drTotal?if_exists?string("#0.00")}</fo:block>  
                       			</fo:table-cell>
                       			<fo:table-cell>
                            		<fo:block text-align="right" font-weight="bold" white-space-collapse="false">${crTotal?if_exists?string("#0.00")}</fo:block>  
                       			</fo:table-cell>
                       			<fo:table-cell>
                            		<fo:block text-align="right" font-weight="bold" white-space-collapse="false"></fo:block>  
                       			</fo:table-cell>
            				</fo:table-row>
		  					</#if>
		  					
				       		</fo:table-body>
		                </fo:table>
		               </fo:block>
				       		
		                 
		              <#if entryList?has_content> 
		              <#list entryList as payAccountingTransEntries>
		              <#--<#if entryList?has_content>-->
        	             <#assign acctgTransId = payAccountingTransEntries.acctgTransId?if_exists>
        	             <#assign acctgTransTypeId = payAccountingTransEntries.acctgTransTypeId?if_exists>
        	             <#assign description = payAccountingTransEntries.description?if_exists>
        	             <#assign transactionDate = payAccountingTransEntries.transactionDate?if_exists>
        	             <#assign isPosted = payAccountingTransEntries.isPosted?if_exists>
        	             <#assign postedDate = payAccountingTransEntries.postedDate?if_exists>
        	             <#assign scheduledPostingDate = payAccountingTransEntries.scheduledPostingDate?if_exists>
        	             <#assign glJournalId = payAccountingTransEntries.glJournalId?if_exists>
        	             <#assign glFiscalTypeId = payAccountingTransEntries.glFiscalTypeId?if_exists>
        	             <#assign voucherRef = payAccountingTransEntries.voucherRef?if_exists>
        	             <#assign voucherDate = payAccountingTransEntries.voucherDate?if_exists>
        	             <#assign fixedAssetId = payAccountingTransEntries.fixedAssetId?if_exists>
        	             <#assign groupStatusId = payAccountingTransEntries.groupStatusId?if_exists>
        	             <#assign inventoryItemId = payAccountingTransEntries.inventoryItemId?if_exists>
        	             <#assign physicalInventoryId = payAccountingTransEntries.physicalInventoryId?if_exists>
        	             <#assign partyId = payAccountingTransEntries.partyId?if_exists>
        	             <#assign roleTypeId = payAccountingTransEntries.roleTypeId?if_exists>
        	             <#assign invoiceId = payAccountingTransEntries.invoiceId?if_exists>
        	             <#assign paymentId = payAccountingTransEntries.paymentId?if_exists>
        	             <#assign finAccountTransId = payAccountingTransEntries.finAccountTransId?if_exists>
        	             <#assign shipmentId = payAccountingTransEntries.shipmentId?if_exists>
        	             <#assign receiptId = payAccountingTransEntries.receiptId?if_exists>
        	             <#assign workEffortId = payAccountingTransEntries.workEffortId?if_exists>
        	             <#assign theirAcctgTransId = payAccountingTransEntries.theirAcctgTransId?if_exists>
        	             <#assign createdByUserLogin = payAccountingTransEntries.createdByUserLogin?if_exists>
         	             <#assign lastModifiedByUserLogin = payAccountingTransEntries.lastModifiedByUserLogin?if_exists>
         	             
         	             <fo:block>
		               
		               <fo:block> &#180;</fo:block>
		               <fo:block>---------------------------------------------------------------------------------------------</fo:block>
		               <fo:block> &#160;</fo:block>
		                 <fo:table>
		                  <fo:table-column column-width="50%"/>
                          <fo:table-column column-width="70%"/>
                             <fo:table-body>
                               <fo:table-row>
                               <fo:table-cell>
                                  <#assign finAccountTransDetails = delegator.findOne("FinAccountTrans", {"finAccountTransId" : finAccountTransId}, false)?if_exists/>
                                  <fo:block  keep-together="always" text-align="right" font-weight = "bold" font-family="Courier,monospace" white-space-collapse="false"><#--<#if finAccountTransDetails?has_content>${(finAccountTransDetails.finAccountTransTypeId)?replace("_"," ")}<#else>${acctgTransTypeId?if_exists?replace("_"," ")}</#if>--></fo:block>
                               </fo:table-cell>
                               </fo:table-row>
                             </fo:table-body>  
		                 </fo:table>
		               </fo:block>
		               <fo:block>
                        <fo:table>
	                    <fo:table-column column-width="50%"/>
	                    <fo:table-column column-width="50%"/>
		                    <fo:table-body>
			                    <fo:table-row>
			                    	<fo:table-cell>
		                        		<fo:block text-align="left" font-weight="bold">Date:${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(nowTimestamp, "MMMM dd,yyyy")}</fo:block>  
		                   			</fo:table-cell>
		                   			<fo:table-cell>
		                        		<fo:block text-align="right" font-weight="bold"><#if invSequenceNum?has_content>Sequence Number:<fo:inline font-weight="bold">${invSequenceNum?if_exists}</fo:inline><#elseif payFinAccntTransSequence?has_content>Sequence Number:<fo:inline font-weight="bold">${payFinAccntTransSequence?if_exists}</fo:inline><#else></#if></fo:block>  
		                   			</fo:table-cell>
			                    </fo:table-row>
		                    </fo:table-body>
	                    </fo:table>
                    </fo:block>
		               <fo:block>---------------------------------------------------------------------------------------------</fo:block>
		               <#assign transType = "">
		               <fo:block><fo:table>
                    <fo:table-column column-width="50%"/>
                    <fo:table-column column-width="70%"/>
                    <fo:table-body>
                    <fo:table-row>
            				<#if acctgTransId?has_content>
            				<fo:table-cell>
                        		<fo:block  text-align="left">Acctg Trans Id:${acctgTransId?if_exists}</fo:block>  
                   			</fo:table-cell>
                   			<#else>
                   			<fo:table-cell>
                        		<fo:block  text-align="left"  font-weight = "bold"></fo:block>  
                   			</fo:table-cell>
                   			</#if>
                   			<#if reportTypeFlag?has_content>
                    				<#if finAccountTransDetails?has_content>
	            				<fo:table-cell>
	                        		<fo:block  keep-together="always" text-align="left" >Acctg Trans Type Id:${finAccountTransDetails.finAccountTransTypeId?if_exists}</fo:block>
	                        		<#assign transType = finAccountTransDetails.finAccountTransTypeId>  
	                   			</fo:table-cell>
	                   			<#else>
	                   			<fo:table-cell>
	                        		<fo:block  text-align="left"  font-weight = "bold"></fo:block>  
	                   			</fo:table-cell>
	                   			</#if>
	                   		<#else>		
	                   			<#if acctgTransTypeId?has_content>
	            				<fo:table-cell>
	                        		<fo:block  keep-together="always" text-align="left">Acctg Trans Type Id:${acctgTransTypeId?if_exists}</fo:block>
	                        		<#assign transType = acctgTransTypeId>  
	                   			</fo:table-cell>
	                   			<#else>
	                   			<fo:table-cell>
	                        		<fo:block  text-align="left"  font-weight = "bold"></fo:block>  
	                   			</fo:table-cell>
	                   			</#if>
	                   		</#if>
                    </fo:table-row>	
                    <fo:table-row>	
                     <#if invoiceId?has_content>
                    		<fo:table-cell>
                            		<fo:block  keep-together="always" text-align="left">Invoice Id:${invoiceId?if_exists}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  font-weight = "bold"></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                       		<#if paymentId?has_content>
                       		<fo:table-cell>
                            		<fo:block  text-align="left" keep-together="always">Payment Id:${paymentId?if_exists}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  font-weight = "bold"></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                     </fo:table-row>
                     <fo:table-row>
                     			
                       			<#if finAccountTransId?has_content>
                    		<fo:table-cell>
                            		<fo:block  keep-together="always" text-align="left">Fin Account Trans Id:${finAccountTransId?if_exists}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  font-weight = "bold"></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                       			<#if description?has_content>
                				<fo:table-cell>
                            		<fo:block  text-align="left" wrap-option="wrap" >Description:${description?if_exists}</fo:block>  
                       			</fo:table-cell>
                       			<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  font-weight = "bold"></fo:block>  
                       			</fo:table-cell>
                       			</#if>
                    </fo:table-row>
                    <fo:table-row>
                    		<#if transactionDate?has_content>
                				<fo:table-cell>
                            		<fo:block  keep-together="always" text-align="left" >Transaction Date:${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(transactionDate, "dd-MM-yyyy")}</fo:block>  
                       			</fo:table-cell>
                       			<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  font-weight = "bold"></fo:block>  
                       			</fo:table-cell>
                       			</#if>
                    
                    		<#if isPosted?has_content>
                				<fo:table-cell>
                            		<fo:block  text-align="left"  keep-together="always">Is Posted:${isPosted?if_exists}</fo:block>  
                       			</fo:table-cell>
                       			<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       			</#if>
                    </fo:table-row>
                    
                    <#--<#if postedDate?has_content>
                                <fo:table-row>
                				<fo:table-cell>
                            		<fo:block  text-align="left"  keep-together="always">Posted Date:${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(postedDate, "dd-MM-yyyy HH:mm:ss")}</fo:block>  
                       			</fo:table-cell>
                       			<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       			</fo:table-row>
                       			</#if>-->
                    <#if scheduledPostingDate?has_content>
                   		<fo:table-row>	
                    		<fo:table-cell>
                            		<fo:block  keep-together="always" text-align="left" >Schd Posting Date:${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(scheduledPostingDate, "dd-MM-yyyy HH:mm:ss")}</fo:block>  
                       		</fo:table-cell>
                       	</fo:table-row>
                    </#if>
                       		
                     
                     <fo:table-row>	
                     <#if glJournalId?has_content>
                       		<fo:table-cell>
                            		<fo:block  text-align="left"  keep-together="always">GL Journal Id:${glJournalId?if_exists}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                     
                     <#--<#if glFiscalTypeId?has_content>
                    		<fo:table-cell>
                            		<fo:block  keep-together="always" text-align="left" >Fiscal GL Type Id:${glFiscalTypeId?if_exists}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>-->
                       		
                     </fo:table-row>
                     <fo:table-row>	
                     <#if voucherRef?has_content>
                       		<fo:table-cell>
                            		<fo:block  text-align="left"  keep-together="always">Voucher Ref:${voucherRef?if_exists}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                     <#if voucherDate?has_content>
                    		<fo:table-cell>
                            		<fo:block  keep-together="always" text-align="left" >Voucher Date:${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(voucherDate, "dd-MM-yyyy")}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                     </fo:table-row>
                     <fo:table-row>	
                     		<#if groupStatusId?has_content>
                    		<fo:table-cell>
                            		<fo:block  keep-together="always" text-align="left" >Group Status Id:${groupStatusId?if_exists}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                       		<#if fixedAssetId?has_content>
                       		<fo:table-cell>
                            		<fo:block  text-align="left"  keep-together="always">Fixed Asset Id:${fixedAssetId?if_exists}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                     </fo:table-row>
                     <fo:table-row>	
                     <#if inventoryItemId?has_content>
                    		<fo:table-cell>
                            		<fo:block  keep-together="always" text-align="left" >Inventory Item Id:${inventoryItemId?if_exists}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                       		<#if physicalInventoryId?has_content>
                       		<fo:table-cell>
                            		<fo:block  text-align="left"  keep-together="always">Physical Inventory Id:${physicalInventoryId?if_exists}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                     </fo:table-row>
                     <fo:table-row>	
                     <#if partyId?has_content>
                     		<#assign partyFullName = Static["org.ofbiz.party.party.PartyHelper"].getPartyName(delegator, partyId?if_exists, false)>
                    		<fo:table-cell>
                            		<fo:block text-align="left" wrap-option="wrap"><#if parameters.reportFlag?has_content && parameters.reportFlag =="Y">&#160;<#else>Party: <fo:inline font-weight="bold">${partyFullName?if_exists}[${partyId?if_exists}]</fo:inline></#if></fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                       		<#--<#if roleTypeId?has_content>
                       		<fo:table-cell>
                            		<fo:block  text-align="left"  keep-together="always">Role Type Id:${roleTypeId?if_exists}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>-->
                       		<#assign invoiceDetails = delegator.findOne("Invoice", {"invoiceId" : invoiceId}, false)?if_exists/>
                       		<#if invoiceDetails?has_content && invoiceDetails.referenceNumber?has_content>
                     		<fo:table-cell>
                            		<fo:block  text-align="left"  keep-together="always">Party Invoice No:<fo:inline font-weight="bold">${invoiceDetails.referenceNumber?if_exists}</fo:inline></fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                     </fo:table-row>
                     
                     <fo:table-row>	
                       		<#if shipmentId?has_content>
                       		<fo:table-cell>
                            		<fo:block  text-align="left"  keep-together="always">Shipment Id:${shipmentId?if_exists}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                       		<#if receiptId?has_content>
                    		<fo:table-cell>
                            		<fo:block  keep-together="always" text-align="left" >Receipt Id:${receiptId?if_exists}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                       		
                     </fo:table-row>
                     <fo:table-row>	
                     
                       		<#if workEffortId?has_content>
                       		<fo:table-cell>
                            		<fo:block  text-align="left"  keep-together="always">Work Effort Id:${workEffortId?if_exists}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                       		<#if theirAcctgTransId?has_content>
                    		<fo:table-cell>
                            		<fo:block  keep-together="always" text-align="left" >Their Acctg Trans Id:${theirAcctgTransId?if_exists}</fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                     </fo:table-row>
                     
                     
                     <fo:table-row>	
                   			<fo:table-cell>
                        		<fo:block  text-align="left"  ></fo:block>  
                   			</fo:table-cell>
                     		<#if invoiceDetails?has_content && invoiceDetails.referenceNumberDate?has_content>
                    		<fo:table-cell>
                            		<fo:block  keep-together="always" text-align="left" >Party Invoice Date:<fo:inline font-weight="bold">${Static["org.ofbiz.base.util.UtilDateTime"].toDateString((invoiceDetails.referenceNumberDate), "dd-MM-yyyy")?if_exists}</fo:inline></fo:block>  
                       		</fo:table-cell>
                       		<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  ></fo:block>  
                       			</fo:table-cell>
                       		</#if>
                     </fo:table-row>
                     
                     <#if reportTypeFlag?has_content>
                     	<#assign finAccountTransDetails = delegator.findOne("FinAccountTrans", {"finAccountTransId" : finAccountTransId}, false)?if_exists/>
                    	<#if finAccountTransDetails?has_content>
		                    <fo:table-row>	
		                     		<#if finAccountTransDetails.contraRefNum?has_content>
		                     		<fo:table-cell>
		                            		<fo:block  text-align="left"  keep-together="always">Instrument Number:${finAccountTransDetails.contraRefNum?if_exists}</fo:block>  
		                       		</fo:table-cell>
		                       		<#else>
		                       			<fo:table-cell>
		                            		<fo:block  text-align="left"  ></fo:block>  
		                       			</fo:table-cell>
		                       		</#if>
		                     		<#if finAccountTransDetails.comments?has_content>
		                    		<fo:table-cell>
		                            		<fo:block  keep-together="always" text-align="left" >Cheque in favour:${finAccountTransDetails.comments?if_exists}</fo:block>  
		                       		</fo:table-cell>
		                       		<#else>
		                       			<fo:table-cell>
		                            		<fo:block  text-align="left"  ></fo:block>  
		                       			</fo:table-cell>
		                       		</#if>
		                     </fo:table-row>
                      	</#if> 
                      </#if> 	   
                     </fo:table-body>
                      </fo:table>
            		</fo:block>
            		<fo:block>--------------------------------------------------------------------------------------------</fo:block>
            		<fo:block font-weight = "bold" font-size = "12pt">Acct Name 		        &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;  Party  &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;Debit Amt    &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;Credit Amt</fo:block>
            		<fo:block>--------------------------------------------------------------------------------------------</fo:block>
		            <fo:block>
                 	<fo:table>
                    <fo:table-column column-width="210pt"/>
                    <fo:table-column column-width="100pt"/>
                    <fo:table-column column-width="90pt"/>
                    <fo:table-column column-width="170pt"/>
                    <fo:table-column column-width="15pt"/> 
                    <fo:table-body>
							<#if finalMap?has_content>
							<#assign crTotal = 0>
							<#assign drTotal = 0>
							<#assign acctngEntriesList = finalMap(transType)> 
							<#list acctngEntriesList as accntngTransEntry>
							<fo:table-row>
                       			<#if accntngTransEntry.glAccountId?has_content>  
                       			<#assign accntngTransEntry = "">
        						<#assign glAccntDetails = delegator.findOne("GlAccount", {"glAccountId" :accntngTransEntry.glAccountId}, true)>
                				<fo:table-cell>
                            		<fo:block text-align="left" wrap-option="wrap"> ${Static["org.ofbiz.order.order.OrderServices"].nameTrim((StringUtil.wrapString(glAccntDetails.accountName?if_exists)),50)}</fo:block>
                            		<#if glAccntDetails.accountName=="ACCOUNTS PAYABLE"||glAccntDetails.accountName=="ACCOUNTS RECEIVABLE">
                            		<fo:block text-align="left" font-size="10pt" >(${Static["org.ofbiz.party.party.PartyHelper"].getPartyName(delegator,accntngTransEntry.partyId, false)})</fo:block>
                            		</#if>  
                       			</fo:table-cell>
                       			</#if>
                       			<fo:table-cell>
                            		<fo:block  text-align="left"  white-space-collapse="false">${accntngTransEntry.partyId?if_exists}</fo:block>  
                       			</fo:table-cell>
                       			<#if accntngTransEntry.debitCreditFlag?has_content && accntngTransEntry.debitCreditFlag == "D">
                       				<fo:table-cell>
                            			<fo:block  text-align="right"  white-space-collapse="false">${accntngTransEntry.amount?if_exists?string("#0.00")}</fo:block>  
                       				</fo:table-cell>
                       				<#if accntngTransEntry.amount?has_content>
                       					<#assign drTotal = drTotal+accntngTransEntry.amount>
                       				</#if>
                       			<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="right"  white-space-collapse="false">0.00</fo:block>  
                       			</fo:table-cell>
                       			</#if>
                       			<#if accntngTransEntry.debitCreditFlag?has_content && accntngTransEntry.debitCreditFlag == "C">
	                       			<fo:table-cell>
	                            		<fo:block  text-align="right"  white-space-collapse="false">${accntngTransEntry.amount?if_exists?string("#0.00")}</fo:block>  
	                       			</fo:table-cell>
                       				<#if accntngTransEntry.amount?has_content>
                       					<#assign crTotal = crTotal+accntngTransEntry.amount>
                       				</#if>
                       			<#else>
                       			<fo:table-cell>
                            		<fo:block  text-align="right"  white-space-collapse="false">0.00</fo:block>  
                       			</fo:table-cell>
                       			</#if>
            				</fo:table-row>
		  					</#list>
		  					<fo:table-row>
                				<fo:table-cell>
                            		<fo:block  text-align="left"  white-space-collapse="false">---------------------------------------------------------------------------------------------</fo:block>  
                       			</fo:table-cell>
                				
            				</fo:table-row>
		  					<fo:table-row>
                				<fo:table-cell>
                            		<fo:block  text-align="left"  white-space-collapse="false"></fo:block>  
                       			</fo:table-cell>
                				<fo:table-cell>
                            		<fo:block text-align="left" wrap-option="wrap" font-weight="bold"> Totals: </fo:block>  
                       			</fo:table-cell>
                       			<fo:table-cell>
                            		<fo:block  text-align="right"  font-weight="bold" white-space-collapse="false">${drTotal?if_exists?string("#0.00")}</fo:block>  
                       			</fo:table-cell>
                       			<fo:table-cell>
                            		<fo:block text-align="right" font-weight="bold" white-space-collapse="false">${crTotal?if_exists?string("#0.00")}</fo:block>  
                       			</fo:table-cell>
                       			<fo:table-cell>
                            		<fo:block text-align="right" font-weight="bold" white-space-collapse="false"></fo:block>  
                       			</fo:table-cell>
            				</fo:table-row>
               			
		  					</#if>
				       		</fo:table-body>
		                </fo:table>
		               </fo:block>
				       		
         	             </#list>
         	           </#if>
         	           <fo:block>
			                 	<fo:table>
			                    <fo:table-column column-width="25%"/>
			                    <fo:table-column column-width="25%"/>
			                    <fo:table-column column-width="25%"/>
			                    <fo:table-column column-width="25%"/>
			                     <fo:table-column column-width="25%"/>
			                    
			                    <fo:table-body>
			                            	<fo:table-row>
								 <fo:table-cell number-columns-spanned="5">
			  						<fo:block text-align="left"  white-space-collapse="false" font-size="12pt">Amount in Words : RUPEES ${Static["org.ofbiz.base.util.UtilNumber"].formatRuleBasedAmount(Static["java.lang.Double"].parseDouble(crTotal?string("#0.00")), "%indRupees-and-paiseRupees", locale).toUpperCase()} ONLY  </fo:block>	
			  					 </fo:table-cell>
				       		</fo:table-row>
			                    		<fo:table-row>
			               					<fo:table-cell number-columns-spanned="2">   						
										 	    <fo:block linefeed-treatment="preserve">&#xA;</fo:block> 
											</fo:table-cell>
					  					</fo:table-row>
									  	<fo:table-row>
			               					<fo:table-cell>
			                    				<fo:block text-align="center">Prepared By</fo:block>
			               					</fo:table-cell>
			               					<fo:table-cell >
			                    				<fo:block text-align="center">Checked By</fo:block>
			               					</fo:table-cell>
			               					<fo:table-cell>
			                    				<fo:block text-align="center">Finance Incharge</fo:block>
			               					</fo:table-cell>
			               					<fo:table-cell >
			                    				<fo:block text-align="center" keep-together="always">RO Incharge</fo:block>
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