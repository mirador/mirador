package miralib.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import miralib.data.DataSet.CodebookPage;

public class DataFactory {
  static public DataSource createSource(InputStream input, String options) 
      throws IOException {
    return (DataSource) new MiraTable(input, options);
  }
  
  static public DataDict createDict(InputStream input, String options) 
      throws IOException {
    return (DataDict) new MiraDict(input, options);
  }
  
  static public DataSource guessedParse(InputStream input, 
      HashMap<String, CodebookPage> codebook, String options, String missing) {
    return (DataSource) MiraTable.guessedParse(input, codebook, options, missing);
  }
}
