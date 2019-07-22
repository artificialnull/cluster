package tk.gabdeg.cluster;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

public class CreateProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile_create);
    }

    public void openVerify(View v) {
        TextInputEditText nameEdit = findViewById(R.id.profile_create_name);
        TextInputLayout nameEditLayout = findViewById(R.id.profile_create_name_layout);
        nameEditLayout.setErrorEnabled(false);
        TextInputEditText phoneEdit = findViewById(R.id.profile_create_phone);
        TextInputLayout phoneEditLayout = findViewById(R.id.profile_create_phone_layout);
        phoneEditLayout.setErrorEnabled(false);

        if (nameEdit.getText().toString().isEmpty()) {
            nameEditLayout.setError("Please enter a name");
            nameEditLayout.setErrorEnabled(true);
        }

        if (phoneEdit.getText().toString().length() != 10) {
            phoneEditLayout.setError("Please enter a valid phone number");
            phoneEditLayout.setErrorEnabled(true);
        }

        if(!(nameEditLayout.isErrorEnabled() || phoneEditLayout.isErrorEnabled())) {
            new SignUpTask().execute(
                    nameEdit.getText().toString(),
                    phoneEdit.getText().toString()
                    );
            SharedPreferences prefs = getSharedPreferences(getString(R.string.preferenceKey), MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(Backend.PREF_USERNAME, phoneEdit.getText().toString());
            editor.commit();
        }
    }

    void actuallyOpenVerify() {
        startActivity(new Intent(this, VerifyActivity.class));
        finish();
    }

    private class SignUpTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected void onPreExecute() {
            findViewById(R.id.welcome_loading).setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(String... strings) {
            String name = strings[0];
            String phone = strings[1];
            return Backend.createProfile(name, phone);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            findViewById(R.id.welcome_loading).setVisibility(View.INVISIBLE);
            try {
                if (jsonObject.getBoolean("status")) {
                    actuallyOpenVerify();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
