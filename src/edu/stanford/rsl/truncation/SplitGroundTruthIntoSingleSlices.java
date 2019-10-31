package edu.stanford.rsl.truncation;

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
import edu.stanford.rsl.conrad.utils.ImageUtil;

public class SplitGroundTruthIntoSingleSlices {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		
		String path = "D:\\wTVprocessedData\\";
		String path2 = "D:\\Tasks\\FAU4\\TruncationCorrection\\gtSlices\\";
		String path3;
		ImagePlus imp1;
		String name1;
		Grid3D data;
		Grid2D data2D;

		int gidx = 0;
		for(int idx = 1; idx <= 18; idx ++){
			name1 = path + idx + ".tif";
			imp1=IJ.openImage(name1);
			data = ImageUtil.wrapImagePlus(imp1);
			data.getGridOperator().divideBy(data, 2000f);
			for(int i = 0; i < data.getSize()[2]; i = i + 10) {
				System.out.println( idx + ", " + i);
				path3 = path2 + gidx + ".tif";
				data2D = data.getSubGrid(i);
			    imp1 = ImageUtil.wrapGrid(data2D, null);
			    IJ.saveAs(imp1, "Tiff", path3);
			    gidx++;

			}

		}
		
	}
		
}
