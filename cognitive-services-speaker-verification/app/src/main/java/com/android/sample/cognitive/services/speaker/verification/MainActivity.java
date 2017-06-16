package com.android.sample.cognitive.services.speaker.verification;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                showProfileListFragment();
                return true;
            case R.id.navigation_dashboard:
                showFragment(VerificationFragment.newInstance());
                return true;
        }
        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        showProfileListFragment();
    }

    public void showAudioRegisterFragment(String identificationProfileId, int remainingEnrollmentsCount) {
        Fragment fragment =
                new AudioRegisterFragmentBuilder(remainingEnrollmentsCount, identificationProfileId)
                        .build();
        showFragment(fragment);
    }

    public void showProfileListFragment() {
        showFragment(ProfileListFragment.newInstance());
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack(fragment.getClass().getSimpleName());
        transaction.replace(R.id.content, fragment, fragment.getClass().getSimpleName());
        transaction.commit();
    }
}
