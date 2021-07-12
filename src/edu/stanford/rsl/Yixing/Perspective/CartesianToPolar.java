package edu.stanford.rsl.Yixing.Perspective;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import edu.stanford.rsl.tutorial.cone.GenerateConeBeamReconstructionFromProjections;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class CartesianToPolar {
	public Grid2D imgCart;
	public Grid2D imgPolar;
	private int[] sizeCart;
	private int[] sizePolar;
	private double[] spacingCart;
	private double[] spacingPolar;
	private double angularStep = 0.5;
	private double radialStep = 1;
	private double[] originCart, originPolar;
	
	public CartesianToPolar(Grid2D imgCart, double radialStep, double angularStep){
		this.sizeCart = imgCart.getSize();
		this.spacingCart = imgCart.getSpacing();
		double width_physical = this. sizeCart[0] * this.spacingCart[0];
		double height_physical = this.sizeCart[1] * this.spacingCart[1];
		double len_diag_half = Math.hypot(width_physical, height_physical) / 2.0;
		double originX = -(width_physical - this.spacingCart[0])/2.0;
		double originY = -(height_physical - this.spacingCart[1])/2.0;
		this.originCart = new double[] {originX, originY};
		this.imgCart = new Grid2D(imgCart);
		this.imgCart.setOrigin(originX, originY);
		
		this.angularStep = angularStep;
		this.radialStep = radialStep;
		int width_polar = (int) (len_diag_half / this.radialStep);
		int height_polar = (int) (360 / this.angularStep);
		this.sizePolar = new int[] {width_polar, height_polar};
		this.spacingPolar = new double[] {radialStep, angularStep};
		this.originPolar = new double[] {radialStep/2.0, 0};
		this.imgPolar = new Grid2D(width_polar, height_polar);
		this.imgPolar.setSpacing(this.spacingPolar);
		this.imgPolar.setOrigin(this.originPolar);
		
	}
	
	
	public Grid2D convertCartisian2Polar() {
		double rho, theta, wx, wy;
		float val;
		for(int i = 0; i < this.sizePolar[0]; i++)
			for(int j = 0; j < this.sizePolar[1]; j++)
			{
				rho = (i + 0.5)* this.spacingPolar[0];
				theta = j * this.spacingPolar[1] * Math.PI/180;
				wx = rho * Math.cos(theta);
				wy = rho * Math.sin(theta);
				val = this.getAtPhysicalCart(wx, wy);
				this.imgPolar.setAtIndex(i, j, val);
			}
		
		return this.imgPolar;
	}
	
	
	public Grid2D convertCartisian2Polar(Grid2D imgCart) {
		double rho, theta, wx, wy;
		float val;
		for(int i = 0; i < this.sizePolar[0]; i++)
			for(int j = 0; j < this.sizePolar[1]; j++)
			{
				rho = (i + 0.5)* this.spacingPolar[0];
				theta = j * this.spacingPolar[1] * Math.PI/180;
				wx = rho * Math.cos(theta);
				wy = rho * Math.sin(theta);
				val = this.getAtPhysicalCart(wx, wy, imgCart);
				this.imgPolar.setAtIndex(i, j, val);
			}
		
		return this.imgPolar;
	}
	
	
	public Grid2D convertPolar2Cartisian() {
		double rho, theta, wx, wy;
		float val;
		for(int i = 0; i < this.sizeCart[0]; i++)
			for(int j = 0; j < this.sizeCart[1]; j++)
			{
				wx = this.originCart[0]  + i * this.spacingCart[0];
				wy = this.originCart[1]  + j * this.spacingCart[1];
				rho = Math.hypot(wx, wy);
				theta = Math.atan2(wy, wx) * 180/Math.PI;
				val = this.getAtPhysicalPolar(rho, theta);
				this.imgCart.setAtIndex(i, j, val);
//				if(i % 10 == 0 && j % 10 == 0)
//				{
//					System.out.println(i + ", " + j + ", " + wx + ", " + wy + ", " + rho + ", " + theta + ", " + val);
//				}
			}
		
		return this.imgCart;
	}
	
	
	public Grid2D convertPolar2Cartisian(Grid2D imgPolar) {
		double rho, theta, wx, wy;
		float val;
		for(int i = 0; i < this.sizeCart[0]; i++)
			for(int j = 0; j < this.sizeCart[1]; j++)
			{
				wx = this.originCart[0]  + i * this.spacingCart[0];
				wy = this.originCart[1]  + j * this.spacingCart[1];
				rho = Math.hypot(wx, wy);
				theta = Math.atan2(wy, wx) * 180/Math.PI;
				val = this.getAtPhysicalPolar(rho, theta, imgPolar);
				this.imgCart.setAtIndex(i, j, val);
			}
		
		return this.imgCart;
	}
	
	
	
	private float getAtPhysicalCart(double wx, double wy) {
		double idx = (wx - this.originCart[0]) / this.spacingCart[0];
		double idy = (wy - this.originCart[1]) / this.spacingCart[1];
				
		int wx_l = (int) Math.floor(idx);
		int wy_t = (int) Math.floor(idy);
		double res_x = idx - wx_l;
		double res_y = idy - wy_t;
		int wx_r = wx_l + 1;
		int wy_b = wy_t + 1;
		float val_tl = 0, val_tr = 0, val_bl = 0, val_br = 0;
		if(wx_l >= 0 && wx_l < this.sizeCart[0] && wy_b >= 0 && wy_b < this.sizeCart[1])
			val_bl = this.imgCart.getAtIndex(wx_l, wy_b);
		if(wx_l >= 0 && wx_l < this.sizeCart[0] && wy_t >= 0 && wy_t < this.sizeCart[1])
			val_tl = this.imgCart.getAtIndex(wx_l, wy_t);
		if(wx_r >= 0 && wx_r < this.sizeCart[0] && wy_b >= 0 && wy_b < this.sizeCart[1])
			val_br = this.imgCart.getAtIndex(wx_r, wy_b);
		if(wx_r >= 0 && wx_r < this.sizeCart[0] && wy_t >= 0 && wy_t < this.sizeCart[1])
			val_tr = this.imgCart.getAtIndex(wx_r, wy_t);
		
		float val = (float)(val_bl * res_y * (1 - res_x) + val_br * res_y * res_x + val_tl * (1 - res_y) * (1 - res_x)
				+ val_tr * (1 - res_y) * res_x);
		
		return val;
	}
	
	
	private float getAtPhysicalCart(double wx, double wy, Grid2D imgCart) {
		double idx = (wx - this.originCart[0]) / this.spacingCart[0];
		double idy = (wy - this.originCart[1]) / this.spacingCart[1];
				
		int wx_l = (int) Math.floor(idx);
		int wy_t = (int) Math.floor(idy);
		double res_x = idx - wx_l;
		double res_y = idy - wy_t;
		int wx_r = wx_l + 1;
		int wy_b = wy_t + 1;
		float val_tl = 0, val_tr = 0, val_bl = 0, val_br = 0;
		if(wx_l >= 0 && wx_l < this.sizeCart[0] && wy_b >= 0 && wy_b < this.sizeCart[1])
			val_bl = imgCart.getAtIndex(wx_l, wy_b);
		if(wx_l >= 0 && wx_l < this.sizeCart[0] && wy_t >= 0 && wy_t < this.sizeCart[1])
			val_tl = imgCart.getAtIndex(wx_l, wy_t);
		if(wx_r >= 0 && wx_r < this.sizeCart[0] && wy_b >= 0 && wy_b < this.sizeCart[1])
			val_br = imgCart.getAtIndex(wx_r, wy_b);
		if(wx_r >= 0 && wx_r < this.sizeCart[0] && wy_t >= 0 && wy_t < this.sizeCart[1])
			val_tr = imgCart.getAtIndex(wx_r, wy_t);
		
		float val = (float)(val_bl * res_y * (1 - res_x) + val_br * res_y * res_x + val_tl * (1 - res_y) * (1 - res_x)
				+ val_tr * (1 - res_y) * res_x);
		
		return val;
	}
	
	
	private float getAtPhysicalPolar(double rho, double theta) {
		double idx = (rho - this.originPolar[0]) / this.spacingPolar[0];
		if(theta < 0)
			theta = theta + 360;
		double idy = (theta - this.originPolar[1]) / this.spacingPolar[1];
				
		int wx_l = (int) Math.floor(idx);
		int wy_t = (int) Math.floor(idy);
		double res_x = idx - wx_l;
		double res_y = idy - wy_t;
		int wx_r = wx_l + 1;
		int wy_b = wy_t + 1;
		if(wy_b == this.sizePolar[1])
			wy_b = 0;
		if(wy_t == -1)
			wy_t = this.sizePolar[1] - 1;
		float val_tl = 0, val_tr = 0, val_bl = 0, val_br = 0;
		if(wx_l >= 0 && wx_l < this.sizePolar[0] && wy_b >= 0 && wy_b < this.sizePolar[1])
			val_bl = this.imgPolar.getAtIndex(wx_l, wy_b);
		if(wx_l >= 0 && wx_l < this.sizePolar[0] && wy_t >= 0 && wy_t < this.sizePolar[1])
			val_tl = this.imgPolar.getAtIndex(wx_l, wy_t);
		if(wx_r >= 0 && wx_r < this.sizePolar[0] && wy_b >= 0 && wy_b < this.sizePolar[1])
			val_br = this.imgPolar.getAtIndex(wx_r, wy_b);
		if(wx_r >= 0 && wx_r < this.sizePolar[0] && wy_t >= 0 && wy_t < this.sizePolar[1])
			val_tr = this.imgPolar.getAtIndex(wx_r, wy_t);
		
		float val = (float)(val_bl * res_y * (1 - res_x) + val_br * res_y * res_x + val_tl * (1 - res_y) * (1 - res_x)
				+ val_tr * (1 - res_y) * res_x);
		
		return val;
	}
	
	
	private float getAtPhysicalPolar(double rho, double theta, Grid2D imgPolar) {
		double idx = (rho - this.originPolar[0]) / this.spacingPolar[0];
		if(theta < 0)
			theta = theta + 360;
		double idy = (theta - this.originPolar[1]) / this.spacingPolar[1];
				
		int wx_l = (int) Math.floor(idx);
		int wy_t = (int) Math.floor(idy);
		double res_x = idx - wx_l;
		double res_y = idy - wy_t;
		int wx_r = wx_l + 1;
		int wy_b = wy_t + 1;
		if(wy_b == this.sizePolar[1])
			wy_b = 0;
		if(wy_t == -1)
			wy_t = this.sizePolar[1] - 1;
		float val_tl = 0, val_tr = 0, val_bl = 0, val_br = 0;
		if(wx_l >= 0 && wx_l < this.sizePolar[0] && wy_b >= 0 && wy_b < this.sizePolar[1])
			val_bl = imgPolar.getAtIndex(wx_l, wy_b);
		if(wx_l >= 0 && wx_l < this.sizePolar[0] && wy_t >= 0 && wy_t < this.sizePolar[1])
			val_tl = imgPolar.getAtIndex(wx_l, wy_t);
		if(wx_r >= 0 && wx_r < this.sizePolar[0] && wy_b >= 0 && wy_b < this.sizePolar[1])
			val_br = imgPolar.getAtIndex(wx_r, wy_b);
		if(wx_r >= 0 && wx_r < this.sizePolar[0] && wy_t >= 0 && wy_t < this.sizePolar[1])
			val_tr = imgPolar.getAtIndex(wx_r, wy_t);
		
		float val = (float)(val_bl * res_y * (1 - res_x) + val_br * res_y * res_x + val_tl * (1 - res_y) * (1 - res_x)
				+ val_tr * (1 - res_y) * res_x);
		
		return val;
	}
	
	public static void main(String[] args) throws Exception {
		new ImageJ();
		String dataPath = "C:\\Data\\CTdata\\1.tif"; 
		ImagePlus imp = IJ.openImage(dataPath);
		Grid3D vol = ImageUtil.wrapImagePlus(imp);
		Grid2D img = (Grid2D)vol.getSubGrid(250).clone();
		img.setSpacing(0.5, 0.5);
		double radialStep = 0.25;
		double angularStep = 0.5;
		CartesianToPolar obj = new CartesianToPolar(img, radialStep, angularStep); 
		Grid2D imgPolar = obj.convertCartisian2Polar();
		Grid2D imgCart = obj.convertPolar2Cartisian();
		
		Grid2D diff = new Grid2D(imgCart);
		diff.getGridOperator().subtractBy(diff, img);
		
		img.clone().show("original image");
		imgPolar.clone().show("polar image");
		imgCart.clone().show("restored cartisian image");
		diff.clone().show("difference");
	

		
	}
}
