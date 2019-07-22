package tk.gabdeg.cluster;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

public class PostFragment extends InfoFragment {

    public static String POST_KEY = "post_serialized";

    String hash = "";
    String imageHash = "";
    int postID = 0;
    int starCount = 0;
    private Bitmap image;

    void formatPost(Post post) {

        starCount = post.stars;
        setText(R.id.star_count, "" + post.stars);

        if (((TextView) ((TextSwitcher) layout.findViewById(R.id.post_time)).getCurrentView()).getText().equals(post.ago())) {
            return;
        }
        setText(R.id.post_time, post.ago());

        if (post.toString().equals(hash)) {
            return;
        }
        hash = post.toString();
        new GetStarredStatusTask().execute(post);

        setText(R.id.post_user, post.user);

        AlphaAnimation animation = new AlphaAnimation(0, 1);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.setDuration(500);

        if (post.text != null && !post.text.equals("")) {
            layout.findViewById(R.id.post_text).startAnimation(animation);
            setText(R.id.post_text, post.text);
        }
        if (image == null) {
            return;
        }
        layout.findViewById(R.id.post_image).startAnimation(animation);
        ((ImageView) layout.findViewById(R.id.post_image)).setImageBitmap(image);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_info, container, false);
        bindActionsToLayout();

        RotateAnimation rotate = new RotateAnimation(
                0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotate.setDuration(750);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setInterpolator(new LinearInterpolator());
        layout.findViewById(R.id.star_button).startAnimation(rotate);

        layout.findViewById(R.id.star_button).setOnClickListener(v -> {
            Log.d("checkmark", "changed!");
            if (postID != 0) {
                Post put = new Post();
                put.id = postID;
                new UpdateStarredStatusTask().execute(put);
            }
        });

        Post post = new Gson().fromJson(this.getArguments().getString(POST_KEY), Post.class);
        TextSwitcher postUser = layout.findViewById(R.id.post_user);
        postUser.setCurrentText("Post #" + post.id);
        postUser.setOutAnimation(getContext(), android.R.anim.slide_out_right);
        postUser.setInAnimation(getContext(), android.R.anim.slide_in_left);

        TextSwitcher postTime = layout.findViewById(R.id.post_time);
        postTime.setCurrentText("Loading...");
        postTime.setOutAnimation(getContext(), android.R.anim.slide_out_right);
        postTime.setInAnimation(getContext(), android.R.anim.slide_in_left);

        new GetPostContentTask().execute(post);

        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        layout = null;
        Log.d("fragment", "DESTROYEED");
    }

    private class GetPostContentTask extends AsyncTask<Post, Void, Post> {
        @Override
        protected Post doInBackground(Post... posts) {
            Log.d("info-fragment", "getting post");
            Post ret = Backend.getPost(posts[0].id);
            if (ret == null) {
                posts[0].user = null;
                return posts[0];
            }

            if (ret.image != null && !ret.image.equals("")) {
                if (!ret.image.equals(imageHash)) {
                    Log.d("info-fragment", "getting image");
                    String imageStr = Backend.getPostImage(ret.id);
                    byte[] decoded = Base64.decode(imageStr.getBytes(), Base64.DEFAULT);
                    image = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);

                    imageHash = ret.image;
                } else {
                    Log.d("info-fragment", imageHash + " = " + ret.image);
                }
            }

            return ret;
        }

        @Override
        protected void onPostExecute(Post post) {
            if (layout == null) {
                Log.d("post", "quid");
                return;
            }
            Log.d("post", "got");
            if (post.user != null) {
                Log.d("post", post.toString());
                formatPost(post);
                postID = post.id;
                new Handler().postDelayed(
                        () -> new GetPostContentTask().execute(post), 5000
                );
            } else {
                new GetPostContentTask().execute(post);
            }
        }
    }

    private class GetStarredStatusTask extends AsyncTask<Post, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Post... posts) {
            return Backend.getStarredStatus(posts[0].id);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            ((CheckBox) layout.findViewById(R.id.star_button)).setChecked(aBoolean);
            layout.findViewById(R.id.star_button).setEnabled(true);
            if (layout.findViewById(R.id.star_button).getAnimation() != null) {
                layout.findViewById(R.id.star_button).getAnimation().setRepeatCount(0);
            }
        }
    }

    private class UpdateStarredStatusTask extends AsyncTask<Post, Void, Boolean> {
        boolean checkedStatus;

        @Override
        protected void onPreExecute() {
            RotateAnimation rotate = new RotateAnimation(
                    0, 72,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );
            rotate.setDuration(750);
            rotate.setInterpolator(new AnticipateOvershootInterpolator());
            layout.findViewById(R.id.star_button).startAnimation(rotate);
            checkedStatus = ((CheckBox) layout.findViewById(R.id.star_button)).isChecked();
            layout.findViewById(R.id.star_button).setEnabled(false);
            starCount += (checkedStatus ? 1 : -1);
            if (starCount < 0) {
                starCount = 0;
                if (checkedStatus) {
                    starCount = 1;
                }
            }
            setText(R.id.star_count, "" + starCount);
        }

        @Override
        protected Boolean doInBackground(Post... posts) {
            return Backend.setStarredStatus(posts[0].id, checkedStatus);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            ((CheckBox) layout.findViewById(R.id.star_button)).setChecked(aBoolean);
            layout.findViewById(R.id.star_button).setEnabled(true);
        }
    }


}