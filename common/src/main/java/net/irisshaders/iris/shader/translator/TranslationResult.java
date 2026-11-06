package net.irisshaders.iris.shader.translator;

/**
 * K\u1ebft qu\u1ea3 d\u1ecbch m\u00f9t shader GLSL sang MSL (Metal Shading Language).
 */
public final class TranslationResult {
	private final String mslSource;
	private final boolean success;
	private final String errorMessage;
	private final String[] warnings;

	public TranslationResult(String mslSource, boolean success, String errorMessage, String[] warnings) {
		this.mslSource = mslSource;
		this.success = success;
		this.errorMessage = errorMessage;
		this.warnings = warnings;
	}

	public String mslSource() {
		return mslSource;
	}

	public boolean success() {
		return success;
	}

	public String errorMessage() {
		return errorMessage;
	}

	public String[] warnings() {
		return warnings;
	}
}
