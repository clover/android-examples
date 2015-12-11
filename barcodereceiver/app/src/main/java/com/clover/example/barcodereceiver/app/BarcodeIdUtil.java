package com.clover.example.barcodereceiver.app;

import android.util.Base64;

/**
 * Created by mmaietta on 10/15/15.
 */
public class BarcodeIdUtil {
    private final static Character[] BASE_32_DIGITS = {
            '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B',
            'C', 'D', 'E', 'F', 'G', 'H',
            'J', 'K', 'M', 'N', 'P', 'Q',
            'R', 'S', 'T', 'V', 'W', 'X',
            'Y', 'Z'
    };

    public static String safeBase64toBase32(String barcodeId) {
        if (barcodeId == null) {
            return null;
        }

        // If length 13 then assume its a base 32 barcodeId and return
        if (barcodeId.length() == 13) {
            return barcodeId;
        }

        // Try the base64 conversion, else return the original string
        try {
            return encodeBase32(Base64.decode(barcodeId, Base64.NO_WRAP));
        } catch (IllegalArgumentException ex) {
            return barcodeId;
        }
    }

    public static String encodeBase32(byte[] bytes) {
        StringBuilder sb = new StringBuilder((bytes.length * 8) / 5);
        int i = 0, index = 0, digit;
        int curByte, nextByte;

        while (i < bytes.length) {
            curByte = unsignByte(bytes, i); // unsign

            if (index > 3) { // we need at least 5 bits per char
                if (i + 1 < bytes.length) {
                    nextByte = unsignByte(bytes, i + 1);
                } else {
                    nextByte = 0;
                }
                digit = curByte & (0xFF >> index);
                index = (index + 5) % 8;
                digit <<= index;
                digit |= nextByte >> (8 - index);
                i++;
            } else {
                digit = (curByte >> (8 - (index + 5))) & 0x1F;
                index = (index + 5) % 8;
                if (index == 0) i++;
            }
            sb.append(BASE_32_DIGITS[digit]);
        }
        return sb.toString();
    }

    private static int unsignByte(byte[] bytes, int i) {
        return bytes[i] & 0xFF;
    }
}
