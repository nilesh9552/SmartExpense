package com.smartexpense.utils;

import android.content.Context;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CurrencyHelper {

    public static class CurrencyItem {
        private final String code;
        private final String name;
        private final String symbol;

        public CurrencyItem(String code, String name, String symbol) {
            this.code = code;
            this.name = name;
            this.symbol = symbol;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public String getSymbol() {
            return symbol;
        }

        public String getDisplayName() {
            return code + " - " + name + " (" + symbol + ")";
        }
    }

    public static List<CurrencyItem> getSupportedCurrencies() {
        List<CurrencyItem> list = new ArrayList<>();
        list.add(new CurrencyItem("USD", "US Dollar", "$"));
        list.add(new CurrencyItem("INR", "Indian Rupee", "₹"));
        list.add(new CurrencyItem("EUR", "Euro", "€"));
        list.add(new CurrencyItem("GBP", "British Pound", "£"));
        list.add(new CurrencyItem("JPY", "Japanese Yen", "¥"));
        list.add(new CurrencyItem("CAD", "Canadian Dollar", "CA$"));
        list.add(new CurrencyItem("AUD", "Australian Dollar", "AU$"));
        return list;
    }

    public static String getSymbolForCode(String code) {
        for (CurrencyItem item : getSupportedCurrencies()) {
            if (item.getCode().equalsIgnoreCase(code)) {
                return item.getSymbol();
            }
        }
        return "$";
    }

    public static String formatAmount(Context context, double amount) {
        PreferenceManager prefManager = new PreferenceManager(context);
        String symbol = prefManager.getCurrencySymbol();
        return formatWithSymbol(symbol, amount);
    }

    public static String formatWithSymbol(String symbol, double amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        DecimalFormat formatter = new DecimalFormat("#,##0.00", symbols);
        return symbol + " " + formatter.format(amount);
    }
}
