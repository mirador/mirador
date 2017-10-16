package miralib.data;

import java.io.File;
import java.io.IOException;

import processing.data.TableRow;

public interface DataSource {  
  public String getMissingString();
  public int getColumnCount();
  public String getColumnTitle(int col);
  public int getColumnType(String columnName);
  public int getColumnType(int column);
  public int getRowCount();
  public TableRow getRow(int row);
  public boolean save(File file, String options) throws IOException;
}