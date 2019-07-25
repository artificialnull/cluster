package tk.gabdeg.cluster;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.TextSwitcher;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class InfoFragment extends Fragment {

    View layout;
    MapActivity activity;
    float size = 0.5f;

    boolean setText(int id, String text) {
        try {
            ((TextView) this.layout.findViewById(id)).setText(text);
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

    void rotateToggleButton() {
        ImageButton but = layout.findViewById(R.id.toggle_button);
        but.animate().rotation(but.getRotation() + 180).setInterpolator(new AccelerateDecelerateInterpolator());
    }

    void bindActionsToLayout() {
        if (layout != null) {
            activity = ((MapActivity) getActivity());

            layout.findViewById(R.id.close_button).setOnClickListener(
                    v -> {
                        layout = null;
                        activity.onBackPressed();
                    }
            );
            if (activity.backStackNotEmpty()) ((ImageButton) layout.findViewById(R.id.close_button)).setImageResource(R.drawable.close_post);

            layout.findViewById(R.id.toggle_button).setOnClickListener(
                    v -> {
                        activity.toggleInfoFragmentSize();
                        rotateToggleButton();
                    }
            );
            layout.setOnTouchListener((v, event) -> {
                layout.performClick();
                return true;
            });
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) layout.findViewById(R.id.post_content_layout).getLayoutParams();
            params.bottomMargin += ((MapActivity) getActivity()).getNavBarHeight();
        }
    }
}
