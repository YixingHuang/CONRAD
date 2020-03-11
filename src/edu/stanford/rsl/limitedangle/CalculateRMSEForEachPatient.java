package edu.stanford.rsl.limitedangle;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.OvalRoi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.NumericPointwiseOperators;
import edu.stanford.rsl.conrad.utils.ImageUtil;

public class CalculateRMSEForEachPatient {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		
		String path = "D:\\Tasks\\FAU4\\LimitedAngle\\Noisy3D\\150Degree\\recon\\";
		String path2 = "D:\\Tasks\\FAU4\\LimitedAngle\\Noisy3D\\150Degree\\testData_d1\\";
		String uNetPath = "D:\\Tasks\\FAU4\\LimitedAngle\\Noisy3D\\150Degree\\UNetRecons\\";
		String path3, path4, path5;
		ImagePlus imp1, imp2, imp3, imp4;
		String name1, name2, saveName1, saveName2, predictionName1, predictionNameRes;
		Grid3D data, mask;
		Grid2D data2D, mask2D, prediction, predictionArtifact, temp;
		int getIndex;
		double rmse0, rmse1;
		Grid3D recon3D = new Grid3D(256,256,256);
		File outPutDir0, outPutDir1;
		BufferedWriter bw0, bw1; 
		double sum0, sum1;
		for(int idx = 1; idx <= 18; idx ++){
			if(idx == 4 )
				continue;
			sum0 = 0;
			sum1 = 0;
			name1 = path + "reconTruncated" + idx + ".tif";
			name2 = path + "reconGT" + idx + ".tif";
			imp1=IJ.openImage(name1);
			data = ImageUtil.wrapImagePlus(imp1);
		
			imp2=IJ.openImage(name2);
			mask = ImageUtil.wrapImagePlus(imp2);
			

			path3 = path2 + idx + "\\";
			
			
			
			outPutDir0 = new File(path3+"RMSE_FBP.txt");
			outPutDir1 = new File(path3+"UNet.txt");
			if(!outPutDir0.exists()){
				outPutDir0.getParentFile().mkdirs();
				outPutDir0.createNewFile();
			}
			
			if(!outPutDir1.exists()){
				outPutDir1.getParentFile().mkdirs();
				outPutDir1.createNewFile();
			}

			
			bw0 = new BufferedWriter(new FileWriter(outPutDir0));
			bw1 = new BufferedWriter(new FileWriter(outPutDir1));
			for(int i=0; i < data.getSize()[2]; i++) {
//				System.out.println( idx + ", " + i);
				
				
				getIndex = i;
				path4 = path3+"evaluation\\";

	
				
				//data2D = data.getSubGrid(getIndex);
				predictionName1 = path4 + i + "_final_prediction.tif";

				imp3=IJ.openImage(predictionName1);


				predictionArtifact = ImageUtil.wrapImagePlus(imp3).getSubGrid(0);
				temp = (Grid2D)data.getSubGrid(i).clone();
				temp.getGridOperator().subtractBy(temp, predictionArtifact);	
				temp.getGridOperator().removeNegative(temp);
				recon3D.setSubGrid(i, (Grid2D)temp.clone());
				
				rmse1 = RMSE(temp, mask.getSubGrid(i));

				rmse0 = RMSE(data.getSubGrid(i), mask.getSubGrid(i));
				if(idx == 3 && i == 174)
				{
					System.out.println(i + ": " + rmse0 + ", " + rmse1);
				}
				if(idx == 3 && i == 195)
				{
					System.out.println(i + ": " + rmse0 + ", " + rmse1);
				}
				if(i >= 20 && i < data.getSize()[2] - 20)
				{
					sum0 = sum0 + rmse0;
					sum1 = sum1 + rmse1;
				}

				
				bw0.write(rmse0 + "\r\n");
				bw0.flush();	
		
				bw1.write(rmse1 + "\r\n");
				bw1.flush();
				
			}
			bw1.close();
			bw0.close();
//			recon3D.clone().show("recon3D");
//			path5 = uNetPath + "UNetP" + idx + ".tif";
//			ImagePlus imp3D = ImageUtil.wrapGrid3D(recon3D, null);
//			IJ.saveAs(imp3D, "Tiff", path5);
			
			sum0 = sum0/(data.getSize()[2] - 40);
			sum1 = sum1/(data.getSize()[2] - 40);
			System.out.println(sum0 + " " + sum1);

		}
		
		
	}
	
//	private static double RMSE(Grid2D recon, Grid2D recon_data) {
//		double err = 0;
//		Grid2D temp = new Grid2D(recon);
//		temp.getGridOperator().subtractBy(temp, recon_data);
//		temp.getGridOperator().multiplyBy(temp, temp);
//		err = temp.getGridOperator().sum(temp);
//		err = err / (temp.getSize()[0] * temp.getSize()[1]);
//		err = Math.sqrt(err);
//		return err;
//	}
//	
	/**
	 * ROI RMSE
	 * @param recon
	 * @param recon_data
	 * @return
	 */
//	private static double RMSE(Grid2D recon, Grid2D recon_data) {
//		double err = 0;
//		Grid2D temp = new Grid2D(recon);
//		temp.getGridOperator().subtractBy(temp, recon_data);
//		temp.getGridOperator().multiplyBy(temp, temp);
//		double sum = 0;
//		int count = 0;
//		float x, y;
//		float thres = 97;
//		float thres2 = thres * thres;
//		for(int i = 0; i < recon.getSize()[0]; i ++)
//		{
//			x = i - (recon.getSize()[0] - 1)/2.0f;
//			for(int j = 0; j < recon.getSize()[1]; j++)
//			{
//				y = j - (recon.getSize()[1] - 1)/2.0f;
//				if(x * x + y * y < thres2)
//				{
//					count ++;
//					sum = sum + temp.getAtIndex(i, j);
//				}
//			}
//		}
//		err = sum /count;
//		err = Math.sqrt(err);
//		return err * 2040.0;
//	}
	
	/**
	 * Full Body RMSE
	 * @param recon
	 * @param recon_data
	 * @return
	 */
	private static double RMSE(Grid2D recon, Grid2D recon_data) {
		double err = 0;
		Grid2D temp = new Grid2D(recon);
		temp.getGridOperator().subtractBy(temp, recon_data);
		temp.getGridOperator().multiplyBy(temp, temp);
		double sum = 0;
		int count = 0;

		for(int i = 0; i < recon.getSize()[0]; i ++)
		{

			for(int j = 0; j < recon.getSize()[1]; j++)
				if(j < 190)
				{
					count ++;
					sum = sum + temp.getAtIndex(i, j);
				}
		}
		err = sum /count;
		err = Math.sqrt(err);
		return err * 2040.0;
	}
}
