package miralib.data;

import java.io.IOException;
import java.io.InputStream;

import miralib.utils.Log;
import processing.data.Table;

public class MiraDict extends Table implements DataDict {

  public MiraDict(InputStream input, String options) throws IOException {
    super(input, options);
  }
  
  @Override
  public DataSource typedSource(InputStream input, String options) throws IOException {
    MiraTable table = new MiraTable();
    table.setColumnTypes(this);
    table.parseInput(input, options);
    return (DataSource) table;
  }  
  
  @Override
  public DataSource typedSource(InputStream input, DataDict dict, String options, String missing) throws IOException {
    MiraTable table = new MiraTable();
    table.setMissingString(missing);
    table.setColumnTypes((Table)dict);
    try {
      table.parseInput(input, options);
    } catch (IOException e) {
      Log.error("Cannot parse data", e);        
    }    
    for (int i = 0; i < table.getColumnCount(); i++) {
      table.dateColumns[i] = MiraTable.isDateColumn(table, i, missing);
    }
    return (DataSource)table;    
  }  
}
