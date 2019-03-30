package edu.cg;

//import sun.plugin.dom.css.RGBColor;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class ImageProcessor extends FunctioalForEachLoops {

	// MARK: fields
	public final Logger logger;
	public final BufferedImage workingImage;
	public final RGBWeights rgbWeights;
	public final int inWidth;
	public final int inHeight;
	public final int workingImageType;
	public final int outWidth;
	public final int outHeight;

	// MARK: constructors
	public ImageProcessor(Logger logger, BufferedImage workingImage, RGBWeights rgbWeights, int outWidth,
			int outHeight) {
		super(); // initializing for each loops...

		this.logger = logger;
		this.workingImage = workingImage;
		this.rgbWeights = rgbWeights;
		inWidth = workingImage.getWidth();
		inHeight = workingImage.getHeight();
		workingImageType = workingImage.getType();
		this.outWidth = outWidth;
		this.outHeight = outHeight;
		setForEachInputParameters();
	}

	public ImageProcessor(Logger logger, BufferedImage workingImage, RGBWeights rgbWeights) {
		this(logger, workingImage, rgbWeights, workingImage.getWidth(), workingImage.getHeight());
	}

	// MARK: change picture hue - example
	public BufferedImage changeHue() {
		logger.log("Preparing for hue changing...");

		int r = rgbWeights.redWeight;
		int g = rgbWeights.greenWeight;
		int b = rgbWeights.blueWeight;
		int max = rgbWeights.maxWeight;

		BufferedImage ans = newEmptyInputSizedImage();
		forEach((y, x) -> {
			Color c = new Color(workingImage.getRGB(x, y));
			int red = r * c.getRed() / max;
			int green = g * c.getGreen() / max;
			int blue = b * c.getBlue() / max;
			Color color = new Color(red, green, blue);
			ans.setRGB(x, y, color.getRGB());
		});

		logger.log("Changing hue done!");

		return ans;
	}

	public final void setForEachInputParameters() {
		setForEachParameters(inWidth, inHeight);
	}

	public final void setForEachOutputParameters() {
		setForEachParameters(outWidth, outHeight);
	}

	public final BufferedImage newEmptyInputSizedImage() {
		return newEmptyImage(inWidth, inHeight);
	}

	public final BufferedImage newEmptyOutputSizedImage() {
		return newEmptyImage(outWidth, outHeight);
	}

	public final BufferedImage newEmptyImage(int width, int height) {
		return new BufferedImage(width, height, workingImageType);
	}

	// A helper method that deep copies the current working image.
	public final BufferedImage duplicateWorkingImage() {
		BufferedImage output = newEmptyInputSizedImage();
		setForEachInputParameters();
		forEach((y, x) -> output.setRGB(x, y, workingImage.getRGB(x, y)));

		return output;
	}

	public BufferedImage greyscale() {

		logger.log("creates a greyscale image.");

		int r = rgbWeights.redWeight;
		int g = rgbWeights.greenWeight;
		int b = rgbWeights.blueWeight;

		BufferedImage ans = newEmptyInputSizedImage();

		forEach((y,x) -> {
			Color c = new Color(workingImage.getRGB(x, y));
			int red = c.getRed() * r;
			int green = c.getGreen() * g;
			int blue = c.getBlue() * b;

			int weightsSum = r + g + b;
			int greyColor = (red + green + blue) / weightsSum;

			Color color = new Color(greyColor, greyColor, greyColor);
			ans.setRGB(x, y, color.getRGB());

		});

		return ans;
	}

	public BufferedImage nearestNeighbor() {
		logger.log("applies nearest neighbor interpolation.");

		BufferedImage ans = newEmptyOutputSizedImage();

		setForEachOutputParameters();
		pushForEachParameters();

		this.forEach((y, x) -> {
			int imgX = Math.round(x * this.inWidth / (float) this.outWidth);
			int imgY = Math.round(y * this.inHeight / (float) this.outHeight);
			int imgX2 = Math.min(imgX, this.inWidth - 1);
			int imgY2 = Math.min(imgY, this.inHeight - 1);
			ans.setRGB(x, y, this.workingImage.getRGB(imgX2, imgY2));
		});
		popForEachParameters();

		return ans;
	}
}
