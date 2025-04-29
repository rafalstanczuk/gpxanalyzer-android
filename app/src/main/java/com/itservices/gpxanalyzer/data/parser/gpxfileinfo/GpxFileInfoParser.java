package com.itservices.gpxanalyzer.data.parser.gpxfileinfo;

import android.location.Location;
import android.util.Log;

import com.itservices.gpxanalyzer.data.model.gpxfileinfo.GpxFileInfo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

@Singleton
public class GpxFileInfoParser {
    private static final String TAG = GpxFileInfoParser.class.getSimpleName();
    
    // Default and error values
    private static final String DEFAULT_CREATOR = "Unknown";
    private static final String DEFAULT_AUTHOR = "Unknown";
    private static final String ERROR_CREATOR = "Error";
    private static final String ERROR_AUTHOR = "Error";
    private static final String N_A = "N/A";
    private static final String GPX_LOCATION_PROVIDER = "gpx";
    private static final String ELEVATION_DEFAULT = "0";
    
    // XML tags and attributes
    private static final String TAG_CREATOR = "creator";
    private static final String TAG_AUTHOR = "author";
    private static final String TAG_NAME = "name";
    private static final String TAG_TRACK_SEGMENT = "trkseg";
    private static final String TAG_TRACK_POINT = "trkpt";
    private static final String ATTR_LATITUDE = "lat";
    private static final String ATTR_LONGITUDE = "lon";
    private static final String TAG_ELEVATION = "ele";
    private static final String TAG_TIME = "time";
    
    // Error messages
    private static final String ERROR_PARSING_FILE = "Error parsing GPX file: ";
    private static final String ERROR_PARSING_TIME = "Error parsing time: ";
    
    // Date format
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final SimpleDateFormat GPX_POINT_DATETIME_FORMAT = 
            new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

    @Inject
    public GpxFileInfoParser() {
    }

    public GpxFileInfo parse(File file) {
        try {
            Document document = createDocument(file);
            String creator = extractCreator(document);
            String authorName = extractAuthorName(document);
            Location firstPointLocation = extractFirstPointLocation(document);
            
            return new GpxFileInfo(0, file, creator, authorName, firstPointLocation, "");
        } catch (Exception e) {
            Log.e(TAG, ERROR_PARSING_FILE + file.getName(), e);
            return new GpxFileInfo(0, file, ERROR_CREATOR, ERROR_AUTHOR, null, "");
        }
    }

    private Document createDocument(File file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(file);
    }

    private String extractCreator(Document document) {
        String creator = document.getDocumentElement().getAttribute(TAG_CREATOR);
        return creator.isEmpty() ? DEFAULT_CREATOR : creator;
    }

    private String extractAuthorName(Document document) {
        NodeList authorNodes = document.getElementsByTagName(TAG_AUTHOR);
        if (authorNodes.getLength() > 0) {
            NodeList nameNodes = ((Element) authorNodes.item(0)).getElementsByTagName(TAG_NAME);
            if (nameNodes.getLength() > 0) {
                return nameNodes.item(0).getTextContent();
            }
        }
        return DEFAULT_AUTHOR;
    }

    private Location extractFirstPointLocation(Document document) {
        NodeList trksegNodes = document.getElementsByTagName(TAG_TRACK_SEGMENT);
        if (trksegNodes.getLength() > 0) {
            NodeList trkptNodes = ((Element) trksegNodes.item(0)).getElementsByTagName(TAG_TRACK_POINT);
            if (trkptNodes.getLength() > 0) {
                return createLocationFromPoint((Element) trkptNodes.item(0));
            }
        }
        return null;
    }

    private Location createLocationFromPoint(Element point) {
        String latStr = point.getAttribute(ATTR_LATITUDE);
        String lonStr = point.getAttribute(ATTR_LONGITUDE);
        
        if (N_A.equals(latStr) || N_A.equals(lonStr)) {
            return null;
        }

        Location location = new Location(GPX_LOCATION_PROVIDER);
        location.setLatitude(Double.parseDouble(latStr));
        location.setLongitude(Double.parseDouble(lonStr));
        location.setAltitude(Double.parseDouble(extractElevation(point)));
        
        String timeStr = extractTime(point);
        if (!N_A.equals(timeStr)) {
            setLocationTime(location, timeStr);
        }
        
        return location;
    }

    private String extractElevation(Element point) {
        NodeList eleNodes = point.getElementsByTagName(TAG_ELEVATION);
        return eleNodes.getLength() > 0 ? eleNodes.item(0).getTextContent() : ELEVATION_DEFAULT;
    }

    private String extractTime(Element point) {
        NodeList timeNodes = point.getElementsByTagName(TAG_TIME);
        return timeNodes.getLength() > 0 ? timeNodes.item(0).getTextContent() : N_A;
    }

    private void setLocationTime(Location location, String timeStr) {
        try {
            location.setTime(Objects.requireNonNull(GPX_POINT_DATETIME_FORMAT.parse(timeStr)).getTime());
        } catch (Exception e) {
            Log.e(TAG, ERROR_PARSING_TIME + timeStr, e);
        }
    }
}
