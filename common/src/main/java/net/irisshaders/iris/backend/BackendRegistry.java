package net.irisshaders.iris.backend;

/**
 * \u0110\u0103ng k\u00fd v\u00e0 l\u00e3y back-end \u0111\u01b0\u1ee3c ch\u1ecdn. OpenGL g\u1eed l\u00e0m m\u1ef9 / fallback.
 */
public final class BackendRegistry {
	private static RenderBackend backend;
	private static BackendType forcedType = null;

	private BackendRegistry() {
	}

	public static RenderBackend get() {
		if (backend == null) {
			backend = createDefault();
		}
		return backend;
	}

	public static void set(RenderBackend backend) {
		BackendRegistry.backend = backend;
	}

	public static BackendType activeType() {
		return get().type();
	}

	/**
	 * \u0110\u1ea1t back-end b\u1ebfd c\u00e0o overrides (tr\u01b0\u1edbc khi get() g\u1ec3i createDefault).
	 */
	public static void forceType(BackendType type) {
		forcedType = type;
		backend = null; // reset \u0111\u1ec3 get() t\u1ea1o m\u1edbi
	}

	/**
	 * Ch\u1ecdn back-end Metal t\u01b0\u01a1ng \u0111\u1ee3ng v\u1edbi platform runtime.
	 */
	public static BackendType detectMetalPlatform() {
		String os = System.getProperty("os.name", "").toLowerCase();
		String arch = System.getProperty("os.arch", "").toLowerCase();
		if (os.contains("mac")) {
			if (arch.contains("aarch64") || arch.contains("arm64")) {
				return BackendType.METAL_MACOS_ARM;
			}
			return BackendType.METAL_MACOS_INTEL;
		}
		if (os.contains("ios")) {
			return BackendType.METAL_IOS;
		}
		return BackendType.OPENGL;
	}

	private static RenderBackend createDefault() {
		BackendType type = forcedType != null ? forcedType : detectMetalPlatform();
		if (type != BackendType.OPENGL) {
			return new MetalBackend(type);
		}
		return new OpenGLBackend();
	}
}
