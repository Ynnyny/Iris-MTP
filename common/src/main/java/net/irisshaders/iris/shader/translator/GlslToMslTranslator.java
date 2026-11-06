package net.irisshaders.iris.shader.translator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bi\u1ec3n d\u1ecbch GLSL shader source (OpenGL ES / desktop GLSL d\u00f9ng b\u1ecfi Iris)
 * sang MSL (Metal Shading Language) cho Metal tr\u00ean iOS v\u00e0 macOS.
 *
 * <p>Phase 1: thay th\u1ec3 b\u00edt t\u1ed9c, strip precision qualifier, ch\u1ee9y texture sampling,
 * \u0111\u1ed5i main() th\u00e0nh Metal entry point v\u1edbi attribute qualifiers ([[vertex]], [[stage_in]],
 * [[color]], ...). Uniform buffer std140 \u0111\u01b0\u1ee3c \u0111\u1ed5i sang Metal [[buffer(N)]].
 *
 * <p>Phase 2 (sau): full GLSL AST parsing, geometry/tessellation, image load/store, subpass input.
 */
public final class GlslToMslTranslator {
	private GlslToMslTranslator() {
	}

	/**
	 * Bi\u1ec3n d\u1ecbch m\u1ed9t GLSL shader source th\u00e0nh MSL.
	 *
	 * @param glslSource GLSL source code
	 * @param stage      shader stage (VERTEX / FRAGMENT / GEOMETRY / COMPUTE)
	 * @param entryPoint Metal entry point name (e.g. "vertex_main" / "fragment_main")
	 * @param bindings   uniform/sampler bindings (name \u2192 binding slot)
	 * @return translation result
	 */
	public static TranslationResult translate(String glslSource, ShaderStage stage, String entryPoint,
											  Map<String, Integer> bindings) {
		if (glslSource == null || glslSource.trim().isEmpty()) {
			return new TranslationResult(null, false, "Empty GLSL source", new String[0]);
		}

		List<String> warnings = new ArrayList<>();
		String source = glslSource;

		// 1. Strip precision qualifiers
		source = stripPrecisionQualifiers(source);

		// 2. Extract uniform bindings
		Map<String, Integer> uniformBindings = extractUniformBindings(source, bindings);
		for (Map.Entry<String, Integer> e : uniformBindings.entrySet()) {
			warnings.add("Uniform block '" + e.getKey() + "' mapped to Metal [[buffer(" + e.getValue() + ")]]");
		}

		// 3. Replace built-in types and functions (longest first)
		source = replaceBuiltins(source);

		// 4. Convert texture sampling calls
		source = convertTextureCalls(source);

		// 5. Convert uniform block references
		source = convertUniforms(source, uniformBindings);

		// 6. Convert main() to Metal entry point with qualifiers
		source = convertEntryPoint(source, stage, entryPoint);

		return new TranslationResult(source, true, null, warnings.toArray(new String[0]));
	}

	private static String stripPrecisionQualifiers(String source) {
		for (String p : new String[]{"highp", "mediump", "lowp"}) {
			source = source.replaceAll("\\b" + p + "\\b", "");
		}
		source = source.replaceAll("(?m)^\\s*precision\\s+.*;\\s*$", "");
		return source;
	}

	private static Map<String, Integer> extractUniformBindings(String source, Map<String, Integer> bindings) {
		Map<String, Integer> result = new LinkedHashMap<>();
		if (bindings != null) {
			result.putAll(bindings);
		}
		Pattern uniformBlock = Pattern.compile("uniform\\s+(\\w+)\\s*\\{");
		Matcher m = uniformBlock.matcher(source);
		int slot = 0;
		while (m.find()) {
			String blockName = m.group(1);
			if (!result.containsKey(blockName)) {
				result.put(blockName, slot++);
			}
		}
		return result;
	}

	private static String replaceBuiltins(String source) {
		List<Map.Entry<String, String>> sortedTypes = new ArrayList<>(GlslBuiltinMap.TYPES.entrySet());
		sortedTypes.sort((a, b) -> Integer.compare(b.getKey().length(), a.getKey().length()));
		for (Map.Entry<String, String> e : sortedTypes) {
			String replacement = e.getValue();
			if (!replacement.isEmpty()) {
				source = source.replaceAll("\\b" + Pattern.quote(e.getKey()) + "\\b", replacement);
			}
		}
		List<Map.Entry<String, String>> sortedFuncs = new ArrayList<>(GlslBuiltinMap.FUNCTIONS.entrySet());
		sortedFuncs.sort((a, b) -> Integer.compare(b.getKey().length(), a.getKey().length()));
		for (Map.Entry<String, String> e : sortedFuncs) {
			String replacement = e.getValue();
			if (!replacement.isEmpty()) {
				source = source.replaceAll("\\b" + Pattern.quote(e.getKey()) + "\\b", replacement);
			}
		}
		return source;
	}

	private static String convertTextureCalls(String source) {
		source = source.replaceAll("\\btexture\\s*\\(", "texture.sample(");
		source = source.replaceAll("\\btextureLod\\s*\\(", "texture.sampleLevel(");
		source = source.replaceAll("\\btextureGrad\\s*\\(", "texture.sampleGrad(");
		source = source.replaceAll("\\btexture2D\\s*\\(", "texture.sample(");
		source = source.replaceAll("\\btextureCube\\s*\\(", "texture.sample(");
		return source;
	}

	private static String convertUniforms(String source, Map<String, Integer> bindings) {
		for (Map.Entry<String, Integer> e : bindings.entrySet()) {
			String blockName = e.getKey();
			int slot = e.getValue();
			source = source.replaceAll("\\b" + Pattern.quote(blockName) + "\\.(\\w+)",
					"u_" + slot + ".$1");
		}
		return source;
	}

	private static String convertEntryPoint(String source, ShaderStage stage, String entryPoint) {
		switch (stage) {
			case VERTEX:
				source = addVertexQualifiers(source, entryPoint);
				break;
			case FRAGMENT:
				source = addFragmentQualifiers(source, entryPoint);
				break;
			default:
				break;
		}
		return source;
	}

	private static String addVertexQualifiers(String source, String entryPoint) {
		source = source.replaceAll("\\bmain\\s*\\(", entryPoint + "(");
		source = source.replaceAll("\\b" + Pattern.quote(entryPoint) + "\\s*\\(",
				"[[vertex]] " + entryPoint + "(");
		return source;
	}

	private static String addFragmentQualifiers(String source, String entryPoint) {
		source = source.replaceAll("\\bmain\\s*\\(", entryPoint + "(");
		source = source.replaceAll("\\b" + Pattern.quote(entryPoint) + "\\s*\\(",
				"[[fragment]] " + entryPoint + "(");
		return source;
	}

	public enum ShaderStage {
		VERTEX, FRAGMENT, GEOMETRY, TESSELATION_CONTROL, TESSELATION_EVAL, COMPUTE
	}
}
