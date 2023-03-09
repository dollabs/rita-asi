package frames.classic;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.font.*;

public class MultilineToolTip extends JToolTip {

  public MultilineToolTip() {
  }

  public void setTipText(String tipText) {
    super.setTipText(tipText);
    // now mess with my preferred height and width:

    Font f = getFont();
    
    StringTokenizer st = new StringTokenizer(getTipText(),"\n\r\f");
    int height = f.getSize()*(st.countTokens()+1);
    int width = 0;
    FontRenderContext frc = new FontRenderContext(null, false, false);
    while(st.hasMoreTokens()) {
      String s = st.nextToken();
      TextLayout tl = new TextLayout(s, f, frc);
      int swidth = (int)tl.getAdvance()+10;
      if(swidth > width) width = swidth;
    }
    setMinimumSize(new Dimension(width,height));
    setMaximumSize(new Dimension(width,height));
    setPreferredSize(new Dimension(width,height));
  }


  public void paintComponent (Graphics g) {
    //super.paintComponent(g); // paint background
    Rectangle r = g.getClipBounds();
    g.setColor(getBackground());
    g.fillRect(r.x,r.y,r.width,r.height);
    

    g.setColor(getForeground());
    StringTokenizer st = new StringTokenizer(getTipText(),"\n\r\f");
    int i=0;
    while(st.hasMoreTokens()) {
      i++;
      g.drawString(st.nextToken(),5,(i*getFont().getSize())+2);
    }
  }
}
