package edu.stanford.rsl.Yixing.Celphalometric;

public class SpacingData3 {
	
	public double[] spacingAllspacingAll = new double[]{
			306.0000,0.4883,0.4883,0.5000,1.0000
			,322.0000,0.5605,0.5605,0.5000,1.0000
			,339.0000,0.4297,0.4297,1.0000,1.0000
			,354.0000,0.4160,0.4160,0.7500,1.5000
			,366.0000,0.5801,0.5801,0.5000,1.0000
			,374.0000,0.5527,0.5527,0.5000,1.0000
			,408.0000,0.4570,0.4570,0.5000,1.0000
			,424.0000,0.4414,0.4414,0.7500,1.5000
			,445.0000,0.5820,0.5820,0.5000,1.0000
			,448.0000,0.4102,0.4102,0.7500,1.5000
			,484.0000,0.6328,0.6328,0.5000,1.0000
			
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
