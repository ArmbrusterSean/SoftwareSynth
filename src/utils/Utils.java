/**
 *  Utility Class 
 */
package utils;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import static java.lang.Math.*;


public class Utils
{
	public static void handleProcedure(Procedure procedure, boolean printStackTrace)
	{
		try
		{
			procedure.invoke();
		}
		catch (Exception e)
		{
			if (printStackTrace)
			{
				e.printStackTrace();
			}
		}
	}
	
	// move mouse back to original click position 
	public static class ParameterHandling 
	{
		public static final Robot PARAMETER_ROBOT;
		
		static 
		{
			try
			{
				PARAMETER_ROBOT = new  Robot();
			}
			catch (AWTException e)
			{
				throw new ExceptionInInitializerError("Cannont construct robot instance");
			}
		}
	}
	
	// for GUI in Oscillator Class 
	public static class WindowDesgin
	{
		public static final Border LINE_BORDER = BorderFactory.createLineBorder(Color.BLACK);
	}
	
	// convert wave frequency to angular frequency
	public static class Math 
	{
		public static double frequencyToAngularFrequency(double freq)
		{
			return 2 * PI * freq;
		}
		
		// get frequency in relation to key 
		public static double getKeyFrequency(int keyNum)
		{
			return pow(root(2, 12), keyNum - 49) * 440;
		}
		
		// create a root functino since java does not supply one in static libraries 
		public static double root(double num, double root)
		{
			return pow(E, log(num) / root);
		}
	}
}
