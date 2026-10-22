package com.lastmile.selenium;

import static org.assertj.core.api.Assertions.assertThat;

import com.lastmile.delivery.DeliveryView;
import com.lastmile.user.Role;
import com.lastmile.user.UserAccount;
import org.junit.jupiter.api.Test;

/** Journey 2: a dispatcher searches for a delivery and opens its detail page. */
class J2SearchDeliverySeleniumIT extends BrowserJourney {

    @Test
    void dispatcherFindsADeliveryByReferenceAndOpensIt() {
        UserAccount dispatcher = data.user("Dina Dispatcher", Role.DISPATCHER);
        data.delivery(dispatcher, null, "Kavya Rao", "ORD-7001");
        DeliveryView target = data.delivery(dispatcher, null, "Imran Shaikh", "ORD-7002");
        data.delivery(dispatcher, null, "Leela Nair", "INV-9000");

        login("dina.dispatcher@lastmile.test");
        click("nav-deliveries");
        assertThat(text("result-count")).isEqualTo("3 deliveries");

        type("search-q", "ord-7002");
        click("search-submit");
        waitForUrl("q=ord-7002");
        assertThat(text("result-count")).isEqualTo("1 delivery");
        assertThat(all("delivery-row")).singleElement()
                .satisfies(row -> assertThat(row.getAttribute("data-code")).isEqualTo(target.deliveryCode()));

        click("delivery-link");
        assertThat(text("delivery-code")).isEqualTo(target.deliveryCode());
        assertThat(text("detail-reference")).isEqualTo("ORD-7002");
        assertThat(text("detail-recipient")).isEqualTo("Imran Shaikh");
    }

    /**
     * Regression for the "filters say Created + Arjun but the table shows everything" report: the
     * server filtered correctly, but after Search and the browser's Back button the browser restored
     * the dropdown choices onto the unfiltered page. The controls must always describe the rows shown.
     */
    @Test
    void statusAndAgentFiltersNarrowTheRowsAndControlsNeverDisagreeWithThem() {
        UserAccount dispatcher = data.user("Dina Dispatcher", Role.DISPATCHER);
        UserAccount arjun = data.user("Arjun Agent", Role.AGENT);
        UserAccount bela = data.user("Bela Agent", Role.AGENT);
        data.delivery(dispatcher, null, "Kavya Rao", "ORD-7001");
        DeliveryView arjunsAssigned = data.delivery(dispatcher, arjun, "Imran Shaikh", "ORD-7002");
        data.delivery(dispatcher, bela, "Leela Nair", "ORD-7003");

        login("dina.dispatcher@lastmile.test");
        click("nav-deliveries");
        assertThat(text("result-count")).isEqualTo("3 deliveries");

        select("filter-status", "Created");
        select("filter-agent", "Arjun Agent");
        click("search-submit");
        assertThat(currentPath()).contains("status=CREATED").contains("agentId=" + arjun.getId());
        assertThat(text("empty-state")).contains("No deliveries match these filters.");
        assertThat(selected("filter-status")).isEqualTo("Created");
        assertThat(selected("filter-agent")).isEqualTo("Arjun Agent");

        select("filter-status", "Assigned");
        click("search-submit");
        assertThat(all("delivery-row")).singleElement()
                .satisfies(row -> assertThat(row.getAttribute("data-code")).isEqualTo(arjunsAssigned.deliveryCode()));
        assertThat(text("active-filters")).contains("Assigned").contains("Arjun Agent");

        back();
        back();
        assertThat(currentPath()).doesNotContain("status=");
        assertThat(text("result-count")).isEqualTo("3 deliveries");
        assertThat(selected("filter-status")).as("status control on the unfiltered page").isEqualTo("All");
        assertThat(selected("filter-agent")).as("agent control on the unfiltered page").isEqualTo("All");
    }

    @Test
    void noMatchesShowsTheEmptyStateAndResetRestoresTheList() {
        UserAccount dispatcher = data.user("Dina Dispatcher", Role.DISPATCHER);
        data.delivery(dispatcher, null, "Kavya Rao", "ORD-7001");

        login("dina.dispatcher@lastmile.test");
        open("/deliveries?q=no-such-delivery");
        assertThat(text("empty-state")).contains("No deliveries match these filters.");

        click("search-reset");
        assertThat(text("result-count")).isEqualTo("1 delivery");
    }
}
