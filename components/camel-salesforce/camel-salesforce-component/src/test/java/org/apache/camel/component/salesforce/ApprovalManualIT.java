/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.salesforce;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.camel.component.salesforce.api.dto.approval.ApprovalRequest;
import org.apache.camel.component.salesforce.api.dto.approval.ApprovalRequest.Action;
import org.apache.camel.component.salesforce.api.dto.approval.ApprovalResult;
import org.apache.camel.component.salesforce.api.dto.approval.Approvals;
import org.apache.camel.component.salesforce.api.dto.approval.Approvals.Info;
import org.apache.camel.test.junit5.params.Parameter;
import org.apache.camel.test.junit5.params.Parameterized;
import org.apache.camel.test.junit5.params.Parameters;
import org.apache.camel.test.junit5.params.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Parameterized
public class ApprovalManualIT extends AbstractApprovalManualIT {

    @Parameter
    private String format;

    public ApprovalManualIT() {
        super(5);
    }

    @Test
    public void shouldSubmitAndFetchApprovals() {
        final ApprovalResult approvalResult = template.requestBody(String.format("salesforce:approval?"//
                                                                                 + "format=%s"//
                                                                                 + "&approvalActionType=Submit"//
                                                                                 + "&approvalContextId=%s"//
                                                                                 + "&approvalNextApproverIds=%s"//
                                                                                 + "&approvalComments=Integration test"//
                                                                                 + "&approvalProcessDefinitionNameOrId=Test_Account_Process",
                format, accountIds.get(0), userId),
                NOT_USED, ApprovalResult.class);

        assertNotNull(approvalResult, "Approval should have resulted in value");

        assertEquals(1, approvalResult.size(), "There should be one Account waiting approval");

        assertEquals("Pending", approvalResult.iterator().next().getInstanceStatus(),
                "Instance status of the item in approval result should be `Pending`");

        // as it stands on 18.11.2016. the GET method on
        // /vXX.X/process/approvals/ with Accept other than
        // `application/json` results in HTTP status 500, so only JSON is
        // supported
        final Approvals approvals = template.requestBody("salesforce:approvals", NOT_USED, Approvals.class);

        assertNotNull(approvals, "Approvals should be fetched");

        final List<Info> accountApprovals = approvals.approvalsFor("Account");
        assertEquals(1, accountApprovals.size(), "There should be one Account waiting approval");
    }

    @Test
    public void shouldSubmitBulkApprovals() {
        final List<ApprovalRequest> approvalRequests = accountIds.stream().map(id -> {
            final ApprovalRequest request = new ApprovalRequest();
            request.setContextId(id);
            request.setComments("Approval for " + id);
            request.setActionType(Action.Submit);

            return request;
        }).collect(Collectors.toList());

        final ApprovalResult approvalResult = template.requestBody(String.format("salesforce:approval?"//
                                                                                 + "format=%s"//
                                                                                 + "&approvalActionType=Submit"//
                                                                                 + "&approvalNextApproverIds=%s"//
                                                                                 + "&approvalProcessDefinitionNameOrId=Test_Account_Process",
                format, userId),
                approvalRequests, ApprovalResult.class);

        assertEquals(approvalRequests.size(), approvalResult.size(), "Should have same number of approval results as requests");
    }

    @Parameters
    public static Iterable<String> formats() {
        return Arrays.asList("JSON", "XML");
    }

}
