/* Generated By:JJTree&JavaCC: Do not edit this line. PtParserConstants.java */
/*
 Copyright (c) 1998-2008 The Regents of the University of California.
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

Created : May 1998
*/

package ptolemy.data.expr;

public interface PtParserConstants {

    int EOF = 0;
    int SINGLE_LINE_COMMENT = 3;
    int MULTI_LINE_COMMENT = 4;
    int PLUS = 10;
    int MINUS = 11;
    int MULTIPLY = 12;
    int DIVIDE = 13;
    int MODULO = 14;
    int POWER = 15;
    int OPENPAREN = 16;
    int CLOSEPAREN = 17;
    int OPENBRACE = 18;
    int CLOSEBRACE = 19;
    int OPENBRACKET = 20;
    int CLOSEBRACKET = 21;
    int COMMA = 22;
    int PERIOD = 23;
    int COLON = 24;
    int QUESTION = 25;
    int OPENUNION = 26;
    int CLOSEUNION = 27;
    int GT = 28;
    int LT = 29;
    int GTE = 30;
    int LTE = 31;
    int NOTEQUALS = 32;
    int EQUALS = 33;
    int COND_AND = 34;
    int COND_OR = 35;
    int BOOL_NOT = 36;
    int BITWISE_NOT = 37;
    int AND = 38;
    int OR = 39;
    int XOR = 40;
    int SHL = 41;
    int SHR = 42;
    int LSHR = 43;
    int INTEGER = 44;
    int INTEGER_FORMAT_SPEC = 45;
    int DECIMAL_LITERAL = 46;
    int HEX_LITERAL = 47;
    int OCTAL_LITERAL = 48;
    int EXPONENT = 49;
    int DOUBLE = 50;
    int COMPLEX = 51;
    int BOOLEAN = 52;
    int FUNCTION = 53;
    int ID = 54;
    int LETTER = 55;
    int STRING = 56;
    int SETEQUALS = 57;
    int SEPARATOR = 58;
    int SMSTRING = 59;
    int SMDOLLAR = 60;
    int SMDOLLARBRACE = 61;
    int SMDOLLARPAREN = 62;
    int SMID = 63;
    int SMLETTER = 64;
    int SMIDBRACE = 65;
    int SMBRACE = 66;
    int SMIDPAREN = 67;
    int SMPAREN = 68;
    int ERROR = 69;

    int DEFAULT = 0;
    int SingleLineCommentMode = 1;
    int MultiLineCommentMode = 2;
    int StringModeIDBrace = 3;
    int StringModeIDParen = 4;
    int StringMode = 5;
    int StringModeIDNone = 6;

    String[] tokenImage = { "<EOF>", "\"//\"", "\"/*\"",
            "<SINGLE_LINE_COMMENT>", "\"*/\"", "<token of kind 5>", "\" \"",
            "\"\\r\"", "\"\\t\"", "\"\\n\"", "\"+\"", "\"-\"", "\"*\"",
            "\"/\"", "\"%\"", "\"^\"", "\"(\"", "\")\"", "\"{\"", "\"}\"",
            "\"[\"", "\"]\"", "\",\"", "\".\"", "\":\"", "\"?\"", "\"{|\"",
            "\"|}\"", "\">\"", "\"<\"", "\">=\"", "\"<=\"", "\"!=\"", "\"==\"",
            "\"&&\"", "\"||\"", "\"!\"", "\"~\"", "\"&\"", "\"|\"", "\"#\"",
            "\"<<\"", "\">>\"", "\">>>\"", "<INTEGER>",
            "<INTEGER_FORMAT_SPEC>", "<DECIMAL_LITERAL>", "<HEX_LITERAL>",
            "<OCTAL_LITERAL>", "<EXPONENT>", "<DOUBLE>", "<COMPLEX>",
            "<BOOLEAN>", "\"function\"", "<ID>", "<LETTER>", "<STRING>",
            "\"=\"", "\";\"", "<SMSTRING>", "\"$\"", "\"${\"", "\"$(\"",
            "<SMID>", "<SMLETTER>", "<SMIDBRACE>", "<SMBRACE>", "<SMIDPAREN>",
            "<SMPAREN>", "<ERROR>", };

}
