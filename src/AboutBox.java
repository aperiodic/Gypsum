//
//  Copyright (c) 2009, Dan Lidral Porter
//  All rights reserved.

//  Redistribution and use in source and binary forms, with or without modification, 
//  are permitted provided that the following conditions are met:

//  * Redistributions of source code must retain the above copyright notice, this list
//    of conditions and the following disclaimer.
//  * Redistributions in binary form must reproduce the above copyright notice, this list
//    of conditions and the following disclaimer in the documentation and/or other materials
//    provided with the distribution.
//  * Neither the name of "Gypsum" nor the names of its contributors may be used to endorse
//    or promote products derived from this software without specific prior written permission.

//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
//  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
//  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
//  SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
//  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
//  OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
//  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
//  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF 
//  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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