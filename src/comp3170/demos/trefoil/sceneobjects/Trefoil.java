package comp3170.demos.trefoil.sceneobjects;

import static com.jogamp.opengl.GL.GL_TRIANGLES;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;

import comp3170.Shader;

public class Trefoil extends SceneObject {
	
	private static final float TAU = (float) (Math.PI * 2);
	
	private static final int NSLICES = 100;
	private static final float CROSS_SECTION_SCALE = 0.4f;
			
	private Vector4f[] crossSection;
	private Vector4f[] vertices;
	private int vertexBuffer;
	private int[] indices;
	private int indexBuffer;

	
	public Trefoil(Shader shader) {
		super(shader);		
		
		// cross section is a square:
		//
		//  3-----2
		//  |     |
		//  |  *  |    Y
		//  |     |    |
		//  0-----1    +--X
				
		this.crossSection = new Vector4f[] {
			new Vector4f(-1, -1, 0, 1),
			new Vector4f( 1, -1, 0, 1),
			new Vector4f( 1,  1, 0, 1),
			new Vector4f(-1,  1, 0, 1),
		};

		this.vertices = new Vector4f[NSLICES * crossSection.length];

		Vector3f vUp = new Vector3f(0,0,1);
		
		Vector3f iAxis = new Vector3f(0,0,0);
		Vector3f jAxis = new Vector3f(0,0,0);
		Vector3f kAxis = new Vector3f(0,0,0);

		Vector4f iAxis4 = new Vector4f(0,0,0,0);
		Vector4f jAxis4 = new Vector4f(0,0,0,0);
		Vector4f kAxis4 = new Vector4f(0,0,0,0);

		Matrix4f scale = new Matrix4f();
		scale.scaling(CROSS_SECTION_SCALE);
		
		int k = 0;
		for (int i = 0; i < NSLICES; i++) {
			float t = i * TAU / NSLICES;  // [0, TAU)
			
			Vector4f origin = new Vector4f(0,0,0,1);
			
			origin.x = (float) (Math.sin(t) + 2 * Math.sin(2 * t));
			origin.y = (float) (Math.cos(t) - 2 * Math.cos(2 * t));
			origin.z = (float) -Math.sin(3*t);

			kAxis.x = (float) (Math.cos(t) + 4 * Math.cos(2 * t));
			kAxis.y = (float) (-Math.sin(t) + 4 * Math.sin(2 * t));
			kAxis.z = (float) (-3 * Math.cos(3*t));		
			kAxis.normalize();
				
			vUp.cross(kAxis, iAxis);	// i = vUp x k
			iAxis.normalize();			
			kAxis.cross(iAxis, jAxis);  // j = k x i
			jAxis.normalize();			
			
			iAxis4.set(iAxis, 0);
			jAxis4.set(jAxis, 0);
			kAxis4.set(kAxis, 0);
			
			// construct the transform M = [i j k T]
			
			Matrix4f matrix = new Matrix4f(iAxis4, jAxis4, kAxis4, origin);
			
			for (int j = 0; j < crossSection.length; j++) {
				vertices[k] = new Vector4f(crossSection[j]);
				vertices[k].mul(scale);
				vertices[k].mul(matrix, vertices[k]);	// v = M * S * p[j]
				
				k++;
			}
			
		}
		
		this.vertexBuffer = shader.createBuffer(vertices);		

		// index buffer contains one line for each slice
		// each line is [i, i+1]
		this.indices = new int[NSLICES * crossSection.length * 4];
		
		k = 0;
		for (int i = 0; i < NSLICES; i++) {
			for (int j = 0; j < crossSection.length; j++) {
				indices[k++] = i * crossSection.length + j;
				indices[k++] = i * crossSection.length + (j + 1) % crossSection.length;				

				indices[k++] = i * crossSection.length + j;
				indices[k++] = ((i+1) % NSLICES) * crossSection.length + j;
			
			}
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

//        gl.glDrawArrays(GL.GL_POINTS, 0, vertices.length);           	
		
		gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
		gl.glDrawElements(GL.GL_LINES, indices.length, GL.GL_UNSIGNED_INT, 0);		

	}
	
	
	

}
