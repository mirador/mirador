/* COPYRIGHT (C) 2014-17 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.*;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.awt.Desktop;
import java.net.URI;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.SwingConstants;
import java.awt.image.BufferedImage;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

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
  final static int FILE_TABLE_WIDTH = Display.scale(250);
  final static int MAX_BUTTON_WIDTH = Display.scale(100);
  final static private int BOX_BORDER = Display.scale(13);
  final static private int INSET = Display.scale(1);
  final static private int GAP = Display.scale(26);
  final static private int FONT_SIZE = Display.scale(12);
  final static private int TEXT_MARGIN = Display.scale(3);
  final static private int LOGO_SIZE = Display.scalepot(128);

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
    if (selectedFile == null || !selectedFile.exists()) {
      JOptionPane.showMessageDialog(new Frame(), "The file somehow disapeared, Mirador will exit now", "Error!",
              JOptionPane.ERROR_MESSAGE);
      System.exit(0);
    }
    loadFile(selectedFile);
  }

  static protected void loadFile(final File selectedFile) {
    new Thread(new Runnable() {
      public void run() {
        MiraApp.inputFile = selectedFile.getAbsolutePath();
        PApplet.main(MiraApp.class.getName());
      }
    }).start();
  }


  public MiraLauncher() {
    super("Please wait while Mirador initializes...");
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

    Box everything = Box.createVerticalBox();
    everything.setBorder(new EmptyBorder(BOX_BORDER, BOX_BORDER, BOX_BORDER, BOX_BORDER));
    outer.add(everything);

    Box welcome = Box.createHorizontalBox();

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

    welcome.add(imageLabel);
    welcome.add(pane);

    everything.add(welcome);
    everything.add(Box.createVerticalStrut(GAP));

    Box recentFiles = Box.createHorizontalBox();

    final String[] columnNames = {"Recently open datasets"};
    final Object[][] data = MiraApp.prefs.getProjectHistory();
    DefaultTableModel model = new DefaultTableModel(data, columnNames) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };

    final JTable table = new JTable(model) {
      DefaultTableCellRenderer renderer = new LeftEllipsisRenderer();
      @Override
      public TableCellRenderer getCellRenderer (int arg0, int arg1) {
        return renderer;
      }
    };

    table.setFillsViewportHeight(true);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    table.setRowHeight(Display.scale(table.getRowHeight()));
    Dimension dim = new Dimension(Display.scale(FILE_TABLE_WIDTH),
            table.getRowHeight() * 10);
    table.setPreferredScrollableViewportSize(dim);

    recentFiles.add(new JScrollPane(table));

//    Dimension bdim = new Dimension(TEXT_WIDTH/2, BUTTON_HEIGHT);

    Box buttons = Box.createVerticalBox();
    buttons.setMaximumSize(new Dimension(Display.scale(MAX_BUTTON_WIDTH), table.getRowHeight() * 10));

    JPanel loadSelPanel = new JPanel(new BorderLayout());
    final JButton loadSelButton = new JButton("Load selected dataset");
//    loadSelButton.setSize(bdim);
    loadSelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      int row = table.getSelectedRow();
      int col = table.getSelectedColumn();
      if (-1 < row && -1 < col) {
        String value = (String)table.getValueAt(row, col);
        File file = new File(value);
        if (file.exists()) {
          loadFile(file);
          setVisible(false);
        } else {
          JOptionPane.showMessageDialog(MiraLauncher.this, "Data file is missing", "Problem!",
            JOptionPane.WARNING_MESSAGE);
        }
      }
      }
    });
    loadSelButton.setEnabled(false);
    loadSelButton.setAlignmentX(Component.LEFT_ALIGNMENT);
    loadSelPanel.add(loadSelButton);
    buttons.add(loadSelPanel);

    JPanel loadNewPanel = new JPanel(new BorderLayout());
    final JButton loadNewButton = new JButton("Load new dataset");
//    loadNewButton.setSize(bdim);
    loadNewButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        selectPrompt("Select data file to load:", new File(MiraApp.prefs.projectFolder),
                     MiraLauncher.this, FileDialog.LOAD);
        setVisible(false);
      }
    });
    loadNewButton.setEnabled(false);
    loadNewButton.setAlignmentX(Component.LEFT_ALIGNMENT);
    loadNewPanel.add(loadNewButton);
    buttons.add(loadNewPanel);


    JPanel quitPanel = new JPanel(new BorderLayout());
    final JButton quitButton = new JButton("Quit");
//    quitButton.setSize(bdim);
    quitButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
        System.exit(0);
      }
    });
    quitButton.setEnabled(false);
    quitButton.setAlignmentX(Component.LEFT_ALIGNMENT);
    quitPanel.add(quitButton);
    buttons.add(Box.createVerticalStrut(GAP));
    buttons.add(quitPanel);

    recentFiles.add(Box.createHorizontalStrut(GAP));
    recentFiles.add(buttons);
    everything.add(recentFiles);

    /*
    JPanel controlPanel = new JPanel();
    controlPanel.setBackground(new Color(247, 247, 247));
    GridBagLayout gridBagLayout = new GridBagLayout();
    controlPanel.setLayout(gridBagLayout);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(INSET, INSET, INSET, INSET);


    Dimension dim = new Dimension(TEXT_WIDTH/2, BUTTON_HEIGHT);
    status.setPreferredSize(dim);

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



    loadSelButton = new JButton("Load data");
    loadSelButton.setPreferredSize(dim);
    loadSelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        selectPrompt("Select data for analysis:", new File(MiraApp.prefs.projectFolder),MiraLauncher.this, FileDialog.LOAD);
        setVisible(false);
      }
    });
    loadSelButton.setEnabled(false);
    gbc.gridx = 1;
    gbc.gridy = 0;
//    gbc.weightx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    controlPanel.add(loadSelButton, gbc);

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
    */

    pack();

    setLocationRelativeTo(null);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setResizable(false);
    setVisible(true);

    final Thread update = new Thread() {
      @Override
      public void run() {
        MiraApp.copyExamples();
        if (0 < data.length) loadSelButton.setEnabled(true);
        loadNewButton.setEnabled(true);
        quitButton.setEnabled(true);
        setTitle("Welcome to Mirador!");
      }
    };
    update.start();
  }

  static public void main(String[] args) {
    MiraApp.loadPreferences();
    MiraLauncher mirador = new MiraLauncher();
  }

  // Create a cell renderer that adds left ellipsis
  // From http://www.javapractices.com/topic/TopicAction.do?Id=168
  final class LeftEllipsisRenderer extends DefaultTableCellRenderer {
    AffineTransform affinetransform = new AffineTransform();
    FontRenderContext frc = new FontRenderContext(affinetransform, true, true);

    LeftEllipsisRenderer() {
      setHorizontalAlignment(SwingConstants.LEFT);
    }
    @Override
    public void setValue(Object aValue) {
      Object result = aValue;
      if ((aValue != null) && (aValue instanceof String)) {
        String stringValue = (String)aValue;
        if (!stringValue.equals("")) {
          int w = (int)getFont().getStringBounds(stringValue, frc).getWidth();
          if (FILE_TABLE_WIDTH < w) {
            String s = stringValue;
            while (FILE_TABLE_WIDTH < w) {
              stringValue = stringValue.substring(5);
              w = (int)getFont().getStringBounds(stringValue, frc).getWidth();
            }
            result = "..." + stringValue;
          }
        }
      }
      super.setValue(result);
    }
  }
}
