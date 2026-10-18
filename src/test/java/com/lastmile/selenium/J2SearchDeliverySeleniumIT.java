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
