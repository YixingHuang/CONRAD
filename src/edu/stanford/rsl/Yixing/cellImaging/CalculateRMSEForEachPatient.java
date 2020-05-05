package edu.stanford.rsl.Yixing.cellImaging;

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
		
		String path = "D:\\tasks\\FAU4\\Lab\\DeepLearningData\\exp1_conebeam120_lesion_P18\\";
		String path2 = "D:\\tasks\\FAU4\\Lab\\DeepLearningData\\exp1_conebeam120_lesion_P18_d1\\";
		String path3, path4, path5;
		ImagePlus imp1, imp2, imp3, imp4;
		String name1, name2, saveName1, saveName2, predictionName1, predictionNameRes;
		Grid3D data, mask;
		Grid2D data2D, mask2D, prediction, predictionArtifact, temp;
		int saveIndex, getIndex;
		double rmse0, rmse1, rmse2;
		Grid3D recon3D = new Grid3D(256,256,256);
		File outPutDir0, outPutDir1, outPutDir2;
		BufferedWriter bw0, bw1, bw2; 
		for(int idx = 1; idx <= 18; idx ++){
			name1 = path + "reconLimited" + idx + ".tif";
			name2 = path + "reconGT" + idx + ".tif";
			imp1=IJ.openImage(name1);
			data = ImageUtil.wrapImagePlus(imp1);
		
			imp2=IJ.openImage(name2);
			mask = ImageUtil.wrapImagePlus(imp2);
			

			path3 = path2 + idx + "\\";
			
			
			
			outPutDir0 = new File(path3+"logLimited.txt");
			outPutDir1 = new File(path3+"log.txt");
			outPutDir2 = new File(path3+"logRes.txt");
			if(!outPutDir0.exists()){
				outPutDir0.getParentFile().mkdirs();
				outPutDir0.createNewFile();
			}
			
			if(!outPutDir1.exists()){
				outPutDir1.getParentFile().mkdirs();
				outPutDir1.createNewFile();
			}
			if(!outPutDir2.exists()){
				outPutDir2.getParentFile().mkdirs();
				outPutDir2.createNewFile();
			}
			
			bw0 = new BufferedWriter(new FileWriter(outPutDir0));
			bw1 = new BufferedWriter(new FileWriter(outPutDir1));
			bw2 = new BufferedWriter(new FileWriter(outPutDir2));
			for(int i=0; i < data.getSize()[2]; i++) {
				System.out.println( idx + ", " + i);
				
				
				getIndex = i;
				path4 = path3+"evaluation\\";
				path5 = path3+"evaluation2\\";
				saveIndex = idx*1000+getIndex;
				
				//data2D = data.getSubGrid(getIndex);
				mask2D = mask.getSubGrid(getIndex);
				predictionName1 = path4 + i + "_final_prediction.tif";
				predictionNameRes = path5 + i + "_final_prediction.tif";
				imp3=IJ.openImage(predictionName1);
				imp4=IJ.openImage(predictionNameRes);
				prediction = ImageUtil.wrapImagePlus(imp3).getSubGrid(0);
				prediction.getGridOperator().removeNegative(prediction);
				
				predictionArtifact = ImageUtil.wrapImagePlus(imp4).getSubGrid(0);
				temp = (Grid2D)data.getSubGrid(i).clone();
				temp.getGridOperator().subtractBy(temp, predictionArtifact);	
				temp.getGridOperator().removeNegative(temp);
				recon3D.setSubGrid(i, (Grid2D)temp.clone());
				
				rmse2 = RMSE(temp, mask.getSubGrid(i));
				rmse2 = rmse2*2040;
				rmse1 = RMSE(prediction, mask.getSubGrid(i));
				rmse1 = rmse1*2040;
				rmse0 = RMSE(data.getSubGrid(i), mask.getSubGrid(i));
				rmse0 = rmse0 * 2040;
				bw0.write(rmse0 + "\r\n");
				bw0.flush();	
				bw1.write(rmse1 + "\r\n");
				bw1.flush();
				bw2.write(rmse2 + "\r\n");
				bw2.flush();
				
			}
			recon3D.clone().show("recon3D");
			bw2.close();
			bw1.close();
			bw0.close();
		}
		
		
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
}
