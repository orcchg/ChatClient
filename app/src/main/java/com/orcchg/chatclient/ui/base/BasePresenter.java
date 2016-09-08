package com.orcchg.chatclient.ui.base;

import android.app.Activity;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import timber.log.Timber;

/**
 * Base class that implements the Presenter interface and provides a base implementation for
 * attachView() and detachView(). It also handles keeping a reference to the mvpView that
 * can be accessed from the children classes by calling getMvpView().
 */
public class BasePresenter<T extends MvpView> implements Presenter<T> {

    private T mMvpView;

    @Override
    public void attachView(T mvpView) {
        mMvpView = mvpView;
    }

    @Override
    public void detachView() {
        mMvpView = null;
    }

    public boolean isViewAttached() {
        if (mMvpView == null) {
            Timber.tag(this.getClass().getSimpleName());
            Timber.e("View is not attached !!!");
        }
        return mMvpView != null;
    }

    public T getMvpView() {
        return mMvpView;
    }

    public void checkViewAttached() {
        if (!isViewAttached()) throw new MvpViewNotAttachedException();
    }

    public static class MvpViewNotAttachedException extends RuntimeException {
        public MvpViewNotAttachedException() {
            super("Please call Presenter.attachView(MvpView) before" +
                    " requesting data to the Presenter");
        }
    }

    protected void showSnackbar(final String message) {
        if (!isViewAttached()) return;
        getMvpView().postOnUiThread(new Runnable() {
            @Override
            public void run() {
                getMvpView().showSnackbar(message, Snackbar.LENGTH_SHORT);
            }
        });
    }

    protected void showSnackbar(final @StringRes int resId) {
        if (!isViewAttached()) return;
        getMvpView().postOnUiThread(new Runnable() {
            @Override
            public void run() {
                getMvpView().showSnackbar(resId, Snackbar.LENGTH_SHORT);
            }
        });
    }

    protected void showToast(final @StringRes int resId) {
        if (!isViewAttached()) return;
        final Activity activity = (Activity) getMvpView();
        getMvpView().postOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, resId, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
