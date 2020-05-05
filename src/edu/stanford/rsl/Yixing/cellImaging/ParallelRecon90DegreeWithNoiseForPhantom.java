package edu.stanford.rsl.Yixing.cellImaging;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
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

public class ParallelRecon90DegreeWithNoiseForPhantom {
	static int startAngle = 45;

	public static void main (String [] args) throws Exception{
		new ImageJ();
		
		String folderPath = "D:\\Tasks\\FAU4\\CellImaging\\TomoSlicesProcessed\\";
		String imgPath;
		String saveFolderPath = "D:\\Tasks\\FAU4\\CellImaging\\FOVRecon\\TrainingData90\\";
		String referenceFolderPath = "D:\\Tasks\\FAU4\\CellImaging\\FOVRecon\\referenceRecons\\";
		String reconFbpPath;
		String artifactPath;
		String reconPath;
		int sizeX = 512;
		int sizeY = sizeX;
		int s = 2; //sampling factor
		float sx = 1.4f;
		ImagePlus imp, impFbp, impArtifact, impRef;
		ParallelRecon90DegreeWithNoiseForPhantom obj = new ParallelRecon90DegreeWithNoiseForPhantom();
		Grid2D phan, recon, reconLimited, artifact, sinogram, filteredSinogram;
		Grid2D phanNoisy;
		int numDet = 512;
		double deltaS = 1;
		ParallelProjector2D projector = new ParallelProjector2D(Math.PI, Math.PI/180.0, numDet * deltaS, deltaS);
		ParallelBackprojector2D backproj = new ParallelBackprojector2D(sizeX/s, sizeY/s, sx, sx);
		RamLakKernel ramLak = new RamLakKernel(numDet, deltaS);
		int idSave;
		for(int imgIdx = 0; imgIdx <= 139; imgIdx++ )
		{
			imgPath = folderPath + imgIdx + ".tif";
			imp = IJ.openImage(imgPath);
			phan = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
			phan.getGridOperator().addBy(phan, 0.15f);
			obj.addFOVCircle(phan);
			phan = obj.rotateImage90Deg(phan, imgIdx % 4 + 3);
			idSave = 3000 + imgIdx + 420;

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
			recon = backproj.backprojectPixelDriven(filteredSinogram);
			if(imgIdx == 0)
				recon.clone().show("recon");
			
			reconPath = referenceFolderPath + idSave + ".tif";
			impRef = ImageUtil.wrapGrid(recon, null);
			IJ.saveAs(impRef, "Tiff", reconPath);
			
			
			obj.addPoissonNoise(sinogram, 1.0e4);

			filteredSinogram = new Grid2D(sinogram);
			for(int theta = 0; theta < startAngle; theta ++)
				for(int i = 0; i < sinogram.getSize()[0]; i++)
				{
					filteredSinogram.setAtIndex(i, theta, 0);
				}
			for (int theta = startAngle; theta < 91 + startAngle; ++theta) {
				ramLak.applyToGrid(filteredSinogram.getSubGrid(theta));
			}
			for(int j = 91 + startAngle; j < sinogram.getSize()[1]; j++)
				for(int i = 0; i < sinogram.getSize()[0]; i++)
					filteredSinogram.setAtIndex(i, j, 0);
			reconLimited = backproj.backprojectPixelDriven(filteredSinogram);
	
			if(imgIdx == 0)
				reconLimited.clone().show("recon limited");
			reconFbpPath = saveFolderPath + "data" + idSave + ".tif";
			artifactPath = saveFolderPath + "data" + idSave + "_mask.tif";
			impFbp = ImageUtil.wrapGrid(reconLimited, null);
			IJ.saveAs(impFbp, "Tiff", reconFbpPath);
			
			reconLimited.getGridOperator().subtractBy(reconLimited, recon);
			if(imgIdx == 0)
				reconLimited.clone().show("artifact");
			impArtifact = ImageUtil.wrapGrid(reconLimited, null);
			IJ.saveAs(impArtifact, "Tiff", artifactPath);
			System.out.print(imgIdx + " ");
		}
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
