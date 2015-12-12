// It shows a spherical panorama view. 
// The included picture was taken by RICOH THETA (https://theta360.com/en/)
// Author: Sunao Hashimoto (https://github.com/kougaku)

import oculus.*;

OculusRift oculus;
PanoramaSphere psphere;
PImage img;

void setup() {
  fullScreen(OculusRift.RENDERER);
  oculus = (OculusRift)g;
  oculus.enableHeadTracking();

  psphere = new PanoramaSphere(100);
  img = loadImage("panorama.jpg");
}

// Draw the scene for each eye
void draw(int eye) {
  rotateZ(radians(-90)); // this corrects the angle of the view
  psphere.draw(1000, img);
}

void keyPressed() {
  // Reset head state
  if (key==' ') {
    oculus.resetHeadState();
  }
} 