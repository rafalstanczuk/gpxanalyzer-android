package com.itservices.gpxanalyzer.data.provider.db.gpxfileinfo;

import android.location.Location;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Objects;

@Entity(tableName = "gpx_files")
@TypeConverters(Converters.class)
public final class GpxFileInfoEntity {
    @PrimaryKey(autoGenerate = true)
    long id;
    final String fileAbsolutePathBase64;
    final String creator;
    final String authorName;
    final Location firstPointLocation;
    final String geoCodedLocation;
    final String miniatureBitmapBase64;

    public GpxFileInfoEntity(
            String fileAbsolutePathBase64,
            String creator,
            String authorName,
            Location firstPointLocation,
            String geoCodedLocation,
            String miniatureBitmapBase64
    ) {
        this.fileAbsolutePathBase64 = fileAbsolutePathBase64;
        this.creator = creator;
        this.authorName = authorName;
        this.firstPointLocation = firstPointLocation;
        this.geoCodedLocation = geoCodedLocation;
        this.miniatureBitmapBase64 = miniatureBitmapBase64;
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

    public Location firstPointLocation() {
        return firstPointLocation;
    }

    public String geoCodedLocation() {
        return geoCodedLocation;
    }

    public String miniatureBitmapBase64() {
        return miniatureBitmapBase64;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (GpxFileInfoEntity) obj;
        return Objects.equals(this.fileAbsolutePathBase64, that.fileAbsolutePathBase64) &&
                Objects.equals(this.creator, that.creator) &&
                Objects.equals(this.authorName, that.authorName) &&
                Objects.equals(this.firstPointLocation, that.firstPointLocation) &&
                Objects.equals(this.miniatureBitmapBase64, that.miniatureBitmapBase64);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileAbsolutePathBase64, creator, authorName, firstPointLocation, miniatureBitmapBase64);
    }

    @Override
    public String toString() {
        return "GpxFileInfo[" +
                "fileAbsolutePathBase64=" + fileAbsolutePathBase64 + ", " +
                "creator=" + creator + ", " +
                "authorName=" + authorName + ", " +
                "firstPointLocation=" + firstPointLocation + ", " +
                "miniatureBitmapBase64=" + miniatureBitmapBase64 + ']';
    }
}