/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.demo.examples.advancedcapabilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import chrriis.dj.nativeswing.NativeComponentOptions;
import chrriis.dj.nativeswing.NativeInterface;
import chrriis.dj.nativeswing.NativeComponentOptions.VisibilityConstraint;
import chrriis.dj.nativeswing.components.JFlashPlayer;
import chrriis.dj.nativeswing.components.JWebBrowser;
import chrriis.dj.nativeswing.demo.examples.flashplayer.SimpleFlashExample;

/**
 * @author Christopher Deckers
 */
public class VisibilityConstraintOption extends JPanel {

  protected static final String LS = System.getProperty("line.separator");
  protected static final int OFFSET_X = 150;
  protected static final int OFFSET_Y = 120;
  protected static final int WIDTH = 200;
  protected static final int HEIGHT = 200;

  public VisibilityConstraintOption() {
    super(new BorderLayout(0, 0));
    JLayeredPane layeredPane = new JLayeredPane();
    layeredPane.setOpaque(true);
    layeredPane.setBackground(new Color(200, 200, 255));
    int layerIndex = 0;
    // A flash player
    NativeComponentOptions.getNextInstanceOptions().setVisibilityConstraint(VisibilityConstraint.FULL_COMPONENT_TREE);
    JFlashPlayer flashPlayer = new JFlashPlayer();
    flashPlayer.setControlBarVisible(false);
    flashPlayer.load(SimpleFlashExample.class, "resource/Movement-pointer_or_click.swf");
    flashPlayer.setBounds(OFFSET_X * layerIndex, OFFSET_Y * layerIndex, WIDTH, HEIGHT);
    layeredPane.setLayer(flashPlayer, layerIndex++);
    layeredPane.add(flashPlayer);
    // A swing panel
    JPanel swingPanel = new JPanel();
    swingPanel.setBorder(BorderFactory.createTitledBorder("Swing JPanel"));
    swingPanel.setBackground(Color.GREEN);
    swingPanel.setBounds(OFFSET_X * layerIndex, OFFSET_Y * layerIndex, WIDTH, HEIGHT);
    layeredPane.setLayer(swingPanel, layerIndex++);
    layeredPane.add(swingPanel);
    // A web browser
    NativeComponentOptions.getNextInstanceOptions().setVisibilityConstraint(VisibilityConstraint.FULL_COMPONENT_TREE);
    JWebBrowser webBrowser = new JWebBrowser();
    webBrowser.setBarsVisible(false);
    webBrowser.setStatusBarVisible(true);
    webBrowser.setHTMLContent(
        "<html>" + LS +
        "  <body>" + LS +
        "    <h1>A web page</h1>" + LS +
        "    <p>A paragraph with a <a href=\"http://www.google.com\">link</a>.</p>" + LS +
        "  </body>" + LS +
        "</html>");
    webBrowser.setBounds(OFFSET_X * layerIndex, OFFSET_Y * layerIndex, WIDTH, HEIGHT);
    layeredPane.setLayer(webBrowser, layerIndex++);
    // A swing button
    JButton swingButton = new JButton("Swing JButton");
    swingButton.setBounds(OFFSET_X * layerIndex, OFFSET_Y * layerIndex, WIDTH, HEIGHT);
    layeredPane.setLayer(swingButton, layerIndex++);
    layeredPane.add(swingButton);
    layeredPane.add(webBrowser);
    layeredPane.setPreferredSize(new Dimension(WIDTH + OFFSET_X * (layerIndex - 1), HEIGHT + OFFSET_Y * (layerIndex - 1)));
    add(new JScrollPane(layeredPane), BorderLayout.CENTER);
  }
  
  /* Standard main method to try that test as a standalone application. */
  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch(Exception e) {}
    NativeInterface.start();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JFrame frame = new JFrame("DJ Native Swing Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new VisibilityConstraintOption(), BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
      }
    });
  }
  
}
