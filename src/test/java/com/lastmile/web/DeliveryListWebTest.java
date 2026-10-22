package com.lastmile.web;

import static com.lastmile.support.TestData.principal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lastmile.delivery.DeliveryStatus;
import com.lastmile.delivery.DeliveryView;
import com.lastmile.delivery.DeliveryService;
import com.lastmile.support.IntegrationTest;
import com.lastmile.support.TestData;
import com.lastmile.user.Role;
import com.lastmile.user.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * The rendered list must contain exactly the rows that match the submitted filters — asserted on the
 * HTML a browser receives, not only on the query builder (see DeliverySearchServiceTest for that).
 */
@IntegrationTest
@AutoConfigureMockMvc
class DeliveryListWebTest {

    private static final Pattern ROW = Pattern.compile("data-testid=\"delivery-row\" data-code=\"([^\"]+)\"");
    private static final Pattern NEXT = Pattern.compile("href=\"([^\"]*)\"\\s+data-testid=\"page-next\"");

    @Autowired MockMvc mvc;
    @Autowired TestData data;
    @Autowired DeliveryService deliveries;

    UserAccount dispatcher;
    UserAccount arjun;
    UserAccount bela;
    DeliveryView created;
    DeliveryView arjunAssigned;
    DeliveryView arjunOut;
    DeliveryView belaAssigned;

    @BeforeEach
    void setUp() {
        data.reset();
        dispatcher = data.user("Dina Dispatcher", Role.DISPATCHER);
        arjun = data.user("Arjun Agent", Role.AGENT);
        bela = data.user("Bela Agent", Role.AGENT);
        created = data.delivery(dispatcher, null, "Kavya Rao", "ORD-5001");
        arjunAssigned = data.delivery(dispatcher, arjun, "Imran Shaikh", "ORD-5002");
        arjunOut = data.delivery(dispatcher, arjun, "Farhan Ali", "INV-5003");
        deliveries.changeStatus(arjunOut.id(), DeliveryStatus.OUT_FOR_DELIVERY, null, null, principal(arjun));
        belaAssigned = data.delivery(dispatcher, bela, "Leela Nair", "ORD-5004");
    }

    private String html(MockHttpServletRequestBuilder request, UserAccount as) throws Exception {
        return mvc.perform(request.with(user(principal(as))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private static List<String> rows(String html) {
        List<String> codes = new ArrayList<>();
        Matcher m = ROW.matcher(html);
        while (m.find()) {
            codes.add(m.group(1));
        }
        return codes;
    }

    @Test
    void createdStatusPlusAnAgentReturnsNoRowsBecauseAssignedDeliveriesAreNoLongerCreated() throws Exception {
        String page = html(get("/deliveries").param("status", "CREATED").param("agentId", "" + arjun.getId()),
                dispatcher);
        assertThat(rows(page)).isEmpty();
        assertThat(page).contains("data-testid=\"empty-state\"").contains("No deliveries match these filters.")
                .contains("Reset filters");
    }

    @Test
    void statusAndAgentCombineWithAndOnTheRenderedRows() throws Exception {
        assertThat(rows(html(get("/deliveries").param("status", "ASSIGNED").param("agentId", "" + arjun.getId()),
                dispatcher))).containsExactly(arjunAssigned.deliveryCode());
        assertThat(rows(html(get("/deliveries").param("status", "ASSIGNED"), dispatcher)))
                .containsExactlyInAnyOrder(arjunAssigned.deliveryCode(), belaAssigned.deliveryCode());
        assertThat(rows(html(get("/deliveries").param("agentId", "" + arjun.getId()), dispatcher)))
                .containsExactlyInAnyOrder(arjunAssigned.deliveryCode(), arjunOut.deliveryCode());
        assertThat(rows(html(get("/deliveries").param("status", "CREATED"), dispatcher)))
                .containsExactly(created.deliveryCode());
    }

    @Test
    void textMatchesCodeRecipientOrReferenceCaseInsensitivelyAndCombinesWithFilters() throws Exception {
        assertThat(rows(html(get("/deliveries").param("q", "ord-50"), dispatcher)))
                .containsExactlyInAnyOrder(created.deliveryCode(), arjunAssigned.deliveryCode(),
                        belaAssigned.deliveryCode());
        assertThat(rows(html(get("/deliveries").param("q", "FARHAN"), dispatcher)))
                .containsExactly(arjunOut.deliveryCode());
        assertThat(rows(html(get("/deliveries").param("q", arjunOut.deliveryCode().toLowerCase()), dispatcher)))
                .containsExactly(arjunOut.deliveryCode());
        assertThat(rows(html(get("/deliveries").param("q", "ord-50").param("agentId", "" + bela.getId()),
                dispatcher))).containsExactly(belaAssigned.deliveryCode());
    }

    @Test
    void appliedFiltersAreStatedFromTheServerSideCriteria() throws Exception {
        String page = html(get("/deliveries").param("q", " ord ").param("status", "ASSIGNED")
                .param("agentId", "" + arjun.getId()), dispatcher);
        assertThat(page).contains("data-testid=\"active-filters\"").contains("text “ord”")
                .contains("· status Assigned").contains("· agent Arjun Agent")
                .contains("value=\"ASSIGNED\" selected").contains("value=\"" + arjun.getId() + "\" selected");
        assertThat(html(get("/deliveries"), dispatcher)).doesNotContain("data-testid=\"active-filters\"");
    }

    @Test
    void searchFormOptsOutOfBrowserStateRestoration() throws Exception {
        assertThat(html(get("/deliveries"), dispatcher))
                .containsPattern("data-testid=\"search-form\"\\s+autocomplete=\"off\" data-reflects-request");
    }

    @Test
    void paginationKeepsTheFiltersAndEveryPageMatchesThem() throws Exception {
        List<String> expected = new ArrayList<>(List.of(arjunAssigned.deliveryCode()));
        for (int i = 0; i < 11; i++) {
            expected.add(data.delivery(dispatcher, arjun, "Bulk Recipient " + i, "BULK-" + i).deliveryCode());
            data.delivery(dispatcher, bela, "Other Recipient " + i, "BULK-B" + i);
        }

        String first = html(get("/deliveries").param("status", "ASSIGNED").param("agentId", "" + arjun.getId()),
                dispatcher);
        assertThat(first).contains("12 deliveries").contains("Page 1 of 2");
        Matcher next = NEXT.matcher(first);
        assertThat(next.find()).isTrue();
        String nextHref = next.group(1).replace("&amp;", "&");
        assertThat(nextHref).contains("status=ASSIGNED").contains("agentId=" + arjun.getId()).contains("page=1");

        String second = html(get(nextHref), dispatcher);
        assertThat(second).contains("Page 2 of 2").contains("value=\"ASSIGNED\" selected");
        List<String> all = new ArrayList<>(rows(first));
        all.addAll(rows(second));
        assertThat(all).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void agentFilterIsIgnoredForAgentsWhoOnlyEverSeeTheirOwnRows() throws Exception {
        String page = html(get("/deliveries").param("agentId", "" + bela.getId()).param("status", "ASSIGNED"), arjun);
        assertThat(rows(page)).containsExactly(arjunAssigned.deliveryCode());
        assertThat(page).doesNotContain("data-testid=\"filter-agent\"");
    }

    @Test
    void malformedParametersAreBadRequestsNotServerErrors() throws Exception {
        for (String[] p : new String[][]{{"status", "LOST"}, {"status", "created;drop"}, {"agentId", "abc"},
                {"page", "x"}, {"size", "1e9"}, {"q", "x".repeat(101)}}) {
            mvc.perform(get("/deliveries").param(p[0], p[1]).with(user(principal(dispatcher))))
                    .andExpect(status().isBadRequest());
        }
        assertThat(rows(html(get("/deliveries").param("page", "-5").param("size", "999"), dispatcher))).hasSize(4);
        assertThat(rows(html(get("/deliveries").param("status", "").param("agentId", ""), dispatcher))).hasSize(4);
    }
}
