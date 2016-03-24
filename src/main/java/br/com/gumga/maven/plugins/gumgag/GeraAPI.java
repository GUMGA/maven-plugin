/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.gumga.maven.plugins.gumgag;

import gumga.framework.domain.domains.GumgaIP4;
import gumga.framework.domain.domains.GumgaImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import br.com.gumga.freemarker.Attribute;
import br.com.gumga.freemarker.ConfigurationFreeMarker;
import br.com.gumga.freemarker.TemplateFreeMarker;

/**
 *
 * @author munif
 */
@Mojo(name = "api", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GeraAPI extends AbstractMojo {

    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    /**
     * Entidade
     */
    @Parameter(property = "entidade", defaultValue = "all")
    private String nomeCompletoEntidade;
    private String nomePacoteBase;
    private String nomeEntidade;
    private String nomePacoteApi;
    private String pastaApi;

    private Class classeEntidade;

    private Set<Field> gumgaImages;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Util.geraGumga(getLog());

        try {
            nomePacoteBase = nomeCompletoEntidade.substring(0, nomeCompletoEntidade.lastIndexOf(".domain"));
            nomeEntidade = nomeCompletoEntidade.substring(nomeCompletoEntidade.lastIndexOf('.') + 1);
            nomePacoteApi = nomePacoteBase + ".api";
            pastaApi = Util.windowsSafe(project.getCompileSourceRoots().get(0)) + "/".concat(nomePacoteApi.replaceAll("\\.", "/"));
            getLog().info("Iniciando plugin Gerador de API ");
            getLog().info("Gerando para " + nomeEntidade);
            classeEntidade = Util.getClassLoader(project).loadClass(nomeCompletoEntidade);
            gumgaImages = new HashSet<>();
            for (Field atributo : Util.getTodosAtributosMenosIdAutomatico(classeEntidade)) {
                if (GumgaImage.class.equals(atributo.getType())) {
                    gumgaImages.add(atributo);
                }
            }
            geraApi();
        } catch (Exception ex) {
            getLog().error(ex);
        }

    }

    private void geraApi() {
        File f = new File(pastaApi);
        f.mkdirs();
//        File arquivoClasse = new File(pastaApi + "/" + nomeEntidade + "API.java");
        try {
        	ConfigurationFreeMarker config = new ConfigurationFreeMarker();
			TemplateFreeMarker template = new TemplateFreeMarker("api.ftl", config);
			
			template.add("packageName", this.nomePacoteApi);
			template.add("packageBase", this.nomePacoteBase);
			template.add("entityName", this.nomeEntidade);
			template.add("completeNameEntity", this.nomeCompletoEntidade);
			template.add("uriBase", "/api/" + this.nomeEntidade.toLowerCase());
			template.add("gumgaImage", !gumgaImages.isEmpty());
			
			List<String> attributes = new ArrayList<>();
			for(Field field : Util.getTodosAtributosNaoEstaticos(classeEntidade)) {
				if ((field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToMany.class)) && attributes.isEmpty() )
					attributes.add(this.nomeEntidade);
			}
			
			template.add("attributes", attributes);
			
			List<Attribute> attributesImage = new ArrayList<>();
			for (Field field: gumgaImages) {
				attributesImage.add(new Attribute(field.getName(), "", Util.primeiraMaiuscula(field.getName()), false, false, false, false, false));
			}
			template.add("gumgaImages", attributesImage);
			
			template.generateTemplate(this.pastaApi + "/" + this.nomeEntidade + "API.java");

//        	
//            FileWriter fw = new FileWriter(arquivoClasse);
//            fw.write(""
//                    + "package " + nomePacoteApi + ";\n"
//                    + "\n"
//                    + "import  " + nomePacoteBase + ".application.service." + nomeEntidade + "Service;\n"
//                    + "import " + nomeCompletoEntidade + ";\n"
//                    + "import gumga.framework.application.GumgaService;\n"
//                    + "import gumga.framework.presentation.GumgaAPI;\n"
//                    + "\n"
//                    + "import org.springframework.beans.factory.annotation.Autowired;\n"
//                    + "import org.springframework.web.bind.annotation.RequestMapping;\n"
//                    + "import org.springframework.web.bind.annotation.RestController;\n"
//                    + "import org.springframework.web.bind.annotation.PathVariable;\n"
//                    + "import org.springframework.transaction.annotation.Transactional;\n"
//                    + "import org.springframework.web.bind.annotation.RequestMethod;\n"
//                    + "import gumga.framework.presentation.RestResponse;\n"
//                    + "import javax.validation.Valid;\n"
//                    + "import org.springframework.web.bind.annotation.RequestBody;\n"
//                    + "import org.springframework.validation.BindingResult;\n"
//                    + "import gumga.framework.application.GumgaTempFileService;\n"
//                    + "import gumga.framework.domain.domains.GumgaImage;\n"
//                    + "import gumga.framework.presentation.GumgaAPI;\n"
//                    + "import org.springframework.web.bind.annotation.RequestMapping;\n"
//                    + "import java.io.IOException;\n"
//                    + "import org.springframework.web.bind.annotation.RequestParam;\n"
//                    + "import org.springframework.web.multipart.MultipartFile;\n"
//                    + ""
//                    + ""
//                    + ""
//                    + "\n"
//                    + "@RestController\n"
//                    + "@RequestMapping(\"/api/" + nomeEntidade.toLowerCase() + "\")\n"
//                    + "public class " + nomeEntidade + "API extends GumgaAPI<" + nomeEntidade + ", Long> {\n"
//                    + "\n");
//
//            if (!gumgaImages.isEmpty()) {
//                fw.write(""
//                        + "    @Autowired\n"
//                        + "    private GumgaTempFileService gumgaTempFileService;\n"
//                        + "\n");
//            }
//
//            fw.write(""
//                    + "    @Autowired\n"
//                    + "    public " + nomeEntidade + "API(GumgaService<" + nomeEntidade + ", Long> service) {\n"
//                    + "        super(service);\n"
//                    + "    }\n\n");
//
//            sobrecarregaLoad(fw);
//
//            for (Field gi : gumgaImages) {
//
//                fw.write(""
//                        + "    @RequestMapping(method = RequestMethod.POST, value = \"/" + gi.getName() + "\")\n"
//                        + "    public String " + gi.getName() + "Upload(@RequestParam MultipartFile " + gi.getName() + ") throws IOException {\n"
//                        + "        System.out.println(\"UPLOAD foto\");\n"
//                        + "        GumgaImage gi = new GumgaImage();\n"
//                        + "        gi.setBytes(" + gi.getName() + ".getBytes());\n"
//                        + "        gi.setMimeType(" + gi.getName() + ".getContentType());\n"
//                        + "        gi.setName(" + gi.getName() + ".getName());\n"
//                        + "        gi.setSize(" + gi.getName() + ".getSize());\n"
//                        + "        String fileName = gumgaTempFileService.create(gi);\n"
//                        + "        return fileName;\n"
//                        + "    }\n"
//                        + "\n"
//                        + "    @RequestMapping(method = RequestMethod.DELETE, value = \"/" + gi.getName() + "\")\n"
//                        + "    public String " + gi.getName() + "Delete(String fileName) {\n"
//                        + "        return gumgaTempFileService.delete(fileName);\n"
//                        + "    }\n"
//                        + "\n"
//                        + "    @RequestMapping(method = RequestMethod.GET, value = \"/" + gi.getName() + "/{fileName}\")\n"
//                        + "    public byte[] " + gi.getName() + "Get(@PathVariable(value = \"fileName\") String fileName) {\n"
//                        + "        return gumgaTempFileService.find(fileName).getBytes();\n"
//                        + "    }\n"
//                        + "\n"
//                        + ""
//                        + ""
//                );
//            }
//
//            if (!gumgaImages.isEmpty()) {
//                fw.write(""
//                        + "    @Transactional\n"
//                        + "    @RequestMapping(method = RequestMethod.POST)\n"
//                        + "    public RestResponse<" + nomeEntidade + "> save(@RequestBody @Valid " + nomeEntidade + " obj, BindingResult result) {\n");
//
//                for (Field gi : gumgaImages) {
//                    fw.write(""
//                            + "        if (obj.get" + Util.primeiraMaiuscula(gi.getName()) + "() != null) {\n"
//                            + "            obj.set" + Util.primeiraMaiuscula(gi.getName()) + "((GumgaImage) gumgaTempFileService.find(obj.get" + Util.primeiraMaiuscula(gi.getName()) + "().getName()));\n"
//                            + "        }\n");
//                }
//
//                fw.write("        return super.save(obj, result);\n"
//                        + "    }\n"
//                        + "\n"
//                        + "");
//
//                fw.write("\n"
//                        + "    @Override\n"
//                        + "    @Transactional\n"
//                        + "    @RequestMapping(value = \"/{id}\", method = RequestMethod.PUT, consumes = \"application/json\")\n"
//                        + "    public RestResponse<"+nomeEntidade+"> update(Long id, "+nomeEntidade+" obj, BindingResult result) {\n");
//                for (Field gi : gumgaImages) {
//                    fw.write(""
//                            + "      if (obj.getFoto()!= null) {\n"
//                            + "        if (\"null\".equals(obj.get" + Util.primeiraMaiuscula(gi.getName()) + "().getName())) {\n"
//                            + "            obj.set" + Util.primeiraMaiuscula(gi.getName()) + "(null);\n"
//                            + "        }else if (obj.get" + Util.primeiraMaiuscula(gi.getName()) + "().getSize() == 0) {\n"
//                            + "            obj.set" + Util.primeiraMaiuscula(gi.getName()) + "((GumgaImage) gumgaTempFileService.find(obj.get" + Util.primeiraMaiuscula(gi.getName()) + "().getName()));\n"
//                            + "        }\n"
//                            + "      }\n");
//                }
//                fw.write(""
//                        + "        return super.update(id, obj, result); \n"
//                        + "    }\n"
//                        + "\n"
//                        + "");
//
//            }
//
//            fw.write(""
//                    + "\n"
//                    + "}\n"
//                    + "\n");
//
//            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

    private void sobrecarregaLoad(FileWriter fw) throws IOException {
        List<Field> todosAtributos = Util.getTodosAtributosNaoEstaticos(classeEntidade);
        for (Field f : todosAtributos) {
            if (f.isAnnotationPresent(OneToMany.class) || f.isAnnotationPresent(ManyToMany.class)) {
                fw.write(""
                        + "    @Override\n"
                        + "    public " + nomeEntidade + " load(@PathVariable Long id) {\n"
                        + "        return ((" + nomeEntidade + "Service)service).load" + nomeEntidade + "Fat(id);\n"
                        + "    }\n"
                        + "\n");
                return;
            }
        }

    }

}
