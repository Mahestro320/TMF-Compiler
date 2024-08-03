
package scripts;

import java.io.File;


public interface CTM {
    void addBinVal(String[] chars, String value);
    void writeResult(byte[] data);
    void updateOutput(File outputFile);
}
