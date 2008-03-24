/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.CloseWindowListener;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.browser.VisibilityWindowAdapter;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import chrriis.common.Utils;
import chrriis.dj.nativeswing.CommandMessage;
import chrriis.dj.nativeswing.ui.event.WebBrowserEvent;
import chrriis.dj.nativeswing.ui.event.WebBrowserListener;
import chrriis.dj.nativeswing.ui.event.WebBrowserNavigationEvent;
import chrriis.dj.nativeswing.ui.event.WebBrowserWindowOpeningEvent;
import chrriis.dj.nativeswing.ui.event.WebBrowserWindowWillOpenEvent;

/**
 * @author Christopher Deckers
 */
class NativeWebBrowser extends NativeComponent {

  private static final String COMMAND_PREFIX = "command://";
  
  private static class CMJ_closeWindow extends ControlCommandMessage {
    @Override
    public Object run() throws Exception {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getComponent();
      JWebBrowser webBrowser = nativeWebBrowser.webBrowser.get();
      if(webBrowser == null) {
        return null;
      }
      Object[] listeners = nativeWebBrowser.listenerList.getListenerList();
      WebBrowserEvent e = null;
      for(int i=listeners.length-2; i>=0; i-=2) {
        if(listeners[i] == WebBrowserListener.class) {
          if(e == null) {
            e = new WebBrowserEvent(webBrowser);
          }
          ((WebBrowserListener)listeners[i + 1]).windowClosing(e);
        }
      }
      JWebBrowserWindow browserWindow = webBrowser.getBrowserWindow();
      if(browserWindow != null) {
        browserWindow.dispose();
      }
      return null;
    }
  }
  
  private static class CMJ_createWindow extends ControlCommandMessage {
    @Override
    public Object run() {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getComponent();
      JWebBrowser webBrowser = nativeWebBrowser.webBrowser.get();
      if(webBrowser == null) {
        return null;
      }
      JWebBrowser jWebBrowser = new JWebBrowser();
      Object[] listeners = nativeWebBrowser.listenerList.getListenerList();
      WebBrowserWindowWillOpenEvent e = null;
      for(int i=listeners.length-2; i>=0 && jWebBrowser != null; i-=2) {
        if(listeners[i] == WebBrowserListener.class) {
          if(e == null) {
            e = new WebBrowserWindowWillOpenEvent(webBrowser, jWebBrowser);
          }
          ((WebBrowserListener)listeners[i + 1]).windowWillOpen(e);
          jWebBrowser = e.isConsumed()? null: e.getNewWebBrowser();
        }
      }
      if(jWebBrowser == null) {
        return null;
      }
      if(!jWebBrowser.isInitialized()) {
        Window windowAncestor = SwingUtilities.getWindowAncestor(jWebBrowser);
        if(windowAncestor == null) {
          final JWebBrowserWindow webBrowserWindow = new JWebBrowserWindow(jWebBrowser);
          windowAncestor = webBrowserWindow;
        } else {
        }
        jWebBrowser.getNativeComponent().initializeNativePeer();
      }
      return jWebBrowser.getNativeComponent().getComponentID();
    }
  }

  private static class CMJ_showWindow extends ControlCommandMessage {
    @Override
    public Object run() {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getComponent();
      final JWebBrowser webBrowser = nativeWebBrowser.webBrowser.get();
      if(webBrowser == null) {
        return null;
      }
      int componentID = (Integer)args[0];
      final JWebBrowser newWebBrowser = ((NativeWebBrowser)getRegistry().get(componentID)).webBrowser.get();
      newWebBrowser.setMenuBarVisible((Boolean)args[1]);
      newWebBrowser.setButtonBarVisible((Boolean)args[2]);
      newWebBrowser.setAddressBarVisible((Boolean)args[3]);
      newWebBrowser.setStatusBarVisible((Boolean)args[4]);
      Point location = (Point)args[5];
      Dimension size = (Dimension)args[6];
      JWebBrowserWindow browserWindow = newWebBrowser.getBrowserWindow();;
      if(browserWindow != null) {
        if(size != null) {
          browserWindow.setSize(size);
        }
        if(location != null) {
          browserWindow.setLocation(location);
        }
      }
      Object[] listeners = nativeWebBrowser.listenerList.getListenerList();
      WebBrowserWindowOpeningEvent e = null;
      for(int i=listeners.length-2; i>=0 && newWebBrowser != null; i-=2) {
        if(listeners[i] == WebBrowserListener.class) {
          if(e == null) {
            e = new WebBrowserWindowOpeningEvent(webBrowser, newWebBrowser, location, size);
          }
          ((WebBrowserListener)listeners[i + 1]).windowOpening(e);
        }
      }
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          JWebBrowserWindow browserWindow = newWebBrowser.getBrowserWindow();
          if(browserWindow != null && !newWebBrowser.getNativeComponent().isDisposed()) {
            browserWindow.setVisible(true);
          }
        }
      });
      return null;
    }
  }
  
  private static class CMJ_urlChanged extends ControlCommandMessage {
    @Override
    public Object run() {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getComponent();
      JWebBrowser webBrowser = nativeWebBrowser.webBrowser.get();
      if(webBrowser == null) {
        return null;
      }
      Object[] listeners = nativeWebBrowser.listenerList.getListenerList();
      String location = (String)args[0];
      boolean isTopFrame = (Boolean)args[1];
      WebBrowserNavigationEvent e = null;
      for(int i=listeners.length-2; i>=0; i-=2) {
        if(listeners[i] == WebBrowserListener.class) {
          if(e == null) {
            e = new WebBrowserNavigationEvent(webBrowser, location, isTopFrame);
          }
          ((WebBrowserListener)listeners[i + 1]).urlChanged(e);
        }
      }
      return null;
    }
  }

  private static class CMJ_commandReceived extends ControlCommandMessage {
    @Override
    public Object run() {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getComponent();
      JWebBrowser webBrowser = nativeWebBrowser.webBrowser.get();
      if(webBrowser == null) {
        return null;
      }
      Object[] listeners = nativeWebBrowser.listenerList.getListenerList();
      WebBrowserEvent e = null;
      String command = (String)args[0];
      String[] arguments = (String[])args[1];
      for(int i=listeners.length-2; i>=0; i-=2) {
        if(listeners[i] == WebBrowserListener.class) {
          if(e == null) {
            e = new WebBrowserEvent(webBrowser);
          }
          ((WebBrowserListener)listeners[i + 1]).commandReceived(e, command, arguments);
        }
      }
      return null;
    }
  }

  private static class CMJ_urlChanging extends ControlCommandMessage {
    @Override
    public Object run() {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getComponent();
      JWebBrowser webBrowser = nativeWebBrowser.webBrowser.get();
      if(webBrowser == null) {
        return false;
      }
      Object[] listeners = nativeWebBrowser.listenerList.getListenerList();
      String location = (String)args[0];
      boolean isTopFrame = (Boolean)args[1];
      boolean isNavigating = true;
      WebBrowserNavigationEvent e = null;
      for(int i=listeners.length-2; i>=0; i-=2) {
        if(listeners[i] == WebBrowserListener.class) {
          if(e == null) {
            e = new WebBrowserNavigationEvent(webBrowser, location, isTopFrame);
          }
          ((WebBrowserListener)listeners[i + 1]).urlChanging(e);
          isNavigating &= !e.isConsumed();
        }
      }
      return isNavigating;
    }
  }
      
  private static class CMJ_urlChangeCanceled extends ControlCommandMessage {
    @Override
    public Object run() {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getComponent();
      JWebBrowser webBrowser = nativeWebBrowser.webBrowser.get();
      if(webBrowser == null) {
        return null;
      }
      Object[] listeners = nativeWebBrowser.listenerList.getListenerList();
      String location = (String)args[0];
      boolean isTopFrame = (Boolean)args[1];
      WebBrowserNavigationEvent e = null;
      for(int i=listeners.length-2; i>=0; i-=2) {
        if(listeners[i] == WebBrowserListener.class) {
          if(e == null) {
            e = new WebBrowserNavigationEvent(webBrowser, location, isTopFrame);
          }
          ((WebBrowserListener)listeners[i + 1]).urlChangeCanceled(e);
        }
      }
      return null;
    }
  }

  private static class CMJ_updateTitle extends ControlCommandMessage {
    @Override
    public Object run() {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getComponent();
      JWebBrowser webBrowser = nativeWebBrowser.webBrowser.get();
      if(webBrowser == null) {
        return null;
      }
      nativeWebBrowser.title = (String)args[0];
      Object[] listeners = nativeWebBrowser.listenerList.getListenerList();
      WebBrowserEvent e = null;
      for(int i=listeners.length-2; i>=0; i-=2) {
        if(listeners[i] == WebBrowserListener.class) {
          if(e == null) {
            e = new WebBrowserEvent(webBrowser);
          }
          ((WebBrowserListener)listeners[i + 1]).titleChanged(e);
        }
      }
      return null;
    }
  }
  
  private static class CMJ_updateStatus extends ControlCommandMessage {
    @Override
    public Object run() {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getComponent();
      JWebBrowser webBrowser = nativeWebBrowser.webBrowser.get();
      if(webBrowser == null) {
        return null;
      }
      nativeWebBrowser.status = (String)args[0];
      Object[] listeners = nativeWebBrowser.listenerList.getListenerList();
      WebBrowserEvent e = null;
      for(int i=listeners.length-2; i>=0; i-=2) {
        if(listeners[i] == WebBrowserListener.class) {
          if(e == null) {
            e = new WebBrowserEvent(webBrowser);
          }
          ((WebBrowserListener)listeners[i + 1]).statusChanged(e);
        }
      }
      return null;
    }
  }
  
  private static class CMJ_updateProgress extends ControlCommandMessage {
    @Override
    public Object run() {
      NativeWebBrowser nativeWebBrowser = (NativeWebBrowser)getComponent();
      JWebBrowser webBrowser = nativeWebBrowser.webBrowser.get();
      if(webBrowser == null) {
        return null;
      }
      nativeWebBrowser.pageLoadingProgressValue = (Integer)args[0];
      Object[] listeners = nativeWebBrowser.listenerList.getListenerList();
      WebBrowserEvent e = null;
      for(int i=listeners.length-2; i>=0; i-=2) {
        if(listeners[i] == WebBrowserListener.class) {
          if(e == null) {
            e = new WebBrowserEvent(webBrowser);
          }
          ((WebBrowserListener)listeners[i + 1]).loadingProgressChanged(e);
        }
      }
      return null;
    }
  }
  
  protected static Control createControl(Shell shell) {
    int style = SWT.NONE;
    if("mozilla".equals(System.getProperty("dj.nativeswing.ui.webbrowser"))) {
      style |= SWT.MOZILLA;
    }
    final Browser browser = new Browser(shell, style);
    browser.addCloseWindowListener(new CloseWindowListener() {
      public void close(WindowEvent event) {
        asyncExec(browser, new CMJ_closeWindow());
      }
    });
    browser.addOpenWindowListener(new OpenWindowListener() {
      public void open(WindowEvent e) {
        // This forces the user to open it himself
        e.required = true;
        final Integer componentID = (Integer)syncExec(browser, new CMJ_createWindow());
        final Browser newWebBrowser;
        final boolean isDisposed;
        if(componentID == null) {
          isDisposed = true;
          Shell shell = new Shell();
          newWebBrowser = new Browser(shell, browser.getStyle());
        } else {
          isDisposed = false;
          newWebBrowser = (Browser)NativeComponent.getRegistry().get(componentID);
        }
        e.browser = newWebBrowser;
        newWebBrowser.addVisibilityWindowListener(new VisibilityWindowAdapter() {
          @Override
          public void show(WindowEvent e) {
            Browser browser = (Browser)e.widget;
            if(isDisposed) {
              final Shell shell = browser.getShell();
              e.display.asyncExec(new Runnable() {
                public void run() {
                  shell.close();
                }
              });
            } else {
              (browser).removeVisibilityWindowListener(this);
              asyncExec(newWebBrowser, new CMJ_showWindow(), componentID, e.menuBar, e.toolBar, e.addressBar, e.statusBar, e.location == null? null: new Point(e.location.x, e.location.y), e.size == null? null: new Dimension(e.size.x, e.size.y));
            }
          }
        });
      }
    });
    browser.addLocationListener(new LocationListener() {
      public void changed(LocationEvent e) {
        asyncExec(browser, new CMJ_urlChanged(), e.location, e.top);
      }
      public void changing(LocationEvent e) {
        final String location = e.location;
        if(location.startsWith(COMMAND_PREFIX)) {
          e.doit = false;
          String query = location.substring(COMMAND_PREFIX.length());
          if(query.endsWith("/")) {
            query = query.substring(0, query.length() - 1);
          }
          List<String> queryElementList = new ArrayList<String>();
          StringTokenizer st = new StringTokenizer(query, "&", true);
          String lastToken = null;
          while(st.hasMoreTokens()) {
            String token = st.nextToken();
            if("&".equals(token)) {
              if(lastToken == null) {
                queryElementList.add("");
              }
              lastToken = null;
            } else {
              lastToken = token;
              queryElementList.add(Utils.decodeURL(token));
            }
          }
          if(lastToken == null) {
            queryElementList.add("");
          }
          String command = queryElementList.isEmpty()? "": queryElementList.remove(0);
          String[] args = queryElementList.toArray(new String[0]);
          asyncExec(browser, new CMJ_commandReceived(), command, args);
          return;
        }
        if(location.startsWith("javascript:")) {
          return;
        }
        e.doit = (Boolean)syncExec(browser, new CMJ_urlChanging(), location, e.top);
        if(!e.doit) {
          asyncExec(browser, new CMJ_urlChangeCanceled(), location, e.top);
        }
      }
    });
    browser.addTitleListener(new TitleListener() {
      public void changed(TitleEvent e) {
        asyncExec(browser, new CMJ_updateTitle(), e.title);
      }
    });
    browser.addStatusTextListener(new StatusTextListener() {
      public void changed(StatusTextEvent e) {
        asyncExec(browser, new CMJ_updateStatus(), e.text);
      }
    });
    browser.addProgressListener(new ProgressListener() {
      public void changed(ProgressEvent e) {
        int loadingProgressValue = e.total == 0? 100: e.current * 100 / e.total;
        asyncExec(browser, new CMJ_updateProgress(), loadingProgressValue);
      }
      public void completed(ProgressEvent progressevent) {
        asyncExec(browser, new CMJ_updateProgress(), 100);
      }
    });
    return browser;
  }

  private Reference<JWebBrowser> webBrowser;
  
  public NativeWebBrowser(JWebBrowser webBrowser) {
    this.webBrowser = new WeakReference<JWebBrowser>(webBrowser);
  }

  private static class CMN_clearSessions extends CommandMessage {
    @Override
    public Object run() {
      Browser.clearSessions();
      return null;
    }
  }

  public static void clearSessions() {
    new CMN_clearSessions().asyncExec();
  }

  private static class CMN_getURL extends ControlCommandMessage {
    @Override
    public Object run() {
      return ((Browser)getControl()).getUrl();
    }
  }
  
  public String getURL() {
    return (String)runSync(new CMN_getURL());
  }
  
  private static class CMN_setURL extends ControlCommandMessage {
    @Override
    public Object run() {
      return ((Browser)getControl()).setUrl((String)args[0]);
    }
  }
  
  public boolean setURL(String url) {
    return Boolean.TRUE.equals(runSync(new CMN_setURL(), url));
  }
  
  private static class CMN_getText extends ControlCommandMessage {
    @Override
    public Object run() {
      return ((Browser)getControl()).getText();
    }
  }
  
  public String getText() {
    return (String)runSync(new CMN_getText());
  }
  
  private static class CMN_setText extends ControlCommandMessage {
    @Override
    public Object run() {
      return ((Browser)getControl()).setText((String)args[0]);
    }
  }
  
  public boolean setText(String html) {
    return Boolean.TRUE.equals(runSync(new CMN_setText(), html));
  }
  
  private static class CMN_execute extends ControlCommandMessage {
    private static Pattern JAVASCRIPT_LINE_COMMENT_PATTERN = Pattern.compile("^\\s*//.*$", Pattern.MULTILINE);
    @Override
    public Object run() {
      String script = (String)args[0];
      // Remove line comments, because it does not work properly on Mozilla.
      script = JAVASCRIPT_LINE_COMMENT_PATTERN.matcher(script).replaceAll("");
      return ((Browser)getControl()).execute(script);
    }
  }
  
  
  public boolean executeAndWait(String script) {
    return Boolean.TRUE.equals(runSync(new CMN_execute(), script));
  }
  
  public void execute(String script) {
    runAsync(new CMN_execute(), script);
  }
  
  private static class CMN_stop extends ControlCommandMessage {
    @Override
    public Object run() {
      ((Browser)getControl()).stop();
      return null;
    }
  }
  
  public void stop() {
    runAsync(new CMN_stop());
  }
  
  private static class CMN_refresh extends ControlCommandMessage {
    @Override
    public Object run() {
      ((Browser)getControl()).refresh();
      return null;
    }
  }
  
  public void refresh() {
    runAsync(new CMN_refresh());
  }
  
  private static class CMN_isBackEnabled extends ControlCommandMessage {
    @Override
    public Object run() {
      return ((Browser)getControl()).isBackEnabled();
    }
  }
  
  public boolean isBackEnabled() {
    return Boolean.TRUE.equals(runSync(new CMN_isBackEnabled()));
  }
  
  private static class CMN_back extends ControlCommandMessage {
    @Override
    public Object run() {
      return ((Browser)getControl()).back();
    }
  }
  
  public void back() {
    runAsync(new CMN_back());
  }
  
  private static class CMN_forward extends ControlCommandMessage {
    @Override
    public Object run() {
      return ((Browser)getControl()).forward();
    }
  }
  
  private static class CMN_isForwardEnabled extends ControlCommandMessage {
    @Override
    public Object run() {
      return ((Browser)getControl()).isForwardEnabled();
    }
  }
  
  public boolean isForwardEnabled() {
    return Boolean.TRUE.equals(runSync(new CMN_isForwardEnabled()));
  }
  
  public void forward() {
    runAsync(new CMN_forward());
  }
  
  private String status;

  public String getStatus() {
    return status == null? "": status;
  }
  
  private String title;

  public String getTitle() {
    return title == null? "": title;
  }
  
  private int pageLoadingProgressValue = 100;
  
  /**
   * @return A value between 0 and 100 indicating the current loading progress.
   */
  public int getPageLoadingProgressValue() {
    return pageLoadingProgressValue;
  }
  
  public void addWebBrowserListener(WebBrowserListener listener) {
    listenerList.add(WebBrowserListener.class, listener);
  }
  
  public void removeWebBrowserListener(WebBrowserListener listener) {
    listenerList.remove(WebBrowserListener.class, listener);
  }
  
  public WebBrowserListener[] getWebBrowserListeners() {
    return listenerList.getListeners(WebBrowserListener.class);
  }
  
}
