/* Generated By:JavaCC: Do not edit this line. Token.java Version 3.0 */
/*
 Copyright (c) 1998-2005 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 @ProposedRating Yellow (nsmyth)
 @AcceptedRating Yellow (yuhong)

 Created : May 1998

 */
package ptolemy.data.expr;

/**
 * Describes the input token stream.
 */
public class Token {
    /**
     * An integer that describes the kind of this token.  This numbering
     * system is determined by JavaCCParser, and a table of these numbers is
     * stored in the file ...Constants.java.
     */
    public int kind;

    /**
     * beginLine and beginColumn describe the position of the first character
     * of this token; endLine and endColumn describe the position of the
     * last character of this token.
     */
    public int beginLine;

    /**
     * beginLine and beginColumn describe the position of the first character
     * of this token; endLine and endColumn describe the position of the
     * last character of this token.
     */
    public int beginColumn;

    /**
     * beginLine and beginColumn describe the position of the first character
     * of this token; endLine and endColumn describe the position of the
     * last character of this token.
     */
    public int endLine;

    /**
     * beginLine and beginColumn describe the position of the first character
     * of this token; endLine and endColumn describe the position of the
     * last character of this token.
     */
    public int endColumn;

    /**
     * The string image of the token.
     */
    public String image;

    /**
     * A reference to the next regular (non-special) token from the input
     * stream.  If this is the last token from the input stream, or if the
     * token manager has not read tokens beyond this one, this field is
     * set to null.  This is true only if this token is also a regular
     * token.  Otherwise, see below for a description of the contents of
     * this field.
     */
    public Token next;

    /**
     * This field is used to access special tokens that occur prior to this
     * token, but after the immediately preceding regular (non-special) token.
     * If there are no such special tokens, this field is set to null.
     * When there are more than one such special token, this field refers
     * to the last of these special tokens, which in turn refers to the next
     * previous special token through its specialToken field, and so on
     * until the first special token (whose specialToken field is null).
     * The next fields of special tokens refer to other special tokens that
     * immediately follow it (without an intervening regular token).  If there
     * is no such token, this field is null.
     */
    public Token specialToken;

    /**
     * Returns the image.
     */
    public String toString() {
        return image;
    }

    /**
     * Return a new Token object.  In this base class, a new Token is
     * returned.  Subclasses may return subclass objects based on the
     * value of ofKind.
     * Subclasses add the cases to the switch for all those special cases.
     * For example, if you have a subclass of Token called IDToken that
     * you want to create if ofKind is ID, simply add something like :
     * <pre>
     *    case MyParserConstants.ID : return new IDToken();
     * </pre>
     * to the following switch statement. Then you can cast matchedToken
     * variable to the appropriate type and use it in your lexical actions.
     *
     * @param ofKind the type of token to return.  Ignored in this base
     * class, a Token is always returned.  Subclasses may use this argument
     * to determine what type of token to return.
     * @return The returned token.
     */
    public static final Token newToken(int ofKind) {
        switch (ofKind) {
        default:
            return new Token();
        }
    }
}
