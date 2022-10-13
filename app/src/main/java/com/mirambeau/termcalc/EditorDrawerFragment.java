package com.mirambeau.termcalc;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.Objects;

public class EditorDrawerFragment extends Fragment {
    Activity editor = EditorActivity.editorActivity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.editor_drawer, container, false);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    //onCreate
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ConstraintLayout saveLayout = editor.findViewById(R.id.saveOptionLayout);
        final ConstraintLayout importLayout = editor.findViewById(R.id.importOptionLayout);
        final ConstraintLayout resetLayout = editor.findViewById(R.id.resetOptionLayout);
        final ConstraintLayout advancedLayout = editor.findViewById(R.id.advancedOptionLayout);

        if (saveLayout != null) {
            saveLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }

        if (importLayout != null) {
            importLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }

        if (resetLayout != null) {
            resetLayout.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onClick(View view) {
                    TextView resetTitle = (TextView) resetLayout.getChildAt(0);
                    resetLayout.setBackgroundColor(Color.parseColor("#F44336"));

                    resetTitle.setText("Are you sure?");

                    try {
                        ObjectAnimator.ofFloat(view, "rotation", 0f, 360f).setDuration(750).start();
                        new Handler((Objects.requireNonNull(Looper.myLooper()))).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                            }
                        }, 200);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            });
        }

        if (advancedLayout != null) {
            advancedLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
    }
}
