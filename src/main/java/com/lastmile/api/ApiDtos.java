package com.lastmile.api;

import java.util.List;

import com.lastmile.delivery.DeliveryStatus;
import org.springframework.data.domain.Page;

/** Request/response shapes of the JSON API (docs/Backend_Schema.md section 8). */
public final class ApiDtos {

    private ApiDtos() {
    }

    public record AssignmentRequest(Long agentId, Long expectedVersion) {
    }

    public record StatusRequest(DeliveryStatus newStatus, String reason, Long expectedVersion) {
    }

    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
        static <T> PageResponse<T> of(Page<T> page) {
            return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                    page.getTotalElements(), page.getTotalPages());
        }
    }

    public record FieldError(String field, String message) {
    }

    /** Safe error body: no stack traces, SQL or internal configuration. */
    public record ErrorResponse(int status, String error, String message, List<FieldError> fieldErrors) {
    }
}
