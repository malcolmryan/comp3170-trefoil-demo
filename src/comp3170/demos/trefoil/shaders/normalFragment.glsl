#version 410

smooth in vec4 v_normal;	// fragment normal

layout(location = 0) out vec4 o_colour;	// output to colour buffer (r,g,b,a)

void main() {
	vec3 n = v_normal.xyz;
    o_colour = vec4(n,1);
}
