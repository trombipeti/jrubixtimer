package jrubixtimer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.text.DefaultHighlighter;

import jrubixtimer.SolutionTimer.State;

public class JRTMainWindow extends JFrame {

	private static final long serialVersionUID = -8003811406449177400L;

	public int initialW;
	public int initialH;

	/********************
	 * A timer betűjének magassága az ablakéhoz képest, százalékban.
	 */
	public static final double timerFontHeight = 0.25;

	private JPanel leftPanel;
	private JPanel bottomPanel;
	private JPanel leftBottomPanel;
	private JPanel middleBottomPanel;
	private JPanel rightPanel;

	private JTextField timerText;
	private JComboBox<Object> activePuzzleCombo;
	private JCheckBox inspectionCheckBox;
	private JTextArea scrText;
	private JLabel puzzleLabel;
	private JTextPane sessionInfo;
	// private JButton newSessionBtn;

	private JComboBox<?> displayPuzzleCombo;
	private ResultsTable resultsTable;
	private JSplitPane splitter;
	private JSplitPane leftSplitter;

	private Solution.Type ACTIVE_PUZZLE = Solution.Type.CUBE_3X3;
	private boolean initialInspectionState = true;
	private int statNum = 1;

	private File confFile;
	private String confFileName;
	private File confDir;
	private String confDirName;

	private SpaceTimer spaceTimer;
	private List<SolutionCollector> sessions = new ArrayList<SolutionCollector>();

	/**
	 * A fő ablak konstruktora. Beolvassa a beállításokat és az elmentett
	 * időket, valamint megjeleníti az ablakot.
	 */
	public JRTMainWindow() {

		setConfDirNameRelative(".jrubixtimer");
		setConfFileNameRelative("jrt.rc");

		Solution.Type types[] = { Solution.Type.CUBE_2X2,
				Solution.Type.CUBE_3X3, Solution.Type.CUBE_4X4,
				Solution.Type.CUBE_5X5, Solution.Type.CUSTOM };
		for (Solution.Type t : types) {
			SolutionCollector sc = new SolutionCollector(t);
			sc.addTableModelListener(new TableModelListener() {

				@Override
				public void tableChanged(TableModelEvent arg0) {
					updateSessionStats();
				}
			});
			sessions.add(sc);
		}
		parseConfFile();
		spaceTimer = new SpaceTimer(this);
		spaceTimer.setUseInspection(initialInspectionState);
		initComponents();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * A confDirName változó getter függvénye.
	 * 
	 * @return A konfigurációs beállításokat tartalmazó könyvtár neve. Lehet
	 *         abszolút vagy relatív is!!!
	 */
	public String getConfDirName() {
		return confDirName;
	}

	/**
	 * A konfigurációs beállításokat tartalmazó mappa neve, relatív elérési
	 * útvonallal a felhasználó home könyvtárán belül.
	 * 
	 * @param confDirName
	 *            A mappa neve a home könyvtáron belül, pl ".jrubixtimer" vagy
	 *            "Beállítások"
	 */
	public void setConfDirNameRelative(String confDirName) {
		this.confDirName = confDirName;
		confDir = new File(System.getProperty("user.home"), this.confDirName);
	}

	/**
	 * A konfigurációs beállításokat tartalmazó mappa neve, abszolút elérési
	 * útvonallal.
	 * 
	 * @param confDirName
	 *            A mappa neve, pl "/home/gipszjakab/.jrubixtimer",
	 *            "C:\Users\Izidor\Settings"
	 */
	public void setConfDirNameAbsolute(String confDirName) {
		this.confDirName = confDirName;
		confDir = new File(this.confDirName);
	}

	/**
	 * A confFileName változó getter függvénye.
	 * 
	 * @return A konfigurációs fájl neve. Lehet abszolút és relatív elérési út
	 *         is!!
	 */
	public String getConfFileName() {
		return confFileName;
	}

	/**
	 * Beállítja a konfigurációs fájlt a megadott helyre, relatív elérési
	 * útvonallal a confDir mappán belül.
	 * 
	 * @param confFileName
	 *            A konfigurációs fájl neve a confDir mappán belül, pl "jrt.rc",
	 *            "sajat_jrubixtimer_beallitasok.conf" stb
	 */
	public void setConfFileNameRelative(String confFileName) {
		this.confFileName = confFileName;
		confFile = new File(confDir, confFileName);
	}

	/**
	 * Beállítja a konfigurációs fájlt a megadott helyre, abszolút elérési
	 * útvonallal.
	 * 
	 * @param confFileName
	 *            A konfigurációs fájl abszolút elérési útvonala, pl
	 *            "/home/user/jrubixtimer.conf", vagy
	 *            "C:\Users\Izidor\Asztal\jrubixtimer_beallitasok.txt"
	 * 
	 */
	public void setConfFileNameAbsolute(String confFileName) {
		this.confFileName = confFileName;
		confFile = new File(confFileName);
	}

	/**
	 * Beolvassa a beállításokat a konfigurációs fájlból
	 */
	private void parseConfFile() {
		if (!confFile.exists()) {
			return;
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(confFile));
			int linenum = 0;
			read: while (true) {
				linenum++;
				String line = br.readLine();
				if (line == null) {
					break read;
				}
				// Az rc fájlban a '#' kommentet jelent
				if (line.length() == 0 || line.charAt(0) == '#') {
					continue read;
				} else {
					int comment = line.indexOf('#');
					if (comment != -1) {
						line = line.substring(0, comment);
					}
				}
				if (line.contains("=")) {
					String[] keyValPair = line.split("=");
					if (keyValPair.length <= 1) {
						continue read;
					}
					String key = keyValPair[0].toLowerCase();
					String val = keyValPair[1].toLowerCase();
					if (key.equals("inspection")) {
						if (val.startsWith("on")) {
							initialInspectionState = true;
						} else if (keyValPair[1].toLowerCase()
								.startsWith("off")) {
							initialInspectionState = false;
						} else {
							JOptionPane.showMessageDialog(null, "A(z) "
									+ linenum
									+ ". sor rossz a konfigurációs fájlban. "
									+ "Érvénytelen érték: " + line,
									"Rossz konfigurációs fájl",
									JOptionPane.WARNING_MESSAGE);
							initialInspectionState = true;
						}
					} else if (key.equals("activepuzzle")) {
						ACTIVE_PUZZLE = Solution.getTypeFromString(val);
					} else if (key.equals("activestats")) {
						try {
							statNum = Integer.parseInt(val);
						} catch (Exception e) {
							JOptionPane.showMessageDialog(null, "A(z) "
									+ linenum
									+ ". sor rossz a konfigurációs fájlban. "
									+ "Érvénytelen érték: " + line,
									"Rossz konfigurációs fájl",
									JOptionPane.WARNING_MESSAGE);
						}

					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Kimenti a beállításokat a konfigurációs fájlba. Ezek a beállítások
	 * jelenleg az Inspection, az aktív kockatípus, valamint az jobb oldalon
	 * kijelzett statisztika. Ez a jövőben bővülhet, de akkor a parseConfIni
	 * függvényt is módosítani kell, illetve újabb tagváltozókkal bővülhet az
	 * osztály, és ezek fényében máshol is kellhet módosítani.
	 */
	private void saveSettingsToConfFile() {
		if (!confDir.exists()) {
			if (confDir.mkdirs() == false) {
				JOptionPane
						.showMessageDialog(
								null,
								"Hiba mentés közben",
								"Nem sikerült létrehozni a konfigurációs fájlt. A beállítások nem lettek elmentve.",
								JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(confFile));
			bw.write("Inspection="
					+ (inspectionCheckBox.isSelected() ? "ON" : "OFF") + "\n");
			bw.write("ActivePuzzle="
					+ activePuzzleCombo.getSelectedItem().toString() + "\n");
			bw.write("ActiveStats=" + displayPuzzleCombo.getSelectedIndex()
					+ "\n");
			bw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Kimenti az összes játékhoz tartozó statisztikákat a futási könyvtárba egy
	 * csv fájlba.
	 */
	private void saveAllStastToFile() {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(
					"jrt_stats.csv"));
			bw.write("");
			bw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		for (SolutionCollector sc : sessions) {
			sc.saveSolutions();
		}
	}

	/**
	 * Az ablak jobb alsó sarkában kijelzett statisztikákat frissíti.
	 */
	private void updateSessionStats() {
		if (sessionInfo != null && displayPuzzleCombo != null) {
			SolutionCollector sc = sessions.get(displayPuzzleCombo
					.getSelectedIndex());
			sessionInfo
					.setText("<center><table border='0' cellpadding='0' cellspacing='0' width=100% >"
							+ "<tr><td align='left'>"
							+ sc.getRowCount()
							+ " solves, mean:</td><td align='rigth'>"
							+ Solution.getSolveStrFromMs(sc.getSolutionsMean())
							+ "</td></tr>"
							+ "<tr><td align='left'>"
							+ "Best single: </td><td align='rigth'>"
							+ Solution.getSolveStrFromMs(sc.getBestSolveTime())
							+ "</td></tr>"
							+ "<tr><td align='left'>"
							+ "Best Average 5:</td><td align='rigth'>"
							+ Solution.getSolveStrFromMs(sc.getBestAvgN(5))
							+ "</td></tr>"
							+ "<tr><td align='left'>"
							+ "Best Average 12: </td><td align='rigth'>"
							+ Solution.getSolveStrFromMs(sc.getBestAvgN(12))
							+ "</td></tr></table></center>");
		}
	}

	/**
	 * Kis hekkelés. A this "pointert" adja vissza. Erre akkor lehet
	 * szükség, ha az egyik komponens listenerének függvényeiben szeretnénk az
	 * osztályt elérni (mert ott ugye a this az magát a komponenst jelenti...)
	 * 
	 * @return Az objektum referenciája.
	 */
	public JRTMainWindow getThis() {
		return this;
	}

	/**
	 * @return Az ablakhoz tartozó, space billentyűvel vezérelt timer.
	 */
	public SpaceTimer getSpaceTimer() {
		return spaceTimer;
	}

	/**
	 * Mivel a timerText egy elég sajátos jószág, saját inicializálófüggvényt
	 * kapott. Ez létrehozza, beállítja minden tulajdonságát, és hozzáadja a
	 * megfelelő listenereket. Ezek után a timerText dupla kattintásra beviteli
	 * mezővé alakul, ahol kézzel tudunk időt hozzáadni. Szöveg bevitele közben
	 * Escape gombnyomásra érvényteleníthetjük a bevitelt. Az idő bevitele
	 * (formátumellenőrzés után) az Enterre történik. Hogy a program viszonylag
	 * jó barátságot ápoljon az összes monitor összes felbontásával, a timerText
	 * betűmérete alkalmazkodik a widget, illetve az ablak méretéhez. Így az idő
	 * sose fog lelógni a widgetről, viszont nagyobb ablakméret esetén nagyobb
	 * lesz.
	 * 
	 */
	private void initTimerText() {
		timerText = new JTextField("Ready", 12); /* 12 oszlop: hh:mm:ss.sss */
		timerText.setFont(new Font(Font.MONOSPACED, Font.BOLD,
				(int) (initialH * timerFontHeight)));
		timerText.setForeground(Color.BLACK);
		timerText.setEditable(false);
		timerText.setHighlighter(null);
		timerText.setHorizontalAlignment(JTextField.CENTER);
		timerText.setMinimumSize(new Dimension(0, 0));
		timerText.addMouseListener(new MouseInputAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					timerText.setEditable(true);
					timerText.getCaret().setVisible(true);
					timerText.setHighlighter(new DefaultHighlighter());
					timerText.setSelectionStart(0);
					timerText.setSelectionEnd(timerText.getText().length());
				}
			}
		});

		timerText.addKeyListener(new KeyListener() {

			/* Őt hagyjuk békén */
			@Override
			public void keyTyped(KeyEvent arg0) {
			}

			/*
			 * Enterre az aktív beviteli mező értéke hozzáadódik az időkhöz. Az
			 * Escape megszakítja a műveletet.
			 */
			@Override
			public void keyReleased(KeyEvent arg0) {
				/* Ha sima entert nyomtak és aktív a bevitel */
				if (arg0.getKeyCode() == KeyEvent.VK_ENTER
						&& arg0.getModifiers() == 0 && timerText.isEditable()) {
					/*
					 * Formátumellenőrzés, hiba esetén kap a júzer egy
					 * üzenetablakot, és újra próbálkozhat
					 */
					if (!Solution.isValidTimeStr(timerText.getText())) {
						JOptionPane
								.showMessageDialog(
										null,
										"<html>Az idő formátuma nem megfelelő:<br>"
												+ "<p style=\"line-height: 120%;\">„"
												+ timerText.getText()
												+ "”</p>"
												+ "<br>A helyes formátum:<br>"
												+ "<br><font size=+2>óra:perc:másodperc.törtmásodperc</font>"
												+ "<br><br>Pl. 13.012, 1.2, 8:01.12, 39:2:2.123 stb.</html>",
										"Rossz formátum",
										JOptionPane.WARNING_MESSAGE);
						/* Kijelöljük a beviteli mezőben a teljes szöveget */
						timerText.getCaret().setVisible(true);
						timerText.setHighlighter(new DefaultHighlighter());
						timerText.setSelectionStart(0);
						timerText.setSelectionEnd(timerText.getText().length());
					} else {
						/* Hozzáadunk egy új solution-t */
						Solution s = new Solution(scrText.getText(), Solution
								.getSolveMsFromStr(timerText.getText()));
						s.setType((Solution.Type) activePuzzleCombo
								.getSelectedItem());
						try {
							sessions.get(activePuzzleCombo.getSelectedIndex())
									.addSolution(s);
							displayPuzzleCombo
									.setSelectedIndex(activePuzzleCombo
											.getSelectedIndex());
						} catch (Exception e) {
							e.printStackTrace();
						}
						/* Új kirakás, új keverés kell */
						scrText.setText(Scramble.getScramble(
								(Solution.Type) activePuzzleCombo
										.getSelectedItem(), 0));
						/* Deaktiváljuk a beviteli mezőt */
						timerText.setEditable(false);
						timerText.setHighlighter(null);
						timerText.getCaret().setVisible(false);
					}
				} else if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE
						&& arg0.getModifiers() == 0 && timerText.isEditable()) {
					/* Megszakítjuk a bevitelt */
					timerText.setEditable(false);
					timerText.getCaret().setVisible(false);
					timerText.setText("Ready");
				}
			}

			@Override
			public void keyPressed(KeyEvent arg0) {
			}
		});

		/* A timerText átméretezésekor átméretezzük a fontját is */
		timerText.addComponentListener(new ComponentListener() {
			public void componentShown(ComponentEvent e) {
			}

			public void componentResized(ComponentEvent e) {
				resizeTimerTextFontToFill();
			}

			public void componentMoved(ComponentEvent e) {
			}

			public void componentHidden(ComponentEvent e) {
			}
		});

		/*
		 * A szöveg változásakor is átméretezzük a fontot.
		 */
		timerText.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				resizeTimerTextFontToFill();

			}

			public void removeUpdate(DocumentEvent e) {
				resizeTimerTextFontToFill();
			}

			public void insertUpdate(DocumentEvent e) {
				resizeTimerTextFontToFill();
			}
		});
	}

	/**
	 * A timerText fontját méretezi át, hogy a lehető legjobban kitöltse a
	 * rendelkezésre álló teret. Ez a jövőben még finomhangolható, jelenleg elég
	 * nagy léptékben változtat a betűméreten, viszont így elkerülhető az idő
	 * mérése közben egy esetleges vibrálás. A legjobb lenne majdnem minden számértéket
	 * beállíthatóva tenni. Ez jelenleg nem fért bele az időmbe.
	 */
	private void resizeTimerTextFontToFill() {
		int textFieldW = timerText.getWidth();
		FontMetrics metr = getFontMetrics(timerText.getFont());
		int fontH = metr.getHeight();
		int textW = metr.stringWidth(timerText.getText());
		int fontW = metr.stringWidth(" ");
		if (textW > textFieldW || textW < textFieldW * 0.7) {
			int newFontW = fontW;
			/*
			 * 6-nál rövidebb is ugyanakkora helyen férjen el. Ez azért kell,
			 * hogy ha pl csak egy karakter van benne, akkor az ne nőjön meg
			 * marha nagyra.
			 */
			if (timerText.getText().length() <= 6) { /* 6 karakter = ss.SSS */
				newFontW = textFieldW / 7; /* Kis ráhagyás */
			} else if (timerText.getColumns() > 0) {
				newFontW = textFieldW / (timerText.getColumns());
			}
			int newFontH = (int) ((fontH * newFontW * 0.8) / fontW);
			newFontH -= newFontH % 10; /* Ezt az értéket kéne itten finomhangolni!!! */
			timerText.setFont(new Font(Font.MONOSPACED, Font.BOLD, newFontH));
		}
		if (fontH > timerText.getHeight()) {
			int newFontH = (int) (timerText.getHeight() * 0.8);
			newFontH = newFontH % 10;
			timerText.setFont(new Font(Font.MONOSPACED, Font.BOLD, newFontH));
		}
	}

	/**
	 * Kényelmi funkció a timerText frissítésére.
	 * @param text A kijelzett string
	 */
	public void updateTimerText(String text) {
		timerText.setText(text);
	}

	/**
	 * Futás közben ezzel a függvénnyel frissíti a timer a kijelzett időt.
	 */
	public void updateTimerTextRunning() {
		if(spaceTimer == null) {
			return;
		}
		timerText.setForeground(Color.BLACK);
		long elapsed = spaceTimer.getElapsedTimeMs();
		String timeStr = Solution.getSolveStrFromMs(elapsed);
		timerText.setText(timeStr);
	}

	/**
	 * Inspection alatt ezzel a függvénnyel frissíti a timer a kijelzett időt.
	 * 15-től számol vissza, 0 és -2 közt "+2"-t, utána DNF-et jelenít meg.
	 */
	public void updateTimerTextInspecting() {
		long fromInspection = 15 - spaceTimer.getInspectionElapsedTime() / 1000;
		if (fromInspection > 0) {
			if (fromInspection <= 7 && fromInspection > 3) {
				timerText.setForeground(new Color(213, 0, 12));
			}
			if (fromInspection <= 3) {
				timerText.setForeground(new Color(155, 0, 12));
			}
			timerText.setText(String.valueOf(fromInspection));
		} else if (fromInspection > -2) {
			timerText.setForeground(new Color(100, 10, 0));
			timerText.setText("+2");
		} else {
			timerText.setText("DNF");
		}
	}
	
	/**
	 * Beállítja az ablak billentyűparancsait. Az idő indítását/leállítását NEM itt kell
	 * beállítani!!!
	 * Jelenlegi billentyűparancsok:
	 *  - Ctrl+Q: Kilépés.
	 *  - Ctrl+S: Idők kimentése fájlba.
	 */
	private void setupKeyStrokes() {
		/* Beállítjuk a CTRL+Q billentyűparancsot a kilépésre */
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK),
				"doQuit");

		getRootPane().getActionMap().put("doQuit", new AbstractAction() {
			private static final long serialVersionUID = -5757337307734851025L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
				System.gc();
				System.exit(0);
			}
		});

		/* CTRL+S kimenti az időket a fájlba */
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK),
				"doSave");

		getRootPane().getActionMap().put("doSave", new AbstractAction() {
			private static final long serialVersionUID = -5757337307734851025L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				saveAllStastToFile();
			}
		});
	}

	/**
	 * Inicializálja a swing dolgait, az ablakot, a rajta lévő widgeteket stb. A
	 * layoutok le vannak kódolva, nem designerrel lettek elkészítve.
	 */
	private void initComponents() {
		initialW = 800;
		initialH = 600;

		this.setTitle("JRubixTimer");
		this.setSize(new Dimension(initialW, initialH));

		setupKeyStrokes();

		this.setLayout(new BorderLayout());
		this.setFocusable(true);

		leftPanel = new JPanel(new BorderLayout());
		bottomPanel = new JPanel(new BorderLayout());
		leftBottomPanel = new JPanel(new BorderLayout());
		middleBottomPanel = new JPanel(new BorderLayout());
		rightPanel = new JPanel(new BorderLayout());

		/* A timer szövegdoboza */
		initTimerText();

		/*
		 * ComboBox a kocka kiválasztásához, amelyiknek a statjait látni
		 * szeretnénk
		 */
		displayPuzzleCombo = new JComboBox<Object>(Solution.Type.values());
		displayPuzzleCombo.setSelectedIndex(statNum);
		displayPuzzleCombo.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				resultsTable.setModel(sessions.get(displayPuzzleCombo
						.getSelectedIndex()));
				updateSessionStats();
			}

		});

		displayPuzzleCombo.setFocusable(false);

		JPanel p = new JPanel(new BorderLayout());
		p.add(new JLabel("See stats for:"), BorderLayout.WEST);
		p.add(displayPuzzleCombo, BorderLayout.CENTER);
		rightPanel.add(p, BorderLayout.NORTH);

		puzzleLabel = new JLabel("Puzzle:");

		/* Az aktív kocka kiválasztásához */
		activePuzzleCombo = new JComboBox<Object>(Solution.Type.values());
		activePuzzleCombo.setSelectedItem(ACTIVE_PUZZLE);
		activePuzzleCombo.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				// TODO beállított keveréshosszal generáltatni
				scrText.setText(Scramble.getScramble(
						(Solution.Type) activePuzzleCombo.getSelectedItem(), 0));
				displayPuzzleCombo.setSelectedItem(activePuzzleCombo
						.getSelectedItem());
			}

		});

		activePuzzleCombo.setFocusable(false);

		/* Inspection váltásához */
		inspectionCheckBox = new JCheckBox("Inspection");
		inspectionCheckBox.setSelected(initialInspectionState);
		/* Hogy ne lopja el a space leütéseinket */
		inspectionCheckBox.setFocusable(false);
		inspectionCheckBox.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if (spaceTimer != null) {
					spaceTimer.setUseInspection(inspectionCheckBox.isSelected());
				}
			}
		});

		this.leftBottomPanel.add(inspectionCheckBox, BorderLayout.CENTER);

		/* A keverés szövegéhez */
		scrText = new JTextArea(Scramble.getScramble(ACTIVE_PUZZLE, 0));
		scrText.setEditable(false);
		scrText.setLineWrap(true);
		scrText.setWrapStyleWord(true);
		scrText.setMargin(new Insets(5, 5, 5, 5));
		scrText.setMinimumSize(new Dimension(0, 0));
		scrText.setSize(300, 230);
		scrText.setFont(new Font("default", Font.BOLD, 16));
		scrText.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent arg0) {
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				Dimension d = scrText.getPreferredSize();
				scrText.setPreferredSize(d);
				middleBottomPanel.setPreferredSize(d);
				// pack();
			}

			@Override
			public void changedUpdate(DocumentEvent arg0) {
			}
		});
		this.middleBottomPanel.add(new JScrollPane(scrText));

		/* A statok megjelenítéséhez egy szép kis table */
		resultsTable = new ResultsTable(sessions.get(statNum));
		/* Azért JScrollPane, hogy megjelenjenek a headerek és lehessen görgetni */
		rightPanel.add(new JScrollPane(resultsTable), BorderLayout.CENTER);

		sessionInfo = new JTextPane();
		sessionInfo.setContentType("text/html");
		sessionInfo.setEditable(false);
		updateSessionStats();
		// newSessionBtn = new JButton("New session");
		rightPanel.add(sessionInfo, BorderLayout.SOUTH);

		/*
		 * Hogy az időket megjelenítő jobb oldali panel és a minden mást
		 * tartalmazó bal panel arányait lehessen változtatni, szépen berakunk
		 * egy JSplitPane-t.
		 */
		splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel,
				rightPanel);
		splitter.setDividerLocation(0.7);

		bottomPanel.add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				leftBottomPanel, middleBottomPanel), BorderLayout.CENTER);

		/*
		 * A timer szövegdoboza és az alatta lévő dolgok közé berakunk egy
		 * JSplitPane-t, hogy ha nem szeretnénk görgetni a keverés szövegét,
		 * akkor át tudjuk méretezni a dolgokat.
		 */
		leftSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, timerText,
				bottomPanel);
		leftPanel.add(leftSplitter);

		/*
		 * A "Puzzle:" feliratot és a mellette lévő legördülő menüt rárakjuk egy
		 * panelre, majd ezt a panelt a bal alsó sarokba rakjuk, de azon belül
		 * felülre. Ez azért kell, hogy a bal oldal méreteinek változtatásakor a
		 * legördülő menü magassága ne nőjön meg, mert akkor nagyon nagy lesz
		 * rajta a nyíl ikonja, és nem látszik a kocka neve.
		 */
		JPanel panel = new JPanel();
		panel.add(puzzleLabel);
		panel.add(activePuzzleCombo);
		leftBottomPanel.add(panel, BorderLayout.NORTH);

		this.add(splitter, BorderLayout.CENTER);
		this.pack();

		/* Hogy ablak átméretezésénél a splitterek ne ugráljanak el */
		this.addComponentListener(new ComponentListener() {

			@Override
			public void componentHidden(ComponentEvent e) {
			}

			@Override
			public void componentMoved(ComponentEvent e) {
			}

			@Override
			public void componentResized(ComponentEvent e) {
				splitter.setDividerLocation(0.7);
				leftSplitter.setDividerLocation(leftSplitter
						.getMaximumDividerLocation());
			}

			@Override
			public void componentShown(ComponentEvent e) {
			}

		});

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				/*
				 * Ne írjuk bele azt, ami benne van. Inkább kitöröljük, és a
				 * belőle beolvasott dolgokat visszaírjuk
				 */
				saveAllStastToFile();
				saveSettingsToConfFile();
			}
		});

		// http://stackoverflow.com/questions/286727/java-keylistener-for-jframe-is-being-unresponsive
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.addKeyEventDispatcher(new KeyEventDispatcher() {

					@Override
					public boolean dispatchKeyEvent(KeyEvent arg0) {

						if (timerText.isEditable()) {
							return false;
						}

						if (arg0.getID() == KeyEvent.KEY_PRESSED) {

							if (arg0.getModifiers() == 0) {
								if (arg0.getKeyCode() == KeyEvent.VK_SPACE) {
									if (spaceTimer == null) {
										return false;
									}
									if (spaceTimer.getState() == State.RUNNING) {
										try {
											spaceTimer.stopSolution();
											Solution s = new Solution(scrText
													.getText(), spaceTimer
													.getElapsedTimeMs());
											s.setType((Solution.Type) activePuzzleCombo
													.getSelectedItem());
											try {
												sessions.get(
														activePuzzleCombo
																.getSelectedIndex())
														.addSolution(s);
												displayPuzzleCombo
														.setSelectedIndex(activePuzzleCombo
																.getSelectedIndex());
											} catch (Exception e) {
												e.printStackTrace();
											}
										} catch (Exception e) {
											e.printStackTrace();
										}
										// TODO
										// Itt a beállított hosszal kell
										// keverést generáltatni
										scrText.setText(Scramble
												.getScramble(
														(Solution.Type) activePuzzleCombo
																.getSelectedItem(),
														0));
									}

								}
							}
						}

						if (arg0.getID() == KeyEvent.KEY_RELEASED
								&& !resultsTable.isEditing()) {
							if (arg0.getModifiers() == 0) {
								if (arg0.getKeyCode() == KeyEvent.VK_SPACE) {
									if (spaceTimer == null) {
										spaceTimer = new SpaceTimer(getThis());
										spaceTimer
												.setUseInspection(inspectionCheckBox
														.isSelected());
									}
									if (spaceTimer.getState() == State.READY_FOR_INSPECTION) {
										spaceTimer.startInspection();
									} else if (spaceTimer.getState() == State.READY
											|| spaceTimer.getState() == State.INSPECTING) {
										spaceTimer.startSolution();
									} else if (spaceTimer.getState() == State.JUST_STOPPED) {
										spaceTimer.setUseInspection(spaceTimer
												.isUseInspection());
										spaceTimer = null;
										System.gc();
									}
									// } else if (arg0.getKeyCode() ==
									// KeyEvent.VK_P) {
									// PreferencesWindow p = new
									// PreferencesWindow(
									// getThis());
									// p.setVisible(true);
								} else if (arg0.getKeyCode() == KeyEvent.VK_DELETE) {
									if (JOptionPane
											.showConfirmDialog(
													null,
													"Biztosan törölsz "
															+ resultsTable
																	.getSelectedRowCount()
															+ " időt?",
													"Törlés?", 2) == JOptionPane.YES_OPTION) {
										int[] selectedSolves = resultsTable
												.getSelectedRows();
										for (int i = selectedSolves.length - 1; i >= 0; --i) {
											sessions.get(
													displayPuzzleCombo
															.getSelectedIndex())
													.removeSolution(
															selectedSolves[i]);
										}
									}

								}
							}
						}
						return false;
					}
				});
	}
	
	
	
	/**
	 * A program belépési pontja. Megpróbálja beállítani az ablak
	 * megjelenését a rendszerére, majd megjeleníti a timer ablakát.
	 * @param args Parancssori argumentumok, jelenleg nem kezelt.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		JRTMainWindow mainWin = new JRTMainWindow();
		mainWin.setVisible(true);
		mainWin.setSize(mainWin.initialW, mainWin.initialH);
	}
}
