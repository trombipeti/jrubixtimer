package jrubixtimer;

import java.util.Date;
import java.util.Random;

/**
 * A keveréseket generáló osztály, csak statikus függvényei vannak, hogy ne
 * kelljen példányt létrehozni belőle. Jelenleg 2x2-5x5 kockákhoz tud keverést
 * adni. Ezek pseudorandom generált keverések, tehát 2x2 és 3x3 esetén nem
 * hivatalosak (ahhoz random state keverést kéne generálni, ami sokkal
 * bonyolultabb folyamat.). A jövőben ezt az osztály brutális módon fel lehet
 * okosítani és bővíteni is lehet újabb típusú keverésekkel (persze csak
 * olyanokkal, amelyek benne vannak a Solution.Type enumban).
 * 
 */
public class Scramble {

	public static String getScramble(Solution.Type type, int scrlen) {
		String ret = "";
		switch (type) {
		case CUBE_2X2:
			ret = get2x2Scramble(scrlen);
			break;
		case CUBE_3X3:
			ret = get3x3Scramble(scrlen);
			break;
		case CUBE_4X4:
			ret = get4x4Scramble(scrlen);
			break;
		case CUBE_5X5:
			ret = get5x5Scramble(scrlen);
			break;
		default:
			break;
		}
		return ret;
	}

	public static String getScramble(String name, int scrlen) {
		String ret = "";
		if ("2x2x2".equals(name)) {
			ret = get2x2Scramble(scrlen);
		} else if ("3x3x3".equals(name)) {
			ret = get3x3Scramble(scrlen);
		} else if ("4x4x4".equals(name)) {
			ret = get4x4Scramble(scrlen);
		} else if ("5x5x5".equals(name)) {
			ret = get5x5Scramble(scrlen);
		}
		return ret;
	}

	public static String get2x2Scramble(int len) {
		Random randgen = new Random(new Date().getTime());
		String scr = new String("");
		String sides[] = { "R", "U", "F" };
		String mod[] = { "", "'", "2" };
		int sd = 10, sd_b = 20;
		for (int i = 0; i < (len == 0 ? 15 : len); ++i) {
			sd_b = sd;
			do {
				sd = (randgen.nextInt(3));
			} while (sd == sd_b);
			scr += sides[sd];
			scr += mod[randgen.nextInt(3)] + " ";
		}
		return scr;
	}

	public static String get3x3Scramble(int len) {
		Random randgen = new Random(new Date().getTime());
		String scr = new String("");
		String sides[] = { "R", "U", "F", "B", "D", "L" };
		String mod[] = { "", "'", "2" };
		int sd = 10, sd_b = 20, sd_b_b = 30;
		for (int i = 0; i < (len == 0 ? 25 : len); ++i) {
			sd_b_b = sd_b;
			sd_b = sd;
			do {
				sd = randgen.nextInt(6);
			} while (sd == sd_b || (sd == sd_b_b && sd_b + sd == 5));
			scr += (sides[sd]);
			scr += (mod[randgen.nextInt(3)]) + " ";
		}
		return scr;
	}

	public static String get4x4Scramble(int len) {
		Random randgen = new Random(new Date().getTime());
		String scr = new String("");
		String sides[] = { "R", "U", "F", "B", "D", "L" };
		String mod[] = { "", "'", "2" };
		int w = 13, w_b = 17, w_bb = 19;
		int s = 17, s_b = 19, s_bb = 13, s_bbb = 23;
		int x, i;
		for (i = 0; i < (len == 0 ? 40 : len); ++i) {
			s_bb = s_b;
			s_b = s;
			w_bb = w_b;
			w_b = w;
			do {
				s = randgen.nextInt(6);
				w = randgen.nextInt(2);
			} while ((s == s_b)
					|| (s == s_bb && s + s_b == 5 && w_bb == w)
					|| ((w & w_b) != 0 && s + s_b == 5)
					|| (s_b == s_bbb && s_bb == s && s + s_b == 5 && s_bbb
							+ s_bb == 5));
			scr += (sides[s]);
			if (w == 1) {
				scr += "w";
			}
			x = randgen.nextInt(3);
			if (x < 2)
				scr += mod[x];
			scr += ' ';
		}
		return scr;
	}

	public static String get5x5Scramble(int len) {
		Random randgen = new Random(new Date().getTime());
		String scr = new String("");
		String sides[] = { "R", "U", "F", "B", "D", "L" };
		String mod[] = { "", "'", "2" };
		int w = 123, w_b = 123, w_b_b = 123; // Random equal numbers NOT 1 or 0.
		int sd = 10, sd_b = 20, sd_b_b = 32; // Random numbers above 5
		for (int i = 0; i < (len == 0 ? 60 : len); ++i) {
			sd_b_b = sd_b;
			sd_b = sd;
			w_b_b = w_b;
			w_b = w;
			do {
				sd = randgen.nextInt(6);
				w = randgen.nextInt(2);
			} while ((sd == sd_b)
					|| (sd == sd_b_b && sd + sd_b == 5 && w_b_b == w));
			scr += (sides[sd]);
			if (w == 1)
				scr += "w";
			int m = randgen.nextInt(3);
			if (m < 2)
				scr += (mod[m]);
			scr += " ";
		}
		return scr;
	}

}
