package edu.stanford.rsl.Yixing.Celphalometric.cyclegan;

import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

/**
 * One model for all;  need flipping before stitching
 * @author Yixing Huang
 *
 */
public class StitchOverlapPatchesForProjection2CepCycleGAN {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		boolean isPix = false;
		String path = "D:\\CycleGAN-tensorflow\\test\\p2cep1Chan\\";
		String savePath = path;
		String saveName;
		ImagePlus imp;
		Grid2D patch;
		String imgNameIn;
		int startX, startY;
		int saveId = 1;
		int szIn = 256;
		StitchOverlapPatchesForProjection2CepCycleGAN obj = new StitchOverlapPatchesForProjection2CepCycleGAN();
		Grid3D patches = new Grid3D(szIn, szIn, 4);
		for(int i = 1; i <= 4; i++)
		{
			imgNameIn = path + "AtoB_" + i + ".png";
			imp = IJ.openImage(imgNameIn);
			patch = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
			if(i == 1)
				patch = obj.fliplr(patch);
			if(i == 3)
				patch = obj.fliplrud(patch);
			if(i == 4)
				patch = obj.flipud(patch);
			patches.setSubGrid(i-1, (Grid2D)patch.clone());
		}
		
		Grid2D us = new Grid2D(512, 512);
		startX = 10;
		startY = 10;
		for(int x = 0; x < szIn; x++)
            for(int y = 0; y < szIn; y++){
            	us.setAtIndex(x + startX, y + startY, patches.getAtIndex(x, y, 0));
            }
		
		startX = 245;
		startY = 10;
		for(int x = 5; x < szIn; x++)
            for(int y = 0; y < szIn; y++){
            	us.setAtIndex(x + startX, y + startY, patches.getAtIndex(x, y, 1));
            }
		
		startX = 10;
		startY = 245;
		for(int x = 0; x < szIn; x++)
            for(int y = 10; y < szIn; y++){
            	us.setAtIndex(x + startX, y + startY, patches.getAtIndex(x, y, 2));
            }
		
		startX = 245;
		startY = 245;
		for(int x = 5; x < szIn; x++)
            for(int y = 10; y < szIn; y++){
            	us.setAtIndex(x + startX, y + startY, patches.getAtIndex(x, y, 3));
            }
		
		
            us.clone().show("us");
            

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
	
	public Grid2D fliplr(Grid2D img) {
		Grid2D img2 = new Grid2D(img.getSize()[0], img.getSize()[1]);
		img2.setSpacing(img.getSpacing());
		img2.setOrigin(img.getOrigin());
		for(int i = 0; i < img.getSize()[0]; i++)
			for(int j = 0; j < img.getSize()[1]; j++)
				img2.setAtIndex(i, j, img.getAtIndex(img.getSize()[0] -1 - i, j));
		
		return img2;
	}

}
