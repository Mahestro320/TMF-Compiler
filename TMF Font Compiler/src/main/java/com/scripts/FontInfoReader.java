package com.scripts;

import java.util.Map;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import java.io.IOException;

public class FontInfoReader {
	/*
	 * Read the font file infos (parameter filepath of the constructor).
	 * 
	 * To get the file content, use getContent() -> returns a Map<String, Object> of
	 * the file. The filepath referenced in the constructor is the name of the file
	 * relative to folder
	 */

	// the font infos content
	private Map<String, Object> fileContent;

	public FontInfoReader(String filepath) {
		File file = new File(filepath);

		// verify if the file exists
		if (!file.exists()) {
			System.out.println("Error: the file \"" + filepath + "\" does not exists.");
			return;
		}

		// read the file
		try {
			fileContent = new ObjectMapper().readValue(file, new TypeReference<Map<String, Object>>() {
			});
		} catch (IOException e) {
			fileContent = null;
		}

		// the font chars infos
		List<Object> chars = null;

		// verify if the file has the correct content
		if (!fileContent.containsKey("fontName") || !fileContent.containsKey("chars")) {
			System.out.println("Error: the file \"" + filepath + "\" has not the key \"chars\" or \"fontName\"");
			fileContent = null;
		} else if (!(fileContent.get("fontName") instanceof String)) {
			System.out.println("Error: the key \"fontName\" of the file \"" + filepath + "\" has bad value");
			fileContent = null;
		} else {
			chars = new ObjectMapper().convertValue(fileContent.get("chars"), new TypeReference<List<Object>>() {
			});
		}

		// verify the chars number
		if (chars != null) {
			if (chars.size() != 256) {
				fileContent = null;
				System.out.println("Error: expected 256 values in the file \"" +
						filepath + "\", but " + chars.size() + " found.");
			}
		}

		// if the fileContent is null, show an error
		if (fileContent == null || chars == null) {
			System.out.println("Error while reading file \"" + filepath + "\"");
			return;
		}
	}

	public Map<String, Object> getContent() {
		return this.fileContent;
	}
}
