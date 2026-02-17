package me.shouheng.deproguard.retrace;

import proguard.classfile.util.ClassUtil;
import proguard.obfuscate.MappingProcessor;
import proguard.obfuscate.MappingReader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 修复问题之后的反混淆工具
 *
 * @author <a href="mailto:shouheng2015@gmail.com">Shouheng.W</a>
 * @version 1.0
 * @date 2023/2/3 0:16
 */
public class ReTraceTool implements MappingProcessor {
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
    private final File dictFile;
    /** 方法，类名等的参数 */
    private final String defaultElementCharset = "A-Za-z0-9_$";
    private final String elementCharset;
    private Map classMap;
    private Map classFieldMap;
    private Map classMethodMap;

    private static int sBufferSize = 8192;

    public ReTraceTool(String regularExpression, boolean verbose, File mappingFile, File dictFile) {
        this.classMap = new HashMap();
        this.classFieldMap = new HashMap();
        this.classMethodMap = new HashMap();
        this.verbose = verbose;
        this.mappingFile = mappingFile;
        this.dictFile = dictFile;
        this.elementCharset = "A-Za-z0-9_$" + dictCharset();
        this.regularExpression = regularExpression.replace(defaultElementCharset, elementCharset);
        this.regexClass = REGEX_CLASS.replace(defaultElementCharset, elementCharset);
        this.regexClassSlash = REGEX_CLASS_SLASH.replace(defaultElementCharset, elementCharset);
        this.regexType = REGEX_TYPE.replace(defaultElementCharset, elementCharset);
        this.regexMember = REGEX_MEMBER.replace(defaultElementCharset, elementCharset);
        this.regexArguments = REGEX_ARGUMENTS.replace(defaultElementCharset, elementCharset);
    }

    /**
     * 执行
     *
     * @param handler 反混淆的一行的处理器
     * @throws IOException 读取文件失败时会抛出异常
     */
    public void execute(String text, TraceLineHandler handler) throws IOException {
        MappingReader mappingReader = new MappingReader(this.mappingFile);
        mappingReader.pump(this);
        StringBuilder expression = new StringBuilder(this.regularExpression.length() + 32);
        char[] var3 = new char[32];
        int var4 = 0;
        int index = 0;

        while(true) {
            int i = this.regularExpression.indexOf(37, index);
            if (i < 0 || i == this.regularExpression.length() - 1 || var4 == var3.length) {
                expression.append(this.regularExpression.substring(index));
                Pattern pattern = Pattern.compile(expression.toString());
                List<String> lines = text.lines().toList();

                StringBuilder var8 = new StringBuilder(256);
                ArrayList list = new ArrayList();
                String var10 = null;

                int lineIndex = 0;
                while(true) {
                    String line = lineIndex < lines.size() ? lines.get(lineIndex++) : null;
                    if (line == null) {
                        return;
                    }

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
                        list.clear();

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
                                        this.originalFieldName(var10, var20, var14, var8, list);
                                        break;
                                    case 'l':
                                        var13 = Integer.parseInt(var20);
                                        var8.append(var20);
                                        break;
                                    case 'm':
                                        this.originalMethodName(var10, var20, var13, var14, var15, var8, list);
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

                        for(var17 = 0; var17 < list.size(); ++var17) {
                            System.out.println(list.get(var17));
                        }
                    }
                }
            }

            expression.append(this.regularExpression.substring(index, i));
            expression.append('(');
            char var7 = this.regularExpression.charAt(i + 1);
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
            index = i + 2;
        }
    }

    /** 根据传入的混淆字典对反混淆正则表达式字符进行 hook */
    private String dictCharset() {
        String dictText = readTextFromDictFile();
        if (dictText.length() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Set<Character> set = new HashSet<>();
        for (int i=0, length=dictText.length(); i<length; i++) {
            char c = dictText.charAt(i);
            if (!((c >= 'a' && c <= 'z')
                    || (c > 'A' && c < 'Z')
                    || c == '_'
                    || c == '$'
                    || c == '\n'
                    || c == '\r'
                    || c == ' ') && !set.contains(c)) {
                set.add(c);
                sb.append(c);
            }
        }
        return sb.toString();
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

    public static String handle(File mappingFile, File dictFile, String text) {
        ReTraceTool reTrace = new ReTraceTool(
                STACK_TRACE_EXPRESSION,
                false,
                mappingFile,
                dictFile
        );

        try {
            StringBuilder sb = new StringBuilder();
            reTrace.execute(text, (TraceLineHandler) line -> sb.append(line).append("\n"));
            return sb.toString();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean processClassMapping(String className, String newClassName) {
        this.classMap.put(newClassName, className);
        return true;
    }

    @Override
    public void processFieldMapping(String className, String fieldType, String fieldName, String newClassName, String newFieldName) {
        Object var5 = (Map) this.classFieldMap.get(className);
        if (var5 == null) {
            var5 = new HashMap();
            this.classFieldMap.put(className, var5);
        }

        Object var6 = (Set)((Map)var5).get(newClassName);
        if (var6 == null) {
            var6 = new LinkedHashSet();
            ((Map)var5).put(newClassName, var6);
        }

        ((Set)var6).add(new FieldInfo(fieldType, fieldName));
    }

    @Override
    public void processMethodMapping(String className, int firstLineNumber, int lastLineNumber, String methodReturnType, String methodName, String methodArguments, String newClassName, int newFirstLineNumber, int newLastLineNumber, String newMethodName) {
        Object var8 = (Map) this.classMethodMap.get(className);
        if (var8 == null) {
            var8 = new HashMap();
            this.classMethodMap.put(className, var8);
        }

        Object var9 = (Set) ((Map)var8).get(newClassName);
        if (var9 == null) {
            var9 = new LinkedHashSet();
            ((Map)var8).put(newClassName, var9);
        }

        ((Set)var9).add(new MethodInfo(firstLineNumber, lastLineNumber, methodReturnType, methodArguments, methodName));
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

    private interface TraceLineHandler {
        void handle(Object line);
    }
}
