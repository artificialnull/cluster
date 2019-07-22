package tk.gabdeg.cluster;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("splash", "splashing!");

        Intent intent;
        if (Backend.setCredentials(this)) {
            intent = new Intent(getApplicationContext(), MapActivity.class);
        } else {
            /*
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(Backend.PREF_USERNAME, "2819498189");
            editor.putString(Backend.PREF_PASSWORD, "SQDGMR8E");
            editor.commit();
            Backend.setCredentials(prefs);
            */

            intent = new Intent(getApplicationContext(), WelcomeActivity.class);
        }
        startActivity(intent);
        finish();
    }

}
