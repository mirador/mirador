package miralib.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Fileu {
  static public String checkExtension(String filename) {
    // Don't consider the .gz as part of the name, createInput()
    // and createOuput() will take care of fixing that up.
    if (filename.toLowerCase().endsWith(".gz")) {
      filename = filename.substring(0, filename.length() - 3);
    }
    int dotIndex = filename.lastIndexOf('.');
    if (dotIndex != -1) {
      return filename.substring(dotIndex + 1).toLowerCase();
    }
    return null;
  }
 
  
  static public String[] loadStrings(File file) {
    InputStream is = createInput(file);
    if (is != null) {
      String[] outgoing = loadStrings(is);
      try {
        is.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return outgoing;
    }
    return null;
  } 
  
  static public String[] loadStrings(InputStream input) {
    try {
      BufferedReader reader =
        new BufferedReader(new InputStreamReader(input, "UTF-8"));
      return loadStrings(reader);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
  
  static public String[] loadStrings(BufferedReader reader) {
    try {
      String lines[] = new String[100];
      int lineCount = 0;
      String line = null;
      while ((line = reader.readLine()) != null) {
        if (lineCount == lines.length) {
          String temp[] = new String[lineCount << 1];
          System.arraycopy(lines, 0, temp, 0, lineCount);
          lines = temp;
        }
        lines[lineCount++] = line;
      }
      reader.close();

      if (lineCount == lines.length) {
        return lines;
      }

      // resize array to appropriate amount for these lines
      String output[] = new String[lineCount];
      System.arraycopy(lines, 0, output, 0, lineCount);
      return output;

    } catch (IOException e) {
      e.printStackTrace();
      //throw new RuntimeException("Error inside loadStrings()");
    }
    return null;
  } 
  
  static public InputStream createInput(File file) {
    if (file == null) {
      throw new IllegalArgumentException("File passed to createInput() was null");
    }
    try {
      InputStream input = new FileInputStream(file);
      if (file.getName().toLowerCase().endsWith(".gz")) {
        return new GZIPInputStream(input);
      }
      return input;

    } catch (IOException e) {
      System.err.println("Could not createInput() for " + file);
      e.printStackTrace();
      return null;
    }
  } 
  
  static public void createPath(File file) {
    try {
      String parent = file.getParent();
      if (parent != null) {
        File unit = new File(parent);
        if (!unit.exists()) unit.mkdirs();
      }
    } catch (SecurityException se) {
      System.err.println("You don't have permissions to create " +
                         file.getAbsolutePath());
    }
  } 
  
  static public PrintWriter createWriter(File file) {
    try {
      createPath(file);  // make sure in-between folders exist
      OutputStream output = new FileOutputStream(file);
      if (file.getName().toLowerCase().endsWith(".gz")) {
        output = new GZIPOutputStream(output);
      }
      return createWriter(output);

    } catch (Exception e) {
      if (file == null) {
        throw new RuntimeException("File passed to createWriter() was null");
      } else {
        e.printStackTrace();
        throw new RuntimeException("Couldn't create a writer for " +
                                   file.getAbsolutePath());
      }
    }
    //return null;
  } 
  
  static public PrintWriter createWriter(OutputStream output) {
    try {
      BufferedOutputStream bos = new BufferedOutputStream(output, 8192);
      OutputStreamWriter osw = new OutputStreamWriter(bos, "UTF-8");
      return new PrintWriter(osw);
    } catch (UnsupportedEncodingException e) { }  // not gonna happen
    return null;
  } 
}
