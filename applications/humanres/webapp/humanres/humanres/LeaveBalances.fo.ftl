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
            <fo:simple-page-master master-name="main" page-height="12in" page-width="10in"
                     margin-left="0.09in" margin-right="0.2in"  margin-top="0.09in" margin-bottom="0.2in" >
                <fo:region-body margin-top="1.8in"/>
                <fo:region-before extent="1in"/>
                <fo:region-after extent="1in"/>
            </fo:simple-page-master>
        </fo:layout-master-set>
        ${setRequestAttribute("OUTPUT_FILENAME", "MonthlyAttendence.txt")}
        <fo:page-sequence master-reference="main">
        <fo:static-content font-family="Courier,monospace"  flow-name="xsl-region-before">    
        		<fo:block text-align="left"  keep-together="always"  font-family="Courier,monospace" font-weight="bold" white-space-collapse="false">&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;UserLogin:<#if userLogin?exists>${userLogin.userLoginId?if_exists}</#if></fo:block>    
        		<fo:block text-align="left"  keep-together="always"  font-family="Courier,monospace" font-weight="bold" white-space-collapse="false">&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;Print Date :${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(nowTimestamp, "dd/MM/yy HH:mm:ss")}</fo:block>
        		<#assign reportHeader = delegator.findOne("TenantConfiguration", {"propertyTypeEnumId" : "COMPANY_HEADER","propertyName" : "reportHeaderLable"}, true)>
            	<#assign reportSubHeader = delegator.findOne("TenantConfiguration", {"propertyTypeEnumId" : "COMPANY_HEADER","propertyName" : "reportSubHeaderLable"}, true)>      
    			<fo:block text-align="center" font-size="12pt" keep-together="always"  white-space-collapse="false" font-weight="bold" font-family="Courier,monospace">&#160;${reportHeader.description?if_exists}</fo:block>
            	<fo:block text-align="center" font-size="12pt" keep-together="always"  white-space-collapse="false" font-weight="bold" font-family="Courier,monospace">&#160;${reportSubHeader.description?if_exists}</fo:block>	 	 	  
        		 <fo:block text-align="center" keep-together="always"  font-weight="bold">LEAVE BALANCE REPORT as on:  ${Date?if_exists}    </fo:block>
				<fo:block  font-size="12pt">Page:<fo:page-number/>&#160;&#160; &#160;&#160;&#160;&#160;&#160; &#160;&#160;&#160;&#160;&#160; &#160;&#160;&#160;&#160;&#160;&#160;  &#160;&#160;&#160;&#160;&#160;&#160;  &#160;&#160;&#160;&#160;&#160;&#160;  &#160;&#160;&#160;&#160;&#160;&#160;  &#160;&#160;&#160;&#160;&#160;&#160;  &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;--------Balance Days--------   &#160;&#160; &#160;&#160;&#160;&#160;&#160; &#160;&#160;&#160;&#160;  &#160;&#160;&#160;&#160;&#160; </fo:block>
            	<fo:block  font-weight="bold" font-size="12pt">S.No &#160;&#160;EmpNo	&#160;&#160;&#160;&#160;&#160;Employee Name &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;Designation&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;			CL &#160;&#160;&#160;&#160;&#160;&#160;&#160;EL&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;HPL</fo:block>
            	<fo:block>-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------</fo:block>	                   		 </fo:static-content>
     	<fo:flow flow-name="xsl-region-body" font-family="Courier,monospace">
		       <fo:block>
		       
		       	 <#assign sno=1>
            	 <#list employeeList as employee>
	         		<fo:table >
                    <fo:table-column column-width="50pt"/>
                    <fo:table-column column-width="80pt"/>
                    <fo:table-column column-width="170pt"/>
                    <fo:table-column column-width="165pt"/>
                    <fo:table-column column-width="75pt"/>
                    <fo:table-column column-width="70pt"/>
                    <fo:table-column column-width="75pt"/>
                    
                    
                    <fo:table-body> 
                    	 <#assign employeeId = ((employee.get("employeeId"))?if_exists)/>
				    	 <#assign emplPositionAndFulfilment=delegator.findByAnd("EmplPositionAndFulfillment", {"employeePartyId" : employeeId})/>
   			    	    <#assign EmpDepartment=delegator.findByAnd("Employment", {"partyIdTo" : employeeId})/>
	                  <fo:table-row>
	                    <fo:table-cell><fo:block text-align="left" keep-together="always" font-size="12">${sno}</fo:block></fo:table-cell>
               	 	    <fo:table-cell><fo:block text-align="left" keep-together="always" font-size="12">${employee.employeeId}</fo:block></fo:table-cell>
	                    <fo:table-cell><fo:block text-align="left" keep-together="always" font-size="12">${(employee.name)?upper_case}</fo:block></fo:table-cell>
	                    <#if emplPositionAndFulfilment?has_content>
						 <#assign designationName=emplPositionAndFulfilment[0].name?if_exists>
                        <fo:table-cell  keep-together = "always">
                            <fo:block text-align="left" font-size="12">${designationName?if_exists}</fo:block>
                        </fo:table-cell>
                         <#else>
	                    <fo:table-cell><fo:block text-align="left" keep-together="always"> </fo:block></fo:table-cell>
                   	 	 </#if>	 
                   	 	 
                   	 	 <#if (employee.CL)?has_content>
                   	 	<fo:table-cell><fo:block text-align="right" keep-together="always" font-size="12">${(employee.CL)?if_exists?string("#0.0")} </fo:block></fo:table-cell>
                         <#else>
	                    <fo:table-cell><fo:block text-align="right" keep-together="always"> </fo:block></fo:table-cell>
                   	 	 </#if> 
                   	 	 <#if (employee.EL)?has_content>
	                    <fo:table-cell><fo:block text-align="right" keep-together="always" font-size="12">${(employee.EL)?if_exists} </fo:block></fo:table-cell>
                         <#else>
	                    <fo:table-cell><fo:block text-align="right" keep-together="always"> </fo:block></fo:table-cell>
                   	 	 </#if>
                   	 	 <#if (employee.HPL)?has_content>
	                    <fo:table-cell><fo:block text-align="right" keep-together="always" font-size="12">${(employee.HPL)?if_exists} </fo:block></fo:table-cell>
                         <#else>
	                    <fo:table-cell><fo:block text-align="right" keep-together="always"> </fo:block></fo:table-cell>
                   	 	 </#if>
                     </fo:table-row>
             <#assign sno=sno+1>
                    </fo:table-body>
                   </fo:table>
                   </#list>
                  </fo:block>
       		 
        		            </fo:flow>
        		 
        		 </fo:page-sequence>
     </fo:root>
</#escape>