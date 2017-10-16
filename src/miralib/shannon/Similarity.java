/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package miralib.shannon;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.distribution.TDistribution;

import java.util.ArrayList;
import java.math.RoundingMode;
import java.util.HashMap;

import miralib.data.DataSlice2D;
import miralib.data.Value2D;
import miralib.data.Variable;
import miralib.math.Numbers;
import miralib.utils.Project;

/**
 * Similarity score between two variables.
 *
 */

public class Similarity {
  protected static NormalDistribution normDist = new NormalDistribution();
  protected static HashMap<Double, Double> criticalValues = new HashMap<Double, Double>();
  
  static public float calculate(DataSlice2D slice, Project prefs, int depTest, int numTail, int binAlgo, float pvalue) {
	    //prefs.pvalue() = pvalue;
	    //int binAlgo = prefs.binAlgo; //it's overloaded so user can quickly compare different algorithms
    
	    Variable varx = slice.varx;
	    Variable vary = slice.vary;
	    
	    if (varx == vary) return 1;

	    if (varx.weight() || vary.weight() || (varx.subsample() && vary.subsample())) {
	      // weight variables are not comparable, or subsample variables between 
	      // each other
	      return 0;
	    } 
	    
	    Double area = new Double(1 - pvalue/2);
	    Double cval = criticalValues.get(area);
	    if (cval == null) {
	      cval = normDist.inverseCumulativeProbability(area);      
	      criticalValues.put(area,  cval);
	    } 
	    
	    int count = slice.values.size();
	    int[] res = BinOptimizer.calculate(slice, prefs.binAlgorithm);
	    int binx = res[0];
	    int biny = res[1];
	    
	    float ixy = MutualInformation.calculate(slice, binx, biny);
	    boolean indep = false;
	    
	    
	    if (Float.isNaN(ixy) || Float.isInfinite(ixy)) {
	      indep = true;
	    } else if (prefs.depTest == DependencyTest.NO_TEST || Numbers.equal(pvalue, 1)) {
	      indep = ixy <= prefs.threshold;
	    } else if (depTest == DependencyTest.SURROGATE_GAUSS) {
	    	indep = surrogateGauss(slice, ixy, binAlgo, prefs.surrCount, cval);                 
	    } else if (depTest == DependencyTest.SURROGATE_GENERAL) {      
	      indep = surrogateGeneral(slice, ixy, binAlgo, pvalue);
	    } else if (depTest == DependencyTest.GAMMA_TEST) {
	      indep = gammaTest(ixy, binx, biny, count, pvalue);
	    } else if (depTest == DependencyTest.SPEARMAN_TEST) {
	    	indep = spearmanTest(slice, pvalue, numTail);
	    } else if (depTest == DependencyTest.PEARSON_TEST) {
	    	indep = pearsonTest(slice, pvalue, numTail);
	    } else if (depTest == DependencyTest.FISHER_TEST) {
	    	indep = fisherTest(slice, pvalue, binAlgo, numTail);
	    } 
	    /*else if (depTest == CHISQUARE_TEST) {
	    	indep = chisquareTest(ixy,binx,biny,count,pvalue);
	    }
	    */
	    /*
	    if (indep) {
	        return 0;
	      } else {
	        float hxy = JointEntropy.calculate(slice, binx, biny);      
	        float w;
	        if (Numbers.equal(0.0, hxy)) {
	          w = 0;
	        } else {
	          w = Numbers.constrain(ixy / hxy, 0, 1);
	          if (Float.isNaN(w)) w = 0;
	        }      
	        return w;
	      }
	    */
	    if (indep) {
	      return 0;
	    } else {
	      float hxy = JointEntropy.calculate(slice, binx, biny);      
	      float w;
	      if (Numbers.equal(0.0, hxy)) {
	        w = 0;
	      } else {
	        w = Numbers.constrain(ixy / hxy, 0, 1);
	        if (Float.isNaN(w)) w = 0;
	      }
	      if(w==0)
	    	  return 0;
	      else
	    	  return 1;
	    }
	  }  
  
  static public float calculate(DataSlice2D slice, float pvalue, Project prefs) {
    Variable varx = slice.varx;
    Variable vary = slice.vary;

    if (varx.weight() || vary.weight() || (varx.subsample() && vary.subsample())) {
      // weight variables are not comparable, or subsample variables between 
      // each other
      return 0;
    } 
    
    Double area = new Double(1 - pvalue/2);
    Double cval = criticalValues.get(area);
    if (cval == null) {
      cval = normDist.inverseCumulativeProbability(area);      
      criticalValues.put(area,  cval);
    } 
    
    int count = slice.values.size();
    int[] res = BinOptimizer.calculate(slice, prefs.binAlgorithm);
    int binx = res[0];
    int biny = res[1];
    
    float ixy = MutualInformation.calculate(slice, binx, biny);
    boolean indep = false;
            
    if (Float.isNaN(ixy) || Float.isInfinite(ixy)) {
      indep = true;
    } else if (prefs.depTest == DependencyTest.NO_TEST || Numbers.equal(pvalue, 1)) {
      indep = ixy <= prefs.threshold;
    } else if (prefs.depTest == DependencyTest.SURROGATE_GAUSS) {
      indep = surrogateGauss(slice, ixy, prefs.binAlgorithm, prefs.surrCount, cval);            
    } else if (prefs.depTest == DependencyTest.SURROGATE_GENERAL) {      
      indep = surrogateGeneral(slice, ixy, prefs.binAlgorithm, pvalue);
    } else if (prefs.depTest == DependencyTest.GAMMA_TEST) {
        indep = gammaTest(ixy, binx, biny, count, pvalue);
    }
    
    if (indep) {
      return 0;
    } else {
      float hxy = JointEntropy.calculate(slice, binx, biny);      
      float w;
      if (Numbers.equal(0.0, hxy)) {
        w = 0;
      } else {
        w = Numbers.constrain(ixy / hxy, 0, 1);
        if (Float.isNaN(w)) w = 0;
      }      
      return w;
    }
  }  
  
  static protected boolean surrogateGauss(DataSlice2D slice, float ixy,
                                          int binAlgo, int scount, double cvalue) {
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
    float zs = (ixy - meani) / stdi;
    if (Float.isNaN(zs) || Float.isInfinite(zs)) {
      return true;
    } else { 
      return -cvalue <= zs && zs <= cvalue;
    }    
  }
  
  static protected boolean surrogateGeneral(DataSlice2D slice, float ixy, 
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
	  for(int i = 0; i < size; ++i)
	  {
		  v1[i] = items.get(i).x;
		  v2[i] = items.get(i).y;
	  }
	    try {
	    	Double s = new SpearmansCorrelation().correlation(v1,v2);
	  	  double t = s * Math.sqrt((size - 2) / (1 - s * s));
	  	  double p = 1 - new TDistribution(size - 1).cumulativeProbability(t);
	  	  if (numTail == 1)
	  		  return pval < p;
	  	  else
	  		  return pval < 2 * p;	 
	      } catch (Exception ex) {
	        return true;
	      }    
  }

  static protected boolean pearsonTest(DataSlice2D slice, double pval, int numTail) {
	  ArrayList<Value2D> items = slice.values;
	  int size = items.size();
	  double [] v1 = new double[size];
	  double [] v2 = new double[size];
	  for(int i = 0; i < size; ++i)
	  {
		  v1[i] = items.get(i).x;
		  v2[i] = items.get(i).y;
	  }
	  double s = new PearsonsCorrelation().correlation(v1,v2);
	  double t = s * Math.sqrt((size - 2) / (1 - s * s));
	  double p = 1 - new TDistribution(size - 1).cumulativeProbability(t);
	  if (numTail == 1)
		  return pval < p;
	  else
		  return pval < 2 * p;	  
//	  double z = 0.5 * Math.log((1+s)/(1-s));
//	  double p = 0.5*(1+Erf.erf(z/Math.sqrt(2.0)));
  }
  
  static protected boolean fisherTest(DataSlice2D slice, float pvalue, int binAlgo, int numTail) {
	  int [][] freqMatrix = Histogram.calculate2DArray(slice, binAlgo);

	try
	{
		  double p = hdMM.getHypergeometricDistribution(freqMatrix, 0, RoundingMode.HALF_UP).doubleValue();
		  if (numTail == 1)
			  return pvalue < p;
		  else
			  return pvalue < p*2;
	  }
	  catch(Exception ex) {
	  	return true;
	  }
  }
}
