package tk.gabdeg.cluster;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;

public class TrendingPostsFragment extends InfoFragment {

    public static String LOCATION_KEY = "location_key";

    static TrendingPostsFragment newInstance(LatLng location) {
        TrendingPostsFragment fragment = new TrendingPostsFragment();
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

        setText(R.id.post_user, "Trending near you");
        setText(R.id.post_time, "Loading...");

        return layout;
    }

    private class GetTrendingTask extends AsyncTask<LatLng, Void, ArrayList<Post>> {
        @Override
        protected ArrayList<Post> doInBackground(LatLng... poss) {
            return Backend.getTrendingPosts(poss[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<Post> posts) {
            RecyclerView recyclerView = layout.findViewById(R.id.post_content_layout);
            recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), new LinearLayoutManager(activity).getOrientation()));
            recyclerView.setLayoutManager(new LinearLayoutManager(activity));
            recyclerView.setAdapter(new PostAdapter(activity, posts, toOpen -> {
                size = activity.infoFragmentSize();
                activity.clickPost(toOpen, true);
            }, null));

            Post recent = posts.get(0);
            for (Post post : posts) {
                if (post.time > recent.time) recent = post;
            }
            setText(R.id.post_time, recent.ago());
        }
    }
}
