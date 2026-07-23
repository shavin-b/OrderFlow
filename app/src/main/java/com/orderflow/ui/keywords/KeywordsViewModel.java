package com.orderflow.ui.keywords;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.ListenerRegistration;
import com.orderflow.data.model.Keyword;
import com.orderflow.data.repository.KeywordRepository;
import com.orderflow.utils.Resource;

import java.util.List;

/**
 * KEYWORDS VIEW MODEL
 *
 * Purpose:
 * Connects the UI to the KeywordRepository.
 * Manages the real-time stream of keywords and operation states.
 */
public class KeywordsViewModel extends ViewModel {

    private final KeywordRepository repository;
    private ListenerRegistration keywordListener;

    // State for the list of keywords
    private final MutableLiveData<Resource<List<Keyword>>> keywordsListState = new MutableLiveData<>();

    // State for individual operations (Add, Edit, Delete)
    private final MutableLiveData<Resource<Boolean>> operationState = new MutableLiveData<>();

    public KeywordsViewModel() {
        this.repository = new KeywordRepository();
        startListening();
    }

    public LiveData<Resource<List<Keyword>>> getKeywordsListState() {
        return keywordsListState;
    }

    public LiveData<Resource<Boolean>> getOperationState() {
        return operationState;
    }

    /**
     * Attaches a real-time listener to the Firestore collection.
     * Updates the keywordsListState automatically whenever data changes on the server.
     */
    private void startListening() {
        keywordsListState.setValue(Resource.loading(null));

        keywordListener = repository.listenToKeywords(new KeywordRepository.KeywordListCallback() {
            @Override
            public void onDataLoaded(List<Keyword> keywords) {
                keywordsListState.setValue(Resource.success(keywords));
            }

            @Override
            public void onError(String errorMessage) {
                keywordsListState.setValue(Resource.error(errorMessage, null));
            }
        });
    }

    public void addKeyword(Keyword keyword) {
        operationState.setValue(Resource.loading(null));
        repository.addKeyword(keyword, new KeywordRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                operationState.setValue(Resource.success(true));
            }

            @Override
            public void onError(String errorMessage) {
                operationState.setValue(Resource.error(errorMessage, null));
            }
        });
    }

    public void updateKeyword(Keyword keyword) {
        operationState.setValue(Resource.loading(null));
        repository.updateKeyword(keyword, new KeywordRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                operationState.setValue(Resource.success(true));
            }

            @Override
            public void onError(String errorMessage) {
                operationState.setValue(Resource.error(errorMessage, null));
            }
        });
    }

    public void deleteKeyword(String keywordId) {
        operationState.setValue(Resource.loading(null));
        repository.deleteKeyword(keywordId, new KeywordRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                operationState.setValue(Resource.success(true));
            }

            @Override
            public void onError(String errorMessage) {
                operationState.setValue(Resource.error(errorMessage, null));
            }
        });
    }

    /**
     * Clears the operation state so Snackbars don't re-trigger on config changes.
     */
    public void clearOperationState() {
        operationState.setValue(null);
    }

    /**
     * Prevents memory leaks by detaching the listener when the ViewModel is destroyed.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        if (keywordListener != null) {
            keywordListener.remove();
        }
    }
}
