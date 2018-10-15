package server.util;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>
 * This class allow to create lock on a object without a synchronized block.
 * </p>
 * 
 * <p>
 * You can create a lock on a object and synchronized several object on this
 * same object.
 * </p>
 * 
 * <p>
 * So you can synchronized on a same reference or different objects which are
 * equals with the methods {@link Object#equals(Object)}.
 * </p>
 * 
 * @author Callimard
 *
 * @param <T>
 */
public class LockManager<T> {

	// Constants.

	// Variables.

	private HashMap<T, Lock> hashMapLock;
	private HashMap<Lock, Integer> hashMapCount;

	// Constructors.

	public LockManager() {
		this(10);
	}

	public LockManager(int capacity) {
		this.hashMapLock = new HashMap<>(capacity);
		this.hashMapCount = new HashMap<>(capacity);
	}

	// Methods.

	/**
	 * Lock on the object.
	 * 
	 * @param object
	 */
	public void lockOn(T object) {
		Lock lock = null;

		synchronized (this) {
			if (!this.hashMapLock.containsKey(object)) {
				lock = new ReentrantLock(true);
				this.hashMapLock.put(object, lock);
				this.hashMapCount.put(lock, 1);
			} else {
				lock = this.hashMapLock.get(object);
				int count = this.hashMapCount.get(lock);
				count++;
				this.hashMapCount.put(lock, count);
			}
		}

		lock.lock();
	}

	/**
	 * Unlock on the object.
	 * 
	 * @param object
	 */
	public void unlockOn(T object) {

		Lock lock = null;

		synchronized (this) {
			if (this.hashMapLock.containsKey(object)) {
				lock = this.hashMapLock.get(object);

				int count = this.hashMapCount.get(lock);
				count--;

				// DEBUG
				if (count < 0) {
					// TODO
					System.err.println("Count inf�rieur � 0!! PB!!!!!!!!");
				}

				if (count <= 0) {
					this.hashMapLock.remove(object);
					this.hashMapCount.remove(lock);
				} else {
					this.hashMapCount.put(lock, count);
				}
			} else {
				// We return because the map does not contains the lock.
				return;
			}
		}

		if (lock != null) {
			lock.unlock();
		}
	}

	public Integer getNumberBlock(Lock lock) {
		return this.hashMapCount.get(lock);
	}
}
