package com.android.sample.cognitive.services.speaker.identification;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_enrollment:
                showFragment(ProfileListFragment.newInstance());
                return true;
            case R.id.navigation_recognition:
                showFragment(IdentificationFragment.newInstance());
                return true;
        }
        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set empty.
        setTitle("");

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        showFragment(ProfileListFragment.newInstance());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void showAudioRegisterFragment(String identificationProfileId) {
        Fragment fragment =
                new AudioRegisterFragmentBuilder(identificationProfileId)
                        .build();
        showFragment(fragment);
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack(fragment.getClass().getSimpleName());
        transaction.replace(R.id.content, fragment, fragment.getClass().getSimpleName());
        transaction.commit();
    }
}