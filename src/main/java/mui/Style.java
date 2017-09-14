/* COPYRIGHT (C) 2014-16 Fathom Information Design. All Rights Reserved. */

package mui;

import java.io.*;
import java.util.HashMap;

import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;

import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSFontFaceRule;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.CSSValue;

import processing.core.PApplet;
import processing.core.PConstants;

/**
 * Parser for UI style defined in a CSS file, uses the SAC and CSSParser
 * libraries:
 * CSS Parser: http://cssparser.sourceforge.net/index.html
 * SAC (The Simple API for CSS): http://www.w3.org/Style/CSS/SAC/
 *
 */

public class Style {
  protected HashMap<String, String> fontFaces;
  protected HashMap<String, Properties> properties;  
  
  public Style() {
    System.setProperty("org.w3c.css.sac.parser", SACParserCSS3.class.getName());
    fontFaces = new HashMap<String, String>();
    properties = new HashMap<String, Properties>();     
  }
  
  public Style(InputStream input) {
    this();
    parseCSS(input);
  }
  
  public String get(String clazz, String property, String dflt) {
    Properties props = properties.get(clazz);    
    if (props == null) return dflt;
    String value = props.get(property);
    if (value == null) return dflt;    
    return value;
  }

  public String getFont(String clazz, String property, String dflt) {
    Properties props = properties.get(clazz);
    if (props == null) return dflt;    
    String value = props.get(property);
    if (value == null) return dflt;
    String filename = fontFaces.get(value);
    filename = filename.replace("url(", "").replace(")", "").replace("'", "");
    if (filename == null) return dflt;
    return filename;
  }  
  
  public String getImage(String clazz, String property, String dflt) {
    return getFilename(clazz, property, dflt); 
  }
  
  public String getShape(String clazz, String property, String dflt) {
    return getFilename(clazz, property, dflt);
  }  
  
  public String getFilename(String clazz, String property, String dflt) {
    Properties props = properties.get(clazz);    
    if (props == null) return dflt;    
    String filename = props.get(property);
//    System.out.println(clazz + "." + property + " " + filename);
    filename = filename.replace("url(", "").replace(")", "").replace("'", "");
    if (filename == null) return dflt;
    return filename;      
  }
  
  public int getColor(String clazz, String property, int dflt) {
    Properties props = properties.get(clazz);    
    if (props == null) return dflt;
    String value = props.get(property);    
    if (value == null) return dflt;
    value = value.toLowerCase();    
    if (value.indexOf('#') == 0) {
      try {      
        return 0xFF000000 | PApplet.unhex(value.substring(1));
      } catch (NumberFormatException ex) {
        return dflt;
      }
    } else if (value.indexOf("rgba(") == 0) {
      int r = 0; 
      int g = 0; 
      int b = 0; 
      int a = 0;

      value = value.substring(5);
      value = value.substring(0, value.length() - 1);

      String[] parts = value.split(",");
      if (parts.length == 4) {        
        r = PApplet.parseInt(parts[0].trim());    
        g = PApplet.parseInt(parts[1].trim());
        b = PApplet.parseInt(parts[2].trim());
        a = 0;
        float fa = PApplet.parseFloat(parts[3]);
        if (0 < fa && fa < 1) {
          a = (int)(fa * 255);
        } else {
          a = (int)fa;
        }  
      } else {
        return dflt; 
      }
      return (a << 24) | (r << 16) | (g << 8) | b;      
    } else if (value.indexOf("rgb(") == 0) {
      int r = 0; 
      int g = 0; 
      int b = 0; 
      int a = 255;
      
      value = value.substring(4);
      value = value.substring(0, value.length() - 1);

      String[] parts = value.split(",");
      if (parts.length == 3) {        
        r = PApplet.parseInt(parts[0].trim());    
        g = PApplet.parseInt(parts[1].trim());
        b = PApplet.parseInt(parts[2].trim());
      } else {
        return dflt; 
      }      
      return (a << 24) | (r << 16) | (g << 8) | b;
    } else if (value.equals("white")) {
      return 0xFFFFFFFF;
    } else if (value.equals("black")) {
      return 0xFF000000;
    }
    return dflt;
  } 
  
  public int getAlign(String clazz, String property, int dflt) {
    Properties props = properties.get(clazz);    
    if (props == null) return dflt;
    String value = props.get(property);  
    if (value == null) return dflt;    
    value = value.toUpperCase();
    if (value.equals("BASELINE")) return PConstants.BASELINE;    
    else if (value.equals("LEFT")) return PConstants.LEFT;
    else if (value.equals("RIGHT")) return PConstants.RIGHT;
    else if (value.equals("CENTER")) return PConstants.CENTER;    
    else if (value.equals("TOP")) return PConstants.TOP;
    else if (value.equals("BOTTOM")) return PConstants.BOTTOM;    
    return dflt;    
  }
  
  public float getPosition(String clazz, String property, float dflt) {
    Properties props = properties.get(clazz);    
    if (props == null) return dflt;
    String value = props.get(property);
    if (value == null) return dflt;
    value = value.replaceAll("px", "").replaceAll("pt", "");
    return Display.scalef(PApplet.parseFloat(value, dflt));
  }  
  
  public float getSize(String clazz, String property, float dflt) {
    Properties props = properties.get(clazz);    
    if (props == null) return dflt;
    String value = props.get(property);  
    if (value == null) return dflt;
    value = value.replaceAll("px", "").replaceAll("pt", "");
    return Display.scalef(PApplet.parseFloat(value, dflt));
  }   
  
  protected void parseCSS(InputStream input) {
    InputSource source = new InputSource(new InputStreamReader(input));
    CSSOMParser parser = new CSSOMParser();
    try {
      CSSStyleSheet stylesheet = parser.parseStyleSheet(source, null, null);      
      CSSRuleList ruleList = stylesheet.getCssRules();
      
      for (int i = 0; i < ruleList.getLength(); i++) {
        CSSRule rule = ruleList.item(i);
//        System.out.println("*********************************");
//        System.out.println(rule + " - Type: " + rule.getType());
        
        int type = rule.getType();
        if (type == CSSRule.FONT_FACE_RULE) {
          CSSFontFaceRule fontRule = (CSSFontFaceRule)rule;
          CSSStyleDeclaration declaration = fontRule.getStyle();
          String fontName = ""; 
          String fontSrc = "";
          for (int j = 0; j < declaration.getLength(); j++) {
            String property = declaration.item(j);
            CSSValue cssval = declaration.getPropertyCSSValue(property);
            String value = cssval.getCssText();
            if (property.equals("font-family")) fontName = value;
            if (property.equals("src")) fontSrc = value;            
          }
          if (!fontName.equals("") && !fontSrc.equals("")) {
            fontFaces.put(fontName, fontSrc);
//            Log.message("Adding font name/src pair: " + fontName + "/" + fontSrc);              
          }          
        } else if (type == CSSRule.STYLE_RULE) {
          CSSStyleRule styleRule = (CSSStyleRule)rule;
          CSSStyleDeclaration declaration = styleRule.getStyle();
          String selector = styleRule.getSelectorText();          
          String[] classes = selector.split(",");          
          String[] clazzes = new String[classes.length];
          for (int j = 0; j < classes.length; j++) {
            String classp = classes[j];            
            String[] pieces = classp.split(" ");
            String clazz = "";            
            for (String piece: pieces) {
              piece = piece.replace("*.", "");
              piece = piece.replace("#.", "");
              piece = piece.replace("*#", "");              
              String[] more = piece.split("#|\\:");              
              for (String s: more) {
                String s0 = s.trim();
                if (s0.equals("")) continue;
                if (!clazz.equals("")) clazz += ".";
                clazz += s;       
              }
            }
            clazzes[j] = clazz;
          }
          
          for (String clazz: clazzes) {
            Properties props = properties.get(clazz);  
            if (props == null) {
              props = new Properties(clazz);
              properties.put(clazz, props);
            }
            
            for (int j = 0; j < declaration.getLength(); j++) {
              String property = declaration.item(j);
              CSSValue cssval = declaration.getPropertyCSSValue(property);
              short valtyp = cssval.getCssValueType();
              if (valtyp == CSSValue.CSS_PRIMITIVE_VALUE) {
                CSSPrimitiveValue pval = (CSSPrimitiveValue)cssval;
                String value = pval.getCssText();             
                if (!property.equals("") && !value.equals("")) {  
                  props.put(property, value);           
                }    
              } else {
                
              // Skipping non-primitive value
//            PApplet.println(CSSValue.CSS_CUSTOM + " CUSTOM");
//            PApplet.println(CSSValue.CSS_INHERIT + " INHERIT");
//            PApplet.println(CSSValue.CSS_PRIMITIVE_VALUE + " PRIMITIVE_VALUE");
//            PApplet.println(CSSValue.CSS_VALUE_LIST + " VALUE_LIST");               

              // Maybe can support shorthand properties in the future:
              // https://developer.mozilla.org/en-US/docs/Web/CSS/Shorthand_properties
              // http://java2s.com/Open-Source/Java/Graphic-Library/batik/org/apache/batik/css/engine/value/css2/FontShorthandManager.java.htm
//              if (valtyp == CSSValue.CSS_VALUE_LIST) {
//                PApplet.println(property + " " + type);
//                PApplet.println("?????????? " + property + " [" + declaration.getPropertyValue(property)+ "]");              
//                CSSValueList list = (CSSValueList)cssval;
//                for (int k = 0; k < list.getLength(); k++) {
//                  CSSValue kval = list.item(k);
//                  if (kval.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
//                    CSSPrimitiveValue pval = (CSSPrimitiveValue)kval;
//                    PApplet.println(k + " " + getPrimitiveString(pval.getPrimitiveType()));
//                  }  
//                }        
//              }
                
              }              
            }            
          
//            Log.message("Adding property to class: " + clazz);
//            Log.message("  -> " + props);
          }
        } else {
          // Skipping unsupported rule
//          System.out.println("CHARSET_RULE: " + CSSRule.CHARSET_RULE);
//          System.out.println("FONT_FACE_RULE: " + CSSRule.FONT_FACE_RULE);
//          System.out.println("IMPORT_RULE: " + CSSRule.IMPORT_RULE);
//          System.out.println("MEDIA_RULE: " + CSSRule.MEDIA_RULE);
//          System.out.println("PAGE_RULE: " + CSSRule.PAGE_RULE);
//          System.out.println("STYLE_RULE: " + CSSRule.STYLE_RULE);
//          System.out.println("UNKNOWN_RULE: " + CSSRule.UNKNOWN_RULE);
        }
      } // end of ruleList loop
    } catch (IOException e) {
      e.printStackTrace();
    }    
  }
  
  protected String getPrimitiveString(int t) {
    // http://www.w3.org/2003/01/dom2-javadoc/org/w3c/dom/css/CSSPrimitiveValue.html    
    if (t == CSSPrimitiveValue.CSS_ATTR) return "CSS_ATTR";
    else if (t == CSSPrimitiveValue.CSS_CM) return "CSS_CM";
    else if (t == CSSPrimitiveValue.CSS_COUNTER) return "CSS_COUNTER";
    else if (t == CSSPrimitiveValue.CSS_DEG) return "CSS_DEG";
    else if (t == CSSPrimitiveValue.CSS_DIMENSION) return "CSS_DIMENSION";    
    else if (t == CSSPrimitiveValue.CSS_EMS) return "CSS_EMS";
    else if (t == CSSPrimitiveValue.CSS_EXS) return "CSS_EXS";
    else if (t == CSSPrimitiveValue.CSS_GRAD) return "CSS_GRAD";
    else if (t == CSSPrimitiveValue.CSS_HZ) return "CSS_HZ";
    else if (t == CSSPrimitiveValue.CSS_IDENT) return "CSS_IDENT";
    else if (t == CSSPrimitiveValue.CSS_KHZ) return "CSS_KHZ";
    else if (t == CSSPrimitiveValue.CSS_MM) return "CSS_MM";
    else if (t == CSSPrimitiveValue.CSS_MS) return "CSS_MS";    
    else if (t == CSSPrimitiveValue.CSS_NUMBER) return "CSS_NUMBER";
    else if (t == CSSPrimitiveValue.CSS_PC) return "CSS_PC";
    else if (t == CSSPrimitiveValue.CSS_PERCENTAGE) return "CSS_PERCENTAGE";
    else if (t == CSSPrimitiveValue.CSS_PT) return "CSS_PT";
    else if (t == CSSPrimitiveValue.CSS_PX) return "CSS_PX";
    else if (t == CSSPrimitiveValue.CSS_RAD) return "CSS_RAD";
    else if (t == CSSPrimitiveValue.CSS_RECT) return "CSS_RECT";
    else if (t == CSSPrimitiveValue.CSS_RGBCOLOR) return "CSS_RGBCOLOR";    
    else if (t == CSSPrimitiveValue.CSS_S) return "CSS_S";
    else if (t == CSSPrimitiveValue.CSS_STRING) return "CSS_STRING";
    else if (t == CSSPrimitiveValue.CSS_UNKNOWN) return "CSS_UNKNOWN";
    else if (t == CSSPrimitiveValue.CSS_URI) return "CSS_URI";        
    return "CSS_UNKNOWN";
  }  
  
  @SuppressWarnings("serial")
  protected class Properties extends HashMap<String, String> {
    Properties(String clazz) {
      super();
      
      // Finding inherited properties
      String[] parts = clazz.split("\\.");
      if (1 < parts.length) {
        String last = parts[parts.length - 1];
        
        Properties props0 = properties.get(last);
        if (props0 != null) this.putAll(props0);
        
        String path = ""; 
        for (int k = 0; k < parts.length - 2; k++) {
          if (0 < k) path += ".";                   
          path += parts[k];
          props0 = properties.get(path + "." + last);
          if (props0 != null) this.putAll(props0);
        }
      }              
    }
  }  
}
