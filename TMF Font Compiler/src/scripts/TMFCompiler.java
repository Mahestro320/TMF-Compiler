
package scripts;

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
import java.util.NoSuchElementException;
import java.awt.image.BufferedImage;
import java.lang.NumberFormatException;


public class TMFCompiler {
	/*
	The main part of the compiler, compile the font
	*/

	// gets the content of the font file infos
	private String[] fontFileInfos;

	// an list of warnings
	private List<String> warnings = new ArrayList<>();

	// gets the font image
	private BufferedImage fontImg;

	// gets the source path (src)
	private String source = Paths.get(System.getProperty("user.dir")).getParent().toString() + "\\";

	// create the output file
	private File outputFile;

    // the output file path but relative to source.
    private String outputPathRelative;

    // the start time of the compilation
    private long startTime;

	private List<byte[]> resultData = new ArrayList<>();


	public TMFCompiler(String[] args) {
        
		System.out.println();
		
        // if the number of args is invalid (less than 2 or more than 3), show the message
        if (args.length < 2 || args.length > 3) {
            System.out.println("usage: java Main.java <fontImageInputPath> <fontInfoInputPath> <fontDataOutputPath (optional, default is src\\out)>");
            System.out.println("\nthe paths is relative to src folder\n\nfor example: \"java Main.java input\\font.png input\\font.txt\"");
            
            return;
        }
        
        // gets the files
        File fontImgFile = new File(source + args[0]);
        File fontFileInfosFile = new File(source + args[1]);
        
        String invalidFile = null;
        
        // Create the output folder
        new File(source + "output").mkdir();

        // check if the files paths is invalid.
        if (!fontImgFile.exists() && !fontFileInfosFile.exists()) {
            invalidFile = fontImgFile.getAbsolutePath() + " and " + fontFileInfosFile.getAbsolutePath();
        }
        
        // set the default output path
        if (args.length > 2) {
            outputFile = new File(source + args[2]);
        } else {
            outputFile = new File(source + "output\\font.tmf");
        }
        
        // the files contents
        fontImg = new FontImgReader(fontImgFile.getAbsolutePath()).getImage();
        fontFileInfos = new FontInfoReader(fontFileInfosFile.getAbsolutePath()).getContent();
        
        if (fontImg == null || fontFileInfos == null) {
            return;
        }
		
        // the output path relative to the source
        outputPathRelative = Paths.get(source).relativize(Paths.get(outputFile.getAbsolutePath())).toString();
        
        
        // show an warning if the output file exists
        if (outputFile.exists()) {
            Scanner scanner = new Scanner(System.in);

            System.out.print("the file " + outputPathRelative + " will be replaced.\npress enter to continue");
            
			try {
				scanner.nextLine();
			} catch (NoSuchElementException e) {}
            
            outputFile.delete();
            
            System.out.println();
        }
        
        // gets the compilation start time
        startTime = System.currentTimeMillis();
		
		// compile the file
		compile();
	}
	
	private void compile() {
        // a string[] of two hexadecimal values, for example {"E", "3"} -> \xE3
		String[] charsLength = new String[2];
        
		// like charsLength
		String[] linePos = new String[2];
        
		// the current character
		String curChar;
		

        // the current line
        Integer[] curLine;
        
        // the current pixel of the var pixels
		Integer[] curPixel = new Integer[3];
		
		
		// the current pixels of the line
		List<Integer[]> pixels = new ArrayList<>();
		
		// the current lines to make in the line
		List<Integer[]> lines = new ArrayList<>();

		
        // the jump length in the x axle to make
		int lineJumpLength = 0;

		// the line start
		int lineStart;

		
        // the type of the color
		LineColors lineColorType;
		
		
		// if the line is empty
		Boolean empty;



        // show start message
        System.out.print("adding start bytes... ");

		// write the first chars on the file, the TM\x00
        addResult(new byte[]{84, 77, 00});

		System.out.print("done\nencoding font name... ");

		byte[] fontNameBytes = fontFileInfos[0].getBytes(StandardCharsets.UTF_8);

        System.out.print("done\nadding font name... ");
        
        // write the font name, and null byte
        addResult(fontNameBytes);
        addResult(new byte[]{00});
		
        System.out.print("done\n");

        
		// write chars length on the file
		for (int i = 1; i < 256; i++) {
            System.out.print("\rcalculating font characters length (char " + i + ", " + Math.round(i / 2.56f) + "%)...");

			// the current character
			curChar = fontFileInfos[i];

			if (curChar.length() == 0) {
				// if the current char is an empty string, write "F" value (15 in hex) for null char

				addBinVal(charsLength, "F");
			} else {
				try {
					Integer.parseInt(String.valueOf(curChar.charAt(0)));
				} catch (NumberFormatException e) {
					System.out.println("\nError: the char " + String.valueOf(curChar.charAt(0)) + " is not an valid number.");
					return;
				}

				if (curChar.length() == 1) {
					// if the char length is 1, add its value in charsLength

					addBinVal(charsLength, Integer.toHexString(Integer.parseInt(curChar)));
				} else {
					// if the char length is more of 1, add 6 in the result

					addBinVal(charsLength, Integer.toHexString(Integer.parseInt(String.valueOf(curChar.charAt(0))) + 6));
				}
			}

			if (charsLength[1] != null) {
				// every charsLength is full, write its values in the file
				addResult(new byte[]{(byte) Integer.parseInt(charsLength[0] + charsLength[1], 16)});

				// clear the charsLength
				charsLength[0] = null;
				charsLength[1] = null;
			}
		}
		
		System.out.print(" done\n");
		
		
		// make the empty lines
		for (int x = 0; x < FontImgReader.IMG_DIM[0]; x++) {
			System.out.print("\rcalculating lines (line " + x + ", " + Math.round(x / 28.27f) + "%)...");

			empty = true;

			for (int y = 0; y < FontImgReader.IMG_DIM[1]; y++) {
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
			lineColorType = LineColors.TRANSPARENT;
			
			// make lines
			for (int i = 0; i < pixels.size(); i++) {
				curPixel = pixels.get(i);
				
				if ((curPixel[0] != 0 && curPixel[0] != 255) ||
					(curPixel[1] != 0 && curPixel[1] != 255) ||
					(curPixel[2] != 0 && curPixel[2] != 255) ||
					(curPixel[3] != 0 && curPixel[3] != 255)) {
					warnings.add("unknown color at x:" + x + ", y:" + i + ". The colors must be black, transparent or white.");
				}
				
				if (curPixel[0] == 0 && lineStart != -1) {
					if (lineColorType != LineColors.TRANSPARENT) {
						lines.add(new Integer[]{lineStart, i - 1});
						lineColorType = LineColors.TRANSPARENT;
					}
				} else if (curPixel[0] == 255 && curPixel[1] == 0 &&
					curPixel[2] == 0 && curPixel[3] == 0) {
					
					if (lineStart == -1) {
						lineStart = i;
					} else if (lineColorType != LineColors.BLACK) {
						lines.add(new Integer[]{lineStart, i - 1});
						lineStart = i;
					}

					lineColorType = LineColors.BLACK;
				} else if (curPixel[0] == 255 && curPixel[1] == 255 &&
						   curPixel[2] == 255 && curPixel[3] == 255) {
					if (lineColorType != LineColors.CUSTOM_COLOR && lineStart != -1) {
						lines.add(new Integer[]{lineStart, i - 1});
						lineColorType = LineColors.CUSTOM_COLOR;
						lineStart = i;
					}
				}
			}
			
            // write lines
			if (empty) {
				lineJumpLength++;
			} else {
				addResult(new byte[]{(byte) (int) lineJumpLength});
				lineJumpLength = 0;
				
				addResult(new byte[]{(byte) (lines.size() - 1)});
				
				for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
					// the current line
					curLine = lines.get(lineIndex);
					linePos = new String[2];

					addBinVal(linePos, Integer.toHexString(curLine[0]));
					addBinVal(linePos, Integer.toHexString(curLine[1]));
					
                    addResult(new byte[]{(byte) Integer.parseInt(linePos[0] + linePos[1], 16)});
				}
			}

			lines.clear();
			pixels.clear();
		}
        
        System.out.print(" done\nwriting result...");
        
        // write result in the file
        updateOutput(outputFile);

        // show end message
        System.out.print(" done\n\n\ncompilation finished successfully in " + Math.round((System.currentTimeMillis() - startTime) / 1000) + "s ");
		System.out.print("with " + warnings.size() + " warnings");
		
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
	
	public void addBinVal(String[] chars, String value) {
		// add an value in the result.

		if (chars[0] == null) {
			chars[0] = value;
		} else {
			chars[1] = value;
		}
	}
	
	public void addResult(byte[] data) {
        resultData.add(data);
    }

    public void updateOutput(File outputFile) {
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
