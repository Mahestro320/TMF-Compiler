
package scripts;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;


public class FontImgReader {
	/*
	Read the image file (parameter filepath of the constructor)
	
	to get the image data, use getImage() -> returns the BufferedImage of the image
	the filepath referenced in the constructor is the name of the image relative to folder
	*/

	private File file;
	private BufferedImage image;


	public FontImgReader(String filepath) {
		file = new File(filepath);

		// check if the file exists
		if (!file.exists()) {
			System.out.println("Error: the file does not exists.");
            return;
		}

		// read the image
		try {
			image = ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
            return;
		}
		
		if (image == null) {
			System.out.println("Error while reading font image.");
		}
	}
	
	public BufferedImage getImage() {
		return this.image;
	}
}
