# Introduction to Visual Computing

## Milestone 1:

A Processing sketch named Game.pde displays a 3D ’board’ at the center of the screen, with a ball (3D
sphere) on it.
* Mouse drag tilts a board around the X and Z axis.
* Mouse wheel increases/decreases the tilt motion speed.
* When the board is tilted, the ball moves according to the gravity and friction (gravity points toward +Y).
* By pressing the Shift key, a top view of the board is displayed (object placement mode). In this mode, a
click on the board surface adds a new cylinder at click’s location.
* These cylinders remain on the board when the Shift key is released, and move with the board when it is
tilted with the mouse.
* The ball collides with the cylinders and board’s edges. The correct collision distance is computed.
* When colliding with a cylinder or hitting the edges of the board, the ball makes a realistic bounce (by
realistic we mean: correct bounce direction + parabolic motion)

## Milestone 2: 3D Model for printing

* A rocket 3D Model (.blend), without holes on the surface, watertight, thickness more than 2mm and all averhangs less than 45◦.
* Can hold on a regular Lego board.

![picture alt](https://github.com/khalidomari/Introduction-to-Visual-Computing/blob/master/Milestone%202/rocket.jpg)


## Milestone 3:

* The result of the Sobel edge detector
* The Hough accumulator
* The four corners of the Lego board

# Milestone 4:

When running the TangibleGame applet, it should show a plate at the center of the screen, with the sphere
on the center of it. The plate should tilt according to the movement of the board that your code reads from the
video, and the sphere roles on it naturally. Show the video on a corner of the displaying window. Also we must
be able to add cylinders (or trees) on the plate at run time.
