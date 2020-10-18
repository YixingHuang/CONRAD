package edu.stanford.rsl.Yixing.ERC;

import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.opencl.OpenCLGrid3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import edu.stanford.rsl.tutorial.pwls.PenalizedWeightedLeastSquare;

public class PreprocessProjectionsUsingPWLS {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		
		String path = "E:\\Lasse\\BVM\\";
		String projName, saveName;
		ImagePlus imp0;
		Grid3D proj0, projProcessed;
		OpenCLGrid3D projCL, projProcessedCL;
		PenalizedWeightedLeastSquare pwls= new PenalizedWeightedLeastSquare(0.5f, 8);
		for(int i = 8; i <= 8; i++)
		{
			projName = path + "projectionNoisy" + i + ".tif";
			imp0 =IJ.openImage(projName);
			proj0 = ImageUtil.wrapImagePlus(imp0);
//			proj0.show("projections");
			
			projCL = new OpenCLGrid3D(proj0);
			projProcessed = new Grid3D(proj0.getSize()[0], proj0.getSize()[1], proj0.getSize()[2]);
			projProcessedCL = new OpenCLGrid3D(projProcessed);
			projCL.getGridOperator().divideBy(projCL, 1000.0f);
			pwls.excute3D(projProcessedCL, projCL);
			projProcessedCL.getDelegate().notifyDeviceChange();
			projProcessedCL.getGridOperator().multiplyBy(projProcessedCL, 1000.0f);
			    
			saveName = path + "projectionPwls" + i + ".tif";

			projProcessed = new Grid3D(projProcessedCL);
			imp0 = ImageUtil.wrapGrid3D(projProcessed, null);
		    IJ.saveAs(imp0, "Tiff", saveName);
		    projCL.release();
		    projProcessedCL.release();
		    System.out.println(i);
		}
		
	    System.out.println("finished");
	}

}
