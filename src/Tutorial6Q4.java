/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import javax.swing.JFrame;

public class Tutorial6Q4 implements GLEventListener {

    public static void main(String[] args) {
        final GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities capabilities = new GLCapabilities(profile);
        final GLCanvas glcanvas = new GLCanvas(capabilities);
        
        Tutorial6Q4 tree = new Tutorial6Q4();
        glcanvas.addGLEventListener(tree);
        glcanvas.setSize(400, 400);

        final JFrame frame = new JFrame("JOGL: Pine Tree");
        frame.getContentPane().add(glcanvas);
        frame.setSize(frame.getContentPane().getPreferredSize());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public void init(GLAutoDrawable drawable) {
        final GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor(0f, 0f, 0f, 1.0f); // black background
    }

    public void display(GLAutoDrawable drawable) {
        final GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);

        // Draw trunk (rectangle) - brown color
        gl.glColor3f(0.55f, 0.27f, 0.07f); // Brown
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex2f(-0.1f, -0.8f);  // bottom left
        gl.glVertex2f(0.1f, -0.8f);   // bottom right
        gl.glVertex2f(0.1f, -0.4f);   // top right
        gl.glVertex2f(-0.1f, -0.4f);  // top left
        gl.glEnd();

        // Draw leaves (3 green triangles stacked)
        gl.glColor3f(0.0f, 0.6f, 0.0f); // Dark Green

        // Bottom triangle
        gl.glBegin(GL2.GL_TRIANGLES);
        gl.glVertex2f(-0.5f, -0.4f);
        gl.glVertex2f(0.5f, -0.4f);
        gl.glVertex2f(0.0f, 0.0f);
        gl.glEnd();

        // Middle triangle
        gl.glBegin(GL2.GL_TRIANGLES);
        gl.glVertex2f(-0.4f, -0.1f);
        gl.glVertex2f(0.4f, -0.1f);
        gl.glVertex2f(0.0f, 0.3f);
        gl.glEnd();

        // Top triangle
        gl.glBegin(GL2.GL_TRIANGLES);
        gl.glVertex2f(-0.3f, 0.2f);
        gl.glVertex2f(0.3f, 0.2f);
        gl.glVertex2f(0.0f, 0.5f);
        gl.glEnd();
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        final GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
    }

    public void dispose(GLAutoDrawable drawable) {
        // Cleanup not needed here
    }
}

