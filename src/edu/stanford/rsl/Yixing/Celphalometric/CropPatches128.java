package edu.stanford.rsl.Yixing.Celphalometric;

import java.io.IOException;

import edu.stanford.rsl.Yixing.Celphalometric.superResolution.GenerateTestPatches;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class CropPatches128 {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		GenerateTestPatches obj = new GenerateTestPatches();
		String path = "D:\\Dropbox\\Dropbox\\Cephalogram\\templateTMI\\Figures\\";
		String savePath = "D:\\Dropbox\\Dropbox\\Cephalogram\\templateTMI\\Figures\\";
		String saveName;
		ImagePlus imp;
		String imgNameIn;
		int sz = 200;
		Grid2D img;
		int idx = 1609;
		int offset = 28;
		//15 31 31 
		//38 34 30
		//146 34 32
		imgNameIn = path + idx + "bicubic.png";
		imp = IJ.openImage(imgNameIn);
		img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		Grid2D img2 = cropImage(img, offset, offset,sz);
		img2.clone().show(idx + "bicubic" + sz);
		imp = ImageUtil.wrapGrid(img2, null);
		imp.setDisplayRange(0, 255);
		saveName = savePath + idx + "bicubic" + sz + ".png";
		IJ.saveAs(imp, "png", saveName);
		
		imgNameIn = path + idx + "RDN.png";
		imp = IJ.openImage(imgNameIn);
		img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		img2 = cropImage(img, offset, offset,sz);
		img2.clone().show(idx + "RDN" + sz);
		imp = ImageUtil.wrapGrid(img2, null);
		imp.setDisplayRange(0, 255);
		saveName = savePath + idx + "RDN" + sz + ".png";
		IJ.saveAs(imp, "png", saveName);
            		
		imgNameIn = path + idx + "RDNss.png";
		imp = IJ.openImage(imgNameIn);
		img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		img2 = cropImage(img, offset, offset,sz);
		img2.clone().show(idx + "RDNss" + sz);
		imp = ImageUtil.wrapGrid(img2, null);
		imp.setDisplayRange(0, 255);
		saveName = savePath + idx + "RDNss" + sz + ".png";
		IJ.saveAs(imp, "png", saveName);
		
		imgNameIn = path + idx + "RRDN.png";
		imp = IJ.openImage(imgNameIn);
		img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		img2 = cropImage(img, offset, offset,sz);
		img2.clone().show(idx + "RRDN"+ sz);
		imp = ImageUtil.wrapGrid(img2, null);
		imp.setDisplayRange(0, 255);
		saveName = savePath + idx + "RRDN" + sz + ".png";
		IJ.saveAs(imp, "png", saveName);
		
		imgNameIn = path + idx + "RRDNss.png";
		imp = IJ.openImage(imgNameIn);
		img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		img2 = cropImage(img, offset, offset,sz);
		img2.clone().show(idx + "RRDNss"+ sz);
		imp = ImageUtil.wrapGrid(img2, null);
		imp.setDisplayRange(0, 255);
		saveName = savePath + idx + "RRDNss" + sz + ".png";
		IJ.saveAs(imp, "png", saveName);

		imgNameIn = path + idx + "pix2pixGAN.png";
		imp = IJ.openImage(imgNameIn);
		img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		img2 = cropImage(img, offset, offset,sz);
		img2.clone().show(idx + "pix2pixGAN"+ sz);
		imp = ImageUtil.wrapGrid(img2, null);
		imp.setDisplayRange(0, 255);
		saveName = savePath + idx + "pix2pixGAN" + sz + ".png";
		IJ.saveAs(imp, "png", saveName);
		
		imgNameIn = path + idx + "targets.png";
		imp = IJ.openImage(imgNameIn);
		img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		img2 = cropImage(img, offset, offset,sz);
		img2.clone().show(idx + "targets"+ sz);
		imp = ImageUtil.wrapGrid(img2, null);
		imp.setDisplayRange(0, 255);
		saveName = savePath + idx + "targets" + sz + ".png";
		IJ.saveAs(imp, "png", saveName);
	}
	
	static private Grid2D cropImage(Grid2D img, int offsetX, int offsetY, int size) {
		Grid2D img2 = new Grid2D(size, size);
		for(int i = 0; i < size; i++)
			for(int j = 0; j < size; j++)
				img2.setAtIndex(i, j, img.getAtIndex(i + offsetX, j + offsetY));
		
		return img2;
	}

}
