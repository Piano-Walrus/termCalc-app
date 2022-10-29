package com.mirambeau.termcalc;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class HistoryBottomSheet extends BottomSheetDialogFragment {
    public static ArrayList<String> equations = new ArrayList<>();
    public static ArrayList<String> answers = new ArrayList<>();
    public static ArrayList<Integer> days = new ArrayList<>(), months = new ArrayList<>(), years = new ArrayList<>();

    ArrayList<HistoryAdapter> adapters = new ArrayList<>();
    HistoryDateGroupAdapter adapter;
    RecyclerView recyclerView;
    ArrayList<ArrayList<HistoryEntry>> entries = new ArrayList<>();

    ArrayList<String> titleList = new ArrayList<>();
    String[] titles;

    Activity main = MainActivity.mainActivity;

    Button[] nums, compBar, trigBar, mainOps;
    Button bDec, bParenthesisOpen, bParenthesisClose, bEquals, bMod;
    Button[][] allButtons;

    final String[] superscripts = {"⁰", "¹", "²", "³", "⁴", "⁵", "⁶", "⁷", "⁸", "⁹"};

    int i;
    String copied = "\0";

    public static HistoryBottomSheet newInstance() {
        return new HistoryBottomSheet();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.history_bottom_sheet, container, false);

        int theme = Ax.getThemeInt();
        final ImageButton overflow = view.findViewById(R.id.historyOverflow);
        final TextView tvEmpty = view.findViewById(R.id.historyEmpty);

        titles = new String[]{getString(R.string.today), getString(R.string.yesterday), getString(R.string.last_week), getString(R.string.older)};

        view.findViewById(R.id.historyMainBG).setBackground(Ax.getSheetBackground(theme));

        if (theme == 2) {
            int darkGray = Color.parseColor("#3C4043");

            overflow.setColorFilter(darkGray);
            tvEmpty.setTextColor(darkGray);

            ((TextView) view.findViewById(R.id.historyTitle)).setTextColor(darkGray);
            ((ImageButton) view.findViewById(R.id.pill)).setImageResource(R.drawable.sheet_pill_light);
        }
        else if (theme != 1){
            ((ImageButton) view.findViewById(R.id.pill)).setImageResource(R.drawable.sheet_pill_black);
        }

        overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(MainActivity.mainActivity, overflow);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.history, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (tvEmpty.getVisibility() == View.VISIBLE)
                            Ax.makeToast("History is already empty", 0);
                        else {
                            final TinyDB tinydb = Ax.tinydb();

                            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.mainActivity, AlertDialog.THEME_DEVICE_DEFAULT_DARK);

                            builder.setTitle("Clear all history entries?");

                            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();

                                    tinydb.putListString("equations", new ArrayList<String>());
                                    tinydb.putListString("answers", new ArrayList<String>());
                                    tinydb.putListInt("dayEntries", new ArrayList<Integer>());
                                    tinydb.putListInt("monthEntries", new ArrayList<Integer>());
                                    tinydb.putListInt("yearEntries", new ArrayList<Integer>());

                                    dismiss();
                                }
                            });
                            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                            builder.show();
                        }

                        return false;
                    }
                });

                popup.show();
            }
        });

        nums = new Button[]{findViewById(R.id.b0), findViewById(R.id.b1), findViewById(R.id.b2), findViewById(R.id.b3), findViewById(R.id.b4), findViewById(R.id.b5), findViewById(R.id.b6), findViewById(R.id.b7), findViewById(R.id.b8), findViewById(R.id.b9)};
        compBar = new Button[]{findViewById(R.id.bSqrt), findViewById(R.id.bExp), findViewById(R.id.bFact), findViewById(R.id.bPi), findViewById(R.id.bE), findViewById(R.id.bLog), findViewById(R.id.bLn), findViewById(R.id.bMod)};
        trigBar = new Button[]{findViewById(R.id.bSin), findViewById(R.id.bCos), findViewById(R.id.bTan), findViewById(R.id.bCsc), findViewById(R.id.bSec), findViewById(R.id.bCot), findViewById(R.id.bInv)};
        mainOps = new Button[]{findViewById(R.id.sPlus), findViewById(R.id.sMinus), findViewById(R.id.sMulti), findViewById(R.id.sDiv)};

        bDec = findViewById(R.id.bDec);
        bParenthesisOpen = findViewById(R.id.bParenthesisOpen);
        bParenthesisClose = findViewById(R.id.bParenthesisClose);
        bEquals = findViewById(R.id.bEquals);
        bMod = compBar[compBar.length - 1];

        allButtons = new Button[][]{nums, compBar, trigBar, mainOps, {bDec, bParenthesisOpen, bParenthesisClose, bEquals, bMod}};

        if (getEntries())
            view.findViewById(R.id.historyEmpty).setVisibility(View.GONE);

        Log.d("size", "" + equations.size());

        for (i=0; i < titles.length; i++) {
            titleList.add(titles[i]);
            adapters.add(new HistoryAdapter(entries.get(i)));
        }

        for (i=0; i < titles.length; i++) {
            final HistoryAdapter current = adapters.get(i);

            current.setOnItemClickListener(new HistoryAdapter.OnItemClickListener() {
                @Override
                public void onAnswerClick(int position) {
                    insert(current.entries.get(position).answer);
                    dismiss();
                }

                @Override
                public void onEquationClick(int position) {
                    insert(current.entries.get(position).equation);
                    dismiss();
                }
            });
        }

        adapter = new HistoryDateGroupAdapter(titleList, adapters);

        recyclerView = view.findViewById(R.id.mainHistoryRv);
        recyclerView.setHasFixedSize(true);
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
                        BottomSheetBehavior.from(bottomSheet).setHideable(true);
                        BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                        BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                }
            });
        }

        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * @return True if at least one history entry was added to an adapter. False otherwise.
     */
    public boolean getEntries() {
        int i;

        try {
            TinyDB tinydb = new TinyDB(MainActivity.mainActivity);

            equations = tinydb.getListString("equations");
            answers = tinydb.getListString("answers");
            days = tinydb.getListInt("dayEntries");
            months = tinydb.getListInt("monthEntries");
            years = tinydb.getListInt("yearEntries");

            // Initialize entries ArrayList
            for (i=0; i < titles.length; i++){
                entries.add(new ArrayList<HistoryEntry>());
            }

            Calendar cal = Calendar.getInstance();
            final int[] today = new int[]{cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.YEAR)};

            for (i=0; i < days.size(); i++){
                int[] current = new int[]{months.get(i), days.get(i), years.get(i)};
                int[] interval = DateCalc.compare(current, today);
                final int[] error = new int[]{0, 0, 0, -1};

                HistoryEntry entry = new HistoryEntry(equations.get(i), answers.get(i));

                if (i == 0 || (!equations.get(i).equals(equations.get(i-1)) && !answers.get(i).equals(answers.get(i-1)))) {
                    if (interval.length > 3 && Arrays.equals(interval, error))
                        continue;

                    if (interval[0] == 0 && interval[2] == 0) {
                        if (interval[1] < 7) {
                            if (interval[1] == 1)
                                entries.get(1).add(entry);
                            else if (interval[1] == 0)
                                entries.get(0).add(entry);
                            else
                                entries.get(2).add(entry);
                        }
                        else
                            entries.get(3).add(entry);
                    }
                    else {
                        entries.get(3).add(entry);
                    }
                }
            }

            return i > 0;
        }
        catch (Exception e){
            e.printStackTrace();
            Ax.saveStack(e);
            main.finishAffinity();
        }

        return false;
    }

    @SuppressLint("SetTextI18n")
    public void insert(String str) {
        TinyDB tinydb = new TinyDB(MainActivity.mainActivity);

        copied = str;
        tinydb.putString("copied", copied);

        Toast toast = Toast.makeText(MainActivity.mainActivity, "\"" + copied + "\" inserted", Toast.LENGTH_SHORT);
        toast.show();

        if (str == null || str.length() < 1)
            return;

        EditText tv = main.findViewById(R.id.equation);

        int cursor = tv.getSelectionStart();
        int cursorOffset = str.length();
        String tvText = tv.getText().toString();

        try {
            if (cursor == tvText.length())
                tv.setText(tvText + str);
            else if ((tvText.equals(" ") || tvText.equals("")) && cursor <= 1)
                tv.setText(str);
            else if (cursor > 0 && cursor < tv.getText().toString().length())
                tv.setText(tvText.substring(0, cursor) + str + tvText.substring(cursor));
            else
                cursorOffset = 0;
        }
        catch (Exception e) {
            e.printStackTrace();

            tv.setText(tvText);
            cursorOffset = 0;
        }

        try {
            tv.requestFocus();
            tv.setSelection(cursor + cursorOffset);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            ((ViewGroup) main.findViewById(R.id.equationLayout)).getLayoutTransition().disableTransitionType(LayoutTransition.CHANGING);
            ((ViewGroup) main.findViewById(R.id.equationScrollView)).getLayoutTransition().disableTransitionType(LayoutTransition.CHANGING);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        dismiss();
    }

    public Button findViewById(int id){
        return main.findViewById(id);
    }
}