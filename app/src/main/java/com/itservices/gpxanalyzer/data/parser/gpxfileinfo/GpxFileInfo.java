package com.itservices.gpxanalyzer.data.parser.gpxfileinfo;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class GpxFileInfo {
    private final long id;
    private final File file;
    private final String creator;
    private final String authorName;
    private final String firstPointLat;
    private final String firstPointLon;
    private final String firstPointEle;
    private final long firstPointTimeMillis;
    private AtomicReference<Bitmap> miniatureBitmap = new AtomicReference<>();
    private final long fileSize;
    private final Date lastFileModified;

    public GpxFileInfo(
            long id, File file,
            String creator,
            String authorName,
            String firstPointLat,
            String firstPointLon,
            String firstPointEle,
            long firstPointTimeMillis,
            long fileSize,
            Date lastFileModified
    ) {
        this.id = id;
        this.file = file;
        this.creator = creator;
        this.authorName = authorName;
        this.firstPointLat = firstPointLat;
        this.firstPointLon = firstPointLon;
        this.firstPointEle = firstPointEle;
        this.firstPointTimeMillis = firstPointTimeMillis;
        this.fileSize = fileSize;
        this.lastFileModified = lastFileModified;
    }

    public GpxFileInfo(long id, File file, String creator, String authorName,
                       String firstPointLat, String firstPointLon,
                       String firstPointEle, long firstPointTimeMillis) {
        this(id, file, creator, authorName, firstPointLat, firstPointLon,
                firstPointEle, firstPointTimeMillis,
                file.length(), new Date(file.lastModified()));
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

    public String firstPointLat() {
        return firstPointLat;
    }

    public String firstPointLon() {
        return firstPointLon;
    }

    public String firstPointEle() {
        return firstPointEle;
    }

    public long firstPointTimeMillis() {
        return firstPointTimeMillis;
    }

    public AtomicReference<Bitmap> miniatureBitmap() {
        return miniatureBitmap;
    }

    public void setMiniatureBitmap(Bitmap miniatureBitmap) {
        //Log.d(GpxFileInfo.class.getSimpleName(), "setMiniatureBitmap() called with: miniatureBitmap = [" + miniatureBitmap + "]");

        this.miniatureBitmap.set(miniatureBitmap);
    }

    public long fileSize() {
        return fileSize;
    }

    public Date lastFileModified() {
        return lastFileModified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GpxFileInfo that)) return false;
        return getId() == that.getId() && firstPointTimeMillis == that.firstPointTimeMillis && fileSize == that.fileSize && Objects.equals(file, that.file) && Objects.equals(creator, that.creator) && Objects.equals(authorName, that.authorName) && Objects.equals(firstPointLat, that.firstPointLat) && Objects.equals(firstPointLon, that.firstPointLon) && Objects.equals(firstPointEle, that.firstPointEle) && Objects.equals(miniatureBitmap, that.miniatureBitmap) && Objects.equals(lastFileModified, that.lastFileModified);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), file, creator, authorName, firstPointLat, firstPointLon, firstPointEle, firstPointTimeMillis, miniatureBitmap, fileSize, lastFileModified);
    }

    @Override
    public String toString() {
        return "GpxFileInfo{" +
                "id=" + id +
                ", file=" + file +
                ", creator='" + creator + '\'' +
                ", authorName='" + authorName + '\'' +
                ", firstPointLat='" + firstPointLat + '\'' +
                ", firstPointLon='" + firstPointLon + '\'' +
                ", firstPointEle='" + firstPointEle + '\'' +
                ", firstPointTimeMillis=" + firstPointTimeMillis +
                ", miniatureBitmap=" + miniatureBitmap +
                ", fileSize=" + fileSize +
                ", lastFileModified=" + lastFileModified +
                '}';
    }
}
