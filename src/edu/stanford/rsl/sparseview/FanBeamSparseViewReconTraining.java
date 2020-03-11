package edu.stanford.rsl.sparseview;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
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

public class FanBeamSparseViewReconTraining {

	public static void main (String [] args) throws Exception{
		new ImageJ();

		String folderPath = "C:\\Tasks\\FAU4\\SparseView\\gtSlices\\";
		String imgPath;
		String saveFolderPath = "C:\\Tasks\\FAU4\\SparseView\\trainingData_d10\\";
		String referenceFolderPath = "C:\\Tasks\\FAU4\\SparseView\\referenceRecons\\";
		String reconFbpPath;
		String artifactPath;
		String reconPath;
		int sizeX = 512;
		int sizeY = sizeX;
		int s = 2; //sampling factor
		float sx = 1.f;
		ImagePlus imp, impFbp, impArtifact, impSparse;
		FanBeamSparseViewReconTraining obj = new FanBeamSparseViewReconTraining();
		Grid2D phan, recon, reconTruncated, artifact, sinogram, filteredSinogram, sinogramTruncated;
		Grid2D phanNoisy;
		double focalLength = 600; //zoom in factor of 2; real value is 800 mm
		double maxBeta = Math.PI * 2;
		double deltaBeta = 12 * Math.PI / 180.0;
		double maxT = 768;
		double deltaT = 1;
		int numDet = (int)(maxT/deltaT);
		int numAngle = (int)(maxBeta/deltaBeta);
		FanBeamProjector2D projector = new FanBeamProjector2D(focalLength, maxBeta, deltaBeta, maxT, deltaT);
	
		FanBeamBackprojector2D backproj = new FanBeamBackprojector2D(focalLength, deltaT, deltaBeta, sizeX/s, sizeY/s);
		RamLakKernel ramLak = new RamLakKernel(numDet, deltaT);
		int idSave;
		int imgIdx;
		for(int patient = 1; patient <= 19; patient ++)
		{
			for(int idx = 0; idx <= 25; idx ++)
			{
				imgIdx = patient * 1000 + idx * 10;
				imgPath = folderPath + imgIdx + ".tif";
				imp = IJ.openImage(imgPath);
				phan = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		
				idSave = imgIdx;

				if(imgIdx == 1000)
					phan.clone().show("phan");
				sinogram = projector.projectRayDrivenCL(phan);
				if(imgIdx == 1000)
					sinogram.show("The Sinogram");
				filteredSinogram = new Grid2D(sinogram);

				for (int theta = 0; theta < sinogram.getSize()[1]; ++theta) {
					ramLak.applyToGrid(filteredSinogram.getSubGrid(theta));
				}
				if(imgIdx == 1000)
					filteredSinogram.show("The Filtered Sinogram");
				recon = backproj.backprojectPixelDrivenCLWithSpacing(filteredSinogram, (float)s, (float)s);
				if(imgIdx == 1000)
					recon.clone().show("recon");
				
				
				reconFbpPath = saveFolderPath + "data" + idSave + ".tif";
				artifactPath = saveFolderPath + "data" + idSave + "_mask.tif";
				impFbp = ImageUtil.wrapGrid(recon, null);
				IJ.saveAs(impFbp, "Tiff", reconFbpPath);
				
				artifact = new Grid2D(recon);
				artifact.getGridOperator().subtractBy(artifact, obj.downSampling(phan));
				if(imgIdx == 1000)
					artifact.clone().show("artifact");
				impArtifact = ImageUtil.wrapGrid(artifact, null);
				IJ.saveAs(impArtifact, "Tiff", artifactPath);
				System.out.print(imgIdx + " ");
		}
		}
		System.out.println();
		System.out.println("Finished!");
		
	}
	
	private Grid2D downSampling(Grid2D img){
		Grid2D dimg = new Grid2D(img.getSize()[0]/2, img.getSize()[1]/2);
		float val = 0;
		for(int i = 0; i < dimg.getSize()[0]; i++){
			for(int j = 0; j < dimg.getSize()[1]; j++){
				val= img.getAtIndex(i*2, j*2) + img.getAtIndex(i*2+1, j*2) +
						img.getAtIndex(i*2, j*2+1) + img.getAtIndex(i*2+1, j*2+1);
				dimg.setAtIndex(i, j, val/4);
				
			}
		}
		return dimg;
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
	
}
