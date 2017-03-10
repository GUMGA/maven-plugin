package io.gumga.maven.plugins.gumgag;

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

import io.gumga.freemarker.Attribute;
import io.gumga.freemarker.ConfigurationFreeMarker;
import io.gumga.freemarker.TemplateFreeMarker;

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

            getLog().info("entity name:" + nomeCompletoEntidade);
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
        try {
            ConfigurationFreeMarker config = new ConfigurationFreeMarker();
            TemplateFreeMarker template = new TemplateFreeMarker("repository.ftl", config);
            template.add("package", this.nomePacoteRepositorio);
            template.add("repositoryName", this.nomeEntidade);
            template.add("packageEntity", this.nomeCompletoEntidade);

            template.generateTemplate(this.pastaRepositorios + "/" + this.nomeEntidade + "Repository.java");

        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

    private void geraService() {
        File f = new File(pastaServices);
        f.mkdirs();
        try {
            ConfigurationFreeMarker config = new ConfigurationFreeMarker();
            TemplateFreeMarker template = new TemplateFreeMarker("service.ftl", config);
            template.add("package", this.nomePacoteService);
            template.add("serviceName", this.nomeEntidade);
            template.add("packageRepository", this.nomePacoteRepositorio + "." + this.nomeEntidade + "Repository");
            template.add("packageEntity", this.nomeCompletoEntidade);

            List<String> imports = new ArrayList<>();
            for (Field fff : Util.getTodosAtributosNaoEstaticos(classeEntidade)) {
                if (fff.isAnnotationPresent(OneToMany.class) || fff.isAnnotationPresent(ManyToMany.class)) {
                    imports.add("import " + Util.getTipoGenerico(fff).getCanonicalName() + ";");
                }
            }

            template.add("imports", imports);

            List<Field> attributesToMany = new ArrayList<>();
            List<Attribute> hibernate01 = new ArrayList<>();
            List<Attribute> hibernate02 = new ArrayList<>();
            for (Field field : Util.getTodosAtributosNaoEstaticos(classeEntidade)) {
                if (field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToMany.class)) {
                    attributesToMany.add(field);
                }

                if (!attributesToMany.isEmpty()) {
                    for (Field fieldToMany : attributesToMany) {
                        hibernate01.add(new Attribute(fieldToMany.getName(), "", Util.primeiraMaiuscula(fieldToMany.getName()), false, false, false, false, false, false, false));

                        for (Field fieldToMany02 : Util.getTipoGenerico(fieldToMany).getDeclaredFields()) {
                            if (fieldToMany02.isAnnotationPresent(OneToMany.class) || fieldToMany02.isAnnotationPresent(ManyToMany.class)) {
                                hibernate02.add(new Attribute(Util.getTipoGenerico(fieldToMany).getSimpleName(), Util.primeiraMaiuscula(fieldToMany.getName()), Util.primeiraMaiuscula(fieldToMany02.getName()), false, false, false, false, false, false, false));
                            }
                        }
                    }
                }
            }

            template.add("attributesToMany", !attributesToMany.isEmpty());
            template.add("hibernate01", hibernate01);
            template.add("hibernate02", hibernate02);
            template.generateTemplate(this.pastaServices + "/" + this.nomeEntidade + "Service.java");

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
