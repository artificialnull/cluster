package tk.gabdeg.cluster;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SubmitActivity extends BackendActivity {

    public static final String LOCATION_KEY = "location_serialized";
    public static final int CAMERA_CAPTURE = 1;
    public static final int SUBMIT_FINISHED = 2;

    boolean imageCaptured = false;
    String imagePath = null;
    Post put;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_CAPTURE && resultCode == RESULT_OK) {
            imageCaptured = true;

            if (imagePath != null) {
                new SetPhotoTask().execute();
            }
        }
    }

    String requestTakePhoto() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {

            File image = null;
            try {
                String filename = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                image = File.createTempFile(filename, ".png", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            Uri photoURI = FileProvider.getUriForFile(this, "tk.gabdeg.fileprovider", image);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

            startActivityForResult(cameraIntent, CAMERA_CAPTURE);

            return image.getAbsolutePath();
        } else {
            Snackbar.make(findViewById(R.id.submit_layout), "Can't open camera", Snackbar.LENGTH_LONG).show();
        }
        return null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit);

        if (getSupportActionBar() != null) { getSupportActionBar().setDisplayHomeAsUpEnabled(true); }

        put = new Gson().fromJson(getIntent().getStringExtra(LOCATION_KEY), Post.class);
        ((TextView) findViewById(R.id.submit_address)).setText(put.latitude + ", " + put.longitude);
        ((EditText) findViewById(R.id.submit_text)).setText(put.text, TextView.BufferType.EDITABLE);

        findViewById(R.id.submit_image).setOnClickListener(
                v -> {
                    if (imageCaptured) {
                        imageCaptured = false;

                        findViewById(R.id.submit_image).animate().rotation(0).setInterpolator(new AccelerateDecelerateInterpolator());
                        put.image = null;

                        TranslateAnimation animation = new TranslateAnimation(
                                0,
                                -findViewById(R.id.submit_image_view).getWidth(),
                                0,
                                0
                        );
                        animation.setInterpolator(new AccelerateDecelerateInterpolator());
                        animation.setDuration(250);
                        animation.setFillAfter(true);
                        animation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                ((ImageView) findViewById(R.id.submit_image_view)).setImageDrawable(null);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });
                        findViewById(R.id.submit_image_view).startAnimation(animation);
                    } else {
                        imagePath = requestTakePhoto();
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
            ((FloatingActionButton) findViewById(R.id.submit_button)).setImageDrawable(null);
            findViewById(R.id.spinner).setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(Post... posts) {
            return Backend.submitPost(posts[0]);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                if (jsonObject.getBoolean("status")) {
                    finish();
                } else {
                    Snackbar.make(findViewById(R.id.submit_layout), jsonObject.getString("message"), Snackbar.LENGTH_LONG).show();
                    ((FloatingActionButton) findViewById(R.id.submit_button)).setImageResource(R.drawable.submit_post);
                    findViewById(R.id.spinner).setVisibility(View.INVISIBLE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class SetPhotoTask extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            //findViewById(R.id.submit_image_loading).setVisibility(View.VISIBLE);
            //findViewById(R.id.submit_image).setVisibility(View.INVISIBLE);

            RotateAnimation rotate = new RotateAnimation(
                    0, 360,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );
            rotate.setDuration(750);
            rotate.setRepeatCount(Animation.INFINITE);
            rotate.setInterpolator(new LinearInterpolator());
            findViewById(R.id.submit_image).startAnimation(rotate);
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Bitmap fullSize = BitmapFactory.decodeFile(imagePath);
            Bitmap small = Bitmap.createScaledBitmap(fullSize, 800, (int) (fullSize.getHeight() * (800 / (float) fullSize.getWidth())), false);
            small.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            put.image = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
            return small;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ((ImageView) findViewById(R.id.submit_image_view)).setImageBitmap(bitmap);
            //findViewById(R.id.submit_image_loading).setVisibility(View.INVISIBLE);
            //findViewById(R.id.submit_image).setVisibility(View.VISIBLE);
            findViewById(R.id.submit_image).clearAnimation();

            findViewById(R.id.submit_image).animate().rotation(45f).setInterpolator(new LinearInterpolator());

            TranslateAnimation animation = new TranslateAnimation(
                    findViewById(R.id.submit_image_view).getWidth(),
                    0,
                    0,
                    0
            );
            animation.setInterpolator(new AccelerateDecelerateInterpolator());
            animation.setDuration(250);
            animation.setFillAfter(true);
            findViewById(R.id.submit_image_view).startAnimation(animation);
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
