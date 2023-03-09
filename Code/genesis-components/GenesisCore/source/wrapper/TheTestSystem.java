package wrapper;

import utils.Mark;
import utils.Webstart;

import com.ascent.gui.frame.ABasicFrame;
import com.ascent.gui.swing.WConstants;

/*
 * Created on Nov 1, 2007 @author phw
 */
@SuppressWarnings("serial")
public class TheTestSystem extends ABasicFrame {

	public TheTestSystem(String[] args) {

		super(args, TheTestSystem.class.getResource("test.xml"));
		if (args.length != 0) {
			Webstart.setWebStart(true);
		}
		else {
			Webstart.setWebStart(false);
		}
		setTitle("Genesis");
		WConstants.setRequiresLogin(false);
		WConstants.setBannerGif(WConstants.getImage(TheTestSystem.class, "genesis.gif"), WConstants
		        .getImage(TheTestSystem.class, "genesis-gray.gif"));
	}
	




	public static void main(String[] args)  {
		Mark.say(System.getProperty("os.name"));
		Mark.say(System.getProperty("os.arch"));

		new TheTestSystem(args).start();
		
	}

}
