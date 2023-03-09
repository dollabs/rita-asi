package utils;

/*
 * Created on Sep 10, 2015
 * @author phw
 */

public class Sleep {

	public static void sleep(int x) {
		try {
			Thread.sleep(x);
		}
		catch (InterruptedException e) {
			Mark.err("Threw exception while sleeping");
		}
	}

	public static void pause(double t) {
		Sleep.sleep((int) (t * 1000));
	}

}
