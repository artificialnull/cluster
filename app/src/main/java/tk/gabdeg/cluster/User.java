package tk.gabdeg.cluster;

import java.util.ArrayList;

public class User {
    String name;
    int stars;
    int postLimit;
    double limitProgress;
    int nextLimit;
    ArrayList<Integer> posts;

    public User(String name, int stars, int postLimit, double limitProgress, int nextLimit, ArrayList<Integer> posts) {
        this.name = name;
        this.stars = stars;
        this.postLimit = postLimit;
        this.limitProgress = limitProgress;
        this.nextLimit = nextLimit;
        this.posts = posts;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", stars=" + stars +
                ", postLimit=" + postLimit +
                ", limitProgress=" + limitProgress +
                ", nextLimit=" + nextLimit +
                ", posts=" + posts +
                '}';
    }
}
