package com.joeyoey.spacestacker.util;

public class JaroAlg {

	// This gets M
	private static int getMatchingChars(String a, String b) {
		int out = 0;
		try {
			char[] arrayA = a.toCharArray();
			char[] arrayB = b.toCharArray();

			for (int i = 0; i < arrayA.length; i++) {
				for (int j = 0; j < arrayB.length; j++) {
					if (arrayA[i] == arrayB[j]) {
						arrayB[j] = '?';
						out++;
						break;
					}
				}
			}
			return out;
		} catch (Exception e) {
			return 0;
		}
	}

	// This gets T
	private static int getTransposition(String a, String b) {
		int out = 0;
		int size = 0;

		char[] arrayA = a.toCharArray();
		char[] arrayB = b.toCharArray();

		if (arrayA.length >= arrayB.length) {
			out = arrayA.length - arrayB.length;
			size = arrayB.length;
		} else if (arrayA.length <= arrayB.length) {
			out = arrayB.length - arrayA.length;
			size = arrayA.length;
		} else {
			size = arrayA.length;
		}

		for (int i = 0; i < size; i++) {
			if (arrayA[i] != arrayB[i]) {
				out++;
			}
		}

		return out;
	}

	// Applies the other two formulas and gets an output percentage
	public static double getJaroWinkler(String a, String b) {
		double out;

		double m = getMatchingChars(a, b);
		double t = getTransposition(a, b);
		double p = 0.1;
		double l = 3;

		double dj = ((1.0 / 3.0) * ((m / a.length()) + (m / b.length()) + ((m - t) / m)));

		double dw = (dj + ((l * p) * (1 - dj)));

		out = dw * 100;

		return out;
	}

}
