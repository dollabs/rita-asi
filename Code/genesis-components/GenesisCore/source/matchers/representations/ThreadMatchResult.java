package matchers.representations;

import frames.entities.Thread;

public class ThreadMatchResult {
	public int minLength = 0;
	public int maxLength = 0;
	
	public int matches = 0;
	public double score = -1;
	
	public Thread thread1 = null;
	public Thread thread2 = null;
	
	public boolean match = false;
	
	public boolean identityMatch = false;
	
	public ThreadMatchResult(Thread thread1, Thread thread2) {
		this.thread1 = thread1;
		this.thread2 = thread2;
	}
}