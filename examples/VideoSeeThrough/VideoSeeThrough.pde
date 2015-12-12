// This is an example for making augmented reality. 
// You need web camera for running it.
// Author: Sunao Hashimoto (https://github.com/kougaku)

import oculus.*;
import processing.video.*;

OculusRift oculus;
Capture cam;

void setup() {
  fullScreen(OculusRift.RENDERER);
  oculus = (OculusRift)g;
  oculus.enableHeadTracking();

  // capture setting
  cam = new Capture(this, 1024, 768);
  cam.start();
}

// Scene for OculusRift
void draw(int eye) {
  background(0);

  // camera image
  hint(DISABLE_DEPTH_TEST);
  pushMatrix();
  resetMatrix();
  translate( 0, 0, -400);
  imageMode(CENTER);
  image(cam, 0, 0);
  popMatrix();
  hint(ENABLE_DEPTH_TEST);

  // light
  fill(255);
  lights();

  // cube
  pushMatrix();
  translate(0, -500, -3000);
  fill(50, 200, 50);
  rotateX(millis()/1000.0);
  rotateY(millis()/900.0);
  box(500);
  popMatrix();
}

void keyPressed() {
  // Reset head state
  if (key==' ') {
    oculus.resetHeadState();
  }
} 

void captureEvent(Capture c) {
  c.read();
}
