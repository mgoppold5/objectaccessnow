// another simple program
// testing some order of operations

public class Test3 {
	public static uint32 doStuff(uint32 a, uint32 b, uint32 c) {
		uint32 d;
		
		d = a + b + c;
		d = a, b, c;
		d = a = b = c;
		
		return d;
	}

	public static void main() {
		uint32 d;
		
		d = doStuff(1, 2, 3);

		return;
	}
}

