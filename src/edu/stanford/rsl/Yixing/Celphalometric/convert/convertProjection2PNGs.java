package edu.stanford.rsl.Yixing.Celphalometric.convert;

import java.io.File;
import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class convertProjection2PNGs {
/**
 * range [0, 5.6] to be [0, 255]
 * @param args
 * @throws IOException
 */

	
	public static void main(String[] args) throws IOException{
		new ImageJ();
//		String pathIn = "D:\\Tasks\\FAU4\\Cephalometric\\parallelProjectionsEnhanced\\";
//		String pathOut = "D:\\Tasks\\FAU4\\Cephalometric\\parallelProjectionsEnhancedPNG\\";
//		String pathIn = "D:\\Tasks\\FAU4\\Cephalometric\\coneBeamProjections\\";
//		String pathOut = "D:\\Tasks\\FAU4\\Cephalometric\\coneBeamProjectionsPNG\\";
		String pathIn = "D:\\Tasks\\FAU4\\Cephalometric\\coneBeamProjectionsLowerAug\\";
		String pathOut = "D:\\Tasks\\FAU4\\Cephalometric\\coneBeamProjectionsLowerAugPNG\\";
		String nameIn, nameOut;
		ImagePlus imp;
		Grid2D p;
		Grid2D mask, celp;
		convertProjection2PNGs obj = new convertProjection2PNGs();
		for(int idx = 0; idx < 490; idx++) {
			nameIn = pathIn + idx + ".tif";
			File outPutDir=new File(nameIn);
			if (!outPutDir.exists())
				continue;
			imp = IJ.openImage(nameIn);
			imp.setDisplayRange(0, 5.6);
    		nameOut = pathOut + idx + ".png";
    		IJ.saveAs(imp, "png", nameOut);
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
				val = (float)(235.0 / (1.0 + Math.exp(- a * ((double) img.getAtIndex(i, j) - t))) + 20);
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
