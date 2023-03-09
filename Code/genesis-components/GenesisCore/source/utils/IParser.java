package utils;

import frames.entities.Sequence;

public interface IParser {

	public Sequence parse(String sentence) throws Exception;

}