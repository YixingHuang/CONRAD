package edu.stanford.rsl.Yixing.Celphalometric;

import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import flanagan.interpolation.*;

public class GenerateSuperResolutionTrainingImagesLR2 {
	/**
	 * Keep the original size of low resolutions
	 * Fixed the image origins
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException{
		new ImageJ();
		GenerateSuperResolutionTrainingImagesLR2 obj = new GenerateSuperResolutionTrainingImagesLR2();
		String path = "D:\\Tasks\\FAU4\\MasterProjectOfFan\\RawImage\\TrainingData\\";
		String savePath = "D:\\Tasks\\FAU4\\Cephalometric\\SuperResolutionImages2\\";
		String name;
		ImagePlus imp;
		Grid2D gt;
		Grid2D ds, us, dds;
		int factor = 5;
		int factor2 = 2;
		Grid2D gtcopy;
		String imgNameIn, imgNameIn2, imgNameOut;
		
		int[] size0 = new int[]{1935, 2400};
		double[] spacing0 = new double[] {0.1, 0.1};
		double[] origin0 = new double[]{-(size0[0] - 1.0) * spacing0[0]/2.0, -(size0[1] - 1.0) * spacing0[1]/2.0};
		int width = (size0[0] - 1)/factor + 1;
		int height = (size0[1] - 1)/factor + 1;
		us = new Grid2D(width, height);
		us.setSpacing(0.1 * factor, 0.1 * factor);
		us.setOrigin(origin0[0] + spacing0[0] * (factor - 1)/2.0, origin0[1] + spacing0[1] * (factor - 1)/2.0);
		for(int idx = 1; idx <=150; idx ++) {
			String idxS = String.format("%03d", idx);
			name = path + idxS + ".bmp";
			imp = IJ.openImage(name);
			gt = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
			gt.setSpacing(spacing0);
			gt.setOrigin(origin0);
//			gt.clone().show("gt");
			ds = obj.subsampling(gt,  factor);
			dds = obj.subsampling(ds, factor2);
			obj.upsampling2(dds, us);
//			imp = ImageUtil.wrapGrid(ds, null);
//			imgNameIn = savePath + "data" + idx + ".png";
//			imp.setDisplayRange(0, 255);
//			IJ.saveAs(imp, "png", imgNameIn);
			
			imp = ImageUtil.wrapGrid(us, null);
			imgNameIn2 = savePath + "data" + idx + "_rs" + factor2 + ".png";
			imp.setDisplayRange(0, 255);
			IJ.saveAs(imp, "png", imgNameIn2);
			
//			imp = ImageUtil.wrapGrid(gt, null);
//			imp.setDisplayRange(0, 255);
//			imgNameOut = savePath + "data" + idx + "_mask.png";
//			IJ.saveAs(imp, "png", imgNameOut);
			System.out.print(" " + idx);
		}
		
	}
	
	public Grid2D reoverSize(Grid2D img, int[] size) {
		Grid2D img2 = new Grid2D(size[0], size[1]);
		img2.setSpacing(img.getSpacing());
		img2.setOrigin((-img2.getSize()[0] - 1.0) * img2.getSpacing()[0]/2.0, -(img2.getSize()[1] - 1.0) * img2.getSpacing()[1]/2.0);
		if(img.getSize()[0] > size[0] || img.getSize()[1] > size[1])
			System.out.println("Input image size is larger than the output size, input size = [" + img.getSize()[0] + "," + img.getSize()[1] + "].");
		for(int i = 0; i < img.getSize()[0]; i++)
			for(int j = 0; j < img.getSize()[1]; j++)
				img2.setAtIndex(i, j, img.getAtIndex(i, j));
		return img2;
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
		img2.setOrigin(img.getOrigin()[0] + img.getSpacing()[0] * (factor - 1)/2.0, img.getOrigin()[1] + img.getSpacing()[1] * (factor - 1)/2.0);
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
	
	/**
	 * 
	 * @param img
	 * @param factor
	 * @return
	 */
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
	
	/**
	 * Upsampling using bicubic interpolation
	 * @param imgInput
	 * @param imgOutPut
	 */
	public void upsampling2(Grid2D imgInput, Grid2D imgOutPut)
	{
		double[][] y = new double[imgInput.getHeight()][imgInput.getWidth()];
		for(int j = 0; j < imgInput.getHeight(); j++)
			for(int i = 0; i < imgInput.getWidth(); i++)
				y[j][i] = (double)imgInput.getAtIndex(i, j);
		
		double[] x1 = new double[imgInput.getHeight()];
		for(int i = 0; i < imgInput.getHeight(); i++)
		{
			x1[i] = i * imgInput.getSpacing()[1] + imgInput.getOrigin()[1];
		}
		
		double[] x2 = new double[imgInput.getWidth()];
		for(int i = 0; i < imgInput.getWidth(); i++)
		{
			x2[i] = i * imgInput.getSpacing()[0] + imgInput.getOrigin()[0];
		}
		
		BiCubicInterpolation bci = new BiCubicInterpolation(x1, x2, y, 0);
//		BiCubicSpline bci = new BiCubicSpline(x1, x2, y);
		double x1Min = imgInput.getOrigin()[1];
		double x1Max = imgInput.getOrigin()[1] + imgInput.getSpacing()[1] * (imgInput.getSize()[1] - 1);
		double x2Min = imgInput.getOrigin()[0];
		double x2Max = imgInput.getOrigin()[0] + imgInput.getSpacing()[0] * (imgInput.getSize()[0] - 1);
		double[] xx1 = new double[imgOutPut.getHeight()];
		
		for(int i = 0; i < imgOutPut.getHeight(); i++)
			xx1[i] = i * imgOutPut.getSpacing()[1] + imgOutPut.getOrigin()[1];

		double[] xx2 = new double[imgOutPut.getWidth()];
		for(int i = 0; i < imgOutPut.getWidth(); i++)
			xx2[i] = i * imgOutPut.getSpacing()[0] + imgOutPut.getOrigin()[0];
		
		for(int i = 0; i < imgOutPut.getWidth(); i++)
			for(int j = 0; j < imgOutPut.getHeight(); j++)
			{
				if(xx1[j] < x1Min || xx1[j] > x1Max || xx2[i] < x2Min || xx2[i] > x2Max)
					imgOutPut.setAtIndex(i, j, 0);
				else
					imgOutPut.setAtIndex(i, j, (float)(bci.interpolate(xx1[j], xx2[i])));
			}
		
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
