
package com.liquidglass.launcher;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private final int WHITE = Color.rgb(245, 248, 255);
    private final int GLASS = Color.argb(155, 28, 43, 67);
    private final Handler handler = new Handler();
    private TextView clock;
    private LinearLayout appRow;

    private int dp(float value) {
        return (int)(value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable glass(int color) {
        GradientDrawable bg = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{color, Color.rgb(32, 45, 73)}
        );
        bg.setCornerRadius(dp(20));
        bg.setStroke(dp(1), Color.argb(90, 180, 210, 255));
        return bg;
    }

    private TextView label(String value, int size, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(WHITE);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setGravity(Gravity.CENTER_VERTICAL);
        return t;
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        LinearLayout root = new LinearLayout(this);
        root.setPadding(dp(16), dp(12), dp(16), dp(12));
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setBackground(new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{Color.rgb(6, 12, 27), Color.rgb(25, 26, 55)}
        ));
        setContentView(root);

        LinearLayout sidebar = new LinearLayout(this);
        sidebar.setOrientation(LinearLayout.VERTICAL);
        sidebar.setPadding(dp(10), dp(10), dp(10), dp(10));
        sidebar.setBackground(glass(GLASS));
        root.addView(sidebar, new LinearLayout.LayoutParams(dp(185), -1));

        clock = label("--:--", 21, true);
        clock.setGravity(Gravity.CENTER);
        sidebar.addView(clock, new LinearLayout.LayoutParams(-1, dp(65)));
        updateClock();
        handler.post(clockTick);

        TextView logo = label("LIQUID GLASS", 12, true);
        logo.setTextColor(Color.rgb(145, 190, 255));
        logo.setGravity(Gravity.CENTER);
        sidebar.addView(logo, new LinearLayout.LayoutParams(-1, dp(36)));

        String[] menu = {
            "⌂  Início", "▦  Apps", "▣  Filmes e Séries",
            "◉  TV ao Vivo", "◇  Jogos", "♫  Música",
            "⚙  Configurações"
        };

        for (String name : menu) {
            TextView item = label(name, 12, false);
            item.setPadding(dp(10), 0, dp(4), 0);
            item.setFocusable(true);
            item.setBackground(glass(Color.argb(50, 255, 255, 255)));

            LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(-1, dp(42));
            lp.setMargins(0, dp(4), 0, dp(4));
            sidebar.addView(item, lp);

            item.setOnClickListener(v -> {
                if (name.contains("Apps")) {
                    if (appRow != null) appRow.requestFocus();
                } else {
                    Toast.makeText(this, name + " selecionado",
                        Toast.LENGTH_SHORT).show();
                }
            });
        }

        ScrollView scroll = new ScrollView(this);
        LinearLayout.LayoutParams scrollLp =
            new LinearLayout.LayoutParams(0, -1, 1);
        scrollLp.leftMargin = dp(15);
        root.addView(scroll, scrollLp);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(content);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView heading = label("Seu entretenimento, em um só lugar", 15, true);
        top.addView(heading, new LinearLayout.LayoutParams(0, dp(40), 1));

        TextView weather = label("☁  Clima demonstrativo", 11, false);
        weather.setGravity(Gravity.CENTER);
        weather.setBackground(glass(GLASS));
        top.addView(weather, new LinearLayout.LayoutParams(dp(160), dp(36)));
        content.addView(top);

        TextView hero = label(
            "LIQUID GLASS\n\nUma experiência de TV mais elegante.\n\n▶  Explorar aplicativos",
            21, true
        );
        hero.setPadding(dp(24), 0, dp(15), 0);
        hero.setBackground(new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{Color.rgb(28, 65, 111), Color.rgb(69, 48, 105)}
        ));
        hero.setFocusable(true);
        hero.setOnClickListener(v -> {
            if (appRow != null) appRow.requestFocus();
        });

        LinearLayout.LayoutParams heroLp =
            new LinearLayout.LayoutParams(-1, dp(205));
        heroLp.setMargins(0, dp(10), 0, dp(12));
        content.addView(hero, heroLp);

        TextView title = label("Seus aplicativos", 17, true);
        content.addView(title, new LinearLayout.LayoutParams(-1, dp(32)));

        HorizontalScrollView horizontal = new HorizontalScrollView(this);
        horizontal.setHorizontalScrollBarEnabled(false);
        appRow = new LinearLayout(this);
        appRow.setOrientation(LinearLayout.HORIZONTAL);
        horizontal.addView(appRow);
        content.addView(horizontal, new LinearLayout.LayoutParams(-1, dp(112)));

        loadApps();

        TextView continueTitle = label("Continuar assistindo", 17, true);
        LinearLayout.LayoutParams ct =
            new LinearLayout.LayoutParams(-1, dp(34));
        ct.topMargin = dp(7);
        content.addView(continueTitle, ct);

        LinearLayout posters = new LinearLayout(this);
        String[] names = {"Cinema", "Séries", "Descobrir", "Favoritos"};
        int[] colors = {
            0xff31557d, 0xff51436e, 0xff2f6668, 0xff6b4b53
        };

        for (int i = 0; i < names.length; i++) {
            TextView poster = label(names[i], 13, true);
            poster.setGravity(Gravity.CENTER);
            poster.setBackground(glass(colors[i]));

            LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(dp(135), dp(85));
            p.setMargins(0, 0, dp(9), 0);
            posters.addView(poster, p);
        }

        content.addView(posters);

        TextView note = label(
            "Clima e conteúdos são demonstrativos nesta versão.", 10, false
        );
        content.addView(note, new LinearLayout.LayoutParams(-1, dp(28)));
    }

    private void updateClock() {
        if (clock != null) {
            Date now = new Date();
            clock.setText(
                new SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
                + "\n" +
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(now)
            );
        }
    }

    private final Runnable clockTick = new Runnable() {
        @Override
        public void run() {
            updateClock();
            handler.postDelayed(this, 30000);
        }
    };

    private void loadApps() {
        PackageManager pm = getPackageManager();
        Intent leanback = new Intent(Intent.ACTION_MAIN);
        leanback.addCategory("android.intent.category.LEANBACK_LAUNCHER");

        List<ResolveInfo> apps = new ArrayList<>(
            pm.queryIntentActivities(leanback, 0)
        );

        Intent normal = new Intent(Intent.ACTION_MAIN);
        normal.addCategory(Intent.CATEGORY_LAUNCHER);

        for (ResolveInfo r : pm.queryIntentActivities(normal, 0)) {
            boolean exists = false;
            for (ResolveInfo a : apps) {
                if (a.activityInfo.packageName.equals(
                    r.activityInfo.packageName)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) apps.add(r);
        }

        int count = 0;
        for (ResolveInfo r : apps) {
            if (r.activityInfo.packageName.equals(getPackageName())) continue;
            if (count++ >= 18) break;

            final String pkg = r.activityInfo.packageName;

            LinearLayout tile = new LinearLayout(this);
            tile.setOrientation(LinearLayout.VERTICAL);
            tile.setGravity(Gravity.CENTER);
            tile.setBackground(glass(GLASS));
            tile.setFocusable(true);

            ImageView icon = new ImageView(this);
            try {
                icon.setImageDrawable(r.loadIcon(pm));
                tile.addView(icon, new LinearLayout.LayoutParams(dp(40), dp(40)));
            } catch (Exception ignored) {}

            TextView appName = label(r.loadLabel(pm).toString(), 10, false);
            appName.setGravity(Gravity.CENTER);
            appName.setMaxLines(2);
            tile.addView(appName, new LinearLayout.LayoutParams(dp(86), dp(30)));

            LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(dp(102), dp(102));
            lp.setMargins(0, 0, dp(8), 0);
            appRow.addView(tile, lp);

            tile.setOnClickListener(v -> {
                Intent launch = pm.getLaunchIntentForPackage(pkg);
                if (launch == null) {
                    launch = new Intent(Intent.ACTION_MAIN);
                    launch.addCategory("android.intent.category.LEANBACK_LAUNCHER");
                    launch.setPackage(pkg);
                }
                try {
                    startActivity(launch);
                } catch (Exception e) {
                    Toast.makeText(this, "Não foi possível abrir o aplicativo",
                        Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (appRow.getChildCount() == 0) {
            appRow.addView(label("Nenhum aplicativo encontrado", 12, false));
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN &&
            (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER ||
             event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
            View current = getCurrentFocus();
            if (current != null) {
                current.performClick();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(clockTick);
        super.onDestroy();
    }
}
