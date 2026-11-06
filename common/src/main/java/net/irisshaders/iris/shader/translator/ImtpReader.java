package net.irisshaders.iris.shader.translator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * \u0110\u1ec3c file .imtp, tr\u1ea3 v\u1ec1 MSL source per program/stage.
 */
public final class ImtpReader {

	private ImtpReader() {
	}

	/**
	 * \u0110\u1ec3c t\u1ea5t c\u1ea3 c\u00e1c program t\u1eeb file .imtp.
	 *
	 * @return map programId \u2192 map ShaderStage \u2192 MSL source
	 * @throws IOException n\u1ebfu c\u00f3 l\u1ed3i \u0111\u1ec3c file
	 */
	public static Map<String, Map<GlslToMslTranslator.ShaderStage, String>> readImtp(Path path) throws IOException {
		Map<String, Map<GlslToMslTranslator.ShaderStage, String>> result = new LinkedHashMap<>();

		try (BufferedReader r = new BufferedReader(
				new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8))) {

			String line;
			String currentProgram = null;
			GlslToMslTranslator.ShaderStage currentStage = null;
			StringBuilder currentSource = new StringBuilder();
			boolean inMslBlock = false;

			while ((line = r.readLine()) != null) {
				String trimmed = line.trim();

				// Skip header lines and empty lines
				if (trimmed.isEmpty() || trimmed.startsWith("#")) {
					continue;
				}

				if (trimmed.equals(ImtpFormat.PROGRAMS_SECTION)) {
					continue;
				}

				// Check for program start: programId = {
				if (trimmed.endsWith(" = {")) {
					// Flush previous program stage
					flushProgram(result, currentProgram, currentStage, currentSource);
					currentProgram = trimmed.substring(0, trimmed.indexOf(" = {")).trim();
					currentStage = null;
					currentSource = new StringBuilder();
					inMslBlock = false;
					continue;
				}

				// Check for end of program: }
				if (trimmed.equals("}")) {
					flushProgram(result, currentProgram, currentStage, currentSource);
					currentProgram = null;
					currentStage = null;
					currentSource = new StringBuilder();
					inMslBlock = false;
					continue;
				}

				// Check for stage assignment: vertex = """..."""
				if (currentProgram != null && trimmed.startsWith("vertex = \"\"\"")) {
					flushProgram(result, currentProgram, currentStage, currentSource);
					currentStage = GlslToMslTranslator.ShaderStage.VERTEX;
					currentSource = new StringBuilder();
					inMslBlock = true;
					String content = trimmed.substring(trimmed.indexOf("\"\"\"") + 3);
					if (content.endsWith("\"\"\"")) {
						inMslBlock = false;
						currentSource.append(content, 0, content.length() - 3);
						saveStage(result, currentProgram, currentStage, currentSource.toString());
					}
					continue;
				}
				if (currentProgram != null && trimmed.startsWith("fragment = \"\"\"")) {
					flushProgram(result, currentProgram, currentStage, currentSource);
					currentStage = GlslToMslTranslator.ShaderStage.FRAGMENT;
					currentSource = new StringBuilder();
					inMslBlock = true;
					String content = trimmed.substring(trimmed.indexOf("\"\"\"") + 3);
					if (content.endsWith("\"\"\"")) {
						inMslBlock = false;
						currentSource.append(content, 0, content.length() - 3);
						saveStage(result, currentProgram, currentStage, currentSource.toString());
					}
					continue;
				}
				if (currentProgram != null && trimmed.startsWith("geometry = \"\"\"")) {
					flushProgram(result, currentProgram, currentStage, currentSource);
					currentStage = GlslToMslTranslator.ShaderStage.GEOMETRY;
					currentSource = new StringBuilder();
					inMslBlock = true;
					String content = trimmed.substring(trimmed.indexOf("\"\"\"") + 3);
					if (content.endsWith("\"\"\"")) {
						inMslBlock = false;
						currentSource.append(content, 0, content.length() - 3);
						saveStage(result, currentProgram, currentStage, currentSource.toString());
					}
					continue;
				}

				// Inside MSL block
				if (inMslBlock && currentProgram != null && currentStage != null) {
					if (trimmed.endsWith("\"\"\"")) {
						currentSource.append(trimmed, 0, trimmed.length() - 3);
						inMslBlock = false;
						saveStage(result, currentProgram, currentStage, currentSource.toString());
					} else {
						currentSource.append(line).append('\n');
					}
				}
			}

			// Flush any remaining
			flushProgram(result, currentProgram, currentStage, currentSource);
		}

		return result;
	}

	private static void flushProgram(Map<String, Map<GlslToMslTranslator.ShaderStage, String>> result,
									 String programId, GlslToMslTranslator.ShaderStage stage, StringBuilder source) {
		if (programId != null && stage != null && source.length() > 0) {
			saveStage(result, programId, stage, source.toString());
		}
	}

	private static void saveStage(Map<String, Map<GlslToMslTranslator.ShaderStage, String>> result,
								  String programId, GlslToMslTranslator.ShaderStage stage, String msl) {
		result.computeIfAbsent(programId, k -> new LinkedHashMap<>()).put(stage, msl);
	}
}
