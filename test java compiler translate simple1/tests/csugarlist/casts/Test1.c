public class Test1 {
	public static (t void) main(pdef (t Array(tp String)) args) {
		(t BaseThing) o = new (t SubThing)(p);
		(t SubThing) o2 = cast (t SubThing) o;
	}
}

class BaseThing {
	(t int) a;
}

class SubThing extends BaseThing {
	(t int) b;
}
