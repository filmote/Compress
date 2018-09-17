import java.io.IOException;
import java.io.OutputStream;
 
public class BitStream { 
 
    OutputStream out; 
 
    int bits; 
    int bits_left; 
    int outCount = 0;

    public BitStream(OutputStream o) { 
        out = o; 
        bits = 0; 
        bits_left = 0; 
    } 
 
    public void close() throws IOException { 
 
    	while (bits_left != 0) {
    		putBit(0);
    	}
        out.flush(); 
        out.close(); 
    } 
 
 
    public void putBit(int i) throws IOException { 

    	bits <<= 1; 
        bits |= i; 
        bits_left++; 
        
        if (bits_left == 8) { 
            out.write((new String("0x")).getBytes());
            out.write(String.format("%02X", bits & 0xFF).getBytes());
            out.write((new String(", ")).getBytes());
            
            outCount++;
            if (outCount == 16) { 
            	out.write((new String("\n")).getBytes()); 
            	outCount = 0;
            }
            
            bits = 0; 
            bits_left = 0; 
        }
        
    } 
 
    public void putBits(int i, int len) throws IOException { 
    	
        for (int j = len - 1; j >= 0; j--) { 
        	int x = (i & (1 << j));
                putBit(x > 0 ? 1 : 0); 
        }     	
    	
    } 
 
}