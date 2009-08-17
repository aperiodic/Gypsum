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

import java.io.File;
import java.io.FilenameFilter;
import javax.imageio.ImageIO;

public class NewLecture extends JFrame implements ActionListener {
	protected Gypsum app;
	private JList images;
	private DefaultListModel model;
	private JButton start, addImage, removeImage;
	private int from;
	public String[] files;
	
	protected static int frameWidth = 500;
	protected static int frameHeight = 400;
	protected static int frameTop = Toolkit.getDefaultToolkit().getScreenSize().height/2 - (frameHeight/2) - 20;
	protected static int frameLeft = Toolkit.getDefaultToolkit().getScreenSize().width/2 - (frameWidth/2);
	
	public NewLecture(Gypsum theApp) {
		super();
		
		app = theApp;
		files = new String[0];
		
		Font labelFont  = new Font("Lucida Grande", Font.PLAIN, 14);
        if (labelFont == null) {
            labelFont = new Font("SansSerif", Font.PLAIN, 14);
        }
		
		ResourceBundle strings = ResourceBundle.getBundle ("strings", Locale.getDefault());
		//this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBorder(BorderFactory.createEmptyBorder(20, -440, 20, 20));
		
		JLabel newLectureMessage = new JLabel(strings.getString("newLectureMessage"));
		contentPanel.add(newLectureMessage);
		contentPanel.add(Box.createVerticalStrut(20));
		
		JLabel imagesLabel = new JLabel(strings.getString("imagesLabel"));
		imagesLabel.setFont(labelFont);
		contentPanel.add(imagesLabel);
		contentPanel.add(Box.createVerticalStrut(2));
		
		model = new DefaultListModel();
		images = new JList(model);
		images.setFocusable(false);
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
		contentPanel.add(Box.createVerticalStrut(10));
		
		JPanel buttonsPane = new JPanel();
		buttonsPane.setLayout(new BoxLayout(buttonsPane, BoxLayout.X_AXIS));
		buttonsPane.add(Box.createHorizontalStrut(454));
		
		addImage = new JButton(strings.getString("addImageButton"));
		addImage.setActionCommand("addImages");
		addImage.addActionListener(this);
		buttonsPane.add(addImage);
		
		removeImage = new JButton(strings.getString("removeImageButton"));
		removeImage.setActionCommand("removeImage");
		removeImage.addActionListener(this);
		removeImage.setEnabled(false);
		buttonsPane.add(removeImage);
		buttonsPane.add(Box.createHorizontalGlue());
		
		contentPanel.add(buttonsPane);
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
		this.getRootPane().setDefaultButton(start);
		
		addWindowListener(new WindowAdapter() {
							public void windowClosing(WindowEvent e) {
								app.handleClosing(e);
							}
							public void windowClosed(WindowEvent e) {
								app.handleClosed(e);
							}
							public void windowActivated(WindowEvent e) {
								app.handleActivated(e);
							}
							public void windowDeactivated(WindowEvent e) {
								app.handleDeactivated(e);
							}
							public void windowOpened(WindowEvent e) {
								app.handleOpened(e);
							}
						  });
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		setTitle(strings.getString("newLectureTitle"));
		setSize(frameWidth, frameHeight);
		setResizable(false);
		setLocation(frameLeft, frameTop);
		
	}
	
	public void actionPerformed(ActionEvent e) {
		if ("addImages".equals(e.getActionCommand())) {
			FileDialog fd = new FileDialog(this, "Open Image", FileDialog.LOAD);
			fd.setFilenameFilter(new FilenameFilter() {
									public boolean accept(File dir, String name){
										String[] imageFormats = ImageIO.getReaderFormatNames();
								 
										for (int i = 0; i < imageFormats.length; i++) {
											if (name.toLowerCase().endsWith("." + imageFormats[i].toLowerCase())) {
												return true;
											}
										}
								 
										return false;
									}
								 });
			
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
			
			files[files.length - 1] = dir + file;
			
			if (files.length > 0) {
				start.setEnabled(true);
				removeImage.setEnabled(true);
			}
			
			if (files.length == 6) {
				addImage.setEnabled(false);
			}
		}
		
		if ("removeImage".equals(e.getActionCommand())) {
			int selected = images.getSelectedIndex();
			String[] oldFiles = files;
			if (selected == -1) {
				model.remove(model.getSize()-1);
				files = new String[oldFiles.length - 1];
				for (int i = 0; i < files.length; i++) {
					files[i] = oldFiles[i];
				}
			} else {
				model.remove(selected);
				files = new String[oldFiles.length - 1];
				for (int i = 0; i < files.length; i++) {
					int index;
					if (i >= selected) {
						index = i+1;
					} else {
						index = i;
					}
					files[index] = oldFiles[index];
				}
			}
			if (files.length == 0) {
				start.setEnabled(false);
				removeImage.setEnabled(false);
			}
			
			addImage.setEnabled(true);
		}
		
		if ("cancel".equals(e.getActionCommand())) {
			WindowEvent closeWindow = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
			Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(closeWindow);
		}
		

		if ("start".equals(e.getActionCommand())) {
			// tell the main class we have images and should
			// start monitoring
			setVisible(false);
			app.startLecture(new Lecture(files, app));
		}
	}
}
