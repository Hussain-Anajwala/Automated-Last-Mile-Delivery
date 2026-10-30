package com.lastmile.delivery;

import static com.lastmile.support.TestData.principal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lastmile.support.IntegrationTest;
import com.lastmile.support.TestData;
import com.lastmile.user.Role;
import com.lastmile.user.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Country selection, E.164 storage, display and edit recovery through the web form and the API. */
@IntegrationTest
@AutoConfigureMockMvc
class DeliveryPhoneTest {

    @Autowired MockMvc mvc;
    @Autowired TestData data;
    @Autowired JdbcTemplate jdbc;

    UserAccount dispatcher;

    @BeforeEach
    void setUp() {
        data.reset();
        dispatcher = data.user("Dina Dispatcher", Role.DISPATCHER);
    }

    private MockHttpServletRequestBuilder form(MockHttpServletRequestBuilder request, String country, String phone) {
        return request.with(csrf()).with(user(principal(dispatcher)))
                .param("recipientName", "Aarav Sharma")
                .param("phoneCountry", country)
                .param("recipientPhone", phone)
                .param("deliveryAddress", "Example Street 12")
                .param("deliveryCity", "Mumbai");
    }

    private String storedPhone(long id) {
        return jdbc.queryForObject("SELECT recipient_phone FROM deliveries WHERE id = ?", String.class, id);
    }

    private long onlyDeliveryId() {
        return jdbc.queryForObject("SELECT id FROM deliveries", Long.class);
    }

    @Test
    void createFormDefaultsToIndiaAndShowsTheCallingCodeSeparately() throws Exception {
        mvc.perform(get("/deliveries/new").with(user(principal(dispatcher))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-testid=\"phoneCallingCode\" data-phone-calling-code>+91</span>")))
                .andExpect(content().string(containsString("India (+91)")));
    }

    @Test
    void indianNumberIsStoredAsE164AndDisplayedInInternationalFormat() throws Exception {
        mvc.perform(form(post("/deliveries/new"), "IN", "9876543210")).andExpect(status().is3xxRedirection());
        long id = onlyDeliveryId();
        assertThat(storedPhone(id)).isEqualTo("+919876543210");
        mvc.perform(get("/deliveries/{id}", id).with(user(principal(dispatcher))))
                .andExpect(content().string(containsString("data-testid=\"detail-phone\">+91 98765 43210</dd>")));
    }

    @Test
    void anotherCountryIsValidatedByItsOwnRulesAndRecoveredOnEdit() throws Exception {
        mvc.perform(form(post("/deliveries/new"), "GB", "9876543210"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("deliveryForm", "recipientPhone"))
                .andExpect(content().string(containsString("This is not a valid phone number for United Kingdom.")))
                .andExpect(content().string(containsString(">+44</span>")));
        assertThat(data.count("deliveries")).isZero();

        mvc.perform(form(post("/deliveries/new"), "GB", "07400123456")).andExpect(status().is3xxRedirection());
        long id = onlyDeliveryId();
        assertThat(storedPhone(id)).isEqualTo("+447400123456");

        mvc.perform(get("/deliveries/{id}/edit", id).with(user(principal(dispatcher))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("value=\"GB\" data-calling-code=\"44\" selected=\"selected\"")))
                .andExpect(content().string(containsString("value=\"7400123456\"")))
                .andExpect(content().string(containsString(">+44</span>")));
    }

    @Test
    void editingChangesCountryAndNumberWithTheSameRules() throws Exception {
        DeliveryView d = data.delivery(dispatcher, null);
        assertThat(storedPhone(d.id())).isEqualTo("+919876543210");

        mvc.perform(form(post("/deliveries/{id}/edit", d.id()), "IN", "12345").param("version", "" + d.version()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Enter exactly 10 digits for India (you entered 5).")));
        assertThat(storedPhone(d.id())).isEqualTo("+919876543210");

        mvc.perform(form(post("/deliveries/{id}/edit", d.id()), "CA", "4165550123").param("version", "" + d.version()))
                .andExpect(status().is3xxRedirection());
        assertThat(storedPhone(d.id())).isEqualTo("+14165550123");
        mvc.perform(get("/deliveries/{id}/edit", d.id()).with(user(principal(dispatcher))))
                .andExpect(content().string(containsString("value=\"CA\" data-calling-code=\"1\" selected=\"selected\"")))
                .andExpect(content().string(containsString("value=\"4165550123\"")));
    }

    @Test
    void anUnknownCountryIsAFieldErrorOnTheCountry() throws Exception {
        mvc.perform(form(post("/deliveries/new"), "XX", "9876543210"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("deliveryForm", "phoneCountry"))
                .andExpect(content().string(containsString("Choose a country from the list.")));
        assertThat(data.count("deliveries")).isZero();
    }

    @Test
    void legacyStoredValuesAreShownUnchangedAndOnlyNormalisedWhenSaved() throws Exception {
        DeliveryView d = data.delivery(dispatcher, null);
        jdbc.update("UPDATE deliveries SET recipient_phone = '9876543210' WHERE id = ?", d.id());
        long version = jdbc.queryForObject("SELECT version FROM deliveries WHERE id = ?", Long.class, d.id());

        mvc.perform(get("/deliveries/{id}", d.id()).with(user(principal(dispatcher))))
                .andExpect(content().string(containsString("data-testid=\"detail-phone\">9876543210</dd>")));
        mvc.perform(get("/deliveries/{id}/edit", d.id()).with(user(principal(dispatcher))))
                .andExpect(content().string(containsString("value=\"IN\" data-calling-code=\"91\" selected=\"selected\"")))
                .andExpect(content().string(containsString("value=\"9876543210\"")));
        assertThat(storedPhone(d.id())).isEqualTo("9876543210");

        mvc.perform(form(post("/deliveries/{id}/edit", d.id()), "IN", "9876543210").param("version", "" + version))
                .andExpect(status().is3xxRedirection());
        assertThat(storedPhone(d.id())).isEqualTo("+919876543210");
    }

    @Test
    void apiUsesTheSameRulesAndStaysCompatibleWithClientsThatSendNoCountry() throws Exception {
        var basic = httpBasic(dispatcher.getEmail(), TestData.PASSWORD);
        String body = """
                {"recipientName":"Meera","recipientPhone":"%s",%s"deliveryAddress":"12 Lane","deliveryCity":"Pune"}""";

        mvc.perform(post("/api/v1/deliveries").with(basic).contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted("9876543210", "")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recipientPhone").value("+919876543210"))
                .andExpect(jsonPath("$.recipientPhoneDisplay").doesNotExist());
        mvc.perform(post("/api/v1/deliveries").with(basic).contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted("+447400123456", "")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recipientPhone").value("+447400123456"));
        String created = mvc.perform(post("/api/v1/deliveries").with(basic).contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted("81234567", "\"phoneCountry\":\"SG\",")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recipientPhone").value("+6581234567"))
                .andReturn().getResponse().getContentAsString();

        for (String[] bad : new String[][]{{"12345", ""}, {"+919876543210", "\"phoneCountry\":\"IN\","},
                {"98765 43210", ""}, {"81234567", "\"phoneCountry\":\"GB\","}}) {
            mvc.perform(post("/api/v1/deliveries").with(basic).contentType(MediaType.APPLICATION_JSON)
                            .content(body.formatted(bad[0], bad[1])))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors[*].field").value(hasItem("recipientPhone")));
        }
        assertThat(data.count("deliveries")).isEqualTo(3);

        long id = Long.parseLong(created.replaceAll("(?s).*?\"id\":(\\d+).*", "$1"));
        mvc.perform(put("/api/v1/deliveries/{id}", id).with(basic).contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted("9123456780", "\"phoneCountry\":\"IN\",")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipientPhone").value("+919123456780"));
        mvc.perform(put("/api/v1/deliveries/{id}", id).with(basic).contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted("91234", "\"phoneCountry\":\"IN\",")))
                .andExpect(status().isBadRequest());
        assertThat(storedPhone(id)).isEqualTo("+919123456780");
    }
}
