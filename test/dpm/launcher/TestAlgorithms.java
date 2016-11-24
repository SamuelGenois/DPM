package dpm.launcher;

import java.util.ArrayList;

public class TestAlgorithms {
	
	public static void main(String[] args){
		//testScanPoint();
		//testOdoCorr();
		testCreateRegionOrder();
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
	
	private static void testCreateRegionOrder(){
		int[] regionOrder = createRegionOrder(15, new int[]{1, 1, 3, 2}, null, true);
		
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
		regionOrder[0] = startingCorner;
		ArrayList<Integer> goodZoneRegions, badZoneRegions;
		
		if(isBuilder){
			goodZoneRegions = getRegions(greenZone);
			badZoneRegions = new ArrayList<Integer>();
		}
		else {
			goodZoneRegions = getRegions(redZone);
			badZoneRegions = getRegions(greenZone);
		}
		
		
		ArrayList<Integer[]> edges = new ArrayList<>();
		
		//For every region i...
		for(int i=0; i<16; i++){
			//If 
			if(!badZoneRegions.contains(i)){
				if(i<12 && !badZoneRegions.contains(i+4)){
					if((i%4)<3 && !badZoneRegions.contains(i+5))
						edges.add(new Integer[]{i, i+5});
					edges.add(new Integer[]{i, i+4});
					if((i%4)>0 && !badZoneRegions.contains(i+3))
						edges.add(new Integer[]{i, i+3});
				}
				if((i%4)<3 && !badZoneRegions.contains(i+1))
					edges.add(new Integer[]{i, i+1});
				
			}
		}
		
		ArrayList<Integer> pathToGoodZone = getShortestPath(regionOrder[0], goodZoneRegions.get(0), edges);
		ArrayList<Integer[]> leftovers = new ArrayList<Integer[]>();
		
		for(int i=0; i<16; i++){
			if(!(pathToGoodZone.contains(i) || badZoneRegions.contains(i) || goodZoneRegions.contains(i))){
				leftovers.add(new Integer[]{i, getShortestPath(goodZoneRegions.get(0), i, edges).size()});
			}
		}
		
		/*System.out.println(pathToGoodZone.size());
		System.out.print(leftovers.size());
		System.exit(0);*/
		
		//Sort the leftovers in increasing order of shortest path from the green zone upper right region.
		for(int i=leftovers.size(); i<0; i--){
			int maxIndex = 0;
			for(int j=0; j<i; j++)
				if(leftovers.get(j)[1] >= leftovers.get(maxIndex)[1])
					maxIndex = j;
			Integer[] temp = leftovers.get(i-1);
			leftovers.set(i-1, leftovers.get(maxIndex));
			leftovers.set(maxIndex, temp);
		}
		
		for(int i=1; i<pathToGoodZone.size(); i++)
			regionOrder[i] = pathToGoodZone.get(i);
		
		/*for(int i=0; i<leftovers.size(); i++){
			regionOrder[i+pathToGoodZone.size()] = leftovers.get(i)[0];
		}*/
		
		return regionOrder;
	}
	
	private static ArrayList<Integer> getRegions(int[] zone){
		ArrayList<Integer> regions = new ArrayList<>();
		
		regions.add(zone[0]/3 + 4*(zone[1]/3));
		regions.add(zone[2]/3 + 4*(zone[3]/3));
		
		if(regions.get(0)-3 == regions.get(1)){
			regions.add(regions.get(0)+1);
			regions.add(regions.get(1)-1);
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
		
		while(!nodesFound.get(i).contains(end)){
			if(nodesFound.get(i).isEmpty())
				return null;
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
		
		for(int j=1; j<=i; j++){
			for(Integer[] edge : edgesUsed.get(i-j)){
				if(edge[0] == node)
					node = edge[1];
				if(edge[1] == node)
					node = edge[0];
			}
			path.add(0, node);
		}
		
		return path;
	}
}
