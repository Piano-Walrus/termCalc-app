package com.mirambeau.termcalc;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class GeometryFragmentNew extends Fragment {
    Activity main = MainActivity.mainActivity;

    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivity);
    String theme = sp.getString(SettingsActivity.KEY_PREF_THEME, "1");
    String color = sp.getString(SettingsActivity.KEY_PREF_COLOR, "1");
    boolean isCustomTheme = sp.getBoolean("custom", false);

    String primary, secondary, tertiary;

    final private ArrayList<GeoCard> areaCards = new ArrayList<>();
    final private ArrayList<GeoCard> volumeCards = new ArrayList<>();
    final private ArrayList<GeoCard> saCards = new ArrayList<>();

    final private ArrayList<ArrayList<GeoCard>> allCards = new ArrayList<>();;

    private GeoAdapter adapter;
    RecyclerView recyclerView;

    String[] oldInputs = {" ", " ", " "};

    static final String[] primaryColors = {"#03DAC5", "#009688", "#54AF57", "#00C7E0", "#2196F3", "#0D2A89", "#3F51B5", "LILAC", "PINK", "#F44336", "#E77369", "#FF9800", "#FFC107", "#FEF65B", "#66BB6A", "#873804", "#B8E2F8"};
    static final String[][] secondaryColors = {{"#53E2D4", "#4DB6AC", "#77C77B", "#51D6E8", "#64B5F6", "#1336A9", "#7986CB", "#8C6DCA", "#F06292", "#FF5956", "#EC8F87", "#FFB74D", "#FFD54F", "#FBF68D", "#EF5350", "#BD5E1E", "#B8E2F8"}, {"#00B5A3", "#00796B", "#388E3C", "#0097A7", "#1976D2", "#0A2068", "#303F9F", "#5E35B1", "#C2185B", "#D32F2F", "#D96459", "#F57C00", "#FFA000", "#F4E64B", "#EF5350", "#572300", "#9BCEE9"}};
    static final String[][] tertiaryColors = {{"#3CDECE", "#26A69A", "#68B86E", "#39CFE3", "#42A5F5", "#0D2F9E", "#5C6BC0", "#7857BA", "#EC407A", "#FA4E4B", "#EB837A", "#FFA726", "#FFCB2E", "#F8F276", "#FF5754", "#A14D15", "#ABDBF4"}, {"#00C5B1", "#00897B", "#43A047", "#00ACC1", "#1E88E5", "#0A2373", "#3949AB", "#663ABD", "#D81B60", "#E33532", "#DE685D", "#FB8C00", "#FFB300", "#FCEE54", "#FF5754", "#612703", "#ABDBF4"}};

    final int darkGray = Color.parseColor("#222222");
    final int monochromeTextColor = Color.parseColor("#303030");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_geometry_new, container, false);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        TinyDB tinydb = new TinyDB(MainActivity.mainActivity);

        tinydb.putString("fragTag", "Geometry");

        MainActivity.mainActivity.recreate();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        savedInstanceState = null;

        super.onViewCreated(view, savedInstanceState);

        try {
            int i;

            final TinyDB tinydb = new TinyDB(MainActivity.mainActivity);

            boolean theme_boolean;

            if (theme == null || !Ax.isDigit(theme))
                theme = "1";

            if (theme.equals("5"))
                theme_boolean = tinydb.getBoolean("theme_boolean");
            else {
                final SharedPreferences mPrefs = main.getSharedPreferences("THEME", 0);
                theme_boolean = mPrefs.getBoolean("theme_boolean", true);
            }

            if (color == null || !Ax.isDigit(color))
                color = "1";

            if (!isCustomTheme) {
                primary = primaryColors[Integer.parseInt(color) - 1];

                //Light
                if (!theme_boolean) {
                    secondary = secondaryColors[0][Integer.parseInt(color) - 1];
                    tertiary = tertiaryColors[0][Integer.parseInt(color) - 1];
                }
                //Dark
                else {
                    secondary = secondaryColors[1][Integer.parseInt(color) - 1];
                    tertiary = tertiaryColors[1][Integer.parseInt(color) - 1];
                }

                if (primary.equals("LILAC")) {
                    if (!theme_boolean)
                        primary = "#6C42B6";
                    else
                        primary = "#7357C2";
                }
                else if (primary.equals("PINK")) {
                    if (!theme_boolean)
                        primary = "#E32765";
                    else
                        primary = "#E91E63";
                }
                else if (primary.equals("#B8E2F8")) {
                    if (!theme_boolean)
                        primary = "#9BCEE9";
                    else
                        primary = "#B8E2F8";
                }
            }
            else {
                String cPrimary = tinydb.getString("cPrimary");
                String cSecondary = tinydb.getString("cSecondary");
                String cTertiary = tinydb.getString("cTertiary");

                //Primary
                if (cPrimary.length() == 7) {
                    primary = cPrimary;
                }
                else {
                    color = "1";
                    primary = "#03DAC5";
                }

                //Secondary
                if (cSecondary.length() == 7) {
                    secondary = cSecondary;
                }
                else {
                    color = "1";
                    secondary = "#00B5A3";
                }

                //Tertiary
                if (cTertiary.length() == 7) {
                    tertiary = cTertiary;
                }
                else {
                    color = "1";
                    tertiary = "#00897B";
                }
            }

            ConstraintLayout geoBG = main.findViewById(R.id.geoMainBG);
            TabLayout tabLayout = (TabLayout) main.findViewById(R.id.geoTabs);

            final String formColor;

            if (theme == null || theme.equals("\0"))
                theme = "1";

            if (theme.equals("2"))
                formColor = "\\(\\color{black}";
            else
                formColor = "\\(\\color{white}";

            final String[][] formTexts = {{"{a^2}\\)", "{l \\times w}\\)", "{\\pi r^2}\\)", "{\\pi R r}\\)", "{\\frac 1 2 bh}\\)", "{\\frac 1 2 (b1 + b2) \\times h}\\)", "{b \\times h}\\)", "{\\frac 5 2 sa}\\)", "{\\frac 6 2 sa}\\)", "{\\frac 7 2 sa}\\)", "{\\frac 8 2 sa}\\)", "{\\frac 9 2 sa}\\)", "{\\frac {10} 2 sa}\\)"},
                    {"{a^3}\\)", "{l \\times w \\times h}\\)", "{\\frac4 3 \\pi r^3}\\)", "{\\frac2 3 \\pi r^3}\\)", "{\\frac 1 2 bhl}\\)", "{\\frac 1 3 Ah}\\)", "{\\frac 1 3 lwh}\\)", "{\\frac 5 2 sah}\\)", "{\\frac 1 3 \\pi r^2 h}\\)", "{\\pi r^2 h}\\)", "{\\frac {\\sqrt{2}} 3 s^3}\\)", "{\\frac {15 + 7 \\sqrt{5}} 4 s^3}\\)", "{\\pi r^2 \\times 2 \\pi R}\\)"},
                    {"{6a^2}\\)", "{2(lw + lh + wh)}\\)", "{4 \\pi r^2}\\)", "{3 \\pi r^2}\\)", "{2(bh + bl + hl)}\\)", "{A_b + \\frac 1 2 P_b h}\\)", "{A_b + l\\sqrt{{\\frac w 2}^2 + h^2} + w\\sqrt{{\\frac l 2}^2 + h^2}}\\)", "{5s(a + h)}\\)", "{\\pi r (r + \\sqrt{h^2 + r^2})}\\)", "{2 \\pi r (h + r)}\\)", "{2 \\sqrt{3} s^2}\\)", "{3 s^2 \\sqrt{25 + 10 \\sqrt{5}}}\\)", "{4 \\pi^2 R r}\\)"}};

            final String[][] titles = {{getString(R.string.shape_square), getString(R.string.rectangle), getString(R.string.circle), getString(R.string.ellipse), getString(R.string.triangle), getString(R.string.trapezoid), getString(R.string.parallelogram), getString(R.string.pentagon), getString(R.string.hexagon), getString(R.string.heptagon), getString(R.string.octagon), getString(R.string.nonagon), getString(R.string.decagon)},
                    {getString(R.string.cube), getString(R.string.rectangular_prism), getString(R.string.sphere), getString(R.string.hemisphere), getString(R.string.triangular_prism), getString(R.string.pyramid_triangular_base), getString(R.string.pyramid_rectangular_base), getString(R.string.pentagonal_prism), getString(R.string.cone), getString(R.string.cylinder), getString(R.string.regular_octahedron), getString(R.string.dodecahedron), getString(R.string.torus)},
                    {getString(R.string.cube), getString(R.string.rectangular_prism), getString(R.string.sphere), getString(R.string.hemisphere), getString(R.string.triangular_prism), getString(R.string.pyramid_triangular_base), getString(R.string.pyramid_rectangular_base), getString(R.string.pentagonal_prism), getString(R.string.cone), getString(R.string.cylinder), getString(R.string.regular_octahedron), getString(R.string.dodecahedron), getString(R.string.torus)}};

            final String[][] areaTitles = {{getString(R.string.side_length)}, {getString(R.string.length), getString(R.string.width)}, {getString(R.string.radius)}, {getString(R.string.major_radius), getString(R.string.minor_radius)}, {getString(R.string.base_length), getString(R.string.height)}, {getString(R.string.lower_base), getString(R.string.upper_base), getString(R.string.height)}, {getString(R.string.length), getString(R.string.width)},
                    {getString(R.string.side_length), getString(R.string.apothem)}, {getString(R.string.side_length), getString(R.string.apothem)}, {getString(R.string.side_length), getString(R.string.apothem)}, {getString(R.string.side_length), getString(R.string.apothem)}, {getString(R.string.side_length), getString(R.string.apothem)}, {getString(R.string.side_length), getString(R.string.apothem)}};

            final String[][] volumeTitles = {{getString(R.string.side_length)}, {getString(R.string.length), getString(R.string.width), getString(R.string.height)}, {getString(R.string.radius)}, {getString(R.string.radius)}, {getString(R.string.base_length_2), getString(R.string.length), getString(R.string.height)}, {getString(R.string.base_area), getString(R.string.pyramid_height)},
                    {getString(R.string.base_length), getString(R.string.base_width), getString(R.string.pyramid_height)}, {getString(R.string.side_length), getString(R.string.apothem), getString(R.string.prism_height)}, {getString(R.string.radius), getString(R.string.height)}, {getString(R.string.radius), getString(R.string.height)}, {getString(R.string.side_length)}, {getString(R.string.side_length)}, {getString(R.string.major_radius), getString(R.string.minor_radius)}};

            final String[][] saTitles = {{getString(R.string.side_length)}, {getString(R.string.length), getString(R.string.width), getString(R.string.height)}, {getString(R.string.radius)}, {getString(R.string.radius)}, {getString(R.string.base_length_2), getString(R.string.length), getString(R.string.height)}, {getString(R.string.base_area), getString(R.string.base_perimeter), getString(R.string.slant_height)},
                    {getString(R.string.base_length), getString(R.string.base_width), getString(R.string.pyramid_height)}, {getString(R.string.side_length), getString(R.string.apothem), getString(R.string.prism_height)}, {getString(R.string.radius), getString(R.string.height)}, {getString(R.string.radius), getString(R.string.height)}, {getString(R.string.side_length)}, {getString(R.string.side_length)},
                    {getString(R.string.major_radius), getString(R.string.minor_radius)}};

            for (i = 0; i < titles[0].length; i++) {
                areaCards.add(new GeoCard(titles[0][i], areaTitles[i]));
                volumeCards.add(new GeoCard(titles[1][i], volumeTitles[i]));
                saCards.add(new GeoCard(titles[2][i], saTitles[i]));
            }

            allCards.add(areaCards);
            allCards.add(volumeCards);
            allCards.add(saCards);

            adapter = new GeoAdapter(areaCards);

            adapter.setFormColor(formColor);
            adapter.setFormTexts(formTexts[0]);

            recyclerView = main.findViewById(R.id.geoRecyclerView);

            if (recyclerView != null) {
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.mainActivity));
                recyclerView.setAdapter(adapter);
            }

            adapter.setOnItemClickListener(onEqualsClickListener);

            String bgColor = secondary;

            if (theme.equals("2")) {
                bgColor = "#FFFFFF";

                if (tabLayout != null) {
                    tabLayout.setTabTextColors(darkGray, darkGray);
                    tabLayout.setBackgroundColor(Color.WHITE);
                }

                MainActivity.mainActivity.getWindow().setNavigationBarColor(Color.parseColor("#1A1A1B"));

                if (Build.VERSION.SDK_INT >= 23) {
                    if (geoBG != null)
                        geoBG.setFitsSystemWindows(true);

                    MainActivity.mainActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                }
            }
            else if (theme.equals("5")) {
                if (tabLayout != null) {
                    tabLayout.setTabTextColors(monochromeTextColor, monochromeTextColor);
                }
            }
            else {
                bgColor = "#000000";

                if (isCustomTheme)
                    MainActivity.mainActivity.getWindow().setStatusBarColor(0);
                else if (theme.equals("1"))
                    MainActivity.mainActivity.getWindow().setStatusBarColor(Color.BLACK);

                if (tabLayout != null) {
                    tabLayout.setTabTextColors(Color.WHITE, Color.WHITE);
                }
            }

            if (geoBG != null)
                geoBG.setBackgroundColor(Color.parseColor(bgColor));

            String bigColor = "#03DAC5";
            final FloatingActionButton jumpTo = main.findViewById(R.id.jumpTo);


            if (theme.equals("5")) {
                bigColor = "#303030";
            }
            else {
                if (!isCustomTheme && Ax.isColor(tinydb.getString("accentPrimary"))) {
                    bigColor = tinydb.getString("accentPrimary");
                }
                else if (isCustomTheme) {
                    if (Ax.isColor(tinydb.getString("cPrimary")) && !Ax.isGray(tinydb.getString("cPrimary"))) {
                        bigColor = tinydb.getString("cPrimary");
                    }
                    else if (Ax.isColor(tinydb.getString("cSecondary")) && !Ax.isGray(tinydb.getString("cSecondary"))) {
                        bigColor = tinydb.getString("cSecondary");
                    }
                    else if (Ax.isColor(tinydb.getString("-b=t")) && !Ax.isGray(tinydb.getString("-b=t"))) {
                        bigColor = tinydb.getString("-b=t");
                    }
                    else if (Ax.isColor(tinydb.getString("cTop")) && !Ax.isGray(tinydb.getString("cTop"))) {
                        bigColor = tinydb.getString("cTop");
                    }
                    else if (Ax.isColor(tinydb.getString("cTertiary")) && !Ax.isGray(tinydb.getString("cTertiary"))) {
                        bigColor = tinydb.getString("cTertiary");
                    }
                    else if (Ax.isColor(tinydb.getString("-b+t")) && !Ax.isGray(tinydb.getString("-b+t"))) {
                        bigColor = tinydb.getString("-b+t");
                    }
                }
            }

            if (tabLayout != null) {
                tabLayout.setSelectedTabIndicatorColor(Color.parseColor(bigColor));
                tabLayout.setTabRippleColor(ColorStateList.valueOf(Color.parseColor(bigColor)));
            }

            if (jumpTo != null) {
                if (theme.equals("5")) {
                    jumpTo.setBackgroundTintList(ColorStateList.valueOf(monochromeTextColor));

                    if (!isCustomTheme && Ax.isColor(tinydb.getString("accentPrimary")))
                        jumpTo.setColorFilter(Color.parseColor(tinydb.getString("accentPrimary")));
                }
                else if (theme.equals("2")) {
                    jumpTo.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(bigColor)));

                    if (!isCustomTheme && color != null && (color.equals("14") || color.equals("17")))
                        jumpTo.setColorFilter(darkGray);
                }
                else {
                    jumpTo.setBackgroundTintList(ColorStateList.valueOf(darkGray));
                    jumpTo.setColorFilter(Color.parseColor(bigColor));
                }

                jumpTo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final PopupMenu popup = new PopupMenu(MainActivity.mainActivity, v);
                        MenuInflater inflater = popup.getMenuInflater();
                        inflater.inflate(R.menu.area_jump_menu, popup.getMenu());

                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                int position = popup.getMenu().findItem(item.getItemId()).getOrder();

                                if (recyclerView != null) {
                                    recyclerView.scrollToPosition(position);
                                }

                                return false;
                            }
                        });
                        popup.show();
                    }
                });
            }

            if (tabLayout != null && tabLayout.getTabAt(0) != null)
                tabLayout.getTabAt(0).select();

            if (tabLayout != null) {
                tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        final int position = tab.getPosition();

                        adapter = new GeoAdapter(allCards.get(position));

                        adapter.setFormColor(formColor);
                        adapter.setFormTexts(formTexts[position]);

                        if (recyclerView != null)
                            recyclerView.setAdapter(adapter);

                        adapter.setOnItemClickListener(onEqualsClickListener);

                        if (jumpTo != null) {
                            jumpTo.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final PopupMenu popup = new PopupMenu(MainActivity.mainActivity, v);
                                    MenuInflater inflater = popup.getMenuInflater();

                                    if (position == 0)
                                        inflater.inflate(R.menu.area_jump_menu, popup.getMenu());
                                    else
                                        inflater.inflate(R.menu.volume_jump_menu, popup.getMenu());

                                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                        @Override
                                        public boolean onMenuItemClick(MenuItem item) {
                                            int position = popup.getMenu().findItem(item.getItemId()).getOrder();

                                            if (recyclerView != null) {
                                                recyclerView.scrollToPosition(position);
                                            }

                                            return false;
                                        }
                                    });
                                    popup.show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {

                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {

                    }
                });
            }
        }
        catch (Exception e){
            e.printStackTrace();
            Ax.saveStack(e);
            MainActivity.mainActivity.finishAffinity();
        }
    }

    final private GeoAdapter.OnItemClickListener onEqualsClickListener = new GeoAdapter.OnItemClickListener() {
        @Override
        public void onEqualsClick(int position, String[] inputs, TextInputLayout[] params) {
            TabLayout tabs = MainActivity.mainActivity.findViewById(R.id.geoTabs);
            int tab = tabs.getSelectedTabPosition();

            GeoCard card = allCards.get(tab).get(position);
            boolean arraysAreEqual = true;

            int j;

            if (oldInputs != null && oldInputs.length > 0) {
                for (j = 0; j < inputs.length; j++) {
                    if (!inputs[j].equals(oldInputs[j])) {
                        arraysAreEqual = false;
                        break;
                    }
                }
            }

            if (!arraysAreEqual && Ax.isFullNum(inputs[0])) {
                if (allCards.get(tab).get(0).getShapeTitle().equalsIgnoreCase("square")) {
                    if (GeoCalc.area(card.getShapeTitle().toLowerCase(), inputs) != 0.0)
                        card.setAnswerText(" = " + GeoCalc.area(card.getShapeTitle().toLowerCase(), inputs));
                }
                else if (allCards.get(tab).get(0).getShapeTitle().equalsIgnoreCase("cube")) {
                    if (allCards.get(tab).get(5).getParamHint(2).equalsIgnoreCase("slant height")) {
                        if (GeoCalc.sa(card.getShapeTitle().toLowerCase(), inputs) != 0.0)
                            card.setAnswerText(" = " + GeoCalc.sa(card.getShapeTitle().toLowerCase(), inputs));
                    }
                    else if (GeoCalc.volume(card.getShapeTitle().toLowerCase(), inputs) != 0.0)
                        card.setAnswerText(" = " + GeoCalc.volume(card.getShapeTitle().toLowerCase(), inputs));
                }

                card.setInputTexts(inputs, card.getShapeTitle());

                if (card.getAnswerText() != null && card.getAnswerText().contains("="))
                    adapter.notifyItemChanged(position);
            }
        }
    };
}
