package com.itservices.gpxanalyzer.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import java.io.File;
import java.util.Date;

@Entity(tableName = "gpx_files")
@TypeConverters(Converters.class)
public class GpxFileInfoEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private File file;
    private String creator;
    private String authorName;
    private String firstPointLat;
    private String firstPointLon;
    private String firstPointEle;
    private String firstPointTime;
    private long fileSize;
    private Date lastFileModified;

    public GpxFileInfoEntity(File file, String creator, String authorName, 
                           String firstPointLat, String firstPointLon, 
                           String firstPointEle, String firstPointTime) {
        this.file = file;
        this.creator = creator;
        this.authorName = authorName;
        this.firstPointLat = firstPointLat;
        this.firstPointLon = firstPointLon;
        this.firstPointEle = firstPointEle;
        this.firstPointTime = firstPointTime;
        this.fileSize = file.length();
        this.lastFileModified = new Date(file.lastModified());
    }

    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public File getFile() { return file; }
    public void setFile(File file) { this.file = file; }
    
    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }
    
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    
    public String getFirstPointLat() { return firstPointLat; }
    public void setFirstPointLat(String firstPointLat) { this.firstPointLat = firstPointLat; }
    
    public String getFirstPointLon() { return firstPointLon; }
    public void setFirstPointLon(String firstPointLon) { this.firstPointLon = firstPointLon; }
    
    public String getFirstPointEle() { return firstPointEle; }
    public void setFirstPointEle(String firstPointEle) { this.firstPointEle = firstPointEle; }
    
    public String getFirstPointTime() { return firstPointTime; }
    public void setFirstPointTime(String firstPointTime) { this.firstPointTime = firstPointTime; }
    
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    
    public Date getLastFileModified() { return lastFileModified; }
    public void setLastFileModified(Date lastFileModified) { this.lastFileModified = lastFileModified; }
} 