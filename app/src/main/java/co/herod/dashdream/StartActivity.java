package co.herod.dashdream;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }

    @Override
    public void onStart() {
        super.onStart();
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.android.systemui", "com.android.systemui.Somnambulator");
        startActivity(intent);
        finish();
    }

}
