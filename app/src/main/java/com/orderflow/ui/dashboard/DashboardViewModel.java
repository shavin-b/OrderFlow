package com.orderflow.ui.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * DASHBOARD VIEW MODEL
 *
 * Purpose:
 * Provides summary statistics and state data to the DashboardFragment.
 * Currently uses mock data. Will be connected to Firestore in later phases.
 */
public class DashboardViewModel extends ViewModel {

    // Quick Stats
    private final MutableLiveData<Integer> todayReplies = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> activeKeywords = new MutableLiveData<>(0);

    // Mock Recent Activity Logs (We will use a proper Model later)
    private final MutableLiveData<String> recentActivity = new MutableLiveData<>("No recent activity.");

    public DashboardViewModel() {
        // TODO: In later phases, fetch this data from Firestore repositories.
        // For Phase 3, we mock some initial data.
        fetchSummaryStats();
    }

    public LiveData<Integer> getTodayReplies() {
        return todayReplies;
    }

    public LiveData<Integer> getActiveKeywords() {
        return activeKeywords;
    }

    public LiveData<String> getRecentActivity() {
        return recentActivity;
    }

    /**
     * Simulates fetching summary statistics from the database.
     */
    private void fetchSummaryStats() {
        // Mock data
        todayReplies.setValue(12);
        activeKeywords.setValue(5);
        recentActivity.setValue("• Replied to John Doe (10m ago)\n• Replied to Jane Smith (45m ago)");
    }
}
