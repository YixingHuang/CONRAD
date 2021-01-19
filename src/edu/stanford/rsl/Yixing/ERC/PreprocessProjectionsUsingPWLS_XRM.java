package edu.stanford.rsl.Yixing.ERC;

import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.opencl.OpenCLGrid3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import edu.stanford.rsl.tutorial.pwls.PenalizedWeightedLeastSquare;
import java.util.Arrays;

public class PreprocessProjectionsUsingPWLS_XRM {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		PreprocessProjectionsUsingPWLS_XRM obj = new PreprocessProjectionsUsingPWLS_XRM();
		String path = "C:\\Users\\Yixing Huang\\Downloads\\In-Vivo-Distance-Check\\AgingKinetcis_6519-2\\AgingKinetcis_6519-2_2021-01-05_141550\\6519(2)_4530nm_30kV_2W_LE4_Bin4_2s\\";
		String projName, saveName;
		ImagePlus imp0;
		Grid3D proj0, projProcessed, projProcessed2;
		OpenCLGrid3D projCL, projProcessedCL;
		int iters = 10;
		PenalizedWeightedLeastSquare pwls= new PenalizedWeightedLeastSquare(0.5f, iters);
		int I0 = 1000;
		for(int i = 1; i <= 1; i++)
		{
			projName = path + "projection.tif";
			imp0 =IJ.openImage(projName);
			proj0 = ImageUtil.wrapImagePlus(imp0);
//			proj0.show("projections");
			
			projCL = new OpenCLGrid3D(proj0);
			projProcessed = new Grid3D(proj0.getSize()[0], proj0.getSize()[1], proj0.getSize()[2]);
			projProcessedCL = new OpenCLGrid3D(projProcessed);
			projCL.getGridOperator().divideBy(projCL, I0);
			projCL.getGridOperator().log(projCL);
			projCL.getGridOperator().multiplyBy(projCL, -1);
			projCL.getGridOperator().removeNegative(projCL);
			pwls.excute3D(projProcessedCL, projCL);
			projProcessedCL.getDelegate().notifyDeviceChange();
			projProcessedCL.getGridOperator().divideBy(projProcessedCL, -1);
			projProcessedCL.getGridOperator().exp(projProcessedCL);
			projProcessedCL.getGridOperator().multiplyBy(projProcessedCL, I0);
			    
			saveName = path + "projectionPwlsIters" + iters + "I0" + I0 + ".tif";

			projProcessed = new Grid3D(projProcessedCL);
			projProcessed2 = obj.medianFilter(projProcessed);
			imp0 = ImageUtil.wrapGrid3D(projProcessed, null);
		    IJ.saveAs(imp0, "Tiff", saveName);
		    projCL.release();
		    projProcessedCL.release();
		    System.out.println(i);
		}
		
	    System.out.println("finished");
	}
	
	private Grid3D medianFilter(Grid3D img)
	{
		Grid3D img2 = new Grid3D(img);
		for(int k = 0; k < img.getSize()[2]; k++)
			img.setSubGrid(k, medianFilter2D(img.getSubGrid(k)));
		return img2;
	}

	
	private Grid2D medianFilter2D(Grid2D img)
	{
		Grid2D img2 = new Grid2D(img);
		float[] pixel = new float[9];
		for(int i=1; i<img.getWidth()-1; i++)
            for(int j=1; j<img.getHeight()-1; j++)
            {
               pixel[0]=img.getAtIndex(i-1,j-1);
               pixel[1]=img.getAtIndex(i-1,j);
               pixel[2]=img.getAtIndex(i-1,j+1);
               pixel[3]=img.getAtIndex(i,j+1);
               pixel[4]=img.getAtIndex(i+1,j+1);
               pixel[5]=img.getAtIndex(i+1,j);
               pixel[6]=img.getAtIndex(i+1,j-1);
               pixel[7]=img.getAtIndex(i,j-1);
               pixel[8]=img.getAtIndex(i,j);
     
               Arrays.sort(pixel);

               img2.setAtIndex(i, j, pixel[4]);
            }
		
		return img2;
	}
}
