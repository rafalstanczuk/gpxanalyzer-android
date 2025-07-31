package com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava;

import android.util.Xml;

import com.itservices.gpxanalyzer.core.data.parser.GPXParser;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.mapper.StravaStreamMapper;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model.StravaActivity;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model.StravaStream;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Generates GPX files from Strava activity data using XML libraries.
 */
public class StravaGpxGenerator {
    private static final String GPX_NAMESPACE = "http://www.topografix.com/GPX/1/1";
    private static final String GPX_CREATOR = "GPXAnalyzer-StravaProxy";
    private static final String GPX_VERSION = "1.1";
    
    /**
     * Generates GPX content from Strava activity and streams using XmlSerializer.
     */
    public static String generateGpx(StravaActivity activity, List<StravaStream> streams) {
        StringWriter writer = new StringWriter();
        XmlSerializer serializer = Xml.newSerializer();
        
        try {
            // Find necessary streams
            StravaStream latlngStream = null;
            StravaStream altitudeStream = null;
            StravaStream timeStream = null;

            for (StravaStream stream : streams) {
                if (stream.isLatLngStream()) {
                    latlngStream = stream;
                } else if ("altitude".equals(stream.getType())) {
                    altitudeStream = stream;
                } else if ("time".equals(stream.getType())) {
                    timeStream = stream;
                }
            }
            
            // Setup serializer
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            
            // GPX root element
            serializer.setPrefix("", GPX_NAMESPACE);
            serializer.startTag(GPX_NAMESPACE, GPXParser.TAG_GPX);
            serializer.attribute("", GPXParser.TAG_VERSION, GPX_VERSION);
            serializer.attribute("", GPXParser.TAG_CREATOR, GPX_CREATOR);
            
            // Metadata
            serializer.startTag(GPX_NAMESPACE, GPXParser.TAG_METADATA);
            
            // Name
            serializer.startTag(GPX_NAMESPACE, GPXParser.TAG_NAME);
            serializer.text(activity.getName() != null ? activity.getName() : "");
            serializer.endTag(GPX_NAMESPACE, GPXParser.TAG_NAME);
            
            // Description
            serializer.startTag(GPX_NAMESPACE, GPXParser.TAG_DESC);
            serializer.text("Imported from Strava Activity ID: " + activity.getId());
            serializer.endTag(GPX_NAMESPACE, GPXParser.TAG_DESC);
            
            // Time
            if (activity.getStartDate() != null) {
                serializer.startTag(GPX_NAMESPACE, GPXParser.TAG_TIME);
                serializer.text(formatDateIso8601(activity.getStartDate()));
                serializer.endTag(GPX_NAMESPACE, GPXParser.TAG_TIME);
            }
            
            serializer.endTag(GPX_NAMESPACE, GPXParser.TAG_METADATA);
            
            // Track
            serializer.startTag(GPX_NAMESPACE, GPXParser.TAG_TRACK);
            
            // Track name
            serializer.startTag(GPX_NAMESPACE, GPXParser.TAG_NAME);
            serializer.text(activity.getName() != null ? activity.getName() : "");
            serializer.endTag(GPX_NAMESPACE, GPXParser.TAG_NAME);
            
            // Track type
            serializer.startTag(GPX_NAMESPACE, GPXParser.TAG_TYPE);
            serializer.text(activity.getType() != null ? activity.getType() : "");
            serializer.endTag(GPX_NAMESPACE, GPXParser.TAG_TYPE);
            
            // Track segment
            serializer.startTag(GPX_NAMESPACE, GPXParser.TAG_SEGMENT);
            
            // Generate track points
            if (latlngStream != null) {
                List<List<Double>> coordinates = latlngStream.getLatLngData();
                List<Double> altitudes = altitudeStream != null ? altitudeStream.getElevationData() : null;
                List<Integer> times = timeStream != null ? timeStream.getTimeData() : null;
                
                if (coordinates != null) {
                    for (int i = 0; i < coordinates.size(); i++) {
                        List<Double> coord = coordinates.get(i);
                        if (coord != null && coord.size() >= 2) {
                            // Track point
                            serializer.startTag(GPX_NAMESPACE, GPXParser.TAG_TRACK_POINT);
                            serializer.attribute("", GPXParser.TAG_LAT, String.valueOf(coord.get(0)));
                            serializer.attribute("", GPXParser.TAG_LON, String.valueOf(coord.get(1)));
                            
                            // Elevation
                            if (altitudes != null && i < altitudes.size() && altitudes.get(i) != null) {
                                serializer.startTag(GPX_NAMESPACE, GPXParser.TAG_ELEVATION);
                                serializer.text(String.valueOf(altitudes.get(i)));
                                serializer.endTag(GPX_NAMESPACE, GPXParser.TAG_ELEVATION);
                            }
                            
                            // Time
                            if (times != null && i < times.size() && times.get(i) != null && activity.getStartDate() != null) {
                                try {
                                    long startTimeMs = activity.getStartDate().getTime();
                                    long pointTimeMs = startTimeMs + (times.get(i) * 1000L);
                                    
                                    serializer.startTag(GPX_NAMESPACE, GPXParser.TAG_TIME);
                                    serializer.text(formatDateIso8601(new Date(pointTimeMs)));
                                    serializer.endTag(GPX_NAMESPACE, GPXParser.TAG_TIME);
                                } catch (Exception e) {
                                    // Skip time if parsing fails
                                }
                            }
                            
                            serializer.endTag(GPX_NAMESPACE, GPXParser.TAG_TRACK_POINT);
                        }
                    }
                }
            }
            
            // Close tags
            serializer.endTag(GPX_NAMESPACE, GPXParser.TAG_SEGMENT);
            serializer.endTag(GPX_NAMESPACE, GPXParser.TAG_TRACK);
            serializer.endTag(GPX_NAMESPACE, GPXParser.TAG_GPX);
            
            serializer.endDocument();
            return writer.toString();
            
        } catch (IOException e) {
            return generateGpxFallback(activity, streams);
        }
    }
    
    /**
     * Fallback method using string concatenation in case XML serialization fails.
     */
    private static String generateGpxFallback(StravaActivity activity, List<StravaStream> streams) {
        StringBuilder gpx = new StringBuilder();
        gpx.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        gpx.append("<" + GPXParser.TAG_GPX + " " + GPXParser.TAG_VERSION + "=\"1.1\" " + GPXParser.TAG_CREATOR + "=\"GPXAnalyzer-StravaProxy\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n");
        gpx.append("  <" + GPXParser.TAG_METADATA + ">\n");
        gpx.append("    <" + GPXParser.TAG_NAME + ">").append(escapeXml(activity.getName())).append("</" + GPXParser.TAG_NAME + ">\n");
        gpx.append("    <" + GPXParser.TAG_DESC + ">Imported from Strava Activity ID: ").append(activity.getId()).append("</" + GPXParser.TAG_DESC + ">\n");
        if (activity.getStartDate() != null) {
            gpx.append("    <" + GPXParser.TAG_TIME + ">").append(formatDateIso8601(activity.getStartDate())).append("</" + GPXParser.TAG_TIME + ">\n");
        }
        gpx.append("  </" + GPXParser.TAG_METADATA + ">\n");
        gpx.append("  <" + GPXParser.TAG_TRACK + ">\n");
        gpx.append("    <" + GPXParser.TAG_NAME + ">").append(escapeXml(activity.getName())).append("</" + GPXParser.TAG_NAME + ">\n");
        gpx.append("    <" + GPXParser.TAG_TYPE + ">").append(escapeXml(activity.getType())).append("</" + GPXParser.TAG_TYPE + ">\n");
        gpx.append("    <" + GPXParser.TAG_SEGMENT + ">\n");

        // Find streams
        StravaStream latlngStream = null;
        StravaStream altitudeStream = null;
        StravaStream timeStream = null;

        for (StravaStream stream : streams) {
            if (stream.isLatLngStream()) {
                latlngStream = stream;
            } else if ("altitude".equals(stream.getType())) {
                altitudeStream = stream;
            } else if ("time".equals(stream.getType())) {
                timeStream = stream;
            }
        }

        // Generate track points
        if (latlngStream != null) {
            List<List<Double>> coordinates = latlngStream.getLatLngData();
            List<Double> altitudes = altitudeStream != null ? altitudeStream.getElevationData() : null;
            List<Integer> times = timeStream != null ? timeStream.getTimeData() : null;

            if (coordinates != null) {
                for (int i = 0; i < coordinates.size(); i++) {
                    List<Double> coord = coordinates.get(i);
                    if (coord != null && coord.size() >= 2) {
                        gpx.append("      <" + GPXParser.TAG_TRACK_POINT + " " + GPXParser.TAG_LAT + "=\"").append(coord.get(0)).append("\" " + GPXParser.TAG_LON + "=\"").append(coord.get(1)).append("\">\n");

                        // Add elevation if available
                        if (altitudes != null && i < altitudes.size() && altitudes.get(i) != null) {
                            gpx.append("        <" + GPXParser.TAG_ELEVATION + ">").append(altitudes.get(i)).append("</" + GPXParser.TAG_ELEVATION + ">\n");
                        }

                        // Add time if available
                        if (times != null && i < times.size() && times.get(i) != null && activity.getStartDate() != null) {
                            try {
                                // Get start date and add seconds offset
                                long startTimeMs = activity.getStartDate().getTime();
                                long pointTimeMs = startTimeMs + (times.get(i) * 1000L);
                                gpx.append("        <" + GPXParser.TAG_TIME + ">").append(formatDateIso8601(new Date(pointTimeMs))).append("</" + GPXParser.TAG_TIME + ">\n");
                            } catch (Exception e) {
                                // Skip time if parsing fails
                            }
                        }

                        gpx.append("      </" + GPXParser.TAG_TRACK_POINT + ">\n");
                    }
                }
            }
        }

        gpx.append("    </" + GPXParser.TAG_SEGMENT + ">\n");
        gpx.append("  </" + GPXParser.TAG_TRACK + ">\n");
        gpx.append("</" + GPXParser.TAG_GPX + ">\n");

        return gpx.toString();
    }

    /**
     * Formats a Date object to an ISO 8601 compliant string ("yyyy-MM-dd'T'HH:mm:ss'Z'").
     */
    private static String formatDateIso8601(Date date) {
        if (date == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    /**
     * Validates that streams contain sufficient data for GPX generation.
     * @deprecated Use {@link com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.mapper.StravaStreamMapper#validateStreamsForGpx} instead.
     */
    @Deprecated
    public static boolean validateStreamsForGpx(List<StravaStream> streams) {
        return StravaStreamMapper.validateStreamsForGpx(streams);
    }

    /**
     * Escapes XML special characters.
     */
    private static String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&apos;");
    }
}