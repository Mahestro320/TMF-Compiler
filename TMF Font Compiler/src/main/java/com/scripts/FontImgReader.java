package com.scripts;

import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class FontImgReader {
	/*
	 * Read the font image (parameter filepath of the constructor)
	 * 
	 * to get the image data, use getImage() -> returns the BufferedImage of the
	 * image
	 * the filepath referenced in the constructor is the name of the image relative
	 * to folder
	 */

	public static final int[] IMG_DIM = new int[] { 2827, 16 };
	private BufferedImage image;

	public FontImgReader(String filepath) {
		File file = new File(filepath);

		// check if the file exists
		if (!file.exists()) {
			System.out.println("Error: the file \"" + filepath + "\" does not exists.");
			return;
		}

		// read the image
		try {
			image = ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// verify dimensions
		if (image.getWidth() != IMG_DIM[0] || image.getHeight() != IMG_DIM[1]) {
			image = null;
			System.out.println("Error: the image dimensions must be 2827x16px.");
		}

		// check if the image is not null
		if (image == null) {
			System.out.println("Error while reading font image.");
		}
	}

	public BufferedImage getImage() {
		return this.image;
	}
}
