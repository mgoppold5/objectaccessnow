// tests variable initializations

public class Test1 {
	public static int32 LIMIT = 10;
	
	public static void printNum(int32 i) {
		// not implemented
	}
	
	public static void main() {
		int32 i = 0;
		int32 count = LIMIT;
		
		while(i < count) {
			printNum(i);
			i += 1;
		}
	}
}
