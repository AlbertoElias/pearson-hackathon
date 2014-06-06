package com.ft.ftwear.activities;

import android.app.Activity;
import android.os.Bundle;

import com.ft.ftwear.R;
import com.ft.ftwear.fragments.SettingsFragment;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsFragment())
                    .commit();
        }
    }
}
