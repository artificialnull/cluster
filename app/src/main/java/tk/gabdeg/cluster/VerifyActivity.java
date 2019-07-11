package tk.gabdeg.cluster;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

public class VerifyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
    }

    public void verifyAccount(View v) {
        TextInputEditText passEdit = findViewById(R.id.profile_verify_code);

        SharedPreferences prefs = getSharedPreferences(getString(R.string.preferenceKey), MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Backend.PREF_PASSWORD, passEdit.getText().toString());
        editor.commit();

        Backend.setCredentials(this);

        new VerifyAccountTask().execute();
    }

    void openPermissions() {
        startActivity(new Intent(this, PermissionsActivity.class));
        finish();
    }

    private class VerifyAccountTask extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected void onPreExecute() {
            findViewById(R.id.welcome_loading).setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(Void... strings) {
            return Backend.setCredentials(getApplicationContext()) ? Backend.verifyProfile() : null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            findViewById(R.id.welcome_loading).setVisibility(View.INVISIBLE);

            try {
                if (jsonObject.getBoolean("status")) {
                    openPermissions();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
