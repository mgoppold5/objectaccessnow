public class Test1 {
	public int b;
	
	public static void printNum(int32 n) {
		System.out.println(n);
	}
	
	public static void main(String[] args) {
		int32[] a = new int32[4];
		a[3] = 1;
		printNum(a[3]);

		Test1 o = new Test1();
		o.b = 2;
		printNum(o.b);
	}
}
