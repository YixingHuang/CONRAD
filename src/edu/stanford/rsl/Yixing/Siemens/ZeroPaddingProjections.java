package edu.stanford.rsl.Yixing.Siemens;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.OvalRoi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.NumericPointwiseOperators;
import edu.stanford.rsl.conrad.utils.DoubleArrayUtil;
import edu.stanford.rsl.conrad.utils.ImageUtil;

public class ZeroPaddingProjections {
	private int numTrunc = 100;
	
	public static void main(String[] args) throws IOException{
		new ImageJ();
		String sinoPath = "E:\\SiemensMarkerData\\projection.tif";
		ImagePlus imp0 =IJ.openImage(sinoPath);
		Grid3D sinogram = ImageUtil.wrapImagePlus(imp0);
		ZeroPaddingProjections obj = new ZeroPaddingProjections();
		Grid3D sinogram2 = new Grid3D(sinogram.getSize()[0] + obj.numTrunc * 2, sinogram.getSize()[1], sinogram.getSize()[2]);	
		for(int i = 0; i < sinogram.getSize()[0]; i++)
			for(int j = 0; j < sinogram.getSize()[1]; j++)
				for(int k = 0; k < sinogram.getSize()[2]; k++)
				{
					sinogram2.setAtIndex(i + obj.numTrunc, j, k, sinogram.getAtIndex(i, j, k));
				}
		
		String savePath = "E:\\SiemensMarkerData\\projectionPadded.tif";
		imp0 = ImageUtil.wrapGrid(sinogram2, null);
		IJ.saveAs(imp0, "Tiff", savePath);
		System.out.println("Done!");
	}
	
	
}
