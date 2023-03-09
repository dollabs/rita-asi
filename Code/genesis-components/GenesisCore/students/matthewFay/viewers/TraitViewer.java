package matthewFay.viewers;

import gui.ElaborationView;
import gui.panels.ParallelJPanel;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import connections.Connections;
import connections.WiredBox;
import connections.signals.BetterSignal;
import frames.entities.Entity;
import frames.entities.Sequence;
import matthewFay.CharacterModeling.representations.Trait;

public class TraitViewer extends JPanel implements WiredBox {
	private static TraitViewer viewer = null;
	public static TraitViewer getTraitViewer() {
		if(viewer==null) {
			viewer = new TraitViewer();
		}
		return viewer;
	}
	
	private String name = "Trait Viewer";
	public String getName() {
		return name;
	}
	
	
	private JTabbedPane tabbed_pane;
	
	private ParallelJPanel control_panel;
	public JCheckBox generalize_trait_description = new JCheckBox("Generalize Trait Descriptions", true);
	
	private List<Trait> traits = new ArrayList<Trait>();
	private Map<Trait, ElaborationView> trait_tabs = new HashMap<>();
	
	public TraitViewer() {
		super(new BorderLayout());
		tabbed_pane = new JTabbedPane();
	
		control_panel = new ParallelJPanel();
		control_panel.addLeft(generalize_trait_description);
		tabbed_pane.addTab("Controls", control_panel);
		
		this.add(tabbed_pane);
		
		Connections.getPorts(this).addSignalProcessor("processTrait");
	}
	
	public void processTrait(Object o) {
		if(o instanceof Trait) {
			addTrait((Trait)o);
		}
	}
	
	public void addTrait(Trait trait) {
		String trait_name = trait.getName();
		if(!traits.contains(trait)) {
			traits.add(trait);
			
			ElaborationView elaboration_viewer = new ElaborationView();
			elaboration_viewer.setAlwaysShowAllElements(true);
			
			Connections.wire(trait_name, this, ElaborationView.INSPECTOR, elaboration_viewer);
			
			tabbed_pane.addTab(trait_name, elaboration_viewer);
			
			trait_tabs.put(trait, elaboration_viewer);
		}
		
		//Refresh view
		trait.markDirty();
		
		Set<Entity> elts = new HashSet<>(trait.getElements());
		Sequence seq = new Sequence();
		for(Entity e : trait.getElements())
			seq.addElement(e);
		BetterSignal bs = new BetterSignal(seq);
		Connections.getPorts(this).transmit(trait_name, bs);
	}
}
