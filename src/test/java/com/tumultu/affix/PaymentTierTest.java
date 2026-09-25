package com.tumultu.affix;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentTierTest {

    @Test
    void higherPaymentTiersHaveStrictlyHigherBiasExponents() {
        assertTrue(PaymentTier.IRON.biasExponent() < PaymentTier.GOLD.biasExponent());
        assertTrue(PaymentTier.GOLD.biasExponent() < PaymentTier.DIAMOND.biasExponent());
        assertTrue(PaymentTier.DIAMOND.biasExponent() < PaymentTier.NETHERITE.biasExponent());
    }
}
