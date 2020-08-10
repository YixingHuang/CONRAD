package edu.stanford.rsl.Yixing.Celphalometric.convert;

import java.io.File;
import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class convertProjection2RGBs {


	
	public static void main(String[] args) throws IOException{
		new ImageJ();
//		String pathIn = "D:\\Tasks\\FAU4\\Cephalometric\\parallelProjectionsEnhanced\\";
//		String pathOut = "D:\\Tasks\\FAU4\\Cephalometric\\parallelProjectionsEnhancedPNG\\";
		
//		String pathIn = "D:\\Tasks\\FAU4\\Cephalometric\\coneBeamProjections\\";
//		String pathIn2 = "D:\\Tasks\\FAU4\\Cephalometric\\coneBeamProjections\\";
//		String pathOut = "D:\\Tasks\\FAU4\\Cephalometric\\coneBeamProjectionsRGB\\";
		
//		String pathIn = "D:\\Tasks\\FAU4\\Cephalometric\\coneBeamProjectionsLower\\";
//		String pathIn2 = "D:\\Tasks\\FAU4\\Cephalometric\\coneBeamProjectionsLower\\";
//		String pathOut = "D:\\Tasks\\FAU4\\Cephalometric\\coneBeamProjectionsRGBLower\\";
		
		String pathIn = "D:\\Tasks\\FAU4\\Cephalometric\\coneBeamProjectionsLowerAug\\";
		String pathIn2 = "D:\\Tasks\\FAU4\\Cephalometric\\coneBeamProjectionsLowerAug\\";
		String pathOut = "D:\\Tasks\\FAU4\\Cephalometric\\coneBeamProjectionsRGBLowerAug\\";
		
//		String pathIn = "D:\\Tasks\\FAU4\\Cephalometric\\coneBeamProjectionsTest\\";
//		String pathIn2 = "D:\\Tasks\\FAU4\\Cephalometric\\coneBeamProjectionsTest\\";
//		String pathOut = "D:\\Tasks\\FAU4\\Cephalometric\\coneBeamProjectionsTestRGB\\";
		String nameIn, nameIn2, nameOut;
		ImagePlus imp;
		Grid2D p;
		Grid2D mask, celp;
		Grid2D input;
		Grid3D input3D = new Grid3D(512, 512, 3);
		convertProjection2RGBs obj = new convertProjection2RGBs();
		for(int idx = 0; idx <501; idx++) {
			nameIn = pathIn + idx + ".tif";
			File outPutDir=new File(nameIn);
			if (!outPutDir.exists())
				continue;
			nameIn2 = pathIn2 + idx + "_2.tif";
			File outPutDir2=new File(nameIn2);
			if (!outPutDir2.exists())
				continue;
			imp = IJ.openImage(nameIn);
			input = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
			input3D.setSubGrid(0, (Grid2D)input.clone());
			input3D.setSubGrid(2, (Grid2D)input.clone());
			imp = IJ.openImage(nameIn2);
			input = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
			input3D.setSubGrid(1, (Grid2D)input.clone());
//			input3D.clone().show("3D");
			imp = ImageUtil.wrapGrid(input3D, null);
			imp.setDisplayRange(0, 5.6);
			IJ.run(imp, "8-bit", "");
			//for RGB
//			IJ.run(imp,"Stack to RGB", "");
//			imp = IJ.getImage();
//    		nameOut = pathOut + idx + ".png";
//    		IJ.saveAs(imp, "png", nameOut);
//    		imp.close();
			//for Tiff
    		nameOut = pathOut + idx + ".tif";
    		IJ.saveAs(imp, "TIFF", nameOut);
    		imp.close();

    		System.out.print(idx + " ");
		}
		System.out.println("");
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
