package dpm.repository;

import dpm.blocksearch.BlockSearch;
import dpm.claw.Claw;
import dpm.finalization.Finalization;
import dpm.localization.Localization;
import dpm.navigation.Navigation;
import dpm.odometry.Odometer;

public class Repository {

	private static final long	INTERRUPT_DELAY = 5000l;
	
	private static BlockSearch blockSearch;
	private static Finalization finalization;
	private static Localization localization;
	private static Navigation navigation;
	private static Odometer odometry;
	private static Claw pincer;
	
	/**
	 * Drops held blocks
	 */
	public static void drop(){
		getPincer().drop();
	}
	
	/**
	 * Returns the navigation subsystem. Initializes
	 * the subsysetm if it is not yet initialized
	 * @return the navigation subsystem
	 */
	public static Navigation getNavigation(){
		if(navigation == null)
			navigation = new Navigation();
		return navigation;
	}
	
	/**
	 * Returns the odometry subsystem. Initializes
	 * the subsysetm if it is not yet initialized
	 * @return the odometry subsystem
	 */
	public static Odometer getOdometer(){
		if(odometry == null)
			odometry = new Odometer();
		return odometry;
	}
	
	//This is private because no subsystem should
	//require direct access to the Pincer object
	private static Claw getPincer(){
		if(pincer == null)
			pincer = new Claw();
		return pincer;
	}
	
	public static void grab(){
		getPincer().grab();
	}
	
	/**
	 * Interrupts the ongoing Block Search subsystem algorithm execution and
	 * any possible ongoing use of the Navigation subsystem. A delay is introduced
	 * to let any other ongoing tasks finish.
	 */
	public static void interruptBlockSearch(){
		blockSearch.interrupt();
		//navigation.interrupt();
		try {Thread.sleep(INTERRUPT_DELAY);} catch(InterruptedException e){}
	}
	
	/**
	 * Localizes the robot
	 */
	public static void localize(){
		if(localization == null)
			localization = new Localization();
		localization.doLocalization();
	}
	
	/**
	 * Executes the finalization routine
	 */
	public static void doFinalization(){
		if(finalization == null)
			finalization = new Finalization();
		finalization.doFinalization();
	}
}
