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
            <fo:simple-page-master master-name="main" page-height="12in" page-width="15in" margin-left="1in"  margin-right=".5in" margin-top=".2in" margin-bottom=".2in">
                <fo:region-body margin-top="1.6in"/>
                <fo:region-before extent="1in"/>
                <fo:region-after extent="1in"/>
            </fo:simple-page-master>
       </fo:layout-master-set>
       ${setRequestAttribute("OUTPUT_FILENAME", "DepartmentTotalReport.txt")}
		<#if finalMap?has_content>
       <fo:page-sequence master-reference="main">
            <fo:static-content flow-name="xsl-region-before" font-family="Courier,monospace" font-weight="bold">
            	<fo:block text-align="left" white-space-collapse="false" font-size="7pt">&#160;                                                  ANDHRA PRADESH DAIRY DEVELOPMENT CO-OP, FEDERATION LIMITED</fo:block>
                <fo:block text-align="left" white-space-collapse="false" font-size="7pt">&#160;                                                  DEPARTMENT TOTALS FOR ${(Static["org.ofbiz.base.util.UtilDateTime"].toDateString(timePeriodStart?if_exists, "MMMMM, yyyy"))?upper_case} SHED CODE: ${ShedId?upper_case}</fo:block>
                <fo:block font-size="7pt">-------------------------------------------------------------------------------------------------------------------------------------------------------------------</fo:block>
                 <fo:block font-size="7pt">
                    <fo:table>
                    	<fo:table-column column-width="30pt"/>
                        <fo:table-column column-width="93pt"/>
                        <fo:table-column column-width="93pt"/>
                        <fo:table-column column-width="93pt"/>
                        <fo:table-column column-width="93pt"/>
                        <fo:table-column column-width="93pt"/>
                        <fo:table-column column-width="93pt"/>
                        <fo:table-column column-width="93pt"/>
                        <fo:table-column column-width="93pt"/>
	                        <fo:table-body>
		                        <fo:table-row><fo:table-cell><fo:block keep-together="always" text-align="right">DEPT</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">PAY</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">RISK ALW</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">SUPP.ARRS</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">EPF</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">WATER</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">CONV.ADV</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">TOT.DEDNS</fo:block></fo:table-cell> </fo:table-row>
		                        <fo:table-row><fo:table-cell><fo:block keep-together="always" text-align="right"></fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">SPL.PAY</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">OT ALW</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">MISC.I</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">GPF</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">IT</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">MRG.LN</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">NET PAY</fo:block></fo:table-cell> </fo:table-row>
		                        <fo:table-row><fo:table-cell><fo:block keep-together="always" text-align="right"></fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">PNL.PAY</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">HD ALW</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">MISC.II</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">VOL.PF</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">COURT</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">MISC.APP</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">RND.NET PAY</fo:block></fo:table-cell> </fo:table-row>
		                        <fo:table-row><fo:table-cell><fo:block keep-together="always" text-align="right"></fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">FPP</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">OP ALW</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">MISC.III</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">GPF.LN</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">ELECT</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">DEPT.DUES</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right"> </fo:block></fo:table-cell> </fo:table-row>
		                        <fo:table-row><fo:table-cell><fo:block keep-together="always" text-align="right"></fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">DA</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">IC ALW</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">GROSS</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">EPF.RFND</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">PAY.ADV</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">WEL.FUND</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right"></fo:block></fo:table-cell> </fo:table-row>
		                        <fo:table-row><fo:table-cell><fo:block keep-together="always" text-align="right"></fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">HRA</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">CONV.ALW</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right"></fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">ESIC</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">TOUR.ADV</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">BANK LOANS</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right"></fo:block></fo:table-cell> </fo:table-row>
		                        <fo:table-row><fo:table-cell><fo:block keep-together="always" text-align="right"></fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">CCA</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">MED.ALW</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right"></fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">GIS</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">FEST.ADV</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">MILK CARDS</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right"></fo:block></fo:table-cell> </fo:table-row>
		                        <fo:table-row><fo:table-cell><fo:block keep-together="always" text-align="right"></fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">IR</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">SPL.ALW</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right"></fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">P.TAX</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">EDN.ADV</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">MILK DUES</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right"></fo:block></fo:table-cell> </fo:table-row>
		                        <fo:table-row><fo:table-cell><fo:block keep-together="always" text-align="right"></fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">ND.ALW</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">WASH.ALW</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right"></fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">SSS</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">MED.ADV</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">APGLIF AND LOAN</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right"></fo:block></fo:table-cell> </fo:table-row>
		                        <fo:table-row><fo:table-cell><fo:block keep-together="always" text-align="right"></fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">CB.ALW</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">EXTV.ALW</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right"></fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">HRR</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right">DPT.HB LN</fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right"></fo:block></fo:table-cell> <fo:table-cell><fo:block keep-together="always" text-align="right"></fo:block></fo:table-cell> </fo:table-row>
	                        </fo:table-body>
                    </fo:table>
                 </fo:block>
                 <fo:block font-size="7pt">-------------------------------------------------------------------------------------------------------------------------------------------------------------------</fo:block>
             </fo:static-content> 
            <fo:flow flow-name="xsl-region-body" font-family="Courier,monospace">
                 <fo:block font-size="7pt">
                 <#assign count=0>
                 <#assign DeptTotals=finalMap.entrySet()>
                 <#list DeptTotals as deptTotals>
                    <fo:table >
                        <fo:table-column column-width="30pt"/>
                        <fo:table-column column-width="93pt"/>
                        <fo:table-column column-width="93pt"/>
                        <fo:table-column column-width="93pt"/>
                        <fo:table-column column-width="93pt"/>
                        <fo:table-column column-width="93pt"/>
                        <fo:table-column column-width="93pt"/>
                        <fo:table-column column-width="93pt"/>
                        <fo:table-column column-width="93pt"/>
                        <fo:table-body>
                        <#assign count=count+1>
                        <#if count==5>
                        <fo:table-row>
                        	<fo:table-cell>
                        		 <fo:block page-break-before="always" font-size="7pt">-------------------------------------------------------------------------------------------------------------------------------------------------------------------</fo:block>
                        	</fo:table-cell>
                        </fo:table-row>
                        <#assign count=0>
                        <#else>
                        <fo:table-row>
                        	<fo:table-cell>
                        		 <fo:block font-size="7pt">-------------------------------------------------------------------------------------------------------------------------------------------------------------------</fo:block>
                        	</fo:table-cell>
                        </fo:table-row>
                        </#if>
                            <fo:table-row>
                                <fo:table-cell>
                                <fo:block keep-together="always" text-align="right">${deptTotals.getKey()}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_SALARY=0>
                                <#if deptTotals.getValue().get("PAYROL_BEN_SALARY")?has_content>
                                <#assign PAYROL_BEN_SALARY=deptTotals.getValue().get("PAYROL_BEN_SALARY")>
                                </#if>
                                  <fo:block keep-together="always" text-align="right">${PAYROL_BEN_SALARY?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_RISKALW=0>
                                <#if deptTotals.getValue().get("PAYROL_BEN_RISKALW")?has_content>
                                <#assign PAYROL_BEN_RISKALW=deptTotals.getValue().get("PAYROL_BEN_RISKALW")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${PAYROL_BEN_RISKALW?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_SUPARRS=0>
                                <#if deptTotals.getValue().get("PAYROL_BEN_SUPARRS")?has_content>
                                <#assign PAYROL_BEN_SUPARRS=deptTotals.getValue().get("PAYROL_BEN_SUPARRS")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${PAYROL_BEN_SUPARRS?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_EPF=0>
                                <#if deptTotals.getValue().get("PAYROL_DD_EPF")?has_content>
                                <#assign PAYROL_DD_EPF=deptTotals.getValue().get("PAYROL_DD_EPF")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_EPF)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_WATER=0>
                                <#if deptTotals.getValue().get("PAYROL_DD_WATER")?has_content>
                                <#assign PAYROL_DD_WATER=deptTotals.getValue().get("PAYROL_DD_WATER")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_WATER)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_CONADV=0>
                                <#if deptTotals.getValue().get("PAYROL_DD_CONADV")?has_content>
                                <#assign PAYROL_DD_CONADV=deptTotals.getValue().get("PAYROL_DD_CONADV")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_CONADV)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign totDeductions=0>
                                <#if deptTotals.getValue().get("totDeductions")?has_content>
                                <#assign totDeductions=deptTotals.getValue().get("totDeductions")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*totDeductions)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                                <fo:table-cell>
                                <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_SPLPAY=0>
                                <#if deptTotals.getValue().get("PAYROL_BEN_SPLPAY")?has_content>
                                <#assign PAYROL_BEN_SPLPAY=deptTotals.getValue().get("PAYROL_BEN_SPLPAY")>
                                </#if>
                                  <fo:block keep-together="always" text-align="right">${PAYROL_BEN_SPLPAY?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_OTALW=0>
                                <#if deptTotals.getValue().get("PAYROL_BEN_OTALW")?has_content>
                                <#assign PAYROL_BEN_OTALW=deptTotals.getValue().get("PAYROL_BEN_OTALW")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${PAYROL_BEN_OTALW?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_MISCONE=0>
                                <#if deptTotals.getValue().get("PAYROL_BEN_MISCONE")?has_content>
                                <#assign PAYROL_BEN_MISCONE=deptTotals.getValue().get("PAYROL_BEN_MISCONE")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${PAYROL_BEN_MISCONE?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_GPF=0>
                                <#if deptTotals.getValue().get("PAYROL_DD_GPF")?has_content>
                                <#assign PAYROL_DD_GPF=deptTotals.getValue().get("PAYROL_DD_GPF")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_GPF)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_IT=0>
                                <#if deptTotals.getValue().get("PAYROL_DD_IT")?has_content>
                                <#assign PAYROL_DD_IT=deptTotals.getValue().get("PAYROL_DD_IT")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_IT)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_MRGLN=0>
                                <#if deptTotals.getValue().get("PAYROL_DD_MRGLN")?has_content>
                                <#assign PAYROL_DD_MRGLN=deptTotals.getValue().get("PAYROL_DD_MRGLN")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_MRGLN)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign netAmount=0>
                                <#if deptTotals.getValue().get("netAmount")?has_content>
                                <#assign netAmount=deptTotals.getValue().get("netAmount")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${netAmount?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                                <fo:table-cell>
                                <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_PNLPAY=0>
                                <#if deptTotals.getValue().get("PAYROL_BEN_PNLPAY")?has_content>
                                <#assign PAYROL_BEN_PNLPAY=deptTotals.getValue().get("PAYROL_BEN_PNLPAY")>
                                </#if>
                                  <fo:block keep-together="always" text-align="right">${PAYROL_BEN_PNLPAY?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_HDALW=0>
                                <#if deptTotals.getValue().get("PAYROL_BEN_HDALW")?has_content>
                                <#assign PAYROL_BEN_HDALW=deptTotals.getValue().get("PAYROL_BEN_HDALW")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${PAYROL_BEN_HDALW?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_MISCTWO=0>
                                <#if deptTotals.getValue().get("PAYROL_BEN_MISCTWO")?has_content>
                                <#assign PAYROL_BEN_MISCTWO=deptTotals.getValue().get("PAYROL_BEN_MISCTWO")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${PAYROL_BEN_MISCTWO?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_VOLPF=0>
                                <#if deptTotals.getValue().get("PAYROL_DD_VOLPF")?has_content>
                                <#assign PAYROL_DD_VOLPF=deptTotals.getValue().get("PAYROL_DD_VOLPF")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_VOLPF)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_COURT=0>
                                <#if deptTotals.getValue().get("PAYROL_DD_COURT")?has_content>
                                <#assign PAYROL_DD_COURT=deptTotals.getValue().get("PAYROL_DD_COURT")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_COURT)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_MISAPP=0>
                                <#if deptTotals.getValue().get("PAYROL_DD_MISAPP")?has_content>
                                <#assign PAYROL_DD_MISAPP=deptTotals.getValue().get("PAYROL_DD_MISAPP")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_MISAPP)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign rndNetAmt=0>
                                <#if deptTotals.getValue().get("rndNetAmt")?has_content>
                                <#assign rndNetAmt=deptTotals.getValue().get("rndNetAmt")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${rndNetAmt?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                                <fo:table-cell>
                                <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_FPP=0>
                                <#if deptTotals.getValue().get("PAYROL_BEN_FPP")?has_content>
                                <#assign PAYROL_BEN_FPP=deptTotals.getValue().get("PAYROL_BEN_FPP")>
                                </#if>
                                  <fo:block keep-together="always" text-align="right">${PAYROL_BEN_FPP?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_OPALW=0>
                                <#if deptTotals.getValue().get("PAYROL_BEN_OPALW")?has_content>
                                <#assign PAYROL_BEN_OPALW=deptTotals.getValue().get("PAYROL_BEN_OPALW")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${PAYROL_BEN_OPALW?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_MISCTHREE=0>
                                <#if deptTotals.getValue().get("PAYROL_BEN_MISCTHREE")?has_content>
                                <#assign PAYROL_BEN_MISCTHREE=deptTotals.getValue().get("PAYROL_BEN_MISCTHREE")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${PAYROL_BEN_MISCTHREE?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_GPFLN=0>
                                <#if deptTotals.getValue().get("PAYROL_DD_GPFLN")?has_content>
                                <#assign PAYROL_DD_GPFLN=deptTotals.getValue().get("PAYROL_DD_GPFLN")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_GPFLN)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_ELECT=0>
                                <#if deptTotals.getValue().get("PAYROL_DD_ELECT")?has_content>
                                <#assign PAYROL_DD_ELECT=deptTotals.getValue().get("PAYROL_DD_ELECT")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_ELECT)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_DPTDUES=0>
                                <#if deptTotals.getValue().get("PAYROL_DD_DPTDUES")?has_content>
                                <#assign PAYROL_DD_DPTDUES=deptTotals.getValue().get("PAYROL_DD_DPTDUES")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_DPTDUES)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                                <fo:table-cell>
                                <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_DA=0>
                                <#if deptTotals.getValue().get("PAYROL_BEN_DA")?has_content>
                                <#assign PAYROL_BEN_DA=deptTotals.getValue().get("PAYROL_BEN_DA")>
                                </#if>
                                  <fo:block keep-together="always" text-align="right">${PAYROL_BEN_DA?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_ICALW=0>
                                <#if deptTotals.getValue().get("PAYROL_BEN_ICALW")?has_content>
                                <#assign PAYROL_BEN_ICALW=deptTotals.getValue().get("PAYROL_BEN_ICALW")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${PAYROL_BEN_ICALW?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign totEarnings=0>
                                <#if deptTotals.getValue().get("totEarnings")?has_content>
                                <#assign totEarnings=deptTotals.getValue().get("totEarnings")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${totEarnings?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_EPFREF=0>
                                <#if deptTotals.getValue().get("PAYROL_DD_EPFREF")?has_content>
                                <#assign PAYROL_DD_EPFREF=deptTotals.getValue().get("PAYROL_DD_EPFREF")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_EPFREF)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_PAYADV=0>
                                <#if deptTotals.getValue().get("PAYROL_DD_PAYADV")?has_content>
                                <#assign PAYROL_DD_PAYADV=deptTotals.getValue().get("PAYROL_DD_PAYADV")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_PAYADV)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_WELFARE=0>
                                <#if deptTotals.getValue().get("PAYROL_DD_WELFARE")?has_content>
                                <#assign PAYROL_DD_WELFARE=deptTotals.getValue().get("PAYROL_DD_WELFARE")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_WELFARE)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                                <fo:table-cell>
                                <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_HRA=0>
                                <#if deptTotals.getValue().get("PAYROL_BEN_HRA")?has_content>
                                <#assign PAYROL_BEN_HRA=deptTotals.getValue().get("PAYROL_BEN_HRA")>
                                </#if>
                                  <fo:block keep-together="always" text-align="right">${PAYROL_BEN_HRA?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_CONALW=0>
                                <#if deptTotals.getValue().get("PAYROL_BEN_CONALW")?has_content>
                                <#assign PAYROL_BEN_CONALW=deptTotals.getValue().get("PAYROL_BEN_CONALW")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${PAYROL_BEN_CONALW?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_ESI=0>
                                <#if deptTotals.getValue().get("PAYROL_DD_ESI")?has_content>
                                <#assign PAYROL_DD_ESI=deptTotals.getValue().get("PAYROL_DD_ESI")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_ESI)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_TOURADV=0>
                                <#if deptTotals.getValue().get("PAYROL_DD_TOURADV")?has_content>
                                <#assign PAYROL_DD_TOURADV=deptTotals.getValue().get("PAYROL_DD_TOURADV")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_TOURADV)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign loanAmount=0>
                                <#if deptTotals.getValue().get("loanAmount")?has_content>
                                <#assign loanAmount=deptTotals.getValue().get("loanAmount")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*loanAmount)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                                <fo:table-cell>
                                <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_CCA=0>
                                <#if deptTotals.getValue().get("PAYROL_BEN_CCA")?has_content>
                                <#assign PAYROL_BEN_CCA=deptTotals.getValue().get("PAYROL_BEN_CCA")>
                                </#if>
                                  <fo:block keep-together="always" text-align="right">${PAYROL_BEN_CCA?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_MEDALW=0>
                                <#if deptTotals.getValue().get("PAYROL_BEN_MEDALW")?has_content>
                                <#assign PAYROL_BEN_MEDALW=deptTotals.getValue().get("PAYROL_BEN_MEDALW")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${PAYROL_BEN_MEDALW?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_GIS=0>
                                <#if deptTotals.getValue().get("PAYROL_DD_GIS")?has_content>
                                <#assign PAYROL_DD_GIS=deptTotals.getValue().get("PAYROL_DD_GIS")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_GIS)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_FESTADV=0>
                                <#if deptTotals.getValue().get("PAYROL_DD_FESTADV")?has_content>
                                <#assign PAYROL_DD_FESTADV=deptTotals.getValue().get("PAYROL_DD_FESTADV")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_FESTADV)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_MILKCARDS=0>
                                <#if deptTotals.getValue().get("PAYROL_DD_MILKCARDS")?has_content>
                                <#assign PAYROL_DD_MILKCARDS=deptTotals.getValue().get("PAYROL_DD_MILKCARDS")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_MILKCARDS)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                                <fo:table-cell>
                                <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_IR=0>
                                <#if deptTotals.getValue().get("PAYROL_BEN_IR")?has_content>
                                <#assign PAYROL_BEN_IR=deptTotals.getValue().get("PAYROL_BEN_IR")>
                                </#if>
                                  <fo:block keep-together="always" text-align="right">${PAYROL_BEN_IR?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_SPLALW=0>
                                <#if deptTotals.getValue().get("PAYROL_BEN_SPLALW")?has_content>
                                <#assign PAYROL_BEN_SPLALW=deptTotals.getValue().get("PAYROL_BEN_SPLALW")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${PAYROL_BEN_SPLALW?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_PTAX=0>
                                <#if deptTotals.getValue().get("PAYROL_DD_PTAX")?has_content>
                                <#assign PAYROL_DD_PTAX=deptTotals.getValue().get("PAYROL_DD_PTAX")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_PTAX)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_EDNADV=0>
                                <#if deptTotals.getValue().get("PAYROL_DD_EDNADV")?has_content>
                                <#assign PAYROL_DD_EDNADV=deptTotals.getValue().get("PAYROL_DD_EDNADV")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_EDNADV)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_MILKDUES=0>
                                <#if deptTotals.getValue().get("PAYROL_DD_MILKDUES")?has_content>
                                <#assign PAYROL_DD_MILKDUES=deptTotals.getValue().get("PAYROL_DD_MILKDUES")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_MILKDUES)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                                <fo:table-cell>
                                <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_NDALW=0>
                                <#if deptTotals.getValue().get("PAYROL_BEN_NDALW")?has_content>
                                <#assign PAYROL_BEN_NDALW=deptTotals.getValue().get("PAYROL_BEN_NDALW")>
                                </#if>
                                  <fo:block keep-together="always" text-align="right">${PAYROL_BEN_NDALW?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_WASHALW=0>
                                <#if deptTotals.getValue().get("PAYROL_BEN_WASHALW")?has_content>
                                <#assign PAYROL_BEN_WASHALW=deptTotals.getValue().get("PAYROL_BEN_WASHALW")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${PAYROL_BEN_WASHALW?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_SSS=0>
                                <#if deptTotals.getValue().get("PAYROL_DD_SSS")?has_content>
                                <#assign PAYROL_DD_SSS=deptTotals.getValue().get("PAYROL_DD_SSS")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_SSS)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_MEDADV=0>
                                <#if deptTotals.getValue().get("PAYROL_DD_MEDADV")?has_content>
                                <#assign PAYROL_DD_MEDADV=deptTotals.getValue().get("PAYROL_DD_MEDADV")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_MEDADV)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_APGLIFLN=0>
                                <#if deptTotals.getValue().get("PAYROL_DD_APGLIFLN")?has_content>
                                <#assign PAYROL_DD_APGLIFLN=deptTotals.getValue().get("PAYROL_DD_APGLIFLN")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_APGLIFLN)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                                <fo:table-cell>
                                <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_CBALW=0>
                                <#if deptTotals.getValue().get("PAYROL_BEN_CBALW")?has_content>
                                <#assign PAYROL_BEN_CBALW=deptTotals.getValue().get("PAYROL_BEN_CBALW")>
                                </#if>
                                  <fo:block keep-together="always" text-align="right">${PAYROL_BEN_CBALW?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_EXTALW=0>
                                <#if deptTotals.getValue().get("PAYROL_BEN_EXTALW")?has_content>
                                <#assign PAYROL_BEN_EXTALW=deptTotals.getValue().get("PAYROL_BEN_EXTALW")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${PAYROL_BEN_EXTALW?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_HRR=0>
                                <#if deptTotals.getValue().get("PAYROL_DD_HRR")?has_content>
                                <#assign PAYROL_DD_HRR=deptTotals.getValue().get("PAYROL_DD_HRR")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_HRR)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_DPTHB=0>
                                <#if deptTotals.getValue().get("PAYROL_DD_DPTHB")?has_content>
                                <#assign PAYROL_DD_DPTHB=deptTotals.getValue().get("PAYROL_DD_DPTHB")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_DPTHB)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                        	<fo:table-cell>
                        		 <fo:block font-size="7pt">-------------------------------------------------------------------------------------------------------------------------------------------------------------------</fo:block>
                        	</fo:table-cell>
                        </fo:table-row>
                        </fo:table-body>                        
                    </fo:table>  
                    </#list>  
                </fo:block>
                <fo:block font-size="7pt">
                <#if grandTotalMap?has_content>
                    <fo:table >
                        <fo:table-column column-width="30pt"/>
                        <fo:table-column column-width="93pt"/>
                        <fo:table-column column-width="93pt"/>
                        <fo:table-column column-width="93pt"/>
                        <fo:table-column column-width="93pt"/>
                        <fo:table-column column-width="93pt"/>
                        <fo:table-column column-width="93pt"/>
                        <fo:table-column column-width="93pt"/>
                        <fo:table-column column-width="93pt"/>
                        <fo:table-body>
                        <fo:table-row>
                        	<fo:table-cell>
                        		 <fo:block font-size="7pt">-------------------------------------------------------------------------------------------------------------------------------------------------------------------</fo:block>
                        	</fo:table-cell>
                        </fo:table-row>
                            <fo:table-row>
                                <fo:table-cell>
                                <fo:block keep-together="always" text-align="left">GrandTotals</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_SALARY=0>
                                <#if grandTotalMap.get("PAYROL_BEN_SALARY")?has_content>
                                <#assign PAYROL_BEN_SALARY=grandTotalMap.get("PAYROL_BEN_SALARY")>
                                </#if>
                                  <fo:block keep-together="always" text-align="right">${PAYROL_BEN_SALARY?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_RISKALW=0>
                                <#if grandTotalMap.get("PAYROL_BEN_RISKALW")?has_content>
                                <#assign PAYROL_BEN_RISKALW=grandTotalMap.get("PAYROL_BEN_RISKALW")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${PAYROL_BEN_RISKALW?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_SUPARRS=0>
                                <#if grandTotalMap.get("PAYROL_BEN_SUPARRS")?has_content>
                                <#assign PAYROL_BEN_SUPARRS=grandTotalMap.get("PAYROL_BEN_SUPARRS")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${PAYROL_BEN_SUPARRS?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_EPF=0>
                                <#if grandTotalMap.get("PAYROL_DD_EPF")?has_content>
                                <#assign PAYROL_DD_EPF=grandTotalMap.get("PAYROL_DD_EPF")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_EPF)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_WATER=0>
                                <#if grandTotalMap.get("PAYROL_DD_WATER")?has_content>
                                <#assign PAYROL_DD_WATER=grandTotalMap.get("PAYROL_DD_WATER")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_WATER)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_CONADV=0>
                                <#if grandTotalMap.get("PAYROL_DD_CONADV")?has_content>
                                <#assign PAYROL_DD_CONADV=grandTotalMap.get("PAYROL_DD_CONADV")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_CONADV)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign grTotalDeductions=0>
                                <#if grandTotalMap.get("grTotalDeductions")?has_content>
                                <#assign grTotalDeductions=grandTotalMap.get("grTotalDeductions")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*grTotalDeductions)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                                <fo:table-cell>
                                <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_SPLPAY=0>
                                <#if grandTotalMap.get("PAYROL_BEN_SPLPAY")?has_content>
                                <#assign PAYROL_BEN_SPLPAY=grandTotalMap.get("PAYROL_BEN_SPLPAY")>
                                </#if>
                                  <fo:block keep-together="always" text-align="right">${PAYROL_BEN_SPLPAY?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_OTALW=0>
                                <#if grandTotalMap.get("PAYROL_BEN_OTALW")?has_content>
                                <#assign PAYROL_BEN_OTALW=grandTotalMap.get("PAYROL_BEN_OTALW")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${PAYROL_BEN_OTALW?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_MISCONE=0>
                                <#if grandTotalMap.get("PAYROL_BEN_MISCONE")?has_content>
                                <#assign PAYROL_BEN_MISCONE=grandTotalMap.get("PAYROL_BEN_MISCONE")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${PAYROL_BEN_MISCONE?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_GPF=0>
                                <#if grandTotalMap.get("PAYROL_DD_GPF")?has_content>
                                <#assign PAYROL_DD_GPF=grandTotalMap.get("PAYROL_DD_GPF")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_GPF)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_IT=0>
                                <#if grandTotalMap.get("PAYROL_DD_IT")?has_content>
                                <#assign PAYROL_DD_IT=grandTotalMap.get("PAYROL_DD_IT")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_IT)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_MRGLN=0>
                                <#if grandTotalMap.get("PAYROL_DD_MRGLN")?has_content>
                                <#assign PAYROL_DD_MRGLN=grandTotalMap.get("PAYROL_DD_MRGLN")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_MRGLN)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign grNetAmt=0>
                                <#if grandTotalMap.get("grNetAmt")?has_content>
                                <#assign grNetAmt=grandTotalMap.get("grNetAmt")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${grNetAmt?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                                <fo:table-cell>
                                <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_PNLPAY=0>
                                <#if grandTotalMap.get("PAYROL_BEN_PNLPAY")?has_content>
                                <#assign PAYROL_BEN_PNLPAY=grandTotalMap.get("PAYROL_BEN_PNLPAY")>
                                </#if>
                                  <fo:block keep-together="always" text-align="right">${PAYROL_BEN_PNLPAY?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_HDALW=0>
                                <#if grandTotalMap.get("PAYROL_BEN_HDALW")?has_content>
                                <#assign PAYROL_BEN_HDALW=grandTotalMap.get("PAYROL_BEN_HDALW")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${PAYROL_BEN_HDALW?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_MISCTWO=0>
                                <#if grandTotalMap.get("PAYROL_BEN_MISCTWO")?has_content>
                                <#assign PAYROL_BEN_MISCTWO=grandTotalMap.get("PAYROL_BEN_MISCTWO")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${PAYROL_BEN_MISCTWO?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_VOLPF=0>
                                <#if grandTotalMap.get("PAYROL_DD_VOLPF")?has_content>
                                <#assign PAYROL_DD_VOLPF=grandTotalMap.get("PAYROL_DD_VOLPF")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_VOLPF)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_COURT=0>
                                <#if grandTotalMap.get("PAYROL_DD_COURT")?has_content>
                                <#assign PAYROL_DD_COURT=grandTotalMap.get("PAYROL_DD_COURT")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_COURT)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_MISAPP=0>
                                <#if grandTotalMap.get("PAYROL_DD_MISAPP")?has_content>
                                <#assign PAYROL_DD_MISAPP=grandTotalMap.get("PAYROL_DD_MISAPP")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_MISAPP)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign grRndNetAmt=0>
                                <#if grandTotalMap.get("grRndNetAmt")?has_content>
                                <#assign grRndNetAmt=grandTotalMap.get("grRndNetAmt")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${grRndNetAmt?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                                <fo:table-cell>
                                <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_FPP=0>
                                <#if grandTotalMap.get("PAYROL_BEN_FPP")?has_content>
                                <#assign PAYROL_BEN_FPP=grandTotalMap.get("PAYROL_BEN_FPP")>
                                </#if>
                                  <fo:block keep-together="always" text-align="right">${PAYROL_BEN_FPP?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_OPALW=0>
                                <#if grandTotalMap.get("PAYROL_BEN_OPALW")?has_content>
                                <#assign PAYROL_BEN_OPALW=grandTotalMap.get("PAYROL_BEN_OPALW")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${PAYROL_BEN_OPALW?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_MISCTHREE=0>
                                <#if grandTotalMap.get("PAYROL_BEN_MISCTHREE")?has_content>
                                <#assign PAYROL_BEN_MISCTHREE=grandTotalMap.get("PAYROL_BEN_MISCTHREE")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${PAYROL_BEN_MISCTHREE?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_GPFLN=0>
                                <#if grandTotalMap.get("PAYROL_DD_GPFLN")?has_content>
                                <#assign PAYROL_DD_GPFLN=grandTotalMap.get("PAYROL_DD_GPFLN")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_GPFLN)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_ELECT=0>
                                <#if grandTotalMap.get("PAYROL_DD_ELECT")?has_content>
                                <#assign PAYROL_DD_ELECT=grandTotalMap.get("PAYROL_DD_ELECT")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_ELECT)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_DPTDUES=0>
                                <#if grandTotalMap.get("PAYROL_DD_DPTDUES")?has_content>
                                <#assign PAYROL_DD_DPTDUES=grandTotalMap.get("PAYROL_DD_DPTDUES")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_DPTDUES)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                                <fo:table-cell>
                                <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_DA=0>
                                <#if grandTotalMap.get("PAYROL_BEN_DA")?has_content>
                                <#assign PAYROL_BEN_DA=grandTotalMap.get("PAYROL_BEN_DA")>
                                </#if>
                                  <fo:block keep-together="always" text-align="right">${PAYROL_BEN_DA?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_ICALW=0>
                                <#if grandTotalMap.get("PAYROL_BEN_ICALW")?has_content>
                                <#assign PAYROL_BEN_ICALW=grandTotalMap.get("PAYROL_BEN_ICALW")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${PAYROL_BEN_ICALW?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign grTotalEarnings=0>
                                <#if grandTotalMap.get("grTotalEarnings")?has_content>
                                <#assign grTotalEarnings=grandTotalMap.get("grTotalEarnings")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${grTotalEarnings?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_EPFREF=0>
                                <#if grandTotalMap.get("PAYROL_DD_EPFREF")?has_content>
                                <#assign PAYROL_DD_EPFREF=grandTotalMap.get("PAYROL_DD_EPFREF")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_EPFREF)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_PAYADV=0>
                                <#if grandTotalMap.get("PAYROL_DD_PAYADV")?has_content>
                                <#assign PAYROL_DD_PAYADV=grandTotalMap.get("PAYROL_DD_PAYADV")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_PAYADV)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_WELFARE=0>
                                <#if grandTotalMap.get("PAYROL_DD_WELFARE")?has_content>
                                <#assign PAYROL_DD_WELFARE=grandTotalMap.get("PAYROL_DD_WELFARE")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_WELFARE)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                                <fo:table-cell>
                                <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_HRA=0>
                                <#if grandTotalMap.get("PAYROL_BEN_HRA")?has_content>
                                <#assign PAYROL_BEN_HRA=grandTotalMap.get("PAYROL_BEN_HRA")>
                                </#if>
                                  <fo:block keep-together="always" text-align="right">${PAYROL_BEN_HRA?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_CONALW=0>
                                <#if grandTotalMap.get("PAYROL_BEN_CONALW")?has_content>
                                <#assign PAYROL_BEN_CONALW=grandTotalMap.get("PAYROL_BEN_CONALW")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${PAYROL_BEN_CONALW?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_ESI=0>
                                <#if grandTotalMap.get("PAYROL_DD_ESI")?has_content>
                                <#assign PAYROL_DD_ESI=grandTotalMap.get("PAYROL_DD_ESI")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_ESI)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_TOURADV=0>
                                <#if grandTotalMap.get("PAYROL_DD_TOURADV")?has_content>
                                <#assign PAYROL_DD_TOURADV=grandTotalMap.get("PAYROL_DD_TOURADV")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_TOURADV)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign grLoanAmt=0>
                                <#if grandTotalMap.get("grLoanAmt")?has_content>
                                <#assign grLoanAmt=grandTotalMap.get("grLoanAmt")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*grLoanAmt)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                                <fo:table-cell>
                                <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_CCA=0>
                                <#if grandTotalMap.get("PAYROL_BEN_CCA")?has_content>
                                <#assign PAYROL_BEN_CCA=grandTotalMap.get("PAYROL_BEN_CCA")>
                                </#if>
                                  <fo:block keep-together="always" text-align="right">${PAYROL_BEN_CCA?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_MEDALW=0>
                                <#if grandTotalMap.get("PAYROL_BEN_MEDALW")?has_content>
                                <#assign PAYROL_BEN_MEDALW=grandTotalMap.get("PAYROL_BEN_MEDALW")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${PAYROL_BEN_MEDALW?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_GIS=0>
                                <#if grandTotalMap.get("PAYROL_DD_GIS")?has_content>
                                <#assign PAYROL_DD_GIS=grandTotalMap.get("PAYROL_DD_GIS")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_GIS)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_FESTADV=0>
                                <#if grandTotalMap.get("PAYROL_DD_FESTADV")?has_content>
                                <#assign PAYROL_DD_FESTADV=grandTotalMap.get("PAYROL_DD_FESTADV")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_FESTADV)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_MILKCARDS=0>
                                <#if grandTotalMap.get("PAYROL_DD_MILKCARDS")?has_content>
                                <#assign PAYROL_DD_MILKCARDS=grandTotalMap.get("PAYROL_DD_MILKCARDS")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_MILKCARDS)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                                <fo:table-cell>
                                <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_IR=0>
                                <#if grandTotalMap.get("PAYROL_BEN_IR")?has_content>
                                <#assign PAYROL_BEN_IR=grandTotalMap.get("PAYROL_BEN_IR")>
                                </#if>
                                  <fo:block keep-together="always" text-align="right">${PAYROL_BEN_IR?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_SPLALW=0>
                                <#if grandTotalMap.get("PAYROL_BEN_SPLALW")?has_content>
                                <#assign PAYROL_BEN_SPLALW=grandTotalMap.get("PAYROL_BEN_SPLALW")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${PAYROL_BEN_SPLALW?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_PTAX=0>
                                <#if grandTotalMap.get("PAYROL_DD_PTAX")?has_content>
                                <#assign PAYROL_DD_PTAX=grandTotalMap.get("PAYROL_DD_PTAX")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_PTAX)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_EDNADV=0>
                                <#if grandTotalMap.get("PAYROL_DD_EDNADV")?has_content>
                                <#assign PAYROL_DD_EDNADV=grandTotalMap.get("PAYROL_DD_EDNADV")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_EDNADV)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_MILKDUES=0>
                                <#if grandTotalMap.get("PAYROL_DD_MILKDUES")?has_content>
                                <#assign PAYROL_DD_MILKDUES=grandTotalMap.get("PAYROL_DD_MILKDUES")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_MILKDUES)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                                <fo:table-cell>
                                <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_NDALW=0>
                                <#if grandTotalMap.get("PAYROL_BEN_NDALW")?has_content>
                                <#assign PAYROL_BEN_NDALW=grandTotalMap.get("PAYROL_BEN_NDALW")>
                                </#if>
                                  <fo:block keep-together="always" text-align="right">${PAYROL_BEN_NDALW?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_WASHALW=0>
                                <#if grandTotalMap.get("PAYROL_BEN_WASHALW")?has_content>
                                <#assign PAYROL_BEN_WASHALW=grandTotalMap.get("PAYROL_BEN_WASHALW")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${PAYROL_BEN_WASHALW?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_SSS=0>
                                <#if grandTotalMap.get("PAYROL_DD_SSS")?has_content>
                                <#assign PAYROL_DD_SSS=grandTotalMap.get("PAYROL_DD_SSS")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_SSS)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_MEDADV=0>
                                <#if grandTotalMap.get("PAYROL_DD_MEDADV")?has_content>
                                <#assign PAYROL_DD_MEDADV=grandTotalMap.get("PAYROL_DD_MEDADV")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_MEDADV)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_APGLIFLN=0>
                                <#if grandTotalMap.get("PAYROL_DD_APGLIFLN")?has_content>
                                <#assign PAYROL_DD_APGLIFLN=grandTotalMap.get("PAYROL_DD_APGLIFLN")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_APGLIFLN)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                                <fo:table-cell>
                                <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_CBALW=0>
                                <#if grandTotalMap.get("PAYROL_BEN_CBALW")?has_content>
                                <#assign PAYROL_BEN_CBALW=grandTotalMap.get("PAYROL_BEN_CBALW")>
                                </#if>
                                  <fo:block keep-together="always" text-align="right">${PAYROL_BEN_CBALW?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_BEN_EXTALW=0>
                                <#if grandTotalMap.get("PAYROL_BEN_EXTALW")?has_content>
                                <#assign PAYROL_BEN_EXTALW=grandTotalMap.get("PAYROL_BEN_EXTALW")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${PAYROL_BEN_EXTALW?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_HRR=0>
                                <#if grandTotalMap.get("PAYROL_DD_HRR")?has_content>
                                <#assign PAYROL_DD_HRR=grandTotalMap.get("PAYROL_DD_HRR")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_HRR)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                <#assign PAYROL_DD_DPTHB=0>
                                <#if grandTotalMap.get("PAYROL_DD_DPTHB")?has_content>
                                <#assign PAYROL_DD_DPTHB=grandTotalMap.get("PAYROL_DD_DPTHB")>
                                </#if>
                                    <fo:block keep-together="always" text-align="right">${((-1)*PAYROL_DD_DPTHB)?if_exists?string("##0.00")}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block keep-together="always" text-align="right"></fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                        	<fo:table-cell>
                        		 <fo:block font-size="7pt">-------------------------------------------------------------------------------------------------------------------------------------------------------------------</fo:block>
                        	</fo:table-cell>
                        </fo:table-row>
                        </fo:table-body>                        
                    </fo:table>  
                </fo:block>
                </#if>
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