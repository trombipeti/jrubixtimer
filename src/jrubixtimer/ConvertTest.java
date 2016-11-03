package jrubixtimer;

import static org.junit.Assert.*;
import jrubixtimer.SolutionCollector.WrongSolutionTypeException;

import org.junit.Before;
import org.junit.Test;

public class ConvertTest {

	String testTime;
	long testTimeLong;
	SolutionCollector sc;

	@Before
	public void setUp() {
		testTime = "10.123";
		testTimeLong = 12321;
		sc = new SolutionCollector(Solution.Type.CUBE_3X3);
	}

	@Test
	public void test() {
		long tm = Solution.getSolveMsFromStr(testTime);
		assertEquals(10123, tm);
		try {
			Solution s = new Solution("RUFBLD", 10000);
			s.setType(Solution.Type.CUBE_3X3);
			sc.addSolution(s);
			for(int i = 1; i<=6;++i) {
				Solution b = new Solution("RW", i*312);
				b.setType(Solution.Type.CUBE_3X3);
				sc.addSolution(b);
			}
		} catch (WrongSolutionTypeException e) {
			e.printStackTrace();
		}
		assertEquals(7,sc.solutions.size());
		assertEquals(1, sc.getBestSolveIndex());
		assertEquals(312, sc.getBestSolveTime());
		assertEquals(936, sc.getBestAvgN(5));
	}

}
