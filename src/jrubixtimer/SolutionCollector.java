package jrubixtimer;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

/**
 * A kirakásokat gyűjtő, valamint azokat modellbe rendező osztály. A
 * ResultsTable osztályt használjuk a megjelenítésére.
 * 
 */
public class SolutionCollector extends AbstractTableModel {

	private static final long serialVersionUID = -7256345845961809264L;

	/**
	 * A kirakásokat tároló lista. Csak egyféle típusú játékhoz tartozó
	 * kirakásokat tartalmazhat.
	 */
	List<Solution> solutions = new ArrayList<Solution>();

	/**
	 * A kirakások típusa a solutions listában.
	 */
	private Solution.Type type;

	/**
	 * Konstruktor, amely beolvassa a futási könyvtárban lévő jrt_stats.csv
	 * fájlból az esetleges előzőleg kimentett időket, és el is tárolj azokat.
	 * Magyarul ugyan onnan folytathatjuk a kockázást, ahol abbahagytuk.
	 * 
	 * @param _type
	 *            A kirakások típusa.
	 */
	public SolutionCollector(Solution.Type _type) {
		this.type = _type;
		// System.out.println(type);
		/*
		 * Szépen beolvassuk az előzőleg elmentett időket a fájlból. Ha még nem
		 * mentettünk ki időt, akkor semmi baj nincs, csak egyszerűen nem
		 * létezik a fájl. Majd megszűnéskor kiírjuk jól.
		 */
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("jrt_stats.csv"));
			while (true) {
				String line;
				line = br.readLine();
				if (line == null)
					break;
				String[] data = line.split(";");
				if (data.length < 4) {
					continue;
				}
				if (!(data[0].equals(type.toString())))
					continue;
				Solution s = new Solution(data[2], data[1]);
				if (!(data[3].equals("null"))) {
					s.setComment(data[3]);
				}
				solutions.add(s);
			}
			br.close();
			recalcAvgs();
		} catch (FileNotFoundException e) { /* Semmi baj */
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Kimenti az időket a jrt_stats.csv fájlba. Azért nem szerializál, hogy
	 * utólag bármikor bemásolhassuk az időket keverésestül, mindenestül
	 * bármilyen fórumra, facebookra stb.
	 */
	public void saveSolutions() {
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter("jrt_stats.csv", true));
			for (Solution s : solutions) {
				String line = "";
				line += type.toString() + ";";
				line += s.getSolveTimeStr() + ";";
				line += s.getScramble() + ";";
				line += s.getComment() + ";";
				bw.write(line + System.getProperty("line.separator"));
			}
			bw.close();
		} catch (FileNotFoundException e) { /* Semmi baj */
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Az oszlopok száma jelenleg 4: Idő, 5 átlag, 12 átlag, komment. Ha ez a
	 * jövőben változik, akkor elég sok mindent meg kell még változtatni ezen a
	 * függvényen kívül is!
	 * 
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return 4;
	}

	/**
	 * A táblázatban lévő sorok számát adja vissza (ez a kirakások száma).
	 * 
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return solutions.size();
	}

	/**
	 * Az első és a negyedik oszlop szerkeszthetőek csak. (Idő és komment)
	 * 
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int row, int col) {
		return (col == 0 || col == 3);
	}

	/**
	 * Az oszlopok headerjei adja vissza. Ez jelenleg hard-code-olva van, a
	 * jövőben szeretném konfigurálhatóvá tenni.
	 * 
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int col) {
		switch (col) {
		case 0:
			return "Time";
		case 1:
			return "Avg. 5";
		case 2:
			return "Avg. 12";
		case 3:
			return "Comment";
		default:
			return "";
		}
	}

	/**
	 * Az oszlopok osztálya, jelenleg mindenhol String.
	 * 
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int col) {
		return String.class;
	}

	/**
	 * A cellák értékét adja meg. Mindig a rowIndex-edik időnek adja meg az
	 * idejét, 5 és 12 átlagát, valamint a hozzá tartozó kommentet, szöveges
	 * formában.
	 * 
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Solution s = solutions.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return s.getSolveTimeStr();
		case 1:
			return Solution.getSolveStrFromMs(s.getAvg5());
		case 2:
			return Solution.getSolveStrFromMs(s.getAvg12());
		case 3:
			return s.getComment();
		default:
			return null;
		}
	}

	/**
	 * Az adott cella értékét állítja be. Ha az időt szeretnénk átírni kézzel
	 * utólag, ez a függvény ellenőrzi a formátumát.
	 * 
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object,
	 *      int, int)
	 */
	@Override
	public void setValueAt(Object val, int row, int col) {
		try {
			switch (col) {
			case 0:
				if (val.getClass() == String.class) {
					if (!Solution.isValidTimeStr((String) val)) {
						JOptionPane.showMessageDialog(null,
								"Rossz az idő formátuma!", "Rossz formátum",
								JOptionPane.WARNING_MESSAGE);
					} else {
						solutions.get(row).setSolveTimeMs(
								Solution.getSolveMsFromStr((String) val));
						solutions.get(row).setSolveTimeStr(
								Solution.getSolveStrFromMs(Solution
										.getSolveMsFromStr((String) val)));
					}
				} else if (val.getClass() == Long.class) {
					solutions.get(row).setSolveTimeStr(
							Solution.getSolveStrFromMs((Long) val));
					solutions.get(row).setSolveTimeMs((Long) val);
				}
				recalcAvgs();
				break;
			case 1:
				if (val.getClass() == String.class) {
					solutions.get(row).setAvg5(
							Solution.getSolveMsFromStr((String) val));
				} else if (val.getClass() == Long.class) {
					solutions.get(row).setAvg5((Long) val);
				}
				break;
			case 2:
				if (val.getClass() == String.class) {
					solutions.get(row).setAvg12(
							Solution.getSolveMsFromStr((String) val));
				} else if (val.getClass() == Long.class) {
					solutions.get(row).setAvg12((Long) val);
				}
				break;
			case 3:
				solutions.get(row).setComment((String) val);
				break;
			default:
				break;
			}
			fireTableDataChanged();
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null,
					"Csak számokat adhatsz meg időnek!", "Rossz formátum",
					JOptionPane.WARNING_MESSAGE);
		}

	}

	/**
	 * Minden kirakáshoz kiszámolja az 5 és 12 átlagot. Ezt a függvényt kell
	 * meghívni, ha bármelyik kirakás megváltozik, hozzáadódik vagy kitörlődik.
	 */
	private void recalcAvgs() {
		for (int i = 0; i < solutions.size(); ++i) {
			long avg5 = getLastAvg(5, i);
			solutions.get(i).setAvg5(avg5);
			long avg12 = getLastAvg(12, i);
			solutions.get(i).setAvg12(avg12);
		}
	}

	/**
	 * Az összes rakáshoz tartozó 5 átlagokat becsomagolja egy listába, és
	 * odaadja nekünk.
	 * 
	 * @return Az 5 átlagok listája.
	 */
	private List<Long> getAllAvg5() {
		if (solutions.size() < 1)
			return null;

		ArrayList<Long> ret = new ArrayList<Long>();
		for (Solution s : solutions) {
			ret.add(s.getAvg5());
		}
		return ret;
	}

	/**
	 * Az összes rakáshoz tartozó 12 átlagokat adja meg.
	 * 
	 * @return Az 12 átlagok listája.
	 */
	private List<Long> getAllAvg12() {
		if (solutions.size() < 1)
			return null;

		ArrayList<Long> ret = new ArrayList<Long>();
		for (Solution s : solutions) {
			ret.add(s.getAvg12());
		}
		return ret;
	}

	/**
	 * Megadja az összes rakás átlagát, a legjobb és legrosszab idővel együtt
	 * számolva. Csak akkor lesz ez DNF, ha _csak_ DNF idők vannak.
	 * 
	 * @return Az összes rakás átlaga.
	 */
	public long getSolutionsMean() {
		if (solutions.size() == 0) {
			return -1;
		}
		long ret;
		int numDNFs = 0;
		long sumtime = 0;
		for (int i = 0; i < solutions.size(); ++i) {
			long thetime = solutions.get(i).getSolveTimeMs();
			if (thetime == -2) {
				numDNFs += 1;
			} else {
				sumtime += thetime;
			}
		}
		if (numDNFs == solutions.size()) {
			return -2;
		}
		ret = sumtime / (solutions.size() - numDNFs);
		return ret;
	}

	/**
	 * Megadja az utolsó n idő átlagát (legjobb és legrosszabb idő nélkül
	 * számolva) a lastSolveIndex-edik időig bezárólag. Értelemszerű okokból n
	 * >= 3. Jelenleg jellemzően n = 5 vagy n = 12.
	 * 
	 * @param n
	 *            Hány idő átlagát szeretnénk tudni.
	 * @param lastSolveIndex
	 *            Az átlag utolsó idejének indexe
	 * @return Az átlag, ezredmásodpercben.
	 */
	public long getLastAvg(int n, int lastSolveIndex) {
		if (solutions.size() < n || lastSolveIndex < (n - 1) || n < 3) {
			return -1;
		}
		long ret;
		int numDNFs = 0;
		long sumtime = 0, besttime, worsttime;
		besttime = worsttime = solutions.get(lastSolveIndex).getSolveTimeMs();
		for (int i = 0; i < n; ++i) {
			long thetime = solutions.get(lastSolveIndex - i).getSolveTimeMs();
			if (thetime < besttime) {
				besttime = thetime;
			}
			if (thetime > worsttime) {
				worsttime = thetime;
			}
			if (thetime == -2) {
				numDNFs += 1;
			}
			sumtime += thetime;
		}
		if (numDNFs > 1) {
			return -2;
		}
		ret = sumtime - besttime;
		if (numDNFs == 0) {
			ret -= worsttime;
		}
		ret = (ret / (n - 2));
		return ret;
	}

	/**
	 * @return A legjobb idő indexe.
	 */
	public int getBestSolveIndex() {
		int ret = 0;
		if (solutions == null || solutions.size() == 0) {
			ret = -1;
		} else {
			for (int i = 0; i < solutions.size(); ++i) {
				if (solutions.get(i).getSolveTimeMs() > 0
						&& solutions.get(i).getSolveTimeMs() < solutions.get(
								ret).getSolveTimeMs()) {
					ret = i;
				}
			}
		}
		return ret;
	}

	/**
	 * @return A legjobb idő, ezredmásodpercben.
	 */
	public long getBestSolveTime() {
		int n = getBestSolveIndex();
		System.out.println(n);
		if (n >= solutions.size() || n < 0) {
			return -1;
		}
		System.out.println(solutions.get(n).getSolveTimeMs());
		return solutions.get(n).getSolveTimeMs();
	}

	/**
	 * Megadja a legjobb n-átlagot a rakásokból.
	 * 
	 * @param n
	 *            Hány rakás átlagaiból adja meg a legjobbat. Jelenleg 5 vagy 12
	 *            lehet csak!
	 * @return A legjobb n-átlag, ezredmásodpercben.
	 */
	public long getBestAvgN(int n) {
		long best = -1;
		if (n == 5 && solutions.size() >= 5) {
			List<Long> avg5s = getAllAvg5();
			best = Collections.max(avg5s);
			for (long l : avg5s) {
				if (l > 0 && l < best) {
					best = l;
				}
			}
		} else if (n == 12 && solutions.size() >= 12) {
			List<Long> avg12s = getAllAvg12();
			best = Collections.max(avg12s);
			for (long l : avg12s) {
				if (l > 0 && l < best) {
					best = l;
				}
			}
		}
		return best;

	}

	/**
	 * Ha több helyen is ugyanannyi az n-átlag, mint a legjobbnak, akkor ez a
	 * függvény megadja az összes ilyen hely indexét.
	 * 
	 * @param n
	 *            Hány rakás átlagaiból adja meg a legjobbakat. Jelenleg 5 vagy
	 *            12 lehet csak!
	 * @return Az indexek egy HashSet-ben.
	 */
	public HashSet<Integer> getBestAvgNIndeces(int n) {
		HashSet<Integer> ret = new HashSet<Integer>();
		if (n == 5 && solutions.size() >= 5) {
			List<Long> avg5s = getAllAvg5();
			long best = getBestAvgN(5);
			long bestIndex = avg5s.lastIndexOf(best);
			for (int i = 0; i < solutions.size(); ++i) {
				if (i >= bestIndex - 4 && i <= bestIndex) {
					ret.add(i);
				}
			}
		} else if (n == 12 && solutions.size() >= 12) {
			List<Long> avg12s = getAllAvg12();
			long best = getBestAvgN(12);
			long bestIndex = avg12s.lastIndexOf(best);
			for (int i = 0; i < solutions.size(); ++i) {
				if (i >= bestIndex - 11 && i <= bestIndex) {
					ret.add(i);
				}
			}
		}
		return ret;
	}

	/**
	 * A lista végére fűz egy kirakást, újraszámolja az átlagokat és frissíti a
	 * megjelenített táblázatot.
	 * 
	 * @param s
	 *            Egy inicializált Solution, jellemzően a legutóbbi rakás
	 *            adataival.
	 * @throws WrongSolutionTypeException
	 */
	public void addSolution(Solution s) throws WrongSolutionTypeException {
		if (s == null) {
			return;
		}
		if (s.getType() != this.type) {
			throw new WrongSolutionTypeException("Nem stimmel a kockatípus: "
					+ s.getType().toString());
		}
		solutions.add(s);
		recalcAvgs();
		fireTableDataChanged();
	}

	/**
	 * Kitörli a megadott indexű időt. Újraszámolja az átlagokat, frissíti a
	 * megjelenített táblázatot.
	 * 
	 * @param index
	 *            A törölni kívánt kirakás indexe.
	 */
	public void removeSolution(int index) {
		solutions.remove(index);
		recalcAvgs();
		fireTableRowsDeleted(index, index);
		fireTableDataChanged();
	}

	/**
	 * @return A tárolt idők típusa.
	 */
	public Solution.Type getType() {
		return type;
	}

	/**
	 * Beállítja a tárolt idők típusát. Jelenleg ezt nem nagyon kéne használni,
	 * ugyanis az az eset nincs lekezelve, hogy már tárol néhány A típust, és
	 * átállítjuk a típust B-re.
	 * 
	 * @param type
	 *            Az új típus.
	 */
	public void setType(Solution.Type type) {
		this.type = type;
	}

	/**
	 * Külön exception arra az esetre, ha rossz típusú rakást próbálnánk
	 * hozzáadni.
	 */
	class WrongSolutionTypeException extends Exception {

		public WrongSolutionTypeException() {
			super();
		}

		public WrongSolutionTypeException(String message, Throwable cause,
				boolean enableSuppression, boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}

		public WrongSolutionTypeException(String message, Throwable cause) {
			super(message, cause);
		}

		public WrongSolutionTypeException(String message) {
			super(message);
		}

		public WrongSolutionTypeException(Throwable cause) {
			super(cause);
		}

		private static final long serialVersionUID = 5657978269451448923L;

	}

}
