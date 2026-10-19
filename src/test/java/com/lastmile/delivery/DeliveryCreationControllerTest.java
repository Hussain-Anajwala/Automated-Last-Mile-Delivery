package com.lastmile.delivery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lastmile.support.IntegrationTest;
import com.lastmile.support.TestData;
import com.lastmile.user.Role;
import com.lastmile.user.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@IntegrationTest
@AutoConfigureMockMvc
class DeliveryCreationControllerTest {

    @Autowired MockMvc mvc;
    @Autowired TestData data;
    @Autowired JdbcTemplate jdbc;

    UserAccount dispatcher;
    UserAccount agent;

    @BeforeEach
    void setUp() {
        data.reset();
        dispatcher = data.user("Dina Dispatcher", Role.DISPATCHER);
        agent = data.user("Ari Agent", Role.AGENT);
    }

    private MockHttpServletRequestBuilder validPost() {
        return post("/deliveries/new")
                .param("recipientName", "Aarav Sharma")
                .param("recipientPhone", "+919876543210")
                .param("deliveryAddress", "Example Street, Apartment 12")
                .param("deliveryCity", "Mumbai")
                .param("referenceNumber", "ORD-1001");
    }

    @Test
    void dispatcherSeesTheFormWithActiveAgents() throws Exception {
        mvc.perform(get("/deliveries/new").with(user(TestData.principal(dispatcher))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-testid=\"delivery-form\"")))
                .andExpect(content().string(containsString("Ari Agent")));
    }

    @Test
    void unauthenticatedUserIsRedirectedToLogin() throws Exception {
        mvc.perform(get("/deliveries/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void agentIsForbiddenFromFormAndSubmission() throws Exception {
        mvc.perform(get("/deliveries/new").with(user(TestData.principal(agent))))
                .andExpect(status().isForbidden());
        mvc.perform(validPost().with(csrf()).with(user(TestData.principal(agent))))
                .andExpect(status().isForbidden());
        assertThat(data.count("deliveries")).isZero();
    }

    @Test
    void validSubmissionPersistsAndRedirectsToDetailWithSuccessMessage() throws Exception {
        mvc.perform(validPost().with(csrf()).with(user(TestData.principal(dispatcher))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/deliveries/*"))
                .andExpect(flash().attribute("successMessage", containsString("created")));

        assertThat(data.count("deliveries")).isEqualTo(1);
        assertThat(data.count("delivery_status_history")).isEqualTo(1);
    }

    @Test
    void invalidSubmissionShowsFieldErrorsPreservesInputAndSavesNothing() throws Exception {
        mvc.perform(post("/deliveries/new").with(csrf()).with(user(TestData.principal(dispatcher)))
                        .param("recipientName", "")
                        .param("recipientPhone", "12ab")
                        .param("deliveryAddress", "Example Street")
                        .param("deliveryCity", ""))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("deliveryForm", "recipientName", "recipientPhone", "deliveryCity"))
                .andExpect(content().string(containsString("data-testid=\"form-error-summary\"")))
                .andExpect(content().string(containsString("value=\"Example Street\"")));

        assertThat(data.count("deliveries")).isZero();
    }

    @Test
    void systemManagedFieldsSuppliedByTheClientAreIgnored() throws Exception {
        UserAccount other = data.user("Other Dispatcher", Role.DISPATCHER);

        mvc.perform(validPost().with(csrf()).with(user(TestData.principal(dispatcher)))
                        .param("deliveryCode", "HACKED-1")
                        .param("status", "DELIVERED")
                        .param("createdBy", String.valueOf(other.getId()))
                        .param("createdAt", "2001-01-01T00:00:00Z")
                        .param("id", "777"))
                .andExpect(status().is3xxRedirection());

        var row = jdbc.queryForMap("SELECT delivery_code, status, created_by_user_id, id FROM deliveries");
        assertThat((String) row.get("delivery_code")).startsWith("LMD-");
        assertThat(row.get("status")).isEqualTo("CREATED");
        assertThat(((Number) row.get("created_by_user_id")).longValue()).isEqualTo(dispatcher.getId());
        assertThat(((Number) row.get("id")).longValue()).isNotEqualTo(777L);
    }

    @Test
    void submissionWithoutCsrfTokenIsRejected() throws Exception {
        mvc.perform(validPost().with(user(TestData.principal(dispatcher))))
                .andExpect(status().isForbidden());
        assertThat(data.count("deliveries")).isZero();
    }

    @Test
    void invalidAgentSelectionIsReportedOnTheField() throws Exception {
        mvc.perform(validPost().with(csrf()).with(user(TestData.principal(dispatcher)))
                        .param("assignedAgentId", String.valueOf(dispatcher.getId())))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("deliveryForm", "assignedAgentId"));
        assertThat(data.count("deliveries")).isZero();
    }
}
