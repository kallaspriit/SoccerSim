package ee.ut.robotex.util;

public class FpsCounter
{
	private double startTime = 0.0;
	private double endTime = 0.0;
	private int countedFrames = 0;
	private int lastFps = 0;
	private int bestFps = 0;
	private int worstFps = -1;
	private int targetFps = 60;
	private int calculationFrequency = 50;
	
	public FpsCounter()
	{
		startTime = System.currentTimeMillis();
		
		lastFps = targetFps;
	}
	
	public FpsCounter(int frequency, int prefferedFps)
	{
		startTime = System.currentTimeMillis();
		targetFps = prefferedFps;
		lastFps = targetFps;
		
		calculationFrequency = frequency;
	}
	
	public void setCalculationFrequency(int frequency)
	{
		calculationFrequency = frequency;
	}
	
	public void setTargetFps(int prefferredFps)
	{
		targetFps = prefferredFps;
	}
	
	public void registerFrame()
	{
		countedFrames++;
		
		if(countedFrames == calculationFrequency)
		{
			endTime = System.currentTimeMillis();
			
			double deltaTime = endTime - startTime;
			
			int currentFps = (int)Math.round(countedFrames / (deltaTime / 1000.0));
			
			if(Math.abs(currentFps - lastFps) > 1)
			{
				lastFps = currentFps;
			}
			
			if(lastFps > bestFps)
			{
				bestFps = lastFps;
			}
			
			if(lastFps < worstFps || worstFps == -1)
			{
				worstFps = lastFps;
			}
			
			startTime = System.currentTimeMillis();
			countedFrames = 0;
		}
	}
	
	public void reset()
	{
		startTime = System.currentTimeMillis();
		endTime = 0.0;
		countedFrames = 0;
		lastFps = targetFps;
		bestFps = 0;
		worstFps = -1;
	}
	
	public int getFps()
	{
		return lastFps;
	}
	
	public int getBestFps()
	{
		return bestFps;
	}
	
	public int getWorstFps()
	{
		return worstFps;
	}
}