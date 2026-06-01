package me.shouheng.deproguard.retrace;

import proguard.classfile.util.ClassUtil;
import proguard.obfuscate.MappingProcessor;
import proguard.obfuscate.MappingReader;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:shouheng2015@gmail.com">Shouheng.W</a>
 * @version 1.0
 * @date 2023/2/3 0:16
 */

public class MyReTrace implements MappingProcessor {
    private static final String REGEX_OPTION = "-regex";
    private static final String VERBOSE_OPTION = "-verbose";
    private static final String STACK_TRACE_EXPRESSION = "(?:\\s*%c:.*)|(?:\\s*at\\s+%c.%m\\s*\\(.*?(?::%l)?\\)\\s*)";
    private static final String REGEX_CLASS = "\\b(?:[A-Za-z0-9_$]+\\.)*[A-Za-z0-9_$]+\\b";
    private static final String REGEX_CLASS_SLASH = "\\b(?:[A-Za-z0-9_$]+/)*[A-Za-z0-9_$]+\\b";
    private static final String REGEX_LINE_NUMBER = "\\b[0-9]+\\b";
    private static final String REGEX_TYPE = "\\b(?:[A-Za-z0-9_$]+\\.)*[A-Za-z0-9_$]+\\b(?:\\[\\])*";
    private static final String REGEX_MEMBER = "<?\\b[A-Za-z0-9_$]+\\b>?";
    private static final String REGEX_ARGUMENTS = "(?:\\b(?:[A-Za-z0-9_$]+\\.)*[A-Za-z0-9_$]+\\b(?:\\[\\])*(?:\\s*,\\s*\\b(?:[A-Za-z0-9_$]+\\.)*[A-Za-z0-9_$]+\\b(?:\\[\\])*)*)?";

    private final String regularExpression;
    private final String regexClass;
    private final String regexClassSlash;
    private final String regexType;
    private final String regexMember;
    private final String regexArguments;
    private final boolean verbose;
    private final File mappingFile;
    private final File stackTraceFile;
    private final File dictFile;

    private final Map<Character, String> charToNormalMap = new HashMap<>();
    private Map classMap;
    private Map classFieldMap;
    private Map classMethodMap;

    private static int sBufferSize = 8192;

    public MyReTrace(
            String regularExpression,
            boolean verbose,
            File mappingFile,
            File stackTraceFile,
            File dictFile
    ) {
        this.classMap = new HashMap();
        this.classFieldMap = new HashMap();
        this.classMethodMap = new HashMap();
        this.verbose = verbose;
        this.mappingFile = mappingFile;
        this.stackTraceFile = stackTraceFile;
        this.dictFile = dictFile;
        this.initCharMap();
        this.regularExpression = regularExpression;
        this.regexClass = REGEX_CLASS;
        this.regexClassSlash = REGEX_CLASS_SLASH;
        this.regexType = REGEX_TYPE;
        this.regexMember = REGEX_MEMBER;
        this.regexArguments = REGEX_ARGUMENTS;
    }

    private void initCharMap() {
        String dictText = readTextFromDictFile();
        dictText = dictText.trim();
        if (dictText.isEmpty()) return;
        int index = 0;
        for (int i = 0; i < dictText.length(); i++) {
            char c = dictText.charAt(i);
            if (!isNormal(c) && !charToNormalMap.containsKey(c)) {
                charToNormalMap.put(c, "_v" + (index++) + "_");
            }
        }
    }

    private boolean isNormal(char c) {
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || (c >= '0' && c <= '9')
                || c == '_'
                || c == '$'
                || c == '\n';
    }

    private String transform(String text) {
        if (text == null || charToNormalMap.isEmpty()) return text;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (charToNormalMap.containsKey(c)) {
                sb.append(charToNormalMap.get(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private File transformMappingFile(File mappingFile) throws IOException {
        String content = readTextFromFile(mappingFile);
        String transformed = transform(content);
        File tempFile = File.createTempFile("mapping", ".txt");
        tempFile.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(transformed.getBytes());
        }
        return tempFile;
    }

    private String readTextFromFile(File file) {
        if (file != null && file.exists()) {
            try (FileInputStream fis = new FileInputStream(file);
                 ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                byte[] b = new byte[sBufferSize];
                int len;
                while ((len = fis.read(b)) != -1) {
                    os.write(b, 0, len);
                }
                return new String(os.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * 执行
     *
     * @param handler 反混淆的一行的处理器
     * @throws IOException 读取文件失败时会抛出异常
     */
    public void execute(TraceLineHandler handler) throws IOException {
        File transformedMappingFile = mappingFile;
        if (!charToNormalMap.isEmpty()) {
            transformedMappingFile = transformMappingFile(mappingFile);
        }

        MappingReader var1 = new MappingReader(transformedMappingFile);
        var1.pump(this);
        StringBuilder expression = new StringBuilder(this.regularExpression.length() + 32);
        char[] var3 = new char[32];
        int var4 = 0;
        int var5 = 0;

        while(true) {
            int var6 = this.regularExpression.indexOf(37, var5);
            if (var6 < 0 || var6 == this.regularExpression.length() - 1 || var4 == var3.length) {
                expression.append(this.regularExpression.substring(var5));
                Pattern pattern = Pattern.compile(expression.toString());
                LineNumberReader lineNumberReader = new LineNumberReader(this.stackTraceFile == null ?
                        new InputStreamReader(System.in) :
                        new BufferedReader(new FileReader(this.stackTraceFile)));

                try {
                    StringBuilder var8 = new StringBuilder(256);
                    ArrayList var9 = new ArrayList();
                    String var10 = null;

                    while(true) {
                        String line = lineNumberReader.readLine();
                        if (line == null) {
                            return;
                        }
                        line = transform(line);

                        Matcher matcher = pattern.matcher(line);
                        if (!matcher.matches()) {
                            handler.handle(line);
                        } else {
                            int var13 = 0;
                            String var14 = null;
                            String var15 = null;

                            int var16;
                            int var17;
                            for(var16 = 0; var16 < var4; ++var16) {
                                var17 = matcher.start(var16 + 1);
                                if (var17 >= 0) {
                                    String var18 = matcher.group(var16 + 1);
                                    char var19 = var3[var16];
                                    switch(var19) {
                                        case 'C':
                                            var10 = this.originalClassName(ClassUtil.externalClassName(var18));
                                            break;
                                        case 'a':
                                            var15 = this.originalArguments(var18);
                                            break;
                                        case 'c':
                                            var10 = this.originalClassName(var18);
                                            break;
                                        case 'l':
                                            var13 = Integer.parseInt(var18);
                                            break;
                                        case 't':
                                            var14 = this.originalType(var18);
                                    }
                                }
                            }

                            var16 = 0;
                            var8.setLength(0);
                            var9.clear();

                            for(var17 = 0; var17 < var4; ++var17) {
                                int var34 = matcher.start(var17 + 1);
                                if (var34 >= 0) {
                                    int var35 = matcher.end(var17 + 1);
                                    String var20 = matcher.group(var17 + 1);
                                    var8.append(line.substring(var16, var34));
                                    char var21 = var3[var17];
                                    switch(var21) {
                                        case 'C':
                                            var10 = this.originalClassName(ClassUtil.externalClassName(var20));
                                            var8.append(ClassUtil.internalClassName(var10));
                                            break;
                                        case 'a':
                                            var15 = this.originalArguments(var20);
                                            var8.append(var15);
                                            break;
                                        case 'c':
                                            var10 = this.originalClassName(var20);
                                            var8.append(var10);
                                            break;
                                        case 'f':
                                            this.originalFieldName(var10, var20, var14, var8, var9);
                                            break;
                                        case 'l':
                                            var13 = Integer.parseInt(var20);
                                            var8.append(var20);
                                            break;
                                        case 'm':
                                            this.originalMethodName(var10, var20, var13, var14, var15, var8, var9);
                                            break;
                                        case 't':
                                            var14 = this.originalType(var20);
                                            var8.append(var14);
                                    }

                                    var16 = var35;
                                }
                            }

                            var8.append(line.substring(var16));
                            handler.handle(var8);

                            for(var17 = 0; var17 < var9.size(); ++var17) {
                                System.out.println(var9.get(var17));
                            }
                        }
                    }
                } catch (IOException var30) {
                    throw new IOException("Can't read stack trace (" + var30.getMessage() + ")");
                } finally {
                    if (this.stackTraceFile != null) {
                        try {
                            lineNumberReader.close();
                        } catch (IOException var29) {
                            ;
                        }
                    }

                }
            }

            expression.append(this.regularExpression.substring(var5, var6));
            expression.append('(');
            char var7 = this.regularExpression.charAt(var6 + 1);
            switch(var7) {
                case 'C':
                    expression.append(regexClassSlash);
                    break;
                case 'a':
                    expression.append(regexArguments);
                    break;
                case 'c':
                    expression.append(regexClass);
                    break;
                case 'f':
                    expression.append(regexMember);
                    break;
                case 'l':
                    expression.append(REGEX_LINE_NUMBER);
                    break;
                case 'm':
                    expression.append(regexMember);
                    break;
                case 't':
                    expression.append(regexType);
            }

            expression.append(')');
            var3[var4++] = var7;
            var5 = var6 + 2;
        }
    }

    /**
     * 从字典文件中读取文本
     *
     * @return 字典文本
     */
    private String readTextFromDictFile() {
        if (dictFile != null && dictFile.exists()) {
            FileInputStream fis  = null;
            ByteArrayOutputStream os = null;
            try {
                fis = new FileInputStream(dictFile);
                os = new ByteArrayOutputStream();
                byte[] b = new byte[8 * 1052];
                int len;
                while ((len = fis.read(b, 0, sBufferSize)) != -1) {
                    os.write(b, 0, len);
                }
                byte[] bytes = os.toByteArray();
                return new String(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return "";
    }

    private void originalFieldName(String var1, String var2, String var3, StringBuilder var4, List var5) {
        int var6 = -1;
        Map var7 = (Map)this.classFieldMap.get(var1);
        if (var7 != null) {
            Set var8 = (Set)var7.get(var2);
            if (var8 != null) {
                Iterator var9 = var8.iterator();

                label46:
                while(true) {
                    while(true) {
                        FieldInfo var10;
                        do {
                            if (!var9.hasNext()) {
                                break label46;
                            }

                            var10 = (FieldInfo)var9.next();
                        } while(!var10.matches(var3));

                        if (var6 < 0) {
                            var6 = var4.length();
                            if (this.verbose) {
                                var4.append(var10.type).append(' ');
                            }

                            var4.append(var10.originalName);
                        } else {
                            StringBuilder var11 = new StringBuilder();

                            for(int var12 = 0; var12 < var6; ++var12) {
                                var11.append(' ');
                            }

                            if (this.verbose) {
                                var11.append(var10.type).append(' ');
                            }

                            var11.append(var10.originalName);
                            var5.add(var11);
                        }
                    }
                }
            }
        }

        if (var6 < 0) {
            var4.append(var2);
        }

    }

    private void originalMethodName(String var1, String var2, int var3, String var4, String var5, StringBuilder var6, List var7) {
        int var8 = -1;
        Map var9 = (Map)this.classMethodMap.get(var1);
        if (var9 != null) {
            Set var10 = (Set)var9.get(var2);
            if (var10 != null) {
                Iterator var11 = var10.iterator();

                label52:
                while(true) {
                    while(true) {
                        MethodInfo var12;
                        do {
                            if (!var11.hasNext()) {
                                break label52;
                            }

                            var12 = (MethodInfo)var11.next();
                        } while(!var12.matches(var3, var4, var5));

                        if (var8 < 0) {
                            var8 = var6.length();
                            if (this.verbose) {
                                var6.append(var12.type).append(' ');
                            }

                            var6.append(var12.originalName);
                            if (this.verbose) {
                                var6.append('(').append(var12.arguments).append(')');
                            }
                        } else {
                            StringBuilder var13 = new StringBuilder();

                            for(int var14 = 0; var14 < var8; ++var14) {
                                var13.append(' ');
                            }

                            if (this.verbose) {
                                var13.append(var12.type).append(' ');
                            }

                            var13.append(var12.originalName);
                            if (this.verbose) {
                                var13.append('(').append(var12.arguments).append(')');
                            }

                            var7.add(var13);
                        }
                    }
                }
            }
        }

        if (var8 < 0) {
            var6.append(var2);
        }

    }

    private String originalArguments(String var1) {
        StringBuilder var2 = new StringBuilder();
        int var3 = 0;

        while(true) {
            int var4 = var1.indexOf(44, var3);
            if (var4 < 0) {
                var2.append(this.originalType(var1.substring(var3).trim()));
                return var2.toString();
            }

            var2.append(this.originalType(var1.substring(var3, var4).trim())).append(',');
            var3 = var4 + 1;
        }
    }

    private String originalType(String var1) {
        int var2 = var1.indexOf(91);
        return var2 >= 0 ? this.originalClassName(var1.substring(0, var2)) + var1.substring(var2) : this.originalClassName(var1);
    }

    private String originalClassName(String var1) {
        String var2 = (String)this.classMap.get(var1);
        return var2 != null ? var2 : var1;
    }

    public boolean processClassMapping(String var1, String var2) {
        this.classMap.put(var2, var1);
        return true;
    }

    @Override
    public void processFieldMapping(String var1, String var2, String var3, String var4, String s4) {
        Object var5 = (Map)this.classFieldMap.get(var1);
        if (var5 == null) {
            var5 = new HashMap();
            this.classFieldMap.put(var1, var5);
        }

        Object var6 = (Set)((Map)var5).get(var4);
        if (var6 == null) {
            var6 = new LinkedHashSet();
            ((Map)var5).put(var4, var6);
        }

        ((Set)var6).add(new FieldInfo(var2, var3));
    }

    @Override
    public void processMethodMapping(String var1, int var2, int var3, String var4, String var5, String var6, String var7, int i2, int i3, String s5) {
        Object var8 = (Map)this.classMethodMap.get(var1);
        if (var8 == null) {
            var8 = new HashMap();
            this.classMethodMap.put(var1, var8);
        }

        Object var9 = (Set)((Map)var8).get(var7);
        if (var9 == null) {
            var9 = new LinkedHashSet();
            ((Map)var8).put(var7, var9);
        }

        ((Set)var9).add(new MethodInfo(var2, var3, var4, var6, var5));
    }

    public static String main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java proguard.ReTrace [-verbose] <mapping_file> <stacktrace_file> <dict_file>");
            System.exit(-1);
        }

        String regularExpression = STACK_TRACE_EXPRESSION;
        boolean verbose = false;

        int index = 0;
        for(int length = args.length; index < length; ++index) {
            String arg = args[index];
            if (arg.equals(REGEX_OPTION)) {
                ++index;
                regularExpression = args[index];
            } else {
                if (!arg.equals(VERBOSE_OPTION)) {
                    break;
                }
                verbose = true;
            }
        }

        if (index >= args.length || args.length - index < 3) {
            System.err.println("Usage: java proguard.ReTrace [-regex <regex>] [-verbose] <mapping_file> <stacktrace_file> <dict_file>");
            System.exit(-1);
        }

        File mappingFile = new File(args[index++]);
        File stackTraceFile = new File(args[index++]);
        File dictFile = new File(args[index++]);
        MyReTrace reTrace = new MyReTrace(
                regularExpression,
                verbose,
                mappingFile,
                stackTraceFile,
                dictFile
        );

        try {
            StringBuilder sb = new StringBuilder();
            reTrace.execute(new TraceLineHandler() {
                @Override
                public void handle(Object line) {
                    sb.append(line).append("\n");
                }
            });
//            write2Stream(new FileOutputStream("text/retrace.txt"), sb.toString().getBytes());
            return sb.toString();
        } catch (IOException ex) {
            if (verbose) {
                ex.printStackTrace();
            } else {
                System.err.println("Error: " + ex.getMessage());
            }

            System.exit(1);
        }

        System.exit(0);
        return null;
    }

    private static boolean write2Stream(final OutputStream os, final byte[] bytes) {
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(os);
            bos.write(bytes);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (bos != null) {
                try {
                    bos.flush();
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class MethodInfo {
        private int firstLineNumber;
        private int lastLineNumber;
        private String type;
        private String arguments;
        private String originalName;

        private MethodInfo(int var1, int var2, String var3, String var4, String var5) {
            this.firstLineNumber = var1;
            this.lastLineNumber = var2;
            this.type = var3;
            this.arguments = var4;
            this.originalName = var5;
        }

        private boolean matches(int var1, String var2, String var3) {
            return (var1 == 0 || this.firstLineNumber <= var1 && var1 <= this.lastLineNumber || this.lastLineNumber == 0) && (var2 == null || var2.equals(this.type)) && (var3 == null || var3.equals(this.arguments));
        }
    }

    private static class FieldInfo {
        private String type;
        private String originalName;

        private FieldInfo(String var1, String var2) {
            this.type = var1;
            this.originalName = var2;
        }

        private boolean matches(String var1) {
            return var1 == null || var1.equals(this.type);
        }
    }

    private static interface TraceLineHandler {
        void handle(Object line);
    }
}
