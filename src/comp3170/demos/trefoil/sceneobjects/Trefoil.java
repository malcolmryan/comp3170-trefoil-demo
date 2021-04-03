package comp3170.demos.trefoil.sceneobjects;

import org.joml.Vector4f;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;

import comp3170.Shader;

public class Trefoil extends SceneObject {
	
	private static final float TAU = (float) (Math.PI * 2);
	
	private static final int NSLICES = 1000;
	private Vector4f[] vertices;
	private int vertexBuffer;
	
	public Trefoil(Shader shader) {
		super(shader);

		this.vertices = new Vector4f[NSLICES];
		
		for (int i = 0; i < NSLICES; i++) {
			float t = i * TAU / NSLICES;  // [0, TAU)
			
			Vector4f v = new Vector4f(0,0,0,1);
			
			v.x = (float) (Math.sin(t) + 2 * Math.sin(2 * t));
			v.y = (float) (Math.cos(t) - 2 * Math.cos(2 * t));
			v.z = (float) -Math.sin(3*t);
			
			vertices[i] = v;
		}
		
		this.vertexBuffer = shader.createBuffer(vertices);		

	}

	@Override
	public void draw() {
		GL4 gl = (GL4) GLContext.getCurrentGL();

		shader.enable();		

		calcModelMatrix();
		shader.setUniform("u_modelMatrix", modelMatrix);
		shader.setUniform("u_colour", colour);
		shader.setAttribute("a_position", vertexBuffer);		

        gl.glDrawArrays(GL.GL_POINTS, 0, vertices.length);           	
	}
	
	
	

}
