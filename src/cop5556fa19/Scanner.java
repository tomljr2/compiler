

/* *
 * Developed  for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2019.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2019 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites or repositories,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2019
 */
package cop5556fa19;


import static cop5556fa19.Token.Kind.*;


import java.io.IOException;
import java.io.Reader;

public class Scanner {
	
	Reader r;
	int chr;
	int lin;
	int nextChr;


	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {	
		public LexicalException(String arg0) {
			super(arg0);
		}
	}
	
	public Scanner(Reader r) throws IOException {
		this.r = r;
		this.chr = 0;
		this.lin = 0;
	}


	public Token getNext() throws Exception {
			int inputChr;
			String tok="";
			
			// Consume all whitespace prior to a character
			do {
				inputChr = r.read();
				chr++;
				if(inputChr=='\n')
				{
					chr=0;
					lin++;
				}
			}while(Character.isWhitespace(inputChr));
			
			// If it is the ending to a file, then return EOF token
			if(inputChr==-1) { return new Token(EOF,"eof",chr,lin); }

			// Zero on its own is a token
			if(inputChr=='0') { return new Token(INTLIT,String.valueOf(inputChr),chr++,lin); }

			throw new LexicalException("Useful error message");
		}
}
