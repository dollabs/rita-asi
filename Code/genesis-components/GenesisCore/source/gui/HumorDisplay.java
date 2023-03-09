package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
//import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import connections.Connections;
import connections.WiredBox;
import connections.signals.BetterSignal;
import frames.entities.Entity;
import frames.entities.Sequence;
import generator.Generator;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
//import utils.Mark;
import utils.Mark;

/*
 * Created on Jan 13, 2015
 * @author phw, jmn
 */

@SuppressWarnings("serial")
public class HumorDisplay extends JPanel implements WiredBox {

	public static String FROM_EXPERT = "from expert port";

	JPanel overview = new JPanel();
	JFXPanel fxPanel = new JFXPanel();
	Label label = new Label("TEXT");
	TableView<List<String>> table = new TableView<>();
	JList<String> lister = new JList<String>();

	@Override
	public String getName() {
		return "Humor display"; //TODO: rename
	}

	public HumorDisplay() {

		JLabel listerLabel = new JLabel("Summary");
		lister.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane l1Scroller = new JScrollPane(lister);
		l1Scroller.setPreferredSize(new Dimension(300, 600));
		
		overview.setLayout(new BorderLayout());
		overview.setPreferredSize(new Dimension(300, 600));
		overview.setBorder(BorderFactory.createTitledBorder("Overview"));
		table.setPrefSize(300,600);
		
		overview.add(listerLabel);
		overview.add(l1Scroller);
		
		String[] items = {
				"<html><b>01 Morbidity:</b><br/>"
				+ "<div style=\"text-align:center\" width='150px'>"
				+ "panda is SAFE keyword"
				+ "<br/> ->"
				+ "<br/> shoots is DEADLY keyword</div></html>",
				
				"<html><b>05 Word Meaning:</b><br/>"
				+ "<div style=\"text-align:center\" width='150px'>"
				+ "shoots (noun)"
				+ "<br/> ->"
				+ "<br/> shoots (verb)</div></html>"
				};
		lister.setListData(items);
		
		
		//setBackground(Color.WHITE);
//		add(getConceptBar(), BorderLayout.CENTER);
//		add(getStoryProgressBar(), BorderLayout.SOUTH);
		add(fxPanel);
		add(overview);
	
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				initializeOnFXThread();
			}
		});
		Connections.getPorts(this).addSignalProcessor(FROM_EXPERT, this::process);
	}
	
	private void initializeOnFXThread() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				initFX(fxPanel);
			}
		});
	}

	private void initFX(JFXPanel fxPanel) {
		List<List<String>> data = generateFakeData("FakeData ");
		constructTable(data);
		Scene scene = createScene();
		fxPanel.setScene(scene);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	}

	public void constructTable(List<String> columnHeaders, List<List<String>> data) {
		if (columnHeaders.isEmpty()) constructTable(data);
		addColumnsToTable(columnHeaders);
		addDataToTable(data);
	}
//
//	public void constructTable(int numberOfColumns, List<List<String>> data) {
//		addColumnsToTable(numberOfColumns);
//		addDataToTable(data);
//	}

	public void constructTable(List<List<String>> data) {
		if (data.isEmpty()) return;
		List<String> sampleRow = data.get(0);
		addColumnsToTable(sampleRow.size());
		addDataToTable(data);
	}

	private void addColumnsToTable(int numberOfColumns){
		List<String> columnNames = new ArrayList<String>();
		columnNames.add("Level");
		columnNames.add("Expert Type");
		columnNames.add("Flag");
		addColumnsToTable(columnNames);
	}

	private void addColumnsToTable(List<String> columnNames){
		for (int i = 0; i < columnNames.size(); i++) {
			TableColumn<List<String>, String> col = new TableColumn<>(columnNames.get(i));
			final int columnIndex = i;
			col.setCellValueFactory(columnData -> {
				List<String> rowValues = columnData.getValue();
				String cellValue = (columnIndex < rowValues.size()) ? rowValues.get(columnIndex) : "";
				return new ReadOnlyStringWrapper(cellValue);
			});
			table.getColumns().add(col);
		}
	}

	private void clearTable() {
		table.getItems().clear();
		table.getColumns().clear();
	}

	private void addDataToTable(List<List<String>> data) {
		table.getItems().addAll(data);
	}

	private Scene createScene() {
		Group root = new Group();
		Scene scene = new Scene(root, Color.ALICEBLUE);
		root.getChildren().addAll(label, table); //TODO: label is obscured by table
		return scene;
	}

	public void process(Object obj) {
		if (obj instanceof BetterSignal) {
			BetterSignal signal = (BetterSignal) obj;
			Object received = signal.get(0, Object.class);
//			if (received instanceof String) {
//				processString((String) received);
//			} else if (received instanceof Observations) {
//				processObservations((Observations) received);
//			}
		}
	}
	
	private void processString(String s) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				label.setText(s);
			}
		});
	}

//	private void processObservations(Observations observations) {
////		List<String> columnHeaders = Arrays.asList("mycol0", "mycol1", "mycol2");
////		List<List<String>> data = createFakeData("new data ");
//		Platform.runLater(new Runnable() {
//			@Override
//			public void run() {
//				clearTable();
////				constructTable(columnHeaders, data);
////				constructTable(5, observations.getDataForTable());
//				constructTable(observations.getColumnHeadersForTable(), observations.getDataForTable());
//				Scene scene = createScene();
//				fxPanel.setScene(scene);
//			}
//		});
//	}
	
	public static String entityToText(Entity e) {
		return Generator.getGenerator().generate(e);
	}

//	public static List<String> convertEntitiesToStrings(List<Entity> entities) {
//		return entities.stream().map(e -> JmnUtils.entityToText(e)).collect(Collectors.toList());
//	}

	public static List<String> convertBooleansToStrings(List<Boolean> booleans, Function<Boolean, String> fn) {
		return booleans.stream().map(b -> fn.apply(b)).collect(Collectors.toList());
	}

	public static List<String> convertBooleansToStrings(List<Boolean> booleans, String trueString, String falseString) {
		return booleans.stream().map(b -> b ? trueString : falseString).collect(Collectors.toList());
	}

	public static List<Boolean> convertPresenceIDsToBooleans(List<Integer> presenceIDs, Integer length) {
		List<Boolean> booleans = new ArrayList<Boolean>();
		for (int i=0; i<length; i++) {
			booleans.add(presenceIDs.contains(i));
		}
		return booleans;
	}

	public static List<List<String>> generateFakeData(String s) {
		// columnNames.add("Expert Type");
		// columnNames.add("Expected");
		// columnNames.add("Flag");
		// columnNames.add("Repair");
		List<String> row1 = Arrays.asList("01", "Morbidity", "FLAG");
		List<String> row2 = Arrays.asList("02", "Language", "---");
		List<String> row4 = Arrays.asList("03", "Overall Topics", "---");
		List<String> row3 = Arrays.asList("04", "Ambiguity", "---");
		List<String> row0 = Arrays.asList("05", "Word Meaning Assignment", "FLAG");
		List<String> row5 = Arrays.asList("06", "Lies and Dialogue", "---");
		List<String> row6 = Arrays.asList("07", "Entity Class Traits", "---");
		List<String> row7 = Arrays.asList("08", "Logic Error", "---");
		
		List<List<String>> data = new ArrayList<List<String>>();
		data.addAll(Arrays.asList(row0, row1, row2, row3, row4, row5, row6, row7));
		return data;
	}
	
	public static String sequenceToString(Sequence story) {
//		Stream<Entity> stream = story.stream(); //not sure how to use streams
		String english = "";
		for (Entity entity : story.getElements()) {
			String sentence = Generator.getGenerator().generate(entity);
			if (sentence != null) {
				english += sentence.trim() + "  ";
			}
			else {
				Mark.err("Unexpected null sentence");
			}
		}
		return english;
	}
	
	
//	public List<List<String>> getDataForTable() {
//		int eventCount = events.size();
//		List<List<String>> data = new ArrayList<List<String>>();
//		for (Actor actor : getActors()) {
//			List<String> row = new ArrayList<String>();
//			row.add(actor.getName());
//			row.addAll(JmnUtils.convertBooleansToStrings(
//					JmnUtils.convertPresenceIDsToBooleans(actor.getObservedEventIDs(), eventCount),
//					"OBSERVED", "-"));
//			data.add(row);
//		}
//		return data;
//	}
}