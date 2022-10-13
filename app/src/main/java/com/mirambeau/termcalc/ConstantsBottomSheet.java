package com.mirambeau.termcalc;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ConstantsBottomSheet extends BottomSheetDialogFragment {
    final Activity main = MainActivity.mainActivity;

    RecyclerView recyclerView;
    ConstantsAdapter adapter;
    ArrayList<ConstantCard> cards = new ArrayList<>();

    ArrayList<String> constantTitles, constantNums, constantUnits;

    static final String[] initConstantTitles = {"Avogadro's Number", "Atomic Mass Unit", "Planck's Constant", "Electron Charge", "Gas Constant", "Faraday Constant", "Acceleration of Gravity"};
    static final String[] initConstantNums = {"6.0221409×10²³", "1.67377×10⁻²⁷", "6.6260690×10⁻³⁴", "1.6021766×10⁻¹⁹", "8.3145", "96,485.337", "9.8"};
    static final String[] initConstantUnits = {"mol⁻¹", "kg", "J·s", "C", "J·K⁻¹·mol⁻¹", "C/mol", "m/s²"};

    public static ConstantsBottomSheet newInstance() {
        return new ConstantsBottomSheet();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.constants_bottom_sheet, container, false);

        final TinyDB tinydb = new TinyDB(MainActivity.mainActivity);

        view.findViewById(R.id.addConstant).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.findViewById(R.id.addCustom).performClick();
            }
        });

        final int theme = Aux.getThemeInt();
        final int darkGray = Color.parseColor("#3C4043");

        view.findViewById(R.id.constantsMainBG).setBackground(Aux.getSheetBackground(theme));

        if (theme == 2) {
            ((TextView) view.findViewById(R.id.constantsTitle)).setTextColor(darkGray);
            ((ImageButton) view.findViewById(R.id.addConstant)).setColorFilter(darkGray);
            ((ImageButton) view.findViewById(R.id.pill)).setImageResource(R.drawable.sheet_pill_light);
        }
        else if (theme != 1){
            ((ImageButton) view.findViewById(R.id.pill)).setImageResource(R.drawable.sheet_pill_black);
        }

        getConstants();
        adapter = new ConstantsAdapter(cards, true);
        adapter.setOnItemClickListener(new ConstantsAdapter.OnItemClickListener() {
            @Override
            public void onOverflowClick(final int position, View anchor) {
                final androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(MainActivity.mainActivity, anchor);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.constant_overflow, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int i;

                        //Edit
                        if (item.getOrder() == 0){
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.mainActivity, AlertDialog.THEME_DEVICE_DEFAULT_DARK);

                            builder.setTitle("Edit Constant");
                            View viewInflated = LayoutInflater.from(MainActivity.mainActivity).inflate(R.layout.new_constant, (ViewGroup) view.findViewById(R.id.main), false);

                            final EditText titleInput = viewInflated.findViewById(R.id.constantTitleInput);
                            final EditText numInput = viewInflated.findViewById(R.id.constantNumInput);
                            final EditText unitInput = viewInflated.findViewById(R.id.constantUnitInput);

                            titleInput.setText(constantTitles.get(position));
                            numInput.setText(constantNums.get(position));
                            unitInput.setText(constantUnits.get(position));

                            builder.setView(viewInflated);

                            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    int i;
                                    boolean inputExists = false;

                                    for (i=0; i < constantNums.size(); i++) {
                                        if (constantNums.get(i).equals(numInput.getText().toString())) {
                                            inputExists = true;
                                            break;
                                        }
                                    }

                                    if (!inputExists && numInput.getText() != null && (Aux.isFullNum(numInput.getText().toString()) || Aux.containsBinaryOperator(numInput.getText().toString())) && titleInput.getText() != null) {
                                        dialog.dismiss();

                                        for (i=0; i < 9; i++) {
                                            if (tinydb.getString("shortcut" + (i+1)).equals(constantNums.get(position))) {
                                                tinydb.putString("shortcut" + (i+1), numInput.getText().toString());
                                            }
                                        }

                                        constantTitles.set(position, titleInput.getText().toString());
                                        constantNums.set(position, numInput.getText().toString());
                                        constantUnits.set(position, unitInput.getText().toString());

                                        cards.set(position, new ConstantCard(constantTitles.get(position), constantNums.get(position), constantUnits.get(position)));

                                        tinydb.putListString("constantTitles", constantTitles);
                                        tinydb.putListString("constantNums", constantNums);
                                        tinydb.putListString("constantUnits", constantUnits);

                                        adapter.cards = cards;
                                        adapter.notifyItemChanged(position);
                                    }
                                    else if (inputExists) {
                                        Aux.makeLongToast("Error: Constant value has already been saved.");
                                    }
                                    else if (titleInput.getText() == null) {
                                        Toast.makeText(MainActivity.mainActivity, "Error: Constant title cannot be blank.", Toast.LENGTH_LONG).show();
                                    }
                                    else {
                                        Toast.makeText(MainActivity.mainActivity, "Error: Constant value must be a number.", Toast.LENGTH_LONG).show();
                                    }
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
                        //Assign Shortcut
                        else if (item.getOrder() == 1){
                            AlertDialog.Builder builder;

                            if (theme == 2)
                                builder = new AlertDialog.Builder(MainActivity.mainActivity, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                            else
                                builder = new AlertDialog.Builder(MainActivity.mainActivity, AlertDialog.THEME_DEVICE_DEFAULT_DARK);

                            builder.setTitle("Assign Shortcut");

                            View viewInflated = LayoutInflater.from(MainActivity.mainActivity).inflate(R.layout.assign_shortcut, (ViewGroup) view.findViewById(R.id.main), false);

                            builder.setView(viewInflated);

                            final Button[] nums = {viewInflated.findViewById(R.id.assign1), viewInflated.findViewById(R.id.assign2), viewInflated.findViewById(R.id.assign3),
                                    viewInflated.findViewById(R.id.assign4), viewInflated.findViewById(R.id.assign5), viewInflated.findViewById(R.id.assign6), viewInflated.findViewById(R.id.assign7),
                                    viewInflated.findViewById(R.id.assign8), viewInflated.findViewById(R.id.assign9)};

                            TextView instructions = viewInflated.findViewById(R.id.assignInstructions);

                            if (theme == 2)
                                instructions.setTextColor(darkGray);
                            else
                                instructions.setTextColor(Color.WHITE);

                            instructions.setText(instructions.getText().toString() + "\"" + constantTitles.get(position) + ".\"");

                            final FinalString initSelected = new FinalString();
                            final FinalString selected = new FinalString();
                            final int addAmt = theme == 2 ? -20 : 20;

                            for (i=0; i < 9; i++) {
                                String bgColor;

                                //Background colors
                                if (Aux.isTinyColor("-b" + (i + 1)))
                                    bgColor = tinydb.getString("-b" + (i+1));
                                else if (Aux.isTinyColor("cKeypad"))
                                    bgColor = tinydb.getString("cKeypad");
                                else {
                                    if (theme == 2) {
                                        bgColor = "#FFFFFF";
                                    }
                                    else if (theme == 5) {
                                        bgColor = Aux.hexAdd(tinydb.getString("accentSecondary"), -6);
                                    }
                                    else if (theme == 1) {
                                        bgColor = "#202227";
                                    }
                                    else {
                                        bgColor = "#000000";
                                    }
                                }

                                nums[i].setBackgroundColor(Color.parseColor(bgColor));

                                if (tinydb.getString("shortcut" + (i+1)).equals(constantNums.get(position))) {
                                    nums[i].setBackgroundColor(Color.parseColor(Aux.hexAdd(bgColor, addAmt)));
                                    initSelected.add("" + (i+1));
                                    selected.add("" + (i+1));
                                }

                                //Text Colors
                                if (Aux.isTinyColor("-b" + (i + 1) + "t"))
                                    nums[i].setTextColor(Aux.getTinyColor("-b" + (i+1) + "t"));
                                else if (Aux.isTinyColor("cNum"))
                                    nums[i].setTextColor(Aux.getTinyColor("cNum"));
                                else {
                                    if (theme == 2) {
                                        nums[i].setTextColor(darkGray);
                                    }
                                    else if (theme == 5) {
                                        nums[i].setTextColor(Color.parseColor("#303030"));
                                    }
                                    else {
                                        nums[i].setTextColor(Color.WHITE);
                                    }
                                }

                                //OnClick Methods
                                final int fi = i;
                                final String finalBgColor = bgColor;
                                nums[i].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (!selected.contains("" + (fi+1))) {
                                            selected.add("" + (fi+1));
                                            nums[fi].setBackgroundColor(Color.parseColor(Aux.hexAdd(finalBgColor, addAmt)));
                                        }
                                        else {
                                            selected.remove("" + (fi+1));
                                            nums[fi].setBackgroundColor(Color.parseColor(finalBgColor));
                                        }
                                    }
                                });
                            }

                            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();

                                    int i;

                                    for (i = 0; i < selected.toString().length(); i++) {
                                        tinydb.putString("shortcut" + Aux.chat(selected.toString(), i), constantNums.get(position));

                                        if (initSelected.contains(Aux.chat(selected.toString(), i)))
                                            initSelected.remove(Aux.chat(selected.toString(), i));
                                    }

                                    for (i = 0; i < initSelected.length(); i++) {
                                        tinydb.putString("shortcut" + Aux.chat(initSelected.toString(), i), "");
                                    }


                                    if (tinydb.getInt("shortcutsAssigned") < 3)
                                        Aux.makeLongToast("Shortcuts updated. Long-press your selected number(s) to insert the selected constant");
                                    else
                                        Aux.makeToast("Shortcuts updated", 0);

                                    tinydb.putInt("shortcutsAssigned", tinydb.getInt("shortcutsAssigned") + 1);
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
                        //Delete
                        else {
                            AlertDialog.Builder builder;

                            if (theme == 2)
                                builder = new AlertDialog.Builder(MainActivity.mainActivity, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                            else
                                builder = new AlertDialog.Builder(MainActivity.mainActivity, AlertDialog.THEME_DEVICE_DEFAULT_DARK);

                            boolean shouldCloseDialog = false;

                            try {
                                builder.setTitle("Are you sure you want to delete \"" + constantTitles.get(position) + "\"?\n");
                            }
                            catch (Exception e){
                                Aux.makeLongToast("Error: Something went wrong. Please try again.");
                                shouldCloseDialog = true;
                                dismiss();
                            }

                            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();

                                    int i;

                                    try {
                                        tinydb.putBoolean("hasDeletedConstant", true);

                                        for (i=0; i < 9; i++) {
                                            if (tinydb.getString("shortcut" + (i+1)).equals(constantNums.get(position))) {
                                                tinydb.putString("shortcut" + (i+1), "");
                                            }
                                        }

                                        constantTitles.remove(position);
                                        constantNums.remove(position);
                                        constantUnits.remove(position);
                                        cards.remove(position);

                                        tinydb.putListString("constantTitles", constantTitles);
                                        tinydb.putListString("constantNums", constantNums);
                                        tinydb.putListString("constantUnits", constantUnits);

                                        adapter.cards = cards;
                                        adapter.notifyItemRemoved(position);
                                    } catch (NullPointerException | IndexOutOfBoundsException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                            if (!shouldCloseDialog)
                                builder.show();
                        }
                        return false;
                    }
                });

                popup.show();
            }

            @Override
            public void onCopyClick(int position) {
                String label = cards.get(position).title;
                String text = cards.get(position).constant + " " + cards.get(position).unit;

                ClipboardManager clipboard = (ClipboardManager) MainActivity.mainActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(label, text);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(MainActivity.mainActivity, "Copied " + label + " (" + text + ") to clipboard.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPasteClick(int position) {
                insertConstant(cards.get(position).constant);
                dismiss();
            }

        });

        recyclerView = view.findViewById(R.id.constantsRv);
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

                    if (bottomSheet != null)
                        BottomSheetBehavior.from(bottomSheet).setHideable(true);
                }
            });
        }

        super.onViewCreated(view, savedInstanceState);
    }

    public void getConstants() {
        int i;

        try {
            TinyDB tinydb = Aux.tinydb();

            constantTitles = tinydb.getListString("constantTitles");
            constantNums = tinydb.getListString("constantNums");
            constantUnits = tinydb.getListString("constantUnits");

            if ((constantTitles.size() < 1 && !tinydb.getBoolean("hasDeletedConstant"))) {
                for (i = 0; i < initConstantTitles.length; i++) {
                    constantTitles.add(initConstantTitles[i]);
                    constantNums.add(initConstantNums[i]);
                    constantUnits.add(initConstantUnits[i]);
                }

                tinydb.putListString("constantTitles", constantTitles);
                tinydb.putListString("constantNums", constantNums);
                tinydb.putListString("constantUnits", constantUnits);
            }

            for (i = 0; i < constantTitles.size(); i++) {
                cards.add(new ConstantCard(constantTitles.get(i), constantNums.get(i), constantUnits.get(i)));
            }
        }
        catch (Exception e){
            e.printStackTrace();
            Aux.saveStack(e);
            main.finishAffinity();
        }
    }

    @SuppressLint("SetTextI18n")
    public void insertConstant(String constant) {
        try {
            if (constant == null || constant.length() < 1)
                return;

            EditText tv = MainActivity.mainActivity.findViewById(R.id.equation);

            int cursor = tv.getSelectionStart();
            int cursorOffset = constant.length();
            String tvText = tv.getText().toString();

            try {
                if (cursor == tvText.length())
                    tv.setText(tvText + constant);
                else if ((tvText.equals(" ") || tvText.equals("")) && cursor <= 1)
                    tv.setText(constant);
                else if (cursor > 0 && cursor < tv.getText().toString().length())
                    tv.setText(tvText.substring(0, cursor) + constant + tvText.substring(cursor));
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
        catch (Exception e){
            e.printStackTrace();
            Aux.saveStack(e);
            main.finishAffinity();
        }
    }
}