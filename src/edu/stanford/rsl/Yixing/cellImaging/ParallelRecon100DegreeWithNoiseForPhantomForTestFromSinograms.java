package edu.stanford.rsl.Yixing.cellImaging;

import java.io.File;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.filtering.PoissonNoiseFilteringTool;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import edu.stanford.rsl.tutorial.filters.RamLakKernel;
import edu.stanford.rsl.tutorial.parallel.ParallelBackprojector2D;
import edu.stanford.rsl.tutorial.parallel.ParallelProjector2D;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

/**
 * Do parallel-beam FBP reconstructions
 * @author Yixing Huang
 *
 */

public class ParallelRecon100DegreeWithNoiseForPhantomForTestFromSinograms {
	static int startAngle = 41;

	public static void main (String [] args) throws Exception{
		new ImageJ();
		
		String sinosPath = "D:\\Tasks\\FAU4\\CellImaging\\FOVRecon\\AlgaeTestPhantomNoise10e5\\sinogramProcessed\\sinogramsPwls2.tif";
		String saveFolderPath = "D:\\Tasks\\FAU4\\CellImaging\\FOVRecon\\AlgaeTestPhantomNoise10e5\\TestSlicesPWLS2\\";
		String referenceFolderPath = saveFolderPath + "referenceRecons\\";
		String referencePath = saveFolderPath + "reference\\";
		String sinogramPath = saveFolderPath + "sinograms\\";
		String evaluationPath = saveFolderPath + "evaluation\\";
		File outPutDir;
		outPutDir = new File(referenceFolderPath);
		if(!outPutDir.exists()){
		   outPutDir.mkdirs();
		}
		outPutDir = new File(referencePath);
		if(!outPutDir.exists()){
		   outPutDir.mkdirs();
		}
		outPutDir = new File(sinogramPath);
		if(!outPutDir.exists()){
		   outPutDir.mkdirs();
		}
		outPutDir = new File(evaluationPath);
		if(!outPutDir.exists()){
		   outPutDir.mkdirs();
		}
		
		
		String reconFbpPath;
		String artifactPath;
		String reconPath;
		String path2;
		int sizeX = 512;
		int sizeY = sizeX;
		int s = 2; //sampling factor
		float sx = 1.4f;
		int zs = 1;
		ImagePlus imp, impFbp, impArtifact, impRef;
		ParallelRecon100DegreeWithNoiseForPhantomForTestFromSinograms obj = new ParallelRecon100DegreeWithNoiseForPhantomForTestFromSinograms();
		Grid2D phan, recon, reconLimited, artifact, sinogram, filteredSinogram;
		Grid2D phanNoisy;
		int numDet = 512;
		double deltaS = 1;
		ParallelProjector2D projector = new ParallelProjector2D(Math.PI, Math.PI/180.0, numDet * deltaS, deltaS);
		ParallelBackprojector2D backproj = new ParallelBackprojector2D(sizeX/s, sizeY/s, sx, sx);
		RamLakKernel ramLak = new RamLakKernel(numDet, deltaS);
		imp = IJ.openImage(sinosPath);
		Grid3D sino3D = ImageUtil.wrapImagePlus(imp);
		int idSave;
		
		Grid3D recon3D = new Grid3D(sizeX/s, sizeY/s, sino3D.getSize()[2]/zs);
		for(int imgIdx = 0; imgIdx < 512; imgIdx = imgIdx + zs )
		{
			idSave = imgIdx;
			sinogram = sino3D.getSubGrid(imgIdx);
			sinogram.setSpacing(deltaS, Math.PI/180.0);
			if(imgIdx == 0)
				sinogram.clone().show("sinogram");
			filteredSinogram = new Grid2D(sinogram);
			for(int theta = 0; theta < startAngle; theta ++)
				for(int i = 0; i < sinogram.getSize()[0]; i++)
				{
					filteredSinogram.setAtIndex(i, theta, 0);
				}
			for (int theta = startAngle; theta < 100 + startAngle; ++theta) {
				ramLak.applyToGrid(filteredSinogram.getSubGrid(theta));
			}
			for(int j = 100 + startAngle; j < sinogram.getSize()[1]; j++)
				for(int i = 0; i < sinogram.getSize()[0]; i++)
					filteredSinogram.setAtIndex(i, j, 0);
			
			if(imgIdx == 0)
				filteredSinogram.clone().show("filtered sinogram");
			reconLimited = backproj.backprojectPixelDriven(filteredSinogram);
	
			if(imgIdx == 0)
				reconLimited.clone().show("recon limited");
			path2 = saveFolderPath + idSave + "\\";
			outPutDir = new File(path2);
			if(!outPutDir.exists()){
			   outPutDir.mkdirs();
			}
				
			reconFbpPath = path2 + "data" + idSave + ".tif";
			artifactPath = path2 + "data" + idSave + "_mask.tif";
			impFbp = ImageUtil.wrapGrid(reconLimited, null);
			IJ.saveAs(impFbp, "Tiff", reconFbpPath);
			recon3D.setSubGrid(imgIdx/zs, (Grid2D)reconLimited.clone());
			//reconLimited.getGridOperator().subtractBy(reconLimited, recon);
			if(imgIdx == 0)
				reconLimited.clone().show("artifact");
			impArtifact = ImageUtil.wrapGrid(reconLimited, null);
			IJ.saveAs(impArtifact, "Tiff", artifactPath);
			System.out.print(imgIdx + " ");
			
		}
		ImagePlus imp3D = ImageUtil.wrapGrid3D(recon3D, null);
		String path4 = saveFolderPath + "reconFbp3D.tif";
		IJ.saveAs(imp3D, "Tiff", path4);
		System.out.println();
		System.out.println("Finished!");
		
	}
	
	private Grid2D rotateImage90Deg(Grid2D img, int tag)
	{
		int sizeX = img.getSize()[0];
		int sizeY = img.getSize()[1];
		Grid2D imgRot = new Grid2D(img);
		switch (tag)
		{
		case 1:
			for(int i = 0; i < img.getSize()[0]; i++)
				for(int j = 0; j < img.getSize()[1]; j++)
					imgRot.setAtIndex(sizeY - 1 - j, i, img.getAtIndex(i, j));
			break;
		case 2:
			for(int i = 0; i < img.getSize()[0]; i++)
				for(int j = 0; j < img.getSize()[1]; j++)
					imgRot.setAtIndex(sizeX - 1 - i, sizeY - 1 - j, img.getAtIndex(i, j));
			break;
		case 3:
			for(int i = 0; i < img.getSize()[0]; i++)
				for(int j = 0; j < img.getSize()[1]; j++)
					imgRot.setAtIndex(j, sizeX - 1 - i, img.getAtIndex(i, j));
			break;
		default:
			break;
		}
		
		
		
		return imgRot;
	}
	
	private void addPoissonNoise(Grid2D sinogram, double d) throws Exception{
		double val;
		float amp = 50.f;
		sinogram.getGridOperator().divideBy(sinogram, amp);
		Grid2D I = new Grid2D(sinogram.getWidth(), sinogram.getHeight());
		for (int i = 0; i < sinogram.getWidth(); i ++)
			for(int j = 0; j < sinogram.getHeight(); j++)
			{
				val = d * Math.pow(Math.E, -sinogram.getAtIndex(i, j));
				I.setAtIndex(i, j, (float)(val));
			}
		

		PoissonNoiseFilteringTool poisson = new PoissonNoiseFilteringTool();
		poisson.applyToolToImage(I);
	
		
		for (int i = 0; i < sinogram.getWidth(); i ++)
			for(int j = 0; j < sinogram.getHeight(); j++)
			{
				val = - Math.log(I.getAtIndex(i, j)/d);
				sinogram.setAtIndex(i, j, (float)(val>=0?val:0));
			
			}
		sinogram.getGridOperator().multiplyBy(sinogram, amp);
	}
	
	private void addGaussianNoise(Grid2D phan)
	{
		java.util.Random r = new java.util.Random();
		double variance = 0.001;
		double mean = 0;
		double noise;
		for(int i = 0; i < phan.getSize()[0]; i++)
			for(int j = 0; j < phan.getSize()[1]; j++)
			{
				
				noise = r.nextGaussian() * Math.sqrt(variance) + mean;
				phan.setAtIndex(i, j, phan.getAtIndex(i, j) + (float) noise);
			}
	}
	
	private void addFOVCircle(Grid2D phan)
	{
		float r = (phan.getSize()[0] - 1.0f)/2.0f;
		float rr = r * r;
		float xCent = r;
		float yCent = xCent;
		float dd;
		for(int i = 0; i < phan.getSize()[0]; i ++)
			for(int j = 0; j < phan.getSize()[1]; j ++)
			{
				dd = (i - xCent) * (i - xCent) + (j - yCent) * (j - yCent);
				if(dd <= rr && dd > rr - 250)
					phan.setAtIndex(i, j, 1.0f);
			}
	}
	
}
