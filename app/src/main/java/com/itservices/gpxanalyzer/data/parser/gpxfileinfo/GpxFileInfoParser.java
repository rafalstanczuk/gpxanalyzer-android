package com.itservices.gpxanalyzer.data.parser.gpxfileinfo;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

@Singleton
public class GpxFileInfoParser {
    private static final String TAG = GpxFileInfoParser.class.getSimpleName();

    private static final SimpleDateFormat GPX_POINT_DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());


    @Inject
    public GpxFileInfoParser() {
    }
    public GpxFileInfo parse(File file) {
        String creator = "Unknown";
        String authorName = "Unknown";
        String latStr = "N/A";
        String lonStr = "N/A";
        String eleStr = "N/A";
        String timeStr = "N/A";
        long timeMillis = 0;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);

            // Get creator
            creator = document.getDocumentElement().getAttribute("creator");
            if (creator.isEmpty()) {
                creator = "Unknown";
            }

            // Get author name
            NodeList authorNodes = document.getElementsByTagName("author");
            if (authorNodes.getLength() > 0) {
                NodeList nameNodes = ((Element) authorNodes.item(0)).getElementsByTagName("name");
                if (nameNodes.getLength() > 0) {
                    authorName = nameNodes.item(0).getTextContent();
                }
            }

            // Get first track point
            NodeList trksegNodes = document.getElementsByTagName("trkseg");
            if (trksegNodes.getLength() > 0) {
                NodeList trkptNodes = ((Element) trksegNodes.item(0)).getElementsByTagName("trkpt");
                if (trkptNodes.getLength() > 0) {
                    Element firstPoint = (Element) trkptNodes.item(0);
                    latStr = firstPoint.getAttribute("lat");
                    lonStr = firstPoint.getAttribute("lon");

                    NodeList eleNodes = firstPoint.getElementsByTagName("ele");
                    if (eleNodes.getLength() > 0) {
                        eleStr = eleNodes.item(0).getTextContent();
                    }

                    NodeList timeNodes = firstPoint.getElementsByTagName("time");
                    if (timeNodes.getLength() > 0) {
                        timeStr = timeNodes.item(0).getTextContent();
                    }
                }
            }

            if (!"N/A".equals(timeStr)) {
                Date date = GPX_POINT_DATETIME_FORMAT.parse(timeStr);
                if (date != null) {
                    timeMillis = date.getTime();
                }
            }

            // Pass the generated bitmap (can be null if generation failed or no valid point)
            return new GpxFileInfo(0, file, creator, authorName, latStr, lonStr, eleStr, timeMillis);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing GPX file: " + file.getName(), e);
            // Return with null bitmap in case of parsing error
            return new GpxFileInfo(0, file, "Error", "Error", "N/A", "N/A", "N/A", 0);
        }
    }
}
