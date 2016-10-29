package dpm.util;

import java.util.ArrayList;
import java.util.List;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;

/**
 * This class offers access to the Mindstorm Brick's screen across all of the code
 * in this project. It includes a few utility methods such that different method calls
 * can print text on different parts of the screen.
 * 
 * @author Samuel Genois
 */
public class Printer {
	
	public static final TextLCD T = LocalEV3.get().getTextLCD();
	
	/**
	 * Prints a list of Strings on consecutive lines on the screen.
	 * 
	 * @param strs			The Strings to be printed
	 * @param startLine		The line at which the printing begins
	 * @param clearFirst	If true, clears all text from the screen before printing
	 */
	public static void display(List<String> strs, int startLine, boolean clearFirst){
		if(clearFirst)
			T.clear();
		for(int i = 0; i<strs.size(); i++){
			T.clear(startLine+i);
			T.drawString(strs.get(i), 0, i + startLine);
		}
	}
	
	/**
	 * Prints a list of Strings on consecutive lines on the screen. All other text is
	 * erased prior to printing. Printing starts at the first line at the top of the screen.
	 * 
	 * @param strs			The Strings to be printed
	 */
	public static void display(List<String> strs){
		display(strs, 0, true);
	}
	
	/**
	 * Prints a String on a line on the screen.
	 * 
	 * @param strs			The Strings to be printed
	 * @param startLine		The line where the String is to be printed
	 * @param clearFirst	If true, clears all text from the screen before printing
	 */
	public static void display(String str, int startLine, boolean clearFirst){
		List<String> strs = new ArrayList<String>();
		strs.add(str);
		display(strs, startLine, clearFirst);
	}
	/**
	 * Prints a String on a line on the screen. All other text is
	 * erased prior to printing. Prints at the first line at the top of the screen.
	 * 
	 * @param strs			The Strings to be printed
	 */
	public static void display(String str){
		display(str, 0, true);
	}
	
	public static TextLCD getTextLCD(){
		return T;
	}
}
