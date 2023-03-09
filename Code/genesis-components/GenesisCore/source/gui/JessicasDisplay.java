package gui;

import java.util.ArrayList;
//import java.util.Arrays;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;

import javax.swing.*;

//import utils.Mark;
import jessicaNoss.JmnUtils;
import jessicaNoss.Observations;
import connections.*;
import connections.signals.BetterSignal;

/*
 * Created on Jan 13, 2015
 * @author phw, jmn
 */

@SuppressWarnings("serial")
public class JessicasDisplay extends JPanel implements WiredBox { //TODO: generally clean up code in this class

	public static String FROM_EXPERT = "from expert port";

	JFXPanel fxPanel = new JFXPanel();
	Label label = new Label("TEXT");
	TableView<List<String>> table = new TableView<>();

	@Override
	public String getName() {
		return "Jessica's observations display"; //TODO: rename
	}

	public JessicasDisplay() {
		add(fxPanel);
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
		List<List<String>> data = JmnUtils.generateFakeData("FakeData ");
		constructTable(data);
		Scene scene = createScene();
		fxPanel.setScene(scene);
	}

	public void constructTable(List<String> columnHeaders, List<List<String>> data) {
		if (columnHeaders.isEmpty()) constructTable(data);
		addColumnsToTable(columnHeaders);
		addDataToTable(data);
	}

	public void constructTable(int numberOfColumns, List<List<String>> data) {
		addColumnsToTable(numberOfColumns);
		addDataToTable(data);
	}

	public void constructTable(List<List<String>> data) {
		if (data.isEmpty()) return;
		List<String> sampleRow = data.get(0);
		addColumnsToTable(sampleRow.size());
		addDataToTable(data);
	}

	private void addColumnsToTable(int numberOfColumns){
		List<String> columnNames = new ArrayList<String>();
		for (int i = 0; i < numberOfColumns; i++) {
			columnNames.add("Col"+i);
		}
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
			if (received instanceof String) {
				processString((String) received);
			} else if (received instanceof Observations) {
				processObservations((Observations) received);
			}
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

	private void processObservations(Observations observations) {
//		List<String> columnHeaders = Arrays.asList("mycol0", "mycol1", "mycol2");
//		List<List<String>> data = createFakeData("new data ");
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				clearTable();
//				constructTable(columnHeaders, data);
//				constructTable(5, observations.getDataForTable());
				constructTable(observations.getColumnHeadersForTable(), observations.getDataForTable());
				Scene scene = createScene();
				fxPanel.setScene(scene);
			}
		});
	}
}