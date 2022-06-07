package ro.lrg.method.defragmenter.utils;

public class EnvyComputer {
	public static boolean computeEnvy(int ATFD, int FDP, double LAA, int ATFDTreshold, int FDPTreshold, double LAATreshold) {
		if (ATFD > ATFDTreshold && LAA < LAATreshold && FDP <= FDPTreshold) return true;
		return false;
	}
}
