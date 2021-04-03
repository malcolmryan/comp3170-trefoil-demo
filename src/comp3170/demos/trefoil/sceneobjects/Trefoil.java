package comp3170.demos.trefoil.sceneobjects;

import org.joml.Vector3f;
import org.joml.Vector4f;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;

import comp3170.Shader;

public class Trefoil extends SceneObject {
	
	private static final float TAU = (float) (Math.PI * 2);
	
	private static final int NSLICES = 100;
			
	private Vector4f[] vertices;
	private int vertexBuffer;

	private int[] indices;

	private int indexBuffer;
	
	public Trefoil(Shader shader) {
		super(shader);

		this.vertices = new Vector4f[NSLICES * 2];

		Vector4f kAxis = new Vector4f(0,0,0,0);
		
		int k = 0;
		for (int i = 0; i < NSLICES; i++) {
			float t = i * TAU / NSLICES;  // [0, TAU)
			
			Vector4f v = new Vector4f(0,0,0,1);
			
			v.x = (float) (Math.sin(t) + 2 * Math.sin(2 * t));
			v.y = (float) (Math.cos(t) - 2 * Math.cos(2 * t));
			v.z = (float) -Math.sin(3*t);

			vertices[k++] = v;

			kAxis.x = (float) (Math.cos(t) + 4 * Math.cos(2 * t));
			kAxis.y = (float) (-Math.sin(t) + 4 * Math.sin(2 * t));
			kAxis.z = (float) (3 * Math.cos(3*t));		
			kAxis.normalize3();
			
			Vector4f v2 = new Vector4f(v);
			v2.add(kAxis);
			
			vertices[k++] = v2;
			
		}
		
		this.vertexBuffer = shader.createBuffer(vertices);		

		// index buffer contains one line for each slice
		// each line is [i, i+1]
		this.indices = new int[2 * NSLICES];
		
		for (int i = 0; i < NSLICES; i++) {
			indices[2*i] = 2*i;
			indices[2*i+1] = 2*i+1;
		}
		
		this.indexBuffer = shader.createIndexBuffer(indices);
		
	}

	@Override
	public void draw() {
		GL4 gl = (GL4) GLContext.getCurrentGL();

		shader.enable();		

		calcModelMatrix();
		shader.setUniform("u_modelMatrix", modelMatrix);
		shader.setUniform("u_colour", colour);
		shader.setAttribute("a_position", vertexBuffer);		

		gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
		gl.glDrawElements(GL.GL_LINES, indices.length, GL.GL_UNSIGNED_INT, 0);		

	}
	
	
	

}
