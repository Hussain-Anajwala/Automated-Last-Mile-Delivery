package com.lastmile.dashboard;

import java.util.List;
import java.util.Map;

import com.lastmile.delivery.DeliveryStatus;
import com.lastmile.delivery.DeliverySummary;

/** {@code scope} is ALL_DELIVERIES for staff and MY_ASSIGNMENTS for agents. */
public record DashboardSummary(
        String scope,
        long total,
        Map<DeliveryStatus, Long> countsByStatus,
        List<DeliverySummary> recent) {

    public long count(DeliveryStatus status) {
        return countsByStatus.getOrDefault(status, 0L);
    }
}
