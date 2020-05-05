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

public class getGroundTruthRecons {
	
	public static void main(String[] args) throws IOException{
		new ImageJ();
		
		for(int idx = 2; idx <= 18; idx++)
		{
			String sinoPath = "E:\\SiemensMarkerData\\volumesWithMarker\\" + idx + ".tif";
			ImagePlus imp0 =IJ.openImage(sinoPath);
			Grid3D vol = ImageUtil.wrapImagePlus(imp0);

			Grid3D vol2 = new Grid3D(256, 256, 256);
			float val;
			for(int i = 0; i < vol2.getSize()[0]; i++)
				for(int j = 0; j < vol2.getSize()[1]; j++)
					for(int k = 0; k < vol2.getSize()[2]; k++)
					{
						val = 0;
						for(int m = 0; m < 2; m++)
							for(int n = 0; n < 2; n++)
							{
								val += vol.getAtIndex(i * 2 + 128, j * 2 + 128, k + 22);
							}
						val = val/4.0f;
						vol2.setAtIndex(i, j, k, val);
					}
			vol2.getGridOperator().subtractBy(vol2, 30);
			vol2.getGridOperator().divideBy(vol2, 2040);
			String savePath = "E:\\SiemensMarkerData\\reconsGT\\" + idx + ".tif";
			imp0 = ImageUtil.wrapGrid(vol2, null);
			IJ.saveAs(imp0, "Tiff", savePath);
		}
		
		System.out.println("Done!");
	}
	
	
}
