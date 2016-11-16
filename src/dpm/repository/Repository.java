package dpm.repository;

import dpm.blocksearch.BlockSearch;
import dpm.claw.Claw;
import dpm.finalization.Finalization;
import dpm.launcher.Launcher;
import dpm.localization.Localization;
import dpm.navigation.Navigation;
import dpm.odometry.Odometer;

public class Repository {

	private static final long	INTERRUPT_DELAY = 5000l;
	
	private static Launcher launcher = null;
	
	private static BlockSearch blockSearch;
	private static Finalization finalization;
	private static Localization localization;
	private static Navigation navigation;
	private static Odometer odometry;
	private static Claw claw;
	
	/**
	 * Drops held blocks
	 */
	public static void drop(){
		getClaw().drop();
	}
	
	/**
	 * Executes the block search routine
	 */
	public static void search(){
		getBlockSearch().search();
	}
	
	private static BlockSearch getBlockSearch(){
		if(blockSearch == null)
			blockSearch = new BlockSearch();
		return blockSearch;
	}
	
	/**
	 * Returns true if the claw holds no blocks
	 * @return true if the claw holds no blocks
	 */
	public static boolean clawIsEmpty(){
		return claw.clawIsEmpty();
	}
	
	/**
	 * Returns true if the claw is at full capacity
	 * @return true if the claw is at full capacity
	 */
	public static boolean clawIsFull(){
		return claw.clawIsFull();
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
	 * @param position the new position values of the robot
	 * @param update which values must be updated
	 */
	public static void setPosition(double[] position, boolean[] update){
		getOdometer().setPosition(position, update);
	}
	
	/**
	 * Returns the coordinates of the top left and bottom right corners of the green zone.
	 * @return the coordinates of the top left and bottom right corners of the green zone
	 */
	public static int[] getGreenZone() {
		return launcher.getGreenZone();
	}
	
	/**
	 * Returns the coordinates of the top left and bottom right corners of the green zone.
	 * @return the coordinates of the top left and bottom right corners of the green zone
	 */
	public static int[] getRedZone() {
		return launcher.getRedZone();
	}
	
	/**
	 * Set the launcher subsystem of the repository.
	 * This method is expected to be called by the launcher itself.
	 */
	public static void launch(Launcher launcher){
		Repository.launcher = launcher;
	}
	
	/**
	 * Returns the starting corner of the robot.
	 * @return the starting corner of the robot
	 */
	public static int getStartZone() {
		return launcher.getStartZone();
	}
	
	/**
	 * Returns the role of the robot.
	 * @return the role of the robot
	 */
	public static int getRole(){
		return launcher.getRole();
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
	private static Claw getClaw(){
		if(claw == null)
			claw = new Claw();
		return claw;
	}
	
	public static void grab(){
		getClaw().grab();
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