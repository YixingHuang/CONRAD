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

public class ParallelRecon100DegreeMicroSphereNoNoise {
	static int startAngle = 41;

	public static void main (String [] args) throws Exception{
		new ImageJ();
		String dataPath = "D:\\Tasks\\FAU4\\CellImaging\\microSphere512.tif";
		ImagePlus imp0 =IJ.openImage(dataPath);
		Grid3D data = ImageUtil.wrapImagePlus(imp0);
		
		String saveFolderPath = "D:\\Tasks\\FAU4\\CellImaging\\FbpReconsMicroSphere\\";
		String referenceFolderPath = "D:\\Tasks\\FAU4\\CellImaging\\referenceReconsMicroSphere\\";
		String reconFbpPath;
		String artifactPath;
		String reconPath;
		int sizeX = 512;
		int sizeY = sizeX;
		int s = 2; //sampling factor
		int zs = 1;
		Grid3D recon3D = new Grid3D(sizeX/s, sizeY/s, data.getSize()[2]/zs);
		ImagePlus imp, impFbp, impArtifact, impRef;
		ParallelRecon100DegreeMicroSphereNoNoise obj = new ParallelRecon100DegreeMicroSphereNoNoise();
		Grid2D phan, recon, reconLimited, artifact, sinogram, filteredSinogram;
		int numDet = 512;
		double deltaS = 1;
		ParallelProjector2D projector = new ParallelProjector2D(Math.PI, Math.PI/180.0, numDet * deltaS, deltaS);
		ParallelBackprojector2D backproj = new ParallelBackprojector2D(sizeX/s, sizeY/s, s, s);
		RamLakKernel ramLak = new RamLakKernel(numDet, deltaS);
		int idSave;
		File outPutDir;
		String path2;
		for(int imgIdx = 0; imgIdx < 512; imgIdx = imgIdx + zs )
		{
			idSave = imgIdx;
			phan = (Grid2D)data.getSubGrid(imgIdx).clone();
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
			
			
			//obj.addPoissonNoise(sinogram, 1.0e5);

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
			reconLimited = backproj.backprojectPixelDriven(filteredSinogram);
	
			if(imgIdx == 0)
				reconLimited.clone().show("recon limited");
			recon3D.setSubGrid(imgIdx/zs, (Grid2D)reconLimited.clone());
			path2 = saveFolderPath + idSave + "\\";
			outPutDir = new File(path2);
			if(!outPutDir.exists()){
		    outPutDir.mkdirs();
			}
			reconFbpPath = path2 + "data" + idSave + ".tif";
			artifactPath = path2 + "data" + idSave + "_mask.tif";
			impFbp = ImageUtil.wrapGrid(reconLimited, null);
			IJ.saveAs(impFbp, "Tiff", reconFbpPath);
			
			reconLimited.getGridOperator().subtractBy(reconLimited, recon);
			if(imgIdx == 0)
				reconLimited.clone().show("artifact");
			impArtifact = ImageUtil.wrapGrid(reconLimited, null);
			IJ.saveAs(impArtifact, "Tiff", artifactPath);
			
			System.out.print(imgIdx + " ");
		}
		String path3 = saveFolderPath + "evaluation\\";
		outPutDir = new File(path3);
		if(!outPutDir.exists()){
	    outPutDir.mkdirs();
		}
		
		ImagePlus imp3D = ImageUtil.wrapGrid3D(recon3D, null);
		String path4 = saveFolderPath + "reconFbp3D.tif";
		IJ.saveAs(imp3D, "Tiff", path4);
		System.out.println();
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
	
	
	
}
