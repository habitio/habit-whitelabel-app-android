package com.muzzley.util.rx;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.CompletableTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class RxComposers {

    public static <T> ObservableTransformer<T, T> applyIoRefresh(final Consumer<Boolean> refresh) {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(@NonNull Observable<T> observable) {
                return observable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(new Consumer<Disposable>() {
                        @Override
                        public void accept(@NonNull Disposable disposable) throws Exception {
                            refresh.accept(true);
                        }
                    })
                    .doOnNext(new Consumer<T>() { // should we disable this and rely just on error and complete ?
                        @Override
                        public void accept(@NonNull T t) throws Exception {
                            refresh.accept(false);
                        }
                    })
                    .doOnError(new Consumer<Throwable>() {
                        @Override
                        public void accept(@NonNull Throwable throwable) throws Exception {
                            refresh.accept(false);
                        }
                    })
                    .doOnComplete(new Action() {
                        @Override
                        public void run() throws Exception {
                            refresh.accept(false);
                        }
                    });
            }
        };
    }

    public static <T> ObservableTransformer<T, T> applyIo() {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(@NonNull Observable<T> observable) {
                return observable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    public static CompletableTransformer applyIoCompletable() {
        return new CompletableTransformer() {
            @Override
            public CompletableSource apply(@NonNull Completable completable) {
                return completable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    public static CompletableTransformer applyIoRefreshCompletable(final Consumer<Boolean> refresh) {
        return new CompletableTransformer() {
            @Override
            public CompletableSource apply(@NonNull Completable completable) {
                return completable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(@NonNull Disposable disposable) throws Exception {
                                refresh.accept(true);
                            }
                        })
                        .doOnComplete(new Action() {
                            @Override
                            public void run() throws Exception {
                                refresh.accept(false);
                            }
                        })
                        .doOnError(new Consumer<Throwable>() {
                            @Override
                            public void accept(@NonNull Throwable throwable) throws Exception {
                                refresh.accept(false);
                            }
                        });
            }
        };
    }

}