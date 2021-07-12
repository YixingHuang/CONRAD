package edu.stanford.rsl.Yixing.Perspective;

import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;


public class GenerateBeadPhantoms {

	public static void main(String[] args) {
		new ImageJ();
		BeadPhantom phanObj = new BeadPhantom(512, 512, 512, 100, 0, 100);
		Grid3D phan;
		
//		phan= phanObj.getNumericalPhantom();
//		phan.show("algae");
		
		ImagePlus imp;
		String saveFolderPath = "C:\\Perspective\\phantomData\\";
		String saveName;
		for(int i = 101; i <= 200; i++)
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
