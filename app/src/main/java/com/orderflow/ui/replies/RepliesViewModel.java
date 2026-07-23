package com.orderflow.ui.replies;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.ListenerRegistration;
import com.orderflow.data.model.Reply;
import com.orderflow.data.repository.ReplyRepository;
import com.orderflow.utils.Resource;

import java.util.List;

/**
 * REPLIES VIEW MODEL
 *
 * Connects the UI to the ReplyRepository.
 */
public class RepliesViewModel extends ViewModel {

    private final ReplyRepository repository;
    private ListenerRegistration replyListener;

    private final MutableLiveData<Resource<List<Reply>>> repliesListState = new MutableLiveData<>();
    private final MutableLiveData<Resource<Boolean>> operationState = new MutableLiveData<>();

    public RepliesViewModel() {
        this.repository = new ReplyRepository();
        startListening();
    }

    public LiveData<Resource<List<Reply>>> getRepliesListState() {
        return repliesListState;
    }

    public LiveData<Resource<Boolean>> getOperationState() {
        return operationState;
    }

    private void startListening() {
        repliesListState.setValue(Resource.loading(null));

        replyListener = repository.listenToReplies(new ReplyRepository.ReplyListCallback() {
            @Override
            public void onDataLoaded(List<Reply> replies) {
                repliesListState.setValue(Resource.success(replies));
            }

            @Override
            public void onError(String errorMessage) {
                repliesListState.setValue(Resource.error(errorMessage, null));
            }
        });
    }

    public void addReply(Reply reply) {
        operationState.setValue(Resource.loading(null));
        repository.addReply(reply, new ReplyRepository.OperationCallback() {
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

    public void updateReply(Reply reply) {
        operationState.setValue(Resource.loading(null));
        repository.updateReply(reply, new ReplyRepository.OperationCallback() {
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

    public void deleteReply(String replyId) {
        operationState.setValue(Resource.loading(null));
        repository.deleteReply(replyId, new ReplyRepository.OperationCallback() {
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

    public void clearOperationState() {
        operationState.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (replyListener != null) {
            replyListener.remove();
        }
    }
}
