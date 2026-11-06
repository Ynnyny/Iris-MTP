package net.irisshaders.iris.metal.ui;

import net.irisshaders.iris.shader.translator.GlslToMslTranslator;
import net.irisshaders.iris.shader.translator.ImtpWriter;
import net.irisshaders.iris.shader.translator.TranslationResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Nhiem vu nen dich shaderpack GLSL sang .imtp, cap nhat progress tren UI thread.
 */
public class ShaderTranslationTask implements Runnable {
	private final Path shaderpackDir;
	private final Path outputFile;
	private final String mcVersion;
	private final TranslationProgressListener listener;

	private volatile boolean cancelled = false;

	public ShaderTranslationTask(Path shaderpackDir, Path outputFile, String mcVersion,
								 TranslationProgressListener listener) {
		this.shaderpackDir = shaderpackDir;
		this.outputFile = outputFile;
		this.mcVersion = mcVersion;
		this.listener = listener;
	}

	public void cancel() {
		this.cancelled = true;
	}

	@Override
	public void run() {
		try {
			// Discover shader files
			Map<String, String> vertexShaders = findShaders(shaderpackDir, ".vsh");
			Map<String, String> fragmentShaders = findShaders(shaderpackDir, ".fsh");
			Map<String, String> geometryShaders = findShaders(shaderpackDir, ".gsh");

			var allProgramIds = new LinkedHashMap<String, Boolean>();
			vertexShaders.keySet().forEach(id -> allProgramIds.put(id, true));
			fragmentShaders.keySet().forEach(id -> allProgramIds.put(id, true));
			geometryShaders.keySet().forEach(id -> allProgramIds.put(id, true));

			int total = allProgramIds.size();
			int completed = 0;

			Map<String, Map<GlslToMslTranslator.ShaderStage, TranslationResult>> programs = new LinkedHashMap<>();

			for (String programId : allProgramIds.keySet()) {
				if (cancelled) {
					listener.onCancelled();
					return;
				}

				Map<GlslToMslTranslator.ShaderStage, TranslationResult> stages = new LinkedHashMap<>();

				String vsh = vertexShaders.get(programId);
				if (vsh != null) {
					TranslationResult r = GlslToMslTranslator.translate(
							vsh, GlslToMslTranslator.ShaderStage.VERTEX,
							"vertex_" + programId, Map.of());
					stages.put(GlslToMslTranslator.ShaderStage.VERTEX, r);
					listener.onShaderTranslated(programId, "vertex", r);
				}

				String fsh = fragmentShaders.get(programId);
				if (fsh != null) {
					TranslationResult r = GlslToMslTranslator.translate(
							fsh, GlslToMslTranslator.ShaderStage.FRAGMENT,
							"fragment_" + programId, Map.of());
					stages.put(GlslToMslTranslator.ShaderStage.FRAGMENT, r);
					listener.onShaderTranslated(programId, "fragment", r);
				}

				String gsh = geometryShaders.get(programId);
				if (gsh != null) {
					TranslationResult r = GlslToMslTranslator.translate(
							gsh, GlslToMslTranslator.ShaderStage.GEOMETRY,
							"geometry_" + programId, Map.of());
					stages.put(GlslToMslTranslator.ShaderStage.GEOMETRY, r);
					listener.onShaderTranslated(programId, "geometry", r);
				}

				if (!stages.isEmpty()) {
					programs.put(programId, stages);
				}

				completed++;
				float progress = (float) completed / (float) total;
				listener.onProgress(progress, "Translating " + programId + "...");
			}

			ImtpWriter.writeImtp(outputFile, shaderpackDir.getFileName().toString(), programs, mcVersion);
			listener.onCompleted(outputFile, programs.size());
		} catch (IOException e) {
			listener.onShaderError("io", e);
		}
	}

	private Map<String, String> findShaders(Path dir, String ext) throws IOException {
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
							result.put(id, Files.readString(p));
						} catch (IOException ex) {
							listener.onShaderError(id, ex);
						}
					});
		}
		return result;
	}

	public interface TranslationProgressListener {
		void onProgress(float progress, String message);
		void onShaderTranslated(String programId, String stage, TranslationResult result);
		void onShaderError(String programId, Throwable error);
		void onCompleted(Path outputFile, int programCount);
		void onCancelled();
	}
}
