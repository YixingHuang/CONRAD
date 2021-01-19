package edu.stanford.rsl.Yixing.ERC;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.opencl.OpenCLGrid3D;
import edu.stanford.rsl.conrad.geometry.trajectories.Trajectory;
import edu.stanford.rsl.conrad.numerics.SimpleVector;
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

public class GenerateReconstrutions {
	protected boolean isExt = false;
	private boolean isPwls = false;
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
	
//	public OpenCLGrid3D sinoCL, volCL, reconCL, artifactCL;
	public Grid3D recon;
	public Grid3D sinogram;

	private String sinoPath = "E:\\Lasse\\LimitedAngle\\";
	private String savePath = "E:\\Lasse\\LimitedAngle\\";
	
	public static void main(String[] args) throws Exception {
		new ImageJ();
		
		GenerateReconstrutions obj = new GenerateReconstrutions(); 
		obj.initialGeometry();

		TVOpenCLGridOperators op = TVOpenCLGridOperators.getInstance();
		ImagePlus imp, imp3;
		String saveName, sliceName;
		Grid2D slice;
		for(int i = 1; i <= 1; i++){
			
			if(i == 6)
				obj.isExt = true;
			else
				obj.isExt = false;
			obj.cbp=new ConeBeamProjector();
			obj.cbbp=new ConeBeamBackprojector();

			obj.loadMeasuredSinogram(i);
			

			

//			obj.sinogram.clone().show("sinogram");
		    
	
			obj.FDKReconstruction(obj.sinogram);
			
			imp = ImageUtil.wrapGrid(obj.recon, null);
			if (obj.isPwls)	
				saveName = obj.savePath + "reconPwls" + i + ".tif";
			else
				saveName = obj.savePath + "reconNoisy" + i + ".tif";
			IJ.saveAs(imp, "Tiff", saveName);
			
			if(obj.isPwls)
				sliceName = obj.savePath + "Pwls" + i + ".png";
			else
				sliceName = obj.savePath + "FDK" + i + ".png";
			slice = (Grid2D) obj.recon.getSubGrid(119).clone();
	    	imp3 = ImageUtil.wrapGrid(slice, null);
	    	imp3.setDisplayRange(0, 0.0039);
	    	IJ.saveAs(imp3, "png", sliceName);
	    	imp3.close();
	    	
			System.out.println(i);
			
		}
		System.out.println("Finished!");
    }
	
	private void loadMeasuredSinogram(int idx)
	{
		String name;
		
		if(isPwls)
			name = sinoPath + "projectionPwls" + idx + ".tif";
		else
			name = sinoPath + "projection150Degree" + idx + ".tif";
		
		ImagePlus imp0 =IJ.openImage(name);
		sinogram = ImageUtil.wrapImagePlus(imp0);
	
	}

	
	public void initialGeometry() throws Exception {
		Configuration.loadConfiguration();
		Configuration conf = Configuration.getGlobalConfiguration();
		geo = conf.getGeometry();
		width = geo.getDetectorWidth();
		height = geo.getDetectorHeight();
		SimpleVector spacingUV = new SimpleVector(geo.getPixelDimensionX(), geo.getPixelDimensionY());
		double[] SDD = geo.getProjectionMatrix(0).computeSourceToDetectorDistance(spacingUV);
		System.out.println("SDD[0] = " + SDD[0] + "SDD[1] = " + SDD[1]);
		geo.setSourceToDetectorDistance(1166.82);
		geo.setSourceToAxisDistance(780);
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
		sinogram.setSpacing(sinogram.getSpacing());
		ConeBeamCosineFilter cbFilter = new ConeBeamCosineFilter(focalLength, width, height, deltaU, deltaV);
		RamLakKernel ramK = new RamLakKernel(width, deltaU);
		ParkerWeightingTool parker = new ParkerWeightingTool(geo);
		if(isExt)
		{
			
			linearInterpolation(sinogram, 707, 20);
		}
		System.out.println("Start filtering");
		for (int i = 0; i < numProjs; ++i) 
			
		{
//			parker.setImageIndex(i);
//			parker.applyToolToImage(sinogram2.getSubGrid(i));
		
			sinogram2.setSubGrid(i, (Grid2D) sinogram.getSubGrid(i).clone());
			cbFilter.applyToGrid(sinogram2.getSubGrid(i));
			//ramp
			for (int j = 0;j <height; ++j)
				ramK.applyToGrid(sinogram2.getSubGrid(i).getSubGrid(j));
			System.out.print(i + " ");
		}
		System.out.println(" ");
		System.out.println("Start backprojection");
		recon = cbbp.backprojectPixelDrivenCL(sinogram2);
//		reconFDK.show("FDK recon");
		//float scalCorrection = (float)( 260/(34.5*720000));
		//reconCL.getGridOperator().multiplyBy(reconCL, scalCorrection);
		//reconFDK = new Grid3D(reconCL);
		//reconFDK.show("FDK reconstruction");	
		
	}
	
	private void linearInterpolation(Grid3D sinogram, int numTrunc, int numExt)
	{
		float diameter_ROI = 350;
		float width_detector = diameter_ROI * 1.34f * 2.5f/2.0f; // the width of the ROI on the detector
		float leftCollimator = (2000 - width_detector)/2.0f; //748.75
		float rightCollimator = leftCollimator + width_detector; //1251.25
		
		float val1, val2, r1;
		for(int k = 0; k < sinogram.getSize()[2]; k++)
			for(int j = 0; j < sinogram.getSize()[1]; j++)
			{
				val1 = sinogram.getAtIndex(numTrunc -1, j, k);
				val2 = sinogram.getAtIndex(sinogram.getSize()[0] - numTrunc, j, k);
				for(int i = 0; i < numExt; i++)
				{
					r1 = 1 - (float)(i)/numExt;
					sinogram.setAtIndex(numTrunc + i, j, k, val1 * r1);
					sinogram.setAtIndex(sinogram.getSize()[0] - numTrunc - i, j, k, val2 * r1);
					
				}
				for(int i = numTrunc + numExt; i < sinogram.getSize()[0] - numTrunc - numExt + 1; i++)
					sinogram.setAtIndex(i, j, k, 0);
			}
	}
	

	
	
	private void reloadImages(String path3DGrids,Grid3D fullRecons, Grid3D limitedRecons, Grid3D artifacts){
 		ImagePlus imp = IJ.openImage(path3DGrids + "limited angle reconstruction.tif");
		Grid3D temp = ImageUtil.wrapImagePlus(imp);
		limitedRecons.getGridOperator().copy(limitedRecons, temp);
		imp = IJ.openImage(path3DGrids + "artifacts.tif");
		temp = ImageUtil.wrapImagePlus(imp);
		artifacts.getGridOperator().copy(artifacts, temp);
		imp = IJ.openImage(path3DGrids + "ground truth reconstruction.tif");
		temp = ImageUtil.wrapImagePlus(imp);
		fullRecons.getGridOperator().copy(fullRecons, temp);
	}
	
	public Grid3D getGroundTruthData(String path, int ii, boolean isTumor){
		String pathTemp;
		ImagePlus imp;
		Grid3D gtImages = new Grid3D(256, 256, 256);
		Grid3D imgTemp;
		pathTemp = path + ii + ".tif";
		imp=IJ.openImage(pathTemp);
		imgTemp = ImageUtil.wrapImagePlus(imp);
		for(int j = 0; j < gtImages.getSize()[2]; j ++)
		{
				gtImages.setSubGrid(j, downSampling(imgTemp.getSubGrid(j)));
		}

		return gtImages;
	}
	
	public Grid3D getOriginalGroundTruthData(String path, int ii){
		String pathTemp;
		ImagePlus imp;
	
		pathTemp = path + ii + ".tif";
		imp=IJ.openImage(pathTemp);
		Grid3D imgTemp = ImageUtil.wrapImagePlus(imp);
		
		
		return imgTemp;
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
	

	
	public void rescaleData(OpenCLGrid3D volCL){
		volCL.getGridOperator().subtractBy(volCL, 30);
		volCL.getGridOperator().divideBy(volCL, 2040);
		volCL.getGridOperator().multiplyBy(volCL, 0.07f);
		volCL.getGridOperator().removeNegative(volCL);
	}
	
	
	
	public void saveTrainingData(String path, Grid3D reconGT, Grid3D reconLimited, Grid3D artifacts, int index){
		ImagePlus imp1,imp2, imp3;
		
//			imp1 = ImageUtil.wrapGrid(reconGT, null);
//			IJ.saveAs(imp1, "Tiff", (path + "reconGT" + index + ".tif"));
			imp2 = ImageUtil.wrapGrid(reconLimited, null);
			IJ.saveAs(imp2, "Tiff", (path + "reconTruncated" + index + ".tif"));
//			imp3 = ImageUtil.wrapGrid(artifacts, null);
//			IJ.saveAs(imp3, "Tiff", (path + "artifacts" + index + ".tif"));
		
		
	}
	
	public void saveFullReconData(String path, Grid3D reconFull, int index){
		ImagePlus imp1;
		
			imp1 = ImageUtil.wrapGrid(reconFull, null);
			IJ.saveAs(imp1, "Tiff", (path + "reconFull" + index + ".tif"));
	}
}
