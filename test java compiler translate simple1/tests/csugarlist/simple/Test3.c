// another simple program
// testing some order of operations

public class Test3 {
	public static (t uint32) doStuff(pdef (t uint32) a,
		(t uint32) b, (t uint32) c) {

		(t uint32) d;
		
		d = a + b + c;
		d = a, b, c;
		d = a = b = c;
		
		return d;
	}

	public static (t void) main(pdef) {
		(t uint32) d;
		
		d = doStuff(p 1, 2, 3);

		return;
	}
}

