package hack;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.Random;

import com.google.common.io.LineReader;

public class Sample {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		double low = 0;
		double high = 1;
		Random rand = new Random(0);
		
		if (args.length > 0) {
			low = Double.parseDouble(args[0]);
			high = Double.parseDouble(args[1]);
		}
		Reader in = new InputStreamReader(System.in);
		LineReader re = new LineReader(in);
		String buf = null;
		while ((buf = re.readLine()) != null && buf.length() > 0) {
//			System.err.println(buf);
			double sample = rand.nextDouble();
			if (sample >= low && sample < high) {
				System.out.println(buf);
			} else {
				System.err.println(sample);
			}
		}
		System.out.flush();
	}

}
