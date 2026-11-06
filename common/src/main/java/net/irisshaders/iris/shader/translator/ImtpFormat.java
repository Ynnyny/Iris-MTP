package net.irisshaders.iris.shader.translator;

/**
 * \u0110\u1ecbnh d\u1ea1ng file .imtp (Iris Metal Translated Package).
 *
 * <p>C\u00e1c d\u00f2ng c\u1ee7a file .imtp:
 * <pre>
 * # Iris Metal Translated Package (imtp)
 * # version=1; source=shaderpack-name; mc=26.3; timestamp=...
 * [programs]
 * program_id = {
 *   vertex = """...msl..."""
 *   fragment = """...msl..."""
 *   geometry = """...msl..."""  # optional
 * }
 * </pre>
 */
public final class ImtpFormat {
	public static final String MAGIC = "# Iris Metal Translated Package (imtp)";
	public static final String VERSION_KEY = "version";
	public static final String SOURCE_KEY = "source";
	public static final String MC_KEY = "mc";
	public static final String TIMESTAMP_KEY = "timestamp";
	public static final String PROGRAMS_SECTION = "[programs]";

	public static final int VERSION = 1;

	private ImtpFormat() {
	}
}
