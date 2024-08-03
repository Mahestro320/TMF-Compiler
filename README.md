# TMF-Compiler v1
A compiler for pixelated fonts.


How to use:
  1. you need java to run this program. https://www.java.com/en/download/

  2. run with:
    java Main.java <fontImage> <fontInfo> <output (optional)>

    *all the parameters is an path relative to src\


    output is optional, the default value is src\output\font.tmf


##Files
  In input folder:
    font.png:
      The font image.
      dimensions: 2827x16 px (11x16 px per character).

    font.txt:
      The informations of the font.

      In first line, the font name

      The 256 next digits is the max size x of an character in the image, starting from the center.
      If the digits got a "+" (or other char) after then the char size is not equal.
      For example: 4+ -> [4, 5] -> 4 to left, 5 to right

  In output folder:
    There will be the output file when the program has finished compiling.
