package by.muna.monads;

import by.muna.data.IEither;
import by.muna.data.either.EitherLeft;
import by.muna.data.either.EitherRight;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.BiConsumer;

public class OneTimeEventAsyncMonad<R, E> implements IAsyncMonad<R, E> {
    private IEither<Queue<BiConsumer<R, E>>, IEither<E, R>> listenersOrValue = new EitherLeft<>(new LinkedList<>());

    public OneTimeEventAsyncMonad() {}

    @Override
    public void run(BiConsumer<R, E> callback) {
        if (this.listenersOrValue.isRight()) {
            IEither<E, R> r = this.listenersOrValue.getRight();
            callback.accept(r.getRight(), r.getLeft());
        } else {
            synchronized (this) {
                if (this.listenersOrValue.isRight()) {
                    IEither<E, R> r = this.listenersOrValue.getRight();
                    callback.accept(r.getRight(), r.getLeft());
                } else {
                    this.listenersOrValue.getLeft().add(callback);
                }
            }
        }
    }

    /**
     * Can be called only once, else noop.
     * @param value
     */
    public void event(R value, E error) {
        Queue<BiConsumer<R, E>> listeners;

        synchronized (this) {
            if (this.listenersOrValue.isRight()) return;

            listeners = this.listenersOrValue.getLeft();
            this.listenersOrValue = new EitherRight<>(error == null ? new EitherRight<>(value) : new EitherLeft<>(error));
        }

        for (BiConsumer<R, E> listener : listeners) listener.accept(value, error);
    }

    public boolean isEventHappened() {
        synchronized (this) {
            return this.listenersOrValue.isRight();
        }
    }
}
