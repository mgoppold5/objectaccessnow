// another simple program
// with variable declarations and function calls

public class Test2 {
	public static uint32 myAdd(uint32 a, uint32 b) {
		uint32 c;
		
		c = a + b;
		
		return c;
	}

	public static void main() {
		uint32 c;
		
		c = myAdd(1, 2);

		return;
	}
}

