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

public class AddMetalMarker2Volumes {

	public static void main(String[] args) throws IOException{
		new ImageJ();
		AddMetalMarkers marker = new AddMetalMarkers(768, 768, 300, 250, 250);
		AddMetalMarker2Volumes obj = new AddMetalMarker2Volumes();
		Grid3D vol2;
		for(int idx = 18; idx <= 18; idx++)
		{
			String sinoPath = "E:\\SiemensMarkerData\\volumes\\" + idx + ".tif";
			ImagePlus imp0 =IJ.openImage(sinoPath);
			Grid3D vol = ImageUtil.wrapImagePlus(imp0);
			vol2 = marker.addMarkers(vol);
//			vol2 = marker.getMarkers();
			vol2.show("vol2");
			String savePath = "E:\\SiemensMarkerData\\volumesWithMarker\\" + idx + ".tif";
			imp0 = ImageUtil.wrapGrid(vol2, null);
			IJ.saveAs(imp0, "Tiff", savePath);
			System.out.print(idx + " ");
		
		}
		System.out.println("Done!");
	}
	
	
}
