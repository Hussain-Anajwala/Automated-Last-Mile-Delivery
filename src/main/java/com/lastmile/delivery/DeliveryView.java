package com.lastmile.delivery;

import java.time.Instant;
import java.util.List;

import com.lastmile.workflow.DeliveryStatusHistory;

/** Read model for views and API responses; entities never leave the service layer. */
public record DeliveryView(
        long id,
        String deliveryCode,
        String recipientName,
        String recipientPhone,
        String deliveryAddress,
        String deliveryCity,
        String referenceNumber,
        Long assignedAgentId,
        String assignedAgentName,
        String createdByName,
        DeliveryStatus status,
        String failureReason,
        Instant createdAt,
        Instant updatedAt,
        Instant deliveredAt,
        List<HistoryEntry> history) {

    public record HistoryEntry(
            DeliveryStatus previousStatus,
            DeliveryStatus newStatus,
            String reason,
            String changedByName,
            Instant changedAt) {

        static HistoryEntry from(DeliveryStatusHistory h) {
            return new HistoryEntry(h.getPreviousStatus(), h.getNewStatus(), h.getReason(),
                    h.getChangedBy().getFullName(), h.getChangedAt());
        }
    }

    static DeliveryView from(Delivery d, List<DeliveryStatusHistory> history) {
        var agent = d.getAssignedAgent();
        return new DeliveryView(
                d.getId(), d.getDeliveryCode(), d.getRecipientName(), d.getRecipientPhone(),
                d.getDeliveryAddress(), d.getDeliveryCity(), d.getReferenceNumber(),
                agent == null ? null : agent.getId(),
                agent == null ? null : agent.getFullName(),
                d.getCreatedBy().getFullName(),
                d.getStatus(), d.getFailureReason(),
                d.getCreatedAt(), d.getUpdatedAt(), d.getDeliveredAt(),
                history.stream().map(HistoryEntry::from).toList());
    }
}
