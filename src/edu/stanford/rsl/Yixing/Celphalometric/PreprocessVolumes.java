package edu.stanford.rsl.Yixing.Celphalometric;

import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class PreprocessVolumes {

	public static void main(String[] args) throws IOException{
		new ImageJ();
		PreprocessVolumes obj = new PreprocessVolumes();
		String inputPath = "E:\\CQ500CTData\\completeHeadData\\data241p.tif";
		ImagePlus imp = IJ.openImage(inputPath);
		Grid3D vol = ImageUtil.wrapImagePlus(imp);
		vol.clone().show("vol");
		obj.thresholding(vol, 400);
		vol.show("vol2");
	}
	
	public void thresholding(Grid3D vol, float thres) {
		for(int i = 0; i < vol.getSize()[0]; i++)
			for(int j = 0; j < vol.getSize()[1]; j++)
				for(int k = 0; k < vol.getSize()[2]; k++) {
					if(vol.getAtIndex(i, j, k) < thres)
						vol.setAtIndex(i, j, k, 0);
				}
	}
}
