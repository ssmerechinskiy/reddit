package com.sergey.redditreader.task;

import android.os.Handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by sober on 22.09.2017.
 */

public abstract class Task {

    private static ExecutorService defaultExecutorService = Executors.newFixedThreadPool(5);

    private ExecutorService executorService;

    public <RL> void executeTask(final ResultListener<RL> resultListener, ExecutorService executorService) {
        if (executorService == null) return;
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final RL result = performAction();
                    if(resultListener != null) resultListener.onSuccessHandler(result);
                } catch (final Exception e) {
                    if(resultListener != null) resultListener.onErrorHandler(e);
                }
            }
        });
    }

    public <RL> void executeTask(final ResultListener<RL> resultListener) {
        if (defaultExecutorService == null) defaultExecutorService = Executors.newFixedThreadPool(5);
        defaultExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final RL result = performAction();
                    if(resultListener != null) resultListener.onSuccessHandler(result);
                } catch (final Exception e) {
                    if(resultListener != null) resultListener.onErrorHandler(e);
                }
            }
        });
    }

    public abstract <S> S performAction() throws Exception;

    public Task executor(ExecutorService es) {
        executorService = es;
        return this;
    }

    public static abstract class ResultListener<T> {
        private Handler handler;

        private void onSuccessHandler(final T response) {
            if(handler == null) {
                onSuccess(response);
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onSuccess(response);
                    }
                });
            }
        }

        private void onErrorHandler(final Throwable e) {
            if(handler == null) {
                onError(e);
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onError(e);
                    }
                });
            }
        }

        public abstract void onSuccess(T response);
        public abstract void onError(Throwable error);

        public ResultListener<T> handler(Handler h) {
            handler = h;
            return this;
        }
    }

    public static void release() {
        if(defaultExecutorService != null) {
            defaultExecutorService.shutdown();
            defaultExecutorService.shutdownNow();
            defaultExecutorService = null;
        }
    }
}
