package com.mirambeau.termcalc;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.fragment.app.Fragment;

import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;
import com.github.appintro.AppIntroPageTransformerType;

import java.util.ArrayList;

public class Intro<isColorTransitionsEnabled> extends AppIntro {
        static final String[] initConstantTitles = {"Avogadro's Number", "Atomic Mass Unit", "Planck's Constant", "Electron Charge", "Gas Constant", "Faraday Constant"};
        static final String[] initConstantNums = {"6.0221409×10²³", "1.67377×10⁻²⁷", "6.6260690×10⁻³⁴", "1.6021766×10⁻¹⁹", "8.3145", "96,485.337"};
        static final String[] initConstantUnits = {"mol⁻¹", "kg", "J·s", "C", "J·K⁻¹·mol⁻¹", "C/mol"};

        ArrayList<String> constantTitles = new ArrayList<>(), constantNums = new ArrayList<>(), constantUnits = new ArrayList<>();

        @Override
        protected void onCreate( Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);

                try {
                    TinyDB tinydb = new TinyDB(this);

                    tinydb.putBoolean("isDarkTab", true);

                    tinydb.putInt("stackCount", 0);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                setColorTransitionsEnabled(true);
                setWizardMode(true);

                setNavBarColor(Color.parseColor("#000000"));

                //TODO: Localize these strings

                addSlide(AppIntroFragment.newInstance("Welcome!",
                        "Before you start using TermCalc, let's run through a few things it can do!", R.drawable.cheat_the_system, Color.parseColor("#00BEA4")));

                addSlide(AppIntroFragment.newInstance(
                        "Extra Functions",
                        "The log, √, and trig buttons, can be long-pressed for advanced functions. Check the \"Help\" section in the app for more info.",
                        R.drawable.cheat_the_system, Color.parseColor("#272C33")));

                addSlide(AppIntroFragment.newInstance(
                        "Fully Customizable!",
                        "Using the app's theme editor, you can change the color of almost any part of the UI!",
                        R.drawable.cheat_the_system, Color.parseColor("#E87369")
                ));

                addSlide(AppIntroFragment.newInstance(
                        "That's it!",
                        "You're all set! (:",
                        R.drawable.cheat_the_system, Color.parseColor("#00BEA4")
                ));

                setTransformer(new AppIntroPageTransformerType.Parallax(1.0, -1.0, 2.0));

                try {
                    final HandlerThread thread = new HandlerThread("SetDefaultConstantsThread");
                    thread.start();

                    new Handler(thread.getLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                int i;

                                TinyDB tinydb = new TinyDB(MainActivity.mainActivity);

                                for (i = 0; i < initConstantTitles.length; i++) {
                                    constantTitles.add(initConstantTitles[i]);
                                    constantNums.add(initConstantNums[i]);
                                    constantUnits.add(initConstantUnits[i]);
                                }

                                tinydb.putListString("constantTitles", constantTitles);
                                tinydb.putListString("constantNums", constantNums);
                                tinydb.putListString("constantUnits", constantUnits);
                            }
                            catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                    }, 3);
                }
                catch (NullPointerException e){
                    e.printStackTrace();
                }
        }

        @Override
        protected void onSkipPressed(Fragment currentFragment) {
                super.onSkipPressed(currentFragment);
                finish();
        }

        @Override
        protected void onDonePressed(Fragment currentFragment) {
                super.onDonePressed(currentFragment);
                finish();
        }
}