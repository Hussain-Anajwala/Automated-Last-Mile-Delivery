package com.lastmile.selenium;

import static org.assertj.core.api.Assertions.assertThat;

import com.lastmile.delivery.DeliveryView;
import com.lastmile.user.Role;
import com.lastmile.user.UserAccount;
import org.junit.jupiter.api.Test;

/** Journey 3: a dispatcher assigns an agent; that agent sees the delivery and another agent cannot. */
class J3AssignmentVisibilitySeleniumIT extends BrowserJourney {

    @Test
    void assignedAgentSeesTheDeliveryAndAnotherAgentCannot() {
        UserAccount dispatcher = data.user("Dina Dispatcher", Role.DISPATCHER);
        data.user("Ari Agent", Role.AGENT);
        data.user("Oli Agent", Role.AGENT);
        DeliveryView delivery = data.delivery(dispatcher, null, "Farhan Ali", "ORD-7101");

        login("dina.dispatcher@lastmile.test");
        open("/deliveries/" + delivery.id());
        select("assign-agent", "Ari Agent");
        click("assign-submit");
        assertThat(text("flash-success")).isEqualTo("Assigned to Ari Agent.");
        assertThat(statusBadge()).isEqualTo("ASSIGNED");
        assertThat(text("detail-agent")).isEqualTo("Ari Agent");
        logout();

        login("ari.agent@lastmile.test");
        click("nav-deliveries");
        assertThat(all("delivery-row")).singleElement()
                .satisfies(row -> assertThat(row.getAttribute("data-code")).isEqualTo(delivery.deliveryCode()));
        logout();

        login("oli.agent@lastmile.test");
        click("nav-deliveries");
        assertThat(text("empty-state")).contains("There are no deliveries yet.");
        open("/deliveries/" + delivery.id());
        assertThat(waitFor("error-page").getAttribute("data-error")).isEqualTo("404");
        assertThat(driverPageContains("Farhan Ali")).isFalse();
    }

    private boolean driverPageContains(String text) {
        return driver().getPageSource().contains(text);
    }
}
