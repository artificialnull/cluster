package tk.gabdeg.cluster;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;

public class TrendingPostsFragment extends InfoFragment {

    public static String LOCATION_KEY = "location_key";

    static PostFragment newInstance(LatLng location) {
        PostFragment fragment = new PostFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TrendingPostsFragment.LOCATION_KEY, new Gson().toJson(location));
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (layout != null) {
            if (size != activity.infoFragmentSize()) {
                //adapt to different size by changing the view size
                activity.toggleInfoFragmentSize();
            }
            return layout;
        }
        layout = inflater.inflate(R.layout.fragment_trending, container, false);
        bindActionsToLayout();

        new GetTrendingTask().execute(new Gson().fromJson(this.getArguments().getString(LOCATION_KEY), LatLng.class));

        return layout;
    }

    private class GetTrendingTask extends AsyncTask<LatLng, Void, ArrayList<Post>> {
        @Override
        protected ArrayList<Post> doInBackground(LatLng... poss) {
            return Backend.getTrendingPosts(poss[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<Post> posts) {

        }
    }
}
