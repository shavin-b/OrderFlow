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

    private final MutableLiveData<Integer> todayReplies = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> activeKeywords = new MutableLiveData<>(0);
    private final MutableLiveData<String> recentActivity = new MutableLiveData<>("No recent activity.");

    private final com.orderflow.data.repository.KeywordRepository keywordRepository;
    private final com.orderflow.data.repository.LogRepository logRepository;
    private com.google.firebase.firestore.ListenerRegistration keywordListener;
    private com.google.firebase.firestore.ListenerRegistration logListener;

    public DashboardViewModel() {
        this.keywordRepository = new com.orderflow.data.repository.KeywordRepository();
        this.logRepository = new com.orderflow.data.repository.LogRepository();
        startListening();
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

    private void startListening() {
        // Listen to Active Keywords
        keywordListener = keywordRepository.listenToKeywords(new com.orderflow.data.repository.KeywordRepository.KeywordListCallback() {
            @Override
            public void onDataLoaded(java.util.List<com.orderflow.data.model.Keyword> keywords) {
                int count = 0;
                if (keywords != null) {
                    for (com.orderflow.data.model.Keyword k : keywords) {
                        if (k.isEnabled()) count++;
                    }
                }
                activeKeywords.setValue(count);
            }

            @Override
            public void onError(String errorMessage) {
                android.util.Log.e("DashboardVM", "Error fetching keywords: " + errorMessage);
            }
        });

        // Listen to Recent Activity and Stats
        logListener = logRepository.listenToLogs(new com.orderflow.data.repository.LogRepository.LogListCallback() {
            @Override
            public void onDataLoaded(java.util.List<com.orderflow.data.model.MessageLog> logs) {
                if (logs == null || logs.isEmpty()) {
                    todayReplies.setValue(0);
                    recentActivity.setValue("No recent activity.");
                    return;
                }

                // 1. Calculate Replies Today
                int count = 0;
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0);
                long startOfDay = cal.getTimeInMillis();

                for (com.orderflow.data.model.MessageLog log : logs) {
                    if (log.isReplied() && log.getTimestamp() != null && 
                        log.getTimestamp().toDate().getTime() >= startOfDay) {
                        count++;
                    }
                }
                todayReplies.setValue(count);

                // 2. Format Recent Activity (Last 3 logs)
                StringBuilder sb = new java.lang.StringBuilder();
                int limit = Math.min(logs.size(), 3);
                for (int i = 0; i < limit; i++) {
                    com.orderflow.data.model.MessageLog log = logs.get(i);
                    String time = "Just now";
                    if (log.getTimestamp() != null) {
                        long diff = System.currentTimeMillis() - log.getTimestamp().toDate().getTime();
                        long mins = diff / (60 * 1000);
                        if (mins < 1) time = "Just now";
                        else if (mins < 60) time = mins + "m ago";
                        else time = (mins / 60) + "h ago";
                    }
                    
                    String statusText = log.isReplied() ? "Replied to" : "Blocked (Cooldown)";
                    if (log.isNoMatch()) statusText = "No Match for";
                    
                    sb.append("• ").append(statusText).append(" ").append(log.getDisplayName()).append(" (").append(time).append(")\n");
                }
                recentActivity.setValue(sb.toString().trim());
            }

            @Override
            public void onError(String errorMessage) {
                android.util.Log.e("DashboardVM", "Error fetching logs: " + errorMessage);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (keywordListener != null) keywordListener.remove();
        if (logListener != null) logListener.remove();
    }
}
