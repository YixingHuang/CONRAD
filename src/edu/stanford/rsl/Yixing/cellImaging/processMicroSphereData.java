package edu.stanford.rsl.Yixing.cellImaging;

import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.opencl.OpenCLGrid3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class processMicroSphereData {

	public static void main (String [] args) throws Exception{
		new ImageJ();
		processMicroSphereData obj = new processMicroSphereData();
		String path = "D:\\Tasks\\FAU4\\CellImaging\\microsphere.tif";
		ImagePlus imp0 =IJ.openImage(path);
		Grid3D vol = ImageUtil.wrapImagePlus(imp0);
		Grid3D volDown = obj.downSampling(vol);
		OpenCLGrid3D volCL = new OpenCLGrid3D(volDown);
		volCL.getGridOperator().subtractBy(volCL, 18259);
		//volCL.getGridOperator().removeNegative(volCL);
		volCL.getGridOperator().divideBy(volCL, 10000);
		//volCL.show();
		Grid3D volPad = obj.zeroPadding(volCL, 512);
		Grid3D vol2 = obj.rescaleData(volPad, 0.94f, 0.94f);
		vol2.show("vol2");
	}
	
	Grid3D downSampling(Grid3D vol) {
		Grid3D volDown = new Grid3D(vol.getSize()[0]/2, vol.getSize()[1]/2, vol.getSize()[2]/2);
		float val = 0;
		for(int i = 0; i < volDown.getSize()[0]; i++)
			for(int j = 0; j < volDown.getSize()[1]; j++)
				for(int k = 0; k < volDown.getSize()[2]; k++)
				{
					val = 0;
					for(int ii = 0; ii < 2; ii++)
						for(int jj = 0; jj < 2; jj++)
							for(int kk = 0; kk < 2; kk++)
							{
								val = val + vol.getAtIndex(i * 2 + ii, j * 2 + jj, k * 2 + kk);
							}
					volDown.setAtIndex(i, j, k, val/8.0f);
				}
		
		return volDown;
	}
	
	Grid3D zeroPadding(Grid3D vol, int size) {
		Grid3D volPad = new Grid3D(size, size, size);
		int xIdx = (size - vol.getSize()[0])/2;
		int yIdx = (size - vol.getSize()[1])/2;
		int zIdx = (size - vol.getSize()[2])/2;
		for(int i = 0; i < vol.getSize()[0]; i ++)
			for(int j = 0; j < vol.getSize()[1]; j ++)
				for(int k = 0; k < vol.getSize()[2]; k++)
					volPad.setAtIndex(i + xIdx, j + yIdx, k + zIdx, vol.getAtIndex(i, j, k));
		
		return volPad;
	}
	
	Grid3D rescaling(Grid3D vol, int size) {
		Grid3D volPad = new Grid3D(size, size, vol.getSize()[2]);
		int xIdx = (size - vol.getSize()[0])/2;
		int yIdx = (size - vol.getSize()[1])/2;
		int zIdx = (size - vol.getSize()[2])/2;
		for(int i = 0; i < vol.getSize()[0]; i ++)
			for(int j = 0; j < vol.getSize()[1]; j ++)
				for(int k = 0; k < vol.getSize()[2]; k++)
					volPad.setAtIndex(i + xIdx, j + yIdx, k + zIdx, vol.getAtIndex(i, j, k));
		
		return volPad;
	}
	
	Grid3D cropping(Grid3D vol, int hPixels, int wPixels) {
		Grid3D volCropp = new Grid3D(vol.getSize()[0] - wPixels * 2, vol.getSize()[1] - hPixels * 2, vol.getSize()[2]);
		for(int i = 0; i < volCropp.getSize()[0]; i ++)
			for(int j = 0; j < volCropp.getSize()[1]; j++)
				for(int k = 0; k < volCropp.getSize()[2]; k++)
					volCropp.setAtIndex(i, j, k, vol.getAtIndex(i + wPixels, j + hPixels, k));
		
		return volCropp;
	}
	
	Grid3D rescaleData(Grid3D vol, float s0, float s1) {
		Grid3D vol2 = new Grid3D(vol.getSize()[0], vol.getSize()[1], vol.getSize()[2]);
		int wPixels = (int)(vol.getSize()[0] * (1 - s0)/2);
		int hPixels = (int)(vol.getSize()[1] * (1 - s1)/2);
		float x,y, xf, yf;
		int xi, yi;
		float val;
		for(int i = 0; i < vol2.getSize()[0]; i++)
			for(int j = 0; j < vol2.getSize()[1]; j++)
				for(int k = 0; k < vol2.getSize()[2]; k++)
				{
					x = i * s0 + wPixels;
					xi = (int)(x);
					xf = x - xi;
					
					y = j * s1 + hPixels;
					yi = (int)(y);
					yf = y - yi;
					
					val = xf * yf * vol.getAtIndex(xi + 1, yi+1, k) 
							+ (1 - xf) * (1 - yf) * vol.getAtIndex(xi, yi, k)
							+ xf * (1 - yf) * vol.getAtIndex(xi + 1, yi, k) 
							+ (1 - xf) * yf * vol.getAtIndex(xi, yi + 1, k);
					vol2.setAtIndex(i, j, k, val);
				}
		return vol2;
	}
}
