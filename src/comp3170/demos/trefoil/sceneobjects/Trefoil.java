package comp3170.demos.trefoil.sceneobjects;

import java.io.IOException;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;

import comp3170.demos.trefoil.shaders.ShaderLibrary;
import comp3170.demos.trefoil.textures.TextureLibrary;

public class Trefoil extends SceneObject {
	
	private final static String VERTEX_SHADER = "diffuseVertex.glsl";
	private final static String FRAGMENT_SHADER = "diffuseFragment.glsl";

	private static final float TAU = (float) (Math.PI * 2);
	
	private static final int NSLICES = 100;
	private static final float CROSS_SECTION_SCALE = 0.4f;
	private static final float U_MAX = 20;
	private static final float V_MAX = 1;	
	private static final String TEXTURE = "wood.jpg";
			
	private static final Vector3f AMBIENT_INTENSITY= new Vector3f(0.1f, 0.1f, 0.1f);
	private static final Vector3f DIFFUSE_INTENSITY= new Vector3f(1f, 1f, 1f);
	private static final Vector4f LIGHT_DIRECTION = new Vector4f(0f, 1f, 0f, 0);

	private Matrix4f normalMatrix = new Matrix4f();
	
	private Vector4f[] crossSection;
	private Vector3f[] crossSectionColour;

	private Vector4f[] vertices;
	private int vertexBuffer;
	private Vector4f[] normals;
	private int normalBuffer;
	private Vector3f[] colours;
	private int colourBuffer;
	private Vector2f[] uvs;
	private int uvBuffer;
	private int[] indices;
	private int indexBuffer;

	private int texture;
	private Vector4f[] crossSectionNormal;

	
	public Trefoil() {
		super(ShaderLibrary.compileShader(VERTEX_SHADER, FRAGMENT_SHADER));		
		
		createCrossSection();		
		createVertices();		
		createIndexBuffer();
		
		try {
			this.texture = TextureLibrary.loadTexture(TEXTURE);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}		
	}
	
	private void createCrossSection() {
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

		this.crossSectionNormal = new Vector4f[] {
			new Vector4f( 0, -1, 0, 0),
			new Vector4f( 1,  0, 0, 0),
			new Vector4f( 0,  1, 0, 0),
			new Vector4f(-1,  0, 0, 0),
		};

		this.crossSectionColour = new Vector3f[] {
			new Vector3f(1, 0, 0),		// Red
			new Vector3f(1, 1, 0),		// Yellow
			new Vector3f(0, 1, 0),		// Green
			new Vector3f(0, 0, 1),		// Blue
		};
	}



	private void createVertices() {
		this.vertices = new Vector4f[2* (NSLICES+1) * crossSection.length];
		this.normals = new Vector4f[vertices.length];
		this.colours = new Vector3f[vertices.length];
		this.uvs = new Vector2f[vertices.length];

		Vector3f vUp = new Vector3f(0,0,1);
		
		Vector3f iAxis = new Vector3f(0,0,0);
		Vector3f jAxis = new Vector3f(0,0,0);
		Vector3f kAxis = new Vector3f(0,0,0);

		Vector4f iAxis4 = new Vector4f(0,0,0,0);
		Vector4f jAxis4 = new Vector4f(0,0,0,0);
		Vector4f kAxis4 = new Vector4f(0,0,0,0);

		Matrix4f matrix = new Matrix4f();
				
		// Texture coordinates:
		//     u
		//   0                 u_max 
		//  0+---+---+ ... +---+
		//   |\  |\  |     |\  |
		// v | \ | \ |     | \ |
		//   |  \|  \|     |  \|
		//  1+---+---+ ... +---+
		//
		
		
		int k = 0;
		for (int i = 0; i <= NSLICES; i++) {
			float t = i * TAU / NSLICES;  // [0, TAU]
			
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
			
			// construct the transform M = [i j k T] * R * S
			matrix.set(iAxis4, jAxis4, kAxis4, origin);
			matrix.rotateZ(TAU / 4 * i / NSLICES);	// rotate by 90° after one full loop
			matrix.scale(CROSS_SECTION_SCALE);
			
			float u = i * U_MAX / NSLICES; // [0, U_MAX] 
			Vector2f uv0 = new Vector2f(u,0);
			Vector2f uv1 = new Vector2f(u,V_MAX);
			
			for (int j = 0; j < crossSection.length; j++) {
				// cross section
				//
				//    (u,0) (u,1)
				// (u,1) +---+ (u,0)
				//       |   |
				//       |   |
				//       |   |
				// (u,0) +---+ (u,1)
				//    (u,1) (u,0)
				
				vertices[k] = new Vector4f(crossSection[j]);
				vertices[k].mul(matrix, vertices[k]);	// v = M p[j]
				normals[k] = crossSectionNormal[j].mul(matrix, new Vector4f());
				colours[k] = crossSectionColour[j];
				uvs[k] = uv0;				
				k++;

				vertices[k] = new Vector4f(crossSection[(j+1) % crossSection.length]);
				vertices[k].mul(matrix, vertices[k]);	// v = M p[j+1]
				normals[k] = normals[k-1];
				colours[k] = crossSectionColour[(j+1) % crossSection.length];
				uvs[k] = uv1;				
				k++;

			}
			
		}
		
		this.vertexBuffer = shader.createBuffer(vertices);		
		this.normalBuffer = shader.createBuffer(normals);		
		this.colourBuffer = shader.createBuffer(colours);
		this.uvBuffer = shader.createBuffer(uvs);
	}

	private void createIndexBuffer() {
		this.indices = new int[NSLICES * crossSection.length * 2 * 3];
		
		//  i
		//   0   1   2     n-1 n
		//j 0+---+---+ ... +---+
		//   |\  |\  |     |\  |
		// 0 | \ | \ |     | \ |
		//   |  \|  \|     |  \|
		//  1+---+---+ ... +---+
		//  2+---+---+ ... +---+
		//   |\  |\  |     |\  |
		// 1 | \ | \ |     | \ |
		//   |  \|  \|     |  \|
		//  3+---+---+ ... +---+
		//  4+---+---+ ... +---+
		//   |\  |\  |     |\  |
		// 2 | \ | \ |     | \ |
		//   |  \|  \|     |  \|
		//  5+---+---+ ... +---+
		//  6+---+---+ ... +---+
		//   |\  |\  |     |\  |
		// 3 | \ | \ |     | \ |
		//   |  \|  \|     |  \|
		//  7+---+---+ ... +---+
		
		
		int k = 0;
		for (int i = 0; i < NSLICES; i++) {
			for (int j = 0; j < crossSection.length; j++) {
				int i2 = i + 1;
				
				indices[k++] = i * crossSection.length * 2 + 2 * j;
				indices[k++] = i * crossSection.length * 2 + 2 * j + 1;		
				indices[k++] = i2 * crossSection.length * 2 + 2 * j;		
				
				indices[k++] = i2 * crossSection.length * 2 + 2 * j + 1;				
				indices[k++] = i2 * crossSection.length * 2 + 2 * j;			
				indices[k++] = i * crossSection.length * 2 + 2 * j + 1;				
			}
		}

		
		this.indexBuffer = shader.createIndexBuffer(indices);
	}


	@Override
	public void draw(Matrix4f viewMatrix, Matrix4f projectionMatrix) {
		GL4 gl = (GL4) GLContext.getCurrentGL();

		shader.enable();		

		calcModelMatrix();
		shader.setUniform("u_modelMatrix", modelMatrix);
		shader.setUniform("u_viewMatrix", viewMatrix);
		shader.setUniform("u_projectionMatrix", projectionMatrix);
		shader.setUniform("u_normalMatrix", modelMatrix.normal(normalMatrix));
		
		shader.setAttribute("a_position", vertexBuffer);		
		shader.setAttribute("a_texcoord", uvBuffer);		
		shader.setAttribute("a_normal", normalBuffer);		

		gl.glActiveTexture(GL.GL_TEXTURE0);
		gl.glBindTexture(GL.GL_TEXTURE_2D, this.texture);
		shader.setUniform("u_texture", 0);
		
		shader.setUniform("u_ambientIntensity", AMBIENT_INTENSITY);
		shader.setUniform("u_diffuseIntensity", DIFFUSE_INTENSITY);
		shader.setUniform("u_lightDirection", LIGHT_DIRECTION);
		
		gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
		gl.glDrawElements(GL.GL_TRIANGLES, indices.length, GL.GL_UNSIGNED_INT, 0);		

	}
	
	
	

}
