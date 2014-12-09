/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.gumga.maven.plugins.gumgag;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    private String valoesPossiveis;

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
        String pastaEnum = project.getCompileSourceRoots().get(0) + "/".concat(nomePacote.replaceAll("\\.", "/"));

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
                    + "    " + valoesPossiveis + "\n\n"
                    + "}\n");
            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

}
