# Iris-MTP — Port Metal + Dịch GLSL→MSL (.imtp)

Fork từ [IrisShaders/Iris](https://github.com/IrisShaders/Iris) nhánh 26.3. Nhánh `metal-port`, commit `96d8a5b`.

**Tiếng Việt | [English](#english)**

---

## Tiếng Việt

### Giới thiệu

Iris-MTP là một port của Iris Shader mods dành cho Minecraft 26.3 (Fabric/NeoForge). Thay vì dịch GLSL→Metal runtime (khối lượng lớn, cấp tháng), Iris-MTP dịch **trước** shader GLSL sang MSL (Metal Shading Language), lưu ra file `.imtp` (Iris Metal Translated Package), rồi mod chỉ nạp file `.imtp` đã dịch.

- `.zip` / thư mục không đuôi → nạp nguyên bản (chưa dịch), dùng OpenGL backend (mặc định).
- `.imtp` → nạp qua Metal backend (iOS, macOS Intel, macOS ARM).
- OpenGL giữ nguyên làm fallback/mặc định; không xóa/sửa code OpenGL hiện có.

### Credit

- **IrisShaders/Iris** — OpenGL gốc, bản nhánh 26.3.
- **Sodium** — GL shader base.
- **Metallum** (kokodio) — Metal native binding (metal-jvm).
- **Ynnyny** — Iris-MTP port (này).

### Cách cài đặt

1. Clone repo: `https://github.com/Ynnyny/Iris-MTP.git` (nhánh `metal-port`).
2. Metallum fork: `https://github.com/Ynnyny/Metallum` (nhánh `metal-multi-platform`), build và cài vào Maven Local hoặc dùng file jar trong `build/libs`.
3. Trong `iris/common/build.gradle.kts`, Metallum đã được thêm làm dependency (flatDir trỏ đến `metallum/build/libs`).
4. Build: `./gradlew :fabric:build` (hoặc `:neoforge:build`).
   - Yêu cầu Java 25 (xem `build.gradle.kts`). Máy hiện tại build OK với Java 25 (Temurin 25.0.5).

### Cách dùng

1. Dùng task Gradle để dịch shaderpack sang `.imtp`:
   ```
   ./gradlew :common:translateShaders
   ```
   Hoặc chạy CLI trực tiếp:
   ```
   java net.irisshaders.iris.metal.ShaderTranslatorCLI <shaderpack-dir> <output-dir> [mc-version]
   ```
2. File `.imtp` xuất ra sẽ được đặt vào thư mục đầu ra (tên pack + `.imtp`).
3. Đặt file `.imtp` vào thư mục `shaderpacks/` của Minecraft.
4. Trong game, chọn shaderpack `.imtp`. Nếu `iris.metal.enabled=true` và có `.imtp`, mod sẽ nạp qua Metal backend. Nếu là `.zip` / không đuôi, mod dùng OpenGL và báo "chưa dịch, dùng OpenGL".
5. Cấu hình trong `iris.properties`:
   - `metalEnabled=true/false`
   - `backend=opengl|metal`
   - `metalRequireImtp=true/false`

### UI trong game

Màn hình dịch shader (`MetalShaderTranslationScreen`) và thanh tiến trình (`ProgressBarWidget`) đã được tạo làm placeholder (P5). Hoàn thiện rendering GUI cần API Minecraft 26.3 client đầy đủ.

### Trạng thái build

- `:common:compileJava` — OK (Java 25).
- `:fabric:compileJava` — OK.
- Task `:common:translateShaders` — OK, xuất file `.imtp` đúng cấu trúc.
- Minecraft client test: **chưa thể chạy** do không có Minecraft dev environment đầy đủ / đăng nhập Microsoft. Ghi chú này trong README.

### Ràng buộc

- Không xóa/sửa code OpenGL cũ — chỉ thêm lớp mới song song.
- Mỗi phase commit riêng, không gộp quá lớn.
- Pure-Java classes (backend abstraction, translator) có thể test bằng javac độc lập trước khi ghép vào Gradle.

---

## English

### Introduction

Iris-MTP is a Metal port of Iris Shader mods for Minecraft 26.3 (Fabric/NeoForge). Instead of translating GLSL→Metal at runtime (a multi-month effort), Iris-MTP translates GLSL to MSL (Metal Shading Language) **ahead-of-time**, saving to `.imtp` files (Iris Metal Translated Package), then the mod loads only the pre-translated `.imtp`.

- `.zip` / folder without extension → loaded raw (untranslated), using OpenGL backend (default).
- `.imtp` → loaded via Metal backend (iOS, macOS Intel, macOS ARM).
- OpenGL remains as fallback/default; existing OpenGL code is untouched.

### Credits

- **IrisShaders/Iris** — original OpenGL, branch 26.3.
- **Sodium** — GL shader base.
- **Metallum** (kokodio) — Metal native binding (metal-jvm).
- **Ynnyny** — Iris-MTP port.

### Installation

1. Clone repo: `https://github.com/Ynnyny/Iris-MTP.git` (branch `metal-port`).
2. Metallum fork: `https://github.com/Ynnyny/Metallum` (branch `metal-multi-platform`), build and install to Maven Local or use the jar in `build/libs`.
3. In `iris/common/build.gradle.kts`, Metallum is added as a dependency (flatDir pointing to `metallum/build/libs`).
4. Build: `./gradlew :fabric:build` (or `:neoforge:build`).
   - Requires Java 25 (see `build.gradle.kts`). Build verified with Java 25 (Temurin 25.0.5).

### Usage

1. Translate a shaderpack to `.imtp` via Gradle task:
   ```
   ./gradlew :common:translateShaders
   ```
   Or run the CLI directly:
   ```
   java net.irisshaders.iris.metal.ShaderTranslatorCLI <shaderpack-dir> <output-dir> [mc-version]
   ```
2. The `.imtp` file is written to the output directory (pack name + `.imtp`).
3. Place the `.imtp` file into Minecraft's `shaderpacks/` folder.
4. In-game, select the `.imtp` shaderpack. If `iris.metal.enabled=true` and `.imtp` exists, the mod loads via Metal backend. If `.zip` / no extension, the mod uses OpenGL and reports "not translated, using OpenGL".
5. Config in `iris.properties`:
   - `metalEnabled=true/false`
   - `backend=opengl|metal`
   - `metalRequireImtp=true/false`

### In-game UI

The shader translation screen (`MetalShaderTranslationScreen`) and progress bar (`ProgressBarWidget`) are placeholders (P5). Full GUI rendering requires a complete Minecraft 26.3 client environment.

### Build Status

- `:common:compileJava` — OK (Java 25).
- `:fabric:compileJava` — OK.
- Task `:common:translateShaders` — OK, outputs `.imtp` with correct structure.
- Minecraft client test: **not runnable** due to missing full Minecraft dev environment / Microsoft account login. Noted in this README.

### Constraints

- Do not delete/modify existing OpenGL code — only add parallel new layers.
- Each phase committed separately, not squashed.
- Pure-Java classes (backend abstraction, translator) can be tested with javac independently before integrating into Gradle.
