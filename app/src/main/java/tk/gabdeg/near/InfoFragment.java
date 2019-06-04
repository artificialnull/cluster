package tk.gabdeg.near;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
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
        post.image = "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";
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
                v -> ((MapActivity) getActivity()).removeInfoFragment()
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
            if (post.user != null) {
                formatPost(post);
            } else {
                new GetPostContentTask().execute(post);
            }
        }
    }
}
