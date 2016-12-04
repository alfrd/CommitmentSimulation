package main;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

public class CommitmentSimulation {
	private MessageDigest md;
	public int x;

	private int vAlice;
	private int kAlice;
	private int nbrConcealed;
	public double bindingProb;
	public double concealingProb;

	//int x is the number of bits left after truncation.
	public CommitmentSimulation(int x) {
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.x = x;
		nbrConcealed = 0;

		bindingProb = breakBinding(400);
		
		concealingProb = breakConcealing(400);

		System.out.println(nbrConcealed + " votes are still concealed out of 100 when we truncate to " + x + " bits");

	}

	private double breakBinding(int iterations) {
		// We try to break the binding property of iteration-nbr of commitments
		int iter = iterations;
		double broken = 0;
		System.out.println("Number of binding simulations (" + x + " bits): ");
		for (int i = 1; i <= iter; i++) {
			if (bindingSimulation()) {
				broken++;
				//System.out.println(broken + " broken, " + i + " tried.");
			}
			System.out.print(i + ", ");
			if(i%10==0) {
				System.out.println("");
			}
			
		}
		double prob = broken / iter;
		
		return prob;
	}

	private double breakConcealing(int iterations) {
		// We try to break the concealing property of iterations-nbr of commitments
		int iterC = iterations;
		double brokenC = 0;
		Random r = new Random();
		System.out.println("Number of concealing simulations (" + x + " bits): ");
		for (int j = 1; j <= iterC; j++) {
			if (concealingSimulation(r.nextInt(2))) {
				brokenC++;
				//System.out.println(brokenC + " disclosed, " + j + " tried.");
			}
			System.out.print(j + ", ");
			if(j%10==0) {
				System.out.println("");
			}
			// System.out.println("Found a match after trying " + iterations +
			// " different commitments.");
		}
		System.out.println("");
		double probC = brokenC / iterC;
		
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
		md.reset();
		md.update(toHash.getBytes());
		byte[] commitment = md.digest();
		String commitmentString = makeBitStringAndTrunc(commitment, x);
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
			String guessString = makeBitStringAndTrunc(guess, x);
			if (guessString.equals(commitmentString)) {
				go = false;
				return true;

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
		
		//Make bit-string of commitment
		String commitmentString = makeBitStringAndTrunc(commitment, x);
		

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
			String guessString = makeBitStringAndTrunc(guess, x);
			
			if (guessString.equals(commitmentString)) {
				
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

	private String makeBitStringAndTrunc(byte[] array, int x) {
		int bytesTrim = x/8 + 1;
		String commitmentString = "";
		for (int i = 0; i < bytesTrim; i++) {
			String s = String.format("%8s", Integer.toBinaryString(array[i] & 0xFF)).replace(' ', '0');
			commitmentString += s;
		}
		return commitmentString.substring(0, x);
	}
	
	
	public static void main(String args[]) {
		// K is fixed at 16 bits, V is one bit
		// We need a byte array to put in the hash function.
		// The first bit in that byte array is the V and the 16 subsequent bits
		// represent the random number k
		CommitmentSimulation cs = new CommitmentSimulation(14);
		CommitmentSimulation cs1 = new CommitmentSimulation(15);
		CommitmentSimulation cs2 = new CommitmentSimulation(16);
		CommitmentSimulation cs3 = new CommitmentSimulation(17);
		CommitmentSimulation cs4 = new CommitmentSimulation(18);
		
		System.out.println("");
		System.out.println("Stats time:");
		System.out.println("P(bindingBreak) = " + cs.bindingProb + ", P(concealingBreak) =  " + cs.concealingProb 
				+ " when truncated to " + cs.x + " bits.");
		System.out.println("P(bindingBreak) = " + cs1.bindingProb + ", P(concealingBreak) =  " + cs1.concealingProb 
				+ " when truncated to " + cs1.x + " bits.");
		System.out.println("P(bindingBreak) = " + cs2.bindingProb + ", P(concealingBreak) =  " + cs2.concealingProb 
				+ " when truncated to " + cs2.x + " bits.");
		System.out.println("P(bindingBreak) = " + cs3.bindingProb + ", P(concealingBreak) =  " + cs3.concealingProb 
				+ " when truncated to " + cs3.x + " bits.");
		System.out.println("P(bindingBreak) = " + cs4.bindingProb + ", P(concealingBreak) =  " + cs4.concealingProb 
				+ " when truncated to " + cs4.x + " bits.");
	}
}
