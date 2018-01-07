package dc.aap;

public class IDGen {
	static int counter = 0;

	public static int GID() {
		counter++;
		return counter;
	}
}
