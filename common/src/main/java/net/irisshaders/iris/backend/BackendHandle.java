package net.irisshaders.iris.backend;

/**
 * \u0110\u1ea1u t\u01b0 \u00e2n tr\u01b0\u1eddng tr\u1ee3 d\u1ea1t resource c\u1ee7a back-end native.
 * OpenGL d\u00f9ng int GL id; Metal d\u00f9ng con tr\u01a1 MTL id / Objective-C object.
 */
public final class BackendHandle {
	private final long nativeId;
	private final String debugLabel;

	public BackendHandle(long nativeId, String debugLabel) {
		this.nativeId = nativeId;
		this.debugLabel = debugLabel;
	}

	public long nativeId() {
		return nativeId;
	}

	public String debugLabel() {
		return debugLabel;
	}

	@Override
	public String toString() {
		return debugLabel + "@" + nativeId;
	}
}
