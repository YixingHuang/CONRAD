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

public class ParallelReconFromCombinedProjectionsForCell {
	private int startAngle = 20;

	public static void main (String [] args) throws Exception{
		new ImageJ();
		//float scale = 40.0f*180.0f/160.0f;
		float scale = 1;
		ParallelReconFromCombinedProjectionsForCell obj = new ParallelReconFromCombinedProjectionsForCell();
		
		String path = "D:\\Tasks\\FAU4\\CellImaging\\AlgaeTestPhantomNoise10e4\\TestSlicesPWLS2\\CombineProjections\\";
		ImagePlus imp0 =IJ.openImage(path+"sino3DReo.tif");
		Grid3D proj0 = ImageUtil.wrapImagePlus(imp0);
		proj0.show("projections");
		
		Grid3D sinos = obj.reorderProjections(proj0);
		
		


		String saveFolderPath = "D:\\Tasks\\FAU4\\CellImaging\\AlgaeTestPhantomNoise10e4\\TestSlicesPWLS2\\";
		String reconFbpPath;
		String artifactPath;
		int sizeX = 512;
		int sizeY = sizeX;
		int s = 2; //sampling factor
		int zs = 1;
		ImagePlus impFbp, imp3D;
		Grid2D recon, sinogram, filteredSinogram;
		int numDet = 512;
		double deltaS = 1;
		ParallelBackprojector2D backproj = new ParallelBackprojector2D(sizeX/s, sizeY/s, s, s);
		RamLakKernel ramLak = new RamLakKernel(numDet, deltaS);
		int idSave;
		Grid3D recon3D = new Grid3D(sizeX/s, sizeY/s, sinos.getSize()[2]/zs);
		String path2, path3, path4;
		File outPutDir;
		
		Grid2D sinoPadd;
		for(int imgIdx = 0; imgIdx < sinos.getSize()[2]; imgIdx = imgIdx + zs )
		{
			
			idSave = imgIdx;
			
			sinogram = (Grid2D) sinos.getSubGrid(imgIdx).clone();
			sinogram.setSpacing(1, Math.PI/180.0);
			if(imgIdx == 0)
				sinogram.show("The Sinogram");
			filteredSinogram = new Grid2D(sinogram);
			for (int theta = 0; theta < sinogram.getSize()[1]; ++theta) {
				ramLak.applyToGrid(filteredSinogram.getSubGrid(theta));
			}
			
	
			recon = backproj.backprojectPixelDriven(filteredSinogram);
			recon.getGridOperator().multiplyBy(recon, scale);
			if(imgIdx == 0)
				recon.clone().show("recon");
			
			
//			path2 = saveFolderPath + idSave + "\\";
//			outPutDir = new File(path2);
//			if(!outPutDir.exists()){
//		    outPutDir.mkdirs();
//			}
		    
//			reconFbpPath = path2 + "data" + idSave + ".tif";
//			artifactPath = path2 + "data" + idSave + "_mask.tif";
//			impFbp = ImageUtil.wrapGrid(recon, null);
//			IJ.saveAs(impFbp, "Tiff", reconFbpPath);
//			IJ.saveAs(impFbp, "Tiff", artifactPath);
			recon3D.setSubGrid(imgIdx/zs, (Grid2D)recon.clone());
			System.out.print(imgIdx + " ");			
		}
//		path3 = saveFolderPath + "evaluation\\";
//		outPutDir = new File(path3);
//		if(!outPutDir.exists()){
//	    outPutDir.mkdirs();
//		}
		recon3D.show("recon3D");
		imp3D = ImageUtil.wrapGrid3D(recon3D, null);
		path4 = saveFolderPath + "reconFbp3DCombined.tif";
		IJ.saveAs(imp3D, "Tiff", path4);
		System.out.println("\nFinished!");
		Grid3D recon3D2 = obj.reorderVolume(recon3D);
		recon3D2.show("recon3D2");
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
	
	private Grid3D reorderVolume(Grid3D proj){
		
		Grid3D sino = new Grid3D(proj.getSize()[1], proj.getSize()[2], proj.getSize()[0]);
		for(int i = 0; i < proj.getSize()[0]; i++){
			for(int j = 0; j < proj.getSize()[1]; j++) {
				for(int k  = 0; k < proj.getSize()[2]; k++) {
					sino.setAtIndex(j, k, i, proj.getAtIndex(i, j, k));
				}
			}
		}
		
		return sino;	
	}
	
	private Grid2D zeroPaddingProjections(Grid2D sinogram, int numAngle) {
		Grid2D sinoPadd = new Grid2D(sinogram.getSize()[0], sinogram.getSize()[1] + numAngle);
		sinoPadd.setSpacing(sinogram.getSpacing());
		for(int i = 0; i < sinogram.getSize()[0]; i++)
			for(int j = 21; j < sinogram.getSize()[1]; j++)
				sinoPadd.setAtIndex(i, j + numAngle, sinogram.getAtIndex(i, j));
		
		return sinoPadd;
	}
	
}
