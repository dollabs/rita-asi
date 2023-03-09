package memory;

import frames.entities.Bundle;
import frames.entities.Thread;

public interface ThreadMemory {
	void add(String word, Thread thread);

	Bundle lookup(String word);

	Bundle lookup(String word, String pos);
}
