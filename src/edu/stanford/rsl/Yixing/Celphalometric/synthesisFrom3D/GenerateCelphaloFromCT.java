package edu.stanford.rsl.Yixing.Celphalometric.synthesisFrom3D;

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
		float s = 0.00002f;

		for(int idx = 0; idx <= 0; idx++) {
			name = inputPath + idx + "p2.tif";
			ImagePlus imp = IJ.openImage(name);
			Grid3D vol = ImageUtil.wrapImagePlus(imp);
			vol.setSpacing(spacingAll[idx * 3], spacingAll[idx * 3 + 1], spacingAll[idx * 3 + 2]); 
			Grid3D bone = new Grid3D(vol.getSize()[0], vol.getSize()[1], vol.getSize()[2]);
			Grid3D soft = new Grid3D(vol.getSize()[0], vol.getSize()[1], vol.getSize()[2]);
			Grid3D air = new Grid3D(vol.getSize()[0], vol.getSize()[1], vol.getSize()[2]);
			bone.setSpacing(spacingAll[idx * 3], spacingAll[idx * 3 + 1], spacingAll[idx * 3 + 2]); 
			soft.setSpacing(spacingAll[idx * 3], spacingAll[idx * 3 + 1], spacingAll[idx * 3 + 2]); 
			air.setSpacing(spacingAll[idx * 3], spacingAll[idx * 3 + 1], spacingAll[idx * 3 + 2]); 
//			obj.enhanceBones(vol, 1200, 3.0f);
//			obj.enhanceBonesAndAir(vol, 1200, 2.0f, 100, -500);
			obj.segmentRegions(vol, bone, soft, air, 1500, 400);
			vol.getGridOperator().multiplyBy(vol, s);
			bone.getGridOperator().multiplyBy(bone, s);
			soft.getGridOperator().multiplyBy(soft, s);
//			vol.show("vol");
			
			Grid2D p = obj.projection(vol);
			Grid2D p2 = obj.fliplrud(p);
//			p2.getGridOperator().removeNegative(p2);
			p2.show("projection" + idx);
//			
			Grid2D celp = new Grid2D(512, 512);
			celp.setSpacing(0.5, 0.5);
			celp.setOrigin(-(celp.getSize()[0] - 1.0) * celp.getSpacing()[0]/2.0, -(celp.getSize()[1] - 1.0) * celp.getSpacing()[1]/2.0);
			obj.resampleCelp(celp, p2);
			Grid2D mask = obj.getBackgroundMask(celp, 0.08f);
			mask.show("mask");
			celp.clone().show("celp" + idx);
//			Grid2D celp2 = obj.sigmoidTransform2(celp, 2.68f, 1);
//			celp2.clone().show("celpTahn2_" + idx);
//			celp2.getGridOperator().multiplyBy(celp2, mask);
//			celp2.clone().show("afterMask" + idx);
//			Grid2D celp3 = obj.sigmoidTransform2(celp, 2.68f, 1.5f);
//			celp3.clone().show("celpTahn3_" + idx);
//			celp3.getGridOperator().multiplyBy(celp3, mask);
//			celp3.clone().show("afterMask2" + idx);
////			imp = ImageUtil.wrapGrid(celp, null);
////			name = outputPath + "celp" + idx + ".tif";
////		    IJ.saveAs(imp, "Tiff", name);
//			
//			Grid2D pBone = obj.projection(bone);
//			Grid2D pBone2 = obj.fliplrud(pBone);
//			pBone2.show("projection bone" + idx);
//			
//			Grid2D celpBone = new Grid2D(512, 512);
//			celpBone.setSpacing(0.5, 0.5);
//			celpBone.setOrigin(-(celp.getSize()[0] - 1.0) * celp.getSpacing()[0]/2.0, -(celp.getSize()[1] - 1.0) * celp.getSpacing()[1]/2.0);
//			obj.resampleCelp(celpBone, pBone2);
//			celpBone.clone().show("celpBone");
//			
//			Grid2D pSoft = obj.projection(soft);
//			Grid2D pSoft2 = obj.fliplrud(pSoft);
//			pSoft2.show("projection soft" + idx);
//			
//			Grid2D celpSoft = new Grid2D(512, 512);
//			celpSoft.setSpacing(0.5, 0.5);
//			celpSoft.setOrigin(-(celp.getSize()[0] - 1.0) * celp.getSpacing()[0]/2.0, -(celp.getSize()[1] - 1.0) * celp.getSpacing()[1]/2.0);
//			obj.resampleCelp(celpSoft, pSoft2);
//			celpSoft.clone().show("celpSoft");
			
			vol = obj.combineBoneAndSoft(bone, soft, 1.4f);
			Grid2D pCombine = obj.projection(vol);
			Grid2D pCombine2 = obj.fliplrud(pCombine);
			pCombine2.show("projection combine" + idx);
			Grid2D celpCombine = new Grid2D(512, 512);
			celpCombine.setSpacing(0.5, 0.5);
			celpCombine.setOrigin(-(celp.getSize()[0] - 1.0) * celp.getSpacing()[0]/2.0, -(celp.getSize()[1] - 1.0) * celp.getSpacing()[1]/2.0);
			obj.resampleCelp(celpCombine, pCombine2);
			celpCombine.clone().show("celp combine");
			//celpCombine.getGridOperator().divideBy(celpCombine, 1.1f);
			Grid2D celpCombine3 = obj.sigmoidTransform2(celpCombine, 3.f, 1.5f);
			celpCombine3.clone().show("celpTahn3_" + idx);
			celpCombine3.getGridOperator().multiplyBy(celpCombine3, mask);
			celpCombine3.clone().show("afterMask2" + idx);
			
			Grid2D gradient = obj.computeGradient(mask, 0.5f);
			gradient.clone().show("original gradient");
			for(int i = 444; i <= 452; i++)
				for(int j = 183; j <=188; j++)
					gradient.setAtIndex(i, j, 0);
			
			for(int i = 55; i <=63; i++)
				for(int j = 434; j <= 454; j++)
					gradient.setAtIndex(i, j, 0);
			gradient.show("gradient");
			Grid3D celpRGB = new Grid3D(celpCombine3.getWidth(), celpCombine3.getHeight(), 3);
			for(int i = 0; i < 3; i++)
				celpRGB.setSubGrid(i, (Grid2D)celpCombine3.clone());
			for(int i = 0; i < gradient.getWidth(); i++)
				for(int j = 0; j < gradient.getHeight(); j++) {
					if(gradient.getAtIndex(i, j) > 0)
					{
						celpRGB.setAtIndex(i, j, 0, 0);
						celpRGB.setAtIndex(i, j, 1, 0);
						celpRGB.setAtIndex(i, j, 2, 255);
					}
				}
			celpRGB.clone().show("RGB");
			
			
		    System.out.print(idx + " ");
		}

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
	//original paramters 215 and 20
	public Grid2D sigmoidTransform2(Grid2D img, float t, float a)
	{
		float val;
		Grid2D img2 = new Grid2D(img.getWidth(), img.getHeight());
		for(int i = 0; i < img.getSize()[0]; i++)
			for(int j = 0; j < img.getSize()[1]; j++) {
//				val = (float)(215.0 / (1.0 + Math.exp(- a * ((double) img.getAtIndex(i, j) - t))) + 20);
				val = (float)(210.0 / (1.0 + Math.exp(- a * ((double) img.getAtIndex(i, j) - t))) + 40);
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
