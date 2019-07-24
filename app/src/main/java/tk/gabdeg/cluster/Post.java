package tk.gabdeg.cluster;

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
    boolean starred;

    public String ago() {
        long passed = (System.currentTimeMillis() / 1000) - time;
        String passedStr = "Posted ";
        if (passed >= 3600) {
            passedStr += (passed / 3600) + "h";
        } else if (passed >= 60) {
            passedStr += (passed / 60) + "m";
        } else {
            passedStr += passed + "s";
        }
        passedStr += " ago";
        return passedStr;
    }

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
