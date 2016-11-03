package jrubixtimer;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * A kirakást tároló osztály. Jelenleg a típust, kirakás idejét, a keverést, a
 * hozzá tartozó kommentet, valamint az ezzel az idővel befejeződő 5 és 12 rakás
 * átlagát tárolja.
 * 
 * Az osztálynak vannak statikus függvényei is, ezek segítik az idő
 * konvertálását szöveggés és vissza, stb.
 */

public class Solution {

	private String scramble;
	private Date stopDate;
	private long solveTimeMs;
	private String solveTimeStr;
	private Type type;
	private String comment;

	/**
	 * @return A kirakáshoz tartozó kocka típusa.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Beállítja a kirakáshoz tartozó játék típusát.
	 * 
	 * @param type
	 *            A típus.
	 */
	public void setType(Type type) {
		this.type = type;
	}

	private long avg5;
	private long avg12;

	/**
	 * @author Trombitás Péter
	 * @summary JELENLEG NEM HASZNÁLT! A jövőben tervben van a kirakásokhoz
	 *          büntetések eltárolása is. Ez lehet +2mp büntetés vagy DNF.
	 */
	public static enum Penalty {
		NONE(""), PLUS2("+2"), DNF("DNF");

		private final String text;

		Penalty(String t) {
			text = t;
		}

		public String toString() {
			return text;
		}
	}

	/**
	 * A kirakás típusát meghatározó enumeráció.
	 * Jelenleg 2x2x2-5x5x5 kocka, vagy egyéni kategória van.
	 */
	public static enum Type {
		CUBE_2X2("2x2x2"), CUBE_3X3("3x3x3"), CUBE_4X4("4x4x4"), CUBE_5X5(
				"5x5x5"), CUSTOM("Custom");

		private final String text;

		Type(String t) {
			text = t;
		}

		public String toString() {
			return text;
		}
	}

	/**
	 * Egy inverz toString(), megadja a paraméterként kapott stringről,
	 * hogy milyen típus tartozik hozzá. Ha nem talál, null-t ad vissza.
	 * @param s A típus neve
	 * @return A paraméterhez tartozó típus, vagy null.
	 */
	public static Solution.Type getTypeFromString(String s) {
		Type ret = null;
		for (Type t : Type.values()) {
			if ((t.toString().toLowerCase()).equals(s.toLowerCase())) {
				ret = t;
			}
		}
		return ret;

	}

	/**
	 * Konstruktor, amely csak beállítja a keverést a kirakáshoz. A többi
	 * tulajdonságot a setter függvényekkel kell beállítani.
	 * 
	 * @param scr
	 *            A keverés.
	 */
	public Solution(String scr) {
		this.setScramble(scr);
	}

	/**
	 * Konstruktor, amely beállítja a keverést és a kirakási időt.
	 * 
	 * @param scr
	 *            A keverés
	 * @param solvems
	 *            A kirakás ideje, ezredmásodpercben.
	 */
	public Solution(String scr, long solvems) {
		this.setScramble(scr);
		this.setSolveTimeMs(solvems);
		this.setSolveTimeStr(getSolveStrFromMs(solvems));
	}

	/**
	 * Konstruktor, amely beállítja a keverést és a kirakási időt egy string
	 * alapján.
	 * 
	 * @param scr
	 *            A keverés
	 * @param solveStr
	 *            A kirakás ideje, szöveges formában. A formátumot nem ellenőrzi
	 *            a függvény!
	 */
	public Solution(String scr, String solveStr) {
		this.setScramble(scr);
		this.setSolveTimeStr(solveStr);
		this.setSolveTimeMs(getSolveMsFromStr(solveStr));
	}

	public String getSolveTimeStr() {
		return solveTimeStr;
	}

	public void setSolveTimeStr(String solveTimeStr) {
		this.solveTimeStr = solveTimeStr;
	}

	public long getSolveTimeMs() {
		return solveTimeMs;
	}

	public void setSolveTimeMs(long solveTimeMs) {
		this.solveTimeMs = solveTimeMs;
	}

	public Date getStopDate() {
		return stopDate;
	}

	public void setStopDate(Date stopDate) {
		this.stopDate = stopDate;
	}

	public String getScramble() {
		return scramble;
	}

	public void setScramble(String scramble) {
		this.scramble = scramble;
	}

	public long getAvg5() {
		return avg5;
	}

	public void setAvg5(long avg5) {
		this.avg5 = avg5;
	}

	public long getAvg12() {
		return avg12;
	}

	public void setAvg12(long avg12) {
		this.avg12 = avg12;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * Statikus segédfüggvény. Megmondja, hogy a paraméterként kapott string
	 * teljesíti-e az alábbi feltétleket: 1) Az óra, perc megadása opcionális,
	 * de ha mindekettő meg van adva, kettőspont van köztük. 2) Ha csak a perc
	 * van megadva pluszban, akkor közte és a másodperc közt kettőspont van. 3)
	 * A másodpercet ha megadjuk, akkor ponttal elválasztva tőle legalább egy
	 * tizedesjegyig (tizedmp) meg kell adni a törtmásodpercet is. 4) Ha simán
	 * csak egy számot adunk meg, akkor a program azt ezredmásodpercként
	 * értelmezi.
	 * 
	 * @param str
	 *            Az időt tartalmazó string
	 * @return A megadott string formátuma érvényes-e a fentebb említett
	 *         szempontok alapján.
	 */
	public static boolean isValidTimeStr(String str) {
		return str
				.matches("^([0-5]?[0-9]?):?([0-5]?[0-9])?:?[0-5]?[0-9]\\.([0-9][0-9]?[0-9]?)") || str.equals("DNF");
	}

	/**
	 * Statikus segédfüggvény. Átkonvertálja a milliszekundumban lévő időt
	 * stringgé. Ha ms = -1, akkor "N/A"-t ad vissza, ha -2, akkor az DNF-et jelent.
	 * Nem szálbiztos!!! (A SimpleDateFormat használata miatt)
	 * 
	 * @param ms
	 *            A kirakás ideje ezredmásodpercben.
	 * @return Az idő reprezentációja stringben.
	 */
	public static String getSolveStrFromMs(long ms) {
		if (ms == -2) {
			return "DNF";
		} else if (ms == -1) {
			return "N/A";
		}
		String ret = "";
		if (ms >= 60 * 60 * 1000) {
			/* Több, mint egy óra */
			ret = new SimpleDateFormat("HH:mm:ss.SSS").format(ms);
		} else if (ms >= 60 * 1000) {
			/* Több, mint egy perc */
			ret = new SimpleDateFormat("mm:ss.SSS").format(ms);
		} else {
			ret = new SimpleDateFormat("ss.SSS").format(ms);
		}
		return ret;
	}

	/**
	 * Statikus segédfüggvény. A stringként reprezentált időt átkonvertálja
	 * milliszekundumokká.
	 * 
	 * @param solveStr
	 *            Az idő reprezentációja szövegként. Nincs formátumellenőrzés!
	 * @return Az idő ezredmásodpercekben.
	 */
	public static long getSolveMsFromStr(String solveStr) {
		if (solveStr.equals("DNF")) {
			return -2;
		}
		long ret = 0;
		String times[] = solveStr.split("(\\.|:)");
		for (int i = 1; i <= times.length; ++i) {
			int szor_ezer = 1;
			double hatvan_kitevo = 0;
			if (i == 1) {
				/*
				 * Ha nem adott meg milliszekundumot, csak mondjuk tized mp-et,
				 * akkor kitöltjük 0-val
				 */
				int l = times[times.length - i].length();
				for (int x = 0; x < 3 - l; ++x) {
					times[times.length - i] += '0';
				}
				/*
				 * Itt nem kell ezerrel beszorozni, mert ez magában
				 * milliszekundum már
				 */
				szor_ezer = 0;
			}
			if (i > 2) {
				hatvan_kitevo = (i - 2);
			}
			ret += Long.parseLong(times[times.length - i])
					* Math.pow(60, hatvan_kitevo) * Math.pow(1000, szor_ezer);
		}
		return ret;
	}
}
