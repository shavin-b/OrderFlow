package com.orderflow.ui.logs;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.ListenerRegistration;
import com.orderflow.data.model.MessageLog;
import com.orderflow.data.repository.LogRepository;
import com.orderflow.utils.Resource;

import java.util.List;

/**
 * LOGS VIEW MODEL
 *
 * Exposes real-time list of MessageLogs to the LogsFragment.
 */
public class LogsViewModel extends ViewModel {

    private final LogRepository repository;
    private final MutableLiveData<Resource<List<MessageLog>>> logsState = new MutableLiveData<>();
    private ListenerRegistration listenerRegistration;

    public LogsViewModel() {
        repository = new LogRepository();
        loadLogs();
    }

    public LiveData<Resource<List<MessageLog>>> getLogsState() {
        return logsState;
    }

    private void loadLogs() {
        logsState.setValue(Resource.loading(null));
        listenerRegistration = repository.listenToLogs(new LogRepository.LogListCallback() {
            @Override
            public void onDataLoaded(List<MessageLog> logs) {
                logsState.setValue(Resource.success(logs));
            }

            @Override
            public void onError(String errorMessage) {
                logsState.setValue(Resource.error(errorMessage, null));
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}
