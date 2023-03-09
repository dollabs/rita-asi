package gui.panels;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import javax.swing.*;

/*
 * Panel with two parts, a title in a blue bar and a body, which is a JPanel with BorderLayout by default. Used heavily
 * in Chiai's latest improvements to WSE look and feel.
 */
public class TitledJPanel extends SpecialJPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6737701934393843165L;

	protected JPanel container;

	private TitledJPanelBorder theBorder;

	public TitledJPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
	}

	public TitledJPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
	}

	public TitledJPanel(LayoutManager layout) {
		super(layout);
	}

	private Dimension preferredTitleSize = new Dimension(50, 20);

	protected TitlePanel titleLabel = new TitlePanel();

	protected JComponent mainPanel = new JPanel();

	private boolean showTitleBar = true;

	private boolean showMainPanel = true;

	public final static int NORMAL = 0, HIGHLIGHTED = 1, FADED = 2;

	private int titleState = NORMAL;

	private JComponent embeddedComponent = new JPanel();

	public static final Color topColor = new Color(184, 187, 207);

	public static final Color bottomColor = new Color(195, 200, 223);

	public static final int LEFT_MARGIN = 10;

	/*
	 * Construct a titled panel with no title.
	 */
	public TitledJPanel() {
		this("");
	}

	public TitledJPanel(String title) {
		this(title, "");
	}

	/*
	 * Construct a titled panel with a title. Border argument remove border on sides denoted by L,R,T, B
	 */
	public TitledJPanel(String title, String border) {
		setTitleState(NORMAL);
		titleLabel.setOpaque(true);
		titleLabel.setForeground(new Color(88, 94, 133));
		titleLabel.setPreferredSize(preferredTitleSize);
		// mainPanel.setBackground(WConstants.TitlePanelColor);
		mainPanel.setOpaque(true);
		this.setMainPanel(embeddedComponent);
		setTitle(title);
		// setLayout(
		mainPanel.setLayout(new GridLayout(1, 1));

		container = new JPanel();
		container.setLayout(new MyLayoutManager());
		container.add(titleLabel);
		container.add(mainPanel);
		container.add(getAdvicePanel());
		theBorder = new TitledJPanelBorder();
		setBorders(border);
		setBorder(theBorder);
		this.setLayout(new BorderLayout());
		this.add(container, BorderLayout.CENTER);

		titleLabel.setBorder(null);
		mainPanel.setBorder(null);
	}

	public void setBorders(String border) {
		theBorder.setBorders(border);
	}

	public void includeBorders(String border) {
		theBorder.includeBorders(border);
	}

	/*
	 * Preferred size is main panel width and sum of title and main panel height.
	 */
	public Dimension getPreferredSize() {
		// System.out.println("Entering TitledJPanel.getPreferredSize");
		Dimension d1 = titleLabel.getPreferredSize();
		Dimension d2 = embeddedComponent.getPreferredSize();
		Dimension d3 = getAdvicePanel().getPreferredSize();
		int height = 0, width = 0;
		if (this.isShowTitleBar()) {
			width = d1.width;
			height = d1.height;
		}
		if (this.isShowMainPanel()) {
			width = Math.max(width, d2.width);
			height += d2.height;
		}
		if (TitledJPanel.isShowAdvice()) {
			height += d3.height;
		}
		return new Dimension(width, height);
	}

	Dimension minimumSize = new Dimension(0, 0);

	public Dimension getMinimumSize() {
		return minimumSize;
	}

	Dimension maximumSize = new Dimension(10000, 10000);

	public Dimension getMaximumSize() {
		return maximumSize;
	}

	/*
	 * Bevel to right.
	 */
	public void setRightInset() {
		getInsets().right = super.getInsets().right;
	}

	/*
	 * Bevel to left.
	 */
	public void setLeftInset() {
		getInsets().left = super.getInsets().left;
	}

	/*
	 * Bevel top.
	 */
	public void setTopInset() {
		getInsets().top = super.getInsets().top;
	}

	/*
	 * Bevel bottom.
	 */
	public void setBottomInset() {
		getInsets().bottom = super.getInsets().bottom;
	}

	/**
	 * @deprecated -- use two arg version
	 */
	public Component addMain(Component c) {
		return mainPanel.add(c);
	}

	/*
	 * Set layout of main panel.
	 */
	public void setLayoutMain(LayoutManager layout) {
		mainPanel.setLayout(layout);
	}

	/*
	 * Get main panel.
	 */
	public JComponent getMainPanel() {
		return embeddedComponent;
	}

	/*
	 * Set main panel.
	 */
	public void setMainPanel(JComponent c) {
		embeddedComponent = c;
		mainPanel.removeAll();
		mainPanel.add(c);
	}

	/*
	 * Set title of panel.
	 */
	public void setTitle(String title) {
		// System.out.println("Title |" + titleLabel.getText() + "|");
		titleLabel.setText(title);
		// titleLabel.setText(title);
	}

	class MyLayoutManager implements LayoutManager {
		public void removeLayoutComponent(Component component) {
		}

		public void addLayoutComponent(String string, Component arg1) {
		}

		public Dimension minimumLayoutSize(Container container) {
			return null;
		}

		public Dimension preferredLayoutSize(Container container) {
			return null;
		}

		public void layoutContainer(Container container) {
			int height = getHeight();
			int width = getWidth();
			int tHeight = 0;
			int mHeight = 0;
			int correctedWidth = width;
			int correctedHeight = height;
			if (isShowTitleBar()) {
				tHeight = titleLabel.getPreferredSize().height;
			}
			mHeight = correctedHeight - tHeight - 1;
			titleLabel.setBounds(0, 0, correctedWidth, tHeight);

			int adviceHeight = 0;

			if (isShowNames() || (isShowAdvice() && hasAdvice())) {
				adviceHeight = getAdvicePanel().getPreferredSize().height;
			}
			else {
				adviceHeight = 0;
			}
			if (showMainPanel) {
				mainPanel.setBounds(0, tHeight, correctedWidth, mHeight - adviceHeight);
			}
			else {
				mainPanel.setBounds(0, tHeight, 0, 0);
			}
			// System.err.println("titleLabel bounds: " +
			// titleLabel.getBounds());
			// System.err.println("mainPanel bounds: " + mainPanel.getBounds());

			getAdvicePanel().setBounds(0, correctedHeight - adviceHeight, correctedWidth, adviceHeight);

		}
	}

	protected boolean isShowTitleBar() {
		return showTitleBar;
	}

	/*
	 * Determine whether title bar shows at all. Used in subclass, UntitledJPanel.
	 */
	protected void setShowTitleBar(boolean showTitleBar) {
		this.showTitleBar = showTitleBar;
	}

	public boolean isShowMainPanel() {
		return showMainPanel;
	}

	/*
	 * Determine whether main panel shows at all. Used in subclass, PopupWizardPanel
	 */
	public void setShowMainPanel(boolean showMainPanel) {
		this.showMainPanel = showMainPanel;
	}

	public TitlePanel getTitleLabel() {
		return titleLabel;
	}

	public void setTitleState(int state) {
		this.titleState = state;
		if (state == NORMAL) {
			// titleLabel.setBackground(WConstants.TitleLabelColor);
		}
		else if (state == HIGHLIGHTED) {
			// titleLabel.setBackground(WConstants.TitleLabelColorEnabled);
		}
		else if (state == FADED) {
			// titleLabel.setBackground(WConstants.TitleLabelColorDisabled);
		}
	}

	public class TitlePanel extends JLabel {
		public void paintComponent(Graphics graphics) {
			int width = getWidth();
			int height = getHeight();
			Graphics2D g = (Graphics2D) graphics;
			GradientPaint paint = new GradientPaint(0, 0, topColor, 0, height, bottomColor);
			g.setPaint(paint);
			Rectangle2D rectangle = new Rectangle2D.Float(0, 0, width, height);
			g.fill(rectangle);
			// int baseline = WUtils.getCenterBaseline(g.getFontMetrics(), height);
			// System.err.println ("Data " + super.getText() + ", " +
			// LEFT_MARGIN + ", " + baseline + ", " + height);
			g.setColor(Color.BLACK);
			Icon icon = this.getIcon();
			int xOffset = LEFT_MARGIN;
			if (icon != null) {
				int wIcon = icon.getIconWidth();
				int hIcon = icon.getIconHeight();
				this.getIcon().paintIcon(this, g, xOffset, (height - hIcon) / 2);
				xOffset += 1.5 * wIcon;
			}
			// g.drawString(super.getText(), xOffset, baseline);
		}
	}

	public static void main(String[] ignore) {
		JFrame frame = new JFrame();
		TitledJPanel panel = new TitledJPanel("Hello world");
		panel.setLayout(new GridLayout(1, 0));
		frame.getContentPane().add(panel);
		frame.setSize(800, 400);
		frame.setVisible(true);

	}

}
