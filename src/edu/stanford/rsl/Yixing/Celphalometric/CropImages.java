package edu.stanford.rsl.Yixing.Celphalometric;

import java.io.IOException;

import edu.stanford.rsl.Yixing.Celphalometric.superResolution.GenerateTestPatches;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class CropImages {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		GenerateTestPatches obj = new GenerateTestPatches();
		String path = "D:\\Dropbox\\Dropbox\\Cephalogram\\templateTMI\\Figures\\";
		String savePath = "D:\\Dropbox\\Dropbox\\Cephalogram\\templateTMI\\Figures\\";
		String saveName;
		ImagePlus imp;
		String imgNameIn;
		int width = 1935;
		int height = 2200;
		Grid2D img;
		int offset = 625;
		int offset2 = 180;
		//15 31 31 
		//38 34 30
		//146 34 32
		imgNameIn = path + "celpBicubic0.png";
		imp = IJ.openImage(imgNameIn);
		img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		Grid2D img2 = cropImage(img, offset, offset2, width, height);
		img2.clone().show("bicubic");
		imp = ImageUtil.wrapGrid(img2, null);
		imp.setDisplayRange(0, 255);
		saveName = savePath +"celpBicubicCrop2" + ".png";
		IJ.saveAs(imp, "png", saveName);
		
		imgNameIn = path + "celpRDNMask0.png";
		imp = IJ.openImage(imgNameIn);
		img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		img2 = cropImage(img, offset, offset2, width, height);
		img2.clone().show("RDN");
		imp = ImageUtil.wrapGrid(img2, null);
		imp.setDisplayRange(0, 255);
		saveName = savePath +"celpRDNMaskCrop2" + ".png";
		IJ.saveAs(imp, "png", saveName);
            		
		imgNameIn = path + "celpRDNssMask0.png";
		imp = IJ.openImage(imgNameIn);
		img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		img2 = cropImage(img, offset, offset2, width, height);
		img2.clone().show("RDNNss");
		imp = ImageUtil.wrapGrid(img2, null);
		imp.setDisplayRange(0, 255);
		saveName = savePath +"celpRDNssMaskCrop2" + ".png";
		IJ.saveAs(imp, "png", saveName);
		
		imgNameIn = path + "celpRRDN0.png";
		imp = IJ.openImage(imgNameIn);
		img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		img2 = cropImage(img, offset, offset2, width, height);
		img2.clone().show("RRDN");
		imp = ImageUtil.wrapGrid(img2, null);
		imp.setDisplayRange(0, 255);
		saveName = savePath +"celpRRDNCrop2" + ".png";
		IJ.saveAs(imp, "png", saveName);
		
		imgNameIn = path + "celpRRDNss0.png";
		imp = IJ.openImage(imgNameIn);
		img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		img2 = cropImage(img, offset, offset2, width, height);
		img2.clone().show("RRDNss");
		imp = ImageUtil.wrapGrid(img2, null);
		imp.setDisplayRange(0, 255);
		saveName = savePath +"celpRRDNssCrop2" + ".png";
		IJ.saveAs(imp, "png", saveName);

		imgNameIn = path + "celpPix2pixGAN0.png";
		imp = IJ.openImage(imgNameIn);
		img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		img2 = cropImage(img, offset, offset2, width, height);
		img2.clone().show("pix2pix");
		imp = ImageUtil.wrapGrid(img2, null);
		imp.setDisplayRange(0, 255);
		saveName = savePath +"celpPix2pixGANCrop2" + ".png";
		IJ.saveAs(imp, "png", saveName);
		
		System.out.println("finished!");

	}
	
	static private Grid2D cropImage(Grid2D img, int offsetX, int offsetY, int width, int height) {
		Grid2D img2 = new Grid2D(width, height);
		for(int i = 0; i < width; i++)
			for(int j = 0; j < height; j++)
				img2.setAtIndex(i, j, img.getAtIndex(i + offsetX, j + offsetY));
		
		return img2;
	}

}
