/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */
package miralib.shannon;

import miralib.utils.Log;

/**
 * Types of dependency tests in Miralib
 *
 */

public class DependencyTest {
  // List of accepted dependency-testing algorithms
	  final static public int NO_TEST           = 0;
	  final static public int SURROGATE_GAUSS   = 1;
	  final static public int SURROGATE_GENERAL = 2;
	  final static public int NORMAL_TRANSFORM  = 3;
	  final static public int GAMMA_TEST        = 4;
	  final static public int PEARSON_TEST		= 5;
	  final static public int SPEARMAN_TEST		= 6;
	  final static public int FISHER_TEST		= 7;
	  final static public int CHISQUARE_TEST	= 8;
  
	  static public String algorithmToString(int algo) {
		    if (algo == NO_TEST) {        
		      return "NO_TEST";
		    } else if (algo == SURROGATE_GAUSS) {
		      return "SURROGATE_GAUSS";
		    } else if (algo == SURROGATE_GENERAL) {
		      return "SURROGATE_GENERAL";
		    } else if (algo == NORMAL_TRANSFORM) {
		      return "NORMAL_TRANSFORM";
		    } else if (algo == GAMMA_TEST) {
		      return "GAMMA_TEST";
		    } else if (algo == PEARSON_TEST) {
		    	return "PEARSON_TEST";
		    } else if (algo == SPEARMAN_TEST) {
		    	return "SPEARMAN_TEST";
		    } else if (algo == FISHER_TEST) {
		    	return "FISHER_TEST";
		    } else if (algo == CHISQUARE_TEST) {
		    	return "CHISQUARE_TEST";
		    }
		    String err = "Unsupported similarity algorithm: " + algo;
		    Log.error(err, new RuntimeException(err));
		    return "unsupported";    
		  }
		  
		  static public int stringToAlgorithm(String name) {
		    name = name.toUpperCase();
		    if (name.equals("NO_TEST")) {
		      return NO_TEST;
		    } else if (name.equals("SURROGATE_GAUSS")) {
		      return SURROGATE_GAUSS;
		    } else if (name.equals("SURROGATE_GENERAL")) {
		      return SURROGATE_GENERAL;
		    } else if (name.equals("NORMAL_TRANSFORM")) {
		      return NORMAL_TRANSFORM;
		    } else if (name.equals("GAMMA_TEST")) {
		      return GAMMA_TEST;
		    } else if (name.equals("PEARSON_TEST")){
		    	return PEARSON_TEST;
		    } else if(name.equals("SPEARMAN_TEST")){
		    	return SPEARMAN_TEST;
		    } else if(name.equals("FISHER_TEST")){
		    	return FISHER_TEST;
		    } else if(name.equals("CHISQUARE_TEST")) {
		    	return CHISQUARE_TEST;
		    }
		    String err = "Unsupported similarity algorithm: " + name;
		    Log.error(err, new RuntimeException(err));
		    return -1;
		  }
}
