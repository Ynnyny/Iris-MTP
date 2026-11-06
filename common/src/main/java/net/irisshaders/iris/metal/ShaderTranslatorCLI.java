package net.irisshaders.iris.metal;

import net.irisshaders.iris.shader.translator.GlslBuiltinMap;
import net.irisshaders.iris.shader.translator.GlslToMslTranslator;
import net.irisshaders.iris.shader.translator.ImtpFormat;
import net.irisshaders.iris.shader.translator.ImtpReader;
import net.irisshaders.iris.shader.translator.ImtpWriter;
import net.irisshaders.iris.shader.translator.TranslationResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CLI d\u1ecbch shaderpack GLSL sang .imtp.
 *
 * Usage:
 *   java net.irisshaders.iris.metal.ShaderTranslatorCLI <shaderpack-dir> <output-dir> [mc-version]
 */
public final class ShaderTranslatorCLI {

	private ShaderTranslatorCLI() {
	}

	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			System.err.println("Usage: ShaderTranslatorCLI <shaderpack-dir> <output-dir> [mc-version]");
			System.exit(1);
		}

		Path shaderpackDir = Paths.get(args[0]);
		Path outputDir = Paths.get(args[1]);
		String mcVersion = args.length > 2 ? args[2] : "26.3";

		if (!Files.isDirectory(shaderpackDir)) {
			System.err.println("Shaderpack directory not found: " + shaderpackDir);
			System.exit(1);
		}

		String packName = shaderpackDir.getFileName().toString();
		Path imtpOutput = outputDir.resolve(packName + ".imtp");

		Map<String, Map<GlslToMslTranslator.ShaderStage, TranslationResult>> programs = new LinkedHashMap<>();

		// Find shader files
		Map<String, String> vertexShaders = findShaders(shaderpackDir, ".vsh");
		Map<String, String> fragmentShaders = findShaders(shaderpackDir, ".fsh");
		Map<String, String> geometryShaders = findShaders(shaderpackDir, ".gsh");

		// Collect all program IDs
		var allProgramIds = new LinkedHashMap<String, Boolean>();
		vertexShaders.keySet().forEach(id -> allProgramIds.put(id, true));
		fragmentShaders.keySet().forEach(id -> allProgramIds.put(id, true));
		geometryShaders.keySet().forEach(id -> allProgramIds.put(id, true));

		Map<String, Integer> emptyBindings = Map.of();

		for (String programId : allProgramIds.keySet()) {
			Map<GlslToMslTranslator.ShaderStage, TranslationResult> stages = new LinkedHashMap<>();

			String vsh = vertexShaders.get(programId);
			if (vsh != null) {
				TranslationResult r = GlslToMslTranslator.translate(
						vsh, GlslToMslTranslator.ShaderStage.VERTEX,
						"vertex_" + programId, emptyBindings);
				stages.put(GlslToMslTranslator.ShaderStage.VERTEX, r);
				if (!r.success()) {
					System.err.println("WARN: " + programId + " vertex: " + r.errorMessage());
				}
			}

			String fsh = fragmentShaders.get(programId);
			if (fsh != null) {
				TranslationResult r = GlslToMslTranslator.translate(
						fsh, GlslToMslTranslator.ShaderStage.FRAGMENT,
						"fragment_" + programId, emptyBindings);
				stages.put(GlslToMslTranslator.ShaderStage.FRAGMENT, r);
				if (!r.success()) {
					System.err.println("WARN: " + programId + " fragment: " + r.errorMessage());
				}
			}

			String gsh = geometryShaders.get(programId);
			if (gsh != null) {
				TranslationResult r = GlslToMslTranslator.translate(
						gsh, GlslToMslTranslator.ShaderStage.GEOMETRY,
						"geometry_" + programId, emptyBindings);
				stages.put(GlslToMslTranslator.ShaderStage.GEOMETRY, r);
				if (!r.success()) {
					System.err.println("WARN: " + programId + " geometry: " + r.errorMessage());
				}
			}

			if (!stages.isEmpty()) {
				programs.put(programId, stages);
			}
		}

		ImtpWriter.writeImtp(imtpOutput, packName, programs, mcVersion);
		System.out.println("Translated " + programs.size() + " programs to " + imtpOutput);
	}

	private static Map<String, String> findShaders(Path dir, String ext) throws IOException {
		Map<String, String> result = new LinkedHashMap<>();
		if (!Files.isDirectory(dir)) {
			return result;
		}
		try (var stream = Files.list(dir)) {
			stream.filter(p -> p.getFileName().toString().endsWith(ext))
					.forEach(p -> {
						String id = p.getFileName().toString();
						id = id.substring(0, id.length() - ext.length());
						try {
							String content = Files.readString(p);
							result.put(id, content);
						} catch (IOException e) {
							System.err.println("Failed to read " + p + ": " + e.getMessage());
						}
					});
		}
		return result;
	}
}
