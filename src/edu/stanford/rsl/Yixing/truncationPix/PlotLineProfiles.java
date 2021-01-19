package edu.stanford.rsl.Yixing.truncationPix;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.OvalRoi;
import ij.io.FileInfo;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import ij.io.FileOpener;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.NumericPointwiseOperators;
import edu.stanford.rsl.conrad.utils.ImageUtil;

public class PlotLineProfiles {
	public static void main(String[] args) throws IOException{
		new ImageJ();
		PlotLineProfiles obj = new PlotLineProfiles();
		String path = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\";
		String savePath = "D:\\Tasks\\FAU4\\TruncationCorrection\\NoiseFree3D\\ChosenFigures\\";
		ImagePlus imp1, imp2;
		String nameGT, nameWCE, nameRep, nameCombine, nameScale, nameSub;
		Grid2D dataGT, dataWCE, dataRep, dataCombine, dataScale, dataSub;
		String saveName;
		int saveIndex, getIndex;
		File outPutDir;
		Grid3D test, result;
		Grid2D test2D;
		test = new Grid3D(256, 256, 256);
		
		int slice = 68;
		for(int idx = 18; idx<=18; idx ++){
			if(idx == 4) continue;
//			nameGT = path + "projectionsGT\\projection" + idx + ".tif";
//			nameWCE = path + "projectionsWCE\\projection" + idx + ".tif";
//			nameRep = path + "reprojections\\projection" + idx + ".tif";
//			nameCombine = path + "combinedProjections\\projection" + idx + ".tif";
//			nameScale = path + "combinedProjectionsScale\\projection" + idx + ".tif";
			nameSub = path + "combinedProjectionsSubtraction\\projection" + idx + ".tif";
			
//			imp1=IJ.openImage(nameGT);
//			dataGT = ImageUtil.wrapImagePlus(imp1).getSubGrid(slice);
//			imp1=IJ.openImage(nameWCE);
//			dataWCE = ImageUtil.wrapImagePlus(imp1).getSubGrid(slice);
//			imp1=IJ.openImage(nameRep);
//			dataRep = ImageUtil.wrapImagePlus(imp1).getSubGrid(slice);
//			imp1=IJ.openImage(nameCombine);
//			dataCombine = ImageUtil.wrapImagePlus(imp1).getSubGrid(slice);
//			imp1=IJ.openImage(nameScale);
//			dataScale = ImageUtil.wrapImagePlus(imp1).getSubGrid(slice);
			imp1=IJ.openImage(nameSub);
			dataSub = ImageUtil.wrapImagePlus(imp1).getSubGrid(slice);
			
//			saveName = savePath + "gt.tif";
//			imp1 = ImageUtil.wrapGrid(dataGT, null);
//		    IJ.saveAs(imp1, "Tiff", saveName);
//		    
//		    saveName = savePath + "wce.tif";
//			imp1 = ImageUtil.wrapGrid(dataWCE, null);
//		    IJ.saveAs(imp1, "Tiff", saveName);
//		    
//		    saveName = savePath + "rep.tif";
//			imp1 = ImageUtil.wrapGrid(dataRep, null);
//		    IJ.saveAs(imp1, "Tiff", saveName);
//		    
//		    saveName = savePath + "combine.tif";
//			imp1 = ImageUtil.wrapGrid(dataCombine, null);
//		    IJ.saveAs(imp1, "Tiff", saveName);
//		    
//		    saveName = savePath + "scale.tif";
//			imp1 = ImageUtil.wrapGrid(dataScale, null);
//		    IJ.saveAs(imp1, "Tiff", saveName);
		    
		    saveName = savePath + "subtraction.tif";
			imp1 = ImageUtil.wrapGrid(dataSub, null);
		    IJ.saveAs(imp1, "Tiff", saveName);
		}
			
		System.out.println("Finished!");
	}
	
	Grid2D mergeImages(Grid2D data2D, Grid2D mask2D) {
		Grid2D merge = new Grid2D(512, 256);
		for(int i = 0; i < data2D.getSize()[0]; i++)
			for(int j = 0; j < data2D.getSize()[1]; j++)
			{
				merge.setAtIndex(i, j, data2D.getAtIndex(i, j));
				merge.setAtIndex(i + data2D.getSize()[0], j, mask2D.getAtIndex(i, j));
			}
		
		return merge;
	}
	
	
	private Grid2D getRawImage(String rawPath, String rawName){
		
		FileInfo fi=new FileInfo();
		fi.width=256;
		fi.height=256;
		fi.nImages=1;
		fi.offset=0;
		fi.intelByteOrder=true;
		fi.fileFormat=FileInfo.RAW;
		fi.fileType=FileInfo.GRAY32_FLOAT;
		fi.directory=rawPath;
		fi.fileName=rawName;
		FileOpener fopen=new FileOpener(fi);
		ImagePlus imp=fopen.open(false);
		Grid2D img2D = ImageUtil.wrapImagePlus(imp).getSubGrid(0);
		
		return img2D;
		
	}
	
	/**
	 * Full Body RMSE
	 * @param recon
	 * @param recon_data
	 * @return
	 */
	private static double RMSE_FullBody(Grid2D recon, Grid2D recon_data) {
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
