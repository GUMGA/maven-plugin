package io.gumga.maven.plugins.gumgag;

import io.gumga.domain.domains.GumgaAddress;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import io.gumga.freemarker.Attribute;
import io.gumga.freemarker.ConfigurationFreeMarker;
import io.gumga.freemarker.TemplateFreeMarker;
import io.gumga.domain.domains.GumgaBarCode;
import io.gumga.domain.domains.GumgaBoolean;
import io.gumga.domain.domains.GumgaCEP;
import io.gumga.domain.domains.GumgaCNPJ;
import io.gumga.domain.domains.GumgaCPF;
import io.gumga.domain.domains.GumgaEMail;
import io.gumga.domain.domains.GumgaFile;
import io.gumga.domain.domains.GumgaGeoLocation;
import io.gumga.domain.domains.GumgaIP4;
import io.gumga.domain.domains.GumgaIP6;
import io.gumga.domain.domains.GumgaImage;
import io.gumga.domain.domains.GumgaMoney;
import io.gumga.domain.domains.GumgaMultiLineString;
import io.gumga.domain.domains.GumgaPhoneNumber;
import io.gumga.domain.domains.GumgaTime;
import io.gumga.domain.domains.GumgaURL;

/**
 *
 * @author munif
 */
@Mojo(name = "apresentacao", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GeraPresentation extends AbstractMojo {

    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    /**
     * Entidade
     */
    @Parameter(property = "entidade", defaultValue = "all")
    private String nomeCompletoEntidade;
    @Parameter(property = "override", defaultValue = "false")
    private boolean override;

    private String nomeEntidade;

    private Class classeEntidade;

    private Set<Class> dependenciasManyToOne;
    private Set<Class> dependenciasOneToMany;
    private Set<Class> dependenciasManyToMany;
    private Set<Field> atributosGumgaImage;
    private Set<Class> dependenciasEnums;

    private String pastaApp;
    private String pastaControllers;
    private String pastaServices;
    private String pastaViews;
    private String pastaI18n;

    private Boolean isWebpack;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Util.geraGumga(getLog());
        if (override) {
            System.out.println("NAO ADICIONANDO AO MENU NEM AO INTERNACIONALIZACAO");
        }

        try {
            nomeEntidade = nomeCompletoEntidade.substring(nomeCompletoEntidade.lastIndexOf('.') + 1);

            pastaApp = Util.windowsSafe(project.getFile().getParent()) + "/src/main/webapp/app/modules/" + (nomeEntidade.toLowerCase());

            isWebpack = pastaApp.indexOf("presentation-webpack") != -1;

            System.out.println("WEBPACK: "+isWebpack);
            
            getLog().info("Iniciando plugin Gerador de Html e JavaScript de Apresentação oi");
            getLog().info("Gerando para " + nomeEntidade);

            File f = new File(pastaApp);
            f.mkdirs();
            pastaControllers = pastaApp + "/controllers";
            pastaServices = pastaApp + "/services";
            pastaViews = pastaApp + "/views";
            pastaI18n = Util.windowsSafe(project.getFile().getParent()) + "/src/main/webapp/i18n/";
            new File(pastaControllers).mkdirs();
            new File(pastaServices).mkdirs();
            new File(pastaViews).mkdirs();
            new File(pastaI18n).mkdirs();

            classeEntidade = Util.getClassLoader(project).loadClass(nomeCompletoEntidade);

            dependenciasManyToOne = new HashSet<>();
            dependenciasManyToMany = new HashSet<>();
            dependenciasOneToMany = new HashSet<>();
            atributosGumgaImage = new HashSet<>();
            dependenciasEnums = new HashSet<>();

            for (Field atributo : Util.getTodosAtributosMenosIdAutomatico(classeEntidade)) {
                if (atributo.isAnnotationPresent(OneToOne.class)) {
                    dependenciasManyToOne.add(atributo.getType());
                }
                if (atributo.isAnnotationPresent(ManyToOne.class)) {
                    dependenciasManyToOne.add(atributo.getType());
                }
                if (atributo.isAnnotationPresent(ManyToMany.class)) {
                    dependenciasManyToMany.add(Util.getTipoGenerico(atributo));
                }
                if (atributo.isAnnotationPresent(OneToMany.class)) {
                    dependenciasOneToMany.add(Util.getTipoGenerico(atributo));
                }
                if (atributo.getType().isEnum()) {
                    dependenciasEnums.add(atributo.getType());
                }
                if (atributo.getType().equals(GumgaImage.class)) {
                    atributosGumgaImage.add(atributo);
                }

            }

            geraModule();
            geraServices();
            geraControllers();
            geraViews();

            if (!override) {
                getLog().info("18n");
                geraI18n();
            }
            if (!override) {
                getLog().info("menu");
                adicionaAoMenu();
            }
        } catch (Exception ex) {
            getLog().error(ex);
        }

    }

    private void adicionaAoMenu() throws IOException {

        StringBuilder menu = new StringBuilder();
        menu.append("	{\n");
        menu.append("		\"type\": \"item\",\n");
        menu.append("		\"label\": \""+nomeEntidade+"\",\n");
        menu.append("		\"key\": \"CRUD-"+nomeEntidade+"\",\n");
        menu.append("		\"children\": [\n");
        menu.append("				{\n");
        menu.append("					\"type\": \"item\",\n");
        menu.append("					\"label\": \"Inserir\",\n");
        menu.append("					\"state\": \""+nomeEntidade.toLowerCase()+".insert\",\n");
        menu.append("					\"key\": \"CRUD-"+nomeEntidade+"\"\n");
        menu.append("				},\n");
        menu.append("				{\n");
        menu.append("					\"type\": \"item\",\n");
        menu.append("					\"label\": \"Listagem\",\n");
        menu.append("					\"state\": \""+nomeEntidade.toLowerCase()+".list\",\n");
        menu.append("					\"key\": \"CRUD-"+nomeEntidade+"\"\n");
        menu.append("				}\n");
        menu.append("			]\n");
        menu.append("	},\n");

        Util.adicionaLinha(Util.windowsSafe(project.getFile().getParent()) + "/src/main/webapp/gumga-menu.json", "{", menu.toString());
//        Util.adicionaLinha(Util.windowsSafe(project.getFile().getParent()) + "/src/main/webapp/gumga-menu.json", "{",
//                "    {\n"
//                + "        \"label\": \"" + nomeEntidade + "\",\n"
//                + "        \"URL\": \"" + nomeEntidade.toLowerCase() + ".list\",\n"
//                + "        \"key\": \"CRUD-" + nomeEntidade + "\",\n"
//                + "        \"icon\": \"glyphicon glyphicon-user\",\n"
//                + "        \"icon_color\": \"\",\n"
//                + "        \"imageUrl\": \"\",\n"
//                + "        \"imageWidth\": \"\",\n"
//                + "        \"imageHeight\": \"\",\n"
//                + "        \"filhos\": [\n"
//                + "             {\n"
//                + "             \"label\": \" Inserir \",\n"
//                + "             \"URL\": \"" + nomeEntidade.toLowerCase() + ".insert\",\n"
//                + "             \"key\": \"CRUD-" + nomeEntidade + "\",\n"
//                + "             \"icon\": \"glyphicon glyphicon-user\",\n"
//                + "             \"filhos\": []\n"
//                + "             }\n"
//                + "         ]\n"
//                + "    },");,

        Util.adicionaLinha(Util.windowsSafe(project.getFile().getParent()) + "/src/main/webapp/app/app.js", "//FIMROUTE", ""
                + Util.IDENTACAO04 + Util.IDENTACAO04 + ".state('" + nomeEntidade.toLowerCase() + "', {\n"
                + Util.IDENTACAO04 + Util.IDENTACAO04 + "data: {\n"
                + Util.IDENTACAO04 + Util.IDENTACAO04 + Util.IDENTACAO04 + "id: 1\n"
                + Util.IDENTACAO04 + Util.IDENTACAO04 + "}, \n"
                + Util.IDENTACAO04 + Util.IDENTACAO04 + Util.IDENTACAO04 + "url: '/" + nomeEntidade.toLowerCase() + "',\n"
                + Util.IDENTACAO04 + Util.IDENTACAO04 + Util.IDENTACAO04 + "templateUrl: tempĺateBase\n"
                + Util.IDENTACAO04 + Util.IDENTACAO04 + "})\n"
                + "");

        if(isWebpack){
            Util.adicionaLinha(Util.windowsSafe(project.getFile().getParent()) + "/src/main/webapp/app/import-modules.js", "//FIMREQUIRE", "require('./modules/" + nomeEntidade.toLowerCase() + "/module');");
        }else{
            Util.adicionaLinha(Util.windowsSafe(project.getFile().getParent()) + "/src/main/webapp/app/app.js", "//FIMREQUIRE", Util.IDENTACAO04 + "require('app/modules/" + nomeEntidade.toLowerCase() + "/module');");
        }

        Util.adicionaLinha(Util.windowsSafe(project.getFile().getParent()) + "/src/main/webapp/app/app.js", "//FIMINJECTIONS", Util.IDENTACAO04 + Util.IDENTACAO04 + ",'app." + nomeEntidade.toLowerCase() + "'");

        Util.adicionaLinha(Util.windowsSafe(project.getFile().getParent()) + "/src/main/webapp/keys.json", "]", ",\"CRUD-" + nomeEntidade + "\"");

    }

    private void geraControllers() {
        try {
            ConfigurationFreeMarker config = new ConfigurationFreeMarker();
            TemplateFreeMarker template = new TemplateFreeMarker(isWebpack ? "moduleControllerPresentationWebpack.ftl" : "moduleControllerPresentation.ftl", config);
            template.add("entityName", this.nomeEntidade);
            template.add("entityNameLowerCase", this.nomeEntidade.toLowerCase());
            List<String> controllers = new ArrayList<>();
            for (Class classe : dependenciasOneToMany) {
                controllers.add(classe.getSimpleName().trim());
            }
            template.add("controllers", controllers);
            template.generateTemplate(this.pastaControllers + "/module.js");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            ConfigurationFreeMarker config = new ConfigurationFreeMarker();
            TemplateFreeMarker template = new TemplateFreeMarker(isWebpack ? "listControllerPresentationWebpack.ftl" : "listControllerPresentation.ftl", config);
            template.add("entityName", this.nomeEntidade);
            template.add("entityNameLowerCase", this.nomeEntidade.toLowerCase());
            template.add("firstAttribute", Util.primeiroAtributo(classeEntidade).getName());
            template.generateTemplate(this.pastaControllers + "/" + this.nomeEntidade + "ListController.js");

            for (Field field : Util.getTodosAtributosMenosIdAutomatico(this.classeEntidade)) {
                if (getAttributeType(field).equals("oneToMany")) {
                    config = new ConfigurationFreeMarker();
                    template = new TemplateFreeMarker(isWebpack ? "modalControllerPresentationWebpack.ftl" : "modalControllerPresentation.ftl", config);
                    template.add("entityName", Util.getTipoGenerico(field).getSimpleName().trim());
                }
            }

            for (Class classe : dependenciasOneToMany) {
                Set<Attribute> dependenciasManyToOneModal = new HashSet<>();
                Set<Class> dependenciasOneToManyModal = new HashSet<>();
                Set<Class> dependenciasManyToManyModal = new HashSet<>();
                Set<Field> atributosGumgaImageModal = new HashSet<>();
                Set<Class> dependenciasEnumsModal = new HashSet<>();
                List<Attribute> attributesOneToMany = new ArrayList<>();
                String fieldName = "";
                String entitySimpleNameLowerCase = "";



                for (Field atributo : Util.getTodosAtributosMenosIdAutomatico(classe)) {

                    if (atributo.isAnnotationPresent(OneToOne.class)) {
                        Field field = Util.primeiroAtributo(atributo.getType());
                        dependenciasManyToOneModal.add(new Attribute(field.getName(), "", atributo.getType().getSimpleName(), false, false, false, false, false, false, false));
//                        dependenciasManyToOneModal.add(atributo.getType());
                    }
                    if (atributo.isAnnotationPresent(ManyToOne.class)) {
                        Field field = Util.primeiroAtributo(atributo.getType());
                        dependenciasManyToOneModal.add(new Attribute(field.getName(), "", atributo.getType().getSimpleName(), false, false, false, false, false, false, false));
//                        dependenciasManyToOneModal.add(atributo.getType());
                    }
                    if (atributo.isAnnotationPresent(ManyToMany.class)) {
                        dependenciasManyToManyModal.add(Util.getTipoGenerico(atributo));
                    }
                    if (atributo.isAnnotationPresent(OneToMany.class)) {
                        dependenciasOneToManyModal.add(Util.getTipoGenerico(atributo));
                        attributesOneToMany.add(new Attribute(atributo.getName(), "", this.classeEntidade.getSimpleName().toLowerCase(), false, false, false, false, false, false, false));
                    }
                    if (atributo.getType().isEnum()) {
                        dependenciasEnumsModal.add(atributo.getType());
                    }
                    if (atributo.getType().equals(GumgaImage.class)) {
                        atributosGumgaImageModal.add(atributo);
                    }
                }

                String injectManyToOne = "";
                String injectControllerManyToOne = "";
                for (Attribute dp : dependenciasManyToOneModal) {
                    injectManyToOne += ",'" + dp.getNameGetterAndSetter() + "Service'";
                    injectControllerManyToOne += "," + dp.getNameGetterAndSetter() + "Service";
                }

                config = new ConfigurationFreeMarker();
                template = new TemplateFreeMarker(isWebpack ? "modalControllerPresentationWebpack.ftl" : "modalControllerPresentation.ftl", config);
                template.add("entityName", classe.getSimpleName().trim());
                template.add("dpManyToOne", dependenciasManyToOneModal);
                template.add("injectManyToOne", injectManyToOne);
                template.add("injectControllerManyToOne", injectControllerManyToOne);
                template.add("fieldName", fieldName);
                template.add("entitySimpleNameLowerCase", entitySimpleNameLowerCase);
                template.add("attributesOneToMany", attributesOneToMany);
                template.add("entity", this.classeEntidade.getSimpleName().toLowerCase());
                template.generateTemplate(this.pastaControllers + "/Modal" + classe.getSimpleName() + "Controller.js");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            Set<Class> dependenciasManyToX = new HashSet<>();
            dependenciasManyToX.addAll(dependenciasManyToMany);
            dependenciasManyToX.addAll(dependenciasManyToOne);

            ConfigurationFreeMarker config = new ConfigurationFreeMarker();
            TemplateFreeMarker template = new TemplateFreeMarker(isWebpack ? "formControllerPresentationWebpack.ftl" : "formControllerPresentation.ftl", config);
            template.add("entityName", this.nomeEntidade);
            template.add("entityNameLowerCase", this.nomeEntidade.toLowerCase());
            template.add("dependenciesInject", Util.dependenciasSeparadasPorVirgula(dependenciasManyToX, "Service", true));
            template.add("dependenciesParam", Util.dependenciasSeparadasPorVirgula(dependenciasManyToX, "Service", false));

            List<Attribute> dependenciesManyTo = new ArrayList<>();
            for (Class c : dependenciasManyToX) {
                dependenciesManyTo.add(new Attribute(c.getSimpleName(), c.getSimpleName().toLowerCase(), Util.primeiroAtributo(c).getName(), false, false, false, false, false, false, false));
            }

            template.add("dependenciesManyTo", dependenciesManyTo);
            List<String> attributesNotStatic = new ArrayList<>();
            for (Field field : Util.getTodosAtributosNaoEstaticos(classeEntidade)) {
                if (field.isAnnotationPresent(ManyToMany.class)) {
                    attributesNotStatic.add(field.getName());
                }
            }

            template.add("attributesNotStatic", attributesNotStatic);

            List<Attribute> attributes = new ArrayList<>();
            for (Field field : Util.getTodosAtributosMenosIdAutomatico(classeEntidade)) {
                if (field.getType().equals(Date.class)) {
                    attributes.add(new Attribute(field.getName(), "opened" + Util.primeiraMaiuscula(field.getName()), this.classeEntidade.getSimpleName().toLowerCase(), false, false, false, false, false, false, false));
                }
            }

            template.add("attributes", attributes);

            List<String> dependenciesEnums = new ArrayList<>();
            for (Class c : this.dependenciasEnums) {
                Object[] enums = c.getEnumConstants();
                String value = "";
                value = "$scope.value" + c.getSimpleName() + " = [";
                boolean first = true;
                int index = 0;

                for (Field field : c.getFields()) {
                    if (first) {
                        first = false;
                        value += "{value:'" + field.getName() + "', label:'" + enums[index++] + "'}";
                    } else {
                        value += ", {value:'" + field.getName() + "', label:'" + enums[index++] + "'}";
                    }
                }

                value += "]";
                dependenciesEnums.add(value);
            }
            template.add("dependenciesEnums", dependenciesEnums);

            List<String> oneToManys = new ArrayList<>();

            for (Field field : Util.getTodosAtributosMenosIdAutomatico(this.classeEntidade)) {
                if (field.isAnnotationPresent(OneToMany.class)) {
                    oneToManys.add(field.getName().toLowerCase());
                }
            }

            template.add("oneToManys", oneToManys);

            template.generateTemplate(this.pastaControllers + "/" + this.nomeEntidade + "FormController.js");

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void geraServices() {
        try {
            ConfigurationFreeMarker config = new ConfigurationFreeMarker();
            TemplateFreeMarker template = new TemplateFreeMarker(isWebpack ? "moduleServicePresentationWebpack.ftl" : "moduleServicePresentation.ftl", config);
            template.add("entityName", this.nomeEntidade);
            template.add("entityNameLowerCase", this.nomeEntidade.toLowerCase());

            template.generateTemplate(this.pastaServices + "/module.js");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            ConfigurationFreeMarker config = new ConfigurationFreeMarker();
            TemplateFreeMarker template = new TemplateFreeMarker(isWebpack ? "servicePresentationWebpack.ftl" : "servicePresentation.ftl", config);
            template.add("entityName", this.nomeEntidade);
            template.add("entityNameLowerCase", this.nomeEntidade.toLowerCase());

            template.generateTemplate(this.pastaServices + "/" + this.nomeEntidade + "Service.js");

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void geraViews() {
//		try {
//
//			ConfigurationFreeMarker config = new ConfigurationFreeMarker();
//			TemplateFreeMarker template = new TemplateFreeMarker("baseViewPresentation.ftl", config);
//			template.add("entityName", this.nomeEntidade);
//			template.add("entityNameLowerCase", this.nomeEntidade.toLowerCase());
//
//			template.generateTemplate(this.pastaViews + "/base.html");
//
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
        try {
            ConfigurationFreeMarker config = new ConfigurationFreeMarker();
            TemplateFreeMarker template = new TemplateFreeMarker("formViewPresentation.ftl", config);
            template.add("entityName", this.nomeEntidade);
            template.add("entityNameLowerCase", this.nomeEntidade.toLowerCase());
            List<AttributePresentation> attributes = new ArrayList<>();
            generateFields(attributes, this.classeEntidade);
            template.add("attributes", attributes);

            template.generateTemplate(this.pastaViews + "/form.html");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            ConfigurationFreeMarker config = new ConfigurationFreeMarker();
            TemplateFreeMarker template = new TemplateFreeMarker("listViewPresentation.ftl", config);
            template.add("entityName", this.nomeEntidade);
            template.add("entityNameLowerCase", this.nomeEntidade.toLowerCase());
            template.add("field", Util.todosAtributosSeparadosPorVirgula(this.classeEntidade));

            List<Attribute> attributesSearchField = new ArrayList<>();
            List<Attribute> attributesAdvancedSearchField = new ArrayList<>();
            for (Field field : Util.getTodosAtributosNaoEstaticos(classeEntidade)) {
                if (!field.getName().equalsIgnoreCase("id")) {
                    attributesSearchField.add(new Attribute(field.getName(), converteTipoParaAdvanced(field.getType()), field.getName().toLowerCase(), false, false, false, false, false, false, false));
                    attributesAdvancedSearchField.add(new Attribute(field.getName(), converteTipoParaAdvanced(field.getType()), field.getName().toLowerCase(), false, false, false, false, false, false, false));
                }
            }
            template.add("attributesSearchField", attributesSearchField);
            template.add("attributesAdvancedSearchField", attributesAdvancedSearchField);

            template.generateTemplate(this.pastaViews + "/list.html");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        for (Class classe : dependenciasOneToMany) {
            try {

                ConfigurationFreeMarker config = new ConfigurationFreeMarker();
                TemplateFreeMarker template = new TemplateFreeMarker("modalViewPresentation.ftl", config);
                template.add("entityNameLowerCase", classe.getSimpleName().toLowerCase());
                template.add("entityName", this.nomeEntidade.toLowerCase());
                List<AttributePresentation> attributes = new ArrayList<>();
                generateFields(attributes, classe);
                template.add("attributes", attributes);

                template.generateTemplate(this.pastaViews + "/modal" + classe.getSimpleName() + ".html");

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void generateFields(List<AttributePresentation> attributes, Class entity) {
        for (Field field : Util.getTodosAtributosMenosIdAutomatico(entity)) {

            String type = getAttributeType(field);
            String typeField = "";
            String fieldName = "";
            String fieldSimpleNameLowerCase = "";
            String entitySimpleNameLowerCase = "";
            String firstAttributeOfField = "";
            String typeGenericSimpleNameOfFieldLowerCase = "";
            String typeGenericNameOfField = "";
            String required = "";
            String firstAttributeTypeGeneric = "";
            String firstAttributeTypeGenericNameLowerCase = "";
            String typeGenericSimpleNameOfField = "";
            String declaringClassSimpleNameLowerCase = "";
            String entityNameLowerCase = "";
            String opened = "";
            String tag = "";

            switch (type) {
                case "manyToOne":
                    typeField = type;
                    fieldName = field.getName();
                    entitySimpleNameLowerCase = this.classeEntidade.getSimpleName().toLowerCase();
                    fieldSimpleNameLowerCase = field.getType().getSimpleName().toLowerCase();
                    firstAttributeOfField = Util.primeiroAtributo(field.getType()).getName();
                    break;
                case "oneToOne":
                    typeField = type;
                    fieldName = field.getName();
                    entitySimpleNameLowerCase = this.classeEntidade.getSimpleName().toLowerCase();
                    fieldSimpleNameLowerCase = field.getType().getSimpleName().toLowerCase();
                    firstAttributeOfField = Util.primeiroAtributo(field.getType()).getName();
                    break;
                case "gumgaEMail":
                    typeField = type;
                    fieldName = field.getName();
                    entitySimpleNameLowerCase = this.classeEntidade.getSimpleName().toLowerCase();
                    required = geraValidacoesDoBenValidator(field);
                    break;
                case "manyToMany":
                    typeField = type;
                    fieldName = field.getName();
                    entitySimpleNameLowerCase = this.classeEntidade.getSimpleName().toLowerCase();
                    typeGenericSimpleNameOfFieldLowerCase = Util.getTipoGenerico(field).getSimpleName().toLowerCase();
                    firstAttributeTypeGeneric = Util.primeiroAtributo(Util.getTipoGenerico(field)).getName();
                    break;
                case "oneToMany":
                    typeField = type;
                    fieldName = field.getName();
                    entitySimpleNameLowerCase = this.classeEntidade.getSimpleName().toLowerCase();
                    typeGenericSimpleNameOfField = Util.getTipoGenerico(field).getSimpleName();
                    declaringClassSimpleNameLowerCase = field.getDeclaringClass().getSimpleName().toLowerCase();
                    firstAttributeTypeGenericNameLowerCase = Util.primeiroAtributo(Util.getTipoGenerico(field)).getName().toLowerCase();
                    break;
                case "gumgaCustomFields":
                    typeField = type;
                    entitySimpleNameLowerCase = this.classeEntidade.getSimpleName().toLowerCase();
                    break;
                case "gumgaAddress":
                    typeField = type;
                    fieldName = field.getName();
                    entitySimpleNameLowerCase = this.classeEntidade.getSimpleName().toLowerCase();
                    break;
                case "gumgaBarCode":
                    typeField = type;
                    fieldName = field.getName();
                    entitySimpleNameLowerCase = this.classeEntidade.getSimpleName().toLowerCase();
                    required = geraValidacoesDoBenValidator(field);
                    break;
                case "gumgaCEP":
                    typeField = type;
                    fieldName = field.getName();
                    entitySimpleNameLowerCase = this.classeEntidade.getSimpleName().toLowerCase();
                    required = geraValidacoesDoBenValidator(field);
                    break;
                case "gumgaCNPJ":
                    typeField = type;
                    fieldName = field.getName();
                    entitySimpleNameLowerCase = this.classeEntidade.getSimpleName().toLowerCase();
                    required = geraValidacoesDoBenValidator(field);
                    break;
                case "gumgaCPF":
                    typeField = type;
                    fieldName = field.getName();
                    entitySimpleNameLowerCase = this.classeEntidade.getSimpleName().toLowerCase();
                    required = geraValidacoesDoBenValidator(field);
                    break;
                case "gumgaIP4":
                    typeField = type;
                    fieldName = field.getName();
                    entitySimpleNameLowerCase = this.classeEntidade.getSimpleName().toLowerCase();
                    required = geraValidacoesDoBenValidator(field);
                    break;
                case "gumgaIP6":
                    typeField = type;
                    fieldName = field.getName();
                    entitySimpleNameLowerCase = this.classeEntidade.getSimpleName().toLowerCase();
                    required = geraValidacoesDoBenValidator(field);
                    break;
                case "gumgaFile":
                    break;
                case "gumgaImage":
                    typeField = type;
                    fieldName = field.getName();
                    entitySimpleNameLowerCase = this.classeEntidade.getSimpleName().toLowerCase();
                    entityNameLowerCase = this.nomeEntidade.toLowerCase();
                    break;
                case "gumgaMoney":
                    typeField = type;
                    fieldName = field.getName();
                    entitySimpleNameLowerCase = this.classeEntidade.getSimpleName().toLowerCase();
                    required = geraValidacoesDoBenValidator(field);
                    break;
                case "gumgaPhoneNumber":
                    typeField = type;
                    fieldName = field.getName();
                    entitySimpleNameLowerCase = this.classeEntidade.getSimpleName().toLowerCase();
                    required = geraValidacoesDoBenValidator(field);
                    break;
                case "gumgaURL":
                    typeField = type;
                    fieldName = field.getName();
                    entitySimpleNameLowerCase = this.classeEntidade.getSimpleName().toLowerCase();
                    required = geraValidacoesDoBenValidator(field);
                    break;
                case "gumgaTime":
                    typeField = type;
                    fieldName = field.getName();
                    entitySimpleNameLowerCase = this.classeEntidade.getSimpleName().toLowerCase();
                    break;
                case "gumgaMultiLineString":
                    typeField = type;
                    fieldName = field.getName();
                    entitySimpleNameLowerCase = this.classeEntidade.getSimpleName().toLowerCase();
                    required = geraValidacoesDoBenValidator(field);
                    tag = Util.etiqueta(field);
                    break;
                case "gumgaGeoLocation":
                    typeField = type;
                    fieldName = field.getName();
                    entitySimpleNameLowerCase = this.classeEntidade.getSimpleName().toLowerCase();
                    required = geraValidacoesDoBenValidator(field);
                    break;
                case "gumgaBoolean":
                    typeField = type;
                    fieldName = field.getName();
                    entitySimpleNameLowerCase = this.classeEntidade.getSimpleName().toLowerCase();
                    required = geraValidacoesDoBenValidator(field);
                    break;
                case "date":
                    typeField = type;
                    fieldName = field.getName();
                    entitySimpleNameLowerCase = this.classeEntidade.getSimpleName().toLowerCase();
                    required = geraValidacoesDoBenValidator(field);
                    opened = "opened" + Util.primeiraMaiuscula(field.getName());
                    break;
                case "enum":
                    typeField = type;
                    fieldName = field.getName();
                    fieldSimpleNameLowerCase = field.getType().getSimpleName();
                    entitySimpleNameLowerCase = this.classeEntidade.getSimpleName().toLowerCase();
                    required = geraValidacoesDoBenValidator(field);
                    break;
                default:
                    typeField = type;
                    fieldName = field.getName();
                    entitySimpleNameLowerCase = this.classeEntidade.getSimpleName().toLowerCase();
                    required = geraValidacoesDoBenValidator(field);
                    break;
            }

            if (typeField != "") {
                attributes.add(AttributePresentation
                        .create()
                        .withType(typeField)
                        .withFieldName(fieldName)
                        .withFieldSimpleNameLowerCase(fieldSimpleNameLowerCase)
                        .withEntitySimpleNameLowerCase(entitySimpleNameLowerCase)
                        .withFirstAttributeOfField(firstAttributeOfField)
                        .withTypeGenericNameOfField(typeGenericNameOfField)
                        .withTypeGenericSimpleNameOfFieldLowerCase(typeGenericSimpleNameOfFieldLowerCase)
                        .withRequired(required)
                        .withFirstAttributeTypeGeneric(firstAttributeTypeGeneric)
                        .withFirstAttributeTypeGenericNameLowerCase(firstAttributeTypeGenericNameLowerCase)
                        .withTypeGenericSimpleNameOfField(typeGenericSimpleNameOfField)
                        .withDeclaringClassSimpleNameLowerCase(declaringClassSimpleNameLowerCase)
                        .withEntityNameLowerCase(entityNameLowerCase)
                        .withOpened(opened)
                        .withTag(tag)
                        .build());
            }
        }
    }

    public String getAttributeType(Field field) {

        return field.isAnnotationPresent(ManyToOne.class) ? "manyToOne"
                : field.isAnnotationPresent(OneToOne.class) ? "oneToOne"
                : field.isAnnotationPresent(ManyToMany.class) ? "manyToMany"
                : field.isAnnotationPresent(OneToMany.class) ? "oneToMany"
                : "gumgaCustomFields".equals(field.getName()) ? "gumgaCustomFields"
                : GumgaAddress.class.equals(field.getType()) ? "gumgaAddress"
                : GumgaBarCode.class.equals(field.getType()) ? "gumgaBarCode"
                : GumgaCEP.class.equals(field.getType()) ? "gumgaCEP"
                : GumgaCNPJ.class.equals(field.getType()) ? "gumgaCNPJ"
                : GumgaCPF.class.equals(field.getType()) ? "gumgaCPF"
                : GumgaIP4.class.equals(field.getType()) ? "gumgaIP4"
                : GumgaIP6.class.equals(field.getType()) ? "gumgaIP6"
                : GumgaFile.class.equals(field.getType()) ? "gumgaFile"
                : GumgaImage.class.equals(field.getType()) ? "gumgaImage"
                : GumgaEMail.class.equals(field.getType()) ? "gumgaEMail"
                : GumgaMoney.class.equals(field.getType()) ? "gumgaMoney"
                : GumgaPhoneNumber.class.equals(field.getType()) ? "gumgaPhoneNumber"
                : GumgaURL.class.equals(field.getType()) ? "gumgaURL"
                : GumgaTime.class.equals(field.getType()) ? "gumgaTime"
                : GumgaMultiLineString.class.equals(field.getType()) ? "gumgaMultiLineString"
                : GumgaGeoLocation.class.equals(field.getType()) ? "gumgaGeoLocation"
                : GumgaBoolean.class.equals(field.getType()) ? "gumgaBoolean"
                : Date.class.equals(field.getType()) ? "date"
                : field.getType().isEnum() ? "enum" : " ";

    }

    public static class AttributePresentation {

        private String type;
        private String fieldName;
        private String fieldSimpleNameLowerCase;
        private String entitySimpleNameLowerCase;
        private String firstAttributeOfField;
        private String typeGenericSimpleNameOfFieldLowerCase;
        private String typeGenericNameOfField;
        private String firstAttributeTypeGeneric;
        private String required;
        private String firstAttributeTypeGenericNameLowerCase;
        private String typeGenericSimpleNameOfField;
        private String declaringClassSimpleNameLowerCase;
        private String entityNameLowerCase;
        private String opened;
        private String tag;

        public String getType() {
            return type;
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getFieldSimpleNameLowerCase() {
            return fieldSimpleNameLowerCase;
        }

        public String getEntitySimpleNameLowerCase() {
            return entitySimpleNameLowerCase;
        }

        public String getFirstAttributeOfField() {
            return firstAttributeOfField;
        }

        public String getTypeGenericSimpleNameOfFieldLowerCase() {
            return typeGenericSimpleNameOfFieldLowerCase;
        }

        public String getTypeGenericNameOfField() {
            return typeGenericNameOfField;
        }

        public String getRequired() {
            return required;
        }

        public String getFirstAttributeTypeGeneric() {
            return firstAttributeTypeGeneric;
        }

        public String getFirstAttributeTypeGenericNameLowerCase() {
            return firstAttributeTypeGenericNameLowerCase;
        }

        public String getTypeGenericSimpleNameOfField() {
            return typeGenericSimpleNameOfField;
        }

        public String getDeclaringClassSimpleNameLowerCase() {
            return declaringClassSimpleNameLowerCase;
        }

        public String getEntityNameLowerCase() {
            return entityNameLowerCase;
        }

        public String getOpened() {
            return opened;
        }

        public String getTag() {
            return tag;
        }

        public static Builder create() {
            return new Builder();
        }

        private static class Builder {

            private AttributePresentation attribute;

            public Builder() {
                this.attribute = new AttributePresentation();
            }

            public Builder withOpened(String opened) {
                this.attribute.opened = opened;
                return this;
            }

            public Builder withEntityNameLowerCase(String entityNameLowerCase) {
                this.attribute.entityNameLowerCase = entityNameLowerCase;
                return this;
            }

            public Builder withType(String type) {
                this.attribute.type = type;
                return this;
            }

            public Builder withFieldName(String fieldName) {
                this.attribute.fieldName = fieldName;
                return this;
            }

            public Builder withFieldSimpleNameLowerCase(String fieldSimpleNameLowerCase) {
                this.attribute.fieldSimpleNameLowerCase = fieldSimpleNameLowerCase;
                return this;
            }

            public Builder withEntitySimpleNameLowerCase(String entitySimpleNameLowerCase) {
                this.attribute.entitySimpleNameLowerCase = entitySimpleNameLowerCase;
                return this;
            }

            public Builder withFirstAttributeOfField(String firstAttributeOfField) {
                this.attribute.firstAttributeOfField = firstAttributeOfField;
                return this;
            }

            public Builder withTypeGenericSimpleNameOfFieldLowerCase(String typeGenericSimpleNameOfFieldLowerCase) {
                this.attribute.typeGenericSimpleNameOfFieldLowerCase = typeGenericSimpleNameOfFieldLowerCase;
                return this;
            }

            public Builder withTypeGenericNameOfField(String typeGenericNameOfField) {
                this.attribute.typeGenericNameOfField = typeGenericNameOfField;
                return this;
            }

            public Builder withRequired(String required) {
                this.attribute.required = required;
                return this;
            }

            public Builder withFirstAttributeTypeGeneric(String firstAttributeTypeGeneric) {
                this.attribute.firstAttributeTypeGeneric = firstAttributeTypeGeneric;
                return this;
            }

            public Builder withFirstAttributeTypeGenericNameLowerCase(String firstAttributeTypeGenericNameLowerCase) {
                this.attribute.firstAttributeTypeGenericNameLowerCase = firstAttributeTypeGenericNameLowerCase;
                return this;
            }

            public Builder withTypeGenericSimpleNameOfField(String typeGenericSimpleNameOfField) {
                this.attribute.typeGenericSimpleNameOfField = typeGenericSimpleNameOfField;
                return this;
            }

            public Builder withDeclaringClassSimpleNameLowerCase(String declaringClassSimpleNameLowerCase) {
                this.attribute.declaringClassSimpleNameLowerCase = declaringClassSimpleNameLowerCase;
                return this;
            }

            public Builder withTag(String tag) {
                this.attribute.tag = tag;
                return this;
            }

            public AttributePresentation build() {
                return this.attribute;
            }

        }

    }

    public void geraCampos(FileWriter fw, Class classeEntidade) throws IOException {
        boolean primeiro;
        for (Field atributo : Util.getTodosAtributosMenosIdAutomatico(classeEntidade)) {
            //COLOCAR OS TIPOS

            if (atributo.isAnnotationPresent(ManyToOne.class) || atributo.isAnnotationPresent(OneToOne.class)) {
                fw.write(""
                        + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                        + Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                        + Util.IDENTACAO12 + "<gumga-many-to-one\n"
                        + Util.IDENTACAO16 + "input-name=\"" + atributo.getName() + "\"\n"
                        + Util.IDENTACAO16 + "value=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + "\"\n"
                        + Util.IDENTACAO16 + "search-method=\"" + atributo.getType().getSimpleName().toLowerCase() + ".methods.asyncSearch('" + Util.primeiroAtributo(atributo.getType()).getName() + "',param)\"\n"
                        + Util.IDENTACAO16 + "field=\"" + Util.primeiroAtributo(atributo.getType()).getName() + "\"\n"
                        + Util.IDENTACAO16 + "authorize-add=\"true\"\n"
                        + Util.IDENTACAO16 + "add-method=\"" + atributo.getType().getSimpleName().toLowerCase() + ".methods.asyncPost(value,'" + Util.primeiroAtributo(atributo.getType()).getName() + "')\">\n"
                        + Util.IDENTACAO12 + "</gumga-many-to-one>\n"
                        + Util.IDENTACAO08 + "</div>\n");

            } else if (atributo.isAnnotationPresent(ManyToMany.class
            )) {
                fw.write(
                        ""
                                + "        <div class=\"col-md-6\">\n"
                                + "            <label for=\"" + atributo.getName() + "\"  gumga-translate-tag=\"" + Util.getTipoGenerico(atributo).getSimpleName().toLowerCase() + ".title\"></label>\n"
                                + "        </div>\n"
                                + "        <div class=\"col-md-6\">\n"
                                + "            <label for=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\" gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\"></label>\n"
                                + "        </div>"
                                + "\n");

                fw.write(Util.IDENTACAO08
                        + "<div class=\"full-width-without-padding\">\n"
                        + Util.IDENTACAO04 + Util.IDENTACAO08 + "<gumga-many-to-many \n"
                        + Util.IDENTACAO04 + Util.IDENTACAO04 + Util.IDENTACAO08 + "left-list=\"" + Util.getTipoGenerico(atributo).getSimpleName().toLowerCase() + ".data\" \n"
                        + Util.IDENTACAO04 + Util.IDENTACAO04 + Util.IDENTACAO08 + "right-list=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + "\" \n"
                        //+ Util.IDENTACAO04 + Util.IDENTACAO04 + Util.IDENTACAO08 + "left-search=\"" + Util.getTipoGenerico(atributo).getSimpleName().toLowerCase() + ".methods.search('" + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + "',param)\" \n"

                        + Util.IDENTACAO04 + Util.IDENTACAO04 + Util.IDENTACAO08 + "left-search=\"" + Util.getTipoGenerico(atributo).getSimpleName().toLowerCase() + ".methods.advancedSearch('obj." + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + " like\\''+param+'%\\'')\"\n"
                        + Util.IDENTACAO04 + Util.IDENTACAO04 + Util.IDENTACAO08 + "filter-parameters=\"" + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + "\"\n"
                        + Util.IDENTACAO04 + Util.IDENTACAO04 + Util.IDENTACAO08 + "post-method=\"" + Util.getTipoGenerico(atributo).getSimpleName().toLowerCase() + ".methods.save(value)\"\n"
                        + Util.IDENTACAO04 + Util.IDENTACAO04 + Util.IDENTACAO08 + "authorize-add=\"true\">\n"
                        + Util.IDENTACAO04 + Util.IDENTACAO04 + Util.IDENTACAO08 + "    <left-field>{{$value." + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + "}}</left-field>\n"
                        + Util.IDENTACAO04 + Util.IDENTACAO04 + Util.IDENTACAO08 + "    <right-field>{{$value." + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + "}}</right-field>\n"
                        + Util.IDENTACAO04 + Util.IDENTACAO08 + "</gumga-many-to-many>\n\n"
                        + Util.IDENTACAO08 + "</div>\n"
                        + "");

            } else if (atributo.isAnnotationPresent(OneToMany.class
            )) {
                fw.write(
                        "<div class=\"full-width-without-padding\">");
                fw.write(Util.IDENTACAO04
                        + Util.IDENTACAO04 + "<label for=\"" + atributo.getName() + "\"  gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\"></label>\n");
                fw.write(
                        "<gumga-one-to-many\n"
                                + "     children=\"entity." + atributo.getName() + "\"\n"
                                + "     template-url=\"app/modules/" + atributo.getDeclaringClass().getSimpleName().toLowerCase() + "/views/modal" + Util.getTipoGenerico(atributo).getSimpleName() + ".html\"\n"
                                + "     displayable-property=\"" + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName().toLowerCase() + "\"\n"
                                + "     controller=\"Modal" + Util.getTipoGenerico(atributo).getSimpleName() + "Controller\">"
                                + "</gumga-one-to-many>\n"
                                + "</div>"
                                + "\n");

            } else if ("gumgaCustomFields".equals(atributo.getName())) {
                fw.write(""
                        + Util.IDENTACAO08 + "\n<gumga-custom-fields fields=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data\"></gumga-custom-fields>\n\n"
                );
            } else if (GumgaAddress.class.equals(atributo.getType())) {
                fw.write(""
                        + Util.IDENTACAO08 + "<div class=\"row\">\n"
                        + Util.IDENTACAO12 + "<div class=\"col-md-12\">\n"
                        + Util.IDENTACAO12 + Util.IDENTACAO04 + "<gumga-address name=\"" + atributo.getName() + "\" value=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + "\"> </gumga-address>\n"
                        + Util.IDENTACAO12 + "</div>\n"
                        + Util.IDENTACAO08 + "</div>\n"
                        + "\n");
            } else if (GumgaBarCode.class.equals(atributo.getType())) {
                fw.write(""
                        + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                        + Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                        + Util.IDENTACAO12 + "<input id=\"" + atributo.getName() + "\" gumga-error  type=\"text\" name=\"" + atributo.getName() + "\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".value\" class=\"form-control\" />\n"
                        + Util.IDENTACAO08 + "</div>\n"
                        + "\n");
            } else if (GumgaCEP.class.equals(atributo.getType())) { //TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR A DEPENDENCIA EXTERNA
                fw.write(""
                        + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                        + Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                        + Util.IDENTACAO12 + "<input id=\"" + atributo.getName() + "\" gumga-mask=\"99999-999\" gumga-error type=\"text\" name=\"" + atributo.getName() + "\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".value\" class=\"form-control\"/>\n"
                        + Util.IDENTACAO08 + "</div>\n"
                        + "\n");
            } else if (GumgaCNPJ.class.equals(atributo.getType())) { //TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR A DEPENDENCIA EXTERNA
                fw.write(""
                        + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                        + Util.IDENTACAO12 + "        <input id=\"" + atributo.getName() + "\" gumga-mask=\"99.999.999/9999-99\" gumga-error type=\"text\" name=\"" + atributo.getName() + "\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".value\" class=\"form-control\"  />\n"
                        + Util.IDENTACAO08 + "        </div>"
                        + "\n");
            } else if (GumgaCPF.class.equals(atributo.getType())) { //TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR A DEPENDENCIA EXTERNA
                fw.write(""
                        + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                        + Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                        + Util.IDENTACAO12 + "<input id=\"" + atributo.getName() + "\" gumga-mask=\"999.999.999-99\" gumga-error type=\"text\" name=\"" + atributo.getName() + "\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".value\" class=\"form-control\" />\n"
                        + Util.IDENTACAO08 + "</div>\n"
                        + "\n");
            } else if (GumgaIP4.class.equals(atributo.getType())) { //TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR O GUMGAMAX E GUMGAMIN
                fw.write(""
                        + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                        + Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                        + Util.IDENTACAO12 + "<input id=\"" + atributo.getName() + "\" gumga-error type=\"text\" name=\"" + atributo.getName() + "\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".value\" class=\"form-control\" gumga-min=\"12\" gumga-max=\"12\"/>\n"
                        + Util.IDENTACAO08 + "</div>\n"
                        + "\n");
            } else if (GumgaIP6.class.equals(atributo.getType())) { //TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR O GUMGAMAX E GUMGAMIN
                fw.write(""
                        + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                        + Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                        + Util.IDENTACAO12 + "<input id=\"" + atributo.getName() + "\" gumga-error type=\"text\" name=\"" + atributo.getName() + "\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".value\" class=\"form-control\" gumga-min=\"12\" gumga-max=\"12\"/>\n"
                        + Util.IDENTACAO08 + "</div>\n"
                        + "\n");
            } else if (GumgaFile.class.equals(atributo.getType())) {//TODO SUBSTITUIR PELA DIRETIVA DE IMPORTAÇÃO E ARQUIVO QUANDO ESTIVER PRONTA

            } else if (GumgaImage.class.equals(atributo.getType())) {
                fw.write(""
                        + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                        + Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                        + Util.IDENTACAO12 + "<gumga-upload attribute=\"" + nomeEntidade.toLowerCase() + ".data." + atributo.getName() + "\"\n"
                        + Util.IDENTACAO16 + "upload-method=\"" + nomeEntidade.toLowerCase() + ".methods.postImage(image)\"\n"
                        + Util.IDENTACAO16 + "delete-method=\"" + nomeEntidade.toLowerCase() + ".methods.deleteImage(image)\">\n"
                        + Util.IDENTACAO12 + "</gumga-upload>\n"
                        + Util.IDENTACAO08 + "</div>\n"
                        + "\n");

            } else if (GumgaEMail.class.equals(atributo.getType())) {
                fw.write(""
                        + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                        + Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                        + Util.IDENTACAO12 + "<input id=\"" + atributo.getName() + "\" gumga-error type=\"email\" name=\"" + atributo.getName() + "\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".value\" class=\"form-control\" />\n"
                        + Util.IDENTACAO08 + "</div>\n"
                        + "\n");
            } else if (GumgaMoney.class.equals(atributo.getType())) {//TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR A DEPENDENCIA EXTERNA
                fw.write(""
                        + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                        + Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                        + Util.IDENTACAO12 + "<input gumga-error type=\"text\" name=\"" + atributo.getName() + "\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".value\" class=\"form-control\" ui-money-mask=\"2\"/>\n"
                        + Util.IDENTACAO08 + "</div>\n"
                        + "\n");
            } else if (GumgaPhoneNumber.class.equals(atributo.getType())) {//TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR A DEPENDENCIA EXTERNA
                fw.write(""
                        + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                        + Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                        + Util.IDENTACAO12 + "<input id=\"" + atributo.getName() + "\" gumga-error type=\"text\" name=\"" + atributo.getName() + "\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".value\" class=\"form-control\" ui-br-phone-number/>\n"
                        + Util.IDENTACAO08 + "</div>\n"
                        + "\n");
            } else if (GumgaURL.class.equals(atributo.getType())) {
                fw.write(""
                        + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                        + Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                        + Util.IDENTACAO12 + "<input gumga-error type=\"url\" name=\"" + atributo.getName() + "\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".value\" class=\"form-control\" />\n"
                        + Util.IDENTACAO08 + "</div>\n"
                        + "\n");
            } else if (GumgaTime.class.equals(atributo.getType())) {
                fw.write(""
                        + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                        + Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                        + Util.IDENTACAO12 + "<timepicker ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".value\"  show-meridian=\"false\"></timepicker>\n"
                        + Util.IDENTACAO08 + "</div>"
                        + "");
            } else if (GumgaMultiLineString.class.equals(atributo.getType())) {
                fw.write(""
                        + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                        + Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                        + Util.IDENTACAO12 + "<textarea ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".value\" " + geraValidacoesDoBenValidator(atributo) + " class=\"form-control\" placeholder=\"Digite " + Util.etiqueta(atributo) + ".\" rows=\"4\" cols=\"50\"></textarea>\n\n"
                        + Util.IDENTACAO08 + "</div>"
                        + "\n");
            } else if (GumgaGeoLocation.class.equals(atributo.getType())) { //TODO SUBSTITUIR PELO COMPONENTE GUMGAMAPS QUANDO ELE ESTIVER PRONTO
                fw.write(""
                        + Util.IDENTACAO04 + "<div class=\"row\">\n"
                        + Util.IDENTACAO08 + "<div class=\"col-md-12\">\n"
                        + Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                        + Util.IDENTACAO12 + "<div gumga-form-class=\"" + atributo.getName() + "latitude\">\n"
                        + Util.IDENTACAO16 + "<label gumga-translate-tag=\"gumga.latitude\">Latitude</label>\n"
                        + Util.IDENTACAO16 + "<input gumga-error type=\"text\" name=\"" + atributo.getName() + "latitude\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".latitude\" class=\"form-control\" />\n"
                        + Util.IDENTACAO12 + "</div>\n"
                        + Util.IDENTACAO12 + "<div gumga-form-class=\"" + atributo.getName() + "longitude\">\n"
                        + Util.IDENTACAO16 + "<label gumga-translate-tag=\"gumga.longitude\">Longitude</label>\n"
                        + Util.IDENTACAO16 + "<input gumga-error type=\"text\" name=\"" + atributo.getName() + "longitude\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".longitude\" class=\"form-control\" />\n"
                        + Util.IDENTACAO12 + "</div>\n"
                        + Util.IDENTACAO12 + "<a class=\"btn btn-default\" ng-href=\"http://maps.google.com/maps?q={{" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".latitude + ',' + " + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".longitude}}\" target=\"_blank\"> <p class=\"glyphicon glyphicon-globe\"></p> GOOGLE MAPS</a>\n"
                        + Util.IDENTACAO08 + "</div>\n"
                        + Util.IDENTACAO04 + "</div>\n"
                        + "\n");
            } else if (GumgaBoolean.class.equals(atributo.getType())) {
                fw.write(""
                        + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                        + Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                        + Util.IDENTACAO12 + "<input style=\"width:15px\" gumga-error type=\"checkbox\" " + geraValidacoesDoBenValidator(atributo) + " name=\"" + atributo.getName() + "\" ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".value\" class=\"form-control\" />\n"
                        + Util.IDENTACAO08 + "</div>\n"
                        + "\n");
            } else if (Date.class.equals(atributo.getType())) {
                String varOpened = "opened" + Util.primeiraMaiuscula(atributo.getName());
                fw.write(""
                        + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                        + Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                        + Util.IDENTACAO12 + "<input type=\"text\" name=\"" + atributo.getName() + "\" class=\"form-control\" " + geraValidacoesDoBenValidator(atributo) + " datepicker-popup=\"fullDate\" ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + "\" is-open=\"" + varOpened + "\" ng-click=\"" + varOpened + "= !" + varOpened + "\" close-text=\"Close\" />\n"
                        + Util.IDENTACAO08 + "</div>\n"
                        + "\n");

            } else if (atributo.getType().isEnum()) {
                Object[] constants = atributo.getType().getEnumConstants();
                fw.write(Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n");
                fw.write(Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n");
                fw.write(Util.IDENTACAO12 + "<select class='form-control' gumga-error name=\"" + atributo.getName() + "\" ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + "\" >\n");
                fw.write(Util.IDENTACAO12 + "  <option ng-selected=\"value.value === entity." + atributo.getName() + "\"  value=\"{{value.value}}\" ng-repeat=\"value in value" + atributo.getType().getSimpleName() + "\">{{value.label}}</option>\n");
                fw.write(Util.IDENTACAO12 + "</select>\n");
                fw.write(Util.IDENTACAO08 + "</div>\n");
            } else {
                fw.write(""
                        + Util.IDENTACAO04 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                        + Util.IDENTACAO08 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                        + Util.IDENTACAO08 + "<input gumga-error type=\"text\" name=\"" + atributo.getName() + "\" ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + "\"" + geraValidacoesDoBenValidator(atributo) + "  class=\"form-control\" />\n"
                        + Util.IDENTACAO04 + "</div>\n\n"
                );

            }
            primeiro = false;
        }
    }

    private String geraValidacoesDoBenValidator(Field atributo) {
        String aRetornar = "";

        /*
         ConstraintComposition 	Boolean operator that is applied to all constraints of a composing constraint annotation.
         CreditCardNumber 	The annotated element has to represent a valid credit card number.
         CreditCardNumber.List 	Defines several @CreditCardNumber annotations on the same element.
         Email 	The string has to be a well-formed email address.
         Email.List 	Defines several @Email annotations on the same element.
         Length 	Validate that the string is between min and max included.
         Length.List 	Defines several @Length annotations on the same element.
         NotBlank 	Validate that the annotated string is not null or empty.
         NotBlank.List 	Defines several @NotBlank annotations on the same element.
         NotEmpty 	Asserts that the annotated string, collection, map or array is not null or empty.
         NotEmpty.List 	Defines several @NotEmpty annotations on the same element.
         Range 	The annotated element has to be in the appropriate range.
         Range.List 	Defines several @Range annotations on the same element.
         SafeHtml 	Validate a rich text value provided by the user to ensure that it contains no malicious code, such as embedded <script> elements.
         SafeHtml.List 	Defines several @WebSafe annotations on the same element.
         ScriptAssert 	A class-level constraint, that evaluates a script expression against the annotated element.
         ScriptAssert.List 	Defines several @ScriptAssert annotations on the same element.
         URL 	Validate that the string is a valid URL.
         URL.List 	Defines several @URL annotations on the same element.
         */
        if (atributo.isAnnotationPresent(NotNull.class
        )
                || atributo.isAnnotationPresent(NotEmpty.class
        )
                || atributo.isAnnotationPresent(NotBlank.class
        )) {
            if (!GumgaBoolean.class.equals(atributo.getType())) {
                aRetornar += " gumga-required ";
            }
        }

        return aRetornar;
    }

    private void geraModule() {
        try {
            ConfigurationFreeMarker config = new ConfigurationFreeMarker();
            TemplateFreeMarker template = new TemplateFreeMarker(isWebpack ? "modulePresentationWebpack.ftl" : "modulePresentation.ftl", config);
            template.add("entityName", this.nomeEntidade);
            template.add("entityNameLowerCase", this.nomeEntidade.toLowerCase());
            template.generateTemplate(this.pastaApp + "/module.js");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void geraI18n() {
        try {
            String texto = Util.IDENTACAO04 + ",\"" + nomeEntidade.toLowerCase() + "\":{\n"
                    + Util.IDENTACAO04 + Util.IDENTACAO04 + "\"title\":\"" + Util.primeiraMaiuscula(nomeEntidade) + "\"\n"
                    + Util.IDENTACAO04 + Util.IDENTACAO04 + ",\"menulabel\": \"" + Util.primeiraMaiuscula(nomeEntidade) + "\"\n"
                    + Util.IDENTACAO04 + Util.IDENTACAO04 + ",\"edit\": \"Editar " + Util.primeiraMaiuscula(nomeEntidade) + "\"\n"
                    + Util.IDENTACAO04 + Util.IDENTACAO04 + ",\"insert\": \"Inserir " + Util.primeiraMaiuscula(nomeEntidade) + "\"\n"
                    + Util.IDENTACAO04 + Util.IDENTACAO04 + ",\"list\": \"Consulta " + Util.primeiraMaiuscula(nomeEntidade) + "\"\n"
                    + Util.IDENTACAO04 + Util.IDENTACAO04 + ",\"id\": \"id\"\n";

            for (Field atributo : Util.getTodosAtributosMenosIdAutomatico(classeEntidade)) {
                texto += Util.IDENTACAO04 + Util.IDENTACAO04 + ",\"" + atributo.getName().toLowerCase() + "\":\"" + Util.primeiraMaiuscula(atributo.getName()) + "\"\n";
            }
            texto += Util.IDENTACAO04 + "}\n";
            Util.adicionaLinha(pastaI18n + "/pt-br.json", ",\"address\":", texto);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String converteTipoParaAdvanced(Class<?> type) {  //TODO OUTROS TIPOS JAVA
        if (type.equals(String.class
        )) {

            return "string";
        }

        if (type.equals(BigDecimal.class
        )) {

            return "number";
        }

        if (type.equals(Double.class
        )) {

            return "number";
        }

        if (type.equals(Integer.class
        )) {

            return "number";
        }

        if (type.equals(Long.class
        )) {

            return "number";
        }

        if (type.equals(Byte.class
        )) {

            return "number";
        }

        if (type.equals(Boolean.class
        )) {

            return "boolean";
        }

        if (type.equals(GumgaBoolean.class
        )) {

            return "boolean";
        }

        if (type.equals(Date.class
        )) {

            return "date";
        }

        if (type.equals(GumgaMoney.class
        )) {

            return "money";
        }
        return "string";
    }

}
