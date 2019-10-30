package wci.frontend.pascal.tokens;

import wci.frontend.*;
import wci.frontend.pascal.*;

import static wci.frontend.pascal.PascalTokenType.*;
import static wci.frontend.pascal.PascalErrorCode.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <h1>PascalNumberToken</h1>
 *
 * <p>Pascal number tokens (integer and real).</p>
 *
 * <p>Copyright (c) 2009 by Ronald Mak</p>
 * <p>For instructional purposes only.  No warranties.</p>
 */
public class PascalNumberToken extends PascalToken
{
    private static final int MAX_EXPONENT = 37;
    private static String REAL_PATTERN = "^(?<wholeDigits>[0-9]+)([.](?<fractionDigits>[0-9]+))?((E|e)(?<exponentSign>[+]|[-])?(?<exponentDigits>[0-9]+))?";
    
    /**
     * Constructor.
     * @param source the source from where to fetch the token's characters.
     * @throws Exception if an error occurred.
     */
    public PascalNumberToken(Source source)
        throws Exception
    {
        super(source);
    }

    /**
     * Extract a Pascal number token from the source.
     * @throws Exception if an error occurred.
     */
    protected void extract()
        throws Exception
    {
        StringBuilder textBuffer = new StringBuilder();  // token's characters
        System.out.println(textBuffer.toString());
        extractNumber(textBuffer);
        text = textBuffer.toString();
    }

    /**
     * Extract a Pascal number token from the source.
     * @param textBuffer the buffer to append the token's characters.
     * @throws Exception if an error occurred.
     */
    protected void extractNumber(StringBuilder textBuffer)
        throws Exception
    {
        String wholeDigits      = null;     // digits before the decimal point
        String fractionDigits   = null;  // digits after the decimal point
        String exponentDigits   = null;  // exponent digits
        char exponentSign       = '+';       // exponent sign '+' or '-'
        char currentChar;              // current character

        type = INTEGER;  // assume INTEGER token type for now

        Pattern pattern = Pattern.compile(REAL_PATTERN);

        int currentPos = source.getPosition();
        String line = source.getLine();
        line = line.substring(currentPos);
        
        
        // get a matcher object
        Matcher match = pattern.matcher(line); 
        
        if(match.find()){
            
            wholeDigits      = match.group("wholeDigits");     // digits before the decimal point
            fractionDigits   = match.group("fractionDigits");  // digits after the decimal point
            exponentDigits   = match.group("exponentDigits");  // exponent digits
            if(match.group("exponentSign") != null){
                exponentSign = match.group("exponentSign").charAt(0);       // exponent sign '+' or '-'
            }

            if(fractionDigits != null |exponentDigits != null ){
                type = REAL;
                source.setPosition(currentPos+match.end());
                textBuffer.append(match.group());
            
            }else if(wholeDigits != null){
                type = INTEGER;    
                source.setPosition(currentPos+match.end());
                if(peekChar()=='.'|peekChar()=='E'|peekChar()=='e'){
                    type = ERROR;
                    value = INVALID_NUMBER;
                }        
                textBuffer.append(match.group());
            }
            
            

        }
        else{
            type = ERROR;
            value = INVALID_NUMBER; 
        }

        // Compute the value of an integer number token.
        if (type == INTEGER) {
            int integerValue = computeIntegerValue(wholeDigits);

            if (type != ERROR) {
                value = new Integer(integerValue);
            }
        }

        // Compute the value of a real number token.
        else if (type == REAL) {
            float floatValue = computeFloatValue(wholeDigits, fractionDigits,
                                                 exponentDigits, exponentSign);

            if (type != ERROR) {
                value = new Float(floatValue);
            }
        }
    }


    /**
     * Compute and return the integer value of a string of digits.
     * Check for overflow.
     * @param digits the string of digits.
     * @return the integer value.
     */
    private int computeIntegerValue(String digits)
    {
        // Return 0 if no digits.
        if (digits == null) {
            return 0;
        }

        int integerValue = 0;
        int prevValue = -1;    // overflow occurred if prevValue > integerValue
        int index = 0;

        // Loop over the digits to compute the integer value
        // as long as there is no overflow.
        while ((index < digits.length()) && (integerValue >= prevValue)) {
            prevValue = integerValue;
            integerValue = 10*integerValue +
                           Character.getNumericValue(digits.charAt(index++));
        }

        // No overflow:  Return the integer value.
        if (integerValue >= prevValue) {
            return integerValue;
        }

        // Overflow:  Set the integer out of range error.
        else {
            type = ERROR;
            value = RANGE_INTEGER;
            return 0;
        }
    }

    /**
     * Compute and return the float value of a real number.
     * @param wholeDigits the string of digits before the decimal point.
     * @param fractionDigits the string of digits after the decimal point.
     * @param exponentDigits the string of exponent digits.
     * @param exponentSign the exponent sign.
     * @return the float value.
     */
    private float computeFloatValue(String wholeDigits, String fractionDigits,
                                    String exponentDigits, char exponentSign)
    {
        double floatValue = 0.0;
        int exponentValue = computeIntegerValue(exponentDigits);
        String digits = wholeDigits;  // whole and fraction digits

        // Negate the exponent if the exponent sign is '-'.
        if (exponentSign == '-') {
            exponentValue = -exponentValue;
        }

        // If there are any fraction digits, adjust the exponent value
        // and append the fraction digits.
        if (fractionDigits != null) {
            exponentValue -= fractionDigits.length();
            digits += fractionDigits;
        }

        // Check for a real number out of range error.
        if (Math.abs(exponentValue + wholeDigits.length()) > MAX_EXPONENT) {
            type = ERROR;
            value = RANGE_REAL;
            return 0.0f;
        }

        // Loop over the digits to compute the float value.
        int index = 0;
        while (index < digits.length()) {
            floatValue = 10*floatValue +
                         Character.getNumericValue(digits.charAt(index++));
        }

        // Adjust the float value based on the exponent value.
        if (exponentValue != 0) {
            floatValue *= Math.pow(10, exponentValue);
        }

        return (float) floatValue;
    }
}
