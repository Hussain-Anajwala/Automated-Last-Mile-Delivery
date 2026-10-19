package com.lastmile.delivery;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeliveryRepository extends JpaRepository<Delivery, Long>, JpaSpecificationExecutor<Delivery> {

    /** Loads the assigned agent with each row to avoid one query per row in the list. */
    @Override
    @EntityGraph(attributePaths = "assignedAgent")
    Page<Delivery> findAll(Specification<Delivery> spec, Pageable pageable);

    /** Rows of [status, count] for all deliveries. */
    @Query("SELECT d.status, COUNT(d) FROM Delivery d GROUP BY d.status")
    List<Object[]> countByStatus();

    /** Rows of [status, count] for one agent's assigned deliveries. */
    @Query("SELECT d.status, COUNT(d) FROM Delivery d WHERE d.assignedAgent.id = :agentId GROUP BY d.status")
    List<Object[]> countByStatusForAgent(@Param("agentId") long agentId);
}
