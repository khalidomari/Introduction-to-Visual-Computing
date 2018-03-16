final float boxLength=500, boxHeight=20, radius=20, gravityConstant= 9;
float speed=1;
final float MAX_SPEED=1.5, MIN_SPEED=0.2; //goal

Mover ball= new Mover();
boolean SHIFT_PRESSED=false;
PVector gravityForce=new PVector(0, gravityConstant, 0);
float rotX, rotZ;

//Obstacles
float cylinderBaseSize = 30;
float cylinderHeight = 70;
int cylinderResolution = 50;
float X=0, Y=0;
PShape Cylinder = new PShape();
PShape base = new PShape();
ArrayList<PVector> array=new ArrayList<PVector>();


void setup() {
  size(1000, 1000, P3D);
  stroke(0);
  fill(239, 195, 52);

  float angle;
  float[] x = new float[cylinderResolution + 1];
  float[] y = new float[cylinderResolution + 1];
  //get the x and y position on a circle for all the sides
  for (int i = 0; i < x.length; i++) {
    angle = (TWO_PI / cylinderResolution) * i;
    x[i] = sin(angle) * cylinderBaseSize;
    y[i] = cos(angle) * cylinderBaseSize;
  }
  Cylinder = createShape();
  Cylinder.beginShape(QUAD_STRIP);
  //draw the border of the cylinder
  for (int i = 0; i < x.length; i++) {
    Cylinder.vertex(x[i], y[i], 0);
    Cylinder.vertex(x[i], y[i], cylinderHeight);
  }
  Cylinder.endShape();

  //base
  base = createShape();
  base.beginShape(TRIANGLE_FAN);
  base.vertex(0, 0, cylinderHeight);
  for (int i = 0; i < x.length; i++) {
    base.vertex(x[i], y[i], cylinderHeight);
  }
  base.endShape();
}

void draw() {
  background(204, 207, 253);
  directionalLight(50, 100, 125, 0, -1, 0);
  ambientLight(102, 102, 102);
  lights();
  translate(width/2, height/2, 0);
  if (!SHIFT_PRESSED) {
    rotateZ(rotZ);
    rotateX(rotX);
    fill(239, 195, 52);
    box(boxLength, boxHeight, boxLength);
    for (int i=0; i<array.size (); i++) {
      drawShape(array.get(i));
    }
    ball.checkedges();
    ball.checkCylinderCollision();
    ball.rolling();
    ball.display();
  } else {

    rotateX(-PI/2);

    for (int i=0; i<array.size (); i++) {
      drawShape(array.get(i));
    }
    pushMatrix();
    fill(239, 195, 52);
    box(boxLength, boxHeight, boxLength);
    popMatrix();


    ball.display();
  }
}
float pressedY;
float currentZ;
float pressedX;
float currentX;
public void mousePressed(MouseEvent e) {
  pressedY = e.getY();
  pressedX= e.getX();
}
void mouseDragged(MouseEvent e) {
  currentX += map(mouseY, 0, height, PI/3 * speed, -PI/3 * speed) - map(pressedY, 0, height, PI/3 * speed, -PI/3 * speed);
  currentZ += map(mouseX, 0, width, -PI/3 * speed, PI/3 * speed)- map(pressedX, 0, width, -PI/3 * speed, PI/3 * speed);
  currentX = testAngle(currentX);
  currentZ = testAngle(currentZ);
  rotX = currentX;
  rotZ = currentZ;
  pressedY = e.getY();
  pressedX= e.getX();
}

float testAngle(float angle) {
  if (angle<-PI/3) return -PI/3;
  if (angle>PI/3) return PI/3;
  return angle;
}

void mouseWheel(MouseEvent event) {
  speed += 0.05*event.getCount();
  if (speed<MIN_SPEED) speed=MIN_SPEED;
  else if (speed>MAX_SPEED) speed=MAX_SPEED;
}

void keyPressed() {
  if (key == CODED) {
    if (keyCode==SHIFT) SHIFT_PRESSED=true;
  }
}

void keyReleased() {
  SHIFT_PRESSED=false;
}

void mouseClicked(MouseEvent e) {
  if (SHIFT_PRESSED) {
    if ( (e.getX() <= boxLength/2+width/2-cylinderBaseSize)&&(e.getX()>=-boxLength/2+width/2+cylinderBaseSize)
      && (e.getY() <= boxLength/2+ height/2-cylinderBaseSize) && (e.getY() >= -boxLength/2+ height/2+cylinderBaseSize) ) {
      PVector mouse=new PVector(e.getX()-width/2, ball.location.y, e.getY()-height/2);
      if (mouse.dist(ball.location) >= (radius+cylinderBaseSize)) {
        PVector position= new PVector(e.getX(), e.getY(), 0);
        if (checkCylinder(position)) array.add(new PVector(e.getX(), e.getY(), 0));
      }
    }
  }
}

//check for other cylinders position (Cylinder)
boolean checkCylinder(PVector p) {
  if (array.isEmpty())return true;
  PVector copy = p.get();
  for (int i = 0; i<array.size (); i++) {
    copy.z=array.get(i).z;
    if (array.get(i).dist(copy)< 2*cylinderBaseSize) return false;
  }
  return true;
}

void drawShape(PVector p) {
  pushMatrix();
  rotateX(PI/2);
  translate(p.x-width/2, p.y-height/2, p.z);
  shape(Cylinder);
  shape(base);
  popMatrix();
}

