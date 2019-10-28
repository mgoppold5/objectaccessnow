public class Test1 {
	public static void main(String[] args) {
		BaseThing o = new SubThing();
		SubThing o2 = (SubThing) o;
	}
}

class BaseThing {
	int a;
}

class SubThing extends BaseThing {
	int b;
}
