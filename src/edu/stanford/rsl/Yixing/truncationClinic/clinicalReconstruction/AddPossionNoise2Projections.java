package edu.stanford.rsl.Yixing.truncationClinic.clinicalReconstruction;

import java.io.File;
import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.opencl.OpenCLGrid2D;
import edu.stanford.rsl.conrad.data.numeric.opencl.OpenCLGrid3D;
import edu.stanford.rsl.conrad.filtering.PoissonNoiseFilteringTool;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import edu.stanford.rsl.tutorial.pwls.PenalizedWeightedLeastSquare;

public class AddPossionNoise2Projections {
	OpenCLGrid2D sinoCL;
	public static void main(String[] args) throws Exception{
		new ImageJ();
		
		String path = "D:\\Tasks\\FAU4\\TruncationCorrection\\clinical\\CQ500Projections\\";
		String savePath = "D:\\Tasks\\FAU4\\TruncationCorrection\\clinical\\CQ500ProjectionsNoisy\\";
		String projName, saveName;
		ImagePlus imp0;
		Grid3D sinogram, projProcessed;
		OpenCLGrid3D projCL, projProcessedCL;
		AddPossionNoise2Projections obj = new AddPossionNoise2Projections();
//		float s = 1.216f;
		File projDir;
		
		for(int i = 0; i <= 4; i++)
		{
			long t_start=System.currentTimeMillis();
			projName = path + "projection" + i + ".tif";
			projDir=new File(projName);
			if (!projDir.exists())
				continue;
			imp0 =IJ.openImage(projName);
			sinogram = ImageUtil.wrapImagePlus(imp0);
			projCL= new OpenCLGrid3D(sinogram);
//			projCL.getGridOperator().multiplyBy(projCL, s);
			sinogram = new Grid3D(projCL);
			projCL.release();
			obj.addPoissonNoise3DCL(sinogram);
			
			long t_end=System.currentTimeMillis();
			System.out.println("time is "+(t_end-t_start)/1000.0);
			
			saveName = savePath + "projection" + i + ".tif";

			imp0 = ImageUtil.wrapGrid3D(sinogram, null);
		    IJ.saveAs(imp0, "Tiff", saveName);
		    System.out.println(i);
		}
		
	    System.out.println("finished");
	}
	
	private void addPoissonNoise3D(Grid3D sinogram) throws Exception{
		for(int i = 0; i < sinogram.getSize()[2]; i++){
			addPoissonNoise(sinogram.getSubGrid(i));
		}
	}
	
	private void addPoissonNoise(Grid2D sinogram) throws Exception{
		//Grid2D noise = new Grid2D(sinogram);

		float photonNumber = 5.e5f;
		double val;
		float amp = 1.f;//transfer the intensity to linear attenuation coefficient, water 0.02/mm, pixel size 0.5mm
		sinogram.getGridOperator().divideBy(sinogram, amp);
		Grid2D I = new Grid2D(sinogram.getWidth(), sinogram.getHeight());
		for (int i = 0; i < sinogram.getWidth(); i ++)
			for(int j = 0; j < sinogram.getHeight(); j++)
			{
				val = photonNumber * Math.pow(Math.E, -sinogram.getAtIndex(i, j));
				I.setAtIndex(i, j, (float)(val));
			}
		
		PoissonNoiseFilteringTool poisson = new PoissonNoiseFilteringTool();
		poisson.applyToolToImage(I);
		for (int i = 0; i < sinogram.getWidth(); i ++)
			for(int j = 0; j < sinogram.getHeight(); j++)
			{
				val = - Math.log(I.getAtIndex(i, j)/photonNumber);
				sinogram.setAtIndex(i, j, (float)(val>=0?val:0));
			
			}
		sinogram.getGridOperator().multiplyBy(sinogram, amp);
		
		//noise.getGridOperator().subtractBy(noise, sinogram);
		//noise.getGridOperator().multiplyBy(noise, -1);
		//noise.show("poisson noise");	
	}
	
	private void addPoissonNoise3DCL(Grid3D sinogram) throws Exception{
		Grid2D sinoTemp;
		for(int i = 0; i < sinogram.getSize()[2]; i++){
			sinoTemp = addPoissonNoiseCL(sinogram.getSubGrid(i));
			sinogram.setSubGrid(i, sinoTemp);
		}
	}
	
	private Grid2D addPoissonNoiseCL(Grid2D sinogram) throws Exception{
		//Grid2D noise = new Grid2D(sinogram);

		float photonNumber = 5.e5f;
		sinoCL = new OpenCLGrid2D(sinogram);
		sinoCL.getGridOperator().multiplyBy(sinoCL, -1.0f);
		sinoCL.getGridOperator().exp(sinoCL);
		sinoCL.getGridOperator().multiplyBy(sinoCL, photonNumber);
		Grid2D I = new Grid2D(sinoCL);
		sinoCL.release();
		
		PoissonNoiseFilteringTool poisson = new PoissonNoiseFilteringTool();
		poisson.applyToolToImage(I);
		sinoCL = new OpenCLGrid2D(I);
		sinoCL.getGridOperator().divideBy(sinoCL, photonNumber);
		sinoCL.getGridOperator().log(sinoCL);
		sinoCL.getGridOperator().multiplyBy(sinoCL, -1.0f);
		sinoCL.getGridOperator().removeNegative(sinoCL);
		Grid2D sino2 = new Grid2D(sinoCL);
		sinoCL.release();
		return sino2;
		
	}

}
