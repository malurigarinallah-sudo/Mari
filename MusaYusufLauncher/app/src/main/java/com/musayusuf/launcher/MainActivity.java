package com.musayusuf.launcher;

import android.app.*;
import android.app.WallpaperManager;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.text.*;
import android.view.*;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {

    LinearLayout root, appGrid;

    float downY, downX;
    long downTime;

    int homePage = 0;

    boolean dimWallpaper = false;
    int iconShape = 0;

    final int[] photos = {
        R.drawable.musa_yusuf_01,
        R.drawable.musa_yusuf_02,
        R.drawable.musa_yusuf_03,
        R.drawable.musa_yusuf_04,
        R.drawable.musa_yusuf_poster
    };

    final Set<String> hidden = new HashSet<>();

    SharedPreferences prefs;

    Handler clockHandler = new Handler(Looper.getMainLooper());

    int dp(float x) {
        return (int) (x * getResources().getDisplayMetrics().density + .5f);
    }

    TextView tv(String s, float size) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(Color.WHITE);
        t.setTextSize(size);
        t.setPadding(dp(8), dp(8), dp(8), dp(8));
        return t;
    }

    GradientDrawable round(int c, int r) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(c);
        g.setCornerRadius(dp(r));
        return g;
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("launcher", 0);
        load();
        getWindow().setStatusBarColor(Color.rgb(5, 19, 42));
        showHome();
    }

    void load() {
        hidden.clear();
        hidden.addAll(prefs.getStringSet("hidden", new HashSet<String>()));
        dimWallpaper = prefs.getBoolean("dim", false);
        iconShape = prefs.getInt("shape", 0);
    }

    void save() {
        prefs.edit()
                .putStringSet("hidden", hidden)
                .putBoolean("dim", dimWallpaper)
                .putInt("shape", iconShape)
                .apply();
    }

    void base() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12), dp(10), dp(12), dp(8));

        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(5, 19, 42), Color.rgb(7, 46, 72), Color.rgb(5, 65, 44)}
        );

        root.setBackground(bg);
        setContentView(root);
    }

    void showHome() {
        base();

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);

        TextView brand = tv("MUSA YUSUF\nLauncher • Offline", 17);
        brand.setTypeface(null, Typeface.BOLD);

        top.addView(brand, new LinearLayout.LayoutParams(0, dp(58), 1));

        TextView settings = tv("⚙", 25);
        settings.setGravity(Gravity.CENTER);
        settings.setBackground(round(0x22FFFFFF, 16));
        settings.setOnClickListener(v -> showCustomize());

        top.addView(settings, new LinearLayout.LayoutParams(dp(48), dp(48)));

        root.addView(top);

        FrameLayout hero = new FrameLayout(this);

        ImageView im = new ImageView(this);
        im.setImageResource(homePage == 0 ? R.drawable.musa_yusuf_01 : R.drawable.musa_yusuf_03);
        im.setScaleType(ImageView.ScaleType.CENTER_CROP);

        hero.addView(im, new FrameLayout.LayoutParams(-1, dp(330)));

        View fade = new View(this);
        fade.setBackground(new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0x00000000, 0xE6001025}
        ));

        hero.addView(fade, new FrameLayout.LayoutParams(-1, dp(330)));

        if (dimWallpaper) {
            View dim = new View(this);
            dim.setBackgroundColor(0x55000000);
            hero.addView(dim, new FrameLayout.LayoutParams(-1, dp(330)));
        }

        TextView name = tv("Musa Yusuf\nPublic Profile Theme", 24);
        name.setTypeface(null, Typeface.BOLD);

        FrameLayout.LayoutParams np = new FrameLayout.LayoutParams(-1, dp(90), Gravity.BOTTOM);
        np.leftMargin = dp(10);

        hero.addView(name, np);

        root.addView(hero, new LinearLayout.LayoutParams(-1, dp(330)));

        TextView clock = tv("", 34);
        clock.setGravity(Gravity.CENTER);
        root.addView(clock, new LinearLayout.LayoutParams(-1, dp(60)));

        TextView date = tv("", 12);
        date.setGravity(Gravity.CENTER);
        date.setTextColor(0xBBD9E8F2);

        root.addView(date, new LinearLayout.LayoutParams(-1, dp(28)));

        updateClock(clock, date);

        TextView search = tv("🔎  Search apps", 16);
        search.setGravity(Gravity.CENTER_VERTICAL);
        search.setBackground(round(0x22FFFFFF, 24));
        search.setOnClickListener(v -> showApps());

        root.addView(search, new LinearLayout.LayoutParams(-1, dp(50)));

        LinearLayout folders = new LinearLayout(this);
        folders.setGravity(Gravity.CENTER);

        String[] labels = {"📁 Favorites", "🖼 Photos", "🎨 Themes"};

        for (String s : labels) {
            TextView q = tv(s, 13);
            q.setGravity(Gravity.CENTER);
            q.setBackground(round(0x18FFFFFF, 16));

            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(48), 1);
            p.setMargins(dp(4), dp(7), dp(4), 0);

            folders.addView(q, p);
        }

        folders.getChildAt(0).setOnClickListener(v -> showApps());
        folders.getChildAt(1).setOnClickListener(v -> showWallpaperGallery());
        folders.getChildAt(2).setOnClickListener(v -> showThemes());

        root.addView(folders);

        LinearLayout dock = new LinearLayout(this);
        dock.setGravity(Gravity.CENTER);

        String[] d = {"☎", "✉", "▣", "☷"};

        for (String x : d) {
            TextView q = tv(x, 24);
            q.setGravity(Gravity.CENTER);
            q.setBackground(round(0x18FFFFFF, 16));

            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(54), 1);
            p.setMargins(dp(4), dp(7), dp(4), 0);

            dock.addView(q, p);
        }

        dock.getChildAt(2).setOnClickListener(v -> showWallpaperGallery());
        dock.getChildAt(3).setOnClickListener(v -> showApps());

        root.addView(dock);

        TextView page = tv(
                homePage == 0
                        ? "● ○   Swipe left for second home page"
                        : "○ ●   Swipe right for first home page",
                10
        );

        page.setGravity(Gravity.CENTER);
        page.setTextColor(0xA8D9E6F0);

        root.addView(page, new LinearLayout.LayoutParams(-1, dp(28)));

        TextView hint = tv("↑ Apps   ↓ Quick Panel   •   Long press to customize", 10);
        hint.setGravity(Gravity.CENTER);
        hint.setTextColor(0xA8D9E6F0);

        root.addView(hint, new LinearLayout.LayoutParams(-1, dp(28)));
    }

    void updateClock(TextView clock, TextView date) {
        Runnable r = new Runnable() {
            public void run() {
                Date d = new Date();

                clock.setText(new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(d));
                date.setText(new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(d));

                clockHandler.postDelayed(this, 30000);
            }
        };

        clockHandler.post(r);
    }

    List<ApplicationInfo> launcherApps() {
        PackageManager pm = getPackageManager();

        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> infos = pm.queryIntentActivities(i, 0);

        List<ApplicationInfo> out = new ArrayList<>();

        for (ResolveInfo r : infos) {
            if (r.activityInfo == null) continue;

            ApplicationInfo ai = r.activityInfo.applicationInfo;

            if (!hidden.contains(ai.packageName) && !contains(out, ai.packageName)) {
                out.add(ai);
            }
        }

        Collections.sort(out, (a, b) ->
                pm.getApplicationLabel(a).toString().compareToIgnoreCase(pm.getApplicationLabel(b).toString())
        );

        return out;
    }

    boolean contains(List<ApplicationInfo> l, String p) {
        for (ApplicationInfo a : l) {
            if (a.packageName.equals(p)) return true;
        }
        return false;
    }

    void showApps() {
        base();

        LinearLayout head = new LinearLayout(this);
        head.setGravity(Gravity.CENTER_VERTICAL);

        TextView back = tv("‹", 34);
        back.setGravity(Gravity.CENTER);
        back.setOnClickListener(v -> showHome());

        head.addView(back, new LinearLayout.LayoutParams(dp(45), dp(55)));

        TextView h = tv("All Apps", 23);
        h.setTypeface(null, Typeface.BOLD);

        head.addView(h, new LinearLayout.LayoutParams(0, dp(55), 1));

        TextView more = tv("⋮", 28);
        more.setGravity(Gravity.CENTER);
        more.setOnClickListener(v -> showCustomize());

        head.addView(more, new LinearLayout.LayoutParams(dp(45), dp(55)));

        root.addView(head);

        EditText e = new EditText(this);
        e.setHint("Search apps...");
        e.setSingleLine();
        e.setTextColor(Color.WHITE);
        e.setHintTextColor(0x99FFFFFF);
        e.setPadding(dp(16), 0, dp(16), 0);
        e.setBackground(round(0x22FFFFFF, 24));

        root.addView(e, new LinearLayout.LayoutParams(-1, dp(52)));

        TextView sort = tv("A–Z   •   4 columns", 11);
        sort.setGravity(Gravity.RIGHT);
        sort.setTextColor(0x99FFFFFF);

        root.addView(sort, new LinearLayout.LayoutParams(-1, dp(28)));

        ScrollView sv = new ScrollView(this);

        appGrid = new LinearLayout(this);
        appGrid.setOrientation(LinearLayout.VERTICAL);

        sv.addView(appGrid);

        root.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1));

        renderApps(launcherApps());

        e.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}

            public void onTextChanged(CharSequence s, int st, int b, int c) {
                String q = s.toString().toLowerCase(Locale.getDefault());

                List<ApplicationInfo> f = new ArrayList<>();

                PackageManager pm = getPackageManager();

                for (ApplicationInfo ai : launcherApps()) {
                    if (pm.getApplicationLabel(ai).toString().toLowerCase(Locale.getDefault()).contains(q)) {
                        f.add(ai);
                    }
                }

                renderApps(f);
            }

            public void afterTextChanged(Editable x) {}
        });
    }

    void renderApps(List<ApplicationInfo> list) {
        appGrid.removeAllViews();

        PackageManager pm = getPackageManager();

        LinearLayout row = null;
        int col = 0;

        for (ApplicationInfo ai : list) {
            if (col == 0) {
                row = new LinearLayout(this);
                row.setGravity(Gravity.TOP);

                appGrid.addView(row, new LinearLayout.LayoutParams(-1, dp(104)));
            }

            LinearLayout cell = new LinearLayout(this);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER);

            ImageView icon = new ImageView(this);
            icon.setImageDrawable(pm.getApplicationIcon(ai));

            if (iconShape == 1)
                icon.setBackground(round(0x22FFFFFF, 14));
            else if (iconShape == 2)
                icon.setBackground(round(0x22FFFFFF, 26));

            TextView label = tv(pm.getApplicationLabel(ai).toString(), 11);
            label.setGravity(Gravity.CENTER);
            label.setSingleLine(true);
            label.setEllipsize(TextUtils.TruncateAt.END);

            cell.addView(icon, new LinearLayout.LayoutParams(dp(48), dp(52)));
            cell.addView(label, new LinearLayout.LayoutParams(-1, dp(38)));

            cell.setOnClickListener(v -> {
                Intent launch = pm.getLaunchIntentForPackage(ai.packageName);
                if (launch != null) startActivity(launch);
            });

            cell.setOnLongClickListener(v -> {
                showAppOptions(ai);
                return true;
            });

            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, -1, 1);
            cp.setMargins(dp(3), 0, dp(3), 0);

            row.addView(cell, cp);

            col = (col + 1) % 4;
        }
    }

    void showAppOptions(ApplicationInfo ai) {
        PackageManager pm = getPackageManager();
        String name = pm.getApplicationLabel(ai).toString();

        new AlertDialog.Builder(this)
                .setTitle(name)
                .setItems(new String[]{"Open", "Hide app", "App info"}, (d, w) -> {
                    if (w == 0) {
                        Intent i = pm.getLaunchIntentForPackage(ai.packageName);
                        if (i != null) startActivity(i);
                    } else if (w == 1) {
                        hidden.add(ai.packageName);
                        save();
                        showApps();
                    } else {
                        startActivity(new Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + ai.packageName)
                        ));
                    }
                })
                .show();
    }

    void showQuick() {
        base();

        TextView h = tv("Quick Panel", 25);
        h.setTypeface(null, Typeface.BOLD);

        root.addView(h, new LinearLayout.LayoutParams(-1, dp(58)));

        String[] items = {"Wi-Fi", "Bluetooth", "Sound", "Brightness", "Wallpaper", "System Settings"};

        for (String s : items) {
            TextView q = tv("◉  " + s, 18);
            q.setBackground(round(0x22FFFFFF, 16));

            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(58));
            p.setMargins(0, dp(5), 0, dp(5));

            root.addView(q, p);

            if (s.equals("Wallpaper")) {
                q.setOnClickListener(v -> showWallpaperGallery());
            }

            if (s.equals("System Settings")) {
                q.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_SETTINGS)));
            }
        }

        TextView back = tv("← Back to Home", 16);
        back.setGravity(Gravity.CENTER);
        back.setOnClickListener(v -> showHome());

        root.addView(back, new LinearLayout.LayoutParams(-1, dp(58)));
    }

    void showWallpaperGallery() {
        base();

        TextView h = tv("Wallpapers", 23);
        h.setTypeface(null, Typeface.BOLD);

        root.addView(h, new LinearLayout.LayoutParams(-1, dp(58)));

        ScrollView sv = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);

        sv.addView(list);

        root.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1));

        for (int id : photos) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);

            ImageView im = new ImageView(this);
            im.setImageResource(id);
            im.setScaleType(ImageView.ScaleType.CENTER_CROP);

            card.addView(im, new LinearLayout.LayoutParams(-1, dp(300)));

            Button set = new Button(this);
            set.setText("Set as Wallpaper");
            set.setOnClickListener(v -> setWallpaper(id));

            card.addView(set, new LinearLayout.LayoutParams(-1, dp(50)));

            list.addView(card);
        }
    }

    void setWallpaper(int id) {
        try {
            WallpaperManager.getInstance(this).setResource(id);
            Toast.makeText(this, "Wallpaper set", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Wallpaper action unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    void showThemes() {
        base();

        TextView h = tv("Themes", 24);
        h.setTypeface(null, Typeface.BOLD);

        root.addView(h, new LinearLayout.LayoutParams(-1, dp(58)));

        String[] themes = {"Musa Blue + Green", "Midnight Blue", "Clean White", "Forest Green"};

        for (String s : themes) {
            TextView t = tv("🎨  " + s, 18);
            t.setBackground(round(0x22FFFFFF, 18));
            t.setOnClickListener(v -> Toast.makeText(this, "Theme selected: " + s, Toast.LENGTH_SHORT).show());

            root.addView(t, new LinearLayout.LayoutParams(-1, dp(58)));
        }

        TextView back = tv("← Home", 16);
        back.setGravity(Gravity.CENTER);
        back.setOnClickListener(v -> showHome());

        root.addView(back, new LinearLayout.LayoutParams(-1, dp(58)));
    }

    void showCustomize() {
        base();

        TextView h = tv("Customize Launcher", 25);
        h.setTypeface(null, Typeface.BOLD);

        root.addView(h, new LinearLayout.LayoutParams(-1, dp(60)));

        TextView wall = tv("🖼  Wallpapers", 18);
        wall.setBackground(round(0x22FFFFFF, 16));
        wall.setOnClickListener(v -> showWallpaperGallery());

        root.addView(wall, new LinearLayout.LayoutParams(-1, dp(58)));

        TextView theme = tv("🎨  Themes", 18);
        theme.setBackground(round(0x22FFFFFF, 16));
        theme.setOnClickListener(v -> showThemes());

        root.addView(theme, new LinearLayout.LayoutParams(-1, dp(58)));

        TextView shape = tv(
                "◉  Icon shape: " + (iconShape == 0 ? "System" : iconShape == 1 ? "Square" : "Round"),
                18
        );

        shape.setBackground(round(0x22FFFFFF, 16));
        shape.setOnClickListener(v -> {
            iconShape = (iconShape + 1) % 3;
            save();
            showCustomize();
        });

        root.addView(shape, new LinearLayout.LayoutParams(-1, dp(58)));

        TextView dim = tv("◐  Wallpaper dim: " + (dimWallpaper ? "On" : "Off"), 18);
        dim.setBackground(round(0x22FFFFFF, 16));
        dim.setOnClickListener(v -> {
            dimWallpaper = !dimWallpaper;
            save();
            showCustomize();
        });

        root.addView(dim, new LinearLayout.LayoutParams(-1, dp(58)));

        TextView hiddenItem = tv("👁  Hidden Apps", 18);
        hiddenItem.setBackground(round(0x22FFFFFF, 16));
        hiddenItem.setOnClickListener(v -> showHiddenApps());

        root.addView(hiddenItem, new LinearLayout.LayoutParams(-1, dp(58)));

        TextView reset = tv("↻  Reset Hidden Apps", 18);
        reset.setBackground(round(0x22FFFFFF, 16));
        reset.setOnClickListener(v -> {
            hidden.clear();
            save();
            Toast.makeText(this, "Hidden apps reset", Toast.LENGTH_SHORT).show();
        });

        root.addView(reset, new LinearLayout.LayoutParams(-1, dp(58)));

        TextView back = tv("← Home", 16);
        back.setGravity(Gravity.CENTER);
        back.setOnClickListener(v -> showHome());

        root.addView(back, new LinearLayout.LayoutParams(-1, dp(58)));
    }

    void showHiddenApps() {
        base();

        TextView h = tv("Hidden Apps", 24);
        h.setTypeface(null, Typeface.BOLD);

        root.addView(h, new LinearLayout.LayoutParams(-1, dp(58)));

        PackageManager pm = getPackageManager();

        for (String pkg : new HashSet<>(hidden)) {
            try {
                ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);

                TextView row = tv("○  " + pm.getApplicationLabel(ai) + "   (tap to restore)", 17);

                row.setOnClickListener(v -> {
                    hidden.remove(pkg);
                    save();
                    showHiddenApps();
                });

                root.addView(row, new LinearLayout.LayoutParams(-1, dp(58)));
            } catch (Exception ignored) {}
        }

        TextView back = tv("← Customize", 16);
        back.setGravity(Gravity.CENTER);
        back.setOnClickListener(v -> showCustomize());

        root.addView(back, new LinearLayout.LayoutParams(-1, dp(58)));
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            downY = ev.getY();
            downX = ev.getX();
            downTime = System.currentTimeMillis();
            return true;
        }

        if (ev.getAction() == MotionEvent.ACTION_UP) {
            float dy = ev.getY() - downY;
            float dx = ev.getX() - downX;
            long dt = System.currentTimeMillis() - downTime;

            if (Math.abs(dx) > 120 && Math.abs(dx) > Math.abs(dy)) {
                if (dx < 0 && homePage == 0) {
                    homePage = 1;
                    showHome();
                } else if (dx > 0 && homePage == 1) {
                    homePage = 0;
                    showHome();
                }
                return true;
            }

            if (dy < -120) {
                showApps();
                return true;
            }

            if (dy > 120) {
                showQuick();
                return true;
            }

            if (Math.abs(dy) < 25 && Math.abs(dx) < 25 && dt > 600) {
                showCustomize();
                return true;
            }

            return true;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        showHome();
    }
}
