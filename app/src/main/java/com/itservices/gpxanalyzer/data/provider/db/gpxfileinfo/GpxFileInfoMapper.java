package com.itservices.gpxanalyzer.data.provider.db.gpxfileinfo;

import android.graphics.Bitmap;

import com.itservices.gpxanalyzer.data.model.geocoding.GeocodingResult;
import com.itservices.gpxanalyzer.data.model.gpxfileinfo.GpxFileInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper class to convert between GpxFileInfo and GpxFileInfoEntity objects
 */
public final class GpxFileInfoMapper {

    /**
     * Maps a GpxFileInfo to a GpxFileInfoEntity
     * 
     * @param gpxFileInfo The source GpxFileInfo object
     * @return A GpxFileInfoEntity object
     */
    public static GpxFileInfoEntity toEntity(GpxFileInfo gpxFileInfo) {
        if (gpxFileInfo == null) {
            return null;
        }
        
        // Get bitmap from AtomicReference and convert to byte array using Converters
        Bitmap bitmap = gpxFileInfo.miniatureBitmap().get();
        String bitmapBytesBase64 = Converters.fromBitmap(bitmap);

        GpxFileInfoEntity gpxFileInfoEntity = new GpxFileInfoEntity(
                Converters.fromFile(gpxFileInfo.file()),
                gpxFileInfo.creator(),
                gpxFileInfo.authorName(),
                gpxFileInfo.firstPointLocation(),
                gpxFileInfo.geoCodedLocation(),
                bitmapBytesBase64
        );

        gpxFileInfoEntity.setId(gpxFileInfo.getId());
        
        return gpxFileInfoEntity;
    }
    
    /**
     * Maps a GpxFileInfoEntity to a GpxFileInfo
     * 
     * @param entity The source GpxFileInfoEntity object
     * @return A GpxFileInfo object
     */
    public static GpxFileInfo fromEntity(GpxFileInfoEntity entity) {
        if (entity == null) {
            return null;
        }
        
        GpxFileInfo gpxFileInfo = new GpxFileInfo(
                entity.getId(),
                Converters.toFile(entity.fileAbsolutePathBase64()),
                entity.creator(),
                entity.authorName(),
                entity.firstPointLocation(),
                entity.geoCodedLocation()
        );
        
        // Convert byte array to Bitmap using Converters and set it to the GpxFileInfo
        Bitmap bitmap = Converters.toBitmap(entity.miniatureBitmapBase64());
        if (bitmap != null) {
            gpxFileInfo.setMiniatureBitmap(bitmap);
        }
        
        return gpxFileInfo;
    }
    
    /**
     * Maps a list of GpxFileInfo objects to a list of GpxFileInfoEntity objects
     * 
     * @param gpxFileInfoList The source list of GpxFileInfo objects
     * @return A list of GpxFileInfoEntity objects
     */
    public static List<GpxFileInfoEntity> toEntityList(List<GpxFileInfo> gpxFileInfoList) {
        if (gpxFileInfoList == null) {
            return null;
        }
        
        List<GpxFileInfoEntity> entityList = new ArrayList<>(gpxFileInfoList.size());
        for (GpxFileInfo gpxFileInfo : gpxFileInfoList) {
            entityList.add(toEntity(gpxFileInfo));
        }
        
        return entityList;
    }
    
    /**
     * Maps a list of GpxFileInfoEntity objects to a list of GpxFileInfo objects
     * 
     * @param entityList The source list of GpxFileInfoEntity objects
     * @return A list of GpxFileInfo objects
     */
    public static List<GpxFileInfo> fromEntityList(List<GpxFileInfoEntity> entityList) {
        if (entityList == null) {
            return null;
        }
        
        List<GpxFileInfo> gpxFileInfoList = new ArrayList<>(entityList.size());
        for (GpxFileInfoEntity entity : entityList) {
            gpxFileInfoList.add(fromEntity(entity));
        }
        
        return gpxFileInfoList;
    }
} 