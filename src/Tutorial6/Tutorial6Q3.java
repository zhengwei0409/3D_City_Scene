/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Tutorial6;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import javax.swing.JFrame;

public class Tutorial6Q3 implements GLEventListener {

    // Main method to run the program
    public static void main(String[] args) {
        // Step 1: Create a GLProfile, which defines the version of OpenGL to use
        final GLProfile profile = GLProfile.get(GLProfile.GL2);
        
        // Step 2: Setup OpenGL capabilities
        GLCapabilities capabilities = new GLCapabilities(profile);
        
        // Step 3: Create a canvas where OpenGL will draw
        final GLCanvas glcanvas = new GLCanvas(capabilities);
        
        // Step 4: Create an instance of our class and add it as a listener
        Tutorial6Q3 tutorial = new Tutorial6Q3();
        glcanvas.addGLEventListener(tutorial);
        glcanvas.setSize(400, 400); // Set the window size

        // Step 5: Create a JFrame (window) to display everything
        final JFrame frame = new JFrame("JOGL: Red Triangle and White Rectangle");
        frame.getContentPane().add(glcanvas);
        frame.setSize(frame.getContentPane().getPreferredSize());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    // This method is called when the canvas is created
    public void init(GLAutoDrawable drawable) {
        final GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor(0f, 0f, 0f, 1.0f); // Set background color to black
    }

    // This method is called to draw on the canvas
    public void display(GLAutoDrawable drawable) {
        final GL2 gl = drawable.getGL().getGL2();

        // Clear the screen
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);

        // Draw red triangle
        gl.glColor3f(1.0f, 0.0f, 0.0f); // Red color
        gl.glBegin(GL2.GL_TRIANGLES); // Start drawing triangle
        gl.glVertex2f(-0.8f, -0.5f); // Left bottom
        gl.glVertex2f(-0.2f, -0.5f); // Right bottom
        gl.glVertex2f(-0.5f, 0.2f);  // Top
        gl.glEnd();

        // Draw white rectangle
        gl.glColor3f(1.0f, 1.0f, 1.0f); // White color
        gl.glBegin(GL2.GL_QUADS); // Start drawing rectangle
        gl.glVertex2f(0.2f, -0.2f);  // Bottom left
        gl.glVertex2f(0.8f, -0.2f);  // Bottom right
        gl.glVertex2f(0.8f, 0.4f);   // Top right
        gl.glVertex2f(0.2f, 0.4f);   // Top left
        gl.glEnd();
    }

    // This method is called when the window is resized
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        final GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
    }

    // This method is called when the canvas is being destroyed
    public void dispose(GLAutoDrawable drawable) {
        // No cleanup needed for this simple example
    }
}

