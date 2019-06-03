package tk.gabdeg.near;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.FeatureCollection;
import com.cocoahero.android.geojson.Point;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Circle;
import com.mapbox.mapboxsdk.plugins.annotation.CircleManager;
import com.mapbox.mapboxsdk.plugins.annotation.CircleOptions;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.layers.TransitionOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class MapActivity extends AppCompatActivity {

    static String USER_ICON = "user_icon_string";
    static String POST_ICON = "post_icon_string";
    private MapView mapView;
    private MapboxMap mapboxMap;
    private Location location;
    private CircleManager userSymManager;
    private Style mapboxStyle;
    private ProgressBar spinner;
    private Circle locationMarker;
    private boolean finishedLoading = false;

    void checkIfLoaded() {
        if (finishedLoading) {
            mapView.setVisibility(View.VISIBLE);
            spinner.setVisibility(View.GONE);
        }
    }

    int mixTwoColors(int color1, int color2, float amount) {
        final byte ALPHA_CHANNEL = 24;
        final byte RED_CHANNEL = 16;
        final byte GREEN_CHANNEL = 8;
        final byte BLUE_CHANNEL = 0;

        final float inverseAmount = 1.0f - amount;

        int a = ((int) (((float) (color1 >> ALPHA_CHANNEL & 0xff) * amount) +
                ((float) (color2 >> ALPHA_CHANNEL & 0xff) * inverseAmount))) & 0xff;
        int r = ((int) (((float) (color1 >> RED_CHANNEL & 0xff) * amount) +
                ((float) (color2 >> RED_CHANNEL & 0xff) * inverseAmount))) & 0xff;
        int g = ((int) (((float) (color1 >> GREEN_CHANNEL & 0xff) * amount) +
                ((float) (color2 >> GREEN_CHANNEL & 0xff) * inverseAmount))) & 0xff;
        int b = ((int) (((float) (color1 & 0xff) * amount) +
                ((float) (color2 & 0xff) * inverseAmount))) & 0xff;

        return a << ALPHA_CHANNEL | r << RED_CHANNEL | g << GREEN_CHANNEL | b << BLUE_CHANNEL;
    }

    String mixColorsHex(int color1, int color2, double amount) {
        int mixed = 0xFF000000 + mixTwoColors(color1, color2, (float) amount);
        return String.format("#%06X", (0xFFFFFF & mixed));
    }

    int getNavBarHeight() {
        Resources resources = getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    LatLng getLatLng(Location loc) {
        return new LatLng(loc.getLatitude(), loc.getLongitude());
    }

    void updateUserPositionMarker(LatLng loc) {
        if (userSymManager != null) {
            if (locationMarker != null) {
                locationMarker.setLatLng(loc);
                userSymManager.update(locationMarker);
            } else {
                CircleOptions userIcon = new CircleOptions()
                        .withCircleColor("#1E88E5")
                        .withCircleRadius(8f)
                        .withCircleStrokeColor("#343332")
                        .withCircleStrokeWidth(2f)
                        .withLatLng(loc);
                locationMarker = userSymManager.create(userIcon);
            }
        }
    }

    void onUserFirstLocated() {
        if (location != null && mapboxMap != null && mapView != null && mapboxStyle != null) {
            CameraPosition position = new CameraPosition.Builder()
                    .target(getLatLng(location))
                    .zoom(13.0)
                    .build();
            mapboxMap.moveCamera(
                    CameraUpdateFactory.newCameraPosition(position)
            );
            updateUserPositionMarker(getLatLng(location));
            checkIfLoaded();
        }
    }

    void jumpToUserLocation() {
        if (location != null && mapboxMap != null && mapView != null) {
            CameraPosition position = new CameraPosition.Builder()
                    .target(getLatLng(location))
                    .zoom(Math.max(mapboxMap.getCameraPosition().zoom, 13.0))
                    .build();
            mapboxMap.animateCamera(
                    CameraUpdateFactory.newCameraPosition(position),
                    1000
            );
        }
    }

    void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(this);
            locationClient.getLastLocation().addOnSuccessListener(this, loc -> {
                if (location == null) {
                    location = loc;
                    new GetPostsTask().execute(getLatLng(location));
                }
                location = loc;
            });
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationCallback locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult result) {
                    Log.d("location", "updated");
                    if (result == null) {
                        return;
                    }
                    for (Location loc : result.getLocations()) {
                        updateUserPositionMarker(getLatLng(loc));
                        location = loc;
                    }
                }
            };
            locationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            checkLocationPermission();
        }
    }

    void loadMap(FeatureCollection posts) {
        if (mapView != null) {
            mapView.getMapAsync(map -> {

                mapboxMap = map;
                mapboxMap.getUiSettings().setRotateGesturesEnabled(false);
                mapboxMap.getUiSettings().setTiltGesturesEnabled(false);

                mapboxMap.setStyle(Style.DARK, style -> {
                    mapboxStyle = style;

                    mapboxStyle.addImage(USER_ICON, Objects.requireNonNull(BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.find_location))), true);
                    mapboxStyle.addImage(POST_ICON, Objects.requireNonNull(BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.post_marker))), true);

                    String jsonStr = "";
                    try {
                        Log.d("points", "adding source");
                        jsonStr = posts.toJSON().toString();
                    } catch (JSONException e) {
                        Log.d("points", "bad json!");
                    }
                    GeoJsonSource source = new GeoJsonSource("points", jsonStr, new GeoJsonOptions()
                            .withCluster(true)
                            .withClusterMaxZoom(26)
                            .withMaxZoom(26)
                            //.withMaxZoom(14)
                            .withClusterRadius(64));
                    mapboxStyle.addSource(
                            source
                    );

                    float defaultRadius = 10f;
                    int defaultColor = getResources().getColor(R.color.secondaryColor);
                    int clusteredColor = getResources().getColor(R.color.clusteredColor);
                    
                    Log.d("points", "adding base layer");
                    mapboxStyle.addLayer(new CircleLayer("points", "points").withProperties(
                            PropertyFactory.circleColor(defaultColor),
                            PropertyFactory.circleRadius(defaultRadius)
                            ).withFilter(
                            Expression.not(Expression.has("point_count"))
                            )
                    );
                    CircleLayer clusterLayer = new CircleLayer("points-clustered", "points").withProperties(
                            PropertyFactory.circleColor(
                                    Expression.step(
                                            Expression.get("point_count"),
                                            Expression.literal(mixColorsHex(defaultColor, clusteredColor, 0.75)),
                                            Expression.stop(10, mixColorsHex(defaultColor, clusteredColor, 0.5)),
                                            Expression.stop(100, mixColorsHex(defaultColor, clusteredColor, 0.25)),
                                            Expression.stop(1000, mixColorsHex(defaultColor, clusteredColor, 0))
                                    )
                            ),
                            PropertyFactory.circleRadius(
                                    Expression.product(
                                            Expression.sum(
                                                    Expression.literal(1.5),
                                                    Expression.division(
                                                            Expression.log10(
                                                                    Expression.get("point_count")
                                                            ),
                                                            Expression.literal(2)
                                                    )
                                            ),
                                            Expression.literal(defaultRadius)
                                    )
                            ),
                            PropertyFactory.circleStrokeWidth(4f),
                            PropertyFactory.circleStrokeColor(Color.WHITE)
                    );
                    clusterLayer.setFilter(Expression.has("point_count"));
                    mapboxStyle.addLayer(clusterLayer);

                    SymbolLayer textLayer = new SymbolLayer("point_labels", "points").withProperties(
                            PropertyFactory.textAllowOverlap(true),
                            PropertyFactory.textColor(Color.WHITE),
                            PropertyFactory.textField(Expression.get("point_count_abbreviated")),
                            PropertyFactory.textIgnorePlacement(false),
                            PropertyFactory.textSize(14f)
                    );

                    userSymManager = new CircleManager(mapView, mapboxMap, mapboxStyle);

                    mapboxStyle.setTransition(new TransitionOptions(0, 0, false));

                    userSymManager.addClickListener(symbol -> {
                        Log.d("symbolo", symbol.toString());
                    });

                    mapboxStyle.addLayer(
                            textLayer
                    );

                    mapboxMap.addOnMapClickListener(point -> {
                        PointF pointf = mapboxMap.getProjection().toScreenLocation(point);
                        List<com.mapbox.geojson.Feature> featureList = mapboxMap.queryRenderedFeatures(pointf, "points-clustered");
                        if (featureList.size() > 0) {
                            for (com.mapbox.geojson.Feature feature : featureList) {
                                Log.d("points", feature.toJson());
                                if (source.getClusterExpansionZoom(feature) <= mapboxMap.getMaxZoomLevel()) {
                                    try {
                                        LatLng pos = new LatLng(
                                                new JSONObject(feature.geometry().toJson()).getJSONArray("coordinates").getDouble(1),
                                                new JSONObject(feature.geometry().toJson()).getJSONArray("coordinates").getDouble(0)
                                        );
                                        Log.d("points", pos.toString());
                                        CameraPosition position = new CameraPosition.Builder()
                                                .target(pos)
                                                .zoom(source.getClusterExpansionZoom(feature) + 0.1)
                                                .build();
                                        mapboxMap.animateCamera(
                                                CameraUpdateFactory.newCameraPosition(position),
                                                250
                                        );

                                    } catch (JSONException e) {
                                        continue;
                                    }
                                } else {
                                    Log.d("points", "not zooming!");
                                    Log.d("points", "contains number of elements = to " + source.getClusterLeaves(feature, feature.getNumberProperty("point_count").longValue(), 0).features().size());
                                }
                                Log.d("points", "zoom level " + source.getClusterExpansionZoom(feature));
                            }
                            return true;
                        }
                        featureList = mapboxMap.queryRenderedFeatures(pointf, "points");
                        if (featureList.size() > 0) {
                            for (com.mapbox.geojson.Feature feature : featureList) {
                                Log.d("points", feature.toJson());
                            }
                            return true;
                        }
                        return false;
                    });

                });
            });
        }
    }

    private class GetPostsTask extends AsyncTask<LatLng, Void, FeatureCollection> {
        protected FeatureCollection doInBackground(LatLng... positions) {
            return new Backend().getPosts(positions[0]);
        }

        @Override
        protected void onPostExecute(FeatureCollection featureCollection) {
            loadMap(featureCollection);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);

        Log.d("navbar", Integer.toString(getNavBarHeight()));

        Mapbox.getInstance(this, "pk.eyJ1IjoicGllbWFuMjIwMSIsImEiOiJjanc3ZDd0cTIxanc5NDBvdzFjanN2dmFzIn0.HWPlehe-8Hp6YvGnVH-0BQ");
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mapView);
        FloatingActionButton postFab = findViewById(R.id.postFab);
        spinner = findViewById(R.id.spinner);
        postFab.setOnClickListener(v -> {
            Log.d("post", "posted!");
        });
        FloatingActionButton locateFab = findViewById(R.id.locateFab);
        locateFab.setOnClickListener(v -> jumpToUserLocation());
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) postFab.getLayoutParams();
        params.bottomMargin += getNavBarHeight();

        mapView.onCreate(savedInstanceState);
        mapView.addOnDidFinishRenderingMapListener(fully -> {
            Log.d("style", "finished rendering");
            finishedLoading = true;
            onUserFirstLocated();
        });

        checkLocationPermission();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        checkLocationPermission();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
