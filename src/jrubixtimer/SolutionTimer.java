/**
 * 
 */
package jrubixtimer;

/**
 * Az idő/inspection mérésére alkalmas interfész.
 * 
 */
public interface SolutionTimer {

	/**
	 * A timer állapotát rögzítő enumeráció. READY: A timer kész elindulni
	 * ("nyugalmi" állapot) READY_FOR_INSPECTION: Ha használ inspectiont, akkor
	 * ez a nyugalmi állapot. INSPECTING: Inspection állapot, nem meglepő.
	 * RUNNING: Tényleges időmérés. JUST_STOPPED: A timert éppen megállították.
	 * Ez azért lehet szükséges, mert ha billentyűlenyomás állítja le, és
	 * felengedés indítja el, akkor a leállítás utáni felengedésre nem szabad
	 * elindulnia a timernek.
	 */
	static enum State {
		READY, READY_FOR_INSPECTION, INSPECTING, RUNNING, JUST_STOPPED
	}
	
	
	/**
	 * Az inspection indítására szolgáló függvény.
	 */
	void startInspection(); 

	/**
	 * Az inspection leállítására szolgáló függvény.
	 */
	void stopInspection();

	/**
	 * A tényleges időmérés indítására szolgáló függvény.
	 */
	void startSolution();

	/**
	 * Az időmérés leállítására szolgáló függvény.
	 */
	void stopSolution();

	/**
	 * Az eltelt idő lekérdezésére szolgáló függvény.
	 * @return Az indítás óta eltelt idő ezredmásodpercekben.
	 */
	long getElapsedTimeMs();
}
