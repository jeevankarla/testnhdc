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
	<fo:simple-page-master master-name="main" page-height="12in" page-width="10in"  margin-left=".3in" margin-right=".3in" margin-bottom=".3in" margin-top=".3in">
        <fo:region-body margin-top="0.2in" margin-bottom=".6in"/>
        <fo:region-before extent="1in"/>
        <fo:region-after extent="1in"/>     
    </fo:simple-page-master>   
</fo:layout-master-set>
${setRequestAttribute("OUTPUT_FILENAME", "productPriceRevisionReport.pdf")}
     <#if ProductCatMap?has_content> 
    <fo:page-sequence master-reference="main">
        <fo:static-content font-size="13pt" font-family="Courier,monospace"  flow-name="xsl-region-before" font-weight="bold">
        </fo:static-content>
           <fo:flow flow-name="xsl-region-body"   font-family="Courier,monospace">
			   <fo:block  keep-together="always" text-align="right" font-family="Courier,monospace" font-weight="bold" white-space-collapse="false">&#160; UserLogin : <#if userLogin?exists>${userLogin.userLoginId?if_exists}</#if></fo:block>
			   <fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-weight="bold">&#160;                  ${uiLabelMap.KMFDairyHeader}       Date:${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(nowTimestamp, "dd-MM-yyyy")}</fo:block>
			   <fo:block  keep-together="always" text-align="left" font-family="Courier,monospace" white-space-collapse="false" font-weight="bold">&#160;               ${uiLabelMap.KMFDairySubHeader}            ${uiLabelMap.CommonPage}- <fo:page-number/> </fo:block> 
	           <fo:block text-align="center" keep-together="always"  >---------------------------------------------------------------</fo:block>
	           <fo:block text-align="center" white-space-collapse="false" font-size="12pt"  font-weight="bold" >PRICE HISTORY  BETWEEN ${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(fromDate, "dd-MMM-yyyy")} AND ${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(thruDate, "dd-MMM-yyyy")} </fo:block>                
	           <fo:block font-size="10pt">-------------------------------------------------------------------------------------------------</fo:block>
	           <fo:block>
	              <fo:table text-align="center" >
	                 <fo:table-column column-width="40pt"/>
				     <fo:table-column column-width="60pt"/>
				     <fo:table-column column-width="200pt"/>
					 <fo:table-column column-width="60pt"/>
					 <fo:table-column column-width="120pt"/>
					 <fo:table-column column-width="90pt"/>
	                 <fo:table-body>
	            	    <fo:table-row height="20pt">
	            		   <fo:table-cell>
	            			   <fo:block  keep-together="always" text-align="left" font-weight="bold"  font-size="10pt" white-space-collapse="false">S.NO</fo:block>  
	        			   </fo:table-cell>
	            	       <fo:table-cell>
	            			   <fo:block  keep-together="always" text-align="left" font-weight="bold"  font-size="10pt" white-space-collapse="false">PRODUCT ID</fo:block>  
	        			   </fo:table-cell>
	        			   <fo:table-cell>
	            			   <fo:block  keep-together="always" text-align="center" font-weight="bold"  font-size="10pt" white-space-collapse="false">DESCRIPTION</fo:block>  
	        			   </fo:table-cell>
	        			   <fo:table-cell>
	            			   <fo:block  keep-together="always" text-align="center" font-weight="bold"  font-size="10pt" white-space-collapse="false">FROM DATE</fo:block>  
	        			   </fo:table-cell>
	        			   <fo:table-cell>
	            			   <fo:block  keep-together="always" text-align="center" font-weight="bold"  font-size="10pt" white-space-collapse="false">THRU DATE</fo:block>  
	        			   </fo:table-cell>
	        			   <fo:table-cell>
	        			       <fo:block  keep-together="always" text-align="center" font-weight="bold"  font-size="10pt" white-space-collapse="false">DEFAULT PRICE</fo:block> 
	        			   </fo:table-cell>
	        		   </fo:table-row>
	               </fo:table-body>
	           </fo:table>
	       </fo:block>
	       <fo:block font-size="10pt">-------------------------------------------------------------------------------------------------</fo:block>
            <#assign productCatList = ProductCatMap.entrySet()>
            <#list productCatList as category>
            <#assign productDetails=category.getValue()>  
            <#assign sno=1>
            <#assign productPriceList = productDetails.entrySet()> 
		    <fo:block  keep-together="always" text-align="center"  font-weight="bold" font-size="11pt" white-space-collapse="false">${category.getKey()}</fo:block>
            <fo:block font-size="10pt">-------------------------------------------------------------------------------------------------</fo:block>
            <fo:block>
               <fo:table text-align="center">
	              <fo:table-column column-width="40pt"/>
				  <fo:table-column column-width="70pt"/>
				  <fo:table-column column-width="180pt"/>
				  <fo:table-column column-width="80pt"/>
				  <fo:table-column column-width="100pt"/>
				  <fo:table-column column-width="100pt"/>
                      <fo:table-body>
	                    <#list productPriceList as productList>
                        <#assign productId = productList.getKey()?if_exists>
                        <#assign product = delegator.findOne("Product", {"productId" : productId}, true)?if_exists/>
                        <#assign prodDetails = productList.getValue()>
                        <#assign prodPriceList = prodDetails.entrySet()> 
                        <#list prodPriceList as eachProdPrice>
                        <#assign startDate= eachProdPrice.getKey()?if_exists> 
                           <fo:table-row>
                            <fo:table-cell>
	            			   <fo:block  keep-together="always" text-align="left"  font-size="10pt" white-space-collapse="false">${sno}</fo:block>  
	        			    </fo:table-cell>
	        			    <fo:table-cell>
	            			   <fo:block  keep-together="always" text-align="left"  font-size="10pt" white-space-collapse="false">${product.internalName?if_exists}</fo:block>  
	        			    </fo:table-cell>
                            <fo:table-cell>
	            			   <fo:block text-align="left" font-size="10pt" >${product.description?if_exists}</fo:block>  
	        			    </fo:table-cell>
	        			    <fo:table-cell>
	            			   <fo:block  keep-together="always" text-align="center" font-size="10pt" white-space-collapse="false">${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(startDate, "dd/MM/yyyy")?if_exists}</fo:block> 
	        			    </fo:table-cell>
	        			    <#if eachProdPrice.getValue().get("thruDate")?has_content>
 	        			    	<#assign endDate = eachProdPrice.getValue().get("thruDate")>
	        			    <fo:table-cell>
	            			   <fo:block  keep-together="always" text-align="center" font-size="10pt" white-space-collapse="false">${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(endDate, "dd/MM/yyyy")?if_exists}</fo:block> 
	        			    </fo:table-cell>
                            <#else>
                              <fo:table-cell>
	            			   <fo:block  keep-together="always" text-align="center" font-size="10pt" white-space-collapse="false"></fo:block> 
	        			     </fo:table-cell>
	        			    </#if>
                             <fo:table-cell>
	            			  <fo:block  keep-together="always" text-align="right" font-size="10pt" white-space-collapse="false">${eachProdPrice.getValue().get("DEFAULT_PRICE")?if_exists?string("##0.00")}</fo:block>  
	        			    </fo:table-cell>
	        			</fo:table-row>
	        			 <#assign sno=sno+1>
	        			</#list>
                      </#list>
                    </fo:table-body>
               </fo:table>
           </fo:block> 
           <fo:block font-size="10pt">-------------------------------------------------------------------------------------------------</fo:block>
           </#list>
           <fo:block text-align="left"  keep-together="always"  white-space-collapse="false" linefeed-treatment="preserve">&#xA;</fo:block> 
           <fo:block text-align="left"  keep-together="always"  white-space-collapse="false" linefeed-treatment="preserve">&#xA;</fo:block>   
           <#if CatMap?has_content>
           <fo:block text-align="center" white-space-collapse="false" font-size="12pt"  font-weight="bold" >PRICE REVISION BETWEEN ${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(fromDate, "dd-MMM-yyyy")} AND ${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(thruDate, "dd-MMM-yyyy")} </fo:block>                
           <fo:block font-size="10pt">-------------------------------------------------------------------------------------------------</fo:block>
           <fo:block>
              <fo:table text-align="center">
                 <fo:table-column column-width="40pt"/>
			     <fo:table-column column-width="60pt"/>
			     <fo:table-column column-width="200pt"/>
				 <fo:table-column column-width="60pt"/>
				 <fo:table-column column-width="120pt"/>
				 <fo:table-column column-width="90pt"/>
                 <fo:table-body>
            	    <fo:table-row height="20pt">
            		   <fo:table-cell>
            			   <fo:block  keep-together="always" text-align="left" font-weight="bold"  font-size="10pt" white-space-collapse="false">S.NO</fo:block>  
        			   </fo:table-cell>
            	       <fo:table-cell>
            			   <fo:block  keep-together="always" text-align="left" font-weight="bold"  font-size="10pt" white-space-collapse="false">PRODUCT ID</fo:block>  
        			   </fo:table-cell>
        			   <fo:table-cell>
            			   <fo:block  keep-together="always" text-align="center" font-weight="bold"  font-size="10pt" white-space-collapse="false">DESCRIPTION</fo:block>  
        			   </fo:table-cell>
        			   <fo:table-cell>
            			   <fo:block  keep-together="always" text-align="center" font-weight="bold"  font-size="10pt" white-space-collapse="false">FROM DATE</fo:block>  
        			   </fo:table-cell>
        			   <fo:table-cell>
            			   <fo:block  keep-together="always" text-align="center" font-weight="bold"  font-size="10pt" white-space-collapse="false">THRU DATE</fo:block>  
        			   </fo:table-cell>
        			   <fo:table-cell>
        			       <fo:block  keep-together="always" text-align="center" font-weight="bold"  font-size="10pt" white-space-collapse="false">DEFAULT PRICE</fo:block> 
        			   </fo:table-cell>
        		   </fo:table-row>
               </fo:table-body>
           </fo:table>
       </fo:block>
       <fo:block font-size="10pt">-------------------------------------------------------------------------------------------------</fo:block>
       <#assign CatList = CatMap.entrySet()>
            <#list CatList as eachCategory>
            <#assign eachCategoryDetails=eachCategory.getValue()>  
            <#assign sno=1>
            <#assign prodPricesList = eachCategoryDetails.entrySet()> 
		    <fo:block  keep-together="always" text-align="center"  font-weight="bold" font-size="11pt" white-space-collapse="false">${eachCategory.getKey()}</fo:block>
            <fo:block font-size="10pt">-------------------------------------------------------------------------------------------------</fo:block>
             <fo:block>
               <fo:table text-align="center">
	              <fo:table-column column-width="40pt"/>
				  <fo:table-column column-width="70pt"/>
				  <fo:table-column column-width="180pt"/>
				  <fo:table-column column-width="80pt"/>
				  <fo:table-column column-width="100pt"/>
				  <fo:table-column column-width="100pt"/>
                      <fo:table-body>
	                    <#list prodPricesList as proList>
                           <#assign prodId = proList.getKey()?if_exists>
                           <#assign products = delegator.findOne("Product", {"productId" : prodId}, true)?if_exists/> 
                            <#assign prodDet = proList.getValue()>
                             <#list prodDet as eachProdPrices>
                              <#assign dayBegin = eachProdPrices.getKey()?if_exists>     
                            <fo:table-row>
	                            <fo:table-cell>
		            			   <fo:block  keep-together="always" text-align="left"  font-size="10pt" white-space-collapse="false">${sno}</fo:block>  
		        			    </fo:table-cell>
		        			    <fo:table-cell>
		            			   <fo:block  keep-together="always" text-align="left"  font-size="10pt" white-space-collapse="false">${products.internalName?if_exists}</fo:block>  
		        			    </fo:table-cell>
	                             <fo:table-cell>
		            			   <fo:block  text-align="left"  font-size="10pt" >${products.description?if_exists}</fo:block>  
		        			    </fo:table-cell>
                                <fo:table-cell>
	            			       <fo:block  keep-together="always" text-align="center" font-size="10pt" white-space-collapse="false">${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(dayBegin, "dd/MM/yyyy")?if_exists}</fo:block> 
	        			       </fo:table-cell>
                                <#if eachProdPrices.getValue().get("thruDate")?has_content>
 	        			    	<#assign dayEnd = eachProdPrices.getValue().get("thruDate")>
		        			    <fo:table-cell>
		            			   <fo:block  keep-together="always" text-align="center" font-size="10pt" white-space-collapse="false">${Static["org.ofbiz.base.util.UtilDateTime"].toDateString(dayEnd, "dd/MM/yyyy")?if_exists}</fo:block> 
		        			    </fo:table-cell>
	                            <#else>
	                              <fo:table-cell>
		            			   <fo:block  keep-together="always" text-align="center" font-size="10pt" white-space-collapse="false"></fo:block> 
		        			     </fo:table-cell>
		        			    </#if>
                                <fo:table-cell>
	            			       <fo:block  keep-together="always" text-align="right" font-size="10pt" white-space-collapse="false">${eachProdPrices.getValue().get("DEFAULT_PRICE")?if_exists?string("##0.00")}</fo:block>  
	        			        </fo:table-cell>  
	        			     </fo:table-row> 
	        			     <#assign sno=sno+1>
                           </#list>
                      </#list>
                    </fo:table-body>
               </fo:table>
           </fo:block> 
           <fo:block font-size="10pt">-------------------------------------------------------------------------------------------------</fo:block>
          </#list>  
       </#if>
	   </fo:flow>	
</fo:page-sequence>	
	<#else>
		<fo:page-sequence master-reference="main">
    			<fo:flow flow-name="xsl-region-body" font-family="Courier,monospace">
       		 		<fo:block font-size="14pt" text-align="center">
            			 No Records Found....!
       		 		</fo:block>
    			</fo:flow>
		</fo:page-sequence>
	  </#if>
	</fo:root>
</#escape>		   		     	 