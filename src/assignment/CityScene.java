/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package assignment;

import com.jogamp.opengl.*;

import javax.swing.*;



import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;


import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


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
    
    // Road properties
    private static final float Y_LEVEL = 0.01f; // Y-coordinate for the road surface
    private static final float MARKING_Y_LEVEL = 0.02f; // Y-coordinate for markings (slightly above road)
    
    // EXTENT defines the outermost surface of the road grid
    private static final float EXTENT = 10.0f;

    // Where markings are drawn. Original code used -8.0f to 8.0f.
    private static final float MARKING_RANGE_START = -8.0f;
    private static final float MARKING_RANGE_END = 8.0f;

    private static final float DASH_LENGTH = 0.5f;
    private static final float MARKING_STEP = 1.0f; // Combined length of a dash and a gap
    private static final float MARKING_OFFSET_FROM_CENTER = 0.1f; // Offset for parallel marking lines from road arm centerline
    
    private float trafficLightTimer = 0.0f;
    private int currentLight = 0; // 0=red, 1=yellow, 2=green
    
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
            
            drawTrafficLights(gl);
            
            // Draw clouds
            drawClouds(gl);

            // Draw sun
            drawSun(gl);
            
            // Draw other elements
            drawCars(gl);
            drawStreetLights(gl);

            time += 0.016f;
            
            trafficLightTimer += 0.016f;
            if (trafficLightTimer > 3.0f) { // Change every 3 seconds
                currentLight = (currentLight + 1) % 3;
                trafficLightTimer = 0.0f;
            }
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
    
    // KLCC Petronas Twin Towers 
    drawTwinTowers(gl, 5.5f, 0, -6.0f);

    }

    // BUILDING TYPES
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

    //  HELPER METHODS FOR BUILDING DETAILS
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

    //  ENHANCED STREET ELEMENTS
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

    // ENHANCED GROUND WITH SIDEWALKS
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

    private void drawTruncatedCone(GL2 gl, float baseRadius, float topRadius, float height, int slices) {
        float da = 2.0f * (float)Math.PI / slices;

        // Sides
        gl.glBegin(GL2.GL_QUAD_STRIP);
        for (int j = 0; j <= slices; j++) {
            float angle = (j == slices) ? 0.0f : j * da; // Wrap around for last slice
            float cosAng = (float)Math.cos(angle);
            float sinAng = (float)Math.sin(angle);

            // Calculate normal for the side surface
            float nx = height * cosAng;
            float ny = height * sinAng;
            float nz = baseRadius - topRadius; //  Points "outward" correctly from the slope
            float length = (float)Math.sqrt(nx*nx + ny*ny + nz*nz);
            if (length > 1e-6f) { // Avoid division by zero if height=0 and radii are same (though unlikely for cone)
                gl.glNormal3f(nx/length, ny/length, nz/length);
            } else { // Fallback for a degenerate case (e.g. flat disk) or simple cylinder
                 gl.glNormal3f(cosAng,sinAng,0.0f);
            }

            gl.glVertex3f(topRadius * cosAng, topRadius * sinAng, height / 2.0f);   // Top vertex
            gl.glVertex3f(baseRadius * cosAng, baseRadius * sinAng, -height / 2.0f); // Bottom vertex
        }
        gl.glEnd();

        // Top Cap
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        gl.glNormal3f(0.0f, 0.0f, 1.0f);
        gl.glVertex3f(0.0f, 0.0f, height / 2.0f); // Center
        for (int i = 0; i <= slices; i++) {
            float angle = (i == slices) ? 0.0f : i * da;
            gl.glVertex3f(topRadius * (float)Math.cos(angle), topRadius * (float)Math.sin(angle), height / 2.0f);
        }
        gl.glEnd();

        // Bottom Cap
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        gl.glNormal3f(0.0f, 0.0f, -1.0f);
        gl.glVertex3f(0.0f, 0.0f, -height / 2.0f); // Center
        for (int i = slices; i >= 0; i--) { // Iterate in reverse for correct face orientation
            float angle = (i == 0 && slices != 0) ? 0.0f : i * da; // Ensure angle=0 for the last point if i=0
            gl.glVertex3f(baseRadius * (float)Math.cos(angle), baseRadius * (float)Math.sin(angle), -height / 2.0f);
        }
        gl.glEnd();
    }


    private void drawTwinTowers(GL2 gl, float x, float y, float z) {
    // --- Parameters for Tower Dimensions ---
    float towerBodyHeight = 10.0f; // Main structure height (remains the same)
    int segments = 6;             // Number of stacked sections for the main body

    // --- REDUCED WIDTH PARAMETERS ---
    float radiusReductionFactor = 0.75f; // Reduce radii by 25%
    float baseRadius = 1.2f * radiusReductionFactor;      // New: 0.9f
    float topBodyRadius = 0.7f * radiusReductionFactor;   // New: 0.525f

    // Spacing between tower centers (remains the same for now)
    float spacing = 3.5f;

    // Pinnacle dimensions (also scaled in width)
    float pinnacleBaseRadius = topBodyRadius * 0.9f; // Proportionate to topBodyRadius
    float pinnacleBaseHeight = 0.8f; // Height remains
    float spireRadius = Math.max(0.1f, 0.15f * radiusReductionFactor); // New: ~0.1125f, ensure not too thin
    float spireHeight = 6.0f;     // Height remains

    // Calculated values
    float segmentHeight = towerBodyHeight / segments;
    float radiusStep = (baseRadius - topBodyRadius) / segments;

    // Color and slices
    float greyR = 0.55f;
    float greyG = 0.58f;
    float greyB = 0.6f;
    int towerSlices = 24; // Keep smoothness

    // Loop for two towers (left and right)
    for (int towerIdx = 0; towerIdx < 2; towerIdx++) {
        gl.glPushMatrix();
        float towerX = x + (towerIdx == 0 ? -spacing / 2 : spacing / 2);
        gl.glTranslatef(towerX, y, z);
        gl.glRotatef(-90f, 1f, 0f, 0f);

        gl.glColor3f(greyR, greyG, greyB);

        // --- Draw Main Tower Body ---
        gl.glTranslatef(0, 0, segmentHeight / 2.0f);

        for (int i = 0; i < segments; i++) {
            float currentBaseRadius = baseRadius - i * radiusStep;
            float currentTopRadius = baseRadius - (i + 1) * radiusStep;
            // Ensure top radius doesn't go below a minimum for aesthetics during tapering
            currentTopRadius = Math.max(currentTopRadius, topBodyRadius * 0.8f); // Prevent overly sharp taper mid-body
            if (i == segments -1) currentTopRadius = topBodyRadius; // Final segment top matches definition

            drawTruncatedCone(gl, currentBaseRadius, currentTopRadius, segmentHeight, towerSlices);
            if (i < segments - 1) {
                gl.glTranslatef(0, 0, segmentHeight);
            }
        }
        gl.glTranslatef(0, 0, segmentHeight / 2.0f); // Move to top surface of main body

        // --- Draw Pinnacle ---
        // 1. Pinnacle Base
        gl.glColor3f(greyR * 0.9f, greyG * 0.9f, greyB * 0.9f);
        gl.glTranslatef(0, 0, pinnacleBaseHeight / 2.0f);
        drawTruncatedCone(gl, pinnacleBaseRadius, pinnacleBaseRadius * 0.7f, pinnacleBaseHeight, towerSlices);
        gl.glTranslatef(0, 0, pinnacleBaseHeight / 2.0f);

        // 2. Spire
        gl.glColor3f(greyR * 0.8f, greyG * 0.8f, greyB * 0.8f);
        gl.glTranslatef(0, 0, spireHeight / 2.0f);
        drawTruncatedCone(gl, spireRadius, spireRadius * 0.3f, spireHeight, towerSlices/2);

        gl.glPopMatrix(); // End of this tower's transformations
    }

    // --- Draw Skybridge ---
    // Skybridge dimensions mostly unchanged unless they look out of proportion
    float bridgeLevel = towerBodyHeight * 0.47f;
    float bridgeHeight = 0.4f; // Kept same
    float bridgeDepth = 0.5f;  // Kept same

    gl.glPushMatrix();
    gl.glTranslatef(x, y + bridgeLevel, z);
    gl.glColor3f(greyR * 0.8f, greyG * 0.8f, greyB * 0.8f);

    // Main bridge structure
    gl.glPushMatrix();
    gl.glScalef(spacing * 0.95f, bridgeHeight, bridgeDepth);
    drawCube(gl);
    gl.glPopMatrix();

    // Simplified V-supports/diagonal braces
    float supportLength = bridgeLevel * 0.3f;
    float supportThickness = 0.1f; // Kept same
    float supportAngle = 35.0f;

    // Estimate tower radius at bridge level for more accurate support placement start
    // This is an approximation as the taper isn't linear if Math.max was hit often.
    float segmentsAtBridgeLevel = bridgeLevel / segmentHeight;
    float estimatedRadiusAtBridge = baseRadius - segmentsAtBridgeLevel * radiusStep;
    estimatedRadiusAtBridge = Math.max(estimatedRadiusAtBridge, topBodyRadius); // Ensure it's not less than the top body radius


    // Left Tower Supports
    gl.glPushMatrix();
    gl.glTranslatef(-spacing/2 + estimatedRadiusAtBridge*0.7f , -bridgeHeight/2 - 0.1f, 0);
    gl.glRotatef(supportAngle, 0,0,1);
    gl.glRotatef(10, 1,0,0);
    gl.glTranslatef(0, -supportLength/2, 0);
    gl.glScalef(supportThickness, supportLength, supportThickness);
    drawCube(gl);
    gl.glPopMatrix();

    gl.glPushMatrix();
    gl.glTranslatef(-spacing/2 + estimatedRadiusAtBridge*0.7f , -bridgeHeight/2 - 0.1f, 0);
    gl.glRotatef(-supportAngle, 0,0,1);
    gl.glRotatef(10, 1,0,0);
    gl.glTranslatef(0, -supportLength/2, 0);
    gl.glScalef(supportThickness, supportLength, supportThickness);
    drawCube(gl);
    gl.glPopMatrix();

    // Right Tower Supports
    gl.glPushMatrix();
    gl.glTranslatef(spacing/2 - estimatedRadiusAtBridge*0.7f , -bridgeHeight/2 - 0.1f, 0);
    gl.glRotatef(supportAngle, 0,0,1);
    gl.glRotatef(-10, 1,0,0);
    gl.glTranslatef(0, -supportLength/2, 0);
    gl.glScalef(supportThickness, supportLength, supportThickness);
    drawCube(gl);
    gl.glPopMatrix();

    gl.glPushMatrix();
    gl.glTranslatef(spacing/2 - estimatedRadiusAtBridge*0.7f , -bridgeHeight/2 - 0.1f, 0);
    gl.glRotatef(-supportAngle, 0,0,1);
    gl.glRotatef(-10, 1,0,0);
    gl.glTranslatef(0, -supportLength/2, 0);
    gl.glScalef(supportThickness, supportLength, supportThickness);
    drawCube(gl);
    gl.glPopMatrix();

    gl.glPopMatrix(); // End of skybridge transformations
}

    private void drawCylinder(GL2 gl, float baseRadius, float topRadius, float height) {
        GLU glu = GLU.createGLU();
        GLUquadric quad = glu.gluNewQuadric();
        glu.gluQuadricNormals(quad, GLU.GLU_SMOOTH);
        glu.gluCylinder(quad, baseRadius, topRadius, height, 20, 20);
        glu.gluDeleteQuadric(quad);
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
    
   private void drawTrafficLight(GL2 gl, float x, float y, float z) {
    gl.glPushMatrix();
    gl.glTranslatef(x, y, z);
    
    // Traffic light pole
    gl.glColor3f(0.3f, 0.3f, 0.3f);
    gl.glBegin(GL2.GL_QUADS);
    gl.glVertex3f(-0.05f, 0.0f, -0.05f);
    gl.glVertex3f(0.05f, 0.0f, -0.05f);
    gl.glVertex3f(0.05f, 3.0f, -0.05f);
    gl.glVertex3f(-0.05f, 3.0f, -0.05f);
    gl.glEnd();
    
    // Traffic light box
    gl.glColor3f(0.2f, 0.2f, 0.2f);
    gl.glPushMatrix();
    gl.glTranslatef(0.0f, 2.7f, 0.0f);
    gl.glScalef(0.3f, 0.6f, 0.2f);
    drawCube(gl);
    gl.glPopMatrix();
    
    // Red light
    if (currentLight == 0) {
        gl.glColor3f(1.0f, 0.2f, 0.2f); // Bright red when active
    } else {
        gl.glColor3f(0.3f, 0.1f, 0.1f); // Dark red when inactive
    }
    gl.glPushMatrix();
    gl.glTranslatef(0.0f, 2.9f, 0.12f);
    drawSphere(gl, 0.08f);
    gl.glPopMatrix();
    
    // Yellow light
    if (currentLight == 1) {
        gl.glColor3f(1.0f, 1.0f, 0.2f); // Bright yellow when active
    } else {
        gl.glColor3f(0.3f, 0.3f, 0.1f); // Dark yellow when inactive
    }
    gl.glPushMatrix();
    gl.glTranslatef(0.0f, 2.7f, 0.12f);
    drawSphere(gl, 0.08f);
    gl.glPopMatrix();
    
    // Green light
    if (currentLight == 2) {
        gl.glColor3f(0.2f, 1.0f, 0.2f); // Bright green when active
    } else {
        gl.glColor3f(0.1f, 0.3f, 0.1f); // Dark green when inactive
    }
    gl.glPushMatrix();
    gl.glTranslatef(0.0f, 2.5f, 0.12f);
    drawSphere(gl, 0.08f);
    gl.glPopMatrix();
    
    gl.glPopMatrix();
}
   
   private void drawTrafficLights(GL2 gl) {
    // Traffic lights at major intersections
    drawTrafficLight(gl, -1.0f, 0.0f, -1.0f);  // Northwest intersection
    drawTrafficLight(gl, 1.0f, 0.0f, -1.0f);   // Northeast intersection
    drawTrafficLight(gl, -1.0f, 0.0f, 1.0f);   // Southwest intersection
    drawTrafficLight(gl, 1.0f, 0.0f, 1.0f);    // Southeast intersection
}

    private void drawRoadQuad(GL2 gl, float x1, float z1, float x2, float z2, float x3, float z3, float x4, float z4) {
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex3f(x1, Y_LEVEL, z1);
        gl.glVertex3f(x2, Y_LEVEL, z2);
        gl.glVertex3f(x3, Y_LEVEL, z3);
        gl.glVertex3f(x4, Y_LEVEL, z4);
        gl.glEnd();
    }

    private void drawRoads(GL2 gl) {
        gl.glColor3f(0.3f, 0.3f, 0.3f); // Dark gray roads
        gl.glNormal3f(0.0f, 1.0f, 0.0f);

        final float RWH = ROAD_WIDTH / 2.0f;
        final float C = EXTENT - RWH;

        // Center (cx, cz) coordinates for intersections:
        float[][] centers = {
                {0, 0},    // 0: Center
                {0, C},    // 1: Top-Mid
                {0, -C},   // 2: Bot-Mid
                {-C, 0},   // 3: Left-Mid
                {C, 0},    // 4: Right-Mid
                {-C, C},   // 5: Top-Left
                {C, C},    // 6: Top-Right
                {-C, -C},  // 7: Bot-Left
                {C, -C}    // 8: Bot-Right
        };

        for (float[] center : centers) {
            float cx = center[0];
            float cz = center[1];
            drawRoadQuad(gl,
                    cx - RWH, cz - RWH, // bottom-left
                    cx + RWH, cz - RWH, // bottom-right
                    cx + RWH, cz + RWH, // top-right
                    cx - RWH, cz + RWH  // top-left
            );
        }

        //Draw 12 Road Segments connecting intersections ---
        drawRoadQuad(gl, -RWH, RWH, RWH, RWH, RWH, C - RWH, -RWH, C - RWH);
        drawRoadQuad(gl, -RWH, -C + RWH, RWH, -C + RWH, RWH, -RWH, -RWH, -RWH);
        drawRoadQuad(gl, -C + RWH, -RWH, -RWH, -RWH, -RWH, RWH, -C + RWH, RWH);
        drawRoadQuad(gl, RWH, -RWH, C - RWH, -RWH, C - RWH, RWH, RWH, RWH);

        // Segments from Mid-T-junctions to Corners (Outer frame segments)
        drawRoadQuad(gl, -C + RWH, C - RWH, -RWH, C - RWH, -RWH, C + RWH, -C + RWH, C + RWH);
        drawRoadQuad(gl, RWH, C - RWH, C - RWH, C - RWH, C - RWH, C + RWH, RWH, C + RWH);
        drawRoadQuad(gl, -C + RWH, -C - RWH, -RWH, -C - RWH, -RWH, -C + RWH, -C + RWH, -C + RWH);
        drawRoadQuad(gl, RWH, -C - RWH, C - RWH, -C - RWH, C - RWH, -C + RWH, RWH, -C + RWH);
        drawRoadQuad(gl, -C - RWH, RWH, -C + RWH, RWH, -C + RWH, C - RWH, -C - RWH, C - RWH);
        drawRoadQuad(gl, -C - RWH, -C + RWH, -C + RWH, -C + RWH, -C + RWH, -RWH, -C - RWH, -RWH);
        drawRoadQuad(gl, C + RWH, RWH, C - RWH, RWH, C - RWH, C - RWH, C + RWH, C - RWH);
        drawRoadQuad(gl, C - RWH, RWH,     C + RWH, RWH,      C + RWH, C - RWH, C - RWH, C - RWH);
        drawRoadQuad(gl, C - RWH, -C + RWH, C + RWH, -C + RWH, C + RWH, -RWH,    C - RWH, -RWH);

        gl.glColor3f(1.0f, 1.0f, 0.0f); // Yellow lines
        drawRoadMarkings(gl);
    }

    private void drawMarkingsOnSegment(GL2 gl, float x_bl, float z_bl, float x_tr, float z_tr, boolean isHorizontal) {

    if (isHorizontal) {
        float segment_center_z = (z_bl + z_tr) / 2.0f;

        // Calculate z-coordinates for the two parallel marking lines
        float z_marking_line1 = segment_center_z - MARKING_OFFSET_FROM_CENTER;
        float z_marking_line2 = segment_center_z + MARKING_OFFSET_FROM_CENTER;

        // Iterate along the x-axis of the segment
        for (float x = x_bl; x <= x_tr - DASH_LENGTH; x += MARKING_STEP) {
            if (!(x + DASH_LENGTH >= MARKING_RANGE_START && x <= MARKING_RANGE_END)) {
                continue;
            }

            // Draw first dashed line
            gl.glVertex3f(x, MARKING_Y_LEVEL, z_marking_line1);
            gl.glVertex3f(x + DASH_LENGTH, MARKING_Y_LEVEL, z_marking_line1);

            // Draw second dashed line
            gl.glVertex3f(x, MARKING_Y_LEVEL, z_marking_line2);
            gl.glVertex3f(x + DASH_LENGTH, MARKING_Y_LEVEL, z_marking_line2);
        }
    } else {
        float segment_center_x = (x_bl + x_tr) / 2.0f;

        float x_marking_line1 = segment_center_x - MARKING_OFFSET_FROM_CENTER;
        float x_marking_line2 = segment_center_x + MARKING_OFFSET_FROM_CENTER;

        for (float z = z_bl; z <= z_tr - DASH_LENGTH; z += MARKING_STEP) {
            if (!(z + DASH_LENGTH >= MARKING_RANGE_START && z <= MARKING_RANGE_END)) {
                continue;
            }

            // Draw first dashed line
            gl.glVertex3f(x_marking_line1, MARKING_Y_LEVEL, z);
            gl.glVertex3f(x_marking_line1, MARKING_Y_LEVEL, z + DASH_LENGTH);

            // Draw second dashed line
            gl.glVertex3f(x_marking_line2, MARKING_Y_LEVEL, z);
            gl.glVertex3f(x_marking_line2, MARKING_Y_LEVEL, z + DASH_LENGTH);
        }
    }
}


    private void drawRoadMarkings(GL2 gl) {
        gl.glLineWidth(3.0f);
        gl.glBegin(GL.GL_LINES);

        final float RWH = ROAD_WIDTH / 2.0f;
        final float C = EXTENT - RWH;


        // Segments from Center to Mid-T-junctions
        drawMarkingsOnSegment(gl, -RWH, RWH, RWH, C - RWH, false);
        drawMarkingsOnSegment(gl, -RWH, -C + RWH, RWH, -RWH, false);
        drawMarkingsOnSegment(gl, -C + RWH, -RWH, -RWH, RWH, true);
        drawMarkingsOnSegment(gl, RWH, -RWH, C - RWH, RWH, true);

        // Segments from Mid-T-junctions to Corners (Outer frame segments)
        drawMarkingsOnSegment(gl, -C + RWH, C - RWH, -RWH, C + RWH, true);
        drawMarkingsOnSegment(gl, RWH, C - RWH, C - RWH, C + RWH, true);
        drawMarkingsOnSegment(gl, -C + RWH, -C - RWH, -RWH, -C + RWH, true);
        drawMarkingsOnSegment(gl, RWH, -C - RWH, C - RWH, -C + RWH, true);
        drawMarkingsOnSegment(gl, -C - RWH, RWH, -C + RWH, C - RWH, false);
        drawMarkingsOnSegment(gl, -C - RWH, -C + RWH, -C + RWH, -RWH, false);
        drawMarkingsOnSegment(gl, C - RWH, RWH, C + RWH, C - RWH, false);
        drawMarkingsOnSegment(gl, C - RWH, -C + RWH, C + RWH, -RWH, false);

        gl.glEnd();
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
    
    
    private void drawCylinder(GL2 gl, float radius, float height, int slices, int stacks) {
        double da = 2.0 * Math.PI / slices;
        double dh = height / stacks;
        double dz = -height / 2.0;

        // Draw side an
        gl.glBegin(GL2.GL_QUAD_STRIP);
        for (int j = 0; j <= slices; j++) {
            double angle = (j == slices) ? 0.0 : j * da; // Wrap around for last slice
            float x = (float) (radius * Math.cos(angle));
            float y = (float) (radius * Math.sin(angle));
            gl.glNormal3f(x / radius, y / radius, 0.0f); // Normal for side
            gl.glVertex3f(x, y, (float) dz);             // Bottom edge of strip
            gl.glVertex3f(x, y, (float) (dz + height));  // Top edge of strip (or use stacks)
                                                       // Simplified for stacks=1 for wheels
        }
        gl.glEnd();

        // Draw Top Cap (at z = height/2)
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        gl.glNormal3f(0.0f, 0.0f, 1.0f);
        gl.glVertex3f(0.0f, 0.0f, height / 2.0f); // Center of fan
        for (int i = 0; i <= slices; i++) {
            double angle = (i == slices) ? 0.0 : i * da;
            gl.glVertex3f((float) (radius * Math.cos(angle)),
                          (float) (radius * Math.sin(angle)),
                          height / 2.0f);
        }
        gl.glEnd();

        // Draw Bottom Cap (at z = -height/2)
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        gl.glNormal3f(0.0f, 0.0f, -1.0f);
        gl.glVertex3f(0.0f, 0.0f, -height / 2.0f); // Center of fan
        for (int i = slices; i >= 0; i--) { // Iterate in reverse for correct winding
            double angle = (i == slices) ? 0.0 : i * da;
            gl.glVertex3f((float) (radius * Math.cos(angle)),
                          (float) (radius * Math.sin(angle)),
                          -height / 2.0f);
        }
        gl.glEnd();
    }


    private void drawCars(GL2 gl) {
        // Cars are now smaller
        drawCar(gl, -1.2f, 0.0f, 0.1f, 1.0f, 0.0f, 0.0f); // Red car, adjusted position
        drawCar(gl, 1.2f, 0.0f, -0.1f, 0.0f, 0.0f, 1.0f); // Blue car, adjusted position
        drawCar(gl, 0.1f, 0.0f, -1.2f, 0.0f, 1.0f, 0.0f); // Green car, adjusted position
    }

    private void drawCar(GL2 gl, float x, float y, float z, float r, float g, float b) {
    // --- Smaller Car Dimensions ---
    float scaleFactor = 0.6f; // Factor to make the car smaller

    float chassisWidth = 0.8f * scaleFactor;
    float chassisHeight = 0.3f * scaleFactor;
    float chassisLength = 1.6f * scaleFactor;

    float cabinWidth = chassisWidth * 0.85f;
    float cabinHeight = 0.4f * scaleFactor;
    float cabinLength = chassisLength * 0.5f;

    float wheelRadius = 0.20f * scaleFactor;
    float wheelThickness = 0.1f * scaleFactor;

    gl.glPushMatrix(); // Save current world matrix
    gl.glTranslatef(x, y + wheelRadius, z); // Position car: y is ground, lift by wheelRadius for axle

    // --- 1. Draw Chassis ---
    float chassisYOffset = chassisHeight * 0.3f;
    gl.glPushMatrix();
    gl.glColor3f(r, g, b);
    gl.glTranslatef(0.0f, chassisYOffset, 0.0f);
    gl.glScalef(chassisWidth, chassisHeight, chassisLength);
    drawCube(gl);
    gl.glPopMatrix();

    // --- 2. Draw Cabin ---
    // Calculate cabin's actual center position in car's local space
    float cabinActualY = (chassisYOffset + chassisHeight / 2.0f) + (cabinHeight / 2.0f);
    float cabinActualZ = -chassisLength * 0.15f;

    gl.glPushMatrix(); // Cabin's main transformation block
    gl.glColor3f(r * 0.9f, g * 0.9f, b * 0.9f); // Cabin color
    gl.glTranslatef(0.0f, cabinActualY, cabinActualZ); // Translate to cabin's designated center
    gl.glScalef(cabinWidth, cabinHeight, cabinLength); // Scale the unit cube to cabin dimensions
    drawCube(gl); // Draw the main cabin block
    gl.glPopMatrix(); // End of Cabin's main block


    // --- 2a. Draw Windows ---
    // Windows are drawn relative to the cabin's center.
    // Cabin's center in car space is (0, cabinActualY, cabinActualZ).
    // Cabin dimensions are cabinWidth, cabinHeight, cabinLength.

    gl.glColor3f(0.6f, 0.8f, 1.0f); // Light blue for windows (r, g, b)

    float windowPanelThickness = 0.015f * scaleFactor; // Make it very thin
    float epsilonOffset = 0.005f; // Tiny offset to prevent Z-fighting, pushing windows slightly outward

    // Window dimensions (as fractions of cabin dimensions)
    float frontWindowRelWidth = 0.80f;
    float frontWindowRelHeight = 0.65f;
    float rearWindowRelWidth = 0.75f;
    float rearWindowRelHeight = 0.60f;
    float sideWindowRelLength = 0.70f; // Length of side window along cabin's Z-axis
    float sideWindowRelHeight = 0.60f;

    gl.glPushMatrix(); // Group for all windows, translated to cabin's center
    gl.glTranslatef(0.0f, cabinActualY, cabinActualZ); // Move to where cabin's center is

    // Front Windshield
    // Centered on the cabin's front face.
    gl.glPushMatrix();
    gl.glTranslatef(0.0f, 0.0f, (cabinLength / 2.0f) + epsilonOffset); // Position on front face, slightly outward
    gl.glScalef(cabinWidth * frontWindowRelWidth, cabinHeight * frontWindowRelHeight, windowPanelThickness);
    drawCube(gl);
    gl.glPopMatrix();

    // Rear Windshield
    // Centered on the cabin's rear face.
    gl.glPushMatrix();
    gl.glTranslatef(0.0f, 0.0f, -(cabinLength / 2.0f) - epsilonOffset); // Position on rear face, slightly outward
    gl.glScalef(cabinWidth * rearWindowRelWidth, cabinHeight * rearWindowRelHeight, windowPanelThickness);
    drawCube(gl);
    gl.glPopMatrix();

    // Side Window (Right)
    // Centered on the cabin's right face.
    // For side windows, the 'width' of the window panel is its thickness.
    gl.glPushMatrix();
    gl.glTranslatef((cabinWidth / 2.0f) + epsilonOffset, 0.0f, 0.0f); // Position on right face, slightly outward
    gl.glScalef(windowPanelThickness, cabinHeight * sideWindowRelHeight, cabinLength * sideWindowRelLength);
    drawCube(gl);
    gl.glPopMatrix();

    // Side Window (Left)
    // Centered on the cabin's left face.
    gl.glPushMatrix();
    gl.glTranslatef(-(cabinWidth / 2.0f) - epsilonOffset, 0.0f, 0.0f); // Position on left face, slightly outward
    gl.glScalef(windowPanelThickness, cabinHeight * sideWindowRelHeight, cabinLength * sideWindowRelLength);
    drawCube(gl);
    gl.glPopMatrix();

    gl.glPopMatrix(); // End of windows group


    // --- 3. Draw Wheels (as Cylinders) ---
    gl.glColor3f(0.1f, 0.1f, 0.1f); // Dark grey/black for wheels

    float wheelLocalYPos = 0.0f;
    float wheelFrontZ = chassisLength * 0.38f;
    float wheelRearZ = -chassisLength * 0.38f;
    float wheelXOffset = chassisWidth / 2.0f + wheelThickness / 2.0f - (0.02f * scaleFactor);
    int wheelSlices = 16;

    // Wheel 1: Front-Right
    gl.glPushMatrix();
    gl.glTranslatef(wheelXOffset, wheelLocalYPos, wheelFrontZ);
    gl.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);
    drawCylinder(gl, wheelRadius, wheelThickness, wheelSlices, 1);
    gl.glPopMatrix();

    // Wheel 2: Front-Left
    gl.glPushMatrix();
    gl.glTranslatef(-wheelXOffset, wheelLocalYPos, wheelFrontZ);
    gl.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);
    drawCylinder(gl, wheelRadius, wheelThickness, wheelSlices, 1);
    gl.glPopMatrix();

    // Wheel 3: Rear-Right
    gl.glPushMatrix();
    gl.glTranslatef(wheelXOffset, wheelLocalYPos, wheelRearZ);
    gl.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);
    drawCylinder(gl, wheelRadius, wheelThickness, wheelSlices, 1);
    gl.glPopMatrix();

    // Wheel 4: Rear-Left
    gl.glPushMatrix();
    gl.glTranslatef(-wheelXOffset, wheelLocalYPos, wheelRearZ);
    gl.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);
    drawCylinder(gl, wheelRadius, wheelThickness, wheelSlices, 1);
    gl.glPopMatrix();

    gl.glPopMatrix(); // Restore original world matrix (from the very start of drawCar)
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
    
    private void drawClouds(GL2 gl) {
        GLU glu = new GLU();
        GLUquadric quadric = glu.gluNewQuadric();

        gl.glPushMatrix();
        gl.glColor4f(1.0f, 1.0f, 1.0f, 0.8f); // Semi-transparent white

        // Enable blending for transparency
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

        // Example cloud cluster positions
        float[][] cloudPositions = {
            { -4.0f, 15.0f, -5.0f },
            {  0.0f, 12.0f, -6.0f },
            {  3.0f, 13f, -4.0f },
            {  4.0f, 10f, 5.0f },
            {  0.0f, 12f, 6.0f }
        };

        for (float[] pos : cloudPositions) {
            drawCloudCluster(gl, glu, quadric, pos[0], pos[1], pos[2]);
        }

        gl.glDisable(GL2.GL_BLEND);
        gl.glPopMatrix();
        glu.gluDeleteQuadric(quadric);
    }

    private void drawCloudCluster(GL2 gl, GLU glu, GLUquadric quadric, float cx, float cy, float cz) {
        float[] offsets = {
            -0.5f, 0f, 0f,
             0.5f, 0f, 0f,
             0f, 0f, -0.5f,
             0f, 0f, 0.5f,
             0f, 0.3f, 0f
        };

        for (int i = 0; i < offsets.length; i += 3) {
            gl.glPushMatrix();
            gl.glTranslatef(cx + offsets[i], cy + offsets[i + 1], cz + offsets[i + 2]);
            glu.gluSphere(quadric, 0.5f, 16, 16);
            gl.glPopMatrix();
        }
    }
    
    private void drawSun(GL2 gl) {
        gl.glPushMatrix();
        gl.glTranslatef(7.0f, 23.0f, -7.0f);
        
        // Enable lighting for the sun
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT1);

        // Sun properties (bright yellow light)
        float[] sunLightPos = {0.0f, 0.0f, 0.0f, 1.0f};
        float[] sunLightColor = {1.0f, 0.9f, 0.7f, 1.0f};
        float[] sunAmbient = {0.3f, 0.3f, 0.2f, 1.0f};

        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, sunLightPos, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, sunLightColor, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, sunAmbient, 0);

        // Sun appearance (glowing yellow sphere)
        gl.glDisable(GL2.GL_LIGHTING); // We want the sun to appear self-lit
        gl.glColor3f(1.0f, 0.9f, 0.1f);

        // Main sun sphere
        drawSphere(gl, 1.0f);

        // Corona effect (slightly larger transparent sphere)
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
        gl.glColor4f(1.0f, 0.7f, 0.1f, 0.3f);
        drawSphere(gl, 1.2f);
        gl.glDisable(GL2.GL_BLEND);
        
        
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glPopMatrix();
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