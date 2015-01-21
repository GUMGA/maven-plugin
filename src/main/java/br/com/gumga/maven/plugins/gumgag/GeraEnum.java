/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.gumga.maven.plugins.gumgag;

import java.awt.Event;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 *
 * @author munif
 */
@Mojo(name = "enum", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GeraEnum extends AbstractMojo {

    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    /**
     * Entidade
     */
    @Parameter(property = "enum", defaultValue = "nada")
    private String nomeEnumCompleto;

    @Parameter(property = "valores", defaultValue = "")
    private String valoresPossiveis;

    @Parameter(property = "rich", defaultValue = "false")
    private String rich;

    @Parameter(property = "multi", defaultValue = "false")
    private String multi;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Util.geraGumga(getLog());

        if ("nada".equals(nomeEnumCompleto)) {
            Scanner entrada = new Scanner(System.in);
            System.out.print("Nome completo da entidade a ser gerada:");
            nomeEnumCompleto = entrada.next();
        }
        String nomePacote = nomeEnumCompleto.substring(0, nomeEnumCompleto.lastIndexOf('.'));
        String nomeEnum = nomeEnumCompleto.substring(nomeEnumCompleto.lastIndexOf('.') + 1);
        String pastaEnum = Util.windowsSafe(project.getCompileSourceRoots().get(0)) + "/".concat(nomePacote.replaceAll("\\.", "/"));

        getLog().info("Iniciando plugin Gerador de Enum GUMGA ");
        getLog().info("Gerando " + nomePacote + "." + nomeEnum);
        File f = new File(pastaEnum);
        f.mkdirs();
        File arquivoClasse = new File(pastaEnum + "/" + nomeEnum + ".java");

        try {
            FileWriter fw = new FileWriter(arquivoClasse);
            fw.write("package " + nomePacote + ";\n\n");

            fw.write("/**\n"
                    + " *\n"
                    + " * @author gumgag\n"
                    + " */\n"
                    + "public enum " + nomeEnum + " {\n\n"
            );

            if (rich.equals("false") && multi.equals("false")) {
                String valuesPossiblesSimple[] = valoresPossiveis.split(",");

                for (int i = 1; i <= valuesPossiblesSimple.length; i++) {
                    String valueEnum = valuesPossiblesSimple[i - 1].trim();
                    if (i < valuesPossiblesSimple.length) {
                        fw.write("    " + valueEnum + ",\n");
                    } else {
                        fw.write("    " + valueEnum + "\n");
                    }
                }
            } else if (rich.equals("true") && multi.equals("false")) {
                String valorPossivel[] = valoresPossiveis.split(",");

                for (int i = 1; i <= valorPossivel.length; i++) {
                    String valor = valorPossivel[i - 1].trim();
                    String sepatorValue[] = valor.split(":");
                    String valueEnum = sepatorValue[0];
                    String description = sepatorValue[1];

                    if (i < valorPossivel.length) {
                        fw.write("    " + valueEnum + "(\"" + description + "\"), \n");
                    } else {
                        fw.write("    " + valueEnum + "(\"" + description + "\"); \n");
                    }
                }

                fw.write("\n"
                        + "    private final String description; \n\n"
                        + "    " + nomeEnum + " (String description) { \n"
                        + "        this.description = description; \n"
                        + "    } \n\n"
                        + "    public String getDescription() {\n"
                        + "        return description; \n"
                        + "    } \n\n"
                        + "    public String toString() {\n"
                        + "        return description; \n"
                        + "    } \n");
            } else if (multi.equals("true") && rich.equals("true")) {
                String valuePossibles[] = valoresPossiveis.split(",");
                ArrayList<String> impressoes = new ArrayList<>();
                int x = 1;
                int i = 1;

                for (String valor : valuePossibles) {
                    String sepatorValue[] = valor.split(":");
                    String valueEnum = sepatorValue[0].trim();
                    String description = sepatorValue[1];

                    String textFormat = ("    " + valueEnum + "(\"" + description + "\", " + x + ")");
                    impressoes.add(textFormat);
                    x *= 2;
                }

                for (String impressoe : impressoes) {
                    if (i < impressoes.size()) {
                        fw.write(impressoe + ",\n");
                    } else {
                        fw.write(impressoe + ";\n");
                    }
                    i++;
                }

                fw.write("\n"
                        + "    private final String description; \n"
                        + "    private final int multValue; \n\n"
                        + "    " + nomeEnum + " (String description, int multValue) { \n"
                        + "        this.description = description;\n"
                        + "        this.multValue = multValue; \n"
                        + "    } \n\n"
                        + "    public String getDescription() {\n"
                        + "        return description; \n"
                        + "    } \n\n"
                        + "    public int getMultValue() {\n"
                        + "        return multValue; \n"
                        + "    } \n\n"
                        + "    public String toString() {\n"
                        + "        return description; \n"
                        + "    } \n");
            } else if (multi.equals("true") && rich.equals("false")) {
                String valuePossibles[] = valoresPossiveis.split(",");
                ArrayList<String> impressoes = new ArrayList<>();
                
                int x = 1;
                int i = 1;

                for (String valor : valuePossibles) {
                    String sepatorValue[] = valor.split(":");
                    String valueEnum = sepatorValue[0].trim();

                    String textFormat = ("    " + valueEnum + "(" + x + ")");
                    impressoes.add(textFormat);
                    x *= 2;
                }

                for (String impressoe : impressoes) {
                    if (i < impressoes.size()) {
                        fw.write(impressoe + ",\n");
                    } else {
                        fw.write(impressoe + ";\n");
                    }
                    i++;
                }

                fw.write("\n"
                        + "    private final int multValue; \n\n"
                        + "    " + nomeEnum + " (int multValue) { \n"
                        + "        this.multValue = multValue; \n"
                        + "    } \n\n"
                        + "    public int getMultValue() {\n"
                        + "        return multValue; \n"
                        + "    } \n");
            }
            fw.write(" \n}");
            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

}
