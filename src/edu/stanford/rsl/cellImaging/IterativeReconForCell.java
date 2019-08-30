package edu.stanford.rsl.cellImaging;

import java.io.File;

import edu.stanford.rsl.conrad.data.numeric.Grid1D;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.NumericPointwiseOperators;
import edu.stanford.rsl.conrad.filtering.PoissonNoiseFilteringTool;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import edu.stanford.rsl.tutorial.filters.RamLakKernel;
import edu.stanford.rsl.tutorial.parallel.ParallelBackprojector2D;
import edu.stanford.rsl.tutorial.parallel.ParallelProjector2D;
import edu.stanford.rsl.tutorial.weightedtv.TVGradient;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

/**
 * Do parallel-beam FBP reconstructions
 * @author Yixing Huang
 *
 */

public class IterativeReconForCell {
	private int iterMax = 10;
	private int maxTVIter = 10;
	private int startAngle = 20;
	private int angularRange = 100;
	private ParallelProjector2D projector;
	private ParallelBackprojector2D backprojector;
	
	private int sizeX = 512;
	private int sizeY = 512;
	private int numDet = 512;
	private double deltaS = 1;
	private double deltaTheta = Math.PI/180.0;
	private double spacingX = 1, spacingY = 1;
	private int s = 2; //sampling factor
	private int zs = 2;
	
	private float sinoMean, gridMean; //compensation weights for SART
	private Grid2D recon = null;
	private Grid2D sinogram;
	private Grid1D sino1D;
	private Grid2D updGrid;
	private boolean isInitial = true;
	private Grid3D reconUNet;
	private String initialPath = "D:\\Tasks\\FAU4\\CellImaging\\FbpCellRecons100Degree\\reconUNet500Epoch.tif";
	private String saveFolderPath = "D:\\Tasks\\FAU4\\CellImaging\\wTV\\";
	
	private TVGradient tvOp;
	private double step;
	
	public static void main (String [] args) throws Exception{
		new ImageJ();
		
		IterativeReconForCell obj = new IterativeReconForCell();
		
//		String path =  "D:\\Tasks\\FAU4\\CellImaging\\";
//		ImagePlus imp0 =IJ.openImage(path+"projections.tif");
		String path = "D:\\Tasks\\FAU4\\CellImaging\\CombineProjections\\";
		ImagePlus imp0 =IJ.openImage(path+"sino3DReo.tif");
		Grid3D proj0 = ImageUtil.wrapImagePlus(imp0);
		proj0.show("projections");
		
		Grid3D sinos = obj.reorderProjections(proj0);
		
		if(obj.isInitial)
			obj.reconUNet = obj.read3DVolume(obj.initialPath);

		ImagePlus imp, imp3D;
		Grid2D sinoRaw;
		sinoRaw = new Grid2D(obj.numDet, 180);
		sinoRaw.setSpacing(obj.deltaS, obj.deltaTheta);
		obj.projector = new ParallelProjector2D(Math.PI, obj.deltaTheta, obj.numDet * obj.deltaS, obj.deltaS);
		obj.backprojector = new ParallelBackprojector2D(obj.sizeX/obj.s, obj.sizeY/obj.s, obj.s, obj.s);
		obj.backprojector.initSinogramParams(sinoRaw);
		
		obj.getNormProj();
		obj.getNormGrids();
		
		
		int idSave;
		String saveName;
		Grid3D recon3D = new Grid3D(obj.sizeX/obj.s, obj.sizeY/obj.s, sinos.getSize()[2]/obj.zs);
		String path4;

		float scale = 40.0f*180.0f/160.0f;
	
		obj.recon = new Grid2D(obj.sizeX/obj.s, obj.sizeY/obj.s);
		obj.tvOp = new TVGradient(obj.recon);
		obj.tvOp.weps = 0.0001f;
		for(int imgIdx = 230; imgIdx <= 230 /*sinos.getSize()[2]*/; imgIdx = imgIdx + obj.zs )
		{
			
			idSave = imgIdx;
			if(obj.isInitial)
			{
				obj.recon = (Grid2D)obj.reconUNet.getSubGrid(imgIdx).clone();
				obj.recon.getGridOperator().divideBy(obj.recon, scale);
				obj.tvOp.weightMatrixUpdate(obj.recon);
			}
			else
				obj.recon.getGridOperator().fill(obj.recon, 0);
			
			sinoRaw = (Grid2D) sinos.getSubGrid(imgIdx).clone();
			sinoRaw.setSpacing(1, Math.PI/180.0);
			if(imgIdx == 0)
				sinoRaw.show("The Sinogram");
			obj.sinogram = sinoRaw;
//			obj.sinogram = obj.zeroPaddingProjections(sinoRaw, obj.startAngle);
//			obj.thresholding(obj.sinogram, 1.0f);
			obj.sinogram.show("sinogram");

			for(int iter = 0; iter < obj.iterMax; iter ++)
			{
				obj.runSart();
				obj.runWTV();	
				System.out.print(" " + iter);
				if(iter == 0)
					obj.recon.show("recon");
			}
			
			//obj.recon.getGridOperator().multiplyBy(obj.recon, scale);
			recon3D.setSubGrid(imgIdx/obj.zs, (Grid2D)obj.recon.clone());
			System.out.print("\n" + imgIdx + " \n");			
			saveName = obj.saveFolderPath + idSave + ".tif";
			imp = ImageUtil.wrapGrid(obj.recon, null);
			IJ.saveAs(imp, "Tiff", saveName);
		}
		obj.recon.show("recon");
		recon3D.show("recon3D");
		imp3D = ImageUtil.wrapGrid3D(recon3D, null);
		path4 = obj.saveFolderPath + "reconWtv3D.tif";
		IJ.saveAs(imp3D, "Tiff", path4);
		System.out.println("\nFinished!");
		Grid3D recon3D2 = obj.reorderVolume(recon3D);
		imp3D = ImageUtil.wrapGrid3D(recon3D2, null);
		recon3D2.show("recon3D2");
		path4 = obj.saveFolderPath + "reconTomo3D.tif";
		IJ.saveAs(imp3D, "Tiff", path4);
	}
	
	

	private void runSart()
	{
		float beta = -0.001f/gridMean;
		int startIdx = startAngle + 20;
		int endIdx = startAngle + 20 + angularRange + 1;
		if(isInitial)
		{
			startIdx = 0;
			endIdx = (int)(Math.PI/deltaTheta);
		}
		for(int projIdx = startIdx; projIdx < endIdx; projIdx ++)
		{
			sino1D = projector.projectRayDriven1DCL(recon, projIdx);
//			if(projIdx < 45)
//				sino1D.clone().show("sino1D");
			sino1D.getGridOperator().subtractBy(sino1D, sinogram.getSubGrid(projIdx));
		
			if(projIdx < startAngle + 20 || projIdx >= startAngle + 20 + angularRange)
				sino1D.getGridOperator().softThresholding(sino1D, 0.01f);
			else
				sino1D.getGridOperator().softThresholding(sino1D, 0.01f);
			sino1D.getGridOperator().divideBy(sino1D, sinoMean);
			updGrid = backprojector.backprojectPixelDriven(sino1D, projIdx);
			updGrid.getGridOperator().multiplyBy(updGrid, beta);
//			if(projIdx < 45)
//				updGrid.clone().show("upd");

			recon.getGridOperator().addBy(recon, updGrid);
//			if(projIdx > 130)
//				recon.clone().show("recon");
	
//			System.out.print(projIdx + " ");
		}
//		recon.clone().show("recon");
		recon.getGridOperator().removeNegative(recon);
		applyFovMask(recon);
//		System.out.println(" ");
	}
	
	
	/**
	 * weighted TV gradient descent part
	 */
	public void runWTV()
	{
		Grid2D gradient;
		double wTV = tvOp.getWeightedTVvalue(recon);
		int i=0;
		while(i < maxTVIter ){

			gradient = tvOp.computewTVGradient(recon);
			gradient.getGridOperator().divideBy(gradient, (float) tvOp.maxValue);
			backTrackingLineSearch(gradient);//****************
			wTV = tvOp.getWeightedTVvalue(recon);

			System.out.println(" i="+i+" wTV="+wTV+" step="+ step);
			i++;				
		} 
		
		tvOp.weightMatrixUpdate(recon);
	}

	/**
	 * Using back tracking line search algorithm to find the step size for weighted TV gradient descent
	 * @param grad
	 */
	private void backTrackingLineSearch(Grid2D grad){//weighted TV
		double t = 1.0, tmin = 0.0001;
		float alpha = 0.3f, beta = 0.6f;
		double  wTV;
		double wTV0 = tvOp.getWeightedTVvalue(recon);
		double gradNorm = alpha * grid2DNorm(grad);
	
		grad.getGridOperator().multiplyBy(grad, (float) t);
		recon.getGridOperator().subtractBy(recon, grad);
		wTV = tvOp.getWeightedTVvalue(recon); 
	
		while(calEnergyFunction(wTV, wTV0, t, gradNorm) > 0.0 && t > tmin)
		{
			t = t * beta;
			recon.getGridOperator().addBy(recon, grad);
			grad.getGridOperator().multiplyBy(grad, beta);
			recon.getGridOperator().subtractBy(recon, grad);
			wTV = tvOp.getWeightedTVvalue(recon); //
		}
		step = t;
	}
	
	private double calEnergyFunction(double wTV, double wTV0, double t, double gradNorm){
		return wTV - wTV0 + t * gradNorm;	
	}
	
	/**
	 * compute the normalization weight for projection
	 */
	private void getNormProj(){
		Grid2D phanC = new Grid2D(sizeX, sizeY);//Constant grid with all values as 1;
		phanC.setSpacing(spacingX, spacingY);
		phanC.getGridOperator().fill(phanC, 1.0f);

		Grid1D sino = projector.projectRayDriven1DCL(phanC, 0);
		sinoMean = sino.getGridOperator().sum(sino)/sino.getNumberOfElements();
		System.out.println("sinoMean = " + sinoMean);
	}

	/**
	 * compute normalization weight for backprojection
	 */
	private void getNormGrids(){
		Grid1D sino1DC = new Grid1D(numDet);
		sino1DC.setSpacing(deltaS);
		sino1DC.getGridOperator().fill(sino1DC, 1.0f);
	
		Grid2D normGrid = backprojector.backprojectPixelDriven(sino1DC, 0);
		gridMean = normGrid.getGridOperator().sum(normGrid)/normGrid.getNumberOfElements();
		System.out.println("gridMean = " + gridMean);
	}
	
	/**
	 * L2 norm
	 * @param imgGrid
	 * @return
	 */
	private double grid2DNorm(Grid2D imgGrid) {

		double d = 0;
		for (int row = 0; row < imgGrid.getSize()[0]; row++)
			for (int col = 0; col < imgGrid.getSize()[1]; col++)
				d = d + imgGrid.getAtIndex(row, col) * imgGrid.getAtIndex(row, col);
		return d;
	}
	
	private void applyFovMask(Grid2D phan)
	{
		float r = (phan.getSize()[0] - 1.0f)/2.0f;
		float rr = r * r;
		float xCent = r;
		float yCent = xCent;
		float dd;
		for(int i = 0; i < phan.getSize()[0]; i ++)
			for(int j = 0; j < phan.getSize()[1]; j ++)
			{
				dd = (i - xCent) * (i - xCent) + (j - yCent) * (j - yCent);
				if(dd > rr)
					phan.setAtIndex(i, j, 0f);
			}
	}
	
	private Grid3D read3DVolume(String path)
	{
		ImagePlus imp=IJ.openImage(path);
		Grid3D data = ImageUtil.wrapImagePlus(imp);
		return data;
	}
	
	private Grid3D reorderProjections(Grid3D proj){
		
		Grid3D sino = new Grid3D(proj.getSize()[0], proj.getSize()[2], proj.getSize()[1]);
		for(int h = 0; h < proj.getSize()[1]; h++){
			for(int s = 0; s < proj.getSize()[0]; s++) {
				for(int theta  = 0; theta < proj.getSize()[2]; theta++) {
					sino.setAtIndex(s, theta, h, proj.getAtIndex(s, h, theta));
				}
			}
		}
		
		return sino;	
	}
	
	private Grid3D reorderVolume(Grid3D proj){
		
		Grid3D sino = new Grid3D(proj.getSize()[1], proj.getSize()[2], proj.getSize()[0]);
		for(int i = 0; i < proj.getSize()[0]; i++){
			for(int j = 0; j < proj.getSize()[1]; j++) {
				for(int k  = 0; k < proj.getSize()[1]; k++) {
					sino.setAtIndex(k, j, i, proj.getAtIndex(i, j, k));
				}
			}
		}
		
		return sino;	
	}
	
	private Grid2D zeroPaddingProjections(Grid2D sinogram, int numAngle) {
		Grid2D sinoPadd = new Grid2D(sinogram.getSize()[0], sinogram.getSize()[1] + numAngle);
		sinoPadd.setSpacing(sinogram.getSpacing());
		for(int i = 0; i < sinogram.getSize()[0]; i++)
			for(int j = 0; j < sinogram.getSize()[1]; j++)
				sinoPadd.setAtIndex(i, j + numAngle, sinogram.getAtIndex(i, j));
		
		return sinoPadd;
	}
	
	private void thresholding(Grid2D sino, float thres) {
		for(int i = 0; i < sino.getSize()[0]; i++) {
			for(int j = 0; j < sino.getSize()[1]; j ++) {
				if(sino.getAtIndex(i, j) < thres)
					sino.setAtIndex(i, j, 0);
			}
		}
	}
}
