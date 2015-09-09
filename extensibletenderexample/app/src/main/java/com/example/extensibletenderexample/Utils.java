package com.example.extensibletenderexample;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.NumberFormat;
import java.util.Currency;

/**
 * Created by mmaietta on 9/9/15.
 */
public class Utils {

    public static String nextRandomId() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }

    public static String longToAmountString(Currency currency, long amt) {
        NumberFormat format = NumberFormat.getCurrencyInstance();
        if (currency != null)
            format.setCurrency(currency);

        double currencyAmount = (double) amt / Math.pow(10.0D, (double) format.getCurrency().getDefaultFractionDigits());

        return format.format(currencyAmount);
    }
}
