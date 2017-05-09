/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.gumga.maven.plugins.gumgag;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import javax.persistence.GeneratedValue;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author munif
 */
public class Util {

    public final static String IDENTACAO04 = "    ";
    public final static String IDENTACAO08 = "        ";
    public final static String IDENTACAO12 = "            ";
    public final static String IDENTACAO16 = "                ";
    public final static String IDENTACAO20 = "                    ";
    public final static String IDENTACAO24 = "                        ";
    public final static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public static String primeiraMaiuscula(String s) {
        return s.substring(0, 1).toUpperCase().concat(s.substring(1));
    }

    public static String primeiraMinuscula(String s) {
        return s.substring(0, 1).toLowerCase().concat(s.substring(1));
    }

    public static List<Field> getTodosAtributosMenosIdAutomatico(Class classe) {
        List<Field> todosAtributos = getTodosAtributosNaoEstaticos(classe);
        Field aRemover = null;
        Field aRemoverOi = null;
        Field aRemoverVersion = null;
        Field aRemoverGumgaCustomFields = null;
        for (Field f : todosAtributos) {
            if (f.isAnnotationPresent(GeneratedValue.class)) {
                aRemover = f;
            }
            if ("oi".equals(f.getName())) {
                aRemoverOi = f;
            }
            if ("version".equals(f.getName())) {
                aRemoverVersion = f;
            }
            if ("gumgaCustomFields".equals(f.getName())) {
                aRemoverGumgaCustomFields = f;
            }
        }
        if (aRemover != null) {
            todosAtributos.remove(aRemover);
        }
        if (aRemoverOi != null) {
            todosAtributos.remove(aRemoverOi);
        }
        if (aRemoverVersion != null) {
            todosAtributos.remove(aRemoverVersion);
        }
        if (aRemoverGumgaCustomFields!=null){
            todosAtributos.remove(aRemoverGumgaCustomFields);
            todosAtributos.add(aRemoverGumgaCustomFields);
            System.out.println(todosAtributos);
        }

        return todosAtributos;
    }

    public static List<Field> getTodosAtributosNaoEstaticos(Class classe) throws SecurityException {
        List<Field> aRetornar = new ArrayList<Field>();
        List<Field> aRemover = new ArrayList<Field>();
        if (!classe.getSuperclass().equals(Object.class)) {
            aRetornar.addAll(getTodosAtributosNaoEstaticos(classe.getSuperclass()));
        }
        aRetornar.addAll(Arrays.asList(classe.getDeclaredFields()));
        for (Field f : aRetornar) {
            if (Modifier.isStatic(f.getModifiers())) {
                aRemover.add(f);
            }
            if (f.getName().equals("version")) {
                aRemover.add(f);
            }
            if (f.getName().equals("oi")) {
                aRemover.add(f);
            }
        }
        aRetornar.removeAll(aRemover);
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

    public static Class getTipoGenerico(Field atributo) {
        Class tipoGenerico;
        if (atributo.getGenericType() instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) atributo.getGenericType();
            Type[] typeArguments = type.getActualTypeArguments();
            tipoGenerico = (Class) typeArguments[atributo.getType().equals(Map.class) ? 1 : 0];
        } else {
            tipoGenerico = atributo.getType();
        }
        return tipoGenerico;
    }

    public static void geraGumga(Log log) {
        log.info("\n"
                + "\n"
                + "   _____ _    _ __  __  _____          \n"
                + "  / ____| |  | |  \\/  |/ ____|   /\\    \n"
                + " | |  __| |  | | \\  / | |  __   /  \\   \n"
                + " | | |_ | |  | | |\\/| | | |_ | / /\\ \\  \n"
                + " | |__| | |__| | |  | | |__| |/ ____ \\ \n"
                + "  \\_____|\\____/|_|  |_|\\_____/_/    \\_\\\n"
                + "                                       \n"
                + "                                       \n"
                + "");
    }

    public static String etiqueta(Field atributo) {
        return primeiraMaiuscula(atributo.getName());
    }

    public static String windowsSafe(String s) {
        return s.replaceAll("\\\\", "/");
    }

    public static void adicionaLinha(String nomeArquivo, String linhaMarcador, String linhaNova) throws IOException {
        String arquivo = nomeArquivo;
        String arquivoTmp = nomeArquivo + "-tmp";

        BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoTmp));
        BufferedReader reader = new BufferedReader(new FileReader(arquivo));

        String linha;
        boolean colocou = false;
        while ((linha = reader.readLine()) != null) {
            if (linha.contains(linhaMarcador) && !colocou) {
                writer.write(linhaNova + "\n");
                colocou = true;
            }
            writer.write(linha + "\n");
        }

        writer.close();
        reader.close();

        new File(arquivo).delete();
        new File(arquivoTmp).renameTo(new File(arquivo));
    }

    public static String todosAtributosSeparadosPorVirgula(Class classeEntidade) {
        StringBuilder sb = new StringBuilder();
        for (Field f : getTodosAtributosMenosIdAutomatico(classeEntidade)) {
            sb.append(f.getName() + ",");

        }
        sb.setLength(sb.length() - 1);

        return sb.toString().replace("oi,", "");
    }

    public static String hoje() {
        return sdf.format(new Date());
    }

    public static void escreveCabecario(FileWriter fw) throws IOException {
        fw.write("/*\n"
                + "* Gerado automaticamente por GUMGAGenerator em " + hoje() + "\n"
                + "*/\n"
                + "\n");
    }

    public static String dependenciasSeparadasPorVirgula(Set<Class> dependencias,String sufixo,boolean apostrofe) {
        StringBuilder sb = new StringBuilder();
        for (Class clazz:dependencias) {
            sb.append(", "+(apostrofe?"'":"")+clazz.getSimpleName()+sufixo+(apostrofe?"'":"") );
        }
        return sb.toString();
    }

}