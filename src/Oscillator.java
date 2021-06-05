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
import javax.swing.*;
import utils.Utils;

public class Oscillator extends SynthControlContainer
{
	private static final int TONE_OFFSET_LIMIT = 2000; 
	
	private Wavetable wavetable = Wavetable.Sine;
	private double keyFrequency;
	private int wavetableStepSize;
	private int wavetableIndex;
	private int toneOffset;
	
	public Oscillator(Synthesizer synth) 
	{
		super(synth);
		JComboBox<Wavetable> comboBox = new JComboBox<>(Wavetable.values());			// Let User choose Waveform 
		comboBox.setSelectedItem(Wavetable.Sine);										// default temp
		comboBox.setBounds(10, 10, 75, 25);												// x:10,y:10,w:75,h:25
		
		 //This listens for when items change in comboBox
		comboBox.addItemListener(l ->
		{
			// if l is changed, then state changes to the current waveform 
			if (l.getStateChange() == ItemEvent.SELECTED)
			{
				wavetable = (Wavetable)l.getItem();																
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
	
	public void setKeyFrequency(double frequency)
	{
		keyFrequency = frequency;
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
		double sample = wavetable.getSamples()[wavetableIndex];
		wavetableIndex = (wavetableIndex + wavetableStepSize) % Wavetable.SIZE;
		return sample;
	}
		
	
	// Apply tone offset method 
	private void applyToneOffset()
	{
		wavetableStepSize = (int)(Wavetable.SIZE * (keyFrequency * Math.pow(2, getToneOffset())) / Synthesizer.AudioInfo.SAMPLE_RATE);
	}
	
	
}
