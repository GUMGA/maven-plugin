/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.gumga.maven.plugins.gumgag;

import java.io.File;
import java.io.FileWriter;
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
@Mojo(name = "aplicacao", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GeraAplicacao extends AbstractMojo {

    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    /**
     * Entidade
     */
    @Parameter(property = "entidade", defaultValue = "all")
    private String nomeCompletoEntidade;
    private String nomePacoteRepositorio;
    private String nomePacoteService;
    private String nomePacoteBase;
    private String nomeEntidade;
    private String pastaRepositorios;
    private String pastaServices;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        nomePacoteBase = nomeCompletoEntidade.substring(0, nomeCompletoEntidade.lastIndexOf(".domain"));
        nomeEntidade = nomeCompletoEntidade.substring(nomeCompletoEntidade.lastIndexOf('.') + 1);

        nomePacoteRepositorio = nomePacoteBase + ".application.repository";
        nomePacoteService = nomePacoteBase + ".application.service";

        pastaRepositorios = project.getCompileSourceRoots().get(0) + "/".concat(nomePacoteRepositorio.replaceAll("\\.", "/"));
        pastaServices = project.getCompileSourceRoots().get(0) + "/".concat(nomePacoteService.replaceAll("\\.", "/"));

        getLog().info("Iniciando plugin Gerador de Classes de Aplicação ");
        getLog().info("Gerando para " + nomeEntidade);

        geraRepositorio();
        geraService();

    }

    private void geraRepositorio() {
        File f = new File(pastaRepositorios);
        f.mkdirs();
        File arquivoClasse = new File(pastaRepositorios + "/" + nomeEntidade + "Repository.java");
        try {
            FileWriter fw = new FileWriter(arquivoClasse);
            fw.write(""
                    + "package " + nomePacoteRepositorio + ";\n"
                    + "\n"
                    + "import gumga.framework.domain.repository.GumgaCrudRepository;\n"
                    + "import " + nomeCompletoEntidade + ";\n"
                    + "\n"
                    + "public interface " + nomeEntidade + "Repository extends GumgaCrudRepository<" + nomeEntidade + ", Long> {\n"
                    + "\n"
                    + "}\n"
                    + "\n");

            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

    private void geraService() {
        File f = new File(pastaServices);
        f.mkdirs();
        File arquivoClasse = new File(pastaServices + "/" + nomeEntidade + "Service.java");
        try {
            FileWriter fw = new FileWriter(arquivoClasse);
            fw.write(""
                    + "package " + nomePacoteService + ";\n"
                    + ""
                    + "import gumga.framework.application.GumgaService;\n"
                    + "\n"
                    + "import org.springframework.beans.factory.annotation.Autowired;\n"
                    + "import org.springframework.stereotype.Service;\n"
                    + "\n"
                    + "import " + nomePacoteRepositorio +"."+nomeEntidade+"Repository;\n"
                    + "import " + nomeCompletoEntidade + ";\n"
                    + "\n"
                    + "@Service\n"
                    + "public class " + nomeEntidade + "Service extends GumgaService<" + nomeEntidade + ", Long> {\n"
                    + "	\n"
                    + "	private " + nomeEntidade + "Repository repository;\n"
                    + "	\n"
                    + "	@Autowired\n"
                    + "	public " + nomeEntidade + "Service(" + nomeEntidade + "Repository repository) {\n"
                    + "		super(repository);\n"
                    + "		this.repository = repository;\n"
                    + "	}\n"
                    + "\n"
                    + "}\n"
                    + "\n");

            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

}
