package edu.stanford.rsl.cellImaging;

import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.phantom.BlueGreenAlgaePhantom;
import ij.ImageJ;


public class TryBlueGreenAlgaePhantom {

	public static void main(String[] args) {
		new ImageJ();
		
		Grid3D phan = new BlueGreenAlgaePhantom(256, 256, 256).getNumericalPhantom();
		phan.show("algae");
	}
	
}
