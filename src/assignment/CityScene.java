/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package assignment;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.awt.image.BufferedImage;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.awt.image.BufferedImage;

public class CityScene implements GLEventListener, KeyListener {
    private GLU glu = new GLU();
    private float cameraX = 0.0f, cameraY = 5.0f, cameraZ = 15.0f;
    private float rotateX = -20.0f, rotateY = 0.0f;
    private static final int GRID_SIZE = 6; // 6x6 grid of buildings
    private float time = 0.0f; // For animations
    
    // Building properties
    private static final float BUILDING_SIZE = 2.0f;
    private static final float ROAD_WIDTH = 1.5f;
    private static final float SPACING = BUILDING_SIZE + ROAD_WIDTH;
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("JOGL City Scene");
        GLProfile profile = GLProfile.getDefault();
        GLCapabilities capabilities = new GLCapabilities(profile);
        capabilities.setDoubleBuffered(true);
        capabilities.setHardwareAccelerated(true);
        
        GLCanvas canvas = new GLCanvas(capabilities);
        CityScene scene = new CityScene();
        canvas.addGLEventListener(scene);
        canvas.addKeyListener(scene);
        canvas.setFocusable(true);
        
        frame.add(canvas);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        
        FPSAnimator animator = new FPSAnimator(canvas, 60);
        animator.start();
    }
    
    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        
        // Enable depth testing
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LEQUAL);
        
        // Enable lighting
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);
        
        // Set up light
        float[] lightPos = {5.0f, 10.0f, 5.0f, 1.0f};
        float[] lightColor = {1.0f, 1.0f, 1.0f, 1.0f};
        float[] ambient = {0.3f, 0.3f, 0.3f, 1.0f};
        
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightColor, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, ambient, 0);
        
        // Enable color material
        gl.glEnable(GL2.GL_COLOR_MATERIAL);
        gl.glColorMaterial(GL.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE);
        
        // Set clear color to sky blue
        gl.glClearColor(0.5f, 0.8f, 1.0f, 1.0f);
    }
    
    @Override
    public void display(GLAutoDrawable drawable) {
            GL2 gl = drawable.getGL().getGL2();

            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
            gl.glLoadIdentity();

            // Set up camera
            gl.glTranslatef(-cameraX, -cameraY, -cameraZ);
            gl.glRotatef(rotateX, 1.0f, 0.0f, 0.0f);
            gl.glRotatef(rotateY, 0.0f, 1.0f, 0.0f);

            // Draw ground plane
            drawEnhancedGround(gl);

            // Draw roads
            drawRoads(gl);

            // Draw buildings
            drawBuildingGrid(gl);

            // Draw vegetation
            drawTrees(gl);
            drawBrushes(gl); // ADD THIS LINE

            // Draw other elements
            drawCars(gl);
            drawStreetLights(gl);

            time += 0.016f;
    }
    
    private void drawBuildingGrid(GL2 gl) {
    // Draw one building of each type in fixed positions
    
    // Residential building (top-left quadrant)
    drawResidentialBuilding(gl, -3.0f, 0, -3.0f, 3.5f);
    
    // Office building (top-right quadrant)
    drawOfficeBuilding(gl, 3.0f, 0, -3.0f, 4.0f);
    
    // Skyscraper (bottom-left quadrant)
    drawSkyscraper(gl, -3.0f, 0, 3.0f, 6.0f);
    
    // Shop building (bottom-right quadrant)
    drawShopBuilding(gl, 3.0f, 0, 3.0f, 2.5f);
}

// 3. NEW BUILDING TYPES - ADD THESE METHODS
private void drawResidentialBuilding(GL2 gl, float x, float y, float z, float height) {
    gl.glPushMatrix();
    gl.glTranslatef(x, y, z);
    
    // Main building - brick red
    gl.glColor3f(0.7f, 0.3f, 0.2f);
    drawBuildingBody(gl, BUILDING_SIZE, height, BUILDING_SIZE);
    
    // Balconies
    gl.glColor3f(0.8f, 0.8f, 0.8f);
    for (float h = 0.8f; h < height; h += 0.8f) {
        drawBalcony(gl, h);
    }
    
    // Peaked roof
    gl.glColor3f(0.4f, 0.2f, 0.1f);
    drawPeakedRoof(gl, BUILDING_SIZE, height, BUILDING_SIZE);
    
    gl.glPopMatrix();
}

private void drawOfficeBuilding(GL2 gl, float x, float y, float z, float height) {
    gl.glPushMatrix();
    gl.glTranslatef(x, y, z);
    
    // Glass building - blue tinted
    gl.glColor3f(0.3f, 0.4f, 0.6f);
    drawBuildingBody(gl, BUILDING_SIZE, height, BUILDING_SIZE);
    
    // Lots of windows
    gl.glColor3f(0.8f, 0.9f, 1.0f);
    drawOfficeWindows(gl, BUILDING_SIZE, height, BUILDING_SIZE);
    
    // Flat roof with AC units
    gl.glColor3f(0.5f, 0.5f, 0.5f);
    drawFlatRoof(gl, BUILDING_SIZE, height, BUILDING_SIZE);
    drawACUnits(gl, height);
    
    gl.glPopMatrix();
}

private void drawSkyscraper(GL2 gl, float x, float y, float z, float height) {
    gl.glPushMatrix();
    gl.glTranslatef(x, y, z);
    
    // Tall modern building - dark gray
    gl.glColor3f(0.3f, 0.3f, 0.4f);
    drawBuildingBody(gl, BUILDING_SIZE * 0.8f, height, BUILDING_SIZE * 0.8f);
    
    // Antenna on top
    gl.glColor3f(0.8f, 0.1f, 0.1f);
    drawAntenna(gl, height);
    
    // Modern grid windows
    gl.glColor3f(0.9f, 0.9f, 0.7f);
    drawGridWindows(gl, BUILDING_SIZE * 0.8f, height, BUILDING_SIZE * 0.8f);
    
    gl.glPopMatrix();
}

private void drawShopBuilding(GL2 gl, float x, float y, float z, float height) {
    gl.glPushMatrix();
    gl.glTranslatef(x, y, z);
    
    // Colorful shop - random bright colors
    // Deterministic colors based on position
    int seed = (int)(x * 100 + z * 50);
    float r = 0.6f + (seed % 40) / 100.0f;
    float g = 0.6f + ((seed * 7) % 40) / 100.0f;
    float b = 0.6f + ((seed * 13) % 40) / 100.0f;
    gl.glColor3f(r, g, b);
    
    drawBuildingBody(gl, BUILDING_SIZE, height, BUILDING_SIZE);
    
    // Shop sign
    gl.glColor3f(1.0f, 1.0f, 1.0f);
    drawShopSign(gl, height);
    
    gl.glPopMatrix();
}

// 4. HELPER METHODS FOR BUILDING DETAILS
private void drawBalcony(GL2 gl, float height) {
    gl.glBegin(GL2.GL_QUADS);
    gl.glNormal3f(0.0f, 1.0f, 0.0f);
    gl.glVertex3f(-BUILDING_SIZE/2 - 0.1f, height, BUILDING_SIZE/2);
    gl.glVertex3f(BUILDING_SIZE/2 + 0.1f, height, BUILDING_SIZE/2);
    gl.glVertex3f(BUILDING_SIZE/2 + 0.1f, height, BUILDING_SIZE/2 + 0.1f);
    gl.glVertex3f(-BUILDING_SIZE/2 - 0.1f, height, BUILDING_SIZE/2 + 0.1f);
    gl.glEnd();
}

private void drawPeakedRoof(GL2 gl, float width, float height, float depth) {
    gl.glBegin(GL2.GL_TRIANGLES);
    // Front triangle
    gl.glNormal3f(0.0f, 0.5f, 0.5f);
    gl.glVertex3f(-width/2, height, depth/2);
    gl.glVertex3f(width/2, height, depth/2);
    gl.glVertex3f(0.0f, height + 0.5f, 0.0f);
    
    // Back triangle
    gl.glNormal3f(0.0f, 0.5f, -0.5f);
    gl.glVertex3f(-width/2, height, -depth/2);
    gl.glVertex3f(0.0f, height + 0.5f, 0.0f);
    gl.glVertex3f(width/2, height, -depth/2);
    gl.glEnd();
    
    // Roof sides
    gl.glBegin(GL2.GL_QUADS);
    gl.glNormal3f(-0.5f, 0.5f, 0.0f);
    gl.glVertex3f(-width/2, height, -depth/2);
    gl.glVertex3f(-width/2, height, depth/2);
    gl.glVertex3f(0.0f, height + 0.5f, 0.0f);
    gl.glVertex3f(0.0f, height + 0.5f, 0.0f);
    
    gl.glNormal3f(0.5f, 0.5f, 0.0f);
    gl.glVertex3f(width/2, height, -depth/2);
    gl.glVertex3f(0.0f, height + 0.5f, 0.0f);
    gl.glVertex3f(0.0f, height + 0.5f, 0.0f);
    gl.glVertex3f(width/2, height, depth/2);
    gl.glEnd();
}

private void drawAntenna(GL2 gl, float buildingHeight) {
    gl.glBegin(GL.GL_LINES);
    gl.glVertex3f(0.0f, buildingHeight, 0.0f);
    gl.glVertex3f(0.0f, buildingHeight + 1.0f, 0.0f);
    gl.glEnd();
    
    // Blinking light
    if ((int)(time * 3) % 2 == 0) {
        gl.glPointSize(5.0f);
        gl.glBegin(GL.GL_POINTS);
        gl.glVertex3f(0.0f, buildingHeight + 1.0f, 0.0f);
        gl.glEnd();
    }
}

private void drawACUnits(GL2 gl, float height) {
    gl.glColor3f(0.7f, 0.7f, 0.7f);
    for (int i = 0; i < 3; i++) {
        gl.glPushMatrix();
        gl.glTranslatef((i-1) * 0.5f, height + 0.1f, 0.0f);
        gl.glScalef(0.2f, 0.1f, 0.3f);
        drawCube(gl);
        gl.glPopMatrix();
    }
}

private void drawShopSign(GL2 gl, float height) {
    gl.glBegin(GL2.GL_QUADS);
    gl.glNormal3f(0.0f, 0.0f, 1.0f);
    gl.glVertex3f(-0.8f, height * 0.8f, BUILDING_SIZE/2 + 0.01f);
    gl.glVertex3f(0.8f, height * 0.8f, BUILDING_SIZE/2 + 0.01f);
    gl.glVertex3f(0.8f, height * 0.9f, BUILDING_SIZE/2 + 0.01f);
    gl.glVertex3f(-0.8f, height * 0.9f, BUILDING_SIZE/2 + 0.01f);
    gl.glEnd();
}

// 5. ENHANCED STREET ELEMENTS
private void drawStreetLights(GL2 gl) {
    // Place street lights at intersections and along roads
    float[] positions = {
        -SPACING, 0, 0,    SPACING, 0, 0,    // Horizontal road
        0, 0, -SPACING,    0, 0, SPACING     // Vertical road
    };
    
    for (int i = 0; i < positions.length; i += 3) {
        drawStreetLight(gl, positions[i], positions[i+1], positions[i+2]);
    }
}

private void drawStreetLight(GL2 gl, float x, float y, float z) {
    gl.glPushMatrix();
    gl.glTranslatef(x, y, z);
    
    // Pole
    gl.glColor3f(0.3f, 0.3f, 0.3f);
    gl.glBegin(GL2.GL_QUADS);
    gl.glVertex3f(-0.03f, 0.0f, -0.03f);
    gl.glVertex3f(0.03f, 0.0f, -0.03f);
    gl.glVertex3f(0.03f, 2.5f, -0.03f);
    gl.glVertex3f(-0.03f, 2.5f, -0.03f);
    gl.glEnd();
    
    // Light (brighter at night)
    gl.glColor3f(1.0f, 1.0f, 0.8f);
    gl.glPushMatrix();
    gl.glTranslatef(0.0f, 2.3f, 0.0f);
    gl.glScalef(0.1f, 0.1f, 0.1f);
    drawCube(gl);
    gl.glPopMatrix();
    
    gl.glPopMatrix();
}

// 6. ENHANCED GROUND WITH SIDEWALKS
private void drawEnhancedGround(GL2 gl) {
    // Grass areas
    gl.glColor3f(0.3f, 0.5f, 0.3f);
    gl.glBegin(GL2.GL_QUADS);
    gl.glNormal3f(0.0f, 1.0f, 0.0f);
    gl.glVertex3f(-10.0f, 0.0f, -10.0f);
    gl.glVertex3f(10.0f, 0.0f, -10.0f);
    gl.glVertex3f(10.0f, 0.0f, 10.0f);
    gl.glVertex3f(-10.0f, 0.0f, 10.0f);
    gl.glEnd();
    
    // Sidewalks
    gl.glColor3f(0.6f, 0.6f, 0.6f);
    drawSidewalks(gl);
}

private void drawOfficeWindows(GL2 gl, float width, float height, float depth) {
    float windowSize = 0.12f;
    float windowSpacing = 0.25f;
    
    // Front face windows - more systematic grid
    for (float y = 0.3f; y < height - 0.2f; y += windowSpacing) {
        for (float x = -width/2 + 0.15f; x < width/2 - 0.1f; x += windowSpacing) {
            gl.glBegin(GL2.GL_QUADS);
            gl.glNormal3f(0.0f, 0.0f, 1.0f);
            gl.glVertex3f(x - windowSize/2, y - windowSize/2, depth/2 + 0.01f);
            gl.glVertex3f(x + windowSize/2, y - windowSize/2, depth/2 + 0.01f);
            gl.glVertex3f(x + windowSize/2, y + windowSize/2, depth/2 + 0.01f);
            gl.glVertex3f(x - windowSize/2, y + windowSize/2, depth/2 + 0.01f);
            gl.glEnd();
        }
    }
    
    // Back face windows
    for (float y = 0.3f; y < height - 0.2f; y += windowSpacing) {
        for (float x = -width/2 + 0.15f; x < width/2 - 0.1f; x += windowSpacing) {
            gl.glBegin(GL2.GL_QUADS);
            gl.glNormal3f(0.0f, 0.0f, -1.0f);
            gl.glVertex3f(x - windowSize/2, y - windowSize/2, -depth/2 - 0.01f);
            gl.glVertex3f(x - windowSize/2, y + windowSize/2, -depth/2 - 0.01f);
            gl.glVertex3f(x + windowSize/2, y + windowSize/2, -depth/2 - 0.01f);
            gl.glVertex3f(x + windowSize/2, y - windowSize/2, -depth/2 - 0.01f);
            gl.glEnd();
        }
    }
}

private void drawFlatRoof(GL2 gl, float width, float height, float depth) {
    // Main roof surface
    gl.glBegin(GL2.GL_QUADS);
    gl.glNormal3f(0.0f, 1.0f, 0.0f);
    gl.glVertex3f(-width/2, height, -depth/2);
    gl.glVertex3f(width/2, height, -depth/2);
    gl.glVertex3f(width/2, height, depth/2);
    gl.glVertex3f(-width/2, height, depth/2);
    gl.glEnd();
    
    // Roof edge/parapet
    gl.glColor3f(0.4f, 0.4f, 0.4f);
    float parapetHeight = 0.1f;
    
    // Front edge
    gl.glBegin(GL2.GL_QUADS);
    gl.glNormal3f(0.0f, 0.0f, 1.0f);
    gl.glVertex3f(-width/2, height, depth/2);
    gl.glVertex3f(width/2, height, depth/2);
    gl.glVertex3f(width/2, height + parapetHeight, depth/2);
    gl.glVertex3f(-width/2, height + parapetHeight, depth/2);
    gl.glEnd();
    
    // Back edge
    gl.glBegin(GL2.GL_QUADS);
    gl.glNormal3f(0.0f, 0.0f, -1.0f);
    gl.glVertex3f(-width/2, height, -depth/2);
    gl.glVertex3f(-width/2, height + parapetHeight, -depth/2);
    gl.glVertex3f(width/2, height + parapetHeight, -depth/2);
    gl.glVertex3f(width/2, height, -depth/2);
    gl.glEnd();
}

private void drawGridWindows(GL2 gl, float width, float height, float depth) {
    float windowWidth = 0.15f;
    float windowHeight = 0.3f;
    float spacingX = 0.2f;
    float spacingY = 0.4f;
    
    // Front face - large rectangular windows
    for (float y = 0.5f; y < height - 0.3f; y += spacingY) {
        for (float x = -width/2 + 0.2f; x < width/2 - 0.15f; x += spacingX) {
            gl.glBegin(GL2.GL_QUADS);
            gl.glNormal3f(0.0f, 0.0f, 1.0f);
            gl.glVertex3f(x - windowWidth/2, y - windowHeight/2, depth/2 + 0.01f);
            gl.glVertex3f(x + windowWidth/2, y - windowHeight/2, depth/2 + 0.01f);
            gl.glVertex3f(x + windowWidth/2, y + windowHeight/2, depth/2 + 0.01f);
            gl.glVertex3f(x - windowWidth/2, y + windowHeight/2, depth/2 + 0.01f);
            gl.glEnd();
        }
    }
    
    // Side faces
    for (float y = 0.5f; y < height - 0.3f; y += spacingY) {
        for (float z = -depth/2 + 0.2f; z < depth/2 - 0.15f; z += spacingX) {
            // Left side
            gl.glBegin(GL2.GL_QUADS);
            gl.glNormal3f(-1.0f, 0.0f, 0.0f);
            gl.glVertex3f(-width/2 - 0.01f, y - windowHeight/2, z - windowWidth/2);
            gl.glVertex3f(-width/2 - 0.01f, y - windowHeight/2, z + windowWidth/2);
            gl.glVertex3f(-width/2 - 0.01f, y + windowHeight/2, z + windowWidth/2);
            gl.glVertex3f(-width/2 - 0.01f, y + windowHeight/2, z - windowWidth/2);
            gl.glEnd();
            
            // Right side
            gl.glBegin(GL2.GL_QUADS);
            gl.glNormal3f(1.0f, 0.0f, 0.0f);
            gl.glVertex3f(width/2 + 0.01f, y - windowHeight/2, z - windowWidth/2);
            gl.glVertex3f(width/2 + 0.01f, y + windowHeight/2, z - windowWidth/2);
            gl.glVertex3f(width/2 + 0.01f, y + windowHeight/2, z + windowWidth/2);
            gl.glVertex3f(width/2 + 0.01f, y - windowHeight/2, z + windowWidth/2);
            gl.glEnd();
        }
    }
}

private void drawSidewalks(GL2 gl) {
    float sidewalkWidth = 0.3f;
    
    // Horizontal sidewalks
    for (float z = -ROAD_WIDTH/2 - sidewalkWidth; z <= ROAD_WIDTH/2 + sidewalkWidth; z += ROAD_WIDTH + sidewalkWidth) {
        gl.glBegin(GL2.GL_QUADS);
        gl.glNormal3f(0.0f, 1.0f, 0.0f);
        gl.glVertex3f(-10.0f, 0.005f, z);
        gl.glVertex3f(10.0f, 0.005f, z);
        gl.glVertex3f(10.0f, 0.005f, z + sidewalkWidth);
        gl.glVertex3f(-10.0f, 0.005f, z + sidewalkWidth);
        gl.glEnd();
    }
    
    // Vertical sidewalks
    for (float x = -ROAD_WIDTH/2 - sidewalkWidth; x <= ROAD_WIDTH/2 + sidewalkWidth; x += ROAD_WIDTH + sidewalkWidth) {
        gl.glBegin(GL2.GL_QUADS);
        gl.glNormal3f(0.0f, 1.0f, 0.0f);
        gl.glVertex3f(x, 0.005f, -10.0f);
        gl.glVertex3f(x + sidewalkWidth, 0.005f, -10.0f);
        gl.glVertex3f(x + sidewalkWidth, 0.005f, 10.0f);
        gl.glVertex3f(x, 0.005f, 10.0f);
        gl.glEnd();
    }
}
    
    private void drawGround(GL2 gl) {
        gl.glColor3f(0.4f, 0.6f, 0.4f); // Green ground
        gl.glBegin(GL2.GL_QUADS);
        gl.glNormal3f(0.0f, 1.0f, 0.0f);
        gl.glVertex3f(-10.0f, 0.0f, -10.0f);
        gl.glVertex3f(10.0f, 0.0f, -10.0f);
        gl.glVertex3f(10.0f, 0.0f, 10.0f);
        gl.glVertex3f(-10.0f, 0.0f, 10.0f);
        gl.glEnd();
    }
    
    private void drawRoads(GL2 gl) {
        gl.glColor3f(0.3f, 0.3f, 0.3f); // Dark gray roads
        
        // Horizontal road
        gl.glBegin(GL2.GL_QUADS);
        gl.glNormal3f(0.0f, 1.0f, 0.0f);
        gl.glVertex3f(-10.0f, 0.01f, -ROAD_WIDTH/2);
        gl.glVertex3f(10.0f, 0.01f, -ROAD_WIDTH/2);
        gl.glVertex3f(10.0f, 0.01f, ROAD_WIDTH/2);
        gl.glVertex3f(-10.0f, 0.01f, ROAD_WIDTH/2);
        gl.glEnd();
        
        // Vertical road
        gl.glBegin(GL2.GL_QUADS);
        gl.glNormal3f(0.0f, 1.0f, 0.0f);
        gl.glVertex3f(-ROAD_WIDTH/2, 0.01f, -10.0f);
        gl.glVertex3f(ROAD_WIDTH/2, 0.01f, -10.0f);
        gl.glVertex3f(ROAD_WIDTH/2, 0.01f, 10.0f);
        gl.glVertex3f(-ROAD_WIDTH/2, 0.01f, 10.0f);
        gl.glEnd();
        
        // Road markings
        gl.glColor3f(1.0f, 1.0f, 0.0f); // Yellow lines
        drawRoadMarkings(gl);
    }
    
    private void drawRoadMarkings(GL2 gl) {
        gl.glLineWidth(3.0f);
        gl.glBegin(GL.GL_LINES);
        
        // Horizontal road markings
        for (float x = -8.0f; x <= 8.0f; x += 1.0f) {
            gl.glVertex3f(x, 0.02f, -0.1f);
            gl.glVertex3f(x + 0.5f, 0.02f, -0.1f);
            gl.glVertex3f(x, 0.02f, 0.1f);
            gl.glVertex3f(x + 0.5f, 0.02f, 0.1f);
        }
        
        // Vertical road markings
        for (float z = -8.0f; z <= 8.0f; z += 1.0f) {
            gl.glVertex3f(-0.1f, 0.02f, z);
            gl.glVertex3f(-0.1f, 0.02f, z + 0.5f);
            gl.glVertex3f(0.1f, 0.02f, z);
            gl.glVertex3f(0.1f, 0.02f, z + 0.5f);
        }
        
        gl.glEnd();
    }
    
    private void drawBuilding(GL2 gl, float x, float y, float z, float r, float g, float b, float height) {
        gl.glPushMatrix();
        gl.glTranslatef(x, y, z);
        
        // Main building body
        gl.glColor3f(r, g, b);
        drawBuildingBody(gl, BUILDING_SIZE, height, BUILDING_SIZE);
        
        // Windows
        gl.glColor3f(0.7f, 0.9f, 1.0f); // Light blue windows
        drawWindows(gl, BUILDING_SIZE, height, BUILDING_SIZE);
        
        // Roof
        gl.glColor3f(0.5f, 0.3f, 0.2f); // Brown roof
        drawRoof(gl, BUILDING_SIZE, height, BUILDING_SIZE);
        
        gl.glPopMatrix();
    }
    
    private void drawBuildingBody(GL2 gl, float width, float height, float depth) {
        gl.glBegin(GL2.GL_QUADS);
        
        // Front face
        gl.glNormal3f(0.0f, 0.0f, 1.0f);
        gl.glVertex3f(-width/2, 0.0f, depth/2);
        gl.glVertex3f(width/2, 0.0f, depth/2);
        gl.glVertex3f(width/2, height, depth/2);
        gl.glVertex3f(-width/2, height, depth/2);
        
        // Back face
        gl.glNormal3f(0.0f, 0.0f, -1.0f);
        gl.glVertex3f(-width/2, 0.0f, -depth/2);
        gl.glVertex3f(-width/2, height, -depth/2);
        gl.glVertex3f(width/2, height, -depth/2);
        gl.glVertex3f(width/2, 0.0f, -depth/2);
        
        // Left face
        gl.glNormal3f(-1.0f, 0.0f, 0.0f);
        gl.glVertex3f(-width/2, 0.0f, -depth/2);
        gl.glVertex3f(-width/2, 0.0f, depth/2);
        gl.glVertex3f(-width/2, height, depth/2);
        gl.glVertex3f(-width/2, height, -depth/2);
        
        // Right face
        gl.glNormal3f(1.0f, 0.0f, 0.0f);
        gl.glVertex3f(width/2, 0.0f, -depth/2);
        gl.glVertex3f(width/2, height, -depth/2);
        gl.glVertex3f(width/2, height, depth/2);
        gl.glVertex3f(width/2, 0.0f, depth/2);
        
        gl.glEnd();
    }
    
    private void drawWindows(GL2 gl, float width, float height, float depth) {
        float windowSize = 0.15f;
        float windowSpacing = 0.4f;
        
        // Front face windows
        for (float y = 0.5f; y < height - 0.3f; y += windowSpacing) {
            for (float x = -width/2 + 0.3f; x < width/2 - 0.2f; x += windowSpacing) {
                gl.glBegin(GL2.GL_QUADS);
                gl.glNormal3f(0.0f, 0.0f, 1.0f);
                gl.glVertex3f(x - windowSize/2, y - windowSize/2, depth/2 + 0.01f);
                gl.glVertex3f(x + windowSize/2, y - windowSize/2, depth/2 + 0.01f);
                gl.glVertex3f(x + windowSize/2, y + windowSize/2, depth/2 + 0.01f);
                gl.glVertex3f(x - windowSize/2, y + windowSize/2, depth/2 + 0.01f);
                gl.glEnd();
            }
        }
        
        // Repeat for other faces...
        // (Similar code for back, left, right faces)
    }
    
    private void drawRoof(GL2 gl, float width, float height, float depth) {
        gl.glBegin(GL2.GL_QUADS);
        gl.glNormal3f(0.0f, 1.0f, 0.0f);
        gl.glVertex3f(-width/2 - 0.1f, height, -depth/2 - 0.1f);
        gl.glVertex3f(width/2 + 0.1f, height, -depth/2 - 0.1f);
        gl.glVertex3f(width/2 + 0.1f, height, depth/2 + 0.1f);
        gl.glVertex3f(-width/2 - 0.1f, height, depth/2 + 0.1f);
        gl.glEnd();
    }
    
    private void drawTrees(GL2 gl) {
            // Trees around buildings
        drawTree(gl, -4.5f, 0.0f, -4.5f, 1.2f); // Near residential
        drawTree(gl, -1.5f, 0.0f, -4.5f, 1.0f);
        drawTree(gl, 4.5f, 0.0f, -1.5f, 1.3f);  // Near office
        drawTree(gl, 1.5f, 0.0f, -4.5f, 0.9f);
        drawTree(gl, -4.5f, 0.0f, 1.5f, 1.1f);  // Near skyscraper
        drawTree(gl, -1.5f, 0.0f, 4.5f, 1.4f);
        drawTree(gl, 4.5f, 0.0f, 4.5f, 1.0f);   // Near shop
        drawTree(gl, 1.5f, 0.0f, 1.5f, 1.2f);

        // Trees along roads
        drawTree(gl, -6.0f, 0.0f, 0.8f, 1.1f);
        drawTree(gl, 6.0f, 0.0f, -0.8f, 1.0f);
        drawTree(gl, 0.8f, 0.0f, -6.0f, 1.3f);
        drawTree(gl, -0.8f, 0.0f, 6.0f, 1.2f);
    }
    
    private void drawTree(GL2 gl, float x, float y, float z, float height) {
    gl.glPushMatrix();
    gl.glTranslatef(x, y, z);
    
    // Tree trunk
    gl.glColor3f(0.4f, 0.2f, 0.1f);
    gl.glBegin(GL2.GL_QUADS);
    float trunkHeight = height * 0.6f;
    gl.glVertex3f(-0.05f, 0.0f, -0.05f);
    gl.glVertex3f(0.05f, 0.0f, -0.05f);
    gl.glVertex3f(0.05f, trunkHeight, -0.05f);
    gl.glVertex3f(-0.05f, trunkHeight, -0.05f);
    
    gl.glVertex3f(-0.05f, 0.0f, 0.05f);
    gl.glVertex3f(-0.05f, trunkHeight, 0.05f);
    gl.glVertex3f(0.05f, trunkHeight, 0.05f);
    gl.glVertex3f(0.05f, 0.0f, 0.05f);
    gl.glEnd();
    
    // Tree leaves (layered spheres for more realistic look)
    gl.glColor3f(0.2f, 0.6f, 0.2f);
    gl.glTranslatef(0.0f, trunkHeight + 0.2f, 0.0f);
    drawSphere(gl, 0.3f * height);
    
    // Second layer of leaves
    gl.glColor3f(0.1f, 0.5f, 0.1f);
    gl.glTranslatef(0.0f, 0.15f, 0.0f);
    drawSphere(gl, 0.25f * height);
    
    gl.glPopMatrix();
}
    private void drawBrushes(GL2 gl) {
    // Small bushes scattered around the scene
    drawBush(gl, -5.5f, 0.0f, -2.0f, 0.3f);
    drawBush(gl, -5.0f, 0.0f, -1.5f, 0.25f);
    drawBush(gl, -4.5f, 0.0f, -2.5f, 0.35f);
    
    drawBush(gl, 5.5f, 0.0f, 2.0f, 0.28f);
    drawBush(gl, 5.0f, 0.0f, 1.5f, 0.32f);
    drawBush(gl, 4.8f, 0.0f, 2.5f, 0.27f);
    
    drawBush(gl, -2.0f, 0.0f, 5.5f, 0.3f);
    drawBush(gl, -1.5f, 0.0f, 5.0f, 0.29f);
    drawBush(gl, -2.5f, 0.0f, 4.8f, 0.33f);
    
    drawBush(gl, 2.0f, 0.0f, -5.5f, 0.31f);
    drawBush(gl, 1.5f, 0.0f, -5.0f, 0.26f);
    drawBush(gl, 2.5f, 0.0f, -4.8f, 0.34f);
    
    // Corner clusters
    drawBush(gl, -7.0f, 0.0f, -7.0f, 0.4f);
    drawBush(gl, -6.5f, 0.0f, -6.8f, 0.35f);
    drawBush(gl, 7.0f, 0.0f, 7.0f, 0.38f);
    drawBush(gl, 6.8f, 0.0f, 6.5f, 0.32f);
}

private void drawBush(GL2 gl, float x, float y, float z, float size) {
    gl.glPushMatrix();
    gl.glTranslatef(x, y, z);
    
    // Bush body - darker green, lower and wider than trees
    gl.glColor3f(0.15f, 0.4f, 0.15f);
    gl.glTranslatef(0.0f, size * 0.5f, 0.0f);
    
    // Main bush sphere
    drawSphere(gl, size);
    
    // Additional smaller spheres for irregular shape
    gl.glColor3f(0.18f, 0.45f, 0.18f);
    gl.glTranslatef(size * 0.3f, size * 0.1f, 0.0f);
    drawSphere(gl, size * 0.7f);
    
    gl.glTranslatef(-size * 0.6f, 0.0f, size * 0.2f);
    drawSphere(gl, size * 0.6f);
    
    gl.glPopMatrix();
}
    
    
    private void drawCars(GL2 gl) {
        // Simple cars on roads
        drawCar(gl, -2.0f, 0.0f, 0.2f, 1.0f, 0.0f, 0.0f); // Red car
        drawCar(gl, 2.0f, 0.0f, -0.2f, 0.0f, 0.0f, 1.0f); // Blue car
        drawCar(gl, 0.2f, 0.0f, -2.0f, 0.0f, 1.0f, 0.0f); // Green car
    }
    
    private void drawCar(GL2 gl, float x, float y, float z, float r, float g, float b) {
        gl.glPushMatrix();
        gl.glTranslatef(x, y + 0.1f, z);
        gl.glColor3f(r, g, b);
        
        // Car body
        gl.glScalef(0.4f, 0.2f, 0.8f);
        drawCube(gl);
        
        gl.glPopMatrix();
    }
    
    private void drawCube(GL2 gl) {
        gl.glBegin(GL2.GL_QUADS);
        
        // All faces of a unit cube
        // Front
        gl.glNormal3f(0.0f, 0.0f, 1.0f);
        gl.glVertex3f(-0.5f, -0.5f, 0.5f);
        gl.glVertex3f(0.5f, -0.5f, 0.5f);
        gl.glVertex3f(0.5f, 0.5f, 0.5f);
        gl.glVertex3f(-0.5f, 0.5f, 0.5f);
        
        // Back
        gl.glNormal3f(0.0f, 0.0f, -1.0f);
        gl.glVertex3f(-0.5f, -0.5f, -0.5f);
        gl.glVertex3f(-0.5f, 0.5f, -0.5f);
        gl.glVertex3f(0.5f, 0.5f, -0.5f);
        gl.glVertex3f(0.5f, -0.5f, -0.5f);
        
        // Top
        gl.glNormal3f(0.0f, 1.0f, 0.0f);
        gl.glVertex3f(-0.5f, 0.5f, -0.5f);
        gl.glVertex3f(-0.5f, 0.5f, 0.5f);
        gl.glVertex3f(0.5f, 0.5f, 0.5f);
        gl.glVertex3f(0.5f, 0.5f, -0.5f);
        
        // Bottom
        gl.glNormal3f(0.0f, -1.0f, 0.0f);
        gl.glVertex3f(-0.5f, -0.5f, -0.5f);
        gl.glVertex3f(0.5f, -0.5f, -0.5f);
        gl.glVertex3f(0.5f, -0.5f, 0.5f);
        gl.glVertex3f(-0.5f, -0.5f, 0.5f);
        
        // Right
        gl.glNormal3f(1.0f, 0.0f, 0.0f);
        gl.glVertex3f(0.5f, -0.5f, -0.5f);
        gl.glVertex3f(0.5f, 0.5f, -0.5f);
        gl.glVertex3f(0.5f, 0.5f, 0.5f);
        gl.glVertex3f(0.5f, -0.5f, 0.5f);
        
        // Left
        gl.glNormal3f(-1.0f, 0.0f, 0.0f);
        gl.glVertex3f(-0.5f, -0.5f, -0.5f);
        gl.glVertex3f(-0.5f, -0.5f, 0.5f);
        gl.glVertex3f(-0.5f, 0.5f, 0.5f);
        gl.glVertex3f(-0.5f, 0.5f, -0.5f);
        
        gl.glEnd();
    }
    
    private void drawSphere(GL2 gl, float radius) {
        int slices = 16;
        int stacks = 16;
        
        for (int i = 0; i <= stacks; i++) {
            float V = (float) i / stacks;
            float phi = V * (float) Math.PI;
            
            gl.glBegin(GL2.GL_QUAD_STRIP);
            for (int j = 0; j <= slices; j++) {
                float U = (float) j / slices;
                float theta = U * 2.0f * (float) Math.PI;
                
                float x = (float) (Math.cos(theta) * Math.sin(phi));
                float y = (float) Math.cos(phi);
                float z = (float) (Math.sin(theta) * Math.sin(phi));
                
                gl.glNormal3f(x, y, z);
                gl.glVertex3f(x * radius, y * radius, z * radius);
                
                x = (float) (Math.cos(theta) * Math.sin(phi + Math.PI / stacks));
                y = (float) Math.cos(phi + Math.PI / stacks);
                z = (float) (Math.sin(theta) * Math.sin(phi + Math.PI / stacks));
                
                gl.glNormal3f(x, y, z);
                gl.glVertex3f(x * radius, y * radius, z * radius);
            }
            gl.glEnd();
        }
    }
    
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        
        if (height <= 0) height = 1;
        float aspect = (float) width / height;
        
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        
        glu.gluPerspective(45.0, aspect, 0.1, 100.0);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }
    
    @Override
    public void dispose(GLAutoDrawable drawable) {
        // Cleanup resources if needed
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W: cameraZ -= 0.5f; break;
            case KeyEvent.VK_S: cameraZ += 0.5f; break;
            case KeyEvent.VK_A: cameraX -= 0.5f; break;
            case KeyEvent.VK_D: cameraX += 0.5f; break;
            case KeyEvent.VK_Q: cameraY += 0.5f; break;
            case KeyEvent.VK_E: cameraY -= 0.5f; break;
            case KeyEvent.VK_UP: rotateX -= 5.0f; break;
            case KeyEvent.VK_DOWN: rotateX += 5.0f; break;
            case KeyEvent.VK_LEFT: rotateY -= 5.0f; break;
            case KeyEvent.VK_RIGHT: rotateY += 5.0f; break;
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {}
    
    @Override
    public void keyTyped(KeyEvent e) {}
}