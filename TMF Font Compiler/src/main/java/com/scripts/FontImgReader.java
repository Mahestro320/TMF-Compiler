package com.scripts;

import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class FontImgReader {
	/*
	 * Read the font image (parameter filepath of the constructor)
	 * 
	 * to get the image data, use getImage() -> returns the image BufferedImage
	 * the filepath referenced in the constructor is the name of the image relative
	 * to folder
	 */

	public static final int[] IMG_DIM = new int[] { 2827, 16 };
	private BufferedImage image;

	public FontImgReader(File imageFile) {

		// check if the file exists
		if (!imageFile.exists()) {
			System.err.println("Error: the file \"" + imageFile.getAbsolutePath() + "\" does not exists.");
			return;
		}

		// read the image
		try {
			image = ImageIO.read(imageFile);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// check the dimensions
		if (image.getWidth() != IMG_DIM[0] || image.getHeight() != IMG_DIM[1]) {
			image = null;
			System.err.println("Error: the image dimensions must be 2827x16px.");
		}

		// check if the image is not null
		if (image == null) {
			System.err.println("Error while reading font image.");
		}
	}

	public BufferedImage getImage() {
		return this.image;
	}
}
