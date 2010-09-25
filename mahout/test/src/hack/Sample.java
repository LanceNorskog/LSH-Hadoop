package hack;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberInputStream;
import java.io.LineNumberReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.Random;

public class Sample {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		double sample = 0.1;
		Random rand = new Random();
		
		if (args.length > 0) {
			sample = Double.parseDouble(args[0]);
		}
		int n;
		String line;
		Reader in = new InputStreamReader(System.in);
		LineNumberReader re = new LineNumberReader(in);
		String buf = null;
		while ((buf = re.readLine()) != null && buf.length() > 0) {
			if (rand.nextDouble() < sample) {
				System.out.println(buf);
			}
		}

	}

}
