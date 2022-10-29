package com.mirambeau.termcalc;

import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import static android.content.ContentValues.TAG;

/**
 * Created by Vinicius Sauter on 28/09/2016.
 * ...
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class CurrencyConverter extends AsyncTask<Void, Void, Exception> {

    private static SortedMap<Currency, ArrayList<Locale>> currencyLocaleMap;
    private static HashMap<String, Double> rates = null;
    private static Calendar ratesDate = null;


    static {
        currencyLocaleMap = new TreeMap<>(new Comparator<Currency>() {
            @Override
            public int compare(Currency c1, Currency c2) {
                return c1.getCurrencyCode().compareTo(c2.getCurrencyCode());
            }
        });
        for (Locale locale : Locale.getAvailableLocales()) {
            try {
                Currency currency = Currency.getInstance(locale);
                if (!currencyLocaleMap.containsKey(currency)) {
                    ArrayList<Locale> array = new ArrayList<>();
                    array.add(locale);
                    currencyLocaleMap.put(currency, array);
                } else currencyLocaleMap.get(currency).add(locale);
            } catch (Exception ignored) {
            }
        }
    }

    Double returnValue = null;
    final double value;
    final String valueCurrency;
    final String desiredCurrency;
    final Callback callback;

    public CurrencyConverter(final double value, final String valueCurrency, final String desiredCurrency, final Callback callback) {
        this.value = value;
        this.valueCurrency = valueCurrency;
        this.desiredCurrency = desiredCurrency;
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        returnValue = null;
    }

    @Override
    protected Exception doInBackground(Void... params) {
        try {
            if (!Ax.ratesChecked) {
                generateRatesFromFloatRates();
                returnValue = calculate(value, valueCurrency, desiredCurrency);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return e;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Exception e) {
        callback.onValueCalculated(returnValue, e);
    }

    public interface Callback {
        void onValueCalculated(Double value, Exception e);
    }

    public static String getCurrencyFlag(String currencyCode) {
        return "https://www.ecb.europa.eu/shared/img/flags/" + currencyCode + ".gif";
    }

    public static String formatCurrencyValue(String currencyCode, double value) {
        Currency currency = Currency.getInstance(currencyCode);
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
        format.setCurrency(currency);
        return format.format(value);
    }

    public static ArrayList<Locale> getCurrencyLocale(Currency currencyCode) {
        return currencyLocaleMap.get(currencyCode);
    }

    public static String getCurrencySymbol(String currencyCode) {
        Currency currency = Currency.getInstance(currencyCode);
        if (currencyLocaleMap.get(currency).size() > 0) {
            Locale locale = currencyLocaleMap.get(currency).get(0);
            return currency.getSymbol(locale);
        }
        return "";
    }

    public static List<Currency> getCurrencyList() {
        return new ArrayList<>(currencyLocaleMap.keySet());
    }

    public String getStringFormat(String currencyCode, Number number) {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
        format.setCurrency(Currency.getInstance(currencyCode));
        return format.format(number);
    }

    public static void calculate(final double value, final Currency valueCurrency, final Currency desiredCurrency, final Callback callback) {
        calculate(value, valueCurrency.toString(), desiredCurrency.toString(), callback);
    }

    public static void calculate(final double value, final String valueCurrency, final String desiredCurrency, final Callback callback) {
        if (shouldGenerateRates()) {
            CurrencyConverter task = new CurrencyConverter(value, valueCurrency, desiredCurrency, callback);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else {
            try {
                callback.onValueCalculated(calculate(value, valueCurrency, desiredCurrency), null);
            } catch (Exception e) {
                e.printStackTrace();
                callback.onValueCalculated(null, e);
            }
        }
    }

    public static void reset() {
        ratesDate = null;
        rates = null;
    }

    private static boolean shouldGenerateRates() {
        if (ratesDate != null) {
            Calendar today = Calendar.getInstance();
            boolean isSameDay = ratesDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    ratesDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
            return !isSameDay;
        }
        return true;
    }

    /***
     *  https://www.floatrates.com/json-feeds.html
     *
     *  @throws Exception if you can not get the rates
     */
    public static void generateRatesFromFloatRates() throws Exception {
        rates = new HashMap<>();
        rates.put("BRL", 1D);

        URL url= null;

        try {
            url = new URL("https://www.floatrates.com/daily/brl.json");
        }
        catch (MalformedURLException e){
            e.printStackTrace();
            return;
        }

        InputStream stream = null;

        try {
            stream = url.openStream();
        }
        catch (IOException e){
            e.printStackTrace();
            return;
        }

        JsonReader reader = new JsonReader(new InputStreamReader(stream));
        reader.setLenient(true);
        reader.beginObject();
        while (reader.hasNext()) {
            String codeName = reader.nextName();
            reader.beginObject();
            String code = null;
            double rate = 0;
            while (reader.hasNext()) {
                String name = reader.nextName();
                switch (name) {
                    case "code":
                        code = reader.nextString();
                        break;
                    case "rate":
                        rate = reader.nextDouble();
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }
            reader.endObject();
            if (code != null)
                rates.put(code, rate);
        }
        reader.endObject();
        ratesDate = Calendar.getInstance();
    }

    /**
     * https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml
     *
     * @throws Exception if you can not get the rates
     */
    public static void generateRatesFromECB() throws Exception {
        rates = new HashMap<>();
        // EU Bank Currency Rate data source URL
        URL url = new URL("https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml");
        InputStream stream = url.openStream();
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp = spf.newSAXParser();

        XMLReader xr = sp.getXMLReader();
        xr.setContentHandler(new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                Log.d(TAG, "start element: localname=" + localName);
                for (int i = 0; i < attributes.getLength(); i++) {
                    Log.d(TAG, "start element: attr=" + attributes.getLocalName(i) + " value=" + attributes.getValue(i));
                }

                if ("Cube".equals(localName)) {
                    String name = null;
                    Double rate = null;
                    for (int i = 0; i < attributes.getLength(); i++) {
//                        if ("time".equals(attributes.getLocalName(i))) {
//                            time = attributes.getValue(i);
//                        } else
                        if ("currency".equals(attributes.getLocalName(i))) {
                            name = attributes.getValue(i);
                        } else if ("rate".equals(attributes.getLocalName(i))) {
                            rate = Double.parseDouble(attributes.getValue(i));
                        }
                    }
                    // add new element in the list
                    if (name != null)
                        rates.put(name, rate);
                }
            }

        });
        xr.parse(new InputSource(stream));
        ratesDate = Calendar.getInstance();
    }

    public static Double calculate(Double value, String valueCurrency, String desiredCurrency) throws TypeNotPresentException {
        double rateValue = -1.0, rateDesired = -1.0;

        int i;
        boolean valueFound = false, desiredFound = false;

        for (i=0; i < Ax.currencyCodes.length; i++){
            if (!valueFound && valueCurrency.equals(Ax.currencyCodes[i])) {
                rateValue = Ax.rates[i];
                valueFound = true;
            }
            if (!desiredFound && desiredCurrency.equals(Ax.currencyCodes[i])) {
                rateDesired = Ax.rates[i];
                desiredFound = true;
            }

            if (valueFound && desiredFound)
                break;
        }

        if (rateValue != -1.0 && rateDesired != -1.0)
            return rateValue == 0 ? 0 : rateDesired / rateValue * value;
        else
            throw new TypeNotPresentException("Currency not found.", new Throwable());
    }

    public static void sendRatesToAux(String[] codes) {
        if (codes == null || codes.length < 1)
            return;

        int i;

        for (i=0; i < codes.length; i++){
            try {
                if (Ax.rates[i] < 0)
                    Ax.rates[i] = rates.get(codes[i]);
            }
            catch (NullPointerException ignored) {}
        }
    }
}
