import java.util.ArrayList;


/**
 * Assignment 2, COMP7502 Computer Vision 
 * University number: 2013950101
 * Name: S. Matthew English
 * Due : 5. December 2013, 23:00pm
 * 
 * NOTE: The following link contains code which I used as a reference 
 * http://subversion.developpez.com/projets/Millie/trunk/Millie/src/millie/operator/detection/HarrisFastDetectionOperator.java
 */
public class Assign2 {
	/**
	 * This method is called when the corner detection button is clicked.
	 * 
	 * @param image
	 *            - the input image
	 * @param w
	 *            - width of image
	 * @param h
	 *            - height of image
	 * @param sigma
	 *            - the sigma value for the gaussian convolution
	 * @param threshold
	 *            - the minimum value for qualifying a corner
	 * @param cornersOut
	 *            the data structure that contains all the corners. Each corner
	 *            is represented by a double array of two double values. Example
	 *            (add to data structure): double[] p = new double[2]; p[0] =
	 *            10.3; p[1] = 10.2; cornersOut.add(p);
	 */
	int width, height;
	int radius = 3;
	double k = 0.04; 
	static double sig;
	
	public void obtainCorners(final byte[] image, int w, int h, double sigma, double threshold, final ArrayList<double[]> cornersOut) 
	{

		width = w;
		height = h; 
		sig = sigma; 
		
		double[][] img = new double[w][h]; 

		for (int i=0; i<h; i++)
		for(int j=0; j<w; j++)
		{
			img[j][i] = (double)(1.0 * (image[i*w+j] & 0xff));
			
		}
		double[][] Ix = new double[w][h]; 
		
		
		for(int y=0; y<h; y++)
		{
		   for(int x=0; x<w; x++)
		   {
			if(x == 0) 
			{
				Ix[x][y] = (img[x+1][y])*(0.5);
			}
			else if(x == (w-1))
			{
				Ix[x][y] = (img[x-1][y])*(0.5);
			}

			else
			{
				Ix[x][y] = (img[x-1][y])*(-0.5)+(img[x+1][y])*(0.5);
			}
		    }
		}
		
	
		double[][] Iy = new double[w][h];
		
		for(int y=0; y<h; y++)
		{
		   for(int x=0; x<w; x++)
		   {
			if(y == 0) 
			{
				Iy[x][y] = (img[x][y+1])*(0.5);
			}
			else if(y == (h-1))
			{
				Iy[x][y] = (img[x][y-1])*(0.5);
			}

			else
			{
				Iy[x][y] = (img[x][y-1])*(-0.5)+(img[x][y+1])*(0.5);
			}
		    }
		}
		
		double[][] Ixy = new double[w][h]; 
	
			for(int y=0; y<h; y++){
		   for(int x=0; x<w; x++){
				Ixy[x][y] = Ix[x][y] * Iy[x][y];
			}
			}

		double[][] Ix2 = new double[w][h]; 
	
			for(int y=0; y<h; y++){
		   for(int x=0; x<w; x++){
				Ix2[x][y] = Ix[x][y] * Ix[x][y];
				
			}
			}
	
		double[][] Iy2 = new double[w][h]; 
	
			for(int y=0; y<h; y++){
		   for(int x=0; x<w; x++){
				Iy2[x][y] = Iy[x][y] * Iy[x][y];
			}
			}		
		
		// precompute the coefficients of the gaussian filter 
		double[][] filter = new double[2*radius+1][2*radius+1];
		double filtersum = 0;
		for(int j=-radius;j<=radius;j++) {
			for(int i=-radius;i<=radius;i++) {
				double g = gaussian(i,j,sigma);
				filter[i+radius][j+radius]=g;
				filtersum+=g;
			}
		}
		
		for(int j=-radius;j<=radius;j++) {
			for(int i=-radius;i<=radius;i++) {
				
				filter[i+radius][j+radius]=filter[i+radius][j+radius]/filtersum;
			}
		}

						
		// Convolve gradient with gaussian filter:
		
		//for each image row in input image:
		for (int y=0; y<h; y++) {
		//for each pixel in image row:
			for (int x=0; x<w; x++) { 
				 
				 		double result1 = 0.0;
						double result2 = 0.0;
						double result3 = 0.0; 
						
				// for each kernel row in kernel:
				for(int dy=-radius;dy<=radius;dy++) {
				//for each element in kernel row:
					for(int dx=-radius;dx<=radius;dx++) {
						int xk = x + dx;
						int yk = y + dy;
						if (xk<0 || xk>=w) continue;
						if (yk<0 || yk>=h) continue;
				
						//filter weight
						double f = filter[dx+radius][dy+radius];		
						
						// convolution						
						result1+=f*Ix2[xk][yk];
						result2+=f*Iy2[xk][yk];
						result3+=f*Ixy[xk][yk];
					}
				}
						Ix2[x][y] =result1;
						Iy2[x][y] =result2;
						Ixy[x][y] =result3;
			}
		}		
		double[][] R = new double[width][height];
		R = computeR(Ix2, Iy2, Ixy);
		
		ArrayList<Double> xCoords = new ArrayList<Double>();
		ArrayList<Double> yCoords = new ArrayList<Double>();
		
		// for each pixel in the hmap, keep the local maxima
		for (int y=1; y<height-1; y++) {
			for (int x=1; x<width-1; x++) {
				double hh = R[x][y];
				if (hh<threshold) continue;
				if (!isSpatialMaxima(R, (int)x, (int)y)) continue;
				
				
				
				// Sub-pixel Accuracy
				double y2 = y - (R[x][y+1] - R[x][y-1]) / (2 * (R[x][y+1] + R[x][y-1] - 2 * R[x][y]));
			
				double x2 = x - (R[x+1][y] - R[x-1][y]) / (2 * (R[x+1][y] + R[x-1][y] - 2 * R[x][y]));
			
				
				// add the corner to the list
				xCoords.add((double)y2);
				yCoords.add((double)x2);
	
			}
		}
	
		for(int m=0; m<xCoords.size(); m++)
		{
			double xkorner = xCoords.get(m);
			
			double ykorner = yCoords.get(m);
			 
			
			double[] p = new double[2];

			p[1] = xkorner;
			p[0] = ykorner;
				
			cornersOut.add(p);	
	
		}	
	}
	/**
	 * Gaussian function
	*/
	private static double gaussian(double x, double y, double sigma2) {
		double t = (x*x+y*y)/(2*sigma2);
		double u = 1.0/(2*Math.PI*sigma2);
		double e = u*Math.exp( -t );
		return e;
	}
	 

	public double[][] computeR(double[][] Ix2, double[][] Iy2, double[][] Ixy)
	{
	double[][] output = new double[width][height]; 
	/**
	 * compute harris measure for a pixel
	 */
	 for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
			
					// matrix elements (normalized)
					double m00 = Ix2[x][y]; 
					double m01 = Ixy[x][y];
					double m10 = Ixy[x][y];
					double m11 = Iy2[x][y];
		
					// Harris corner measure = det(M)-lambda.trace(M)^2
 
					output[x][y] = m00*m11 - m01*m10 - k*(m00+m11)*(m00+m11);
				
			}
	    }
	    return output;
	}


	/**
	 * return true if the measure at pixel (x,y) is a local spatial Maxima
	 */
	private boolean isSpatialMaxima(double[][] R, int x, int y) {
		int n=8;
		int[] dx = new int[] {-1,0,1,1,1,0,-1,-1};
		int[] dy = new int[] {-1,-1,-1,0,1,1,1,0};
		double w =  R[x][y];
		for(int i=0;i<n;i++) {
			double wk = R[x+dx[i]][y+dy[i]];
			if (wk>=w) return false;
		}
		return true;
	}

}			
