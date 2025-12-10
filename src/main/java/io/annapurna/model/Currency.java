package io.annapurna.model;

/**
 * Common trading currencies.
 */
public enum Currency {
    USD("US Dollar"),
    EUR("Euro"),
    GBP("British Pound"),
    JPY("Japanese Yen"),
    CHF("Swiss Franc"),
    AUD("Australian Dollar"),
    CAD("Canadian Dollar"),
    CNY("Chinese Yuan"),
    HKD("Hong Kong Dollar"),
    SGD("Singapore Dollar");

    private final String displayName;

    Currency(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}