# TMF Compiler v1.7
A compiler for pixelated fonts

> authors: Mahestro_320
> 
> copyright © Commys corporation

## How to use
> you need java to run this program. https://www.java.com/en/download/.
> 
> run with: ```java Main.java [fontImage] [fontInfo] [output (optional)]```

*all the parameters is an path relative to the root directory.
output is optional, the default value is output\font.tmf


## Files
### In input folder
> font.png
> 
> The font image.
> dimensions: 2827x16 px (11x16 px per character).


> font.json
> 
> The informations of the font in an JSON.
> The font name, in string, with the "fontName" key. Next the chars, with "chars" key. It's an list of 256 values, it's the max size x of an character in the image, starting from the center. If the value is null, then the character is null. if the value is an list, then the char size is not equal. For example: ```[4, null] -> 4 to left, 5 to right```

### In output folder:
There will be the output file when the program has finished compiling.

## Update informations
### Update v1.7
#### No changes visually
> No changes visually but the code is more lisible.

### Update v1.6
#### Replacing "font.txt"
> The file font.txt is now replaced by font.json, including jackson dependency.

### Update v1.5
#### More lisible code
> The code is more lisible now

#### Warnings infos
> Warnings are now displayed next to the step making a warning

### Update v1.4
#### More understandable errors
> If there is an error in the input image or image information file, it will be specifieds

#### Works better
> The program will crash less if there is an error, because it is interpreted with an try-catch.

