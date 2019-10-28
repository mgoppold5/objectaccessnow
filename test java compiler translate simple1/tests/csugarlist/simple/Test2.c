// another simple program
// with variable declarations and function calls

public class Test2 {
	public static (t uint32) myAdd(pdef (t uint32) a, (t uint32) b) {
		(t uint32) c;
		
		c = a + b;
		
		return c;
	}

	public static (t void) main(pdef) {
		(t uint32) c;
		
		c = myAdd(p 1, 2);

		return;
	}
}
