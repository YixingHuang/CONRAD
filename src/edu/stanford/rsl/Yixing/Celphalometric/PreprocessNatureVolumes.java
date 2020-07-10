package edu.stanford.rsl.Yixing.Celphalometric;

import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class PreprocessNatureVolumes {

	public static void main(String[] args) throws IOException{
		new ImageJ();
		PreprocessNatureVolumes obj = new PreprocessNatureVolumes();
		String inputPath = "E:\\CQ500CTData\\LowerHalf\\old\\data500.tif";
		ImagePlus imp = IJ.openImage(inputPath);
		Grid3D vol = ImageUtil.wrapImagePlus(imp);
		vol.clone().show("vol");
//		int[] pos = new int[] {17, 495, 0, 146, 105, 217};
//		int[] pos = new int[] {88, 421, 0, 227, 94, 154};
//		int[] pos = new int[] {109, 403, 0, 123, 72, 103};
//		int[] pos = new int[] {147, 387, 0, 143, 78, 113};
//		int[] pos = new int[] {155, 399, 0, 141, 113, 176};
//		int[] pos = new int[] {121, 389, 0, 144, 56, 98};
//		int[] pos = new int[] {121, 389, 0, 148, 73, 141};
//		int[] pos = new int[] {121, 396, 0, 164, 37, 69};
//		int[] pos = new int[] {126, 401, 0, 169, 142, 226};
		int[] pos = new int[] {103, 422, 0, 176, 127, 212};
		obj.setBlock2Zero(vol, pos);
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
	
	public void setBlock2Zero(Grid3D vol, int[] pos)
	{
		for(int i = pos[0]; i < pos[1]; i ++)
			for(int j = pos[2]; j < pos[3]; j++)
				for(int k = pos[4]; k < pos[5]; k++)
				vol.setAtIndex(i, j, k, 0);
	}
}
