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
 import org.ofbiz.entity.GenericValue;
 import org.ofbiz.entity.condition.*;
 import org.ofbiz.entity.util.EntityUtil;
 import org.ofbiz.base.util.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.math.BigDecimal;
 import java.sql.Timestamp;
 import org.ofbiz.base.util.UtilMisc;
 import java.text.ParseException;
 
 import org.ofbiz.base.util.UtilDateTime;
 import org.ofbiz.base.util.UtilValidate;
 import org.ofbiz.entity.util.EntityUtil;
 import org.ofbiz.entity.condition.EntityCondition;
 import org.ofbiz.entity.condition.EntityOperator;
 

conditionList = [];
if(parameters.custRequestId){
conditionList.add(EntityCondition.makeCondition("custRequestId", EntityOperator.EQUALS, parameters.custRequestId));
}
conditionList.add(EntityCondition.makeCondition("finstatusId", EntityOperator.EQUALS, "CREATED"));
if(parameters.AccDate){
conditionList.add(EntityCondition.makeCondition("custRequestDate", EntityOperator.EQUALS, parameters.AccDate));
}
condition = EntityCondition.makeCondition(conditionList, EntityOperator.AND);
custRequestList = delegator.findList("CustRequest", condition, null, null, null, true);
 
 context.custRequestList = custRequestList;

 