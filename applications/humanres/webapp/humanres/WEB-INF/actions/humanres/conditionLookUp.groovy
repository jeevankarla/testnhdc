/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
 
 
 /*   ********* AjaxConditionLookUp.groovy **********
 * This Groovy add conditional Parameters to Ajax call   
 * based on the screen which Ajax is call has been Made.
 */
 
import org.ofbiz.base.util.*;
import org.ofbiz.entity.condition.*;
import java.net.URL;
import org.ofbiz.base.conversion.NetConverters;
import org.ofbiz.base.conversion.NetConverters.StringToURL;

List roleTypeList=[];
if(parameters.ajaxLookup == 'Y'){
	roleTypeId=parameters.roleTypeId;

// initialising  conditionFields Map 
	
	conditionFields=[:];
//checking for the roleType parameter in Ajax call
//if roleTypeId is null use the default view  "PartyNameView"


roleTypeId ="EMPLOYEE";
if(roleTypeId != null){
	parameters.roleTypeId=roleTypeId;
	context.entityName="PartyRoleAndPartyDetail";
	
	if(parameters.invoiceTypeId == "PAYROL_INVOICE"){
		roleTypeList.add("EMPLOYEE");
		roleTypeList.add("INTERNAL_ORGANIZATIO");
		conditionFields.roleTypeId=roleTypeList;
		context.conditionFields=conditionFields;
		
	}
	else{
		conditionFields.roleTypeId=roleTypeId;
		context.conditionFields=conditionFields;	
	}
	
}

}
