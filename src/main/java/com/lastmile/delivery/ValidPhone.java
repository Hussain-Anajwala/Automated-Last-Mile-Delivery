package com.lastmile.delivery;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

/**
 * Checks {@code recipientPhone} against the selected {@code phoneCountry} using {@link PhoneNumbers}; the
 * message is attached to the field that is wrong (web form and API {@code fieldErrors} alike).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidPhone.Validator.class)
public @interface ValidPhone {

    String message() default "Enter a valid phone number.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<ValidPhone, DeliveryForm> {

        @Override
        public boolean isValid(DeliveryForm form, ConstraintValidatorContext context) {
            String problem = PhoneNumbers.problem(form.getPhoneCountry(), form.getRecipientPhone());
            if (problem == null) {
                return true;
            }
            boolean countryIsWrong = form.getPhoneCountry() != null && !form.getPhoneCountry().isBlank()
                    && !PhoneNumbers.isSupportedRegion(form.getPhoneCountry());
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(problem.replace("{", "\\{").replace("$", "\\$"))
                    .addPropertyNode(countryIsWrong ? "phoneCountry" : "recipientPhone")
                    .addConstraintViolation();
            return false;
        }
    }
}
