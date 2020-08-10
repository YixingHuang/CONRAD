package edu.stanford.rsl.Yixing.Celphalometric.processing;

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
		String inputPath = "E:\\CQ500CTData\\UpperHalf\\data310.tif";
		ImagePlus imp = IJ.openImage(inputPath);
		Grid3D vol = ImageUtil.wrapImagePlus(imp);
		vol.clone().show("vol");
//		obj.thresholding(vol, 400);
		vol = obj.removeTop(vol, 120);
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
	
	private Grid3D removeTop(Grid3D vol, int numZ) {
		Grid3D vol2 = new Grid3D(vol.getSize()[0], vol.getSize()[1], vol.getSize()[2] - numZ);
		for(int i = 0; i < vol2.getSize()[0]; i++)
			for(int j = 0; j < vol2.getSize()[1]; j++)
				for(int k = 0; k < vol2.getSize()[2]; k++)
					vol2.setAtIndex(i, j, k, vol.getAtIndex(i, j, k + numZ));
		
		return vol2;
	}
}

