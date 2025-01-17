/********************************************************************************
 * CruiseControl, a Continuous Integration Toolkit
 * Copyright (c) 2001-2003, ThoughtWorks, Inc.
 * 651 W Washington Ave. Suite 600
 * Chicago, IL 60661 USA
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *     + Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     + Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 *     + Neither the name of ThoughtWorks, Inc., CruiseControl, nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior
 *       written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ********************************************************************************/
package com.isencia.util.commandline ;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Commandline objects help handling command lines specifying processes to
 * execute.
 *
 * The class can be used to define a command line as nested elements or as a
 * helper to define a command line by an application.
 * <p>
 * <code>
 * &lt;someelement&gt;<br>
 * &nbsp;&nbsp;&lt;acommandline executable="/executable/to/run"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;argument value="argument 1" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;argument line="argument_1 argument_2 argument_3" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;argument value="argument 4" /&gt;<br>
 * &nbsp;&nbsp;&lt;/acommandline&gt;<br>
 * &lt;/someelement&gt;<br>
 * </code>
 * The element <code>someelement</code> must provide a method
 * <code>createAcommandline</code> which returns an instance of this class.
 *
 * @author thomas.haas@softwired-inc.com
 * @author <a href="mailto:stefan.bodewig@epost.de">Stefan Bodewig</a>
 */
public class Commandline implements Cloneable {

    private static final Logger LOG = LoggerFactory.getLogger(Commandline.class);

    private Vector arguments = new Vector();
    private String executable = null;
    private File workingDir = null;

    public Commandline(String toProcess) {
        super();
        String[] tmp = new String[0];
        try {
            tmp = translateCommandline(toProcess);
        } catch (CommandLineException e) {
            LOG.error("Error translating Commandline.", e);
        }
        if (tmp != null && tmp.length > 0) {
            setExecutable(tmp[0]);
            for (int i = 1; i < tmp.length; i++) {
                createArgument().setValue(tmp[i]);
            }
        }
    }

    public Commandline() {
        super();
    }
    
    protected File getWorkingDir() {
      return workingDir;
    }

    /**
     * Used for nested xml command line definitions.
     */
    public static class Argument {

        private String[] parts;

        /**
         * Sets a single commandline argument.
         *
         * @param value a single commandline argument.
         */
        public void setValue(String value) {
            parts = new String[] { value };
        }

        /**
         * Line to split into several commandline arguments.
         *
         * @param line line to split into several commandline arguments
         */
        public void setLine(String line) {
            if (line == null) {
                return;
            }
            try {
                parts = translateCommandline(line);
            } catch (CommandLineException e) {
                LOG.error("Error translating Commandline.", e);
            }
        }

        /**
         * Sets a single commandline argument to the absolute filename
         * of the given file.
         *
         * @param value a single commandline argument.
         */
        public void setFile(File value) {
            parts = new String[] { value.getAbsolutePath()};
        }

        /**
         * Returns the parts this Argument consists of.
         */
        public String[] getParts() {
            return parts;
        }
    }

    /**
     * Class to keep track of the position of an Argument.
     */
    // <p>This class is there to support the srcfile and targetfile
    // elements of &lt;execon&gt; and &lt;transform&gt; - don't know
    // whether there might be additional use cases.</p> --SB
    public class Marker {

        private int position;
        private int realPos = -1;

        Marker(int position) {
            this.position = position;
        }

        /**
         * Return the number of arguments that preceeded this marker.
         *
         * <p>The name of the executable - if set - is counted as the
         * very first argument.</p>
         */
        public int getPosition() {
            if (realPos == -1) {
                realPos = (executable == null ? 0 : 1);
                for (int i = 0; i < position; i++) {
                    Argument arg = (Argument) arguments.elementAt(i);
                    realPos += arg.getParts().length;
                }
            }
            return realPos;
        }
    }

    /**
     * Creates an argument object.
     *
     * <p>Each commandline object has at most one instance of the
     * argument class.  This method calls
     * <code>this.createArgument(false)</code>.</p>
     *
     * @see #createArgument(boolean)
     * @return the argument object.
     */
    public Argument createArgument() {
        return this.createArgument(false);
    }

    /**
     * Creates an argument object and adds it to our list of args.
     *
     * <p>Each commandline object has at most one instance of the
     * argument class.</p>
     *
     * @param insertAtStart if true, the argument is inserted at the
     * beginning of the list of args, otherwise it is appended.
     */
    public Argument createArgument(boolean insertAtStart) {
        Argument argument = new Argument();
        if (insertAtStart) {
            arguments.insertElementAt(argument, 0);
        } else {
            arguments.addElement(argument);
        }
        return argument;
    }

    /**
     * Sets the executable to run.
     */
    public void setExecutable(String executable) {
        if (executable == null || executable.length() == 0) {
            return;
        }
        this.executable =
            executable.replace('/', File.separatorChar).replace('\\', File.separatorChar);
    }

    public String getExecutable() {
        return executable;
    }

    public void addArguments(String[] line) {
        for (int i = 0; i < line.length; i++) {
            createArgument().setValue(line[i]);
        }
    }

    /**
     * Returns the executable and all defined arguments.
     */
    public String[] getCommandline() {
        final String[] args = getArguments();
        if (executable == null) {
            return args;
        }
        final String[] result = new String[args.length + 1];
        result[0] = executable;
        System.arraycopy(args, 0, result, 1, args.length);
        return result;
    }

    /**
     * Returns all arguments defined by <code>addLine</code>,
     * <code>addValue</code> or the argument object.
     */
    public String[] getArguments() {
        Vector result = new Vector(arguments.size() * 2);
        for (int i = 0; i < arguments.size(); i++) {
            Argument arg = (Argument) arguments.elementAt(i);
            String[] s = arg.getParts();
            if (s != null) {
                for (int j = 0; j < s.length; j++) {
                    result.addElement(s[j]);
                }
            }
        }

        String[] res = new String[result.size()];
        result.copyInto(res);
        return res;
    }

    public String toString() {
        return toString(getCommandline());
    }

    /**
     * Put quotes around the given String if necessary.
     *
     * <p>If the argument doesn't include spaces or quotes, return it
     * as is. If it contains double quotes, use single quotes - else
     * surround the argument by double quotes.</p>
     *
     * @exception CommandLineException if the argument contains both, single
     *                           and double quotes.
     */
    public static String quoteArgument(String argument) throws CommandLineException {
        if (argument.indexOf("\"") > -1) {
            if (argument.indexOf("\'") > -1) {
                throw new CommandLineException("Can't handle single and double quotes in same argument");
            } else {
                return '\'' + argument + '\'';
            }
        } else if (argument.indexOf("\'") > -1 || argument.indexOf(" ") > -1) {
            return '\"' + argument + '\"';
        } else {
            return argument;
        }
    }

    public static String toString(String[] line) {
        // empty path return empty string
        if (line == null || line.length == 0) {
            return "";
        }

        // path containing one or more elements
        final StringBuffer result = new StringBuffer();
        for (int i = 0; i < line.length; i++) {
            if (i > 0) {
                result.append(' ');
            }
            try {
                result.append(quoteArgument(line[i]));
            } catch (CommandLineException e) {
                LOG.error("Error quoting argument.", e);
            }
        }
        return result.toString();
    }

    public static String[] translateCommandline(String toProcess) throws CommandLineException {
        if (toProcess == null || toProcess.length() == 0) {
            return new String[0];
        }

        // parse with a simple finite state machine

        final int normal = 0;
        final int inQuote = 1;
        final int inDoubleQuote = 2;
        int state = normal;
        StringTokenizer tok = new StringTokenizer(toProcess, "\"\' ", true);
        Vector v = new Vector();
        StringBuffer current = new StringBuffer();

        while (tok.hasMoreTokens()) {
            String nextTok = tok.nextToken();
            switch (state) {
                case inQuote :
                    if ("\'".equals(nextTok)) {
                        state = normal;
                    } else {
                        current.append(nextTok);
                    }
                    break;
                case inDoubleQuote :
                    if ("\"".equals(nextTok)) {
                        state = normal;
                    } else {
                        current.append(nextTok);
                    }
                    break;
                default :
                    if ("\'".equals(nextTok)) {
                        state = inQuote;
                    } else if ("\"".equals(nextTok)) {
                        state = inDoubleQuote;
                    } else if (" ".equals(nextTok)) {
                        if (current.length() != 0) {
                            v.addElement(current.toString());
                            current.setLength(0);
                        }
                    } else {
                        current.append(nextTok);
                    }
                    break;
            }
        }

        if (current.length() != 0) {
            v.addElement(current.toString());
        }

        if (state == inQuote || state == inDoubleQuote) {
            throw new CommandLineException("unbalanced quotes in " + toProcess);
        }

        String[] args = new String[v.size()];
        v.copyInto(args);
        return args;
    }

    public int size() {
        return getCommandline().length;
    }

    public Object clone() throws CloneNotSupportedException {
        super.clone();
        
        Commandline c = new Commandline();
        c.setExecutable(executable);
        c.addArguments(getArguments());
        return c;
    }

    /**
     * Clear out the whole command line.  */
    public void clear() {
        executable = null;
        arguments.removeAllElements();
    }

    /**
     * Clear out the arguments but leave the executable in place for another operation.
     */
    public void clearArgs() {
        arguments.removeAllElements();
    }

    /**
     * Return a marker.
     *
     * <p>This marker can be used to locate a position on the
     * commandline - to insert something for example - when all
     * parameters have been set.</p>
     */
    public Marker createMarker() {
        return new Marker(arguments.size());
    }

    /**
     * Sets execution directory.
     */
    public void setWorkingDirectory(String path) throws CommandLineException {
        if (path != null) {
            File dir = new File(path);
            checkWorkingDir(dir);
            workingDir = dir;
        } else {
            workingDir = null;
        }
    }
   
    /** 
     * Sets exeuction directory
     */ 
    public void setWorkingDir(File workingDir) throws CommandLineException {
        checkWorkingDir(workingDir);
        this.workingDir = workingDir;
    }

    // throws an exception if the specified working directory is non null 
    // and not a valid working directory
    private void checkWorkingDir(File dir) throws CommandLineException {
        if (dir != null) {
            if (!dir.exists()) {
                throw new CommandLineException(
                    "Working directory \"" + dir.getAbsolutePath() + "\" does not exist!");
            } else if (!dir.isDirectory()) {
                throw new CommandLineException(
                    "Path \"" + dir.getAbsolutePath() + "\" does not specify a directory.");
            }
        }
    }

    public File getWorkingDirectory() {
        return workingDir;
    }

    /**
     * Executes the command.
     */
    public Process execute() throws IOException {
        Process process;

        if (workingDir == null) {
            LOG.debug("Executing \"" + this + "\"");
            process = Runtime.getRuntime().exec(getCommandline());
        } else {
            LOG.debug(
                "Executing \""
                    + this
                    + "\" in directory "
                    + workingDir.getAbsolutePath());
            process = Runtime.getRuntime().exec(getCommandline(), null, workingDir);
        }

        return process;
    }

}
