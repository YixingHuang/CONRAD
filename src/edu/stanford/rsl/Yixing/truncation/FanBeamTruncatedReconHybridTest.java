package edu.stanford.rsl.Yixing.truncation;

import java.io.File;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.filtering.PoissonNoiseFilteringTool;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import edu.stanford.rsl.tutorial.filters.RamLakKernel;
import edu.stanford.rsl.tutorial.fan.FanBeamProjector2D;
import edu.stanford.rsl.tutorial.fan.FanBeamBackprojector2D;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

/**
 * Do fan-beam FBP reconstruction from truncated projections
 * @author Yixing Huang
 *
 */

public class FanBeamTruncatedReconHybridTest {
	static int startAngle = 41;

	public static void main (String [] args) throws Exception{
		new ImageJ();
		
		String testVolPath = "D:\\wTVprocessedData\\18.tif";
		String deepLearningVolPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree\\TestWCE\\SEUNet.tif";
		String imgPath;
		String saveFolderPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree\\TestWCE\\";
		String referenceFolderPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree\\Test\\referenceRecons\\";
		File outPutDir;
		outPutDir = new File(referenceFolderPath);
		if(!outPutDir.exists()){
		   outPutDir.mkdirs();
		}
		String reconFbpPath;
		String artifactPath;
		String reconPath;
		int sizeX = 512;
		int sizeY = sizeX;
		int s = 2; //sampling factor
		float sx = 1.f;
		ImagePlus imp, impFbp, impArtifact, impRef;
		FanBeamTruncatedReconHybridTest obj = new FanBeamTruncatedReconHybridTest();
		Grid2D phan, recon, reconHybrid, artifact, sinogram, sinogram2, filteredSinogram, sinogramTruncated;
		Grid2D phanNoisy;
		double focalLength = 1600; //zoom in factor of 2; real value is 800 mm
		double maxBeta = Math.PI * 2;
		double deltaBeta = 0.5 * Math.PI / 180.0;
		double maxT = 768;
		double deltaT = 1;
		int numDet = (int)(maxT/deltaT);
		int numAngle = (int)(maxBeta/deltaBeta);
		FanBeamProjector2D projector = new FanBeamProjector2D(focalLength, maxBeta, deltaBeta, maxT, deltaT);
	
		FanBeamBackprojector2D backproj = new FanBeamBackprojector2D(focalLength, deltaT, deltaBeta, sizeX/s, sizeY/s);
		RamLakKernel ramLak = new RamLakKernel(numDet, deltaT);
		int idSave;
		int numTrunc = 180;
		sinogramTruncated = new Grid2D(numDet, numAngle);
		sinogramTruncated.setSpacing(deltaT, deltaBeta);
		
		imp = IJ.openImage(testVolPath);
		Grid3D vol = ImageUtil.wrapImagePlus(imp);
		vol.getGridOperator().divideBy(vol, 2000f);
		imp = IJ.openImage(deepLearningVolPath);
		Grid3D volDL = ImageUtil.wrapImagePlus(imp);
		String path2;
		Grid3D recon3D = new Grid3D(sizeX/s, sizeY/s, 256);
		Grid3D sino3D = new Grid3D(numDet, numAngle, recon3D.getSize()[2]);
		for(int imgIdx = 0; imgIdx < recon3D.getSize()[2]; imgIdx ++)
		{
			
			phan = vol.getSubGrid(imgIdx);
	
			idSave = imgIdx;

			if(imgIdx == 0)
				phan.clone().show("phan");
			sinogram = projector.projectRayDrivenCL(phan);
			if(imgIdx == 0)
				sinogram.show("The Sinogram");
			
			phan = volDL.getSubGrid(imgIdx);
			sinogram2 = projector.projectRayDrivenCLWithSpacing(phan, s, s);
			
			obj.mergeSinograms(sinogram, sinogram2, numTrunc);
			sino3D.setSubGrid(imgIdx, (Grid2D)sinogram.clone());


			filteredSinogram = new Grid2D(sinogram);
			
			for (int theta = 0; theta < sinogram.getSize()[1]; ++theta) {
				ramLak.applyToGrid(filteredSinogram.getSubGrid(theta));
			}
			reconHybrid = backproj.backprojectPixelDrivenCLWithSpacing(filteredSinogram, (float)s, (float)s);
	
			if(imgIdx == 0)
				reconHybrid.clone().show("recon limited");
			
			recon3D.setSubGrid(imgIdx, (Grid2D)reconHybrid.clone());
			
			System.out.print(imgIdx + " ");
		}
		System.out.println();
		ImagePlus imp3D = ImageUtil.wrapGrid3D(recon3D, null);
		String path4 = saveFolderPath + "reconFbp3DHybrid.tif";
		IJ.saveAs(imp3D, "Tiff", path4);
		String path5 = saveFolderPath + "sino3D.tif";
		imp3D = ImageUtil.wrapGrid3D(sino3D, null);
		IJ.saveAs(imp3D, "Tiff", path5);
		System.out.println("Finished!");
		
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
	
	private void truncateSinogram(Grid2D sinogram, Grid2D sinogramTrunc, int truncWidth)
	{
		for(int i = 0; i < sinogramTrunc.getWidth(); i++)
			for(int j = 0; j < sinogramTrunc.getHeight(); j++)
				sinogramTrunc.setAtIndex(i, j, sinogram.getAtIndex(i + truncWidth, j));
	}
	
	private void truncateSinogram2(Grid2D sinogram, Grid2D sinogramTrunc, int truncWidth)
	{
		for(int i = truncWidth; i < sinogramTrunc.getWidth() - truncWidth; i++)
			for(int j = 0; j < sinogramTrunc.getHeight(); j++)
				sinogramTrunc.setAtIndex(i, j, sinogram.getAtIndex(i, j));
	}
	
	private void mergeSinograms(Grid2D sinogram1, Grid2D sinogram2, int truncWidth)
	{
		for(int i = 0; i < truncWidth; i++)
			for(int j = 0; j < sinogram1.getHeight(); j++)
				sinogram1.setAtIndex(i, j, sinogram2.getAtIndex(i, j));
		
		for(int i = sinogram1.getWidth() - truncWidth; i < sinogram1.getWidth(); i++)
			for(int j = 0; j < sinogram1.getHeight(); j++)
				sinogram1.setAtIndex(i, j, sinogram2.getAtIndex(i, j));
	}
	
}
