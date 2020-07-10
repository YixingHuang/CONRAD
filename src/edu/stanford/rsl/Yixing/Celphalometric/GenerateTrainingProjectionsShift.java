package edu.stanford.rsl.Yixing.Celphalometric;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.OvalRoi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.NumericPointwiseOperators;
import edu.stanford.rsl.conrad.data.numeric.opencl.OpenCLGrid2D;
import edu.stanford.rsl.conrad.data.numeric.opencl.OpenCLGrid3D;
import edu.stanford.rsl.conrad.geometry.Projection;
import edu.stanford.rsl.conrad.geometry.shapes.simple.PointND;
import edu.stanford.rsl.conrad.geometry.trajectories.Trajectory;
import edu.stanford.rsl.conrad.utils.Configuration;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import edu.stanford.rsl.tutorial.cone.ConeBeamBackprojector;
import edu.stanford.rsl.tutorial.cone.ConeBeamProjector;

public class GenerateTrainingProjectionsShift {
	protected int maxProjs;
	public int imgSizeX;
	public int imgSizeY;
	public int imgSizeZ;
	protected Configuration conf;
	protected Trajectory geo = null;
	protected int width;
	protected int height;
	protected double spacingX;
	protected double spacingY;
	protected double spacingZ;
	protected double detSpacingX, detSpacingY;
	protected double originX;
	protected double originY;
	protected double originZ;
	protected PointND origin;
	public ConeBeamProjector cbp;
//	public ConeBeamBackprojector cbbp;
	
	public OpenCLGrid3D sinoCL, volCL;
	public OpenCLGrid2D sinoCL2D;
	public Grid3D sinogram;

	
	public static void main(String[] args) throws Exception{
		new ImageJ();
		SpacingDataNature sp = new SpacingDataNature();
		GenerateTrainingProjectionsShift obj = new GenerateTrainingProjectionsShift();
		String inputPath = "E:\\CQ500CTData\\UpperHalf\\";
//		String inputPath = "E:\\CQ500CTData\\LowerHalf\\";
		String outputPath = "D:\\Tasks\\FAU4\\Cephalometric\\coneBeamProjectionsLower\\";
		String name, saveName;
		float s = 0.00002f;
		obj.initialGeometry();
		
		int num = sp.getNumberOfCases();
		int idx;
		String idxS;
		ImagePlus imp;
		double heightZ;
		double z0 = 220;
		int deltaZNum = 0;
		Grid3D vol2;
		for(int i = 0; i < num; i++) {
			idx = (int)sp.getAt(i, 0);
			idxS = String.format("%03d", idx);
			name = inputPath + "data" + idxS + ".tif";
			File outPutDir=new File(name);
			if (!outPutDir.exists())
				continue;
		    System.out.println("idx = " + idx + " ");
			imp = IJ.openImage(name);
			Grid3D vol = ImageUtil.wrapImagePlus(imp);
			obj.thresholdVol(vol, 900, 3600);
//			obj.thresholdVol(vol, 0, 3600);
			vol.getGridOperator().multiplyBy(vol, s);
			obj.spacingX = sp.getAt(i, 1);
			obj.spacingY = sp.getAt(i, 2);
			obj.spacingZ = sp.getAt(i, 3);
			heightZ = vol.getSize()[2] * obj.spacingZ;
			if(heightZ < 0.5 * z0) {
				continue;
			}
			else if(heightZ < z0) {
				deltaZNum = Math.round((float)((z0 - heightZ) / obj.spacingZ));
				obj.imgSizeZ = vol.getSize()[2] + deltaZNum;
//				vol2 = new Grid3D(vol.getSize()[0], vol.getSize()[1], obj.imgSizeZ);
//				obj.zeroPadding(vol, vol2, deltaZNum);
				vol = obj.zeroPadding2(vol, deltaZNum);	
			}
			else
			{
				obj.imgSizeZ = vol.getSize()[2];
//				vol2 = (Grid3D) vol.clone();
			}
			obj.resetGeometry();
			obj.cbp=new ConeBeamProjector();
			obj.volCL = new OpenCLGrid3D(vol);
	
			
			obj.getPerspectiveProjection();

			Grid2D p = new Grid2D(obj.sinoCL2D);
			obj.sinoCL2D.release();
			obj.volCL.release();
			p.setSpacing(obj.detSpacingX, obj.detSpacingY);
			p.setOrigin(-(p.getSize()[0] - 1.0) * p.getSpacing()[0]/2.0, -(p.getSize()[1] - 1.0) * p.getSpacing()[1]/2.0);

			Grid2D p2 = obj.fliplrud(p);
//			p2.getGridOperator().removeNegative(p2);
//			p2.show("projection" + idx);
			imp = ImageUtil.wrapGrid(p2, null);
			saveName = outputPath + idx + ".tif";
			IJ.saveAs(imp, "tiff", saveName);

		    obj.cbp.unload();
		}

	}
	private void thresholdVol(Grid3D vol, float thres1, float thres2) {
		for(int i = 0; i < vol.getSize()[0]; i++)
			for(int j = 0; j < vol.getSize()[1]; j++)
				for(int k = 0; k < vol.getSize()[2]; k++)
					if(vol.getAtIndex(i, j, k) < thres1)
						vol.setAtIndex(i, j, k, 0);
					else if(vol.getAtIndex(i, j, k) > thres2)
						vol.setAtIndex(i, j, k, thres2);
	}
	
	private void zeroPadding(Grid3D vol, Grid3D vol2, int numZ) {
		for(int i = 0; i < vol.getSize()[0]; i++)
			for(int j = 0; j < vol.getSize()[1]; j++)
				for(int k = 0; k < vol.getSize()[2]; k++)
					vol2.setAtIndex(i, j, k + numZ, vol.getAtIndex(i, j, k));
	}
	
	private Grid3D zeroPadding(Grid3D vol, int numZ) {
		Grid3D vol2 = new Grid3D(vol.getSize()[0], vol.getSize()[1], vol.getSize()[2] + numZ);
		for(int i = 0; i < vol.getSize()[0]; i++)
			for(int j = 0; j < vol.getSize()[1]; j++)
				for(int k = 0; k < vol.getSize()[2]; k++)
					vol2.setAtIndex(i, j, k + numZ, vol.getAtIndex(i, j, k));
		
		return vol2;
	}
	
	private Grid3D zeroPadding2(Grid3D vol, int numZ) {
		Grid3D vol2 = new Grid3D(vol.getSize()[0], vol.getSize()[1], vol.getSize()[2] + numZ);
		for(int i = 0; i < vol.getSize()[0]; i++)
			for(int j = 0; j < vol.getSize()[1]; j++)
				for(int k = 0; k < vol.getSize()[2]; k++)
					vol2.setAtIndex(i, j, k, vol.getAtIndex(i, j, k));
		
		return vol2;
	}
	
	public Grid2D computeGradient(Grid2D img, float thres) {
		Grid2D gradient = new Grid2D(img);
		float dx, dy, val;
		for(int i = 0; i < img.getSize()[0]; i++)
			for(int j = 0; j < img.getSize()[1]; j++)
			{
				if (i == 0)
					dx = 0;
				else
					dx = img.getAtIndex(i, j) - img.getAtIndex(i - 1, j);
				if(j == 0)
					dy = 0;
				else
					dy = img.getAtIndex(i, j) - img.getAtIndex(i, j-1);
				val = (float)Math.sqrt(dx * dx + dy * dy);
				if(val > thres)
					gradient.setAtIndex(i, j, 1);
				else
					gradient.setAtIndex(i, j, 0);
			}
		return gradient;
	}
	
	public void initialGeometry() throws Exception {
		Configuration.loadConfiguration();
		this.conf = Configuration.getGlobalConfiguration();
		geo = conf.getGeometry();
		
		width = geo.getDetectorWidth();
		height = geo.getDetectorHeight();
		detSpacingX = geo.getPixelDimensionX();
		detSpacingY = geo.getPixelDimensionY();
		
		// create context
		maxProjs = geo.getProjectionStackSize();
		imgSizeX = geo.getReconDimensionX();
		imgSizeY = geo.getReconDimensionY();
		imgSizeZ = geo.getReconDimensionZ();
		spacingX = geo.getVoxelSpacingX();
		spacingY = geo.getVoxelSpacingY();
		spacingZ = geo.getVoxelSpacingZ();
		originX = geo.getOriginX();
		originY = geo.getOriginY();
		originZ = geo.getOriginZ();
	

//		System.out.println(detSpacingX + " " + detSpacingY);
//		System.out.println(spacingX+ " " + spacingY + " " + spacingZ);
//		System.out.println(originX+ " " + originY + " " + originZ);
//		System.out.println(geo.getDetectorOffsetU()+ " " + geo.getDetectorOffsetV());
//		System.out.println(" ");
	}
	
	public void resetGeometry() {
		geo.setReconDimensionZ(this.imgSizeZ);
		geo.setVoxelSpacingX(this.spacingX);
		geo.setVoxelSpacingY(this.spacingY);
		geo.setVoxelSpacingZ(this.spacingZ);
		this.originX = -(this.imgSizeX - 1)/2.0 * this.spacingX;
		this.originY = -(this.imgSizeY - 1)/2.0 * this.spacingY;
		this.originZ = -(this.imgSizeZ - 1)/2.0 * this.spacingZ;
		this.origin = new PointND(this.originX, this.originY, this.originZ);
		geo.setOriginInPixelsZ((this.imgSizeZ - 1)/2.0);
		geo.setOriginInWorld(this.origin);
//		geo.prepareForSerialization();
//		this.conf.setGeometry(geo);
//		Configuration.setGlobalConfiguration(conf);

	}
	
	public void getPerspectiveProjection() throws Exception {
		volCL.getDelegate().prepareForDeviceOperation();
		sinoCL2D = new OpenCLGrid2D(new Grid2D(width, height));
		sinoCL2D.getDelegate().prepareForDeviceOperation();
		cbp.projectRayDrivenCL2(sinoCL2D, volCL, 0);
		sinoCL2D.getDelegate().notifyDeviceChange();;
	}
	
	
	public void segmentRegions(Grid3D img, Grid3D bone, Grid3D soft, Grid3D air, float thresBone, float thresAir)
	{
		float val;
		for(int i = 0; i < img.getSize()[0]; i ++)
			for(int j = 0; j < img.getSize()[1]; j++)
				for(int k = 0; k < img.getSize()[2]; k++) {
					val = img.getAtIndex(i, j, k);
					if(val >= thresBone)
						bone.setAtIndex(i, j, k, val);
					else if(val > thresAir)
						soft.setAtIndex(i, j, k, val);
					else
						air.setAtIndex(i, j, k, 1);
				}
	}
	
	public Grid3D combineBoneAndSoft(Grid3D bone, Grid3D soft, float s)
	{
		Grid3D vol = new Grid3D(soft);
		for(int i = 0; i < soft.getSize()[0]; i++)
			for(int j = 0; j < soft.getSize()[1]; j++)
				for(int k = 0; k < soft.getSize()[2]; k++)
					vol.setAtIndex(i, j, k, soft.getAtIndex(i, j, k) + s * bone.getAtIndex(i, j, k));
		
		return vol;
	}
	
	public Grid2D getBackgroundMask(Grid2D img, float thres)
	{
		Grid2D mask = new Grid2D(img.getWidth(), img.getHeight());
		for(int i = 0; i < img.getWidth(); i++)
			for(int j = 0; j < img.getHeight(); j++) {
				if(img.getAtIndex(i, j) > thres)
					mask.setAtIndex(i, j, 1.0f);
			}
		return mask;
	}
	
	
	/**
	 * y = 1/(1 + a * exp(-x + t)
	 * @param img
	 * @param t  horizontal shift
	 * @param a  scale
	 */
	public void sigmoidTransform(Grid2D img, float t, float a)
	{
		float val;
		for(int i = 0; i < img.getSize()[0]; i++)
			for(int j = 0; j < img.getSize()[1]; j++) {
				val = (float)(1.0 / (1.0 + Math.exp(- a * ((double) img.getAtIndex(i, j) - t))));
				img.setAtIndex(i, j, val);
			}
	}
	
	/**
	 * return a new image
	 * @param img
	 * @param t
	 * @param a
	 * @return
	 */
	public Grid2D sigmoidTransform2(Grid2D img, float t, float a)
	{
		float val;
		Grid2D img2 = new Grid2D(img.getWidth(), img.getHeight());
		for(int i = 0; i < img.getSize()[0]; i++)
			for(int j = 0; j < img.getSize()[1]; j++) {
				val = (float)(215.0 / (1.0 + Math.exp(- a * ((double) img.getAtIndex(i, j) - t))) + 20);
				img2.setAtIndex(i, j, val);
			}
		
		return img2;
	}
	
	public Grid2D projection(Grid3D vol) {
		int[] size = vol.getSize();
		Grid2D p = new Grid2D(size[1], size[2]);
		p.setSpacing(vol.getSpacing()[1], vol.getSpacing()[2]);
		p.setOrigin(-(p.getSize()[0] - 1.0) * p.getSpacing()[0]/2.0, -(p.getSize()[1] - 1.0) * p.getSpacing()[1]/2.0);
		float val = 0;
		for(int i = 0; i < size[1]; i++)
			for(int j = 0; j < size[2]; j++) {
				val = 0;
				for(int k = 0; k < size[0]; k++) {
					val += vol.getAtIndex(k, i, j);
				}
				p.setAtIndex(i, j, (float)(val * vol.getSpacing()[0]));
			}
		
		return p;
	}
	
	public Grid2D flipud(Grid2D img) {
		Grid2D img2 = new Grid2D(img.getSize()[0], img.getSize()[1]);
		img2.setSpacing(img.getSpacing());
		img2.setOrigin(img.getOrigin());
		for(int i = 0; i < img.getSize()[0]; i++)
			for(int j = 0; j < img.getSize()[1]; j++)
				img2.setAtIndex(i, j, img.getAtIndex(i, img.getSize()[1] -1 - j));
		
		return img2;
	}
	
	public Grid2D fliplrud(Grid2D img) {
		Grid2D img2 = new Grid2D(img.getSize()[0], img.getSize()[1]);
		img2.setSpacing(img.getSpacing());
		img2.setOrigin(img.getOrigin());
		for(int i = 0; i < img.getSize()[0]; i++)
			for(int j = 0; j < img.getSize()[1]; j++)
				img2.setAtIndex(i, j, img.getAtIndex(img.getSize()[0] - 1 - i, img.getSize()[1] - 1 - j));
		
		return img2;
	}
	
	public float gridGetAtPhysical(Grid2D img, double x, double y)
	{
		double[] origin = img.getOrigin();
		double idx = (x - origin[0])/img.getSpacing()[0];
		double idy = (y - origin[1])/img.getSpacing()[1];
		int x_floor = (int) Math.floor(idx);
		int x_ceiling = x_floor + 1;
		int y_floor = (int) Math.floor(idy);
		int y_ceiling = y_floor + 1;
		if (x_floor < 0 || x_floor > img.getSize()[0] - 1)
			return 0;
		else if(x_ceiling < 0 || x_ceiling > img.getSize()[0] - 1)
			return 0;
		else if(y_floor < 0 || y_floor > img.getSize()[1] - 1)
			return 0;
		else if(y_ceiling < 0 || y_ceiling > img.getSize()[1] - 1)
			return 0;
		else
		{
			float val = 0;
			float x_res = (float)(idx - x_floor);
			float y_res = (float)(idy - y_floor);
			val = img.getAtIndex(x_floor, y_floor) * (1 - x_res) * (1 - y_res)
					+ img.getAtIndex(x_ceiling, y_floor) * x_res * (1 - y_res)
					+ img.getAtIndex(x_floor, y_ceiling) * (1 - x_res) * y_res
					+ img.getAtIndex(x_ceiling, y_ceiling) * x_res * y_res;
			return val;
		}
	}
	
	public double[] index2physical(Grid2D img, int i, int j)
	{
		double [] idxy = new double[2];
	    if(img.getOrigin()[0] == 0)
	    {
	    	img.setOrigin(-(img.getSize()[0] - 1.0) * img.getSpacing()[0]/2.0, -(img.getSize()[1] - 1.0) * img.getSpacing()[1]/2.0);
	    }
	        idxy[0] = img.getSpacing()[0] * i + img.getOrigin()[0];
	        idxy[1] = img.getSpacing()[1] * j + img.getOrigin()[1];
	        
	        return idxy;
	}
	
	public void resampleCelp(Grid2D celp, Grid2D input) {
		double[] idxy;
		for(int i = 0; i < celp.getSize()[0]; i++)
			for(int j = 0; j < celp.getSize()[1]; j++)
			{
				idxy = index2physical(celp, i, j);
				celp.setAtIndex(i, j, gridGetAtPhysical(input, idxy[0], idxy[1]));
			}
	}
	
	public void enhanceBones(Grid3D vol, float thres, float factor)
	{
		for(int i = 0; i < vol.getSize()[0]; i++)
			for(int j = 0; j < vol.getSize()[1]; j++)
				for(int k = 0; k < vol.getSize()[2]; k++)
				{
					if(vol.getAtIndex(i, j, k) > thres)
						vol.setAtIndex(i, j, k, vol.getAtIndex(i, j, k) * factor);
				}
	}
	
	public void enhanceBonesAndAir(Grid3D vol, float thresBone, float factor, float thresAir, float vair)
	{
		for(int i = 0; i < vol.getSize()[0]; i++)
			for(int j = 0; j < vol.getSize()[1]; j++)
				for(int k = 0; k < vol.getSize()[2]; k++)
				{
					if(vol.getAtIndex(i, j, k) > thresBone)
						vol.setAtIndex(i, j, k, vol.getAtIndex(i, j, k) * factor);
					else if(vol.getAtIndex(i, j, k) < thresAir)
						vol.setAtIndex(i, j, k, vair);
				}
	}
}
