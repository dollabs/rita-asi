package utils;

public class TestMe {

	public static int instanceCounter = 0;

	public int myNumber = 0;

	public TestMe() {
		myNumber = instanceCounter++;
	}

	public String toString() {
		return "Hello, myNumber is " + myNumber;
	}

	public static void main(String[] ignore) {

		TestMe testMe1 = new TestMe();

		TestMe testMe2 = new TestMe();

		System.out.println("testMe1 myNumber = " + testMe1.myNumber);

		System.out.println("testMe2 myNumber = " + testMe2.myNumber);

		System.out.println("testMe1 = " + testMe1.toString());

		System.out.println("testMe2 = " + testMe2.toString());
		
		System.out.println("testMe1 = " + testMe1);

		System.out.println("testMe2 = " + testMe2);

	}

}
/*
 * testMe1 myNumber = 0 testMe2 myNumber = 1
 */
