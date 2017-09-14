package miralib.shannon;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode; //included RoundingMode
import java.util.ArrayList;
import java.util.List;

//DISCLAIMER: MODIFICATIONS MADE BY ELIZABETH CHIN 06/26/2015
//Comments are placed after each modification

/**
 * Class to find Hypergeometric Distribution.
 * 
 * <p>
 * Example use:
 * <p>
 * <code>	int[][] a = { { 5, 0 }, { 1, 4 } };</code><br>
 * <code>	System.out.println(hdMM.getHypergeometricDistribution(a, 5, 6));</code>
 * <p>
 * Copyright (C) 2011 by Margus Martsepp
 * 
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * @author Margus Martsepp
 */

public class hdMM {
	private static final int maxSize = 100; //increased max size since symmetric matrix
	private static ArrayList<BigInteger> f = new ArrayList<BigInteger>();
	static {
		f.add(BigInteger.ONE);
		getFactorial(maxSize);
	}

	/**
	 * Function that returns factorial. Uses dynamic programming to speed up
	 * calculations. Quite efficient for factorials below 1000.
	 * 
	 * @param nr
	 *            Factorial to calculate.
	 * @return factorial or null for negative numbers.
	 */
	static BigInteger getFactorial(int nr) throws OutOfMemoryError {
		if (nr < 0)
			return null;
		for (int i = f.size(); i <= nr; i++)
			f.add(f.get(i - 1).multiply(BigInteger.valueOf(i)));
		return f.get(nr);
	}

	/**
	 * Using multiplicative formula.
	 * 
	 * @param n
	 *            nr of elements, nonnegative integer, with k ? n
	 * @param k
	 *            nr of distinct elements, nonnegative integer
	 * @return Binomial coefficient or null for invalid inputs.
	 */
	static BigInteger getBinomialCoefficient(int n, int k)
			throws OutOfMemoryError, NullPointerException {
		if (n < 1 || k < 1 || k > n)
			return null;
		return BigInteger.valueOf(n).pow(k).divide(getFactorial(k));
	}

	/**
	 * Based on <a
	 * href="http://mathworld.wolfram.com/FishersExactTest.html" >Fisher's
	 * exact test</a>.
	 * 
	 * @param a
	 *            element [1,1], nonnegative integer
	 * @param b
	 *            element [1,1], nonnegative integer
	 * @param c
	 *            element [1,1], nonnegative integer
	 * @param d
	 *            element [2,2], nonnegative integer
	 * 
	 * @return Hypergeometric distribution.
	 */
	public static BigDecimal getHypergeometricDistribution(//
			int a[][], int scale, RoundingMode roundingMode// //modified type int -> RoundingMode
	) throws OutOfMemoryError, NullPointerException {
		ArrayList<Integer> R = new ArrayList<Integer>();
		ArrayList<Integer> C = new ArrayList<Integer>();
		ArrayList<Integer> E = new ArrayList<Integer>();
		int n = 0;

		for (int i = 0; i < a.length - 1; i++) { //modified since comparing symmetric matrix
			for (int j = i+1; j < a[i].length; j++) {
				if (a[i][j] < 0)
					return null;

				n += a[i][j];
				add(C, j, a[i][j]);
				add(R, i, a[i][j]);
				E.add(a[i][j]);
			}
		}
		BigDecimal term1 = //
		new BigDecimal(multiplyFactorials(C).multiply(multiplyFactorials(R)));
		BigDecimal term2 = //
		new BigDecimal(getFactorial(n).multiply(multiplyFactorials(E)));

		return term1.divide(term2, scale, roundingMode);
	}

	// utility method
	private static BigInteger multiplyFactorials(List<Integer> c) {
		BigInteger sum = BigInteger.ONE;
		for (Integer i : c) {
			sum = sum.multiply(getFactorial(i));
		}
		return sum;
	}

	// utility method
	private static void add(List<Integer> r, int nr, int val) {
		while (r.size() <= nr)
			r.add(0);
		r.set(nr, r.get(nr) + val);
	}
}