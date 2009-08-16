//
//	File:	AboutBox.java
//

import java.awt.*;
import java.awt.event.*;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.*;

public class AboutBox extends JFrame implements ActionListener {
    protected JLabel titleLabel, aboutLabel[];
    protected static int labelCount = 4;
    protected static int aboutWidth = 280;
    protected static int aboutHeight = 230;
    protected static int aboutTop = 200;
    protected static int aboutLeft = 350;
    protected Font titleFont, bodyFont;
    protected ResourceBundle resbundle;

    public AboutBox() {
        super("");
        this.setResizable(false);
        resbundle = ResourceBundle.getBundle ("strings", Locale.getDefault());
        SymWindow aSymWindow = new SymWindow();
        this.addWindowListener(aSymWindow);	
		
        // Initialize useful fonts
        titleFont = new Font("Lucida Grande", Font.BOLD, 14);
        if (titleFont == null) {
            titleFont = new Font("SansSerif", Font.BOLD, 14);
        }
        bodyFont  = new Font("Lucida Grande", Font.BOLD, 12);
        if (bodyFont == null) {
            bodyFont = new Font("SansSerif", Font.BOLD, 12);
        }
		
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		
		add(Box.createVerticalStrut(10));
		java.net.URL appIconURL = Gypsum.class.getResource("images/aboutAppIcon.png");
		ImageIcon appIcon = new ImageIcon(appIconURL);
		JLabel iconLabel = new JLabel(appIcon);
		iconLabel.setHorizontalAlignment(JLabel.CENTER);
		iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
		this.add(iconLabel);
		
        aboutLabel = new JLabel[labelCount];
        aboutLabel[0] = new JLabel(resbundle.getString("frameConstructor"));
        aboutLabel[0].setFont(titleFont);
        aboutLabel[1] = new JLabel(resbundle.getString("appVersion"));
        aboutLabel[1].setFont(bodyFont);
        aboutLabel[2] = new JLabel(resbundle.getString("copyright"));
        aboutLabel[2].setFont(bodyFont);
        aboutLabel[3] = new JLabel("");		
		
        Panel textPanel2 = new Panel(new GridLayout(labelCount, 1));
        for (int i = 0; i<labelCount; i++) {
            aboutLabel[i].setHorizontalAlignment(JLabel.CENTER);
            textPanel2.add(aboutLabel[i]);
        }
        this.getContentPane().add (textPanel2, BorderLayout.CENTER);
		add(Box.createVerticalStrut(10));
		this.pack();
        this.setLocation(aboutLeft, aboutTop);
        this.setSize(aboutWidth, aboutHeight);
    }

    class SymWindow extends java.awt.event.WindowAdapter {
	    public void windowClosing(java.awt.event.WindowEvent event) {
		    setVisible(false);
	    }
    }
    
    public void actionPerformed(ActionEvent newEvent) {
        setVisible(false);
    }		
}