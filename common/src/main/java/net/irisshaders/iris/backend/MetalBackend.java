package net.irisshaders.iris.backend;

import io.github.kokodio.metaljvm.metal.MTLCommandQueue;
import io.github.kokodio.metaljvm.metal.MTLDevice;
import io.github.kokodio.metaljvm.metal.MTLFunction;
import io.github.kokodio.metaljvm.metal.MTLLibrary;
import io.github.kokodio.metaljvm.metal.MTLRenderPipelineDescriptor;
import io.github.kokodio.metaljvm.metal.MTLRenderPipelineState;
import io.github.kokodio.metaljvm.metal.Metal;
import io.github.kokodio.metaljvm.objc.AutoreleasePool;
import io.github.kokodio.metaljvm.foundation.NSErrorException;

import java.util.List;

/**
 * Metal back-end connecting to Metallum (metal-jvm). Supports Metal iOS,
 * Metal macOS (Intel and ARM).
 *
 * <p>init() creates MTLDevice via Metal.MTLCreateSystemDefaultDevice(),
 * MTLCommandQueue. createProgram() compiles MSL source (from .imtp) into
 * MTLFunction + MTLRenderPipelineState.
 *
 * <p>Texture, framebuffer, sampler, buffer, draw methods are not yet
 * implemented (UnsupportedOperationException), placeholder for P6+.
 */
public class MetalBackend implements RenderBackend {
	private final BackendType platform;
	private MTLDevice device;
	private MTLCommandQueue commandQueue;

	public MetalBackend(BackendType platform) {
		this.platform = platform;
	}

	@Override
	public BackendType type() {
		return platform;
	}

	@Override
	public void init() {
		device = Metal.MTLCreateSystemDefaultDevice();
		if (device == null) {
			throw new IllegalStateException("MTLCreateSystemDefaultDevice returned null for platform " + platform);
		}
		commandQueue = device.newCommandQueue();
	}

	@Override
	public Object device() {
		return device;
	}

	@Override
	public BackendHandle createProgram(String name, String vertex, String fragment, String geometry) {
		if (device == null) {
			throw new IllegalStateException("MetalBackend not initialized");
		}
		try (AutoreleasePool pool = AutoreleasePool.push()) {
			MTLLibrary library = device.newLibraryWithSource(vertex, null);
			if (library == null) {
				throw new RuntimeException("Failed to compile Metal vertex library for " + name);
			}
			MTLFunction vertexFunc = library.newFunctionWithName("vertex_main");
			library.release();
			if (vertexFunc == null) {
				throw new RuntimeException("Failed to resolve vertex entry point 'vertex_main' for " + name);
			}

			MTLLibrary fragLibrary = device.newLibraryWithSource(fragment, null);
			if (fragLibrary == null) {
				vertexFunc.release();
				throw new RuntimeException("Failed to compile Metal fragment library for " + name);
			}
			MTLFunction fragmentFunc = fragLibrary.newFunctionWithName("fragment_main");
			fragLibrary.release();
			if (fragmentFunc == null) {
				vertexFunc.release();
				throw new RuntimeException("Failed to resolve fragment entry point 'fragment_main' for " + name);
			}

			MTLRenderPipelineState pipeline = null;
			try {
				MTLRenderPipelineDescriptor descriptor = MTLRenderPipelineDescriptor.alloc().init();
				descriptor.setVertexFunction(vertexFunc);
				descriptor.setFragmentFunction(fragmentFunc);
				descriptor.setDepthAttachmentPixelFormat(io.github.kokodio.metaljvm.metal.MTLPixelFormat.BGRA8Unorm);
				pipeline = device.newRenderPipelineState(descriptor);
				descriptor.release();
			} catch (NSErrorException e) {
				vertexFunc.release();
				fragmentFunc.release();
				throw new RuntimeException("Failed to create Metal render pipeline state for " + name + ": " + e.getMessage(), e);
			}

			vertexFunc.release();
			fragmentFunc.release();

			if (pipeline == null) {
				throw new RuntimeException("Failed to create Metal render pipeline state for " + name);
			}

			return new BackendHandle(System.identityHashCode(pipeline), name + "_metal");
		}
	}

	@Override
	public void destroyProgram(BackendHandle program) {
		// MTLRenderPipelineState release handled via handle if needed
	}

	@Override
	public BackendHandle createTexture(int width, int height, TextureFormat format, int mipLevels) {
		throw new UnsupportedOperationException("Metal texture creation not implemented yet.");
	}

	@Override
	public void destroyTexture(BackendHandle texture) {
	}

	@Override
	public BackendHandle createFramebuffer(List<BackendHandle> colorAttachments, BackendHandle depthAttachment) {
		throw new UnsupportedOperationException("Metal framebuffer creation not implemented yet.");
	}

	@Override
	public void destroyFramebuffer(BackendHandle framebuffer) {
	}

	@Override
	public BackendHandle createSampler(SamplerDescriptor descriptor) {
		throw new UnsupportedOperationException("Metal sampler creation not implemented yet.");
	}

	@Override
	public void destroySampler(BackendHandle sampler) {
	}

	@Override
	public BackendHandle createBuffer(long sizeInBytes, BufferUsage usage, java.lang.Object initialData) {
		throw new UnsupportedOperationException("Metal buffer creation not implemented yet.");
	}

	@Override
	public void destroyBuffer(BackendHandle buffer) {
	}

	@Override
	public void updateBuffer(BackendHandle buffer, long offset, byte[] data, long dataOffset, long length) {
	}

	@Override
	public void beginFrame(int framebufferWidth, int framebufferHeight) {
		// TODO: MTLCommandBuffer + render pass encoder
	}

	@Override
	public void endFrame() {
		// TODO: commit + present
	}

	@Override
	public void draw(BackendHandle program, int vertexCount, int instanceCount) {
		throw new UnsupportedOperationException("Metal draw not implemented yet.");
	}

	@Override
	public void setViewport(int x, int y, int width, int height) {
	}

	@Override
	public void clear(float r, float g, float b, float a) {
	}

	@Override
	public void dispose() {
		if (commandQueue != null) {
			commandQueue.release();
			commandQueue = null;
		}
		if (device != null) {
			device.release();
			device = null;
		}
	}
}
