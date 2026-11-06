package net.irisshaders.iris.backend;

import java.util.List;

/**
 * Common render backend interface. Every backend (OpenGL, Metal iOS, Metal macOS Intel/ARM)
 * implements this so Iris can render without being tied to a specific graphics API.
 *
 * <p>Each resource (texture, buffer, shader program, framebuffer...) is owned by a
 * {@link BackendHandle} so the backend can manage its own native object (GL id, MTL id, ...).
 */
public interface RenderBackend {

	BackendType type();

	/**
	 * Initialize the backend after the window / MTL device has been created.
	 */
	void init();

	/**
	 * Return the current render device (GPU device).
	 */
	Object device();

	/**
	 * Create a shader program from source (GLSL already translated to MSL if needed).
	 *
	 * @param name shader name (used for logging/debug)
	 * @param vertex vertex shader source
	 * @param fragment fragment shader source
	 * @param geometry geometry shader source (may be null)
	 * @return handle used to bind the shader program
	 */
	BackendHandle createProgram(String name, String vertex, String fragment, String geometry);

	void destroyProgram(BackendHandle program);

	BackendHandle createTexture(int width, int height, TextureFormat format, int mipLevels);

	void destroyTexture(BackendHandle texture);

	BackendHandle createFramebuffer(List<BackendHandle> colorAttachments, BackendHandle depthAttachment);

	void destroyFramebuffer(BackendHandle framebuffer);

	BackendHandle createSampler(SamplerDescriptor descriptor);

	void destroySampler(BackendHandle sampler);

	BackendHandle createBuffer(long sizeInBytes, BufferUsage usage, Object initialData);

	void destroyBuffer(BackendHandle buffer);

	void updateBuffer(BackendHandle buffer, long offset, byte[] data, long dataOffset, long length);

	void beginFrame(int framebufferWidth, int framebufferHeight);

	void endFrame();

	void draw(BackendHandle program, int vertexCount, int instanceCount);

	void setViewport(int x, int y, int width, int height);

	void clear(float r, float g, float b, float a);

	void dispose();
}
