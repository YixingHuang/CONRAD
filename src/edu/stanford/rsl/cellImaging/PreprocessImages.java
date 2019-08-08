package edu.stanford.rsl.cellImaging;


import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class PreprocessImages {

	public static void main(String[] args) {
		new ImageJ();
		
		String imagePath1 = "D:\\Tasks\\FAU4\\CellImaging\\AlgaeCellsProcessed\\3.tif";
		String imagePath2 = "D:\\Tasks\\FAU4\\CellImaging\\AlgaeCells\\37.png";
		
		PreprocessImages obj = new PreprocessImages();
		ImagePlus imp;
		imp = IJ.openImage(imagePath1);
		Grid2D imgRaw = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		float maxV = imgRaw.getGridOperator().max(imgRaw);
		imgRaw.getGridOperator().divideBy(imgRaw, maxV);
//		imgRaw = obj.downSampling(imgRaw);
//		imgRaw = obj.downSampling(imgRaw);
//		imgRaw = obj.downSampling(imgRaw);
//		imgRaw.getGridOperator().multiplyBy(imgRaw, -1.f);
//		imgRaw.getGridOperator().addBy(imgRaw, 1.0f);
		obj.thresholding(imgRaw, 0.11f);
		imgRaw.show();
		
		
		Grid2D img = new Grid2D(512, 512);
		int xDim = imgRaw.getSize()[0];
		int yDim = imgRaw.getSize()[1];
		
		int dim = 512;
		float originX = (dim - 1)/2;
		float originY = (dim - 1)/2;
		if(xDim >= dim && yDim >= dim)
		{
			int startX = 0;
			int startY = 0;
			for(int i = 0; i < dim; i++)
				for(int j = 0; j < dim; j++)
					img.setAtIndex(i, j, imgRaw.getAtIndex(startX + i, startY + j));
			img.clone().show("cropped image");
			
			obj.keepCircularROI(img, 312, 359, 120, 121, 0, 1.0f);
			img.clone().show("ROI image");
			
		}
		else
		{
			int xShift = 0;
			int yShift = 0;
			int xRawShift = 0;
			int yRawShift = 0;
			int xDimMin = dim, yDimMin = dim;
			if(xDim < dim)
			{
				xShift = (int) ((dim - xDim)/2);
				xDimMin = xDim;
				
			}
			if(yDim < dim)
			{
				yShift = (int) ((dim - yDim)/2);
				yDimMin = yDim;
			}
			for(int i = 0; i < xDimMin; i++)
				for(int j = 0; j < yDimMin; j++)
				{
					img.setAtIndex(i + xShift, j + yShift, imgRaw.getAtIndex(i + xRawShift, j + yRawShift));
				}
			img.clone().show("padded image");
			
			obj.keepCircularROI(img, 263, 259, 103.0f, 100, -30 * Math.PI/ 180, 0.9f);
			img.clone().show("ROI image");
		}
	}
	
	public void keepCircularROI(Grid2D img, float centX, float centY, float ra, float rb, double theta, float val)
	{
		float rra = ra * ra;
		float rrb = rb * rb;
		float dx = 4 + (float)(Math.random() - 0.5) * 2.0f;
		float rra2 = (ra + dx) * (ra + dx);
		float rrb2 = (rb + dx) * (rb + dx);
		double a, b, rota, rotb, aa, bb;
		double cosTheta = Math.cos(theta);
		double sinTheta = Math.sin(theta);
		for(int i = 0; i < img.getSize()[0]; i++)
			for(int j = 0; j < img.getSize()[1]; j++) {
				a = (i - centX);
				b = (j - centY);
				rota = a * cosTheta - b * sinTheta;
				rotb = a * sinTheta + b * cosTheta;
				aa = rota * rota;
				bb = rotb * rotb;
				if(aa/rra2 + bb/rrb2 > 1)
					img.setAtIndex(i, j, 0);
				else if(aa/rra + bb/rrb > 1)
					img.setAtIndex(i, j, val);
			}
				
	}
	
	public Grid2D downSampling(Grid2D img)
	{
		int sizeX = img.getSize()[0];
		int sizeY = img.getSize()[1];
		int sizeX2 = sizeX/2;
		int sizeY2 = sizeY/2;
		Grid2D imgDown = new Grid2D(sizeX2, sizeY2);
		float val = 0;
		for(int i = 0; i < sizeX2; i++)
			for(int j = 0; j < sizeY2; j++)
			{
				val = 0;
				for(int m = 0; m <2; m++)
					for(int n = 0; n < 2; n++)
						val = val + img.getAtIndex(i * 2 + m, j * 2 + n);
				val = val/4.0f;
				imgDown.setAtIndex(i, j, val);
			}
		return imgDown;			
	}
	
	public void thresholding(Grid2D img, float thres)
	{
		for(int i = 0; i < img.getSize()[0]; i++)
			for(int j = 0; j < img.getSize()[1]; j++)
				if(img.getAtIndex(i, j) < thres)
					img.setAtIndex(i, j, 0);
	}
}
