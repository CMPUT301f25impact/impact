package com.example.impact.view;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.impact.view.adapter.AdminPagerAdapter;
import com.google.android.material.appbar.MaterialToolbar;

import com.example.impact.R;
import com.example.impact.utils.AppSession;
import com.example.impact.utils.role.AdminDb;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Hosts the admin dashboard tabs and routes logout/device-binding cleanup through {@link AdminDb}.
 * All session context (current user id, role) is sourced from {@link AppSession}.
 */
public class AdminActivity extends AppCompatActivity {

    // These are the tabs that can be navigated in the dashboard
    private static final String[] tabs = {"Events", "Images", "Profiles"};

    private String adminId;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        adminId = AppSession.getUserId();

        MaterialToolbar toolbar = findViewById(R.id.adminToolbar);
        tabLayout = findViewById(R.id.adminDashboardTabs);
        viewPager = findViewById(R.id.adminDashboardViewPager);

        AdminPagerAdapter pagerAdapter = new AdminPagerAdapter(tabs, this);
        viewPager.setAdapter(pagerAdapter);
        initializeTabs();

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.admin_dashboard_title);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_session, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            performLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Configures the tab layout + view pager to display Events, Images, and Profiles pages.
     */
    private void initializeTabs() {
        new TabLayoutMediator(tabLayout, viewPager, (tab, pos) -> {
            if (pos < tabs.length) {
                tab.setText(tabs[pos]);
            }
        }).attach();
    }

    /**
     * Clears the admin's device binding via {@link AdminDb#clearDeviceBinding(String)} and returns to login.
     */
    private void performLogout() {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (TextUtils.isEmpty(deviceId)) {
            navigateToLogin();
            return;
        }

        AdminDb.clearDeviceBinding(deviceId)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, R.string.logout_success, Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                })
                .addOnFailureListener(error -> {
                    Toast.makeText(this, R.string.logout_error, Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                });
    }

    private void navigateToLogin() {
        AppSession.initialize(null);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
