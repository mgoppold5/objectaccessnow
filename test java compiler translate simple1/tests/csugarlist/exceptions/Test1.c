public class Test1 {
	public static (t void) printString(pdef (t String) s) {
		if(e s == null) return;
		
		System.out.println(p s);
		return;
	}
	
	public static (t void) main(pdef (t Array(tp String)) args) {
		try {
			printString(p "point 1");
			(t MyEx) ex = new (t MyEx)(p);
			ex.msg = "throwing stuff oh boy";
			throw ex;
			printString(p "point 2");
		}
		catch(pdef (t MyEx) ex2) {
			printString(p ex2.msg);
			printString(p "point 3");
		}
		printString(p "point 4");
	}
}

class MyEx extends RuntimeException {
	public (t String) msg;
}
