package wci.frontend.pascal.tokens;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wci.frontend.*;
import wci.frontend.pascal.*;


import static wci.frontend.pascal.PascalTokenType.*;
import static wci.frontend.pascal.PascalErrorCode.*;
/**
 * <h1>PascalWordToken</h1>
 *
 * <p> Pascal word tokens (identifiers and reserved words).</p>
 *
 * <p>Copyright (c) 2009 by Ronald Mak</p>
 * <p>For instructional purposes only.  No warranties.</p>
 */
public class PascalWordToken extends PascalToken
{   
    private static String PATTERN = "^[a-zA-Z0-9]+";
    /**
     * Constructor.
     * @param source the source from where to fetch the token's characters.
     * @throws Exception if an error occurred.
     */
    public PascalWordToken(Source source)
        throws Exception
    {
        super(source);
    }

    /**
     * Extract a Pascal word token from the source.
     * @throws Exception if an error occurred.
     */
    protected void extract()
        throws Exception
    {
        Pattern pattern = Pattern.compile(PATTERN);

        StringBuilder textBuffer = new StringBuilder();
        int currentPos = source.getPosition();
        String line = source.getLine();
        line = line.substring(currentPos);
        
        
        // get a matcher object
        Matcher match = pattern.matcher(line); 
        
        if(match.find()){
            source.setPosition(currentPos+match.end());
            text = match.group();
        }else{
            nextChar();  // consume bad character
            type = ERROR;
            value = INVALID_CHARACTER; 
        }

        // // Get the word characters (letter or digit).  The scanner has
        // // already determined that the first character is a letter.
        // while (Character.isLetterOrDigit(currentChar)) {
        //     textBuffer.append(currentChar);
        //     currentChar = nextChar();  // consume character
        // }

        // text = textBuffer.toString();

        // Is it a reserved word or an identifier?
        type = (RESERVED_WORDS.contains(text.toLowerCase()))
               ? PascalTokenType.valueOf(text.toUpperCase())  // reserved word
               : IDENTIFIER;                                  // identifier
    }
}
