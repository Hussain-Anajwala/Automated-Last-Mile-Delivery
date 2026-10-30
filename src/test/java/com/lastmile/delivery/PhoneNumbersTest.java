package com.lastmile.delivery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lastmile.web.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/** The phone rule itself; web and API behaviour is covered in the controller and API tests. */
class PhoneNumbersTest {

    @Test
    void validIndianTenDigitNumbersAreStoredAsE164() {
        assertThat(PhoneNumbers.problem("IN", "9876543210")).isNull();
        assertThat(PhoneNumbers.toE164("IN", " 9876543210 ")).isEqualTo("+919876543210");
        assertThat(PhoneNumbers.toE164("IN", "9000000000")).isEqualTo("+919000000000");   // demo seed value
        assertThat(PhoneNumbers.toE164("IN", "2025550123")).isEqualTo("+912025550123");   // Pune landline
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "987654321    | Enter exactly 10 digits for India (you entered 9).",
            "98765432101  | Enter exactly 10 digits for India (you entered 11).",
            "09876543210  | Enter exactly 10 digits for India (you entered 11).",
            "919876543210 | Enter the 10-digit number without the country code 91.",
            "+919876543210| Enter the number without the country code; +91 is added from the country you chose.",
            "98765abc10   | Use digits only, with no spaces, letters or symbols.",
            "98765 43210  | Use digits only, with no spaces, letters or symbols.",
            "98765-43210  | Use digits only, with no spaces, letters or symbols.",
            "(987)6543210 | Use digits only, with no spaces, letters or symbols.",
            "0123456789   | This is not a valid phone number for India.",
            "1111111111   | This is not a valid phone number for India.",
    })
    void invalidIndianNumbersGetOneSpecificMessage(String input, String message) {
        assertThat(PhoneNumbers.problem("IN", input)).isEqualTo(message);
        assertThatThrownBy(() -> PhoneNumbers.toE164("IN", input))
                .isInstanceOf(BusinessRuleException.class).hasMessage(message)
                .extracting("field").isEqualTo("recipientPhone");
    }

    @ParameterizedTest
    @CsvSource({
            "GB, 7400123456,  +447400123456",
            "GB, 07400123456, +447400123456",   // UK numbers are usually written with the trunk 0
            "US, 2025550123,  +12025550123",
            "CA, 4165550123,  +14165550123",
            "AE, 501234567,   +971501234567",
            "SG, 81234567,    +6581234567",
            "DE, 15123456789, +4915123456789",
            "AU, 412345678,   +61412345678",
            "IT, 0612345678,  +390612345678",   // Italian landlines keep their leading 0 in E.164
    })
    void otherCountriesFollowTheirOwnNumberingRules(String region, String national, String e164) {
        assertThat(PhoneNumbers.problem(region, national)).isNull();
        assertThat(PhoneNumbers.toE164(region, national)).isEqualTo(e164);
    }

    @ParameterizedTest
    @CsvSource({
            "GB, 74001234,       This is not a valid phone number for United Kingdom.",
            "US, 1234567890,     This is not a valid phone number for United States.",
            "SG, 812345678,      This is not a valid phone number for Singapore.",
            "AE, 50123456,       This is not a valid phone number for United Arab Emirates.",
            "AE, 971501234567,   Enter the number without the country code 971.",
            "GB, 447400123456,   Enter the number without the country code 44.",
            "US, 12025550123,    Enter the number without the country code 1.",
            "GB, 00447400123456, Enter the number without the country code 44.",
            "XX, 9876543210,     Choose a country from the list.",
    })
    void otherCountriesRejectWrongLengthsAndEmbeddedCountryCodes(String region, String national, String message) {
        assertThat(PhoneNumbers.problem(region, national)).isEqualTo(message);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void blankIsLeftToTheRequiredFieldMessage(String blank) {
        assertThat(PhoneNumbers.problem("IN", blank)).isNull();
        assertThat(PhoneNumbers.problem(null, blank)).isNull();
        assertThatThrownBy(() -> PhoneNumbers.toE164("IN", blank)).hasMessage("Phone number is required.");
    }

    @Test
    void withoutACountryTheApiAcceptsFullInternationalOrIndianNationalNumbers() {
        assertThat(PhoneNumbers.toE164(null, "9876543210")).isEqualTo("+919876543210");
        assertThat(PhoneNumbers.toE164(null, "+447400123456")).isEqualTo("+447400123456");
        assertThat(PhoneNumbers.problem(null, "+4477009")).isEqualTo("This is not a valid international phone number.");
        assertThat(PhoneNumbers.problem(null, "+44 7700 900123"))
                .isEqualTo("Use + followed by digits only, with no spaces, letters or symbols.");
        assertThat(PhoneNumbers.problem(null, "12345")).isEqualTo("Enter exactly 10 digits for India (you entered 5).");
    }

    @ParameterizedTest
    @CsvSource({
            "+919876543210, IN, 9876543210",
            "+447400123456, GB, 7400123456",
            "+14165550123,  CA, 4165550123",
            "+12025550123,  US, 2025550123",
            "+390612345678, IT, 0612345678",
    })
    void storedE164ValuesRecoverTheirCountryAndNationalNumberAndRoundTrip(String stored, String region,
                                                                           String national) {
        PhoneNumbers.Parts parts = PhoneNumbers.split(stored);
        assertThat(parts).isEqualTo(new PhoneNumbers.Parts(region, national));
        assertThat(PhoneNumbers.toE164(parts.region(), parts.nationalNumber())).isEqualTo(stored);
    }

    @Test
    void legacyValuesAreShownUnchangedNeverRewritten() {
        assertThat(PhoneNumbers.split("9876543210")).isEqualTo(new PhoneNumbers.Parts("IN", "9876543210"));
        assertThat(PhoneNumbers.split("1234567")).isEqualTo(new PhoneNumbers.Parts("IN", "1234567"));
        assertThat(PhoneNumbers.split("+999123")).isEqualTo(new PhoneNumbers.Parts("IN", "+999123"));
        assertThat(PhoneNumbers.display("9876543210")).isEqualTo("9876543210");
        assertThat(PhoneNumbers.display("+999123")).isEqualTo("+999123");
        assertThat(PhoneNumbers.display("+919876543210")).isEqualTo("+91 98765 43210");
        assertThat(PhoneNumbers.display(null)).isNull();
    }

    @Test
    void theSelectorCoversEveryRegionInTheMetadataWithIndiaAvailable() {
        assertThat(PhoneNumbers.countries()).hasSizeGreaterThan(240)
                .contains(new PhoneNumbers.Country("IN", "India", 91),
                        new PhoneNumbers.Country("GB", "United Kingdom", 44),
                        new PhoneNumbers.Country("US", "United States", 1),
                        new PhoneNumbers.Country("CA", "Canada", 1))
                .extracting(PhoneNumbers.Country::name).isSorted();
        assertThat(PhoneNumbers.callingCodeLabel("IN")).isEqualTo("+91");
        assertThat(PhoneNumbers.callingCodeLabel("<script>")).isEqualTo("+91");
        assertThat(new PhoneNumbers.Country("IN", "India", 91).label()).isEqualTo("India (+91)");
    }
}
