#version 410

uniform vec3 u_ambientIntensity;  // (linear)
uniform vec3 u_diffuseIntensity;  // (linear)
uniform vec4 u_lightDirection;  // (WORLD)

uniform sampler2D u_texture;

in vec2 v_texcoord;		// texture coordinates
in vec4 v_normal;		// normal (WORLD)

const float GAMMA = 2.2f;

layout(location = 0) out vec4 o_colour;	// output to colour buffer (r,g,b,a)

void main() {
	vec4 s = normalize(u_lightDirection);
	vec4 n = normalize(v_normal);

	vec3 colour = texture(u_texture, v_texcoord).rgb;
	colour = pow(colour, vec3(GAMMA));
	
	vec3 ambient = u_ambientIntensity * colour;
	vec3 diffuse = u_diffuseIntensity * colour * max(0, dot(s, n));
	
	vec3 intensity = ambient + diffuse;
	
	vec3 brightness = pow(intensity, vec3(1./GAMMA));
    o_colour = vec4(brightness, 1);
}
