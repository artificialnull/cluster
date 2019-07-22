package tk.gabdeg.cluster;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextSwitcher;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private ArrayList<Post> posts;
    private Context context;
    public PostAdapter(Context ctx, ArrayList<Post> posts) {
        this.posts = (ArrayList<Post>) posts.clone();
        this.context = ctx;
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public PostViewHolder(View v) {
            super(v);
            view = v;
            view.findViewById(R.id.star_button).setEnabled(false);
            ((CheckBox) view.findViewById(R.id.star_button)).setChecked(true);

            TextSwitcher postUser = view.findViewById(R.id.post_preview);
            postUser.setCurrentText("Loading...");
            postUser.setOutAnimation(context, android.R.anim.slide_out_right);
            postUser.setInAnimation(context, android.R.anim.slide_in_left);

            TextSwitcher postTime = view.findViewById(R.id.post_time);
            postTime.setCurrentText("");
            postTime.setOutAnimation(context, android.R.anim.slide_out_right);
            postTime.setInAnimation(context, android.R.anim.slide_in_left);
        }

        boolean setText(int id, String text) {
            try {
                ((TextView) view.findViewById(id)).setText(text);
            } catch (Exception e) {
                try {
                    ((TextSwitcher) view.findViewById(id)).setText(text);
                    return true;
                } catch (Exception f) {
                    return false;
                }
            }
            return true;
        }
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PostViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_post, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post put = posts.get(position);
        holder.setText(R.id.post_preview, put.text);
        holder.setText(R.id.post_time, put.ago());
        holder.setText(R.id.star_count, Integer.toString(put.stars));

    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
}