package net.irisshaders.iris.shader.translator;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * B\u1ea3ng \u00e1nh x\u1ea5 c\u00e1c h\u00e0m/\u01b0a t\u1ecd g\u1eedi t\u1eeb GLSL sang MSL.
 * \u0110\u00e3y l\u00e0 n\u01a1i dung c\u01a1 s\u1edf, c\u1ea7n b\u00f9 sung \u0111\u1ea7y \u0111\u1ee3 c\u00e1c built-in c\u1ee7a Iris
 * v\u00e0 Sodium (texture, textureLod, textureGrad, textureOffset, dFdx, dFdy, inversesqrt, ...).
 */
public final class GlslBuiltinMap {
	private GlslBuiltinMap() {
	}

	public static final Map<String, String> FUNCTIONS = new LinkedHashMap<>();

	public static final Map<String, String> TYPES = new LinkedHashMap<>();

	static {
		// Texture sampling
		FUNCTIONS.put("texture2D", "texture.sample");
		FUNCTIONS.put("texture", "texture.sample");
		FUNCTIONS.put("textureLod", "texture.sampleLevel");
		FUNCTIONS.put("textureGrad", "texture.sampleGrad");
		FUNCTIONS.put("textureOffset", "texture.sample"); // TODO: \u0111\u1ed9i offset v\u00e0o sampler

		// Math
		FUNCTIONS.put("inversesqrt", "rsqrt");
		FUNCTIONS.put("mod", "fmod");
		FUNCTIONS.put("mix", "mix");
		FUNCTIONS.put("clamp", "clamp");
		FUNCTIONS.put("dot", "dot");
		FUNCTIONS.put("cross", "cross");
		FUNCTIONS.put("normalize", "normalize");
		FUNCTIONS.put("reflect", "reflect");
		FUNCTIONS.put("refract", "refract");
		FUNCTIONS.put("faceforward", "faceforward");
		FUNCTIONS.put("dFdx", "dpdx");
		FUNCTIONS.put("dFdy", "dpdy");
		FUNCTIONS.put("fwidth", "fwidth");

		// Types
		TYPES.put("sampler2D", "texture2d<float, access::sample>");
		TYPES.put("sampler2DArray", "texture2d_array<float, access::sample>");
		TYPES.put("samplerCube", "texturecube<float, access::sample>");
		TYPES.put("sampler2DShadow", "depth2d<float, access::sample>");
		TYPES.put("sampler2DRect", "texture2d<float, access::sample>");
		TYPES.put("isampler2D", "texture2d<int, access::sample>");
		TYPES.put("usampler2D", "texture2d<uint, access::sample>");

		TYPES.put("ivec2", "int2");
		TYPES.put("ivec3", "int3");
		TYPES.put("ivec4", "int4");
		TYPES.put("uvec2", "uint2");
		TYPES.put("uvec3", "uint3");
		TYPES.put("uvec4", "uint4");
		TYPES.put("vec2", "float2");
		TYPES.put("vec3", "float3");
		TYPES.put("vec4", "float4");
		TYPES.put("mat2", "float2x2");
		TYPES.put("mat3", "float3x3");
		TYPES.put("mat4", "float4x4");
		TYPES.put("mat2x3", "float2x3");
		TYPES.put("mat3x2", "float3x2");
		TYPES.put("mat4x3", "float4x3");
		TYPES.put("bvec2", "bool2");
		TYPES.put("bvec3", "bool3");
		TYPES.put("bvec4", "bool4");
		TYPES.put("bool", "bool");
		TYPES.put("float", "float");
		TYPES.put("int", "int");
		TYPES.put("uint", "uint");
		TYPES.put("void", "void");
		TYPES.put("sampler2DMS", "texture2d_ms<float, access::read>");

		TYPES.put("highp", "");
		TYPES.put("mediump", "");
		TYPES.put("lowp", "");
		TYPES.put("precision", "");
	}
}
