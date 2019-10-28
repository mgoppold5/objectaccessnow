// program
// tests the dangling else problem
// else should map to the nearest if

public class TestIf1 {
	public static void printNum(int32 i) {
		// not implemented
	}

	public static void main() {
		if(true) printNum(1);

		if(false)  // IF_THEN
		if(false)  // IF_THEN_ELSE
		{  
			printNum(2);
		} else {
			printNum(3);
		}

	}
}
