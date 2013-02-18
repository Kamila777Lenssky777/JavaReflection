/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javareflection;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Андрей
 */
public class Implementor {

    private Class myInstance;
    private OutputStreamWriter writer;
    private StringBuilder program;
    private Set<String> setOfImports;

    public Implementor(Class classToImplement) {
        myInstance = classToImplement;
        program = new StringBuilder();
        setOfImports = new TreeSet<>();
    }

    public boolean writeProgramToFile() throws IOException {
        try {
            writer = new FileWriter(myInstance.getSimpleName() + "Impl.java");
            writer.append(program.toString());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
        return true;
    }

    public void implementCLass() throws IOException {
        program.append(getTitle());
        program.append(getOverrideConstructors());
        program.append(getOverridedMethods());
        program.insert(0, getImports() + "\n");
        program.append("}\n");
        writeProgramToFile();
    }

    private String getImports() {
        String result = "";
        List<String> list = processImports();
        for (String line : list) {
            result += line + "\n";
        }
        return result;
    }

    private String getTitle() {
        String result = "";
        setOfImports.add(myInstance.getCanonicalName());
        result += "public class " + myInstance.getSimpleName() + "Impl";
        String genericType = "";
        TypeVariable[] tv = myInstance.getTypeParameters();
        int tvLength = tv.length;
        if (tvLength != 0) {
            genericType += "<";
            for (int i = 0; i < tv.length; i++) {
                genericType += tv[i].getName();
                if (i != tvLength - 1) {
                    genericType += ",";
                }
            }
            genericType += ">";
        }
        result += genericType;
        result += myInstance.isInterface() ? " implements " : " extends ";
        result += myInstance.getSimpleName() + genericType + "{\n";
        return result;
    }

    public String getOverridedMethods() {
        String result = "";
        Method[] methods = myInstance.getDeclaredMethods();
        for (Method method : methods) {
            result += getOverridedMethodString(method);
        }
        return result;
    }

    public String getOverridedMethodString(Method method) {
        String result = "";
        int modifiers = method.getModifiers();
        if (!Modifier.isPrivate(modifiers) && Modifier.isAbstract(modifiers)) {
            String str = method.toGenericString();
            str = str.replaceAll("(\\(=?).+", "");
            str = processString(str.replaceAll("abstract", ""));
            result += "\t" + str + "(";

            Type[] parametrs = method.getGenericParameterTypes();
            for (int i = 0; i < parametrs.length; i++) {
                result += processString(parametrs[i].toString()) + " arg" + i;
                if (i != parametrs.length - 1) {
                    result += ", ";
                }
            }
            result += ")";
            Type[] exceptions = method.getGenericExceptionTypes();
            for (int i = 0; i < exceptions.length; i++) {
                result += processString(exceptions[i].toString());
                if (i != exceptions.length - 1) {
                    result += ", ";
                }
            }
            result += "{\n";
            result += "\t\tthrow new UnsupportedOperationException(\"Not supported yet.\");\n\t}\n";
        }
        return result;
    }

    public String processString(String line) {
        String regexp = "(class )?(\\w+(\\.|\\$))+(?=[A-Z]+|\\w+)";
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(line);

        List<String> list = new ArrayList<String>();
        while (m.find()) {
            if (m.group().length() != 0) {
                String importLine = m.group().trim();
                System.out.println("imp: " + importLine);
                line = line.replaceAll(regexp, "");
                if (!importLine.contains("java.lang")) {
                    setOfImports.add(importLine.replaceAll("\\$", "\\."));
                }
            }
        }
        return line;
    }

    private List<String> processImports() {
        List<String> result = new ArrayList<>();
        String line = null;
        Iterator<String> iterator = setOfImports.iterator();
        if (iterator.hasNext()) {
            line = iterator.next();
        }
        while (iterator.hasNext()) {
            String importLine = iterator.next();
            if (!importLine.contains(line)) {
                result.add("import " + line + ";");
                line = importLine;
            }
        }
        result.add("import " + line + (line.endsWith(".") ? "*;" : ";"));
        return result;
    }

    private String getOverrideConstructors() {
        String result = "";
        Constructor[] constructors = myInstance.getDeclaredConstructors();
        for (Constructor constructor : constructors) {
            result += getOverridedConstructorString(constructor);
        }
        return result;
    }

    private String getOverridedConstructorString(Constructor c) {
        String result = "";
        String arguments = "\tsuper(";
        if (Modifier.isPrivate(c.getModifiers())) {
            return result;
        }
        Type[] types = c.getGenericParameterTypes();
        if (types.length != 0) {
            result += "\tpublic " + myInstance.getSimpleName() + "Impl(";
            for (int i = 0; i < types.length; i++) {
                result += processString(types[i].toString()) + " arg" + i;
                arguments += "arg" + i;
                if (i != types.length - 1) {
                    result += ", ";
                    arguments += ", ";
                }
            }
            result += ")";
            Type[] exceptions = c.getGenericExceptionTypes();
            if (exceptions.length > 0) {
                result += " throws ";
            }
            for (int i = 0; i < exceptions.length; i++) {
                result += processString(exceptions[i].toString());
                if (i != exceptions.length - 1) {
                    result += ", ";
                }
            }
            result += "{\n\t" + arguments + ");\n\t}\n";
        }
        return result;
    }
}
