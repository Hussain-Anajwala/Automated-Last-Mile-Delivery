package com.lastmile.delivery;

import java.util.Locale;

import com.lastmile.web.BusinessRuleException;
import org.springframework.data.jpa.domain.Specification;

/**
 * List criteria. {@code agentId} is honoured only for staff; for agents the service replaces it
 * with the agent's own id, whatever the request contained.
 */
public record DeliverySearch(String q, DeliveryStatus status, Long agentId) {

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 50;
    public static final int MAX_QUERY_LENGTH = 100;

    /** The form limits the field, but crafted URLs and API calls must be bounded on the server too. */
    public DeliverySearch {
        if (q != null && q.length() > MAX_QUERY_LENGTH) {
            throw new BusinessRuleException("q", "Search text must be at most " + MAX_QUERY_LENGTH + " characters.");
        }
    }

    public String normalizedQuery() {
        return q == null || q.isBlank() ? null : q.trim();
    }

    public DeliverySearch forAgent(long ownId) {
        return new DeliverySearch(q, status, ownId);
    }

    public boolean hasFilters() {
        return normalizedQuery() != null || status != null || agentId != null;
    }

    Specification<Delivery> toSpecification() {
        Specification<Delivery> spec = (root, query, cb) -> cb.conjunction();
        String text = normalizedQuery();
        if (text != null) {
            String pattern = "%" + escapeLike(text.toLowerCase(Locale.ROOT)) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("deliveryCode")), pattern, '\\'),
                    cb.like(cb.lower(root.get("recipientName")), pattern, '\\'),
                    cb.like(cb.lower(root.get("referenceNumber")), pattern, '\\')));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (agentId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("assignedAgent").get("id"), agentId));
        }
        return spec;
    }

    /** User input is data, never a pattern: escape LIKE wildcards. */
    static String escapeLike(String text) {
        return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
