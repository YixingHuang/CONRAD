package edu.stanford.rsl.tutorial.differentiatebackprojection;

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
import edu.stanford.rsl.conrad.geometry.Projection;
import edu.stanford.rsl.conrad.numerics.SimpleVector;

public class ConeBeamReconExample {
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
	public ConeBeamBackprojector cbbp, cbbp2;
	 private dbpOperators dbpOp;
	public OpenCLGrid3D sinoCL, volCL, reconCL, artifactCL;
	public Grid3D sinogram;
	
	public static void main(String[] args) throws Exception {
		new ImageJ();
		
		String path = "D:\\wTVprocessedData\\"; //path for wTV data
		String pathRecon = "D:\\Tasks\\FAU4\\SparseViewCT\\Noisy3D\\recon\\";
		String savePath = "D:\\Tasks\\FAU4\\SparseViewCT\\Noisy3D\\projections\\";
		String saveName1;
		ConeBeamReconExample obj = new ConeBeamReconExample(); 
		obj.initialGeometry();
		obj.dbpOp = new dbpOperators(obj.imgSizeX, obj.imgSizeY, obj.spacingX, obj.spacingY);

//		ImagePlus imp1, imp2;
//		boolean isTumor = false;
//		boolean isNoisy = false;
//		TVOpenCLGridOperators op = TVOpenCLGridOperators.getInstance();
//		Grid3D phan = new NumericalSheppLogan3D(obj.imgSizeX, obj.imgSizeY, obj.imgSizeZ).getNumericalSheppLoganPhantom();
//		Grid2D tempSino;
//		for(int i = 1; i <= 1; i++){
//		//int i = 1;
//			obj.cbp=new ConeBeamProjector();
//			obj.cbbp=new ConeBeamBackprojector();
//			obj.cbbp2=new ConeBeamBackprojector();
//			obj.volCL = new OpenCLGrid3D(obj.getGroundTruthData(path, i, isTumor));
//			obj.rescaleData(obj.volCL);
//			obj.volCL.clone().show();
////			obj.volCL = new OpenCLGrid3D(phan);
//			obj.volCL.setSpacing(1.25, 1.25, 1);
//			obj.volCL.setOrigin(obj.geo.getOriginX(), obj.geo.getOriginY(), obj.geo.getOriginZ());
//			
//			obj.getMeasuredSinoCL();
//
//			obj.sinogram = new Grid3D(obj.sinoCL);
//			obj.sinoCL.release();
////			for(int theta = 0; theta < obj.maxProjs; theta ++)
////			{
////				obj.sinogram.setSubGrid(theta, dbpOp.differentiatedProjection2D(obj.sinogram.getSubGrid(theta)));
////			}
//			//obj.sinogram.clone().show("sinogram");
//		    
//			if(isNoisy)
//				obj.addPoissonNoise3D(obj.sinogram);
//			
////			imp1 = ImageUtil.wrapGrid(obj.sinogram, null);
////			saveName1 = savePath + "projection" +i + ".tif";
////		    IJ.saveAs(imp1, "Tiff", saveName1);
////			obj.FDKReconstruction((Grid3D)obj.sinogram.clone());
//			
//			obj.DBPReconstruction((Grid3D)obj.sinogram.clone());
//			
//			obj.reconCL.show("reconCL");
////			obj.artifactCL = new OpenCLGrid3D(obj.reconCL);
////			obj.artifactCL.getGridOperator().subtractBy(obj.artifactCL, obj.volCL);
////			obj.volCL.getGridOperator().divideBy(obj.volCL, 0.07f);
////			obj.reconCL.getGridOperator().divideBy(obj.reconCL, 0.07f);
////			obj.artifactCL.getGridOperator().divideBy(obj.artifactCL, 0.07f);
////			obj.saveTrainingData(pathRecon, obj.volCL, obj.reconCL, obj.artifactCL, i);
//			//obj.saveFullReconData(pathRecon, obj.reconCL, i);
//			
//			obj.volCL.release();
//			obj.reconCL.release();
////			obj.artifactCL.release();
//			
//			System.out.println(i);
//			
//		}
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
		width = geo.getDetectorWidth();
		height = geo.getDetectorHeight();
		double[] prim = geo.getPrimaryAngles();
		Projection[] pms = geo.getProjectionMatrices();
		double angle;
		SimpleVector vec;
		for(int i = 0; i < prim.length; i++)
		{
			vec = pms[i].computePrincipalAxis();
			angle = Math.atan(vec.getElement(0)/vec.getElement(1)) * 180.0 / Math.PI;
			System.out.println(i + ": " + prim[i] + " " + vec.toString() + " " + angle);
			
		}
		System.out.println();
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
		ConeBeamCosineFilter cbFilter = new ConeBeamCosineFilter(focalLength, width, height, deltaU, deltaV);
		RamLakKernel ramK = new RamLakKernel(width, deltaU);
		ParkerWeightingTool parker = new ParkerWeightingTool(geo);
		for (int i = 0; i < numProjs; ++i) 
			
		{
			parker.setImageIndex(i);
//			sinogram2.setSubGrid(i, (Grid2D)parker.applyToolToImage(sinogram.getSubGrid(i)).clone());
			parker.applyToolToImage(sinogram.getSubGrid(i));
			cbFilter.applyToGrid(sinogram.getSubGrid(i));
			//ramp
			for (int j = 0;j <height; ++j)
				ramK.applyToGrid(sinogram.getSubGrid(i).getSubGrid(j));
			System.out.print(i + " ");
		}
		System.out.println(" ");
		

		Grid3D reconFDK = cbbp.backprojectPixelDrivenCL(sinogram);
		reconFDK.clone().show("FDK");
//		reconCL= new OpenCLGrid3D(reconFDK);

		//float scalCorrection = (float)( 260/(34.5*720000));
		//reconCL.getGridOperator().multiplyBy(reconCL, scalCorrection);
		//reconFDK = new Grid3D(reconCL);
		//reconFDK.show("FDK reconstruction");	
		
	}
	
	public void DBPReconstruction(Grid3D sinogram){
		double focalLength = geo.getSourceToDetectorDistance();
		double deltaU = geo.getPixelDimensionX();
		double deltaV = geo.getPixelDimensionY();
		int numProjs = geo.getNumProjectionMatrices();	

		ConeBeamCosineFilter cbFilter = new ConeBeamCosineFilter(focalLength, width, height, deltaU, deltaV);

		ParkerWeightingTool parker = new ParkerWeightingTool(geo);
		for (int i = 0; i < numProjs; ++i) 
		{
			cbFilter.applyToGrid(sinogram.getSubGrid(i));
			sinogram.setSubGrid(i, dbpOp.differentiatedProjection2D(sinogram.getSubGrid(i)));
			parker.setImageIndex(i);
			parker.applyToolToImage(sinogram.getSubGrid(i));

			
			System.out.print(i + " ");
		}
		System.out.println(" ");
		

		Grid3D reconFDK = cbbp2.backprojectPixelDrivenCL(sinogram);
		reconFDK.clone().show("DBP0");
		Grid2D recon2D;
//		for(int z = 0; z < reconFDK.getSize()[2]; z++)
//		{
//			recon2D = reconFDK.getSubGrid(z);
//			for(int i = 0; i < reconFDK.getSize()[1]; i ++)
//			{
//				recon2D.setSubGrid(i, dbpOp.WeightedHilbertTransform(recon2D.getSubGrid(i)));
//			}
//			recon2D.getGridOperator().multiplyBy(recon2D, -1);
//		}
		
		for(int z = 0; z < reconFDK.getSize()[2]; z++)
		{
			recon2D = reconFDK.getSubGrid(z);
//			for(int i = 0; i < reconFDK.getSize()[1]; i ++)
//			{
//				recon2D.setSubGrid(i, dbpOp.WeightedHilbertTransform(recon2D.getSubGrid(i)));
//			}
			dbpOp.weightedHilbertTranform2DVertical(recon2D);
			recon2D.getGridOperator().multiplyBy(recon2D, -1);
		}
		
		reconCL= new OpenCLGrid3D(reconFDK);

	}
	
	public void getMeasuredSinoCL() throws Exception {
		sinoCL = new OpenCLGrid3D(new Grid3D(width, height, maxProjs));
		sinoCL.getDelegate().prepareForDeviceOperation();
		cbp.fastProjectRayDrivenCL(sinoCL, volCL);
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
