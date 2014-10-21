/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.gumga.maven.plugins.gumgag;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.GeneratedValue;
import org.apache.maven.project.MavenProject;

/**
 *
 * @author munif
 */
public class Util {

    public static String primeiraMaiuscula(String s) {
        return s.substring(0, 1).toUpperCase().concat(s.substring(1));
    }

    public static List<Field> getTodosAtributosMenosIdAutomatico(Class classe) {
        List<Field> todosAtributos = getTodosAtributos(classe);
        Field aRemover = null;
        for (Field f : todosAtributos) {
            if (f.isAnnotationPresent(GeneratedValue.class)) {
                aRemover = f;
                break;
            }
        }
        if (aRemover != null) {
            todosAtributos.remove(aRemover);
        }
        return todosAtributos;
    }

    public static List<Field> getTodosAtributos(Class classe) throws SecurityException {
        List<Field> aRetornar = new ArrayList<Field>();
        if (!classe.getSuperclass().equals(Object.class)) {
            aRetornar.addAll(getTodosAtributosMenosIdAutomatico(classe.getSuperclass()));
        }
        aRetornar.addAll(Arrays.asList(classe.getDeclaredFields()));
        return aRetornar;
    }

    public static ClassLoader getClassLoader(MavenProject project) {
        ClassLoader aRetornar = null;
        try {
            List elementos = new ArrayList();
            elementos.addAll(project.getRuntimeClasspathElements());
            elementos.addAll(project.getTestClasspathElements());

            URL[] runtimeUrls = new URL[elementos.size()];
            for (int i = 0; i < elementos.size(); i++) {
                String element = (String) elementos.get(i);
                runtimeUrls[i] = new File(element).toURI().toURL();
            }
            aRetornar = new URLClassLoader(runtimeUrls,
                    Thread.currentThread().getContextClassLoader());

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return aRetornar;
    }

    public static Field primeiroAtributo(Class classe) {
        return getTodosAtributosMenosIdAutomatico(classe).get(0);
    }

}
