package edu.stanford.rsl.tem;

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

public class ParallelRecon121DegreeForCell {

	public static void main (String [] args) throws Exception{
		new ImageJ();
		
		ParallelRecon121DegreeForCell obj = new ParallelRecon121DegreeForCell();
		
		String path =  "D:\\Tasks\\FAU4\\TEM\\cryoTif\\";
		ImagePlus imp0 =IJ.openImage(path+"projection01.tif");
		Grid3D proj0 = ImageUtil.wrapImagePlus(imp0);

		//proj0 = obj.downSamplingXY(proj0, 4);
		
		proj0.show("projections");
		
		Grid3D sinos = obj.reorderProjections(proj0);

		
		sinos.getGridOperator().divideBy(sinos, 40000.f);
		sinos.getGridOperator().log(sinos);
		sinos.getGridOperator().multiplyBy(sinos, -1.0f);
		
		sinos.show("sinos");

		String saveFolderPath = "D:\\Tasks\\FAU4\\TEM\\recon\\";
		String reconFbpPath;
		String artifactPath;
		int sizeX = 2000;
		int sizeY = sizeX;
		int s = 1; //sampling factor
		ImagePlus impFbp, imp3D;
		Grid2D recon, sinogram, filteredSinogram;
		int numDet = sinos.getSize()[0];
		double deltaS = 1;
		ParallelBackprojector2D backproj = new ParallelBackprojector2D(sizeX/s, sizeY/s, s, s);
		RamLakKernel ramLak = new RamLakKernel(numDet, deltaS);
		int idSave;
//		Grid3D recon3D = new Grid3D(sizeX/s, sizeY/s, sinos.getSize()[2]);
		Grid3D recon3D = new Grid3D(sizeX/s, sizeY/s, 1);
		String path2, path3, path4;
		File outPutDir;
		float scale = 20.0f*180.0f/160.0f;
//		for(int imgIdx = 0; imgIdx < sinos.getSize()[2]; imgIdx++ )
		for(int imgIdx = 40; imgIdx < 41; imgIdx++ )
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
			
			
			path2 = saveFolderPath + idSave + "\\";
			outPutDir = new File(path2);
			if(!outPutDir.exists()){
		    outPutDir.mkdirs();
			}
		    
			reconFbpPath = path2 + "data" + idSave + ".tif";
			artifactPath = path2 + "data" + idSave + "_mask.tif";
			impFbp = ImageUtil.wrapGrid(recon, null);
			IJ.saveAs(impFbp, "Tiff", reconFbpPath);
			IJ.saveAs(impFbp, "Tiff", artifactPath);
//			recon3D.setSubGrid(imgIdx, (Grid2D)recon.clone());
			recon3D.setSubGrid(0, (Grid2D)recon.clone());
			System.out.print(imgIdx + " ");			
		}
		path3 = saveFolderPath + "evaluation\\";
		outPutDir = new File(path3);
		if(!outPutDir.exists()){
	    outPutDir.mkdirs();
		}
		recon3D.show("recon3D");
		imp3D = ImageUtil.wrapGrid3D(recon3D, null);
		path4 = saveFolderPath + "reconFbp3D.tif";
		IJ.saveAs(imp3D, "Tiff", path4);
		System.out.println("\nFinished!");
		
	}
	
	private Grid3D reorderProjections(Grid3D proj){
		
		Grid3D sino = new Grid3D(proj.getSize()[1], proj.getSize()[2], proj.getSize()[0]);
		for(int h = 0; h < proj.getSize()[0]; h++){
			for(int s = 0; s < proj.getSize()[1]; s++) {
				for(int theta  = 0; theta < proj.getSize()[2]; theta++) {
					sino.setAtIndex(s, theta, h, proj.getAtIndex(h, s, theta));
				}
			}
		}
		
		return sino;	
	}
	
	private Grid3D downSamplingXY(Grid3D proj, int factor)
	{
		Grid3D proj2 = new Grid3D((int)(proj.getSize()[0]/factor), (int)(proj.getSize()[1]/factor), proj.getSize()[2]);
		
		for(int i = 0; i < proj2.getSize()[0]; i++)
			for(int j = 0; j < proj2.getSize()[1]; j++)
				for(int k = 0; k < proj2.getSize()[2]; k++)
				{
					proj2.setAtIndex(i, j, k, proj.getAtIndex(i * factor, j * factor, k));
				}
		
		return proj2;
	}
	
}
