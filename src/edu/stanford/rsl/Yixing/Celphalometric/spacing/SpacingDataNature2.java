package edu.stanford.rsl.Yixing.Celphalometric.spacing;

public class SpacingDataNature2 {
	
	public double[] spacingAllspacingAll = new double[]{
			62.0000,0.4102,0.4102,0.6250,0.6250
			,127.0000,0.4727,0.4727,0.6250,20.0000
     		,172.0000,0.5176,0.5176,0.6250,0.6250
			,230.0000,0.3652,0.3652,1.0000,1.0000
			,266.0000,0.4883,0.4883,1.2500,10.0000
			,310.0000,0.3574,0.3574,0.6250,0.6250
	
			
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
