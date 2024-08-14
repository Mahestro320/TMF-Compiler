
package scripts;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class FontInfoReader {
	/*
	   Read the font file infos (parameter filepath of the constructor)
	   
	   to get the file content, use getContent() -> returns a String[] of the file
	   the filepath referenced in the constructor is the name of the file relative to folder
	*/
	
	// the font file infos
	private File file;

	// the lines of the file content
	private String[] fileContent;


	public FontInfoReader(String filepath) {
		file = new File(filepath);
        
        if (!file.exists()) {
            System.out.println("Error: the file \"" + filepath + "\" does not exists.");
            return;
        }
        
		byte[] fileContentBytes = new byte[(int) file.length()];

		// gets the file content
		try (FileInputStream fis = new FileInputStream(file)) {
			int bytesRead = fis.read(fileContentBytes);
			
			// convert byte[] in String
			String fileContentString = new String(fileContentBytes, StandardCharsets.UTF_8);
			
			// gets the number of the file lines
			int fileContentLines = getCharOccurense(fileContentString, "\n".charAt(0));
			
			// verify the lines number
			if (fileContentLines != 255) {
				System.out.println("Error: the font infos file must be have 256 lines, but " + 
								   (fileContentLines + 1) + " found.");
				fileContentString = null;
			}

			// verify if the file is correctely readed
			if (bytesRead != fileContentBytes.length || fileContentString == null) {
				System.out.println("Error while reading font infos file.");
				return;
			}
			
			fileContent = fileContentString.split("\n");
		} catch (IOException e) {
			e.printStackTrace();
            return;
		}
	}
	
	public String[] getContent() {
		return this.fileContent;
	}
	
	private int getCharOccurense(String string, char character) {
		// return the occurenses of the char in the file

		int occurenses = 0;
		
		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) == character) {
				occurenses++;
			}
		}
		
		return occurenses;
	}
}
