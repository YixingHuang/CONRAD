package edu.stanford.rsl.truncation;

import edu.stanford.rsl.conrad.data.numeric.Grid1D;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
/**
 * reference paper "A novel reconstruction algorithm to extend the CT scan field-of-view"
 * @author Yixing Huang
 *
 */
public class WaterCylinderExtrapolation2DFan {
	int numTrunc = 150;
	public float xsinc = 0.2f; //adjust the level of extrapolation
	//zeroMask: positions of zero values, 1 stands for nonzero value, 0 stands for zero value
	private int trans = 20; //number of samples used for slope calculation
	private int[] ibL, ibR;//left and right stat points for extrapolation
	private Grid2D left_samples, right_samples; //samples for slope calculation
	private float[] slopeL, slopeR;  //slope
	private float[] xL, rL, xR, rR; //location of the water cylinder and its radius
    private float[] proL, proR; //projection magnitudes
	public float mu = 0.2f;
	private int height;
	private boolean[] isLTruncated;
	private boolean[] isRTruncated;
	
public WaterCylinderExtrapolation2DFan(int height, int numTrunc){
	this.numTrunc = numTrunc;
	this.height = height;
	this.ibL = new int[this.height];
	this.ibR = new int[this.height];
	this.left_samples = new Grid2D(this.trans, this.height);
	this.right_samples = new Grid2D(this.trans, this.height);
	this.proL = new float[this.height];
	this.proR = new float[this.height];
	this.slopeL = new float[this.height];
	this.slopeR = new float[this.height];
	this.xL = new float[this.height];
	this.xR = new float[this.height];
	this.rL = new float[this.height];
	this.rR = new float[this.height];
	this.isLTruncated = new boolean[this.height];
	this.isRTruncated = new boolean[this.height];
}


public Grid2D run2DWaterCylinderExtrapolation(Grid2D proj){
	Grid2D wceProj = new Grid2D(proj);
	this.getStartPointIntensity(proj);
	this.getLeftAndRightSamples(proj);
	this.getSlopes();
	this.getLocationsAndRadius();
	this.extrapolation(wceProj);
//	for(int i = 0; i < proj.getSize()[1]; i ++)
//	{
////		System.out.println(i + ": " + this.xL[i] + " " + this.rL[i] + " " + this.xR[i] + " " + this.rR[i]);
//		System.out.println(i + ": " + this.proR[i] + " " + this.xR[i] + " " + this.rR[i] + " " + this.slopeR[i]);
//	}
	return wceProj;
}



/**
 * 
 * @param proj
 */
private void getStartPointIntensity(Grid2D proj){
	for(int j = 0; j < proj.getSize()[1]; j++){
		this.ibL[j] = this.numTrunc;
		this.ibR[j] = proj.getSize()[0] - this.numTrunc - 1;
		this.proL[j] = proj.getAtIndex(this.ibL[j], j);
		this.proR[j] = proj.getAtIndex(this.ibR[j], j);
		if(this.proL[j] > 0)
			this.isLTruncated[j] = true;
		else
			this.isLTruncated[j] = false;
		if(this.proR[j] > 0)
			this.isRTruncated[j] = true;
		else
			this.isRTruncated[j] = false;
	}
}


/**
 * 
 * @param proj
 */
private void getLeftAndRightSamples(Grid2D proj){
	for(int j = 0; j < proj.getSize()[1]; j++){
		if(this.isLTruncated[j])
			for(int i = 0; i < this.trans; i++){
				this.left_samples.setAtIndex(i, j, proj.getAtIndex(i + this.ibL[j], j));
			}
		if(this.isRTruncated[j])
			for(int i = 0; i < this.trans; i++){
				this.right_samples.setAtIndex(i, j, proj.getAtIndex(this.ibR[j] - this.trans + i + 1, j));
			}
	}
}

/**
 * 
 * @param proj
 */
private void getSlopes(){
	for(int j = 0; j < this.height; j ++){
		if(this.isLTruncated[j])
			this.slopeL[j] = (float) this.lineFitting(this.left_samples.getSubGrid(j));
		if(this.isRTruncated[j])
			this.slopeR[j] = (float) this.lineFitting(this.right_samples.getSubGrid(j));
	}
}

/**
 * 
 */
private void getLocationsAndRadius(){
	for(int j = 0; j < this.height; j++){
		if(this.isLTruncated[j])
		{
			this.xL[j] = (float) this.getCylinderLocation(this.proL[j], this.slopeL[j]);
			this.rL[j] = (float) this.getCylinderRadius(this.proL[j], this.xL[j]);
		}
		if(this.isRTruncated[j])
		{
			this.xR[j] = (float) this.getCylinderLocation(this.proR[j], this.slopeR[j]);
			this.rR[j] = (float) this.getCylinderRadius(this.proR[j], this.xR[j]);
		}
	}
}


/**
 * calculate the position of the cylinder
 * @param p
 * @param slope
 * @return
 */
private double getCylinderLocation(float p, float slope){
	return -p * slope/(4*this.mu*this.mu);
}


/**
 * calculate the cylinder radius
 * @param p
 * @param x
 * @return
 */
private double getCylinderRadius(float p, double x){
	return Math.sqrt(p*p/(4*this.mu*this.mu) + x*x);
}


/**
 * calculate the slope based on the samples, least square line fitting
 * @param samples
 * @return
 */
private double lineFitting(Grid1D samples){
	int len = samples.getNumberOfElements();
	float xMean = (len - 1)/2;
	double mean = samples.getGridOperator().sum(samples)/len;
	Grid1D x = new Grid1D(len);
	Grid1D xx = new Grid1D(len);
	Grid1D y = new Grid1D(len);
	float val;
	for(int i = 0; i < len; i++){
		val = i - xMean;
		x.setAtIndex(i, val);
		xx.setAtIndex(i, val*val);
		y.setAtIndex(i, (float)(samples.getAtIndex(i) - mean));
	}
	y.getGridOperator().multiplyBy(y, x);
	double slope = y.getGridOperator().sum(y)/xx.getGridOperator().sum(xx);
	return slope;
}


/**
 * extrapolate the missing data
 * @param proj
 */
private void extrapolation(Grid2D proj){
	float xinc = this.xsinc, X;
	double square;
	
	//left side
	for(int j = 0; j < proj.getSize()[1]; j++){
		if(this.isLTruncated[j])
		{
			X = this.xL[j];
			for(int i = this.ibL[j]; i >= 0; i--){
				X = X + xinc;
				square = 4 * this.mu * this.mu * (rL[j]*rL[j] - X*X);
				if(square > 0)
					proj.setAtIndex(i, j, (float)(Math.sqrt(square)));
			}
		}
	}
	
	//right side
	for(int j = 0; j < proj.getSize()[1]; j++){
		if(this.isRTruncated[j])
		{
			X = this.xR[j];
			for(int i = this.ibR[j]; i < proj.getSize()[0]; i++){
				X = X + xinc;
				square = 4 * this.mu * this.mu * (rR[j]*rR[j] - X*X);
				if(square > 0)
					proj.setAtIndex(i, j, (float)(Math.sqrt(square)));
			}
		}
	}
}


}
