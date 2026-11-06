package net.irisshaders.iris.backend;

import java.util.List;

/**
 * OpenGL back-end m\u1ef9: \u0111\u01b0\u1ee3c g\u1eed l\u00e0m m\u1ef9 m\u00e0 kh\u00f4ng \u0111\u1ed9i \u0111\u1ee3i b\u00eat k\u1ef3 m\u00e3
 * OpenGL hi\u1ec7n t\u1ea1i c\u1ee7a Iris. C\u00e1c ph\u01b0\u01a1ng th\u1ee9c ch\u01b0a \u0111\u01b0\u1ee3c \u00e1nh \u00e1nh b\u1ebangi
 * Migration n\u00eau c\u1ea7n \u0111\u1ebfp n\u1ed9i bo v\u00e0o IrisRenderSystem.
 */
public class OpenGLBackend implements RenderBackend {
	@Override
	public BackendType type() {
		return BackendType.OPENGL;
	}

	@Override
	public void init() {
		// OpenGL d\u00e3 \u0111\u01b0\u1ee3c Kh\u1edfi t\u1ea1o b\u1ebangi Minecraft / IrisRenderSystem.
	}

	@Override
	public Object device() {
		return null;
	}

	@Override
	public BackendHandle createProgram(String name, String vertex, String fragment, String geometry) {
		throw new UnsupportedOperationException("OpenGL program creation stays in ShaderCreator (GL backend).");
	}

	@Override
	public void destroyProgram(BackendHandle program) {
	}

	@Override
	public BackendHandle createTexture(int width, int height, TextureFormat format, int mipLevels) {
		throw new UnsupportedOperationException("OpenGL texture creation stays in GlTexture.");
	}

	@Override
	public void destroyTexture(BackendHandle texture) {
	}

	@Override
	public BackendHandle createFramebuffer(List<BackendHandle> colorAttachments, BackendHandle depthAttachment) {
		throw new UnsupportedOperationException("OpenGL framebuffer creation stays in GlFramebuffer.");
	}

	@Override
	public void destroyFramebuffer(BackendHandle framebuffer) {
	}

	@Override
	public BackendHandle createSampler(SamplerDescriptor descriptor) {
		throw new UnsupportedOperationException("OpenGL sampler creation stays in GlSampler.");
	}

	@Override
	public void destroySampler(BackendHandle sampler) {
	}

	@Override
	public BackendHandle createBuffer(long sizeInBytes, BufferUsage usage, java.lang.Object initialData) {
		throw new UnsupportedOperationException("OpenGL buffer creation stays in GlStateManager.");
	}

	@Override
	public void destroyBuffer(BackendHandle buffer) {
	}

	@Override
	public void updateBuffer(BackendHandle buffer, long offset, byte[] data, long dataOffset, long length) {
	}

	@Override
	public void beginFrame(int framebufferWidth, int framebufferHeight) {
	}

	@Override
	public void endFrame() {
	}

	@Override
	public void draw(BackendHandle program, int vertexCount, int instanceCount) {
		throw new UnsupportedOperationException("OpenGL draw stays in pipeline renderers.");
	}

	@Override
	public void setViewport(int x, int y, int width, int height) {
	}

	@Override
	public void clear(float r, float g, float b, float a) {
	}

	@Override
	public void dispose() {
	}
}
