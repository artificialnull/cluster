package tk.gabdeg.near;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class SubmitActivity extends AppCompatActivity {

    public static final String LOCATION_KEY = "location_serialized";
    public static final int CAMERA_CAPTURE = 1;

    boolean imageCaptured = false;
    Post put;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap thumbnail = (Bitmap) extras.get("data");

            ((ImageView) findViewById(R.id.submit_image_view)).setImageBitmap(thumbnail);
            ((ImageButton) findViewById(R.id.submit_image)).setImageResource(R.drawable.cancel);

            imageCaptured = true;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            thumbnail.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            put.image = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit);

        put = new Gson().fromJson(getIntent().getStringExtra(LOCATION_KEY), Post.class);
        ((TextView) findViewById(R.id.submit_address)).setText(put.latitude + ", " + put.longitude);
        ((EditText) findViewById(R.id.submit_text)).setText(put.text, TextView.BufferType.EDITABLE);

        findViewById(R.id.submit_image).setOnClickListener(
                v -> {
                    if (imageCaptured) {
                        imageCaptured = false;
                        ((ImageButton) findViewById(R.id.submit_image)).setImageResource(R.drawable.add_photo);
                        ((ImageView) findViewById(R.id.submit_image_view)).setImageDrawable(null);
                        put.image = null;
                    } else {
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(cameraIntent, CAMERA_CAPTURE);
                        } else {
                            Snackbar.make(findViewById(R.id.submit_layout), "Can't open camera", Snackbar.LENGTH_LONG).show();
                        }
                    }

                }
        );

        getSupportActionBar().setTitle("Submit a post");
        findViewById(R.id.submit_button).setOnClickListener(
                v -> {
                    put.text = ((EditText) findViewById(R.id.submit_text)).getText().toString();
                    new SubmitPostTask().execute(put);
                }
        );

        new GetAddressTask(this).execute(new LatLng(put.latitude, put.longitude));
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
