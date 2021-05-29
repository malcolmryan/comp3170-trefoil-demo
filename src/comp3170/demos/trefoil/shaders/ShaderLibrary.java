package comp3170.demos.trefoil.shaders;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import comp3170.GLException;
import comp3170.Shader;

public class ShaderLibrary {

	final private static File DIRECTORY = new File("src/comp3170/demos/trefoil/shaders"); 
	
	private final static Map<Pair<String, String>, Shader> loadedShaders = new HashMap<Pair<String, String>, Shader>();

	/**
	 * Load a given vertex and fragment shader from the shaders folder and link them together.
	 * 
	 * @param vertex	The filename of the vertex shader
	 * @param fragment	The filename of the fragement shader
	 * @return The resulting shader
	 */
	
	public static Shader compileShader(String vertex, String fragment) {
		
		// if the shader is already loaded, return a stored copy
		
		var pair = new Pair<String, String>(vertex, fragment);
		if (loadedShaders.containsKey(pair)) {
			return loadedShaders.get(pair);
		}
		
		Shader shader = null;
		try {
			File vertexShader = new File(DIRECTORY, vertex);
			File fragmentShader = new File(DIRECTORY, fragment);
			shader = new Shader(vertexShader, fragmentShader);		

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (GLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		loadedShaders.put(pair, shader);
		
		return shader;

	}

	public static class Pair<A, B> {
	    private final A first;
	    private final B second;

	    public Pair(A first, B second) {
	        super();
	        this.first = first;
	        this.second = second;
	    }

	    public int hashCode() {
	        int hashFirst = first != null ? first.hashCode() : 0;
	        int hashSecond = second != null ? second.hashCode() : 0;

	        return (hashFirst + hashSecond) * hashSecond + hashFirst;
	    }

	    public boolean equals(Object other) {
	        if (other instanceof Pair) {
	            Pair otherPair = (Pair) other;
	            return 
	            ((  this.first == otherPair.first ||
	                ( this.first != null && otherPair.first != null &&
	                  this.first.equals(otherPair.first))) &&
	             (  this.second == otherPair.second ||
	                ( this.second != null && otherPair.second != null &&
	                  this.second.equals(otherPair.second))) );
	        }

	        return false;
	    }

	    public String toString()
	    { 
	           return "(" + first + ", " + second + ")"; 
	    }

	    public A getFirst() {
	        return first;
	    }

	    public B getSecond() {
	        return second;
	    }

	}	
}
