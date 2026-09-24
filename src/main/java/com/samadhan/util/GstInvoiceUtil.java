package com.samadhan.util;

import java.time.LocalDate;
import java.time.Month;
import java.util.Map;
import java.util.TreeMap;

// Helpers for turning a completed ride into a GST-compliant document (PaymentController
// #generateInvoice): which state a GSTIN belongs to (for the CGST+SGST vs IGST decision), what
// the current Indian financial year's label is (for invoice numbering), and spelling out a rupee
// amount in words (standard on Indian tax invoices/cheques).
public final class GstInvoiceUtil {

    private GstInvoiceUtil() {
    }

    // Official CBIC/GST state codes — the first two digits of any 15-character GSTIN. Stable,
    // publicly published, not something that changes with app data. 25 (Daman and Diu) and 28
    // (undivided Andhra Pradesh) are kept even though superseded, since GSTINs issued under the
    // old codes are still valid/in use.
    private static final Map<String, String> STATE_CODES = new TreeMap<>();
    static {
        STATE_CODES.put("01", "Jammu and Kashmir");
        STATE_CODES.put("02", "Himachal Pradesh");
        STATE_CODES.put("03", "Punjab");
        STATE_CODES.put("04", "Chandigarh");
        STATE_CODES.put("05", "Uttarakhand");
        STATE_CODES.put("06", "Haryana");
        STATE_CODES.put("07", "Delhi");
        STATE_CODES.put("08", "Rajasthan");
        STATE_CODES.put("09", "Uttar Pradesh");
        STATE_CODES.put("10", "Bihar");
        STATE_CODES.put("11", "Sikkim");
        STATE_CODES.put("12", "Arunachal Pradesh");
        STATE_CODES.put("13", "Nagaland");
        STATE_CODES.put("14", "Manipur");
        STATE_CODES.put("15", "Mizoram");
        STATE_CODES.put("16", "Tripura");
        STATE_CODES.put("17", "Meghalaya");
        STATE_CODES.put("18", "Assam");
        STATE_CODES.put("19", "West Bengal");
        STATE_CODES.put("20", "Jharkhand");
        STATE_CODES.put("21", "Odisha");
        STATE_CODES.put("22", "Chhattisgarh");
        STATE_CODES.put("23", "Madhya Pradesh");
        STATE_CODES.put("24", "Gujarat");
        STATE_CODES.put("25", "Daman and Diu");
        STATE_CODES.put("26", "Dadra and Nagar Haveli and Daman and Diu");
        STATE_CODES.put("27", "Maharashtra");
        STATE_CODES.put("28", "Andhra Pradesh (Old)");
        STATE_CODES.put("29", "Karnataka");
        STATE_CODES.put("30", "Goa");
        STATE_CODES.put("31", "Lakshadweep");
        STATE_CODES.put("32", "Kerala");
        STATE_CODES.put("33", "Tamil Nadu");
        STATE_CODES.put("34", "Puducherry");
        STATE_CODES.put("35", "Andaman and Nicobar Islands");
        STATE_CODES.put("36", "Telangana");
        STATE_CODES.put("37", "Andhra Pradesh");
        STATE_CODES.put("38", "Ladakh");
        STATE_CODES.put("97", "Other Territory");
        STATE_CODES.put("99", "Centre Jurisdiction");
    }

    /** Null if the value isn't a well-formed 15-character GSTIN. */
    public static String stateForGstin(String gstin) {
        if (gstin == null || gstin.trim().length() != 15) {
            return null;
        }
        return STATE_CODES.get(gstin.trim().substring(0, 2));
    }

    // Indian FY runs Apr 1 - Mar 31, labelled by its start/end years, e.g. "2025-26" for the year
    // starting April 2025.
    public static String currentFinancialYear(LocalDate date) {
        int startYear = date.getMonthValue() >= Month.APRIL.getValue() ? date.getYear() : date.getYear() - 1;
        int endYearShort = (startYear + 1) % 100;
        return startYear + "-" + String.format("%02d", endYearShort);
    }

    private static final String[] ONES = {
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
            "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
    };
    private static final String[] TENS = {
            "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };

    private static String twoDigitsInWords(int n) {
        if (n < 20) {
            return ONES[n];
        }
        String tens = TENS[n / 10];
        String ones = ONES[n % 10];
        return ones.isEmpty() ? tens : tens + " " + ones;
    }

    private static String threeDigitsInWords(int n) {
        if (n >= 100) {
            String rest = twoDigitsInWords(n % 100);
            return ONES[n / 100] + " Hundred" + (rest.isEmpty() ? "" : " " + rest);
        }
        return twoDigitsInWords(n);
    }

    // Indian numbering (lakh/crore), not the Western thousand/million grouping — standard on
    // Indian tax invoices. E.g. 1699.50 -> "Rupees One Thousand Six Hundred Ninety Nine and Fifty
    // Paise Only".
    public static String amountInWords(double amount) {
        long rupees = (long) Math.floor(amount + 1e-6);
        int paise = (int) Math.round((amount - rupees) * 100);
        if (paise == 100) {
            rupees += 1;
            paise = 0;
        }

        StringBuilder sb = new StringBuilder("Rupees ");
        if (rupees == 0) {
            sb.append("Zero");
        } else {
            long crore = rupees / 10000000;
            long lakh = (rupees / 100000) % 100;
            long thousand = (rupees / 1000) % 100;
            long hundredsBlock = rupees % 1000;

            StringBuilder parts = new StringBuilder();
            if (crore > 0) {
                parts.append(threeDigitsInWords((int) crore)).append(" Crore ");
            }
            if (lakh > 0) {
                parts.append(twoDigitsInWords((int) lakh)).append(" Lakh ");
            }
            if (thousand > 0) {
                parts.append(twoDigitsInWords((int) thousand)).append(" Thousand ");
            }
            if (hundredsBlock > 0) {
                parts.append(threeDigitsInWords((int) hundredsBlock));
            }
            sb.append(parts.toString().trim());
        }

        if (paise > 0) {
            sb.append(" and ").append(twoDigitsInWords(paise)).append(" Paise");
        }
        sb.append(" Only");
        return sb.toString();
    }
}
