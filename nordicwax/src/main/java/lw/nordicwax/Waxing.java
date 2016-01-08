package lw.nordicwax;

public class Waxing {
	String name;
	int reliability;
	public Waxing(String name, int reliability) {
		super();
		this.name = name;
		this.reliability = reliability;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return the reliability
	 */
	public int getReliability() {
		return reliability;
	}
}
