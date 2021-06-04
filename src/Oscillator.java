/*
 * Oscillator control class 
 * Will allow user to choose which waveform from GUI control 
 */

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.swing.*;
import utils.Utils;

public class Oscillator extends SynthControlContainer
{
	private static final int TONE_OFFSET_LIMIT = 2000; 
	
	private final Random r = new Random();
	
	private Waveform waveform = Waveform.Sine;
	private double keyFrequency;
	private double frequency;
	private int toneOffset;
	private int wavePos;
	
	
	
	public Oscillator(Synthesizer synth) 
	{
		super(synth);
		JComboBox<Waveform> comboBox = new JComboBox<>
		(new Waveform[] {Waveform.Sine, Waveform.Square, Waveform.Saw, Waveform.Triangle, Waveform.Noise});			// Let User choose Waveform 
		comboBox.setSelectedItem(Waveform.Sine);																	// default temp
		comboBox.setBounds(10, 10, 75, 25);																			// x:10,y:10,w:75,h:25
		
		 //This listens for when items change in comboBox
		comboBox.addItemListener(l ->
		{
			// if l is changed, then state changes to the current waveform 
			if (l.getStateChange() == ItemEvent.SELECTED)
			{
				waveform = (Waveform)l.getItem();																
				//System.out.println(waveform);
			}
		});
		
		add(comboBox);
		JLabel toneParameter = new JLabel("x.0.00");	
		toneParameter.setBounds(165, 65, 50, 25);							//x:165, y:65, w:50, h:25
		setSize( 279, 100);													//width: 279, Height: 100
		toneParameter.setBorder(Utils.WindowDesgin.LINE_BORDER);			// reference Util Class
		toneParameter.addMouseListener(new MouseAdapter()
		{
			@Override 
			public void mousePressed(MouseEvent e)
			{
				final Cursor BLANK_CURSOR = Toolkit.getDefaultToolkit().createCustomCursor(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
						, new Point(0, 0), "blank_cursor");
				setCursor(BLANK_CURSOR);
				mouseClickLocation = e.getLocationOnScreen();				// field in SynthControlContainer
			}
			@Override
			public void mouseReleased(MouseEvent e)
			{
				setCursor(Cursor.getDefaultCursor());
			}
		});
		toneParameter.addMouseMotionListener(new MouseAdapter()
		{
			@Override 
			public void mouseDragged(MouseEvent e)
			{
				if (mouseClickLocation.y != e.getYOnScreen());
				{
					boolean mouseMovingUp = mouseClickLocation.y - e.getYOnScreen() > 0;
					if (mouseMovingUp && toneOffset < TONE_OFFSET_LIMIT)
					{
						++toneOffset;
					}
					else if (!mouseMovingUp && toneOffset > -TONE_OFFSET_LIMIT)
					{
						--toneOffset;
					}
					
					applyToneOffset(); 
					toneParameter.setText("x" + String.format("%.3f", getToneOffset()));						// always will print three decimal places 
				}
				Utils.ParameterHandling.PARAMETER_ROBOT.mouseMove(mouseClickLocation.x, mouseClickLocation.y);	// reset mouse location // Robot in Utils 
			}
		});
		add(toneParameter);
		JLabel toneText = new JLabel ("Tone");
		toneText.setBounds(172, 40, 75, 25);								// x:172, y:40, w:75, h:25
		add(toneText);
		
		setBorder(Utils.WindowDesgin.LINE_BORDER);							//From Util Class 
		setLayout(null);													// layout manager 
	}
	
	
	//specify which waveform user wants to use 
	private enum Waveform
	{
		Sine, Square, Saw, Triangle, Noise
	}
	
	public double getKeyFrequency()
	{
		return frequency;
	}
	
	public void setKeyFrequency(double frequency)
	{
		//set key frequency 
		keyFrequency = this.frequency = frequency;
		// apply a tone offset 
		applyToneOffset();
		
	}

	// get tone offset in decimal 
	private double getToneOffset()
	{
		return toneOffset / 1000d;
	}
	
	// returns sample based on selected waveform 
	public double nextSample()
	{
		
		double tDivP = (wavePos++ / (double)Synthesizer.AudioInfo.SAMPLE_RATE) / (1d / frequency);				// (time / time period)
		switch (waveform)
		{
			case Sine:
				return Math.sin(Utils.Math.frequencyToAngularFrequency(frequency) * (wavePos - 1) / Synthesizer.AudioInfo.SAMPLE_RATE);
			case Square:
				return Math.signum(Math.sin(Utils.Math.frequencyToAngularFrequency(frequency) * (wavePos - 1) / Synthesizer.AudioInfo.SAMPLE_RATE));		// 1 or 0 || 1 or -1
			case Saw:
				return 2d * (tDivP - Math.floor(0.5 + tDivP));
			case Triangle:
				return 2d * Math.abs(2d * (tDivP - Math.floor(0.5 + tDivP))) - 1;
			case Noise:
				return r.nextDouble();
				default:
					throw new RuntimeException("Oscillator sets to unknown wavefomr");
		}
	}
	
	// Apply tone offset method 
	private void applyToneOffset()
	{
		frequency = keyFrequency * Math.pow(2,  getToneOffset());
		
	}
	
	
}
