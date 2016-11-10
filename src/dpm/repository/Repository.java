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
	 * Travels to the specified (x,y) coordinates, avoiding
	 * obstacles along the way. Initializes the subsystem 
	 * if it is not yet initialized.
	 * 
	 * @param x the x coordinate of the destination
	 * @param y the y coordinate of the destination
	 */
	public static void travelTo(double x, double y){
		getNavigation().travelTo(x, y);
	}
	
	/**
	 * Rotates the robot to the specified angle. The angle is in degrees.
	 * Angles are counter clockwise from the positive x axis.
	 * 
	 * @param theta	The angle to with the robot must rotate
	 */
	public static void turnTo(double angle){
		getNavigation().turnTo(angle);
	}
	
	/**
	 * Returns the orientation of the robot. Initializes
	 * the Odometry subsystem if not yet been initialized.
	 * @return the orientation of the robot
	 */
	public static double getAng(){
		return getOdometer().getAng();
	}
	
	/**
	 * Returns the position of the robot. Initializes
	 * the Odometry subsystem if not yet been initialized.
	 * @return the position of the robot
	 */
	public static double[] getPosition(){
		return getOdometer().getPosition();
	}
	
	/**
	 * Returns the x coordinate of the robot. Initializes
	 * the Odometry subsystem if not yet been initialized.
	 * @return the x coordinate of the robot
	 */
	public static double getX(){
		return getOdometer().getX();
	}
	
	/**
	 * Returns the x coordinate of the robot. Initializes
	 * the Odometry subsystem if not yet been initialized.
	 * @return the x coordinate of the robot
	 */
	public static double getY(){
		return getOdometer().getY();
	}
	
	/**
	 * Sets the position of the robot. Initializes
	 * the Odometry subsystem if not yet been initialized.
	 * @param position the new position of the robot
	 */
	public static void setPosition(double[] position){
		getOdometer().setPosition(position, new boolean[] {true, true, true});
	}
	
	//This is private because no subsystem should
	//require direct access to the Odometer object
	private static Odometer getOdometer(){
		if(odometry == null)
			odometry = new Odometer();
		return odometry;
	}
	
	//This is private because no subsystem should
	//require direct access to the Navigation object
	private static Navigation getNavigation(){
		if(navigation == null)
			navigation = new Navigation();
		return navigation;
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
