package edu.stanford.rsl.Yixing.truncationClinic.clinicalReconstruction;

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

public class CombineProjectionsScale {
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
		boolean isPix2pix = true;
//
//		String unetRecon = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\UNetRecons\\";
//		String savePath = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\reprojections\\";
//		String sinoPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\projections\\";
		
		String unetRecon = "D:\\Tasks\\FAU4\\TruncationCorrection\\clinical\\clinicRecons\\";
		String savePath = "D:\\Tasks\\FAU4\\TruncationCorrection\\clinical\\clinicProjectionComb\\";
		String sinoPath = "D:\\Tasks\\FAU4\\TruncationCorrection\\clinical\\clinicProjectionPWLS\\";
		String saveName1;
		CombineProjectionsScale obj = new CombineProjectionsScale(); 
		obj.initialGeometry();
	
		
		//Grid3D img = obj.getOriginalGroundTruthData(path, 7);
		//img.clone().show("img");
		
		ImagePlus imp1, imp2;
	
		int numTrunc = 320;
		TVOpenCLGridOperators op = TVOpenCLGridOperators.getInstance();
		WaterCylinderExtrapolation2DFan wceObj = new WaterCylinderExtrapolation2DFan(obj.height, (int)numTrunc);
		Grid2D tempSino;
		String pathTemp;
		Grid3D imgTemp;
		for(int i = 6; i <= 6; i++){
			obj.cbp=new ConeBeamProjector();
			obj.cbbp=new ConeBeamBackprojector();
			pathTemp = sinoPath + "projection1.tif";
			imp2 = IJ.openImage(pathTemp);
			imgTemp = ImageUtil.wrapImagePlus(imp2);
			obj.sinoCL = new OpenCLGrid3D(imgTemp);
			obj.sinoCL.getDelegate().prepareForDeviceOperation();
			
		
			pathTemp = unetRecon + "pix2pix512_4.tif";

			imp2 = IJ.openImage(pathTemp);
			imgTemp = ImageUtil.wrapImagePlus(imp2);
			obj.volCL = new OpenCLGrid3D(imgTemp);
			obj.volCL.getGridOperator().multiplyBy(obj.volCL, 0.07f);
			obj.volCL.getGridOperator().softThresholding(obj.volCL, 0);

			obj.getMeasuredSinoCL2();


			op.combineProjectionsScale(obj.sinoCL, obj.sinoCL2, numTrunc);

			obj.sinogram = new Grid3D(obj.sinoCL);
			obj.sinoCL.release();
			obj.sinoCL2.release();
			obj.volCL.release();
			imp1 = ImageUtil.wrapGrid(obj.sinogram, null);
			saveName1 = savePath + "projection" +i + ".tif";
		    IJ.saveAs(imp1, "Tiff", saveName1);
			System.out.println(i);
			
		}
    }
	
	private void addTumors(Grid3D gtImages){
		float tumor = 0.1f * 0.07f;
		int xcent = 230, ycent = 95;
		for(int i = -5; i <=5; i++){
			for(int j = -5; j <= 5; j ++){
				for(int k = 0; k < gtImages.getSize()[2]; k++){
					if((i-0.5)*(i-0.5)+(j-0.5)*(j-0.5)<= 25)
						gtImages.setAtIndex(xcent + i , ycent + j, k, gtImages.getAtIndex(xcent+i, ycent+j, k) + tumor);
				}
			}
		}
	}
	
	private void addTumors2(Grid3D gtImages){
		float tumor = 200.f;
		int xcent = 230, ycent = 95;
		for(int i = -5; i <=5; i++){
			for(int j = -5; j <= 5; j ++){
				for(int k = 0; k < gtImages.getSize()[2]; k++){
					if((i-0.5)*(i-0.5)+(j-0.5)*(j-0.5)<= 25)
						gtImages.setAtIndex(xcent + i , ycent + j, k, gtImages.getAtIndex(xcent+i, ycent+j, k) + tumor);
				}
			}
		}
	}
	
	private void addPoissonNoise3D(Grid3D sinogram) throws Exception{
		for(int i = 0; i < sinogram.getSize()[2]; i++){
			addPoissonNoise(sinogram.getSubGrid(i));
		}
	}
	
	private void addPoissonNoise(Grid2D sinogram) throws Exception{
		//Grid2D noise = new Grid2D(sinogram);

		float photonNumber = 1.e5f;
		double val;
		float amp = 2.f;//transfer the intensity to linear attenuation coefficient, water 0.02/mm, pixel size 0.5mm
		sinogram.getGridOperator().divideBy(sinogram, amp);
		Grid2D I = new Grid2D(sinogram.getWidth(), sinogram.getHeight());
		for (int i = 0; i < sinogram.getWidth(); i ++)
			for(int j = 0; j < sinogram.getHeight(); j++)
			{
				val = photonNumber * Math.pow(Math.E, -sinogram.getAtIndex(i, j));
				I.setAtIndex(i, j, (float)(val));
			}
		
		PoissonNoiseFilteringTool poisson = new PoissonNoiseFilteringTool();
		poisson.applyToolToImage(I);
		for (int i = 0; i < sinogram.getWidth(); i ++)
			for(int j = 0; j < sinogram.getHeight(); j++)
			{
				val = - Math.log(I.getAtIndex(i, j)/photonNumber);
				sinogram.setAtIndex(i, j, (float)(val>=0?val:0));
			
			}
		sinogram.getGridOperator().multiplyBy(sinogram, amp);
		
		//noise.getGridOperator().subtractBy(noise, sinogram);
		//noise.getGridOperator().multiplyBy(noise, -1);
		//noise.show("poisson noise");
		
	}
	
	public void initialGeometry() throws Exception {
		Configuration.loadConfiguration();
		Configuration conf = Configuration.getGlobalConfiguration();
		geo = conf.getGeometry();
//		getProjectionMatrix();
		
		
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
		
		System.out.println(geo.getNumProjectionMatrices());
		SimpleVector spacingUV = new SimpleVector(geo.getPixelDimensionX(), geo.getPixelDimensionY());
		double[] sdd = geo.getProjectionMatrices()[0].computeSourceToDetectorDistance(spacingUV);
		geo.setSourceToDetectorDistance(sdd[0]);
		SimpleVector sp = geo.getProjectionMatrices()[0].computeCameraCenter();
		double sid = sp.normL2();
		geo.setSourceToAxisDistance(sid);
		
//		radius=(float)(radius*0.4/spacingX);
		
	}
	public void FDKReconstruction(Grid3D sinogram){
		double focalLength = geo.getSourceToDetectorDistance();
		double deltaU = geo.getPixelDimensionX();
		double deltaV = geo.getPixelDimensionY();
		int numProjs = geo.getNumProjectionMatrices();	
		Grid3D sinogram2=new Grid3D(sinogram.getSize()[0],sinogram.getSize()[1], numProjs);
		ConeBeamCosineFilter cbFilter = new ConeBeamCosineFilter(focalLength, width, height, deltaU, deltaV);
		RamLakKernel ramK = new RamLakKernel(width, deltaU);
		//ParkerWeightingTool parker = new ParkerWeightingTool(geo);
		for (int i = 0; i < numProjs; ++i) 
			
		{
			//parker.setImageIndex(i);
			//parker.applyToolToImage(sinogram2.getSubGrid(i));
		
			sinogram2.setSubGrid(i, (Grid2D) sinogram.getSubGrid(i).clone());
			cbFilter.applyToGrid(sinogram2.getSubGrid(i));
			//ramp
			for (int j = 0;j <height; ++j)
				ramK.applyToGrid(sinogram2.getSubGrid(i).getSubGrid(j));
			System.out.print(i + " ");
		}
		System.out.println(" ");
		
		Grid3D reconFDK = cbbp.backprojectPixelDrivenCL(sinogram2);
		reconCL= new OpenCLGrid3D(reconFDK);
		//float scalCorrection = (float)( 260/(34.5*720000));
		//reconCL.getGridOperator().multiplyBy(reconCL, scalCorrection);
		//reconFDK = new Grid3D(reconCL);
		//reconFDK.show("FDK reconstruction");	
		
	}
	
	public void getMeasuredSinoCL() throws Exception {
		sinoCL = new OpenCLGrid3D(new Grid3D(width, height, maxProjs));
		sinoCL.getDelegate().prepareForDeviceOperation();
		cbp.fastProjectRayDrivenCL(sinoCL, volCL);
		//sinoCL.show("sinoCL");
	}
	
	public void getMeasuredSinoCL2() throws Exception {
		sinoCL2 = new OpenCLGrid3D(new Grid3D(width, height, maxProjs));
		sinoCL2.getDelegate().prepareForDeviceOperation();
		cbp.fastProjectRayDrivenCL(sinoCL2, volCL);
		//sinoCL.show("sinoCL");
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
		if(isTumor)
			addTumors2(imgTemp);
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
		
			imp1 = ImageUtil.wrapGrid(reconGT, null);
			IJ.saveAs(imp1, "Tiff", (path + "reconGT" + index + ".tif"));
			imp2 = ImageUtil.wrapGrid(reconLimited, null);
			IJ.saveAs(imp2, "Tiff", (path + "reconLimited" + index + ".tif"));
			imp3 = ImageUtil.wrapGrid(artifacts, null);
			IJ.saveAs(imp3, "Tiff", (path + "artifacts" + index + ".tif"));
		
		
	}
	
	public void saveFullReconData(String path, Grid3D reconFull, int index){
		ImagePlus imp1;
		
			imp1 = ImageUtil.wrapGrid(reconFull, null);
			IJ.saveAs(imp1, "Tiff", (path + "reconFull" + index + ".tif"));
	}
}
