package com.scripts;

import java.util.Map;
import java.util.List;
import java.io.File;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

public class FontDataReader {
	/*
	 * Read the font data file (parameter filepath referenced in the constructor).
	 * 
	 * To get the font data, use getData() -> returns a Map<String, Object> of
	 * the file. The filepath (in the constructor) is the name of the file relative
	 * to source folder (src)
	 */

	private Map<String, Object> fontData;

	public FontDataReader(File fontDataFile) {
		String filepath = fontDataFile.getAbsolutePath();
		String readException = null; // if there is an exception while reading fontDataFile, this variable keeps it.
		List<Object> chars = null;

		// verify if fontDataFile exists
		if (!fontDataFile.exists()) {
			System.err.println("Error: the file \"" + filepath + "\" does not exists.");
			return;
		}

		// read fontDataFile
		try {
			fontData = new ObjectMapper().readValue(fontDataFile, new TypeReference<Map<String, Object>>() {
			});
		} catch (IOException e) {
			readException = e.getMessage();
			fontData = null;
		}

		if (fontData != null) {
			// checks if the file has the correct content and type of values

			if (!checkKeys(new String[] { "name", "chars" }, fontData, filepath)) {
				fontData = null;
			} else if (!checkValues(filepath, new String[] { "name", "chars" },
					new Class[] { String.class, List.class }, fontData)) {
				fontData = null;
			} else {
				chars = new ObjectMapper().convertValue(fontData.get("chars"), new TypeReference<List<Object>>() {
				});
			}
		}

		if (chars != null) {
			// verify the chars number
			if (chars.size() != 256) {
				fontData = null;
				System.err.println("Error: expected 256 values in the file \""
						+ filepath + "\", but " + chars.size() + " found.");
			}

			// verify every chars values
			for (int i = 0; i < chars.size(); i++) {
				Object character = chars.get(i);

				if (!(character instanceof Integer || character instanceof List<?> || character == null)) {
					fontData = null;
					System.err.println("Error: the font character " + i + " is an invalid value; expecting "
							+ "List<Integer>, null or Integer but " + character.getClass().getSimpleName() + " found");
				}
			}
		}

		// if the fontData is null, show an error
		if (fontData == null || chars == null) {
			System.err.println("Error while reading file \"" + filepath + "\""
					+ (readException == null ? "" : (":\n" + readException)));
			return;
		}
	}

	public Map<String, Object> getData() {
		return this.fontData;
	}

	private boolean checkKeys(String[] keys, Map<String, Object> mapToCheck, String filepath) {
		boolean hasKeys = true;

		for (String key : keys) {
			if (!mapToCheck.containsKey(key)) {
				hasKeys = false;

				System.err.println("Error: the file \"" + filepath + "\" has not the key \"" + key + "\"");
			}
		}

		return hasKeys;
	}

	private boolean checkValues(String filepath, String[] keys,
			Class<?>[] expectedTypes, Map<String, Object> mapToCheck) {
		boolean hasValidValues = true;

		for (int i = 0; i < expectedTypes.length; i++) {
			String key = keys[i];
			Class<?> expectedType = expectedTypes[i];

			if (!expectedType.isInstance(mapToCheck.get(key))) {
				hasValidValues = false;
				System.err.println("Error: the key \"" + key + "\" of the file \"" + filepath + "\" has invalid value;"
						+ "expecting " + expectedType.getSimpleName() + " but "
						+ (mapToCheck.get(key) != null ? mapToCheck.get(key).getClass().getSimpleName() : "null")
						+ " found.");
			}
		}

		return hasValidValues;
	}
}
