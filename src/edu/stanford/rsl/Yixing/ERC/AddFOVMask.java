package edu.stanford.rsl.Yixing.ERC;

import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.opencl.OpenCLGrid3D;
import edu.stanford.rsl.conrad.utils.DoubleArrayUtil;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import edu.stanford.rsl.tutorial.pwls.PenalizedWeightedLeastSquare;

public class AddFOVMask {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		
		String path = "D:\\Dropbox\\Dropbox\\BVM2021\\Autorenkit-BVM2020\\";
		String projName, saveName;
		ImagePlus imp0;
		Grid2D img;
		AddFOVMask obj = new AddFOVMask();
		String refPath = path + "reference.png";
		imp0 =IJ.openImage(refPath);
		Grid2D ref = ImageUtil.wrapImagePlus(imp0).getSubGrid(0);
		double ssim;
		for(int i = 1; i <= 8; i++)
		{
			projName = path + "FDK" + i + ".png";
			imp0 =IJ.openImage(projName);
			img = ImageUtil.wrapImagePlus(imp0).getSubGrid(0);
//			proj0.show("projections");
			obj.addFOV(img, 590.0f);
			
			ssim = obj.calSSIM(ref, img);
			System.out.println("i = " + i + ": " + ssim);
//			saveName = path + "RPWLS" + i + ".png";	
//			imp0 = ImageUtil.wrapGrid(img, null);
//			imp0.setDisplayRange(0, 255);
//		    IJ.saveAs(imp0, "png", saveName);
//		    System.out.println(i);
		}
		
	    System.out.println("finished");
	}
	
	public double calSSIM(Grid2D img1, Grid2D img2) {
		ImagePlus Imp1 = ImageUtil.wrapGrid(img1, null);
		ImagePlus Imp2 = ImageUtil.wrapGrid(img2, null);
		double returnSSIMDoubleValue = 0;
		int lengthOfImp1 = Imp1.getWidth() * Imp1.getHeight();
		int lengthOfImp2 = Imp2.getWidth() * Imp2.getHeight();
		if (lengthOfImp1 != lengthOfImp2) {
			System.out.println("[!] Error: ROI sizes are not equal!");
		} else {
			double[] xCoord = new double[lengthOfImp1];
			double[] yCoord = new double[lengthOfImp2];
			int XnonFinitiyCounts = 0;
			int YnonFinitiyCounts = 0;
			for (int i = 0; i < lengthOfImp1; i++) {

				if (Float.isFinite(Imp1.getProcessor().getf(i))) {
					xCoord[i] = Imp1.getProcessor().getf(i);
				} else {// kick out non-Finite values of Imp1
					XnonFinitiyCounts = XnonFinitiyCounts + 1;
					xCoord[i] = 0;
				}

				if (Float.isFinite(Imp2.getProcessor().getf(i))) {
					yCoord[i] = Imp2.getProcessor().getf(i);
				} else {// kick out non-Finite values of Imp2
					YnonFinitiyCounts = YnonFinitiyCounts + 1;
					yCoord[i] = 0;
				}

			}

			// Calculate the Correlation result.
			double SSIMDoubleValue = DoubleArrayUtil.computeSSIMDoubleArrays(xCoord, yCoord);
			returnSSIMDoubleValue = SSIMDoubleValue;

		}
		return returnSSIMDoubleValue;

	}
	
	private void addFOV(Grid2D img, float r)
	{
		float rr = r * r;
		float centX = (img.getSize()[0] - 1)/2.0f;
		float centY = (img.getSize()[1] - 1)/2.0f;
		
		for(int i = 0; i < img.getSize()[0]; i++)
			for(int j = 0; j < img.getSize()[1]; j++)
				if( (i - centX) * (i - centX) + (j - centY) * (j - centY) > rr)
					img.setAtIndex(i, j, 0);
	}

}
