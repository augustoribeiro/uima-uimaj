/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.uima.tools.docanalyzer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ProgressMonitor;
import javax.swing.SpringLayout;
import javax.swing.Timer;
import javax.swing.UIManager;

import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.UIMARuntimeException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.TypeOrFeature;
import org.apache.uima.analysis_engine.metadata.FixedFlow;
import org.apache.uima.analysis_engine.metadata.SofaMapping;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.TCAS;
import org.apache.uima.collection.CasConsumerDescription;
import org.apache.uima.collection.CollectionProcessingManager;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.collection.EntityProcessStatus;
import org.apache.uima.collection.StatusCallbackListener;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.resource.metadata.Capability;
import org.apache.uima.resource.metadata.ConfigurationParameterSettings;
import org.apache.uima.tools.images.Images;
import org.apache.uima.tools.stylemap.StyleMapEditor;
import org.apache.uima.tools.stylemap.StyleMapEntry;
import org.apache.uima.tools.util.gui.Caption;
import org.apache.uima.tools.util.gui.SplashScreenDialog;
import org.apache.uima.tools.util.gui.SpringUtilities;
import org.apache.uima.tools.util.htmlview.AnnotationViewGenerator;
import org.apache.uima.util.AnalysisEnginePerformanceReports;
import org.apache.uima.util.CasCreationUtils;
import org.apache.uima.util.FileSystemCollectionReader;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLInputSource;
import org.apache.uima.util.XmiWriterCasConsumer;
import org.apache.uima.util.XmlDetagger;

/**
 * A simple GUI for the RunTextAnalysis application library. Note that currently this will only run
 * under Windows since it relies on Windows-specific commands for invoking a web browser to view the
 * annotated documents.
 * 
 * 
 * 
 */
public class DocumentAnalyzer extends JFrame implements StatusCallbackListener, ActionListener {
  private static final long serialVersionUID = 8795969283257780425L;

  private static final String HELP_MESSAGE = "Instructions for using UIMA Document Analyzer:\n\n"
          + "1) In the \"Input Directory\" field, either type or use the browse\n"
          + "button to select a directory containing the documents that you want\n"
          + "to analyze.\n\n"
          + "2) In the \"Output Directory\" field, either type or use the browse\n"
          + "button to select a directory where you would like the analyzed\n"
          + "documents to be placed.\n\n"
          + "3) In the \"Location of Analysis Engine XML Descriptor\" field, either type or use\n"
          + "the browse button to select the XML Descriptor file for the Analysis Engine you\n"
          + "want to use.\n\n"
          + "4) Optionally, if your input documents are XML files and you only\n"
          + "want to analyze the contents of a particular tag within those files,\n"
          + "you may enter in the \"XML Tag Containing Text\" field the name of\n"
          + "the XML tag that contains the text to be analyzed.\n\n "
          + "5) In the \"Language\" field, you may select or type the language \n"
          + "of your input documents.  Some Analysis Engines may require this.\n\n"
          + "6) Click the \"Run\" button at the buttom of the window.\n\n\n"
          + "When processing is complete, a list of the analyzed documents will\n"
          + "be displayed.  Select the view format (Java Viewer is recommended),\n"
          + "and double-click on a document to view it.\n\n";

  private FileSelector inputFileSelector;

  protected FileSelector outputFileSelector; // JMP

  protected FileSelector xmlFileSelector; // JMP

  protected String outputFileSelected = null; // JMP

  private JTextField runParametersField;

  private JComboBox languageComboBox;

  private JComboBox encodingComboBox;

  private ProgressMonitor progressMonitor;

  protected TypeSystem currentTypeSystem; // JMP

  protected String[] currentTaeOutputTypes; // JMP

  private File styleMapFile;

  protected boolean useGeneratedStyleMap = false; // JMP

  private FileSystemCollectionReader collectionReader;

  private CollectionProcessingManager mCPM;

  protected String interactiveTempFN = "__DAtemp__.txt"; // JMP

  private AnnotationViewGenerator annotationViewGenerator;

  private JDialog aboutDialog;

  private int numDocs;

  private int numDocsProcessed = 0;

  private AnalysisEngine ae;

  private File tempDir = new File(System.getProperty("java.io.tmpdir"));

  /** Directory in which this program will write its output files. */
  private File outputDirectory;

  private JButton runButton;

  private JButton interButton;

  protected boolean interactive = false; // JMP

  private final JRadioButton javaViewerRB = new JRadioButton("Java Viewer");

  private final JRadioButton javaViewerUCRB = new JRadioButton(
          "<html><font color=maroon>JV user colors</font></html>");

  private final JRadioButton htmlRB = new JRadioButton("HTML");

  protected final JRadioButton xmlRB = new JRadioButton("XML"); // JMP

  private JDialog analyzeInputDialog = null;

  protected boolean javaViewerRBisSelected = false; // JMP

  protected boolean javaViewerUCRBisSelected = false; // JMP

  protected PrefsMediator prefsMed; // JMP

  protected String statsString; // JMP

  protected File taeDescFile; // JMP

  protected String taeDescFileName; // JMP

  private File aeSpecifierFile;

  protected TCAS tcas; // JMP

  private Timer progressTimer;

  private boolean usingXmlDetagger;

  /**
   * Constructor. Sets up the GUI.
   */
  public DocumentAnalyzer() {
    this(null, false, false);
  }

  public DocumentAnalyzer(String outputFileSelected, boolean interactiveDA, boolean jvucrbis) {
    super("Document Analyzer");
    prefsMed = new PrefsMediator();

    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      // I don't think this should ever happen, but if it does just print
      // error and continue
      // with defalt look and feel
      System.err.println("Could not set look and feel: " + e.getMessage());
    }
    // UIManager.put("Panel.background",Color.WHITE);
    // Need to set other colors as well

    // Set frame icon image
    try {
      this.setIconImage(Images.getImage(Images.MICROSCOPE));
      // new
      // ImageIcon(getClass().getResource(FRAME_ICON_IMAGE)).getImage());
    } catch (IOException e) {
      System.err.println("Image could not be loaded: " + e.getMessage());
    }

    // create about dialog
    aboutDialog = new SplashScreenDialog(this, "About Document Analyzer");
    this.outputFileSelected = outputFileSelected;
    this.interactive = interactiveDA;
    this.javaViewerUCRBisSelected = jvucrbis;

    // Creating Menu Bar
    JMenuBar menuBar = new JMenuBar();
    setJMenuBar(menuBar);

    JMenu fileMenu = new JMenu("File");
    JMenu helpMenu = new JMenu("Help");

    // Menu Items
    JMenuItem aboutMenuItem = new JMenuItem("About");
    JMenuItem helpMenuItem = new JMenuItem("Help");
    JMenuItem exitMenuItem = new JMenuItem("Exit");

    // menuBar.add(Box.createHorizontalGlue());
    // ...create the rightmost menu...
    fileMenu.add(exitMenuItem);
    helpMenu.add(aboutMenuItem);
    helpMenu.add(helpMenuItem);
    menuBar.add(fileMenu);
    menuBar.add(helpMenu);

    // setResizable(false);

    // Labels to identify the text fields
    final Caption labelInputFile;
    final Caption labelOutputFile;
    final Caption labelXmlFile;
    final Caption labelRunParameters;
    final Caption labelLanguage;
    final Caption labelEncoding;

    // Strings for the labels
    final String inputString = "Input Directory: ";
    final String outputString = "Output Directory: ";
    final String xmlString = "Location of Analysis Engine XML Descriptor: ";
    final String runParametersString = "XML Tag containing Text (optional): ";

    // Create field label captions (right-aligned JLabel):

    labelInputFile = new Caption(inputString);

    labelOutputFile = new Caption(outputString);

    labelXmlFile = new Caption(xmlString);

    labelRunParameters = new Caption(runParametersString);

    labelLanguage = new Caption("Language: ");

    labelEncoding = new Caption("Character Encoding: ");

    JPanel controlPanel = new JPanel();
    controlPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    controlPanel.setLayout(new SpringLayout());

    // Once we add components to controlPanel, we'll
    // call SpringUtilities::makeCompactGrid on it.

    // controlPanel.setLayout(new GridLayout(4, 2, 8, 4));

    // Set default values for input fields
    outputDirectory = new File(prefsMed.getOutputDir());

    File browserRootDir = new File(System.getProperty("user.dir"));

    TfFocusListener tlf = new TfFocusListener(prefsMed);
    TfFileSelectorListener fsl = new TfFileSelectorListener(prefsMed);
    TfDocumentListener dl = new TfDocumentListener(prefsMed);
    inputFileSelector = new FileSelector(prefsMed.getInputDir(), "Input Directory",
            JFileChooser.DIRECTORIES_ONLY, browserRootDir);
    // inputFileSelector.addFocusListener(tlf);
    inputFileSelector.addFileSelectorListener(fsl);
    inputFileSelector.addDocumentListener(dl);

    outputFileSelector = new FileSelector(prefsMed.getOutputDir(), "Output Directory",
            JFileChooser.DIRECTORIES_ONLY, browserRootDir);
    // outputFileSelector.addFocusListener( tlf);
    outputFileSelector.addFileSelectorListener(fsl);
    outputFileSelector.addDocumentListener(dl);

    xmlFileSelector = new FileSelector(prefsMed.getTAEfile(),
            "Analysis Engine Descriptor XML file", JFileChooser.FILES_ONLY, browserRootDir);
    // xmlFileSelector.addFocusListener( tlf);
    xmlFileSelector.addFileSelectorListener(fsl);
    xmlFileSelector.addDocumentListener(dl);

    prefsMed.setFileSelectors(inputFileSelector, outputFileSelector, xmlFileSelector);
    runParametersField = new JTextField(16);
    runParametersField.setText(prefsMed.getXmlTag());

    JPanel runParametersPanel = new JPanel();
    runParametersPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
    runParametersPanel.add(runParametersField);

    languageComboBox = new JComboBox(new Object[] { "en", "de", "es", "fr", "it", "pt", "ja",
        "ko-kr", "pt-br", "zh-cn", "zh-tw", "x-unspecified" });
    languageComboBox.setEditable(true);
    languageComboBox.setSelectedItem(prefsMed.getLanguage());
    JPanel languagePanel = new JPanel();
    languagePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
    languagePanel.add(languageComboBox);

    Map charsetMap = Charset.availableCharsets();
    Set charsets = charsetMap.keySet();
    Object[] charsetArr = charsets.toArray();
    encodingComboBox = new JComboBox(charsetArr);
    encodingComboBox.setEditable(true);
    encodingComboBox.setSelectedItem(prefsMed.getEncoding());
    JPanel encodingPanel = new JPanel();
    encodingPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
    encodingPanel.add(encodingComboBox);

    controlPanel.add(labelInputFile);
    controlPanel.add(inputFileSelector);
    controlPanel.add(labelOutputFile);
    controlPanel.add(outputFileSelector);
    controlPanel.add(labelXmlFile);
    controlPanel.add(xmlFileSelector);
    controlPanel.add(labelRunParameters);
    controlPanel.add(runParametersPanel);
    controlPanel.add(labelLanguage);
    controlPanel.add(languagePanel);
    controlPanel.add(labelEncoding);
    controlPanel.add(encodingPanel);

    SpringUtilities.makeCompactGrid(controlPanel, 6, 2, // rows, cols
            4, 4, // initX, initY
            4, 4); // xPad, yPad

    // Event Handlling of "Exit" Menu Item
    exitMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        savePreferences();
        System.exit(0);
      }
    });

    // Event Handlling of "About" Menu Item
    aboutMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        aboutDialog.setVisible(true);
      }
    });

    // Event Handlling of "Help" Menu Item
    helpMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JOptionPane.showMessageDialog(DocumentAnalyzer.this, HELP_MESSAGE,
                "Document Analyzer Help", JOptionPane.PLAIN_MESSAGE);
      }
    });

    // Add the panels to the frame
    Container contentPanel = getContentPane();
    contentPanel.setBackground(Color.WHITE);
    JLabel banner = new JLabel(Images.getImageIcon(Images.BANNER));
    contentPanel.add(banner, BorderLayout.NORTH);
    contentPanel.add(controlPanel, BorderLayout.CENTER);

    // contentPanel.add(parameterPane, BorderLayout.WEST);
    // Add the run Button to run AE
    runButton = new JButton("Run");
    runButton.setToolTipText("Runs Analysis Engine and displays results");
    // Add the interactive Button to run AE on entered text
    interButton = new JButton("Interactive");
    interButton.setToolTipText("Type in text to analyze");
    // add view button
    JButton viewButton = new JButton("View");
    // viewButton.setToolTipText( "View results of already processed data");
    viewButton.setDefaultCapable(true);
    viewButton.setRequestFocusEnabled(true);
    viewButton.requestFocus();
    viewButton.addFocusListener(tlf);

    // copy into the mediator
    prefsMed.setDocButtons(runButton, interButton, viewButton);
    // Add the run button to another panel
    JPanel lowerButtonsPanel = new JPanel();
    lowerButtonsPanel.add(runButton, BorderLayout.WEST);
    lowerButtonsPanel.add(interButton, BorderLayout.EAST);
    lowerButtonsPanel.add(viewButton, BorderLayout.EAST);
    lowerButtonsPanel.setFocusCycleRoot(true);

    contentPanel.add(lowerButtonsPanel, BorderLayout.SOUTH);
    setContentPane(contentPanel);

    // Event Handling of run Button
    runButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ee) {
        interactive = false;
        savePreferences();
        analyzeDocuments(null); // JMP added arg
      }
    });

    // Event Handling of interactive Button
    interButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ee) {
        if (outputFileSelector.getSelected().length() == 0)
          displayError("Need to specify an output directory for temporary results.");
        else {
          interactive = true;
          savePreferences();
          analyzeInputarea();
        }
      }
    });

    // event to display already processed data
    viewButton.addActionListener(this);
    // new ActionListener() {
    // }
    // );

    // load user preferences
    restorePreferences();

    annotationViewGenerator = new AnnotationViewGenerator(tempDir);
    progressTimer = new Timer(100, new ActionListener() {
      public void actionPerformed(ActionEvent ee) {
        checkProgressMonitor();
      }
    });
  }

  /**
   * JMP addition Opens a dialog for the user to enter text, which will be saved to a file and then
   * processed as by analyzeDocumenbts below.
   */
  public void analyzeInputarea() {
    analyzeInputDialog = new JDialog(DocumentAnalyzer.this, "Annotation Input");

    File styleMapFile = prefsMed.getStylemapFile();

    if (!styleMapFile.exists()) {
      useGeneratedStyleMap = true;
    }

    TextAreaViewer viewer = new TextAreaViewer(analyzeInputDialog, useGeneratedStyleMap);
    analyzeInputDialog.getContentPane().add(viewer);
    analyzeInputDialog.setSize(850, 630);
    analyzeInputDialog.pack();
    analyzeInputDialog.show();

  }

  // moves this one to top
  public void actionPerformed(ActionEvent e) {
    savePreferences();
    try {
      aeSpecifierFile = new File(xmlFileSelector.getSelected());
      XMLInputSource in = new XMLInputSource(aeSpecifierFile);
      // ResourceSpecifier aeSpecifier = UIMAFramework.getXMLParser()
      // .parseResourceSpecifier(in);
      AnalysisEngineDescription aed = UIMAFramework.getXMLParser().parseAnalysisEngineDescription(
              in);

      // this generates style map file if one does not currently exist
      if (!prefsMed.getStylemapFile().exists()) {
        annotationViewGenerator.autoGenerateStyleMapFile(aed.getAnalysisEngineMetaData(), prefsMed
                .getStylemapFile());
      }
    } catch (InvalidXMLException e1) {
      e1.printStackTrace();
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    interactive = false; // prevent re-viewing temp file
    showAnalysisResults(outputDirectory);

  }

  /**
   * Invokes the <code>RunTextAnalysis</code> application library that actually analyzes the
   * documents and generates the output. Displays a progress bar while processing is occuring. When
   * processing is complete, allows the user to view the results. JMP added arg for input text to
   * analyze.
   */
  public void analyzeDocuments(String analysisText) {
    // get field values from GUI
    outputFileSelected = outputFileSelector.getSelected();

    File inputDir = new File(inputFileSelector.getSelected());
    if (outputFileSelector.getSelected().length() > 0)
      outputDirectory = new File(outputFileSelector.getSelected());
    else
      outputDirectory = null;

    // reset file pointers in case of typed-in text. JMP
    String tempFileDir = null;
    if ((analysisText != null) && (outputDirectory != null)) {
      tempFileDir = outputFileSelector.getSelected() + "/interactive_temp";
      inputDir = new File(tempFileDir);
      if (!inputDir.exists())
        inputDir.mkdirs();
      outputFileSelected = outputFileSelector.getSelected() + "/interactive_out";
      prefsMed.setOutputDirForInteractiveMode(outputFileSelected, outputFileSelector.getSelected());
      outputDirectory = new File(outputFileSelected);
    } else {
      analysisText = null; // 
    } // should just return

    aeSpecifierFile = new File(xmlFileSelector.getSelected());
    String xmlTag = runParametersField.getText();
    if ("".equals(xmlTag)) {
      xmlTag = null;
    }

    String language = (String) languageComboBox.getSelectedItem();
    String encoding = (String) encodingComboBox.getSelectedItem();

    // validate parameters
    if (aeSpecifierFile.getName().equals("")) {
      displayError("An Analysis Engine Descriptor is Required");
    } else if (!aeSpecifierFile.exists()) {
      displayError("Analysis Engine Descriptor \"" + xmlFileSelector.getSelected()
              + "\" does not exist.");
    } else if (aeSpecifierFile.isDirectory()) {
      displayError("The Analysis Engine Descriptor (" + xmlFileSelector.getSelected()
              + ") must be a file, not a directory.");
    } else if (inputDir.getName().equals("")) {
      displayError("An Input Directory is Required");
    } else if (!inputDir.exists()) {
      displayError("The input directory \"" + inputFileSelector.getSelected()
              + "\" does not exist.");
    } else if (!inputDir.isDirectory()) {
      displayError("The input directory (" + inputFileSelector.getSelected()
              + ") must be a directory, not a file.");
    } else if (outputDirectory != null && (!outputDirectory.exists() && !outputDirectory.mkdirs())
            || !outputDirectory.isDirectory()) {
      displayError("The output directory \"" + outputFileSelector.getSelected()
              + "\" does not exist and could not be created.");
    } else if (inputDir.equals(outputDirectory)) {
      displayError("The input directory and the output directory must be different.");
    } else
    // parameters are OK
    {
      // Set up files for case of typed-in text. JMP
      if (analysisText != null) {
        // delete contents of the outputDirectory, to clear out old
        // results
        File[] filesInOutDir = inputDir.listFiles();
        for (int i = 0; i < filesInOutDir.length; i++) {
          if (!filesInOutDir[i].isDirectory()) {
            filesInOutDir[i].delete();
          }
        }
        File tempFile = new File(tempFileDir + "/" + interactiveTempFN);
        PrintWriter writer;
        try {
          writer = new PrintWriter(new BufferedWriter(new FileWriter(tempFile)));
          writer.println(analysisText);
          writer.close();
        } catch (IOException e) {
          e.printStackTrace();
        }

      }

      // Delete contents of the outputDirectory, to clear out old results.
      // However -- if output dir contains files with names different than
      // those in input dir, prompt.
      File[] filesInOutDir = outputDirectory.listFiles();
      for (int i = 0; i < filesInOutDir.length; i++) {
        if (!filesInOutDir[i].isDirectory()) {
          String filename = filesInOutDir[i].getName();
          if (filename.endsWith(".xmi")) {
            filename = filename.substring(0, filename.length() - 4);
          }
          if (!new File(inputDir, filename).exists()) {
            int choice = JOptionPane.showConfirmDialog(DocumentAnalyzer.this, "All files in "
                    + outputDirectory.getPath() + " will be deleted.  These files don't "
                    + "appear to match the files in the input directory.  Is this OK?", "Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.NO_OPTION) {
              return;
            } else {
              break;
            }
          }
        }
      }
      // go ahead and delete files in output dir
      for (int i = 0; i < filesInOutDir.length; i++) {
        if (!filesInOutDir[i].isDirectory()) {
          filesInOutDir[i].delete();
        }
      }
      // read radio buttons. JMP
      javaViewerRBisSelected = javaViewerRB.isSelected();
      javaViewerUCRBisSelected = javaViewerUCRB.isSelected();

      // start separate thread to do component initialization and
      // processing
      ProcessingThread thread = new ProcessingThread(inputDir, outputDirectory, aeSpecifierFile,
              xmlTag, language, encoding);
      thread.start();
    }
  }

  private void checkProgressMonitor() {
    // if user has clicked cancel, abort
    if (progressMonitor.isCanceled()) {
      progressMonitor.setNote("Cancelling...");
      mCPM.stop();
      aborted();
    }
  }

  /**
   * @see org.apache.uima.collection.StatusCallbackListener#entityProcessComplete(org.apache.uima.cas.CAS,
   *      org.apache.uima.collection.EntityProcessStatus)
   */
  public void entityProcessComplete(CAS aCas, EntityProcessStatus aStatus) {
    // if an error occurred, display error
    if (aStatus.isException()) {
      displayError((Throwable) aStatus.getExceptions().get(0));
      // CPM will stop itself on error, we don't need to call
      // mCPM.stop(). In fact it causes a hang to do so, since
      // this code is callback code is executing within a CPM thread.
    }
    // increment the number of documents processed and update the
    // ProgressMonitor
    numDocsProcessed++;
    progressMonitor.setProgress(numDocsProcessed + 2);
    progressMonitor.setNote("Processed " + numDocsProcessed + " of " + numDocs + " documents.");
  }

  /**
   * @see org.apache.uima.collection.base_cpm.BaseStatusCallbackListener#aborted()
   */
  public void aborted() {
    // close progress monitor
    if (progressMonitor != null) {
      progressMonitor.close();
    }
    progressTimer.stop();
    // Re-enable frame:
    setEnabled(true);
    // reset cursor
    setCursor(Cursor.getDefaultCursor());
  }

  /**
   * @see org.apache.uima.collection.base_cpm.BaseStatusCallbackListener#batchProcessComplete()
   */
  public void batchProcessComplete() {
  }

  /**
   * @see org.apache.uima.collection.base_cpm.BaseStatusCallbackListener#collectionProcessComplete()
   */
  public void collectionProcessComplete() {
    // invoke ProcessingCompleteRunnable in Swing event handler thread
    // SwingUtilities.invokeLater(new ProcessingCompleteRunnable());
    // hide progress bar dialog if it is visible
    if (!progressMonitor.isCanceled()) {
      progressMonitor.close();
      progressTimer.stop();
    }

    // Re-enable frame:
    setEnabled(true);

    // reset cursor
    setCursor(Cursor.getDefaultCursor());

    // if everything works, output performance stats and print them to a
    // pane. Allow users to open generated files.
    showAnalysisResults(new AnalysisEnginePerformanceReports(mCPM.getPerformanceReport()),
            outputDirectory);
  }

  /**
   * @see org.apache.uima.collection.base_cpm.BaseStatusCallbackListener#initializationComplete()
   */
  public void initializationComplete() {
  }

  /**
   * @see org.apache.uima.collection.base_cpm.BaseStatusCallbackListener#paused()
   */
  public void paused() {
  }

  /**
   * @see org.apache.uima.collection.base_cpm.BaseStatusCallbackListener#resumed()
   */
  public void resumed() {
  }

  /**
   * Pops up a dialog that displays the performance stats. This dialog will have a "Show Annotated
   * Documents" button that takes the user to a list of the Annotated Docuemnts produced by this
   * analysis.
   * 
   * @param aReports
   *          performance stats for the analysis
   * @param aOutputDir
   *          directory containing annotated files
   */
  public void showAnalysisResults(AnalysisEnginePerformanceReports aReports, File aOutputDir) {
    statsString = ("PERFORMANCE STATS\n-------------\n\n" + aReports);
    try {
      tcas = createTCasFromDescriptor(prefsMed.getTAEfile());
    } catch (Exception e) {
      displayError(e);
    }
    currentTypeSystem = tcas.getTypeSystem();
    show_analysis(aOutputDir);
  }

  // this call is used when you click the "View" button
  public void showAnalysisResults(File aOutputDir) {

    try {
      tcas = createTCasFromDescriptor(prefsMed.getTAEfile());
      currentTypeSystem = tcas.getTypeSystem();
    } catch (Exception e) {
      displayError(e);
    }

    statsString = null;
    // what is this code doing??? - APL
    StyleMapEditor sedit = new StyleMapEditor(this, tcas);
    String sXml = readStylemapFile(prefsMed.getStylemapFile());
    ArrayList sme = sedit.parseStyleList(sXml);
    currentTaeOutputTypes = new String[sme.size()];
    for (int i = 0; i < sme.size(); i++) {
      StyleMapEntry e = (StyleMapEntry) sme.get(i);
      currentTaeOutputTypes[i] = e.getAnnotationTypeName();
    }
    show_analysis(aOutputDir);
  }

  /**
   * Creates a TCAS from an descriptor. Supports both local AE descriptors and remote service
   * specifiers. In the latter case the service is contacted to obtain its type system.
   * 
   * @throws ResourceInitializationException
   * @throws InvalidXMLException
   * @throws IOException
   */
  protected TCAS createTCasFromDescriptor(String aDescriptorFile) // JMP
          throws ResourceInitializationException, InvalidXMLException, IOException {
    ResourceSpecifier spec = UIMAFramework.getXMLParser().parseResourceSpecifier(
            new XMLInputSource(aDescriptorFile));
    if (spec instanceof AnalysisEngineDescription) {
      return CasCreationUtils.createTCas((AnalysisEngineDescription) spec);
    } else {
      AnalysisEngine ae = UIMAFramework.produceAnalysisEngine(spec);
      return CasCreationUtils.createTCas(ae.getAnalysisEngineMetaData());
    }
  }

  protected String readStylemapFile(File smapFile) // JMP
  {
    String styleMapXml = "";

    if (smapFile.exists()) {
      try {
        FileReader reader = new FileReader(smapFile);
        StringBuffer buf = new StringBuffer();
        char[] chars = new char[2048];
        int charsRead = reader.read(chars);

        while (charsRead > 0) {
          buf.append(chars, 0, charsRead);
          charsRead = reader.read(chars);
        }

        styleMapXml = buf.toString();
        reader.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return styleMapXml;
  }

  private void show_analysis(File outputDir) {
    File styleMapFile = getStyleMapFile();
    // added the following to prevent NPE when "View" button is used
    if (styleMapFile == null) {
      styleMapFile = prefsMed.getStylemapFile();
    }

    // create Annotation Viewer Main Panel. Depends on interactive setting.
    // JMP
    if (interactive) {
      // this version of the XCasAnnotationViewerDialog automatically
      // calls setVisible(true) to make the dialog visible
      new XCasAnnotationViewerDialog(this, "Analysis Results", prefsMed, styleMapFile, statsString,
              currentTypeSystem, currentTaeOutputTypes, interactiveTempFN + ".xmi",
              javaViewerRBisSelected, javaViewerUCRBisSelected, xmlRB.isSelected(), tcas);
    } else {
      // this version of the XCasAnnotationViewerDialog constructor does
      // not automatically launch the viewer.
      XCasAnnotationViewerDialog viewerDialog = new XCasAnnotationViewerDialog(this,
              "Analysis Results", prefsMed, styleMapFile, statsString, currentTypeSystem,
              currentTaeOutputTypes, useGeneratedStyleMap, tcas);
      if (usingXmlDetagger) {
        viewerDialog.setDefaultCasViewName("plainTextDocument");
      }
      viewerDialog.pack();
      viewerDialog.setModal(true);
      viewerDialog.setVisible(true);
    }

  }

  /**
   * Save user's preferences using Java's Preference API.
   */
  public void savePreferences() {
    // all now set and managed in the Mediator class
    prefsMed.setInputDir(inputFileSelector.getSelected());
    prefsMed.setOutputDir(outputFileSelector.getSelected());
    prefsMed.setTAEfile(xmlFileSelector.getSelected());
    prefsMed.setXmlTag(runParametersField.getText());
    prefsMed.setLanguage((String) languageComboBox.getSelectedItem());
    prefsMed.setEncoding((String) encodingComboBox.getSelectedItem());
  }

  /**
   * Reset GUI to preferences last saved via {@link #savePreferences}.
   */
  public void restorePreferences() {
    prefsMed.restorePreferences();
  }

  /**
   * Displays an error message to the user.
   * 
   * @param aErrorString
   *          error message to display
   */
  public void displayError(String aErrorString) {
    // word-wrap long mesages
    StringBuffer buf = new StringBuffer(aErrorString.length());
    final int CHARS_PER_LINE = 80;
    int charCount = 0;
    StringTokenizer tokenizer = new StringTokenizer(aErrorString, " \n", true);

    while (tokenizer.hasMoreTokens()) {
      String tok = tokenizer.nextToken();

      if (tok.equals("\n")) {
        buf.append("\n");
        charCount = 0;
      } else if ((charCount > 0) && ((charCount + tok.length()) > CHARS_PER_LINE)) {
        buf.append("\n").append(tok);
        charCount = tok.length();
      } else {
        buf.append(tok);
        charCount += tok.length();
      }
    }

    JOptionPane.showMessageDialog(DocumentAnalyzer.this, buf.toString(), "Error",
            JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Displays an error message to the user.
   * 
   * @param aThrowable
   *          Throwable whose message is to be displayed.
   */
  public void displayError(Throwable aThrowable) {
    aThrowable.printStackTrace();

    String message = aThrowable.toString();

    // For UIMAExceptions or UIMARuntimeExceptions, add cause info.
    // We have to go through this nonsense to support Java 1.3.
    // In 1.4 all exceptions can have a cause, so this wouldn't involve
    // all of this typecasting.
    while ((aThrowable instanceof UIMAException) || (aThrowable instanceof UIMARuntimeException)) {
      if (aThrowable instanceof UIMAException) {
        aThrowable = ((UIMAException) aThrowable).getCause();
      } else if (aThrowable instanceof UIMARuntimeException) {
        aThrowable = ((UIMARuntimeException) aThrowable).getCause();
      }

      if (aThrowable != null) {
        message += ("\nCausedBy: " + aThrowable.toString());
      }
    }

    displayError(message);
  }

  /**
   * Runs the application.
   */
  public static void main(String[] args) {
    final DocumentAnalyzer frame = new DocumentAnalyzer();

    // frame.setIconImage(frame.getToolkit().getImage("main.gif"));
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
  }

  /**
   * Class for dialog in which user types in text to be analyzed, and sets browser parameters
   */
  class TextAreaViewer extends JPanel {
    private static final long serialVersionUID = -7503162930412929062L;

    private JTextPane textPane = new JTextPane();

    /**
     * Constructor for dialog
     */
    public TextAreaViewer(final JDialog aiDialog, boolean generatedStyleMap) {

      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

      mainPanel.setPreferredSize(new Dimension(620, 600));
      mainPanel.setMinimumSize(new Dimension(200, 200));
      this.setLayout(new BorderLayout());
      this.add(mainPanel);

      JPanel analyzeTitlePanel = new JPanel();
      // analyzeTitlePanel.setLayout(new BoxLayout(analyzeTitlePanel,
      // BoxLayout.Y_AXIS));
      analyzeTitlePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
      analyzeTitlePanel.add(new JLabel("Type or cut-and-paste in your text to be annotated. "));
      analyzeTitlePanel.add(new JLabel("Then click on Analyze."));
      mainPanel.add(analyzeTitlePanel);

      // add JTextPane to top of vertical split pane
      // JTextPane textPane = new JTextPane();
      textPane.setEditable(true);
      textPane.setSelectionColor(new Color(100, 100, 200, 75));
      textPane.setPreferredSize(new Dimension(620, 400));
      textPane.setMinimumSize(new Dimension(200, 100));
      JScrollPane textScrollPane = new JScrollPane(textPane);
      // vertSplitPane.setTopComponent(textScrollPane);
      mainPanel.add(textScrollPane);

      JPanel southernPanel = new JPanel();
      southernPanel.setLayout(new BoxLayout(southernPanel, BoxLayout.Y_AXIS));

      JPanel controlsPanel = new JPanel();
      controlsPanel.setLayout(new SpringLayout());

      Caption displayFormatLabel = new Caption("Results Display Format:");
      controlsPanel.add(displayFormatLabel);

      JPanel displayFormatPanel = new JPanel();
      displayFormatPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
      displayFormatPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

      if (generatedStyleMap)
        javaViewerRB.setSelected(true);
      else
        javaViewerUCRB.setSelected(true);

      ButtonGroup displayFormatButtonGroup = new ButtonGroup();
      displayFormatButtonGroup.add(javaViewerRB);
      displayFormatButtonGroup.add(javaViewerUCRB);
      displayFormatButtonGroup.add(htmlRB);
      displayFormatButtonGroup.add(xmlRB);

      displayFormatPanel.add(javaViewerRB);
      displayFormatPanel.add(javaViewerUCRB);
      displayFormatPanel.add(htmlRB);
      displayFormatPanel.add(xmlRB);

      if (generatedStyleMap) {
        javaViewerRB.setSelected(true);
        javaViewerUCRB.setEnabled(false);
      } else {
        javaViewerUCRB.setSelected(true);
        javaViewerUCRB.setEnabled(true);
      }

      controlsPanel.add(displayFormatPanel);
      controlsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

      southernPanel.add(controlsPanel);

      JButton analyzeButton = new JButton("Analyze");
      analyzeButton.setToolTipText("Runs Analysis Engine and displays results");
      JPanel buttonsPanel = new JPanel();
      buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
      buttonsPanel.add(analyzeButton);

      JButton closeButton = new JButton("Close");
      closeButton.setToolTipText("Close application");
      buttonsPanel.add(closeButton);

      southernPanel.add(buttonsPanel);

      // add panel container to Dialog

      // vertSplitPane.setBottomComponent(buttonPanel);
      mainPanel.add(southernPanel);
      // Event Handling of analyze Button
      analyzeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ee) {
          analyzeTextArea();
        }
      });

      // event for the closeButton button
      closeButton.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent evt) {
          setVisible(false);
          aiDialog.dispose();
        }
      });

      // add mouse listener to update annotation tree
      // textPane.addMouseListener(this);

    }

    public void analyzeTextArea() {
      String text = textPane.getText();
      analyzeDocuments(text);
    }

  }

  public void runProcessingThread(File inputDir, File outputDir, File aeSpecifierFile,
          String xmlTag, String language, String encoding) {
    try {
      // create and configure collection reader that will read input docs
      CollectionReaderDescription collectionReaderDesc = FileSystemCollectionReader
              .getDescription();
      ConfigurationParameterSettings paramSettings = collectionReaderDesc.getMetaData()
              .getConfigurationParameterSettings();
      paramSettings.setParameterValue(FileSystemCollectionReader.PARAM_INPUTDIR, inputDir
              .getAbsolutePath());
      paramSettings.setParameterValue(FileSystemCollectionReader.PARAM_LANGUAGE, language);
      paramSettings.setParameterValue(FileSystemCollectionReader.PARAM_ENCODING, encoding);
      collectionReader = (FileSystemCollectionReader) UIMAFramework
              .produceCollectionReader(collectionReaderDesc);

      // show progress Monitor
      String progressMsg = "  Processing " + collectionReader.getNumberOfDocuments()
              + " Documents.";

      numDocs = collectionReader.getNumberOfDocuments();
      progressMonitor = new ProgressMonitor(DocumentAnalyzer.this, progressMsg, "", 0, numDocs + 2);
      String initial = "Initializing.... Please wait ";
      progressMonitor.setNote(initial);
      progressMonitor.setMillisToPopup(-1);
      progressMonitor.setMillisToDecideToPopup(-1);
      numDocsProcessed = 0;
      progressTimer.start();

      // set wait cursor
      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      // Disable frame while processing:
      setEnabled(false);

      // create CPM instance that will drive processing
      mCPM = UIMAFramework.newCollectionProcessingManager();

      // read AE descriptor from file
      XMLInputSource in = new XMLInputSource(aeSpecifierFile);
      ResourceSpecifier aeSpecifier = UIMAFramework.getXMLParser().parseResourceSpecifier(in);

      // create and configure CAS consumer that will write the output (in
      // XMI format)
      CasConsumerDescription casConsumerDesc = XmiWriterCasConsumer.getDescription();
      ConfigurationParameterSettings consumerParamSettings = casConsumerDesc.getMetaData()
              .getConfigurationParameterSettings();
      consumerParamSettings.setParameterValue(XmiWriterCasConsumer.PARAM_OUTPUTDIR, outputDir
              .getAbsolutePath());
      // declare uima.cas.TOP as an input so that ResultSpec on user's AE will be set to produce all
      // types
      casConsumerDesc.getCasConsumerMetaData().getCapabilities()[0].addInputType("uima.cas.TOP",
              true);

      // if XML tag was specified, also create XmlDetagger annotator that handles this
      AnalysisEngineDescription xmlDetaggerDesc = null;
      if (xmlTag != null && xmlTag.length() > 0) {
        xmlDetaggerDesc = XmlDetagger.getDescription();
        ConfigurationParameterSettings xmlDetaggerParamSettings = xmlDetaggerDesc.getMetaData()
                .getConfigurationParameterSettings();
        xmlDetaggerParamSettings.setParameterValue(XmlDetagger.PARAM_TEXT_TAG, xmlTag);
        usingXmlDetagger = true;
      }
      else {
        usingXmlDetagger = false;
      }
      
      // create an aggregate AE that includes the XmlDetagger (if needed), followed by
      //th user's AE descriptor, followed by the XMI Writer CAS Consumer, using fixed flow.
      // We use an aggregate AE here, rather than just adding the CAS Consumer to the CPE, so 
      //that we can support the user's AE being a CAS Multiplier and we can specify sofa mappings.
      AnalysisEngineDescription aggDesc = UIMAFramework.getResourceSpecifierFactory()
              .createAnalysisEngineDescription();
      aggDesc.setPrimitive(false);
      aggDesc.getDelegateAnalysisEngineSpecifiersWithImports().put("UserAE", aeSpecifier);
      aggDesc.getDelegateAnalysisEngineSpecifiersWithImports().put("XmiWriter", casConsumerDesc);
      FixedFlow flow = UIMAFramework.getResourceSpecifierFactory().createFixedFlow();      
            
      if (xmlDetaggerDesc != null) {
        aggDesc.getDelegateAnalysisEngineSpecifiersWithImports().put("XmlDetagger", xmlDetaggerDesc);
        flow.setFixedFlow(new String[] {"XmlDetagger", "UserAE", "XmiWriter"});
        
        //to run XmlDetagger we need sofa mappings
        //XmlDetagger's "xmlDocument" input sofa gets mapped to the default sofa
        SofaMapping sofaMapping1 = UIMAFramework.getResourceSpecifierFactory().createSofaMapping();
        sofaMapping1.setComponentKey("XmlDetagger");
        sofaMapping1.setComponentSofaName("xmlDocument");
        sofaMapping1.setAggregateSofaName(CAS.NAME_DEFAULT_SOFA);
        
        //for UserAE and XmiWriter, may default sofa to the "plainTextDocument" produced by the XmlDetagger
        SofaMapping sofaMapping2 = UIMAFramework.getResourceSpecifierFactory().createSofaMapping();
        sofaMapping2.setComponentKey("UserAE");
        sofaMapping2.setAggregateSofaName("plainTextDocument");
        SofaMapping sofaMapping3 = UIMAFramework.getResourceSpecifierFactory().createSofaMapping();
        sofaMapping3.setComponentKey("XmiWriter");
        sofaMapping3.setAggregateSofaName("plainTextDocument");
                
        aggDesc.setSofaMappings(new SofaMapping[] {sofaMapping1, sofaMapping2, sofaMapping3});
      }
      else {
        //no XML detagger needed in the aggregate in flow
        flow.setFixedFlow(new String[] { "UserAE", "XmiWriter" });          
      }

      
      aggDesc.getAnalysisEngineMetaData().setName("DocumentAnalyzerAE");
      aggDesc.getAnalysisEngineMetaData().setFlowConstraints(flow);
      aggDesc.getAnalysisEngineMetaData().getOperationalProperties().setMultipleDeploymentAllowed(
              false);

      
      
      progressMonitor.setProgress(1);

      // instantiate AE
      ae = UIMAFramework.produceAnalysisEngine(aggDesc);
      mCPM.setAnalysisEngine(ae);

      progressMonitor.setProgress(2);

      // this generates style map file if one does not currently exist
      if (!prefsMed.getStylemapFile().exists()) {
        annotationViewGenerator.autoGenerateStyleMapFile(ae, prefsMed.getStylemapFile());
      }

      // register callback listener
      mCPM.addStatusCallbackListener(DocumentAnalyzer.this);

      // save type system for later use in deserializing XCASes
      List descriptorList = new ArrayList();
      descriptorList.add(collectionReaderDesc);
      descriptorList.add(ae.getMetaData());
      descriptorList.add(casConsumerDesc);
      currentTypeSystem = CasCreationUtils.createCas(descriptorList).getTypeSystem();

      // save AE output types for later use in configuring viewer
      if (aeSpecifier instanceof AnalysisEngineDescription) {
        ArrayList outputTypeList = new ArrayList();
        Capability[] capabilities = ((AnalysisEngineDescription) aeSpecifier)
                .getAnalysisEngineMetaData().getCapabilities();
        for (int i = 0; i < capabilities.length; i++) {
          TypeOrFeature[] outputs = capabilities[i].getOutputs();
          for (int j = 0; j < outputs.length; j++) {
            if (outputs[j].isType()) {
              outputTypeList.add(outputs[j].getName());
              // also add subsumed types
              Type t = currentTypeSystem.getType(outputs[j].getName());
              if (t != null) {
                List subsumedTypes = currentTypeSystem.getProperlySubsumedTypes(t);
                Iterator it = subsumedTypes.iterator();
                while (it.hasNext()) {
                  outputTypeList.add(((Type) it.next()).getName());
                }
              }
            }
          }
        }
        // always allow viewing document annotation
        outputTypeList.add("uima.tcas.DocumentAnnotation");
        currentTaeOutputTypes = new String[outputTypeList.size()];
        outputTypeList.toArray(currentTaeOutputTypes);
      } else {
        currentTaeOutputTypes = null; // indicates all types should be
        // selected
      }

      // Process (in separate thread)
      mCPM.process(collectionReader);

      // if the user has already clicked cancel, call the
      // runner.terminate() immediately.
      if (progressMonitor.isCanceled()) {
        mCPM.stop();
        progressMonitor.close();
      }

    } catch (Throwable t) {
      displayError(t);
      aborted();
    }
  }

  class ProcessingThread extends Thread {
    File inputDir;

    File outputDir;

    File taeSpecifierFile;

    String xmlTag;

    String language;

    String encoding;

    ProcessingThread(File inputDir, File outputDir, File taeSpecifierFile, String xmlTag,
            String language, String encoding) {
      this.inputDir = inputDir;
      this.outputDir = outputDir;
      this.taeSpecifierFile = taeSpecifierFile;
      this.xmlTag = xmlTag;
      this.language = language;
      this.encoding = encoding;
    }

    public void run() {
      // Code moved outside class to make accessible by programs that call
      // DocumentAnalyzer. JMP
      runProcessingThread(inputDir, outputDir, taeSpecifierFile, xmlTag, language, encoding);
    }
  }

  /**
   * @return Returns the styleMapFile.
   */
  public File getStyleMapFile() {
    if (styleMapFile == null && prefsMed != null) {
      styleMapFile = prefsMed.getStylemapFile();
    }
    return styleMapFile;
  }

  /**
   * @param styleMapFile
   *          The styleMapFile to set.
   */
  public void setStyleMapFile(File styleMapFile) {
    this.styleMapFile = styleMapFile;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.Component#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return new Dimension(700, 350);
  }

}
