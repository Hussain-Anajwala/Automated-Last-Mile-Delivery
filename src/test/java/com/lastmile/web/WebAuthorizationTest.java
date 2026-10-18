package com.lastmile.web;

import static com.lastmile.support.TestData.principal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lastmile.delivery.DeliveryView;
import com.lastmile.support.IntegrationTest;
import com.lastmile.support.TestData;
import com.lastmile.user.Role;
import com.lastmile.user.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** Crafted browser requests must be refused server-side, whatever the UI shows (risk R7). */
@IntegrationTest
@AutoConfigureMockMvc
class WebAuthorizationTest {

    @Autowired MockMvc mvc;
    @Autowired TestData data;

    UserAccount admin;
    UserAccount dispatcher;
    UserAccount agent;
    UserAccount otherAgent;
    DeliveryView mine;
    DeliveryView theirs;

    @BeforeEach
    void setUp() {
        data.reset();
        admin = data.user("Ada Admin", Role.ADMIN);
        dispatcher = data.user("Dina Dispatcher", Role.DISPATCHER);
        agent = data.user("Ari Agent", Role.AGENT);
        otherAgent = data.user("Oli Agent", Role.AGENT);
        mine = data.delivery(dispatcher, agent, "Mine Recipient", "REF-MINE");
        theirs = data.delivery(dispatcher, otherAgent, "Theirs Recipient", "REF-THEIRS");
    }

    @Test
    void agentListShowsOnlyOwnDeliveriesEvenWithACraftedAgentFilter() throws Exception {
        mvc.perform(get("/deliveries").param("agentId", String.valueOf(otherAgent.getId())).with(user(principal(agent))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(mine.deliveryCode())))
                .andExpect(content().string(not(containsString(theirs.deliveryCode()))))
                .andExpect(content().string(not(containsString("data-testid=\"list-create-delivery\""))));
    }

    @Test
    void agentGets404ForAnotherAgentsDeliveryAndItsStatusEndpoint() throws Exception {
        mvc.perform(get("/deliveries/{id}", theirs.id()).with(user(principal(agent))))
                .andExpect(status().isNotFound())
                .andExpect(content().string(not(containsString("Theirs Recipient"))));
        mvc.perform(post("/deliveries/{id}/status", theirs.id()).with(csrf()).with(user(principal(agent)))
                        .param("newStatus", "OUT_FOR_DELIVERY"))
                .andExpect(status().isNotFound());
        assertThat(data.status(theirs.id())).isEqualTo("ASSIGNED");
    }

    @Test
    void agentCannotUseStaffEndpointsEvenOnOwnDelivery() throws Exception {
        mvc.perform(get("/deliveries/{id}/edit", mine.id()).with(user(principal(agent)))).andExpect(status().isForbidden());
        mvc.perform(post("/deliveries/{id}/edit", mine.id()).with(csrf()).with(user(principal(agent)))
                .param("recipientName", "Changed")).andExpect(status().isForbidden());
        mvc.perform(post("/deliveries/{id}/assignment", mine.id()).with(csrf()).with(user(principal(agent)))
                .param("agentId", String.valueOf(otherAgent.getId()))).andExpect(status().isForbidden());
        mvc.perform(get("/admin/users").with(user(principal(agent)))).andExpect(status().isForbidden());
    }

    @Test
    void onlyAdminsSeeTheUserList() throws Exception {
        mvc.perform(get("/admin/users").with(user(principal(dispatcher)))).andExpect(status().isForbidden());
        mvc.perform(get("/admin/users").with(user(principal(admin))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ari.agent@lastmile.test")))
                .andExpect(content().string(not(containsString("{bcrypt}"))));
    }

    @Test
    void detailShowsOnlyActionsTheServerWillAccept() throws Exception {
        mvc.perform(get("/deliveries/{id}", mine.id()).with(user(principal(agent))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-testid=\"status-out_for_delivery\"")))
                .andExpect(content().string(not(containsString("data-testid=\"assignment-form\""))))
                .andExpect(content().string(not(containsString("data-testid=\"edit-delivery\""))));
        mvc.perform(get("/deliveries/{id}", mine.id()).with(user(principal(dispatcher))))
                .andExpect(content().string(containsString("data-testid=\"assignment-form\"")))
                .andExpect(content().string(containsString("data-testid=\"edit-delivery\"")));
    }

    @Test
    void agentAdvancesOwnDeliveryThroughTheWebFormAndFailureWithoutReasonIsExplained() throws Exception {
        mvc.perform(post("/deliveries/{id}/status", mine.id()).with(csrf()).with(user(principal(agent)))
                        .param("newStatus", "OUT_FOR_DELIVERY"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("successMessage", containsString("Out for delivery")));
        mvc.perform(post("/deliveries/{id}/status", mine.id()).with(csrf()).with(user(principal(agent)))
                        .param("newStatus", "FAILED").param("reason", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", containsString("reason is required")));
        assertThat(data.status(mine.id())).isEqualTo("OUT_FOR_DELIVERY");
    }

    @Test
    void dashboardForAgentIsScopedAndForStaffIsGlobal() throws Exception {
        mvc.perform(get("/dashboard").with(user(principal(agent))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("assigned to you")))
                .andExpect(content().string(not(containsString(theirs.deliveryCode()))));
        mvc.perform(get("/dashboard").with(user(principal(dispatcher))))
                .andExpect(content().string(containsString("Summary of all deliveries")))
                .andExpect(content().string(containsString(theirs.deliveryCode())));
    }

    @Test
    void searchWithNoMatchesShowsTheEmptyStateWithReset() throws Exception {
        mvc.perform(get("/deliveries").param("q", "no-such-delivery").with(user(principal(dispatcher))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-testid=\"empty-state\"")))
                .andExpect(content().string(containsString("No deliveries match these filters.")));
    }

    @Test
    void unknownStatusFilterIsABadRequestNotAServerError() throws Exception {
        mvc.perform(get("/deliveries").param("status", "LOST").with(user(principal(dispatcher))))
                .andExpect(status().isBadRequest());
    }
}
