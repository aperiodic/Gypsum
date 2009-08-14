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

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.apple.eawt.*;

public class Gypsum extends JFrame {

	private Font font = new Font("serif", Font.ITALIC+Font.BOLD, 36);
	protected ResourceBundle strings;
	protected Properties config;
	protected AboutBox aboutBox;
	protected PrefPane prefs;
	protected Configuration configurate;
	protected NewLecture newlect;
	protected RectangleManager rectManager;
	protected ProjectorView projector;
	protected VideoMonitor vidmon;
	private int vidmonRefCount = 0;
	protected JFrame adjustor;
	private Application fApplication = Application.getApplication();
	protected Action newAction, openAction, closeAction, saveAction, saveAsAction, configureAction,  adjustorAction;
	static final JMenuBar mainMenuBar = new JMenuBar();	
	protected JMenu fileMenu, editMenu, windowMenu;
	
	public Gypsum() {
		
		super("");
		// The ResourceBundle below contains all of the strings used in this
		// application.  ResourceBundles are useful for localizing applications.
		// New localities can be added by adding additional properties files.
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
		
		fApplication.setEnabledPreferencesMenu(true);
		fApplication.addApplicationListener(new com.apple.eawt.ApplicationAdapter() {
			public void handleAbout(ApplicationEvent e) {
                                if (aboutBox == null) {
                                    aboutBox = new AboutBox();
                                }
                                about(e);
                                e.setHandled(true);
			}
			public void handleOpenApplication(ApplicationEvent e) {
			}
			public void handleOpenFile(ApplicationEvent e) {
			}
			public void handlePreferences(ApplicationEvent e) {
                                if (prefs == null) {
                                    prefs = new PrefPane();
                                }
				preferences(e);
			}
			public void handlePrintFile(ApplicationEvent e) {
			}
			public void handleQuit(ApplicationEvent e) {
				quit(e);
			}
		});
		
		if(!loadConfiguration()) {
			// couldn't find or create the config file
		} else {
			if (!config.getProperty("configured").equals("yes")) {
				configure();
				
			} else {
				newlect = new NewLecture(this);
				newlect.setVisible(true);
			}
		}
	}

	// attempt to load the configuration file
	// if it doesn't exist, create a new one
	public boolean loadConfiguration() {
		config = new Properties();
		try {
			java.io.FileInputStream configFile = new java.io.FileInputStream("Gypsum.app/Contents/Resources/Gypsum.config");
			
			try {
				config.load(configFile);
			} catch (java.io.IOException ioe) {
				System.out.println("There was an IO error while trying to read from the configuration file");
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
					System.err.println("There was an IO error while trying to write to the configuration file.");
					return false;
				}
			} catch (java.io.FileNotFoundException frnf) {
				System.err.println("There was an error while trying to create the configuration file.");
				return false;
			}
			
			return true;
		}
		
	}
	
	// start the configuration process
	public void configure() {
		
		configurate = new Configuration(this);
		configurate.startConfiguration(config);
		configurate.setVisible(true);
	}
	
	public void configurationFinished(Properties cfg) {
		config = cfg;
		newlect = new NewLecture(this);
		newlect.setVisible(true);
	}
	
	public Properties getConfiguration() {
		return config;
	}
	
	public void	newLecture() {
		newlect = new NewLecture(this);
		newlect.setVisible(true);
	}
	
	public void startLecture(Lecture theLecture) {
		projector = new ProjectorView(theLecture, this);
		rectManager = new RectangleManager(projector);
		vidmon = new VideoMonitor(640, 480, config, this, rectManager);
		vidmon.setThresholded(true);
		vidmon.setName("vidmon");
		
		fileMenu.getItem(3).setEnabled(true);
		fileMenu.getItem(4).setEnabled(true);
	
		rectManager.setVideoMonitor(vidmon);
	}
	
	private void dummyConfig() {
		config = new Properties();
		config.setProperty("projectorMode", "extendedR");
		config.setProperty("perspTLx", "240"); config.setProperty("perspTLy", "160");
		config.setProperty("perspTRx", "400"); config.setProperty("perspTRy", "160");
		config.setProperty("perspBRx", "400"); config.setProperty("perspBRy", "320");
		config.setProperty("perspBLx", "240"); config.setProperty("perspBLy", "320");
		config.setProperty("perspSideLength", "160");
	}

	public void paint(Graphics g) {
		super.paint(g);
	}
	
	public void handleClosing(WindowEvent e) {
		String windowClass = e.getWindow().getClass().toString();
		
		// if they just closed a lecture, pop up a new lecture window
		if (windowClass.indexOf("ProjectorView") != -1) {
			newLecture();
			return;
		}
		
		// if they closed the adjustor, release the video monitor
		if (windowClass.indexOf("VideoAdjustor") != -1) {
			releaseVideoMonitor();
			return;
		}
		
		// if the user is closing the configuration window, and 
		// the app has been configured, just spawn a new lecture 
		// window instead of quitting
		if (windowClass.indexOf("Configuration") != -1 && 
			!"yes".equals(config.getProperty("configured"))) {
			e.getWindow().setVisible(false);
			newLecture();
			return;
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
		if (windowClass.indexOf("NewLecture") >= 0) {
			if (configurate != null) configurate.setVisible(false);
			
		} else if (windowClass.indexOf("Configuration") >= 0) {
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
	
	public VideoMonitor newVideoMonitor(int w, int h) {
		if (vidmon != null) {
			return null;
		}
		
		vidmon = new VideoMonitor(w, h);
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
		aboutBox.setResizable(false);
		aboutBox.setVisible(true);
	}
	
	public void preferences(ApplicationEvent e) {
		prefs.setResizable(false);
		prefs.setVisible(true);
	}
	
	public void quit(ApplicationEvent e) {	
		System.exit(0);
	}
	
	// -- ACTIONS & MENUS -- //5
	
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
			System.out.println("Open...");
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
			System.out.println("Save...");
		}
	}
	
	public class saveAsActionClass extends AbstractAction {
		public saveAsActionClass(String text) {
			super(text);
		}
		
		public void actionPerformed(ActionEvent e) {
			System.out.println("Save As...");
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