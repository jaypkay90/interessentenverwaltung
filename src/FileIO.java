

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
//import java.util.Locale;
import java.util.Scanner;

public class FileIO {
	private static BufferedWriter bw;
	private static Scanner scan;
	
	private static Locale defaultLocale;
	private static Locale locale;
	
	static {
		bw = null;
		scan = null;
		defaultLocale = Locale.getDefault();
		locale = Locale.getDefault();
	}
	
	public static void openWriter(String file) {
		openWriter(new File(file), false);
	}

	public static void openWriter(File file) {
		openWriter(file, false);
	}

	public static void openWriter(String file, boolean append) {
		openWriter(new File(file), append);
	}

	public static void openWriter(File file, boolean append) {
		try {
			bw = new BufferedWriter(new FileWriter(file, append));
		} catch (IOException e) {
//			e.printStackTrace();
			System.err.println(e.getMessage());
		}
	}

	public static void print(String s) {
		try {
			bw.write(s);
			bw.flush();
		} catch (IOException e) {
			// e.printStackTrace();
			System.err.println(e.getMessage());
		}
	}

	public static void print(int i) {
		print("" + i);
	}

	public static void print(double d) {
		print("" + d);
	}

	public static void print(char c) {
		print("" + c);
	}

	public static void print(boolean b) {
		print("" + b);
	}

	public static void println(String s) {
		try {
			bw.write(s);
			bw.newLine();
			bw.flush();
		} catch (IOException e) {
			// e.printStackTrace();
			System.err.println(e.getMessage());
		}
	}

	public static void println() {
		println("");
	}

	public static void println(int i) {
		println("" + i);
	}

	public static void println(double d) {
		println("" + d);
	}

	public static void println(char c) {
		println("" + c);
	}

	public static void println(boolean b) {
		println("" + b);
	}

	public static void printf(String format, Object... args) {
		String s = String.format(locale, format, args);
		print(s);
	}

	public static void closeWriter() {
		try {
			bw.close();
			bw = null;
		} catch (IOException e) {
			// e.printStackTrace();
			System.err.println(e.getMessage());
		}
	}

	public static void openReader(String s) {
		openReader(new File(s));
	}

	public static void openReader(File f) {
		try {
			scan = new Scanner(f);
			scan.useLocale(locale);
		} catch (FileNotFoundException e) {
			// e.printStackTrace();
			System.err.println(e.getMessage());
		}
	}

	public static boolean hasNext() {
		return scan.hasNext();
	}

	public static String next() {
		return scan.next();
	}

	public static String nextLine() {
		return scan.nextLine();
	}

	public static int nextInt() {
		int erg = scan.nextInt();
		scan.nextLine();
		return erg;
	}

	public static double nextDouble() {
		double erg = scan.nextDouble();
		scan.nextLine();
		return erg;
	}

	public static boolean nextBoolean() {
		boolean erg = scan.nextBoolean();
		scan.nextLine();
		return erg;
	}

	public static void closeReader() {
		try {
			scan.close();
			scan = null;
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void setLocale(Locale l) {
		locale = l;
		
		if(scan != null) {
			scan.useLocale(locale);
		}
	}
	
	public static void setDefaultLocale() {
		setLocale(defaultLocale);
	}
}
