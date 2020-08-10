package edu.stanford.rsl.Yixing.Celphalometric.crop;

import java.io.IOException;

import edu.stanford.rsl.Yixing.Celphalometric.superResolution.GenerateTestPatches;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class CropPatches2 {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		GenerateTestPatches obj = new GenerateTestPatches();
		String path = "D:\\Dropbox\\Dropbox\\Cephalogram\\templateTMI\\Figures\\";
		String savePath = "D:\\Dropbox\\Dropbox\\Cephalogram\\templateTMI\\Figures\\";
		String saveName;
		ImagePlus imp;
		String imgNameIn;
		int sz = 60;
		Grid2D img;
		int idx = 11207;
		//15 31 31 
		//38 34 30
		//146 34 32
		//11207 2 3
		imgNameIn = path + "Test1Target.png";
		imp = IJ.openImage(imgNameIn);
		img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		Grid2D img2 = cropImage(img, 66, 216,sz);
		img2.clone().show("targetPatch");
		
		imgNameIn = path + "Test1RGBOutput.png";
		imp = IJ.openImage(imgNameIn);
		img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		img2 = cropImage(img, 66, 216,sz);
		img2.clone().show("dualOutputPatch");
		
		imgNameIn = path + "Test1pOutput.png";
		imp = IJ.openImage(imgNameIn);
		img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		img2 = cropImage(img, 66, 216,sz);
		img2.clone().show("oneOutputPatch");
            		


	}
	
	static private Grid2D cropImage(Grid2D img, int offsetX, int offsetY, int size) {
		Grid2D img2 = new Grid2D(size, size);
		for(int i = 0; i < size; i++)
			for(int j = 0; j < size; j++)
				img2.setAtIndex(i, j, img.getAtIndex(i + offsetX, j + offsetY));
		
		return img2;
	}

}
