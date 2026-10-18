package com.lastmile.selenium;

import static org.assertj.core.api.Assertions.assertThat;

import com.lastmile.delivery.DeliveryView;
import com.lastmile.user.Role;
import com.lastmile.user.UserAccount;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

/** Journey 4: an agent advances a delivery through allowed statuses; detail and dashboard reflect it. */
class J4AgentProgressSeleniumIT extends BrowserJourney {

    @Test
    void agentDeliversAndTheDetailAndDashboardShowThePersistedResult() {
        UserAccount dispatcher = data.user("Dina Dispatcher", Role.DISPATCHER);
        UserAccount agent = data.user("Ari Agent", Role.AGENT);
        DeliveryView delivery = data.delivery(dispatcher, agent, "Nisha Verma", "ORD-7201");

        login("ari.agent@lastmile.test");
        assertThat(metric("metric-assigned")).isEqualTo("1");
        assertThat(metric("metric-delivered")).isEqualTo("0");

        open("/deliveries/" + delivery.id());
        click("status-out_for_delivery");
        assertThat(text("flash-success")).isEqualTo("Status changed to Out for delivery.");
        assertThat(statusBadge()).isEqualTo("OUT_FOR_DELIVERY");

        click("status-delivered");
        assertThat(text("flash-success")).isEqualTo("Status changed to Delivered.");
        assertThat(statusBadge()).isEqualTo("DELIVERED");
        assertThat(all("history-entry")).hasSize(4);
        assertThat(all("actions-panel")).isEmpty();
        assertThat(data.status(delivery.id())).isEqualTo("DELIVERED");

        click("nav-dashboard");
        assertThat(metric("metric-delivered")).isEqualTo("1");
        assertThat(metric("metric-assigned")).isEqualTo("0");
    }

    private String metric(String cardTestId) {
        return waitFor(cardTestId).findElement(By.cssSelector("[data-testid='metric-value']")).getText().trim();
    }
}
