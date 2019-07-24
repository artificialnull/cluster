package tk.gabdeg.cluster;

import android.content.Context;
import android.content.SharedPreferences;

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
    static String server = "http://73.76.97.119:5000";

    static final String PREF_USERNAME = "cluster_username";
    static final String PREF_PASSWORD = "cluster_password";
    static String phone = "";
    static String password = "";

    static boolean setCredentials(Context ctx) {
        SharedPreferences preferences = ctx.getSharedPreferences(ctx.getString(R.string.preferenceKey), Context.MODE_PRIVATE);
        phone = preferences.getString(PREF_USERNAME, "");
        password = preferences.getString(PREF_PASSWORD, "");
        return (!phone.isEmpty() && !password.isEmpty());
    }

    static String streamToString(InputStream is) throws IOException {
        String out = IOUtils.toString(is, StandardCharsets.UTF_8);
        is.close();
        return out;
    }

    static void writeJSON(JSONObject json, OutputStream out) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        writer.write(json.toString());
        writer.flush();
        writer.close();
    }

    static String postJSON(URL url, JSONObject put) {
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

    public static JSONObject submitPost(Post post) {
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

    public static FeatureCollection getPosts(LatLng position) {
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

    public static Post getPost(int postID) {
        try {
            Post ret = new Gson().fromJson(postJSON(new URL(server + "/post/" + postID), new JSONObject()), Post.class);
            return ret;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getPostImage(int postID) {
        try {
            return postJSON(new URL(server + "/post/" + postID + "/image"), new JSONObject());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean getStarredStatus(int postID) {
        try {
            JSONObject resp = new JSONObject(postJSON(new URL(server + "/post/" + postID + "/starred"), new JSONObject()));
            if (resp.getBoolean("status")) {
                return resp.getBoolean("starred");
            }
        } catch (Exception e) {}
        return false;
    }

    public static boolean setStarredStatus(int postID, boolean starred) {
        try {
            JSONObject resp = new JSONObject(postJSON(new URL(server + "/post/" + postID + "/star"), new JSONObject("{\"star\": " + starred + "}")));
            if (resp.getBoolean("status")) {
                return resp.getBoolean("starred");
            }
        } catch (Exception e) {}
        return false;
    }

    public static User getProfile() {
        try {
            User ret = new Gson().fromJson(postJSON(new URL(server + "/me"), new JSONObject()), User.class);
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject createProfile(String name, String phone) {
        try {
            JSONObject base = new JSONObject();
            JSONObject put = new JSONObject();
            put.put("name", name);
            put.put("phone", phone);
            base.put("signup", put);
            return new JSONObject(postJSON(new URL(server + "/signup"), base));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject verifyProfile() {
        try {
            return new JSONObject(postJSON(new URL(server + "/verify"), new JSONObject()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
