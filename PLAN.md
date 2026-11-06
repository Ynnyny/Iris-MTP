# Iris-MTP — Port Metal + Dịch GLSL→MSL (.imtp)

Fork từ IrisShaders/Iris nhánh 26.3, nhánh `metal-port`, commit `96d8a5b`.

## Mục tiêu chính

Mod Iris **không** dịch GL→Metal runtime. Thay vào đó: dịch trước shader GLSL sang MSL, lưu file `.imtp`, mod chỉ nạp `.imtp` đã dịch. `.zip` / không đuôi → nạp nguyên bản (chưa dịch, dùng OpenGL).

Backend Metal: iOS, macOS Intel, macOS ARM. OpenGL giữ nguyên làm fallback/mặc định.

## Kế hoạch tổng (Phase)

| Phase | Mục tiêu |
|-------|----------|
| P0 | Chuẩn bị repo, nhánh, remote, tài liệu kế hoạch |
| P1 | Fork Metallum, bổ iOS + macOS Intel, xuất bản làm dependency |
| P2 | Hoàn thiện lớp abstraction RenderBackend + MetalBackend skeleton (kết nối Metallum) |
| P3 | Bộ dịch GLSL→MSL hoàn chỉnh, xuất .imtp, CLI/gradlew task dịch shaderpack |
| P4 | Tải shaderpack: ưu tiên .imtp; .zip/không đuôi → raw GL; UI chọn chế độ |
| P5 | UI trong game: màn hình dịch + progress bar + log |
| P6 | Kết nối backend vào ShaderCreator/pipeline (Metal bật qua cờ, GL mặc định) |
| P7 | Build/test Minecraft 26.3, chụp ảnh media, viết README + credit |
| P8 | Đóng gói, release, tài liệu triệt hạ |

## Ràng buộc quan trọng

- Port toàn bộ engine OpenGL sang Metal là khối lượng lớn (cấp tháng). Plan chia thành phase nhỏ, mỗi phase commit/push riêng, có thể dừng và tiếp tục phiên sau.
- Không thay đổi logic render OpenGL hiện có. Chỉ thêm lớp abstraction + backend Metal song song; GL backend không bị xóa.
- Pure-Java classes (backend abstraction, translator) có thể test bằng javac độc lập trước khi ghép vào Gradle.
- Minecraft 26.3 test chỉ làm được khi có môi trường Fabric dev đầy đủ; nếu không, skip P7 client test.

## Java 25

`build.gradle.kts` dùng `java.toolchain.languageVersion = JavaLanguageVersion.of(25)`. Máy hiện tại chỉ có Java 21 (OpenJDK 21 qua Homebrew). Java 25 chưa cài. Nếu không cài được Java 25, build/test dùng Java 21 làm tạm và ghi chú trong README.

## P0 — Chuẩn bị repo & plan (phiên này)

1. Tạo file `PLAN.md` tại root repo (file này).
2. Commit `PLAN.md` vào nhánh `metal-port`.
3. Push nhánh `metal-port` lên remote `origin` (`https://github.com/Ynnyny/Iris-MTP.git`).
4. (Tùy chọn) Tạo tag `v0.1.0-plan`.

Lệnh tham khảo:
```
cd /Volumes/project/Metal-Shader/iris
git checkout metal-port
git add PLAN.md
git commit -m "feat(metal-port): add PLAN.md for P0-P8 roadmap"
git push origin metal-port
git tag v0.1.0-plan
git push origin v0.1.0-plan
```

## P1 — Fork Metallum & bổ platform iOS / macOS Intel

Mục tiêu: Metallum hiện chỉ hỗ trợ macOS ARM. Cần fork về repo riêng (hoặc làm submodule/subtree trong Iris-MTP), bổ:

- Metal cho iOS (`MTLCreateSystemDefaultDevice` trên iOS, texture format, command queue)
- Metal cho macOS Intel (`MTLCreateSystemDefaultDevice`, fallback feature set)

Công việc chi tiết:

1. Fork `https://github.com/kokodio/metallum.git` → repo của Ynnyny (ví dụ `Ynnyny/Metallum`).
2. Clone nhánh mặc định, tạo nhánh `metal-multi-platform`.
3. Thêm abstraction platform trong Metallum:
   - `MetalPlatform` enum: `MACOS_ARM`, `MACOS_INTEL`, `IOS`
   - `MetalDeviceFactory.create(platform)` → `MTLDevice`
   - `MetalCommandQueueFactory`, `MetalTextureFormatMapper` (iOS/macOS có pixel format khác nhau nhất định)
4. Cập nhật `build.gradle` (Metallum) để compile cho cả 3 target (hoặc ít nhất không crash khi chạy trên Intel/iOS).
5. Commit + push Metallum fork.
6. Trong Iris-MTP `build.gradle.kts`, thêm Metallum làm dependency (Maven Local / Jitpack / git dependency).
7. Cập nhật `settings.gradle.kts` (thêm repository Metallum).

File dự kiến thay đổi (Iris-MTP): `build.gradle.kts`, `settings.gradle.kts`.

Acceptance:
- Metallum build thành công trên macOS ARM (hiện tại) và không lỗi compile trên Intel/iOS target.
- Iris-MTP `build.gradle.kts` resolve được Metallum.

## P2 — Lớp abstraction RenderBackend + MetalBackend kết nối Metallum

Mục tiêu: Iris có thể switch giữa OpenGL (mặc định) và Metal mà không đụng logic render cũ.

Công việc chi tiết:

1. Hoàn thiện `RenderBackend.java` (đã có skeleton): `init()`, `device()`, `createProgram`/`destroyProgram`, `createTexture`/`destroyTexture`, `createFramebuffer`/..., `createSampler`/..., `createBuffer`/..., `updateBuffer`, `beginFrame`/`endFrame`, `draw`, `setViewport`, `clear`, `dispose`.
2. `OpenGLBackend`: delegate giữ nguyên (không đổi code GL cũ), chỉ wrap để implement interface.
3. `MetalBackend`: sử dụng Metallum để:
   - `init()`: tạo `MTLDevice`, `MTLCommandQueue`, `MTLRenderPipelineLibrary`
   - `createProgram()`: nhận MSL source (từ `.imtp`), tạo `MTLFunction` + `MTLRenderPipelineState`
   - `createTexture()`: tạo `MTLTexture` từ `TextureFormat`
   - `createFramebuffer()`: gom `MTLRenderPassDescriptor`
   - `createSampler()`: `MTLSamplerState`
   - `createBuffer()`/`updateBuffer()`: `MTLBuffer`
   - `draw()`: encode `drawPrimitives`/`drawIndexedPrimitives`
   - `beginFrame()`/`endFrame()`: `MTLCommandBuffer`, commit, present
4. `BackendRegistry`: chọn backend theo cờ cấu hình (`iris.backend=metal|opengl`) và OS/arch.
5. Thêm enum `BackendSelection` / config key trong `IrisConfig` (hoặc file properties mới).

File dự kiến:
- `common/src/main/java/net/irisshaders/iris/backend/RenderBackend.java` (hoàn thiện)
- `.../backend/OpenGLBackend.java` (hoàn thiện delegate)
- `.../backend/MetalBackend.java` (kết nối Metallum)
- `.../backend/BackendRegistry.java` (thêm cờ chọn)
- Config: `common/src/main/resources/iris.toml` hoặc config class mới `MetalConfig.java`

Acceptance:
- Compile không lỗi (pure Java + Metallum dependency).
- Chuyển cờ `opengl` ↔ `metal` không crash khi khởi tạo (metal có thể throw "not implemented" ở từng method, nhưng `init` phải chạy được).

## P3 — Bộ dịch GLSL→MSL hoàn chỉnh, xuất file .imtp

Mục tiêu: Dịch 100% shader GLSL của Iris/Sodium sang MSL, lưu ra file `.imtp` (không dịch runtime).

Cấu trúc file `.imtp`:

```
# Iris Metal Translated Package (imtp)
# Header: version, source shaderpack, mc version, timestamp
[programs]
<program_id> = {
  vertex = """...msl..."""
  fragment = """...msl..."""
  geometry = """...msl..."""  # optional
}
```

Công việc chi tiết:

1. Nâng cấp `GlslToMslTranslator.java`:
   - Parse GLSL cơ bản (regex → AST nhẹ cho các trường hợp phổ biến của Iris/Sodium).
   - Bảng mapping đầy đủ: built-in functions (`texture`, `textureLod`, `textureGrad`, `textureOffset`, `dFdx`, `dFdy`, `fwidth`, `inversesqrt`, `mod`, `mix`, `clamp`, `dot`, `cross`, `normalize`, ...), types (`sampler2D` → `texture2d<float, access::sample>`, `vec4` → `float4`, `mat4` → `float4x4`, ...).
   - Strip precision/`highp`/`mediump`/`lowp`.
   - Chuyển `main()` → entry point với qualifiers Metal: `[[vertex]]`, `[[fragment]]`, `[[stage_in]]`, `[[color(N)]]`, `[[buffer(N)]]`.
   - Xử lý uniform buffer std140 → Metal `[[buffer(N)]]`.
   - Xử lý texture sampling với offset/Lod/Grad.
   - Geometry/tessellation (nếu có trong shaderpack).
2. Tạo `ImtpWriter.java`: ghi kết quả dịch ra file `.imtp` (theo cấu trúc trên).
3. Tạo `ImtpReader.java`: đọc `.imtp`, trả về MSL source per program.
4. Gradle task `translateShaders` (hoặc main class `net.irisshaders.iris.metal.ShaderTranslatorCLI`):
   - Input: thư mục shaderpack (`.vsh`, `.fsh`, `.gsh`, `.json`)
   - Output: thư mục `.imtp` cùng tên
5. Unit test (pure Java, không cần Minecraft): dịch một số shader mẫu từ `assets/iris/shaders/`.

File dự kiến:
- `.../shader/translator/GlslToMslTranslator.java` (nâng cấp từ skeleton)
- `.../shader/translator/GlslBuiltinMap.java` (bổ sung đầy đủ)
- `.../shader/translator/ImtpFormat.java` (mới, định dạng file)
- `.../shader/translator/ImtpWriter.java` (mới)
- `.../shader/translator/ImtpReader.java` (mới)
- `.../metal/ShaderTranslatorCLI.java` (mới)
- `build.gradle.kts`: thêm task `translateShaders`

Acceptance:
- Task `./gradlew translateShaders` chạy được, xuất `.imtp` đúng cấu trúc.
- `.imtp` có thể đọc lại và ra MSL hợp lệ (kiểm tra bằng eye-test / rồi sau là Metal compile).

## P4 — Nạp shaderpack: ưu tiên .imtp; .zip/không đuôi → raw GL

Mục tiêu: Mod chỉ nhận shader đuôi `.imtp` (đã dịch). Nếu là `.zip` hoặc không đuôi → nạp nguyên bản (chưa dịch, dùng OpenGL).

Công việc chi tiết:

1. Trong `ShaderpackDirectoryManager.java` / `IrisShaderPack.java` (tìm class nạp shaderpack):
   - Kiểm tra đuôi file/tên thư mục shaderpack.
   - Nếu `.imtp`: đọc bằng `ImtpReader`, nạp MSL vào `MetalBackend.createProgram()`.
   - Nếu `.zip` hoặc không đuôi: giữ logic nạp GLSL hiện tại (OpenGL), báo thông báo "chưa dịch, dùng OpenGL".
2. Thêm cờ cấu hình `iris.metal.enabled` (true/false) và `iris.metal.requireImtp` (true/false).
3. Nếu `metal.enabled=true` nhưng shaderpack không có `.imtp` và `requireImtp=true` → từ chối nạp, thông báo lỗi rõ ràng.

File dự kiến:
- `common/src/main/java/net/irisshaders/iris/shaderpack/...` (class nạp shaderpack, cần grep tên chính xác)
- `.../backend/BackendRegistry.java` (đọc cờ)
- Config mới hoặc `iris.toml`

Acceptance:
- Shaderpack `.imtp` → nạp qua Metal backend.
- Shaderpack `.zip` / thư mục không đuôi → nạp qua OpenGL, có thông báo.

## P5 — UI trong game: màn hình dịch + progress bar

Mục tiêu: Người dùng có thể bật màn hình dịch shaderpack trong game, xem thanh tiến trình.

Công việc chi tiết:

1. Tạo screen GUI (Fabric screen): `MetalShaderTranslationScreen.java` (mở từ menu shaderpack / command).
   - Danh sách shaderpack phát hiện được.
   - Nút "Dịch sang .imtp" cho từng shaderpack (hoặc tất cả).
2. Thanh tiến trình: `ProgressBarWidget.java` (tùy chỉnh Fabric widget) + labels percent.
3. Luồng dịch nền: `ShaderTranslationTask.java` (`Runnable`/`CompletableFuture`) gọi `GlslToMslTranslator` + `ImtpWriter`, cập nhật progress trên UI thread.
4. Log dịch: hiển thị warning/lỗi từng shader (missing builtin, unsupported geometry...).
5. Nút "Mở thư mục kết quả .imtp", "Nạp shaderpack đã dịch".

File dự kiến:
- `.../metal/ui/MetalShaderTranslationScreen.java` (mới)
- `.../metal/ui/ProgressBarWidget.java` (mới)
- `.../metal/ui/ShaderTranslationTask.java` (mới)
- Menu entry: sửa `Iris.java` hoặc screen menu shaderpack hiện có để thêm nút mở màn hình này.

Acceptance:
- Mở màn hình dịch từ trong game.
- Chọn shaderpack → nhấn dịch → progress bar chạy 0→100% → file `.imtp` xuất ra.
- Có thông báo thành công/lỗi.

## P6 — Kết nối backend vào ShaderCreator/pipeline

Mục tiêu: Khi bật Metal, shader program được tạo bằng `MetalBackend` thay vì OpenGL `GlStateManager.glCreateProgram`.

Công việc chi tiết:

1. Trong `ShaderCreator.java` (`link()` / `create()`):
   - Nếu `BackendRegistry.activeType() == METAL_*`:
   - Lấy MSL source từ `.imtp` (đã dịch trong P3/P4).
   - Gọi `metalBackend.createProgram(name, vertexMSL, fragmentMSL, geometryMSL)`.
   - Trả về `ExtendedShader` / `FallbackShader` wrapper dành cho Metal (hoặc adapter `MetalShader` implements `IrisProgram`).
   - Nếu OpenGL: giữ nguyên code cũ.
2. Tạo `MetalShader.java` implements `IrisProgram` (gộp uniform, texture binding qua `MetalBackend`).
3. Cập nhật `ExtendedShader.java` / `FallbackShader.java` để hỗ trợ Metal (hoặc tách ra `MetalExtendedShader`).
4. Pipeline renderers (`IrisRenderingPipeline`, `WorldRenderingPipeline`, ...): khi bật Metal, gọi `metalBackend.beginFrame`/`endFrame`/`draw`/`setViewport`/`clear` thay vì lệnh GL trực tiếp (giai đoạn cuối, có thể để P7+).

File dự kiến:
- `.../pipeline/programs/ShaderCreator.java` (nhánh if metal)
- `.../pipeline/programs/MetalShader.java` (mới)
- `.../pipeline/programs/MetalExtendedShader.java` (mới, tùy chọn)
- `.../backend/MetalBackend.java` (hoàn thiện các method vẽ)

Acceptance:
- Bật cờ `metal.enabled=true`, có `.imtp` → game vào được Main Menu (hoặc không crash khi nạp shader).
- Bật cờ `metal.enabled=false` → hoạt động như bản OpenGL gốc.

## P7 — Build/test Minecraft 26.3 + media

Mục tiêu: Build mod, chạy thử trên Minecraft 26.3, chụp ảnh cho README.

Công việc chi tiết:

1. Kiểm tra `build.gradle.kts` có thể build với Fabric-loom 1.17.20 + MC 26.3.
2. Chạy `./gradlew build` (hoặc `fabric-loom runClient`) trên máy có Minecraft dev environment.
3. Sử dụng `/Volumes/project/mc-api/API-Bridge` để điều khiển/test UI (nếu có).
4. Chụp ảnh:
   - Màn hình dịch shader (progress bar).
   - Menu shaderpack với `.imtp`.
   - In-game với shader Metal (nếu chạy được).
5. Lưu ảnh vào `docs/media/` (hoặc `src/main/resources/assets/iris/meta/` tùy convention).

Lưu ý: Máy hiện tại có thể không có Minecraft client đầy đủ / đăng nhập Microsoft. Nếu không chạy được client, chỉ build và ghi "chưa thể chạy client do môi trường".

File dự kiến:
- `docs/media/*.png`
- Cập nhật `build.gradle.kts` nếu cần thêm test dependency.

## P8 — Rewrite README + credit + release

Mục tiêu: Tài liệu cuối cùng, công khai.

Nội dung README (tiếng Việt + Anh):

- Giới thiệu Iris-MTP (fork từ IrisShaders/Iris nhánh 26.3).
- Cách cài đặt (Metallum dependency, Fabric/NeoForge 26.3).
- Cách dùng: bật Metal, shaderpack `.imtp`, màn hình dịch.
- Credit: IrisShaders/Iris (OpenGL gốc), Sodium (GL shader base), Metallum (kokodio, Metal binding), Ynnyny (Iris-MTP port).
- Cảnh báo: Metal backend đang phát triển, OpenGL là mặc định/an định.

File dự kiến: `README.md` (viết lại hoàn toàn), `README_vi.md` (tùy chọn).

## File tạo/sửa (tổng hợp)

Mới tạo:
- `PLAN.md`
- `common/src/main/java/net/irisshaders/iris/shader/translator/ImtpFormat.java`
- `common/src/main/java/net/irisshaders/iris/shader/translator/ImtpWriter.java`
- `common/src/main/java/net/irisshaders/iris/shader/translator/ImtpReader.java`
- `common/src/main/java/net/irisshaders/iris/metal/ShaderTranslatorCLI.java`
- `common/src/main/java/net/irisshaders/iris/metal/ui/MetalShaderTranslationScreen.java`
- `common/src/main/java/net/irisshaders/iris/metal/ui/ProgressBarWidget.java`
- `common/src/main/java/net/irisshaders/iris/metal/ui/ShaderTranslationTask.java`
- `common/src/main/java/net/irisshaders/iris/pipeline/programs/MetalShader.java`
- `common/src/main/java/net/irisshaders/iris/pipeline/programs/MetalExtendedShader.java`
- `docs/media/*.png`
- `README.md`

Cần sửa:
- `build.gradle.kts` (thêm Metallum dependency + task `translateShaders`)
- `settings.gradle.kts` (repository Metallum)
- `ShaderCreator.java` (nhánh Metal)
- `Iris.java` / menu shaderpack (nút mở màn hình dịch)
- Shaderpack nạp class (phân loại `.imtp` vs `.zip`/không đuôi)
- `common/src/main/java/net/irisshaders/iris/backend/RenderBackend.java` (đã có skeleton)
- `.../backend/OpenGLBackend.java` (đã có skeleton)
- `.../backend/MetalBackend.java` (đã có skeleton)
- `.../backend/BackendRegistry.java` (đã có skeleton)
- `.../shader/translator/GlslToMslTranslator.java` (đã có skeleton)
- `.../shader/translator/GlslBuiltinMap.java` (đã có)
- `.../shader/translator/TranslationResult.java` (đã có)

## Rủi ro & giảm thiểu

| Rủi ro | Mức độ | Cách giảm thiểu |
|--------|--------|-----------------|
| Port toàn bộ OpenGL→Metal quá lớn | Cao | Chỉ làm abstraction + Metal backend skeleton; GL giữ mặc định; Metal bật tùy chọn. |
| GLSL→MSL không dịch đúng 100% | Cao | Dùng `.imtp` (dịch trước, có thể chỉnh tay); cho phép fallback OpenGL khi `.imtp` thiếu/lỗi. |
| Metallum chưa hỗ Intel/iOS | Trung | Fork Metallum, bổ platform abstraction; nếu không làm được, MetalBackend throw "not implemented" và fallback GL. |
| Không thể chạy Minecraft client test | Trung | Chỉ build Gradle + pure-Java unit test translator; ghi rõ "chưa test client". |
| Java 25 vs build tool | Thấp | `build.gradle.kts` đã dùng Java 25; kiểm tra `./gradlew compileJava`. |

## Lệnh Git tham khảo

```
cd /Volumes/project/Metal-Shader/iris
git checkout metal-port
# edit file...
git add <file>
git commit -m "feat(metal-port): <mô tả ngắn>"
git push origin metal-port
```

## Người thực hiện cần biết

- Không được xóa/sửa code OpenGL cũ — chỉ thêm lớp mới song song.
- Mỗi phase commit riêng, không gộp quá lớn.
- Pure-Java classes (backend abstraction, translator) có thể test bằng javac độc lập trước khi ghép vào Gradle.
- Minecraft 26.3 test chỉ làm được khi có môi trường Fabric dev đầy đủ; nếu không, skip P7 client test.
