package edu.stanford.rsl.Yixing.Celphalometric.flipping;

import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.ChannelSplitter;

public class FlipPatches {

	public static void main(String[] args) throws IOException{
		new ImageJ();
		FlipPatches obj = new FlipPatches();
		String inputPath = "D:\\Pix2pix\\tools\\p2cep\\trainRGB4\\";
		String savePath = "D:\\Pix2pix\\tools\\p2cep\\trainRGBFour\\";
		String inputName, outputName;
	
		ImagePlus imp;
		Grid3D input;
		int saveId = 1201;
		Grid3D output = new Grid3D(512, 256, 3);
		Grid2D left = new Grid2D(256, 256);
		Grid2D right = new Grid2D(256, 256);
		Grid2D merge, left2, right2, merge2;
		ImagePlus[] channels;
		for(int idx = 1; idx <= 486; idx++)
		{
			inputName = inputPath + idx + ".png";
			imp = IJ.openImage(inputName);
			channels = ChannelSplitter.split(imp);
			input = ImageUtil.wrapImagePlus(imp);

			for(int c = 0; c < 2; c++)
			{
				merge = ImageUtil.wrapImagePlus(channels[c]).getSubGrid(0);
				obj.splitImages(merge, left, right);
				left2 = obj.flipud(left);
				right2 = obj.flipud(right);
				merge2 = obj.mergeImages(left2, right2);
				output.setSubGrid(c, (Grid2D)merge2.clone());
				if(c == 0)
					output.setSubGrid(2, (Grid2D)merge2.clone());
			}	
			imp = ImageUtil.wrapGrid(output, null);
			imp.setDisplayRange(0, 255);
			IJ.run(imp, "8-bit", "");
			//for RGB
			IJ.run(imp,"Stack to RGB", "");
			imp = IJ.getImage();
    		outputName = savePath + saveId + ".png";
    		IJ.saveAs(imp, "png", outputName);
    		imp.close();
    		saveId++;
    		
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
	
}
