// program
// tests a while loop

public class TestWhile1 {
	public static void printNum(int32 i) {
		// not implemented
	}

	public static void main() {
		uint32 i;
		uint32 count;

		i = 0;
		count = 10;
		while(i < count) {
			printNum(i);
			i = i + 1;
		}
	}
}
