package br.com.gumga.maven.plugins.gumgag;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo(name = "diagrama", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GeraDiagrama extends AbstractMojo {

    private static final String PREFIXO = "-----GUMGA-----";

    private ClassLoader classLoader;

    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    /**
     * Pasta de destino da documentacao
     */
    @Parameter(defaultValue = "${project.build.directory}", property = "pastaDestino", required = true)
    private File pastaDestino;

    /**
     * Pasta onde estão as classes
     */
    @Parameter(defaultValue = "${project.build.directory}", property = "pastaClasses", required = true)
    private File pastaClasses;

    public void execute() throws MojoExecutionException {
        Util.geraGumga(getLog());
        if (!pastaClasses.toString().endsWith("classes")) {
            pastaClasses = new File(pastaClasses, "classes");
        }
        getLog().info(PREFIXO + " Iniciando plugin de documentação ENTIDADES " + pastaClasses);
        incializaClassLoader();

        pastaDestino = new File(pastaDestino, "docs");
        if (!pastaDestino.exists()) {
            pastaDestino.mkdirs();
        }

        List<Class> classes = pesquisaClasses(pastaClasses);

        processaClasses(classes);

        // criaTouch();
    }

    private List<Class> pesquisaClasses(File pasta) {
        if (!pastaClasses.isDirectory()) {
            return Collections.EMPTY_LIST;
        }
        List<Class> aRetornar = new ArrayList<Class>();
        for (File f : pasta.listFiles()) {
            if (f.isDirectory()) {
                aRetornar.addAll(pesquisaClasses(f));
            } else {
                if (f.getName().endsWith(".class")) {
                    String nomeClasse = transformaEmNomeDeClasse(f);
                    try {
                        //Class classe = Class.forName(nomeClasse);
                        Class classe = classLoader.loadClass(nomeClasse);

                        if (classe.isAnnotationPresent(Entity.class)) {
                            aRetornar.add(classe);
                        }
                    } catch (Exception ex) {

                        getLog().error(PREFIXO + " Erro carregando classe " + nomeClasse);
                    }
                }
            }
        }
        return aRetornar;

    }

    private String transformaEmNomeDeClasse(File f) {
        String nomeCompleto = Util.windowsSafe(f.getAbsolutePath());
        String incio =  Util.windowsSafe(pastaClasses.getAbsolutePath());
        return nomeCompleto.replaceFirst(incio, "").replace(".class", "").replaceAll("/", ".").replaceFirst(".", "");
    }

    private void processaClasses(List<Class> classes) {
        Map<String, List<Class>> pacotes = pesquisaPacotes(classes);
        getLog().info(PREFIXO + " Pacotes " + pacotes.keySet());

        for (String pacote : pacotes.keySet()) {
            List<String> associcaos = new ArrayList<String>();
            try {
                File pacoteDot = new File(pastaDestino, pacote.replaceAll("\\.", "_") + ".dot");
                FileWriter fw = new FileWriter(pacoteDot, false);
                escreveCabecalho(fw);
                fw.write("subgraph cluster" + pacote.replaceAll("\\.", "_") + "\n{\n");
                fw.write("label=\"" + pacote + "\";\n");
                for (Class entidade : pacotes.get(pacote)) {
                    associcaos.addAll(criaClasse(entidade, fw));
                }
                fw.write("}\n\n");
                for (String s : associcaos) {
                    fw.write(s + "\n");
                }
                fw.write("\n}\n\n");
                fw.close();
            } catch (Exception ex) {
                getLog().error(ex);
            }
        }

        try {
            Set<String> associcaos = new HashSet<String>();
            File pacoteDot = new File(pastaDestino, "allclasses.dot");
            FileWriter fw = new FileWriter(pacoteDot, false);
            escreveCabecalho(fw);
            for (String pacote : pacotes.keySet()) {
                fw.write("subgraph cluster" + pacote.replaceAll("\\.", "_") + "\n{\n");
                fw.write("label=\"" + pacote + "\";\n");
                for (Class entidade : pacotes.get(pacote)) {
                    associcaos.addAll(criaClasse(entidade, fw));
                }
                fw.write("\n}\n\n");

            }
            for (String s : associcaos) {
                fw.write(s + "\n");
            }

            fw.write("\n}\n\n");

            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }
        pacotes.put("allclasses", null);
        geraArquivoDeLoteShPng(pacotes);
        geraArquivoDeLoteShSvg(pacotes);
        geraArquivoDeLoteBatPng(pacotes);
        geraArquivoDeLoteBatSvg(pacotes);

    }

    public void geraArquivoDeLoteShPng(Map<String, List<Class>> pacotes) {
        try {
            File script = new File(pastaDestino, "dot2png.sh");
            FileWriter fw = new FileWriter(script, false);
            script.setExecutable(true);
            fw.write("#!/bin/sh\n");
            for (String pacote : pacotes.keySet()) {
                fw.write("dot -T png -o " + pacote.replaceAll("\\.", "_") + ".png " + pacote.replaceAll("\\.", "_") + ".dot\n");
            }
            fw.write("\n");
            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

    public void geraArquivoDeLoteBatPng(Map<String, List<Class>> pacotes) {
        try {
            File script = new File(pastaDestino, "dot2png.bat");
            FileWriter fw = new FileWriter(script, false);
            //script.setExecutable(true);
            for (String pacote : pacotes.keySet()) {
                fw.write("dot -T png -o " + pacote.replaceAll("\\.", "_") + ".png " + pacote.replaceAll("\\.", "_") + ".dot\n");
            }
            fw.write("\n");
            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

    public void geraArquivoDeLoteShSvg(Map<String, List<Class>> pacotes) {
        try {
            File script = new File(pastaDestino, "dot2svg.sh");
            FileWriter fw = new FileWriter(script, false);
            script.setExecutable(true);
            fw.write("#!/bin/sh\n");
            for (String pacote : pacotes.keySet()) {
                fw.write("dot -T svg -o " + pacote.replaceAll("\\.", "_") + ".svg " + pacote.replaceAll("\\.", "_") + ".dot\n");
            }
            fw.write("\n");
            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

    public void geraArquivoDeLoteBatSvg(Map<String, List<Class>> pacotes) {
        try {
            File script = new File(pastaDestino, "dot2svg.bat");
            FileWriter fw = new FileWriter(script, false);
            //script.setExecutable(true);
            for (String pacote : pacotes.keySet()) {
                fw.write("dot -T svg -o " + pacote.replaceAll("\\.", "_") + ".svg " + pacote.replaceAll("\\.", "_") + ".dot\n");
            }
            fw.write("\n");
            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

    private void escreveCabecalho(FileWriter fileWriter) throws IOException {
        fileWriter.write("//Gerado automaticamente por plugin da www.gumga.com.br munif@munifgebara.com.br\n\n"
                + ""
                + "digraph G{\n"
                + "fontname = \"Bitstream Vera Sans\"\n"
                + "fontsize = 8\n\n"
                + "node [\n"
                + "        fontname = \"Bitstream Vera Sans\"\n"
                + "        fontsize = 8\n"
                + "        shape = \"record\"\n"
                + "]\n\n"
                + "edge [\n"
                + "        fontname = \"Bitstream Vera Sans\"\n"
                + "        fontsize = 8\n"
                + "]\n\n");
    }

    private List<String> criaClasse(Class entidade, FileWriter fw) throws Exception {
        List<String> associacoes = new ArrayList<String>();

        if (!entidade.getSuperclass().equals(Object.class)) {
            if (entidade.getSuperclass().getSimpleName().equals("GumgaModel")) {
                //COLOCAR apenas uma marca
            } else {
                associacoes.add("edge [ arrowhead = \"empty\" headlabel = \"\" taillabel = \"\"] " + entidade.getSimpleName() + " -> " + entidade.getSuperclass().getSimpleName());
            }
        }

        String cor = "";
        fw.write(entidade.getSimpleName() + " [" + cor + "label = \"{" + entidade.getSimpleName() + "|");
        Field atributos[] = entidade.getDeclaredFields();
        int i = 0;
        Set<String> metodosExcluidosDoDiagrama = new HashSet<>();
        metodosExcluidosDoDiagrama.add("equals");
        metodosExcluidosDoDiagrama.add("hashCode");
        metodosExcluidosDoDiagrama.add("toString");

        for (Field f : atributos) {
            i++;
            Class tipoAtributo = f.getType();
            String tipo = tipoAtributo.getSimpleName();
            String nomeAtributo = f.getName();
            String naA = nomeAtributo.substring(0, 1).toUpperCase() + nomeAtributo.substring(1);
            metodosExcluidosDoDiagrama.add("set" + naA);
            metodosExcluidosDoDiagrama.add("get" + naA);

            if ((f.getModifiers() & Modifier.STATIC) != 0) {
                continue;
            }

            if (f.getType().equals(List.class) || f.getType().equals(Set.class) || f.getType().equals(Map.class)) {
                ParameterizedType type = (ParameterizedType) f.getGenericType();
                Type[] typeArguments = type.getActualTypeArguments();
                Class tipoGenerico = (Class) typeArguments[f.getType().equals(Map.class) ? 1 : 0];

                if (f.isAnnotationPresent(ManyToMany.class)) {
                    ManyToMany mm = f.getAnnotation(ManyToMany.class);
                    if (!mm.mappedBy().isEmpty()) {
                        continue;
                    }
                    associacoes.add("edge [arrowhead = \"none\" headlabel = \"*\" taillabel = \"*.\"] " + entidade.getSimpleName() + " -> " + tipoGenerico.getSimpleName() + " [label = \"" + nomeAtributo + "\"]");
                } else if (f.isAnnotationPresent(OneToMany.class)) {
                    OneToMany oo = f.getAnnotation(OneToMany.class);
                    if (!oo.mappedBy().isEmpty()) {
                        continue;
                    }
                    associacoes.add("edge [arrowhead = \"none\" headlabel = \"*\" taillabel = \"1.\"] " + entidade.getSimpleName() + " -> " + tipoGenerico.getSimpleName() + " [label = \"" + nomeAtributo + "\"]");
                }

            } else if (f.isAnnotationPresent(ManyToOne.class)) {
                ManyToOne mo = f.getAnnotation(ManyToOne.class);
                associacoes.add("edge [arrowhead = \"none\" headlabel = \"1\" taillabel = \"*.\"] " + entidade.getSimpleName() + " -> " + tipo + " [label = \"" + nomeAtributo + "\"]");
            } else if (f.isAnnotationPresent(OneToOne.class)) {
                OneToOne oo = f.getAnnotation(OneToOne.class);
                if (!oo.mappedBy().isEmpty()) {
                    continue;
                }
                associacoes.add("edge [arrowhead = \"none\" headlabel = \"1\" taillabel = \"1.\"] " + entidade.getSimpleName() + " -> " + tipo + " [label = \"" + nomeAtributo + "\"]");

            } else {
                fw.write(nomeAtributo + ":" + tipo + "\\l");
            }
        }

        fw.write("|");
        Method metodos[] = entidade.getDeclaredMethods();
        for (Method m : metodos) {
            if (!metodosExcluidosDoDiagrama.contains(m.getName())) {
                fw.write(m.getName() + ":" + m.getReturnType().getSimpleName() + "\\l");
            }
        }
        fw.write("}\"]\n");
        return associacoes;
    }

    public Map<String, List<Class>> pesquisaPacotes(List<Class> classes) {
        Map<String, List<Class>> pacotes = new HashMap<String, List<Class>>();
        for (Class classe : classes) {
            String nomeClasse = classe.getName();
            String pacote = nomeClasse.substring(0, nomeClasse.lastIndexOf('.'));
            List<Class> lista = pacotes.get(pacote);
            if (lista == null) {
                lista = new ArrayList<Class>();
                pacotes.put(pacote, lista);
            }
            lista.add(classe);
        }
        return pacotes;
    }

    private void incializaClassLoader() {
        try {
            List elementos = new ArrayList();
            elementos.addAll(project.getRuntimeClasspathElements());
            elementos.addAll(project.getTestClasspathElements());

            URL[] runtimeUrls = new URL[elementos.size()];
            for (int i = 0; i < elementos.size(); i++) {
                String element = (String) elementos.get(i);
                //     getLog().info(PREFIXO + " Claspath " + element);
                runtimeUrls[i] = new File(element).toURI().toURL();
            }
            classLoader = new URLClassLoader(runtimeUrls,
                    Thread.currentThread().getContextClassLoader());

        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

}
