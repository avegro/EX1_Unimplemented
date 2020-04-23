package edu.cg;

import java.awt.image.BufferedImage;
import java.lang.*;
public class SeamsCarver extends ImageProcessor {

	// MARK: An inner interface for functional programming.
	@FunctionalInterface
	interface ResizeOperation {
		BufferedImage resize();
	}

	// MARK: Fields
	private int numOfSeams;
	private ResizeOperation resizeOp;
	boolean[][] imageMask;
	// TODO: Add some additional fields

	public SeamsCarver(Logger logger, BufferedImage workingImage, int outWidth, RGBWeights rgbWeights,
			boolean[][] imageMask) {
		super((s) -> logger.log("Seam carving: " + s), workingImage, rgbWeights, outWidth, workingImage.getHeight());

		numOfSeams = Math.abs(outWidth - inWidth);
		this.imageMask = imageMask;
		if (inWidth < 2 | inHeight < 2)
			throw new RuntimeException("Can not apply seam carving: workingImage is too small");

		if (numOfSeams > inWidth / 2)
			throw new RuntimeException("Can not apply seam carving: too many seams...");

		// Setting resizeOp by with the appropriate method reference
		if (outWidth > inWidth)
			resizeOp = this::increaseImageWidth;
		else if (outWidth < inWidth)
			resizeOp = this::reduceImageWidth;
		else
			resizeOp = this::duplicateWorkingImage;

		BufferedImage greyImage = this.greyscale();


		this.logger.log("preliminary calculations were ended.");
	}

	public long[][] createEnergyMatrix(BufferedImage greyImage, boolean[][] mask){
		int height = greyImage.getHeight();
		int width = greyImage.getWidth();
		long[][] ans = new long[height][width];
		int e1;
		int e2;
		int e3;

		for(int i = 0; i < height; i++){
			for(int j = 0; j< width; j++){
				e3 = (mask[i][j] == true) ? Integer.MIN_VALUE: 0;
				e1 = (j < width-1) ? Math.abs(greyImage.getRGB(i,j) - greyImage.getRGB(i,j+1)): Math.abs(greyImage.getRGB(i,j) - greyImage.getRGB(i,j-1));
				e2 = (i < height-1) ? Math.abs(greyImage.getRGB(i,j) - greyImage.getRGB(i+1,j)): Math.abs(greyImage.getRGB(i,j) - greyImage.getRGB(i-1,j));
				ans[i][j] = e1 + e2 + e3;
			}
		}
		return ans;
	}

	public long[][] calculateCostMatrix(long[][] energyMatrix){
		int height = energyMatrix.length;
		int width = energyMatrix[0].length;
		long[][] ans = new long[height][width];
		for(int i = 0; i < height; i++){
			for(int j = 0; j < width; j++){
				if (i == 0) {
					ans[i][j] = energyMatrix[i][j];
				}
				if(j > 0 && j < width-1){
					ans[i][j] = energyMatrix[i][j] + Math.min(Math.min(ans[i-1][j-1],ans[i-1][j]), ans[i-1][j+1]);
				}
				else if(j == 0){
					ans[i][j] = energyMatrix[i][j] + Math.min(ans[i-1][j],ans[i-1][j+1]);
				}
				else{
					ans[i][j] = energyMatrix[i][j] + Math.min(ans[i-1][j-1],ans[i-1][j]);
				}
			}
		}
		return ans;
	}

	public int[] findSeam(long[][] costMatrix, long[][] energyMatrix){
		int height = costMatrix.length;
		int width = costMatrix[0].length;
		long minim = costMatrix[height-1][0];
		int ind = 0;
		int[] ans = new int[height];

		for(int i = 0; i < width; i++){
			if(costMatrix[height-1][i] < minim){
				minim = costMatrix[height-1][i];
				ind = i;
			}
		}

		for(int j = height-1; j>0; j--){
			ans[j] = ind;

			if(costMatrix[j][ind] == energyMatrix[j][ind] + costMatrix[j-1][ind] + __________){

			}
			else if(costMatrix[j][ind] == energyMatrix[j][ind] + costMatrix[j-1][ind-1] + __________){
				ind--;
			}
			else{
				ind++;
			}
		}
		return ans;

	}

	public BufferedImage resize() {
		return resizeOp.resize();
	}

	private BufferedImage reduceImageWidth() {
		// TODO: Implement this method, remove the exception.
		throw new UnimplementedMethodException("reduceImageWidth");
	}

	private BufferedImage increaseImageWidth() {
		// TODO: Implement this method, remove the exception.
		throw new UnimplementedMethodException("increaseImageWidth");
	}

	public BufferedImage showSeams(int seamColorRGB) {
		// TODO: Implement this method (bonus), remove the exception.
		throw new UnimplementedMethodException("showSeams");
	}

	public boolean[][] getMaskAfterSeamCarving() {
		// TODO: Implement this method, remove the exception.
		// This method should return the mask of the resize image after seam carving.
		// Meaning, after applying Seam Carving on the input image,
		// getMaskAfterSeamCarving() will return a mask, with the same dimensions as the
		// resized image, where the mask values match the original mask values for the
		// corresponding pixels.
		// HINT: Once you remove (replicate) the chosen seams from the input image, you
		// need to also remove (replicate) the matching entries from the mask as well.
		throw new UnimplementedMethodException("getMaskAfterSeamCarving");
	}
}
