/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javareflection;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.FileChooserUI;

/**
 *
 * @author Андрей
 */
public class JavaReflection {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ClassNotFoundException, IOException {

        //by default
        String className = "java.util.Set";

        if (args != null && args.length > 0) {
            className = args[0];
        }
        Class myclassImpl = Class.forName(className);
        Implementor impl = new Implementor(myclassImpl);
        impl.implementCLass();
    }
}
