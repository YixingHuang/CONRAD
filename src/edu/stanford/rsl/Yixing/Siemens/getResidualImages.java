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

public class getResidualImages {
	
	public static void main(String[] args) throws IOException{
		new ImageJ();
		
		for(int idx = 1; idx <= 18; idx++)
		{
			String fbpPath = "E:\\SiemensMarkerData\\recon\\reconTruncated" + idx + ".tif";
			ImagePlus imp0 =IJ.openImage(fbpPath);
			Grid3D vol = ImageUtil.wrapImagePlus(imp0);
			vol.getGridOperator().multiplyBy(vol, 2.0f);
			imp0 = ImageUtil.wrapGrid(vol, null);
			IJ.saveAs(imp0, "Tiff", fbpPath);
			
			String gtPath = "E:\\SiemensMarkerData\\reconsGT\\" + idx + ".tif";
			imp0 =IJ.openImage(gtPath);
			Grid3D volGT = ImageUtil.wrapImagePlus(imp0);
			vol.getGridOperator().subtractBy(vol, volGT);
			String savePath = "E:\\SiemensMarkerData\\recon\\artifact" + idx + ".tif";
			imp0 = ImageUtil.wrapGrid(vol, null);
			IJ.saveAs(imp0, "Tiff", savePath);
			System.out.print(idx + " ");
		}
		
		System.out.println("Done!");
	}
	
	
}
