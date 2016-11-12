package dpm.launcher;

import java.util.ArrayList;
import java.util.Arrays;

import dpm.util.Printer;
import dpm.util.Sensors;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class TestLight {
	public static void main (String[] args){
		SampleProvider lightSensor = Sensors.getSensor(Sensors.COLOR_BLOCK_ID);
		float[] lightData = new float[lightSensor.sampleSize()];
		String string;
		
		while (true){
			
			lightSensor.fetchSample(lightData, 0);
			if (lightData[1] > lightData[0] && 1000*(lightData[0]+lightData[1]+lightData[2]) > 5){
				string = "Block";
			}
			else if (lightData[0] > lightData[1] && lightData[1] > 2*lightData[2] && 1000*(lightData[0]+lightData[1]+lightData[2]) > 100){
				string = "Obstacle";
			}
			else{
				string = "Floor";
			}
			ArrayList<String> list = new ArrayList(Arrays.asList(""+(1000*lightData[0]), ""+(1000*lightData[1]), ""+(1000*lightData[2]), string));
			Printer.getInstance().display(list);
			
			Delay.msDelay(100);
		}
	}
}