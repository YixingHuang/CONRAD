package edu.stanford.rsl.Yixing.Celphalometric.superResolution;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import java.lang.Math;
import java.io.IOException;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;


public class CalculatePNSR_RRDN {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		

		String gtPath = "D:\\imageSuperResolutionV2_1\\high_res\\testset1\\";
		String path = "D:\\imageSuperResolutionV2_1\\testResults\\RDN5_testset1\\";

		ImagePlus imp1;
		String nameGT, nameOut;
		Grid2D gt, out;
		double rmse, psnr;
		double sumRmse = 0, sumPsnr = 0;
		int count = 0;
		float max = 0;
//		for(int idx = 5601; idx <= 8400; idx ++){	
//		for(int idx = 4201; idx <= 6300; idx ++){
		for(int idx = 1; idx <= 2800; idx ++){
			nameGT = gtPath + idx + ".png";
			nameOut = path + idx + ".png";
//			nameOut = path + idx + "-inputs.png";
			
			imp1=IJ.openImage(nameGT);
			gt = ImageUtil.wrapImagePlus(imp1).getSubGrid(0);
		
			imp1=IJ.openImage(nameOut);
			out = ImageUtil.wrapImagePlus(imp1).getSubGrid(0);
			rmse = RMSE(gt, out);
			max = getMax(gt);
			if(max < 1)
				continue;
			psnr = 20 * Math.log10(max/rmse);
			if(Double.isInfinite(psnr)) {
				System.out.println("inf_" + idx + " RMSE_" + rmse + " max_" + max);
				continue;
			}
			if(Double.isNaN(psnr))
			{
				System.out.print("Nan_" + idx);
				continue;	
			}
			sumRmse += rmse;
			sumPsnr += psnr;
			count++;
		}
		sumRmse = sumRmse/count;
		sumPsnr = sumPsnr/count;
			
			
			System.out.println("Average RMSE:" + sumRmse + ", average PSNR " + sumPsnr);
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
	
	
}
