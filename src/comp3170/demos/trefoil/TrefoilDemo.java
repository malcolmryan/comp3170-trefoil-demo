package comp3170.demos.trefoil;

import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.Animator;

import comp3170.GLException;
import comp3170.Shader;
import comp3170.demos.trefoil.sceneobjects.Axes;
import comp3170.demos.trefoil.sceneobjects.Trefoil;

public class TrefoilDemo extends JFrame implements GLEventListener {

	public static final float TAU = (float) (2 * Math.PI);		// https://tauday.com/tau-manifesto
	
	private int width = 800;
	private int height = 800;

	private GLCanvas canvas;
	private Shader shader;
	
	final private File DIRECTORY = new File("src/comp3170/demos/trefoil"); 
	final private String VERTEX_SHADER = "vertex.glsl";
	final private String FRAGMENT_SHADER = "fragment.glsl";

	private Animator animator;
	private long oldTime;
	private InputManager input;

	private Axes axes;
	private Trefoil trefoil;

	private Matrix4f viewMatrix;
	private Matrix4f projectionMatrix;


	public TrefoilDemo() {
		super("Trefoil demo");

		// set up a GL canvas
		GLProfile profile = GLProfile.get(GLProfile.GL4);		 
		GLCapabilities capabilities = new GLCapabilities(profile);
		this.canvas = new GLCanvas(capabilities);
		this.canvas.addGLEventListener(this);
		this.add(canvas);
		
		// set up Animator		

		this.animator = new Animator(canvas);
		this.animator.start();
		this.oldTime = System.currentTimeMillis();		

		// input
		
		this.input = new InputManager(canvas);
		
		// set up the JFrame
		
		this.setSize(width,height);
		this.setVisible(true);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}

	@Override
	public void init(GLAutoDrawable arg0) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		// set the background colour to black
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		
		// Compile the shader
		try {
			File vertexShader = new File(DIRECTORY, VERTEX_SHADER);
			File fragementShader = new File(DIRECTORY, FRAGMENT_SHADER);
			this.shader = new Shader(vertexShader, fragementShader);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (GLException e) {
			e.printStackTrace();
			System.exit(1);
		}

		this.axes = new Axes(shader);
		this.trefoil = new Trefoil(shader);
		
		this.viewMatrix = new Matrix4f();
		this.projectionMatrix = new Matrix4f();
	}

	
	private static final float ROTATION_SPEED = TAU / 4;
	
	private void update() {
		long time = System.currentTimeMillis();
		float deltaTime = (time-oldTime) / 1000f;
		oldTime = time;
		
		Vector3f angle = new Vector3f();
		this.trefoil.getAngle(angle);
		angle.y += ROTATION_SPEED * deltaTime;
		this.trefoil.setAngle(angle);
				
		input.clear();
	}
	
	private static final float CAMERA_DISTANCE = 5;
	private static final float CAMERA_WIDTH = 8;
	private static final float CAMERA_HEIGHT = 8;
	private static final float CAMERA_NEAR = 1;
	private static final float CAMERA_FAR = 10;
	
	@Override	
	public void display(GLAutoDrawable arg0) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		update();
		
        // clear the colour buffer
		gl.glClear(GL_COLOR_BUFFER_BIT);		
		
		//  Y up W--X
		//       |
		//       Z (out of screen)
		//
		//    
		//    (0,0,3)
		//  Y up C--X
		//       |
		//       Z
		
		viewMatrix.identity();
		viewMatrix.translate(0,0,CAMERA_DISTANCE);
		viewMatrix.invert();

		projectionMatrix.setOrtho(
				-CAMERA_WIDTH/2, CAMERA_WIDTH/2, 
				-CAMERA_HEIGHT/2, CAMERA_HEIGHT/2, 
				CAMERA_NEAR, CAMERA_FAR);
		
		shader.setUniform("u_viewMatrix", viewMatrix);
		shader.setUniform("u_projectionMatrix", projectionMatrix);
		
		// draw the scene
		this.axes.draw();
		this.trefoil.draw();
		
	}

	@Override
	public void reshape(GLAutoDrawable d, int x, int y, int width, int height) {
		this.width = width;
		this.height = height;		
	}

	@Override
	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) { 
		new TrefoilDemo();
	}


}
