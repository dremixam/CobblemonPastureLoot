package com.dremixam.pastureloot.override;

/** Reads a quantity written as a number (2) or as a range ("1-3"). */
final class QuantityRange {

    private QuantityRange() {
    }

    /** {min, max} with 0 <= min <= max, or null when the value cannot be read. */
    static int[] parse(Object value) {
        if (value instanceof Number number) {
            double quantity = number.doubleValue();
            if (quantity < 0 || quantity != Math.floor(quantity)) {
                return null;
            }
            return new int[]{(int) quantity, (int) quantity};
        }
        if (!(value instanceof String text)) {
            return null;
        }
        String[] parts = text.trim().split("-", -1);
        if (parts.length > 2) {
            return null;
        }
        try {
            int minimum = Integer.parseInt(parts[0].trim());
            int maximum = Integer.parseInt(parts[parts.length - 1].trim());
            return minimum >= 0 && minimum <= maximum ? new int[]{minimum, maximum} : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
