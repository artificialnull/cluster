package tk.gabdeg.cluster;

import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.FeatureCollection;
import com.cocoahero.android.geojson.Point;
import com.google.gson.Gson;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Backend {
    static String server = "http://192.168.1.58:5000";

    static String phone = "2819498189";
    static String password = "5npwci599zuvxuxc";

    String streamToString(InputStream is) throws IOException {
        String out = IOUtils.toString(is, StandardCharsets.UTF_8);
        is.close();
        return out;
    }

    void writeJSON(JSONObject json, OutputStream out) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        writer.write(json.toString());
        writer.flush();
        writer.close();
    }

    public String postJSON(URL url, JSONObject put) {
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setChunkedStreamingMode(0);
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            put.put("login",
                    new JSONObject()
                            .put("phone", phone)
                            .put("password", password)
            );
            writeJSON(put, conn.getOutputStream());
            String ret = streamToString(conn.getInputStream());
            conn.disconnect();
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public JSONObject submitPost(Post post) {
        try {
            JSONObject put = new JSONObject();
            put.put("post", new JSONObject(new Gson().toJson(post)));
            JSONObject status = new JSONObject(postJSON(new URL(server + "/submit"), put));
            return status;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public FeatureCollection getPosts(LatLng position) {
        try {
            JSONObject location = new JSONObject()
                    .put("location", new JSONArray().put(position.getLatitude()).put(position.getLongitude()));

            String ret = postJSON(new URL(server + "/posts"), location);
            if (ret == null || ret.length() == 0) {
                return null;
            }

            JSONArray resp = new JSONArray(ret);
            FeatureCollection collection = new FeatureCollection();

            for (int i = 0; i < resp.length(); i++) {
                JSONObject obj = resp.getJSONObject(i);
                Feature feature = new Feature(new Point(obj.getDouble("latitude"), obj.getDouble("longitude")));
                feature.setIdentifier(Integer.toString(obj.getInt("id")));
                feature.setProperties(obj);
                collection.addFeature(feature);
            }

            return collection;

        }  catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Post getPost(int postID) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(server + "/post/" + postID).openConnection();
            conn.setChunkedStreamingMode(0);

            Post ret = new Gson().fromJson(streamToString(conn.getInputStream()), Post.class);
            conn.disconnect();

            return ret;

        } catch (IOException e) {
        }
        return null;
    }

    public boolean getStarredStatus(int postID) {
        try {
            JSONObject resp = new JSONObject(postJSON(new URL(server + "/post/" + postID + "/starred"), new JSONObject()));
            if (resp.getBoolean("status")) {
                return resp.getBoolean("starred");
            }
        } catch (Exception e) {}
        return false;
    }

    public boolean setStarredStatus(int postID, boolean starred) {
        try {
            JSONObject resp = new JSONObject(postJSON(new URL(server + "/post/" + postID + "/star"), new JSONObject("{\"star\": " + starred + "}")));
            if (resp.getBoolean("status")) {
                return resp.getBoolean("starred");
            }
        } catch (Exception e) {}
        return false;
    }

    public User getProfile() {
        try {
            User ret = new Gson().fromJson(postJSON(new URL(server + "/me"), new JSONObject()), User.class);
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
