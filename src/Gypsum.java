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
		this.getContentPane().setLayout(null);
		
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
			
		}
		
		/*if (!configured) {
			configure();
		} else {
			setSize(310, 150);
			setVisible(true);
		}*/
	}

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
	
	public boolean loadConfiguration() {
		try {
			config = new Properties();
			java.io.FileInputStream configFile = new java.io.FileInputStream("Gypsum.config");
			
			config.load(configFile);
			String configured = config.getProperty("configured");
			
			if (configured.equals("yes")) {
				return true;
			} else {
				return false;
			}
		}
		catch (java.io.FileNotFoundException fnf) {
			try {
				java.io.FileOutputStream configFile = new java.io.FileOutputStream("Gypsum.config", true);
				config.setProperty("configured", "no");
				config.store(configFile, "");
				configFile.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return false;
		}
		
	}
	
	public void configure() {
		if (configurate == null) {
			configurate = new Configuration();
		}
		configurate.startConfiguration();
	}

	public void paint(Graphics g) {
		super.paint(g);
		g.setColor(Color.blue);
		g.setFont (font);
		g.drawString(strings.getString("message"), 40, 80);
	}
	
	public class newActionClass extends AbstractAction {
		public newActionClass(String text, KeyStroke shortcut) {
			super(text);
			putValue(ACCELERATOR_KEY, shortcut);
		}
		public void actionPerformed(ActionEvent e) {
			System.out.println("New...");
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