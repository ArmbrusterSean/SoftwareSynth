/*
 *  This class interfaces with the openAL Libraries 
 *  Run-down: this class will use samples that the synthesizer will generate in small buffers ~ 512 samples long.
 *  Each sample will be stored in a queue to a source that will be sent to the device.
 *  As the samples plays, the buffers will push and pop in accordance to trigger length
 *  this allows for a constant stream of real-time audio 
 *  
 */

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;

import utils.Utils;

// static for reference of library AL10 & ALC10 FOR THIS CLASS ONLY 
import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;

import java.util.function.Supplier;

class AudioThread extends Thread
{
	 // These are all native types because openAL is written in c/c++ with pointers.
	static final int BUFFER_SIZE = 512;
	static final int BUFFER_COUNT = 8;		// how many buffers in queue
	
	private final Supplier<short[]> bufferSupplier;
	private final int[] buffers = new int[BUFFER_COUNT];
	private final long device = alcOpenDevice(alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER));		// open device
	private final long context = alcCreateContext(device, new int[1]);								// 0 attribute list 
	private final int source;
	
	private int bufferIndex;
	private boolean closed;
	private boolean running;
	
	AudioThread(Supplier<short[]> bufferSupplier) 
	{
		this.bufferSupplier = bufferSupplier;
		// these functions call the sources to see if runnable 
		alcMakeContextCurrent(context);								// make current context 
		AL.createCapabilities(ALC.createCapabilities(device));		// what device is currently capable of 
		source = alGenSources();									// generate pointer to source to buffer data for play-back
		
		for (int i = 0; i < BUFFER_COUNT; i++)
		{
			bufferSamples(new short[0]); 
		}
		alSourcePlay(source);
		
		catchInternalException();
		start();
	}
	
	boolean isRunning()
	{
		return running;
	}
	
	@Override
	public synchronized void run()
	{
		while (!closed)
		{
			while(!running)
			{
				// exception from Util Package
				Utils.handleProcedure(this::wait, false);
			}
			
			int processedBuffs = alGetSourcei(source, AL_BUFFERS_PROCESSED);
			
			for (int i = 0; i < processedBuffs; i++)
			{
				short[] samples = bufferSupplier.get();
				if (samples == null)
				{
					running = false;
					break;
				}
		
				alDeleteBuffers(alSourceUnqueueBuffers(source));
				buffers[bufferIndex] = alGenBuffers();
				bufferSamples(samples);
			}
			
			if (alGetSourcei(source, AL_SOURCE_STATE) != AL_PLAYING)
			{
				alSourcePlay(source);
			}
			catchInternalException();
		}
		//clean up garbage since OpenAL is written in c/c++ 
		alDeleteSources(source);
		alDeleteBuffers(buffers);
		alcDestroyContext(context);
		alcCloseDevice(device);
	}
	
	// called by synth when key is pressed - notify thread 
	synchronized void triggerPlayback()
	{
		running = true;
		notify();
	}
	
	// close the thread 
	void close() 
	{
		closed = true;
		triggerPlayback(); 			 // break out of loop
	}
	
	// increase the bufferIndex until threshold, restart the queue
	private void bufferSamples(short[] samples)
	{
		int buf = buffers[bufferIndex++];
		alBufferData(buf, AL_FORMAT_MONO16, samples, Synthesizer.AudioInfo.SAMPLE_RATE);	// buffer the sample in mono with the 44100 frequency 
		alSourceQueueBuffers(source, buf);													// queue buffer to source 
		bufferIndex %= BUFFER_COUNT;														// reset the buffer index to 0 if index = 0
	}
	
	// reference the Util Package 
	private void catchInternalException()
	{
		int err = alcGetError(device);
		if (err != ALC_NO_ERROR)
		{
			throw new OpenALException(err);
		}
	}
	
}
