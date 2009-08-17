Gypsum 0.5
----------
(c) 2009 Dan Lidral-Porter, all rights reserved.


ABOUT
-----
Gypsum allows you to seamlessly integrate projected images with chalkboard lectures. All you need is a computer, a webcam, and a projector. Point the webcam and the projector at the blackboard, and then launch Gypsum, which will walk you through setting up the images. During the lecture, just draw a rectangle on the board, and then label it with a shape that has the same number of holes as the index of the image you want to project. For example, drawing a square above the rectangle will project the first image, putting a line through the square (dividing it in two) will project the second, etc.

INSTALLATION
------------
OS X
Double click the OpenCV installer package to install the OpenCV framework, then drag Gypsum to your Applications folder.

Linux
First, you'll need to install the OpenCV framework. If your package manager doesn't have it, you can compile it from the latest opencv*-*.tar.gz at http://sourceforge.net/projects/opencvlibrary/files/. Then, you'll need to drag the libOpenCV.so file to somewhere in your Java Library Path. Finally, you should make sure that Gypsum.jar is run with a 32-bit VM, since the Java bindings for OpenCV are 32-bit, and cannot be loaded from a 64-bit VM.

TIPS
----
Use the thickest chalk you can get.
If your rectangles are not being recognized, make sure your corners meet, and that the sides don't extend beyond the corners.
If your rectangles stop being recognized after the label is drawn, erase the label and draw it slightly farther away from the rectangle (a good two inches or so).
You can create .lec files by putting images named "1", "2", etc., into a folder, zipping it, and then changing the file extension.

LICENSE
-------
See License.txt.