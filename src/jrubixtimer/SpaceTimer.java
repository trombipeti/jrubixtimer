package jrubixtimer;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

/**
 * 
 * Az kirakás idejét/inspectiont mérő timer osztály. A név picit csalóka,
 * ugyanis az osztály nem tud róla, hogy őt mi vezérli, space gombnyomások,
 * egérkattintások vagy cuki kismacskák pislogásai.
 * 
 * Hogy tudja frissíteni az ablakon kijelzett időt, meg kell kapnia egy
 * JRTMainWindow objektumot.
 */
public class SpaceTimer implements SolutionTimer {

	/**
	 * Konstruktor, alapértelmezetten 1 ms frissítési időközt állít be,
	 * és használ inspectiont.
	 * @param aWindow Az időt megjelenítő ablak.
	 */
	public SpaceTimer(JRTMainWindow aWindow) {
		useInspection = true;
		timerUpdateMs = 1;
		state = State.READY_FOR_INSPECTION;
		window = aWindow;
	}

	private Date solveStart;
	private Date solveEnd;
	private Date insStart;

	private int timerUpdateMs;

	/**
	 * Ezzel a függvénnyel lehet lekérdezni a timer frissítési időközét.
	 * 
	 * @return A timer frissítési időköze, ezredmásodpercben.
	 */
	public int getTimerUpdateMs() {
		return timerUpdateMs;
	}

	/**
	 * Beállítja a timer frissítési időközét.
	 * 
	 * @param timerUpdateMs
	 *            A kívánt idő, ezredmásodpercben.
	 */
	public void setTimerUpdateMs(int timerUpdateMs) {
		this.timerUpdateMs = timerUpdateMs;
	}

	/**
	 * Megadja, hogy mikor kezdőtött a kirakás.
	 * 
	 * @return A kirakás kezdetének ideje.
	 */
	public Date getSolveStart() {
		return solveStart;
	}

	/**
	 * Megadja, hogy mikor kezdődött az inspection.
	 * 
	 * @return Az inspection kezdetének ideje.
	 */
	public Date getInspectionStart() {
		return insStart;
	}

	/* Használ-e a timer inspectiont? */
	private boolean useInspection;

	/**
	 * Megadja, hogy a timer használ-e inspectiont.
	 * 
	 * @return A useInspection értéke.
	 */
	public boolean isUseInspection() {
		return useInspection;
	}

	/**
	 * Beállítja, hogy a timer használjon-e inspectiont. A timer állapotát is
	 * átállítja, ha kell (READY és READY_FOR_INSPECTION közt). Tipp: A timer
	 * alapállapotba állítása a következőképpen is elérhető:
	 * aTimer.setUseInspection(aTimer.isUseInspection());
	 * 
	 * @param inspection
	 *            Használjon-e inspectiont a timer.
	 */
	public void setUseInspection(boolean inspection) {
		useInspection = inspection;
		if (this.state == State.READY && inspection == true) {
			this.state = State.READY_FOR_INSPECTION;
		} else if (this.state == State.READY_FOR_INSPECTION
				&& inspection == false) {
			this.state = State.READY;
		}
	}

	private State state;

	/**
	 * @return A timer állapota.
	 */
	public State getState() {
		return state;
	}

	/**
	 * A timer állapotát állítja be a kívánt értékre.
	 * 
	 * @param theState
	 *            A beállítandó állapot.
	 */
	public void setState(State theState) {
		state = theState;
	}

	/**
	 * A timer belső 'órája'. Ez egy java.util.timer, melynek segítségével
	 * időzítetten lehet külön szálakat (TimerTask-okat) futtatni.
	 * Erre az idő kijelzéséhez van szükség.
	 */
	private Timer _timer;
	
	/**
	 * Inspection esetén ez a TimerTask fut le másodpercenként,
	 * és frissíti az ablakon a kijelzett időt.
	 */
	private TimerTask inspection = new TimerTask() {
		@Override
		public void run() {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					window.updateTimerTextInspecting();
				}
			});

		}
	};

	/**
	 * Kirakás közben ez a TimerTask fut le a timerUpdateMs
	 * által meghatározott időközönként. Frissíti a stopper által
	 * kijelzett időt.
	 */
	private TimerTask solution = new TimerTask() {
		@Override
		public void run() {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					window.updateTimerTextRunning();
				}
			});
		}
	};

	/**
	 * Erre a tagra azért van szükség, hogy tudjuk frissíteni
	 * a timerTexten az időt.
	 */
	private JRTMainWindow window;

	/**
	 * Ha nem a konstruktorban adtuk meg az ablakot, akkor
	 * ezzel a függvénnyel megtehetjük.
	 * @param win Az ablak.
	 */
	public void setWindow(JRTMainWindow win) {
		window = win;
	}

	/**
	 * Megadja, hogy mennyi idő telt el az inspection kezdete óta.
	 * @return Az eltelt idő ezredmásodpercekben.
	 */
	public long getInspectionElapsedTime() {
		long ret = 0;
		if (insStart != null) {
			ret = new Date().getTime() - insStart.getTime();
		}
		return ret;
	}

	/**
	 * Megadja a timer indítása óta eltelt időt. Ha a timert
	 * már leállították, akkor a futási idejét adja meg.
	 * @see jrubixtimer.SolutionTimer#getElapsedTimeMs()
	 */
	@Override
	public long getElapsedTimeMs() {
		long ret = 0;
		if (solveEnd != null && solveStart != null) {
			ret = solveEnd.getTime() - solveStart.getTime();
		} else if (solveEnd == null) {
			ret = new Date().getTime() - solveStart.getTime();
		}
		return ret;
	}

	/**
	 * @see jrubixtimer.SolutionTimer#startInspection()
	 */
	@Override
	public void startInspection() {
		if (!useInspection) {
			return;
		} else {
			if (_timer == null) {
				_timer = new Timer();
			}
			this.insStart = new Date();
			_timer.scheduleAtFixedRate(inspection, 0, 1000);
			this.state = State.INSPECTING;
		}
	}

	/* (non-Javadoc)
	 * @see jrubixtimer.SolutionTimer#stopInspection()
	 */
	@Override
	public void stopInspection() {
		inspection.cancel();
	}

	/**
	 * Elindítja az időmérést. Ha a frissítési időköz 0, akkor
	 * a window timerText-jén nem frissíti az időt, csak
	 * beállítja a szövegét "Running"-ra.
	 * @see jrubixtimer.SolutionTimer#startSolution()
	 */
	@Override
	public void startSolution() {
		if (state != State.READY && state != State.INSPECTING) {
			return;
		} else {
			if (_timer == null) {
				_timer = new Timer();
			}
			if (useInspection) {
				inspection.cancel();
			}
			solveStart = new Date();
			if (timerUpdateMs > 0) {
				_timer.scheduleAtFixedRate(solution, 0, timerUpdateMs);
			} else {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						window.updateTimerText("Running");
					}
				});
			}

			this.state = State.RUNNING;
		}
	}

	/**
	 * Leállítja a kirakást. Még egyszer lefrissíti az eltelt időt is.
	 * @see jrubixtimer.SolutionTimer#stopSolution()
	 */
	@Override
	public void stopSolution() {
		if (state != State.RUNNING) {
			return;
		} else {
			solution.cancel();
			solveEnd = new Date();
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					window.updateTimerText(Solution.getSolveStrFromMs(getElapsedTimeMs()));
				}
			});
			this.state = State.JUST_STOPPED;
		}
	}

}
