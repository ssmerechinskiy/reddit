package com.sergey.redditreader.task;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by sober on 22.09.2017.
 */

public abstract class Task {
    private static ExecutorService singleThreadExecutorService = Executors.newSingleThreadExecutor();
    private static Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    public static <R> void executeTask(final Task task, final ResultListener<R> resultListener) {
        singleThreadExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final R result = task.execute();
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(resultListener != null) resultListener.onSuccess(result);
                        }
                    });
                } catch (final Exception e) {
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(resultListener != null) resultListener.onError(e);
                        }
                    });
                }
            }
        });
    }

    public static <R> void executeTask(final Task task, final ResultListener<R> resultListener, ExecutorService executorService) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final R result = task.execute();
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(resultListener != null) resultListener.onSuccess(result);
                        }
                    });
                } catch (final Exception e) {
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(resultListener != null) resultListener.onError(e);
                        }
                    });
                }
            }
        });
    }

    public abstract <S> S execute() throws Exception;

    public interface ResultListener<T> {
        void onSuccess(T response);
        void onError(Throwable e);
    }
}
