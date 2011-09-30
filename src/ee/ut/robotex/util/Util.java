package ee.ut.robotex.util;

import java.util.Random;

public class Util {
	public static float random(float min, float max) {
		Random r = new Random();
		
		return min + (r.nextFloat() * (max - min));
	}
}