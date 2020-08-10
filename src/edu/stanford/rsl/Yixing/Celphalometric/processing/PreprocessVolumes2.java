package edu.stanford.rsl.Yixing.Celphalometric.processing;

import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class PreprocessVolumes2 {

	public static void main(String[] args) throws IOException{
		new ImageJ();
		PreprocessVolumes2 obj = new PreprocessVolumes2();
		String inputPath = "E:\\CQ500CTData\\completeHeadData\\data183.tif";
		ImagePlus imp = IJ.openImage(inputPath);
		Grid3D vol = ImageUtil.wrapImagePlus(imp);
		vol.clone().show("vol");
		obj.thresholding(vol, 100, 0);
		vol.clone().show("vol2");
		obj.removePillow(vol, 45f);
		vol.clone().show("vol3");
		
		String inputPath2 = "E:\\CQ500CTData\\completeHeadData\\0p2.tif";
		ImagePlus imp2 = IJ.openImage(inputPath2);
		Grid3D p2 = ImageUtil.wrapImagePlus(imp2);
		p2.clone().show("p2");
		p2.getGridOperator().addBy(p2, vol);
		p2.clone().show("p3");
	}
	
	public void thresholding(Grid3D vol, float thres1, float thres2) {
		for(int i = 0; i < vol.getSize()[0]; i++)
			for(int j = 0; j < vol.getSize()[1]; j++)
				for(int k = 0; k < vol.getSize()[2]; k++) {
					if(vol.getAtIndex(i, j, k) >= thres1 || vol.getAtIndex(i, j, k) < thres2)
						vol.setAtIndex(i, j, k, 0);
				}
	}
	
	public void removePillow(Grid3D vol, float thres) {
		for(int i = 0; i < vol.getSize()[0]; i++)
			for(int j = 300; j < vol.getSize()[1]; j++)
				for(int k = 0; k < vol.getSize()[2]; k++) {
					if(vol.getAtIndex(i, j, k) >= thres)
						vol.setAtIndex(i, j, k, 0);
				}
	}
}
