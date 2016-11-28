package dpm.repository;

import dpm.blocksearch.BlockSearch;
import dpm.claw.Claw;
import dpm.finalization.Finalization;
import dpm.localization.Localization;
import dpm.navigation.Navigation;
import dpm.odometry.Odometer;
import dpm.util.DPMConstants;
import dpm.launcher.Launcher;
/**
 * Repository class that allows any subsystem to interact with any other subsystem indirectly
 * @author Samuel Genois, Emile Traoré
 *
 */
public class Repository implements DPMConstants{

	private static final long	INTERRUPT_DELAY = 5000l;
	
	private static BlockSearch blockSearch;
	private static Finalization finalization;
	private static Localization localization;
	private static Navigation navigation;
	private static Odometer odometry;
	private static Claw pincer;
	private static Launcher launcher;
	
	/**
	 * Executes the block search routine
	 */
	public static void search(){
		getBlockSearch().search();
	}
	
	//This is private because no subsystem should
	//require direct access to the BlockSearch object
	private static BlockSearch getBlockSearch(){
		if(blockSearch == null)
			blockSearch = new BlockSearch();
		return blockSearch;
	}
	
	/**
	 * Travels to the specified (x,y) coordinates, avoiding
	 * obstacles along the way. Initializes the subsystem 
	 * if it is not yet initialized.
	 * 
	 * @param x the x coordinate of the destination
	 * @param y the y coordinate of the destination
	 */
	public static boolean travelTo(double x, double y, int avoidanceSetting){
		return getNavigation().travelTo(x, y, avoidanceSetting);
	}
	
	/**
	 * Travels to the specified (x,y) coordinates, avoiding
	 * obstacles along the way. Initializes the subsystem 
	 * if it is not yet initialized.
	 * 
	 * @param x the x coordinate of the destination
	 * @param y the y coordinate of the destination
	 */
	public static boolean travelTo(double x, double y){
		return getNavigation().travelTo(x, y, AVOID_OR_PICKUP);
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
	 * Interrupts a running travel operation
	 */
	public static void interruptNavigation(){
		getNavigation().interrupt();
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
	public static void setPosition(double[] position, boolean[] whichOne){
		getOdometer().setPosition(position, whichOne);
	}
	
	//This is private because no subsystem should
	//require direct access to the Launcher object.
	//If no Launcher is provided to Repository via the launch method,
	//getLauncher creates a default launcher.
	private static Launcher getLauncher(){
		if(launcher == null)
			launcher = new Launcher(){

				@Override
				public int[] getGreenZone() {
					return new int[] {2, 3, 3, 2};
				}

				@Override
				public int[] getRedZone() {
					return new int[] {-1, -1, 0, -2};
				}

				@Override
				public int getStartZone() {
					return 0;
				}

				@Override
				public int getRole() {
					return BUILDER;
				}
		};
		return launcher;
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
	
	/**
	 * Grabs a block
	 */
	public static void grab(){
		getPincer().grab();
	}
	
	/**
	 * Drops held blocks
	 */
	public static void drop(){
		getPincer().drop();
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
	 * Executes the finalization routine (for final test)
	 */
	public static void doFinalization(){
		if(finalization == null)
			finalization = new Finalization();
		finalization.doFinalization();
	}
	
	/**
	 * TODO
	 * @return
	 */
	public static double[] getNextDumpZone(){
		return getBlockSearch().getNextDumpZone();
	}
	
	 /** Returns the coordinates of the top left and bottom right corners of the green zone.
	 * @return the coordinates of the top left and bottom right corners of the green zone
	 */
	public static int[] getGreenZone() {
		return getLauncher().getGreenZone();
	}
	
	/**
	 * Returns the coordinates of the top left and bottom right corners of the green zone.
	 * @return the coordinates of the top left and bottom right corners of the green zone
	 */
	public static int[] getRedZone() {
		return getLauncher().getRedZone();
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
		return getLauncher().getStartZone();
	}
	
	/**
	 * Returns the role of the robot.
	 * @return the role of the robot
	 */
	public static int getRole(){
		return getLauncher().getRole();
	}
	
	/**
	 * Returns true if the claw holds no blocks
	 * @return true if the claw holds no blocks
	 */
	public static boolean clawIsEmpty(){
		return pincer.clawIsEmpty();
	}
	
	/**
	 * Returns true if the claw is at full capacity
	 * @return true if the claw is at full capacity
	 */
	public static boolean clawIsFull(){
		return pincer.clawIsFull();
	}
	
	public static void setRT(double r, double t){
		getOdometer().leftRadius = r;
		getOdometer().rightRadius = r;
		getOdometer().width = t;
	}
	
	public static boolean quickPickup(double distance){
		return getBlockSearch().quickPickup(distance);
	}
}