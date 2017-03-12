/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Rockets;

import static Rockets.Rockets.HEIGHT;
import static Rockets.Rockets.SCALER;
import static Rockets.Rockets.WIDTH;
import static Rockets.Rockets.rockets;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
/**
 *
 * @author Sam
 */
public class Rockets extends JPanel {
    public static int WIDTH = 1920;
    public static int HEIGHT = 1080;
    public static int SCALER = 1;
    public static double milliSecondTimer;
    public static double delta;
    public static ArrayList<Integer> keysDown;
    public static ArrayList<Ball> rockets;
    public static double  timeToStart;
    public static double count;
    public static double loopsGone;
    public static double yCollide, xCollide, timeCollide;
    public static boolean hit;
    public static boolean running;
    public static double distanceAway;
    public static double vM, vAA, aM, aAA;
    public static BufferedImage bg1;
    
    public Rockets(){
        //Init
        keysDown = new ArrayList<Integer>();
        rockets = new ArrayList<Ball>();    
        hit  = false;
        
        bg1 = null;
        try {
            bg1 = ImageIO.read(new File(getClass().getResource("BackgroundMineLowRes.jpg").getFile()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        //init rockets
        vM = 200;
        aM = 40;
        vAA = 300;
        aAA = 80;
        distanceAway = 1600;
        rockets.add(new Ball(0,0,10,vM,aM, true));
        rockets.add(new Ball(1600,0,10,vAA,aAA, false));  
        timeToStart = 1110; // any high number
        milliSecondTimer = 0;
        count = 1;
        xCollide = 0;
        timeCollide = 0;
        yCollide = 0;
        running = true;
        
        //Other
        KeyListener listener = new MyKeyListener();
        addKeyListener(listener);
        setFocusable(true);   
    }
    public void reset(){
        rockets = new ArrayList<Ball>();
        rockets.add(new Ball(0,0,10,vM,aM, true));
        rockets.add(new Ball(1600,0,10,vAA,aAA, false)); 
                
        timeToStart = 1110; // any high number
        milliSecondTimer = 0;
        count = 1;
        running = true;
        hit = false;
        
    }
    
    
    public static void main(String[] args) throws InterruptedException{
        JFrame frame = new JFrame("Missile Launcher");
        Rockets app = new Rockets();
        frame.setSize((int)(WIDTH * SCALER),(int)(HEIGHT * SCALER));
        frame.add(app);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.requestFocus();
        long lastLoopTime = System.nanoTime();
        int fps = 0, lastFpsTime = 0, lastMilliSecondTimer = 0, count = 1;
        final int TARGET_FPS = 60;
        final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;
        //Game Loop
        while(true){
            long now = System.nanoTime();
            long updateLength = now - lastLoopTime;
            lastLoopTime = now;
            delta = updateLength / ((double)OPTIMAL_TIME);
            lastFpsTime += updateLength;
            lastMilliSecondTimer += updateLength;
            fps++;
            if (lastFpsTime > 100000000 / 6 * count){
                if (running){
                    milliSecondTimer += (0.1 / 6);
                    gravity();
                }
                count++;
            }
            if (lastFpsTime >= 1000000000){
                System.out.println("(FPS: "+fps+")");
                //milliSecondTimer += 1;
                lastFpsTime = 0;
                fps = 0;
                count = 1;
            }
            loopsGone++;
            app.repaint();
            Thread.sleep( (lastLoopTime-System.nanoTime() + OPTIMAL_TIME)/1000000 );
        }
    }
    
    public static void gravity(){
        for (int i = 0; i < rockets.size(); i++){
            
            if(timeToStart <= milliSecondTimer){
                rockets.get(1).fired = true;
                //Trail of other rocket
                if (milliSecondTimer > 0.1 * count){
                    rockets.add(new Ball(rockets.get(1).x - 20 ,rockets.get(1).y - HEIGHT + 100,5,0,0,false));
                }
            }
            
            if(rockets.get(i).fired){
                rockets.get(i).vSpeed -= 9.81 / 60.0;
                rockets.get(i).move(0, (double) (rockets.get(i).vSpeed / 60.0));
                rockets.get(i).move(1, (double) (rockets.get(i).hSpeed / 60.0));
            } else if (timeToStart==1110){
                //function to work out the time displacment
                derr(1);
            }
            
            double dx = Math.abs(rockets.get(1).getCenterX() - rockets.get(0).getCenterX());
            double dy = Math.abs(rockets.get(1).getCenterY() - rockets.get(0).getCenterY());
            double r = Math.sqrt(dx * dx + dy * dy);
            if (r <= rockets.get(1).getHeight() / 2 + rockets.get(0).getHeight() / 2 && !hit){
                //They have collided
                double speed = rockets.get(1).speed + rockets.get(0).speed;
                for (int z = 0; z < speed / 10; z++){
                    rockets.add(new Ball(rockets.get(1).x - 20 ,rockets.get(1).y - HEIGHT + 100));
                }
                hit = true;
                rockets.get(0).fired = false;
                rockets.get(1).fired = false;
            }
            
            //For the trail
            if (milliSecondTimer > 0.1 * count){
                rockets.add(new Ball(rockets.get(0).x - 20 ,rockets.get(0).y - HEIGHT + 100,5,0,0,false));
                count++;
            }
            
        }
    }     
    public static void derr(int index){
        double X = distanceAway;
        double angle = 90 - Math.abs(90 - rockets.get(index).angle); 
        
        double s = rockets.get(index).speed, gamma = Math.toRadians(angle);
        double v = rockets.get(0).speed, alpha = Math.toRadians(rockets.get(0).angle);
           
        double aTop = s * s * Math.cos(gamma) * Math.cos(gamma) - v * v * Math.cos(alpha) * Math.cos(alpha);
        double aBottom = v * v * Math.cos(gamma) * Math.cos(gamma) * Math.cos(alpha) * Math.cos(alpha);
        double a = aTop / aBottom;
            
        double bTop = Math.cos(gamma) * Math.sin(gamma) * 2 * s * s + 2 * s * s * Math.cos(gamma) * Math.cos(gamma) * Math.tan(alpha) - 2 * X * 9.81;
        double bBottom = 9.81 * Math.cos(gamma) * Math.cos(gamma);          
        double b = - bTop / bBottom;
           
        double cTop = 2 * X * s * s * Math.cos(gamma) * Math.sin(gamma) - 9.81 * 1600 * 1600;
        double cBottom = 9.81 * Math.cos(gamma) * Math.cos(gamma);
        double c = cTop / cBottom;
            
        double derr = b * b - 4 * a * c;
        if (derr >= 0){
            double x = (- b - Math.sqrt(derr)) / ( 2 * a);
            xCollide = x;
            double y = x * Math.tan(alpha) - (9.91 * x * x)/ (2 * v * v * Math.cos(alpha));
            yCollide = y;
            double dx = X - x;
            
            double a1 = 9.81 / 2;
            double b1 = - s * Math.sin(gamma);
            double c1 = dx * Math.tan(gamma) - (9.81 * dx * dx) / (2 * s * s * Math.cos(gamma) * Math.cos(gamma));
            
            double tA = (- b1 - Math.sqrt(b1 * b1 - 4 * a1 * c1)) / ( 2 * a1);
            
            double b2 = - v * Math.sin(alpha);
            double c2 = x * Math.tan(alpha) - (9.81 * x * x) / (2 * v * v * Math.cos(alpha) * Math.cos(alpha));
            
            double tB = (- b2 - Math.sqrt(b2 * b2 - 4 * a1 * c2)) / ( 2 * a1);
                        
            timeCollide = tB;
            if (a == 0){
                // angles are the same, therefore it should fire immediatly
                timeToStart = -1000;
            } else{
                timeToStart = Math.abs(tB - tA);
            }
        } else {
            System.err.println("Missile cannot be stopped!");
        }
        
    }
    
    public BufferedImage transparanty(BufferedImage image){
        ImageFilter filter = new RGBImageFilter() {
        int transparentColor = Color.white.getRGB() | 0xFF000000;

        public final int filterRGB(int x, int y, int rgb) {
            if ((rgb | 0xFF000000) == transparentColor) {
               return 0x00FFFFFF & rgb;
            } else {
                return rgb;
            }
        }
        };

        ImageProducer filteredImgProd = new FilteredImageSource( image.getSource(), filter);
        Image transparentImg = Toolkit.getDefaultToolkit().createImage(filteredImgProd);
        BufferedImage bimage = new BufferedImage(transparentImg.getWidth(null), transparentImg.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(transparentImg, 0, 0, null);
        bGr.dispose();
        
        return bimage;

    }
    
    
    //Window Painter
    public void paint(Graphics g){
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        this.setFocusable(true);
        this.requestFocusInWindow();
        
        g2d.drawImage(bg1, 0,0, null);
        
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(getClass().getResource("missile.png").getFile()));
        } catch (IOException e) {
            e.printStackTrace();
        } img = transparanty(img);
        double angle = Math.atan2(rockets.get(0).vSpeed, rockets.get(0).hSpeed);
        AffineTransform transform = new AffineTransform();
        transform.translate(img.getWidth() / 2, img.getWidth() / 2);
        transform.rotate(4 * Math.PI / 2 - angle, img.getWidth() / 2, img.getHeight()/ 2);
        transform.translate(-img.getWidth() /2 ,-img.getWidth() / 2);
        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        img = op.filter(img, null);
              
        BufferedImage img2 = null;
        try {
            img2 = ImageIO.read(new File(getClass().getResource("AAMissileCopy.png").getFile()));
        } catch (IOException e) {
            e.printStackTrace();
        } 
        img2 = transparanty(img2);
        angle = Math.PI - ( Math.atan2(rockets.get(1).vSpeed, rockets.get(1).hSpeed) - Math.PI / 2);
        AffineTransform transform2 = new AffineTransform();
        transform2.translate(img2.getWidth() / 2, img2.getWidth() / 2);
        transform2.rotate(angle, img2.getWidth() / 2, img2.getHeight()/ 2);
        transform2.translate(-img2.getWidth() / 2 + 4 ,-img2.getWidth() / 2);
        AffineTransformOp op2 = new AffineTransformOp(transform2, AffineTransformOp.TYPE_BILINEAR);
        img2 = op2.filter(img2, null);
        
        
        for (int i = 2; i < rockets.size(); i++){
            rockets.get(i).paint(g2d);
        }
        if (rockets.get(0).fired){
            rockets.get(0).paint(g2d);
            g2d.drawImage(img, (int) rockets.get(0).getCenterX() - img.getWidth() / 2, (int) rockets.get(0).getCenterY() - img.getHeight() / 2, null);
        }
        if (rockets.get(1).fired){
            rockets.get(1).paint(g2d);
            g2d.drawImage(img2, (int) rockets.get(1).getCenterX() - img2.getWidth() / 2, (int) rockets.get(1).getCenterY() - img2.getHeight() / 2, null);
        }
        g2d.setColor(Color.WHITE);
        
        g2d.drawString("Missile:", 10, 20);
        g2d.drawString("Angle: " + aM + "°", 10, 35);
        g2d.drawString("Velocity: " + vM + "m/s", 10, 50);
        
        g2d.drawString("Anti-Air Missile:", WIDTH - 150, 20);
        g2d.drawString("Angle: " + aAA + "°", WIDTH - 150, 35);
        g2d.drawString("Velocity: " + vAA + "m/s", WIDTH - 150, 50);
        g2d.drawString("Situated " + distanceAway + "m away", WIDTH - 150, 65);
        DecimalFormat df = new DecimalFormat("#.00"); 
        g2d.drawString("Collision at:", 10, HEIGHT - 80);
        g2d.drawString("( x = " + df.format(xCollide) + "m, y = " + df.format(yCollide) + "m)" , 10, HEIGHT - 65);
        g2d.drawString("After " + df.format(timeCollide) + "s", 10, HEIGHT - 50);
             
        g2d.drawString("Controls:", WIDTH - 420,  HEIGHT - 82);
        g2d.drawString("Speed: A and D for Missile; Left and Right arrow for AA.", WIDTH - 350,  HEIGHT - 80);
        g2d.drawString("Angle: W and S for Missile; Up and Down arrow for AA.", WIDTH - 350,  HEIGHT - 65);
        g2d.drawString("Pause Simulation: Space, Reset Simulation: Escape ", WIDTH - 350,  HEIGHT - 50);
    }
    //Listens for button presses
    public class MyKeyListener implements KeyListener{

        public void action(){
            if (keysDown.contains(KeyEvent.VK_SPACE)){
                running = !running;
            }
            if (keysDown.contains(KeyEvent.VK_ESCAPE)){
                reset();
            }
            if (keysDown.contains(KeyEvent.VK_W)){
                aM += 3;
            }
            if (keysDown.contains(KeyEvent.VK_S)){
                aM -= 3;
            }
            if (keysDown.contains(KeyEvent.VK_D)){
                vM += 5;
            }
            if (keysDown.contains(KeyEvent.VK_A)){
                vM -= 3;
            }
            if (keysDown.contains(KeyEvent.VK_UP)){
                aAA += 3;
            }
            if (keysDown.contains(KeyEvent.VK_DOWN)){
                aAA -= 3;
            }
            if (keysDown.contains(KeyEvent.VK_RIGHT)){
                vAA -= 5;
            }
            if (keysDown.contains(KeyEvent.VK_LEFT)){
                vAA += 3;
            }
        }
        @Override
        public void keyTyped(KeyEvent e) {
                       
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (!keysDown.contains(e.getKeyCode())){
               keysDown.add(e.getKeyCode()); 
            }
            action();
        }

        @Override
        public void keyReleased(KeyEvent e) {
            keysDown.remove(new Integer(e.getKeyCode()));
        }
    }
}
//Define other objects.
class Ball extends Ellipse2D.Float {

    Color colour;
    double[] startPos;
    double ha; //horizontal acceleration, + = -->
    double va; //vertical acceleration, + = ^
    double vSpeed; //vertical speed, + = ^
    double hSpeed; //horizontal speed, + = -->
    double initialVSpeed;
    double speed;
    int mass;
    boolean fired;
    double angle;

    public Ball(float x, float y, float r, double speed, double angle, boolean fire) {
        super(x + 20, y + HEIGHT - 100, r, r);
        colour = Color.GRAY;
        this.va = (int) 9.81;
        if (rockets.size()>0){
            angle = 90 + Math.abs(90 - angle); 
        }
        this.angle = angle;
        this.speed = speed;
        this.fired = fire;
        this.startPos = new double[2]; this.startPos[0] = x + 20; this.startPos[1] = y + HEIGHT - 100;
        this.ha = (int) 0;
        this.vSpeed = speed * Math.sin(Math.toRadians(angle));
        this.initialVSpeed = this.vSpeed;
        this.hSpeed = speed * Math.cos(Math.toRadians(angle));
        this.mass = 3;
    }
    public Ball (float x, float y){
        super(x + 20, y + HEIGHT - 100, 3,3);
        double random = Math.random() * 5;
        super.height = (float) (random / 2) + 1;
        super.width = (float) (random / 2) + 1;
        double speed = rockets.get(1).speed + rockets.get(0).speed;
        speed /= 10;
        double random2 = Math.random() * speed - speed / 2;
        double random3 = Math.random() * speed - speed;
        this.hSpeed = random2;this.vSpeed = random3;
        this.mass = 1;
        this.fired = true;
        colour= Color.BLACK;
        
    }
    public Ball(float x, float y, float r){
        super (x, y, r, r);
        this.fired = false;
    }

    public void move(int d, double s) {
        switch (d) {
            //if within the screen then move s up
            case 0:
                if (s < 0) {
                    //if (HEIGHT * SCALER - super.height > super.getCenterY() + s) {
                        super.y -= s;
                    //}
                } else {
                    //if (super.height < super.getCenterY() - s) {
                        super.y -= s;
                    //}
                }
                break;
            case 1:
                //if within the screen then move s to the right
                if (s < 0) {
                    //if (super.width < super.getCenterX() - s) {
                        super.x += s;
                    //}
                } else {
                    //if (WIDTH * SCALER - super.width > super.getCenterX() + s) {
                        super.x += s;
                    //}
                }
                break;
        }
    }

    public void paint(Graphics2D g) {
        g.setColor(colour);
        g.fill(this);
    }
}

/* Useful shortcuts:
grid (tab) = grid layout every 10 pixels, on a 100 pixel = 1 metre scale, that is 10 cm.
grid2 (tab) = grid layout every 100 pixels, on a 100 pixel = 1 metre scale, that is 1m.
Ball (tab) = creates the Ball class.
Rectangle (tab) = creates the Rectangle class.
*/