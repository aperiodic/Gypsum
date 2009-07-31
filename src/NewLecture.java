//
//  NewLecture.java
//  Gypsum
//
//  Created by DLP on 7/27/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class NewLecture extends JFrame implements ActionListener {
	protected Gypsum app;
	private JList images;
	private DefaultListModel model;
	private JButton start;
	private int from;
	public String[] files;
	
	public NewLecture(Gypsum theApp) {
		super();
		
		app = theApp;
		
		files = new String[0];
		
		ResourceBundle strings = ResourceBundle.getBundle ("strings", Locale.getDefault());
		this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBorder(BorderFactory.createEmptyBorder(20, -320, 20, 20));
		
		JLabel newLectureMessage = new JLabel(strings.getString("newLectureMessage"));
		contentPanel.add(newLectureMessage);
		contentPanel.add(Box.createVerticalStrut(20));
		
		JLabel imagesLabel = new JLabel(strings.getString("imagesLabel"));
		contentPanel.add(imagesLabel);
		
		model = new DefaultListModel();
		images = new JList(model);
		images.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		images.setLayoutOrientation(JList.VERTICAL);
		images.setVisibleRowCount(-1);
		
		images.addMouseListener(new MouseAdapter() {
								public void mousePressed(MouseEvent m) {
								from = images.getSelectedIndex();
								}});
		
		images.addMouseMotionListener(new MouseMotionAdapter() {
									  public void mouseDragged(MouseEvent m) {
									  int to  = images.getSelectedIndex();
									  if (to == from) return;
									  String name = (String) model.remove(from);
									  model.add(to, name);
									  from = to;
									  
									  String selectedFile = files[from];
									  String replacedFile = files[to];
									  
									  files[from] = replacedFile;
									  files[to] = selectedFile;
									  
									  }		
									  });
		
		JScrollPane listScroller = new JScrollPane(images);
		listScroller.setPreferredSize(new Dimension(500, 200));
		listScroller.setAlignmentX(LEFT_ALIGNMENT);
		
		contentPanel.add(listScroller);
		
		JButton addImageButton = new JButton(strings.getString("addImageButton"));
		addImageButton.setActionCommand("addImages");
		addImageButton.addActionListener(this);
		contentPanel.add(addImageButton);
		contentPanel.add(Box.createVerticalStrut(20));
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		JButton cancel = new JButton(strings.getString("cancelButton"));
		cancel.setActionCommand("cancel");
		cancel.addActionListener(this);
		buttonPane.add(cancel);
		
		start = new JButton(strings.getString("startButton"));
		start.setActionCommand("start");
		start.addActionListener(this);
		start.setEnabled(false);
		buttonPane.add(start);
		
		contentPanel.add(buttonPane);
		
		this.getContentPane().add(contentPanel);
		
		setSize(500, 400);
		setResizable(false);
		setLocation(420, 200);
		
	}
	
	public void actionPerformed(ActionEvent e) {
		if ("addImages".equals(e.getActionCommand())) {
			FileDialog fd = new FileDialog(this, "Open Image");
			fd.setVisible(true);
			
			if (fd.getDirectory() == null || fd.getFile() == null) {
				return;
			}
			
			String dir = fd.getDirectory();
			String file = fd.getFile();
			
			model.add(model.getSize(), file);
			String[] oldFiles = files;
			
			files = new String[oldFiles.length + 1];
			
			for (int i = 0; i < oldFiles.length; i++) {
				files[i] = oldFiles[i];
			}
			
			files[files.length - 1] = file;
			
			if (files.length > 0) {
				start.setEnabled(true);
			}
		}
		
		if ("cancel".equals(e.getActionCommand())) {
			setVisible(false);
		}
		
		if ("start".equals(e.getActionCommand())) {
			// tell the main class we have images and should
			// start monitoring
			//theApp.startLecture(new Lecture(files));
		}
	}
}
