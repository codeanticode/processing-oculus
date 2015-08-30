import codeanticode.oculus.*;

void setup () {
  size(640, 360, Oculus.RENDERER);
  smooth(2);
}  
  
void draw() {
  background(120);
  translate(width/2, height/2);
  rotateX(frameCount * 0.01f);
  rotateY(frameCount * 0.01f);
  box(150);
}