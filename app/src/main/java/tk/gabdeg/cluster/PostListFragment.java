package tk.gabdeg.cluster;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextSwitcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class PostListFragment extends InfoFragment {

    public static String POST_LIST_KEY = "post_list_serialized";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("post-list", "creating!");
        if (getView() != null) {
            Log.d("post-list", "getView not null");
        }
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
        layout = inflater.inflate(R.layout.fragment_post_list, container, false);
        bindActionsToLayout();

        List<Post> posts = new Gson().fromJson(this.getArguments().getString(POST_LIST_KEY), new TypeToken<ArrayList<Post>>() {
        }.getType());

        ArrayList<Integer> postIDs = new ArrayList<>();
        for (Post post : posts) {
            postIDs.add(post.id);
        }

        TextSwitcher postUser = layout.findViewById(R.id.post_user);
        postUser.setCurrentText(posts.size() + " posts clustered");
        postUser.setOutAnimation(activity, android.R.anim.slide_out_right);
        postUser.setInAnimation(activity, android.R.anim.slide_in_left);

        TextSwitcher postTime = layout.findViewById(R.id.post_time);
        postTime.setCurrentText("Loading...");
        postTime.setOutAnimation(activity, android.R.anim.slide_out_right);
        postTime.setInAnimation(activity, android.R.anim.slide_in_left);

        new GetPostsTask().execute(postIDs);

        return layout;
    }

    private class GetPostsTask extends AsyncTask<ArrayList<Integer>, Void, ArrayList<Post>> {
        @Override
        protected ArrayList<Post> doInBackground(ArrayList<Integer>... arrayLists) {
            ArrayList<Integer> postList = arrayLists[0];
            ArrayList<Post> ret = new ArrayList<>();
            for (int postID : postList) {
                ret.add(Backend.getPost(postID));
            }
            return ret;
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
