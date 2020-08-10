package edu.stanford.rsl.Yixing.Celphalometric.processing;

import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class PostprocessPatches {

	public static void main(String[] args) throws IOException{
		new ImageJ();
		PostprocessPatches obj = new PostprocessPatches();
		String inputPath = "D:\\Dropbox\\Dropbox\\Cephalogram\\templateTMI\\Figures\\patchTest1Output_RGB4.png";
		String maskPath = "D:\\Tasks\\FAU4\\Cephalometric\\generatedCelps\\maskGradient0.tif";
		ImagePlus imp = IJ.openImage(maskPath);
		Grid2D mask = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		int startX = 256;
		int startY = 10;
		int szIn = 256;
		int ycut = 0;
		Grid2D maskPatch = new Grid2D(szIn, szIn);
		for(int x = 0; x < szIn; x++)
            for(int y = 0; y < szIn - ycut; y++){
            	maskPatch.setAtIndex(x, y, mask.getAtIndex(startX + x , startY  + y));
        }

		
		imp = IJ.openImage(inputPath);
		Grid2D img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		Grid3D rgb = new Grid3D(szIn, szIn, 3);
		
		Grid2D mask2 = obj.getBackgroundMask(img, 1);
		Grid2D maskGradient2 = obj.computeGradient(mask2, 0.5f);
		for(int i = 0; i < szIn; i++)
			for(int j = 0; j < szIn; j++)
			{
				for(int k = 0; k < 3; k++)
					rgb.setAtIndex(i, j, k, img.getAtIndex(i, j));
				if(maskPatch.getAtIndex(i, j) == 1)
				{
					rgb.setAtIndex(i, j, 0, 0);
					rgb.setAtIndex(i, j, 1, 0);
					rgb.setAtIndex(i, j, 2, 255);
				}
//				if(maskGradient2.getAtIndex(i, j) == 1)
//				{
//					rgb.setAtIndex(i, j, 0, 0);
//					rgb.setAtIndex(i, j, 1, 255);
//					rgb.setAtIndex(i, j, 2, 0);
//				}
				
			}
		
		rgb.show("rgb");
		imp = ImageUtil.wrapGrid(rgb, null);
		imp.setDisplayRange(0, 255);
		IJ.run(imp, "8-bit", "");
		//for RGB
		IJ.run(imp,"Stack to RGB", "");
	}
	
	public void thresholding(Grid3D vol, float thres1, float thres2) {
		for(int i = 0; i < vol.getSize()[0]; i++)
			for(int j = 0; j < vol.getSize()[1]; j++)
				for(int k = 0; k < vol.getSize()[2]; k++) {
					if(vol.getAtIndex(i, j, k) >= thres1 || vol.getAtIndex(i, j, k) < thres2)
						vol.setAtIndex(i, j, k, 0);
				}
	}
	
	public void removePillow(Grid3D vol, float thres) {
		for(int i = 0; i < vol.getSize()[0]; i++)
			for(int j = 300; j < vol.getSize()[1]; j++)
				for(int k = 0; k < vol.getSize()[2]; k++) {
					if(vol.getAtIndex(i, j, k) >= thres)
						vol.setAtIndex(i, j, k, 0);
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
