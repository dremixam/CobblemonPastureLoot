package com.dremixam.pastureloot.override;

/** One drop added by an override: the item, a quantity range and a chance in percent. */
public record DropSpec(String item, int minQuantity, int maxQuantity, double chance) {

    public static final double ALWAYS = 100;
}
