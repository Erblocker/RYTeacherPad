package org.greenrobot.greendao.rx;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import org.greenrobot.greendao.annotation.apihint.Experimental;
import org.greenrobot.greendao.query.LazyList;
import org.greenrobot.greendao.query.Query;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Scheduler;
import rx.Subscriber;
import rx.exceptions.Exceptions;

@Experimental
public class RxQuery<T> extends RxBase {
    private final Query<T> query;

    @Experimental
    public /* bridge */ /* synthetic */ Scheduler getScheduler() {
        return super.getScheduler();
    }

    public RxQuery(Query<T> query) {
        this.query = query;
    }

    public RxQuery(Query<T> query, Scheduler scheduler) {
        super(scheduler);
        this.query = query;
    }

    @Experimental
    public Observable<List<T>> list() {
        return wrap(new Callable<List<T>>() {
            public List<T> call() throws Exception {
                return RxQuery.this.query.forCurrentThread().list();
            }
        });
    }

    @Experimental
    public Observable<T> unique() {
        return wrap(new Callable<T>() {
            public T call() throws Exception {
                return RxQuery.this.query.forCurrentThread().unique();
            }
        });
    }

    public Observable<T> oneByOne() {
        return wrap((Observable) Observable.create(new OnSubscribe<T>() {
            public void call(Subscriber<? super T> subscriber) {
                LazyList<T> lazyList;
                try {
                    lazyList = RxQuery.this.query.forCurrentThread().listLazyUncached();
                    Iterator it = lazyList.iterator();
                    while (it.hasNext()) {
                        T entity = it.next();
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(entity);
                        }
                    }
                    lazyList.close();
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onCompleted();
                    }
                } catch (Throwable e) {
                    Exceptions.throwIfFatal(e);
                    subscriber.onError(e);
                }
            }
        }));
    }
}
