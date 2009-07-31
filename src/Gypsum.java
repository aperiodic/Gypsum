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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

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
	private Application fApplication = Application.getApplication();
	protected Action newAction, openAction, closeAction, saveAction, saveAsAction,
					 undoAction, cutAction, copyAction, pasteAction, clearAction, selectAllAction;
	static final JMenuBar mainMenuBar = new JMenuBar();	
	protected JMenu fileMenu, editMenu; 
	
	public Gypsum() {
		
		super("");
		// The ResourceBundle below contains all of the strings used in this
		// application.  ResourceBundles are useful for localizing applications.
		// New localities can be added by adding additional properties files.
		strings = ResourceBundle.getBundle ("strings", Locale.getDefault());
		setTitle(strings.getString("frameConstructor"));
		
		createActions();
		addMenus();

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
			dummyConfig();
			rectManager = new RectangleManager();
			VideoMonitor vidmon = new VideoMonitor(640, 480, config, this, rectManager);
			this.add(vidmon);
			rectManager.setVideoMonitor(vidmon);
			setSize(640, 480);
			setVisible(true);
			
			//if (!config.getProperty("configured").equals("yes")) {
			//	configure();
				
			//} else {
				//monitor();
			//}
		}
		//setVisible(true);
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
		if (configurate == null) {
			configurate = new Configuration(this);
		}
		this.setVisible(false);
		configurate.startConfiguration(config);
	}
	
	public void configurationFinished() {
		monitor();
	}
	
	public Properties getConfiguration() {
		return config;
	}
	
	public void monitor() {
		this.add(new VideoAdjustor());
		
		setSize(740, 502);
		setVisible(true);
	}
	
	public void startLecture(Lecture theLecture) {
		
	}
	
	private void dummyConfig() {
		config = new Properties();
		config.setProperty("threshold", "122");
		config.setProperty("contrast", "87");
		config.setProperty("projectorMode", "mirrored");
		config.setProperty("perspTLx", "240"); config.setProperty("perspTLy", "160");
		config.setProperty("perspTRx", "400"); config.setProperty("perspTRy", "160");
		config.setProperty("perspBRx", "400"); config.setProperty("perspBRy", "320");
		config.setProperty("perspBLx", "240"); config.setProperty("perspBLy", "320");
		config.setProperty("perspSideLength", "160");
	}

	public void paint(Graphics g) {
		super.paint(g);
	}
	
	// -- APPLICATION-WIDE CONVENIENCE METHODS & CLASSES -- //
	
	public class fsWindowProperties {
		int x, y, width, height;
		
		fsWindowProperties() {
			try {
				// figure out how many logical display devices there are
				if (GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length == 1) {
					// if there's just one, fullscreening is easy (this is for development)
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
		
		undoAction = new undoActionClass( strings.getString("undoItem"),
										 KeyStroke.getKeyStroke(KeyEvent.VK_Z, shortcutKeyMask) );
		cutAction = new cutActionClass( strings.getString("cutItem"),
									   KeyStroke.getKeyStroke(KeyEvent.VK_X, shortcutKeyMask) );
		copyAction = new copyActionClass( strings.getString("copyItem"),
										 KeyStroke.getKeyStroke(KeyEvent.VK_C, shortcutKeyMask) );
		pasteAction = new pasteActionClass( strings.getString("pasteItem"),
										   KeyStroke.getKeyStroke(KeyEvent.VK_V, shortcutKeyMask) );
		clearAction = new clearActionClass( strings.getString("clearItem") );
		selectAllAction = new selectAllActionClass( strings.getString("selectAllItem"),
												   KeyStroke.getKeyStroke(KeyEvent.VK_A, shortcutKeyMask) );
	}
	
	public void addMenus() {
		
		fileMenu = new JMenu(strings.getString("fileMenu"));
		fileMenu.add(new JMenuItem(newAction));
		fileMenu.add(new JMenuItem(openAction));
		fileMenu.add(new JMenuItem(closeAction));
		fileMenu.add(new JMenuItem(saveAction));
		fileMenu.add(new JMenuItem(saveAsAction));
		mainMenuBar.add(fileMenu);
		
		editMenu = new JMenu(strings.getString("editMenu"));
		editMenu.add(new JMenuItem(undoAction));
		editMenu.addSeparator();
		editMenu.add(new JMenuItem(cutAction));
		editMenu.add(new JMenuItem(copyAction));
		editMenu.add(new JMenuItem(pasteAction));
		editMenu.add(new JMenuItem(clearAction));
		editMenu.addSeparator();
		editMenu.add(new JMenuItem(selectAllAction));
		mainMenuBar.add(editMenu);
		
		setJMenuBar (mainMenuBar);
	}
	
	// -- APPLE JAVA EXTENSTION NESTED CLASSES -- //
	
	public class newActionClass extends AbstractAction {
		public newActionClass(String text, KeyStroke shortcut) {
			super(text);
			putValue(ACCELERATOR_KEY, shortcut);
		}
		public void actionPerformed(ActionEvent e) {
			if (newlect == null) {
				newlect = new NewLecture(Gypsum.this);
			}
			newlect.setVisible(true);
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
			System.out.println("Close...");
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
	
	public class undoActionClass extends AbstractAction {
		public undoActionClass(String text, KeyStroke shortcut) {
			super(text);
			putValue(ACCELERATOR_KEY, shortcut);
		}
		public void actionPerformed(ActionEvent e) {
			System.out.println("Undo...");
		}
	}
	
	public class cutActionClass extends AbstractAction {
		public cutActionClass(String text, KeyStroke shortcut) {
			super(text);
			putValue(ACCELERATOR_KEY, shortcut);
		}
		public void actionPerformed(ActionEvent e) {
			System.out.println("Cut...");
		}
	}
	
	public class copyActionClass extends AbstractAction {
		public copyActionClass(String text, KeyStroke shortcut) {
			super(text);
			putValue(ACCELERATOR_KEY, shortcut);
		}
		public void actionPerformed(ActionEvent e) {
			System.out.println("Copy...");
		}
	}
	
	public class pasteActionClass extends AbstractAction {
		public pasteActionClass(String text, KeyStroke shortcut) {
			super(text);
			putValue(ACCELERATOR_KEY, shortcut);
		}
		public void actionPerformed(ActionEvent e) {
			System.out.println("Paste...");
		}
	}
	
	public class clearActionClass extends AbstractAction {
		public clearActionClass(String text) {
			super(text);
		}
		public void actionPerformed(ActionEvent e) {
			System.out.println("Clear...");
		}
	}
	
	public class selectAllActionClass extends AbstractAction {
		public selectAllActionClass(String text, KeyStroke shortcut) {
			super(text);
			putValue(ACCELERATOR_KEY, shortcut);
		}
		public void actionPerformed(ActionEvent e) {
			System.out.println("Select All...");
		}
	}
	
	 public static void main(String args[]) {
		new Gypsum();
	 }

}