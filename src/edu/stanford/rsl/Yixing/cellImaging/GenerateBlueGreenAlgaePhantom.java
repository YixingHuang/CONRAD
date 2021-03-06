package edu.stanford.rsl.Yixing.cellImaging;

import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;


public class GenerateBlueGreenAlgaePhantom {

	public static void main(String[] args) {
		new ImageJ();
		BlueGreenAlgaePhantom phanObj = new BlueGreenAlgaePhantom(512, 512, 512, 100, 100, 200000);
		Grid3D phan;
		
//		phan= phanObj.getNumericalPhantom();
//		phan.show("algae");
		
		ImagePlus imp;
		String saveFolderPath = "D:\\Tasks\\FAU4\\CellImaging\\FOVRecon120\\AlgaePhantoms\\";
		String saveName;
		for(int i = 30; i <= 39; i++)
		{
			phanObj.resetVolume();
			phan = phanObj.getNumericalPhantom();
			imp = ImageUtil.wrapGrid3D(phan, null);
			saveName = saveFolderPath + i + ".tif";
			IJ.saveAs(imp, "Tiff", saveName);
			System.out.print(" " + i);
		}
	}
	
}
