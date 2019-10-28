public class Test1 {
	public static void printString(String s) {
		System.out.println(s);
	}

	public static void main(String[] args) {
		MyThing o = new MyThing();
		if(o instanceof MyThing) {
			printString("success");
			return;
		}

		printString("failure");
		return;
	}
}

class MyThing {
	int i;
}
