package tk.gabdeg.cluster;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.DialogFragment;

import com.google.gson.Gson;

public class ConfirmPostSubmissionDialog extends DialogFragment {

    static ConfirmPostSubmissionDialog newInstance(Post current, int limit) {
        ConfirmPostSubmissionDialog dialog = new ConfirmPostSubmissionDialog();
        Bundle bundle = new Bundle();
        bundle.putString("current", new Gson().toJson(current));
        bundle.putInt("limit", limit);

        dialog.setArguments(bundle);
        return dialog;
    }

    Post approved;

    public interface ConfirmPostSubmissionDialogListener {
        public void onDialogPositiveClick(ConfirmPostSubmissionDialog dialog);
        public void onDialogNegativeClick(ConfirmPostSubmissionDialog dialog);
    }

    ConfirmPostSubmissionDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (ConfirmPostSubmissionDialogListener) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        approved = new Gson().fromJson(getArguments().getString("current"), Post.class);
        int limit = getArguments().getInt("limit");
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.DialogTheme));
        builder.setMessage("You have reached your limit of " + limit + " posts. Submitting will remove your earliest visible post, though your stars will remain.")
                .setPositiveButton("submit", (dialogInterface, i) -> listener.onDialogPositiveClick(ConfirmPostSubmissionDialog.this))
                .setNegativeButton("cancel", (dialogInterface, i) -> listener.onDialogNegativeClick(ConfirmPostSubmissionDialog.this));
        return builder.create();
    }
}
