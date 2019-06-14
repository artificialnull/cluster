package tk.gabdeg.near;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.mapbox.mapboxsdk.location.LocationComponent;

public class InfoFragment extends Fragment {

    public static String POST_KEY = "post_serialized";

    private View layout;

    boolean setText(int id, String text) {
        try {
            ((TextView) layout.findViewById(id)).setText(text);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    void formatPost(Post post) {
        setText(R.id.post_user, post.user);

        long passed = (System.currentTimeMillis() / 1000) - post.time;
        String passedStr = "Posted ";
        if (passed >= 3600) {
            passedStr += (passed / 3600) + "h";
        } else if (passed >= 60) {
            passedStr += (passed / 60) + "m";
        } else {
            passedStr += passed + "s";
        }
        passedStr += " ago";
        setText(R.id.post_time, passedStr);

        if (post.text != null && !post.text.equals("")) {
            setText(R.id.post_text, post.text);
        }
        if (post.image != null && !post.image.equals("")) {
            byte[] decoded = Base64.decode(post.image.getBytes(), Base64.DEFAULT);
            Bitmap image = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            ((ImageView) layout.findViewById(R.id.post_image)).setImageBitmap(image);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_info, container, false);

        layout.findViewById(R.id.close_button).setOnClickListener(
                v -> {
                    layout = null;
                    ((MapActivity) getActivity()).removeInfoFragment();
                }
        );

        layout.findViewById(R.id.toggle_button).setOnClickListener(
                v -> {
                    boolean expando = ((MapActivity) getActivity()).toggleInfoFragmentSize();
                    ((ImageButton) layout.findViewById(R.id.toggle_button)).setImageResource(expando ? R.drawable.chevron_down : R.drawable.chevron_up);
                }
        );

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) layout.findViewById(R.id.post_content_layout).getLayoutParams();
        params.bottomMargin = ((MapActivity) getActivity()).getNavBarHeight();

        layout.setOnTouchListener((v, event) -> {
            layout.performClick();
            return true;
        });

        Post post = new Gson().fromJson(this.getArguments().getString(POST_KEY), Post.class);
        setText(R.id.post_user, post.user);

        new GetPostContentTask().execute(post);

        return layout;
    }

    private class GetPostContentTask extends AsyncTask<Post, Void, Post> {
        @Override
        protected Post doInBackground(Post... posts) {
            Post ret = new Backend().getPost(posts[0].id);
            if (ret == null) {
                posts[0].user = null;
                return posts[0];
            }
            return ret;
        }

        @Override
        protected void onPostExecute(Post post) {
            Log.d("post", "got");
            if (layout == null) {
                return;
            }
            if (post.user != null) {
                formatPost(post);
                new Handler().postDelayed(
                        () -> new GetPostContentTask().execute(post), 5000
                );
            } else {
                new GetPostContentTask().execute(post);
            }
        }
    }
}