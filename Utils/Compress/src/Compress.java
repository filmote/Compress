import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

public class Compress {
	
	public static void main(String[] args) {
		
		BufferedImage image;
		String output = new String();
				
		try {

			if (args.length == 0) { throw new Exception("An image name needs to be specified."); }
			
			File f = new File(args[0]);
			String fileName = f.getName();
			
			image = ImageIO.read(new File(args[0]));

			int originalSize = ((image.getHeight() / 8) * image.getWidth()) + 2;
			
			for (int i = 2; i < 7; i++) {
				
				String newOutput = getEncoding(i, image);
				
				if (output.length() == 0 || newOutput.length() < output.length()) {
					output = newOutput;
				}
				
			}
			
			
			Pattern pattern = Pattern.compile(",");
			Matcher matcher = pattern.matcher(output);
			
			int newSize = 0;
			while (matcher.find()) {
				newSize++;
			}
			
			
			System.out.println("const uint8_t PROGMEM " + fileName + "[] {");
			System.out.println(output);
			System.out.print("};\n\n");
			System.out.print("Original: " + originalSize + " bytes");
			System.out.print("\nCompressed: " + newSize + " bytes");
			System.out.print("\nRatio " + String.format( "%.2f", (newSize / (float)originalSize)));
			
			
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private static String getEncoding(int shortRun, BufferedImage image) {

		int short_run_length_in_bits = shortRun;								// Can have a value between 1 and 7. 
		int long_run_length_in_bits = short_run_length_in_bits * 3;

		int short_run_length = (1 << short_run_length_in_bits);
		int long_run_length = (1 << long_run_length_in_bits);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BitStream bitStream = new BitStream(baos);
		
		try {

			StringBuffer pixels = new StringBuffer();
			
			for (int x = 0; x < image.getWidth(); x++) {

				for (int y = 0; y < image.getHeight(); y++) {

					int p = image.getRGB(x, y);
					int a = (p >> 24) & 0xff;
					int r = (p >> 16) & 0xff;
					int g = (p >> 8) & 0xff;
					int b = p & 0xff;
					int avg = (r + g + b) / 3;

					pixels.append((avg > 127 ? "1" : "0"));
				}
				
			}
			
			
			// Convert runs of 1010 (dithering pixels) to 2, first pass ..

			Pattern p = Pattern.compile("1010");
			Matcher m = p.matcher(pixels.toString());
			pixels = new StringBuffer();
			 
			while (m.find()) {
			    m.appendReplacement(pixels, "22");
			}
			m.appendTail(pixels);
			
			
			// Second pass ..

			p = Pattern.compile("210");
			m = p.matcher(pixels.toString());
			pixels = new StringBuffer();
			 
			while (m.find()) {
			    m.appendReplacement(pixels, "22");
			}
			m.appendTail(pixels);
			
			
			
			// Header ..
			
			bitStream.putBits(image.getHeight() / 8, 4);
			bitStream.putBits(image.getWidth(), 8);
			bitStream.putBits(short_run_length_in_bits, 3);
	
			
			int i = 0;
			while (i < pixels.length()) {
				
				int run = 0;
				int colour = Integer.parseInt(Character.toString(pixels.charAt(i)));
				
				while (Integer.parseInt(Character.toString(pixels.charAt(i))) == colour) {
					
					run++;
					i++;
					
					if (i == pixels.length()) break;
					if ((run + 1) == long_run_length) break;
					
				}
				
				bitStream.putBits(colour, 2);
				
				if (run < short_run_length) {
					bitStream.putBit(0);
					bitStream.putBits(run, short_run_length_in_bits);
				}
				else {
					bitStream.putBit(1);
					bitStream.putBits(run, long_run_length_in_bits);				
				}
				
			}
			
			bitStream.close();
			return baos.toString();
			
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;

	}

}
