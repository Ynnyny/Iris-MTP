package net.irisshaders.iris.backend;

/**
 * M\u00f4 t\u1ea3 sampler state (filter, wrap, ...).
 */
public final class SamplerDescriptor {
	public final MagFilter magFilter;
	public final MinFilter minFilter;
	public final WrapMode wrapS;
	public final WrapMode wrapT;
	public final boolean anisotropyEnabled;

	public SamplerDescriptor(MagFilter magFilter, MinFilter minFilter, WrapMode wrapS, WrapMode wrapT, boolean anisotropyEnabled) {
		this.magFilter = magFilter;
		this.minFilter = minFilter;
		this.wrapS = wrapS;
		this.wrapT = wrapT;
		this.anisotropyEnabled = anisotropyEnabled;
	}

	public enum MagFilter { NEAREST, LINEAR }
	public enum MinFilter { NEAREST, LINEAR, NEAREST_MIPMAP_NEAREST, LINEAR_MIPMAP_LINEAR }
	public enum WrapMode { REPEAT, CLAMP_TO_EDGE, MIRRORED_REPEAT }
}
