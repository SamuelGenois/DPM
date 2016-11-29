package dpm.launcher;

import java.util.ArrayList;

public class TestAlgorithms{
	
	public static void main(String[] args){
		//testScanPoint();
		//testOdoCorr();
		//testGetRegion();
		//testShortestPath();
		//testCreateRegionOrder();
		testGetDistance();
	}
	
	private static void testGetDistance() {
		double	distance = 100,
				x = 30,
				y = 15,
				angle = 0;
		double[] badZone = {1*30, 2*30, 2*30, 1*30};
		
		System.out.println(Integer.toString(getDistance(distance, x, y, angle, badZone)));
	}

	private static void testScanPoint(){
		for(int region = 0; region<16; region++){
			double x = (region%4)*3;
			double y = (region/4)*3;
			System.out.printf("region: %d \tx: %f \ty: %f\n", region, x, y);
		}
	}
	
	private static void testOdoCorr(){
		odoCorr(15, 15); //No correction expected
		odoCorr(30, 61.999); //No correction expected
		odoCorr(31.999, 27.999); //x correction expected
		odoCorr(7, 118.1); //y correction expected
	}
	
	private static void odoCorr(double x, double y){
		
		final double SQUARE_SIZE = 30.0;
		final double CORRECTION_THRESHOLD = 2.0;
		
		double xDistanceFromGrid = x%SQUARE_SIZE;
		if(xDistanceFromGrid > SQUARE_SIZE/2)
			xDistanceFromGrid = SQUARE_SIZE - xDistanceFromGrid;
		
		double yDistanceFromGrid = y%SQUARE_SIZE;
		if(yDistanceFromGrid > SQUARE_SIZE/2)
			yDistanceFromGrid = SQUARE_SIZE - yDistanceFromGrid;
		
		if(xDistanceFromGrid < CORRECTION_THRESHOLD){
			if(!(yDistanceFromGrid < CORRECTION_THRESHOLD))
				System.out.printf("Correction of x: %f\n", xDistanceFromGrid);
			else
				System.out.print("No correction\n");
		}
		else if(yDistanceFromGrid < CORRECTION_THRESHOLD)
			System.out.printf("Correction of y: %f\n", yDistanceFromGrid);
		
		else{
			System.out.print("No correction\n");
		}
	}
	
	private static void testGetRegion(){
		
		for(Integer i : getRegions(new int[] {1, 2, 2, 1}))
			System.out.print(i+", ");
		
		System.out.println();
	}
	
	private static void testShortestPath(){
		
		ArrayList<Integer> badZoneRegions = new ArrayList<>();
		ArrayList<Integer[]> edges = new ArrayList<>();
		badZoneRegions.add(2);
		badZoneRegions.add(6);
		
		//For every region i...
		for(int i=0; i<16; i++){
			
			//If i is not a bad zone region
			if(!badZoneRegions.contains(i)){
				
				//If i is not in the topmost row of regions
				if(i<12){
					
					//If i is not in the leftmost column of regions
					//and if the upper left adjacent region is not a bad zone region,
					//add the edge between it and i to edges.
					if((i%4)>0 && !badZoneRegions.contains(i+3))
						edges.add(new Integer[]{i, i+3});
					
					//If the upper adjacent region is not a bad zone region,
					//add the edge between it and i to edges.
					if(!badZoneRegions.contains(i+4))
						edges.add(new Integer[]{i, i+4});
					
					//If i is not in the rightmost column of regions
					//and if the upper right adjacent region is not a bad zone region,
					//add the edge between it and i to edges.
					if((i%4)<3 && !badZoneRegions.contains(i+5))
						edges.add(new Integer[]{i, i+5});

				}
				
				//If i is not in the rightmost column of regions
				//and if the right adjacent region is not a bad zone region,
				//add the edge between it and i to edges.
				if((i%4)<3 && !badZoneRegions.contains(i+1))
					edges.add(new Integer[]{i, i+1});
				
			}
		}
		
		ArrayList<Integer> path = getShortestPath(3, 8, edges);
		
		System.out.println();
		
		for(Integer n : path)
			System.out.print(n+", ");
		System.out.println();
	}
	
	private static void testCreateRegionOrder(){
		
		int[] regionOrder = createRegionOrder(3, new int[]{6, 11, 8, 10}, new int[] {-1, 4, 1, 0}, true);
		
		for(int i=3; i>=0; i--){
			for(int j=0; j<4; j++){
				boolean orderFound = false;
				for(int a=0; a<16; a++){
					if(regionOrder[a] == i*4+j){
						System.out.print(a);
						orderFound = true;
						break;
					}
				}
				if(!orderFound)
					System.out.print("-");
				System.out.print("\t");
			}
			System.out.println();
		}
		
	}
	
	private static int[] createRegionOrder(int startingCorner, int[] greenZone, int[] redZone, boolean isBuilder){
		
		int[] regionOrder = new int[16];
		for(int i=0; i<16; i++)
			regionOrder[i] = -1;
		
		regionOrder[0] = startingCorner;
		ArrayList<Integer> goodZoneRegions, badZoneRegions;
		
		if(isBuilder){
			goodZoneRegions = getRegions(greenZone);
			badZoneRegions = getRegions(redZone);
		}
		else {
			goodZoneRegions = getRegions(redZone);
			badZoneRegions = getRegions(greenZone);
		}
		
		
		ArrayList<Integer[]> edges = new ArrayList<>();
		
		//For every region i...
		for(int i=0; i<16; i++){
			
			//If i is not a bad zone region
			if(!badZoneRegions.contains(i)){
				
				//If i is not in the topmost row of regions
				if(i<12){
					
					//If i is not in the leftmost column of regions
					//and if the upper left adjacent region is not a bad zone region,
					//add the edge between it and i to edges.
					if((i%4)>0 && !badZoneRegions.contains(i+3))
						edges.add(new Integer[]{i, i+3});
					
					//If the upper adjacent region is not a bad zone region,
					//add the edge between it and i to edges.
					if(!badZoneRegions.contains(i+4))
						edges.add(new Integer[]{i, i+4});
					
					//If i is not in the rightmost column of regions
					//and if the upper right adjacent region is not a bad zone region,
					//add the edge between it and i to edges.
					if((i%4)<3 && !badZoneRegions.contains(i+5))
						edges.add(new Integer[]{i, i+5});

				}
				
				//If i is not in the rightmost column of regions
				//and if the right adjacent region is not a bad zone region,
				//add the edge between it and i to edges.
				if((i%4)<3 && !badZoneRegions.contains(i+1))
					edges.add(new Integer[]{i, i+1});
				
			}
		}
		
		ArrayList<Integer> pathToGoodZone;
		ArrayList<Integer[]> leftovers = new ArrayList<Integer[]>();
		
		if(!goodZoneRegions.isEmpty()){
		
			//System.out.println(regionOrder[0]);
			pathToGoodZone = getShortestPath(regionOrder[0], goodZoneRegions.get(0), edges);
			
			for(int i=0; i<16; i++)
				if(!(pathToGoodZone.contains(i) || badZoneRegions.contains(i)))
					leftovers.add(new Integer[]{i, getShortestPath(goodZoneRegions.get(0), i, edges).size()});
		}
		
		else{
			pathToGoodZone = new ArrayList<>();
			
			for(int i=0; i<16; i++)
				if(!badZoneRegions.contains(i))
					leftovers.add(new Integer[]{i, getShortestPath(startingCorner, i, edges).size()});
		}
		
		//Sort the leftovers in increasing order of shortest path from the green zone upper right region.
		for(int i=leftovers.size()-1; i>=0; i--){
			int maxIndex = 0;
			for(int j=0; j<i; j++)
				if(leftovers.get(j)[1] >= leftovers.get(maxIndex)[1])
					maxIndex = j;
			Integer[] temp = leftovers.get(i);
			leftovers.set(i, leftovers.get(maxIndex));
			leftovers.set(maxIndex, temp);
		}
		
		for(int i=1; i<pathToGoodZone.size(); i++)
			regionOrder[i] = pathToGoodZone.get(i);
		
		for(int i=0; i<leftovers.size(); i++){
			regionOrder[i+pathToGoodZone.size()] = leftovers.get(i)[0];
		}
		
		return regionOrder;
	}
	
	private static ArrayList<Integer> getRegions(int[] zone){
		ArrayList<Integer> regions = new ArrayList<>();
		
		if(	   zone[0]<-1 || zone[0]>10
			|| zone[1]<0 || zone[1]>11
			|| zone[2]<0 || zone[2]>11
			|| zone[3]<-1 || zone[3]>10
			|| zone[1]<-1 || zone[1]>11
			|| zone[2]<-1 || zone[2]>11
			|| zone[3]<-1 || zone[3]>11
			|| zone[2]<zone[0] || zone[3]>zone[1])
				return new ArrayList<Integer>();
		
		regions.add(0, ((zone[0]+1)/3) + 4*(zone[1]/3));
		regions.add(1, (zone[2]/3) + 4*((zone[3]+1)/3));
		
		if(regions.get(0)%4 != regions.get(1)%4){
			regions.add(regions.get(0)+1);
			regions.add(regions.get(1)-1);
		}
		if(regions.get(0)/4 != regions.get(1)/4){
			regions.add(regions.get(0)-4);
			regions.add(regions.get(1)+4);
		}
		
		return regions;
	}
	
	private static ArrayList<Integer> getShortestPath(Integer start, Integer end, ArrayList<Integer[]> edges){
		ArrayList<ArrayList<Integer[]>> edgesUsed = new ArrayList<>();
		ArrayList<ArrayList<Integer>> nodesFound = new ArrayList<>();
		boolean[] discovered = new boolean[16];
		
		int i = 0;
		nodesFound.add(new ArrayList<Integer>());
		nodesFound.get(i).add(start);
		discovered[start] = true;
		
		while(!nodesFound.get(i).contains(end)){
			
			if(nodesFound.get(i).isEmpty())
				return new ArrayList<Integer>();
			
			edgesUsed.add(new ArrayList<Integer[]>());
			nodesFound.add(new ArrayList<Integer>());
			
			for(Integer node : nodesFound.get(i))
				for(Integer[] edge: edges){
					if(edge[0] == node && !discovered[edge[1]]){
						nodesFound.get(i+1).add(edge[1]);
						edgesUsed.get(i).add(edge);
						discovered[edge[1]] = true;
					}
					if(edge[1] == node && !discovered[edge[0]]){
						nodesFound.get(i+1).add(edge[0]);
						edgesUsed.get(i).add(edge);
						discovered[edge[0]] = true;
					}
				}
			i++;
		}
		
		ArrayList<Integer> path = new ArrayList<>();
		Integer node = end;
		path.add(node);
		
		for(int j=0; j<i; j++){
			
			for(Integer[] edge : edgesUsed.get(i-1-j)){
				if(edge[0] == node){
					node = edge[1];
					break;
				}
				if(edge[1] == node){
					node = edge[0];
					break;
				}
			}
			path.add(0, node);
		}
		
		return path;
	}
	
	private static int getDistance(double distance, double x, double y, double angle, double badZone[]){
		int modifiedDistance = (int)distance;
		int distanceFromEdge = modifiedDistance;
		
		//If the robot is facing right and the left side of the bad zone is at the robot's right
		if((angle<90 || angle>270) && badZone[0]-x >= 0){
			//Calculate the y value of the intersection of the robot's direction and the vertical line
			double yIntercept = Math.tan(Math.toRadians(angle))*(badZone[0]-x)+y;
			System.out.println("Case1: y = " + yIntercept);
			//If the intersection point is within the left edge of the badZone
			if(yIntercept <= badZone[1] && yIntercept >= badZone[3])
				//Calculate the distance from the left badZone as seen by the us sensor
				distanceFromEdge = (int)calculateDistance(x, y, badZone[0], yIntercept);
			//If that calculated distance is lesser than the current output, set the current output to the calculated distance
			if(distanceFromEdge < modifiedDistance)
				modifiedDistance = distanceFromEdge;
		}
		
		//If the robot is facing up and the bottom side of the bad zone is above the robot 
		if((angle<180 && angle>0) && badZone[3]-y >= 0){
			//Calculate the x value of the intersection of the robot's direction and the horizontal line
			double xIntercept = (badZone[3]-y)/Math.tan(Math.toRadians(angle))+x;
			System.out.println("Case2: x = " + xIntercept);
			//If the intersection point is within the bottom edge of the badZone
			if(xIntercept <= badZone[2] && xIntercept >= badZone[0])
				//Calculate the distance from the left badZone as seen by the us sensor
				distanceFromEdge = (int)calculateDistance(x, y, xIntercept, badZone[3]);
			//If that calculated distance is lesser than the current output, set the current output to the calculated distance
			if(distanceFromEdge < modifiedDistance)
				modifiedDistance = distanceFromEdge;
		}
		
		//If the robot is facing left and the right side of the bad zone is at the robot's left
		if((angle<270 && angle>90) && badZone[2]-x <= 0){
			//Calculate the y value of the intersection of the robot's direction and the vertical line
			double yIntercept = Math.tan(Math.toRadians(angle))*(badZone[2]-x)+y;
			System.out.println("Case3: y = " + yIntercept);
			//If the intersection point is within the right edge of the badZone
			if(yIntercept <= badZone[1] && yIntercept >= badZone[3])
				//Calculate the distance from the left badZone as seen by the us sensor
				distanceFromEdge = (int)calculateDistance(x, y, badZone[2], yIntercept);
			//If that calculated distance is lesser than the current output, set the current output to the calculated distance
			if(distanceFromEdge < modifiedDistance)
				modifiedDistance = distanceFromEdge;
		}
		
		//If the robot is facing down and the top side of the bad zone is below the robots 
		if((angle<360 && angle>180) && badZone[1]-y <= 0){
			//Calculate the x value of the intersection of the robot's direction and the horizontal line
			double xIntercept = (badZone[1]-y)/Math.tan(Math.toRadians(angle))+x;
			System.out.println("Case4: x = " + xIntercept);
			//If the intersection point is within the top edge of the badZone
			if(xIntercept <= badZone[2] && xIntercept >= badZone[0])
				//Calculate the distance from the left badZone as seen by the us sensor
				distanceFromEdge = (int)calculateDistance(x, y, xIntercept, badZone[1]);
			//If that calculated distance is lesser than the current output, set the current output to the calculated distance
			if(distanceFromEdge < modifiedDistance)
				modifiedDistance = distanceFromEdge;
		}
		
		return modifiedDistance;
	}
	
	private static double calculateDistance(double x_init, double y_init, double x_fin, double y_fin){
		return Math.sqrt(Math.pow(x_fin-x_init, 2)+Math.pow(y_fin-y_init, 2));
	}
}
