package com.mirambeau.termcalc;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import ch.obermuhlner.math.big.BigDecimalMath;

public class BetterMath {
    final public static MathContext mc = new MathContext(40, RoundingMode.HALF_UP);
    final public static MathContext smolMc = new MathContext(20, RoundingMode.HALF_UP);

    public static String pi;
    public static String e;

    public static void main(String[] args) {
        System.out.println("Original expression: " + args[0]);

        try {
            System.out.println(formatResult(evaluate(args[0], args.length >= 2 && Boolean.parseBoolean(args[1]), true, smolMc, smolMc.getPrecision() / 2, true), mc, 8));
        }
        catch (NaNException nan) {
            System.out.println(nan.getMessage());
        }
    }

    public static String formatResult(BigDecimal result, MathContext mc, int scale) {
        String resultStr = result.toPlainString();
        String eFormatPattern = "0.00";
        String normalFormatPattern = "#,###.##";

        if (scale > 2) {
            String zeros = Integer.toString((int) Math.pow(10, scale - 2)).substring(1);

            eFormatPattern += zeros + "E0";
            normalFormatPattern += zeros.replace("0", "#");
        }

        if (result.abs().compareTo(BigDecimal.ONE) < 0 && result.setScale(scale, RoundingMode.HALF_UP).compareTo(BigDecimal.ZERO) != 0)
            return result.setScale(scale, RoundingMode.HALF_UP).toPlainString();

        if (result.compareTo(BigDecimal.valueOf(Double.MAX_VALUE)) >= 0)
            return resultStr.charAt(0) + "." + resultStr.substring(1, 7) + "E" + (resultStr.length() - 1);
        else if (result.compareTo(BigDecimal.valueOf(Double.MAX_VALUE).subtract(BigDecimal.ONE, mc).negate(mc)) <= 0)
            return resultStr.substring(0, 2) + "." + resultStr.substring(2, 8) + "E" + (resultStr.length() - 2);
        else {
            try {
                if (!resultStr.equals("0") && result.compareTo(BigDecimal.ZERO) != 0 && (result.abs(mc).compareTo(parseBigDecimal("1000000000000", new MathContext(15, RoundingMode.HALF_UP))) >= 0 || result.abs(mc).compareTo(parseBigDecimal("0.000001", new MathContext(10, RoundingMode.HALF_UP))) <= 0))
                    return new DecimalFormat(eFormatPattern).format(Double.parseDouble(resultStr));
                else
                    return new DecimalFormat(normalFormatPattern).format(Double.parseDouble(resultStr));
            }
            catch (Exception e) {
                return result.toPlainString();
            }
        }
    }

    public static BigDecimal evaluate(String eq, boolean prioritizeCoefficients, int scale) throws NaNException {
        return evaluate(eq, prioritizeCoefficients, true, mc, scale, true);
    }

    public static BigDecimal evaluate(String eq, boolean prioritizeCoefficients, boolean isRad, MathContext mc, int scale, boolean debug) throws NaNException {
        int i;
        int parenthesisDifference = Ax.charDiff(eq, "(", ")");

        if (eq.contains("Error"))
            throw new NaNException("Parse Error");

        eq = eq.trim();

        eq = eq.replace(Ax.emDash, "-");
        eq = eq.replace(Ax.multiDot, "*");
        eq = eq.replace(Ax.bulletDot, "*");
        eq = eq.replace(Ax.multi, "*");
        eq = eq.replace(Ax.divi, "/");
        eq = eq.replace("÷", "/");

        if (eq.startsWith("-("))
            eq = "-1(" + eq.substring(2);

        if (eq.endsWith(Ax.sq))
            throw new NaNException("Parse Error");

        if (Ax.isFullSignedNumE(eq) && !eq.contains(Ax.pi) && !eq.contains("e"))
            return parseBigDecimal(eq.replace(",", ""), mc);

        eq = eq.replace("ln", "log" + Ax.eSub);

        for (i=0; i < parenthesisDifference; i++) {
            eq += ")";
        }

        ArrayList<String> eqArray = parseEq(eq);

        final MathContext piMc = new MathContext(scale + 1, RoundingMode.HALF_UP);

        pi = BigDecimalMath.pi(piMc).toPlainString();
        e = BigDecimalMath.e(piMc).toPlainString();

        //TODO: Replace all the while loops with the new for loop thingy

        //Replace pi symbol with value 3.1415...
        while (eqArray.contains(Ax.pi)) {
            int index = eqArray.indexOf(Ax.pi);

            eqArray.set(index, pi);

            try {
                if (Ax.isFullSignedNumE(eqArray.get(index - 1))) {
                    eqArray.add(index, "*");
                    index++;
                }
            }
            catch (Exception ignored) {}

            try {
                if (Ax.isFullSignedNumE(eqArray.get(index + 1)))
                    eqArray.add(index + 1, "*");
            }
            catch (Exception ignored) {}
        }

        //Replace e symbol with value 2.7182...
        while (eqArray.contains("e")) {
            int index = eqArray.indexOf("e");

            eqArray.set(index, e);

            try {
                if (Ax.isFullSignedNumE(eqArray.get(index - 1))) {
                    eqArray.add(index, "*");
                    index++;
                }
            }
            catch (Exception ignored) {}

            try {
                if (Ax.isFullSignedNumE(eqArray.get(index + 1)))
                    eqArray.add(index + 1, "*");
            }
            catch (Exception ignored) {}
        }

        //Handle Negative Signs
        for (i=0; i < eqArray.size(); i++) {
            if ((eqArray.get(i).equals("-"))) {
                String previous = "~";

                try {
                    if (i > 0)
                        previous = eqArray.get(i - 1);
                }
                catch (Exception ignored) {}

                if ((i == 0 || (!Ax.isFullNum(previous) && !previous.equals(")") && !previous.equals("!")))) {
                    if (Ax.isFullNum(eqArray.get(i + 1))) {
                        eqArray.set(i + 1, parseBigDecimal(eqArray.get(i + 1), mc).negate(mc).toPlainString());
                        eqArray.remove(i);
                    }
                }
            }
        }

        if (debug)
            System.out.println(eqArray);

        //Handle Parenthesis
        while (eqArray.contains("(") || eqArray.contains(")")) {
            int start = eqArray.lastIndexOf("(");
            int end = new ArrayList<>(eqArray.subList(start + 1, eqArray.size())).indexOf(")") + start + 1;

            ArrayList<String> subList;

            try {
                subList = new ArrayList<>(eqArray.subList(start + 1, end));
            }
            catch (Exception e) {
                throw new NaNException("Parse Error");
            }

            eqArray.set(start, evaluate(subList.toString().trim().replace("[", "").replace("]", "").replace(",", "").replace(" ", ""), prioritizeCoefficients, isRad, mc, scale, debug).toPlainString());

            try {
                if (Ax.isFullSignedNum(eqArray.get(end + 1)) || eqArray.get(end + 1).equals(Ax.sq))
                    eqArray.add(end + 1, "*");
            }
            catch (Exception ignored) {}

            for (i=0; i < end-start; i++) {
                eqArray.remove(start+1);
            }

            //Handle coefficients that appear before parenthesis
            try {
                if (Ax.isFullSignedNum(eqArray.get(start-1)) && (start < 2 || !eqArray.get(start-2).equals(Ax.sq))) {
                    if (prioritizeCoefficients) {
                        eqArray.add(start + 1, ")");
                        eqArray.add(start, "*");
                        eqArray.add(start - 1, "(");
                    }
                    else {
                        eqArray.add(start, "*");
                    }
                }
                else if (eqArray.get(start-1).equals("!") || eqArray.get(start-1).equals(")")) {
                    eqArray.add(start, "*");
                }
                else if (start >= 2 && Ax.isFullSignedNum(eqArray.get(start-1)) && eqArray.get(start-2).equals(Ax.sq)) {
                    eqArray.add(start, "*");
                    eqArray.set(start-1, sqrt(parseBigDecimal(eqArray.get(start-1), mc), mc).toPlainString());
                    eqArray.remove(start-2);
                }
            }
            catch (Exception ignored) {}

            if (debug)
                System.out.println(eqArray);
        }

        //TODO: Handle parenthesis the first time so it doesn't have to iterate through the entire array twice
        //Handle Negative Signs (again)
        for (i=0; i < eqArray.size(); i++) {
            if ((eqArray.get(i).equals("-"))) {
                String previous = "~";

                try {
                    if (i > 0)
                        previous = eqArray.get(i - 1);
                }
                catch (Exception ignored) {}

                if ((i == 0 || (!Ax.isFullNum(previous) && !previous.equals(")") && !previous.equals("!")))) {
                    if (Ax.isFullNum(eqArray.get(i + 1))) {
                        eqArray.set(i + 1, parseBigDecimal(eqArray.get(i + 1), mc).negate(mc).toPlainString());
                        eqArray.remove(i);
                    }
                }
            }
        }

        //Handle Trig
        for (i=0; i < eqArray.size(); i++) {
            String current = eqArray.get(i);
            String next;

            if (Ax.trigList.contains(current)) {
                try {
                    next = eqArray.get(i+1);
                }
                catch (IndexOutOfBoundsException e) {
                    throw new NaNException("Parse Error");
                }

                if (Ax.isFullSignedNum(next)) {
                    try {
                        eqArray.set(i, Trig.evaluate(current, next, mc, isRad, scale));
                    }
                    catch (ArithmeticException e) {
                        throw new NaNException("NaN");
                    }

                    String trigTest = eqArray.get(i);

                    String zeros = Integer.toString((int) Math.pow(10, scale)).substring(1);
                    String nines = zeros.replace("0", "9");

                    if (trigTest.replace("-", "").contains("." + zeros))
                        eqArray.set(i, trigTest.substring(0, trigTest.indexOf(".")));
                    else if (trigTest.contains("." + nines))
                        eqArray.set(i, parseBigDecimal(trigTest.substring(0, trigTest.indexOf("."))).add(BigDecimal.ONE).toPlainString());

                    eqArray.remove(i + 1);
                }
            }
        }

        if (debug)
            System.out.println(eqArray);

        //Handle log and ln
        while (eqArray.contains("log")) {
            int index = eqArray.indexOf("log");
            String next, base = "~";

            try {
                next = eqArray.get(index+1);
            }
            catch (IndexOutOfBoundsException e) {
                throw new NaNException("Parse Error");
            }

            if (Ax.isFullSubNum(next)) {
                base = next;

                try {
                    next = eqArray.get(index+2);
                }
                catch (IndexOutOfBoundsException e) {
                    throw new NaNException("Parse Error");
                }
            }

            if (!Ax.isFullSignedNum(next))
                throw new NaNException("Parse Error");
            else if (Ax.isFullSignedNum(next) && next.startsWith("-"))
                throw new NaNException("NaN");

            //Log Base 10
            if (Ax.isFullSignedNum(next) && (base.equals("10") || base.equals("~"))) {
                try {
                    eqArray.set(index, BigDecimalMath.log10(BigDecimalMath.toBigDecimal(next, mc), mc).toPlainString());
                }
                catch (ArithmeticException e) {
                    throw new NaNException("NaN");
                }

                eqArray.remove(index+1);

                if (base.equals("10"))
                    eqArray.remove(index+1);
            }
            else if (!base.equals("~")) {
                //Natural Log
                if (base.equals(Ax.eSub)) {
                    try {
                        eqArray.set(index, BigDecimalMath.log(BigDecimalMath.toBigDecimal(next, mc), mc).toPlainString());
                    }
                    catch (ArithmeticException e) {
                        throw new NaNException("NaN");
                    }

                    eqArray.remove(index + 1);
                    eqArray.remove(index + 1);
                }
                //Log Base 2
                else if (base.equals(Ax.subscripts[2])) {
                    try {
                        eqArray.set(index, BigDecimalMath.log2(BigDecimalMath.toBigDecimal(next, mc), mc).toPlainString());
                    }
                    catch (ArithmeticException e) {
                        throw new NaNException("NaN");
                    }

                    eqArray.remove(index + 1);
                    eqArray.remove(index + 1);
                }
                //Log Base n
                else if (Ax.isFullSubNum(base)) {
                    if (base.equals("0")) {
                        throw new NaNException("Nan");
                    }
                    else {
                        base = Ax.subToNum(base);

                        try {
                            eqArray.set(index, logBase(base, next, new MathContext((mc.getPrecision() / 5) + scale, RoundingMode.HALF_UP), scale).toPlainString());
                        }
                        catch (Exception e) {
                            throw new NaNException("Parse Error");
                        }

                        eqArray.remove(index + 1);
                        eqArray.remove(index + 1);
                    }
                }
                else
                    throw new NaNException("Parse Error");
            }

            if (debug)
                System.out.println(eqArray);
        }

        //Handle Roots
        while (eqArray.contains(Ax.sq)) {
            int index = eqArray.indexOf(Ax.sq);

            try {
                if (Ax.isFullNum(eqArray.get(index+1))) {
                    //N-th Root
                    if (index >= 1 && Ax.superlist.contains(eqArray.get(index-1))) {
                        //TODO: Handle non-integer nth-roots (have parseEq group all superscripts
                        // in a row so i can just isFullSuperNum(previous)
                        eqArray.set(index-1, newPow(parseBigDecimal(eqArray.get(index+1), mc), BigDecimal.ONE.divide(parseBigDecimal(Integer.toString(Ax.superlist.indexOf(eqArray.get(index-1))), mc), mc)).toPlainString());

                        eqArray.remove(index);
                        eqArray.remove(index);
                    }
                    //Square Root
                    else {
                        eqArray.set(index, sqrt(parseBigDecimal(eqArray.get(index + 1), mc), mc).toPlainString());
                        eqArray.remove(index + 1);
                    }
                }
            }
            catch (Exception e) {
                try {
                    if (index < 0) {
                        index = 0;

                        eqArray.set(index, sqrt(parseBigDecimal(eqArray.get(index + 1), mc), mc).toPlainString());
                        eqArray.remove(index + 1);
                    }
                    else {
                        eqArray.remove(index);
                    }
                }
                catch (Exception e2) {
                    throw new NaNException("NaN");
                }
            }

            if (debug)
                System.out.println(eqArray);
        }

        //Handle Factorials
        while (eqArray.contains("!")) {
            String init = eqArray.toString();

            if (eqArray.get(0).equals("!"))
                throw new NaNException("Parse Error");

            int numIndex = eqArray.indexOf("!") - 1;

            if (Ax.isFullSignedNum(eqArray.get(numIndex))){
                eqArray.set(numIndex, fact(eqArray.get(numIndex), mc));
                eqArray.remove(numIndex + 1);
            }
            else
                throw new NaNException("NaN");

            if (init.equals(eqArray.toString()))
                throw new NaNException("Parse Error");

            if (debug)
                System.out.println(eqArray);
        }

        //Handle Exponents
        for (int index = eqArray.indexOf("^"); index != -1; index = eqArray.indexOf("^")) {
            int initLength = eqArray.size();

            if (index == 0)
                throw new NaNException("Parse Error");

            if (Ax.isFullNum(eqArray.get(index-1)) && Ax.isFullNum(eqArray.get(index+1))) {
                String previous = eqArray.get(index - 1);
                String next = eqArray.get(index + 1);

                try {
                    int lengthCheck;

                    try {
                        lengthCheck = previous.substring(0, previous.indexOf(".")).replace(",", "").replace("-", "").length();
                    }
                    catch (Exception e) {
                        if (!debug)
                            e.printStackTrace();

                        lengthCheck = previous.replace(",", "").length();
                    }

                    try {
                        lengthCheck += next.substring(0, next.indexOf(".")).replace(",", "").replace("-", "").length();
                    }
                    catch (Exception e) {
                        if (!debug)
                            e.printStackTrace();

                        lengthCheck += next.replace(",", "").length();
                    }

                    if (lengthCheck <= 7)
                        eqArray.set(index - 1, BigDecimalMath.pow(parseBigDecimal(previous, mc), parseBigDecimal(next, mc), mc).toPlainString());
                    else
                        throw new NaNException("Error: Result too large");
                }
                catch (Exception e) {
                    if (e.getMessage() != null && e.getMessage().startsWith("Error"))
                        throw new NaNException(e.getMessage());
                    else
                        throw new NaNException("Parse Error");
                }

                eqArray.remove(index);
                eqArray.remove(index);
            }

            if (initLength == eqArray.size())
                throw new NaNException("Parse Error");

            if (debug)
                System.out.println(eqArray);
        }

        //Handle Multiplication, Division, and Modulus
        if (eqArray.contains("*") || eqArray.contains("%") || eqArray.contains("/")) {
            int initLength = eqArray.size();

            for (i = 0; i < eqArray.size(); i++) {
                String current = eqArray.get(i);
                String previous = "", next = "";

                if (!eqArray.contains("*") && !eqArray.contains("/") && !eqArray.contains("%"))
                    break;

                try {
                    previous = eqArray.get(i - 1);
                }
                catch (Exception ignored) {
                }

                try {
                    next = eqArray.get(i + 1);
                }
                catch (Exception ignored) {
                }

                if (Ax.isBinaryOp(current) && Ax.isFullSignedNum(previous) && Ax.isFullSignedNum(next) && !(Ax.isNull(current) || Ax.chat(current, 0) == null)) {
                    if (current.equals("*")) {
                        eqArray.set(i - 1, parseBigDecimal(previous, mc).multiply(parseBigDecimal(next, mc), mc).toPlainString());

                        eqArray.remove(i);

                        try {
                            eqArray.remove(i);
                        }
                        catch (Exception ignored) {
                        }
                    }
                    else if (current.equals("/")) {
                        try {
                            eqArray.set(i - 1, parseBigDecimal(previous, mc).divide(parseBigDecimal(next, mc), mc).toPlainString());
                        }
                        catch (ArithmeticException e) {
                            throw new NaNException("NaN");
                        }

                        eqArray.remove(i);

                        try {
                            eqArray.remove(i);
                        }
                        catch (Exception ignored) {
                        }
                    }
                    else if (current.equals("%")) {
                        eqArray.set(i - 1, modulus(parseBigDecimal(previous, mc), parseBigDecimal(next, mc), mc).toPlainString());

                        eqArray.remove(i);

                        try {
                            eqArray.remove(i);
                        }
                        catch (Exception ignored) {
                        }
                    }
                }

                if (i >= eqArray.size() - 1 && (eqArray.contains("*") || eqArray.contains("/") || eqArray.contains("%"))) {
                    if (initLength != eqArray.size())
                        i = 0;
                    else
                        throw new NaNException(eqArray.toString().contains("/, 0, ") ? "NaN" : "Parse Error");
                }
            }

            if (debug)
                System.out.println(eqArray);
        }

        //Handle Addition & Subtraction
        if (eqArray.contains("+") || eqArray.contains("-")) {
            int initLength = eqArray.size();

            for (i = 0; i < eqArray.size(); i++) {
                String current = eqArray.get(i);
                String previous = "", next = "";

                try {
                    previous = eqArray.get(i - 1);
                }
                catch (Exception ignored) {
                }

                try {
                    next = eqArray.get(i + 1);
                }
                catch (Exception ignored) {
                }

                if (Ax.isBinaryOp(current) && Ax.isFullSignedNum(previous) && Ax.isFullSignedNum(next) && !(Ax.isNull(current) || Ax.chat(current, 0) == null)) {
                    if (current.equals("+")) {
                        eqArray.set(i - 1, parseBigDecimal(previous, mc).add(parseBigDecimal(next, mc), mc).toPlainString());

                        eqArray.remove(i);

                        try {
                            eqArray.remove(i);
                        }
                        catch (Exception ignored) {
                        }
                    }
                    else if (current.equals("-")) {
                        if (i == 0) {
                            eqArray.set(1, parseBigDecimal(next, mc).negate(mc).toPlainString());

                            eqArray.remove(0);
                        }
                        else {
                            eqArray.set(i - 1, parseBigDecimal(previous, mc).subtract(parseBigDecimal(next, mc), mc).toPlainString());

                            eqArray.remove(i);

                            try {
                                eqArray.remove(i);
                            }
                            catch (Exception ignored) {
                            }
                        }
                    }
                }

                if (i >= eqArray.size() - 1 && (eqArray.contains("+") || eqArray.contains("-"))) {
                    if (initLength != eqArray.size())
                        i = 0;
                    else
                        throw new NaNException("Parse Error");
                }
            }

            if (debug)
                System.out.println(eqArray);
        }

        try {
            if (eqArray.size() > 1) {
                if (debug)
                    System.out.println(eqArray);

                throw new NaNException("Parse Error");
            }
        }
        catch (Exception e) {
            e.printStackTrace();

            throw new NaNException("Parse Error");
        }

        if (eqArray.size() > 0 && (Ax.isFullNum(eqArray.get(0)) || (eqArray.get(0).length() > 1 && Ax.isFullNum(eqArray.get(0).substring(1)) && (eqArray.get(0).startsWith("-")))))
            return parseBigDecimal(eqArray.get(0), mc);
        else
            throw new NaNException("Parse Error");
    }

    public static ArrayList<String> parseEq (String eq) {
        int i, j;
        ArrayList<String> eqArray = new ArrayList<>();

        for (i=0; i < eq.length(); i++) {
            String current = Ax.chat(eq, i);

            if (current == null || current.equals("\0") || current.equals(" ") || current.equals(","))
                continue;

            if (Ax.isDigit(current) || current.equals(".")) {
                String lastItem = eqArray.size() > 0 ? eqArray.get(eqArray.size() - 1) : "";

                //TODO: Handle superscripts preceding numbers
                if (eqArray.size() < 1 || (Ax.isOp(lastItem) || Ax.trigList.contains(lastItem) || lastItem.equals(Ax.pi) || lastItem.equals("e") || lastItem.equals("ₑ")))
                    eqArray.add(current);
                else
                    eqArray.set(eqArray.size() - 1, lastItem + current);
            }
            //TODO: Parse n-th roots properly
            else if (Ax.isOp(current) || current.equals(Ax.pi) || current.equals("e") || Ax.superlist.contains(current) ||
                    Ax.sublist.contains(current) || current.equals("ₑ") || current.equals(Ax.superMinus)) {
                String lastItem = eqArray.size() > 0 ? eqArray.get(eqArray.size() - 1) : "";

                if (Ax.isDigit(lastItem) && current.equals(Ax.sq))
                    eqArray.add("*");
                else if (lastItem != null && current.equals(Ax.superMinus) && Ax.trigList.contains(lastItem) && i < eq.length() - 1 && Ax.chat(eq, i+1) != null && Ax.chat(eq, i+1).equals(Ax.superscripts[1])) {
                    eqArray.set(eqArray.size() - 1, lastItem + Ax.superMinus + Ax.superscripts[1]);
                    i++;
                    continue;
                }

                eqArray.add(current);
            }
            else {
                String trigCheck = "~";

                //An extremely lazy way to check for log or trig functions
                try {
                    trigCheck = eq.substring(i, i+6);
                }
                catch (Exception e) {
                    try {
                        trigCheck = eq.substring(i, i+5);
                    }
                    catch (Exception e2) {
                        try {
                            trigCheck = eq.substring(i, i+3);
                        }
                        catch (Exception e3) {
                            try {
                                trigCheck = eq.substring(i, i+2);
                            }
                            catch (Exception e4) {
                                try {
                                    trigCheck = eq.substring(i, i+1);
                                }
                                catch (Exception ignored) {}
                            }
                        }
                    }
                }

                if (!trigCheck.equals("~")) {
                    int length = Ax.trigIn.length - 1;

                    for (j=length; j >= 0; j--) {
                        if (j == length) {
                            boolean isLog = false;

                            if (trigCheck.startsWith("log")) {
                                eqArray.add(trigCheck.substring(0, 3));

                                isLog = true;
                            }
                            else if (trigCheck.startsWith("ln")) {
                                eqArray.add(trigCheck.substring(0, 2));

                                isLog = true;
                            }

                            if (isLog) {
                                try {
                                    //In an effort to be as unnecessary and complicated as possible, this literally just increments
                                    //by the length of either "log" or "ln"
                                    i += eqArray.get(eqArray.size() - 1).length() - 1;
                                }
                                catch (Exception ignored) {}

                                //Handle coefficients before log or ln
                                try {
                                    if (Ax.isFullSignedNum(eqArray.get(eqArray.size() - 2))) {
                                        eqArray.add(eqArray.size() - 1, "*");
                                    }
                                }
                                catch (Exception ignored) {}

                                break;
                            }
                        }

                        if (trigCheck.startsWith(Ax.trigIn[j])) {
                            eqArray.add(trigCheck.substring(0, Ax.trigIn[j].length()));
                            eq = eq.substring(0, i) + eq.substring(i + Ax.trigIn[j].length() - 1);

                            try {
                                if (Ax.isFullSignedNum(eqArray.get(eqArray.size() - 2))) {
                                    eqArray.add(eqArray.size() - 1, "*");
                                }
                            }
                            catch (Exception ignored) {}

                            break;
                        }
                    }
                }
            }
        }

        return eqArray;
    }

    public static BigDecimal logBase(String base, String  num) throws NaNException {
        return logBase(parseBigDecimal(base), parseBigDecimal(num), smolMc);
    }

    public static BigDecimal logBase(String base, String  num, MathContext mc, int scale) throws NaNException {
        return logBase(parseBigDecimal(base, mc, scale), parseBigDecimal(num, mc, scale), mc);
    }

    public static BigDecimal logBase(BigDecimal base, BigDecimal num) throws NaNException {
        return logBase(base, num, smolMc);
    }

    public static BigDecimal logBase(BigDecimal base, BigDecimal num, MathContext mc) throws NaNException {
        BigDecimal result = BigDecimal.ZERO;

        if (num == null || mc == null)
            throw new NaNException("Parse Error");

        if (num.compareTo(BigDecimal.ZERO) <= 0 || base.compareTo(BigDecimal.ZERO) <= 0 || base.compareTo(BigDecimal.ONE) == 0)
            throw new NaNException("NaN");

        if (num.compareTo(BigDecimal.ONE) == 0)
            return BigDecimal.ZERO;
        else if (num.compareTo(base) == 0)
            return BigDecimal.ONE;
        else if (num.compareTo(base) != 0) {
            BigDecimal initNum = num;
            String maxIncrement = "0.25", minIncrement = "0.1";

            if (num.compareTo(base) >= 0) {
                while (num.compareTo(BigDecimal.ONE) > 0) {
                    num = num.divide(base, mc);

                    if (num.compareTo(BigDecimal.ONE) > 0)
                        result = result.add(BigDecimal.ONE);
                    else {
                        break;
                    }
                }
            }

            BigDecimal lowerBound = BigDecimalMath.pow(base, result, mc);
            BigDecimal upperBound = BigDecimalMath.pow(base, result.add(BigDecimal.ONE), mc);

            BigDecimal bound;

            BigDecimal increment = parseBigDecimal(maxIncrement, mc);
            BigDecimal previousBound, previousIncrement;

            int i = 0;
            int maxPrecision = mc.getPrecision();
            int whichBound = initNum.subtract(lowerBound, mc).abs(mc).compareTo(initNum.subtract(upperBound, mc).abs(mc));

            //Add or subtract from middle bound
            if (whichBound == 0) {
                int direction = BigDecimalMath.pow(base, result, mc).compareTo(initNum);
                result = result.add(parseBigDecimal("0.5", mc), mc);

                if (direction == 0)
                    return result;
                else {
                    bound = direction < 0 ? lowerBound : upperBound;

                    while ((direction < 0 ? initNum.compareTo(bound) : bound.compareTo(initNum)) >= 0) {
                        result = result.add(direction < 0 ? increment : increment.negate());
                        bound = BigDecimalMath.pow(base, result, mc);

                        if (i < maxPrecision && (direction < 0 ? bound.compareTo(initNum) : initNum.compareTo(bound)) > 0) {
                            result = result.add(direction < 0 ? increment.negate() : increment);
                            bound = BigDecimalMath.pow(base, result, mc);

                            if (increment.toPlainString().contains("1"))
                                i++;

                            increment = updateIncrement(increment, maxIncrement, minIncrement);
                        }

                        if (result.scale() > base.scale())
                            break;
                    }
                }
            }
            //Add from lower bound (whichBound < 0) or subtract from upper bound (whichBound > 0)
            else {
                if (whichBound > 0)
                    result = result.add(BigDecimal.ONE);

                bound = whichBound > 0 ? upperBound : lowerBound;

                for (i = 0;
                     i < maxPrecision && (whichBound > 0 ? initNum.compareTo(bound) : bound.compareTo(initNum)) <= 0;
                     i += increment.toPlainString().contains(maxIncrement.replace("0.", "")) && previousIncrement.toPlainString().contains(minIncrement.replace("0.", "")) ? 1 : 0) {
                    previousIncrement = increment;
                    previousBound = bound;

                    result = result.add(whichBound > 0 ? increment.negate() : increment);
                    bound = BigDecimalMath.pow(base, result, mc);

                    if ((whichBound > 0 ? bound.compareTo(initNum) : initNum.compareTo(bound)) < 0) {
                        bound = previousBound;
                        result = result.add(whichBound > 0 ? increment : increment.negate());

                        increment = updateIncrement(increment, maxIncrement, minIncrement);
                    }

                    if (result.scale() > base.scale())
                        break;

                    //System.out.println("count = " + count++ + "     increment = " + previousIncrement + "    " + (previousIncrement.toPlainString().contains("2.5") ? "" : "  ") + "i = " + i + "     power = " + result + "     bound = " + bound);
                }
            }

            return result;
        }
        else
            throw new NaNException("NaN");
    }

    public static BigDecimal updateIncrement(BigDecimal increment, String maxIncrement, String minIncrement) {
        maxIncrement = maxIncrement.replace("0.", "");
        minIncrement = minIncrement.replace("0.", "");

        if (increment.toPlainString().contains(maxIncrement))
            return parseBigDecimal(increment.toPlainString().replace(maxIncrement, minIncrement));

        return parseBigDecimal(increment.toPlainString().replace(".", ".0").replace(minIncrement, maxIncrement));
    }

    public static BigDecimal sqrt(BigDecimal num) {
        return sqrt(num, smolMc);
    }

    public static BigDecimal sqrt(BigDecimal num, MathContext mc) {
        return BigDecimalMath.pow(num, parseBigDecimal("0.5", mc), mc);
    }

    public static String fact(String num) throws NaNException {
        return fact(num, smolMc);
    }

    public static String fact(String num, MathContext mc) throws NaNException {
        BigDecimal i;

        if (num == null || num.equals("\0") || num.contains(".") || !Ax.isFullNum(num))
            throw new NaNException("NaN") ;

        BigDecimal number;

        try {
            number = parseBigDecimal(num, mc);
        }
        catch (Exception e) {
            throw new NaNException("NaN");
        }

        if (!BigDecimalMath.isIntValue(number))
            throw new NaNException("NaN");

        for (i = number; i.compareTo(BigDecimal.ONE) > 0; i = i.subtract(BigDecimal.ONE, mc)) {
            number = number.multiply(i.subtract(BigDecimal.ONE), mc);
        }

        return number.toPlainString();
    }

    public static BigDecimal modulus(BigDecimal n1, BigDecimal n2, MathContext mc) {
        if (n1.compareTo(n2) < 0)
            return n1;
        else if (n1.compareTo(n2) == 0)
            return BigDecimal.ZERO;
        else {
            BigDecimal temp = n1.divide(n2, mc);

            //TODO: Test negative and decimal values

            return n1.subtract(n2.multiply(parseBigDecimal(temp.toBigInteger().toString()), mc));
        }
    }

    public static BigDecimal newPow(BigDecimal base, BigDecimal power) {
        BigDecimal i;
        BigDecimal intPower;
        BigDecimal result = base;

        boolean powerCheck = true;

        try {
            intPower = BigDecimal.valueOf(Integer.parseInt(power.toPlainString()));
        }
        catch (Exception e) {
            powerCheck = false;
            intPower = BigDecimal.valueOf(((int) Double.parseDouble(power.toPlainString())));
        }

        //Power is an int
        if (powerCheck) {
            return base.pow(Integer.parseInt(intPower.toPlainString()), mc);
        }
        //Power is a double
        else {
            if (power.compareTo(BigDecimal.ONE) > 0) {
                for (i = BigDecimal.ONE; i.compareTo(intPower) < 0; i = i.add(BigDecimal.ONE, mc)) {
                    result = result.multiply(base, mc);
                }

                return result.multiply(BigDecimal.valueOf(Math.pow(Double.parseDouble(base.toPlainString()), Double.parseDouble(power.subtract(intPower, mc).toPlainString()))), mc);
            }
            else {
                return BigDecimal.valueOf(Math.pow(Double.parseDouble(base.toPlainString()), Double.parseDouble(power.toPlainString())));
            }
        }
    }

    public static BigDecimal parseBigDecimal(String str) {
        try {
            return BigDecimalMath.toBigDecimal(str, mc);
        }
        catch (Exception e) {
            return new BigDecimal(str, mc);
        }
    }

    public static BigDecimal parseBigDecimal(String str, MathContext mc) {
        try {
            return BigDecimalMath.toBigDecimal(str, mc);
        }
        catch (Exception e) {
            return new BigDecimal(str, mc);
        }
    }

    public static BigDecimal parseBigDecimal(String str, MathContext mc, int scale) {
        try {
            return BigDecimalMath.toBigDecimal(str, mc).setScale(scale, RoundingMode.HALF_UP);
        }
        catch (Exception e) {
            return new BigDecimal(str, mc).setScale(scale, RoundingMode.HALF_UP);
        }
    }
}

class Trig {
    public static MathContext mc = new MathContext(80, RoundingMode.HALF_UP);
    public static MathContext smolMc = new MathContext(40, RoundingMode.HALF_UP);

    public static BigDecimal toDegrees(BigDecimal num) {
        return num.divide(BigDecimalMath.pi(smolMc), smolMc).multiply(parseBigDecimal("180"), smolMc);
    }

    public static BigDecimal toRadians(BigDecimal num) {
        return num.multiply(BigDecimalMath.pi(smolMc), smolMc).divide(parseBigDecimal("180"), smolMc);
    }

    public static String evaluate(String trigOp, String n, MathContext mc, boolean isRad, int scale) throws NaNException {
        BigDecimal num = parseBigDecimal(n);

        String op = trigOp.replace("arc", "").replace(Ax.superMinus + Ax.superscripts[1], "");

        if (op.endsWith("h"))
            op = Ax.newTrim(op, 1);

        //Hyperbolic
        if (trigOp.contains("h")) {
            if (trigOp.contains("arc") || trigOp.contains(Ax.superMinus + Ax.superscripts[1])) {
                String cscIdentity = "ln((1/" + n + ")+(1+(1/(" + n + "^2)))^0.5)";
                String secIdentity = "ln((1/" + n + ")+((1-2)+(1/(" + n + "^2)))^0.5)";
                String cotIdentity = null;

                //TODO: Finish identities

                switch(op) {
                    case "sin": return BigDecimalMath.asinh(num, mc).toPlainString();
                    case "cos": return BigDecimalMath.acosh(num, mc).toPlainString();
                    case "tan": return BigDecimalMath.atanh(num, mc).toPlainString();

                    case "csc": return BetterMath.evaluate(cscIdentity, false, isRad, mc, scale, true).toPlainString();
                    case "sec": return BetterMath.evaluate(secIdentity, false, isRad, mc, scale, true).toPlainString();
                    case "cot": return BetterMath.evaluate(cotIdentity, false, isRad, mc, scale, false).toPlainString();
                }
            }
            else {
                switch(op) {
                    case "sin": return BigDecimalMath.sinh(num, mc).toPlainString();
                    case "cos": return BigDecimalMath.cosh(num, mc).toPlainString();
                    case "tan": return BigDecimalMath.tanh(num, mc).toPlainString();

                    case "csc": return BigDecimal.ONE.divide(BigDecimalMath.sinh(num, mc), mc).toPlainString();
                    case "sec": return BigDecimal.ONE.divide(BigDecimalMath.cosh(num, mc), mc).toPlainString();
                    case "cot": return BigDecimal.ONE.divide(BigDecimalMath.tanh(num, mc), mc).toPlainString();
                }
            }
        }
        //Normal
        else {
            if (trigOp.contains("arc") || trigOp.contains(Ax.superMinus + Ax.superscripts[1])) {
                switch(op) {
                    case "sin": return isRad ? BigDecimalMath.asin(num, mc).toPlainString() : toDegrees(BigDecimalMath.asin(num, mc)).toPlainString();
                    case "cos": return isRad ? BigDecimalMath.acos(num, mc).toPlainString() : toDegrees(BigDecimalMath.acos(num, mc)).toPlainString();
                    case "tan": return isRad ? BigDecimalMath.atan(num, mc).toPlainString() : toDegrees(BigDecimalMath.atan(num, mc)).toPlainString();

                    case "csc": return evaluate("sin⁻¹", BigDecimal.ONE.divide(num, mc).toPlainString(), mc, isRad, scale);
                    case "sec": return evaluate("cos⁻¹", BigDecimal.ONE.divide(num, mc).toPlainString(), mc, isRad, scale);
                    case "cot": return evaluate("tan⁻¹", BigDecimal.ONE.divide(num, mc).toPlainString(), mc, isRad, scale);
                }
            }
            else {
                switch(op) {
                    case "sin": return isRad ? BigDecimalMath.sin(num, mc).toPlainString() : BigDecimalMath.sin(toRadians(num), mc).toPlainString();
                    case "cos": return isRad ? BigDecimalMath.cos(num, mc).toPlainString() : BigDecimalMath.cos(toRadians(num), mc).toPlainString();
                    case "tan": return isRad ? BigDecimalMath.tan(num, mc).toPlainString() : BigDecimalMath.tan(toRadians(num), mc).toPlainString();

                    case "csc": return isRad ? BigDecimal.ONE.divide(BigDecimalMath.sin(num, mc), mc).toPlainString() : BigDecimal.ONE.divide(BigDecimalMath.sin(toRadians(num), mc), mc).toPlainString();
                    case "sec": return isRad ? BigDecimal.ONE.divide(BigDecimalMath.cos(num, mc), mc).toPlainString() : BigDecimal.ONE.divide(BigDecimalMath.cos(toRadians(num), mc), mc).toPlainString();
                    case "cot": return isRad ? BigDecimal.ONE.divide(BigDecimalMath.tan(num, mc), mc).toPlainString() : BigDecimal.ONE.divide(BigDecimalMath.tan(toRadians(num), mc), mc).toPlainString();
                }
            }
        }

        return num.toPlainString();
    }

    public static BigDecimal parseBigDecimal(String str) {
        return BigDecimalMath.toBigDecimal(str, mc);
    }

    public static BigDecimal parseBigDecimal(String str, MathContext mc) {
        return BigDecimalMath.toBigDecimal(str, mc);
    }
}



class Ax {
    public static final String eSub = "ₑ";
    public static final String opSub = "₍";
    public static final String cpSub = "₎";

    static final String[] superscripts = {"⁰", "¹", "²", "³", "⁴", "⁵", "⁶", "⁷", "⁸", "⁹"};
    static final String[] superLowerLetters = {"ᵃ", "ᵇ", "ᶜ", "ᵈ", "ᵉ", "ᶠ", "ᵍ", "ʰ", "ᶦ", "ʲ", "ᵏ", "ˡ", "ᵐ", "ⁿ", "ᵒ", "ᵖ", "ᑫ", "ʳ", "ˢ", "ᵗ", "ᵘ", "ᵛ", "ʷ", "ˣ", "ʸ", "ᶻ"};
    static final String[] superUpperLetters = {"ᴬ", "ᴮ", "ᶜ", "ᴰ", "ᴱ", "ᶠ", "ᴳ", "ᴴ", "ᴵ", "ᴶ", "ᴷ", "ᴸ", "ᴹ", "ᴺ", "ᴼ", "ᴾ", "Q", "ᴿ", "ˢ", "ᵀ", "ᵁ", "ⱽ", "ᵂ", "ˣ", "ʸ", "ᶻ"};
    static final String[] subscripts = {"₀", "₁", "₂", "₃", "₄", "₅", "₆", "₇", "₈", "₉"};

    public static final ArrayList<String> superlist = new ArrayList<>(Arrays.asList("⁰", "¹", "²", "³", "⁴", "⁵", "⁶", "⁷", "⁸", "⁹"));
    public static final ArrayList<String> sublist = new ArrayList<>(Arrays.asList("₀", "₁", "₂", "₃", "₄", "₅", "₆", "₇", "₈", "₉"));
    public static final ArrayList<String> superlistMisc = new ArrayList<>(Arrays.asList("ᵉ", "⁽", "⁾", "·"));
    public static final ArrayList<String> sublistMisc = new ArrayList<>(Arrays.asList(eSub, opSub, cpSub, ".", "₋"));
    public static final ArrayList<String> normalListMisc = new ArrayList<>(Arrays.asList("e", "(", ")", ".", "-"));

    static final String[] trigIn = {"sin", "cos", "tan", "csc", "sec", "cot", "sinh", "cosh", "tanh", "csch", "sech", "coth", "arcsin", "arccos", "arctan", "arccsc", "arcsec", "arccot", "arcsinh", "arccosh", "arctanh", "arccsch", "arcsech", "arccoth", "sin⁻¹", "cos⁻¹", "tan⁻¹", "csc⁻¹", "sec⁻¹", "cot⁻¹", "sinh⁻¹", "cosh⁻¹", "tanh⁻¹", "csch⁻¹", "sech⁻¹", "coth⁻¹"};
    static final ArrayList<String> trigList = new ArrayList<>(Arrays.asList("sin", "cos", "tan", "csc", "sec", "cot", "sinh", "cosh", "tanh", "csch", "sech", "coth", "arcsin", "arccos", "arctan", "arccsc", "arcsec", "arccot", "arcsinh", "arccosh", "arctanh", "arccsch", "arcsech", "arccoth", "sin⁻¹", "cos⁻¹", "tan⁻¹", "csc⁻¹", "sec⁻¹", "cot⁻¹", "sinh⁻¹", "cosh⁻¹", "tanh⁻¹", "csch⁻¹", "sech⁻¹", "coth⁻¹"));

    public static final String divi = "÷";
    public static final String multi = "×";
    public static final String pi = "π";
    public static final String sq = "√";
    public static final String bulletDot = "•";
    public static final String multiDot = "⋅";
    public static final String superDot = "‧";
    public static final String emDash = "—";
    public static final String superMinus = "⁻";

    public static final String piStr = superscripts[3] + superDot + superscripts[1] + superscripts[4] + superscripts[1] + superscripts[5] + superscripts[9];

    public static ArrayList<String> ops = new ArrayList<>(Arrays.asList("+", "-", multi, divi, sq, "^", "(", ")", "!", "%", bulletDot, multiDot, "*", "/", "log", "ln"));
    public static ArrayList<String> binaryOps = new ArrayList<String>(Arrays.asList("+", "-", "/", "*", multi, divi, "^", "%", Ax.emDash, bulletDot, multiDot));

    //The method's name is a shortened version of "charAt." It's literally just a shortcut for writing "Character.toString(str.charAt(index))"
    public static String chat(String str, int index) {
        if (str == null || str.equals("\0"))
            return null;
        else if (index >= str.length() || index < 0)
            return null;

        try {
            str = Character.toString(str.charAt(index));
        }
        catch (Exception e){
            return null;
        }

        return str;
    }

    //A fancier version of the original chat function that gets a specified number of characters at the specified index, rather than just one
    public static String chat(int numChars, String str, int index) {
        if (numChars == 0)
            return null;
        if (numChars == 1)
            return chat(str, index);

        if (isNull(str))
            return null;
        if (!isNull(str) && (index >= str.length() || index < 0))
            return null;

        if (numChars > str.length() - index) {
            try {
                return str.substring(index);
            }
            catch (IndexOutOfBoundsException e){
                return null;
            }
        }
        else {
            try {
                return str.substring(index, index + numChars);
            }
            catch (IndexOutOfBoundsException e) {
                return null;
            }
        }
    }

    public static boolean isOp(String str) {
        if (str == null || str.equals("\0"))
            return false;

        if (ops.contains(str))
            return true;

        return false;
    }

    public static boolean isBinaryOp(String str) {
        if (isNull(str))
            return false;

        return binaryOps.contains(str);
    }

    public static int charDiff(String str, String s1, String s2) {
        if (str == null || s1 == null || s2 == null || str.length() < 2 || s1.length() != 1 || s2.length() != 1)
            return 0;

        int count1 = 0, count2 = 0;

        for (int i=0; i < str.length(); i++) {
            char current = str.charAt(i);

            if (current == s1.charAt(0))
                count1++;
            else if (current == s2.charAt(0))
                count2++;
        }

        return count1 - count2;
    }

    public static boolean isDigit(char character) {
        return (int) character >= 48 && (int) character <= 57;
    }

    public static boolean isDigit(String str) {
        return !isNull(str) && str.length() == 1 && (int) str.charAt(0) >= 48 && (int) str.charAt(0) <= 57;
    }

    public static boolean isNum(String str) {
        if (isNull(str))
            return false;

        if (!isDigit(str)){
            if (str.equals("e") || str.equals("π")) {
                return true;
            }
        }

        return isDigit(str);
    }

    public static boolean isFullNum(String str) {
        int i, length;
        if (isNull(str))
            return false;

        length = str.length();

        if (length == 0)
            return false;

        if (length == 1) {
            if (isDigit(str))
                return true;
            if (str.equals("."))
                return false;
        }

        for (i=0; i < length; i++) {
            String current = chat(str, i);

            if (current == null || (!isDigit(current) && !(current.equals("e") || current.equals("π") || current.equals("."))))
                return false;
        }

        return countChars(str, ".") <= 1;
    }

    public static boolean isFullSignedNum(String str) {
        return str != null && !str.equals("\0") && !str.equals("") && (isFullNum(str.replace(",", "")) || (str.startsWith("-") && isFullNum(str.substring(1).replace(",", ""))));
    }

    public static boolean isFullSignedNumE(String str) {
        return str != null && !str.equals("\0") && !str.equals("") && (isFullSignedNum(str) || isFullSignedNum(str.replace("E-", "").replace("E", "")) || (str.startsWith("-") && isFullSignedNum(str.replace("E-", "").replace("E", ""))));
    }

    public static int countChars(String str, String input){
        if (isNull(str) || isNull(input) || input.length() < 1)
            return 0;

        int i;
        int numChars = 0;
        int length = str.length();
        int inputLength = input.length();

        try {
            for (i = 0; i < length; i += inputLength) {
                if (chat(inputLength, str, i).equals(input))
                    numChars++;
            }
        }
        catch (Exception ignored) {}

        return numChars;
    }

    public static String newTrim(String str, int numChars) {
        int s;

        if (isNull(str))
            return "";

        for (s=0; s < numChars; s++) {
            if (!str.equals("\0") && str.length() == 1)
                return "";

            if (!str.equals("\0") && str.length() > 1)
                str = str.substring(0, str.length() - 1);
        }

        return str;
    }

    public static String lastChar(String str) {
        return (!isNull(str) && str.length() > 1) ? str.substring(str.length() - 1) : str;
    }

    public static boolean isSuperscript(String str) {
        if (!isNull(str) && str.length() == 1) {
            if (superlist.contains(str))
                return true;
            if (str.equals("⋅") || str.equals(superDot))
                return true;
            if (str.equals("⁻"))
                return true;
            if (str.equals("ᵉ"))
                return true;
        }

        return false;
    }



    public static boolean isSubscript(String str) {
        if (str == null || str.equals("\0"))
            return false;

        if (str.length() == 1)
            return sublist.contains(str) || sublistMisc.contains(str);

        return false;
    }

    public static boolean isSubDigit(String str) {
        if (str == null || str.equals("\0"))
            return false;

        if (str.length() == 1)
            return sublist.contains(str);

        return false;
    }

    public static boolean isFullSubNum(String str) {
        int i;

        if (str == null || str.equals("\0"))
            return false;

        for (i=0; i < str.length(); i++) {
            if (!isSubscript(chat(str, i)))
                return false;
        }

        return true;
    }

    public static String subToNum(String str) {
        int i;
        String output = "";

        if (str == null || str.equals("\0"))
            return null;

        for (i=0; i < str.length(); i++) {
            if (isSubDigit(chat(str, i)))
                output += Integer.toString(sublist.indexOf(chat(str, i)));
            else if (sublistMisc.contains(str))
                output += normalListMisc.get(sublistMisc.indexOf(str));
            else
                return null;
        }

        return output;
    }

    public static String numToSuper(String str) {
        int i;
        String output = "";

        if (str == null || str.equals("\0"))
            return null;

        for (i=0; i < str.length(); i++) {
            if (isDigit(chat(str, i)))
                output += superlist.get(Integer.parseInt(chat(str, i)));
            else if (normalListMisc.contains(str))
                output += superlistMisc.get(normalListMisc.indexOf(str));
            else
                return null;
        }

        return output;
    }

    public static String superToNum(String str) {
        int i;
        String output = "";

        if (str == null || str.equals("\0"))
            return null;

        for (i=0; i < str.length(); i++) {
            if (isSuperscript(chat(str, i)))
                output += Integer.toString(superlist.indexOf(chat(str, i)));
            else if (superlistMisc.contains(str))
                output += normalListMisc.get(superlistMisc.indexOf(str));
            else
                return null;
        }

        return output;
    }

    public static String getLast(String str, int numChars){
        int s;
        String result = lastChar(str);

        if (str == null || str.equals("\0"))
            return null;
        if (numChars >= str.length())
            return str;

        str = newTrim(str, 1);

        for (s=0; s < numChars - 1; s++) {
            if (!isNull(str)) {
                if (str.length() == 1)
                    return lastChar(str) + result;

                if (str.length() > 1)
                    result = lastChar(str) + result;

                if (str.length() > 0)
                    str = str.substring(0, str.length() - 1);
            }
        }

        return result;
    }

    public static boolean isNull(Object input){
        if (input == null)
            return true;

        if (input.getClass() == String.class) {
            String str = (String) input;

            return str.equals("\0") || str.equals("");
        }

        return false;
    }
}

class NaNException extends Exception {
    public NaNException(String errorMessage) {
        super(errorMessage);
    }
}