package edu.stanford.rsl.Yixing.Celphalometric.superResolution;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import edu.stanford.rsl.conrad.utils.DoubleArrayUtil;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import java.lang.Math;
import java.io.IOException;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;


public class CalculateSSIM_RRDN {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		

//		String gtPath = "D:\\imageSuperResolutionV2_1\\high_res\\testset1\\";
//		String path = "D:\\imageSuperResolutionV2_1\\testResults\\RDN5_testset1\\";
//		String path = "D:\\imageSuperResolutionV2_1\\testResults\\RRDN5_testset1\\";
//		String gtPath = "D:\\imageSuperResolutionV2_1\\high_res\\testset1ss\\";
//		String path = "D:\\imageSuperResolutionV2_1\\testResults\\RDN1_testset1\\";
//		String path = "D:\\imageSuperResolutionV2_1\\testResults\\RRDN1_testset1\\";
		String path = "D:\\Pix2pix\\superResolutionResults\\superResolution_testset1\\images\\";
		ImagePlus imp1;
		String nameGT, nameOut;
		Grid2D gt, out;
		double rmse;
		double sumRmse = 0;
		int count = 0;

//		for(int idx = 5601; idx <= 8400; idx ++){	
//		for(int idx = 4201; idx <= 6300; idx ++){
		for(int idx = 1; idx <= 2800; idx ++){
//			nameGT = gtPath + idx + ".png";
//			nameOut = path + idx + ".png";
			nameGT = path + idx + "-targets.png";
			nameOut = path + idx + "-outputs.png";
			
			imp1=IJ.openImage(nameGT);
			gt = ImageUtil.wrapImagePlus(imp1).getSubGrid(0);
		
			imp1=IJ.openImage(nameOut);
			out = ImageUtil.wrapImagePlus(imp1).getSubGrid(0);
			rmse = getSSIM(gt, out);
			if(idx <= 10)
				System.out.println(" " + rmse);

			sumRmse += rmse;

			count++;

		}
		sumRmse = sumRmse/count;

		System.out.println("Average SSIM:" + sumRmse);
	}

	
	private static double RMSE(Grid2D recon, Grid2D recon_data) {
		double err = 0;
		Grid2D temp = new Grid2D(recon);
		temp.getGridOperator().subtractBy(temp, recon_data);
		temp.getGridOperator().multiplyBy(temp, temp);
		err = temp.getGridOperator().sum(temp);
		err = err / (temp.getSize()[0] * temp.getSize()[1]);
		err = Math.sqrt(err);
		return err;
	}
	
	private static float getMax(Grid2D img) {
		float max = img.getAtIndex(0, 0);
		for(int i = 0; i < img.getWidth(); i++)
			for(int j = 0; j < img.getHeight(); j++)
			{
				if(img.getAtIndex(i, j) > max)
					max = img.getAtIndex(i, j);
			}
		return max;		
	}
	
	
	public static double getSSIM(Grid2D img1, Grid2D img2) {
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
	
}
