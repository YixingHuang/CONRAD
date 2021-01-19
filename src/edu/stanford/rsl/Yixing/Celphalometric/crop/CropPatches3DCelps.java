package edu.stanford.rsl.Yixing.Celphalometric.crop;

import java.io.IOException;

import edu.stanford.rsl.Yixing.Celphalometric.superResolution.GenerateTestPatches;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class CropPatches3DCelps {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		GenerateTestPatches obj = new GenerateTestPatches();
		String path = "D:\\Dropbox\\Dropbox\\Cephalogram\\templateTMI\\Figures\\";
		String savePath = "D:\\Dropbox\\Dropbox\\Cephalogram\\templateTMI\\Figures\\";
		String saveName;
		ImagePlus imp;
		String imgNameIn;
		int sz = 70; 
		int sz2 = 140;
		int offset1 = 420;
		int offset2 = 250;//for nose
//		int sz = 70;
//		int sz2 = 70;
//		int offset1 = 250;
//		int offset2 = 350;
		Grid2D img;
		int idx = 1609;

		//15 31 31 
		//38 34 30
		//146 34 321
		imgNameIn = path + "Farma.png";
		imp = IJ.openImage(imgNameIn);
		img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		Grid2D img2 = cropImage(img, offset1, offset2,sz, sz2);;
		img2.clone().show(idx + "Farma" + sz);
		imp = ImageUtil.wrapGrid(img2, null);
		imp.setDisplayRange(0, 125);
		saveName = savePath + "Farma" + sz + "_2.png";
		IJ.saveAs(imp, "png", saveName);
		
		imgNameIn = path + "Kumar.png";
		imp = IJ.openImage(imgNameIn);
		img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		img2 = cropImage(img, offset1, offset2,sz, sz2);;
		img2.clone().show(idx + "Kumar" + sz);
		imp = ImageUtil.wrapGrid(img2, null);
		imp.setDisplayRange(0, 125);
		saveName = savePath + "Kumar" + sz + "_2.png";
		IJ.saveAs(imp, "png", saveName);
            		
		imgNameIn = path + "MIP50.png";
		imp = IJ.openImage(imgNameIn);
		img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		img2 = cropImage(img, offset1, offset2,sz, sz2);;
		img2.clone().show(idx + "MIP50" + sz);
		imp = ImageUtil.wrapGrid(img2, null);
		imp.setDisplayRange(0, 125);
		saveName = savePath + "MIP50" + sz + "_2.png";
		IJ.saveAs(imp, "png", saveName);
		
		imgNameIn = path + "cycleGANTypeI2.png";
		imp = IJ.openImage(imgNameIn);
		img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		img2 = cropImage(img, offset1, offset2,sz, sz2);;
		img2.clone().show(idx + "cycleGANTypeI2" + sz);
		imp = ImageUtil.wrapGrid(img2, null);
		imp.setDisplayRange(0, 125);
		saveName = savePath + "cycleGANTypeI2" + sz + "_2.png";
		IJ.saveAs(imp, "png", saveName);
		
		
		imgNameIn = path + "MIP100.png";
		imp = IJ.openImage(imgNameIn);
		img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		img2 = cropImage(img, offset1, offset2,sz, sz2);;
		img2.clone().show(idx + "MIP100"+ sz);
		imp = ImageUtil.wrapGrid(img2, null);
		imp.setDisplayRange(0, 125);
		saveName = savePath + "MIP100" + sz + "_2.png";
		IJ.saveAs(imp, "png", saveName);

		imgNameIn = path + "parallelProjectionEnhanced.png";
		imp = IJ.openImage(imgNameIn);
		img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		img2 = cropImage(img, offset1, offset2,sz, sz2);;
		img2.clone().show(idx + "parallelProjectionEnhanced"+ sz);
		imp = ImageUtil.wrapGrid(img2, null);
		imp.setDisplayRange(0, 125);
		saveName = savePath + "parallelProjectionEnhanced" + sz + "_2.png";
		IJ.saveAs(imp, "png", saveName);
		
		imgNameIn = path + "backgroundRecovery.png";
		imp = IJ.openImage(imgNameIn);
		img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		img2 = cropImage(img, offset1, offset2,sz, sz2);;
		img2.clone().show(idx + "backgroundRecovery"+ sz);
		imp = ImageUtil.wrapGrid(img2, null);
		imp.setDisplayRange(0, 125);
		saveName = savePath + "backgroundRecovery" + sz + "_2.png";
		IJ.saveAs(imp, "png", saveName);
		
		imgNameIn = path + "p0.png";
		imp = IJ.openImage(imgNameIn);
		img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		img2 = cropImage(img, offset1, offset2,sz, sz2);;
		img2.clone().show(idx + "p0"+ sz);
		imp = ImageUtil.wrapGrid(img2, null);
		imp.setDisplayRange(0, 125);
		saveName = savePath + "p0" + sz + "_2.png";
		IJ.saveAs(imp, "png", saveName);
		
		imgNameIn = path + "backgroundRecoveryPerspective.png";
		imp = IJ.openImage(imgNameIn);
		img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		img2 = cropImage(img, offset1, offset2,sz, sz2);;
		img2.clone().show(idx + "backgroundRecoveryPerspective"+ sz);
		imp = ImageUtil.wrapGrid(img2, null);
		imp.setDisplayRange(0, 125);
		saveName = savePath + "backgroundRecoveryPerspective" + sz + "_2.png";
		IJ.saveAs(imp, "png", saveName);
		
		
	}
	
	static private Grid2D cropImage(Grid2D img, int offsetX, int offsetY, int size, int size2) {
		Grid2D img2 = new Grid2D(size, size2);
		for(int i = 0; i < size; i++)
			for(int j = 0; j < size2; j++)
				img2.setAtIndex(i, j, img.getAtIndex(i + offsetX, j + offsetY));
		
		return img2;
	}

}
