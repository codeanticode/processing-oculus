/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2012 Ben Fry and Casey Reas

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License version 2.1 as published by the Free Software Foundation.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
 */

package codeanticode.oculus;

import processing.core.PGraphics;
import processing.core.PShape;
import processing.core.PShapeOBJ;
import processing.core.PSurface;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PShapeOpenGL;
//import processing.opengl.PSurfaceJOGL;


public class Oculus extends PGraphicsOpenGL {
  
  public final static String RENDERER = "codeanticode.oculus.Oculus";
  
  public Oculus() {
    super();
  }

  @Override
  public PSurface createSurface() {  // ignore
    return new OculusSurface(this);
  }  
  
  public float getPixelScale() {
    PSurface surf = parent.getSurface();
    if (surf == null) {
      return pixelDensity;
    } else if (surf instanceof OculusSurface) {
      return ((OculusSurface)surf).getPixelScale();
    } else {
      throw new RuntimeException("Renderer cannot find an Oculus surface");
    }
  }  
  

  //////////////////////////////////////////////////////////////

  // RENDERER SUPPORT QUERIES


  @Override
  public boolean is2D() {
    return false;
  }


  @Override
  public boolean is3D() {
    return true;
  }


  //////////////////////////////////////////////////////////////

  // PROJECTION


  @Override
  protected void defaultPerspective() {
    perspective();
  }


  //////////////////////////////////////////////////////////////

  // CAMERA


  @Override
  protected void defaultCamera() {
    camera();
  }


  //////////////////////////////////////////////////////////////

  // MATRIX MORE!


  @Override
  protected void begin2D() {
    pushProjection();
    ortho(-width/2f, width/2f, -height/2f, height/2f);
    pushMatrix();

    // Set camera for 2D rendering, it simply centers at (width/2, height/2)
    float centerX = width/2f;
    float centerY = height/2f;
    modelview.reset();
    modelview.translate(-centerX, -centerY);

    modelviewInv.set(modelview);
    modelviewInv.invert();

    camera.set(modelview);
    cameraInv.set(modelviewInv);

    updateProjmodelview();
  }


  @Override
  protected void end2D() {
    popMatrix();
    popProjection();
  }



  //////////////////////////////////////////////////////////////

  // SHAPE I/O


  static protected boolean isSupportedExtension(String extension) {
    return extension.equals("obj");
  }


  static protected PShape loadShapeImpl(PGraphics pg, String filename,
                                                      String extension) {
    PShapeOBJ obj = null;

    if (extension.equals("obj")) {
      obj = new PShapeOBJ(pg.parent, filename);
      int prevTextureMode = pg.textureMode;
      pg.textureMode = NORMAL;
      PShapeOpenGL p3d = PShapeOpenGL.createShape((PGraphicsOpenGL)pg, obj);
      pg.textureMode = prevTextureMode;
      return p3d;
    }
    return null;
  }

}