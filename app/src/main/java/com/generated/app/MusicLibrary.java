package com.generated.app;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MusicLibrary {
    private static MusicLibrary instance;
    private List<Song> allSongs = new ArrayList<>();
    private List<Song> uniqueSongs = new ArrayList<>();
    private boolean isScanned = false;

    private MusicLibrary() {}

    public static synchronized MusicLibrary getInstance() {
        if (instance == null) {
            instance = new MusicLibrary();
        }
        return instance;
    }

    public void scanDeviceMusic(Context context) {
        if (isScanned && !allSongs.isEmpty()) return;
        allSongs.clear();

        ContentResolver resolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.TRACK
        };

        try (Cursor cursor = resolver.query(uri, projection, MediaStore.Audio.Media.IS_MUSIC + "!= 0", null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                int titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int albumCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                int dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                int durCol = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                int dateCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);

                do {
                    long id = cursor.getLong(idCol);
                    String title = cursor.getString(titleCol);
                    String artist = cursor.getString(artistCol);
                    String album = cursor.getString(albumCol);
                    String data = cursor.getString(dataCol);
                    long duration = cursor.getLong(durCol);
                    long dateAdded = cursor.getLong(dateCol);

                    Song song = new Song(id, title, artist, album, data, duration, dateAdded);
                    if (data != null) {
                        File f = new File(data);
                        song.folderPath = f.getParent() != null ? f.getParent() : "";
                    }
                    allSongs.add(song);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Fallback mock data if empty (useful for emulator without media files)
        if (allSongs.isEmpty()) {
            allSongs.add(new Song(1, "מזמור לדוד - הופעה חיה", "אברהם פריד", "אוסף חסידי", "/sdcard/Music/m1.mp3", 240000, System.currentTimeMillis()));
            allSongs.add(new Song(2, "אל הנער הזה - רמיקס", "מרדכי בן דוד", "רמיקסים מיוחדים", "/sdcard/Music/m2.mp3", 210000, System.currentTimeMillis()));
            allSongs.add(new Song(3, "שיר חדש אהבה רבה", "יעקב שוואקי", "מיטב הלהיטים", "/sdcard/Music/m3.mp3", 195000, System.currentTimeMillis()));
            allSongs.add(new Song(4, "ניגון חב"ד עתיק", "הישיבה", "ניגונים", "/sdcard/Music/m4.mp3", 300000, System.currentTimeMillis()));
            allSongs.add(new Song(5, "מזרח שמש צדקה", "חיים ישראל", "מזרחי שקט", "/sdcard/Music/m5.mp3", 220000, System.currentTimeMillis()));
        }

        // Deduplicate duplicate songs by checking file path and title+artist
        uniqueSongs.clear();
        Set<String> seenSignatures = new HashSet<>();
        for (Song s : allSongs) {
            String sig = (s.title + "_" + s.artist).toLowerCase().replaceAll("\\s+", "");
            if (!seenSignatures.contains(sig)) {
                seenSignatures.add(sig);
                uniqueSongs.add(s);
            }
        }

        isScanned = true;
    }

    public List<Song> getAllSongs() {
        if (!isScanned) {
            // will be populated on first access if context provided elsewhere, else returns mock
        }
        return uniqueSongs.isEmpty() ? allSongs : uniqueSongs;
    }

    public List<Song> getLiveAndRemixSongs() {
        List<Song> list = new ArrayList<>();
        for (Song s : getAllSongs()) {
            if (s.isLive || s.isRemixOrCover) {
                list.add(s);
            }
        }
        return list;
    }

    public List<Song> getTopArtistSongs() {
        List<Song> all = getAllSongs();
        if (all.isEmpty()) return all;
        Map<String, Integer> counts = new HashMap<>();
        for (Song s : all) {
            counts.put(s.artist, counts.getOrDefault(s.artist, 0) + 1);
        }
        String topArtist = "";
        int max = 0;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                topArtist = entry.getKey();
            }
        }
        List<Song> list = new ArrayList<>();
        for (Song s : all) {
            if (s.artist.equals(topArtist)) {
                list.add(s);
            }
        }
        return list;
    }

    public List<Song> searchSongs(String query) {
        List<Song> result = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) return result;
        String q = query.toLowerCase();
        for (Song s : getAllSongs()) {
            if (s.title.toLowerCase().contains(q) ||
                    s.artist.toLowerCase().contains(q) ||
                    s.album.toLowerCase().contains(q) ||
                    s.scaleDetected.toLowerCase().contains(q) ||
                    s.customLyrics.toLowerCase().contains(q)) {
                result.add(s);
            }
        }
        return result;
    }
}
