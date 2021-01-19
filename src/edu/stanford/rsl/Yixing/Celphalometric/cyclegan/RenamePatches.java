package edu.stanford.rsl.Yixing.Celphalometric.cyclegan;

import java.io.File;
import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.ChannelSplitter;

public class RenamePatches {

	public static void main(String[] args) throws IOException{
		new ImageJ();
		RenamePatches obj = new RenamePatches();

		
		String inputPath = "D:\\CycleGAN-tensorflow\\datasets\\volume2cep2\\trainA0\\";
		String savePathA = "D:\\CycleGAN-tensorflow\\datasets\\volume2cep2\\trainA1\\";

		String inputName, outputName;
	
		ImagePlus imp;
		Grid2D input;
		Grid2D patch1, patch2;
		int saveId = 3;
		for(int idx = 1; idx <= 1000; idx++)
		{
			inputName = inputPath + idx + ".png";
			File outPutDir=new File(inputName);
			if (!outPutDir.exists())
				continue;
			imp = IJ.openImage(inputName);
//			input = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
//	
//
//			
//			imp = ImageUtil.wrapGrid(input, null);
//			imp.setDisplayRange(0, 255);
			IJ.run(imp, "RGB Color", "");
    		outputName = savePathA + saveId + ".png";
    		IJ.saveAs(imp, "png", outputName); 		
    		if(saveId%2 == 1) 
    			saveId++;
    		else 
    			saveId = saveId + 3;
    		
 
		}
		System.out.println("finished!");
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
	
	public void splitImages(Grid2D merge, Grid2D left, Grid2D right)
	{
		for(int i = 0; i < left.getWidth(); i++)
			for(int j = 0; j < left.getHeight(); j++)
			{
				left.setAtIndex(i, j, merge.getAtIndex(i, j));
				right.setAtIndex(i, j, merge.getAtIndex(i + left.getWidth(), j));
			}
	}
	
	
	Grid2D mergeImages(Grid2D data2D, Grid2D mask2D) {
		Grid2D merge = new Grid2D(data2D.getSize()[0] * 2, data2D.getSize()[1]);
		for(int i = 0; i < data2D.getSize()[0]; i++)
			for(int j = 0; j < data2D.getSize()[1]; j++)
			{
				merge.setAtIndex(i, j, data2D.getAtIndex(i, j));
				merge.setAtIndex(i + data2D.getSize()[0], j, mask2D.getAtIndex(i, j));
			}
		
		return merge;
	}
	
	Grid3D mergeImages(Grid3D data2D, Grid3D mask2D) {
		Grid3D merge = new Grid3D(data2D.getSize()[0] * 2, data2D.getSize()[1], data2D.getSize()[2]);
		for(int i = 0; i < data2D.getSize()[0]; i++)
			for(int j = 0; j < data2D.getSize()[1]; j++)
				for(int c = 0; c < 3; c++)
				{
					merge.setAtIndex(i, j, c, data2D.getAtIndex(i, j, c));
					merge.setAtIndex(i + data2D.getSize()[0], j, c, mask2D.getAtIndex(i, j, c));
				}
		
		return merge;
	}
	
	private Grid2D cropImage(Grid2D img, int offsetX, int offsetY, int width, int height) {
		Grid2D img2 = new Grid2D(width, height);
		for(int i = 0; i < width; i++)
			for(int j = 0; j < height; j++)
				img2.setAtIndex(i, j, img.getAtIndex(i + offsetX, j + offsetY));
		
		return img2;
	}
}
