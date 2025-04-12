package com.itservices.gpxanalyzer.data.parser.gpxfileinfo;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class GpxFileInfoParser {
    private static final String TAG = GpxFileInfoParser.class.getSimpleName();

    private static final SimpleDateFormat GPX_POINT_DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());

    public static GpxFileInfo parse(File file) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);

            // Get creator
            String creator = document.getDocumentElement().getAttribute("creator");
            if (creator.isEmpty()) {
                creator = "Unknown";
            }

            // Get author name
            String authorName = "Unknown";
            NodeList authorNodes = document.getElementsByTagName("author");
            if (authorNodes.getLength() > 0) {
                NodeList nameNodes = ((Element) authorNodes.item(0)).getElementsByTagName("name");
                if (nameNodes.getLength() > 0) {
                    authorName = nameNodes.item(0).getTextContent();
                }
            }

            // Get first track point
            String lat = "N/A";
            String lon = "N/A";
            String ele = "N/A";
            String time = "N/A";

            NodeList trksegNodes = document.getElementsByTagName("trkseg");
            if (trksegNodes.getLength() > 0) {
                NodeList trkptNodes = ((Element) trksegNodes.item(0)).getElementsByTagName("trkpt");
                if (trkptNodes.getLength() > 0) {
                    Element firstPoint = (Element) trkptNodes.item(0);
                    lat = firstPoint.getAttribute("lat");
                    lon = firstPoint.getAttribute("lon");

                    NodeList eleNodes = firstPoint.getElementsByTagName("ele");
                    if (eleNodes.getLength() > 0) {
                        ele = eleNodes.item(0).getTextContent();
                    }

                    NodeList timeNodes = firstPoint.getElementsByTagName("time");
                    if (timeNodes.getLength() > 0) {
                        time = timeNodes.item(0).getTextContent();
                    }
                }
            }

            Date date = GPX_POINT_DATETIME_FORMAT.parse(time);
            assert date != null;

            return new GpxFileInfo(file, creator, authorName, lat, lon, ele, date.getTime());
        } catch (Exception e) {
            Log.e(TAG, "Error parsing GPX file: " + file.getName(), e);
            return new GpxFileInfo(file, "Error", "Error", "N/A", "N/A", "N/A", 0);
        }
    }
}
