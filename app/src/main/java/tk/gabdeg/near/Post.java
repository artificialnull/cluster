package tk.gabdeg.near;

import com.mapbox.mapboxsdk.geometry.LatLng;

public class Post {
    int id;
    int userID;
    double latitude;
    double longitude;
    LatLng location() {
        return new LatLng(latitude, longitude);
    }
    String text;
    String image;
    String user;
    int time;
    int stars;

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", userID=" + userID +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", text='" + text + '\'' +
                ", image='" + (image != null && !image.isEmpty()) + '\'' +
                ", user='" + user + '\'' +
                ", time=" + time +
                ", stars=" + stars +
                '}';
    }
}
