/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.gumga.maven.plugins.gumgag;

import java.io.File;
import java.io.FileWriter;
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
@Mojo(name = "entidade", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GeraEntidade extends AbstractMojo {

    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    /**
     * Entidade a ser criada
     */
    @Parameter(property = "entidade", defaultValue = "nada")
    private String nomeCompletoEntidade;

    private File diretorioBase;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        if ("nada".equals(nomeCompletoEntidade)) {
            Scanner entrada = new Scanner(System.in);
            System.out.print("Nome completo da entidade a ser gerada:");
            nomeCompletoEntidade = entrada.next();
        }
        diretorioBase = project.getBasedir();
        String nomePacote = nomeCompletoEntidade.substring(0, nomeCompletoEntidade.lastIndexOf('.'));
        String nomeEntidade = nomeCompletoEntidade.substring(nomeCompletoEntidade.lastIndexOf('.') + 1);
        String pastaClasse = project.getCompileSourceRoots().get(0) + "/".concat(nomePacote.replaceAll("\\.", "/"));

        getLog().info("Iniciando plugin Gerador de Entidade GUMGA ");
        getLog().info("Gerando " + nomePacote + "." + nomeEntidade);
        File f = new File(pastaClasse);
        f.mkdirs();
        File arquivoClasse = new File(pastaClasse + "/" + nomeEntidade + ".java");
        try {
            FileWriter fw = new FileWriter(arquivoClasse);
            fw.write("package " + nomePacote + ";\n\n");
            fw.write("public class " + nomeEntidade + " {\n\n");
            fw.write("}\n");
            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

}
