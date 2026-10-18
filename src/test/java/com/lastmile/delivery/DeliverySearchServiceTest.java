package com.lastmile.delivery;

import static com.lastmile.support.TestData.principal;
import static org.assertj.core.api.Assertions.assertThat;

import com.lastmile.support.IntegrationTest;
import com.lastmile.support.TestData;
import com.lastmile.user.Role;
import com.lastmile.user.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class DeliverySearchServiceTest {

    @Autowired DeliveryService service;
    @Autowired TestData data;

    UserAccount dispatcher;
    UserAccount agent;
    UserAccount otherAgent;
    DeliveryView meera;
    DeliveryView rohan;
    DeliveryView unassigned;

    @BeforeEach
    void setUp() {
        data.reset();
        dispatcher = data.user("Dina Dispatcher", Role.DISPATCHER);
        agent = data.user("Ari Agent", Role.AGENT);
        otherAgent = data.user("Oli Agent", Role.AGENT);
        meera = data.delivery(dispatcher, agent, "Meera Kapoor", "ORD-500");
        rohan = data.delivery(dispatcher, otherAgent, "Rohan Iyer", "INV_77");
        unassigned = data.delivery(dispatcher, null, "Sana Mehta", "ORD-501");
    }

    private java.util.List<String> codes(DeliverySearch search, UserAccount who) {
        return service.search(search, 0, 50, principal(who)).map(DeliverySummary::deliveryCode).getContent();
    }

    @Test
    void searchMatchesCodeRecipientOrReferenceCaseInsensitively() {
        assertThat(codes(new DeliverySearch("meera", null, null), dispatcher)).containsExactly(meera.deliveryCode());
        assertThat(codes(new DeliverySearch("ord-50", null, null), dispatcher))
                .containsExactlyInAnyOrder(meera.deliveryCode(), unassigned.deliveryCode());
        assertThat(codes(new DeliverySearch(rohan.deliveryCode().toLowerCase(), null, null), dispatcher))
                .containsExactly(rohan.deliveryCode());
        assertThat(codes(new DeliverySearch("nobody", null, null), dispatcher)).isEmpty();
    }

    @Test
    void likeWildcardsInTheQueryAreTreatedLiterally() {
        assertThat(codes(new DeliverySearch("%", null, null), dispatcher)).isEmpty();
        // Unescaped, "_" would match the "-" in ORD-500 / ORD-501.
        assertThat(codes(new DeliverySearch("ORD_50", null, null), dispatcher)).isEmpty();
        assertThat(codes(new DeliverySearch("INV_77", null, null), dispatcher)).containsExactly(rohan.deliveryCode());
    }

    @Test
    void statusAndAgentFiltersCombine() {
        assertThat(codes(new DeliverySearch(null, DeliveryStatus.CREATED, null), dispatcher))
                .containsExactly(unassigned.deliveryCode());
        assertThat(codes(new DeliverySearch(null, DeliveryStatus.ASSIGNED, otherAgent.getId()), dispatcher))
                .containsExactly(rohan.deliveryCode());
        assertThat(codes(new DeliverySearch("meera", null, otherAgent.getId()), dispatcher)).isEmpty();
    }

    @Test
    void agentsOnlyEverSeeTheirOwnDeliveriesEvenWhenAskingForAnotherAgent() {
        assertThat(codes(new DeliverySearch(null, null, null), agent)).containsExactly(meera.deliveryCode());
        assertThat(codes(new DeliverySearch(null, null, otherAgent.getId()), agent)).containsExactly(meera.deliveryCode());
        assertThat(codes(new DeliverySearch("rohan", null, null), agent)).isEmpty();
    }

    @Test
    void resultsArePaginatedNewestFirstAndPageSizeIsBounded() {
        var firstPage = service.search(new DeliverySearch(null, null, null), 0, 2, principal(dispatcher));
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(firstPage.getContent()).extracting(DeliverySummary::deliveryCode)
                .containsExactly(unassigned.deliveryCode(), rohan.deliveryCode());

        assertThat(service.search(new DeliverySearch(null, null, null), 0, 10_000, principal(dispatcher)).getSize())
                .isEqualTo(DeliverySearch.MAX_PAGE_SIZE);
        assertThat(service.search(new DeliverySearch(null, null, null), -5, 0, principal(dispatcher)).getNumber())
                .isZero();
    }
}
