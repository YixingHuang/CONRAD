package edu.stanford.rsl.Yixing.Siemens;

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
		
		String path =  "E:\\SiemensMarkerData\\Water\\projectionPadded180.tif";
		ImagePlus imp0 =IJ.openImage(path);
		Grid3D proj0 = ImageUtil.wrapImagePlus(imp0);
		proj0.show("projections");
		
		OpenCLGrid3D projCL = new OpenCLGrid3D(proj0);
		Grid3D projProcessed = new Grid3D(proj0.getSize()[0], proj0.getSize()[1], proj0.getSize()[2]);
		OpenCLGrid3D projProcessedCL = new OpenCLGrid3D(projProcessed);
		PenalizedWeightedLeastSquare pwls= new PenalizedWeightedLeastSquare(0.5f, 5);
		pwls.excute3D(projProcessedCL, projCL);
		projProcessedCL.getDelegate().notifyDeviceChange();
		    
		String path3 = "E:\\SiemensMarkerData\\Water\\projectionPadded180PWLS5.tif";

		projProcessed = new Grid3D(projProcessedCL);
		imp0 = ImageUtil.wrapGrid3D(projProcessed, null);
	    IJ.saveAs(imp0, "Tiff", path3);
	    System.out.println("finished");
	}

}
