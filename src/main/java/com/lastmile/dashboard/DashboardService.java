package com.lastmile.dashboard;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.lastmile.delivery.DeliveryRepository;
import com.lastmile.delivery.DeliverySearch;
import com.lastmile.delivery.DeliveryService;
import com.lastmile.delivery.DeliveryStatus;
import com.lastmile.delivery.DeliverySummary;
import com.lastmile.security.AppUserDetails;
import com.lastmile.user.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Counts come from GROUP BY queries on persisted deliveries; nothing is hard-coded or cached. */
@Service
public class DashboardService {

    private static final int RECENT_LIMIT = 5;

    private final DeliveryRepository deliveries;
    private final DeliveryService deliveryService;

    public DashboardService(DeliveryRepository deliveries, DeliveryService deliveryService) {
        this.deliveries = deliveries;
        this.deliveryService = deliveryService;
    }

    @Transactional(readOnly = true)
    public DashboardSummary summary(AppUserDetails principal) {
        // search() re-validates the account and applies agent scoping for the recent list.
        List<DeliverySummary> recent = deliveryService
                .search(new DeliverySearch(null, null, null), 0, RECENT_LIMIT, principal).getContent();
        boolean agentScope = principal.getRole() == Role.AGENT;
        List<Object[]> rows = agentScope
                ? deliveries.countByStatusForAgent(principal.getId())
                : deliveries.countByStatus();

        Map<DeliveryStatus, Long> counts = new EnumMap<>(DeliveryStatus.class);
        for (DeliveryStatus status : DeliveryStatus.values()) {
            counts.put(status, 0L);
        }
        for (Object[] row : rows) {
            counts.put((DeliveryStatus) row[0], (Long) row[1]);
        }
        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        return new DashboardSummary(agentScope ? "MY_ASSIGNMENTS" : "ALL_DELIVERIES", total, counts, recent);
    }
}
