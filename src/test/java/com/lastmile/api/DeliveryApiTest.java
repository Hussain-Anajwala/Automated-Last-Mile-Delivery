package com.lastmile.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lastmile.delivery.DeliveryView;
import com.lastmile.support.IntegrationTest;
import com.lastmile.support.TestData;
import com.lastmile.user.Role;
import com.lastmile.user.UserAccount;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@IntegrationTest
@AutoConfigureMockMvc
class DeliveryApiTest {

    private static final String VALID_JSON = """
            {"recipientName":"Aarav Sharma","recipientPhone":"9876543210","deliveryAddress":"Example Street 12",
             "deliveryCity":"Mumbai","referenceNumber":"ORD-1001","assignedAgentId":null,
             "status":"DELIVERED","deliveryCode":"HACK","id":999}""";

    @Autowired MockMvc mvc;
    @Autowired TestData data;

    UserAccount dispatcher;
    UserAccount agent;
    UserAccount otherAgent;

    @BeforeEach
    void setUp() {
        data.reset();
        dispatcher = data.user("Dina Dispatcher", Role.DISPATCHER);
        agent = data.user("Ari Agent", Role.AGENT);
        otherAgent = data.user("Oli Agent", Role.AGENT);
    }

    private static RequestPostProcessor as(UserAccount user) {
        return httpBasic(user.getEmail(), TestData.PASSWORD);
    }

    @Test
    void requiresAuthenticationAndHealthIsPublic() throws Exception {
        mvc.perform(get("/api/v1/deliveries")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/health/live")).andExpect(status().isOk());
    }

    @Test
    void browserSessionCookiesAreNotAcceptedByTheApi() throws Exception {
        HttpSession session = mvc.perform(formLogin("/login").user(dispatcher.getEmail()).password(TestData.PASSWORD))
                .andReturn().getRequest().getSession();
        mvc.perform(post("/api/v1/deliveries").session((MockHttpSession) session)
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_JSON))
                .andExpect(status().isUnauthorized());
        assertThat(data.count("deliveries")).isZero();
    }

    @Test
    void dispatcherCreatesAndSystemFieldsInTheBodyAreIgnored() throws Exception {
        mvc.perform(post("/api/v1/deliveries").with(as(dispatcher))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/v1/deliveries/")))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.deliveryCode").value(startsWith("LMD-")))
                .andExpect(jsonPath("$.createdByName").value("Dina Dispatcher"));
    }

    @Test
    void validationErrorsAre400WithFieldDetails() throws Exception {
        mvc.perform(post("/api/v1/deliveries").with(as(dispatcher)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipientName\":\"\",\"recipientPhone\":\"abc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field").value(hasItem("recipientPhone")))
                .andExpect(jsonPath("$.fieldErrors[*].field").value(hasItem("deliveryCity")));
        mvc.perform(post("/api/v1/deliveries").with(as(dispatcher)).contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void agentCannotCreateUpdateOrAssign() throws Exception {
        DeliveryView own = data.delivery(dispatcher, agent);
        mvc.perform(post("/api/v1/deliveries").with(as(agent)).contentType(MediaType.APPLICATION_JSON).content(VALID_JSON))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/deliveries/{id}", own.id()).with(as(agent)).contentType(MediaType.APPLICATION_JSON)
                .content(VALID_JSON)).andExpect(status().isForbidden());
        mvc.perform(patch("/api/v1/deliveries/{id}/assignment", own.id()).with(as(agent))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"agentId\":" + otherAgent.getId() + "}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void statusEndpointEnforcesOwnershipStateMachineAndReason() throws Exception {
        DeliveryView own = data.delivery(dispatcher, agent);
        DeliveryView foreign = data.delivery(dispatcher, otherAgent);

        mvc.perform(patch("/api/v1/deliveries/{id}/status", foreign.id()).with(as(agent))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"newStatus\":\"OUT_FOR_DELIVERY\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(patch("/api/v1/deliveries/{id}/status", own.id()).with(as(agent))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"newStatus\":\"DELIVERED\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(startsWith("Cannot change status")));
        mvc.perform(patch("/api/v1/deliveries/{id}/status", own.id()).with(as(agent))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"newStatus\":\"OUT_FOR_DELIVERY\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OUT_FOR_DELIVERY"));
        mvc.perform(patch("/api/v1/deliveries/{id}/status", own.id()).with(as(agent))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"newStatus\":\"FAILED\",\"reason\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("reason"));
        mvc.perform(patch("/api/v1/deliveries/{id}/status", own.id()).with(as(agent))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"newStatus\":\"LOST\"}"))
                .andExpect(status().isBadRequest());
        assertThat(data.status(own.id())).isEqualTo("OUT_FOR_DELIVERY");
    }

    @Test
    void staleExpectedVersionIs409() throws Exception {
        DeliveryView own = data.delivery(dispatcher, agent);
        mvc.perform(patch("/api/v1/deliveries/{id}/status", own.id()).with(as(agent)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newStatus\":\"OUT_FOR_DELIVERY\",\"expectedVersion\":" + (own.version() + 5) + "}"))
                .andExpect(status().isConflict());
    }

    @Test
    void listAndDashboardAreScopedForAgents() throws Exception {
        DeliveryView own = data.delivery(dispatcher, agent);
        data.delivery(dispatcher, otherAgent);
        mvc.perform(get("/api/v1/deliveries").param("agentId", String.valueOf(otherAgent.getId())).with(as(agent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].deliveryCode").value(own.deliveryCode()))
                .andExpect(jsonPath("$.content[0].recipientPhone").doesNotExist());
        mvc.perform(get("/api/v1/dashboard/summary").with(as(agent)))
                .andExpect(jsonPath("$.scope").value("MY_ASSIGNMENTS"))
                .andExpect(jsonPath("$.total").value(1));
    }
}
