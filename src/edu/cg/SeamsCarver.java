package edu.cg;



import java.awt.*;
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
	// TODO: Add some additional fields
	BufferedImage result = workingImage;
	long[][] costMatrix;
	int[][] greyScaledWorkingImg;

	private static final int DELTA_X = 1;
	private static final int FACTOR = 50;


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
		for (int i = 0; i < numOfSeams; i++) {
			deleteMinSeams();

		}

		return result;
	}

	private void deleteMinSeams() {
		int x = minimumSeamIndex();
		int y = result.getHeight() - 1;
		int[] seamIndex = new int[result.getHeight()];

		seamIndex[y] = x;

		for (y = result.getHeight() - 1; y > 0; y--) {
			x = findNextMin(y, x);
			seamIndex[y] = x;
		}

		BufferedImage img = removeSeam(seamIndex);
		result = img;

	}

	private BufferedImage removeSeam(int[] seamIndex) {
		BufferedImage ans = newEmptyImage(result.getWidth() - 1, result.getHeight());
		int colIndex;
		for (int i = 0; i < result.getHeight(); i++) {
			colIndex = 0;
			for (int j = 0; j < result.getWidth(); j++) {

				if (seamIndex[i] == j) {
					continue;
				}

				ans.setRGB(colIndex, i, result.getRGB(j, i));
				colIndex++;
			}
		}
		return ans;
	}

	private int findNextMin(int i, int j) {

		long leftValue = -1;
		long centerValue = costMatrix[i - 1][j];
		long rightValue = -1;
		long min;

		//Initialize minimum candidates based on validity check.
		if (j == 0) {
			rightValue = costMatrix[i - 1][j + 1];
			min = Math.min(rightValue, centerValue);
		}
		else if (j == result.getWidth() - 1) {
			leftValue = costMatrix[i - 1][j - 1];
			min = Math.min(leftValue, centerValue);
		}
		else {
			leftValue = costMatrix[i - 1][j - 1];
			rightValue = costMatrix[i - 1][j + 1];
			min = min(leftValue, centerValue, rightValue);
		}

		//Return index accordingly
		if (min == leftValue) {
			return  j - 1;
		}
		else if (min == centerValue) {
			return j;
		}
		else {
			return j + 1;
		}
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

		return new boolean[result.getHeight()][result.getWidth() - 1];
		// TODO: Implement this method, remove the exception.
		// This method should return the mask of the resize image after seam carving. Meaning,
		// after applying Seam Carving on the input image, getMaskAfterSeamCarving() will return
		// a mask, with the same dimensions as the resized image, where the mask values match the
		// original mask values for the corresponding pixels.
		// HINT:
		// Once you remove (replicate) the chosen seams from the input image, you need to also
		// remove (replicate) the matching entries from the mask as well.
//		throw new UnimplementedMethodException("getMaskAfterSeamCarving");
	}

	private long pixelEnergy(int x, int y) {

	    //TODO: we might change x to y and y to x
		int forbiddenCost = imageMask[x][y] ? Integer.MAX_VALUE : 0;
        int verticalNeighbor = (x + 1 >= result.getHeight()) ? (x - 1) : (x + 1);
        int horizontalNeighbor = (y + 1 >= result.getWidth()) ? (y - 1) : (y + 1);
        int pixelValue = greyScaledWorkingImg[x][y];

        int horizontalNeighborValue;
		int verticalNeighborValue;


		verticalNeighborValue = greyScaledWorkingImg[verticalNeighbor][y];


		horizontalNeighborValue = greyScaledWorkingImg[x][horizontalNeighbor];

		return Math.abs(horizontalNeighborValue -
                pixelValue) + Math.abs(verticalNeighborValue - pixelValue) + forbiddenCost;
	}

	private void calculateCostMatrix() {

		for (int i = 0; i < result.getHeight(); i++) {
			for (int j = 0; j < result.getWidth(); j++) {
				costMatrix[i][j] = pixelEnergy(i, j) + calcForwardMin(i, j);
			}
		}
	}

	private long calcForwardMin(int i, int j) {

//		if (imageMask[i][j]) {
//			long res = Integer.MAX_VALUE;
//			return res << 4;
//		}

		long mv = Integer.MAX_VALUE;
		long ml = Integer.MAX_VALUE;
		long mr = Integer.MAX_VALUE;

		//Maybe all three should be factor or maybe factor should be 255L test which works better
		long cv = FACTOR;
		long cr = 255L;
		long cl = 255L;

		//legal
		if (i > 0 && j > 0 && j < result.getWidth() - 1) {
			cv = Math.abs(greyScaledWorkingImg[i][j + 1] - greyScaledWorkingImg[i][j - 1]);
			cl = cv + Math.abs(greyScaledWorkingImg[i - 1][j] - greyScaledWorkingImg[i][j - 1]);
			cr = cv + Math.abs(greyScaledWorkingImg[i - 1][j] - greyScaledWorkingImg[i][j + 1]);

			mv = costMatrix[i - 1][j];
			ml = costMatrix[i - 1][j - 1];
			mr = costMatrix[i - 1][j + 1];
		}
		// i != 0, j == 0
		else if (i != 0 && j == 0) {
			cr = cv + Math.abs(greyScaledWorkingImg[i - 1][j] - greyScaledWorkingImg[i][j + 1]);

			mv = costMatrix[i - 1][j];
			mr = costMatrix[i - 1][j + 1];
		}
		// i != 0, j == width
		else if (i != 0 && j == result.getWidth() - 1) {
			cl = cv + Math.abs(greyScaledWorkingImg[i - 1][j] - greyScaledWorkingImg[i][j - 1]);

			mv = costMatrix[i - 1][j];
			ml = costMatrix[i - 1][j - 1];
		}
		// i == 0
		else {
			return 0L;
		}

		return min(ml + cl, mv + cv, mr + cr);
	}

	private static long min(long x, long y, long z) {
		long minXY = Math.min(x, y);
		return Math.min(minXY, z);
	}

	private void initializeCostMatrix() {
		costMatrix = new long[result.getHeight()][result.getWidth()];
		calculateCostMatrix();

	}

	private int[][] convertGreyScaleTo2DArray() {
		BufferedImage img = new ImageProcessor(logger, result, rgbWeights).greyscale();
		int[][] image = new int[this.result.getHeight()][this.result.getWidth()];

		for (int i = 0; i < result.getHeight(); i++) {

			for (int j = 0; j < result.getWidth(); j++) {
				Color c = new Color(img.getRGB(j, i));
				image[i][j] = c.getBlue();
			}
		}


		return image;
	}

	private int minimumSeamIndex() {
		greyScaledWorkingImg = convertGreyScaleTo2DArray();
		initializeCostMatrix();
		long min = Integer.MAX_VALUE;
		min <<= 8;
		int j = 0;
		int minIndex = -1;
		for (j = 0; j < result.getWidth(); j++) {
			if (min > costMatrix[result.getHeight() - 1][j]) {
				min = costMatrix[result.getHeight() - 1][j];
				minIndex = j;
			}
		}
		return minIndex;
	}
}
