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

public class RotateAndShiftVolumes {

	public static void main(String[] args) throws IOException{
		new ImageJ();
		for(int idx = 2; idx <= 18; idx++)
		{
			String sinoPath = "E:\\SiemensMarkerData\\wTVprocessedData2\\" + idx + ".tif";
			ImagePlus imp0 =IJ.openImage(sinoPath);
			Grid3D vol = ImageUtil.wrapImagePlus(imp0);
			RotateAndShiftVolumes obj = new RotateAndShiftVolumes();
			Grid3D vol2 = new Grid3D(768, 768, 300);	
			for(int i = 0; i < vol.getSize()[0]; i++)
				for(int j = 0; j < vol.getSize()[1]; j++)
					for(int k = 0; k < vol2.getSize()[2]; k++)
					{
						vol2.setAtIndex(512 - j + 256, i + 64, k, vol.getAtIndex(i, j, k));
					}
			
		//	vol2.show("vol2");
			String savePath = "E:\\SiemensMarkerData\\volumes\\" + idx + ".tif";
			imp0 = ImageUtil.wrapGrid(vol2, null);
			IJ.saveAs(imp0, "Tiff", savePath);
			System.out.print(idx + " ");
		
		}
		System.out.println("Done!");
	}
	
	
}
