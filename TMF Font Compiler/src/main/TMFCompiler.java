
package main;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.awt.image.BufferedImage;


public class TMFCompiler {
	/*
	The main part of the compiler, compile the font
	*/

	// gets the content of the font file infos
	private String[] fontFileInfos;

	// gets the font image
	private BufferedImage fontImg;

	// gets the source path (src)
	private String source = Paths.get(System.getProperty("user.dir")).getParent().toString() + "\\";

	// create the output file
	private File outputFile;

    // the output file path but relative to source.
    private String outputFileRelative;

	// the image dimensions
	private final int[] IMG_DIM = new int[]{2827, 16};

    // the start time of the compilation
    private long startTime;

    // the result of the compilation
    private List<byte[]> resultData = new ArrayList<>();


	public TMFCompiler(String[] args) {
        if (args.length < 2 || args.length > 3) {
            System.out.println("usage: java Main.java <fontImageInputPath> <fontInfoInputPath> <fontDataOutputPath (optional, default is src\\out)>");
            System.out.println("\nthe paths is relative to src folder\n\nfor example: \"java Main.java input\\font.png input\\font.txt\"");
            return;
        }
        
        File outputDir = new File(source + "output");
        File fontImgFile = new File(source + args[0]);
        File fontFileInfosFile = new File(source + args[1]);
        
        outputDir.mkdir();
        
        if (!fontImgFile.exists()) {
            System.out.println("Error: the file \"" + fontImgFile.getAbsolutePath() + "\" does not exists.");
            return;
        
        } else if (!fontFileInfosFile.exists()) {
            System.out.println("Error: the file \"" + fontFileInfosFile.getAbsolutePath() + "\" does not exists.");
            return;
        }
        
        if (args.length > 2) {
            outputFile = new File(source + args[2]);
        } else {
            outputFile = new File(source + "output\\font.tmf");
        }
        
        fontImg = new FontImgReader(fontImgFile.getAbsolutePath()).getImage();
        fontFileInfos = new String(new FontInfoReader(fontFileInfosFile.getAbsolutePath()).getContent(), StandardCharsets.UTF_8).split("\n");
        outputFileRelative = Paths.get(source).relativize(Paths.get(outputFile.getAbsolutePath())).toString();
        
        
        if (outputFile.exists()) {
            Scanner scanner = new Scanner(System.in);

            System.out.print("WARNING: the file " + outputFileRelative + " will be replaced.\npress enter to continue");
            
            scanner.nextLine();
            outputFile.delete();
            
            System.out.println();
        }
        
        startTime = System.currentTimeMillis();
		
		// compile the file
		compile();
	}
	
	private void compile() {
		// a string[] of two values, in hexadecimal for example {"E", "3"} -> \xE3
		String[] charsLength = new String[2];
		
		// same of charsLength
		String[] linePos = new String[2];
		
		// the current character
		String curChar;
		Integer[] curLine;
		
		
		// the current pixels of the line
		List<Integer[]> pixels = new ArrayList<>();
		
		// the current lines to make in the line
		List<Integer[]> lines = new ArrayList<>();

		// the jump length in the x axle to make
		int lineJumpLength = 0;

		// the line start
		int lineStart;

		// the type of the color
		int lineColorType = -1;

		// the current pixel of the var pixels
		Integer[] curPixel = new Integer[3];
		
		
		// if the line is empty
		Boolean empty;

        System.out.print("writing start... ");

		// write the first chars on the file, the TM\x00
        writeResult(new byte[]{84, 77, 00});
		
		// write the font name, and null byte
        System.out.print("done\nwriting font name... ");
        
        writeResult(fontFileInfos[0].getBytes(StandardCharsets.UTF_8));
		writeNB();
		
        System.out.print("done");
        
		// write chars length on the file
		for (int i = 1; i < 256; i++) {
            System.out.print("\rwriting font characters length (char " + i + ", " + Math.round(i / 2.56) + "%)...");

			// the current character
			curChar = fontFileInfos[i];

			if (curChar.length() == 0) {
				// if the current char is an empty string, write "F" value (15 in hex) for null char

				addBinVal(charsLength, "F");
			} else if (curChar.length() == 1) {
				// if the char length is 1, add its value in charsLength

				addBinVal(charsLength, Integer.toHexString(Integer.parseInt(curChar)));
			} else {
				// if the char length is more of 1, add 6 in the result

				addBinVal(charsLength, Integer.toHexString(Integer.parseInt(String.valueOf(curChar.charAt(0))) + 6));
			}

			if (charsLength[1] != null) {
				// every charsLength is full, write its values in the file
				writeResult(new byte[]{(byte) Integer.parseInt(charsLength[0] + charsLength[1], 16)});

				// clear the charsLength
				charsLength[0] = null;
				charsLength[1] = null;
			}
		}
		
		System.out.print(" done\n");
		
		// make the empty lines
		for (int x = 0; x < IMG_DIM[0]; x++) {
			System.out.print("\rwriting lines (line " + x + ", " + Math.round(x / 28.27) + "%)...");

			empty = true;

			for (int y = 0; y < IMG_DIM[1]; y++) {
				int pixel = fontImg.getRGB(x, y);

				int a = (pixel >> 24) & 0xFF;
				int r = (pixel >> 16) & 0xFF;
				int g = (pixel >> 8) & 0xFF;
				int b = pixel & 0xFF;
				
				pixels.add(new Integer[]{a, r, g, b});
				
				if (a != 0) {
					empty = false;
				}
			}
			
			lineStart = -1;
			lineColorType = -1;  // null = transparent, false = black, true = color
			
			// making lines
			for (int i = 0; i < pixels.size(); i++) {
				curPixel = pixels.get(i);
				
				if (curPixel[0] == 0 && lineStart != -1) {
					if (lineColorType != -1) {
						lines.add(new Integer[]{lineStart, i - 1});
						lineColorType = -1;
					}
				} else if (curPixel[0] == 255 && curPixel[1] == 0 &&
					curPixel[2] == 0 && curPixel[3] == 0) {
					
					if (lineStart == -1) {
						lineStart = i;
					} else if (lineColorType != 0) {
						lines.add(new Integer[]{lineStart, i - 1});
						lineStart = i;
					}

					lineColorType = 0;
				} else if (curPixel[0] == 255 && curPixel[1] == 255 &&
						   curPixel[2] == 255 && curPixel[3] == 255) {
					if (lineColorType != 1 && lineStart != -1) {
						lines.add(new Integer[]{lineStart, i - 1});
						lineColorType = 1;
						lineStart = i;
					}
				}
			}
			
			if (empty) {
				lineJumpLength++;
			} else {
				writeResult(new byte[]{(byte) (int) lineJumpLength});
				lineJumpLength = 0;
				
				writeResult(new byte[]{(byte) (lines.size() - 1)});
				
				for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
					// the current line
					curLine = lines.get(lineIndex);
					linePos = new String[2];

					addBinVal(linePos, Integer.toHexString(curLine[0]));
					addBinVal(linePos, Integer.toHexString(curLine[1]));

					writeResult(new byte[]{(byte) Integer.parseInt(linePos[0] + linePos[1], 16)});
				}
			}

			lines.clear();
			pixels.clear();
		}
        
        System.out.print(" done\nwriting result...");
        
        updateOutput();
        
        System.out.print(" done\n\ncompilation finished successfully in " + Math.round((System.currentTimeMillis() - startTime) / 1_000) + "s !\n");
        System.out.println("the output file is in " + outputFileRelative);
	}
	
	private void writeNB() {
		// write null bytes in the file

		writeResult(new byte[]{00});
	}
	
	private void addBinVal(String[] chars, String value) {
		// add an value in the result.

		if (chars[0] == null) {
			chars[0] = value;
		} else {
			chars[1] = value;
		}
	}
	
	private void writeResult(byte[] data) {
        resultData.add(data);
    }

    private void updateOutput() {
		try (FileOutputStream fos = new FileOutputStream(outputFile, true);
			 BufferedOutputStream bos = new BufferedOutputStream(fos)) {
			 
             for (int i = 0; i < resultData.size(); i++) {
                 bos.write(resultData.get(i));
             }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
