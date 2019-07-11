package tk.gabdeg.cluster;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class ProfileActivity extends BackendActivity {

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
            ((TextView) findViewById(R.id.profile_name)).setText(user.name);
            ((TextView) findViewById(R.id.profile_stars)).setText(Integer.toString(user.stars));
            ((TextView) findViewById(R.id.current_post_limit)).setText(Integer.toString(user.postLimit));
            ((TextView) findViewById(R.id.next_post_limit)).setText(Integer.toString(user.nextLimit));
            ((ProgressBar) findViewById(R.id.post_limit_progress)).setIndeterminate(false);
            ((ProgressBar) findViewById(R.id.post_limit_progress)).setProgress((int) (user.limitProgress * 100));
        }
    }

}
