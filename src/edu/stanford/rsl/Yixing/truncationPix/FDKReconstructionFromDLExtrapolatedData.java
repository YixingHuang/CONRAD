package edu.stanford.rsl.Yixing.truncationPix;

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
import edu.stanford.rsl.Yixing.truncation.WaterCylinderExtrapolation2DFan;

public class FDKReconstructionFromDLExtrapolatedData {
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
	
	public OpenCLGrid3D sinoCL, volCL, reconCL, artifactCL;
	public Grid3D sinogram, reconFDK;
	
	public static void main(String[] args) throws Exception {
		new ImageJ();
		
		String path = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\combineProjectionsDirectPix\\"; //path for wTV data
		String savePath = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\FDKDirectPix\\";

		FDKReconstructionFromDLExtrapolatedData obj = new FDKReconstructionFromDLExtrapolatedData(); 
		obj.initialGeometry();

		
		ImagePlus imp1;
		float numTrunc = 190;
		TVOpenCLGridOperators op = TVOpenCLGridOperators.getInstance();
		WaterCylinderExtrapolation2DFan wceObj = new WaterCylinderExtrapolation2DFan(obj.height, (int)numTrunc);
		String nameProj;
		for(int i = 1; i <= 18; i++){
			if(i == 4)
				continue;
			obj.cbbp=new ConeBeamBackprojector();
			nameProj = path + "projection" + i + ".tif";
			imp1 =IJ.openImage(nameProj);
			obj.sinogram = ImageUtil.wrapImagePlus(imp1);
			obj.FDKReconstruction(obj.sinogram);
			
			imp1 = ImageUtil.wrapGrid(obj.reconFDK, null);
			IJ.saveAs(imp1, "Tiff", (savePath + "FDK" + i+ ".tif"));
			System.out.println(i);
			
		}
    }
	

	
	public void initialGeometry() throws Exception {
		Configuration.loadConfiguration();
		Configuration conf = Configuration.getGlobalConfiguration();
		geo = conf.getGeometry();
		width = geo.getDetectorWidth();
		height = geo.getDetectorHeight();
		
		// create context
		maxProjs = geo.getProjectionStackSize();
		imgSizeX = geo.getReconDimensionX();
		imgSizeY = geo.getReconDimensionY();
		imgSizeZ = geo.getReconDimensionZ();
		spacingX = geo.getVoxelSpacingX();
		spacingY = geo.getVoxelSpacingY();
		spacingZ = geo.getVoxelSpacingZ();
		originX = -geo.getOriginX();
		originY = -geo.getOriginY();
		originZ = -geo.getOriginZ();
		
	}
	
	public void FDKReconstruction(Grid3D sinogram){
		double focalLength = geo.getSourceToDetectorDistance();
		double deltaU = geo.getPixelDimensionX();
		double deltaV = geo.getPixelDimensionY();
		int numProjs = geo.getNumProjectionMatrices();	
		Grid3D sinogram2=new Grid3D(sinogram.getSize()[0],sinogram.getSize()[1], numProjs);
		ConeBeamCosineFilter cbFilter = new ConeBeamCosineFilter(focalLength, width, height, deltaU, deltaV);
		RamLakKernel ramK = new RamLakKernel(width, deltaU);
		ParkerWeightingTool parker = new ParkerWeightingTool(geo);
		for (int i = 0; i < numProjs; ++i) 
			
		{
			parker.setImageIndex(i);
			parker.applyToolToImage(sinogram2.getSubGrid(i));
		
			sinogram2.setSubGrid(i, (Grid2D) sinogram.getSubGrid(i).clone());
			cbFilter.applyToGrid(sinogram2.getSubGrid(i));
			//ramp
			for (int j = 0;j <height; ++j)
				ramK.applyToGrid(sinogram2.getSubGrid(i).getSubGrid(j));
			System.out.print(i + " ");
		}
		System.out.println(" ");
		
		reconFDK = cbbp.backprojectPixelDrivenCL(sinogram2);
		reconFDK.getGridOperator().divideBy(reconFDK, 0.07f);
	
	}
	
	public void getMeasuredSinoCL() throws Exception {
		sinoCL = new OpenCLGrid3D(new Grid3D(width, height, maxProjs));
		sinoCL.getDelegate().prepareForDeviceOperation();
		cbp.fastProjectRayDrivenCL(sinoCL, volCL);
		//sinoCL.show("sinoCL");
	}
	
	
	
	public Grid3D getOriginalGroundTruthData(String path, int ii){
		String pathTemp;
		ImagePlus imp;
	
		pathTemp = path + ii + ".tif";
		imp=IJ.openImage(pathTemp);
		Grid3D imgTemp = ImageUtil.wrapImagePlus(imp);
		
		
		return imgTemp;
	}
	

	
	
	public void saveTrainingData(String path, Grid3D reconGT, Grid3D reconLimited, Grid3D artifacts, int index){
		ImagePlus imp1,imp2, imp3;
		
			imp1 = ImageUtil.wrapGrid(reconGT, null);
			IJ.saveAs(imp1, "Tiff", (path + "reconGT" + index + ".tif"));
			imp2 = ImageUtil.wrapGrid(reconLimited, null);
			IJ.saveAs(imp2, "Tiff", (path + "reconTruncated" + index + ".tif"));
			imp3 = ImageUtil.wrapGrid(artifacts, null);
			IJ.saveAs(imp3, "Tiff", (path + "artifacts" + index + ".tif"));
		
		
	}
	
	public void saveFullReconData(String path, Grid3D reconFull, int index){
		ImagePlus imp1;
		
			imp1 = ImageUtil.wrapGrid(reconFull, null);
			IJ.saveAs(imp1, "Tiff", (path + "reconFull" + index + ".tif"));
	}
}
