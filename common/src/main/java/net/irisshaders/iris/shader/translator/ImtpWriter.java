package net.irisshaders.iris.shader.translator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Ghi k\u1ebft qu\u1ea3 d\u1ecbch GLSL\u2192MSL ra file .imtp.
 */
public final class ImtpWriter {

	private ImtpWriter() {
	}

	/**
	 * Ghi m\u1ed9t ch\u1eefn s\u1ed1 program d\u00e3 d\u1ecbch ra file .imtp.
	 *
	 * @param output     file .imtp \u0111\u1ea1ch n\u1ed9i
	 * @param source     t\u00ean shaderpack ngu\u1ed3n
	 * @param programs   map programId \u2192 TranslationResult per stage (vertex/fragment/geometry)
	 * @param mcVersion  phi\u00ean b\u1ea3n Minecraft
	 * @throws IOException n\u1ebfu c\u00f3 l\u1ed3i ghi file
	 */
	public static void writeImtp(Path output, String source,
								 Map<String, Map<GlslToMslTranslator.ShaderStage, TranslationResult>> programs,
								 String mcVersion) throws IOException {
		Files.createDirectories(output.getParent());
		try (BufferedWriter w = new BufferedWriter(
				new OutputStreamWriter(Files.newOutputStream(output), StandardCharsets.UTF_8))) {

			w.write(ImtpFormat.MAGIC);
			w.newLine();
			w.write(String.format("%s=%d; %s=%s; %s=%s; %s=%d",
					ImtpFormat.VERSION_KEY, ImtpFormat.VERSION,
					ImtpFormat.SOURCE_KEY, escapePropertyValue(source),
					ImtpFormat.MC_KEY, escapePropertyValue(mcVersion),
					ImtpFormat.TIMESTAMP_KEY, System.currentTimeMillis()));
			w.newLine();
			w.newLine();

			w.write(ImtpFormat.PROGRAMS_SECTION);
			w.newLine();

			for (Map.Entry<String, Map<GlslToMslTranslator.ShaderStage, TranslationResult>> entry : programs.entrySet()) {
				String programId = entry.getKey();
				Map<GlslToMslTranslator.ShaderStage, TranslationResult> stages = entry.getValue();

				w.write(programId);
				w.write(" = {");
				w.newLine();

				TranslationResult vertex = stages.get(GlslToMslTranslator.ShaderStage.VERTEX);
				TranslationResult fragment = stages.get(GlslToMslTranslator.ShaderStage.FRAGMENT);
				TranslationResult geometry = stages.get(GlslToMslTranslator.ShaderStage.GEOMETRY);

				if (vertex != null && vertex.success()) {
					w.write("  vertex = ");
					writeMslString(w, vertex.mslSource());
					w.newLine();
				}
				if (fragment != null && fragment.success()) {
					w.write("  fragment = ");
					writeMslString(w, fragment.mslSource());
					w.newLine();
				}
				if (geometry != null && geometry.success()) {
					w.write("  geometry = ");
					writeMslString(w, geometry.mslSource());
					w.newLine();
				}

				w.write("}");
				w.newLine();
				w.newLine();
			}
		}
	}

	private static void writeMslString(BufferedWriter w, String msl) throws IOException {
		w.write("\"\"\"");
		w.newLine();
		w.write(msl);
		if (!msl.endsWith("\n")) {
			w.newLine();
		}
		w.write("\"\"\"");
	}

	private static String escapePropertyValue(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("\\", "\\\\").replace(";", "\\;").replace("\n", " ").replace("\r", " ");
	}

	/**
	 * Ch\u1eefn s\u1ed1 translation result t\u1eeb m\u1ed9t danh s\u00e1ch stage.
	 */
	public static Map<GlslToMslTranslator.ShaderStage, TranslationResult> stageMap(
			TranslationResult vertex, TranslationResult fragment, TranslationResult geometry) {
		java.util.Map<GlslToMslTranslator.ShaderStage, TranslationResult> map = new java.util.LinkedHashMap<>();
		if (vertex != null) map.put(GlslToMslTranslator.ShaderStage.VERTEX, vertex);
		if (fragment != null) map.put(GlslToMslTranslator.ShaderStage.FRAGMENT, fragment);
		if (geometry != null) map.put(GlslToMslTranslator.ShaderStage.GEOMETRY, geometry);
		return map;
	}
}
