package server.ftp.debug;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

public class Report {

	// Constants.

	// Variables.

	private final String title;

	private final LocalDateTime creationDate;

	private final List<Entry<LocalDateTime, String>> listAction = new Vector<>();

	private boolean isFailed;

	private boolean isFinished = false;

	// Constructors.

	public Report(String title) {
		this.title = title;

		this.creationDate = LocalDateTime.now();

		this.offerAction("Thread = " + Thread.currentThread());
	}

	// Methods.

	public void offerAction(String action) {
		this.listAction.add(new Pair<LocalDateTime, String>(LocalDateTime.now(), action));
	}

	// Getters and Setters.

	public String getTitle() {
		return this.title;
	}

	public LocalDateTime getCreationDate() {
		return this.creationDate;
	}

	public List<Entry<LocalDateTime, String>> getListAction() {
		return this.listAction;
	}

	public boolean isFailed() {
		return this.isFailed;
	}

	public void setFailed() {
		this.isFailed = true;
	}

	public boolean isFinished() {
		return this.isFinished;
	}

	public void setFinished() {
		this.isFinished = true;
	}

	// Private

	private static class Pair<K, E> implements Entry<K, E> {

		// Variable.

		private K key;
		private E element;

		// Constructors.

		public Pair(K key, E element) {
			this.key = key;
			this.element = element;
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public E getValue() {
			return this.element;
		}

		@Override
		public E setValue(E value) {
			E tmp = this.element;
			this.element = value;
			return tmp;
		}

	}

}
