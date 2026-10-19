package com.lastmile.delivery;

import java.time.Instant;

/** One row of the delivery list. Phone and address are omitted on purpose. */
public record DeliverySummary(
        long id,
        String deliveryCode,
        String recipientName,
        String deliveryCity,
        String referenceNumber,
        String assignedAgentName,
        DeliveryStatus status,
        Instant createdAt) {

    static DeliverySummary from(Delivery d) {
        return new DeliverySummary(d.getId(), d.getDeliveryCode(), d.getRecipientName(), d.getDeliveryCity(),
                d.getReferenceNumber(), d.getAssignedAgent() == null ? null : d.getAssignedAgent().getFullName(),
                d.getStatus(), d.getCreatedAt());
    }
}
