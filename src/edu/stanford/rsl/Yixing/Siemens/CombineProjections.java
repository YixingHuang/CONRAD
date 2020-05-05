package edu.stanford.rsl.Yixing.Siemens;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.opencl.OpenCLGrid3D;
import edu.stanford.rsl.conrad.geometry.trajectories.Trajectory;
import edu.stanford.rsl.conrad.phantom.NumericalSheppLogan3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import edu.stanford.rsl.tutorial.fan.CosineFilter;
import edu.stanford.rsl.tutorial.fan.FanBeamBackprojector2D;
import edu.stanford.rsl.tutorial.fan.FanBeamProjector2D;
import edu.stanford.rsl.tutorial.filters.RamLakKernel;
import edu.stanford.rsl.conrad.utils.Configuration;
import edu.stanford.rsl.tutorial.cone.ConeBeamBackprojector;
import edu.stanford.rsl.tutorial.cone.ConeBeamCosineFilter;
import edu.stanford.rsl.tutorial.cone.ConeBeamProjector;
import edu.stanford.rsl.conrad.filtering.PoissonNoiseFilteringTool;
import edu.stanford.rsl.conrad.filtering.redundancy.ParkerWeightingTool;
import edu.stanford.rsl.tutorial.weightedtv.TVOpenCLGridOperators;

public class CombineProjections {
	public int factor = 2; //image size factor
	protected int maxProjs;
	public int imgSizeX;
	public int imgSizeY;
	public int imgSizeZ;
	protected Trajectory geo = null;
	protected int width;
	protected int height;
	protected double spacingX;
	protected double spacingY;
	protected double spacingZ;
	protected double originX;
	protected double originY;
	protected double originZ;
	public ConeBeamProjector cbp;
	public ConeBeamBackprojector cbbp;
	
	public OpenCLGrid3D sinoCL, sinoCL2, volCL, reconCL, artifactCL;
	public Grid3D sinogram;
	
	public static void main(String[] args) throws Exception {
		new ImageJ();
		


		String savePath = "E:\\SiemensMarkerData\\combinedProjections\\";
		String sinoPath = "E:\\SiemensMarkerData\\projections\\";
		String sinoPath1 = "E:\\SiemensMarkerData\\reprojections\\";
		String sinoPath2 = "E:\\SiemensMarkerData\\reprojections2\\";
		String saveName1;
		CombineProjections obj = new CombineProjections(); 


		ImagePlus imp1, imp2;

		
		String pathTemp;
		Grid3D sinoOri, sinoRep1, sinoRep2;
		Grid3D sinoComb = new Grid3D(1776, 976, 400);
		for(int idx = 19; idx <= 19; idx++){
		//int i = 1;
			pathTemp = sinoPath + "projection" + idx + ".tif";
			imp2 = IJ.openImage(pathTemp);
			sinoOri= ImageUtil.wrapImagePlus(imp2);

			
			pathTemp = sinoPath1 + "projection" + idx + ".tif";
			imp2 = IJ.openImage(pathTemp);
			sinoRep1= ImageUtil.wrapImagePlus(imp2);

			pathTemp = sinoPath2 + "projection" + idx + ".tif";
			imp2 = IJ.openImage(pathTemp);
			sinoRep2= ImageUtil.wrapImagePlus(imp2);
			
			for(int i = 0; i < 405; i++)
				for(int j = 0; j < sinoComb.getSize()[1]; j++)
					for(int k = 0; k < sinoComb.getSize()[2]; k++)
					{
						sinoComb.setAtIndex(i, j, k, 2 *sinoRep1.getAtIndex(i, j, k));
					}
			for(int i = 405; i < 1371; i++)
				for(int j = 0; j < sinoComb.getSize()[1]; j++)
					for(int k = 0; k < sinoComb.getSize()[2]; k++)
					{
						sinoComb.setAtIndex(i, j, k, sinoOri.getAtIndex(i - 300, j, k));
					}
			
			for(int i = 1371; i < 1776; i++)
				for(int j = 0; j < sinoComb.getSize()[1]; j++)
					for(int k = 0; k < sinoComb.getSize()[2]; k++)
					{
						sinoComb.setAtIndex(i, j, k, 2 * sinoRep2.getAtIndex(i - 1371, j, k));
					}
			
			imp1 = ImageUtil.wrapGrid(sinoComb, null);
			saveName1 = savePath + "projection" +idx + "_2.tif";
		    IJ.saveAs(imp1, "Tiff", saveName1);
			System.out.println(idx);
			
		}
    }
	
}
