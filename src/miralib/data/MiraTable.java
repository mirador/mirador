/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package miralib.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import miralib.data.DataSet.CodebookPage;
import miralib.utils.Log;
import processing.core.PApplet;
import processing.data.Table;
import processing.data.TableRow;

/**
 * Customized table class
 *
 */

public class MiraTable extends Table implements DataSource {
  static public final int DATE = 6;
  
  boolean[] dateColumns;
  
  final static protected int[] CHECK_FRACTION = {1, 2, 10, 100};
  
  {
    missingInt = Integer.MIN_VALUE;
    missingLong = Long.MIN_VALUE;
    missingFloat = Float.NEGATIVE_INFINITY;
    missingDouble = Double.NEGATIVE_INFINITY;
  }
  
  public MiraTable() {
    super();
  }  
  
  public MiraTable(InputStream input, String options) throws IOException {
    super(input, options);
    for (int i = 0; i < getColumnCount(); i++) {
      dateColumns[i] = isDateColumn(this, i, missingString);
    }
  }
  
  public void setColumnTypes(final Table dictionary) {
    ensureColumn(dictionary.getRowCount() - 1);
    int typeCol = 1;
    final String[] typeNames = dictionary.getStringColumn(typeCol);

    if (dictionary.getColumnCount() > 1) {
      if (getRowCount() > 1000) {
        int proc = Runtime.getRuntime().availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(proc/2);
        for (int i = 0; i < dictionary.getRowCount(); i++) {
          final int col = i;
          pool.execute(new Runnable() {
            public void run() {
              setColumnType(col, typeNames[col]);
            }
          });
        }
        pool.shutdown();
        while (!pool.isTerminated()) {
          Thread.yield();
        }

      } else {
        for (int col = 0; col < dictionary.getRowCount(); col++) {
          setColumnType(col, typeNames[col]);
        }
      }
    }
  }
  
  public int getColumnType(int column) {
    if (dateColumns[column]) return DATE; 
    else return super.getColumnType(column);
  }  
  
  public void setColumnType(int column, String columnType) {
    columnType = columnType.toLowerCase().trim();
    int type = -1;
    if (columnType.equals("string") || columnType.equals("date")) {
      type = STRING;
    } else if (columnType.equals("int")) {
      type = INT;
    } else if (columnType.equals("long")) {
      type = LONG;
    } else if (columnType.equals("float")) {
      type = FLOAT;
    } else if (columnType.equals("double")) {
      type = DOUBLE;
    } else if (columnType.equals("category")) {
      type = CATEGORY;
    } else {
      throw new IllegalArgumentException("'" + columnType + "' in column " + 
                                         column + " in dictionary file\n" + 
                                         " is not a valid column type.");
    }
    setColumnType(column, type);
  }  
  
  public void parseInput(InputStream input, String options) throws IOException {
    super.parse(input, options);
  }
  
  static public boolean supportedDateString(String str) {
    return DateVariable.parse(str) != null;
  }
  
  static public MiraTable guessedParse(InputStream input, 
                                       HashMap<String, CodebookPage> codebook, 
                                       String options, String missing) {
    MiraTable table = new MiraTable();
    table.setMissingString(missing);
    try {
      table.parse(input, options);
    } catch (IOException e) {
      Log.error("Cannot parse data", e);
    }
    
    // obtaining types from codebook, or trying to guess from data...
    for (int i = 0; i < table.getColumnCount(); i++) {
      String name = table.getColumnTitle(i);
      if (name == null || name.equals("")) continue;
      CodebookPage pg = codebook == null ? null : codebook.get(name);
      if (pg == null) {
        int guess = guessColumnType(table, i, missing);
        table.setColumnType(i, guess);
        table.dateColumns[i] = isDateColumn(table, i, missing);
      } else {
        table.setColumnType(i, pg.type);
      }
//    Log.message("Column " + i + " " + table.getColumnTitle(i) + ": " + guess  + " " + table.getColumnType(i));
    }
    
    return table;
  }
  
  static protected int guessColumnType(Table table, int i, String missing) {
    int type0 = table.getColumnType(i);
    if (table.getColumnType(i) != Table.STRING) return type0;
    
    int[] typeCounts = {0, 0, 0, 0, 0};  // string, int, long, float, double
    float[] typeFracs = {0, 0, 0, 0, 0}; // string, int, long, float, double  
    HashSet<String> diffValues = new HashSet<String>(); 
    int count = 0;
    int tot = table.getRowCount();
    int step = 1;
    if (tot < 1000) step = CHECK_FRACTION[0];
    else if (tot < 10000) step = CHECK_FRACTION[1];
    else if (tot < 100000) step = CHECK_FRACTION[2];
    else step = CHECK_FRACTION[3];
    for (int n = 0; n < table.getRowCount(); n++) {      
      if (n % step != 0) continue;
      
      TableRow row = table.getRow(n); 
      String value = row.getString(i);
      if (value.equals(missing)) continue;
      diffValues.add(value);
      count++;
      
      if (isInt(value)) typeCounts[Table.INT]++;
      else if (isFloat(value)) typeCounts[Table.FLOAT]++;
      else if (isLong(value)) typeCounts[Table.LONG]++;
      else if (isDouble(value)) typeCounts[Table.DOUBLE]++;
      else typeCounts[Table.STRING]++;       
    }
    
    if (count == 0) return Table.STRING;
    
    int nonZeroTypes = 0;
    for (int t = Table.STRING; t <= Table.DOUBLE; t++) {
      if (0 < typeCounts[t]) nonZeroTypes++;
      typeFracs[t] = (float)typeCounts[t] / count;            
    }
    float min_fraction = 1.0f/nonZeroTypes;
    int strcat_max_count = (int)(count * 0.5f);
    
    if (min_fraction <= typeFracs[Table.STRING]) {
      // String or category 
      if (diffValues.size() <= strcat_max_count) return Table.CATEGORY;
      else return Table.STRING;
    } else {
      // Numerical or category
      if (min_fraction <= typeFracs[Table.INT] && 
          typeFracs[Table.LONG] < min_fraction && 
          typeFracs[Table.FLOAT] < min_fraction && 
          typeFracs[Table.DOUBLE] < min_fraction) {
        if (diffValues.size() <= 5) return Table.CATEGORY;
        else return Table.INT;
      }
      if (min_fraction <= typeFracs[Table.LONG] && 
          typeFracs[Table.INT] < min_fraction && 
          typeFracs[Table.FLOAT] < min_fraction && 
          typeFracs[Table.DOUBLE] < min_fraction) {
        if (diffValues.size() <= 5) return Table.CATEGORY;
        else return Table.LONG;      
      }
      if (min_fraction <= typeFracs[Table.FLOAT] && 
          typeFracs[Table.LONG] < min_fraction && 
          typeFracs[Table.DOUBLE] < min_fraction) {
        return Table.FLOAT;
      }
      if (min_fraction <= typeFracs[Table.DOUBLE]) {
        return Table.DOUBLE;
      } 
    }
    
    return Table.STRING;
  }
  
  static protected boolean isDateColumn(Table table, int i, String missing) {
    if (table.getColumnType(i) == Table.STRING || 
        table.getColumnType(i) == Table.CATEGORY) {
      int totCount = 0;
      int dateCount = 0;
      for (int n = 0; n < table.getRowCount(); n++) {
        TableRow row = table.getRow(n); 
        String value = row.getString(i);
        if (value == null || value.equals(missing)) continue;
        if (supportedDateString(value)) dateCount++;
        totCount++;
        if (totCount == 10) break;
      }
      float frac = (float)dateCount / (float)totCount;
      return 0.5f < frac;    
    } else {
      return false;
    }
  }
  
  public String getMissingString() {
    return missingString;
  }
   
  public int getMissingInt() {
    return missingInt;
  }

  public long getMissingLong() {
    return missingLong;  
  }
  
  public float getMissingFloat() {
    return missingFloat;  
  }
  
  public double getMissingDouble() {
    return missingDouble;  
  }
  
  public int getMissingCategory() {
    return missingCategory;
  }
  
  
  protected void init() {
    super.init();
    dateColumns = new boolean[0];
  }
  
  
  public void insertColumn(int index, String title, int type) {
    super.insertColumn(index, title, type);
    dateColumns = PApplet.splice(dateColumns, false, index);
  }

  
  public void setColumnCount(int newCount) {
    int oldCount = columns.length;
    if (oldCount != newCount) {
      super.setColumnCount(newCount);
      dateColumns = PApplet.expand(dateColumns, newCount);
    }
  }
 
  
  public String getColumnTypeAsString(int column) {
    int type = getColumnType(column);
    return Variable.formatType(type);
  }
  
  
  protected MiraTable createSubset(int[] rowSubset) {
    MiraTable newbie = new MiraTable();
    newbie.setColumnTitles(getColumnTitles());  // also sets columns.length
    newbie.setColumnTypes(getColumnTypes());
    newbie.dateColumns = dateColumns;
    newbie.setRowCount(rowSubset.length);
    for (int i = 0; i < rowSubset.length; i++) {
      int row = rowSubset[i];
      for (int col = 0; col < columns.length; col++) {
        switch (getColumnType(col)) {
          case STRING: newbie.setString(i, col, getString(row, col)); break;
          case INT: newbie.setInt(i, col, getInt(row, col)); break;
          case LONG: newbie.setLong(i, col, getLong(row, col)); break;
          case FLOAT: newbie.setFloat(i, col, getFloat(row, col)); break;
          case DOUBLE: newbie.setDouble(i, col, getDouble(row, col)); break;
        }
      }
    }
    return newbie;
  }
  
  static protected boolean isInt(String str) {
    try {
      new Integer(str);
      return true;
    } catch (NumberFormatException e) {
      return false; 
    }  
  }

  static protected boolean isLong(String str) {
    try {
      new Long(str);
      return true;
    } catch (NumberFormatException e) {
      return false; 
    }  
  }

  static protected boolean isFloat(String str) {
    try {
      new Float(str);
      return true;
    } catch (NumberFormatException e) {
      return false; 
    }  
  }

  static protected boolean isDouble(String str) {
    try {
      new Double(str);
      return true;
    } catch (NumberFormatException e) {
      return false; 
    }  
  }
}
