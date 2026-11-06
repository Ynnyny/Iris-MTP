package net.irisshaders.iris.metal.ui;

import net.irisshaders.iris.shader.translator.TranslationResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Man hinh dich shaderpack GLSL sang .imtp trong game (placeholder, can test client).
 * Render GUI duoc them sau khi xac dinh API Screen chuan xac cua Minecraft 26.3.
 */
public class MetalShaderTranslationScreen extends Screen {
	private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

	private final Screen parent;
	private final Path shaderpacksDir;
	private final Path imtpOutputDir;
	private final String mcVersion;

	private ShaderTranslationTask currentTask;
	private CompletableFuture<?> currentFuture;
	private final List<String> logLines = new ArrayList<>();
	private float progress = 0.0f;
	private boolean translationComplete = false;

	public MetalShaderTranslationScreen(Screen parent) {
		super(Component.literal("Metal Shader Translation"));
		this.parent = parent;
		this.shaderpacksDir = Minecraft.getInstance().gameDirectory.toPath().resolve("shaderpacks");
		this.imtpOutputDir = this.shaderpacksDir.resolve(".imtp");
		this.mcVersion = "26.3";
	}

	public void setProgress(float progress) {
		this.progress = Math.max(0.0f, Math.min(1.0f, progress));
	}

	public float getProgress() {
		return progress;
	}

	public void addLog(String message) {
		logLines.add(message);
	}

	public List<String> getLogLines() {
		return logLines;
	}

	public boolean isTranslationComplete() {
		return translationComplete;
	}

	public void startTranslation(Path shaderpackPath) {
		if (currentFuture != null && !currentFuture.isDone()) {
			return;
		}

		String packName = shaderpackPath.getFileName().toString();
		Path outputFile = imtpOutputDir.resolve(packName.replace(".zip", "") + ".imtp");

		currentTask = new ShaderTranslationTask(
				shaderpackPath.getParent(),
				outputFile,
				mcVersion,
				new ShaderTranslationTask.TranslationProgressListener() {
					@Override
					public void onProgress(float p, String message) {
						setProgress(p);
						addLog(message);
					}

					@Override
					public void onShaderTranslated(String programId, String stage, TranslationResult result) {
						if (!result.success()) {
							addLog("WARN: " + programId + " " + stage + ": " + result.errorMessage());
						}
					}

					@Override
					public void onShaderError(String programId, Throwable error) {
						addLog("ERROR: " + programId + ": " + error.getMessage());
					}

					@Override
					public void onCompleted(Path outputFile, int programCount) {
						translationComplete = true;
						setProgress(1.0f);
						addLog("Completed: " + programCount + " programs -> " + outputFile);
					}

					@Override
					public void onCancelled() {
						addLog("Translation cancelled");
					}
				}
		);

		currentFuture = CompletableFuture.runAsync(currentTask, EXECUTOR);
	}

	public void close() {
		if (currentFuture != null && !currentFuture.isDone()) {
			currentFuture.cancel(true);
		}
		Minecraft.getInstance().gui.setScreen(parent);
	}
}
