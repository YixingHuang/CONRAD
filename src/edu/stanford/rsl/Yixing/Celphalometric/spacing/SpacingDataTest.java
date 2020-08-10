package edu.stanford.rsl.Yixing.Celphalometric.spacing;

public class SpacingDataTest {
	
	public double[] spacingAllspacingAll = new double[]{
			0, 0.4883,0.4883,0.6250, 1,
			1, 0.5469, 0.5469,0.6250, 1,
			2, 0.4883, 0.4883, 0.6250, 1,
			3, 0.4707, 0.4707, 0.6250, 1,
			4, 0.4980, 0.4980, 0.6250, 1 
			
};
	
	public double getAt(int idx, int j)
	{
		return this.spacingAllspacingAll[idx * 5 + j];
	}
	
	public int getNumberOfCases()
	{
		return this.spacingAllspacingAll.length/5;
	}

}
