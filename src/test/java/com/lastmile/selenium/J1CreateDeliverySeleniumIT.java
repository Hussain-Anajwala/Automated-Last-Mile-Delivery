package com.lastmile.selenium;

import static org.assertj.core.api.Assertions.assertThat;

import com.lastmile.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Journey 1: a dispatcher creates a delivery and sees the generated code (and invalid input is refused). */
class J1CreateDeliverySeleniumIT extends BrowserJourney {

    @BeforeEach
    void users() {
        data.user("Dina Dispatcher", Role.DISPATCHER);
        data.user("Ari Agent", Role.AGENT);
    }

    @Test
    void dispatcherCreatesADeliveryAndSeesTheGeneratedCode() {
        login("dina.dispatcher@lastmile.test");
        click("nav-create-delivery");
        type("recipientName", "Meera Kapoor");
        type("recipientPhone", "9876543210");
        type("deliveryAddress", "12 Sample Lane");
        type("deliveryCity", "Pune");
        type("referenceNumber", "SEL-J1");
        click("save-delivery");

        assertThat(text("flash-success")).contains("created");
        assertThat(text("delivery-code")).matches("LMD-\\d{8}-\\d{6}");
        assertThat(statusBadge()).isEqualTo("CREATED");
        assertThat(text("detail-recipient")).isEqualTo("Meera Kapoor");
        assertThat(text("detail-phone")).isEqualTo("+91 98765 43210");
        assertThat(all("history-entry")).hasSize(1);
        assertThat(data.count("deliveries")).isEqualTo(1);
    }

    @Test
    void anotherCountryIsChosenSavedInInternationalFormAndRecoveredOnEdit() {
        login("dina.dispatcher@lastmile.test");
        click("nav-create-delivery");
        assertThat(selected("phoneCountry")).isEqualTo("India (+91)");
        assertThat(text("phoneCallingCode")).isEqualTo("+91");

        select("phoneCountry", "United Kingdom (+44)");
        assertThat(text("phoneCallingCode")).isEqualTo("+44");
        type("recipientName", "Oliver Grant");
        type("recipientPhone", "07400123456");
        type("deliveryAddress", "1 Example Road");
        type("deliveryCity", "London");
        click("save-delivery");

        assertThat(text("detail-phone")).isEqualTo("+44 7400 123456");
        click("edit-delivery");
        assertThat(selected("phoneCountry")).isEqualTo("United Kingdom (+44)");
        assertThat(text("phoneCallingCode")).isEqualTo("+44");
        assertThat(waitFor("recipientPhone").getAttribute("value")).isEqualTo("7400123456");
    }

    @Test
    void indianNumberWithTheCountryCodeTypedInIsRefusedWithAClearMessage() {
        login("dina.dispatcher@lastmile.test");
        open("/deliveries/new");
        type("recipientName", "Meera Kapoor");
        type("recipientPhone", "919876543210");
        type("deliveryAddress", "12 Sample Lane");
        type("deliveryCity", "Pune");
        click("save-delivery");

        assertThat(waitFor("recipientPhone").getAttribute("class")).contains("is-invalid");
        assertThat(text("recipientPhone-error")).isEqualTo("Enter the 10-digit number without the country code 91.");
        assertThat(data.count("deliveries")).isZero();
    }

    @Test
    void invalidInputIsRefusedWithFieldMessagesAndNothingIsSaved() {
        login("dina.dispatcher@lastmile.test");
        open("/deliveries/new");
        type("deliveryAddress", "Kept Address");
        type("recipientPhone", "12ab");
        click("save-delivery");

        assertThat(waitFor("form-error-summary").getText()).contains("Please correct the highlighted fields");
        assertThat(currentPath()).isEqualTo("/deliveries/new");
        assertThat(waitFor("deliveryAddress").getAttribute("value")).isEqualTo("Kept Address");
        assertThat(waitFor("recipientPhone").getAttribute("class")).contains("is-invalid");
        assertThat(data.count("deliveries")).isZero();
    }
}
