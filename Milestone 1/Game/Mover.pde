PVector normalF= new PVector(0, 0, 0);
class Mover {
  PVector location;
  PVector velocity;
  Mover() {
    location = new PVector (width/2, (height-boxHeight)/2-radius, 0);
    velocity = new PVector(0, 0, 0);
  }
  void rolling() {
    PVector gravityForce=new PVector(sin(rotZ) * gravityConstant/200, 0, -sin(rotX) * gravityConstant/200);
    float normalForce = 1;
    float mu = 0.01;
    float frictionMagnitude = normalForce * mu;
    PVector friction = velocity.get();
    friction.mult(-1);
    friction.normalize();
    friction.mult(frictionMagnitude);
    velocity.add(gravityForce);
    velocity.add(friction);
    location.add(velocity);
  }
  void display() {
    pushMatrix();
    noStroke();
    fill(95, 83, 41);
    translate(location.x, location.y, location.z);
    sphere(radius);
    popMatrix();
  }
  void checkedges() {
    if (location.x >= boxLength/2) {
      velocity.x = velocity.x * -1;
      location.x=boxLength/2;
    }
    if (location.x<=-boxLength/2) {
      velocity.x = velocity.x * -1;
      location.x=-boxLength/2;
    }
    if ((location.z >= boxLength/2)) {
      velocity.z = velocity.z * -1;
      location.z=boxLength/2;
    }
    if (location.z <= -boxLength/2) {
      velocity.z = velocity.z * -1;
      location.z=-boxLength/2;
    }
  }

  void checkCylinderCollision() {

    for (int i=0; i<array.size (); i++) {
      PVector cylinderPosition =new PVector(array.get(i).x-width/2, location.y, array.get(i).y-height/2);

      if (cylinderPosition.dist(location)<(radius+cylinderBaseSize)) {
        PVector normal=cylinderPosition.get();
        normal.y=location.y;
        normal.sub(location);
        normal.normalize();
        float k= -2*normal.dot(velocity);
        velocity.add(PVector.mult(normal, k));
      }
    }
  }
}

