/* COPYRIGHT (C) 2014-17 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.*;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.awt.Desktop;
import java.net.URI;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import java.awt.image.BufferedImage;

import mui.Display;
import processing.core.PApplet;
import processing.core.PConstants;


/**
 * Mirador launcher class. 
 * 
 */

public class MiraLauncher extends JFrame {
  final static String MIRADOR_URL = "https://fathom.info/mirador";

  final static int TEXT_WIDTH = Display.scale(280);
  final static int TEXT_HEIGHT = Display.scale(80);
  final static int BUTTON_HEIGHT = Display.scale(30);
  final static private int BOX_BORDER = Display.scale(13);
  final static private int INSET = Display.scale(1);
  final static private int GAP = Display.scale(26);
  final static private int FONT_SIZE = Display.scale(12);
  final static private int TEXT_MARGIN = Display.scale(3);
  final static private int LOGO_SIZE = Display.scalepot(128);

  private JButton loadButton;
  private JButton quitButton;
  private JLabel status;

  // The Swing chooser is much better on Linux
  static final boolean useNativeSelect = PApplet.platform != PConstants.LINUX;
  
  static void selectPrompt(final String prompt,
                           final File defaultSelection,
                           final Frame parentFrame,
                           final int mode) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        File selectedFile = null;

        if (useNativeSelect) {
          FileDialog dialog = new FileDialog(parentFrame, prompt, mode);
          if (defaultSelection != null) {
            dialog.setDirectory(defaultSelection.getParent());
            dialog.setFile(defaultSelection.getName());
          }
          
          dialog.setFilenameFilter(new FilenameFilter() {
            public boolean accept(File dir, String name) {
              if (name.startsWith(".")) return false;
              String lower = name.toLowerCase();
              return lower.endsWith(".mira") || 
                     lower.endsWith(".tsv") || 
                     lower.endsWith(".csv") || 
                     lower.endsWith(".xml") ||
                     lower.endsWith(".bin") ||
                     lower.endsWith(".ods");
            }
          });

          dialog.setVisible(true);
          String directory = dialog.getDirectory();
          String filename = dialog.getFile();
          if (filename != null) {
            selectedFile = new File(directory, filename);
          }

        } else {
          JFileChooser chooser = new JFileChooser();
          chooser.setDialogTitle(prompt);
          if (defaultSelection != null) {
            chooser.setSelectedFile(defaultSelection);
          }

          int result = -1;
          if (mode == FileDialog.SAVE) {
            result = chooser.showSaveDialog(parentFrame);
          } else if (mode == FileDialog.LOAD) {
            result = chooser.showOpenDialog(parentFrame);
          }
          if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
          }
        }
        selectCallback(selectedFile);
      }
    });
  }
  
  static protected void selectCallback(final File selectedFile) {
    if (selectedFile == null) {
      System.exit(0);
    }
    new Thread(new Runnable() {
      public void run() {
        MiraApplet.inputFile = selectedFile.getAbsolutePath();
        PApplet.main(MiraApplet.class.getName());
      }
    }).start();
  }


  public MiraLauncher() {
    super("Welcome to Mirador!");
    setLookAndFeel();
    createLayout();
  }


  private void setLookAndFeel() {
    if (PApplet.platform == PApplet.WINDOWS) {
      try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception e) { }
    }

    // Set a 1x1 transparent icon, apparently the only way to remove the coffee cup icon.
    Image icon = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB_PRE);
    setIconImage(icon);
  }

  private void createLayout() {

    Container outer = getContentPane();
    outer.removeAll();
    outer.setBackground(new Color(247, 247, 247));

    URL iconUrl = getClass().getResource("/data/icons/icon-" + LOGO_SIZE + ".png");
    ImageIcon image = new ImageIcon(iconUrl);
    JLabel imageLabel = new JLabel(image);

    Box vbox = Box.createVerticalBox();
    vbox.setBorder(new EmptyBorder(BOX_BORDER, BOX_BORDER, BOX_BORDER, BOX_BORDER));
    outer.add(vbox);

    Box hbox = Box.createHorizontalBox();

    String labelText =
            "<html> " +
                    "<head> <style type=\"text/css\">" +
                    "p { font: " + FONT_SIZE + "pt \"Lucida Grande\"; " +
                    "margin: " + TEXT_MARGIN + "px; " +
                    "width: " + TEXT_WIDTH + "px }" +
                    "</style> </head>" +
            "<html>" +
            "<body><p><br>Mirador is a tool for visual exploration of complex datasets.<br> " +
            "It is the result of a collaboration between the Sabeti Lab at Harvard University, " +
            "the Broad Institute of MIT and Harvard, and Fathom Information Design.<br><br> " +
            "Check <a href=\"" + MIRADOR_URL + "\">Mirador's webpage</a> for more information.</p></body>";

    JEditorPane pane = new JEditorPane("text/html", labelText);
    pane.addHyperlinkListener(new HyperlinkListener() {
      @Override
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
          try {
            Desktop.getDesktop().browse(new URI(MIRADOR_URL));
          } catch (Exception ex) {
          }
        }
      }
    });
    pane.setEditable(false);
//    pane.setPreferredSize(new Dimension(TEXT_WIDTH, TEXT_HEIGHT));
    JLabel label = new JLabel();
    pane.setBackground(new Color(247, 247, 247));


    JLabel textarea = new JLabel(labelText);
    textarea.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        try {
          Desktop.getDesktop().browse(new URI(MIRADOR_URL));
        } catch (Exception ex) {
        }
      }
    });
    textarea.setPreferredSize(new Dimension(TEXT_WIDTH, TEXT_HEIGHT));

    hbox.add(imageLabel);
//    hbox.add(textarea);
    hbox.add(pane);

    vbox.add(hbox);
    vbox.add(Box.createVerticalStrut(GAP));

    JPanel controlPanel = new JPanel();
    controlPanel.setBackground(new Color(247, 247, 247));
    GridBagLayout gridBagLayout = new GridBagLayout();
    controlPanel.setLayout(gridBagLayout);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(INSET, INSET, INSET, INSET);

    status = new JLabel();
    Dimension dim = new Dimension(TEXT_WIDTH/2, BUTTON_HEIGHT);
    status.setPreferredSize(dim);
    status.setText("Starting up...");
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridheight = 2;
    gbc.fill = GridBagConstraints.VERTICAL;
    controlPanel.add(status, gbc);

    JLabel empty = new JLabel();
    empty.setText("");
    status.setPreferredSize(dim);
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.VERTICAL;
    controlPanel.add(empty, gbc);

    loadButton = new JButton("Load data");
    loadButton.setPreferredSize(dim);
    loadButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        selectPrompt("Select data for analysis:", new File(MiraApplet.prefs.projectFolder),MiraLauncher.this, FileDialog.LOAD);
        setVisible(false);
      }
    });
    loadButton.setEnabled(false);
    gbc.gridx = 1;
    gbc.gridy = 0;
//    gbc.weightx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    controlPanel.add(loadButton, gbc);

    quitButton = new JButton("Quit");
    quitButton.setPreferredSize(dim);
    quitButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
        System.exit(0);
      }
    });
    quitButton.setEnabled(false);
    gbc.gridx = 1;
    gbc.gridy = 1;
//    gbc.weightx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    controlPanel.add(quitButton, gbc);

    vbox.add(controlPanel);
    pack();

    setLocationRelativeTo(null);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setResizable(false);
    setVisible(true);

    final Thread update = new Thread() {
      @Override
      public void run() {
        MiraApplet.copyExamples();
        loadButton.setEnabled(true);
        quitButton.setEnabled(true);
        status.setText("");
      }
    };
    update.start();
  }

  static public void main(String[] args) {
    MiraApplet.loadPreferences();
    MiraLauncher launcher = new MiraLauncher();
  }
}
