/**
 *  Utility Class 
 */

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

import javax.swing.*;
import utils.Utils;						// Utilities Class 


public class Synthesizer
{
	
	
	private static final HashMap<Character, Double> KEY_FREQUENCIES = new HashMap<>();		//Map key to frequency
	
	private boolean shouldGenerate;

	private final Oscillator[] oscillators = new Oscillator[3];		// 3 oscillators 
	private final JFrame frame = new JFrame ("Synthesizer");
	private final AudioThread audioThread = new AudioThread(() ->
	{
		if(!shouldGenerate)
		{
			return null;
		}
		short[] s = new short[AudioThread.BUFFER_SIZE];
		for(int i = 0; i < AudioThread.BUFFER_SIZE; i++)
		{
			double d = 0;
			for (Oscillator o : oscillators)
			{
				d += o.nextSample() / oscillators.length;  			// allows mixing of oscillators 
			}
			s[i] = (short)(Short.MAX_VALUE * d);					// amplify 
		}
		return s;
		
	});
	
	private final KeyAdapter keyAdapter = new KeyAdapter()
	{
		@Override
		public void keyPressed(KeyEvent e)
		{
			if (!audioThread.isRunning())
			{
				// set frequency of o based on the keyEvent
				for (Oscillator o : oscillators)
				{
					o.setKeyFrequency(KEY_FREQUENCIES.get(e.getKeyChar()));
				}
				
				shouldGenerate = true;
				audioThread.triggerPlayback();
			}
		}
		@Override
		public void keyReleased(KeyEvent e)
		{
			shouldGenerate = false;
		}
	};
	
	//static constructor for KEY_FREQUENCIES 
	static 
	{
		final int STARTING_KEY = 16;											//starting key c2 (lowest frequency)
		final int KEY_FREQUENCY_INCREMENT = 2;									// how many keys to increase by
		final char[] KEYS = "zxcvbnm,./asdfghjkl;'qwertyuiop[]".toCharArray();	
		
		// map keys to specific frequencies 
		for (int i = STARTING_KEY, key = 0; i < KEYS.length * KEY_FREQUENCY_INCREMENT + STARTING_KEY; i += KEY_FREQUENCY_INCREMENT, ++key)
		{
			// reference Utils class 
			KEY_FREQUENCIES.put(KEYS[key], Utils.Math.getKeyFrequency(i));
		}
		
		/** TEST call every key value from HashMap
		for (Double d : KEY_FREQUENCIES.values())
		{
			//print all mapped frequencies
			System.out.println(d);
		}**/
	}
	
	Synthesizer() 
	{
		//Oscillator Properties
		int y = 0;
		for (int i = 0; i < oscillators.length; i++)
		{
			oscillators[i] = new Oscillator(this);
			oscillators[i].setLocation(5, y);
			frame.add(oscillators[i]);
			y += 105;
		}
		
		frame.addKeyListener(keyAdapter);		
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				audioThread.close();
			}
		});
		
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setSize(613,357);
		frame.setResizable(false);
		frame.setLayout(null);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	public KeyAdapter getKeyAdapter()
	{
		return keyAdapter;
	}
	
	// This class holds the info pertaining to the sample rate
	public static class AudioInfo
	{
		public static final int SAMPLE_RATE = 44100;
	}
}
