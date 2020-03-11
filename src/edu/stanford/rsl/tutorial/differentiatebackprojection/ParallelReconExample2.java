package edu.stanford.rsl.tutorial.differentiatebackprojection;

import edu.stanford.rsl.conrad.data.numeric.Grid1D;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.tutorial.filters.RamLakKernel;
import edu.stanford.rsl.tutorial.parallel.ParallelBackprojector2D;
import edu.stanford.rsl.tutorial.parallel.ParallelProjector2D;
import edu.stanford.rsl.tutorial.phantoms.DotsGrid2D;
import edu.stanford.rsl.tutorial.phantoms.Phantom;
import edu.stanford.rsl.tutorial.phantoms.SheppLogan;
import ij.ImageJ;

/**
 * This is a simple demonstration of DBP
 * 
 * @author Yixing Huang
 *
 */
public class ParallelReconExample2 {

	public static void main (String [] args){
		new ImageJ();
		ParallelReconExample2 obj = new ParallelReconExample2();
		
		int x = 512;
		int y = 512;
		double xSpacing = 1.0;
		double ySpacing = 1.0;
		
		dbpOperators dbpOp = new dbpOperators(x, y, xSpacing, ySpacing);
		// Create a phantom
		Phantom phan = new SheppLogan(x, false);
		//phan = new UniformCircleGrid2D(x, y);
		//phan = new MickeyMouseGrid2D(x, y);
		phan.setSpacing(xSpacing, ySpacing);
		phan.show("The Phantom");
		
		// Project forward parallel
		ParallelProjector2D projector = new ParallelProjector2D(2* Math.PI, Math.PI/180.0, 768, 1);
		Grid2D sinogram = projector.projectRayDrivenCL(phan);
		sinogram.show("The Sinogram");
		Grid2D filteredSinogram = new Grid2D(sinogram);
		Grid2D sinoCopy = new Grid2D(sinogram);
		
		// Filter with RamLak
		RamLakKernel ramLak = new RamLakKernel(sinogram.getSize()[0], 1);
		for (int theta = 0; theta < sinogram.getSize()[1]; ++theta) {
			ramLak.applyToGrid(filteredSinogram.getSubGrid(theta));
		}
		filteredSinogram.show("The Filtered Sinogram");
		
		for (int theta = 0; theta < sinogram.getSize()[1]; ++theta) {
			sinoCopy.setSubGrid(theta, obj.differentiatedProjection1D(sinogram.getSubGrid(theta)));
		}
		sinoCopy.show("The differentiated Sinogram");
		
		Grid2D sino1 = new Grid2D(sinoCopy);
		Grid2D sino2 = new Grid2D(sinoCopy);
		
		int startAngle = 0;
		for(int theta = 0; theta < startAngle; theta++ )
		{
			sino1.getSubGrid(theta).getGridOperator().fill(sino1.getSubGrid(theta), 0);
		}
		for(int theta = startAngle + 180; theta < 360; theta++)
		{
			sino1.getSubGrid(theta).getGridOperator().fill(sino1.getSubGrid(theta), 0);
		}
		
		startAngle = 90;
		for(int theta = 0; theta < startAngle; theta++ )
		{
			sino2.getSubGrid(theta).getGridOperator().fill(sino2.getSubGrid(theta), 0);
		}
		for(int theta = startAngle + 180; theta < 360; theta++)
		{
			sino2.getSubGrid(theta).getGridOperator().fill(sino2.getSubGrid(theta), 0);
		}
		
		// Backproject and show
		ParallelBackprojector2D backproj = new ParallelBackprojector2D(x, y, 1, 1);
		Grid2D reconFBP = backproj.backprojectPixelDriven(filteredSinogram);
		reconFBP.clone().show("FBP");
		Grid2D reconDBP = backproj.backprojectPixelDriven(sino1);
		reconDBP.clone().show("DBP");
		Grid2D reconDBP2 = backproj.backprojectPixelDriven(sino2);
		reconDBP2.clone().show("DBP2");
		
		for(int row = 0; row < reconDBP2.getHeight(); row++)
		{
//			if(row == 256)
			reconDBP2.setSubGrid(row, dbpOp.WeightedHilbertTransform(reconDBP2.getSubGrid(row)));
		}
		reconDBP2.clone().show("DBP2 IHT");
	}
	
	Grid1D differentiatedProjection1D(Grid1D proj) {
		Grid1D dproj = new Grid1D(proj.getNumberOfElements());
		for(int i = 0; i < proj.getNumberOfElements()-1; i++)
		{
			dproj.setAtIndex(i, proj.getAtIndex(i + 1) - proj.getAtIndex(i));
		}
		return dproj;
	}
	
}
/*
 * Copyright (C) 2010-2014 Shiyang Hu
 * CONRAD is developed as an Open Source project under the GNU General Public License (GPL).
*/