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
		String folder = "D:\\Tasks\\FAU4\\CellImaging\\FbpCellRecons100DegreePwls\\";
		String path = folder + "reconFbp3DCombinedU1P5Pwls.tif";
		String path2 = folder + "sagFBP3D.tif";
		String path3 = folder + "corFBP3D.tif";
		String path4 = folder + "reconFbp3DPos.tif";
		ImagePlus imp0 =IJ.openImage(path);
		Grid3D vol = ImageUtil.wrapImagePlus(imp0);
		vol.clone().show("horizontal");
//		Grid3D vol = obj.downSamplingZ(vol);
		vol = obj.downSamplingZ(vol);
		vol.setSpacing(1, 1, 1);
		Grid3D sag = obj.sagittalVolume(vol);
		sag.clone().show("sag");
		
		Grid3D coronal = obj.coronalVolume(vol);
		coronal.clone().show("coronal");
		vol.getGridOperator().removeNegative(vol);
//		imp0 = ImageUtil.wrapGrid3D(sag, null);
//		IJ.saveAs(imp0, "Tiff", path2);
//		imp0 = ImageUtil.wrapGrid3D(coronal, null);
//		IJ.saveAs(imp0, "Tiff", path3);
//		imp0 = ImageUtil.wrapGrid3D(vol, null);
//		IJ.saveAs(imp0, "Tiff", path4);
	}
		

	
	private Grid3D sagittalVolume(Grid3D volume){
		
		Grid3D sag = new Grid3D(volume.getSize()[1], volume.getSize()[2], volume.getSize()[0]);
		for(int x = 0; x < volume.getSize()[0]; x++){
			for(int y = 0; y < volume.getSize()[1]; y++) {
				for(int z  = 0; z < volume.getSize()[2]; z++) {
					sag.setAtIndex(y, z, x, volume.getAtIndex(x, y, z));
				}
			}
		}
		
		return sag;	
	}
	
	private Grid3D coronalVolume(Grid3D volume){
		
		Grid3D coroal = new Grid3D(volume.getSize()[0], volume.getSize()[2], volume.getSize()[1]);
		for(int x = 0; x < volume.getSize()[0]; x++){
			for(int y = 0; y < volume.getSize()[1]; y++) {
				for(int z  = 0; z < volume.getSize()[2]; z++) {
					coroal.setAtIndex(x, z, y, volume.getAtIndex(x, y, z));
				}
			}
		}
		
		return coroal;	
	}
	
	private Grid3D downSamplingZ(Grid3D vol)
	{
		Grid3D vol2 = new Grid3D(vol.getSize()[0], vol.getSize()[1], vol.getSize()[2]/2);
		for(int i = 0; i < vol2.getSize()[2]; i++)
			vol2.setSubGrid(i, (Grid2D)vol.getSubGrid(i * 2).clone());
		
		return vol2;
	}
}
