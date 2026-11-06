package net.irisshaders.iris.shader.translator;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * B\u1ea3ng \u00e1nh x\u1ea5 c\u00e1c h\u00e0m/\u01b0a t\u1ecd g\u1eedi t\u1eeb GLSL sang MSL.
 * \u0110\u1ea7y \u0111\u1ea7y \u0111\u1ee3 c\u00e1c built-in c\u1ee7a Iris v\u00e0 Sodium.
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
		FUNCTIONS.put("textureOffset", "texture.sample"); // offset handled separately
		FUNCTIONS.put("texture2DLod", "texture.sampleLevel");
		FUNCTIONS.put("texture2DGrad", "texture.sampleGrad");
		FUNCTIONS.put("texture2DOffset", "texture.sample");
		FUNCTIONS.put("textureCube", "texture.sample");
		FUNCTIONS.put("textureCubeLod", "texture.sampleLevel");
		FUNCTIONS.put("textureShadow", "texture.sample"); // shadow comparison
		FUNCTIONS.put("texture2DShadow", "texture.sample");
		FUNCTIONS.put("texture2DArray", "texture.sample");
		FUNCTIONS.put("texture2DArrayLod", "texture.sampleLevel");
		FUNCTIONS.put("texture2DArrayShadow", "texture.sample");
		FUNCTIONS.put("texelFetch", "texture.read");
		FUNCTIONS.put("texelFetchOffset", "texture.read");
		FUNCTIONS.put("textureSize", "texture.get_width"); // approximated
		FUNCTIONS.put("textureQueryLevels", "texture.get_num_mip_levels"); // approximated
		FUNCTIONS.put("textureQueryLod", "texture.sample_level"); // approximated

		// Derivatives
		FUNCTIONS.put("dFdx", "dpdx");
		FUNCTIONS.put("dFdy", "dpdy");
		FUNCTIONS.put("fwidth", "fwidth");

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
		FUNCTIONS.put("degrees", "degrees");
		FUNCTIONS.put("radians", "radians");
		FUNCTIONS.put("sin", "sin");
		FUNCTIONS.put("cos", "cos");
		FUNCTIONS.put("tan", "tan");
		FUNCTIONS.put("asin", "asin");
		FUNCTIONS.put("acos", "acos");
		FUNCTIONS.put("atan", "atan");
		FUNCTIONS.put("pow", "pow");
		FUNCTIONS.put("exp", "exp");
		FUNCTIONS.put("log", "log");
		FUNCTIONS.put("exp2", "exp2");
		FUNCTIONS.put("log2", "log2");
		FUNCTIONS.put("sqrt", "sqrt");
		FUNCTIONS.put("abs", "abs");
		FUNCTIONS.put("sign", "sign");
		FUNCTIONS.put("floor", "floor");
		FUNCTIONS.put("ceil", "ceil");
		FUNCTIONS.put("round", "round");
		FUNCTIONS.put("trunc", "trunc");
		FUNCTIONS.put("min", "min");
		FUNCTIONS.put("max", "max");
		FUNCTIONS.put("step", "step");
		FUNCTIONS.put("smoothstep", "smoothstep");
		FUNCTIONS.put("length", "length");
		FUNCTIONS.put("distance", "distance");
		FUNCTIONS.put("transform", ""); // removed, handled by uniforms
		FUNCTIONS.put("transpose", "transpose");
		FUNCTIONS.put("determinant", "determinant");
		FUNCTIONS.put("inverse", "inverse");
		FUNCTIONS.put("outerProduct", ""); // not in MSL
		FUNCTIONS.put("matrixCompMult", ""); // component-wise multiply

		// Types
		TYPES.put("sampler2D", "texture2d<float, access::sample>");
		TYPES.put("sampler2DArray", "texture2d_array<float, access::sample>");
		TYPES.put("samplerCube", "texturecube<float, access::sample>");
		TYPES.put("sampler2DShadow", "depth2d<float, access::sample>");
		TYPES.put("sampler2DRect", "texture2d<float, access::sample>");
		TYPES.put("isampler2D", "texture2d<int, access::sample>");
		TYPES.put("usampler2D", "texture2d<uint, access::sample>");
		TYPES.put("sampler2DMS", "texture2d_ms<float, access::read>");
		TYPES.put("sampler2DArrayShadow", "depth2d_array<float, access::sample>");
		TYPES.put("samplerCubeShadow", "depthcube<float, access::sample>");

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
		TYPES.put("mat2x2", "float2x2");
		TYPES.put("mat2x3", "float2x3");
		TYPES.put("mat2x4", "float2x4");
		TYPES.put("mat3x2", "float3x2");
		TYPES.put("mat3x3", "float3x3");
		TYPES.put("mat3x4", "float3x4");
		TYPES.put("mat4x2", "float4x2");
		TYPES.put("mat4x3", "float4x3");
		TYPES.put("mat4x4", "float4x4");
		TYPES.put("bvec2", "bool2");
		TYPES.put("bvec3", "bool3");
		TYPES.put("bvec4", "bool4");
		TYPES.put("bool", "bool");
		TYPES.put("float", "float");
		TYPES.put("int", "int");
		TYPES.put("uint", "uint");
		TYPES.put("void", "void");

		// Precision qualifiers (stripped)
		TYPES.put("highp", "");
		TYPES.put("mediump", "");
		TYPES.put("lowp", "");
		TYPES.put("precision", "");
	}
}
