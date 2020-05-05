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
import edu.stanford.rsl.conrad.utils.ImageUtil;

public class GenerateCelphaloFromCT {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		double [] spacingAll = new double[]{0.4883,0.4883,0.6250, 
				0.5469, 0.5469,0.6250, 
				0.4883, 0.4883, 0.6250, 
				0.4707, 0.4707, 0.6250, 
				0.4980, 0.4980, 0.6250};
		GenerateCelphaloFromCT obj = new GenerateCelphaloFromCT();
		String inputPath = "E:\\CQ500CTData\\completeHeadData\\";
		String outputPath = inputPath;
		String name;

		for(int idx = 0; idx < 5; idx++) {
			name = inputPath + idx + "p2.tif";
			ImagePlus imp = IJ.openImage(name);
			Grid3D vol = ImageUtil.wrapImagePlus(imp);
			vol.setSpacing(spacingAll[idx * 3], spacingAll[idx * 3 + 1], spacingAll[idx * 3 + 2]); 
//			obj.enhanceBones(vol, 1200, 3.0f);
//			obj.enhanceBonesAndAir(vol, 1200, 2.0f, 100, -500);
			vol.getGridOperator().multiplyBy(vol, 0.00002f);
//			vol.show("vol");
			Grid2D p = obj.projection(vol);
			Grid2D p2 = obj.fliplrud(p);
//			p2.getGridOperator().removeNegative(p2);
			p2.show("projection" + idx);
			
			Grid2D celp = new Grid2D(512, 512);
			celp.setSpacing(0.5, 0.5);
			celp.setOrigin(-(celp.getSize()[0] - 1.0) * celp.getSpacing()[0]/2.0, -(celp.getSize()[1] - 1.0) * celp.getSpacing()[1]/2.0);
			obj.resampleCelp(celp, p2);
			celp.show("celp" + idx);
			imp = ImageUtil.wrapGrid(celp, null);
			name = outputPath + "celp" + idx + ".tif";
		    IJ.saveAs(imp, "Tiff", name);
		    System.out.print(idx + " ");
		}
//		Grid2D celp2 = new Grid2D(1024, 1024);
//		celp2.setSpacing(0.25, 0.25);
//		celp2.setOrigin(-(celp2.getSize()[0] - 1.0) * celp2.getSpacing()[0]/2.0, -(celp2.getSize()[1] - 1.0) * celp2.getSpacing()[1]/2.0);
//		obj.resampleCelp(celp2, p2);
//		celp2.show("celp2");
//		
//		Grid2D celp3 = new Grid2D(2048, 2048);
//		celp3.setSpacing(0.125, 0.125);
//		celp3.setOrigin(-(celp3.getSize()[0] - 1.0) * celp3.getSpacing()[0]/2.0, -(celp3.getSize()[1] - 1.0) * celp3.getSpacing()[1]/2.0);
//		obj.resampleCelp(celp3, p2);
//		celp3.show("celp3");
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
				p.setAtIndex(i, j, val);
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
