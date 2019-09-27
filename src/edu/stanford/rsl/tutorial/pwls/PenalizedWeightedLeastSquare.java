package edu.stanford.rsl.tutorial.pwls;



import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.opencl.OpenCLGrid2D;
import edu.stanford.rsl.conrad.data.numeric.opencl.OpenCLGrid3D;
import edu.stanford.rsl.tutorial.weightedtv.TVOpenCLGridOperators;

public class PenalizedWeightedLeastSquare {
	private float sigma = 1.0f;
	private int iterNum = 10;
	private TVOpenCLGridOperators op;
	public PenalizedWeightedLeastSquare(float sigma, int iterNum) {
		this.sigma = sigma;

		this.iterNum = iterNum;
		op = TVOpenCLGridOperators.getInstance();
	}

	public void excute3D(OpenCLGrid3D projProcessedCL, OpenCLGrid3D projCL) {
		for(int i  = 0; i < this.iterNum; i ++)
		{
			op.penalizedWeightedLeastSquare(projProcessedCL, projCL, sigma);
			projProcessedCL.getDelegate().notifyDeviceChange();
			projCL.release();
			projCL = new OpenCLGrid3D(projProcessedCL);
		}
			
	}
	
	public void excute2D(OpenCLGrid2D projProcessedCL, OpenCLGrid2D projCL) {
		
	}
	
	

}
