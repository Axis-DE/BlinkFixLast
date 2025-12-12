package moe.ichinomiya.naven.utils;

public class SmoothAnimationTimer {
	public float target;
	public float speed = 0.4f;
	public float value;

	public SmoothAnimationTimer(float target) {
		this.target = target;
		this.value = target;
	}

	public SmoothAnimationTimer(float target, float speed) {
		this.target = target;
		this.speed = speed;
		this.value = target;
	}

	public void update(boolean increment) {
		this.value = AnimationUtils.getAnimationState(value, increment ? target : 0, (Math.max(10, (Math.abs(this.value - (increment ? target : 0))) * 40) * speed));
	}

	public boolean isAnimationDone(boolean increment) {
		return increment ? value == target : value == 0;
	}
}
