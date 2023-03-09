package dictionary;

import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;

import javax.swing.*;

import frames.entities.Bundle;

/*
 * Created on Mar 10, 2008 @author phw
 */

public class DictionaryPage extends JPanel {
	JTextArea definition = new JTextArea();

	JTextField word = new JTextField();

	JScrollPane scroller;

	WordNet wordNet = new WordNet();

	public DictionaryPage() {
		super();
		this.setLayout(new BorderLayout());
		scroller = new JScrollPane(definition);
		this.add(scroller, BorderLayout.CENTER);
		this.add(word, BorderLayout.SOUTH);
		word.addKeyListener(new MyWordListener());

		definition.setFont(new Font("Helvetica", Font.BOLD, 18));
		word.setFont(new Font("Helvetica", Font.BOLD, 18));
	}

	class MyWordListener extends KeyAdapter {
		public void keyTyped(KeyEvent event) {
			String w = word.getText().trim();
			String result = "";
			definition.setText(result);
			if (w.length() != 0) {
				if ('\n' == event.getKeyChar() || '.' == event.getKeyChar()) {
					int index = word.getText().indexOf('.');
					if (index >= 0) {
						w = w.substring(0, index);
					}
					Bundle bundles = wordNet.lookup(w);
					if (bundles.size() == 0) {
						result += "Nothing found in WordNet for " + w;
					}
					else {
						for (Iterator i = bundles.iterator(); i.hasNext();) {
							frames.entities.Thread thread = (frames.entities.Thread) (i.next());
							for (Iterator j = thread.iterator(); j.hasNext();) {
								result += j.next() + " ";
							}
							result += "\n";
						}
					}
				}
				definition.setText(result);
			}
		}
	}

	public static void main(String[] ignore) {
		DictionaryPage page = new DictionaryPage();
		JFrame frame = new JFrame();
		frame.getContentPane().add(page);
		frame.setBounds(50, 100, 200, 300);
		frame.setVisible(true);
	}

}
