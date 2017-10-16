package miralib.data;

import java.io.IOException;
import java.io.InputStream;

import processing.data.TableRow;

public interface DataDict {
  public int getRowCount();
  public TableRow getRow(int row); 
  public DataSource typedSource(InputStream input, String options) throws IOException;
  public DataSource typedSource(InputStream input, DataDict dict, String options, String missing) throws IOException; 
}
