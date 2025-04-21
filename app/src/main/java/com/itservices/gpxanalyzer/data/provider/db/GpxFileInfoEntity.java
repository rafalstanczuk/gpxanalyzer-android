package com.itservices.gpxanalyzer.data.provider.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;
import java.util.Objects;


@Entity(tableName = "gpx_files")
@TypeConverters(Converters.class)
public final class GpxFileInfoEntity {
    @PrimaryKey(autoGenerate = true)
    long id;
    final String fileAbsolutePathBase64;
    final String creator;
    final String authorName;
    final String firstPointLat;
    final String firstPointLon;
    final String firstPointEle;
    final long firstPointTimeMillis;
    final String miniatureBitmapBase64;
    final long fileSize;
    final Date lastFileModified;

    public GpxFileInfoEntity(
            String fileAbsolutePathBase64,
            String creator,
            String authorName,
            String firstPointLat,
            String firstPointLon,
            String firstPointEle,
            long firstPointTimeMillis,
            String miniatureBitmapBase64,
            long fileSize,
            Date lastFileModified
    ) {
        this.fileAbsolutePathBase64 = fileAbsolutePathBase64;
        this.creator = creator;
        this.authorName = authorName;
        this.firstPointLat = firstPointLat;
        this.firstPointLon = firstPointLon;
        this.firstPointEle = firstPointEle;
        this.firstPointTimeMillis = firstPointTimeMillis;
        this.miniatureBitmapBase64 = miniatureBitmapBase64;
        this.fileSize = fileSize;
        this.lastFileModified = lastFileModified;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String fileAbsolutePathBase64() {
        return fileAbsolutePathBase64;
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

    public String miniatureBitmapBase64() {
        return miniatureBitmapBase64;
    }

    public long fileSize() {
        return fileSize;
    }

    public Date lastFileModified() {
        return lastFileModified;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (GpxFileInfoEntity) obj;
        return Objects.equals(this.fileAbsolutePathBase64, that.fileAbsolutePathBase64) &&
                Objects.equals(this.creator, that.creator) &&
                Objects.equals(this.authorName, that.authorName) &&
                Objects.equals(this.firstPointLat, that.firstPointLat) &&
                Objects.equals(this.firstPointLon, that.firstPointLon) &&
                Objects.equals(this.firstPointEle, that.firstPointEle) &&
                this.firstPointTimeMillis == that.firstPointTimeMillis &&
                Objects.equals(this.miniatureBitmapBase64, that.miniatureBitmapBase64) &&
                this.fileSize == that.fileSize &&
                Objects.equals(this.lastFileModified, that.lastFileModified);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileAbsolutePathBase64, creator, authorName, firstPointLat, firstPointLon, firstPointEle, firstPointTimeMillis, miniatureBitmapBase64, fileSize, lastFileModified);
    }

    @Override
    public String toString() {
        return "GpxFileInfo[" +
                "fileAbsolutePathBase64=" + fileAbsolutePathBase64 + ", " +
                "creator=" + creator + ", " +
                "authorName=" + authorName + ", " +
                "firstPointLat=" + firstPointLat + ", " +
                "firstPointLon=" + firstPointLon + ", " +
                "firstPointEle=" + firstPointEle + ", " +
                "firstPointTimeMillis=" + firstPointTimeMillis + ", " +
                "miniatureBitmapBase64=" + miniatureBitmapBase64 + ", " +
                "fileSize=" + fileSize + ", " +
                "lastFileModified=" + lastFileModified + ']';
    }
}