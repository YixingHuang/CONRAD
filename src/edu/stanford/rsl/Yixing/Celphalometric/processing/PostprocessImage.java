package edu.stanford.rsl.Yixing.Celphalometric.processing;

import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class PostprocessImage {

	public static void main(String[] args) throws IOException{
		new ImageJ();
		PostprocessImage obj = new PostprocessImage();
		String inputPath = "D:\\Tasks\\FAU4\\Cephalometric\\generatedCelps2\\qq0.png";
		String maskPath = "D:\\Tasks\\FAU4\\Cephalometric\\generatedCelps2\\p0.png";
	
		ImagePlus imp = IJ.openImage(inputPath);
		Grid2D img = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		img.clone().show("img0");
		imp = IJ.openImage(maskPath);
		Grid2D mask = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		for(int x = 434; x < 440; x++)
			for(int y = 230; y < 250; y++)
			{
				if(img.getAtIndex(x, y) <25 && mask.getAtIndex(x, y) > 5)
					img.setAtIndex(x, y, mask.getAtIndex(x, y));
			}
		img.setAtIndex(439,  243, 20);
		img.setAtIndex(439,  242, 21);
		img.clone().show();
		
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
