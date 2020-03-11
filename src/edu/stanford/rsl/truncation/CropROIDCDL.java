package edu.stanford.rsl.truncation;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.OvalRoi;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.NumericPointwiseOperators;
import edu.stanford.rsl.conrad.utils.ImageUtil;

public class CropROIDCDL {
public static void main(String[] args){
	new ImageJ();


	String mainPath = "D:\\tasks\\FAU4\\Lab\\DCAR\\DCAR\\Figures\\";
	
	String path1= mainPath + "reconGTP2S94.png";
	String path2= mainPath + "reconLimitedP2S94.png";
	String path3= mainPath + "UNetP2S94.png";
	String path4= mainPath + "DCARP2S94.png";
	String path5= mainPath + "wTVP2S94.png";
	ImagePlus imp1=IJ.openImage(path1);
	Grid2D phan1=ImageUtil.wrapImagePlus(imp1).getSubGrid(0);
	ImagePlus imp2=IJ.openImage(path2);
	Grid2D phan2=ImageUtil.wrapImagePlus(imp2).getSubGrid(0);
	ImagePlus imp3=IJ.openImage(path3);
	Grid2D phan3=ImageUtil.wrapImagePlus(imp3).getSubGrid(0);
	ImagePlus imp4=IJ.openImage(path4);
	Grid2D phan4=ImageUtil.wrapImagePlus(imp4).getSubGrid(0);
	ImagePlus imp5=IJ.openImage(path5);
	Grid2D phan5=ImageUtil.wrapImagePlus(imp5).getSubGrid(0);
	
	int width= 34, length = 34, xcor = 138, ycor = 129;
	Grid2D ROI1=new Grid2D(width,length);
	Grid2D ROI2=new Grid2D(width,length);
	Grid2D ROI3=new Grid2D(width,length);
	Grid2D ROI4=new Grid2D(width, length);
	Grid2D ROI5=new Grid2D(width, length);
	for (int i= -width/2;i<width/2;i++)
		for(int j=-length/2;j<length/2;j++){
			ROI1.setAtIndex(i + width/2, j+length/2, phan1.getAtIndex(xcor+i,ycor+ j));
			ROI2.setAtIndex(i + width/2, j+length/2, phan2.getAtIndex(xcor+i,ycor+ j));
		 	ROI3.setAtIndex(i + width/2, j+length/2, phan3.getAtIndex(xcor+i,ycor+ j));
			ROI4.setAtIndex(i + width/2, j+length/2, phan4.getAtIndex(xcor+i,ycor+ j));
			ROI5.setAtIndex(i + width/2, j+length/2, phan5.getAtIndex(xcor+i,ycor+ j));
		}
	ROI1.clone().show("referenceROI");
	ROI2.clone().show("reconLimitedROI");
	ROI3.clone().show("reconUNetROI");
	ROI4.clone().show("reconDCDLROI");
	ROI5.clone().show("reconwTVROI");
	
}

private static double MSE(Grid2D recon, Grid2D recon_data) {
	double err = 0;
	Grid2D temp = new Grid2D(recon);
	NumericPointwiseOperators.subtractBy(temp, recon_data);
	err = Grid2Dnorm(temp);
	err=err / (temp.getSize()[0] * temp.getSize()[1]);
	
	return err;
}


private static double Grid2Dnorm(Grid2D recon) {
	
	double d = 0;
	for (int row = 0; row < recon.getSize()[0]; row++)
		for (int col = 0; col < recon.getSize()[1]; col++)
			d = d + recon.getAtIndex(row, col) * recon.getAtIndex(row, col);
	return d;
}

private static double RMSE(Grid3D img1, Grid3D img2) {
	double err = 0, d = 0;
	Grid3D temp = new Grid3D(img2);
	NumericPointwiseOperators.subtractBy(temp, img1);
	for(int z=40;z<=200;z++)
		for (int row = 0; row < temp.getSize()[0]; row++)
			for (int col = 0; col < temp.getSize()[1]; col++)
				d = d + temp.getAtIndex(row, col,z) * temp.getAtIndex(row, col,z);
	err=d / (temp.getSize()[0] * temp.getSize()[1]*161);
	err=Math.sqrt(err);
	
	return err;
}


}

