package tk.gabdeg.near;

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
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

public class InfoFragment extends Fragment {

    public static String POST_KEY = "post_serialized";

    private View layout;
    private Bitmap image;
    String hash = "";
    int postID = 0;

    boolean setText(int id, String text) {
        try {
            ((TextView) layout.findViewById(id)).setText(text);
        } catch (Exception e) {
            try {
                ((TextSwitcher) layout.findViewById(id)).setText(text);
                return true;
            } catch (Exception f) {
                return false;
            }
        }
        return true;
    }

    void formatPost(Post post) {

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

        if (((TextView) ((TextSwitcher) layout.findViewById(R.id.post_time)).getCurrentView()).getText().equals(passedStr)) {
            return;
        }

        setText(R.id.post_time, passedStr);

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
            if (post.image != null && !post.image.equals("")) {
                byte[] decoded = Base64.decode(post.image.getBytes(), Base64.DEFAULT);
                image = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            } else {
                return;
            }
        }
        layout.findViewById(R.id.post_image).startAnimation(animation);
        ((ImageView) layout.findViewById(R.id.post_image)).setImageBitmap(image);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_info, container, false);

        RotateAnimation rotate = new RotateAnimation(
                0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotate.setDuration(750);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setInterpolator(new LinearInterpolator());
        layout.findViewById(R.id.star_button).startAnimation(rotate);

        layout.findViewById(R.id.close_button).setOnClickListener(
                v -> {
                    layout = null;
                    ((MapActivity) getActivity()).removeInfoFragment();
                }
        );

        layout.findViewById(R.id.toggle_button).setOnClickListener(
                v -> {
                    ((MapActivity) getActivity()).toggleInfoFragmentSize();
                    ImageButton but = layout.findViewById(R.id.toggle_button);
                    but.animate().rotation(but.getRotation() + 180).setInterpolator(new AccelerateDecelerateInterpolator());
                }
        );

        layout.findViewById(R.id.star_button).setOnClickListener(v -> {
            Log.d("checkmark", "changed!");
            if (postID != 0) {
                Post put = new Post();
                put.id = postID;
                new UpdateStarredStatusTask().execute(put);
            }
        });

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) layout.findViewById(R.id.post_content_layout).getLayoutParams();
        params.bottomMargin = ((MapActivity) getActivity()).getNavBarHeight();

        layout.setOnTouchListener((v, event) -> {
            layout.performClick();
            return true;
        });

        Post post = new Gson().fromJson(this.getArguments().getString(POST_KEY), Post.class);
        TextSwitcher postUser = layout.findViewById(R.id.post_user);
        postUser.setCurrentText("Loading...");
        postUser.setOutAnimation(getContext(), android.R.anim.slide_out_right);
        postUser.setInAnimation(getContext(), android.R.anim.slide_in_left);

        TextSwitcher postTime = layout.findViewById(R.id.post_time);
        postTime.setCurrentText("Post #" + post.id);
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
            Post ret = new Backend().getPost(posts[0].id);
            if (ret == null) {
                posts[0].user = null;
                return posts[0];
            }

            if (ret.image != null && !ret.image.equals("")) {
                byte[] decoded = Base64.decode(ret.image.getBytes(), Base64.DEFAULT);
                image = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
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
                Log.d("post" , post.toString());
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
            return new Backend().getStarredStatus(posts[0].id);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            ((CheckBox) layout.findViewById(R.id.star_button)).setChecked(aBoolean);
            layout.findViewById(R.id.star_button).setEnabled(true);
            layout.findViewById(R.id.star_button).getAnimation().setRepeatCount(0);
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
            rotate.setDuration(500);
            rotate.setInterpolator(new AnticipateOvershootInterpolator());
            layout.findViewById(R.id.star_button).startAnimation(rotate);
            checkedStatus = ((CheckBox) layout.findViewById(R.id.star_button)).isChecked();
            layout.findViewById(R.id.star_button).setEnabled(false);
        }

        @Override
        protected Boolean doInBackground(Post... posts) {
            return new Backend().setStarredStatus(posts[0].id, checkedStatus);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            ((CheckBox) layout.findViewById(R.id.star_button)).setChecked(aBoolean);
            layout.findViewById(R.id.star_button).setEnabled(true);
        }
    }


}