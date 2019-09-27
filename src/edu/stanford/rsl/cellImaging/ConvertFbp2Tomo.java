package edu.stanford.rsl.cellImaging;

/**
 * @author Yixing Huang
 * Split the 3D volume into small slices for training
 */
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import java.io.File;
import java.io.IOException;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import edu.stanford.rsl.tutorial.weightedtv.Perform2DWeightedTV;
import edu.stanford.rsl.tutorial.weightedtv.TVGradient;


public class ConvertFbp2Tomo {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		
		ConvertFbp2Tomo obj = new ConvertFbp2Tomo();
		String path = "D:\\Tasks\\FAU4\\CellImaging\\Tifs\\DCRPwls.tif";
		String path2 = "D:\\Tasks\\FAU4\\CellImaging\\Tifs\\tomoDCRPwls.tif";
		ImagePlus imp0 =IJ.openImage(path);
		Grid3D vol = ImageUtil.wrapImagePlus(imp0);
		Grid3D vol2 = obj.downSamplingZ(vol);
		Grid3D tomo = obj.reorderVolume(vol2);
		tomo.clone().show("tomo");
		
		imp0 = ImageUtil.wrapGrid3D(tomo, null);
		IJ.saveAs(imp0, "Tiff", path2);
	}
		

	
	private Grid3D reorderVolume(Grid3D proj){
		
		Grid3D sino = new Grid3D(proj.getSize()[1], proj.getSize()[2], proj.getSize()[0]);
		for(int i = 0; i < proj.getSize()[0]; i++){
			for(int j = 0; j < proj.getSize()[1]; j++) {
				for(int k  = 0; k < proj.getSize()[1]; k++) {
					sino.setAtIndex(k, j, i, proj.getAtIndex(i, j, k));
				}
			}
		}
		
		return sino;	
	}
	
	private Grid3D downSamplingZ(Grid3D vol)
	{
		Grid3D vol2 = new Grid3D(vol.getSize()[0], vol.getSize()[1], vol.getSize()[2]/2);
		for(int i = 0; i < vol2.getSize()[2]; i++)
			vol2.setSubGrid(i, (Grid2D)vol.getSubGrid(i * 2).clone());
		
		return vol2;
	}
}
