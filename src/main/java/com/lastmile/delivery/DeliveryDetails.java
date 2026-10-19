package com.lastmile.delivery;

/** Validated, normalized business fields of a delivery (the only fields a client may supply). */
public record DeliveryDetails(
        String recipientName,
        String recipientPhone,
        String deliveryAddress,
        String deliveryCity,
        String referenceNumber) {
}
