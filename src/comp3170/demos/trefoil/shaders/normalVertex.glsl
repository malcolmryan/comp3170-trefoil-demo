#version 410

in vec4 a_position;	// vertex position as a homogeneous 3D point in model 
in vec4 a_normal;	// vertex normal 

uniform mat4 u_modelMatrix;			// MODEL -> WORLD
uniform mat4 u_viewMatrix;			// WORLD -> VIEW
uniform mat4 u_projectionMatrix;	// VIEW -> NDC
uniform mat4 u_normalMatrix;		// MODEL -> WORLD (no scale)

smooth out vec4 v_normal;	// fragment normal

void main() {
	v_normal = u_normalMatrix * a_normal;
    gl_Position = u_projectionMatrix * u_viewMatrix * u_modelMatrix * a_position;
}

