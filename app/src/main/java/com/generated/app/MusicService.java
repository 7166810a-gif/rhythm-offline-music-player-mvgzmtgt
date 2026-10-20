package com.generated.app;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service {
    private final IBinder binder = new LocalBinder();
    private MediaPlayer mediaPlayer;
    private List<Song> currentQueue = new ArrayList<>();
    private int currentIndex = 0;
    private boolean isPlaying = false;
    private OnPlayerStateChangedListener stateListener;
    private boolean pauseOnZeroVolume = false;

    public interface OnPlayerStateChangedListener {
        void onStateChanged(boolean isPlaying, Song currentSong);
    }

    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.getInstance();
        }
    }

    private static MusicService instance;

    public static MusicService getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mp -> playNext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setQueue(List<Song> queue, int startIndex) {
        if (queue == null || queue.isEmpty()) return;
        this.currentQueue = new ArrayList<>(queue);
        this.currentIndex = Math.max(0, Math.min(startIndex, queue.size() - 1));
        playCurrentSong();
    }

    public void playCurrentSong() {
        if (currentQueue.isEmpty()) return;
        Song song = currentQueue.get(currentIndex);
        try {
            mediaPlayer.reset();
            if (song.dataPath.startsWith("/sdcard/") || song.dataPath.startsWith("/")) {
                // Check if file exists, else use raw or uri fallback
                mediaPlayer.setDataSource(song.dataPath);
            } else {
                mediaPlayer.setDataSource(this, Uri.parse(song.dataPath));
            }
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;
            song.playCount++;
            if (stateListener != null) {
                stateListener.onStateChanged(true, song);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Skip to next if unplayable
            playNext();
        }
    }

    public void togglePlayPause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
        } else {
            mediaPlayer.start();
            isPlaying = true;
        }
        if (stateListener != null && !currentQueue.isEmpty()) {
            stateListener.onStateChanged(isPlaying, currentQueue.get(currentIndex));
        }
    }

    public void playNext() {
        if (currentQueue.isEmpty()) return;
        currentIndex = (currentIndex + 1) % currentQueue.size();
        playCurrentSong();
    }

    public void playPrev() {
        if (currentQueue.isEmpty()) return;
        currentIndex = (currentIndex - 1 + currentQueue.size()) % currentQueue.size();
        playCurrentSong();
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public Song getCurrentSong() {
        if (currentQueue.isEmpty() || currentIndex < 0 || currentIndex >= currentQueue.size()) {
            return null;
        }
        return currentQueue.get(currentIndex);
    }

    public List<Song> getCurrentQueue() {
        return currentQueue;
    }

    public void setListener(OnPlayerStateChangedListener listener) {
        this.stateListener = listener;
    }

    public void seekTo(int progressMs) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(progressMs);
        }
    }

    public int getCurrentPosition() {
        try {
            return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public int getDuration() {
        try {
            return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        instance = null;
    }
}
