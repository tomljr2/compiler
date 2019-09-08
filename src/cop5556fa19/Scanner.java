

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
		this.nextChr=-2;
	}


	public Token getNext() throws Exception {
			int inputChr;
			String tok="";
			
			
			
			// Consume all whitespace prior to a character
			do {
				// If there was a character that needed to be picked up from the last
				// token scan, then it needs we  shouldn't read a new one yet
				if(nextChr==-2){ inputChr = r.read(); chr++; }
				else
				{
					inputChr = nextChr;
					nextChr=-2;
				}
				if(inputChr=='\n')
				{
					chr=0;
					lin++;
				}
			}while(Character.isWhitespace(inputChr));
			
			// If it is the ending to a file, then return EOF token
			if(inputChr==-1) { return new Token(EOF,"eof",chr,lin); }

			// Zero on its own is a token
			if(inputChr=='0') { return new Token(INTLIT,Character.toString(inputChr),chr++,lin); }
			
			// Zero on it's own should never get here.
			if(Character.isDigit(inputChr))
			{
				tok += Character.toString(inputChr);
				while(true)
				{
					nextChr=r.read();
					if(Character.isDigit(nextChr))
					{
						chr++;
						tok+=Character.toString(nextChr);
					}
					else { return new Token(INTLIT,tok,chr++,lin); }
				}
			}
			
			// Check for an identifier start
			if(Character.isLetter(inputChr))
			{
				tok += Character.toString(inputChr);
				while(true)
				{
					nextChr=r.read();
					if(Character.isLetterOrDigit(nextChr) || nextChr =='_' || nextChr=='$')
					{
						chr++;
						tok+=Character.toString(nextChr);
					}
					else 
					{
						// Check if an identifier is a keyword, and return the correct
						// token
						switch(tok)
						{
							case "and": return new Token(KW_and,tok,chr++,lin);
							case "break": return new Token(KW_break,tok,chr++,lin);
							case "do": return new Token(KW_do,tok,chr++,lin);
							case "else": return new Token(KW_else,tok,chr++,lin);
							case "elseif": return new Token(KW_elseif,tok,chr++,lin);
							case "end": return new Token(KW_end,tok,chr++,lin);
							case "false": return new Token(KW_false,tok,chr++,lin);
							case "for": return new Token(KW_for,tok,chr++,lin);
							case "function": return new Token(KW_function,tok,chr++,lin);
							case "goto": return new Token(KW_goto,tok,chr++,lin);
							case "if": return new Token(KW_if,tok,chr++,lin);
							case "in": return new Token(KW_in,tok,chr++,lin);
							case "local": return new Token(KW_local,tok,chr++,lin);
							case "nil": return new Token(KW_nil,tok,chr++,lin);
							case "not": return new Token(KW_not,tok,chr++,lin);
							case "or": return new Token(KW_or,tok,chr++,lin);
							case "repeat": return new Token(KW_repeat,tok,chr++,lin);
							case "return": return new Token(KW_return,tok,chr++,lin);
							case "then": return new Token(KW_then,tok,chr++,lin);
							case "true": return new Token(KW_true,tok,chr++,lin);
							case "until": return new Token(KW_until,tok,chr++,lin);
							case "while": return new Token(KW_while,tok,chr++,lin);
							default: return new Token(NAME,tok,chr++,lin);
						}
					}
				}
			}

			throw new LexicalException("Useful error message");
		}
}
