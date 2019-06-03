package tk.gabdeg.near;

import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.FeatureCollection;
import com.cocoahero.android.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Backend {
    static String server = "http://192.168.1.172:5000";

    String streamToString(InputStream is) throws IOException {
        BufferedReader ir = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        for (String line; (line = ir.readLine()) != null; ) {
            sb.append(line).append('\n');
        }
        return sb.toString();
    }

    void writeJSON(JSONObject json, OutputStream out) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        writer.write(json.toString());
        writer.flush();
        writer.close();
    }

    public FeatureCollection getPosts(LatLng position) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(server + "/posts").openConnection();
            conn.setRequestMethod("POST");

            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            JSONObject location = new JSONObject()
                    .put("location", new JSONArray().put(position.getLatitude()).put(position.getLongitude()));

            writeJSON(location, conn.getOutputStream());
            JSONArray resp = new JSONArray(streamToString(conn.getInputStream()));
            FeatureCollection collection = new FeatureCollection();

            for (int i = 0; i < resp.length(); i++) {
                JSONObject obj = resp.getJSONObject(i);
                Feature feature = new Feature(new Point(obj.getDouble("latitude"), obj.getDouble("longitude")));
                feature.setIdentifier(Integer.toString(obj.getInt("id")));
                feature.setProperties(obj);
                collection.addFeature(feature);
            }

            return collection;


        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
