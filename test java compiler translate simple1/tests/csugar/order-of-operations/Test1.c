public class Test1 {
	public static void printNum(uint32 n) {
		// not implemented
	}
	
	public static void main() {
		uint32 a;
		uint32 b;
		uint32 c;
		uint32[] d;
		boolean x;
		
		a = 0;
		b = 0;
		
		x = true;
		
		//d = new uint32[10];

		d[4] = 1;
		d[4] = a + b + 10;
		printNum(d[4]);

		x = a < 1 && b != 2 && x;
		
		if(x) printNum(1);
		else printNum(0);
	}
}

