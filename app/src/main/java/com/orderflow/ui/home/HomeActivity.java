package com.orderflow.ui.home;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.orderflow.R;
import com.orderflow.databinding.ActivityHomeBinding;

/**
 * HOME ACTIVITY
 *
 * Purpose:
 * The main container for the application once the user is logged in.
 * Uses the Navigation Component to swap Fragments in and out of the NavHostFragment.
 * Uses a BottomNavigationView to switch between the 5 top-level destinations.
 */
public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Edge-to-edge layout support.
        // We only apply insets to the top (status bar) and bottom (navigation bar)
        // so that the BottomNavigationView doesn't draw underneath the system nav bar.
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, systemBars.bottom);
            return insets;
        });

        setupNavigation();
    }

    private void setupNavigation() {
        // Retrieve the NavController from the NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            
            // Link the BottomNavigationView to the NavController.
            // This works magically because the menu item IDs match the fragment IDs in nav_graph.xml.
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
        }
    }
}
