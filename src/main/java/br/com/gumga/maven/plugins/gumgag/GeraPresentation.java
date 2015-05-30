/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.gumga.maven.plugins.gumgag;

import gumga.framework.domain.domains.GumgaAddress;
import gumga.framework.domain.domains.GumgaBarCode;
import gumga.framework.domain.domains.GumgaBoolean;
import gumga.framework.domain.domains.GumgaCEP;
import gumga.framework.domain.domains.GumgaCNPJ;
import gumga.framework.domain.domains.GumgaCPF;
import gumga.framework.domain.domains.GumgaEMail;
import gumga.framework.domain.domains.GumgaFile;
import gumga.framework.domain.domains.GumgaGeoLocation;
import gumga.framework.domain.domains.GumgaIP4;
import gumga.framework.domain.domains.GumgaIP6;
import gumga.framework.domain.domains.GumgaImage;
import gumga.framework.domain.domains.GumgaMoney;
import gumga.framework.domain.domains.GumgaMultiLineString;
import gumga.framework.domain.domains.GumgaPhoneNumber;
import gumga.framework.domain.domains.GumgaTime;
import gumga.framework.domain.domains.GumgaURL;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
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

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Util.geraGumga(getLog());
        if (override) {
            System.out.println("NAO ADICIONANDO AO MENU NEM AO INTERNACIONALIZACAO");
        }

        try {
            nomeEntidade = nomeCompletoEntidade.substring(nomeCompletoEntidade.lastIndexOf('.') + 1);

            pastaApp = Util.windowsSafe(project.getFile().getParent()) + "/src/main/webapp/app/modules/" + (nomeEntidade.toLowerCase());

            getLog().info("Iniciando plugin Gerador de Html e JavaScript de Apresentação oi ");
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
            geraControllers();
            geraServices();
            geraViews();
            geraModule();
            if (!override) {
                geraI18n();
            }
            if (!override) {
                adicionaAoMenu();
            }
        } catch (Exception ex) {
            getLog().error(ex);
        }

    }

    private void adicionaAoMenu() throws IOException {

        Util.adicionaLinha(Util.windowsSafe(project.getFile().getParent()) + "/src/main/webapp/gumga-menu.json", "{", "    {\n"
                + "        \"label\": \"" + nomeEntidade + "\",\n"
                + "        \"URL\": \"" + nomeEntidade.toLowerCase() + ".list\",\n"
                + "        \"key\": \"CRUD-" + nomeEntidade + "\",\n"
                + "         \"icon\": \"glyphicon glyphicon-user\",\n"
                + "        \"filhos\": [\n"
                + "             {\n"
                + "             \"label\": \" Inserir \",\n"
                + "             \"URL\": \"" + nomeEntidade.toLowerCase() + ".insert\",\n"
                + "             \"key\": \"CRUD-" + nomeEntidade + "\",\n"
                + "             \"icon\": \"glyphicon glyphicon-user\",\n"
                + "             \"filhos\": []\n"
                + "             }\n"
                + "         ]\n"
                + "    },");

        Util.adicionaLinha(Util.windowsSafe(project.getFile().getParent()) + "/src/main/webapp/app/app.js", "//FIMROUTE", ""
                + Util.IDENTACAO4 + Util.IDENTACAO4 + ".state('" + nomeEntidade.toLowerCase() + "', {\n"
                + Util.IDENTACAO4 + Util.IDENTACAO4 + "data: {\n"
                + Util.IDENTACAO4 + Util.IDENTACAO4 + Util.IDENTACAO4 + "id: 1\n"
                + Util.IDENTACAO4 + Util.IDENTACAO4 + "}, \n"
                + Util.IDENTACAO4 + Util.IDENTACAO4 + Util.IDENTACAO4 + "url: '/" + nomeEntidade.toLowerCase() + "',\n"
                + Util.IDENTACAO4 + Util.IDENTACAO4 + Util.IDENTACAO4 + "templateUrl: 'app/modules/" + nomeEntidade.toLowerCase() + "/views/base.html'\n"
                + Util.IDENTACAO4 + Util.IDENTACAO4 + "})\n"
                + "");

        Util.adicionaLinha(Util.windowsSafe(project.getFile().getParent()) + "/src/main/webapp/app/app.js", "//FIMREQUIRE", Util.IDENTACAO4 + "require('app/modules/" + nomeEntidade.toLowerCase() + "/module');");

        Util.adicionaLinha(Util.windowsSafe(project.getFile().getParent()) + "/src/main/webapp/app/app.js", "//FIMINJECTIONS", Util.IDENTACAO4 + Util.IDENTACAO4 + ",'app." + nomeEntidade.toLowerCase() + "'");

        Util.adicionaLinha(Util.windowsSafe(project.getFile().getParent()) + "/src/main/webapp/keys.json", "]", ",\"CRUD-" + nomeEntidade + "\"");

    }

    private void geraControllers() {
        try {
            File arquivoModule = new File(pastaControllers + "/module.js");
            FileWriter fw = new FileWriter(arquivoModule);
            fw.write(""
                    + "define(function (require) {\n"
                    + "    var angular = require('angular');\n"
                    + "     require('gumga-core')/\n"
                    + "    require('app/modules/" + nomeEntidade.toLowerCase() + "/services/module');\n"
                    + "    require('angular-ui-router');\n"
                    + "\n"
                    + "    return angular.module('app." + nomeEntidade.toLowerCase() + ".controllers', ['app." + nomeEntidade.toLowerCase() + ".services','ui.router','gumga.core'])\n"
                    + "        .controller('" + nomeEntidade + "FormController', require('app/modules/" + nomeEntidade.toLowerCase() + "/controllers/" + nomeEntidade + "FormController'))\n"
                    + "        .controller('" + nomeEntidade + "ListController', require('app/modules/" + nomeEntidade.toLowerCase() + "/controllers/" + nomeEntidade + "ListController'))\n");

            for (Class classe : dependenciasOneToMany) {
                fw.write("        .controller('Modal" + classe.getSimpleName() + "Controller', require('app/modules/" + nomeEntidade.toLowerCase() + "/controllers/Modal" + classe.getSimpleName() + "Controller'))\n");
            }

            fw.write(""
                    + "});\n"
                    + "");
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            File arquivoModule = new File(pastaControllers + "/" + nomeEntidade + "ListController.js");
            FileWriter fw = new FileWriter(arquivoModule);
            fw.write(""
                    + "define([], function () {\n"
                    + "\n"
                    + "    " + nomeEntidade + "ListController.$inject = ["
                    + "'$scope',"
                    + " '" + nomeEntidade + "Service' "
                    + "];\n"
                    + "    function " + nomeEntidade + "ListController("
                    + "$scope, "
                    + nomeEntidade + "Service "
                    + ") {\n"
                    + "\n"
                    + "        " + nomeEntidade + "Service.resetDefaultState();\n"
                    + "        $scope.content = {};\n"
                    + "        $scope.page = 1;\n"
                    + "\n"
                    + "        function update(values) {\n"
                    + "            $scope.content = values;\n"
                    + "        }\n"
                    + "        $scope.$on('_del',function(){\n"
                    + "            $scope.del($scope.selectedEntities);\n"
                    + "        });\n"
                    + "\n"
                    + "        $scope.get = function () {\n"
                    + "            " + nomeEntidade + "Service.get($scope.page).success(function (values) {\n"
                    + "                $scope.content = values;\n"
                    + "            });\n"
                    + "        };\n"
                    + "\n"
                    + "        $scope.sort = function (field, way) {\n"
                    + "            $scope.page = 1;\n"
                    + "            " + nomeEntidade + "Service.doSort(field, way)\n"
                    + "                .success(update);\n"
                    + "        };\n"
                    + "\n"
                    + "        $scope.del = function (entities) {\n"
                    + "            $scope.page = 1;\n"
                    + "            " + nomeEntidade + "Service.doRemove(entities)\n"
                    + "                .then(function(data){\n"
                    + "                     " + nomeEntidade + "Service.getAfterSearch()"
                    + "                      .success(update);\n"
                    + "                });\n"
                    + "\n"
                    + "        };\n"
                    + "\n"
                    + "        $scope.search = function (field, param) {\n"
                    + "            $scope.page = 1;\n"
                    + "            " + nomeEntidade + "Service.getSearch(field, param)\n"
                    + "                .success(update);\n"
                    + "        };\n"
                    + "\n"
                    + "        $scope.advancedSearch = function (param) {\n"
                    + "            $scope.page = 1;\n"
                    + "            " + nomeEntidade + "Service.advancedSearch(param)\n"
                    + "              .success(update);\n"
                    + "        };\n"
                    + "        $scope.get();\n"
                    + "    }\n"
                    + "\n"
                    + "    return " + nomeEntidade + "ListController;\n"
                    + "\n"
                    + "});"
                    + "");
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            File arquivoFormController = new File(pastaControllers + "/" + nomeEntidade + "FormController.js");
            FileWriter fw = new FileWriter(arquivoFormController);

            Set<Class> dependencias = new HashSet<>();
            dependencias.addAll(dependenciasManyToMany);
            dependencias.addAll(dependenciasManyToOne);

            fw.write(""
                    + "define([], function () {\n"
                    + "\n"
                    + "    " + nomeEntidade + "FormController.$inject = ['" + nomeEntidade + "Service', '$state','entity','$scope'");

            for (Class classe : dependencias) {
                fw.write(",'" + classe.getSimpleName() + "Service'");
            }

            fw.write(""
                    + "];\n"
                    + "\n"
                    + "    function " + nomeEntidade + "FormController(" + nomeEntidade + "Service, $state,entity,$scope");

            for (Class classe : dependencias) {
                fw.write("," + classe.getSimpleName() + "Service");
            }

            fw.write(""
                    + ") {\n");

            for (Class dpEnum : dependenciasEnums) {
                fw.write(""
                        + Util.IDENTACAO8 + "$scope.value" + dpEnum.getSimpleName() + " = " + nomeEntidade + "Service.value" + dpEnum.getSimpleName() + ";"
                        + "\n");
            }

            for (Class classe : dependencias) {
                fw.write("        " + classe.getSimpleName() + "Service.resetDefaultState();\n");
            }

            fw.write("        " + nomeEntidade + "Service.resetDefaultState();\n");  //TODO REMOVER DUPLICIDADE EM AUTORELACIONAMENTOS QUANDO EXISTEREM

            fw.write("        $scope.entity = angular.copy(entity.data);\n"
                    + "       $scope.continue = {};"
                    + "\n"
                    + "        $scope.update = function (entity) {\n"
                    + "            " + nomeEntidade + "Service.update(entity)\n"
                    + "                .success(function () {\n"
                    + "                     $scope.continue.value === true? $scope.entity = angular.copy(entity.data ) : $state.go('" + nomeEntidade.toLowerCase() + ".list')\n;"
                    + "                });\n"
                    + "        };\n"
                    + "\n");
            for (Field atributo : Util.getTodosAtributosNaoEstaticos(classeEntidade)) {
                if (atributo.isAnnotationPresent(ManyToMany.class)) {
                    fw.write("        $scope.entity." + atributo.getName() + " = $scope.entity." + atributo.getName() + "  || [];\n\n");

                    fw.write(""
                            + "        $scope." + atributo.getName() + "Availables = [];\n"
                            + "        $scope." + atributo.getName() + "Search = function(param){\n"
                            + "            " + Util.getTipoGenerico(atributo).getSimpleName() + "Service.getSearch('" + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + "', param).then(function(data){\n"
                            + "                $scope." + atributo.getName() + "Availables = data.data.values;\n"
                            + "            })\n"
                            + "        }\n"
                            + ""
                            + "        $scope.postManyToMany" + Util.primeiraMaiuscula(atributo.getName()) + " = function(value){\n"
                            + "            return " + Util.getTipoGenerico(atributo).getSimpleName() + "Service.update({" + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + ": value});\n"
                            + "        };\n"
                            + "\n"
                            + "        $scope." + atributo.getName() + "Search('');\n"
                            + "");

                }

                if (atributo.isAnnotationPresent(OneToMany.class)) {
                    fw.write("        $scope.entity." + atributo.getName() + " = $scope.entity." + atributo.getName() + "  || [];\n\n");
                }
                if (atributo.isAnnotationPresent(ManyToOne.class) || atributo.isAnnotationPresent(OneToOne.class)) {
                    fw.write(" "
                            + "        $scope.postManyToOne" + Util.primeiraMaiuscula(atributo.getName()) + " = function(value){\n"
                            + "            return " + atributo.getType().getSimpleName() + "Service.update({" + Util.primeiroAtributo(atributo.getType()).getName() + ": value});\n"
                            + "        };\n"
                            + ""
                            + "        $scope.searchManyToOne" + Util.primeiraMaiuscula(atributo.getName()) + " = function(value){\n"
                            + "            return " + atributo.getType().getSimpleName() + "Service.getSearch('" + Util.primeiroAtributo(atributo.getType()).getName() + "',value)\n"
                            + "                .then(function(data){\n"
                            + "                    return data.data.values;\n"
                            + "                });"
                            + "        };\n"
                            + ""
                            + "\n");
                }

            }

            for (Field atributoGumgaImage : atributosGumgaImage) {
                fw.write(""
                        + "        $scope.post" + Util.primeiraMaiuscula(atributoGumgaImage.getName()) + " = function(image){\n"
                        + "            return " + nomeEntidade + "Service.postImage('" + (atributoGumgaImage.getName()) + "',image);\n"
                        + "\n"
                        + "        };\n"
                        + "        // Gumga Image para " + (atributoGumgaImage.getName()) + "\n"
                        + "        $scope.delete" + Util.primeiraMaiuscula(atributoGumgaImage.getName()) + " = function(){\n"
                        + "            " + nomeEntidade + "Service.deleteImage('" + (atributoGumgaImage.getName()) + "',$scope.entity." + (atributoGumgaImage.getName()) + ")\n"
                        + "                .then(function(data){\n"
                        + "                    if(data.data == 'OK'){\n"
                        + "                        $scope.entity." + (atributoGumgaImage.getName()) + " = {}\n"
                        + "                    }\n"
                        + "                });\n"
                        + "        };\n"
                        + ""
                        + "");
            }

            fw.write("    }\n"
                    + "\n"
                    + "    return " + nomeEntidade + "FormController;\n"
                    + "});\n"
                    + ""
            );
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        for (Class classe : dependenciasOneToMany) {
            try {
                Set<Class> dependencias = new HashSet<>();
                for (Field atributo : Util.getTodosAtributosMenosIdAutomatico(classe)) {
                    if (atributo.isAnnotationPresent(OneToOne.class)) {
                        dependencias.add(atributo.getType());
                    }
                    if (atributo.isAnnotationPresent(ManyToOne.class)) {
                        dependencias.add(atributo.getType());
                    }
                    if (atributo.isAnnotationPresent(ManyToMany.class)) {
                        dependencias.add(Util.getTipoGenerico(atributo));
                    }
                    if (atributo.isAnnotationPresent(OneToMany.class)) {
                        dependencias.add(Util.getTipoGenerico(atributo));
                    }
                }

                File f = new File(pastaControllers + "/Modal" + classe.getSimpleName() + "Controller.js");
                FileWriter fw = new FileWriter(f);

                fw.write(""
                        + "define([], function () {\n"
                        + "\n"
                        + "    " + classe.getSimpleName() + "ModalController.$inject = ['" + nomeEntidade + "Service', '$state','$modalInstance','entity','$scope'");

                for (Class classe_ : dependencias) {
                    fw.write(",'" + classe_.getSimpleName() + "Service'");
                }

                fw.write(""
                        + "];\n"
                        + "\n"
                        + "    function " + classe.getSimpleName() + "ModalController(" + nomeEntidade + "Service, $state,$modalInstance,entity,$scope");

                for (Class classe_ : dependencias) {
                    fw.write("," + classe_.getSimpleName() + "Service");
                }

                fw.write(""
                        + ") {\n");

                fw.write(
                        "		entity = entity || {};\n"
                        + "             $scope.entity = angular.copy(entity)\n"
                        + "");
                for (Field atributo : Util.getTodosAtributosNaoEstaticos(classe)) {
                    if (atributo.isAnnotationPresent(ManyToMany.class)) {
                        fw.write("        $scope.entity." + atributo.getName() + " = $scope.entity." + atributo.getName() + "  || [];\n\n");

                        fw.write(""
                                + "        $scope." + atributo.getName() + "Availables = [];\n"
                                + "        $scope." + atributo.getName() + "Search = function(param){\n"
                                + "            " + Util.getTipoGenerico(atributo).getSimpleName() + "Service.getSearch('" + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + "', param).then(function(data){\n"
                                + "                $scope." + atributo.getName() + "Availables = data.data.values;\n"
                                + "            })\n"
                                + "        }\n"
                                + "        $scope.postManyToMany" + Util.primeiraMaiuscula(atributo.getName()) + " = function(value){\n"
                                + "            return " + Util.getTipoGenerico(atributo).getSimpleName() + "Service.update({" + Util.primeiroAtributo(atributo.getClass()) + ": value});\n"
                                + "        };\n"
                                + "\n"
                                + "        $scope." + atributo.getName() + "Search('');\n"
                                + "");

                    }

                    if (atributo.isAnnotationPresent(OneToMany.class)) {
                        fw.write("        $scope.entity." + atributo.getName() + " = $scope.entity." + atributo.getName() + "  || [];\n\n");
                    }
                    if (atributo.isAnnotationPresent(ManyToOne.class) || atributo.isAnnotationPresent(OneToOne.class)) {
                        fw.write(" "
                                + "        $scope.postManyToOne" + Util.primeiraMaiuscula(atributo.getName()) + " = function(param){\n"
                                + "            return " + atributo.getType().getSimpleName() + "Service.update({" + Util.primeiroAtributo(atributo.getType()).getName() + ": param});\n"
                                + "        };\n"
                                + ""
                                + "        $scope.searchManyToOne" + Util.primeiraMaiuscula(atributo.getName()) + " = function(value){\n"
                                + "            return " + atributo.getType().getSimpleName() + "Service.getSearch('" + Util.primeiroAtributo(atributo.getType()).getName() + "',value)\n"
                                + "                .then(function(data){\n"
                                + "                    return data.data.values;\n"
                                + "                });"
                                + "        };\n"
                                + ""
                                + "\n");
                    }

                }

                for (Field atributo : Util.getTodosAtributosMenosIdAutomatico(classe)) {
                    if (GumgaImage.class.equals(atributo.getType()) || GumgaFile.class.equals(atributo.getType())) {
                        fw.write(""
                                + "     $scope." + atributo.getName() + "PostPicture = function(image){\n"
                                + "            return " + classe.getSimpleName() + "Service.postImage('picture',image);\n"
                                + "\n"
                                + "        };\n"
                                + "        $scope." + atributo.getName() + "DeletePicture = function(){\n"
                                + "            " + classe.getSimpleName() + "Service.deleteImage('picture')\n"
                                + "                .then(function(data){\n"
                                + "                    if(data.data == 'OK'){\n"
                                + "                        $scope.data.picture = {}\n"
                                + "                    }\n"
                                + "                });\n"
                                + "        };"
                                + ""
                                + ""
                                + ""
                                + "");
                    }
                }

                fw.write("		$scope.ok = function (obj) {\n"
                        + "			$modalInstance.close(obj);\n"
                        + "		};\n"
                        + "\n"
                        + "		$scope.cancel = function () {\n"
                        + "			$modalInstance.dismiss('cancel');\n"
                        + "		};\n"
                        + "\n"
                        + "	}\n"
                        + "	return " + classe.getSimpleName() + "ModalController;\n"
                        + "})\n"
                        + "\n");

                fw.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

    }

    private void geraServices() {
        try {
            File arquivoModule = new File(pastaServices + "/" + nomeEntidade + "Service.js");
            FileWriter fw = new FileWriter(arquivoModule);
            fw.write(""
                    + "define(['app/apiLocations'], function (APILocation) {\n"
                    + "\n"
                    + "    " + nomeEntidade + "Service.$inject = ['GumgaBase', '$stateParams'];\n"
                    + "\n"
                    + "    function " + nomeEntidade + "Service(GumgaBase, $stateParams) {\n"
                    + "        var url = APILocation.apiLocation + '/api/" + nomeEntidade.toLowerCase() + "';\n"
                    + "        var query = {};\n"
                    + "        query.params = {\n"
                    + "            start: 0,\n"
                    + "            pageSize: 10\n"
                    + "        };\n"
                    + "\n"
                    + "        this.get = function (page) {\n"
                    + "            if (page) {\n"
                    + "                query.params.start = (page - 1) * query.params.pageSize;\n"
                    + "                if (page < 1) throw 'Invalid page';\n"
                    + "            }\n"
                    + "            return GumgaBase.get(url,query);\n"
                    + "        };\n"
                    + "\n"
                    + "        this.getById = function(id) {\n"
                    + "            return GumgaBase.getById(id);\n"
                    + "        };\n"
                    + "\n"
                    + "        this.getNew = function(){\n"
                    + "            return GumgaBase.getNew(url);\n"
                    + "        }\n"
                    + "\n"
                    + "        this.getAfterSearch = function(){\n"
                    + "            this.resetDefaultState();\n"
                    + "            return GumgaBase.get(url,query);\n"
                    + "        }\n"
                    + "\n"
                    + "        this.getSearch = function (field, param) {\n"
                    + "            if (!param) param = '';\n"
                    + "            query.params = {};\n"
                    + "            query.params.q = param;\n"
                    + "            query.params.start = 0;\n"
                    + "            query.params.pageSize = 10;\n"
                    + "            query.params.searchFields = field;\n"
                    + "            return GumgaBase.get(url,query);\n"
                    + "        };\n"
                    + "\n"
                    + "        this.doSort = function (field, way) {\n"
                    + "            query.params.start = 0;\n"
                    + "            query.params.sortField = field;\n"
                    + "            query.params.sortDir = way;\n"
                    + "            return GumgaBase.get(url,query);\n"
                    + "        };\n"
                    + "\n"
                    + "        this.doRemove = function (entities) {\n"
                    + "            return GumgaBase.deleteAll(url,entities);\n"
                    + "        };\n"
                    + "\n"
                    + "        this.update = function (entity) {\n"
                    + "            if (entity.id) {\n"
                    + "                return GumgaBase.update(url,entity);\n"
                    + "            }\n"
                    + "            return GumgaBase.save(url,entity);\n"
                    + "        };\n"
                    + "\n"
                    + "        this.advancedSearch = function (param) {\n"
                    + "            query.params = {};\n"
                    + "            query.params.aq = param;\n"
                    + "            return GumgaBase.get(url,query);\n"
                    + "        };\n"
                    + "\n"
                    + "        this.resetDefaultState = function(){\n"
                    + "            query.params = {\n"
                    + "                start: 0,\n"
                    + "                pageSize: 10\n"
                    + "            };\n"
                    + "        };\n");

            if (!atributosGumgaImage.isEmpty()) {
                fw.write("\n"
                        + "        this.postImage= function(attribute,model){\n"
                        + "            return GumgaBase.postImage(url,attribute,model);\n"
                        + "        };\n"
                        + "\n"
                        + "        this.deleteImage = function(attribute,value){\n"
                        + "            return GumgaBase.deleteImage(url,attribute,value);\n"
                        + "        };\n"
                        + "\n");
            }

            for (Class dpEnum : dependenciasEnums) {
                fw.write(Util.IDENTACAO8 + "this.value" + dpEnum.getSimpleName() + "=[");
                Object[] constants = dpEnum.getEnumConstants();
                for (int i = 0; i < constants.length; i++) {
                    if (i != 0) {
                        fw.write(",");
                    }
                    fw.write("{value:\"" + constants[i] + "\",label:\"" + constants[i].toString().toLowerCase() + "\"}");
                }
                fw.write("];\n");

            }
            fw.write(""
                    + "    }\n"
                    + "\n"
                    + "    return " + nomeEntidade + "Service;\n"
                    + "});\n"
                    + ""
                    + "");
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            File arquivoModule = new File(pastaServices + "/module.js");
            FileWriter fw = new FileWriter(arquivoModule);
            fw.write(""
                    + "/**\n"
                    + " * Created by igorsantana on " + Util.hoje() + ".\n"
                    + " */\n"
                    + "\n"
                    + "define(function(require){\n"
                    + "    require('gumga-core');\n"
                    + "    var angular = require('angular');"
                    + "    var " + nomeEntidade + "Service = require('app/modules/" + nomeEntidade.toLowerCase() + "/services/" + nomeEntidade + "Service');\n"
                    + "    return angular.module('app." + nomeEntidade.toLowerCase() + ".services',['gumga.core'])\n"
                    + "        .service('" + nomeEntidade + "Service'," + nomeEntidade + "Service);\n"
                    + "});"
                    + "");
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void geraViews() {
        try {
            File arquivoModule = new File(pastaViews + "/base.html");
            FileWriter fw = new FileWriter(arquivoModule);
            fw.write("<gumga-nav title=\"" + nomeEntidade + "\" state=\"login.log\"></gumga-nav>\n"
                    + "<gumga-menu menu-url=\"gumga-menu.json\" keys-url=\"keys.json\"  image=\"resources/images/gumga.png\"></gumga-menu>\n"
                    + "<div class=\"gumga-container\" gumga-alert>\n"
                    + "    <gumga-breadcrumb></gumga-breadcrumb>\n"
                    + "    <div class=\"full-width-without-padding\">\n"
                    + "        <h3 style=\"margin-top: 0\" gumga-translate-tag=\"" + nomeEntidade.toLowerCase() + ".title\"></h3>\n"
                    + "    </div>\n"
                    + "    <div class=\"col-md-12\" ui-view>\n"
                    + "\n"
                    + "    </div>\n"
                    + "</div>\n"
                    + "");
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            File arquivoModule = new File(pastaViews + "/form.html");
            FileWriter fw = new FileWriter(arquivoModule);
            fw.write(""
                    + "<form name=\"" + nomeEntidade + "Form\" novalidate>\n"
                    + "    <div class=\"full-width-without-padding\">\n"
                    + "\n");

            boolean primeiro = true;
            geraCampos(fw, this.classeEntidade);
            fw.write(""
                    + "\n"
                    + "        <gumga-form-buttons\n"
                    + "                back=\"" + nomeEntidade.toLowerCase() + ".list\"\n"
                    + "                submit=\"update(entity)\"\n"
                    + "                position=\"right\"\n"
                    + "                valid=\"" + nomeEntidade + "Form.$valid\"\n"
                    + "                continue=\"continue\">\n"
                    + "        </gumga-form-buttons>\n"
                    + "    </div>\n"
                    + "</form>"
            );
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            File arquivoModule = new File(pastaViews + "/list.html");
            FileWriter fw = new FileWriter(arquivoModule);
            fw.write(""
                    + "<div class=\"col-md-12\">\n"
                    + "    <div class=\"col-md-5\">\n"
                    + "        <a ui-sref=\"" + nomeEntidade.toLowerCase() + ".insert\" class=\"btn btn-primary\"><i class=\"glyphicon glyphicon-plus\"></i> New</a>\n"
                    + "        <button type=\"button\" class=\"btn btn-danger\" ng-click=\"del(selectedEntities)\"><i class=\"glyphicon glyphicon-trash\"></i> Delete\n"
                    + "        </button>\n"
                    + "    </div>\n"
                    + "    <div class=\"col-md-7\">\n"
                    + "        <gumga-search fields=\"" + Util.todosAtributosSeparadosPorVirgula(classeEntidade) + "\"\n advanced=\"true\"\n search-method=\"search(field,param)\"\n"
                    + "                      advanced-method=\"advancedSearch(param)\"\n translate-entity=\" " + classeEntidade.getSimpleName().toLowerCase() + "\">\n"
                    + "            <advanced-field name=\"id\" type=\"number\"></advanced-field>\n");
            for (Field atributo : Util.getTodosAtributosMenosIdAutomatico(classeEntidade)) {
                fw.write("            <advanced-field name=\"" + atributo.getName() + "\" type=\"" + converteTipoParaAdvanced(atributo.getType()) + "\"></advanced-field>\n");
            }
            fw.write(""
                    + "        </gumga-search>\n"
                    + "    </div>\n"
                    + "        <gumga-table\n"
                    + "            translate-entity=\"" + nomeEntidade.toLowerCase() + "\""
                    + "            name=\"" + nomeEntidade.toLowerCase() + "\""
                    + "            values=\"content.values\"\n");
            fw.write("            columns=\"" + Util.todosAtributosSeparadosPorVirgula(classeEntidade) + "\"\n");
            fw.write(""
                    + "            sort-function=\"sort(field,way)\"\n"
                    + "            >\n");
            for (Field atributo : Util.getTodosAtributosMenosIdAutomatico(classeEntidade)) {
                if (GumgaEMail.class.equals(atributo.getType())) {
                    fw.write("<object-column column=\"" + atributo.getName().toLowerCase() + "\" property=\"" + atributo.getName().toLowerCase() + ".value\"></object-column>\n");
                } else if (GumgaPhoneNumber.class.equals(atributo.getType())) {
                    fw.write("<object-column column=\"" + atributo.getName().toLowerCase() + "\" property=\"" + atributo.getName().toLowerCase() + ".value\"></object-column>\n");
                } else if (GumgaBoolean.class.equals(atributo.getType())) {
                    fw.write("<object-column column=\"" + atributo.getName().toLowerCase() + "\" property=\"" + atributo.getName().toLowerCase() + ".value\"></object-column>\n");
                } else if (GumgaCNPJ.class.equals(atributo.getType())) {
                    fw.write("<object-column column=\"" + atributo.getName().toLowerCase() + "\" property=\"" + atributo.getName().toLowerCase() + ".value\"></object-column>\n");
                } else if (GumgaURL.class.equals(atributo.getType())) {
                    fw.write("<object-column column=\"" + atributo.getName().toLowerCase() + "\" property=\"" + atributo.getName().toLowerCase() + ".value\"></object-column>\n");
                } else if (GumgaAddress.class.equals(atributo.getType())) {
                    fw.write("<object-column column=\"" + atributo.getName().toLowerCase() + "\" property=\"" + atributo.getName().toLowerCase() + ".localization\"></object-column>\n");
                } else if (GumgaBarCode.class.equals(atributo.getType())) {
                    fw.write("<object-column column=\"" + atributo.getName().toLowerCase() + "\" property=\"" + atributo.getName().toLowerCase() + ".value\"></object-column>\n");
                } else if (GumgaCEP.class.equals(atributo.getType())) {
                    fw.write("<object-column column=\"" + atributo.getName().toLowerCase() + "\" property=\"" + atributo.getName().toLowerCase() + ".value\"></object-column>\n");
                } else if (GumgaCPF.class.equals(atributo.getType())) {
                    fw.write("<object-column column=\"" + atributo.getName().toLowerCase() + "\" property=\"" + atributo.getName().toLowerCase() + ".value\"></object-column>\n");
                } else if (GumgaFile.class.equals(atributo.getType())) {
                    fw.write("<object-column column=\"" + atributo.getName().toLowerCase() + "\" property=\"" + atributo.getName().toLowerCase() + ".name\"></object-column>\n");
                } else if (GumgaImage.class.equals(atributo.getType())) {
                    fw.write("<object-column column=\"" + atributo.getName().toLowerCase() + "\" property=\"" + atributo.getName().toLowerCase() + ".name\"></object-column>\n");
                } else if (GumgaMultiLineString.class.equals(atributo.getType())) {
                    fw.write("<object-column column=\"" + atributo.getName().toLowerCase() + "\" property=\"" + atributo.getName().toLowerCase() + ".value\"></object-column>\n");
                } else if (GumgaTime.class.equals(atributo.getType())) {
                    fw.write("<object-column column=\"" + atributo.getName().toLowerCase() + "\" property=\"" + atributo.getName().toLowerCase() + ".hour\"></object-column>\n");
                } else if (GumgaURL.class.equals(atributo.getType())) {
                    fw.write("<object-column column=\"" + atributo.getName().toLowerCase() + "\" property=\"" + atributo.getName().toLowerCase() + ".value\"></object-column>\n");
                }
            }
            fw.write("        <buttons-column>\n"
                    + "            <a ui-sref=\"" + nomeEntidade.toLowerCase() + ".edit({id: entity.id})\" class=\"btn btn-link pull-right\">Edit</a>\n"
                    + "        </buttons-column>\n"
                    + "        </gumga-table>\n"
                    + "\n"
                    + "    <div class=\"full-width-without-padding\">\n"
                    + "        <pagination ng-model=\"page\"\n"
                    + "                    items-per-page=\"content.pageSize\"\n"
                    + "                    total-items=\"content.count\"\n"
                    + "                    ng-change=\"get()\"></pagination>\n"
                    + "    </div>\n"
                    + "\n"
                    + "</div>\n"
                    + ""
            );
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        for (Class classe : dependenciasOneToMany) {
            try {
                File arquivoModalHtml = new File(pastaViews + "/modal" + classe.getSimpleName() + ".html");
                FileWriter fw = new FileWriter(arquivoModalHtml);
                fw.write("<form name=\"Modal\">\n"
                        + "<div class=\"modal-header\">\n"
                        + "    <h3 class=\"modal-title\" gumga-translate-tag=\" " + classe.getSimpleName().toLowerCase() + ".title\"></h3>\n"
                        + "</div>\n"
                        + "<div class=\"modal-body\" style=\"overflow: auto\">\n");

                geraCampos(fw, classe);

                fw.write("</div>\n"
                        + "<div class=\"clearfix\"></div>\n"
                        + "<div class=\"modal-footer\">\n"
                        + "    <button type=\"button\" class=\"btn btn-primary\" ng-click=\"ok(entity)\" ng-disabled=\"Modal.$invalid\">OK</button>\n"
                        + "    <button type=\"button\" class=\"btn btn-warning\" ng-click=\"cancel()\">Cancel</button>\n"
                        + "</div>\n"
                        + "</form>\n");

                fw.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

    }

    public void geraCampos(FileWriter fw, Class classeEntidade) throws IOException {
        boolean primeiro;
        for (Field atributo : Util.getTodosAtributosMenosIdAutomatico(classeEntidade)) {
            //COLOCAR OS TIPOS
            boolean requerido = false;

            fw.write(Util.IDENTACAO4 + Util.IDENTACAO4 + "<!--" + atributo.getName() + " " + atributo.getType() + "-->\n");

            if (atributo.isAnnotationPresent(ManyToOne.class) || atributo.isAnnotationPresent(OneToOne.class)) {
                fw.write("<div class=\"full-width-without-padding\"> "
                        + Util.IDENTACAO4 + Util.IDENTACAO4 + "<label for=\"" + atributo.getName() + "\"  gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\"></label>\n"
                        + "<gumga-many-to-one "
                        + "         value=\"entity." + atributo.getName() + "\"\n"
                        + "         search-method=\"searchManyToOne" + Util.primeiraMaiuscula(atributo.getName()) + "(param)\"\n"
                        + "         field=\"" + Util.primeiroAtributo(atributo.getType()).getName() + "\"\n"
                        + "         add-method=\"postManyToOne" + Util.primeiraMaiuscula(atributo.getName()) + "(value)\">\n"
                        + "</gumga-many-to-one>"
                        + "</div>");

            } else if (atributo.isAnnotationPresent(ManyToMany.class)) {
                fw.write(""
                        + "        <div class=\"col-md-6\">\n"
                        + "            <label for=\"" + atributo.getName() + "\"  gumga-translate-tag=\"" + Util.getTipoGenerico(atributo).getSimpleName().toLowerCase() + ".title\"></label>\n"
                        + "        </div>\n"
                        + "        <div class=\"col-md-6\">\n"
                        + "            <label for=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\" gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\"></label>\n"
                        + "        </div>"
                        + "\n");

                fw.write("<div class=\"full-width-without-padding\">\n"
                        + Util.IDENTACAO4 + Util.IDENTACAO4 + "<gumga-many-to-many "
                        + "left-list=\"" + atributo.getName().toLowerCase() + "Availables" + "\"\n "
                        + "right-list=\"entity." + atributo.getName() + "\" \n"
                        + "left-search=\"" + atributo.getName() + "Search(param)\" \n"
                        + "filter-parameters=\"" + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + "\"\n"
                        + "post-method=\"postManyToMany" + Util.primeiraMaiuscula(atributo.getName()) + "(value)\"\n"
                        + "authorize-add=\"true\""
                        + ">\n"
                        + Util.IDENTACAO4 + Util.IDENTACAO4 + Util.IDENTACAO4 + "    <left-field>{{$value." + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + "}}</left-field>\n"
                        + Util.IDENTACAO4 + Util.IDENTACAO4 + Util.IDENTACAO4 + "    <right-field>{{$value." + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + "}}</right-field>\n"
                        + Util.IDENTACAO4 + Util.IDENTACAO4 + "</gumga-many-to-many>\n\n"
                        + "</div>\n"
                        + "");

            } else if (atributo.isAnnotationPresent(OneToMany.class)) {
                fw.write("<div class=\"full-width-without-padding\">");
                fw.write(Util.IDENTACAO4 + Util.IDENTACAO4 + "<label for=\"" + atributo.getName() + "\"  gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\"></label>\n");
                fw.write(
                        "<gumga-one-to-many\n"
                        + "     children=\"entity." + atributo.getName() + "\"\n"
                        + "     template-url=\"app/modules/" + atributo.getDeclaringClass().getSimpleName().toLowerCase() + "/views/modal" + Util.getTipoGenerico(atributo).getSimpleName() + ".html\"\n"
                        + "     displayable-property=\"" + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName().toLowerCase() + "\"\n"
                        + "     controller=\"Modal" + Util.getTipoGenerico(atributo).getSimpleName() + "Controller\">"
                        + "</gumga-one-to-many>\n"
                        + "</div>"
                        + "\n");

            } else {
                fw.write(Util.IDENTACAO4 + Util.IDENTACAO4 + "<label for=\"" + atributo.getName() + "\"  gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\"></label>\n");
                if (GumgaAddress.class.equals(atributo.getType())) {
                    fw.write("  <div class=\"row\">"
                            + "     <div class=\"col-md-12\">\n"
                            + "         <gumga-address name=\"" + atributo.getName() + "\" value=\"entity." + atributo.getName() + "\"> </gumga-address>\n"
                            + "     </div>\n"
                            + " </div>\n"
                            + "\n");
                } else if (GumgaBarCode.class.equals(atributo.getType())) {
                    fw.write(""
                            + "        <input id=\"" + atributo.getName() + "\" type=\"text\" name=\"" + atributo.getName() + "\" ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" />\n"
                            + "        <gumga-errors name=\"" + atributo.getName() + "\"></gumga-errors>\n"
                            + "\n");
                } else if (GumgaCEP.class.equals(atributo.getType())) { //TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR A DEPENDENCIA EXTERNA
                    fw.write(""
                            + "        <input id=\"" + atributo.getName() + "\" type=\"text\" name=\"" + atributo.getName() + "\" ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" ui-br-cep-mask/>\n"
                            + "        <gumga-errors name=\"" + atributo.getName() + "\"></gumga-errors>\n"
                            + "\n");
                } else if (GumgaCNPJ.class.equals(atributo.getType())) { //TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR A DEPENDENCIA EXTERNA
                    fw.write(""
                            + "        <input id=\"" + atributo.getName() + "\" type=\"text\" name=\"" + atributo.getName() + ".\" ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" ui-br-cnpj-mask/>\n"
                            + "        <gumga-errors name=\"" + atributo.getName() + "\"></gumga-errors>\n"
                            + "\n");
                } else if (GumgaCPF.class.equals(atributo.getType())) { //TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR A DEPENDENCIA EXTERNA
                    fw.write(""
                            + "        <input id=\"" + atributo.getName() + "\" type=\"text\" name=\"" + atributo.getName() + "\" ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" ui-br-cpf-mask/>\n"
                            + "        <gumga-errors name=\"" + atributo.getName() + "\"></gumga-errors>\n"
                            + "\n");
                } else if (GumgaIP4.class.equals(atributo.getType())) { //TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR O GUMGAMAX E GUMGAMIN
                    fw.write(""
                            + "        <input id=\"" + atributo.getName() + "\" type=\"text\" name=\"" + atributo.getName() + "\" ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" gumga-min=\"12\" gumga-max=\"12\"/>\n"
                            + "        <gumga-errors name=\"" + atributo.getName() + "\"></gumga-errors>\n"
                            + "\n");
                } else if (GumgaIP6.class.equals(atributo.getType())) { //TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR O GUMGAMAX E GUMGAMIN
                    fw.write(""
                            + "        <input id=\"" + atributo.getName() + "\" type=\"text\" name=\"" + atributo.getName() + "\" ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" gumga-min=\"12\" gumga-max=\"12\"/>\n"
                            + "        <gumga-errors name=\"" + atributo.getName() + "\"></gumga-errors>\n"
                            + "\n");
                } else if (GumgaFile.class.equals(atributo.getType())) {//TODO SUBSTITUIR PELA DIRETIVA DE IMPORTAÇÃO E ARQUIVO QUANDO ESTIVER PRONTA

                } else if (GumgaImage.class.equals(atributo.getType())) {
                    fw.write(""
                            + "<gumga-upload attribute=\"entity." + atributo.getName() + "\"\n"
                            + "                      upload-method=\"post" + Util.primeiraMaiuscula(atributo.getName()) + "(image)\"\n"
                            + "                      delete-method=\"delete" + Util.primeiraMaiuscula(atributo.getName()) + "()\">\n"
                            + "        </gumga-upload>\n"
                            + "");

                } else if (GumgaEMail.class.equals(atributo.getType())) {
                    fw.write(""
                            + "        <input id=\"" + atributo.getName() + "\" type=\"email\" name=\"" + atributo.getName() + "\" ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" />\n"
                            + "        <gumga-errors name=\"" + atributo.getName() + "\"></gumga-errors>\n"
                            + "\n");
                } else if (GumgaMoney.class.equals(atributo.getType())) {//TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR A DEPENDENCIA EXTERNA
                    fw.write(""
                            + "        <input id=\"" + atributo.getName() + "\" type=\"text\" name=\"" + atributo.getName() + "\" ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" ui-money-mask=\"2\"/>\n"
                            + "        <gumga-errors name=\"" + atributo.getName() + "\"></gumga-errors>\n"
                            + "\n");
                } else if (GumgaPhoneNumber.class.equals(atributo.getType())) {//TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR A DEPENDENCIA EXTERNA
                    fw.write(""
                            + "        <input id=\"" + atributo.getName() + "\" type=\"text\" name=\"" + atributo.getName() + "\" ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" ui-br-phone-number/>\n"
                            + "        <gumga-errors name=\"" + atributo.getName() + "\"></gumga-errors>\n"
                            + "\n");
                } else if (GumgaURL.class.equals(atributo.getType())) {//TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR A DEPENDENCIA EXTERNA
                    fw.write(""
                            + "        <input id=\"" + atributo.getName() + "\" type=\"url\" name=\"" + atributo.getName() + "\" ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" />\n"
                            + "        <gumga-errors name=\"" + atributo.getName() + "\"></gumga-errors>\n"
                            + "\n");
                } else if (GumgaTime.class.equals(atributo.getType())) {//TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR A DEPENDENCIA EXTERNA
                    fw.write("\n"
                            + " <div class=\"row\">\n"
                            + "     <div class=\"col-md-12\">\n"
                            + "            <label for=\"" + atributo.getName() + ".hour\">Hour</label>\n"
                            + "            <input id=\"" + atributo.getName() + ".hour\" type=\"number\" name=\"" + atributo.getName() + ".hour\" ng-model=\"entity." + atributo.getName() + ".hour\" max=\"23\" min=\"0\" class=\"form-control\"/>\n"
                            + "            <gumga-errors name=\"" + atributo.getName() + ".hour\"></gumga-errors>\n"
                            + "            <label for=\"" + atributo.getName() + ".minute\">Minute</label>\n"
                            + "            <input id=\"" + atributo.getName() + ".minute\" type=\"number\" name=\"" + atributo.getName() + ".minute\" ng-model=\"entity." + atributo.getName() + ".minute\" max=\"59\" min=\"0\" class=\"form-control\"/>\n"
                            + "            <gumga-errors name=\"" + atributo.getName() + ".minute\"></gumga-errors>\n"
                            + "            <label for=\"" + atributo.getName() + ".second\">Second</label>\n"
                            + "            <input id=\"" + atributo.getName() + ".second\" type=\"number\" name=\"" + atributo.getName() + ".second\" ng-model=\"entity." + atributo.getName() + ".second\" max=\"59\" min=\"0\" class=\"form-control\"/>\n"
                            + "            <gumga-errors name=\"" + atributo.getName() + ".second\"></gumga-errors>\n"
                            + "    </div>\n"
                            + " </div>\n"
                            + "\n");
                } else if (GumgaMultiLineString.class.equals(atributo.getType())) {
                    fw.write(""
                            + "        <textarea ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" placeholder=\"Digite " + Util.etiqueta(atributo) + ".\" rows=\"4\" cols=\"50\"></textarea>\n\n"
                            + "        <gumga-errors name=\"" + atributo.getName() + "\"></gumga-errors>\n"
                            + "\n");
                } else if (GumgaGeoLocation.class.equals(atributo.getType())) { //TODO SUBSTITUIR PELO COMPONENTE GUMGAMAPS QUANDO ELE ESTIVER PRONTO
                    fw.write(""
                            + " <div class=\"row\">\n"
                            + "     <div class=\"col-md-12\">\n"
                            + "        <label for=\"" + atributo.getName() + ".latitude\">Latitude</label>\n"
                            + "        <input id=\"" + atributo.getName() + ".latitude\" type=\"text\" name=\"" + atributo.getName() + ".latitude\" ng-model=\"entity." + atributo.getName() + ".latitude\" class=\"form-control\" />\n"
                            + "        <gumga-errors name=\"" + atributo.getName() + ".latitude\"></gumga-errors>\n"
                            + "        <label for=\"" + atributo.getName() + ".longitude\">Longitude</label>\n"
                            + "        <input id=\"" + atributo.getName() + ".longitude\" type=\"text\" name=\"" + atributo.getName() + ".longitude\" ng-model=\"entity." + atributo.getName() + ".longitude\" class=\"form-control\" />\n"
                            + "        <gumga-errors name=\"" + atributo.getName() + ".longitude\"></gumga-errors>\n"
                            + "        <a class=\"btn btn-default\" ng-href=\"http://maps.google.com/maps?q={{entity." + atributo.getName() + ".latitude + ',' + entity." + atributo.getName() + ".longitude}}\" target=\"_blank\"> <p class=\"glyphicon glyphicon-globe\"></p> GOOGLE MAPS</a>\n"
                            + "     </div>\n"
                            + " </div>\n"
                            + "\n");
                } else if (GumgaBoolean.class.equals(atributo.getType())) {
                    fw.write(""
                            + "        <input style=\"width:15px\" type=\"checkbox\" name=\"" + atributo.getName() + "\" ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" />\n"
                            + "        <gumga-errors name=\"" + atributo.getName() + "\"></gumga-errors>\n"
                            + "\n");
                } else if (Date.class.equals(atributo.getType())) {
                    fw.write(""
                            + "        <input type=\"text\" class=\"form-control\" datepicker-popup=\"fullDate\" ng-model=\"entity." + atributo.getName() + "\" is-open=\"opened\" ng-click=\"opened= !opened\" close-text=\"Close\" />"
                            + "        <gumga-errors name=\"" + atributo.getName() + "\"></gumga-errors>\n"
                            + "\n");

                } else if (atributo.getType().isEnum()) {
                    Object[] constants = atributo.getType().getEnumConstants();
                    fw.write(Util.IDENTACAO8 + "<select class='form-control' name=\"" + atributo.getName() + "\" ng-model=\"entity." + atributo.getName() + "\" >\n");
                    fw.write(Util.IDENTACAO12 + "<option  ng-selected=\"value.value === entity." + atributo.getName() + "\"  value=\"{{value.value}}\" ng-repeat=\"value in value" + atributo.getType().getSimpleName() + "\">{{value.label}}</option>");
                    fw.write(Util.IDENTACAO8 + "</select>\n");
                } else {
                    fw.write(""
                            + "        <input id=\"" + atributo.getName() + "\" type=\"text\" name=\"" + atributo.getName() + "\" ng-model=\"entity." + atributo.getName() + "\"" + geraValidacoesDoBenValidator(atributo) + "  class=\"form-control\" />\n"
                            + "        <gumga-errors name=\"" + atributo.getName() + "\"></gumga-errors>\n"
                            + "\n");
                }
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
        if (atributo.isAnnotationPresent(NotNull.class)
                || atributo.isAnnotationPresent(NotEmpty.class)
                || atributo.isAnnotationPresent(NotBlank.class)) {
            aRetornar += " required";
        }
        System.out.println("----------------------> "+atributo.getName()+aRetornar);

        return aRetornar;
    }

    private void geraModule() {
        try {
            File arquivoModule = new File(pastaApp + "/module.js");
            FileWriter fw = new FileWriter(arquivoModule);
            fw.write(""
                    + "define(function (require) {\n"
                    + "\n"
                    + "    var angular = require('angular');\n"
                    + "    require('angular-ui-router');\n"
                    + "    require('app/modules/" + nomeEntidade.toLowerCase() + "/services/module');\n"
                    + "    require('app/modules/" + nomeEntidade.toLowerCase() + "/controllers/module');\n"
                    + "    require('gumga-core');\n"
                    + "    var APILocation = require('app/apiLocations');\n"
                    + "\n"
                    + "    angular.module('app." + nomeEntidade.toLowerCase() + "', ['ui.router', 'app." + nomeEntidade.toLowerCase() + ".controllers', 'app." + nomeEntidade.toLowerCase() + ".services', 'gumga.core'])\n"
                    + "        .config(function ($stateProvider, $httpProvider) {\n"
                    + "            $stateProvider\n"
                    + "                .state('" + nomeEntidade.toLowerCase() + ".list', {\n"
                    + "                    url: '/list',\n"
                    + "                    templateUrl: 'app/modules/" + nomeEntidade.toLowerCase() + "/views/list.html',\n"
                    + "                    controller: '" + nomeEntidade + "ListController',\n"
                    + "                    data: {\n"
                    + "                         id: 2 \n"
                    + "                    }\n"
                    + "                })\n"
                    + "                .state('" + nomeEntidade.toLowerCase() + ".insert', {\n"
                    + "                    url: '/insert',\n"
                    + "                    templateUrl: 'app/modules/" + nomeEntidade.toLowerCase() + "/views/form.html',\n"
                    + "                    controller: '" + nomeEntidade + "FormController',\n"
                    + "                    controllerAs: 'form',\n"
                    + "                    data: {"
                    + "                         id: 3 "
                    + "                    },"
                    + "                    resolve: {\n"
                    + "                        entity: ['$stateParams', '$http', function ($stateParams, $http) {\n"
                    + "                            var url = APILocation.apiLocation + '/api/" + nomeEntidade.toLowerCase() + "/new'\n"
                    + "                            return $http.get(url);\n"
                    + "                        }]\n"
                    + "                    }\n"
                    + "                })\n"
                    + "                .state('" + nomeEntidade.toLowerCase() + ".edit', {\n"
                    + "                    url: '/edit/:id',\n"
                    + "                    templateUrl: 'app/modules/" + nomeEntidade.toLowerCase() + "/views/form.html',\n"
                    + "                    controller: '" + nomeEntidade + "FormController',\n"
                    + "                    data: {"
                    + "                         id: 3 "
                    + "                    },"
                    + "                    resolve: {\n"
                    + "                        entity: ['$stateParams', '$http', function ($stateParams, $http) {\n"
                    + "                            var url = APILocation.apiLocation + '/api/" + nomeEntidade.toLowerCase() + "/' + $stateParams.id;\n"
                    + "                            return $http.get(url);\n"
                    + "                        }]\n"
                    + "                    }\n"
                    + "                });\n"
                    + "        })\n"
                    + "});\n"
                    + ""
                    + "");
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void geraI18n() {
        try {
            String texto = Util.IDENTACAO4 + ",\"" + nomeEntidade.toLowerCase() + "\":{\n"
                    + Util.IDENTACAO4 + Util.IDENTACAO4 + "\"title\":\"" + Util.primeiraMaiuscula(nomeEntidade) + "\"\n"
                    + Util.IDENTACAO4 + Util.IDENTACAO4 + ",\"menulabel\": \"" + Util.primeiraMaiuscula(nomeEntidade) + "\"\n"
                    + Util.IDENTACAO4 + Util.IDENTACAO4 + ",\"edit\": \"Editar " + Util.primeiraMaiuscula(nomeEntidade) + "\"\n"
                    + Util.IDENTACAO4 + Util.IDENTACAO4 + ",\"insert\": \"Inserir " + Util.primeiraMaiuscula(nomeEntidade) + "\"\n"
                    + Util.IDENTACAO4 + Util.IDENTACAO4 + ",\"list\": \"Consulta " + Util.primeiraMaiuscula(nomeEntidade) + "\"\n"
                    + Util.IDENTACAO4 + Util.IDENTACAO4 + ",\"id\": \"id\"\n";
            for (Field atributo : Util.getTodosAtributosMenosIdAutomatico(classeEntidade)) {
                texto += Util.IDENTACAO4 + Util.IDENTACAO4 + ",\"" + atributo.getName().toLowerCase() + "\":\"" + Util.primeiraMaiuscula(atributo.getName()) + "\"\n";
            }
            texto += Util.IDENTACAO4 + "}\n";
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
