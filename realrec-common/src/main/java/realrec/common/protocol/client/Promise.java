package realrec.common.protocol.client;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class Promise<T> implements Future<T> {

	private final Sync<T> sync = new Sync<T>();

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException,
			TimeoutException, ExecutionException {
		return sync.get(unit.toNanos(timeout));
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		return sync.get();
	}

	public T result() {
		try {
			return sync.get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isDone() {
		return sync.isDone();
	}

	@Override
	public boolean isCancelled() {
		return sync.isCancelled();
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return sync.cancel(mayInterruptIfRunning);
	}

	public boolean set(T value) {
		return sync.set(value);
	}

	public boolean setException(Throwable throwable) {
		return sync.setException(throwable);
	}

	static final class Sync<T> extends AbstractQueuedSynchronizer {

		private static final long serialVersionUID = 0L;
		static final int RUNNING = 0;
		static final int COMPLETING = 1;
		static final int COMPLETED = 2;
		static final int CANCELLED = 4;
		static final int INTERRUPTED = 8;
		private T value;
		private Throwable exception;

		@Override
		protected int tryAcquireShared(int ignored) {
			if (isDone())
				return 1;
			return -1;
		}

		@Override
		protected boolean tryReleaseShared(int finalState) {
			setState(finalState);
			return true;
		}

		T get(long nanos) throws TimeoutException, CancellationException,
				ExecutionException, InterruptedException {
			if (!tryAcquireSharedNanos(-1, nanos))
				throw new TimeoutException("Timeout waiting for task.");
			return getValue();
		}

		T get() throws CancellationException, ExecutionException,
				InterruptedException {
			acquireSharedInterruptibly(-1);
			return getValue();
		}

		private T getValue() throws CancellationException, ExecutionException {
			int state = getState();
			switch (state) {
			case COMPLETED:
				if (exception != null)
					throw new ExecutionException(exception);
				else
					return value;
			case CANCELLED:
			case INTERRUPTED:
				CancellationException ex = new CancellationException(
						"Task was cancelled.");
				ex.initCause(exception);
				throw ex;
			default:
				throw new IllegalStateException(
						"Error, synchronizer in invalid state: " + state);
			}
		}

		boolean isDone() {
			return (getState() & (COMPLETED | CANCELLED | INTERRUPTED)) != 0;
		}

		boolean isCancelled() {
			return (getState() & (CANCELLED | INTERRUPTED)) != 0;
		}

		boolean wasInterrupted() {
			return getState() == INTERRUPTED;
		}

		boolean set(T value) {
			return complete(value, null, COMPLETED);
		}

		boolean setException(Throwable t) {
			return complete(null, t, COMPLETED);
		}

		boolean cancel(boolean interrupt) {
			return complete(null, null, interrupt ? INTERRUPTED : CANCELLED);
		}

		private boolean complete(T value, Throwable t, int finalState) {
			boolean doCompletion = compareAndSetState(RUNNING, COMPLETING);
			if (doCompletion) {
				this.value = value;
				this.exception = ((finalState & (CANCELLED | INTERRUPTED)) != 0) ? new CancellationException(
						"Future.cancel() was called.") : t;
				releaseShared(finalState);
			} else if (getState() == COMPLETING) {
				acquireShared(-1);
			}
			return doCompletion;
		}
	}

}
