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

public class FanBeamTruncatedReconWeightedTest {
	static int startAngle = 41;

	public static void main (String [] args) throws Exception{
		new ImageJ();
		
		String testVolPath = "D:\\wTVprocessedData\\18.tif";
		String imgPath;
		String saveFolderPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree\\TestWeighted\\";
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
		FanBeamTruncatedReconWeightedTest obj = new FanBeamTruncatedReconWeightedTest();
		Grid2D phan, recon, reconTruncated, artifact, sinogram, filteredSinogram, sinogramTruncated;
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
		sinogramTruncated = new Grid2D(numDet - 2 * numTrunc, numAngle);
//		sinogramTruncated = new Grid2D(numDet, numAngle);
		sinogramTruncated.setSpacing(deltaT, deltaBeta);
		RamLakKernel ramLak2 = new RamLakKernel(sinogramTruncated.getSize()[0], deltaT);
		
		imp = IJ.openImage(testVolPath);
		Grid3D vol = ImageUtil.wrapImagePlus(imp);
		vol.getGridOperator().divideBy(vol, 2000f);
		String path2;
		Grid3D recon3D = new Grid3D(sizeX/s, sizeY/s, 256);
		
		double halfT = (maxT - deltaT)/2.0 - numTrunc * deltaT;
		double r0 = focalLength * halfT/Math.sqrt(halfT * halfT + focalLength * focalLength);
		System.out.print(r0);
		Grid2D mask = obj.getWeightMask(new Grid2D(sizeX/s, sizeY/s), r0);
	
		for(int imgIdx = 0; imgIdx < recon3D.getSize()[2]; imgIdx ++)
		{
			
			phan = vol.getSubGrid(imgIdx);
	
			idSave = imgIdx;

			if(imgIdx == 0)
				phan.clone().show("phan");
			sinogram = projector.projectRayDrivenCL(phan);
			if(imgIdx == 0)
				sinogram.show("The Sinogram");
			filteredSinogram = new Grid2D(sinogram);

			for (int theta = 0; theta < sinogram.getSize()[1]; ++theta) {
				ramLak.applyToGrid(filteredSinogram.getSubGrid(theta));
			}
			if(imgIdx == 0)
				filteredSinogram.show("The Filtered Sinogram");
			recon = backproj.backprojectPixelDrivenCLWithSpacing(filteredSinogram, (float)s, (float)s);
			if(imgIdx == 0)
				recon.clone().show("recon");
			
			reconPath = referenceFolderPath + idSave + ".tif";
			impRef = ImageUtil.wrapGrid(recon, null);
			IJ.saveAs(impRef, "Tiff", reconPath);
			
			
//			obj.addPoissonNoise(sinogram, 1.0e4);
			
			obj.truncateSinogram(sinogram, sinogramTruncated, numTrunc);
			filteredSinogram = new Grid2D(sinogramTruncated);
			
			for (int theta = 0; theta < sinogram.getSize()[1]; ++theta) {
				ramLak2.applyToGrid(filteredSinogram.getSubGrid(theta));
			}
			reconTruncated = backproj.backprojectPixelDrivenCLWithSpacing(filteredSinogram, (float)s, (float)s);
			reconTruncated.getGridOperator().multiplyBy(reconTruncated, mask);
			if(imgIdx == 0)
				reconTruncated.clone().show("recon limited");
			path2 = saveFolderPath + idSave + "\\";
			outPutDir = new File(path2);
			if(!outPutDir.exists()){
			   outPutDir.mkdirs();
			}
			reconFbpPath = path2 + "data" + idSave + ".tif";
			artifactPath = path2 + "data" + idSave + "_mask.tif";
			impFbp = ImageUtil.wrapGrid(reconTruncated, null);
			IJ.saveAs(impFbp, "Tiff", reconFbpPath);
			recon3D.setSubGrid(imgIdx, (Grid2D)reconTruncated.clone());
			reconTruncated.getGridOperator().subtractBy(reconTruncated, recon);
			if(imgIdx == 0)
				reconTruncated.clone().show("artifact");
			impArtifact = ImageUtil.wrapGrid(reconTruncated, null);
			IJ.saveAs(impArtifact, "Tiff", artifactPath);
			System.out.print(imgIdx + " ");
		}
		System.out.println();
		ImagePlus imp3D = ImageUtil.wrapGrid3D(recon3D, null);
		String path4 = saveFolderPath + "reconFbp3D.tif";
		IJ.saveAs(imp3D, "Tiff", path4);
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
	
	private Grid2D getWeightMask(Grid2D recon, double r0)
	{
		double r, angle, w;
		Grid2D mask = new Grid2D(recon);
		for(int i = 0; i < recon.getSize()[0]; i++)
		{
			double x = i - (recon.getSize()[0] - 1)/2.0; 
			for(int j = 0; j < recon.getSize()[1]; j++)
			{
				double y = j - (recon.getSize()[1] - 1)/2.0;
				r = Math.sqrt(x * x + y * y) * 2;
				if(r > r0)
				{
					angle = Math.asin(r0/r);
					w = Math.PI/(2 * angle);
					mask.setAtIndex(i, j, (float) w);
				}
				else
					mask.setAtIndex(i, j, 1.0f);
			}
		}
		return mask;
	}
	
	
}
