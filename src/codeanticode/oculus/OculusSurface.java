package codeanticode.oculus;

//import java.awt.Dimension;
//import java.awt.Point;
//import java.awt.Frame;
import java.util.Arrays;
//import com.jogamp.nativewindow.ScalableSurface;
import com.jogamp.nativewindow.util.DimensionImmutable;
import com.jogamp.nativewindow.util.PointImmutable;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLAnimatorControl;
import com.jogamp.opengl.GLAutoDrawable;
//import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.math.FovHVHalves;
import com.jogamp.newt.MonitorDevice;
import com.jogamp.newt.Screen;
//import com.jogamp.newt.event.WindowAdapter;
//import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.newt.opengl.util.stereo.StereoDeviceUtil;
//import com.jogamp.opengl.test.junit.jogl.demos.es2.av.MovieSBSStereo;
//import com.jogamp.opengl.test.junit.jogl.demos.es2.av.MovieSimple;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.AnimatorBase;
import com.jogamp.opengl.util.FPSAnimator;

import com.jogamp.opengl.util.av.GLMediaPlayer;
import com.jogamp.opengl.util.stereo.StereoDevice;
import com.jogamp.opengl.util.stereo.StereoDeviceRenderer;
import com.jogamp.opengl.util.stereo.StereoDeviceFactory;
import com.jogamp.opengl.util.stereo.EyeParameter;
import com.jogamp.opengl.util.stereo.StereoClientRenderer;
import com.jogamp.opengl.util.stereo.StereoGLEventListener;
import com.jogamp.opengl.util.stereo.StereoUtil;
import com.jogamp.opengl.util.stereo.ViewerPose;

import jogamp.opengl.util.stereo.GenericStereoDevice;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.opengl.PGL;
import processing.opengl.PSurfaceJOGL;

public class OculusSurface extends PSurfaceJOGL {

  // from https://github.com/sgothel/jogl/blob/master/src/test/com/jogamp/opengl/test/junit/jogl/stereo/StereoDemo01.java
  static boolean useStereoScreen = true;

  static int numSamples = 0;
  static boolean biLinear = true;
  static boolean useSingleFBO = false;
  static boolean useVignette = true;
  static boolean useChromatic = true;
  static boolean useTimewarp = true;
  static boolean useAutoSwap = false;
//  static String useFilmFile = null;
//  static String useFilmURI = null;
  static StereoDeviceFactory.DeviceType deviceType = StereoDeviceFactory.DeviceType.OculusVR;
  static int deviceIndex = 0;
  StereoDevice stereoDevice;
  Animator animator2;
  
  public OculusSurface(PGraphics graphics) {
    super(graphics);
  }


  protected void initWindow() {
    int posx = -1;
    int posy = -1;

    final StereoDeviceFactory stereoDeviceFactory = StereoDeviceFactory.createFactory(deviceType);
    if( null == stereoDeviceFactory ) {
        System.err.println("No StereoDeviceFactory available");
        return;
    }

    stereoDevice = stereoDeviceFactory.createDevice(deviceIndex, null, true /* verbose */);
    if( null == stereoDevice ) {
        System.err.println("No StereoDevice.Context available for index "+deviceIndex);
        return;
    }

    final boolean isGenericDevice = stereoDevice instanceof GenericStereoDevice;

    if( 0 <= posx && 0 <= posy && isGenericDevice ) {
        ((GenericStereoDevice)stereoDevice).setSurfacePosition(posx, posy);
    }
    System.err.println("StereoDevice "+stereoDevice);

    //
    //
    //
    final PointImmutable devicePos = stereoDevice.getPosition();
    final DimensionImmutable deviceRes = stereoDevice.getSurfaceSize();
    System.err.println("Device Res "+deviceRes+", reqRotation "+stereoDevice.getRequiredRotation());
    System.err.println("Device Pos "+devicePos);

    final MonitorDevice monitor = StereoDeviceUtil.getMonitorDevice(stereoDevice, true);
    final Screen screen = monitor.getScreen();

    
    // Start the sensor which provides the Rift’s pose and motion, needs JOGL 2.3.2
    if( !stereoDevice.startSensors(stereoDevice.getSupportedSensorBits(), 0) ) {
        System.err.println("Could not start sensors on device "+deviceIndex);
    }
    
    window = GLWindow.create(screen, pgl.getCaps());
    /*
    if (displayDevice == null) {
      displayDevice = window.getMainMonitor();
    }

    int displayNum = sketch.sketchDisplay();
    if (displayNum > 0) {  // if -1, use the default device
      if (displayNum <= monitors.size()) {
        displayDevice = monitors.get(displayNum - 1);
      } else {
        System.err.format("Display %d does not exist, " +
          "using the default display instead.%n", displayNum);
        for (int i = 0; i < monitors.size(); i++) {
          System.err.format("Display %d is %s%n", i+1, monitors.get(i));
        }
      }
    }

    boolean spanDisplays = sketch.sketchDisplay() == PConstants.SPAN;
    screenRect = spanDisplays ?
      new Rectangle(0, 0, screen.getWidth(), screen.getHeight()) :
      new Rectangle(0, 0,
                    displayDevice.getViewportInWindowUnits().getWidth(),
                    displayDevice.getViewportInWindowUnits().getHeight());
*/
    
    if( useStereoScreen ) {
      window.setPosition(devicePos.getX(), devicePos.getY());
    }
    window.setSurfaceSize(deviceRes.getWidth(), deviceRes.getHeight());
    window.setAutoSwapBufferMode(useAutoSwap);
//    window.setUndecorated(true);    

    // Set the displayWidth/Height variables inside PApplet, so that they're
    // usable and can even be returned by the sketchWidth()/Height() methods.
    sketch.displayWidth = deviceRes.getWidth();
    sketch.displayHeight = deviceRes.getHeight();

    sketchWidth = sketch.sketchWidth();
    sketchHeight = sketch.sketchHeight();

//    boolean fullScreen = sketch.sketchFullScreen();
    // Removing the section below because sometimes people want to do the
    // full screen size in a window, and it also breaks insideSettings().
    // With 3.x, fullScreen() is so easy, that it's just better that way.
    // https://github.com/processing/processing/issues/3545
    /*
    // Sketch has already requested to be the same as the screen's
    // width and height, so let's roll with full screen mode.
    if (screenRect.width == sketchWidth &&
        screenRect.height == sketchHeight) {
      fullScreen = true;
      sketch.fullScreen();
    }
    */

//    if (fullScreen || spanDisplays) {
//      sketchWidth = screenRect.width;
//      sketchHeight = screenRect.height;
//    }

    /*
    float[] reqSurfacePixelScale;
    if (graphics.is2X()) {
       // Retina
       reqSurfacePixelScale = new float[] { ScalableSurface.AUTOMAX_PIXELSCALE,
                                            ScalableSurface.AUTOMAX_PIXELSCALE };
    } else {
      // Non-retina
      reqSurfacePixelScale = new float[] { ScalableSurface.IDENTITY_PIXELSCALE,
                                           ScalableSurface.IDENTITY_PIXELSCALE };
    }
    window.setSurfaceScale(reqSurfacePixelScale);
    window.setSize(sketchWidth, sketchHeight);
    setSize(sketchWidth, sketchHeight);
    sketchX = displayDevice.getViewportInWindowUnits().getX();
    sketchY = displayDevice.getViewportInWindowUnits().getY();
    if (fullScreen) {
      PApplet.hideMenuBar();
      window.setTopLevelPosition(sketchX, sketchY);
      placedWindow = true;
      if (spanDisplays) {
        window.setFullscreen(monitors);
      } else {
        window.setFullscreen(true);
      }
    }
    */

    PApplet.hideMenuBar();
    window.setFullscreen(true);
  }


  protected void initListeners() {
    //
    // Stereo Device Setup
    //
    // EyePos.y = ovrHmd_GetFloat(HMD, OVR_KEY_EYE_HEIGHT, EyePos.y);
    boolean useRecommendedDistortionBits = true;
    final FovHVHalves[] defaultEyeFov = stereoDevice.getDefaultFOV();
    System.err.println("Default Fov[0]: "+defaultEyeFov[0]);
    System.err.println("Default Fov[0]: "+defaultEyeFov[0].toStringInDegrees());
    if( defaultEyeFov.length > 1 ) {
        System.err.println("Default Fov[1]: "+defaultEyeFov[1]);
        System.err.println("Default Fov[1]: "+defaultEyeFov[1].toStringInDegrees());
    } 
    
//    final boolean usesLenses = 0 != ( StereoDeviceRenderer.DISTORTION_BARREL & stereoDevice.getMinimumDistortionBits() );
    final float[] eyePositionOffset = stereoDevice.getDefaultEyePositionOffset(); // default
    System.err.println("Eye Position Offset: "+Arrays.toString(eyePositionOffset));

    final int textureUnit = 0;
    final int reqDistortionBits;
    if( useRecommendedDistortionBits ) {
        reqDistortionBits = stereoDevice.getRecommendedDistortionBits();
    } else {
        reqDistortionBits = ( useVignette ? StereoDeviceRenderer.DISTORTION_VIGNETTE : 0 ) |
                         ( useChromatic ? StereoDeviceRenderer.DISTORTION_CHROMATIC : 0 ) |
                         ( useTimewarp ? StereoDeviceRenderer.DISTORTION_TIMEWARP : 0 );
    }
    System.err.println("Requesting Distortion Bits: "+StereoUtil.distortionBitsToString(reqDistortionBits));    
    final float pixelsPerDisplayPixel = 1f;
    final StereoDeviceRenderer stereoDeviceRenderer =
            stereoDevice.createRenderer(reqDistortionBits, useSingleFBO ? 1 : 2, eyePositionOffset,
                                        defaultEyeFov, pixelsPerDisplayPixel, textureUnit);
    System.err.println("StereoDeviceRenderer: "+stereoDeviceRenderer);    
    final int texFilter = biLinear ? GL.GL_LINEAR : GL.GL_NEAREST;
    final StereoClientRenderer renderer = new StereoClientRenderer(stereoDeviceRenderer, true /* ownsDist */, texFilter, texFilter, numSamples);
    DrawListenerStereo drawlistener = new DrawListenerStereo();
    renderer.addGLEventListener(drawlistener);
    window.addGLEventListener(renderer);

    NEWTMouseListener mouseListener = new NEWTMouseListener();
    window.addMouseListener(mouseListener);
    NEWTKeyListener keyListener = new NEWTKeyListener();
    window.addKeyListener(keyListener);
    NEWTWindowListener winListener = new NEWTWindowListener();
    window.addWindowListener(winListener);
  }


  protected void initAnimator() {
//  System.err.println("1. create animator");
    animator2 = new Animator();
    animator2.setModeBits(false, AnimatorBase.MODE_EXPECT_AWT_RENDERING_THREAD);
    animator2.setExclusiveContext(false);
    drawException = null;

    animator2.setUncaughtExceptionHandler(new GLAnimatorControl.UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(final GLAnimatorControl animator,
                                    final GLAutoDrawable drawable,
                                    final Throwable cause) {
        synchronized (waitObject) {
//        System.err.println("Caught exception: " + cause.getMessage());
          drawException = cause;
          waitObject.notify();
        }
      }
    });

    new Thread(new Runnable() {
      public void run() {
        synchronized (waitObject) {
          try {
            if (drawException == null) waitObject.wait();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
//        System.err.println("Caught exception: " + drawException.getMessage());
          if (drawException != null) {
            Throwable cause = drawException.getCause();
            if (cause instanceof ThreadDeath) {
//            System.out.println("caught ThreadDeath");
//            throw (ThreadDeath)cause;
            } else if (cause instanceof RuntimeException) {
              throw (RuntimeException)cause;
            } else {
              throw new RuntimeException(cause);
            }
          }
        }
      }
    }).start();
  }


  @Override
  public void placeWindow(int[] location, int[] editorLocation) {
    /*
    int x = window.getX() - window.getInsets().getLeftWidth();
    int y = window.getY() - window.getInsets().getTopHeight();
    int w = window.getWidth() + window.getInsets().getTotalWidth();
    int h = window.getHeight() + window.getInsets().getTotalHeight();

    if (location != null) {
      window.setTopLevelPosition(location[0], location[1]);

    } else if (editorLocation != null) {
      int locationX = editorLocation[0] - 20;
      int locationY = editorLocation[1];

      if (locationX - w > 10) {
        // if it fits to the left of the window
        window.setTopLevelPosition(locationX - w, locationY);

      } else {  // doesn't fit
        locationX = editorLocation[0] + 66;
        locationY = editorLocation[1] + 66;

        if ((locationX + w > sketch.displayWidth - 33) ||
            (locationY + h > sketch.displayHeight - 33)) {
          // otherwise center on screen
          locationX = (sketch.displayWidth - w) / 2;
          locationY = (sketch.displayHeight - h) / 2;
        }
        window.setTopLevelPosition(locationX, locationY);
      }
    } else {  // just center on screen
      int sketchX = displayDevice.getViewportInWindowUnits().getX();
      int sketchY = displayDevice.getViewportInWindowUnits().getY();
      window.setTopLevelPosition(sketchX + screenRect.x + (screenRect.width - sketchWidth) / 2,
                                 sketchY + screenRect.y + (screenRect.height - sketchHeight) / 2);

    }

    Point frameLoc = new Point(x, y);
    if (frameLoc.y < 0) {
      // Windows actually allows you to place frames where they can't be
      // closed. Awesome. http://dev.processing.org/bugs/show_bug.cgi?id=1508
      window.setTopLevelPosition(frameLoc.x, 30);
    }
*/
    

//    canvas.setBounds((contentW - sketchWidth)/2,
//                     (contentH - sketchHeight)/2,
//                     sketchWidth, sketchHeight);
  }

  
  public void startThread() {
    if (animator2 != null) {
      window.setVisible(true);
      animator2.add(window);
      animator2.setUpdateFPSFrames(60, System.err);
      animator2.start();

      // Correct window size to actual pixel size,
      // which ration is unknown before window creation when using multiple displays!
      final PointImmutable devicePos = stereoDevice.getPosition();
      final DimensionImmutable deviceRes = stereoDevice.getSurfaceSize();
      System.err.println("Window.0.windowSize : "+window.getWidth()+" x "+window.getHeight());
      System.err.println("Window.0.surfaceSize: "+window.getSurfaceWidth()+" x "+window.getSurfaceHeight());
      window.setSurfaceSize(deviceRes.getWidth(), deviceRes.getHeight());
      if( useStereoScreen ) {
        int x = devicePos.getX();
        int y = devicePos.getY();
        window.setPosition(x, y);
      }
      System.err.println("Window.1.windowSize : "+window.getWidth()+" x "+window.getHeight());
      System.err.println("Window.1.surfaceSize: "+window.getSurfaceWidth()+" x "+window.getSurfaceHeight());              
    }
  }


  class DrawListenerStereo implements StereoGLEventListener {
    @Override
    public void display(GLAutoDrawable drawable) {
      pgl.getGL(drawable);
//      System.out.println(" - " + sketch.frameCount);
      if (sketch.frameCount == 0) {
        requestFocus();
      }

      pgl.clearColor(1, 0, 0, 1);
      pgl.clear(PGL.COLOR_BUFFER_BIT);

      pgl.finish();
      
      //sketch.handleDraw();

      if (sketch.exitCalled()) {
//        System.out.println("exit");
        animator.stop();
        sketch.dispose();
        sketch.exitActual();
      }
    }
    public void dispose(GLAutoDrawable drawable) {
//      pgl.getGL(drawable);
//      System.out.println("dispose");
      sketch.dispose();
//      if (sketch.exitCalled()) {
//        sketch.exitActual();
//      }
    }
    public void init(GLAutoDrawable drawable) {
//      System.err.println("2. init drawable");
      pgl.getGL(drawable);
      pgl.init(drawable);
      sketch.start();
//      setSize(sketchWidth, sketchHeight);

      int c = graphics.backgroundColor;
      pgl.clearColor(((c >> 16) & 0xff) / 255f,
                     ((c >>  8) & 0xff) / 255f,
                     ((c >>  0) & 0xff) / 255f,
                     ((c >> 24) & 0xff) / 255f);
      pgl.clear(PGL.COLOR_BUFFER_BIT);
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {

//      final float[] valReqSurfacePixelScale = window.getRequestedSurfaceScale(new float[2]);
      window.getCurrentSurfaceScale(currentPixelScale);
//      final float[] nativeSurfacePixelScale = window.getMaximumSurfaceScale(new float[2]);
//      System.err.println("[set PixelScale post]: "+
//                         valReqSurfacePixelScale[0]+"x"+valReqSurfacePixelScale[1]+" (val) -> "+
//                         hasSurfacePixelScale[0]+"x"+hasSurfacePixelScale[1]+" (has), "+
//                         nativeSurfacePixelScale[0]+"x"+nativeSurfacePixelScale[1]+" (native)");




//      System.out.println("reshape: " + w + ", " + h);
      pgl.getGL(drawable);
//      if (!graphics.is2X() && 1 < hasSurfacePixelScale[0]) {
//        setSize(w/2, h/2);
//      } else {
//        setSize(w, h);
//      }
      setSize((int)(w/currentPixelScale[0]), (int)(h/currentPixelScale[1]));
    }
    @Override
    public void display(GLAutoDrawable drawable, int arg1) {
      pgl.getGL(drawable);
//      System.out.println(" - " + sketch.frameCount);
      if (sketch.frameCount == 0) {
        requestFocus();
      }

      sketch.handleDraw();

      if (sketch.exitCalled()) {
//        System.out.println("exit");
        animator.stop();
        sketch.dispose();
        sketch.exitActual();
      }
      
    }
    @Override
    public void reshapeForEye(GLAutoDrawable drawable, int x, int y, int w,
        int h, EyeParameter arg5, ViewerPose arg6) {
      // TODO Auto-generated method stub

    }
  }

  /*
  protected class NEWTWindowListener implements com.jogamp.newt.event.WindowListener {
    public NEWTWindowListener() {
      super();
    }
    @Override
    public void windowGainedFocus(com.jogamp.newt.event.WindowEvent arg0) {
//      pg.parent.focusGained(null);
//      System.err.println("gain focus");
      sketch.focused = true;
      sketch.focusGained();
    }

    @Override
    public void windowLostFocus(com.jogamp.newt.event.WindowEvent arg0) {
//      pg.parent.focusLost(null);
//      System.err.println("lost focus");
      sketch.focused = false;
      sketch.focusLost();
    }

    @Override
    public void windowDestroyNotify(com.jogamp.newt.event.WindowEvent arg0) {
//      System.err.println("bye");
      OculusSurface.this.sketch.dispose();
      OculusSurface.this.sketch.exitActual();
    }

    @Override
    public void windowDestroyed(com.jogamp.newt.event.WindowEvent arg0) {
    }

    @Override
    public void windowMoved(com.jogamp.newt.event.WindowEvent arg0) {
    }

    @Override
    public void windowRepaint(com.jogamp.newt.event.WindowUpdateEvent arg0) {
    }

    @Override
    public void windowResized(com.jogamp.newt.event.WindowEvent arg0) {
//      System.err.println("resized");
//      System.err.println(window.hasFocus());
//      window.removeMouseListener(mouseListener);
//      mouseListener = new NEWTMouseListener();
//      window.addMouseListener(mouseListener);
    }

  }

  // NEWT mouse listener
  protected class NEWTMouseListener extends com.jogamp.newt.event.MouseAdapter {
    public NEWTMouseListener() {
      super();
    }
    @Override
    public void mousePressed(com.jogamp.newt.event.MouseEvent e) {
      nativeMouseEvent(e, MouseEvent.PRESS);
    }
    @Override
    public void mouseReleased(com.jogamp.newt.event.MouseEvent e) {
      nativeMouseEvent(e, MouseEvent.RELEASE);
    }
    @Override
    public void mouseClicked(com.jogamp.newt.event.MouseEvent e) {
      nativeMouseEvent(e, MouseEvent.CLICK);
    }
    @Override
    public void mouseDragged(com.jogamp.newt.event.MouseEvent e) {
      nativeMouseEvent(e, MouseEvent.DRAG);
    }
    @Override
    public void mouseMoved(com.jogamp.newt.event.MouseEvent e) {
      nativeMouseEvent(e, MouseEvent.MOVE);
    }
    @Override
    public void mouseWheelMoved(com.jogamp.newt.event.MouseEvent e) {
      nativeMouseEvent(e, MouseEvent.WHEEL);
    }
    @Override
    public void mouseEntered(com.jogamp.newt.event.MouseEvent e) {
//      System.out.println("enter");
      nativeMouseEvent(e, MouseEvent.ENTER);
    }
    @Override
    public void mouseExited(com.jogamp.newt.event.MouseEvent e) {
//      System.out.println("exit");
      nativeMouseEvent(e, MouseEvent.EXIT);
    }
  }

  // NEWT key listener
  protected class NEWTKeyListener extends com.jogamp.newt.event.KeyAdapter {
    public NEWTKeyListener() {
      super();
    }
    @Override
    public void keyPressed(com.jogamp.newt.event.KeyEvent e) {
      nativeKeyEvent(e, KeyEvent.PRESS);
    }
    @Override
    public void keyReleased(com.jogamp.newt.event.KeyEvent e) {
      nativeKeyEvent(e, KeyEvent.RELEASE);
    }
    public void keyTyped(com.jogamp.newt.event.KeyEvent e)  {
      nativeKeyEvent(e, KeyEvent.TYPE);
    }
  }

  protected void nativeMouseEvent(com.jogamp.newt.event.MouseEvent nativeEvent,
                                  int peAction) {
    int modifiers = nativeEvent.getModifiers();
    int peModifiers = modifiers &
                      (InputEvent.SHIFT_MASK |
                       InputEvent.CTRL_MASK |
                       InputEvent.META_MASK |
                       InputEvent.ALT_MASK);

    int peButton = 0;
    if ((modifiers & InputEvent.BUTTON1_MASK) != 0) {
      peButton = PConstants.LEFT;
    } else if ((modifiers & InputEvent.BUTTON2_MASK) != 0) {
      peButton = PConstants.CENTER;
    } else if ((modifiers & InputEvent.BUTTON3_MASK) != 0) {
      peButton = PConstants.RIGHT;
    }

    if (PApplet.platform == PConstants.MACOSX) {
      //if (nativeEvent.isPopupTrigger()) {
      if ((modifiers & InputEvent.CTRL_MASK) != 0) {
        peButton = PConstants.RIGHT;
      }
    }

    int peCount = 0;
    if (peAction == MouseEvent.WHEEL) {
      peCount = nativeEvent.isShiftDown() ? (int)nativeEvent.getRotation()[0] :
                                            (int)nativeEvent.getRotation()[1];
    } else {
      peCount = nativeEvent.getClickCount();
    }

    window.getCurrentSurfaceScale(currentPixelScale);
    int sx = (int)(nativeEvent.getX()/currentPixelScale[0]);
    int sy = (int)(nativeEvent.getY()/currentPixelScale[1]);
    int mx = sx;
    int my = sy;

    if (pgl.presentMode()) {
      mx -= (int)pgl.presentX;
      my -= (int)pgl.presentY;
      if (peAction == KeyEvent.RELEASE &&
          pgl.insideCloseButton(sx, sy - screenRect.height)) {
        sketch.exit();
      }
      if (mx < 0 || sketchWidth < mx || my < 0 || sketchHeight < my) {
        return;
      }
    }

    MouseEvent me = new MouseEvent(nativeEvent, nativeEvent.getWhen(),
                                   peAction, peModifiers,
                                   mx, my,
                                   peButton,
                                   peCount);

    sketch.postEvent(me);
  }

  protected void nativeKeyEvent(com.jogamp.newt.event.KeyEvent nativeEvent,
                                int peAction) {
    int peModifiers = nativeEvent.getModifiers() &
                      (InputEvent.SHIFT_MASK |
                       InputEvent.CTRL_MASK |
                       InputEvent.META_MASK |
                       InputEvent.ALT_MASK);

    short code = nativeEvent.getKeyCode();
    char keyChar;
    int keyCode;
    if (isPCodedKey(code)) {
      keyCode = mapToPConst(code);
      keyChar = PConstants.CODED;
    } else if (isHackyKey(code)) {
      keyCode = code;
      keyChar = hackToChar(code, nativeEvent.getKeyChar());
    } else if (code == com.jogamp.newt.event.KeyEvent.VK_ENTER) {
      // we can return only one char, let it be \n everywhere
      keyCode = 10;
      keyChar = '\n';
    } else {
      keyCode = code;
      keyChar = nativeEvent.getKeyChar();
    }

    // From http://jogamp.org/deployment/v2.1.0/javadoc/jogl/javadoc/com/jogamp/newt/event/KeyEvent.html
    // public final short getKeySymbol()
    // Returns the virtual key symbol reflecting the current keyboard layout.
    // public final short getKeyCode()
    // Returns the virtual key code using a fixed mapping to the US keyboard layout.
    // In contrast to key symbol, key code uses a fixed US keyboard layout and therefore is keyboard layout independent.
    // E.g. virtual key code VK_Y denotes the same physical key regardless whether keyboard layout QWERTY or QWERTZ is active. The key symbol of the former is VK_Y, where the latter produces VK_Y.
    KeyEvent ke = new KeyEvent(nativeEvent, nativeEvent.getWhen(),
                               peAction, peModifiers,
                               keyChar,
                               keyCode,
                               nativeEvent.isAutoRepeat());

    sketch.postEvent(ke);

    if (!isPCodedKey(code) && !isHackyKey(code)) {
      if (peAction == KeyEvent.PRESS) {
        // Create key typed event
        // TODO: combine dead keys with the following key
        KeyEvent tke = new KeyEvent(nativeEvent, nativeEvent.getWhen(),
                                    KeyEvent.TYPE, peModifiers,
                                    keyChar,
                                    0,
                                    nativeEvent.isAutoRepeat());

        sketch.postEvent(tke);
      }
    }
  }

  private static boolean isPCodedKey(short code) {
    return code == com.jogamp.newt.event.KeyEvent.VK_UP ||
           code == com.jogamp.newt.event.KeyEvent.VK_DOWN ||
           code == com.jogamp.newt.event.KeyEvent.VK_LEFT ||
           code == com.jogamp.newt.event.KeyEvent.VK_RIGHT ||
           code == com.jogamp.newt.event.KeyEvent.VK_ALT ||
           code == com.jogamp.newt.event.KeyEvent.VK_CONTROL ||
           code == com.jogamp.newt.event.KeyEvent.VK_SHIFT ||
           code == com.jogamp.newt.event.KeyEvent.VK_WINDOWS;
  }

  // Why do we need this mapping?
  // Relevant discussion and links here:
  // http://forum.jogamp.org/Newt-wrong-keycode-for-key-td4033690.html#a4033697
  // (I don't think this is a complete solution).
  private static int mapToPConst(short code) {
    if (code == com.jogamp.newt.event.KeyEvent.VK_UP) {
      return PConstants.UP;
    } else if (code == com.jogamp.newt.event.KeyEvent.VK_DOWN) {
      return PConstants.DOWN;
    } else if (code == com.jogamp.newt.event.KeyEvent.VK_LEFT) {
      return PConstants.LEFT;
    } else if (code == com.jogamp.newt.event.KeyEvent.VK_RIGHT) {
      return PConstants.RIGHT;
    } else if (code == com.jogamp.newt.event.KeyEvent.VK_ALT) {
      return PConstants.ALT;
    } else if (code == com.jogamp.newt.event.KeyEvent.VK_CONTROL) {
      return PConstants.CONTROL;
    } else if (code == com.jogamp.newt.event.KeyEvent.VK_SHIFT) {
      return PConstants.SHIFT;
    } else if (code == com.jogamp.newt.event.KeyEvent.VK_WINDOWS) {
      return java.awt.event.KeyEvent.VK_META;
    }
    return code;
  }

  private static boolean isHackyKey(short code) {
    return code == com.jogamp.newt.event.KeyEvent.VK_BACK_SPACE ||
           code == com.jogamp.newt.event.KeyEvent.VK_TAB;
  }

  private static char hackToChar(short code, char def) {
    if (code == com.jogamp.newt.event.KeyEvent.VK_BACK_SPACE) {
      return '\b';
    } else if (code == com.jogamp.newt.event.KeyEvent.VK_TAB) {
      return '\t';
    }
    return def;
  }
*/


}
