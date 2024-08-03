
package main;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class FontInfoReader {
	/*
	Read the inforamtions in the file (parameter filepath of the constructor)
	
	to get the file content, use getContent() -> returns a byte[] of the file
	the filepath referenced in the constructor is the name of the file relative to folder
	*/
	
	private File file;
	private byte[] fileContent;


	public FontInfoReader(String filepath) {
		file = new File(filepath);
		fileContent = new byte[(int) file.length()];

		// gets the file content
		try (FileInputStream fis = new FileInputStream(file)) {
			int bytesRead = fis.read(fileContent);
			
			if (bytesRead != fileContent.length) {
				throw new IOException("Error while reading file.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public byte[] getContent() {
		return fileContent;
	}
}
