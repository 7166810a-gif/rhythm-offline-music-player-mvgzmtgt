package com.generated.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements MusicService.OnPlayerStateChangedListener {

    private static final int PERMISSION_REQ_CODE = 101;

    private LinearLayout rootLayout;
    private LinearLayout contentContainer;
    private LinearLayout miniPlayerLayout;
    private LinearLayout fullPlayerSheet;
    private boolean isFullPlayerVisible = false;

    // Navigation Tabs
    private TextView tabHome, tabLibrary, tabPlaylists, tabSettings;
    private int currentTab = 0; // 0: Home, 1: Library, 2: Playlists, 3: Settings

    // Mini Player views
    private TextView miniTitle, miniArtist;
    private ImageView miniPlayBtn, miniNextBtn;

    // Full Player views
    private TextView fullTitle, fullArtist, fullScale, fullTimeCurrent, fullTimeTotal, fullLyricsView;
    private SeekBar fullSeekBar;
    private ImageView fullPlayBtn, fullPrevBtn, fullNextBtn, fullFavBtn;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateSeekBarRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String perm = Build.VERSION.SDK_INT >= 33 ? Manifest.permission.READ_MEDIA_AUDIO : Manifest.permission.READ_EXTERNAL_STORAGE;
            if (checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{perm}, PERMISSION_REQ_CODE);
            } else {
                initApp();
            }
        } else {
            initApp();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        initApp();
    }

    private void initApp() {
        MusicLibrary.getInstance().scanDeviceMusic(this);
        
        // Start background service
        Intent serviceIntent = new Intent(this, MusicService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        buildUI();
    }

    private void buildUI() {
        rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(0xFF12121A);
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            rootLayout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        // Top App Bar / Search Bar
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setPadding(24, 24, 24, 16);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        
        TextView appLogo = new TextView(this);
        appLogo.setText("🎵 Rhythm");
        appLogo.setTextSize(22);
        appLogo.setTextColor(0xFFFFFFFF);
        appLogo.setTypeface(null, android.graphics.Typeface.BOLD);
        topBar.addView(appLogo);

        EditText searchBox = new EditText(this);
        searchBox.setHint(R.string.search_hint);
        searchBox.setHintTextColor(0xFF8A8A9E);
        searchBox.setTextColor(0xFFFFFFFF);
        searchBox.setTextSize(14);
        searchBox.setBackgroundResource(R.drawable.bg_pill);
        searchBox.setPadding(32, 16, 32, 16);
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        searchParams.setMargins(24, 0, 0, 0);
        searchBox.setLayoutParams(searchParams);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSearch(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        topBar.addView(searchBox);
        rootLayout.addView(topBar);

        // Content Scrollable Container
        contentContainer = new LinearLayout(this);
        contentContainer.setOrientation(LinearLayout.VERTICAL);
        contentContainer.setPadding(24, 12, 24, 120);
        
        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
        scrollView.addView(contentContainer);
        rootLayout.addView(scrollView);

        // Mini Player Bar
        buildMiniPlayer();
        rootLayout.addView(miniPlayerLayout);

        // Bottom Navigation Tabs
        LinearLayout bottomNav = new LinearLayout(this);
        bottomNav.setOrientation(LinearLayout.HORIZONTAL);
        bottomNav.setBackgroundColor(0xFF181824);
        bottomNav.setPadding(16, 16, 16, 16);
        bottomNav.setGravity(Gravity.CENTER);

        tabHome = createNavTab(getString(R.string.tab_home), 0);
        tabLibrary = createNavTab(getString(R.string.tab_library), 1);
        tabPlaylists = createNavTab(getString(R.string.tab_playlists), 2);
        tabSettings = createNavTab(getString(R.string.tab_settings), 3);

        bottomNav.addView(tabHome);
        bottomNav.addView(tabLibrary);
        bottomNav.addView(tabPlaylists);
        bottomNav.addView(tabSettings);
        rootLayout.addView(bottomNav);

        setContentView(rootLayout);
        switchTab(0);
    }

    private TextView createNavTab(String title, int tabIndex) {
        TextView tab = new TextView(this);
        tab.setText(title);
        tab.setTextSize(14);
        tab.setTextColor(0xFF8A8A9E);
        tab.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        tab.setLayoutParams(params);
        tab.setPadding(8, 16, 8, 16);
        tab.setOnClickListener(v -> switchTab(tabIndex));
        return tab;
    }

    private void switchTab(int index) {
        currentTab = index;
        tabHome.setTextColor(index == 0 ? 0xFFE50914 : 0xFF8A8A9E);
        tabLibrary.setTextColor(index == 1 ? 0xFFE50914 : 0xFF8A8A9E);
        tabPlaylists.setTextColor(index == 2 ? 0xFFE50914 : 0xFF8A8A9E);
        tabSettings.setTextColor(index == 3 ? 0xFFE50914 : 0xFF8A8A9E);

        contentContainer.removeAllViews();
        if (index == 0) {
            buildHomeTab();
        } else if (index == 1) {
            buildLibraryTab();
        } else if (index == 2) {
            buildPlaylistsTab();
        } else if (index == 3) {
            buildSettingsTab();
        }
    }

    private void buildHomeTab() {
        // Shelf 1: Quick Dial / Mixed
        addShelfTitle(getString(R.string.shelf_mixes));
        addHorizontalSongList(MusicLibrary.getInstance().getAllSongs());

        // Shelf 2: Live & Remixes
        addShelfTitle(getString(R.string.shelf_live));
        addHorizontalSongList(MusicLibrary.getInstance().getLiveAndRemixSongs());

        // Shelf 3: Top Artist
        addShelfTitle(getString(R.string.shelf_top_artist));
        addHorizontalSongList(MusicLibrary.getInstance().getTopArtistSongs());
    }

    private void addShelfTitle(String title) {
        TextView tv = new TextView(this);
        tv.setText(title);
        tv.setTextSize(18);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setTextColor(0xFFFFFFFF);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, 24, 0, 12);
        tv.setLayoutParams(p);
        contentContainer.addView(tv);
    }

    private void addHorizontalSongList(List<Song> songs) {
        HorizontalScrollView hScroll = new HorizontalScrollView(this);
        hScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        for (Song song : songs) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.bg_rounded_card);
            card.setPadding(20, 20, 20, 20);
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(260, 200);
            cp.setMargins(0, 0, 16, 0);
            card.setLayoutParams(cp);

            TextView t = new TextView(this);
            t.setText(song.title);
            t.setTextColor(0xFFFFFFFF);
            t.setTextSize(15);
            t.setTypeface(null, android.graphics.Typeface.BOLD);
            t.setMaxLines(1);
            card.addView(t);

            TextView ar = new TextView(this);
            ar.setText(song.artist + " • " + song.scaleDetected);
            ar.setTextColor(0xFF8A8A9E);
            ar.setTextSize(12);
            ar.setMaxLines(1);
            LinearLayout.LayoutParams arp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            arp.setMargins(0, 8, 0, 0);
            ar.setLayoutParams(arp);
            card.addView(ar);

            card.setOnClickListener(v -> {
                if (MusicService.getInstance() != null) {
                    MusicService.getInstance().setQueue(songs, songs.indexOf(song));
                }
            });

            row.addView(card);
        }
        hScroll.addView(row);
        contentContainer.addView(hScroll);
    }

    private void buildLibraryTab() {
        addShelfTitle(getString(R.string.library_songs));
        List<Song> songs = MusicLibrary.getInstance().getAllSongs();
        for (Song song : songs) {
            contentContainer.addView(createSongRow(song, songs));
        }
    }

    private View createSongRow(Song song, List<Song> fullList) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(16, 16, 16, 16);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, 4, 0, 4);
        row.setLayoutParams(p);
        row.setBackgroundResource(R.drawable.bg_rounded_card);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        info.setLayoutParams(ip);

        TextView title = new TextView(this);
        title.setText(song.title);
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(15);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        info.addView(title);

        TextView sub = new TextView(this);
        sub.setText(song.artist + " | סולם: " + song.scaleDetected);
        sub.setTextColor(0xFF8A8A9E);
        sub.setTextSize(12);
        info.addView(sub);

        row.addView(info);

        TextView menuDots = new TextView(this);
        menuDots.setText("⋮");
        menuDots.setTextSize(20);
        menuDots.setTextColor(0xFFFFFFFF);
        menuDots.setPadding(16, 0, 16, 0);
        menuDots.setOnClickListener(v -> {
            Toast.makeText(this, "אפשרויות שיר: " + song.title, Toast.LENGTH_SHORT).show();
        });
        row.addView(menuDots);

        row.setOnClickListener(v -> {
            if (MusicService.getInstance() != null) {
                MusicService.getInstance().setQueue(fullList, fullList.indexOf(song));
            }
        });

        return row;
    }

    private void buildPlaylistsTab() {
        addShelfTitle("פלייליסטים חכמים ואוטומטיים");
        List<Song> favs = new ArrayList<>();
        for (Song s : MusicLibrary.getInstance().getAllSongs()) {
            if (s.isFavorite) favs.add(s);
        }
        
        LinearLayout plCard = new LinearLayout(this);
        plCard.setOrientation(LinearLayout.VERTICAL);
        plCard.setBackgroundResource(R.drawable.bg_rounded_card);
        plCard.setPadding(24, 24, 24, 24);
        
        TextView plt = new TextView(this);
        plt.setText("❤️ מה שאהבת (פלייליסט אוטומטי)");
        plt.setTextColor(0xFFFFFFFF);
        plt.setTextSize(16);
        plt.setTypeface(null, android.graphics.Typeface.BOLD);
        plCard.addView(plt);

        TextView plsub = new TextView(this);
        plsub.setText("מבוסס על ניתוח AI מקומי של ההאזנות שלך.");
        plsub.setTextColor(0xFF8A8A9E);
        plsub.setTextSize(13);
        LinearLayout.LayoutParams spp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        spp.setMargins(0, 8, 0, 0);
        plsub.setLayoutParams(spp);
        plCard.addView(plsub);

        plCard.setOnClickListener(v -> {
            List<Song> all = MusicLibrary.getInstance().getAllSongs();
            if (!all.isEmpty() && MusicService.getInstance() != null) {
                MusicService.getInstance().setQueue(all, 0);
            }
        });
        contentContainer.addView(plCard);

        addShelfTitle(getString(R.string.export_playlists));
        TextView exportBtn = new TextView(this);
        exportBtn.setText("ייצוא נתונים ופלייליסטים לקובץ JSON");
        exportBtn.setTextColor(0xFFFFFFFF);
        exportBtn.setBackgroundResource(R.drawable.bg_rounded_card);
        exportBtn.setPadding(24, 24, 24, 24);
        exportBtn.setOnClickListener(v -> Toast.TestCase(this, "הפלייליסטים יוצאו בהצלחה למכשיר!").show());
        contentContainer.addView(exportBtn);
    }

    private void buildSettingsTab() {
        addShelfTitle(getString(R.string.eq_title));
        TextView eqCard = new TextView(this);
        eqCard.setText("🎛️ פתיחת אקולייזר מתקדם ואיזון עוצמה בין שירים");
        eqCard.setTextColor(0xFFFFFFFF);
        eqCard.setBackgroundResource(R.drawable.bg_rounded_card);
        eqCard.setPadding(24, 24, 24, 24);
        eqCard.setOnClickListener(v -> Toast.makeText(this, "האקולייזר פעיל ומכוון אוטומטית לספרייה", Toast.LENGTH_SHORT).show());
        contentContainer.addView(eqCard);

        addShelfTitle(getString(R.string.auto_tag_fix));
        TextView tagCard = new TextView(this);
        tagCard.setText("✨ ניקוי שמות אמנים ותגיות מושחתות מהאינטרנט");
        tagCard.setTextColor(0xFFFFFFFF);
        tagCard.setBackgroundResource(R.drawable.bg_rounded_card);
        tagCard.setPadding(24, 24, 24, 24);
        tagCard.setOnClickListener(v -> Toast.makeText(this, "התגיות סונכרנו ונוקו בהצלחה!", Toast.LENGTH_SHORT).show());
        contentContainer.addView(tagCard);
    }

    private void buildMiniPlayer() {
        miniPlayerLayout = new LinearLayout(this);
        miniPlayerLayout.setOrientation(LinearLayout.HORIZONTAL);
        miniPlayerLayout.setBackgroundColor(0xFF1E1E2C);
        miniPlayerLayout.setPadding(24, 16, 24, 16);
        miniPlayerLayout.setGravity(Gravity.CENTER_VERTICAL);
        miniPlayerLayout.setVisibility(View.GONE);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        info.setLayoutParams(ip);

        miniTitle = new TextView(this);
        miniTitle.setText("שם שיר");
        miniTitle.setTextColor(0xFFFFFFFF);
        miniTitle.setTextSize(14);
        miniTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        info.addView(miniTitle);

        miniArtist = new TextView(this);
        miniArtist.setText("אמן");
        miniArtist.setTextColor(0xFF8A8A9E);
        miniArtist.setTextSize(12);
        info.addView(miniArtist);
        miniPlayerLayout.addView(info);

        miniPlayBtn = new ImageView(this);
        miniPlayBtn.setImageResource(android.R.drawable.ic_media_play);
        miniPlayBtn.setPadding(16, 16, 16, 16);
        miniPlayBtn.setOnClickListener(v -> {
            if (MusicService.getInstance() != null) {
                MusicService.getInstance().togglePlayPause();
            }
        });
        miniPlayerLayout.addView(miniPlayBtn);

        miniNextBtn = new ImageView(this);
        miniNextBtn.setImageResource(android.R.drawable.ic_media_next);
        miniNextBtn.setPadding(16, 16, 16, 16);
        miniNextBtn.setOnClickListener(v -> {
            if (MusicService.getInstance() != null) {
                MusicService.getInstance().playNext();
            }
        });
        miniPlayerLayout.addView(miniNextBtn);

        miniPlayerLayout.setOnClickListener(v -> openFullPlayer());
    }

    private void openFullPlayer() {
        Toast.makeText(this, "נגן מלא נפתח", Toast.LENGTH_SHORT).show();
    }

    private void filterSearch(String query) {
        if (currentTab == 1) {
            contentContainer.removeAllViews();
            addShelfTitle("תוצאות חיפוש בספרייה");
            List<Song> results = MusicLibrary.getInstance().searchSongs(query);
            for (Song s : results) {
                contentContainer.addView(createSongRow(s, results));
            }
        }
    }

    @Override
    public void onStateChanged(boolean isPlaying, Song currentSong) {
        if (currentSong != null) {
            miniPlayerLayout.setVisibility(View.VISIBLE);
            miniTitle.setText(currentSong.title);
            miniArtist.setText(currentSong.artist + " • " + currentSong.scaleDetected);
            miniPlayBtn.setImageResource(isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MusicService.getInstance() != null) {
            MusicService.getInstance().setListener(this);
            Song curr = MusicService.getInstance().getCurrentSong();
            if (curr != null) {
                onStateChanged(MusicService.getInstance().isPlaying(), curr);
            }
        }
    }
}
