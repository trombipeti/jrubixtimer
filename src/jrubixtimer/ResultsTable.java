package jrubixtimer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import sun.swing.DefaultLookup;

/**
 * 
 * Egy idők megjelenítésére alkalmas JTable osztály. TableModel-ként kötelezően
 * egy SolutionCollectort használ, és ennek a függvényeit hívogatva jeleníti meg
 * az időket. A színeket/megjelenítési módokat a jövőben szeretném
 * konfigurálhatóvá tenni. Addig is az alábbiak érvényesek: A legjobb idő
 * háttérszíne zöld. A legjobb 5 átlag idejének háttérszíne világoskék. Ha több
 * is van, az összes színes. A legutóbbi legjobb 5 átlagban lévő idők
 * háttlérszíne is világoskék, betűtípusuk dőlt és vastag. 12 átlagnál
 * világoskék helyett világoslilát használok, és a betűtípus csak vastag, nem
 * dőlt.
 * 
 * Csak az idők módosítására van lehetőség utólag, és módosításkor az átlagok
 * újraszámolódnak. Új idő hozzáadáskor a lista aljára görgetünk.
 * 
 * 
 */
public class ResultsTable extends JTable {

	/**
	 * Örüljön a drága eclipse
	 */
	private static final long serialVersionUID = -2089962543953965134L;

	private SolutionCollector collector;

	private Color bestSolveColor;
	private Color bestAvg5Color;
	private Color bestAvg12Color;

	public Color getBestSolveColor() {
		return bestSolveColor;
	}

	public void setBestSolveColor(Color bestSolveColor) {
		this.bestSolveColor = bestSolveColor;
	}

	public Color getBestAvg5Color() {
		return bestAvg5Color;
	}

	public void setBestAvg5Color(Color bestAvg5Color) {
		this.bestAvg5Color = bestAvg5Color;
	}

	public Color getBestAvg12Color() {
		return bestAvg12Color;
	}

	public void setBestAvg12Color(Color bestAvg12Color) {
		this.bestAvg12Color = bestAvg12Color;
	}

	public SolutionCollector getCollector() {
		return collector;
	}

	public ResultsTable(SolutionCollector sc) {
		super(sc);
		collector = (SolutionCollector) getModel();
		init();
	}

	public ResultsTable() {
		init();
	}

	public ResultsTable getThis() {
		return this;
	}

	private void init() {
		setFillsViewportHeight(true);
		this.getTableHeader().setFont(new Font("SansSerif", Font.PLAIN, 11));
		this.addComponentListener(new ComponentListener() {
			@Override
			public void componentShown(ComponentEvent arg0) {
			}

			@Override
			public void componentResized(ComponentEvent arg0) {
				scrollRectToVisible(getCellRect(getRowCount() - 1, 0, true));
			}

			@Override
			public void componentMoved(ComponentEvent arg0) {
			}

			@Override
			public void componentHidden(ComponentEvent arg0) {
			}
		});
		setDefaultRenderer(String.class, new ResultsCellRenderer(
				getDefaultRenderer(String.class)));
		bestSolveColor = new Color(0, 255, 0);
		bestAvg5Color = new Color(56, 175, 255);
		bestAvg12Color = new Color(235, 0, 241);
	}

	/**
	 * Ez az osztály végzi a cellák kirajzolását. Hogy nekünk ne kelljen
	 * foglalkozni a low-level dolgokkal, eltárolunk egy TableCellRenderert, és
	 * az általa kirajzolt komponensnek állítjuk át a paramétereit.
	 */
	class ResultsCellRenderer implements TableCellRenderer {

		private TableCellRenderer renderer;

		/**
		 * @param defRenderer
		 *            Egy TableCellRenderer, amely alapból kirajzolná a
		 *            cellákat. Jellemzően:
		 *            aTable.setDefaultRenderer(valami.class, new
		 *            ResultsCellRenderer( getDefaultRenderer(valami.class)));
		 */
		public ResultsCellRenderer(TableCellRenderer defRenderer) {
			this.renderer = defRenderer;
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {

			/*
			 * Lekérjük az alap kirajzoló komponens, ezen fogunk tetszés szerint
			 * módosítani. Mivel ez módolsulhatott az "előző körben", majd
			 * resetelni kell.
			 */
			Component component = renderer.getTableCellRendererComponent(table,
					value, isSelected, hasFocus, row, column);

			/*
			 * Beállítjuk az alapértelmezettre a háttérszínét és a fontot,
			 * biztos, ami biztos.
			 */
			component.setBackground(DefaultLookup.getColor(getThis(), ui,
					"Table.dropCellBackground"));
			Font ff = component.getFont();
			component.setFont(new Font(ff.getName(), Font.PLAIN, ff.getSize()));

			/*
			 * Eltároljuk az oszlop fejlécét és az adott cella értékét, mert
			 * több helyen is szükség lesz rá.
			 */
			String colHeader = (String) getTableHeader().getColumnModel()
					.getColumn(column).getHeaderValue();
			Object valAt = table.getValueAt(row, column);

			collector = (SolutionCollector) getModel();

			/*
			 * Ha minden adat érvényes, akkor elkezdjük vizsgálni a fejléceket.
			 * Elsőként az idő jön.
			 */
			if (collector != null && valAt != null && colHeader.equals("Time")) {

				/* Benne van-e valamelyik legjobb átlagban? */
				boolean inBestAvgs = false;

				/*
				 * Ha a legjobb 12-átlagban van benne, akkor csak vastagítunk
				 * később.
				 */
				if (collector.getBestAvgNIndeces(12).contains(row)) {
					inBestAvgs = true;
					component.setBackground(bestAvg12Color);
				}

				/*
				 * Ha benne van a legjobb 5-átlagban, akkor dőltté és vastaggá
				 * tesszük a fontot, a hátteret pedig kékké.
				 */
				if (collector.getBestAvgNIndeces(5).contains(row)) {
					Font f = component.getFont();
					component.setFont(new Font(f.getName(), Font.ITALIC, f
							.getSize()));
					component.setBackground(bestAvg5Color);
					inBestAvgs = true;
				}

				/* Megvastagítjuk a fontot, ha bármelyikben benne van */
				if (inBestAvgs == true) {
					Font f = component.getFont();
					component.setFont(new Font(f.getName(), f.getStyle()
							+ Font.BOLD, f.getSize()));
				}
				if (valAt.equals(Solution.getSolveStrFromMs(collector
						.getBestSolveTime()))) {
					component.setBackground(bestSolveColor);
				}
			} else if (colHeader.equals("Avg. 5")) {
				long best5 = collector.getBestAvgN(5);
				if (best5 > 0
						&& valAt.equals(Solution.getSolveStrFromMs(best5))) {
					component.setBackground(bestAvg5Color);
				}
			} else if (colHeader.equals("Avg. 12")) {
				long best12 = collector.getBestAvgN(12);
				if (best12 > 0
						&& valAt.equals(Solution.getSolveStrFromMs(best12))) {
					component.setBackground(bestAvg12Color);
				}
			}
			if (isSelected) {
				component.setBackground(DefaultLookup.getColor(getThis(), ui,
						"Table.selectionBackground"));
			}
			return component;
		}
	}

}
