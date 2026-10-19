package com.lastmile.delivery;

import java.time.Instant;

import com.lastmile.user.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Delivery aggregate. System-managed fields (code, status, creator, timestamps, version) have no
 * public setters; they change only through the methods below, which services call after
 * authorization and transition checks.
 */
@Entity
@Table(name = "deliveries")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "delivery_code", nullable = false, length = 40, updatable = false)
    private String deliveryCode;

    @Column(name = "recipient_name", nullable = false, length = 120)
    private String recipientName;

    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;

    @Column(name = "delivery_address", nullable = false, length = 500)
    private String deliveryAddress;

    @Column(name = "delivery_city", nullable = false, length = 100)
    private String deliveryCity;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_agent_id")
    private UserAccount assignedAgent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false, updatable = false)
    private UserAccount createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DeliveryStatus status;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected Delivery() {
        // JPA
    }

    Delivery(String deliveryCode, DeliveryDetails details, UserAccount createdBy, Instant now) {
        this.deliveryCode = deliveryCode;
        applyDetails(details);
        this.createdBy = createdBy;
        this.status = DeliveryStatus.CREATED;
        this.createdAt = now;
        this.updatedAt = now;
    }

    void updateDetails(DeliveryDetails details, Instant now) {
        applyDetails(details);
        this.updatedAt = now;
    }

    private void applyDetails(DeliveryDetails details) {
        this.recipientName = details.recipientName();
        this.recipientPhone = details.recipientPhone();
        this.deliveryAddress = details.deliveryAddress();
        this.deliveryCity = details.deliveryCity();
        this.referenceNumber = details.referenceNumber();
    }

    /** Applies an already-authorized transition. Callers must have validated it. */
    void applyTransition(DeliveryStatus next, String reason, Instant now) {
        if (!status.canTransitionTo(next)) {
            throw new IllegalStateException("Transition " + status + " -> " + next + " not allowed");
        }
        if (next == DeliveryStatus.FAILED) {
            this.failureReason = reason;
        } else if (status == DeliveryStatus.FAILED) {
            // Retry policy (decision D8): reason stays in history, cleared on the delivery row.
            this.failureReason = null;
        }
        if (next == DeliveryStatus.DELIVERED) {
            this.deliveredAt = now;
        }
        this.status = next;
        this.updatedAt = now;
    }

    void assignAgent(UserAccount agent, Instant now) {
        this.assignedAgent = agent;
        this.updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public String getDeliveryCode() {
        return deliveryCode;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public String getDeliveryCity() {
        return deliveryCity;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public UserAccount getAssignedAgent() {
        return assignedAgent;
    }

    public UserAccount getCreatedBy() {
        return createdBy;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public long getVersion() {
        return version;
    }
}
