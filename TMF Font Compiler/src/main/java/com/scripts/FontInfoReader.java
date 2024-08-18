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
		String readerException = null;

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
			readerException = e.getMessage();
			fileContent = null;
		}

		// the font chars infos
		List<Object> chars = null;

		if (fileContent != null) {
			// verify if the file has the correct content and the correct type of values
			if (!checkKeys(new String[] { "name", "chars" }, fileContent, "the file \"" + filepath + "\"")) {
				fileContent = null;
			} else if (!checkValues(filepath, new String[] { "name", "chars" },
					new Class[] { String.class, List.class }, fileContent)) {
				fileContent = null;
			} else {
				chars = new ObjectMapper().convertValue(fileContent.get("chars"), new TypeReference<List<Object>>() {
				});
			}
		}

		if (chars != null) {
			// verify the chars number
			if (chars.size() != 256) {
				fileContent = null;
				System.out.println("Error: expected 256 values in the file \""
						+ filepath + "\", but " + chars.size() + " found.");
			}

			// verify every chars values
			for (int i = 0; i < chars.size(); i++) {
				Object character = chars.get(i);

				if (!(character instanceof Integer || character instanceof List<?> || character == null)) {
					fileContent = null;
					System.out.println("Error: the font character " + i + " is an invalid value;"
							+ " expecting List<Integer>, null or Integer but " + character.getClass().getSimpleName()
							+ " found");
				}
			}
		}

		// if the fileContent is null, show an error
		if (fileContent == null || chars == null) {
			System.out.println("Error while reading file \"" + filepath + "\""
					+ (readerException == null ? "" : (":\n" + readerException)));
			return;
		}
	}

	public Map<String, Object> getContent() {
		return this.fileContent;
	}

	private boolean checkKeys(String[] keys, Map<String, Object> mapToCheck, String errorText) {
		boolean hasKeys = true;

		for (String key : keys) {
			if (!mapToCheck.containsKey(key)) {
				hasKeys = false;

				System.out.println("Error: " + errorText + " has not the key \"" + key + "\"");
			}
		}

		return hasKeys;
	}

	private boolean checkValues(String filepath, String[] keys, Class<?>[] expectedTypes,
			Map<String, Object> mapToCheck) {
		boolean valid = true;

		for (int i = 0; i < expectedTypes.length; i++) {
			String key = keys[i];
			Class<?> expectedType = expectedTypes[i];

			if (!expectedType.isInstance(mapToCheck.get(key))) {
				valid = false;
				System.out.println("Error: the key \"" + key + "\" of the file \"" + filepath
						+ "\" has invalid value; expecting " + expectedType.getSimpleName() + " but "
						+ (mapToCheck.get(key) != null ? mapToCheck.get(key).getClass().getSimpleName() : "null")
						+ " found.");
			}
		}

		return valid;
	}
}
