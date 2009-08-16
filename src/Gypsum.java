//
//  Gypsum.java
//  Gypsum
//
//  Created by DLP on 6/2/09.
//  Copyright (c) 2009 __MyCompanyName__. All rights reserved.
//
//

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Properties;

import java.io.File;
import java.io.FilenameFilter;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.apple.eawt.*;

public class Gypsum extends JFrame {

	private Font font = new Font("serif", Font.ITALIC+Font.BOLD, 36);
	protected ResourceBundle strings;
	protected Properties config;
	protected AboutBox aboutBox;
	protected Configuration configurate;
	protected NewLecture newlect;
	protected RectangleManager rectManager;
	protected ProjectorView projector;
	protected VideoMonitor vidmon;
	private int vidmonRefCount = 0;
	protected JFrame adjustor;
	private File deferredLecture = null;
	private Application fApplication = Application.getApplication();
	protected Action newAction, openAction, closeAction, saveAction, saveAsAction, configureAction,  adjustorAction;
	static final JMenuBar mainMenuBar = new JMenuBar();	
	protected JMenu fileMenu, editMenu, windowMenu;
	
	public Gypsum() {
		
		super("");
		
		strings = ResourceBundle.getBundle ("strings", Locale.getDefault());
		setTitle(strings.getString("frameConstructor"));
		
		addWindowListener(new WindowAdapter() {
							public void windowClosed(WindowEvent e) {
								Gypsum.this.handleClosed(e);
							}
							public void windowActivated(WindowEvent e) {
								Gypsum.this.handleActivated(e);
							}
							public void windowDeactivated(WindowEvent e) {
								Gypsum.this.handleDeactivated(e);
							}
							public void windowOpened(WindowEvent e) {
								Gypsum.this.handleOpened(e);
							}
						  });
						  
		createActions();
		addMenus();
		vidmon = null;
		
		loadConfiguration();
		if (!config.getProperty("configured").equals("yes")) {
			configure();
		} else {
			newlect = new NewLecture(this);
			newlect.setVisible(true);
		}
		
		fApplication.setEnabledPreferencesMenu(false);
		fApplication.addApplicationListener(new com.apple.eawt.ApplicationAdapter() {
												public void handleAbout(ApplicationEvent e) {
													about(e);
													e.setHandled(true);
												}
												public void handleOpenApplication(ApplicationEvent e) {
												}
												public void handleOpenFile(ApplicationEvent e) {
													File theFile = new File(e.getFilename());
													openFile(new File(e.getFilename()));
													e.setHandled(true);
												}
												public void handlePreferences(ApplicationEvent e) {
												}
												public void handlePrintFile(ApplicationEvent e) {
												}
												public void handleQuit(ApplicationEvent e) {
													quit(e);
												}
											});
		
	}

	/*
	 * Attempt to load the configuration file. If one does not exist,
	 * a new one will be created. If the file can't be created, or
	 * an IO error happens when trying to read from/write to the file,
	 * show an error & quit.
	 */
	public boolean loadConfiguration() {
		config = new Properties();
		try {
			java.io.FileInputStream configFile = new java.io.FileInputStream("Gypsum.app/Contents/Resources/Gypsum.config");
			
			try {
				config.load(configFile);
			} catch (java.io.IOException ioe) {
				showError("An error occurred while trying to read from the configuration file.\nPlease restart Gypsum.", ioe);
				return false;
			}
			
			return true;
			
		} catch (java.io.FileNotFoundException fnf) {
			// the configuration file was not found, so try to 
			// create a new config file and write to it
			config.setProperty("configured", "no");
			
			try {
				java.io.FileOutputStream configFile = new java.io.FileOutputStream("Gypsum.app/Contents/Resources/Gypsum.config");
				
				try {
					config.store(configFile, "");
					configFile.close();
				} catch (java.io.IOException ioe) {
					showError("An error ocurred while trying to write to the configuration file.\nPlease restart Gypsum", ioe);
					return false;
				}
			} catch (java.io.FileNotFoundException frnf) {
				showError("Gypsum was unable to create the configuration file.", frnf);
				return false;
			}
			
			return true;
		}
		
	}
	
	// start the configuration process
	public void configure() {
		if (configurate != null) return;

		configurate = new Configuration(this);
		configurate.startConfiguration(config);
		configurate.setVisible(true);
	}
	
	public void configurationFinished(Properties cfg) {
		config = cfg;
		
		if (deferredLecture != null) {
			openFile(deferredLecture);
			deferredLecture = null;
			return;
		}
		
		newlect = new NewLecture(this);
		newlect.setVisible(true);
	}
	
	public Properties getConfiguration() {
		return config;
	}
	
	
	/*
	 * Show the New Lecture window to select the images for a new lecture.
	 */
	
	public void	newLecture() {
		if (newlect == null) {
			newlect = new NewLecture(this);
		}
		newlect.setVisible(true);
		newlect.toFront();
	}
	
	
	/*
	 * Start a new Lecture. This handles setting up the windows & video monitor. The 
	 * window listener methods take care of hiding any other windows which might be open.
	 */		
	
	public void startLecture(Lecture theLecture) {
		projector = new ProjectorView(theLecture, this);
		rectManager = new RectangleManager(projector);
		VideoMonitor vm = newVideoMonitor(640, 480, config, this, rectManager);
		vm.setThresholded(true);
		vm.setName("vidmon");
		
		fileMenu.getItem(0).setEnabled(false);
		fileMenu.getItem(1).setEnabled(false);
		fileMenu.getItem(3).setEnabled(true);
		fileMenu.getItem(4).setEnabled(true);
	
		rectManager.setVideoMonitor(vm);
	}
	
	
	/*
	 * Display a FileDialog to open a file. This is called by the "Open..." menu item.
	 */
	
	public void open() {
		FileDialog fd = new FileDialog(this, "Open Lecture", FileDialog.LOAD);
		fd.setFilenameFilter(new FilenameFilter() {
								 public boolean accept(java.io.File dir, String name){
									 return(name.endsWith(".lec"));
								 }
							 });
		
		fd.setVisible(true);
		
		if (fd.getDirectory() == null || fd.getFile() == null) {
			return;
		}
		
		Lecture lecture = Lecture.open(fd.getDirectory(), fd.getFile(), this);
		startLecture(lecture);
	}
	
	
	/*
	 * Open a File. This method is called by the Apple ApplicationListener when 
	 * a .lec file is double-clicked, or dragged onto Gypsum's dock icon. If
	 * configuration hasn't taken place, it defers opening the file until after
	 * configuration is complete.
	 */
	
	public void openFile(File theFile) {
		String theName = theFile.getName();
		String theDir = theFile.getParent() + "/";
		
		if (!config.getProperty("configured").equals("yes")) {
			deferredLecture = theFile;
			configure();
			return;
		}
		
		Lecture lecture = Lecture.open(theDir, theName, this);
		startLecture(lecture);
	}
	
	
	/*
	 * Pop open a file dialog to save the current lecture in a new location.
	 */
	
	public void saveAs() {
		FileDialog fd = new FileDialog(this, "Save Lecture", FileDialog.SAVE);
		fd.setFile(projector.getLecture().name);
		fd.setDirectory(projector.getLecture().dir);
		fd.setVisible(true);
		
		String dir = fd.getDirectory();
		String name = fd.getFile();
		
		projector.getLecture().save(dir, name, this);
	}
	
	/*
	 * If this lecture hasn't been saved, pop open a file dialog to specify name
	 * and location. Otherwise, save over the existing file.
	 */
	
	public void save() {
		if (projector.getLecture().name == null) {
			FileDialog fd = new FileDialog(this, "Save Lecture", FileDialog.SAVE);
			fd.setFile("untitled.lec");
			fd.setVisible(true);
			
			String dir = fd.getDirectory();
			String name = fd.getFile();
			
			projector.getLecture().save(dir, name, this);
		} else {
			projector.getLecture().save(this);
		}
	}

	public void paint(Graphics g) {
		super.paint(g);
	}
	
	public void handleClosing(WindowEvent e) {
		String windowClass = e.getWindow().getClass().toString();
		
		// if they just closed a lecture, pop up a new lecture window
		if (windowClass.indexOf("ProjectorView") != -1) {
			releaseVideoMonitor();
			
			fileMenu.getItem(0).setEnabled(true);
			fileMenu.getItem(1).setEnabled(true);
			fileMenu.getItem(3).setEnabled(false);
			fileMenu.getItem(4).setEnabled(false);
			
			newLecture();
			return;
		}
		
		// if the user is closing the configuration window, and 
		// the app has been configured, just spawn a new lecture 
		// window instead of quitting
		if (windowClass.indexOf("Configuration") != -1) {
			if ("yes".equals(config.getProperty("configured"))) {
				e.getWindow().setVisible(false);
				e.getWindow().dispose();
				configurate = null;
				newLecture();
				return;
			} else {
				return;
			}
		}
		
		Object[] buttons = {"OK", "Cancel"};
		int closeResult = JOptionPane.showOptionDialog(e.getWindow(),
													   "<html><span style=\"font-weight: bold; font-size: 14pt;\">Are you sure you want to close this window?</span><br><br>This will quit Gypsum.</html>",
													   "Quit",
													   JOptionPane.YES_NO_OPTION,
													   JOptionPane.QUESTION_MESSAGE,
													   null,
													   buttons,
													   buttons[0]);
		if (closeResult == JOptionPane.NO_OPTION || closeResult == JOptionPane.CLOSED_OPTION) {
			return;
		}
		
		e.getWindow().setVisible(false);
		
		System.exit(0);
	}
	
	public void handleClosed(WindowEvent e) {
	}
	
	public void handleOpened(WindowEvent e) {
		String windowClass = e.getWindow().getClass().toString();
		if (windowClass.indexOf("NewLecture") != -1) {
			if (configurate != null) configurate.setVisible(false);
			
		} else if (windowClass.indexOf("Configuration") != -1) {
			if (newlect != null) newlect.setVisible(false);
		
		} else if (windowClass.indexOf("ProjectorView") != -1) {
			if (newlect != null) newlect.setVisible(false);
		}
	}
	
	public void handleActivated(WindowEvent e) {
		JFrame theFrame = (JFrame) e.getWindow();
		theFrame.setJMenuBar(mainMenuBar);
	}
	
	public void handleDeactivated(WindowEvent e) {
		JFrame theFrame = (JFrame) e.getWindow();
		theFrame.setJMenuBar(null);
	}
	
	// -- APPLICATION-WIDE CONVENIENCE METHODS & CLASSES -- //
	
	public class fsWindowProperties {
		int x, y, width, height;
		
		fsWindowProperties() {
			try {
				// figure out how many logical display devices there are
				if (GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length == 1) {
					// if there's just one, fullscreening is easy (this is mainly for development)
					GraphicsDevice mainDisplay = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];
					width = mainDisplay.getDisplayMode().getWidth();
					height = mainDisplay.getDisplayMode().getHeight();
					x = y = 0;
				} else {
					// if there's more than one, it gets a bit trickier
					GraphicsDevice mainDisplay = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];
					GraphicsDevice projector = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[1];
					width = projector.getDisplayMode().getWidth();
					height = projector.getDisplayMode().getHeight();
					
					if ("mirrored".equals(config.getProperty("projectorMode"))) {
						// if the projector is mirrored, it's easy again
						x = y = 0;
					} else if ("extendedL".equals(config.getProperty("projectorMode"))) {
						y = mainDisplay.getDisplayMode().getHeight() - projector.getDisplayMode().getHeight();
						if (y < 0) y = 0;
						x = 0;
					} else if ("extendedR".equals(config.getProperty("projectorMode"))) {
						x = mainDisplay.getDisplayMode().getWidth();
						y = 0;
					} else {
						// projector mode wasn't set
						x = y = width = height = -1;
					}
				}
			} catch (HeadlessException e) {
				// why the hell would anyone be running this headlessly?
				x = y = width = height = -1;
			}
		}
	}
	
	/*
	 * This is for fatal errors that terminate the program–right now, it's only called
	 * if for some reason we can't create a config file. Use showWarning for recoverable
	 * errors.
	 */
	public void showError(String theError, Exception e) {
		JOptionPane.showMessageDialog(new JFrame(),
									  theError,
									  "Error",
									  JOptionPane.ERROR_MESSAGE);
		if (e != null) {
			e.printStackTrace();
		}
		
		System.exit(1);
	}
	
	/*
	 * This is for recoverable errors which do not require Gypsum to quit–for example,
	 * errors while trying to open lecture files.
	 */
	public void showWarning	(String theWarning, Exception e) {
		JOptionPane.showMessageDialog(new JFrame(),
									  theWarning,
									  "Error",
									  JOptionPane.ERROR_MESSAGE);
		if (e != null) {
			e.printStackTrace();
		}
	}
	
	
	public VideoMonitor newVideoMonitor(int w, int h) {
		if (vidmon != null) {
			return null;
		}
		
		vidmon = new VideoMonitor(w, h);
		vidmonRefCount = 1;
		return vidmon;
	}
	
	public VideoMonitor newVideoMonitor(int w, int h, Properties cfg, Gypsum app, RectangleManager mngr) {
		if (vidmon != null) {
			return null;
		}
		
		vidmon = new VideoMonitor(w, h, cfg, app, mngr);
		vidmonRefCount = 1;
		return vidmon;
	}
	
	public VideoMonitor getVideoMonitor() {
		vidmonRefCount++;
		return vidmon;
	}
	
	public boolean monitoring() {
		if (vidmon == null) {
			return false;
		}
		return true;
	}
	
	public void releaseVideoMonitor () {
		vidmonRefCount--;
		
		if (vidmonRefCount == 0) {
			vidmon.stop();
			vidmon = null;
		}
	}
	
	// -- APPLE JAVA EXTENSION METHODS -- //
	
	public void about(ApplicationEvent e) {
		if (aboutBox == null) {
			aboutBox = new AboutBox();
		}
		aboutBox.setResizable(false);
		aboutBox.setVisible(true);
	}
	
	public void quit(ApplicationEvent e) {	
		System.exit(0);
	}
	
	// -- ACTIONS & MENUS -- //
	
	public void createActions() {
		int shortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		
		//Create actions that can be used by menus, buttons, toolbars, etc.
		newAction = new newActionClass( strings.getString("newItem"),
									   KeyStroke.getKeyStroke(KeyEvent.VK_N, shortcutKeyMask) );
		openAction = new openActionClass( strings.getString("openItem"),
										 KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcutKeyMask) );
		closeAction = new closeActionClass( strings.getString("closeItem"),
										   KeyStroke.getKeyStroke(KeyEvent.VK_W, shortcutKeyMask) );
		saveAction = new saveActionClass( strings.getString("saveItem"),
										 KeyStroke.getKeyStroke(KeyEvent.VK_S, shortcutKeyMask) );
		saveAsAction = new saveAsActionClass( strings.getString("saveAsItem") );
		configureAction = new configureActionClass(strings.getString("configureItem"));
		
	}
	
	public void addMenus() {
		
		fileMenu = new JMenu(strings.getString("fileMenu"));
		fileMenu.add(new JMenuItem(newAction));
		fileMenu.add(new JMenuItem(openAction));
		fileMenu.add(new JMenuItem(closeAction));
		fileMenu.add(new JMenuItem(saveAction));
		fileMenu.add(new JMenuItem(saveAsAction));
		fileMenu.addSeparator();
		fileMenu.add(new JMenuItem(configureAction));
		
		fileMenu.getItem(3).setEnabled(false);
		fileMenu.getItem(4).setEnabled(false);
		
		mainMenuBar.add(fileMenu);
		
		setJMenuBar(mainMenuBar);
	}
	
	public void attachMenu(JFrame aFrame) {
		aFrame.setJMenuBar(mainMenuBar);
	}
	
	// -- ABSTRACT ACTION NESTED CLASSES -- //
	
	public class newActionClass extends AbstractAction {
		public newActionClass(String text, KeyStroke shortcut) {
			super(text);
			putValue(ACCELERATOR_KEY, shortcut);
		}
		public void actionPerformed(ActionEvent e) {
			newLecture();
		}
	}

	public class openActionClass extends AbstractAction {
		public openActionClass(String text, KeyStroke shortcut) {
			super(text);
			putValue(ACCELERATOR_KEY, shortcut);
		}
		public void actionPerformed(ActionEvent e) {
			open();
		}
	}
	
	public class closeActionClass extends AbstractAction {
		public closeActionClass(String text, KeyStroke shortcut) {
			super(text);
			putValue(ACCELERATOR_KEY, shortcut);
		}
		public void actionPerformed(ActionEvent e) {
			// close the window that currently has focus
			java.awt.Window activeWindow = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
			WindowEvent closeWindow = new WindowEvent(activeWindow, WindowEvent.WINDOW_CLOSING);
			Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(closeWindow);
		}
	}
	
	public class saveActionClass extends AbstractAction {
		public saveActionClass(String text, KeyStroke shortcut) {
			super(text);
			putValue(ACCELERATOR_KEY, shortcut);
		}
		public void actionPerformed(ActionEvent e) {
			save();
		}
	}
	
	public class saveAsActionClass extends AbstractAction {
		public saveAsActionClass(String text) {
			super(text);
		}
		
		public void actionPerformed(ActionEvent e) {
			saveAs();
		}
	}
	
	public class configureActionClass extends AbstractAction {
		public configureActionClass(String text) {
			super(text);
		}
		
		public void actionPerformed(ActionEvent e) {
			configure();
		}
	}

	 public static void main(String args[]) {
		new Gypsum();
	 }

}