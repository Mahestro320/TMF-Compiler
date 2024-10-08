package com.scripts;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.io.File;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Scanner;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.NoSuchElementException;
import java.awt.image.BufferedImage;

public class TMFCompiler {
	/*
	 * The main part of the compiler, compile the font
	 */

	private BufferedImage fontImg;

	private File outputFile;
	private String outputPathRelative; // the output file path but relative to source.
	private String source = Paths.get("").toAbsolutePath().resolve("src").toString() + "\\"; // gets the source path

	private int warningsNumber = 0; // the number of warnings
	private long startTime; // the start time of the compilation

	private Map<String, Object> fontData; // the content of the font file infos
	private List<byte[]> resultData = new ArrayList<>(); // the result data of the compilation
	private List<String> warnings = new ArrayList<>(); // an list of warnings during the compilation

	public TMFCompiler(String[] args) {

		/*
		 * if the number of args is invalid (less than 2 or more than 3), show the
		 * message to explain how to launch the compiler
		 */
		if (args.length < 2 || args.length > 3) {
			System.out.println("usage: java Main.java <fontImageInputPath> <fontInfoInputPath> "
					+ "<fontDataOutputPath (optional, default is src\\out)>\n\nthe paths is"
					+ "relative to src folder\n\nfor example: \"java Main.java input\\font.png input\\font.txt\"");
			return;
		}

		File fontImgFile = new File(source + args[0]); // gets the font image file
		File fontInfosFile = new File(source + args[1]); // gets the font infos
		outputFile = new File(source + (args.length > 2 ? args[2] : "output\\font.tmf")); // set the default output path

		// the output path relative to the source
		outputPathRelative = Paths.get(source).relativize(Paths.get(outputFile.getAbsolutePath())).toString();

		fontImg = new FontImgReader(fontImgFile).getImage(); // the font image
		fontData = new FontDataReader(fontInfosFile).getData(); // the font infos content

		// check if files have been read
		if (fontImg == null || fontData == null) {
			return;
		}

		// show an warning if the output file exists
		if (outputFile.exists()) {
			System.out.print("the file " + outputPathRelative + " will be replaced.\npress enter to continue");

			try (Scanner scanner = new Scanner(System.in)) {
				scanner.nextLine();
			} catch (NoSuchElementException e) {
				System.out.println("\noperation canceled");
				return;
			}

			System.out.println();
		}

		// gets the timestamp of the compilation start time
		startTime = System.currentTimeMillis();

		compile();
	}

	private void compile() {
		// show start message
		System.out.print("adding start bytes... ");

		// write the first bytes on the file, the TM\x00
		addResult(new byte[] { 84, 77, 00 });

		System.out.print("done\nencoding font name... ");

		// encode the font name
		byte[] fontNameBytes = String.valueOf(fontData.get("name")).getBytes(StandardCharsets.UTF_8);

		System.out.print("done\nadding font name... ");

		// write the font name, and null byte
		addResult(fontNameBytes);
		addResult(new byte[] { 00 });

		System.out.print("done\n");

		Object curChar; // the current character
		List<Object> chars = new ObjectMapper().convertValue(fontData.get("chars"), new TypeReference<List<Object>>() {
		}); // an list of font chars

		// a string[] of two bit of hexadecimal values, for example {"E", "3"} -> \xE3
		String[] charsLength = new String[2];

		// write chars length on the file
		for (int i = 0; i < 256; i++) {
			System.out.print("\rcalculating font characters length (char " + i
					+ ", " + Math.round(i / 2.56f) + "%)...");

			curChar = chars.get(i);

			if (curChar == null) {
				// if the current char is empty, write "F" value (15 in hex) for null char
				addBinVal(charsLength, "F");

			} else {
				if (curChar instanceof Integer) {
					// if the char is an integer, add its value in charsLength
					addBinVal(charsLength, Integer.toHexString((int) curChar));

				} else {
					// if the char length is more of 1, add 6 in the result
					addBinVal(charsLength, Integer.toHexString(
							(int) (new ObjectMapper().convertValue(curChar, new TypeReference<List<Object>>() {
							})).get(0) + 6));
				}
			}

			if (charsLength[1] != null) {
				// if every charsLength is full, write values in the file
				addResult(new byte[] { (byte) Integer.parseInt(charsLength[0] + charsLength[1], 16) });

				// clear charsLength
				charsLength[0] = null;
				charsLength[1] = null;
			}
		}

		System.out.print(" done\n");

		/*
		 * a string[] of two bit of hexadecimal values, for example {"E", "3"} -> \xE3
		 * for line positions
		 */
		String[] linePos = new String[2];

		boolean empty; // an boolean for the empty line
		boolean imageTransparent = false; // true if there is transparent in the image

		int lineStart; // the line start
		int lineJumpLength = 0; // the jump length in the x axle to make
		int potentialWarningsNumber = 0; // the action have warnings

		LineColors lineColorType; // the line color type

		Integer[] curPixel = new Integer[3]; // the current pixel of the pixels var
		Integer[] curLine = new Integer[2]; // the current line
		List<Integer[]> pixels = new ArrayList<>(); // the current pixels of the line
		List<Integer[]> lines = new ArrayList<>(); // the current lines to make in the line

		// make the empty lines
		for (int x = 0; x < FontImgReader.IMG_DIM[0]; x++) {
			System.out
					.print("\rcalculating lines (line " + x + ", " + Math.round(x / (FontImgReader.IMG_DIM[0] / 100.0f))
							+ "%)...");

			empty = true;

			// save in pixels the pixels of the line
			for (int y = 0; y < FontImgReader.IMG_DIM[1]; y++) {
				int pixel = fontImg.getRGB(x, y);

				int a = (pixel >> 24) & 0xFF;
				int r = (pixel >> 16) & 0xFF;
				int g = (pixel >> 8) & 0xFF;
				int b = pixel & 0xFF;

				// add the pixel in the pixels
				pixels.add(new Integer[] { a, r, g, b });

				if (a != 0) {
					empty = false; // if the opacity is 0 then the line is not empty
				}
			}

			lineStart = -1;
			lineColorType = LineColors.TRANSPARENT;

			// make lines
			for (int i = 0; i < pixels.size(); i++) {
				curPixel = pixels.get(i);

				// if the pixel is an unknown color, show an warning
				if ((curPixel[0] != 0 && curPixel[0] != 255) || (curPixel[1] != 0 && curPixel[1] != 255) ||
						(curPixel[2] != 0 && curPixel[2] != 255) || (curPixel[3] != 0 && curPixel[3] != 255)) {

					warningsNumber++;
					warnings.add("unknown color at x:" + x + ", y:" + i
							+ ". The colors must be black, transparent or white.");
				}

				if (curPixel[0] == 0 && lineStart != -1 && lineColorType != LineColors.TRANSPARENT) {
					if (!imageTransparent) {
						imageTransparent = true;
					}

					lines.add(new Integer[] { lineStart, i - 1 });
					lineColorType = LineColors.TRANSPARENT;
				} else if (curPixel[0] == 255 && curPixel[1] == 0 &&
						curPixel[2] == 0 && curPixel[3] == 0) {

					if (lineStart == -1) {
						lineStart = i;
					} else if (lineColorType != LineColors.BLACK) {
						lines.add(new Integer[] { lineStart, i - 1 });
						lineStart = i;
					}

					lineColorType = LineColors.BLACK;
				} else if (curPixel[0] == 255 && curPixel[1] == 255 &&
						curPixel[2] == 255 && curPixel[3] == 255) {

					// if the line starts with white instead of black then show an warning
					if (lineColorType == LineColors.TRANSPARENT) {
						potentialWarningsNumber++;
						warnings.add("the line at position " + x
								+ " does not start with black, because white was found.");
					}

					if (lineColorType != LineColors.CUSTOM_COLOR && lineStart != -1) {
						lines.add(new Integer[] { lineStart, i - 1 });
						lineColorType = LineColors.CUSTOM_COLOR;
						lineStart = i;
					}
				}
			}

			// write lines
			if (empty) {
				lineJumpLength++;
			} else {
				addResult(new byte[] { (byte) lineJumpLength });
				lineJumpLength = 0;

				addResult(new byte[] { (byte) (lines.size() - 1) });

				for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
					curLine = lines.get(lineIndex);
					linePos = new String[2];

					addBinVal(linePos, Integer.toHexString(curLine[0]));
					addBinVal(linePos, Integer.toHexString(curLine[1]));

					addResult(new byte[] { (byte) Integer.parseInt(linePos[0] + linePos[1], 16) });
				}
			}

			lines.clear();
			pixels.clear();
		}

		/*
		 * if no opaque pixels found in the image, show an warning and supress warnings
		 * of the invalid start of color
		 */
		if (!imageTransparent) {
			warnings.add("no opaque pixels were found in the image.");
			potentialWarningsNumber = 1;

			// suppress warnings of the invalid start of color
			List<String> result = new ArrayList<>();

			for (int i = 0; i < warnings.size(); i++) {
				if (!warnings.get(i).substring(0, 20).equals("the line at position")) {
					result.add(warnings.get(i));
				}
			}

			warnings = result;
		}

		warningsNumber += potentialWarningsNumber;

		// delete the output file if exists
		if (outputFile.exists()) {
			showFinishText();

			System.out.print("deleting output file...");
			outputFile.delete();
		}

		showFinishText();

		// Create the output folder
		File outputFolder = new File(source + "output");

		if (!outputFolder.exists()) {
			System.out.print("making output dir...");
			outputFolder.mkdir();
			System.out.print(" done\n");
		}

		System.out.print("writing result...");

		// write result in the file
		updateOutput(outputFile);

		// show end message
		System.out.print(" done\n\n\ncompilation finished " + (warnings.size() == 0 ? "successfully " : "")
				+ "in " + (System.currentTimeMillis() - startTime) / 1000.0f + "s with " + warnings.size()
				+ " warnings");

		// show the list of warnings
		if (warnings.size() != 0) {
			System.out.println(":");

			for (int i = 0; i < warnings.size(); i++) {
				System.out.println(" - " + warnings.get(i));
			}
			System.out.println();
		} else {
			System.out.println(".");
		}

		System.out.println("the output file is in " + outputPathRelative + ".");
	}

	private void addBinVal(String[] chars, String value) {
		chars[chars[0] == null ? 0 : 1] = value;
	}

	private void addResult(byte[] data) {
		resultData.add(data);
	}

	private void updateOutput(File outputFile) {
		// write the result content in the file

		try (FileOutputStream fos = new FileOutputStream(outputFile, true);
				BufferedOutputStream bos = new BufferedOutputStream(fos)) {

			for (int i = 0; i < resultData.size(); i++) {
				bos.write(resultData.get(i));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void showFinishText() {
		if (warningsNumber != 0) {
			System.out.print(" -> " + warningsNumber + " warning" + (warningsNumber == 1 ? "\n" : "s\n"));
			warningsNumber = 0;
		} else {
			System.out.print(" done\n");
		}
	}
}
