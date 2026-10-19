package com.lastmile.workflow;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryStatusHistoryRepository extends JpaRepository<DeliveryStatusHistory, Long> {

    @EntityGraph(attributePaths = "changedBy")
    List<DeliveryStatusHistory> findByDeliveryIdOrderByChangedAtAscIdAsc(Long deliveryId);

    long countByDeliveryId(Long deliveryId);
}
