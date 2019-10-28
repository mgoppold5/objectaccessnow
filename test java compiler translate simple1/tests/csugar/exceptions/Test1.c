public class Test1 {
	public static void printString(String s) {
		if(s == null) return;
		
		System.out.println(s);
		return;
	}
	
	public static void main(String[] args) {
		try {
			printString("point 1");
			MyEx ex = new MyEx();
			ex.msg = "throwing stuff oh boy";
			throw ex;
			printString("point 2");
		}
		catch(MyEx ex2) {
			printString(ex2.msg);
			printString("point 3");
		}
		printString("point 4");
	}
}

class MyEx extends RuntimeException {
	public String msg;
}
