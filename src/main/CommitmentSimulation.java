package main;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

public class CommitmentSimulation {
	private MessageDigest md;
	private int x;

	private int vAlice;
	private int kAlice;
	private int nbrConcealed;

	//int x is the number of bytes we remove when we truncate. 
	public CommitmentSimulation(int x) {
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.x = x;
		nbrConcealed = 0;

		breakBinding(1000);
		
		breakConcealing(1000);

		System.out.println(nbrConcealed + " votes are still concealed out of 1000 when we truncate " + x + " bytes of 20");

	}

	private double breakBinding(int iterations) {
		// We try to break the binding property of iteration-nbr of commitments
		int iter = iterations;
		double broken = 0;
		for (int i = 0; i < iter; i++) {
			if (bindingSimulation()) {
				broken++;
				//System.out.println(broken + " broken, " + i + " tried.");
			}
			// System.out.println("Found a match after trying " + iterations +
			// " different commitments.");
		}
		double prob = broken / iter;
		System.out.println("Probability of being able to break binding " + prob + " when we truncate " + x +" bytes of 20");
		return prob;
	}

	private double breakConcealing(int iterations) {
		// We try to break the concealing property of iterations-nbr of commitments
		int iterC = iterations;
		double brokenC = 0;
		Random r = new Random();
		
		for (int j = 1; j <= iterC; j++) {
			if (concealingSimulation(r.nextInt(2))) {
				brokenC++;
				//System.out.println(brokenC + " disclosed, " + j + " tried.");
			}
			// System.out.println("Found a match after trying " + iterations +
			// " different commitments.");
		}
		
		double probC = brokenC / iterC;
		System.out.println("Probability of being able to break concealing is "
				+ probC + " when we truncate " + x + " bytes of 20");
		return probC;
	}

	private boolean bindingSimulation() {
		// Alice creates commitment
		Random rand = new Random();
		vAlice = 0;
		kAlice = rand.nextInt(65536);
		String temp1 = Integer.toBinaryString(vAlice);
		String temp2 = Integer.toBinaryString(kAlice);

		// 0 padding
		if (temp2.length() != 16) {
			int howMany0 = 16 - temp2.length();
			String z = "";
			for (int i = 0; i < howMany0; i++) {
				z = z + "0";
			}
			temp2 = z + temp2;
		}
		String toHash = temp1 + temp2;
		// System.out.println("Det här är " + toHash.length() + " st bitar: " +
		// toHash);
		// System.out.println("This number should start with 0: " + toHash);
		md.reset();
		md.update(toHash.getBytes());
		byte[] commitment = md.digest();
		byte[] truncCommit = new byte[20 - x];
		for (int i = 0; i < 20 - x; i++) {
			truncCommit[i] = commitment[i];
		}
		// System.out.println("Commitment length: " + truncCommit.length);

		// Start to break commitment. AKA find a (0,kBreakBinding) that gives
		// the same hash as (1,kAlice) after truncation
		int vBreakBinding = 0;
		if (vAlice == 0) {
			vBreakBinding = 1;
		}
		int kBreakBinding = 0;
		boolean go = true;
		while (go) {
			String temp3 = Integer.toBinaryString(vBreakBinding);
			String temp4 = Integer.toBinaryString(kBreakBinding);

			// 0 padding
			if (temp4.length() < 16) {
				int howMany0 = 16 - temp4.length();
				String zeros = "";
				for (int i = 0; i < howMany0; i++) {
					zeros = zeros + "0";
				}

				temp4 = zeros + temp4;
			}

			String toHashGuess = temp3 + temp4;
			// System.out.println("Binary guess (" + toHashGuess.length() +
			// " bits) : " + toHashGuess);
			md.reset();
			md.update(toHashGuess.getBytes());
			byte[] guess = md.digest();
			byte[] truncGuess = new byte[20 - x];
			for (int j = 0; j < 20 - x; j++) {
				truncGuess[j] = guess[j];
			}
			if (Arrays.equals(truncGuess, truncCommit)) {
				go = false;
				return true;

				// System.out.println("DONE!");
				// //System.out.println("Was supposed to match: (" +
				// toHashGuess.length() + " bits) : " + toHash);
				// System.out.println("Guess hash: ");
				// printByteArray(guess);
				//
				// System.out.println("TruncGuess hash: ");
				// printByteArray(truncGuess);
				//
				// System.out.println("Commit hash: ");
				// printByteArray(commitment);

			} else if (kBreakBinding > 65534) {
				go = false;
			}
			kBreakBinding++;

		}
		return false;
	}

	//returns false if the vote is still concealed
	//returns true if the vote is disclosed
	private boolean concealingSimulation(int v) {
		// Alice creates commitment
		Random rand = new Random();
		int vAliceC = v;
		int kAliceC = rand.nextInt(65536);
		String temp1 = Integer.toBinaryString(vAliceC);
		String temp2 = Integer.toBinaryString(kAliceC);

		// 0 padding
		if (temp2.length() != 16) {
			int howMany0 = 16 - temp2.length();
			String z = "";
			for (int i = 0; i < howMany0; i++) {
				z = z + "0";
			}
			temp2 = z + temp2;
		}
		String toHash = temp1 + temp2;
		md.reset();
		md.update(toHash.getBytes());
		byte[] commitment = md.digest();
		byte[] truncCommit = new byte[20 - x];
		for (int i = 0; i < 20 - x; i++) {
			truncCommit[i] = commitment[i];
		}

		// truncCommit is sent from Alice to Bob. Now bob will try to find out
		// if Alice voted 1 och 0.
		
		boolean v0solution = false;
		boolean v1solution = false;

		int vBreakConcealing = 0;
		int kBreakConcealing = 0; // Tries all k with v=0 first. After that we try all k with v=1;
		boolean go = true;

		while (go) {
			String temp3 = Integer.toBinaryString(vBreakConcealing);
			String temp4 = Integer.toBinaryString(kBreakConcealing);

			// 0 padding
			if (temp4.length() < 16) {
				int howMany0 = 16 - temp4.length();
				String zeros = "";
				for (int i = 0; i < howMany0; i++) {
					zeros = zeros + "0";
				}

				temp4 = zeros + temp4;
			}

			String toHashGuess = temp3 + temp4;
			md.reset();
			md.update(toHashGuess.getBytes());
			byte[] guess = md.digest();
			byte[] truncGuess = new byte[20 - x];
			for (int j = 0; j < 20 - x; j++) {
				truncGuess[j] = guess[j];
			}

			if (Arrays.equals(truncGuess, truncCommit)) {
				
				if(vBreakConcealing == 0) {
					v0solution = true;
				} else {
					v1solution = true;
					go = false;
				}

				
			//Om vi har testat alla k och v=0, då ändrar vi till v=1 och börjar om med k=0
			} else if (kBreakConcealing > 65534 && vBreakConcealing == 0) {
				vBreakConcealing = 1; // If v=0 was not successful, we try with v=1
				kBreakConcealing = -1;
				
			//Om vi har testat alla k och v=1, då har vi testat alla alternativ och bryter loopen
			} else if (kBreakConcealing > 65534) {
				go = false;
			}
			
			//Om vi inte har testat klart och inte heller fått någon match på hashen
			//så ökar vi k och loopar igen.
			kBreakConcealing++;

		}
		
		
		//Om det bara fanns en lösning för v=0
		if(v0solution && !v1solution) {
			//System.out.println("Fanns endast en lösning för v=0 när Alice comittade " + v);
			return true;
		//Om det bara fanns en lösning för v=1
		} else if(!v0solution && v1solution) {
			//System.out.println("Fanns endast en lösning för v=1 när Alice comittade " + v);
			return true;
		//Om det fanns lösning för både v=0 och v=1. Alltså att v är concealed.
		} else {
			//System.out.println("Fanns en lösning för både v=0 och v=1 när Alice comittade " + v);
			nbrConcealed++;
			return false;
		}
		
	}

	private void printByteArray(byte[] array) {
		for (int i = 0; i < array.length; i++) {
			System.out.print(array[i] + " ");
		}
		System.out.print(" " + array.length + " bytes ");
		System.out.println("");
	}

	public static void main(String args[]) {
		// K is fixed at 16 bits, V is one bit
		// We need a byte array to put in the hash function.
		// The first bit in that byte array is the V and the 16 subsequent bits
		// represent the random number k
		CommitmentSimulation cs = new CommitmentSimulation(19);
		CommitmentSimulation cs1 = new CommitmentSimulation(18);
		CommitmentSimulation cs2 = new CommitmentSimulation(17);
		CommitmentSimulation cs3 = new CommitmentSimulation(16);
	}
}
