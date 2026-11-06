package net.irisshaders.iris.metal.ui;

/**
 * Widget hien thi thanh tien trinh dich shader (placeholder, can test client).
 * AbstractWidget API phu thuoc version Minecraft; lop nay luu tru progress
 * de GUI render sau khi co thong tin API chuan xac.
 */
public class ProgressBarWidget {
	private final String label;
	private float progress;

	public ProgressBarWidget(String label) {
		this.label = label;
		this.progress = 0.0f;
	}

	public void setProgress(float progress) {
		this.progress = Math.max(0.0f, Math.min(1.0f, progress));
	}

	public float getProgress() {
		return progress;
	}

	public String getLabel() {
		return label;
	}
}
