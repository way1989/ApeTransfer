package com.ape.transfer.util;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * Created by way on 2016/10/27.
 */
public class RxBus {
    private final Subject<Object> subject;

    private RxBus() {
        subject = PublishSubject.create().toSerialized();
    }

    public static RxBus getInstance() {
        return Singleton.INSTANCE;
    }

    public void post(Object event) {
        subject.onNext(event);
    }

    public <T> Observable<T> toObservable(Class<T> eventType) {
        return subject.ofType(eventType);
    }

    private static class Singleton {
        static final RxBus INSTANCE = new RxBus();
    }
}

