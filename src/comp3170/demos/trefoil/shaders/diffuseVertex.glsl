#version 410

in vec4 a_position;	// vertex position (MODEL) 
in vec4 a_normal;	// normal (MODEL)
in vec2 a_texcoord;	// texture coordinates

uniform mat4 u_modelMatrix;			// MODEL -> WORLD
uniform mat4 u_viewMatrix;			// WORLD -> VIEW
uniform mat4 u_projectionMatrix;	// VIEW -> NDC
uniform mat4 u_normalMatrix;		// MODEL -> WORLD (no scale)

out vec4 v_normal;		// normal (WORLD)
out vec2 v_texcoord;	// texture coordinates

void main() {
	v_normal = u_normalMatrix * a_normal;
	v_texcoord = a_texcoord;
    gl_Position = u_projectionMatrix * u_viewMatrix * u_modelMatrix * a_position;
}

