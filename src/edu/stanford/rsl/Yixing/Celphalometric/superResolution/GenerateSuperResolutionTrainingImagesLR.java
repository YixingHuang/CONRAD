package edu.stanford.rsl.Yixing.Celphalometric.superResolution;

import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import flanagan.interpolation.*;

public class GenerateSuperResolutionTrainingImagesLR {
	/**
	 * Keep the original size of low resolutions
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException{
		new ImageJ();
		GenerateSuperResolutionTrainingImagesLR obj = new GenerateSuperResolutionTrainingImagesLR();
		String path = "D:\\Tasks\\FAU4\\MasterProjectOfFan\\RawImage\\TrainingData\\";
		String savePath = "D:\\Tasks\\FAU4\\Cephalometric\\SuperResolutionImages2\\";
		String name;
		ImagePlus imp;
		Grid2D gt;
		Grid2D ds, us, dds;
		int factor = 5, factor2;
		Grid2D gtcopy;
		String imgNameIn, imgNameIn2, imgNameOut;
		factor2 = factor * 4;
		int width = (1935 - 1)/factor2 + 1;
		int height = (2400 - 1)/factor2 + 1;
		dds = new Grid2D(width, height);
		dds.setSpacing(0.1 * factor2, 0.1 * factor2);
		dds.setOrigin(-(dds.getSize()[0] - 1.0) * dds.getSpacing()[0]/2.0, -(dds.getSize()[1] - 1.0) * dds.getSpacing()[1]/2.0);
	    width = (1935 - 1)/factor + 1;
		height = (2400 - 1)/factor + 1;
		us = new Grid2D(width, height);
		us.setSpacing(0.1 * factor, 0.1 * factor);
		us.setOrigin(-(us.getSize()[0] - 1.0) * us.getSpacing()[0]/2.0, -(us.getSize()[1] - 1.0) * us.getSpacing()[1]/2.0);
		for(int idx = 1; idx <=150; idx ++) {
			String idxS = String.format("%03d", idx);
			name = path + idxS + ".bmp";
			imp = IJ.openImage(name);
			gt = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
			gt.setSpacing(0.1, 0.1);
			gt.setOrigin(-(gt.getSize()[0] - 1.0) * gt.getSpacing()[0]/2.0, -(gt.getSize()[1] - 1.0) * gt.getSpacing()[1]/2.0);
//			gt.clone().show("gt");
			ds = obj.subsampling(gt,  factor);
			obj.resampleGrid(dds, ds);
			obj.resampleGrid(us, dds);

//			imp = ImageUtil.wrapGrid(ds, null);
//			imgNameIn = savePath + "data" + idx + ".png";
//			imp.setDisplayRange(0, 255);
//			IJ.saveAs(imp, "png", imgNameIn);
			
			imp = ImageUtil.wrapGrid(us, null);
			imgNameIn2 = savePath + "data" + idx + "_rs4.png";
			imp.setDisplayRange(0, 255);
			IJ.saveAs(imp, "png", imgNameIn2);
			
//			imp = ImageUtil.wrapGrid(gt, null);
//			imp.setDisplayRange(0, 255);
//			imgNameOut = savePath + "data" + idx + "_mask.png";
//			IJ.saveAs(imp, "png", imgNameOut);
		}
	}
	

	
	public void thresholding(Grid2D img, float thres) {
		for(int i = 0; i < img.getSize()[0]; i++)
			for(int j = 0; j < img.getSize()[1]; j++)
				if(img.getAtIndex(i, j) > thres)
					img.setAtIndex(i, j, thres);
	}

	public Grid2D subsampling(Grid2D img, int factor)
	{
		float val = 0;
		int count = 0;
		int nx, ny;
		int width = (img.getWidth() - 1)/factor + 1;
		int height = (img.getHeight() - 1)/factor + 1;
		Grid2D img2 = new Grid2D(width, height);
		img2.setSpacing(img.getSpacing()[0] * factor, img.getSpacing()[1] * factor);
		img2.setOrigin(-(img2.getSize()[0] - 1.0) * img2.getSpacing()[0]/2.0, -(img2.getSize()[1] - 1.0) * img2.getSpacing()[1]/2.0);
		for(int i = 0; i < width; i++)
			for(int j = 0; j < height; j++) {
				val = 0; count = 0;
				for(int m = 0; m < factor; m++)
					for(int n = 0; n < factor; n++)
					{
						nx = i * factor + m;
						ny = j * factor + n;
						if(nx < img.getWidth() && ny < img.getHeight())
						{
							val = val + img.getAtIndex(nx, ny);
							count++;
						}
					}
				img2.setAtIndex(i, j, val/count);
			}
		
		return img2;
	}
	
	public Grid2D subsampling2(Grid2D img, int factor)
	{
		int width = (img.getWidth() - 1)/factor + 1;
		int height = (img.getHeight() - 1)/factor + 1;
		Grid2D img2 = new Grid2D(width, height);
		img2.setSpacing(img.getSpacing()[0] * factor + img.getSize()[1] * factor);
		img2.setOrigin(-(img2.getSize()[0] - 1.0) * img2.getSpacing()[0]/2.0, -(img2.getSize()[1] - 1.0) * img2.getSpacing()[1]/2.0);
		for(int i = 0; i < width; i++)
			for(int j = 0; j < height; j++) {
				img2.setAtIndex(i, j, img.getAtIndex(i * factor, j * factor));
			}
		
		return img2;
	}
	
	public Grid2D upsampling(Grid2D img, int factor)
	{
		double[][] y = new double[img.getHeight()][img.getWidth()];
		for(int j = 0; j < img.getHeight(); j++)
			for(int i = 0; i < img.getWidth(); i++)
				y[j][i] = (double)img.getAtIndex(i, j);
		
		double[] x1 = new double[img.getHeight()];
		for(int i = 0; i < img.getHeight(); i++)
		{
			x1[i] = i * img.getSpacing()[1];
		}
		System.out.println(" ");
		
		double[] x2 = new double[img.getWidth()];
		for(int i = 0; i < img.getWidth(); i++)
		{
			x2[i] = i * img.getSpacing()[0];
		}
		System.out.println(" ");
		
		BiCubicInterpolation bci = new BiCubicInterpolation(x1, x2, y, 0);
//		BiCubicSpline bci = new BiCubicSpline(x1, x2, y);
		Grid2D img2 = new Grid2D((img.getWidth() - 1) * factor + 1, (img.getHeight() - 1) * factor + 1);
		img2.setSpacing(img.getSpacing()[0]/factor, img.getSpacing()[1]/factor);
		img2.setOrigin(-(img2.getSize()[0] - 1.0) * img2.getSpacing()[0]/2.0, -(img2.getSize()[1] - 1.0) * img2.getSpacing()[1]/2.0);
		
		double[] xx1 = new double[img2.getHeight()];
		for(int i = 0; i < img2.getHeight(); i++)
			xx1[i] = i * img2.getSpacing()[1];

		
		double[] xx2 = new double[img2.getWidth()];
		for(int i = 0; i < img2.getWidth(); i++)
			xx2[i] = i * img2.getSpacing()[0];
		

		for(int i = 0; i < img2.getWidth(); i++)
			for(int j = 0; j < img2.getHeight(); j++)
			{
				img2.setAtIndex(i, j, (float)(bci.interpolate(xx1[j], xx2[i])));
			}
		
		return img2;
	}
	
	
	public float gridGetAtPhysical(Grid2D img, double x, double y)
	{
		double[] origin = img.getOrigin();
		double idx = (x - origin[0])/img.getSpacing()[0];
		double idy = (y - origin[1])/img.getSpacing()[1];
		int x_floor = (int) Math.floor(idx);
		int x_ceiling = x_floor + 1;
		int y_floor = (int) Math.floor(idy);
		int y_ceiling = y_floor + 1;
		if (x_floor < 0 || x_floor > img.getSize()[0] - 1)
			return 0;
		else if(x_ceiling < 0 || x_ceiling > img.getSize()[0] - 1)
			return 0;
		else if(y_floor < 0 || y_floor > img.getSize()[1] - 1)
			return 0;
		else if(y_ceiling < 0 || y_ceiling > img.getSize()[1] - 1)
			return 0;
		else
		{
			float val = 0;
			float x_res = (float)(idx - x_floor);
			float y_res = (float)(idy - y_floor);
			val = img.getAtIndex(x_floor, y_floor) * (1 - x_res) * (1 - y_res)
					+ img.getAtIndex(x_ceiling, y_floor) * x_res * (1 - y_res)
					+ img.getAtIndex(x_floor, y_ceiling) * (1 - x_res) * y_res
					+ img.getAtIndex(x_ceiling, y_ceiling) * x_res * y_res;
			return val;
		}
	}
	
	public double[] index2physical(Grid2D img, int i, int j)
	{
		double [] idxy = new double[2];
	    if(img.getOrigin()[0] == 0)
	    {
	    	img.setOrigin(-(img.getSize()[0] - 1.0) * img.getSpacing()[0]/2.0, -(img.getSize()[1] - 1.0) * img.getSpacing()[1]/2.0);
	    }
	        idxy[0] = img.getSpacing()[0] * i + img.getOrigin()[0];
	        idxy[1] = img.getSpacing()[1] * j + img.getOrigin()[1];
	        
	        return idxy;
	}
	
	public void resampleGrid(Grid2D output, Grid2D input) {
		double[] idxy;
		for(int i = 0; i < output.getSize()[0]; i++)
			for(int j = 0; j < output.getSize()[1]; j++)
			{
				idxy = index2physical(output, i, j);
				output.setAtIndex(i, j, gridGetAtPhysical(input, idxy[0], idxy[1]));
			}
	}
}
