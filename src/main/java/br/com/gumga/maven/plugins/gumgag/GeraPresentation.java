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
import javax.persistence.Column;
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
                + "        \"icon\": \"glyphicon glyphicon-user\",\n"
                + "        \"icon_color\": \"\",\n"
                + "        \"imageUrl\": \"\",\n"
                + "        \"imageWidth\": \"\",\n"
                + "        \"imageHeight\": \"\",\n"
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
                    + "    require('app/modules/" + nomeEntidade.toLowerCase() + "/services/module');\n"
                    + "    require('angular-ui-router');\n"
                    + "\n"
                    + "    return angular.module('app." + nomeEntidade.toLowerCase() + ".controllers', ['app." + nomeEntidade.toLowerCase() + ".services','ui.router'])\n"
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
                    + " '" + nomeEntidade + "Service', 'populateScope' "
                    + "];\n"
                    + "    function " + nomeEntidade + "ListController("
                    + "$scope, "
                    + nomeEntidade + "Service,populateScope "
                    + ") {\n"
                    + "\n"
                    + "         populateScope($scope," + nomeEntidade + "Service,'" + nomeEntidade + "','base-list');\n "
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
                    + "    " + nomeEntidade + "FormController.$inject = ['" + nomeEntidade + "Service', '$state','entity','$scope', 'populateScope'");

            for (Class classe : dependencias) {
                fw.write(",'" + classe.getSimpleName() + "Service'");
            }

            fw.write(""
                    + "];\n"
                    + "\n"
                    + "    function " + nomeEntidade + "FormController(" + nomeEntidade + "Service, $state,entity,$scope,populateScope");

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

            fw.write("        $scope.entity = angular.copy(entity.data);\n"
                    + "       $scope.continue = {};"
                    + "\n"
                    + "       populateScope($scope, " + nomeEntidade + "Service, '" + nomeEntidade + "', 'base-form');\n"
                    + "       $scope.$on('afterUpdate',function(){\n"
                    + "             $scope.continue.value ? $scope.entity = angular.copy(entity.data) : $state.go('" + nomeEntidade.toLowerCase() + ".list');\n"
                    + "       })"
                    + "\n");
            boolean formatDateInserido = false;
            for (Field atributo : Util.getTodosAtributosNaoEstaticos(classeEntidade)) {
                if (atributo.getType().equals(GumgaAddress.class)) {
                    fw.write("        $scope.entity." + atributo.getName() + " = $scope.entity." + atributo.getName() + " || {};\n");
                }

                if (atributo.getType().equals(GumgaTime.class)) {
                    if (!formatDateInserido) {
                        fw.write(
                                  "        var formatDate = function (array) {\n"
                                + "            var date = new Date();\n"
                                + "            date.setHours(array[0]);\n"
                                + "            date.setMinutes(array[1]);\n"
                                + "            date.setSeconds(array[2]);\n"
                                + "            return date;\n"
                                + "        };");
                        formatDateInserido = true;
                    }
                    fw.write("\n       $scope.entity.id && $scope.entity." + atributo.getName() + " ? $scope.entity." + atributo.getName() + " = {value: formatDate($scope.entity." + atributo.getName() + ".value.split(\":\"))} : angular.noop;  \n\n");
                }

                if (atributo.getType().equals(Date.class)) {
                    fw.write("          $scope.entity.id && $scope.entity." + atributo.getName() + " ? $scope.entity." + atributo.getName() + " = new Date($scope.entity." + atributo.getName() + ") : angular.noop;\n");
                }

                if (atributo.isAnnotationPresent(ManyToMany.class)) {
                    fw.write("         $scope.entity." + atributo.getName() + " = $scope.entity." + atributo.getName() + "  || [];\n\n");

                    fw.write(""
                            + "        populateScope($scope, " + Util.getTipoGenerico(atributo).getSimpleName() + "Service, '" + Util.getTipoGenerico(atributo).getSimpleName() + "', 'many-to-many');\n"
                            + "        populateScope($scope, " + Util.getTipoGenerico(atributo).getSimpleName() + "Service, '" + Util.getTipoGenerico(atributo).getSimpleName() + "', 'base-list');\n"
                            + "        $scope.$on('afterSave', function (){\n"
                            + "           $scope." + Util.getTipoGenerico(atributo).getSimpleName().toLowerCase() + "Get();\n"
                            + "        });\n"
                            + "");

                }

                if (atributo.isAnnotationPresent(OneToMany.class)) {
                    fw.write("         $scope.entity." + atributo.getName() + " = $scope.entity." + atributo.getName() + "  || [];\n\n");
                }
                if (atributo.isAnnotationPresent(ManyToOne.class) || atributo.isAnnotationPresent(OneToOne.class)) {
                    fw.write(" "
                            + "        populateScope($scope, " + Util.getTipoGenerico(atributo).getSimpleName() + "Service, '" + Util.primeiraMaiuscula(atributo.getName()) + "', 'many-to-one');\n"
                            + "\n");
                }

            }

            for (Field atributoGumgaImage : atributosGumgaImage) {

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
                        + "    " + classe.getSimpleName() + "ModalController.$inject = ['" + nomeEntidade + "Service', '$state','$modalInstance','entity','$scope','populateScope'");

                for (Class classe_ : dependencias) {
                    fw.write(",'" + classe_.getSimpleName() + "Service'");
                }

                fw.write(""
                        + "];\n"
                        + "\n"
                        + "    function " + classe.getSimpleName() + "ModalController(" + nomeEntidade + "Service, $state,$modalInstance,entity,$scope,populateScope");

                for (Class classe_ : dependencias) {
                    fw.write("," + classe_.getSimpleName() + "Service");
                }

                fw.write(""
                        + ") {\n");

                fw.write(
                        "		entity = entity || {};\n"
                        + "                $scope.entity = angular.copy(entity)\n"
                        + "");
                for (Field atributo : Util.getTodosAtributosNaoEstaticos(classe)) {
                    if (atributo.isAnnotationPresent(ManyToMany.class)) {
                        fw.write("        $scope.entity." + atributo.getName() + " = $scope.entity." + atributo.getName() + "  || [];\n\n");

                        fw.write(""
                                + "        populateScope($scope, " + Util.getTipoGenerico(atributo).getSimpleName() + "Service, '" + Util.getTipoGenerico(atributo).getSimpleName() + "', 'many-to-many');\n"
                                + "        populateScope($scope, " + Util.getTipoGenerico(atributo).getSimpleName() + "Service, '" + Util.getTipoGenerico(atributo).getSimpleName() + "', 'base-list');\n"
                                + "        $scope.$on('afterSave', function (){\n"
                                + "           $scope." + Util.getTipoGenerico(atributo).getSimpleName().toLowerCase() + "Get();\n"
                                + "        });\n"
                                + "");

                    }

                    if (atributo.isAnnotationPresent(OneToMany.class)) {
                        fw.write("         $scope.entity." + atributo.getName() + " = $scope.entity." + atributo.getName() + "  || [];\n\n");
                    }
                    if (atributo.isAnnotationPresent(ManyToOne.class) || atributo.isAnnotationPresent(OneToOne.class)) {
                        fw.write(" "
                                + "        populateScope($scope, " + Util.getTipoGenerico(atributo).getSimpleName() + "Service, '" + Util.primeiraMaiuscula(atributo.getName()) + "', 'many-to-one');\n"
                                + ""
                                + "\n");
                    }

                }

                for (Field atributo : Util.getTodosAtributosMenosIdAutomatico(classe)) {
                    if (GumgaImage.class.equals(atributo.getType()) || GumgaFile.class.equals(atributo.getType())) {

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
                    + "    " + nomeEntidade + "Service.$inject = ['GumgaRest', '$stateParams'];\n"
                    + "\n"
                    + "    function " + nomeEntidade + "Service(GumgaRest, $stateParams) {\n"
                    + "    var Service = new GumgaRest(APILocation.apiLocation + '/api/" + nomeEntidade.toLowerCase() + "');\n"
            );

            if (!atributosGumgaImage.isEmpty()) {

            }

            for (Class dpEnum : dependenciasEnums) {
                fw.write(Util.IDENTACAO8 + "Service.value" + dpEnum.getSimpleName() + "=[");
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
                    + "       return Service;\n"
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
                    + "    var angular = require('angular');"
                    + "    var " + nomeEntidade + "Service = require('app/modules/" + nomeEntidade.toLowerCase() + "/services/" + nomeEntidade + "Service');\n"
                    + "    return angular.module('app." + nomeEntidade.toLowerCase() + ".services',[])\n"
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
            fw.write("<gumga-nav multi-entity=\"true\" put-url=\"http://www.gumga.com.br/security-api/publicoperations/token/\" title=\"" + nomeEntidade + "\" state=\"login.log\"></gumga-nav>\n"
                    + "<gumga-menu menu-url=\"gumga-menu.json\" keys-url=\"keys.json\"  image=\"resources/images/gumga.png\"></gumga-menu>\n"
                    + "<div class=\"gumga-container\" gumga-alert>\n"
                    + "    <gumga-breadcrumb></gumga-breadcrumb>\n"
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
                    + "<form name=\"" + nomeEntidade + "Form\" gumga-form novalidate>\n"
                    + "    <div class=\"full-width-without-padding\">\n"
                    + "\n");

            boolean primeiro = true;
            geraCampos(fw, this.classeEntidade);
            fw.write(""
                    + "\n"
                    + "        <gumga-errors placement=\"right\" icon=\"glyphicon glyphicon-question-sign\" label=\"Lista de erros\" title=\"Lista de erros\"></gumga-errors>\n"
                    + "        <gumga-form-buttons\n"
                    + "                back=\"" + nomeEntidade.toLowerCase() + ".list\"\n"
                    + "                submit=\"" + nomeEntidade.toLowerCase() + "Update(entity)\"\n"
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
                    + "   <h3 style=\"margin-top: 0\" gumga-translate-tag=\"" + nomeEntidade.toLowerCase() + ".title\"></h3>\n"
                    + "    <div class=\"col-md-5\" style=\"padding-left: 0\">\n"
                    + "        <a ui-sref=\"" + nomeEntidade.toLowerCase() + ".insert\" class=\"btn btn-primary\"><i class=\"glyphicon glyphicon-plus\"></i> New</a>\n"
                    + "        <button type=\"button\" class=\"btn btn-danger\" ng-click=\"" + nomeEntidade.toLowerCase() + "Delete(selectedEntities)\"><i class=\"glyphicon glyphicon-trash\"></i> Delete"
                    + "        </button>\n"
                    + "    </div>\n"
                    + "    <div class=\"col-md-7\" style=\"padding-right: 0\">\n"
                    + "        <gumga-search fields=\"" + Util.todosAtributosSeparadosPorVirgula(classeEntidade) + "\"\n "
                    + "                      advanced=\"true\"\n "
                    + "                      get-queries=\"" + classeEntidade.getSimpleName().toLowerCase() + "GetQuery(page)\" \n "
                    + "                      search-method=\"" + classeEntidade.getSimpleName().toLowerCase() + "Search(field,param)\"\n"
                    + "                      advanced-method=\"" + classeEntidade.getSimpleName().toLowerCase() + "AdvancedSearch(param)\"\n "
                    + "                      translate-entity=\" " + classeEntidade.getSimpleName().toLowerCase() + "\">\n"
                    + "            <advanced-field name=\"id\" type=\"number\"></advanced-field>\n");
            for (Field atributo : Util.getTodosAtributosMenosIdAutomatico(classeEntidade)) {
                fw.write("         <advanced-field name=\"" + atributo.getName() + "\" type=\"" + converteTipoParaAdvanced(atributo.getType()) + "\"></advanced-field>\n");
            }
            fw.write(""
                    + "        </gumga-search>\n"
                    + "    </div>\n"
                    + "        <gumga-table\n"
                    + "            translate-entity=\"" + nomeEntidade.toLowerCase() + "\"\n"
                    + "            name=\"" + nomeEntidade.toLowerCase() + "\"\n"
                    + "            values=\"" + classeEntidade.getSimpleName() + ".content.data.values\"\n");
            fw.write("             columns=\"" + Util.todosAtributosSeparadosPorVirgula(classeEntidade) + "\"\n");
            fw.write(""
                    + "            sort-function=\"" + nomeEntidade.toLowerCase() + "Sort(field,way)\"\n"
                    + "            >\n");
            for (Field atributo : Util.getTodosAtributosMenosIdAutomatico(classeEntidade)) {
                if (GumgaEMail.class.equals(atributo.getType())) {
                    fw.write(Util.IDENTACAO12 + "<object-column column=\"" + atributo.getName().toLowerCase() + "\" property=\"" + atributo.getName().toLowerCase() + ".value\"></object-column>\n");
                } else if (GumgaPhoneNumber.class.equals(atributo.getType())) {
                    fw.write(Util.IDENTACAO12 + "<object-column column=\"" + atributo.getName().toLowerCase() + "\" property=\"" + atributo.getName().toLowerCase() + ".value\"></object-column>\n");
                } else if (GumgaBoolean.class.equals(atributo.getType())) {
                    fw.write(Util.IDENTACAO12 + "<object-column column=\"" + atributo.getName().toLowerCase() + "\" property=\"" + atributo.getName().toLowerCase() + ".value\"></object-column>\n");
                } else if (GumgaCNPJ.class.equals(atributo.getType())) {
                    fw.write(Util.IDENTACAO12 + "<object-column column=\"" + atributo.getName().toLowerCase() + "\" property=\"" + atributo.getName().toLowerCase() + ".value\"></object-column>\n");
                } else if (GumgaURL.class.equals(atributo.getType())) {
                    fw.write(Util.IDENTACAO12 + "<object-column column=\"" + atributo.getName().toLowerCase() + "\" property=\"" + atributo.getName().toLowerCase() + ".value\"></object-column>\n");
                } else if (GumgaAddress.class.equals(atributo.getType())) {
                    fw.write(Util.IDENTACAO12 + "<object-column column=\"" + atributo.getName().toLowerCase() + "\" property=\"" + atributo.getName().toLowerCase() + ".localization\"></object-column>\n");
                } else if (GumgaBarCode.class.equals(atributo.getType())) {
                    fw.write(Util.IDENTACAO12 + "<object-column column=\"" + atributo.getName().toLowerCase() + "\" property=\"" + atributo.getName().toLowerCase() + ".value\"></object-column>\n");
                } else if (GumgaCEP.class.equals(atributo.getType())) {
                    fw.write(Util.IDENTACAO12 + "<object-column column=\"" + atributo.getName().toLowerCase() + "\" property=\"" + atributo.getName().toLowerCase() + ".value\"></object-column>\n");
                } else if (GumgaCPF.class.equals(atributo.getType())) {
                    fw.write(Util.IDENTACAO12 + "<object-column column=\"" + atributo.getName().toLowerCase() + "\" property=\"" + atributo.getName().toLowerCase() + ".value\"></object-column>\n");
                } else if (GumgaFile.class.equals(atributo.getType())) {
                    fw.write(Util.IDENTACAO12 + "<object-column column=\"" + atributo.getName().toLowerCase() + "\" property=\"" + atributo.getName().toLowerCase() + ".name\"></object-column>\n");
                } else if (GumgaImage.class.equals(atributo.getType())) {
                    fw.write(Util.IDENTACAO12 + "<object-column column=\"" + atributo.getName().toLowerCase() + "\" property=\"" + atributo.getName().toLowerCase() + ".name\"></object-column>\n");
                } else if (GumgaMultiLineString.class.equals(atributo.getType())) {
                    fw.write(Util.IDENTACAO12 + "<object-column column=\"" + atributo.getName().toLowerCase() + "\" property=\"" + atributo.getName().toLowerCase() + ".value\"></object-column>\n");
                } else if (GumgaTime.class.equals(atributo.getType())) {
                    fw.write(Util.IDENTACAO12 + "<object-column column=\"" + atributo.getName().toLowerCase() + "\" property=\"" + atributo.getName().toLowerCase() + ".hour\"></object-column>\n");
                } else if (GumgaURL.class.equals(atributo.getType())) {
                    fw.write(Util.IDENTACAO12 + "<object-column column=\"" + atributo.getName().toLowerCase() + "\" property=\"" + atributo.getName().toLowerCase() + ".value\"></object-column>\n");
                }
            }
            fw.write(Util.IDENTACAO12 + "<buttons-column>\n"
                    + Util.IDENTACAO12 + "<a ui-sref=\"" + nomeEntidade.toLowerCase() + ".edit({id: entity.id})\" class=\"btn btn-link pull-right\">Edit</a>\n"
                    + Util.IDENTACAO12 + "</buttons-column>\n"
                    + "        </gumga-table>\n"
                    + "\n"
                    + "    <div class=\"full-width-without-padding\">\n"
                    + "        <pagination ng-model=\"page\"\n"
                    + "                    items-per-page=\"" + classeEntidade.getSimpleName() + ".content.data.pageSize\"\n"
                    + "                    total-items=\"" + classeEntidade.getSimpleName() + ".content.data.count\"\n"
                    + "                    ng-change=\"" + classeEntidade.getSimpleName().toLowerCase() + "Get()\"></pagination>\n"
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

//    private boolean isCampoObrigatorio(Field atributo){
//        boolean obrigatorio = false;
//        if (atributo.isAnnotationPresent(Column.class)) {
//            obrigatorio = atributo.getAnnotation(Column.class).nullable();
//        }
//        return obrigatorio;
//    }
    
    public void geraCampos(FileWriter fw, Class classeEntidade) throws IOException {
        boolean primeiro;
        for (Field atributo : Util.getTodosAtributosMenosIdAutomatico(classeEntidade)) {
            //COLOCAR OS TIPOS
//            boolean requerido = isCampoObrigatorio(atributo);

            fw.write(Util.IDENTACAO4 + Util.IDENTACAO4 + "<!--" + atributo.getName() + " " + atributo.getType() + "-->\n");

            if (atributo.isAnnotationPresent(ManyToOne.class) || atributo.isAnnotationPresent(OneToOne.class)) {
                fw.write(Util.IDENTACAO8 + "<div class=\"full-width-without-padding\">\n "
                        + Util.IDENTACAO12 + "<label for=\"" + atributo.getName() + "\"  gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\"></label>\n"
                        + Util.IDENTACAO12 + "<gumga-many-to-one "
                        + Util.IDENTACAO12 + "value=\"entity." + atributo.getName() + "\"\n"
                        + Util.IDENTACAO12 + "search-method=\"" + atributo.getName() + "AsyncSearch('" + Util.primeiroAtributo(atributo.getType()).getName() + "',param)\"\n"
                        + Util.IDENTACAO12 + "field=\"" + Util.primeiroAtributo(atributo.getType()).getName() + "\"\n"
                        + Util.IDENTACAO12 + "authorize-add=\"true\""
                        + Util.IDENTACAO12 + "add-method=\"" + atributo.getName() + "AsyncSave(value,'" + Util.primeiroAtributo(atributo.getType()).getName() + "')\">\n"
                        + Util.IDENTACAO12 + "</gumga-many-to-one>\n"
                        + Util.IDENTACAO8 + "</div>\n");

            } else if (atributo.isAnnotationPresent(ManyToMany.class)) {
                fw.write(""
                        + "        <div class=\"col-md-6\">\n"
                        + "            <label for=\"" + atributo.getName() + "\"  gumga-translate-tag=\"" + Util.getTipoGenerico(atributo).getSimpleName().toLowerCase() + ".title\"></label>\n"
                        + "        </div>\n"
                        + "        <div class=\"col-md-6\">\n"
                        + "            <label for=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\" gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\"></label>\n"
                        + "        </div>"
                        + "\n");

                fw.write(Util.IDENTACAO8 + "<div class=\"full-width-without-padding\">\n"
                        + Util.IDENTACAO4 + Util.IDENTACAO8 + "<gumga-many-to-many \n"
                        + Util.IDENTACAO4 + Util.IDENTACAO4 + Util.IDENTACAO8 + "left-list=\"" + Util.getTipoGenerico(atributo).getSimpleName() + ".content.data.values\" \n"
                        + Util.IDENTACAO4 + Util.IDENTACAO4 + Util.IDENTACAO8 + "right-list=\"entity." + atributo.getName() + "\" \n"
                        + Util.IDENTACAO4 + Util.IDENTACAO4 + Util.IDENTACAO8 + "left-search=\"" + Util.getTipoGenerico(atributo).getSimpleName().toLowerCase() + "Search(param)\" \n"
                        + Util.IDENTACAO4 + Util.IDENTACAO4 + Util.IDENTACAO8 + "filter-parameters=\"" + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + "\"\n"
                        + Util.IDENTACAO4 + Util.IDENTACAO4 + Util.IDENTACAO8 + "post-method=\"" + Util.getTipoGenerico(atributo).getSimpleName().toLowerCase() + "Save(value,'" + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + "')\"\n"
                        + Util.IDENTACAO4 + Util.IDENTACAO4 + Util.IDENTACAO8 + "authorize-add=\"true\">\n"
                        + Util.IDENTACAO4 + Util.IDENTACAO4 + Util.IDENTACAO8 + "    <left-field>{{$value." + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + "}}</left-field>\n"
                        + Util.IDENTACAO4 + Util.IDENTACAO4 + Util.IDENTACAO8 + "    <right-field>{{$value." + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + "}}</right-field>\n"
                        + Util.IDENTACAO4 + Util.IDENTACAO8 + "</gumga-many-to-many>\n\n"
                        + Util.IDENTACAO8 + "</div>\n"
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
                    fw.write(Util.IDENTACAO8 + "<div class=\"row\">\n"
                            + Util.IDENTACAO12 + "<div class=\"col-md-12\">\n"
                            + Util.IDENTACAO12 + Util.IDENTACAO4 + "<gumga-address name=\"" + atributo.getName() + "\" value=\"entity." + atributo.getName() + "\"> </gumga-address>\n"
                            + Util.IDENTACAO12 + "</div>\n"
                            + Util.IDENTACAO8 + "</div>\n"
                            + "\n");
                } else if (GumgaBarCode.class.equals(atributo.getType())) {
                    fw.write(""
                            + "        <div ng-class=\"{'form-group':" + nomeEntidade + "Form." + atributo.getName() + ".$pristine,'form-group has-error': " + nomeEntidade + "Form." + atributo.getName() + ".$invalid,'form-group has-success': " + nomeEntidade + "Form." + atributo.getName() + ".$valid}\">\n"
                            + "        <input id=\"" + atributo.getName() + "\" gumga-error  type=\"text\" name=\"" + atributo.getName() + "\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" />\n"
                            + "        </div>\n"
                            + "\n");
                } else if (GumgaCEP.class.equals(atributo.getType())) { //TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR A DEPENDENCIA EXTERNA
                    fw.write(""
                            + "        <div ng-class=\"{'form-group':" + nomeEntidade + "Form." + atributo.getName() + ".$pristine,'form-group has-error': " + nomeEntidade + "Form." + atributo.getName() + ".$invalid,'form-group has-success': " + nomeEntidade + "Form." + atributo.getName() + ".$valid}\">\n"
                            + "        <input id=\"" + atributo.getName() + "\" gumga-mask=\"99999-999\" gumga-error type=\"text\" name=\"" + atributo.getName() + "\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\"/>\n"
                            + "        </div>"
                            + "\n");
                } else if (GumgaCNPJ.class.equals(atributo.getType())) { //TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR A DEPENDENCIA EXTERNA
                    fw.write(""
                            + "        <div ng-class=\"{'form-group':" + nomeEntidade + "Form." + atributo.getName() + ".$pristine,'form-group has-error': " + nomeEntidade + "Form." + atributo.getName() + ".$invalid,'form-group has-success': " + nomeEntidade + "Form." + atributo.getName() + ".$valid}\">\n"
                            + "        <input id=\"" + atributo.getName() + "\" gumga-mask=\"99.999.999/9999-99\" gumga-error type=\"text\" name=\"" + atributo.getName() + "\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\"  />\n"
                            + "        </div>"
                            + "\n");
                } else if (GumgaCPF.class.equals(atributo.getType())) { //TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR A DEPENDENCIA EXTERNA
                    fw.write(""
                            + "        <div ng-class=\"{'form-group':" + nomeEntidade + "Form." + atributo.getName() + ".$pristine,'form-group has-error': " + nomeEntidade + "Form." + atributo.getName() + ".$invalid,'form-group has-success': " + nomeEntidade + "Form." + atributo.getName() + ".$valid}\">\n"
                            + "        <input id=\"" + atributo.getName() + "\" gumga-mask=\"999.999.999-99\" gumga-error type=\"text\" name=\"" + atributo.getName() + "\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" />\n"
                            + "        </div>"
                            + "\n");
                } else if (GumgaIP4.class.equals(atributo.getType())) { //TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR O GUMGAMAX E GUMGAMIN
                    fw.write(""
                            + "        <div ng-class=\"{'form-group':" + nomeEntidade + "Form." + atributo.getName() + ".$pristine,'form-group has-error': " + nomeEntidade + "Form." + atributo.getName() + ".$invalid,'form-group has-success': " + nomeEntidade + "Form." + atributo.getName() + ".$valid}\">\n"
                            + "        <input id=\"" + atributo.getName() + "\" gumga-error type=\"text\" name=\"" + atributo.getName() + "\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" gumga-min=\"12\" gumga-max=\"12\"/>\n"
                            + "        </div>"
                            + "\n");
                } else if (GumgaIP6.class.equals(atributo.getType())) { //TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR O GUMGAMAX E GUMGAMIN
                    fw.write(""
                            + "        <div ng-class=\"{'form-group':" + nomeEntidade + "Form." + atributo.getName() + ".$pristine,'form-group has-error': " + nomeEntidade + "Form." + atributo.getName() + ".$invalid,'form-group has-success': " + nomeEntidade + "Form." + atributo.getName() + ".$valid}\">\n"
                            + "        <input id=\"" + atributo.getName() + "\" gumga-error type=\"text\" name=\"" + atributo.getName() + "\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" gumga-min=\"12\" gumga-max=\"12\"/>\n"
                            + "        </div>"
                            + "\n");
                } else if (GumgaFile.class.equals(atributo.getType())) {//TODO SUBSTITUIR PELA DIRETIVA DE IMPORTAÇÃO E ARQUIVO QUANDO ESTIVER PRONTA

                } else if (GumgaImage.class.equals(atributo.getType())) {
                    fw.write(""
                            + "<gumga-upload attribute=\"entity." + atributo.getName() + "\"\n"
                            + "                      upload-method=\"" + nomeEntidade.toLowerCase() + "SaveImage(image)\"\n"
                            + "                      delete-method=\"" + nomeEntidade.toLowerCase() + "DeleteImage(image)\">\n"
                            + "</gumga-upload>\n"
                            + "");

                } else if (GumgaEMail.class.equals(atributo.getType())) {
                    fw.write(""
                            + "        <div ng-class=\"{'form-group':" + nomeEntidade + "Form." + atributo.getName() + ".$pristine,'form-group has-error': " + nomeEntidade + "Form." + atributo.getName() + ".$invalid,'form-group has-success': " + nomeEntidade + "Form." + atributo.getName() + ".$valid}\">\n"
                            + "        <input id=\"" + atributo.getName() + "\" gumga-error type=\"email\" name=\"" + atributo.getName() + "\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" />\n"
                            + "        </div>"
                            + "\n");
                } else if (GumgaMoney.class.equals(atributo.getType())) {//TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR A DEPENDENCIA EXTERNA
                    fw.write(""
                            + "        <div ng-class=\"{'form-group':" + nomeEntidade + "Form." + atributo.getName() + ".$pristine,'form-group has-error': " + nomeEntidade + "Form." + atributo.getName() + ".$invalid,'form-group has-success': " + nomeEntidade + "Form." + atributo.getName() + ".$valid}\">\n"
                            + "        <input id=\"" + atributo.getName() + "\" gumga-error type=\"text\" name=\"" + atributo.getName() + "\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" ui-money-mask=\"2\"/>\n"
                            + "        </div>"
                            + "\n");
                } else if (GumgaPhoneNumber.class.equals(atributo.getType())) {//TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR A DEPENDENCIA EXTERNA
                    fw.write(""
                            + "        <div ng-class=\"{'form-group':" + nomeEntidade + "Form." + atributo.getName() + ".$pristine,'form-group has-error': " + nomeEntidade + "Form." + atributo.getName() + ".$invalid,'form-group has-success': " + nomeEntidade + "Form." + atributo.getName() + ".$valid}\">\n"
                            + "        <input id=\"" + atributo.getName() + "\" gumga-error type=\"text\" name=\"" + atributo.getName() + "\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" ui-br-phone-number/>\n"
                            + "        </div>"
                            + "\n");
                } else if (GumgaURL.class.equals(atributo.getType())) {
                    fw.write(""
                            + "        <div ng-class=\"{'form-group':" + nomeEntidade + "Form." + atributo.getName() + ".$pristine,'form-group has-error': " + nomeEntidade + "Form." + atributo.getName() + ".$invalid,'form-group has-success': " + nomeEntidade + "Form." + atributo.getName() + ".$valid}\">\n"
                            + "        <input id=\"" + atributo.getName() + "\" gumga-error type=\"url\" name=\"" + atributo.getName() + "\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" />\n"
                            + "        </div>"
                            + "\n");
                } else if (GumgaTime.class.equals(atributo.getType())) {
                    fw.write("         <timepicker ng-model=\"entity." + atributo.getName() + ".value\"  show-meridian=\"false\"></timepicker>");
                } else if (GumgaMultiLineString.class.equals(atributo.getType())) {
                    fw.write(""
                            + "        <div ng-class=\"{'form-group':" + nomeEntidade + "Form." + atributo.getName() + ".$pristine,'form-group has-error': " + nomeEntidade + "Form." + atributo.getName() + ".$invalid,'form-group has-success': " + nomeEntidade + "Form." + atributo.getName() + ".$valid}\">\n"
                            + "        <textarea ng-model=\"entity." + atributo.getName() + ".value\" " + geraValidacoesDoBenValidator(atributo) + " class=\"form-control\" placeholder=\"Digite " + Util.etiqueta(atributo) + ".\" rows=\"4\" cols=\"50\"></textarea>\n\n"
                            + "        </div>"
                            + "\n");
                } else if (GumgaGeoLocation.class.equals(atributo.getType())) { //TODO SUBSTITUIR PELO COMPONENTE GUMGAMAPS QUANDO ELE ESTIVER PRONTO
                    fw.write(""
                            + Util.IDENTACAO8 + "<div class=\"row\">\n"
                            + Util.IDENTACAO12 + "<div class=\"col-md-12\">\n"
                            + Util.IDENTACAO12 + Util.IDENTACAO4 + "<label for=\"" + atributo.getName() + ".latitude\">Latitude</label>\n"
                            + Util.IDENTACAO12 + Util.IDENTACAO4 + "<div ng-class=\"{'form-group':" + nomeEntidade + "Form." + atributo.getName() + ".$pristine,'form-group has-error': " + nomeEntidade + "Form." + atributo.getName() + ".$invalid,'form-group has-success': " + nomeEntidade + "Form." + atributo.getName() + ".$valid}\">\n"
                            + Util.IDENTACAO12 + Util.IDENTACAO4 + "<input id=\"" + atributo.getName() + ".latitude\" gumga-error type=\"text\" name=\"" + atributo.getName() + ".latitude\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"entity." + atributo.getName() + ".latitude\" class=\"form-control\" />\n"
                            + Util.IDENTACAO12 + Util.IDENTACAO4 + "</div>"
                            + Util.IDENTACAO12 + Util.IDENTACAO4 + "<label for=\"" + atributo.getName() + ".longitude\">Longitude</label>\n"
                            + Util.IDENTACAO12 + Util.IDENTACAO4 + "<div ng-class=\"{'form-group':" + nomeEntidade + "Form." + atributo.getName() + ".$pristine,'form-group has-error': " + nomeEntidade + "Form." + atributo.getName() + ".$invalid,'form-group has-success': " + nomeEntidade + "Form." + atributo.getName() + ".$valid}\">\n"
                            + Util.IDENTACAO12 + Util.IDENTACAO4 + "<input id=\"" + atributo.getName() + ".longitude\" gumga-error type=\"text\" name=\"" + atributo.getName() + ".longitude\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"entity." + atributo.getName() + ".longitude\" class=\"form-control\" />\n"
                            + Util.IDENTACAO12 + Util.IDENTACAO4 + "</div>"
                            + Util.IDENTACAO12 + Util.IDENTACAO4 + "<a class=\"btn btn-default\" ng-href=\"http://maps.google.com/maps?q={{entity." + atributo.getName() + ".latitude + ',' + entity." + atributo.getName() + ".longitude}}\" target=\"_blank\"> <p class=\"glyphicon glyphicon-globe\"></p> GOOGLE MAPS</a>\n"
                            + Util.IDENTACAO12 + "</div>\n"
                            + Util.IDENTACAO8 + "</div>\n"
                            + "\n");
                } else if (GumgaBoolean.class.equals(atributo.getType())) {
                    fw.write(""
                            + "        <div ng-class=\"{'form-group':" + nomeEntidade + "Form." + atributo.getName() + ".$pristine,'form-group has-error': " + nomeEntidade + "Form." + atributo.getName() + ".$invalid,'form-group has-success': " + nomeEntidade + "Form." + atributo.getName() + ".$valid}\">\n"
                            + "        <input style=\"width:15px\" gumga-error type=\"checkbox\" " + geraValidacoesDoBenValidator(atributo) + " name=\"" + atributo.getName() + "\" ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" />\n"
                            + "        </div>"
                            + "\n");
                } else if (Date.class.equals(atributo.getType())) {
                    String varOpened = "opened"+ Util.primeiraMaiuscula(atributo.getName());
                    fw.write(""
                            + "        <div ng-class=\"{'form-group':" + nomeEntidade + "Form." + atributo.getName() + ".$pristine,'form-group has-error': " + nomeEntidade + "Form." + atributo.getName() + ".$invalid,'form-group has-success': " + nomeEntidade + "Form." + atributo.getName() + ".$valid}\">\n"
                            + "        <input type=\"text\" name=\"" + atributo.getName() + "\" class=\"form-control\" " + geraValidacoesDoBenValidator(atributo) + " datepicker-popup=\"fullDate\" ng-model=\"entity." + atributo.getName() + "\" is-open=\""+varOpened+"\" ng-click=\""+varOpened+"= !"+varOpened+"\" close-text=\"Close\" />"
                            + "        </div>"
                            + "\n");

                } else if (atributo.getType().isEnum()) {
                    Object[] constants = atributo.getType().getEnumConstants();
                    fw.write(Util.IDENTACAO8 + "<select class='form-control' gumga-error name=\"" + atributo.getName() + "\" ng-model=\"entity." + atributo.getName() + "\" >\n");
                    fw.write(Util.IDENTACAO12 + "<option  ng-selected=\"value.value === entity." + atributo.getName() + "\"  value=\"{{value.value}}\" ng-repeat=\"value in value" + atributo.getType().getSimpleName() + "\">{{value.label}}</option>");
                    fw.write(Util.IDENTACAO8 + "</select>\n");
                } else {
                    fw.write(""
                            + "        <div ng-class=\"{'form-group':" + nomeEntidade + "Form." + atributo.getName() + ".$pristine,'form-group has-error': " + nomeEntidade + "Form." + atributo.getName() + ".$invalid,'form-group has-success': " + nomeEntidade + "Form." + atributo.getName() + ".$valid}\">\n"
                            + "        <input id=\"" + atributo.getName() + "\" gumga-error type=\"text\" name=\"" + atributo.getName() + "\" ng-model=\"entity." + atributo.getName() + "\"" + geraValidacoesDoBenValidator(atributo) + "  class=\"form-control\" />\n"
                            + "        </div>"
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
            aRetornar += " gumga-required gumga-min-length=\"1\"";
        }

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
                    + "    var APILocation = require('app/apiLocations');\n"
                    + "\n"
                    + "    angular.module('app." + nomeEntidade.toLowerCase() + "', ['ui.router', 'app." + nomeEntidade.toLowerCase() + ".controllers', 'app." + nomeEntidade.toLowerCase() + ".services', 'gumga.core'])\n"
                    + "        .config(function ($stateProvider, $httpProvider, $populateProvider) {\n"
                    + "            $stateProvider\n"
                    + "                .state('" + nomeEntidade.toLowerCase() + ".list', {\n"
                    + "                    url: '/list',\n"
                    + "                    templateUrl: 'app/modules/" + nomeEntidade.toLowerCase() + "/views/list.html',\n"
                    + "                    controller: '" + nomeEntidade + "ListController',\n"
                    + "                    data: {\n"
                    + "                         id: 2 \n"
                    + "                    },"
                    + "                    resolve:  {\n"
                    + "                       populateScope: function(){\n"
                    + "                         return $populateProvider.populateScope;\n"
                    + "                       }\n"
                    + "                     }\n"
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
                    + "                        }],\n"
                    + "                        populateScope: function(){\n"
                    + "                            return $populateProvider.populateScope;\n"
                    + "                        }\n"
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
                    + "                        }],\n"
                    + "                        populateScope: function(){\n"
                    + "                            return $populateProvider.populateScope;\n"
                    + "                        }\n"
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
