package com.mirambeau.termcalc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class FunctionsBottomSheet extends BottomSheetDialogFragment {
    final Activity main = MainActivity.mainActivity;

    RecyclerView recyclerView;
    FunctionsAdapter adapter;
    ArrayList<FunctionCard> cards = new ArrayList<>();
    ArrayList<VariablesAdapter> adapters = new ArrayList<>();

    ArrayList<String> functionTitles, functionTexts, functionVariables;

    static final String[] initFunctionTitles = {"Compound Interest", "Simple Interest", "Pythagorean Theorem"};
    static final String[] initFunctionTexts = {"P(1 + (r/n))^nt", "P(1 + rt)", Aux.sq + "(a^2 + b^2)"};
    static final String[] initFunctionVariables = {"P`r`n`t", "P`r`t", "a`b"};

    Button[] nums, compBar, trigBar, mainOps;
    Button bDec, bParenthesisOpen, bParenthesisClose, bEquals, bMod;
    Button[][] allButtons;

    BottomSheetBehavior<FrameLayout> behavior;

    public static FunctionsBottomSheet newInstance() {
        return new FunctionsBottomSheet();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.functions_bottom_sheet, container, false);

        view.findViewById(R.id.addFunction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.findViewById(R.id.addCustom).performClick();
            }
        });

        int theme = Aux.getThemeInt();

        view.findViewById(R.id.functionsMainBG).setBackground(Aux.getSheetBackground(theme));

        if (theme == 2) {
            int darkGray = Color.parseColor("#3C4043");

            ((TextView) view.findViewById(R.id.functionsTitle)).setTextColor(darkGray);
            ((ImageButton) view.findViewById(R.id.addFunction)).setColorFilter(darkGray);
            ((ImageButton) view.findViewById(R.id.pill)).setImageResource(R.drawable.sheet_pill_light);
        }
        else if (theme != 1){
            ((ImageButton) view.findViewById(R.id.pill)).setImageResource(R.drawable.sheet_pill_black);
        }

        if (main != null) {
            nums = new Button[]{main.findViewById(R.id.b0), main.findViewById(R.id.b1), main.findViewById(R.id.b2), main.findViewById(R.id.b3), main.findViewById(R.id.b4), main.findViewById(R.id.b5), main.findViewById(R.id.b6), main.findViewById(R.id.b7), main.findViewById(R.id.b8), main.findViewById(R.id.b9)};
            compBar = new Button[]{main.findViewById(R.id.bSqrt), main.findViewById(R.id.bExp), main.findViewById(R.id.bFact), main.findViewById(R.id.bPi), main.findViewById(R.id.bE), main.findViewById(R.id.bLog), main.findViewById(R.id.bLn)};
            trigBar = new Button[]{main.findViewById(R.id.bSin), main.findViewById(R.id.bCos), main.findViewById(R.id.bTan), main.findViewById(R.id.bCsc), main.findViewById(R.id.bSec), main.findViewById(R.id.bCot), main.findViewById(R.id.bInv)};
            mainOps = new Button[]{main.findViewById(R.id.sPlus), main.findViewById(R.id.sMinus), main.findViewById(R.id.sMulti), main.findViewById(R.id.sDiv)};

            bDec = main.findViewById(R.id.bDec);
            bParenthesisOpen = main.findViewById(R.id.bParenthesisOpen);
            bParenthesisClose = main.findViewById(R.id.bParenthesisClose);
            bEquals = main.findViewById(R.id.bEquals);
            bMod = main.findViewById(R.id.bMod);

            allButtons = new Button[][]{nums, compBar, trigBar, mainOps, {bDec, bParenthesisOpen, bParenthesisClose, bEquals, bMod}};
        }

        getFunctions();

        adapter = Aux.adapter;

        recyclerView = view.findViewById(R.id.functionsRv);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.mainActivity));
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        if (getDialog() != null) {
            getDialog().setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dia) {
                    BottomSheetDialog dialog = (BottomSheetDialog) dia;
                    FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);

                    if (bottomSheet != null) {
                        behavior = BottomSheetBehavior.from(bottomSheet);
                        behavior.setHideable(true);
                        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                }
            });
        }

        super.onViewCreated(view, savedInstanceState);
    }

    public void getFunctions() {
        int i;

        try {
            TinyDB tinydb = Aux.tinydb();

            functionTitles = tinydb.getListString("functionTitles");
            functionTexts = tinydb.getListString("functionTexts");
            functionVariables = tinydb.getListString("functionVariables");

            if ((functionTitles.size() < 1 && !tinydb.getBoolean("hasDeletedFunction"))) {
                for (i = 0; i < initFunctionTitles.length; i++) {
                    functionTitles.add(initFunctionTitles[i]);
                    functionTexts.add(initFunctionTexts[i]);
                    functionVariables.add(initFunctionVariables[i]);
                }

                tinydb.putListString("functionTitles", functionTitles);
                tinydb.putListString("functionTexts", functionTexts);
                tinydb.putListString("functionVariables", functionVariables);
            }

            for (i = 0; i < functionTitles.size(); i++) {
                cards.add(new FunctionCard(functionTitles.get(i), functionTexts.get(i), functionVariables.get(i)));

                adapters.add(new VariablesAdapter(functionVariables.get(i).split("`"), i));
            }
        }
        catch (Exception e){
            e.printStackTrace();
            Aux.saveStack(e);
            main.finishAffinity();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        int i, j;

        for (i=0; i < adapters.size(); i++) {
            boolean shouldNotify = false;

            for (j=0; j < adapters.get(i).values.length; j++) {
                if (adapters.get(i).values[j] != null && adapters.get(i).values[j].length() > 0) {
                    shouldNotify = true;

                    adapters.get(i).values[j] = "";
                }
            }

            if (shouldNotify)
                adapter.notifyItemChanged(i);
        }

        hideKeyboard();
    }

    //Show the keyboard from a dialog
    public static void showKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) MainActivity.mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    //Hide the keyboard from a dialog
    public void hideKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) MainActivity.mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }
}