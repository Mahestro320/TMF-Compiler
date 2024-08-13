
package scripts;

import java.nio.charset.StandardCharsets;
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
	private String[] fileContent = null;


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
			
			String fileContentString = new String(fileContentBytes, StandardCharsets.UTF_8);
			int fileContentLines = getCharOccurense(fileContentString, "\n".charAt(0));
			
			if (fileContentLines != 255) {
				System.out.println("Error: the font infos file must be have 256 lines, but " + (fileContentLines + 1) + " found.");
				fileContentString = null;
			}
			
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
		int occurenses = 0;
		
		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) == character) {
				occurenses++;
			}
		}
		
		return occurenses;
	}
}
