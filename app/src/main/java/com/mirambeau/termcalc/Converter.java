package com.mirambeau.termcalc;

import java.text.DecimalFormat;

public class Converter {
    final DecimalFormat outputFormat = new DecimalFormat("#,###.##");

    public double convert (double fromDub, int type, int from, int to){
        final double[] result = {0};

        String[][] allUnits = {{"IN", "FT", "YD", "MI", "MM", "CM", "M", "KM"}, {"µG", "MG", "CG", "G", "KG", "OZ", "LB", "N", "Stones"}, {"ML", "L", "M³", " OZ ", "CUP", "PT", "QT", "GAL"}, {"NS", "µS", "MS", "S", "MIN", "HR", "DAY", "WEEK"}, Ax.currencyCodes, {"MPH", "KM/H", "M/S", "FT/S", "KNOT", "-1", "-1", "-1"}, {"°F", "°C", "K", "-1", "-1", "-1", "-1", "-1"}, {"2", "3", "4", "6", "8", "10", "16", "32"}};

        final String selectedFrom = allUnits[type][from];
        final String selectedTo = allUnits[type][to];

        if (type == 4) {
            try {
                try {
                    String answer = outputFormat.format(calculate(fromDub, selectedFrom, selectedTo));

                    while (!Ax.isDigit(Ax.chat(answer, 0)))
                        answer = answer.substring(1);

                    while (!Ax.isDigit(Ax.chat(answer, answer.length() - 1)))
                        answer = Ax.newTrim(answer, 1);

                    answer = answer.replace(",", "");

                    return Double.parseDouble(answer);
                }
                catch (NullPointerException | NumberFormatException e) {
                    e.printStackTrace();
                }

                return -1;
            }
            catch (TypeNotPresentException e){
                e.printStackTrace();
                return -4;
            }
        }

        // ~ From ~
        switch (selectedFrom){
            //Distance
            case ("IN"):
                fromDub *= 1;
                break;
            case ("FT"):
                fromDub *= 12;
                break;
            case ("YD"):
                fromDub *= 36;
                break;
            case ("MI"):
                fromDub *= 63360;
                break;
            case ("MM"):
                fromDub *= 0.0393701;
                break;
            case ("CM"):
                fromDub *= 0.393701;
                break;
            case ("M"):
                fromDub *= 39.3701;
                break;
            case ("KM"):
                fromDub *= 39370.1;
                break;

            //Mass
            case ("µG"):
                fromDub *= 0.0001;
                break;
            case ("MG"):
                fromDub *= 0.1;
                break;
            case ("CG"):
                fromDub *= 1;
                break;
            case ("G"):
                fromDub *= 100;
                break;
            case ("KG"):
                fromDub *= 100000;
                break;
            case ("OZ"):
                fromDub *= 2834.95;
                break;
            case ("LB"):
                fromDub *= 45359.2;
                break;
            case ("N"):
                fromDub *= 0.0000981;
                break;
            case ("Stones"):
                fromDub *= 635029;
                break;
            default:
                break;

            //Volume
            case ("ML"):
                fromDub *= 0.001;
                break;
            case ("L"):
                fromDub *= 1;
                break;
            case ("M³"):
                fromDub *= 1000;
                break;
            case (" OZ "):
                fromDub *= 0.0295735;
                break;
            case ("CUP"):
                fromDub *= 0.236588124995964;
                break;
            case ("PT"):
                fromDub *= 0.473176;
                break;
            case ("QT"):
                fromDub *= 0.946353;
                break;
            case ("GAL"):
                fromDub *= 3.78541;
                break;

            //Time
            case ("NS"):
                fromDub *= Math.pow(10, -9);
                break;
            case ("µS"):
                fromDub *= Math.pow(10, -6);
                break;
            case ("MS"):
                fromDub *= 0.001;
                break;
            case ("S"):
                fromDub *= 1;
                break;
            case ("MIN"):
                fromDub *= 60;
                break;
            case ("HR"):
                fromDub *= 3600;
                break;
            case ("DAY"):
                fromDub *= (3600 * 24);
                break;
            case ("WEEK"):
                fromDub *= (3600 * 24 * 7);
                break;

            //Currency
            case ("USD"):
                fromDub *= 6.89;
                break;
            case ("EURO"):
                fromDub *= 8.15;
                break;
            case ("GBP"):
                fromDub *= 9.1;
                break;
            case ("RUPEE"):
                fromDub *= 0.092;
                break;
            case ("YUAN"):
                fromDub *= 1;
                break;
            case ("MEX. PESO"):
                fromDub *= 0.31;
                break;
            case ("COL. PESO"):
                fromDub *= 0.0018;
                break;
            case ("CUB. PESO"):
                fromDub *= 6.91;
                break;
            case ("BTC"):
                fromDub *= 28090.19;
                break;

            //Speed
            case ("MPH"):
                fromDub *= 1;
                break;
            case ("KM/H"):
                fromDub *= 0.621371;
                break;
            case ("M/S"):
                fromDub *= 2.23694;
                break;
            case ("FT/S"):
                fromDub *= 0.681818;
                break;
            case ("KNOT"):
                fromDub *= 1.15078;
                break;

            //Temperature
            case ("°F"):
                fromDub = (fromDub - 32) * (5.0 / 9.0);
                break;
            case ("°C"):
                fromDub *= 1;
                break;
            case ("K"):
                fromDub -= 273.15;
                break;
        }





        // ~ To ~
        switch (selectedTo){
            //Distance
            case ("IN"):
                result[0] = fromDub;
                break;
            case ("FT"):
                result[0] = fromDub / 12;
                break;
            case ("YD"):
                result[0] = fromDub / 36;
                break;
            case ("MI"):
                result[0] = fromDub / 63360;
                break;
            case ("MM"):
                result[0] = fromDub / 0.0393701;
                break;
            case ("CM"):
                result[0] = fromDub / 0.393701;
                break;
            case ("M"):
                result[0] = fromDub / 39.3701;
                break;
            case ("KM"):
                result[0] = fromDub / 39370.1;
                break;

            //Mass
            case ("µG"):
                result[0] = fromDub / 0.0001;
                break;
            case ("MG"):
                result[0] = fromDub / 0.1;
                break;
            case ("CG"):
                result[0] = fromDub;
                break;
            case ("G"):
                result[0] = fromDub / 100;
                break;
            case ("KG"):
                result[0] = fromDub / 100000;
                break;
            case ("OZ"):
                result[0] = fromDub / 2834.95;
                break;
            case ("LB"):
                result[0] = fromDub / 45359.2;
                break;
            case ("N"):
                result[0] = fromDub / 0.0000981;
                break;
            case ("Stones"):
                result[0] = fromDub / 635029;
                break;

            //Volume
            case ("ML"):
                result[0] = fromDub / 0.001;
                break;
            case ("L"):
                result[0] = fromDub / 1;
                break;
            case ("M³"):
                result[0] = fromDub / 1000;
                break;
            case (" OZ "):
                result[0] = fromDub / 0.0295735;
                break;
            case ("CUP"):
                result[0] = fromDub / 0.236588124995964;
                break;
            case ("PT"):
                result[0] = fromDub / 0.473176;
                break;
            case ("QT"):
                result[0] = fromDub / 0.946353;
                break;
            case ("GAL"):
                result[0] = fromDub / 3.78541;
                break;

            //Time
            case ("NS"):
                result[0] = fromDub / Math.pow(10, -9);
                break;
            case ("µS"):
                result[0] = fromDub / Math.pow(10, -6);
                break;
            case ("MS"):
                result[0] = fromDub / 0.001;
                break;
            case ("S"):
                result[0] = fromDub / 1;
                break;
            case ("MIN"):
                result[0] = fromDub / 60;
                break;
            case ("HR"):
                result[0] = fromDub / 3600;
                break;
            case ("DAY"):
                result[0] = fromDub / (3600 * 24);
                break;
            case ("WEEK"):
                result[0] = fromDub / (3600 * 24 * 7);
                break;

            //Currency
            case ("USD"):
                result[0] = fromDub / 6.89;
                break;
            case ("EURO"):
                result[0] = fromDub / 8.15;
                break;
            case ("GBP"):
                result[0] = fromDub / 9.1;
                break;
            case ("RUPEE"):
                result[0] = fromDub / 0.092;
                break;
            case ("YUAN"):
                result[0] = fromDub / 1;
                break;
            case ("MEX. PESO"):
                result[0] = fromDub / 0.31;
                break;
            case ("COL. PESO"):
                result[0] = fromDub / 0.0018;
                break;
            case ("CUB. PESO"):
                result[0] = fromDub / 6.91;
                break;
            case ("BTC"):
                result[0] = fromDub / 28090.19;
                break;

            //Speed
            case ("MPH"):
                result[0] = fromDub / 1;
                break;
            case ("KM/H"):
                result[0] = fromDub / 0.621371;
                break;
            case ("M/S"):
                result[0] = fromDub / 2.23694;
                break;
            case ("FT/S"):
                result[0] = fromDub / 0.681818;
                break;
            case ("KNOT"):
                result[0] = fromDub / 1.15078;
                break;

            //Temperature
            case ("°F"):
                result[0] = (fromDub * (9.0 / 5.0)) + 32;
                break;
            case ("°C"):
                result[0] = fromDub / 1;
                break;
            case ("K"):
                result[0] += 273.15;
                break;
        }

        return result[0];
    }

    public String bases(int input, int initBase, int finalBase){
        final String str = Integer.toString(input);

        if (!isValid(str, initBase))
            return "Error";

        return Integer.toString(Integer.parseInt(str.trim(), initBase), finalBase);
    }

    public boolean isValid(String str, int base){
        if (str == null)
            return false;

        int i;
        String[] nums = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

        for (i=base; i < 10; i++){
            if (str.contains(nums[i]))
                return false;
        }

        return true;
    }

    final public double calculate(Double value, String valueCurrency, String desiredCurrency) throws TypeNotPresentException {
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
}
