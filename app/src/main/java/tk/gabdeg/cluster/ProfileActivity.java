package tk.gabdeg.cluster;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ProfileActivity extends BackendActivity {

    public static final int PROFILE_FINISHED = 3;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Profile");
        }

        new GetProfileTask().execute();
    }

    private class GetProfileTask extends AsyncTask<Void, Void, User> {
        @Override
        protected User doInBackground(Void... voids) {
            return Backend.getProfile();
        }

        @Override
        protected void onPostExecute(User user) {
            if (user == null) {
                new GetProfileTask().execute();
                return;
            }
            Log.d("user", user.toString());
            new GetPostsTask().execute((ArrayList) user.posts.clone());
            ((TextView) findViewById(R.id.profile_name)).setText(user.name);
            ((TextView) findViewById(R.id.profile_stars)).setText(Integer.toString(user.stars));
            ((TextView) findViewById(R.id.current_post_limit)).setText(Integer.toString(user.postLimit));
            ((TextView) findViewById(R.id.next_post_limit)).setText(Integer.toString(user.nextLimit));
            ((ProgressBar) findViewById(R.id.post_limit_progress)).setIndeterminate(false);
            ((ProgressBar) findViewById(R.id.post_limit_progress)).setProgress((int) (user.limitProgress * 100));
        }
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
            for (Post post : posts) {
                Log.d("user-posts", post.text);
            }
            RecyclerView recyclerView = findViewById(R.id.profile_post_list);
            recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), new LinearLayoutManager(getApplicationContext()).getOrientation()));
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            recyclerView.setAdapter(new PostAdapter(getApplicationContext(), posts));
        }
    }
}
