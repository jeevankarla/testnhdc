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
      <fo:simple-page-master master-name="main" page-height="11in" page-width="8.5in"
        margin-top="0.1in" margin-bottom="0.1in" margin-left=".8in" margin-right=".7in">
          <fo:region-body margin-top=".3in"/>
          <fo:region-before extent="1in"/>
          <fo:region-after extent="1in"/>
      </fo:simple-page-master>
    </fo:layout-master-set>
    <#assign emplDetails =0 />
    <#assign emplLeaves =0 />
    <#assign days=0 />
    <#assign doj=0 />
    <#assign emplPosition=0 />
    <#assign location=0 />
    <#assign paySlipNo=0>
    <#if payRollMap?has_content>
    <#assign partyGroup = delegator.findOne("PartyGroup", {"partyId" : parameters.partyId}, true)>
     <#assign partyAddressResult = dispatcher.runSync("getPartyPostalAddress", Static["org.ofbiz.base.util.UtilMisc"].toMap("partyId", parameters.partyId, "userLogin", userLogin))/>
    	<#assign payRollHeaderList = payRollMap.entrySet()>
      
     <fo:page-sequence master-reference="main"> 	
       <#-- <fo:static-content flow-name="xsl-region-after">
             <fo:block font-size="8pt" text-align="center">             
             	<#if footerImageUrl?has_content><fo:external-graphic src="<@ofbizContentUrl>${footerImageUrl?if_exists}</@ofbizContentUrl>" overflow="hidden" height="20px" content-height="scale-to-fit"/></#if>             
         	 </fo:block>  
         	 <fo:block font-size="8pt" text-align="center" space-before="10pt">
                ${uiLabelMap.CommonPage} <fo:page-number-citation ref-id="theEnd"/> ${uiLabelMap.CommonOf} <fo:page-number-citation ref-id="theEnd"/>
            </fo:block> 
        </fo:static-content>-->
          <fo:flow flow-name="xsl-region-body" font-family="Helvetica">
           	<#--<fo:block id="theEnd"/>-->  
        <#assign pageCnt=0>   	
      <#list payRollHeaderList as payRollHeader>
      	<#assign totalEarnings =0 />
        <#assign totalDeductions =0 />
      	 <#assign payHeader = delegator.findOne("PayrollHeader", {"payrollHeaderId" : payRollHeader.getKey()}, true)>
      	 <#assign partyId = payHeader.partyIdFrom>
      	 <#assign emplDetails = delegator.findOne("PartyPersonAndEmployeeDetail", {"partyId" : partyId}, true)/>
      	 <#--<#if timePeriod?exists>
      	 		<#assign emplLeavesDetails = delegator.findOne("PayrollAttendance", {"partyId" : partyId, "customTimePeriodId": timePeriod?if_exists}, true)/>
      	 </#if>-->  
      	 <#if pageCnt != 0>
	      	 <#assign emplWiseTypeids = emplWiseTypeIdsMap.entrySet()>
	      	 <#list emplWiseTypeids as employee>
	      	 	<#if employee.getKey() == partyId>
	      	 		<#assign benTypeSize = employee.getValue().get("benTypeIds")>
	      	 		<#assign dedTypeSize = employee.getValue().get("dedTypeIds")>
	      	 		<#if (benTypeSize >= 11) || (dedTypeSize >= 11)>
		            	<#assign pageCnt=0>
		            	<fo:block page-break-after="always"></fo:block>
		            </#if>
	      	 	</#if>
	      	 </#list>
	   	</#if>
      	 
      	 <#assign doj=delegator.findByAnd("Employment", {"partyIdTo" : partyId})/>
      	 <#assign doj = Static["org.ofbiz.entity.util.EntityUtil"].filterByDate(doj?if_exists,timePeriodStart) />
      	 <#assign emplPosition=delegator.findByAnd("EmplPosition", {"partyId" : partyId})/>
      	 <#assign payGrade=delegator.findByAnd("PayGradePayHistory", {"partyIdTo" : partyId})/>
      	 <#assign payGrade = Static["org.ofbiz.entity.util.EntityUtil"].filterByDate(payGrade?if_exists,timePeriodStart) />
      	 <#assign emplPositionAndFulfilment=delegator.findByAnd("EmplPositionAndFulfillment", {"employeePartyId" : partyId})/>
         <#assign location=delegator.findByAnd("EmployeeContactDetails", {"partyId" : partyId})/>
         <#assign emplLeaves = delegator.findByAnd("EmplLeaveBalanceStatus", {"partyId" : partyId, "customTimePeriodId": parameters.customTimePeriodId})/>       
            <#assign paySlipNo=paySlipNo+1>
            <fo:block text-align="center" border-style="solid" font-weight="bold">
            	<fo:table>
            		<fo:table-column column-width="0.9in"/>
            		<fo:table-column column-width="6in"/>
            		
            		<fo:table-body>
            			<fo:table-row>
            				<fo:table-cell number-columns-spanned="2">
            					<fo:block keep-together="always" white-space-collapse="false" font-size = "8pt" text-align="right">PaySlip No: ${paySlipNo?if_exists}</fo:block>
            				</fo:table-cell>
            			</fo:table-row>
            			<fo:table-row>
            				<fo:table-cell>
            					<fo:block font-size="8pt" text-align="left">             
		             				<#if logoImageUrl?has_content><fo:external-graphic src="<@ofbizContentUrl>${logoImageUrl}</@ofbizContentUrl>" width="40px" height="40px" content-height="scale-to-fit"/></#if>             
		         				</fo:block>	
            				</fo:table-cell>
            				<fo:table-cell>
            					<#assign reportHeader = delegator.findOne("TenantConfiguration", {"propertyTypeEnumId" : "COMPANY_HEADER","propertyName" : "reportHeaderLable"}, true)>
                                <#assign reportSubHeader = delegator.findOne("TenantConfiguration", {"propertyTypeEnumId" : "COMPANY_HEADER","propertyName" : "reportSubHeaderLable"}, true)>
                                <#assign reportSubHeader00 = delegator.findOne("TenantConfiguration", {"propertyTypeEnumId" : "COMPANY_HEADER","propertyName" : "reportSubHeaderLable_00"}, true)>
                                <#assign reportSubHeader01 = delegator.findOne("TenantConfiguration", {"propertyTypeEnumId" : "COMPANY_HEADER","propertyName" : "reportSubHeaderLable_01"}, true)>
            					<fo:block keep-together="always" white-space-collapse="false" font-size = "10pt" text-align="center">  ${reportHeader.description?if_exists}&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</fo:block>
            					<fo:block keep-together="always" white-space-collapse="false" font-size = "8pt" text-align="center">${reportSubHeader.description?if_exists}&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</fo:block>
            					<fo:block keep-together="always" white-space-collapse="false" font-size = "8pt" text-align="center">${reportSubHeader00.description?if_exists}&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</fo:block>
            					<fo:block keep-together="always" white-space-collapse="false" font-size = "8pt" text-align="center">${reportSubHeader01.description?if_exists}&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</fo:block>
            				</fo:table-cell>
            			</fo:table-row>
            		</fo:table-body>
            	</fo:table>
            	
            </fo:block>
           	<fo:block font-size="8pt">
            	<fo:table width="100%" table-layout="fixed"  border-style="solid">
            		<fo:table-column column-width="7in"/>
            		<fo:table-body>
                  		<fo:table-row>
                    		<fo:table-cell>                      			
                     		 	<fo:block linefeed-treatment="preserve">&#xA;</fo:block>
                     		 	 <#if parameters.billingTypeId=="SP_LEAVE_ENCASH">   
        								<#assign timePeriodEnd=basicSalDate?if_exists>
       							 </#if> 
                     		 	<fo:block text-align="center" font-weight="bold"><fo:inline text-decoration="underline">PAYSLIP FOR ${(Static["org.ofbiz.base.util.UtilDateTime"].toDateString(timePeriodEnd, "MMMMM-yyyy")).toUpperCase()}</fo:inline></fo:block>
                     		 	<fo:block linefeed-treatment="preserve">&#xA;</fo:block>
                     		 	<fo:block>
                     		 		<fo:table width="100%" table-layout="fixed">
                     		 			<fo:table-column column-width="2.9in"/>
                     		 			<fo:table-column column-width="2.7in"/>
                     		 			<fo:table-column column-width="7in"/>
                     		 			<fo:table-body>
                     		 				<fo:table-row>
                     		 					<fo:table-cell>
                     		 						<fo:block text-align="left" keep-together="always" white-space-collapse="false">&#160;Employee Name        : ${Static["org.ofbiz.party.party.PartyHelper"].getPartyName(delegator, partyId, false)}</fo:block>
                     		 					</fo:table-cell>
                     		 					<fo:table-cell>
                     		 						<fo:block text-align="left" keep-together="always" white-space-collapse="false">PF Account Number   : <#if emplDetails.employeeId?has_content>${emplDetails.employeeId?if_exists}<#else>${partyId?if_exists}</#if></fo:block>
                     		 					</fo:table-cell>
                     		 					<#--<fo:table-cell>
                     		 						<fo:block text-align="left" keep-together="always" white-space-collapse="false">Actual Basic   : <#if EmplSalaryDetailsMap?has_content>${EmplSalaryDetailsMap.get(partyId).get("basic")?if_exists}</#if></fo:block>
                     		 					</fo:table-cell>-->
                     		 				</fo:table-row>
                     		 				<fo:table-row>
                     		 					<fo:table-cell>
                     		 						<fo:block text-align="left" keep-together="always" white-space-collapse="false">&#160;Employee No.            : <#if emplDetails.employeeId?has_content>${emplDetails.employeeId?if_exists}<#else>${partyId?if_exists}</#if></fo:block>
                     		 					</fo:table-cell>
                     		 					
                     		 					<fo:table-cell>
                     		 						<fo:block text-align="left" keep-together="always" white-space-collapse="false">Bank A/c Name.         : <#if BankAdvicePayRollMap?has_content>${BankAdvicePayRollMap.get(partyId).get("finAccountName")?if_exists}</#if></fo:block>
                     		 					</fo:table-cell>
                     		 					<#--<fo:table-cell>
                     		 						<fo:block text-align="left" keep-together="always" white-space-collapse="false">ESI Number               : ${emplDetails.presentEsic?if_exists}</fo:block>
                     		 					</fo:table-cell>-->
                     		 					<#--<fo:table-cell>
                     		 						<fo:block text-align="left" keep-together="always" white-space-collapse="false">Actual DA       :  <#if EmplSalaryDetailsMap?has_content>${EmplSalaryDetailsMap.get(partyId).get("daAmt")?if_exists}</#if></fo:block>
                     		 					</fo:table-cell>-->
                     		 				</fo:table-row>
                     		 				<fo:table-row>
                     		 					<fo:table-cell>
                     		 						<fo:block text-align="left" keep-together="always" white-space-collapse="false">&#160;Office                         : ${Static["org.ofbiz.party.party.PartyHelper"].getPartyName(delegator, (doj[0].partyIdFrom)?if_exists, false)}</fo:block>
                     		 					</fo:table-cell>
                     		 					<fo:table-cell>
                     		 						<fo:block text-align="left" keep-together="always" white-space-collapse="false">Bank A/c No.              : <#if BankAdvicePayRollMap?has_content>${BankAdvicePayRollMap.get(partyId).get("acNo")?if_exists}</#if></fo:block>
                     		 					</fo:table-cell>
                     		 					
                     		 					<#--<fo:table-cell>
                     		 						<fo:block text-align="left" keep-together="always" white-space-collapse="false">Actual HRA    :  <#if EmplSalaryDetailsMap?has_content>${EmplSalaryDetailsMap.get(partyId).get("hraAmt")?if_exists}</#if></fo:block>
                     		 					</fo:table-cell>-->
                     		 				</fo:table-row>
                     		 				<#if emplPositionAndFulfilment?has_content>
	                     		 				<#assign designation = delegator.findOne("EmplPositionType", {"emplPositionTypeId" : emplPositionAndFulfilment[0].emplPositionTypeId?if_exists}, true)>
	                     		 				<#assign designationName=emplPositionAndFulfilment[0].name?if_exists>
                     		 				</#if>
                     		 				<fo:table-row>
                     		 					<fo:table-cell>
                     		 						<fo:block text-align="left" keep-together="always" white-space-collapse="false">&#160;Designation               : <#if designationName?has_content>${designationName?if_exists}<#else><#if designation?has_content>${designation.description?if_exists}</#if></#if></fo:block>
                     		 					</fo:table-cell>
                     		 					<fo:table-cell>
                     		 						<fo:block text-align="left" keep-together="always" white-space-collapse="false">IFSC Code                 : <#if BankAdvicePayRollMap?has_content>${BankAdvicePayRollMap.get(partyId).get("ifscCode")?if_exists}</#if></fo:block>
                     		 					</fo:table-cell>
                     		 					
                     		 					<fo:table-cell/>
                     		 				</fo:table-row>                     		 				
                     		 				<fo:table-row>
                     		 					<#--<fo:table-cell>
                     		 						<fo:block text-align="left" keep-together="always" white-space-collapse="false">&#160;Location                    : ${(doj[0].locationGeoId)?if_exists}</fo:block>
                     		 					</fo:table-cell>-->
                     		 					<fo:table-cell>
                     		 						<fo:block text-align="left" keep-together="always" white-space-collapse="false">&#160;PayScale                   : <#if payGrade?has_content>${payGrade.get(0).payScale?if_exists}</#if></fo:block>
                     		 					</fo:table-cell>
                     		 					<fo:table-cell>
                     		 						<fo:block text-align="left" keep-together="always" white-space-collapse="false">Employee-PAN          : <#if BankAdvicePayRollMap?has_content>${BankAdvicePayRollMap.get(partyId).get("panNumber")?if_exists}</#if></fo:block>
                     		 					</fo:table-cell>
                     		 					 
                     		 					<#--<fo:table-cell>
                     		 						<fo:block text-align="left" keep-together="always" white-space-collapse="false">Bank A/c No.              : <#if BankAdvicePayRollMap?has_content>${BankAdvicePayRollMap.get(partyId).get("acNo")?if_exists}</#if></fo:block>
                     		 					</fo:table-cell>
                     		 					<fo:table-cell/>-->
                     		 				</fo:table-row>
                     		 				<fo:table-row>
                     		 					<fo:table-cell>
                     		 						<fo:block text-align="left" keep-together="always" white-space-collapse="false">&#160;D.O.J                         : <#if (doj[0].appointmentDate)?has_content>${Static["org.ofbiz.base.util.UtilDateTime"].toDateString((doj[0].appointmentDate?if_exists), "dd/MM/yyyy")}</#if></fo:block>
                     		 					</fo:table-cell>
                     		 				</fo:table-row>
                     		 				<#--<fo:table-row>
                     		 					<fo:table-cell>
                     		 						<fo:block text-align="left" keep-together="always" white-space-collapse="false">&#160;PayScale                   : <#if payGrade?has_content>${payGrade.get(0).payScale?if_exists}</#if></fo:block>
                     		 					</fo:table-cell>                     		 					
                     		 				</fo:table-row>-->
                     		 			</fo:table-body>
                     		 		</fo:table>
                     		 	</fo:block>
                     		</fo:table-cell>
                    	</fo:table-row>
                    </fo:table-body>		
            	</fo:table>
            </fo:block>
            <fo:block font-size="8pt">
            	<fo:table width="100%"   border-style="solid">
            		<fo:table-column column-width="2.5in"/>
            		<fo:table-column column-width="1in"/>
            		<fo:table-column column-width="3.5in"/>
            		<fo:table-header>
		       			<fo:table-row >
                			<fo:table-cell  border-style="solid">
                    			<fo:block text-align="center" font-weight="bold">Attendance Details</fo:block>
                    		</fo:table-cell>
                     		<fo:table-cell border-style="solid">
                        		<fo:block font-weight="bold" text-align="center">Days</fo:block>
                     		</fo:table-cell> 
                    		<fo:table-cell border-style="solid">
                        		<#--<fo:block font-weight="bold" text-align="center">Leave Details</fo:block>-->
                        		<fo:block font-weight="bold" text-align="center">Loss of pay</fo:block>
                    		</fo:table-cell>
                    	</fo:table-row>
                    </fo:table-header>
                    <fo:table-body>
                    	<fo:table-row>
                    		<fo:table-cell></fo:table-cell>
                    	</fo:table-row>
                    </fo:table-body>
                  </fo:table>	
            </fo:block>
            <fo:block font-size="8pt">
            	<fo:table width="100%"   border-width="solid">
            		<fo:table-column column-width="2.5in"/>
            		<fo:table-column column-width="1in"/>
            		<fo:table-column column-width="3.5in"/>
            		<fo:table-column column-width="2in"/>
            		<fo:table-body>
		       			<fo:table-row >
                			<fo:table-cell  border-style="solid">
                				<fo:block linefeed-treatment="preserve">&#xA;</fo:block>
                    			<fo:block text-align="left" keep-together="always" white-space-collapse="false">&#160;Total Days                </fo:block>
                    			<#--<fo:block text-align="left" keep-together="always" white-space-collapse="false">&#160;Arrears   Days            </fo:block>-->
                    			<fo:block text-align="left" keep-together="always" white-space-collapse="false">&#160;Net Paid Days          </fo:block>
                    		</fo:table-cell>
                    		<#assign totalDays=0>
                    		<#assign lateMin=0>
                    		<#assign lossOfPay=0>
                    		<#assign netPaidDays=0>
                    		<#assign arrearDays=0>
                    		<#assign noOfPayableDays=0>
                    		<#assign lateMinStr = "">
                    		<#if emplAttendanceDetailsMap?has_content>
                    		<#assign emplLeavesDetails = emplAttendanceDetailsMap.get(partyId)>
                    			<#assign totalDays=emplLeavesDetails.get("noOfCalenderDays")?if_exists>
                    			<#assign lateMin=emplLeavesDetails.get("lateMin")?if_exists>
                    			<#assign lossOfPay=emplLeavesDetails.get("lossOfPayDays")?if_exists>
                    			<#--<#assign arrearDays=emplLeavesDetails.get("noOfArrearDays")?if_exists>-->
                    			<#assign noOfPayableDays=emplLeavesDetails.get("noOfPayableDays")?if_exists>
                    			<#assign lateMinStr=emplLeavesDetails.get("lateMinStr")?if_exists>
                    		</#if> 
                    		<#assign lossOfpay=0>
                    		<#assign latemin=0>
                    		<#if lossOfPay?has_content><#assign lossOfpay=lossOfPay></#if>
                    		<#if lateMin?has_content><#assign latemin=lateMin></#if>
                    		<#assign netPaidDays=noOfPayableDays-arrearDays>                		
                     		<fo:table-cell border-style="solid">
                     			<fo:block linefeed-treatment="preserve">&#xA;</fo:block>
                        		<fo:block text-align="center" white-space-collapse="false"><#if parameters.billingTypeId=="SP_LEAVE_ENCASH">${noOfPayableDays?if_exists} days<#else>${totalDays?if_exists}  days</#if></fo:block>
                        		<#--<fo:block text-align="center" white-space-collapse="false">${(lossOfpay-latemin)?if_exists}  days</fo:block>-->
                        		<#--<fo:block text-align="center" white-space-collapse="false">${arrearDays?if_exists}  days</fo:block>-->
                        		<fo:block text-align="center" white-space-collapse="false"><#if parameters.billingTypeId=="SP_LEAVE_ENCASH">${noOfPayableDays?if_exists} days<#else>${netPaidDays?if_exists}  days</#if></fo:block>                        		
                     		</fo:table-cell> 
                    		<fo:table-cell border-style="solid">
                    				<fo:block linefeed-treatment="preserve">&#xA;</fo:block>
                    				<#--<fo:block text-indent="5pt" white-space-collapse="false" wrap-option="wrap">Pay Scale :</fo:block><fo:block text-indent="3pt"  wrap-option="wrap">${payGrade.get(0).payScale?if_exists}</fo:block>-->
                    				<fo:block text-indent="5pt" keep-together="always" white-space-collapse="false">Loss of Pay Days  : ${(lossOfpay-latemin)?if_exists}  days</fo:block>
                    				<#--<fo:block text-indent="5pt" white-space-collapse="false" keep-together="always">Late Minutes      :  <#if lateMinStr?has_content>${(lateMinStr)?if_exists}<#else>0 min</#if></fo:block>-->
                    			<#--<fo:block>
                    				<fo:table width="100%">
                    					<fo:table-column column-width="1.5in"/>
                    					<fo:table-column column-width="0.5in"/>
                    					<fo:table-column column-width="0.5in"/>
                    					<fo:table-column column-width="0.5in"/>
                    					<fo:table-column column-width="0.5in"/>
                    					<fo:table-body>
                    						<fo:table-row>
                    							<fo:table-cell/>
                    							<fo:table-cell>
                    								<fo:block font-weight="bold">EL</fo:block>
                    							</fo:table-cell>
                    							<fo:table-cell>
                    								<fo:block font-weight="bold">CH</fo:block>
                    							</fo:table-cell>
                    							<fo:table-cell>
                    								<fo:block font-weight="bold">HPL</fo:block>
                    							</fo:table-cell>
                    							<fo:table-cell>
                    								<fo:block font-weight="bold">CL</fo:block>
                    							</fo:table-cell>
                    						</fo:table-row>
                    						<fo:table-row>
                    							<fo:table-cell><fo:block text-align="left" keep-together="always" white-space-collapse="false">&#160;Opening Leaves                  :</fo:block></fo:table-cell>
                    							<fo:table-cell><fo:block><#if emplLeaves?has_content><#list emplLeaves as empl><#if empl.leaveTypeId=="EL">${empl.openingBalance?if_exists}</#if></#list></#if></fo:block></fo:table-cell>
                    							<fo:table-cell><fo:block><#if emplLeaves?has_content><#list emplLeaves as empl><#if empl.leaveTypeId=="CH">${empl.openingBalance?if_exists}</#if></#list></#if></fo:block></fo:table-cell>
                    							<fo:table-cell><fo:block><#if emplLeaves?has_content><#list emplLeaves as empl><#if empl.leaveTypeId=="HPL">${empl.openingBalance?if_exists}</#if></#list></#if></fo:block></fo:table-cell>
                    							<fo:table-cell><fo:block><#if emplLeaves?has_content><#list emplLeaves as empl><#if empl.leaveTypeId=="CL">${empl.openingBalance?if_exists}</#if></#list></#if></fo:block></fo:table-cell>
                    						</fo:table-row>
                    						<fo:table-row>
                    							<fo:table-cell><fo:block text-align="left" keep-together="always" white-space-collapse="false">&#160;Additions During the Month :</fo:block></fo:table-cell>
                    							<fo:table-cell><fo:block><#if emplLeaves?has_content><#list emplLeaves as empl><#if empl.leaveTypeId=="EL">${empl.adjustedDays?if_exists}</#if></#list></#if></fo:block></fo:table-cell>
                    							<fo:table-cell><fo:block><#if emplLeaves?has_content><#list emplLeaves as empl><#if empl.leaveTypeId=="CH">${empl.adjustedDays?if_exists}</#if></#list></#if></fo:block></fo:table-cell>
                    							<fo:table-cell><fo:block><#if emplLeaves?has_content><#list emplLeaves as empl><#if empl.leaveTypeId=="HPL">${empl.adjustedDays?if_exists}</#if></#list></#if></fo:block></fo:table-cell>
                    							<fo:table-cell><fo:block><#if emplLeaves?has_content><#list emplLeaves as empl><#if empl.leaveTypeId=="CL">${empl.adjustedDays?if_exists}</#if></#list></#if></fo:block></fo:table-cell>
                    						</fo:table-row>
                    						<fo:table-row>
                    							<fo:table-cell><fo:block text-align="left" keep-together="always" white-space-collapse="false">&#160;Availed During the Month    :</fo:block></fo:table-cell>
                    							<fo:table-cell><fo:block><#if emplLeaves?has_content><#list emplLeaves as empl><#if empl.leaveTypeId=="EL">${empl.availedDays?if_exists}</#if></#list></#if></fo:block></fo:table-cell>
                    							<fo:table-cell><fo:block><#if emplLeaves?has_content><#list emplLeaves as empl><#if empl.leaveTypeId=="CH">${empl.availedDays?if_exists}</#if></#list></#if></fo:block></fo:table-cell>
                    							<fo:table-cell><fo:block><#if emplLeaves?has_content><#list emplLeaves as empl><#if empl.leaveTypeId=="HPL">${empl.availedDays?if_exists}</#if></#list></#if></fo:block></fo:table-cell>
                    							<fo:table-cell><fo:block><#if emplLeaves?has_content><#list emplLeaves as empl><#if empl.leaveTypeId=="CL">${empl.availedDays?if_exists}</#if></#list></#if></fo:block></fo:table-cell>
                    						</fo:table-row>
                    						<fo:table-row>
                    							<fo:table-cell><fo:block text-align="left" keep-together="always" white-space-collapse="false">&#160;Closing Leaves                    :</fo:block></fo:table-cell>
                    							<fo:table-cell><fo:block><#if emplLeaves?has_content><#list emplLeaves as empl><#if empl.leaveTypeId=="EL"><#assign opening=0><#if empl.openingBalance?has_content><#assign opening=empl.openingBalance></#if><#assign adjusted=0><#if empl.adjustedDays?has_content><#assign adjusted=empl.adjustedDays></#if><#assign availed=0><#if empl.availedDays?has_content><#assign availed=empl.availedDays></#if><#assign elClosing=(opening+adjusted-availed)><#if elClosing !=0>${elClosing?if_exists}</#if></#if></#list></#if></fo:block></fo:table-cell>
                    							<fo:table-cell><fo:block><#if emplLeaves?has_content><#list emplLeaves as empl><#if empl.leaveTypeId=="CH"><#assign chOpening=0><#if empl.openingBalance?has_content><#assign chOpening=empl.openingBalance></#if><#assign chAdjusted=0><#if empl.adjustedDays?has_content><#assign chAdjusted=empl.adjustedDays></#if><#assign chAvailed=0><#if empl.availedDays?has_content><#assign chAvailed=empl.availedDays></#if><#assign chClosing=(chOpening+chAdjusted-chAvailed)><#if chClosing !=0>${chClosing?if_exists}</#if></#if></#list></#if></fo:block></fo:table-cell>
                    							<fo:table-cell><fo:block><#if emplLeaves?has_content><#list emplLeaves as empl><#if empl.leaveTypeId=="HPL"><#assign hplOpening=0><#if empl.openingBalance?has_content><#assign hplOpening=empl.openingBalance></#if><#assign hplAdjusted=0><#if empl.adjustedDays?has_content><#assign hplAdjusted=empl.adjustedDays></#if><#assign hplAvailed=0><#if empl.availedDays?has_content><#assign hplAvailed=empl.availedDays></#if><#assign hplClosing=(hplOpening+hplAdjusted-hplAvailed)><#if hplClosing !=0>${hplClosing?if_exists}</#if></#if></#list></#if></fo:block></fo:table-cell>
                    							<fo:table-cell><fo:block><#if emplLeaves?has_content><#list emplLeaves as empl><#if empl.leaveTypeId=="CL"><#assign clOpening=0><#if empl.openingBalance?has_content><#assign clOpening=empl.openingBalance></#if><#assign clAdjusted=0><#if empl.adjustedDays?has_content><#assign clAdjusted=empl.adjustedDays></#if><#assign clAvailed=0><#if empl.availedDays?has_content><#assign clAvailed=empl.availedDays></#if><#assign clClosing=(clOpening+clAdjusted-clAvailed)><#if clClosing !=0>${clClosing?if_exists}</#if></#if></#list></#if></fo:block></fo:table-cell>
                    						</fo:table-row>                   						
                    					</fo:table-body>	
                    				</fo:table>	
                    			</fo:block>-->
                        	</fo:table-cell>
                    	</fo:table-row>                    	
                   </fo:table-body>
                  </fo:table>	
            </fo:block>
            <fo:block font-size="8pt">
            <#assign benNoOfLines = 1>
            <#assign dedNoOfLines = 1>
            <fo:table width="100%"   border-style="solid">
            		<fo:table-column column-width="3.5in"/>
            		<fo:table-column column-width="3.5in"/>            		
            		<fo:table-body>
            			<fo:table-row >
                    		<fo:table-cell border-style="solid">
                    			<fo:block keep-together="always" text-align="left">
            	<fo:table width="100%" >
            		<fo:table-column column-width="2.5in"/>
            		<fo:table-column column-width="1in"/>            		
            		<fo:table-body>
            			<fo:table-row >
                    		<fo:table-cell border-style="solid">
                    			<fo:block keep-together="always" text-align="center" font-weight="bold">Earnings</fo:block>
                    		</fo:table-cell>
                    		<fo:table-cell>
                    			<fo:block keep-together="always" text-align="center" font-weight="bold">${uiLabelMap.CommonAmount}</fo:block>
                    		</fo:table-cell>                    		
                    	</fo:table-row>
                   <#assign payRollHeaderItemList =payRollHeader.getValue().entrySet()> 	
                    <#list benefitTypeIds as benefitType>
                    		<#assign value=0>
                    		<#if (payRollHeader.getValue()).get(benefitType)?has_content>
                    			<#assign value=(payRollHeader.getValue()).get(benefitType)>	
                    		</#if>
                    		<#if value !=0>
	                    		<fo:table-row>                      
		                    		<fo:table-cell border-style="solid">                    		
		                      			<#assign totalEarnings=(totalEarnings+(value))>                  			
		                    			<fo:block keep-together="always">&#160;${benefitDescMap[benefitType]?if_exists}</fo:block>                			
		                    		</fo:table-cell>                 	              		
		                    		<fo:table-cell border-style="solid"><fo:block text-align="right">${value?if_exists?string("#0")}&#160;&#160;</fo:block></fo:table-cell>
		                    		<#assign benNoOfLines = benNoOfLines + 1>
		                    	</fo:table-row>
	                    	</#if>
                    	</#list>
            		</fo:table-body>
            	</fo:table>
            	</fo:block>
            	</fo:table-cell>
           		<fo:table-cell>
            		<fo:block keep-together="always" text-align="left">
            		<fo:table width="100%">
            		<fo:table-column column-width="2.5in"/>
            		<fo:table-column column-width="1in"/>            		
            		<fo:table-body>
            			<fo:table-row >
                    		<fo:table-cell border-style="solid">
                    			<fo:block keep-together="always" text-align="center" font-weight="bold">Deductions</fo:block>
                    		</fo:table-cell>
                    		<fo:table-cell border-style="solid">
                    			<fo:block keep-together="always" text-align="center" font-weight="bold">${uiLabelMap.CommonAmount}</fo:block>
                    		</fo:table-cell>                    		
                    	</fo:table-row>
                    	<#list dedTypeIds as deductionType>
                    		<#assign dedValue=0>
                    		<#if (payRollHeader.getValue()).get(deductionType)?has_content>
                    			<#assign dedValue=(payRollHeader.getValue()).get(deductionType)>	
                    		</#if>
                    		<#if dedValue !=0>
	                    		<fo:table-row>                      
		                    		<fo:table-cell border-style="solid">                    		
		                      			<#assign totalDeductions=(totalDeductions+(dedValue))> 
		                      			<#assign instmtNo=0>
		                      			<#if (InstallmentFinalMap?has_content) && (InstallmentFinalMap[payRollHeader.getKey()]?has_content) && (InstallmentFinalMap[payRollHeader.getKey()].get(deductionType)?has_content)>
		                      				<#assign instmtNo=InstallmentFinalMap[payRollHeader.getKey()].get(deductionType)?if_exists>  
		                      			</#if>               			
		                    			<fo:block keep-together="always">&#160;${dedDescMap[deductionType]?if_exists}<#if instmtNo !=0>[${instmtNo?if_exists}]</#if></fo:block>                			
		                    		</fo:table-cell>                 	              		
		                    		<fo:table-cell border-style="solid"><fo:block text-align="right">${((-1)*(dedValue))?if_exists?string("#0")}&#160;&#160;</fo:block></fo:table-cell>
		                    		<#assign dedNoOfLines = dedNoOfLines + 1>
		                    	</fo:table-row>
	                    	</#if>
                    	</#list>
            		</fo:table-body>
            	</fo:table>
            	</fo:block>         
            	</fo:table-cell>                   		
                   </fo:table-row>
                   <fo:table-row border-style="solid">
                   		<fo:table-cell border-style="solid">
                   			
                   			<fo:block font-weight="bold" white-space-collapse="false" keep-together="always" text-align="right">&#160;Total Earnings :                                <#if totalEarnings?has_content>
                   			<#assign total = totalEarnings?if_exists />
                   			<#-- <@ofbizCurrency amount=total?string("#0") /></#if>&#160;&#160;</fo:block> -->
                   			${total?if_exists?string('#0.00')}</#if>&#160;</fo:block>
                   		</fo:table-cell>
                   		<fo:table-cell>
                   			<#if totalDeductions?has_content>
                   		  				<#assign totalamount = ((-1)*totalDeductions)?if_exists />
                   								<fo:block font-weight="bold" white-space-collapse="false" text-align="right" keep-together="always">&#160;Total Deductions :                                                ${totalamount?if_exists?string('#0.00')}&#160;</fo:block>
                   								</#if>
                   			<#-- <fo:block font-weight="bold" white-space-collapse="false" keep-together="always">&#160;Total Deductions :                                                         <#if totalDeductions?has_content><#assign totalamount = ((-1)*totalDeductions)?if_exists /><@ofbizCurrency amount=totalamount?string("#0")/></#if>&#160;&#160;</fo:block> -->
                   		</fo:table-cell>
                   </fo:table-row>
                   <#assign netAmt= total-totalamount>
                	<fo:table-row>
                   		<fo:table-cell>                   			
                   			<fo:block font-weight="bold">&#160;Net Pay   :
                            	Rs. ${netAmt?if_exists?string('#0.00')}
                          	</fo:block>                       
                   			<fo:block white-space-collapse="false" keep-together="always">&#160;(In Words: RUPEES ${(Static["org.ofbiz.base.util.UtilNumber"].formatRuleBasedAmount(Static["java.lang.Double"].parseDouble(netAmt?string("#0")), "%rupees-and-paise", locale).replace("rupees","")).toUpperCase()}ONLY)</fo:block>
                   			 <fo:block linefeed-treatment="preserve">&#xA;</fo:block>  
                   		</fo:table-cell>
                   </fo:table-row>
            		</fo:table-body>
            	</fo:table>            		          
            </fo:block>
            <fo:block text-align="center" keep-together="always" font-size="8pt">Any deviations on the information stated above shall be brought to the notice of the officer in charge immediately</fo:block>
            <fo:block linefeed-treatment="preserve">&#xA;</fo:block>
            <fo:block linefeed-treatment="preserve">&#xA;</fo:block>            
	            <#assign pageCnt=pageCnt+1> 
	            <#if pageCnt ==1>
		            <#if (dedNoOfLines >= 11) || (benNoOfLines >= 11)>
		            	<#assign pageCnt=0>
		            	<fo:block page-break-after="always"></fo:block>
		            </#if>
		       	</#if>
	            <#if pageCnt==2>
	            	<#assign pageCnt=0>
	            	<fo:block page-break-after="always"></fo:block>
	            </#if>
            </#list>
          </fo:flow>          
        </fo:page-sequence>
    <#else>    	
		<fo:page-sequence master-reference="main">
			<fo:flow flow-name="xsl-region-body" font-family="Courier,monospace">
				<fo:block font-size="8pt" text-align="left">             
     				<#if logoImageUrl?has_content><fo:external-graphic src="<@ofbizContentUrl>${logoImageUrl}</@ofbizContentUrl>" width="50px" height="50px" content-height="scale-to-fit"/></#if>             
 				</fo:block>
   		 		<fo:block font-size="14pt">
        			No Pay Slips Found.......!
   		 		</fo:block>
			</fo:flow>
		</fo:page-sequence>		
    </#if>        
  </fo:root>
</#escape>