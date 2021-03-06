package edu.stanford.rsl.Yixing.Celphalometric;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import edu.stanford.rsl.conrad.utils.DoubleArrayUtil;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import java.lang.Math;
import java.io.IOException;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;

/**
 * C=20:
 * p2cepWeightedResults: Average RMSE:10.024334306299657, average PSNR 28.02864864602169
 * p2cepRGBResults：Average RMSE:5.0110995358866175, average PSNR 33.828689851490466
 * p2cepChannelLossResults：Average RMSE:4.990296665790509, average PSNR 33.859478653519936
 * cone2parallelRGBResults:Average RMSE:3.519998177723013, average PSNR 36.515197999180586
 * cone2parallelResults:Average RMSE:6.7515315371904405, average PSNR 31.526525068894006
 * p2cepRGBResults2:Average RMSE:5.424752329751397, average PSNR 32.880622856683274
 * p2cepRGBResultsDouble: Average RMSE:5.465173398962877, average PSNR 33.09638832261284
 * p2cepRGBResultsDouble2: Average RMSE:5.671830924903245, average PSNR 32.57145570184917
 * p2cepRGBLower3Results: Average RMSE:10.956957729677885, average PSNR 27.353161059140994 //didn't remove bottom 
 * p2cepRGBLower4Results: Average RMSE:10.609437810699104, average PSNR 27.606995947487803 // didn't remove bottom
 * p2cepRGBFourResults: Average RMSE:6.478712233284604, average PSNR 31.441280595185752; Average RMSE:9.569080862678414, average PSNR 28.463773709952562;Average RMSE:11.260679924244851, average PSNR 27.18923689868825
 * p2cepRGBLower3Results2: Average RMSE:8.08093945108555, average PSNR ..   Average RMSE:7.1426088552756095, average PSNR 31.015266174666465
 * p2cepRGBLower4Results2： Average RMSE:7.2190524065412545, average PSNR 31.013259164940372
 * p2cepChannelLossResultsL3：Average RMSE:7.393748989446328, average PSNR 30.746774166662124
 * 
 * C=40:
 * @author Yixing Huang
 *
 */
public class CalculateSSIMSingleImage {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		
		String path = "D:\\Dropbox\\Dropbox\\Cephalogram\\templateTMI\\Figures\\";
		

		ImagePlus imp1;
		String nameGT, nameOut;
		Grid2D gt, out;
		double rmse, psnr;
		double sumRmse = 0, sumPsnr = 0;
		int count = 0;
		float max = 0;
		for(int idx = 1; idx <= 1; idx ++){	

//			nameGT = path + "patch360Target.png";
//			nameOut = path + "patch3601pOutput.png";
//			nameOut = path + "patch360Output_RGB.png";
			
//			nameGT = path + "patchTest1Target.png";
//			nameOut = path + "patch3601pOutput.png";
//			nameOut = path + "patchTest1Output_RGB2.png";
			
//			nameGT = path + "Test1Target.png";
//			nameOut = path + "Test1pOutput.png";
//			nameOut = path + "Test1RGBOutput.png";
			
			nameGT = path + "patchTest1Target.png";
			nameOut = path + "patchTest1pOutputCycleGAN.png";
//			nameOut = path + "Test1RGBOutput.png";
			
			imp1=IJ.openImage(nameGT);
			gt = ImageUtil.wrapImagePlus(imp1).getSubGrid(0);
			
			imp1=IJ.openImage(nameOut);
			out = ImageUtil.wrapImagePlus(imp1).getSubGrid(0);

			rmse = getSSIM(gt, out);
			
			sumRmse += rmse;

			count++;
		}
		sumRmse = sumRmse/count;
	
			
			System.out.println("Average SSIM:" + sumRmse );
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
//	
//	private static double RMSE(Grid2D recon, Grid2D recon_data) {
//		double err = 0;
//		Grid2D temp = new Grid2D(recon);
//		temp.getGridOperator().subtractBy(temp, recon_data);
//		temp.getGridOperator().multiplyBy(temp, temp);
//		for(int i = 0; i < temp.getSize()[0]; i++)
//			for(int j = 0; j < temp.getSize()[1] - 56; j++)
//				err += temp.getAtIndex(i, j);
//		err = err / (temp.getSize()[0] * (temp.getSize()[1] - 56));
//		err = Math.sqrt(err);
//		return err;
//	}
	
	private static void removeBottom(Grid2D img) {
		for(int i = 0; i < img.getSize()[0]; i++)
			for(int j = img.getSize()[1] - 56; j < img.getSize()[1]; j++)
				img.setAtIndex(i, j, 0);
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
