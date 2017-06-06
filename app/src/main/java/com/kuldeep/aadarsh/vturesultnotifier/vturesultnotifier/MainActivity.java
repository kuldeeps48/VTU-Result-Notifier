package com.kuldeep.aadarsh.vturesultnotifier.vturesultnotifier;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by aadarsha on 6/5/17.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Button buttonOldScheme, buttonCbcsScheme, buttonRevaluation, buttonAboutUs;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeTheme.onActivityCreateSetTheme(this);
        setContentView(R.layout.activity_main);

        buttonOldScheme = (Button) findViewById(R.id.button_old_scheme);
        buttonOldScheme.setOnClickListener(this);
        buttonCbcsScheme = (Button) findViewById(R.id.button_cbcs_scheme);
        buttonCbcsScheme.setOnClickListener(this);
        buttonRevaluation = (Button) findViewById(R.id.button_revaluation);
        buttonRevaluation.setOnClickListener(this);
        buttonAboutUs = (Button) findViewById(R.id.button_about_us);
        buttonAboutUs.setOnClickListener(this);
        }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_old_scheme:
                intent = new Intent(MainActivity.this, UsnInputActivity.class);
                intent.putExtra("RESULT_TYPE", "OLD");
                startActivity(intent);
                break;

            case R.id.button_cbcs_scheme:
                intent = new Intent(MainActivity.this, UsnInputActivity.class);
                intent.putExtra("RESULT_TYPE", "CBCS");
                startActivity(intent);
                break;

            case R.id.button_revaluation:
                intent = new Intent(MainActivity.this, UsnInputActivity.class);
                intent.putExtra("RESULT_TYPE", "REVALUATION");
                startActivity(intent);
                break;
        }
    }
}