

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

import java.io.File;
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
			
			//======================== WHITESPACE ========================//
			
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
				if(inputChr=='\n' || inputChr=='\r')
				{
					chr=0;
					lin++;
					if(inputChr == '\r')
					{
						inputChr=r.read();
						chr++;
						if(inputChr!='\n')
							nextChr=inputChr;
					}
				}
			}while(Character.isWhitespace(inputChr));
			if(inputChr!=-1) { tok += Character.toString(inputChr); }
			
			//======================== COMMENTS ========================//
			
			// Consume all comments prior to a token
			if(inputChr=='-')
			{
				nextChr=r.read();
				chr++;
				if(nextChr=='-')
				{
					while(nextChr!='\n' && nextChr!='\r' && nextChr!=-1)
					{
						nextChr=r.read();
						chr++;
					}
					if(nextChr==-1)
						inputChr=-1;
					else
					{
						chr=0;
						lin++;
						int temp = nextChr;
						nextChr=r.read();
						chr++;
						if(temp=='\r' && nextChr=='\n')
						{
							chr++;
							nextChr=r.read();
						}
						return getNext();	// Some fancy recursion
						
					}
				}
			}
			
			
			//======================== EOF ========================//
			
			// If it is the ending to a file, then return EOF token
			if(inputChr==-1) { return new Token(EOF,"eof",chr,lin); }

			//======================== INTEGER LITERALS ========================//
			
			// Zero on its own is a token
			if(inputChr=='0') { return new Token(INTLIT,tok,chr++,lin); }
			
			// Zero on it's own should never get here.
			if(Character.isDigit(inputChr))
			{
				while(true)
				{
					nextChr=r.read();
					if(Character.isDigit(nextChr))
					{
						chr++;
						tok+=Character.toString(nextChr);
					}
					else 
					{ 
						// If the intlit is out of java bounds, throw an exception
						try{
							Integer.parseInt(tok);
						}
						catch(NumberFormatException e)
						{
							throw new LexicalException("Lexical Exception at line " +
			                           lin + " character " + chr + ". Invalid"
			                           		+ " integer : " + tok + "\nThis number is too large or small.");
						}
						return new Token(INTLIT,tok,chr++,lin); 
					}
				}
			}
			
			//======================== IDENTIFIERS / KEYWORDS ========================//
			
			// Check for an identifier start
			if(Character.isLetter(inputChr))
			{
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
			
			//======================== ARITHMETIC, BITWISE, CONDITONAL OPERATORS ========================//
			
			// The operators are pretty easy to handle
			if(inputChr=='+'){ return new Token(OP_PLUS,tok,chr++,lin); }
			if(inputChr=='-'){ return new Token(OP_MINUS,tok,chr++,lin); }
			if(inputChr=='*'){ return new Token(OP_TIMES,tok,chr++,lin); }
			if(inputChr=='/')
			{
				nextChr=r.read();
				if(nextChr=='/')
				{
					tok+=Character.toString(nextChr);
					chr++;
					nextChr=-2;
					return new Token(OP_DIVDIV,tok,chr++,lin);
				}
				else{ return new Token(OP_DIV,tok,chr++,lin); }
			}
			if(inputChr=='%'){ return new Token(OP_MOD,tok,chr++,lin); }
			if(inputChr=='^'){ return new Token(OP_POW,tok,chr++,lin); }
			if(inputChr=='#'){ return new Token(OP_HASH,tok,chr++,lin); }
			if(inputChr=='&'){ return new Token(BIT_AMP,tok,chr++,lin); }
			if(inputChr=='~')
			{
				nextChr=r.read();
				if(nextChr=='=')
				{
					tok+=Character.toString(nextChr);
					chr++;
					nextChr=-2;
					return new Token(REL_NOTEQ,tok,chr++,lin);
				}
				else{ return new Token(BIT_XOR,tok,chr++,lin); }
			}
			if(inputChr=='|'){ return new Token(BIT_OR,tok,chr++,lin); }
			if(inputChr=='<')
			{
				nextChr=r.read();
				if(nextChr=='<')
				{
					tok+=Character.toString(nextChr);
					chr++;
					nextChr=-2;
					return new Token(BIT_SHIFTL,tok,chr++,lin);
				}
				else if(nextChr=='=')
				{
					tok+=Character.toString(nextChr);
					chr++;
					nextChr=-2;
					return new Token(REL_LE,tok,chr++,lin);
				}
				else{ return new Token(REL_LT,tok,chr++,lin); }
			}
			if(inputChr=='>')
			{
				nextChr=r.read();
				if(nextChr=='>')
				{
					tok+=Character.toString(nextChr);
					chr++;
					nextChr=-2;
					return new Token(BIT_SHIFTR,tok,chr++,lin);
				}
				else if(nextChr=='=')
				{
					tok+=Character.toString(nextChr);
					chr++;
					nextChr=-2;
					return new Token(REL_GE,tok,chr++,lin);
				}
				else{ return new Token(REL_GT,tok,chr++,lin); }
			}
			if(inputChr=='=')
			{
				nextChr=r.read();
				if(nextChr=='=')
				{
					tok+=Character.toString(nextChr);
					chr++;
					nextChr=-2;
					return new Token(REL_EQEQ,tok,chr++,lin);
				}
				else{ return new Token(ASSIGN,tok,chr++,lin); }
			}
			
			//======================== OTHER SYMBOLS ========================//
			
			// These should work similarly to the operators
			if(inputChr=='('){ return new Token(LPAREN,tok,chr++,lin); }
			if(inputChr==')'){ return new Token(RPAREN,tok,chr++,lin); }
			if(inputChr=='{'){ return new Token(LCURLY,tok,chr++,lin); }
			if(inputChr=='}'){ return new Token(RCURLY,tok,chr++,lin); }
			if(inputChr=='['){ return new Token(LSQUARE,tok,chr++,lin); }
			if(inputChr==']'){ return new Token(RSQUARE,tok,chr++,lin); }
			if(inputChr==':')
			{
				nextChr=r.read();
				if(nextChr==':')
				{
					tok+=Character.toString(nextChr);
					chr++;
					nextChr=-2;
					return new Token(COLONCOLON,tok,chr++,lin);
				}
				else{ return new Token(COLON,tok,chr++,lin); }
			}
			if(inputChr==';'){ return new Token(SEMI,tok,chr++,lin); }
			if(inputChr==','){ return new Token(COMMA,tok,chr++,lin); }
			if(inputChr=='.')
			{
				nextChr=r.read();
				if(nextChr=='.')
				{
					tok+=Character.toString(nextChr);
					chr++;
					nextChr=r.read();
					if(nextChr=='.')
					{
						tok+=Character.toString(nextChr);
						chr++;
						nextChr=-2;
						return new Token(DOTDOTDOT,tok,chr++,lin);
					}
					else{ return new Token(DOTDOT,tok,chr++,lin); }
				}
				else{ return new Token(DOT,tok,chr++,lin); }
			}
			
			System.out.println(new File(".").getCanonicalPath());
			
			throw new LexicalException("Lexical Exception at line " +
			                           lin + " character " + chr + ". Invalid "
			                           		+ "token: " + Character.toString(inputChr));
		}
}
