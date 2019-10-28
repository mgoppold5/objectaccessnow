public class Test1 {
	private static final int32 SWITCH_NUM = 4;
	
	public static void printString(String s) {
		if(s != null) System.out.println(s);
	}
	
	public static void main(String[] args) {
		int32 a = SWITCH_NUM;
		switch(a) {
		case 1:
			printString("one");
			break;
		case 2:
			printString("two");
			break;
		case SWITCH_NUM:
			printString("SWITCH_NUM");
			break;
		default:
			printString("default");
			break;
		}
	}
}
