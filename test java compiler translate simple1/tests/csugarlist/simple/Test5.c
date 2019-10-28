public class Test5 {
	public static (t void) printFloat(pdef (t float32) f) {
		// NOT IMPLEMENTED
	}

	public static (t void) printString(pdef (t String) s) {
		// NOT IMPLEMENTED
	}

	public static (t void) printChar(pdef (t int) c) {
		// NOT IMPLEMENTED
	}

	public static (t void) main(pdef (t Array(tp String)) args) {
		(t float32) f;
		(t String) s;
		(t int32) c;

		f = 2.5 * 4;
		printFloat(p f);

		s = "HELLO";
		printString(p s);

		c = 'A';
		printChar(p c);		
	}
}
