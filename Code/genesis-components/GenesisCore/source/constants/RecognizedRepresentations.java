package constants;

import java.util.Arrays;
import java.util.List;

/**
 * just some constants to use in hashmap accessing, etc.
 * 
 * @author adk Stuff was fixed.
 */
public interface RecognizedRepresentations {
	public static final Object	PICTURE_FILE_NAME		= "picture-filename";
	public static final Object	MOVIE_FILE_NAME			= "movie-filename";
	public static final Object	TITLE_TAG				= "title";											// meta tag,
																											// no
																											// meaning
																											// in
																											// description

	public static final Object	PATH_ELEMENT_THING		= "path-element";
	public static final Object	PATH_THING				= "path";
	public static final Object	THREAD_THING			= "thread-memory";
	public static final Object	TRAJECTORY_THING		= "trajectory";
	public static final Object	CAUSE_THING				= "cause";
	public static final Object	ROLE_THING				= "roles";
	public static final Object  MENTAL_STATE_THING      = "mental-state";
	public static final Object	QUESTION_THING			= "question";
	public static final Object	ANSWER_THING		    = "answer";
    public static final Object	TRANSFER_THING		    = "transfer";
	public static final Object	GENERIC_THING			= "thing";
	public static final Object	FORCE_THING				= "force";
	public static final Object	GEOMETRY_THING			= "geometry";
	public static final Object	BLOCK_THING				= "blockFrame";
	
	public static final Object	GROUND_GEOMETRY_THING	= "groundGeometry";
	public static final Object CA						= "ca";
	
	public static final Object	TIME_REPRESENTATION		   = "time-representation";
	public static final Object	PLACE_REPRESENTATION	   = "place-representation";
	public static final Object	TRANSITION_REPRESENTATION  = "transition-representation";
	public static final Object	SOCIAL_REPRESENTATION	   = "social-representation";
	public static final Object	ACTION_REPRESENTATION	   = "action-representation";
	
	
	public static final List<Object>	ALL_KNOWN_REPS			= Arrays.asList(new Object[] { PICTURE_FILE_NAME,
			PATH_ELEMENT_THING, PATH_THING, PLACE_REPRESENTATION, THREAD_THING, TRAJECTORY_THING, CAUSE_THING, QUESTION_THING,
			ANSWER_THING, TRANSITION_REPRESENTATION, GENERIC_THING, FORCE_THING, GEOMETRY_THING, BLOCK_THING, TIME_REPRESENTATION,
			GROUND_GEOMETRY_THING, ROLE_THING, ACTION_REPRESENTATION, SOCIAL_REPRESENTATION, MENTAL_STATE_THING						});
	public static final List<Object>	ALL_THING_REPS			= Arrays.asList(new Object[] { PATH_ELEMENT_THING, PATH_THING,
			PLACE_REPRESENTATION, THREAD_THING, TRAJECTORY_THING, CAUSE_THING, ROLE_THING, ACTION_REPRESENTATION, SOCIAL_REPRESENTATION, MENTAL_STATE_THING, QUESTION_THING, ANSWER_THING, TRANSITION_REPRESENTATION,
			TRANSFER_THING, GENERIC_THING, FORCE_THING, GEOMETRY_THING, BLOCK_THING, TIME_REPRESENTATION, GROUND_GEOMETRY_THING, CA });
}
