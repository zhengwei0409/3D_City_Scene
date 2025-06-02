/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package assignment;

/**
 *
 * @author kwong
 */
public class Car {
    float[][] path;  // Waypoints [x, z]
    int currentIndex = 0;
    float x, z;
    float speed;
    float rotationY; // degrees

    Car(float[][] path, float speed) {
        this.path = path;
        this.speed = speed;
        this.x = path[0][0];
        this.z = path[0][1];
        this.rotationY = 0;
    }

    void update() {
        int nextIndex = (currentIndex + 1) % path.length;
        float tx = path[nextIndex][0];
        float tz = path[nextIndex][1];

        float dx = tx - x;
        float dz = tz - z;
        float distance = (float) Math.sqrt(dx * dx + dz * dz);

        if (distance < speed) {
            x = tx;
            z = tz;
            currentIndex = nextIndex;
        } else {
            x += dx / distance * speed;
            z += dz / distance * speed;
        }

        // Rotation: angle car faces, rotate around Y axis
        rotationY = (float) Math.toDegrees(Math.atan2(dx, dz));
    }
}