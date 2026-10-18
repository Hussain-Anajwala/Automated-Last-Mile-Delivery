package com.lastmile.delivery;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Client-editable fields only. Code, status, creator, timestamps and version are deliberately
 * absent, so request parameters with those names are never bound.
 */
public class DeliveryForm {

    @NotBlank(message = "Recipient name is required.")
    @Size(max = 120, message = "Recipient name must be at most 120 characters.")
    private String recipientName;

    @NotBlank(message = "Phone number is required.")
    @Pattern(regexp = "^\\s*\\+?[0-9]{7,15}\\s*$", message = "Enter 7 to 15 digits, optionally starting with +.")
    private String recipientPhone;

    @NotBlank(message = "Delivery address is required.")
    @Size(max = 500, message = "Address must be at most 500 characters.")
    private String deliveryAddress;

    @NotBlank(message = "City is required.")
    @Size(max = 100, message = "City must be at most 100 characters.")
    private String deliveryCity;

    @Size(max = 100, message = "Reference must be at most 100 characters.")
    private String referenceNumber;

    /** Optional; only honoured on creation. Validated as an active AGENT by the service. */
    private Long assignedAgentId;

    public DeliveryDetails toDetails() {
        return new DeliveryDetails(
                recipientName.trim(),
                recipientPhone.trim(),
                deliveryAddress.trim(),
                deliveryCity.trim(),
                referenceNumber == null || referenceNumber.isBlank() ? null : referenceNumber.trim());
    }

    public static DeliveryForm from(DeliveryView delivery) {
        DeliveryForm form = new DeliveryForm();
        form.recipientName = delivery.recipientName();
        form.recipientPhone = delivery.recipientPhone();
        form.deliveryAddress = delivery.deliveryAddress();
        form.deliveryCity = delivery.deliveryCity();
        form.referenceNumber = delivery.referenceNumber();
        return form;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getDeliveryCity() {
        return deliveryCity;
    }

    public void setDeliveryCity(String deliveryCity) {
        this.deliveryCity = deliveryCity;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public Long getAssignedAgentId() {
        return assignedAgentId;
    }

    public void setAssignedAgentId(Long assignedAgentId) {
        this.assignedAgentId = assignedAgentId;
    }
}
