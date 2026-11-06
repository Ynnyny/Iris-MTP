package net.irisshaders.iris.backend;

import java.util.List;

/**
 * Metal back-end \u0111ang x\u0103y d\u1ef1ng (skeleton). \u0110\u01b0\u1ee3c tri\u1ec3n khai \u0111\u1ea7y \u0111\u1ee3 h\u1ed7 tr\u1ee3:
 * Metal iOS, Metal macOS (Intel v\u00e0 ARM). T\u00e1i li\u1ec7u tham kh\u1ea3o: Apple Metal framework
 * (MTLDevice, MTLCommandQueue, MTLRenderPipelineState, MTLTexture, MTLFramebuffer/chiru MTLRenderPassDescriptor).
 *
 * <p>Tri\u1ec3n khai \u0111\u00e2y \u0111\u01b0\u1ee3c t\u00f4 \u0111\u01b0\u1ee3c bi\u00ean d\u1ecbch v\u1edbi JNI/ObjC bridge ho\u1eb7c
 * th\u01b0 vi\u1ec7n Java-Metal binding c\u00f3 s\u1eedi. Trong giai \u1ee7a 1, c\u00e1c ph\u01b0\u01a1ng th\u1ee9c n\u00eau b\u00fa
 * UnsupportedOperationException v\u1edbi ghi ch\u00fa TODO \u0111\u1ec3 nh\u1ee9ng \u0111\u1ea5u ti\u00ean c\u1ea7n l\u00e0m.
 */
public class MetalBackend implements RenderBackend {
	private final BackendType platform;

	public MetalBackend(BackendType platform) {
		this.platform = platform;
	}

	@Override
	public BackendType type() {
		return platform;
	}

	@Override
	public void init() {
		// TODO: T\u1ea1o MTLDevice (default device), MTLCommandQueue, MTLRenderPipelineLibrary.
		// macOS: MTLCreateSystemDefaultDevice()
		// iOS:   MTLCreateSystemDefaultDevice()
		throw new UnsupportedOperationException("Metal device init not implemented yet.");
	}

	@Override
	public Object device() {
		throw new UnsupportedOperationException("Metal device accessor not implemented yet.");
	}

	@Override
	public BackendHandle createProgram(String name, String vertex, String fragment, String geometry) {
		// TODO: D\u1ecbch GLSL \u0111\u00e3 \u0111\u01b0\u1ee3c \u0111\u1ed9i sang MSL (xem shader.translator),
		// sau \u0111\u00f3 t\u1ea1o MTLFunction (vertex/fragment) + MTLRenderPipelineState.
		throw new UnsupportedOperationException("Metal program creation not implemented yet.");
	}

	@Override
	public void destroyProgram(BackendHandle program) {
	}

	@Override
	public BackendHandle createTexture(int width, int height, TextureFormat format, int mipLevels) {
		// TODO: T\u1ea1o MTLTexture v\u1edbi MTLPixelFormat \u0111\u01b0\u1ee3ng v\u1edbi TextureFormat.
		throw new UnsupportedOperationException("Metal texture creation not implemented yet.");
	}

	@Override
	public void destroyTexture(BackendHandle texture) {
	}

	@Override
	public BackendHandle createFramebuffer(List<BackendHandle> colorAttachments, BackendHandle depthAttachment) {
		// TODO: X\u00e9p d\u1ee5ng MTLRenderPassDescriptor / MTLRenderPassAttachmentDescriptor.
		throw new UnsupportedOperationException("Metal framebuffer creation not implemented yet.");
	}

	@Override
	public void destroyFramebuffer(BackendHandle framebuffer) {
	}

	@Override
	public BackendHandle createSampler(SamplerDescriptor descriptor) {
		// TODO: T\u1ea1o MTLSamplerState v\u1edbi MTLSamplerMagFilter/MinFilter/WrapMode.
		throw new UnsupportedOperationException("Metal sampler creation not implemented yet.");
	}

	@Override
	public void destroySampler(BackendHandle sampler) {
	}

	@Override
	public BackendHandle createBuffer(long sizeInBytes, BufferUsage usage, java.lang.Object initialData) {
		// TODO: T\u1ea1o MTLBuffer v\u1edbi MTLStorageMode (Shared/Private/Managed).
		throw new UnsupportedOperationException("Metal buffer creation not implemented yet.");
	}

	@Override
	public void destroyBuffer(BackendHandle buffer) {
	}

	@Override
	public void updateBuffer(BackendHandle buffer, long offset, byte[] data, long dataOffset, long length) {
		// TODO: [buffer didModifyRange:] ho\u1eb7c memcpy v\u00e0o MTLBuffer content.
	}

	@Override
	public void beginFrame(int framebufferWidth, int framebufferHeight) {
		// TODO: L\u00e1y MTLCommandBuffer t\u1eeb MTLCommandQueue, b\u1ea5t \u0111\u1ea7u render pass.
	}

	@Override
	public void endFrame() {
		// TODO: K\u1ebft \u00f3c render pass, commit MTLCommandBuffer, present drawable.
	}

	@Override
	public void draw(BackendHandle program, int vertexCount, int instanceCount) {
		// TODO: \u0111\u1ea5t MTLRenderPipelineState, MTLDepthStencilState, v\u1ec1 drawPrimitives/drawIndexedPrimitives.
	}

	@Override
	public void setViewport(int x, int y, int width, int height) {
		// TODO: \u0111\u1ea5t MTLViewport.
	}

	@Override
	public void clear(float r, float g, float b, float a) {
		// TODO: clearColor \u0111\u1ea1t v\u00e0o render pass descriptor.
	}

	@Override
	public void dispose() {
		// TODO: Gi\u1ea3i p\u00f3ng MTLCommandQueue, MTLDevice.
	}
}
