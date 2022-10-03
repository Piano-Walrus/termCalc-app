package com.mirambeau.termcalc;

import android.util.Log;

public class DateCalc {
    /**
     * Calculates the time between two dates, returned in the format "int months, int days, int years." This is also how each parameter must be passed to the function.
     */
    public static int[] compare(int[] initDate, int[] finalDate) {
        int i;
        int resultDay, resultMonth = 0, resultYear;
        final int[] error = new int[]{0, 0, 0, -1};

        int[] calendar = new int[12];
        
        if (initDate.length != 3 && finalDate.length != 3)
            return error;

        if (initDate[1] > 31 || finalDate[1] > 31 || initDate[0] > 12 || finalDate[0] > 12)
            return error;

        // If initDate is later than finalDate, swap them
        if (initDate[2] > finalDate[2] || (initDate[2] == finalDate[2] && initDate[0] > finalDate[0]) || (initDate[2] == finalDate[2] && initDate[0] == finalDate[0] && initDate[1] > finalDate[1])) {
            int[] temp = initDate;

            initDate = finalDate;
            finalDate = temp;
        }

        for (i = 0; i < 12; i++) {
            if (i == 0 || i == 2 || i == 4 || i == 6 || i == 7 || i == 9 || i == 11) {
                calendar[i] = 31;
            }
            else if (i == 1) {
                if (initDate[2] % 400 == 0) {
                    calendar[i] = 29;
                }
                else if (initDate[2] % 100 == 0) {
                    calendar[i] = 28;
                }
                else if (initDate[2] % 4 == 0) {
                    calendar[i] = 29;
                }
                else {
                    calendar[i] = 28;
                }
            }
            else {
                calendar[i] = 30;
            }
        }

        if (initDate[1] != 0 && finalDate[1] != 0) {
            resultYear = finalDate[2] - initDate[2];

            if (finalDate[0] >= initDate[0]) {
                resultMonth = finalDate[0] - initDate[0];
            }
            else if (finalDate[0] < initDate[0]) {
                resultMonth = 12 - initDate[0];
                resultMonth += finalDate[0];
                resultYear -= 1;

                if (resultYear < 0) {
                    resultYear = 0;
                }
            }

            if (finalDate[0] == initDate[0] && finalDate[1] < initDate[1]) {
                resultYear -= 1;
            }

            if (finalDate[1] >= initDate[1]) {
                resultDay = finalDate[1] - initDate[1];
            }
            else {
                //Log.d("uhh?", "initDate = [" + initDate[0] + ", " + initDate[1] + "]      finalDate = [" + finalDate[0] + ", " + finalDate[1] + "]");

                if (initDate[0] - 1 < 0)
                    initDate[0] = 1;

                try {
                    resultDay = calendar[initDate[0] - 1] - initDate[1];
                }
                catch (Exception e) {
                    e.printStackTrace();

                    resultDay = calendar[0] - initDate[1];
                }
                resultDay += finalDate[1];
                resultMonth -= 1;

                if (resultMonth < 0) {
                    resultMonth += 12;
                }
            }

            return new int[]{resultMonth, resultDay, resultYear};
        }
        else {
            return error;
        }
    }
}
