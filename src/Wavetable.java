import utils.Utils;
import java.math.*;

enum Wavetable
{
	Sine, Square, Saw, Triangle;
	
	// the greater the wavetable, the greater the precision of stored audio data
	static final int SIZE = 8192;
	
	private final float[] samples = new float[SIZE];
	
	// initialize samples array 
	static 
	{
		// fill samples array with each enum value 
		final double FUND_FREQ = 1d / (SIZE / (double)Synthesizer.AudioInfo.SAMPLE_RATE);
		for (int i = 0; i < SIZE; i++)
		{
			double t = i / (double)Synthesizer.AudioInfo.SAMPLE_RATE;
			double tDivP = t / (1d / FUND_FREQ);
			Sine.samples[i] = (float)Math.sin(Utils.Math.frequencyToAngularFrequency(FUND_FREQ) * t);
			Square.samples[i] = Math.signum(Sine.samples[i]);
			Saw.samples[i] = (float)(2d * (tDivP - Math.floor(0.5 + tDivP)));
			Triangle.samples[i] = (float)(2d * Math.abs(Saw.samples[i]) - 1d);
		}
	}
	
	float[] getSamples()
	{
		return samples;
	}
}
