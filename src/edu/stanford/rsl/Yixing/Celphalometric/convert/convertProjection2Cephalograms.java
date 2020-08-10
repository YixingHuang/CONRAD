package edu.stanford.rsl.Yixing.Celphalometric.convert;

import java.io.File;
import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class convertProjection2Cephalograms {


	
	public static void main(String[] args) throws IOException{
		new ImageJ();
//		String pathIn = "D:\\Tasks\\FAU4\\Cephalometric\\parallelProjectionsEnhancedLowerAug\\";
//		String pathOut = "D:\\Tasks\\FAU4\\Cephalometric\\parallelCepsLowerAugEnhanced\\";
		String pathIn = "D:\\Tasks\\FAU4\\Cephalometric\\parallelProjectionsEnhanced\\";
		String pathOut = "D:\\Tasks\\FAU4\\Cephalometric\\parallelCepsEnhanced\\";
		String nameIn, nameOut;
		ImagePlus imp;
		Grid2D p;
		Grid2D mask, celp;
		convertProjection2Cephalograms obj = new convertProjection2Cephalograms();
		for(int idx = 0; idx < 501; idx++) {
			nameIn = pathIn + idx + ".tif";
			File outPutDir=new File(nameIn);
			if (!outPutDir.exists())
				continue;
			imp = IJ.openImage(nameIn);
			p = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
			mask = obj.getBackgroundMask(p, 0.15f);
			celp = obj.sigmoidTransform3(p, 3f, 1.5f);
			celp.getGridOperator().multiplyBy(celp, mask);
//			celp.clone().show("afterMask" + idx);
			imp = ImageUtil.wrapGrid(celp, null);
    		imp.setDisplayRange(0, 255);
    		nameOut = pathOut + idx + ".png";
    		IJ.saveAs(imp, "png", nameOut);
		}
		System.out.println("finished!");
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
				val = (float)(210.0 / (1.0 + Math.exp(- a * ((double) img.getAtIndex(i, j) - t))) + 40);
				img2.setAtIndex(i, j, val);
			}
		
		return img2;
	}
	
	
	/**
	 * return a new image
	 * @param img
	 * @param t
	 * @param a
	 * @return
	 */
	//original paramters 215 and 20
	public Grid2D sigmoidTransform3(Grid2D img, float t, float a)
	{
		float val;
		Grid2D img2 = new Grid2D(img.getWidth(), img.getHeight());
		for(int i = 0; i < img.getSize()[0]; i++)
			for(int j = 0; j < img.getSize()[1]; j++) {
//				val = (float)(215.0 / (1.0 + Math.exp(- a * ((double) img.getAtIndex(i, j) - t))) + 20);
				if(img.getAtIndex(i, j) < 0.08)
					val = 0;
				else if (img.getAtIndex(i, j) < 1.2)
					val = (float)(121 / (1.0 + Math.exp(- ((double) img.getAtIndex(i, j) - 0.6)/1.12f)) -25);
//					val = 20 + (img.getAtIndex(i, j) - 0.08f) * 33/1.12f;
				else
					val = (float)(210.0 / (1.0 + Math.exp(- a * ((double) img.getAtIndex(i, j) - t))) + 40);
				img2.setAtIndex(i, j, val);
			}
		
		return img2;
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
}
