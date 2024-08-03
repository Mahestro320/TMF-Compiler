
package scripts;

import java.io.File;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;


public class CompilerTools implements CTM {
    // Methods for the compiler

    // the result of the compilation
    private List<byte[]> resultData = new ArrayList<>();
	
    @Override
	public void addBinVal(String[] chars, String value) {
		// add an value in the result.

		if (chars[0] == null) {
			chars[0] = value;
		} else {
			chars[1] = value;
		}
	}
	
    @Override
	public void writeResult(byte[] data) {
        resultData.add(data);
    }

    @Override
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
