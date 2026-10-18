package com.lastmile.api;

import java.net.URI;

import com.lastmile.api.ApiDtos.AssignmentRequest;
import com.lastmile.api.ApiDtos.PageResponse;
import com.lastmile.api.ApiDtos.StatusRequest;
import com.lastmile.dashboard.DashboardService;
import com.lastmile.dashboard.DashboardSummary;
import com.lastmile.delivery.DeliveryForm;
import com.lastmile.delivery.DeliverySearch;
import com.lastmile.delivery.DeliveryService;
import com.lastmile.delivery.DeliveryStatus;
import com.lastmile.delivery.DeliverySummary;
import com.lastmile.delivery.DeliveryView;
import com.lastmile.security.AppUserDetails;
import com.lastmile.user.Role;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** JSON API sharing the same services, validation and authorization as the web UI. */
@RestController
@RequestMapping("/api/v1")
public class DeliveryApiController {

    private final DeliveryService deliveryService;
    private final DashboardService dashboardService;

    public DeliveryApiController(DeliveryService deliveryService, DashboardService dashboardService) {
        this.deliveryService = deliveryService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/deliveries")
    public PageResponse<DeliverySummary> list(@RequestParam(required = false) String q,
                                              @RequestParam(required = false) DeliveryStatus status,
                                              @RequestParam(required = false) Long agentId,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "" + DeliverySearch.DEFAULT_PAGE_SIZE) int size,
                                              @AuthenticationPrincipal AppUserDetails principal) {
        Long scopedAgent = principal.getRole() == Role.AGENT ? null : agentId;
        return PageResponse.of(deliveryService.search(new DeliverySearch(q, status, scopedAgent), page, size, principal));
    }

    @PostMapping("/deliveries")
    public ResponseEntity<DeliveryView> create(@Valid @RequestBody DeliveryForm form,
                                               @AuthenticationPrincipal AppUserDetails principal) {
        DeliveryView created = deliveryService.create(form, principal);
        return ResponseEntity.created(URI.create("/api/v1/deliveries/" + created.id())).body(created);
    }

    @GetMapping("/deliveries/{id}")
    public DeliveryView get(@PathVariable long id, @AuthenticationPrincipal AppUserDetails principal) {
        return deliveryService.get(id, principal);
    }

    @PutMapping("/deliveries/{id}")
    public DeliveryView update(@PathVariable long id, @Valid @RequestBody DeliveryForm form,
                               @RequestParam(required = false) Long expectedVersion,
                               @AuthenticationPrincipal AppUserDetails principal) {
        return deliveryService.update(id, form, expectedVersion, principal);
    }

    @PatchMapping("/deliveries/{id}/assignment")
    public DeliveryView assign(@PathVariable long id, @RequestBody AssignmentRequest request,
                               @AuthenticationPrincipal AppUserDetails principal) {
        return deliveryService.assign(id, request.agentId(), request.expectedVersion(), principal);
    }

    @PatchMapping("/deliveries/{id}/status")
    public DeliveryView changeStatus(@PathVariable long id, @RequestBody StatusRequest request,
                                     @AuthenticationPrincipal AppUserDetails principal) {
        return deliveryService.changeStatus(id, request.newStatus(), request.reason(), request.expectedVersion(), principal);
    }

    @GetMapping("/dashboard/summary")
    public DashboardSummary dashboard(@AuthenticationPrincipal AppUserDetails principal) {
        return dashboardService.summary(principal);
    }
}
