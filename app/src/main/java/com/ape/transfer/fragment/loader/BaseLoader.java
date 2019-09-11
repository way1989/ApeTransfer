package com.ape.transfer.fragment.loader;

import android.content.Context;

import androidx.loader.content.AsyncTaskLoader;

import java.util.ArrayList;


/**
 * Created by android on 16-6-28.
 */
public class BaseLoader<T> extends
        AsyncTaskLoader<BaseLoader.Result> {
    private Result mResults;

    public BaseLoader(Context context) {
        super(context);
    }

    @Override
    public Result loadInBackground() {
        return null;
    }

    // Called when there is new data to deliver to the client. The
    // super class will take care of delivering it; the implementation
    // here just adds a little more logic.
    @Override
    public void deliverResult(Result result) {
        mResults = result;

        if (isStarted()) {
            // If the Loader is started, immediately deliver its results.
            super.deliverResult(result);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mResults != null) {
            // If we currently have a result available, deliver it immediately.
            deliverResult(mResults);
        }

        if (takeContentChanged() || mResults == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated if needed.
        mResults = null;
    }

    public static class Result<T> {
        public ArrayList<T> lists;
    }
}
