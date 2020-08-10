package edu.stanford.rsl.Yixing.Celphalometric.crop;

import java.io.IOException;

import edu.stanford.rsl.Yixing.Celphalometric.superResolution.GenerateTestPatches;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class CropImagesForAllPatientsMIP {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		GenerateTestPatches obj = new GenerateTestPatches();
		String path = "D:\\Tasks\\FAU4\\Cephalometric\\generatedCelps\\MIP\\";
		String savePath = "D:\\Tasks\\FAU4\\Cephalometric\\generatedCelps\\MIP\\";
		String saveName;
		ImagePlus imp;
		String imgNameIn;
		int width = 1935;
		int height = 2400;
		Grid2D img;
		int offset = 625;
		int offset2 = 140;
		Grid2D img2;
		
		int idx = 0;
		offset = 625;
		offset2 = 150;
		imgNameIn = path + "MIP_50s_0.png";
		imp = IJ.openImage(imgNameIn);
		img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		img2 = cropImage(img, offset, offset2, width, height);
		img2.clone().show("pix2pix");
		imp = ImageUtil.wrapGrid(img2, null);
		imp.setDisplayRange(0, 255);
		saveName = savePath +"MIP_50s_0Crop" + ".png";
		IJ.saveAs(imp, "png", saveName);
		
		imgNameIn = path + "MIP_100s_0.png";
		imp = IJ.openImage(imgNameIn);
		img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		img2 = cropImage(img, offset, offset2, width, height);
		img2.clone().show("pix2pix");
		imp = ImageUtil.wrapGrid(img2, null);
		imp.setDisplayRange(0, 255);
		saveName = savePath +"MIP_100s_0Crop" + ".png";
		IJ.saveAs(imp, "png", saveName);
		
//		offset = 625;
//		offset2 = 10;  //2,3,4
////		offset2 = 150;
//		for(int id = 1; id <=3; id++)
//		{
//			imgNameIn = path + "MIP_50s_" + id + ".png";
//			imp = IJ.openImage(imgNameIn);
//			img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
//			img2 = cropImage(img, offset, offset2, width, height);
//			img2.clone().show("pix2pix");
//			imp = ImageUtil.wrapGrid(img2, null);
//			imp.setDisplayRange(0, 255);
//			saveName = savePath +"MIP_50s_" + id + "Crop.png";
//			IJ.saveAs(imp, "png", saveName);
//			
//			imgNameIn = path + "MIP_100s_" + id + ".png";
//			imp = IJ.openImage(imgNameIn);
//			img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
//			img2 = cropImage(img, offset, offset2, width, height);
//			img2.clone().show("pix2pix");
//			imp = ImageUtil.wrapGrid(img2, null);
//			imp.setDisplayRange(0, 255);
//			saveName = savePath +"MIP_100s_" + id  + "Crop.png";
//			IJ.saveAs(imp, "png", saveName);
//		}
		
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
