package com.example.lostitfoundit.homeactivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.lostitfoundit.ContactMeActivity;
import com.example.lostitfoundit.LoginActivity;
import com.example.lostitfoundit.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        /* Initialise the Firebase application to check if a user is already logged in. */
        mAuth = FirebaseAuth.getInstance();

        /* Set The Action Bar As The Main Toolbar */
        final Toolbar mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        /* Initialise the views that are needed for fragment movement */
        initialiseViewsTabLayout();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present */
        getMenuInflater().inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final Intent intent;
        if (item.getItemId() == R.id.logout) {
            try {
                mAuth.signOut();
                intent = new Intent(HomeActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (item.getItemId() == R.id.contact) {
            intent = new Intent(HomeActivity.this, ContactMeActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        return super.onOptionsItemSelected(item);
    }

    /* This method initialises the tabs for app movement */
    private void initialiseViewsTabLayout() {
        final TabLayout tabDisplay = findViewById(R.id.tab_display);
        final ViewPager viewPager = findViewById(R.id.view_pager);
        final SliderAdapter adapter = new SliderAdapter(getSupportFragmentManager());

        adapter.addFragment(new MapSearchFragment(), "Map");
        adapter.addFragment(new ItemListFragment(), "Item List");
        adapter.addFragment(new ProfileFragment(), "Profile");
        viewPager.setAdapter(adapter);
        tabDisplay.setupWithViewPager(viewPager);

        TabLayout.Tab tab = tabDisplay.getTabAt(1);
        tab.select();
    }

    /* This is just the local adapter that is used for populating the tabs */
    private class SliderAdapter extends FragmentStatePagerAdapter {

        private List<Fragment> fragmentList = new ArrayList<>();
        private List<String> fragmentNameList = new ArrayList<>();

        private SliderAdapter(@NonNull FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        /* This is a method for setting up and populating the adapter */
        private void addFragment(Fragment fragment, String fragmentName) {
            fragmentList.add(fragment);
            fragmentNameList.add(fragmentName);
        }

        @Override
        public String getPageTitle(int position) {
            return fragmentNameList.get(position);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }
    }
}