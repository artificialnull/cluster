package tk.gabdeg.near;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONObject;

import java.util.List;

public class SubmitActivity extends AppCompatActivity {

    public static String LOCATION_KEY = "location_serialized";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit);

        Post location = new Gson().fromJson(getIntent().getStringExtra(LOCATION_KEY), Post.class);
        ((TextView) findViewById(R.id.submit_address)).setText(location.latitude + ", " + location.longitude);
        ((EditText) findViewById(R.id.submit_text)).setText(location.text, TextView.BufferType.EDITABLE);

        getSupportActionBar().setTitle("Submit a post");
        findViewById(R.id.submit_button).setOnClickListener(
                v -> {
                    location.text = ((EditText) findViewById(R.id.submit_text)).getText().toString();
                    new SubmitPostTask().execute(location);
                }
        );

        new GetAddressTask(this).execute(new LatLng(location.latitude, location.longitude));
    }

    private class SubmitPostTask extends AsyncTask<Post, Void, JSONObject> {
        @Override
        protected void onPreExecute() {
            ((FloatingActionButton) findViewById(R.id.submit_button)).setImageResource(R.drawable.more);
        }

        @Override
        protected JSONObject doInBackground(Post... posts) {
            return new Backend().submitPost(posts[0]);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                if (jsonObject.getBoolean("status")) {
                    finish();
                } else {
                    Snackbar.make(findViewById(R.id.submit_layout), jsonObject.getString("message"), Snackbar.LENGTH_LONG).show();
                    ((FloatingActionButton) findViewById(R.id.submit_button)).setImageResource(R.drawable.submit_post);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class GetAddressTask extends AsyncTask<LatLng, Void, Address> {

        private Context _context;
        public GetAddressTask(Context context) {
            _context = context;
        }

        @Override
        protected Address doInBackground(LatLng... latLngs) {
            Geocoder geocoder = new Geocoder(_context);
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(
                        latLngs[0].getLatitude(),
                        latLngs[0].getLongitude(),
                        1
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (addresses != null && addresses.size() > 0) {
                // there is an address
                return addresses.get(0);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Address address) {
            if (address == null) return;
            ((TextView) findViewById(R.id.submit_address)).setText(address.getAddressLine(0));
        }
    }
}
