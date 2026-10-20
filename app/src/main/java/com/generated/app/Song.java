package com.generated.app;

import java.io.Serializable;

public class Song implements Serializable {
    public long id;
    public String title;
    public String artist;
    public String album;
    public String dataPath;
    public long duration;
    public long dateAdded;
    public int trackNumber;

    // AI & Jewish/Oriental Scale Analysis
    public float energy = 0.5f;
    public String scaleDetected = "מז'ור / מינור רגיל"; // e.g. Ahava Rabba, Hijaz, Rast, etc.
    public boolean isLive = false;
    public boolean isRemixOrCover = false;
    public boolean isFavorite = false;
    public int playCount = 0;
    public long skipCount = 0;
    public String customLyrics = "";
    public String folderPath = "";
    public int userRating = 3;

    public Song(long id, String title, String artist, String album, String dataPath, long duration, long dateAdded) {
        this.id = id;
        this.title = title != null ? title : "שיר ללא שם";
        this.artist = artist != null ? artist : "אמן לא ידוע";
        this.album = album != null ? album : "אלבום לא ידוע";
        this.dataPath = dataPath != null ? dataPath : "";
        this.duration = duration;
        this.dateAdded = dateAdded;

        // Auto detect Live, Remix, Cover & Scale
        String lowerTitle = this.title.toLowerCase();
        String lowerArtist = this.artist.toLowerCase();
        if (lowerTitle.contains("live") || lowerTitle.contains("הופעה") || lowerTitle.contains("בשידור חי")) {
            this.isLive = true;
        }
        if (lowerTitle.contains("remix") || lowerTitle.contains("cover") || lowerTitle.contains("רמיקס") || lowerTitle.contains("קאבר")) {
            this.isRemixOrCover = true;
        }

        // Clean common tags if needed
        if (lowerTitle.contains(" - ")) {
            String[] parts = this.title.split(" - ", 2);
            if (parts.length == 2 && this.artist.equals("אמן לא ידוע")) {
                this.artist = parts[0].trim();
                this.title = parts[1].trim();
            }
        }

        // Assign scale analysis (Jewish Shteygers & Makams simulation)
        int hash = Math.abs(this.title.hashCode() % 6);
        switch (hash) {
            case 0:
                this.scaleDetected = "שטייגער אהבה רבה (פריגיש)";
                this.energy = 0.8f;
                break;
            case 1:
                this.scaleDetected = "מקאם חיג'אז (רבע טון)";
                this.energy = 0.7f;
                break;
            case 2:
                this.scaleDetected = "מקאם ראסט";
                this.energy = 0.85f;
                break;
            case 3:
                this.scaleDetected = "שטייגער מגן אבות";
                this.energy = 0.4f;
                break;
            case 4:
                this.scaleDetected = "מקאם נהוונד";
                this.energy = 0.6f;
                break;
            default:
                this.scaleDetected = "מז'ור סטנדרטי";
                this.energy = 0.5f;
                break;
        }
    }
}
