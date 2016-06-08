package com.orcchg.chatclient.ui.base;

import android.util.Log;

import com.orcchg.chatclient.data.parser.Response;
import com.orcchg.chatclient.data.remote.ServerBridge;

import java.lang.ref.WeakReference;

import timber.log.Timber;

public class SimpleConnectionCallback<P extends BasePresenter> implements ServerBridge.ConnectionCallback {

    private final WeakReference<P> mPresenterRef;

    public SimpleConnectionCallback(P presenter) {
        mPresenterRef = new WeakReference<>(presenter);
    }

    protected WeakReference<P> getPresenterRef() {
        return mPresenterRef;
    }

    @Override
    public void onComplete() {
        Timber.d("Completed (Direct connection)");
        final P presenter = mPresenterRef.get();
        if (presenter != null) {
            presenter.getMvpView().postOnUiThread(new Runnable() {
                @Override
                public void run() {
                    presenter.getMvpView().onComplete();
                }
            });
        } else {
            Timber.v("Presenter has already been GC'ed");
        }
    }

    @Override
    public void onNext(Response response) {
        // override in subclasses
    }

    @Override
    public void onError(Throwable e) {
        Timber.e("Error (Direct connection): %s", Log.getStackTraceString(e));
        final P presenter = mPresenterRef.get();
        if (presenter != null) {
            presenter.getMvpView().postOnUiThread(new Runnable() {
                @Override
                public void run() {
                    presenter.getMvpView().onError();
                }
            });
        } else {
            Timber.v("Presenter has already been GC'ed");
        }
    }
}
