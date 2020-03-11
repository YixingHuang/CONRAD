package edu.stanford.rsl.cellImaging;

import java.io.File;

import edu.stanford.rsl.conrad.data.numeric.Grid1D;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.NumericPointwiseOperators;
import edu.stanford.rsl.conrad.filtering.PoissonNoiseFilteringTool;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import edu.stanford.rsl.tutorial.filters.RamLakKernel;
import edu.stanford.rsl.tutorial.parallel.ParallelBackprojector2D;
import edu.stanford.rsl.tutorial.parallel.ParallelProjector2D;
import edu.stanford.rsl.tutorial.weightedtv.TVGradient;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

/**
 * Do parallel-beam FBP reconstructions
 * @author Yixing Huang
 *
 */

public class CombineProjections {

	private int startAngle = 20;
	private int angularRange = 100;
	private ParallelProjector2D projector;

	
	private int sizeX = 512;
	private int sizeY = 512;
	private int numDet = 512;
	private double deltaS = 1;
	private double deltaTheta = Math.PI/180.0;

	private int s = 2; //sampling factor
	private int zs = 1;
	

	private Grid2D recon = null;

	private boolean isInitial = true;
	private Grid3D reconUNet;
	private String initialPath = "D:\\Tasks\\FAU4\\CellImaging\\FbpCellRecons100DegreePwls\\SEUNet1IterPwls20190917.tif";
	private String saveFolderPath = "D:\\Tasks\\FAU4\\CellImaging\\FbpCellRecons100DegreePwls\\CombineProjections\\";
	
	
	public static void main (String [] args) throws Exception{
		new ImageJ();
		
		CombineProjections obj = new CombineProjections();
		
		String path =  "D:\\Tasks\\FAU4\\CellImaging\\";
		ImagePlus imp0 =IJ.openImage(path+"projectionsPwls5Iter.tif");
		Grid3D proj0 = ImageUtil.wrapImagePlus(imp0);
		proj0.show("projections");
		
		Grid3D sinos = obj.reorderProjections(proj0);
		
		if(obj.isInitial)
		{
			obj.reconUNet = obj.read3DVolume(obj.initialPath);
			obj.thresholding(obj.reconUNet, 0.19f);
			obj.applyFovMask(obj.reconUNet);
//			obj.reconUNet.getGridOperator().removeNegative(obj.reconUNet);
		}
		
		obj.reconUNet.clone().show("reconUNet");
		ImagePlus imp3D;
		Grid2D sinoRaw;
		sinoRaw = new Grid2D(obj.numDet, 180);
		sinoRaw.setSpacing(obj.deltaS, obj.deltaTheta);
		obj.projector = new ParallelProjector2D(Math.PI, obj.deltaTheta, obj.numDet * obj.deltaS, obj.deltaS);


	
		int idSave;
		String saveName;
		Grid3D recon3D = new Grid3D(obj.sizeX/obj.s, obj.sizeY/obj.s, sinos.getSize()[2]/obj.zs);
		String path4;

		float scale = 40.0f*180.0f/160.0f;
	
		obj.recon = new Grid2D(obj.sizeX/obj.s, obj.sizeY/obj.s);
	
		
		Grid3D sino3DRep = new Grid3D(obj.numDet, 180, sinos.getSize()[2]/obj.zs);
		Grid3D sino3DComb = new Grid3D(obj.numDet, 180, sinos.getSize()[2]/obj.zs);
		Grid2D sino2DRep = new Grid2D(obj.numDet, 180);
		Grid2D sino2DComb;
		double[] spacing = {obj.s, obj.s};

		for(int imgIdx = 0; imgIdx < sinos.getSize()[2]; imgIdx = imgIdx + obj.zs )
		{
			
			idSave = imgIdx;
			if(obj.isInitial)
			{
				obj.recon = (Grid2D)obj.reconUNet.getSubGrid(imgIdx).clone();
				obj.recon.getGridOperator().divideBy(obj.recon, scale);
			}
			else
				obj.recon.getGridOperator().fill(obj.recon, 0);
			
			sino2DRep = obj.projector.projectRayDrivenCLWithSpacing(obj.recon, spacing);
			sino3DRep.setSubGrid(imgIdx/obj.zs, (Grid2D) sino2DRep.clone());
			
			sinoRaw = (Grid2D) sinos.getSubGrid(imgIdx).clone();
	
			if(imgIdx == 0)
				sinoRaw.show("The Sinogram");

			sino2DComb = obj.combineProjections(sino2DRep, sinoRaw);
			sino3DComb.setSubGrid(imgIdx/obj.zs, sino2DComb);
			System.out.print( imgIdx + " ");			
		}
		
		Grid3D sino3DReo = obj.inverseReorderProjections(sino3DComb);
		
		sino3DRep.show("sino3DRep");
		sino3DComb.show("sino3DComb");
		sino3DReo.show("sino3DReo");
		
		imp3D = ImageUtil.wrapGrid3D(sino3DRep, null);
		path4 = obj.saveFolderPath + "sino3DRep.tif";
		IJ.saveAs(imp3D, "Tiff", path4);
		
		imp3D = ImageUtil.wrapGrid3D(sino3DComb, null);
		path4 = obj.saveFolderPath + "sino3DComb.tif";
		IJ.saveAs(imp3D, "Tiff", path4);
		
		imp3D = ImageUtil.wrapGrid3D(sino3DReo, null);
		path4 = obj.saveFolderPath + "sino3DReo.tif";
		IJ.saveAs(imp3D, "Tiff", path4);
		
	}
	
	private void thresholding(Grid3D recon, float thres)
	{
		for(int i = 0; i < recon.getSize()[0]; i++)
			for(int j = 0; j < recon.getSize()[1]; j++)
				for(int k = 0; k < recon.getSize()[2]; k++)
					if(recon.getAtIndex(i, j, k) < thres)
						recon.setAtIndex(i, j, k, 0);
	}

	
	private Grid3D read3DVolume(String path)
	{
		ImagePlus imp=IJ.openImage(path);
		Grid3D data = ImageUtil.wrapImagePlus(imp);
		return data;
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
	
	private void applyFovMask(Grid3D phan)
	{
		float r = (phan.getSize()[0] - 1.0f)/2.0f;
		float rr = r * r;
		float xCent = r;
		float yCent = xCent;
		float dd;
		for(int k = 0; k < phan.getSize()[2]; k++)
			for(int i = 0; i < phan.getSize()[0]; i ++)
				for(int j = 0; j < phan.getSize()[1]; j ++)
				{
					dd = (i - xCent) * (i - xCent) + (j - yCent) * (j - yCent);
					if(dd > rr)
						phan.setAtIndex(i, j,k,  0f);
				}
	}

	private Grid2D combineProjections(Grid2D sinoRep, Grid2D sinoRaw) {
		Grid2D sinoComb = new Grid2D(numDet, (int)(Math.PI/deltaTheta));
		for(int j = 0; j < sinoComb.getSize()[1]; j++)
			for(int i = 0; i < sinoComb.getSize()[0]; i++)
			{
				if(j < startAngle + 20 || j >  angularRange + startAngle + 20)
					sinoComb.setAtIndex(i, j, sinoRep.getAtIndex(i, j));
				else
					sinoComb.setAtIndex(i, j, sinoRaw.getAtIndex(i, j - 20));
			}
				
		
		return sinoComb;
	}
	

}
