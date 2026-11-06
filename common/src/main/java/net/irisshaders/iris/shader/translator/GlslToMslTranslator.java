package net.irisshaders.iris.shader.translator;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Translates GLSL shader source (OpenGL ES / desktop GLSL used by Iris) into MSL
 * (Metal Shading Language) for Metal on iOS and macOS.
 *
 * <p>Phase 1: replaces built-ins, strips precision qualifiers, converts texture sampling,
 * and renames main() to a Metal entry point with attribute qualifiers ([vertex], [[stage_in]],
 * [[color]], ...).
 *
 * <p>Phase 2 (later): full GLSL AST parsing, geometry/tessellation support, uniform buffer
 * std140 layout, image load/store, subpass input.
 */
public final class GlslToMslTranslator {
	private GlslToMslTranslator() {
	}

	/**
	 * Translate one GLSL shader source into MSL.
	 *
	 * @param glslSource GLSL source code
	 * @param stage shader stage (VERTEX / FRAGMENT / GEOMETRY / COMPUTE)
	 * @param entryPoint Metal entry point name (e.g. "vertex_main" / "fragment_main")
	 * @return translation result
	 */
	public static TranslationResult translate(String glslSource, ShaderStage stage, String entryPoint) {
		if (glslSource == null || glslSource.trim().isEmpty()) {
			return new TranslationResult(null, false, "Empty GLSL source", new String[0]);
		}

		String source = glslSource;

		// 1. Strip precision qualifiers (not present in MSL)
		source = stripPrecisionQualifiers(source);

		// 2. Replace built-in types and functions
		source = replaceBuiltins(source);

		// 3. Rename main() to the Metal entry point
		source = convertEntryPoint(source, stage, entryPoint);

		// 4. Convert texture calls: texture(sampler, uv) -> texture.sample(sampler, uv)
		//    This regex approach is approximate; a proper AST pass is needed for exactness.
		source = convertTextureCalls(source);

		String[] warnings = {
			"TODO: Uniform buffer layout (std140 -> Metal [[buffer(N)]]) not handled.",
			"TODO: Texture sampling with offset/Lod/Grad not handled exactly.",
			"TODO: Geometry/tessellation shaders not handled.",
		};

		return new TranslationResult(source, true, null, warnings);
	}

	private static String stripPrecisionQualifiers(String source) {
		for (String p : new String[]{"highp", "mediump", "lowp", "precision"}) {
			source = source.replaceAll("\\b" + p + "\\b", "");
		}
		// Remove whole precision declaration lines, e.g. precision highp float;
		source = source.replaceAll("(?m)^\\s*precision\\s+.*;\\s*$", "");
		return source;
	}

	private static String replaceBuiltins(String source) {
		// Replace types first (longest first to avoid partial matches)
		for (Map.Entry<String, String> e : GlslBuiltinMap.TYPES.entrySet()) {
			source = source.replaceAll("\\b" + Pattern.quote(e.getKey()) + "\\b", e.getValue());
		}
		// Replace functions
		for (Map.Entry<String, String> e : GlslBuiltinMap.FUNCTIONS.entrySet()) {
			source = source.replaceAll("\\b" + Pattern.quote(e.getKey()) + "\\b", e.getValue());
		}
		return source;
	}

	private static String convertEntryPoint(String source, ShaderStage stage, String entryPoint) {
		switch (stage) {
			case VERTEX:
			case FRAGMENT:
				source = source.replaceAll("\\bmain\\s*\\(", entryPoint + "(");
				break;
			default:
				// Keep main for geometry/compute (handled later)
				break;
		}
		return source;
	}

	private static String convertTextureCalls(String source) {
		source = source.replaceAll("\\btexture\\s*\\(", "texture.sample(");
		source = source.replaceAll("\\btexture\\.sampleLevel\\s*\\(", "texture.sampleLevel(");
		return source;
	}

	public enum ShaderStage {
		VERTEX, FRAGMENT, GEOMETRY, TESSELATION_CONTROL, TESSELATION_EVAL, COMPUTE
	}
}
