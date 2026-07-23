package com.orderflow.ui.statistics;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.ListenerRegistration;
import com.orderflow.data.model.MessageLog;
import com.orderflow.data.repository.LogRepository;
import com.orderflow.utils.Resource;

import java.util.List;

/**
 * STATISTICS VIEW MODEL
 *
 * Fetches logs and computes business performance metrics:
 * - Total Messages Received
 * - Total Auto-Replies Sent
 * - Total Messages Skipped (No Match / Cooldown)
 * - Success Rate (%)
 */
public class StatisticsViewModel extends ViewModel {

    public static class StatsData {
        public int totalMessages = 0;
        public int totalReplied = 0;
        public int totalNoMatch = 0;
        public int totalCooldown = 0;
        public int totalError = 0;
        public int successRate = 0;
    }

    private final LogRepository repository;
    private final MutableLiveData<Resource<StatsData>> statsState = new MutableLiveData<>();
    private ListenerRegistration listenerRegistration;

    public StatisticsViewModel() {
        repository = new LogRepository();
        loadStats();
    }

    public LiveData<Resource<StatsData>> getStatsState() {
        return statsState;
    }

    private void loadStats() {
        statsState.setValue(Resource.loading(null));
        listenerRegistration = repository.listenToLogs(new LogRepository.LogListCallback() {
            @Override
            public void onDataLoaded(List<MessageLog> logs) {
                StatsData stats = calculateStats(logs);
                statsState.setValue(Resource.success(stats));
            }

            @Override
            public void onError(String errorMessage) {
                statsState.setValue(Resource.error(errorMessage, null));
            }
        });
    }

    private StatsData calculateStats(List<MessageLog> logs) {
        StatsData stats = new StatsData();
        if (logs == null || logs.isEmpty()) return stats;

        stats.totalMessages = logs.size();

        for (MessageLog log : logs) {
            String status = log.getStatus() != null ? log.getStatus() : MessageLog.STATUS_NO_MATCH;
            switch (status) {
                case MessageLog.STATUS_REPLIED:
                    stats.totalReplied++;
                    break;
                case MessageLog.STATUS_NO_MATCH:
                    stats.totalNoMatch++;
                    break;
                case MessageLog.STATUS_COOLDOWN:
                    stats.totalCooldown++;
                    break;
                case MessageLog.STATUS_ERROR:
                    stats.totalError++;
                    break;
            }
        }

        if (stats.totalMessages > 0) {
            stats.successRate = (int) Math.round(((double) stats.totalReplied / stats.totalMessages) * 100.0);
        }

        return stats;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}
