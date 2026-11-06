package net.irisshaders.iris.backend;

import io.github.kokodio.metaljvm.foundation.NSObject;
import io.github.kokodio.metaljvm.metal.*;
import io.github.kokodio.metaljvm.objc.AutoreleasePool;
import io.github.kokodio.metaljvm.foundation.NSErrorException;

import com.metallum.render.MetalPlatform;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Metal back-end connecting to Metallum (metal-jvm). Supports Metal iOS,
 * Metal macOS (Intel and ARM).
 *
 * <p>init() creates MTLDevice via Metal.MTLCreateSystemDefaultDevice(),
 * MTLCommandQueue. createProgram() compiles MSL source (from .imtp) into
 * MTLFunction + MTLRenderPipelineState. Render methods are implemented
 * directly using metal-jvm.
 */
public class MetalBackend implements RenderBackend {
	private final BackendType platform;
	private MTLDevice device;
	private MTLCommandQueue commandQueue;
	private final Map<BackendHandle, MTLRenderPipelineState> programs = new WeakHashMap<>();
	private final Map<BackendHandle, MTLTexture> textures = new WeakHashMap<>();
	private final Map<BackendHandle, MTLSamplerState> samplers = new WeakHashMap<>();
	private final Map<BackendHandle, MTLBuffer> buffers = new WeakHashMap<>();

	private MTLCommandBuffer currentCommandBuffer;
	private MTLRenderCommandEncoder currentEncoder;

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
				descriptor.setDepthAttachmentPixelFormat(MTLPixelFormat.BGRA8Unorm);
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

			BackendHandle handle = new BackendHandle(System.identityHashCode(pipeline), name + "_metal");
			programs.put(handle, pipeline);
			return handle;
		}
	}

	@Override
	public void destroyProgram(BackendHandle program) {
		MTLRenderPipelineState pipeline = programs.remove(program);
		if (pipeline != null) {
			pipeline.release();
		}
	}

	@Override
	public BackendHandle createTexture(int width, int height, TextureFormat format, int mipLevels) {
		if (device == null) {
			throw new IllegalStateException("MetalBackend not initialized");
		}
		MTLPixelFormat pixelFormat = toMetalPixelFormat(format);
		MTLTextureDescriptor descriptor = MTLTextureDescriptor.alloc().init();
		descriptor.setPixelFormat(pixelFormat);
		descriptor.setWidth(width);
		descriptor.setHeight(height);
		descriptor.setMipmapLevelCount((long) mipLevels);
		descriptor.setUsage(MTLTextureUsage.ShaderRead | MTLTextureUsage.ShaderWrite);
		descriptor.setStorageMode(MTLStorageMode.Private);
		try (AutoreleasePool pool = AutoreleasePool.push()) {
			MTLTexture texture = device.newTexture(descriptor);
			descriptor.release();
			if (texture == null) {
				throw new RuntimeException("Failed to create Metal texture for " + width + "x" + height);
			}
			BackendHandle handle = new BackendHandle(System.identityHashCode(texture), "texture_" + width + "x" + height);
			textures.put(handle, texture);
			return handle;
		}
	}

	@Override
	public void destroyTexture(BackendHandle texture) {
		MTLTexture mtlTexture = textures.remove(texture);
		if (mtlTexture != null) {
			mtlTexture.release();
		}
	}

	@Override
	public BackendHandle createFramebuffer(List<BackendHandle> colorAttachments, BackendHandle depthAttachment) {
		// Metal uses MTLRenderPassDescriptor per frame; no persistent framebuffer object.
		BackendHandle handle = new BackendHandle(System.identityHashCode(colorAttachments), "framebuffer");
		return handle;
	}

	@Override
	public void destroyFramebuffer(BackendHandle framebuffer) {
		// No persistent MTL objects to release.
	}

	@Override
	public BackendHandle createSampler(SamplerDescriptor descriptor) {
		if (device == null) {
			throw new IllegalStateException("MetalBackend not initialized");
		}
		MTLSamplerDescriptor samplerDescriptor = MTLSamplerDescriptor.alloc().init();
		samplerDescriptor.setMinFilter(toMetalMinFilter(descriptor.minFilter));
		samplerDescriptor.setMagFilter(toMetalMagFilter(descriptor.magFilter));
		samplerDescriptor.setMipFilter(MTLSamplerMipFilter.NotMipmapped);
		samplerDescriptor.setSAddressMode(toMetalWrapMode(descriptor.wrapS));
		samplerDescriptor.setTAddressMode(toMetalWrapMode(descriptor.wrapT));
		try (AutoreleasePool pool = AutoreleasePool.push()) {
			MTLSamplerState sampler = device.newSamplerState(samplerDescriptor);
			samplerDescriptor.release();
			if (sampler == null) {
				throw new RuntimeException("Failed to create Metal sampler state");
			}
			BackendHandle handle = new BackendHandle(System.identityHashCode(sampler), "sampler");
			samplers.put(handle, sampler);
			return handle;
		}
	}

	@Override
	public void destroySampler(BackendHandle sampler) {
		MTLSamplerState mtlSampler = samplers.remove(sampler);
		if (mtlSampler != null) {
			mtlSampler.release();
		}
	}

	@Override
	public BackendHandle createBuffer(long sizeInBytes, BufferUsage usage, Object initialData) {
		if (device == null) {
			throw new IllegalStateException("MetalBackend not initialized");
		}
		MTLBuffer buffer;
		try (AutoreleasePool pool = AutoreleasePool.push()) {
			buffer = device.newBuffer(sizeInBytes, 0L);
			if (buffer == null) {
				throw new RuntimeException("Failed to create Metal buffer of " + sizeInBytes + " bytes");
			}
			if (initialData instanceof ByteBuffer bb) {
				MemorySegment seg = buffer.contents();
				int limit = Math.min((int) sizeInBytes, bb.remaining());
				for (int i = 0; i < limit; i++) {
					seg.set(ValueLayout.JAVA_BYTE, i, bb.get(i));
				}
			}
		}
		BackendHandle handle = new BackendHandle(System.identityHashCode(buffer), "buffer_" + sizeInBytes);
		buffers.put(handle, buffer);
		return handle;
	}

	@Override
	public void destroyBuffer(BackendHandle buffer) {
		MTLBuffer mtlBuffer = buffers.remove(buffer);
		if (mtlBuffer != null) {
			mtlBuffer.release();
		}
	}

	@Override
	public void updateBuffer(BackendHandle buffer, long offset, byte[] data, long dataOffset, long length) {
		MTLBuffer mtlBuffer = buffers.get(buffer);
		if (mtlBuffer == null) {
			throw new IllegalStateException("Buffer not found: " + buffer);
		}
		try (AutoreleasePool pool = AutoreleasePool.push()) {
			MemorySegment seg = mtlBuffer.contents();
			for (long i = 0; i < length; i++) {
				seg.set(ValueLayout.JAVA_BYTE, offset + i, data[(int) dataOffset + (int) i]);
			}
		}
	}

	@Override
	public void beginFrame(int framebufferWidth, int framebufferHeight) {
		if (commandQueue == null) {
			throw new IllegalStateException("MetalBackend not initialized");
		}
		try (AutoreleasePool pool = AutoreleasePool.push()) {
			currentCommandBuffer = commandQueue.commandBuffer();
			if (currentCommandBuffer == null) {
				throw new RuntimeException("Failed to create Metal command buffer");
			}
			MTLRenderPassDescriptor descriptor = MTLRenderPassDescriptor.alloc().init();
			MTLRenderPassColorAttachmentDescriptorArray colorAttachments = descriptor.colorAttachments();
			MTLRenderPassColorAttachmentDescriptor attachment = colorAttachments.objectAtIndexedSubscript(0L);
			attachment.setLoadAction(MTLLoadAction.Clear);
			attachment.setClearColor(new MTLClearColor(0.0, 0.0, 0.0, 1.0));
			attachment.setStoreAction(MTLStoreAction.Store);
			currentEncoder = currentCommandBuffer.renderCommandEncoder(descriptor);
			descriptor.release();
		}
	}

	@Override
	public void endFrame() {
		try (AutoreleasePool pool = AutoreleasePool.push()) {
			if (currentEncoder != null) {
				currentEncoder.endEncoding();
				currentEncoder = null;
			}
			if (currentCommandBuffer != null) {
				currentCommandBuffer.commit();
				currentCommandBuffer = null;
			}
		}
	}

	@Override
	public void draw(BackendHandle program, int vertexCount, int instanceCount) {
		MTLRenderPipelineState pipeline = programs.get(program);
		if (pipeline == null) {
			throw new IllegalStateException("Program not found: " + program);
		}
		if (currentEncoder == null) {
			throw new IllegalStateException("No active render command encoder. Call beginFrame first.");
		}
		try (AutoreleasePool pool = AutoreleasePool.push()) {
			currentEncoder.setRenderPipelineState(pipeline);
			currentEncoder.drawPrimitives(MTLPrimitiveType.Triangle, 0L, vertexCount, instanceCount);
		}
	}

	@Override
	public void setViewport(int x, int y, int width, int height) {
		if (currentEncoder == null) {
			throw new IllegalStateException("No active render command encoder");
		}
		MTLViewport viewport = new MTLViewport(x, y, width, height, 0.0, 1.0);
		currentEncoder.setViewport(viewport);
	}

	@Override
	public void clear(float r, float g, float b, float a) {
		// Clear color is set via render pass descriptor in beginFrame.
		// This method is kept for API compatibility.
	}

	@Override
	public void dispose() {
		endFrame();
		programs.values().forEach(NSObject::release);
		programs.clear();
		textures.values().forEach(NSObject::release);
		textures.clear();
		samplers.values().forEach(NSObject::release);
		samplers.clear();
		buffers.values().forEach(NSObject::release);
		buffers.clear();
		if (commandQueue != null) {
			commandQueue.release();
			commandQueue = null;
		}
		if (device != null) {
			device.release();
			device = null;
		}
	}

	// ---------- Helpers ----------

	private MetalPlatform toMetalPlatform() {
		return switch (platform) {
			case METAL_IOS -> MetalPlatform.IOS;
			case METAL_MACOS_INTEL -> MetalPlatform.MACOS_INTEL;
			case METAL_MACOS_ARM -> MetalPlatform.MACOS_ARM;
			default -> MetalPlatform.MACOS_ARM;
		};
	}

	private static MTLSamplerMinMagFilter toMetalMinFilter(SamplerDescriptor.MinFilter filter) {
		return switch (filter) {
			case NEAREST -> MTLSamplerMinMagFilter.Nearest;
			case LINEAR, NEAREST_MIPMAP_NEAREST, LINEAR_MIPMAP_LINEAR -> MTLSamplerMinMagFilter.Linear;
		};
	}

	private static MTLSamplerMinMagFilter toMetalMagFilter(SamplerDescriptor.MagFilter filter) {
		return switch (filter) {
			case NEAREST -> MTLSamplerMinMagFilter.Nearest;
			case LINEAR -> MTLSamplerMinMagFilter.Linear;
		};
	}

	private static MTLSamplerAddressMode toMetalWrapMode(SamplerDescriptor.WrapMode wrap) {
		return switch (wrap) {
			case REPEAT -> MTLSamplerAddressMode.Repeat;
			case CLAMP_TO_EDGE -> MTLSamplerAddressMode.ClampToEdge;
			case MIRRORED_REPEAT -> MTLSamplerAddressMode.MirrorRepeat;
		};
	}

	private static MTLPixelFormat toMetalPixelFormat(TextureFormat format) {
		return switch (format) {
			case R8 -> MTLPixelFormat.R8Unorm;
			case RGBA8 -> MTLPixelFormat.RGBA8Unorm;
			case RGBA16F -> MTLPixelFormat.RGBA16Float;
			case RGBA32F -> MTLPixelFormat.RGBA32Float;
			case R32F -> MTLPixelFormat.R32Float;
			case DEPTH16 -> MTLPixelFormat.Depth16Unorm;
			case DEPTH24 -> MTLPixelFormat.Depth32Float;
			case DEPTH32F -> MTLPixelFormat.Depth32Float;
			case DEPTH24_STENCIL8 -> MTLPixelFormat.Depth24Unorm_Stencil8;
		};
	}
}
