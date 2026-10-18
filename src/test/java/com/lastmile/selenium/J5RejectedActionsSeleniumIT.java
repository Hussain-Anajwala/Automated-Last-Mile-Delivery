package com.lastmile.selenium;

import static org.assertj.core.api.Assertions.assertThat;

import com.lastmile.delivery.DeliveryService;
import com.lastmile.delivery.DeliveryStatus;
import com.lastmile.delivery.DeliveryView;
import com.lastmile.support.TestData;
import com.lastmile.user.Role;
import com.lastmile.user.UserAccount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Journey 5: an invalid and an unauthorized action are rejected and the record stays unchanged. */
class J5RejectedActionsSeleniumIT extends BrowserJourney {

    @Autowired
    DeliveryService deliveries;

    @Test
    void failureWithoutAReasonIsRejectedAndNothingChanges() {
        UserAccount dispatcher = data.user("Dina Dispatcher", Role.DISPATCHER);
        UserAccount agent = data.user("Ari Agent", Role.AGENT);
        DeliveryView delivery = data.delivery(dispatcher, agent, "Joel D'Souza", "ORD-7301");
        deliveries.changeStatus(delivery.id(), DeliveryStatus.OUT_FOR_DELIVERY, null, null, TestData.principal(agent));
        int historyBefore = data.historyCount(delivery.id());

        login("ari.agent@lastmile.test");
        open("/deliveries/" + delivery.id());
        click("status-failed");

        assertThat(text("flash-error")).isEqualTo("A reason is required when a delivery fails.");
        assertThat(statusBadge()).isEqualTo("OUT_FOR_DELIVERY");
        assertThat(data.status(delivery.id())).isEqualTo("OUT_FOR_DELIVERY");
        assertThat(data.historyCount(delivery.id())).isEqualTo(historyBefore);

        type("failure-reason", "Recipient unavailable");
        click("status-failed");
        assertThat(statusBadge()).isEqualTo("FAILED");
    }

    @Test
    void agentOpeningTheCreatePageDirectlyIsDenied() {
        data.user("Ari Agent", Role.AGENT);

        login("ari.agent@lastmile.test");
        assertThat(all("nav-create-delivery")).isEmpty();
        open("/deliveries/new");

        assertThat(waitFor("error-page").getAttribute("data-error")).isEqualTo("403");
        assertThat(all("delivery-form")).isEmpty();
    }
}
