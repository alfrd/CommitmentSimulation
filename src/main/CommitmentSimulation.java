package main;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

public class CommitmentSimulation {
	MessageDigest md;
	int x;
	
	int vAlice;
	int kAlice;
	public CommitmentSimulation(int x) {
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.x = x;
		
		if(true) {
		//We try to break the binding property of 1000 commitments
		int iter = 1000;
		double broken = 0;
		for(int i = 0; i <= iter; i++) {
			if(bindingSimulation()) {
				broken++;
				System.out.println(broken + " broken, " + i + " tried.");
			}
			//System.out.println("Found a match after trying " + iterations + " different commitments.");
		}
		double prob = broken/iter;
		System.out.println("Probability of being able to break binding " + prob);
		
		}
		if(true) {
		//We try to break the concealing property of 1000 commitments
		int iterC = 1000;
		double brokenC = 0;
		Random r = new Random();
		for(int j = 1; j <= iterC; j++) {
			if(concealingSimulation(r.nextInt(2))) {
				brokenC++;
				System.out.println(brokenC + " broken, " + j + " tried.");
			}
			//System.out.println("Found a match after trying " + iterations + " different commitments.");
		}
		double probC = brokenC/iterC;
		System.out.println("Probability of being able to break concealing " + probC);
		}
	}
	
	private boolean bindingSimulation() {
		//Alice creates commitment
		Random rand = new Random();
		vAlice = 0;
		kAlice = rand.nextInt(65536);
		String temp1 = Integer.toBinaryString(vAlice);
		String temp2 = Integer.toBinaryString(kAlice);
		
		//0 padding
		if(temp2.length() != 16) {
			int howMany0 = 16 - temp2.length();
			String z = "";
			for(int i = 0; i < howMany0; i++) {
				z = z + "0";
			}
			temp2 = z + temp2;
		}
		String toHash = temp1 + temp2;
//		System.out.println("Det här är " + toHash.length() + " st bitar: " + toHash);
//		System.out.println("This number should start with 0: " + toHash);
		md.reset();
		md.update(toHash.getBytes());
		byte[] commitment = md.digest();
		byte[] truncCommit = new byte[20 - x];
		for(int i = 0; i < 20 - x; i++) {
			truncCommit[i] = commitment[i];
		}
//		System.out.println("Commitment length: " + truncCommit.length); 
		
		//Start to break commitment. AKA find a (0,kBreakBinding) that gives the same hash as (1,kAlice) after truncation
		int vBreakBinding = 0;
		if(vAlice == 0) {
			vBreakBinding = 1;
		}
		int kBreakBinding = 0;
		boolean go = true;
		while(go) {
			String temp3 = Integer.toBinaryString(vBreakBinding);
			String temp4 = Integer.toBinaryString(kBreakBinding);
			
			//0 padding
			if(temp4.length() < 16) {
				int howMany0 = 16 - temp4.length();
				String zeros = "";
				for(int i = 0; i < howMany0; i++) {
					zeros = zeros + "0";
				}
				
				temp4 = zeros + temp4;
			}
			
			
			String toHashGuess = temp3 + temp4;
			//System.out.println("Binary guess (" + toHashGuess.length() + " bits) : " + toHashGuess);
			md.reset();
			md.update(toHashGuess.getBytes());
			byte[] guess = md.digest();
			byte[] truncGuess = new byte[20 - x];
			for(int j = 0; j < 20 - x; j++) {
				truncGuess[j] = guess[j];
			}
			if(Arrays.equals(truncGuess, truncCommit)) {
				go = false;
				return true;
				
//				System.out.println("DONE!");
//				//System.out.println("Was supposed to match: (" + toHashGuess.length() + " bits) : " + toHash);
//				System.out.println("Guess hash: ");
//				printByteArray(guess);
//				
//				System.out.println("TruncGuess hash: ");
//				printByteArray(truncGuess);
//				
//				System.out.println("Commit hash: ");
//				printByteArray(commitment);
				
			} else if(kBreakBinding > 65534) {
				go = false;
			}
			kBreakBinding++;
			
		}
		return false;
	}

	private boolean concealingSimulation(int v) {
		//Alice creates commitment
		Random rand = new Random();
		int vAliceC = v;
		int kAliceC = rand.nextInt(65536);
		String temp1 = Integer.toBinaryString(vAliceC);
		String temp2 = Integer.toBinaryString(kAliceC);
		
		//0 padding
		if(temp2.length() != 16) {
			int howMany0 = 16 - temp2.length();
			String z = "";
			for(int i = 0; i < howMany0; i++) {
				z = z + "0";
			}
			temp2 = z + temp2;
		}
		String toHash = temp1 + temp2;
		md.reset();
		md.update(toHash.getBytes());
		byte[] commitment = md.digest();
		byte[] truncCommit = new byte[20 - x];
		for(int i = 0; i < 20 - x; i++) {
			truncCommit[i] = commitment[i];
		}
		
		//truncCommit is sent from Alice to Bob. Now bob will try to find out if Alice voted 1 och 0.
		
		int vBreakConcealing = 1;
		int kBreakConcealing = 0;    //Tries all k with v=1 first.
		boolean go = true;
		while(go) {
			String temp3 = Integer.toBinaryString(vBreakConcealing);
			String temp4 = Integer.toBinaryString(kBreakConcealing);
			
			//0 padding
			if(temp4.length() < 16) {
				int howMany0 = 16 - temp4.length();
				String zeros = "";
				for(int i = 0; i < howMany0; i++) {
					zeros = zeros + "0";
				}
				
				temp4 = zeros + temp4;
			}
			
			String toHashGuess = temp3 + temp4;
			md.reset();
			md.update(toHashGuess.getBytes());
			byte[] guess = md.digest();
			byte[] truncGuess = new byte[20 - x];
			for(int j = 0; j < 20 - x; j++) {
				truncGuess[j] = guess[j];
			}
			
			if(Arrays.equals(truncGuess, truncCommit)) {
				go = false;
				
				System.out.println("Bob says that alice commited v = " + vBreakConcealing + ", when v truly was " + vAliceC);
				
				
				System.out.println("DONE!");
				//System.out.println("Was supposed to match: (" + toHashGuess.length() + " bits) : " + toHash);
				System.out.println("Guess hash: ");
				printByteArray(guess);
				
				System.out.println("TruncGuess hash: ");
				printByteArray(truncGuess);
				
				System.out.println("Commit hash: ");
				printByteArray(commitment);
				return true;
				
			} else if(kBreakConcealing > 65534 && vBreakConcealing == 1) {
				vBreakConcealing = 0;    //If v=1 was not successful, we try with v=0
				kBreakConcealing = -1;
				
			} else if(kBreakConcealing > 65534) {
				go = false;
			}
			kBreakConcealing++;
			
		}
		return false;
	}
	private void printByteArray(byte[] array) {
		for(int i = 0; i < array.length; i++) {
			System.out.print(array[i] + " ");
		}
		System.out.print(" " + array.length + " bytes ");
		System.out.println("");
	}
	public static void main(String args[]) {
		//K is fixed at 16 bits, V is one bit
		//We need a byte array that is to be put in the hash function.
		//The first bit in that byte array is the V and the 16 subsequent bits represent the random number k
		CommitmentSimulation cs = new CommitmentSimulation(18);
	}
}

