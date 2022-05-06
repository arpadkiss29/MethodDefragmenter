package ro.lrg.method.defragmenter.utils;

import java.util.Map;

public class EnvyComputer {
	public static boolean computeEnvy(int ATFD, Map<String, Integer> FDPMap, int LAA, int ATFDTreshold, int FDPTreshold, double LAATreshold) {
		for (Integer numberOfAccesses : FDPMap.values()) {
			ATFD += numberOfAccesses;
		}
		int totalAccesses = ATFD + LAA;
		int FDP = FDPMap.size();

		if (ATFD > ATFDTreshold
				&& (LAA == 0 ? 0 : (LAA * 1.0) / totalAccesses) < LAATreshold
				&& FDP <= FDPTreshold) {
			return true;
		}
		
		return false;
	}
}
