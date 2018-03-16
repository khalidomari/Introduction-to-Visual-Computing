
void setup() {
  size(800, 800, P2D);
}
void draw() {
  background(255);
  My3DPoint eye = new My3DPoint(0, 0, -5000);
  My3DPoint origin = new My3DPoint(0, 0, 0); //The first vertex of your cuboid
  My3DBox input3DBox = new My3DBox(origin, 100, 150, 300);
  input3DBox= transformBox(input3DBox, scaleMatrix(1, TranslateY, 1));
  input3DBox= transformBox(input3DBox, rotateXMatrix(RotationXAngle));
  input3DBox= transformBox(input3DBox, rotateYMatrix(RotationYAngle));
  input3DBox= transformBox(input3DBox, translationMatrix(width/2, height/2, 0));
  projectBox(eye, input3DBox).render();
}

My2DBox projectBox (My3DPoint eye, My3DBox box) {

  My2DPoint[] projection = new My2DPoint[8];

  for (int i =0; i<box.p.length; i++) {
    projection[i] = projectPoint(eye, box.p[i]);
  }
  return new My2DBox(projection);
}
float TranslateY=1;
float pressedY;
float speed=0.01;
public void mousePressed(MouseEvent e) {
  pressedY = e.getY();
}
void mouseDragged(MouseEvent e) {
  noLoop();
  float y = e.getY();
  if (y < pressedY) {
    TranslateY -= speed;
  } else TranslateY += speed;
  redraw();
}
float RotationXAngle=0;
float RotationYAngle=0;

void keyPressed() {
  noLoop();
  if (keyCode == UP) {
    RotationXAngle = RotationXAngle +0.05;
  } 
  if (keyCode == DOWN) {
    RotationXAngle = RotationXAngle-0.05;
  }
  if (keyCode == LEFT) {
    RotationYAngle = RotationYAngle+0.05;
  } 
  if (keyCode == RIGHT) {
    RotationYAngle = RotationYAngle-0.05;
  } 
  redraw();
}


class My2DPoint {
  float x;
  float y;
  My2DPoint(float x, float y ) {
    this.x=x;
    this.y=y;
  }
}
class My3DPoint {
  float x;
  float y;
  float z;
  My3DPoint(float x, float y, float z) {
    this.x=x;
    this.y=y;
    this.z=z;
  }
}
My2DPoint projectPoint(My3DPoint eye, My3DPoint p) {
  return new My2DPoint(((p.x-eye.x)*eye.z)/(eye.z-p.z), 
  ((p.y-eye.y)*eye.z)/(eye.z-p.z)+TranslateY);
}
class My2DBox {
  My2DPoint[] s;
  My2DBox(My2DPoint[] s) {
    this.s = s;
  }
  void render() {
    line(s[0].x, s[0].y, s[3].x, s[3].y);
    line(s[3].x, s[3].y, s[7].x, s[7].y);
    line(s[7].x, s[7].y, s[4].x, s[4].y);
    line(s[4].x, s[4].y, s[0].x, s[0].y);

    line(s[1].x, s[1].y, s[5].x, s[5].y);
    line(s[5].x, s[5].y, s[6].x, s[6].y);
    line(s[6].x, s[6].y, s[2].x, s[2].y);
    line(s[2].x, s[2].y, s[1].x, s[1].y);

    line(s[0].x, s[0].y, s[1].x, s[1].y);
    line(s[3].x, s[3].y, s[2].x, s[2].y);
    line(s[7].x, s[7].y, s[6].x, s[6].y);
    line(s[4].x, s[4].y, s[5].x, s[5].y);
  }
}

class My3DBox {
  My3DPoint[] p;
  My3DBox(My3DPoint origin, float dimX, float dimY, float dimZ) {
    float x = origin.x;
    float y = origin.y;
    float z = origin.z;
    this.p = new My3DPoint[] {

      new My3DPoint(x, y+dimY, z+dimZ), 
      new My3DPoint(x, y, z+dimZ), 
      new My3DPoint(x+dimX, y, z+dimZ), 
      new My3DPoint(x+dimX, y+dimY, z+dimZ), 
      new My3DPoint(x, y+dimY, z), 
      origin, 
      new My3DPoint(x+dimX, y, z), 
      new My3DPoint(x+dimX, y+dimY, z)
    };
  }
  My3DBox(My3DPoint[] p) {
    this.p = p;
  }
}
float[] homogeneous3DPoint (My3DPoint p) {
  float[] result = {p.x, p.y, p.z, 1};
  return result;
}
float[][] rotateXMatrix(float angle) {
  return(new float[][] {
    {1, 0, 0, 0}, 
    {0, cos(angle), sin(angle), 0}, 
    {0, -sin(angle), cos(angle), 0},
    {0, 0, 0, 1}
  }
  );
}
float[][] rotateYMatrix(float angle) {
  return(new float[][] {
    { 
      cos(angle), 0, -sin(angle), 0
    }
    , {
      0, 1, 0, 0
    }
    , {
      sin(angle), 0, cos(angle), 0
    }
    , {
      0, 0, 0, 1
    }
  }
  );
}
float[][] rotateZMatrix(float angle) {
  return(new float[][] {
    { 
      cos(angle), sin(angle), 0, 0
    }
    , { 
      -sin(angle), cos(angle), 0, 0
    }
    , {
      0, 0, 1, 0
    }
    , {
      0, 0, 0, 1
    }
  }
  );
}
float[][] scaleMatrix(float x, float y, float z) {
  return(new float[][] {
    {
      x, 0, 0, 0
    }
    , {
      0, y, 0, 0
    }
    , {
      0, 0, z, 0
    }
    , {
      0, 0, 0, 1
    }
  }
  );
}
float[][] translationMatrix(float x, float y, float z) {
  return(new float[][] {
    {
      1, 0, 0, 0
    }
    , {
      0, 1, 0, 0
    }
    , {
      0, 0, 1, 0
    }
    , {
      x, y, z, 1
    }
  }
  );
}
float[] matrixProduct(float[][] a, float[] b) {
  float[] result= new float[a.length];
  for (int i = 0; i< a.length; i++) {
    for (int j = 0; j<b.length; j++ ) {
      result[i]+= a[j][i]*b[j];
    }
  }
  return result;
}

My3DBox transformBox(My3DBox box, float[][] transformMatrix) {

  My3DPoint[] points = new My3DPoint[box.p.length];
  float[] retour = new float[box.p.length];
  for (int i = 0; i< retour.length; i++) {
    float[] temp= matrixProduct(transformMatrix, new float[] {
      box.p[i].x, box.p[i].y, box.p[i].z, 1
    }
    );
    points[i]= new My3DPoint(temp[0], temp[1], temp[2]);
  }
  return new My3DBox(points);
}
My3DPoint euclidian3DPoint (float[] a) {
  My3DPoint result = new My3DPoint(a[0]/a[3], a[1]/a[3], a[2]/a[3]);
  return result;
}

