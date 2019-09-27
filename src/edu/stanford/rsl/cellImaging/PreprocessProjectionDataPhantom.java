package edu.stanford.rsl.cellImaging;

import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.opencl.OpenCLGrid3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import edu.stanford.rsl.tutorial.pwls.PenalizedWeightedLeastSquare;

public class PreprocessProjectionDataPhantom {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		PreprocessProjectionDataPhantom obj = new PreprocessProjectionDataPhantom();
		String sinoPath =  "D:\\Tasks\\FAU4\\CellImaging\\AlgaeTestPhantomNoise10e5\\sinograms\\";
		int numDet = 512;
		int numDeg = 180;
		int numHeight = 512;
		Grid3D sino3D = new Grid3D(numDet, numDeg, numHeight);
		ImagePlus imp;
		String subsinoPath;
		Grid2D sinogram;
		for(int i = 0; i < numHeight; i++)
		{
			subsinoPath = sinoPath + i + ".tif";
			imp = IJ.openImage(subsinoPath);
			sinogram = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
			sino3D.setSubGrid(i, (Grid2D)sinogram.clone());
		}
		
		Grid3D proj0 = obj.inverseReorderProjections(sino3D);
		
		
		proj0.clone().show("proj0");
		OpenCLGrid3D projCL = new OpenCLGrid3D(proj0);
		projCL.getGridOperator().divideBy(projCL, 50.0f);
		Grid3D projProcessed = new Grid3D(proj0.getSize()[0], proj0.getSize()[1], proj0.getSize()[2]);
		OpenCLGrid3D projProcessedCL = new OpenCLGrid3D(projProcessed);
		PenalizedWeightedLeastSquare pwls= new PenalizedWeightedLeastSquare(0.1f, 2);
		pwls.excute3D(projProcessedCL, projCL);
		projProcessedCL.getDelegate().notifyDeviceChange();
		projProcessedCL.getGridOperator().multiplyBy(projProcessedCL, 50.0f);
		
		String path3 = "D:\\Tasks\\FAU4\\CellImaging\\FOVRecon\\AlgaeTestPhantomNoise10e4\\sinogramProcessed\\projectionPwls2.tif";
		projProcessed = new Grid3D(projProcessedCL);
		ImagePlus imp0 = ImageUtil.wrapGrid3D(projProcessed, null);
	    IJ.saveAs(imp0, "Tiff", path3);
	    
	    Grid3D sino3D2 = obj.reorderProjections(projProcessed);
	    String path4 = "D:\\Tasks\\FAU4\\CellImaging\\FOVRecon\\AlgaeTestPhantomNoise10e4\\sinogramProcessed\\sinogramsPwls2.tif";
	    imp0 = ImageUtil.wrapGrid3D(sino3D2, null);
	    IJ.saveAs(imp0, "Tiff", path4);
	    System.out.println("finished");
	}
	
	private Grid3D reorderProjections(Grid3D proj){
		
		Grid3D sino = new Grid3D(proj.getSize()[0], proj.getSize()[2], proj.getSize()[1]);
		for(int h = 0; h < proj.getSize()[1]; h++){
			for(int s = 0; s < proj.getSize()[0]; s++) {
				for(int theta  = 0; theta < proj.getSize()[2]; theta++) {
					sino.setAtIndex(s, theta, h, proj.getAtIndex(s, h, theta));
				}
			}
		}
		
		return sino;	
	}
	
	private Grid3D inverseReorderProjections(Grid3D proj){
		
		Grid3D sino = new Grid3D(proj.getSize()[0], proj.getSize()[2], proj.getSize()[1]);
		for(int h = 0; h < proj.getSize()[2]; h++){
			for(int s = 0; s < proj.getSize()[0]; s++) {
				for(int theta  = 0; theta < proj.getSize()[1]; theta++) {
					sino.setAtIndex(s, h, theta, proj.getAtIndex(s, theta, h));
				}
			}
		}
		
		return sino;
			
	}

}
