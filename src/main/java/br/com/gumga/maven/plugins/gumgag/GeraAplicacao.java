/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.gumga.maven.plugins.gumgag;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
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
    private Class<?> classeEntidade;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Util.geraGumga(getLog());

        try {
            nomePacoteBase = nomeCompletoEntidade.substring(0, nomeCompletoEntidade.lastIndexOf(".domain"));
            nomeEntidade = nomeCompletoEntidade.substring(nomeCompletoEntidade.lastIndexOf('.') + 1);

            nomePacoteRepositorio = nomePacoteBase + ".application.repository";
            nomePacoteService = nomePacoteBase + ".application.service";

            pastaRepositorios = Util.windowsSafe(project.getCompileSourceRoots().get(0)) + "/".concat(nomePacoteRepositorio.replaceAll("\\.", "/"));
            pastaServices = Util.windowsSafe(project.getCompileSourceRoots().get(0)) + "/".concat(nomePacoteService.replaceAll("\\.", "/"));

            getLog().info("Iniciando plugin Gerador de Classes de Aplicação ");
            getLog().info("Gerando para " + nomeEntidade);

            classeEntidade = Util.getClassLoader(project).loadClass(nomeCompletoEntidade);

            geraRepositorio();
            geraService();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(GeraAplicacao.class.getName()).log(Level.SEVERE, null, ex);
        }

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
                    + "import javax.transaction.Transactional;\n"
                    + "import org.hibernate.Hibernate;\n"
                    + "\n"
                    + "import " + nomePacoteRepositorio + "." + nomeEntidade + "Repository;\n"
                    + "import " + nomeCompletoEntidade + ";\n");

            for (Field fff : Util.getTodosAtributosNaoEstaticos(classeEntidade)) {
                if (fff.isAnnotationPresent(OneToMany.class) || fff.isAnnotationPresent(ManyToMany.class)) {
                    fw.write("import "+Util.getTipoGenerico(fff).getCanonicalName()+";\n");
                }
            }

            fw.write("\n"
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
                    + "\n");

            geraLoadFat(fw);

            fw.write(""
                    + "}\n"
                    + "\n");

            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

    private void geraLoadFat(FileWriter fw) throws IOException {
        List<Field> todosAtributos = Util.getTodosAtributosNaoEstaticos(classeEntidade);
        List<Field> atributosToMany = new ArrayList<>();
        for (Field f : todosAtributos) {
            if (f.isAnnotationPresent(OneToMany.class) || f.isAnnotationPresent(ManyToMany.class)) {
                atributosToMany.add(f);
            }
        }

        if (!atributosToMany.isEmpty()) {
            fw.write("    @Transactional\n"
                    + "    public " + nomeEntidade + " load" + nomeEntidade + "Fat(Long id) {\n"
                    + "        " + nomeEntidade + " obj = repository.findOne(id);\n");

            for (Field f : atributosToMany) {
                fw.write("        Hibernate.initialize(obj.get" + Util.primeiraMaiuscula(f.getName()) + "());\n");
                for (Field ff : Util.getTipoGenerico(f).getDeclaredFields()) {
                    if (ff.isAnnotationPresent(OneToMany.class) || ff.isAnnotationPresent(ManyToMany.class)) {
                        fw.write(""
                                + "        for(" + Util.getTipoGenerico(f).getSimpleName() + " subObj:obj.get" + Util.primeiraMaiuscula(f.getName()) + "()){\n"
                                + "            Hibernate.initialize(subObj.get" + Util.primeiraMaiuscula(ff.getName()) + "() );\n"
                                + "        }\n");
                    }
                }

            }

            fw.write("        return obj;\n"
                    + "    }\n\n");
        }

    }

}
