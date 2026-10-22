package com.lastmile.delivery;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber.CountryCodeSource;
import com.lastmile.web.BusinessRuleException;

/**
 * The one phone rule for web forms, the JSON API and seed data (docs/Backend_Schema.md section 4.1).
 * <ul>
 *   <li>The country is an ISO 3166 region code ({@code IN}, {@code GB}, ...) because one calling code can
 *       serve several countries with different numbering plans (+1 is the US, Canada and others).</li>
 *   <li>The national number is digits only. India requires exactly ten digits; every country's number must
 *       also be valid under libphonenumber's maintained metadata for that country.</li>
 *   <li>Stored values are E.164 ({@code +919876543210}). Values stored before this rule (any 7-15 digits)
 *       are left untouched and are only re-normalised when someone saves the delivery.</li>
 * </ul>
 */
public final class PhoneNumbers {

    public static final String DEFAULT_REGION = "IN";
    private static final int MAX_DIGITS = 17;
    private static final PhoneNumberUtil UTIL = PhoneNumberUtil.getInstance();

    /** One selector entry: region code, English country name and calling code. */
    public record Country(String region, String name, int callingCode) {
        public String label() {
            return name + " (+" + callingCode + ")";
        }
    }

    /** National parts of a stored value, used to pre-fill the edit form. */
    public record Parts(String region, String nationalNumber) {
    }

    private static final List<Country> COUNTRIES = UTIL.getSupportedRegions().stream()
            .map(r -> new Country(r, Locale.of("", r).getDisplayCountry(Locale.ENGLISH), UTIL.getCountryCodeForRegion(r)))
            .sorted(Comparator.comparing(Country::name).thenComparing(Country::region))
            .toList();

    private PhoneNumbers() {
    }

    /** Every region the metadata supports, sorted by English name. */
    public static List<Country> countries() {
        return COUNTRIES;
    }

    /** "+91" for {@code IN}; the default country's code for anything unsupported. */
    public static String callingCodeLabel(String region) {
        return "+" + UTIL.getCountryCodeForRegion(isSupportedRegion(region) ? region : DEFAULT_REGION);
    }

    public static boolean isSupportedRegion(String region) {
        return region != null && UTIL.getSupportedRegions().contains(region);
    }

    /**
     * The problem with the input, or null when it is acceptable. Blank input is not reported here:
     * {@code @NotBlank} owns that message, so the user sees one error, not two.
     *
     * @param region the selected country; null only for API clients that predate the selector, in which case
     *               the number is either full international ({@code +44...}) or an Indian national number
     */
    public static String problem(String region, String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        String value = input.strip();
        if (region == null || region.isBlank()) {
            return value.startsWith("+") ? internationalProblem(value) : nationalProblem(DEFAULT_REGION, value);
        }
        if (!isSupportedRegion(region)) {
            return "Choose a country from the list.";
        }
        return nationalProblem(region, value);
    }

    /** The E.164 form of a valid input; throws (as a field error on {@code recipientPhone}) otherwise. */
    public static String toE164(String region, String input) {
        String problem = input == null || input.isBlank() ? "Phone number is required." : problem(region, input);
        if (problem != null) {
            throw new BusinessRuleException("recipientPhone", problem);
        }
        String value = input.strip();
        String effectiveRegion = region == null || region.isBlank()
                ? (value.startsWith("+") ? null : DEFAULT_REGION) : region;
        return UTIL.format(parse(value, effectiveRegion), PhoneNumberFormat.E164);
    }

    /**
     * Splits a stored value back into country and national number for the edit form. E.164 values recover
     * their country; legacy values without {@code +} are shown as Indian national numbers, unchanged, so a
     * number that does not meet the current rule is reported when saved instead of being silently altered.
     */
    public static Parts split(String stored) {
        if (stored == null || !stored.startsWith("+")) {
            return new Parts(DEFAULT_REGION, stored);
        }
        try {
            PhoneNumber number = UTIL.parse(stored, null);
            String region = UTIL.getRegionCodeForNumber(number);
            if (region == null) {
                region = UTIL.getRegionCodeForCountryCode(number.getCountryCode());
            }
            if (!isSupportedRegion(region)) {
                return new Parts(DEFAULT_REGION, stored);
            }
            return new Parts(region, UTIL.getNationalSignificantNumber(number));
        } catch (NumberParseException ex) {
            return new Parts(DEFAULT_REGION, stored);
        }
    }

    /** Human-readable international form ({@code +91 98765 43210}); legacy or unparsable values as stored. */
    public static String display(String stored) {
        if (stored == null || !stored.startsWith("+")) {
            return stored;
        }
        try {
            PhoneNumber number = UTIL.parse(stored, null);
            return UTIL.isValidNumber(number) ? UTIL.format(number, PhoneNumberFormat.INTERNATIONAL) : stored;
        } catch (NumberParseException ex) {
            return stored;
        }
    }

    private static String nationalProblem(String region, String value) {
        int callingCode = UTIL.getCountryCodeForRegion(region);
        String country = countryName(region);
        if (value.startsWith("+")) {
            return "Enter the number without the country code; +" + callingCode + " is added from the country you chose.";
        }
        if (!value.chars().allMatch(c -> c >= '0' && c <= '9')) {
            return "Use digits only, with no spaces, letters or symbols.";
        }
        if (DEFAULT_REGION.equals(region) && value.length() != 10) {
            if (value.length() == 12 && value.startsWith("91")) {
                return "Enter the 10-digit number without the country code 91.";
            }
            return "Enter exactly 10 digits for India (you entered " + value.length() + ").";
        }
        if (value.length() > MAX_DIGITS) {
            return "This is not a valid phone number for " + country + ".";
        }
        PhoneNumber number;
        try {
            number = UTIL.parseAndKeepRawInput(value, region);
        } catch (NumberParseException ex) {
            return "This is not a valid phone number for " + country + ".";
        }
        // libphonenumber quietly accepts a leading country code (or international prefix); the form shows it
        // separately, so a number typed with it is ambiguous and is rejected with an explicit message.
        if (number.getCountryCodeSource() != CountryCodeSource.FROM_DEFAULT_COUNTRY) {
            return "Enter the number without the country code " + callingCode + ".";
        }
        if (!UTIL.isValidNumberForRegion(number, region)) {
            return "This is not a valid phone number for " + country + ".";
        }
        return null;
    }

    private static String internationalProblem(String value) {
        if (!value.substring(1).chars().allMatch(c -> c >= '0' && c <= '9') || value.length() > MAX_DIGITS + 1) {
            return "Use + followed by digits only, with no spaces, letters or symbols.";
        }
        try {
            PhoneNumber number = UTIL.parse(value, null);
            return UTIL.isValidNumber(number) ? null : "This is not a valid international phone number.";
        } catch (NumberParseException ex) {
            return "This is not a valid international phone number.";
        }
    }

    private static PhoneNumber parse(String value, String region) {
        try {
            return UTIL.parse(value, region);
        } catch (NumberParseException ex) {
            throw new BusinessRuleException("recipientPhone", "This is not a valid phone number.");
        }
    }

    private static String countryName(String region) {
        return Locale.of("", region).getDisplayCountry(Locale.ENGLISH);
    }
}
