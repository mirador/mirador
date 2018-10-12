package miralib.shannon;

import miralib.data.DataSlice2D;
import miralib.data.Value2D;
import miralib.math.Numbers;
import miralib.utils.Project;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode; //included RoundingMode
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//DISCLAIMER: MODIFICATIONS MADE BY ELIZABETH CHIN 06/26/2015
//Comments are placed after each modification

/**
 * Code to find Statistics Distribution is Copyright (C) 2011 by Margus Martsepp
 * 
 * <p>
 * Example use:
 * <p>
 * <code>	int[][] a = { { 5, 0 }, { 1, 4 } };</code><br>
 * <code>	System.out.println(Statistics.getHypergeometricDistribution(a, 5, 6));</code>
 * <p>
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

public class Statistics {
	protected static NormalDistribution normDist = new NormalDistribution();
	protected static HashMap<Double, Double> criticalValues = new HashMap<Double, Double>();

	private static final int maxSize = 100; //increased max size since symmetric matrix
	private static ArrayList<BigInteger> f = new ArrayList<BigInteger>();
	static {
		f.add(BigInteger.ONE);
		getFactorial(maxSize);
	}

  static protected boolean independenceTest(DataSlice2D slice, float ixy, int binx, int biny, Project prefs,
                                            int depTest, int numTail, int binAlgo, float pvalue) {
    boolean indep = false;
    Double cval = getCriticalValue(pvalue);
    if (Float.isNaN(ixy) || Float.isInfinite(ixy)) {
      indep = true;
    } else if (prefs.depTest == DependencyTest.NO_TEST || Numbers.equal(pvalue, 1)) {
      indep = ixy <= prefs.threshold;
    } else if (depTest == DependencyTest.SURROGATE_GAUSS) {
      indep = surrogateGaussTest(slice, ixy, binAlgo, prefs.surrCount, cval);
    } else if (depTest == DependencyTest.SURROGATE_GENERAL) {
      indep = surrogateGeneralTest(slice, ixy, binAlgo, pvalue);
    } else if (depTest == DependencyTest.GAMMA_TEST) {
      indep = gammaTest(ixy, binx, biny, slice.values.size(), pvalue);
    } else if (depTest == DependencyTest.SPEARMAN_TEST) {
      indep = spearmanTest(slice, pvalue, numTail);
    } else if (depTest == DependencyTest.PEARSON_TEST) {
      indep = pearsonTest(slice, pvalue, numTail);
    } else if (depTest == DependencyTest.FISHER_TEST) {
      indep = fisherTest(slice, pvalue, binAlgo, numTail);
    }
    return indep;
  }

	static protected boolean independenceTest(DataSlice2D slice, float ixy, int binx, int biny, float pvalue,
                                            Project prefs) {
    boolean indep = false;
    Double cval = getCriticalValue(pvalue);

    if (Float.isNaN(ixy) || Float.isInfinite(ixy)) {
      indep = true;
    } else if (prefs.depTest == DependencyTest.NO_TEST || Numbers.equal(pvalue, 1)) {
      indep = ixy <= prefs.threshold;
    } else if (prefs.depTest == DependencyTest.SURROGATE_GAUSS) {
      indep = surrogateGaussTest(slice, ixy, prefs.binAlgorithm, prefs.surrCount, cval);
    } else if (prefs.depTest == DependencyTest.SURROGATE_GENERAL) {
      indep = surrogateGeneralTest(slice, ixy, prefs.binAlgorithm, pvalue);
    } else if (prefs.depTest == DependencyTest.GAMMA_TEST) {
      indep = gammaTest(ixy, binx, biny, slice.values.size(), pvalue);
    }

    return indep;
  }

	static protected Double getCriticalValue(float pvalue) {
		Double area = new Double(1 - pvalue/2);
		Double cval = criticalValues.get(area);
		if (cval == null) {
			cval = normDist.inverseCumulativeProbability(area);
			criticalValues.put(area,  cval);
		}
		return cval;
	}

  static protected float getSurrogateGaussDistribution(DataSlice2D slice, float ixy,
                                                       int binAlgo, int scount) {
    int sbinx = 0;
    int sbiny = 0;
    float meani = 0;
    float meaniSq = 0;
    float stdi = 0;
    for (int i = 0; i < scount; i++) {
      DataSlice2D surrogate = slice.shuffle();
      if (i == 0) {
        int[] sres = BinOptimizer.calculate(surrogate, binAlgo);
        sbinx = sres[0];
        sbiny = sres[1];
      }
      float smi = MutualInformation.calculate(surrogate, sbinx, sbiny);
      meani += smi;
      meaniSq += smi * smi;
    }
    meani /= scount;
    meaniSq /= scount;
    stdi = (float)Math.sqrt(Math.max(0, meaniSq - meani * meani));
    return (ixy - meani) / stdi;
  }

	static protected boolean surrogateGaussTest(DataSlice2D slice, float ixy,
                                              int binAlgo, int scount, double cvalue) {
    float zs = getSurrogateGaussDistribution(slice, ixy, binAlgo, scount);
		if (Float.isNaN(zs) || Float.isInfinite(zs)) {
			return true;
		} else {
			return -cvalue <= zs && zs <= cvalue;
		}
	}

	static protected boolean surrogateGeneralTest(DataSlice2D slice, float ixy,
                                                int binAlgo, float pvalue) {
		int sbinx = 0;
		int sbiny = 0;
		float maxMI = 0;
		int numSurr = (int)(1/pvalue) - 1;
		for (int i = 0; i < numSurr; i++) {
			DataSlice2D surrogate = slice.shuffle();
			if (i == 0) {
				int[] sres = BinOptimizer.calculate(surrogate, binAlgo);
				sbinx = sres[0];
				sbiny = sres[1];
			}
			maxMI = Math.max(maxMI, MutualInformation.calculate(surrogate, sbinx, sbiny));
		}
		return ixy < maxMI;
	}

	static protected boolean gammaTest(float ixy, int binx, int biny, int count, float pvalue) {
		double shapePar = (binx - 1) * (biny - 1) / 2d;
		double scalePar = 1d / count;
		try {
			GammaDistribution gammaDist = new GammaDistribution(shapePar, scalePar);
			double c = gammaDist.inverseCumulativeProbability(1 - pvalue);
			return ixy <= c;
		} catch (Exception ex) {
			return true;
		}
	}

	static protected boolean spearmanTest(DataSlice2D slice, double pval, int numTail) {
		ArrayList<Value2D> items = slice.values;
		int size = items.size();
		double [] v1 = new double[size];
		double [] v2 = new double[size];
		for(int i = 0; i < size; ++i) {
			v1[i] = items.get(i).x;
			v2[i] = items.get(i).y;
		}
		try {
			Double s = new SpearmansCorrelation().correlation(v1,v2);
			double t = s * Math.sqrt((size - 2) / (1 - s * s));
			double p = 1 - new TDistribution(size - 1).cumulativeProbability(t);
			if (numTail == 1) return pval < p;
			else return pval < 2 * p;
		} catch (Exception ex) {
			return true;
		}
	}

	static protected boolean pearsonTest(DataSlice2D slice, double pval, int numTail) {
		ArrayList<Value2D> items = slice.values;
		int size = items.size();
		double [] v1 = new double[size];
		double [] v2 = new double[size];
		for(int i = 0; i < size; ++i) {
			v1[i] = items.get(i).x;
			v2[i] = items.get(i).y;
		}
		double s = new PearsonsCorrelation().correlation(v1,v2);
		double t = s * Math.sqrt((size - 2) / (1 - s * s));
		double p = 1 - new TDistribution(size - 1).cumulativeProbability(t);
		if (numTail == 1) return pval < p;
		else return pval < 2 * p;
//	  double z = 0.5 * Math.log((1+s)/(1-s));
//	  double p = 0.5*(1+Erf.erf(z/Math.sqrt(2.0)));
	}

	static protected boolean fisherTest(DataSlice2D slice, float pvalue, int binAlgo, int numTail) {
		int [][] freqMatrix = Histogram.calculate2DArray(slice, binAlgo);
		try {
			double p = Statistics.getHypergeometricDistribution(freqMatrix, 0, RoundingMode.HALF_UP).doubleValue();
			if (numTail == 1) return pvalue < p;
			else return pvalue < 2 * p;
		} catch(Exception ex) {
			return true;
		}
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
	 * @return Statistics distribution.
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