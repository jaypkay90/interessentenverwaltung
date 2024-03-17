import java.awt.EventQueue;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ProspectsPopUpFrame extends JFrame implements ActionListener {

private static final long serialVersionUID = 1L;
private String[] colNames;
private JFrame frame;

		
public ProspectsPopUpFrame(String[] colNames) {
	this.colNames = colNames;
	this.frame = new JFrame();
	
	// JFrame = a GUI window to add components to
	frame.setTitle("Interessenteninformation"); // sets title of frame
	frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); 
	frame.setResizable(false); // prevent frame from being resized	
	frame.setLayout(new BorderLayout(10, 10));
	
	ImageIcon image = new ImageIcon("MainIcon.png"); // creates an ImageIcon
	frame.setIconImage(image.getImage());
	
	
	JPanel userDataInputPanel = new JPanel(new GridLayout(0, 3, 10, 10));
	//frame.setLayout(new FlowLayout(FlowLayout.LEADING,0,10));
	//frame.setLayout(new GridLayout(0, 3, 10, 10));

	
	// Eingabefelder zur Usereingabe hinzufügen
	addInputFields(userDataInputPanel);
	
	//this.getContentPane().setBackground(new Color(123,50,250)); //change color of background, hex geht auch mit 0x
	
	
	
	frame.add(userDataInputPanel);
	frame.pack();
	frame.setVisible(true); // makes frame visible
		
		
	}
	
	private void addInputFields(JPanel panel) {
		for (int i = 0; i < colNames.length; i++) {
			JPanel itemPanel = new JPanel();
			
			itemPanel.add(new JLabel(colNames[i]));
            itemPanel.add(new JTextField(15));
            panel.add(itemPanel);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	
	}

}
