package com.smartexpense.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.smartexpense.R;
import com.smartexpense.fragments.AnalyticsFragment;
import com.smartexpense.fragments.DashboardFragment;
import com.smartexpense.fragments.HistoryFragment;
import com.smartexpense.fragments.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private FloatingActionButton fabAddExpense;

    private final Fragment dashboardFragment = new DashboardFragment();
    private final Fragment historyFragment = new HistoryFragment();
    private final Fragment analyticsFragment = new AnalyticsFragment();
    private final Fragment settingsFragment = new SettingsFragment();
    private Fragment activeFragment = dashboardFragment;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                // Permission result handled
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupNavigation(savedInstanceState);
        checkNotificationPermission();
    }

    private void initViews() {
        bottomNav = findViewById(R.id.bottom_navigation);
        fabAddExpense = findViewById(R.id.fab_add_expense);

        fabAddExpense.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditExpenseActivity.class);
            startActivity(intent);
        });
    }

    private void setupNavigation(Bundle savedInstanceState) {
        FragmentManager fm = getSupportFragmentManager();

        if (savedInstanceState == null) {
            fm.beginTransaction()
                    .add(R.id.fragment_container, settingsFragment, "4").hide(settingsFragment)
                    .add(R.id.fragment_container, analyticsFragment, "3").hide(analyticsFragment)
                    .add(R.id.fragment_container, historyFragment, "2").hide(historyFragment)
                    .add(R.id.fragment_container, dashboardFragment, "1")
                    .commit();
            activeFragment = dashboardFragment;
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                switchFragment(dashboardFragment);
                return true;
            } else if (itemId == R.id.nav_history) {
                switchFragment(historyFragment);
                return true;
            } else if (itemId == R.id.nav_analytics) {
                switchFragment(analyticsFragment);
                return true;
            } else if (itemId == R.id.nav_settings) {
                switchFragment(settingsFragment);
                return true;
            }
            return false;
        });
    }

    private void switchFragment(Fragment targetFragment) {
        if (activeFragment != targetFragment) {
            getSupportFragmentManager().beginTransaction()
                    .hide(activeFragment)
                    .show(targetFragment)
                    .commit();
            activeFragment = targetFragment;
        }
    }

    public void navigateToTab(int navItemId) {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(navItemId);
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}
