package wrapper;

import com.ascent.gui.frame.WFrameApplication;

/*
 * Created on Nov 1, 2007 @author phw
 */

public class GenesisGroup extends WFrameApplication {
	public String getNavigationBarItem() {
		return "Genesis systems";
	}

	public String getNavigationBarItemHelp() {
		return "View the genesis group";
	}

	@Override
    public String getAccessType() {
	    return null;
    }

	@Override
    public void restoreTaskBarImage() {
	    
    }

	@Override
    public void restoreTaskBarTitle() {
	    
    }
}
