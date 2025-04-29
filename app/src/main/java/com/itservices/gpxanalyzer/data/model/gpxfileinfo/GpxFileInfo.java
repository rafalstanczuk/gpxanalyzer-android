package com.itservices.gpxanalyzer.data.model.gpxfileinfo;

import android.graphics.Bitmap;
import android.location.Location;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class GpxFileInfo {
    private final long id;
    private final File file;
    private final String creator;
    private final String authorName;
    private final Location firstPointLocation;
    private String geoCodedLocation;
    private final AtomicReference<Bitmap> miniatureBitmap = new AtomicReference<>();

    public GpxFileInfo(
            long id, File file,
            String creator,
            String authorName,
            Location firstPointLocation, String geoCodedLocation
    ) {
        this.id = id;
        this.file = file;
        this.creator = creator;
        this.authorName = authorName;
        this.firstPointLocation = firstPointLocation;
        this.geoCodedLocation = geoCodedLocation;
    }

    public long getId() {
        return id;
    }

    public File file() {
        return file;
    }

    public String creator() {
        return creator;
    }

    public String authorName() {
        return authorName;
    }

    public Location firstPointLocation() {
        return firstPointLocation;
    }

    public String geoCodedLocation() {
        return geoCodedLocation;
    }

    public AtomicReference<Bitmap> miniatureBitmap() {
        return miniatureBitmap;
    }

    public void setMiniatureBitmap(Bitmap miniatureBitmap) {
        this.miniatureBitmap.set(miniatureBitmap);
    }

    public void setGeoCodedLocation(String geoCodedLocation) {
        this.geoCodedLocation = geoCodedLocation;
    }

    public long fileSize() {
        return file.length();
    }

    public long lastFileModified() {
        return file.lastModified();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GpxFileInfo that)) return false;
        return getId() == that.getId() && Objects.equals(file, that.file) && Objects.equals(creator, that.creator) && Objects.equals(authorName, that.authorName) && Objects.equals(firstPointLocation, that.firstPointLocation) && Objects.equals(geoCodedLocation, that.geoCodedLocation) && Objects.equals(miniatureBitmap, that.miniatureBitmap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), file, creator, authorName, firstPointLocation, geoCodedLocation, miniatureBitmap);
    }

    @Override
    public String toString() {
        return "GpxFileInfo{" +
                "id=" + id +
                ", file=" + file +
                ", creator='" + creator + '\'' +
                ", authorName='" + authorName + '\'' +
                ", firstPointLocation=" + firstPointLocation +
                ", geoCodedLocation='" + geoCodedLocation + '\'' +
                ", miniatureBitmap=" + miniatureBitmap +
                '}';
    }
}
