package edu.cg;

import java.awt.image.BufferedImage;

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
    BufferedImage greyScaledWorkingImg;
	// TODO: Add some additional fields
	long[][] costMatrix;

	private static final int DELTA_X = 1;

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

		// TODO: You may initialize your additional fields and apply some preliminary
		// calculations.

		this.logger.log("preliminary calculations were ended.");
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
		// This method should return the mask of the resize image after seam carving. Meaning,
		// after applying Seam Carving on the input image, getMaskAfterSeamCarving() will return
		// a mask, with the same dimensions as the resized image, where the mask values match the
		// original mask values for the corresponding pixels.
		// HINT:
		// Once you remove (replicate) the chosen seams from the input image, you need to also
		// remove (replicate) the matching entries from the mask as well.
		throw new UnimplementedMethodException("getMaskAfterSeamCarving");
	}

	private long pixelEnergy(int y, int x) {

	    //TODO: we might change x to y and y to x
		int forbiddenCost = imageMask[x][y] ? Integer.MAX_VALUE : 0;
        int horizontalNeighbor = (x >= greyScaledWorkingImg.getWidth()) ? (x - 1) : (x + 1);
        int verticalNeighbor = (y >= greyScaledWorkingImg.getHeight()) ? (y - 1) : (y + 1);
        int pixelValue = greyScaledWorkingImg.getRGB(x , y);
        int horizontalNeighborValue = greyScaledWorkingImg.getRGB(horizontalNeighbor , y);
        int verticalNeighborValue = greyScaledWorkingImg.getRGB(x , verticalNeighbor);

		return Math.abs(horizontalNeighborValue -
                pixelValue) + Math.abs(verticalNeighborValue - pixelValue) + forbiddenCost;
	}

	private long forwardCost(int i, int j) {
		return 0;
	}

	private void calculateCostMatrix(int y, int x) {

//		costMatrix[x][y] = pixelEnergy(y, x) + min(
//				calculateCostMatrix(y - 1, x - 1),
//				calculateCostMatrix(y - 1, x - 1),
//				calculateCostMatrix(y - 1, x - 1)
//		);
	}

	private static long min(long x, long y, long z) {
		long minXY = Math.min(x, y);
		return Math.min(minXY, z);
	}

	private void initializeCostMatrix() {
		costMatrix = new long[workingImage.getHeight()][workingImage.getWidth()];


	}
}
