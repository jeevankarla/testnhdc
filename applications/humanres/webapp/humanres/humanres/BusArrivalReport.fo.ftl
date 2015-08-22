<#escape x as x?xml>
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
        <fo:layout-master-set>
            <fo:simple-page-master master-name="main" page-height="11in" page-width="10in"
                     margin-left="0.1in" margin-right="0.2in"  margin-top="0.2in" margin-bottom="0.2in" >
                <fo:region-body margin-top="1.8in"/>
                <fo:region-before extent="1in"/>
                <fo:region-after extent="1in"/>
            </fo:simple-page-master>
        </fo:layout-master-set>
        ${setRequestAttribute("OUTPUT_FILENAME", "EmployeeMisPunchData.pdf")}
		
		<#if finalMap?has_content>
			<#assign noofLines=1>
			<fo:page-sequence master-reference="main">
        		<fo:static-content font-size="14pt" font-family="Courier,monospace"  flow-name="xsl-region-before" font-weight="bold">        
	        		<#assign reportHeader = delegator.findOne("TenantConfiguration", {"propertyTypeEnumId" : "COMPANY_HEADER","propertyName" : "reportHeaderLable"}, true)>
                    <#assign reportSubHeader = delegator.findOne("TenantConfiguration", {"propertyTypeEnumId" : "COMPANY_HEADER","propertyName" : "reportSubHeaderLable"}, true)>      
	        		<fo:block text-align="center" font-size="13pt" keep-together="always"  white-space-collapse="false" font-weight="bold">&#160;${reportHeader.description?if_exists}</fo:block>
                    <fo:block text-align="center" font-size="12pt" keep-together="always"  white-space-collapse="false" font-weight="bold">&#160;${reportSubHeader.description?if_exists}</fo:block>
	        		<fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-weight="bold">&#160;      </fo:block>
	        		<fo:block text-align="left" keep-together="always" white-space-collapse="false">&#160;                   BUS ARRIVAL REPORT FROM ${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(fromDate, "dd/MM/yyyy")} TO ${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(thruDate, "dd/MM/yyyy")}        </fo:block>	
	        		<fo:block  keep-together="always" text-align="center" font-family="Courier,monospace" white-space-collapse="false" font-weight="bold">&#160;      </fo:block> 
	        		<fo:block text-align="left" keep-together="always" white-space-collapse="false">&#160;                                                                DATE: ${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(nowTimestamp, "dd-MMM-yyyy")}</fo:block>	 
	        		<fo:block text-align="left" keep-together="always" white-space-collapse="false">&#160;                                                                PAGE: <fo:page-number/></fo:block>	 	 	  	 	  
	        		<fo:block text-align="left" keep-together="always" font-size="12pt">---------------------------------------------------------------------------------------------</fo:block>
          		</fo:static-content>
          		<fo:flow flow-name="xsl-region-body" font-family="Courier,monospace">
	            	<fo:block font-family="Courier,monospace">
	                	<fo:table >
		                	<fo:table-column column-width="200pt"/>
		                    <fo:table-column column-width="120pt"/>
		                    <fo:table-column column-width="150pt"/>                
		                    <fo:table-column column-width="170pt"/>
	                		<fo:table-body> 
	                     		<fo:table-row >
		                            <fo:table-cell >	
		                            	<fo:block text-align="left" keep-together="always" font-weight="bold" font-size="15pt">Date</fo:block>
		                            </fo:table-cell>
		                            <fo:table-cell >	
		                            	<fo:block text-align="left" keep-together="always" font-weight="bold" font-size="15pt">Shift</fo:block>
		                            </fo:table-cell>
		                            <fo:table-cell >	
		                            	<fo:block text-align="right" keep-together="always" font-weight="bold" font-size="15pt">Shift Time</fo:block>
		                            </fo:table-cell>
		                            <fo:table-cell >	
		                            	<fo:block text-align="right" keep-together="always" font-weight="bold" font-size="15pt">Arrival Time</fo:block>
		                            </fo:table-cell>
			                   	</fo:table-row>
			                   	<fo:table-row >
			                   		<fo:table-cell >	
	                            		<fo:block keep-together="always" font-weight="bold">---------------------------------------------------------------------------------------------</fo:block>
	                            	</fo:table-cell>
	                            </fo:table-row>
                            	<#assign busArrivalMap=finalMap.entrySet()>
	                   			<#list busArrivalMap as busArrivalDetails>
	                   				<#assign busArrivalValues=busArrivalDetails.getValue().entrySet()>
	                   				<#list busArrivalValues as busArrivalTimings>
	                   					<#assign busRouteValues=busArrivalTimings.getValue().entrySet()>
	                   					<#list busRouteValues as busRouteDetails>
			                   				<fo:table-row >
						                   		<fo:table-cell >	
				                            		<fo:block keep-together="always" text-align="left" font-size="15pt">${busArrivalDetails.getKey()?if_exists}</fo:block>
				                            	</fo:table-cell>
				                            	<fo:table-cell >	
				                            		<fo:block keep-together="always" text-align="left" font-size="15pt">${busArrivalTimings.getKey()?if_exists}</fo:block>
				                            	</fo:table-cell>
				                            	<fo:table-cell >	
				                            		<fo:block keep-together="always" text-align="right" font-size="15pt">${busRouteDetails.getValue().get("shiftTime")?if_exists?string("HH:mm")}</fo:block>
				                            	</fo:table-cell>
				                            	<fo:table-cell >	
				                            		<fo:block keep-together="always" text-align="right" font-size="15pt">${busRouteDetails.getValue().get("arrivalTime")?if_exists?string("HH:mm")}</fo:block>
				                            	</fo:table-cell>
				                            	<#assign noofLines=noofLines+1>
				                            </fo:table-row>
				                            <#if (noofLines >= 31)>
					                     		<#assign noofLines=1>
						                     	<fo:table-row>
					                            	<fo:table-cell >	
					                            		<fo:block page-break-after="always"></fo:block>
					                            	</fo:table-cell>
					                            </fo:table-row>
					                        </#if>
					                   	</#list>
			                      	</#list>
			                      	<fo:table-row >
				                   		<fo:table-cell >	
		                            		<fo:block keep-together="always" font-weight="bold">&#160;</fo:block>
		                            	</fo:table-cell>
		                            	<#assign noofLines=noofLines+1>
		                            </fo:table-row>
		                            <#if (noofLines >= 31)>
			                     		<#assign noofLines=1>
				                     	<fo:table-row>
			                            	<fo:table-cell >	
			                            		<fo:block page-break-after="always"></fo:block>
			                            	</fo:table-cell>
			                            </fo:table-row>
			                        </#if>
		                       	</#list>
		                       	<fo:table-row >
			                   		<fo:table-cell >	
	                            		<fo:block keep-together="always" font-weight="bold">---------------------------------------------------------------------------------------------</fo:block>
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
		            	${uiLabelMap.NoEmployeeFound}.
		       		 </fo:block>
		    	</fo:flow>
			</fo:page-sequence>
  		</#if>
     </fo:root>
</#escape>