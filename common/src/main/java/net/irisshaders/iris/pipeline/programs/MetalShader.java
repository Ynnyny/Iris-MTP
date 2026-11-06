package net.irisshaders.iris.pipeline.programs;

import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import net.irisshaders.iris.backend.BackendRegistry;
import net.irisshaders.iris.backend.RenderBackend;

import java.util.List;

/**
 * Shader wrapper cho Metal back-end. Implement {@link IrisProgram} nhung delegate
 * cac thao tac render qua {@link RenderBackend} (MetalBackend). Cau hinh cho P6+,
 * cac phuong thuc chua hoan trien toi day (UnsupportedOperationException).
 */
public class MetalShader implements IrisProgram {
	private final String name;
	private final int programHandle;
	private final boolean usesTessellation;
	private boolean setUp = false;

	public MetalShader(String name, int programHandle, boolean usesTessellation) {
		this.name = name;
		this.programHandle = programHandle;
		this.usesTessellation = usesTessellation;
	}

	@Override
	public void iris$setupState(List<BindGroupLayout.UniformDescription> samplers) {
		RenderBackend backend = BackendRegistry.get();
		if (backend == null || !backend.type().name().startsWith("METAL")) {
			throw new IllegalStateException("MetalShader used without Metal backend");
		}
		setUp = true;
	}

	@Override
	public void iris$clearState() {
		setUp = false;
	}

	@Override
	public int iris$getBlockIndex(int program, CharSequence uniformBlockName) {
		// Metal uses [[buffer(N)]] bindings; uniform block index is handled by MetalBackend.
		return -1;
	}

	@Override
	public boolean iris$isSetUp() {
		return setUp;
	}

	public String getName() {
		return name;
	}

	public int getProgramHandle() {
		return programHandle;
	}

	public boolean usesTessellation() {
		return usesTessellation;
	}
}
