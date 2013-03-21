package realrec.cbox.storm.utils;

public class RunningAverage {

	private int count = 0;
	private double average = Double.NaN;

	public synchronized void addDatum(double datum) {
		if (++count == 1) {
			average = datum;
		} else {
			average = average * (count - 1) / count + datum / count;
		}
	}

	public synchronized void removeDatum(double datum) {
		if (count == 0) {
			throw new IllegalStateException();
		}
		if (--count == 0) {
			average = Double.NaN;
		} else {
			average = average * (count + 1) / count - datum / count;
		}
	}

	public synchronized int getCount() {
		return count;
	}

	public synchronized double getAverage() {
		return average;
	}

}
