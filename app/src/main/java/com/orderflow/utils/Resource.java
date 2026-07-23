package com.orderflow.utils;

/**
 * RESOURCE WRAPPER
 *
 * Purpose:
 * A generic wrapper class that encapsulates the state of any data operation
 * (loading, success, or error) into a single observable object.
 *
 * Why is this needed?
 * In MVVM architecture, the ViewModel exposes LiveData to the UI.
 * But a network operation has THREE possible states:
 *   1. LOADING — the request is in progress (show a spinner or shimmer)
 *   2. SUCCESS — data is ready (show the content)
 *   3. ERROR   — something went wrong (show an error message and retry button)
 *
 * Without a wrapper, you would need three separate LiveData objects per operation:
 *   LiveData<Boolean> isLoading
 *   LiveData<T> data
 *   LiveData<String> errorMessage
 *
 * With Resource<T>, you only need ONE:
 *   LiveData<Resource<List<Keyword>>> keywords;
 *
 * Usage in ViewModel:
 *   keywordsLiveData.postValue(Resource.loading(null));
 *   // ... Firestore call ...
 *   keywordsLiveData.postValue(Resource.success(list));
 *   // ... or on failure:
 *   keywordsLiveData.postValue(Resource.error("Failed to load keywords", null));
 *
 * Usage in Fragment:
 *   viewModel.keywords.observe(viewLifecycleOwner, resource -> {
 *       switch (resource.status) {
 *           case LOADING: showShimmer(); break;
 *           case SUCCESS: showData(resource.data); break;
 *           case ERROR:   showError(resource.message); break;
 *       }
 *   });
 *
 * @param <T> The type of data being wrapped (e.g., List<Keyword>, Reply, UserSettings)
 */
public class Resource<T> {

    /**
     * The three possible states of any data operation.
     */
    public enum Status {
        /** Data request is in flight — show loading indicators */
        LOADING,
        /** Data is ready — show content */
        SUCCESS,
        /** An error occurred — show error UI and allow retry */
        ERROR
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FIELDS (final — Resource objects are immutable after creation)
    // ─────────────────────────────────────────────────────────────────────────

    /** Current state of the operation */
    public final Status status;

    /** The data payload — non-null on SUCCESS, may be null on LOADING/ERROR */
    public final T data;

    /** Human-readable error message — non-null on ERROR, null on LOADING/SUCCESS */
    public final String message;

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE CONSTRUCTOR
    // Instances are only created through the static factory methods below.
    // ─────────────────────────────────────────────────────────────────────────

    private Resource(Status status, T data, String message) {
        this.status  = status;
        this.data    = data;
        this.message = message;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STATIC FACTORY METHODS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a SUCCESS resource with the data payload.
     *
     * @param data The successfully loaded data (must not be null)
     * @param <T>  The type of data
     * @return Resource with status=SUCCESS and the given data
     */
    public static <T> Resource<T> success(T data) {
        return new Resource<>(Status.SUCCESS, data, null);
    }

    /**
     * Creates an ERROR resource with an error message.
     *
     * @param message Human-readable error description for display in the UI
     * @param data    Optional stale data to show alongside the error (usually null)
     * @param <T>     The type of data
     * @return Resource with status=ERROR and the given message
     */
    public static <T> Resource<T> error(String message, T data) {
        return new Resource<>(Status.ERROR, data, message);
    }

    /**
     * Creates a LOADING resource.
     * Call this immediately before starting a Firestore/Firebase operation
     * to trigger the loading indicator in the UI.
     *
     * @param data Optional initial data to show while loading (usually null)
     * @param <T>  The type of data
     * @return Resource with status=LOADING
     */
    public static <T> Resource<T> loading(T data) {
        return new Resource<>(Status.LOADING, data, null);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UTILITY METHODS
    // ─────────────────────────────────────────────────────────────────────────

    /** Returns true if status is SUCCESS */
    public boolean isSuccess() { return status == Status.SUCCESS; }

    /** Returns true if status is LOADING */
    public boolean isLoading() { return status == Status.LOADING; }

    /** Returns true if status is ERROR */
    public boolean isError()   { return status == Status.ERROR; }

    @Override
    public String toString() {
        return "Resource{status=" + status + ", message='" + message + "', hasData=" + (data != null) + "}";
    }
}
