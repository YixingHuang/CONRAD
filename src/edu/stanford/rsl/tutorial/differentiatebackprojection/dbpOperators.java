package edu.stanford.rsl.tutorial.differentiatebackprojection;

import edu.stanford.rsl.conrad.data.numeric.Grid1D;
import edu.stanford.rsl.conrad.data.numeric.Grid1DComplex;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;

public class dbpOperators {
	private float R = 0;
	
	public dbpOperators(int imgSizeX, int imgSizeY, double spacingX, double spacingY)
	{
		R = (float) (imgSizeX * spacingX /2.0f);
	}
	/**
	 * not correct. Has not dealt with singularity
	 * @param signal
	 * @return
	 */
	public Grid1D inverseHilbertTransform(Grid1D signal)
	{
		float R2 = R * R;
		int len = signal.getNumberOfElements();
		Grid1D signal2 = new Grid1D(len);
		double ds = signal.getSpacing()[0];
		double s, s2;
		double w, w2 = 0;
		float val = 0;
		double C = 0; //constant value C, which is determined by prior
		double sum = 0;
		for(int i = 0; i < len; i++ )
		{
			sum = 0;
			s = ((i + 0.5) - len/2.0) * ds;
			w = -1 / Math.sqrt(R2 - s * s);
			for(int j = 0; j < len; j++)
			{
				s2 = ((j + 0.5) - len/2.0) * ds;
				w2 = Math.sqrt(R2 - s2 * s2);
				sum += signal.getAtIndex(j) * w2 / (Math.PI * (s - s2));
			}
			sum = sum * ds;
			if(i == 0)
				C = - sum;
			sum += C;
			val = (float) (sum * w);
			signal2.setAtIndex(i, val);
		}
		return signal2;
	}
	
	public Grid1D HilbertTransform(Grid1D signal)
	{
		int N = signal.getNumberOfElements();
		Grid1DComplex coeFourier = new Grid1DComplex (signal, false);
		coeFourier.transformForward();
		
		
		// Copy the input to a complex array which can be processed
        //  in the complex domain by the FFT
		for (int i = 1; i < (N/2); i++)
        {
			coeFourier.setRealAtIndex(i, coeFourier.getRealAtIndex(i) * 2);
			coeFourier.setImagAtIndex(i, coeFourier.getImagAtIndex(i) * 2);
        }

        // zero out negative frequencies
        //  (leaving out the dc component)
        for (int i = (N/2)+1; i < N; i++)
        {
			coeFourier.setRealAtIndex(i, 0);
			coeFourier.setImagAtIndex(i, 0);
        }
		
		coeFourier.transformInverse();
		Grid1D signal2 = coeFourier.getRealSubGrid(0, N);
		return signal2;
	}
	
	public Grid1D WeightedHilbertTransform(Grid1D signal)
	{
		int N = signal.getNumberOfElements();
//		signal.clone().show("signal");
		double s = 0;
		double ds = signal.getSpacing()[0];
		double R2 = R * R;
		double w;

		for(int i = 0; i < N; i++ )
		{
			s = ((i + 0.5) - N/2.0) * ds;
			w = Math.sqrt(R2 - s * s);
			signal.setAtIndex(i, (float) (signal.getAtIndex(i) * w));

		}

		
		Grid1DComplex coeFourier = new Grid1DComplex (signal, false);
		coeFourier.transformForward();
		N = coeFourier.getSize()[0];
		
		// Copy the input to a complex array which can be processed
        //  in the complex domain by the FFT
		for (int i = 1; i < (N/2); i++)
        {
			coeFourier.setRealAtIndex(i, coeFourier.getRealAtIndex(i) * 2);
			coeFourier.setImagAtIndex(i, coeFourier.getImagAtIndex(i) * 2);
        }

        // zero out negative frequencies
        //  (leaving out the dc component)
        for (int i = (N/2)+1; i < N; i++)
        {
			coeFourier.setRealAtIndex(i, 0);
			coeFourier.setImagAtIndex(i, 0);
        }
		
		coeFourier.transformInverse();
		Grid1D signal2 = coeFourier.getImagSubGrid(0, N);//get the imaginary part
		
		float C;
		int tag = 1;
		if(tag == 0)
			C =  -signal2.getAtIndex(0); //compute C with the known prior value 0
		else
		{
			float sum = 0;
			float sumw = 0;
			for(int i = 0; i < 40; i++) {
				s = ((i + 0.5) - N/2.0) * ds;
				w = -1/(Math.sqrt(R2 - s * s) * Math.PI);
				sum += signal2.getAtIndex(i) * w;
				sumw += w;
			}
			C = - sum / sumw;
		}
		for(int i = 0; i < N; i++ )
		{
			s = ((i + 0.5) - N/2.0) * ds;
			w = -1/(Math.sqrt(R2 - s * s) * Math.PI);
			signal2.setAtIndex(i, (float) ((signal2.getAtIndex(i) + C)* w));
		}

		return signal2;
	}
	
	public void weightedHilbertTranform2DVertical(Grid2D img)
	{
		Grid1D signal = new Grid1D(img.getSize()[1]);
		Grid1D signal2;
		for(int x = 0; x < img.getSize()[0]; x++)
		{
			for(int y = 0; y < img.getSize()[1]; y++)
			{
				signal.setAtIndex(y, img.getAtIndex(x, y));
			}
			signal2 = WeightedHilbertTransform(signal);
			for(int y = 0; y < img.getSize()[1]; y++)
			{
				img.setAtIndex(x,y, signal2.getAtIndex(y));
			}
		}
	}
	
	public Grid2D differentiatedProjection2D(Grid2D proj) {
		Grid2D proj2 = new Grid2D(proj);
		for(int i = 0; i < proj.getSize()[1]; i++)
		{
			proj2.setSubGrid(i, differentiatedProjection1D(proj2.getSubGrid(i)));
		}
		return proj2;
	}
	
	
	public Grid1D differentiatedProjection1D(Grid1D proj) {
		Grid1D dproj = new Grid1D(proj.getNumberOfElements());
		for(int i = 0; i < proj.getNumberOfElements()-1; i++)
		{
			dproj.setAtIndex(i, proj.getAtIndex(i + 1) - proj.getAtIndex(i));
		}
		return dproj;
	}

}
