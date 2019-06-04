package tk.gabdeg.near;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

public class InfoFragment extends Fragment {

    public static String POST_KEY = "post_serialized";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout =  inflater.inflate(R.layout.fragment_info, container, false);

        layout.findViewById(R.id.close_button).setOnClickListener(
                v -> ((MapActivity) getActivity()).removeInfoFragment()
        );

        layout.setOnTouchListener((v, event) -> {
            layout.performClick();
            return true;
        });

        Post post = new Gson().fromJson(this.getArguments().getString(POST_KEY), Post.class);

        ((TextView) layout.findViewById(R.id.post_user)).setText(post.user);

        return layout;
    }
}
