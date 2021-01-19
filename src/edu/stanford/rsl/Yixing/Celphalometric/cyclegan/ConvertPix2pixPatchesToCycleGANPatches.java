package edu.stanford.rsl.Yixing.Celphalometric.cyclegan;

import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.ChannelSplitter;

public class ConvertPix2pixPatchesToCycleGANPatches {

	public static void main(String[] args) throws IOException{
		new ImageJ();
		ConvertPix2pixPatchesToCycleGANPatches obj = new ConvertPix2pixPatchesToCycleGANPatches();
//		String inputPath = "D:\\Pix2pix\\tools\\p2cep\\trainRGBFour2_y27\\";
//		String savePathA = "D:\\CycleGAN-tensorflow\\datasets\\p2cepRGB\\trainA\\";
//		String savePathB = "D:\\CycleGAN-tensorflow\\datasets\\p2cepRGB\\trainB\\";
		
		String inputPath = "D:\\Pix2pix\\tools\\p2cep\\TestRGBFour\\";
		String savePathA = "D:\\CycleGAN-tensorflow\\datasets\\p2cepRGB\\testA\\";
		String savePathB = "D:\\CycleGAN-tensorflow\\datasets\\p2cepRGB\\testB\\";
//		String inputPath = "D:\\Pix2pix\\tools\\p2cep\\trainRGBFour2_y27\\";
//		String savePathA = "D:\\CycleGAN-tensorflow\\datasets\\p2cep1Chan\\trainA\\";
//		String savePathB = "D:\\CycleGAN-tensorflow\\datasets\\p2cep1Chan\\trainB\\";
		
//		String inputPath = "D:\\Pix2pix\\tools\\p2cep\\TestRGBFour\\";
//		String savePathA = "D:\\CycleGAN-tensorflow\\datasets\\p2cep1Chan\\testA\\";
//		String savePathB = "D:\\CycleGAN-tensorflow\\datasets\\p2cep1Chan\\testB\\";
		String inputName, outputName;
	
		ImagePlus imp;
		Grid3D input;
		Grid2D output = new Grid2D(512, 256);
		Grid2D left = new Grid2D(256, 256);
		Grid2D right = new Grid2D(256, 256);
		Grid3D leftRGB = new Grid3D(256, 256, 3);
		Grid3D rightRGB = new Grid3D(256, 256, 3);
		Grid2D merge, left2, right2, merge2;
		ImagePlus[] channels;
		for(int idx = 1; idx <= 1686; idx++)
		{
			inputName = inputPath + idx + ".png";
			imp = IJ.openImage(inputName);
			channels = ChannelSplitter.split(imp);	
			output = ImageUtil.wrapImagePlus(channels[0]).getSubGrid(0);
			obj.splitImages(output, left, right);
			leftRGB.setSubGrid(0, (Grid2D)left.clone());
			rightRGB.setSubGrid(0, (Grid2D)right.clone());
			
			//for 1 channel
//			leftRGB.setSubGrid(1, (Grid2D)left.clone());
//			rightRGB.setSubGrid(1, (Grid2D)right.clone());
//			leftRGB.setSubGrid(2, (Grid2D)left.clone());
//			rightRGB.setSubGrid(2, (Grid2D)right.clone());
			
			
			output = ImageUtil.wrapImagePlus(channels[1]).getSubGrid(0);
			obj.splitImages(output, left, right);
			leftRGB.setSubGrid(1, (Grid2D)left.clone());
			rightRGB.setSubGrid(1, (Grid2D)right.clone());
			
			output = ImageUtil.wrapImagePlus(channels[2]).getSubGrid(0);
			obj.splitImages(output, left, right);
			leftRGB.setSubGrid(2, (Grid2D)left.clone());
			rightRGB.setSubGrid(2, (Grid2D)right.clone());
			
			
			imp = ImageUtil.wrapGrid(leftRGB, null);
			imp.setDisplayRange(0, 255);
			IJ.run(imp, "8-bit", "");
			IJ.run(imp,"Stack to RGB", "");
			imp = IJ.getImage();
    		outputName = savePathA + idx + ".png";
    		IJ.saveAs(imp, "png", outputName);
    		imp.close();
    		
    		imp = ImageUtil.wrapGrid(rightRGB, null);
			imp.setDisplayRange(0, 255);
			IJ.run(imp, "8-bit", "");
			IJ.run(imp,"Stack to RGB", "");
			imp = IJ.getImage();
    		outputName = savePathB + idx + ".png";
    		IJ.saveAs(imp, "png", outputName);
    		imp.close();

    		
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
