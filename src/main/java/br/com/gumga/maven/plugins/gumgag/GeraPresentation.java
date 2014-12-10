/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.gumga.maven.plugins.gumgag;

import gumga.framework.domain.domains.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
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
@Mojo(name = "apresentacao", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GeraPresentation extends AbstractMojo {

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
    private String nomePacoteWeb;
    private String pastaApi;
    private String pastaWeb;
    private String pastaJSP;

    private Class classeEntidade;
    private String pastaScripts;
    private String pastaResources;

    private List<Class> dependenciasManyToOne;
    private List<Class> dependenciasOneToMany;
    private List<Class> dependenciasManyToMany;
    private String pastaControllers;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Util.geraGumga(getLog());

        try {
            nomePacoteBase = nomeCompletoEntidade.substring(0, nomeCompletoEntidade.lastIndexOf(".domain"));
            nomeEntidade = nomeCompletoEntidade.substring(nomeCompletoEntidade.lastIndexOf('.') + 1);

            nomePacoteApi = nomePacoteBase + ".presentation.api";
            nomePacoteWeb = nomePacoteBase + ".presentation.web";

            pastaApi = project.getCompileSourceRoots().get(0) + "/".concat(nomePacoteApi.replaceAll("\\.", "/"));
            pastaWeb = project.getCompileSourceRoots().get(0) + "/".concat(nomePacoteWeb.replaceAll("\\.", "/"));
            pastaJSP = project.getFile().getParent() + "/src/main/webapp/WEB-INF/views/crud/" + (nomeEntidade.toLowerCase());
            pastaScripts = project.getFile().getParent() + "/src/main/webapp/WEB-INF/static/scripts/app//" + (nomeEntidade.toLowerCase());
            pastaResources = project.getFile().getParent() + "/src/main/resources/";

            getLog().info("Iniciando plugin Gerador de Classes de Apresentação ");
            getLog().info("Gerando para " + nomeEntidade);

            File f = new File(pastaScripts);
            f.mkdirs();
            pastaControllers = pastaScripts + "/controllers";
            f = new File(pastaControllers);
            f.mkdirs();

            classeEntidade = Util.getClassLoader(project).loadClass(nomeCompletoEntidade);

            dependenciasManyToOne = new ArrayList<>();
            dependenciasManyToMany = new ArrayList<>();
            dependenciasOneToMany = new ArrayList<>();

            for (Field atributo : Util.getTodosAtributosMenosIdAutomatico(classeEntidade)) {
                if (atributo.isAnnotationPresent(ManyToOne.class)) {
                    dependenciasManyToOne.add(atributo.getType());
                }
                if (atributo.getType().equals(List.class) || atributo.getType().equals(Set.class) || atributo.getType().equals(Map.class)) {
                    if (atributo.isAnnotationPresent(ManyToMany.class)) {
                        dependenciasManyToMany.add(Util.getTipoGenerico(atributo));
                    }
                    if (atributo.isAnnotationPresent(OneToMany.class)) {
                        dependenciasOneToMany.add(Util.getTipoGenerico(atributo));
                    }
                }
            }

            geraJSPs();
            geraWeb();
            geraModuleJs();
            geraServiceJs();
            geraFormJs();
            geraListJs();
            adicionaAoMenu();
        } catch (Exception ex) {
            getLog().error(ex);
        }

    }

    private void geraWeb() {
        File f = new File(pastaWeb);
        f.mkdirs();
        File arquivoClasse = new File(pastaWeb + "/" + nomeEntidade + "Controller.java");
        try {
            FileWriter fw = new FileWriter(arquivoClasse);
            fw.write(""
                    + "package " + nomePacoteWeb + ";\n"
                    + "\n"
                    + "import gumga.framework.presentation.GumgaCRUDController;\n"
                    + "import org.springframework.stereotype.Controller;\n"
                    + "import org.springframework.web.bind.annotation.RequestMapping;\n"
                    + "\n"
                    + "@Controller\n"
                    + "@RequestMapping(\"/" + nomeEntidade.toLowerCase() + "\")\n"
                    + "public class " + nomeEntidade + "Controller extends GumgaCRUDController {\n"
                    + "\n"
                    + "	@Override\n"
                    + "	public String path() {\n"
                    + "		return \"crud/" + nomeEntidade.toLowerCase() + "\";\n"
                    + "	}\n"
                    + "\n"
                    + "}\n"
                    + ""
                    + "\n");

            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }

    }

    private void geraJSPs() {

        File f = new File(pastaJSP);
        f.mkdirs();

        try {
            File arquivoBase = new File(pastaJSP + "/base.jsp");
            FileWriter fwBase = new FileWriter(arquivoBase);
            fwBase.write(""
                    + "<%@ page language=\"java\" contentType=\"text/html; charset=UTF-8\" pageEncoding=\"UTF-8\"%>\n"
                    + "<%@ taglib uri=\"http://java.sun.com/jsp/jstl/core\" prefix=\"c\"%>\n"
                    + "<%@ taglib uri=\"http://gumga.com.br/jsp/tags\" prefix=\"g\"%>\n"
                    + "\n"
                    + "<g:basetemplate init=\"app/" + nomeEntidade.toLowerCase() + "/module\" title=\"Cadastro de " + nomeEntidade + "\" openMenu=\"" + nomeEntidade.toLowerCase() + "\">\n"
                    + "	<div ui-view></div>\n"
                    + "</g:basetemplate>"
                    + "");
            fwBase.close();

            File arquivoForm = new File(pastaJSP + "/form.jsp");
            FileWriter fwForm = new FileWriter(arquivoForm);
            fwForm.write(""
                    + "<%@ page language=\"java\" contentType=\"text/html; charset=UTF-8\" pageEncoding=\"UTF-8\"%>\n"
                    + "<%@ taglib uri=\"http://gumga.com.br/jsp/tags\" prefix=\"g\" %>\n"
                    + "<g:form>\n"
                    + "\n");

            geraCampos(fwForm);

            fwForm.write(""
                    + "	\n"
                    + "</g:form>"
                    + "");
            fwForm.close();

            File arquivoList = new File(pastaJSP + "/list.jsp");
            FileWriter fwList = new FileWriter(arquivoList);
            Field primeiroAtributo = Util.getTodosAtributosMenosIdAutomatico(classeEntidade).get(0);
            String nomeAtributo = primeiroAtributo.getName();
            String etiqueta = Util.primeiraMaiuscula(nomeAtributo);

            fwList.write(""
                    + "<%@ page language=\"java\" contentType=\"text/html; charset=UTF-8\" pageEncoding=\"UTF-8\"%>\n"
                    + "<%@ taglib uri=\"http://gumga.com.br/jsp/tags\" prefix=\"g\" %>\n"
                    + "\n"
                    + "<g:grid values=\"list.values\">\n"
                    + "    <jsp:attribute name=\"searchFields\">\n"
                    + "        <gumga:search:field field=\"" + nomeAtributo + "\" label=\"" + etiqueta + "\" selected=\"true\"></gumga:search:field>\n"
                    + "        </jsp:attribute>\n"
                    + "    <jsp:attribute name=\"advancedFields\">\n"
                    + "        <gumga:filter:item field=\"" + nomeAtributo + "\" label=\"" + etiqueta + "\"></gumga:filter:item>\n"
                    + "        </jsp:attribute>"
                    + "\n"
                    + "    <jsp:attribute name=\"gridColumns\">\n"
                    + "        <gumga:column sort-field=\"" + nomeAtributo + "\" label=\"" + etiqueta + "\">{{$value." + nomeAtributo + "}}</gumga:column>\n"
                    + "        <gumga:column label=\"\">\n"
                    + "            <div class=\"text-right\">\n"
                    + "                <a href=\"#/edit/{{$value.id}}\" class=\"btn btn-primary\" title=\"Editar\">\n"
                    + "                    <i class=\"glyphicon glyphicon-pencil\"></i>\n"
                    + "                </a>\n"
                    + "            </div>\n"
                    + "        </gumga:column>\n"
                    + "    </jsp:attribute>\n"
                    + "</g:grid>"
                    + "");
            fwList.close();

        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

    public void geraCampos(FileWriter fwForm) throws IOException {
        boolean primeiro = true;
        for (Field atributo : Util.getTodosAtributosMenosIdAutomatico(classeEntidade)) {

            Class<?> type = atributo.getType();
            String nomeAtributo = atributo.getName();
            String etiqueta = Util.primeiraMaiuscula(nomeAtributo);
            boolean requerido = false; // VERIFICAR
            fwForm.write("\n\n<!--" + type + "-->\n");
            if (atributo.isAnnotationPresent(ManyToOne.class)) {
                geraGumgaSelect(type, fwForm, nomeAtributo, etiqueta);

            } else if (atributo.isAnnotationPresent(OneToOne.class)) {
                geraGumgaSelect(type, fwForm, nomeAtributo, etiqueta);

            } else if (atributo.isAnnotationPresent(OneToMany.class)) {

                geraModal(atributo);

                fwForm.write(""
                        + "	<div class=\"form-group\" gumga-form-group=\"" + nomeAtributo + "\">\n"
                        + "		<label class=\"control-label\">" + etiqueta + "</label>\n"
                        + "		<label class=\"control-label\">" + type + " (OneToMany)</label>\n"
                        + "	</div>\n");

            } else if (atributo.isAnnotationPresent(ManyToMany.class)) {
                fwForm.write(""
                        + ""
                        + "    <div class=\"form-group\">\n"
                        + "        \n"
                        + "        <div class=\"row\">\n"
                        + "            <div class=\"col-md-6\">\n"
                        + "                <label class=\"control-label\">" + Util.primeiraMaiuscula(atributo.getName()) + " Disponíveis</label>\n"
                        + "                <input type=\"text\" ng-model=\"filtro\" class=\"form-control\" ng-change=\"ctrl.refresh" + Util.getTipoGenerico(atributo).getSimpleName() + "(filtro)\"/><br>\n"
                        + "                <ul class=\"list-group\">\n"
                        + "                    <li class=\"list-group-item\" ng-repeat=\"f in lista" + Util.getTipoGenerico(atributo).getSimpleName() + Util.primeiraMaiuscula(atributo.getName()) + " | filter:filtro  | orderBy: '" + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + "'\" ng-show=\"!ctrl.contains" + Util.primeiraMaiuscula(atributo.getName()) + "(f)\">\n"
                        + "                        <button type=\"button\" class=\"btn btn-default btn-lg btn-block\" ng-click=\"ctrl.add" + Util.primeiraMaiuscula(atributo.getName()) + "(f)\" >{{f." + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + "}}</button>\n"
                        + "                    </li>\n"
                        + "                </ul>\n"
                        + "            </div>\n"
                        + "\n"
                        + "            <div class=\"col-md-6\">\n"
                        + "                <label class=\"control-label\">" + Util.primeiraMaiuscula(atributo.getName()) + "</label>\n"
                        + "                <input type=\"text\" ng-model=\"filtroEscolhidos\" class=\"form-control\" /> <br>\n"
                        + "                <ul class=\"list-group\">\n"
                        + "                    <li class=\"list-group-item\" ng-repeat=\"f in entity." + atributo.getName() + "| filter:filtroEscolhidos | orderBy: '" + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + "'\">\n"
                        + "                        <button type=\"button\" class=\"btn btn-default btn-lg btn-block\" ng-click=\"ctrl.remove" + Util.primeiraMaiuscula(atributo.getName()) + "(f)\">{{f." + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + "}}</button>\n"
                        + "                    </li>\n"
                        + "                </ul>\n"
                        + "            </div>\n"
                        + "\n"
                        + "        </div>\n"
                        + "    </div>"
                        + ""
                        + "\n");
            } else if (Boolean.class.equals(type) || Boolean.TYPE.equals(type)) {
                fwForm.write(""
                        + "    <div class=\"form-group\" gumga-form-group=\"" + nomeAtributo + "\">\n"
                        + "        <label><input type=\"checkbox\" name=\"" + nomeAtributo + "\" ng-model=\"entity." + nomeAtributo + "\" /> " + etiqueta + "</label>\n"
                        + "        <gumga:input:errors field=\"" + nomeAtributo + "\"></gumga:input:errors>\n"
                        + "    </div>"
                        + ""
                );
            } else if (BigDecimal.class.equals(type)) {
                fwForm.write(""
                        + "	<div class=\"form-group\" gumga-form-group=\"" + nomeAtributo + "\">\n"
                        + "		<label class=\"control-label\">" + etiqueta + "</label>\n"
                        + "		<input name=\"descricao\" class=\"form-control\" ng-model=\"entity." + nomeAtributo + "\" required=\"" + requerido + "\"" + (primeiro ? "autofocus" : "") + " gumga-number decimal-places=\"2\" />\n"
                        + "		<gumga:input:errors field=\"" + nomeAtributo + "\"></gumga:input:errors>\n"
                        + "	</div>\n");
            } else if (GumgaAddress.class.equals(type)) {
                fwForm.write(""
                        + ""
                        + " <%-- CEP --%>\n"
                        + "    <gumga:accordion close-others=\"true\" >\n"
                        + "        <gumga:accordion:group heading=\"" + etiqueta + "\" is-open=\"true\">\n"
                        + "            <div class=\"form-group\" gumga-form-group=\"cep\"> \n"
                        + "                <input name=\"cep\" size=\"9\" ng-model=\"entity." + nomeAtributo + ".cep\"  gumga-mask=\"99999-999\" required=\"true\"/>\n"
                        + "                <button class=\"btn btn-xs btn-primary\" ng-click=\"ctrl." + nomeAtributo + "UpdateAddress()\">Procurar Endereço <span class=\"glyphicon glyphicon-search\"></span></button><br><br>\n"
                        + "\n"
                        + "                <select  ng-options=\"ps for ps in pais\" ng-model=\"entity." + nomeAtributo + ".pais\" required=\"true\"></select>\n"
                        + "\n"
                        + "                <input name=\"descricao\" class=\"form-group-sm\" ng-model=\"entity." + nomeAtributo + ".localidade\" required=\"false\" placeholder=\"Localidade\" />\n"
                        + "                <select  ng-options=\"uf for uf in allUF track by uf\" ng-model=\"entity." + nomeAtributo + ".uf\" required=\"true\" >\n"
                        + "                </select>\n"
                        + "            </div>\n"
                        + "\n"
                        + "            <%-- TipoLogradouro/Logradouro/Número//Complemento//Bairro --%>\n"
                        + "\n"
                        + "            <div class=\"form-group\" gumga-form-group=\" numero\">\n"
                        + "                <select required=\"true\" ng-options=\"log for log in allLogradouro\" ng-model=\"entity." + nomeAtributo + ".tipoLogradouro\"></select>\n"
                        + "                <input name=\"descricao\" size=\"25\" ng-model=\"entity." + nomeAtributo + ".logradouro\"  placeholder=\"Nome do Logradouro\" required=\"false\" />\n"
                        + "                <input type=\"text\" size=\"6\" ng-model=\"entity." + nomeAtributo + ".numero\" placeholder=\"Número\" autofocus=\"\" required=\"true\"> <br><br>\n"
                        + "                <input name=\"descricao\" size=\"25\" ng-model=\"entity." + nomeAtributo + ".complemento\" placeholder=\"Complemento\"/>\n"
                        + "                <input name=\"descricao\" ng-model=\"entity." + nomeAtributo + ".bairro\" required=\"false\" placeholder=\"Bairro\" />\n"
                        + "            </div>\n"
                        + "            <a ng-href=\"https://www.google.com.br/maps/place/{{entity." + nomeAtributo + ".tipoLogradouro + ',' + entity." + nomeAtributo + ".logradouro + ',' + entity." + nomeAtributo + ".numero + ',' + entity." + nomeAtributo + ".localidade}}\" target=\"_blank\" class=\"btn btn-primary btn-primary\">GOOGLE MAPS <span class=\"glyphicon glyphicon-globe\"></span></a>\n"
                        + "        </gumga:accordion:group>\n"
                        + "    </gumga:accordion>\n");
            } else if (GumgaBoolean.class.equals(type)) {
                fwForm.write(""
                        + "    <div class=\"form-group\" gumga-form-group=\"" + nomeAtributo + "\">\n"
                        + "        <label><input type=\"checkbox\" name=\"" + nomeAtributo + "\" ng-model=\"entity." + nomeAtributo + ".value\" /> " + etiqueta + "</label>\n"
                        + "        <gumga:input:errors field=\"" + nomeAtributo + "\"></gumga:input:errors>\n"
                        + "    </div>\n");
            } else if (GumgaCEP.class.equals(type)) {
                fwForm.write(""
                        + "	<div class=\"form-group\" gumga-form-group=\"" + nomeAtributo + "\">\n"
                        + "		<label class=\"control-label\">" + etiqueta + "</label>\n"
                        + "		<input name=\"descricao\" class=\"form-control\" ng-model=\"entity." + nomeAtributo + ".value\" required=\"" + requerido + "\" gumga-mask=\"99999-999\" " + (primeiro ? "autofocus" : "") + " />\n"
                        + "		<gumga:input:errors field=\"" + nomeAtributo + "\"></gumga:input:errors>\n"
                        + "	</div>\n");
            } else if (GumgaCNPJ.class.equals(type)) {
                fwForm.write(""
                        + "	<div class=\"form-group\" gumga-form-group=\"" + nomeAtributo + "\">\n"
                        + "		<label class=\"control-label\">" + etiqueta + "</label>\n"
                        + "		<input name=\"descricao\" class=\"form-control\" ng-model=\"entity." + nomeAtributo + ".value\" required=\"" + requerido + "\" gumga-mask=\"99.999.999/9999-99\" " + (primeiro ? "autofocus" : "") + " />\n"
                        + "		<gumga:input:errors field=\"" + nomeAtributo + "\"></gumga:input:errors>\n"
                        + "	</div>\n");
            } else if (GumgaCPF.class.equals(type)) {
                fwForm.write(""
                        + "	<div class=\"form-group\" gumga-form-group=\"" + nomeAtributo + "\">\n"
                        + "		<label class=\"control-label\">" + etiqueta + "</label>\n"
                        + "		<input name=\"descricao\" class=\"form-control\" ng-model=\"entity." + nomeAtributo + ".value\" required=\"" + requerido + "\"  gumga-mask=\"999.999.999-99\" " + (primeiro ? "autofocus" : "") + " />\n"
                        + "		<gumga:input:errors field=\"" + nomeAtributo + "\"></gumga:input:errors>\n"
                        + "	</div>\n");
            } else if (GumgaEMail.class.equals(type)) {
                fwForm.write(""
                        + "	<div class=\"form-group\" gumga-form-group=\"" + nomeAtributo + "\">\n"
                        + "		<label class=\"control-label\">" + etiqueta + "</label>\n"
                        + "		<input type=\"email\"  name=\"descricao\" class=\"form-control\" ng-model=\"entity." + nomeAtributo + ".value\" required=\"" + requerido + "\"" + (primeiro ? "autofocus" : "") + " />\n"
                        + "		<gumga:input:errors field=\"" + nomeAtributo + "\"></gumga:input:errors>\n"
                        + "	</div>\n");
            } else if (GumgaFile.class.equals(type)) {
                /*
                 fwForm.write(""
                 + "	<div class=\"form-group\" gumga-form-group=\"" + nomeAtributo + "\">\n"
                 + "		<label class=\"control-label\">" + etiqueta + "</label>\n"
                 + "		<input name=\"descricao\" class=\"form-control\" ng-model=\"entity." + nomeAtributo + ".value\" required=\"" + requerido + "\"" + (primeiro ? "autofocus" : "") + " />\n"
                 + "		<gumga:input:errors field=\"" + nomeAtributo + "\"></gumga:input:errors>\n"
                 + "	</div>\n");*/
            } else if (GumgaGeoLocation.class.equals(type)) {
                fwForm.write(""
                        + "    <div class=\"form-group\" gumga-form-group=\"" + nomeAtributo + "\">\n"
                        + " 	       <label class=\"control-label\">" + etiqueta + "</label>\n"
                        + "            <input type=\"text\" name=\"descricao\" ng-model=\"entity." + nomeAtributo + ".latitude\" required=\"true\" min=\"-90\" max=\"90\" " + (primeiro ? "autofocus" : "") + "  gumga-number decimal-places=\"8\" />      \n"
                        + "            <input type=\"text\" name=\"descricao\" ng-model=\"entity." + nomeAtributo + ".longitude\" required=\"true\" min=\"-180\" max=\"180\" gumga-number decimal-places=\"8\" />     \n"
                        + "            <a ng-href=\"http://maps.google.com/maps?q={{entity." + nomeAtributo + ".latitude + ',' + entity." + nomeAtributo + ".longitude}}\" target=\"_blank\"> <p class=\"glyphicon glyphicon-globe\"></p> GOOGLE MAPS</a>\n"
                        + "    </div>"
                        + ""
                        + ""
                        + "");
            } else if (GumgaIP4.class.equals(type)) {
                fwForm.write(""
                        + "	<div class=\"form-group\" gumga-form-group=\"" + nomeAtributo + "\">\n"
                        + "		<label class=\"control-label\">" + etiqueta + "</label>\n"
                        + "		<input name=\"descricao\" class=\"form-control\" ng-model=\"entity." + nomeAtributo + ".value\" required=\"" + requerido + "\" gumga-mask=\"999.999.999.999\" " + (primeiro ? "autofocus" : "") + " />\n"
                        + "		<gumga:input:errors field=\"" + nomeAtributo + "\"></gumga:input:errors>\n"
                        + "	</div>\n");
            } else if (GumgaIP6.class.equals(type)) {
                fwForm.write(""
                        + "	<div class=\"form-group\" gumga-form-group=\"" + nomeAtributo + "\">\n"
                        + "		<label class=\"control-label\">" + etiqueta + "</label>\n"
                        + "		<input name=\"descricao\" class=\"form-control\" ng-model=\"entity." + nomeAtributo + ".value\" required=\"" + requerido + "\"" + (primeiro ? "autofocus" : "") + " />\n"
                        + "		<gumga:input:errors field=\"" + nomeAtributo + "\"></gumga:input:errors>\n"
                        + "	</div>\n");
            } else if (GumgaImage.class.equals(type)) {
                fwForm.write(""
                        + ""
                        + "<form name=\"myForm\">\n"
                        + "  	<fieldset>\n"
                        + "	    " + nomeAtributo + ": <input ng-file-select=\"\" ng-model=\"picFile\" name=\"file\" accept=\"image/*\" ng-file-change=\"generateThumb(picFile[0], $files)\" required=\"\" type=\"file\">\n"
                        + "	<button ng-disabled=\"!myForm.$valid\" ng-click=\"uploadPic(picFile)\">Submit</button>\n"
                        + "  	</fieldset>\n"
                        + "</form>"
                        + "");

            } else if (GumgaMoney.class.equals(type)) {
                fwForm.write(""
                        + "	<div class=\"form-group\" gumga-form-group=\"" + nomeAtributo + "\">\n"
                        + "		<label class=\"control-label\">" + etiqueta + "</label>\n"
                        + "		<input name=\"descricao\" class=\"form-control\" ng-model=\"entity." + nomeAtributo + ".value\" required=\"" + requerido + "\"" + (primeiro ? "autofocus" : "") + "  gumga-number decimal-places=\"2\"  />\n"
                        + "		<gumga:input:errors field=\"" + nomeAtributo + "\"></gumga:input:errors>\n"
                        + "             <p class=\"help-block\">Valor: {{entity.money.value| currency }}</p>\n"
                        + "	</div>\n");
            } else if (GumgaMultiLineString.class.equals(type)) {
                fwForm.write(""
                        + "	<div class=\"form-group\" gumga-form-group=\"" + nomeAtributo + "\">\n"
                        + "             <label class=\"control-label\">" + etiqueta + "</label><br>\n"
                        + "             <textarea ng-model=\"entity." + nomeAtributo + ".value\" class=\"form-control\" placeholder=\"Digite " + etiqueta + ".\" rows=\"4\" cols=\"50\" ng-model=\"entity.multiLine.value\" required=\"" + requerido + "\"" + (primeiro ? "autofocus" : "") + " ></textarea>\n"
                        + "		<gumga:input:errors field=\"" + nomeAtributo + "\"></gumga:input:errors>\n"
                        + "	</div>\n");
            } else if (GumgaPhoneNumber.class.equals(type)) {
                fwForm.write(""
                        + "	<div class=\"form-group\" gumga-form-group=\"" + nomeAtributo + "\">\n"
                        + "		<label class=\"control-label\">" + etiqueta + "</label>\n"
                        + "		<input name=\"descricao\" class=\"form-control\" ng-model=\"entity." + nomeAtributo + ".value\" required=\"" + requerido + "\"" + (primeiro ? "autofocus" : "") + " />\n"
                        + "		<gumga:input:errors field=\"" + nomeAtributo + "\"></gumga:input:errors>\n"
                        + "	</div>\n");

            } else if (GumgaTime.class.equals(type)) {
                fwForm.write(""
                        + "    <div class=\"form-group\" gumga-form-group=\"" + nomeAtributo + "\">\n"
                        + "        <label class=\"control-label\">" + nomeAtributo + ":</label><br>\n"
                        + "        <input type=\"number\" size=\"20\" ng-model=\"entity." + nomeAtributo + ".hour\" max=\"23\" min=\"0\" required=\"true\"/>\n"
                        + "        <input type=\"number\" size=\"20\" ng-model=\"entity." + nomeAtributo + ".minute\" max=\"59\" min=\"0\" required=\"true\"/>\n"
                        + "        <input type=\"number\" size=\"20\" ng-model=\"entity." + nomeAtributo + ".second\" max=\"59\" min=\"0\" required=\"true\"/>\n"
                        + "        <p class=\"help-block\">" + nomeAtributo + ": {{ entity." + nomeAtributo + ".hour + ':' + entity." + nomeAtributo + ".minute + ':' + entity." + nomeAtributo + ".second }}</p>\n"
                        + "    </div>"
                        + ""
                        + ""
                        + "");

            } else if (GumgaURL.class.equals(type)) {
                fwForm.write(""
                        + "	<div class=\"form-group\" gumga-form-group=\"" + nomeAtributo + "\">\n"
                        + "		<label class=\"control-label\">" + etiqueta + "</label>\n"
                        + "		<input name=\"descricao\" class=\"form-control\" ng-model=\"entity." + nomeAtributo + ".value\" required=\"" + requerido + "\"" + (primeiro ? "autofocus" : "") + " />\n"
                        + "		<gumga:input:errors field=\"" + nomeAtributo + "\"></gumga:input:errors>\n"
                        + "	</div>\n");

            } else {
                fwForm.write(""
                        + "	<div class=\"form-group\" gumga-form-group=\"" + nomeAtributo + "\">\n"
                        + "		<label class=\"control-label\">" + etiqueta + "</label>\n"
                        + "		<input name=\"descricao\" class=\"form-control\" ng-model=\"entity." + nomeAtributo + "\" required=\"" + requerido + "\"" + (primeiro ? "autofocus" : "") + " />\n"
                        + "		<gumga:input:errors field=\"" + nomeAtributo + "\"></gumga:input:errors>\n"
                        + "	</div>\n");
            }
            primeiro = false;
        }
    }

    public void geraGumgaSelect(Class<?> type, FileWriter fwForm, String nomeAtributo, String etiqueta) throws IOException {
        String nomePrimeiroAtributo = Util.primeiroAtributo(type).getName();

        fwForm.write("<!--OneToOne -->\n"
                + "    <div class=\"form-group\" gumga-form-group=\"" + nomeAtributo + "\">\n"
                + "        <label class=\"control-label\">" + etiqueta + "</label>\n"
                + "\n"
                + "        <gumga:select ng-model=\"entity." + nomeAtributo + "\">\n"
                + "            <gumga:select:match placeholder=\"Selecione um " + etiqueta + "...\">{{$select.selected." + nomePrimeiroAtributo + "}}</gumga:select:match>\n"
                + "            <gumga:select:choices repeat=\"" + nomeAtributo.toLowerCase() + " in lista" + type.getSimpleName() + " track by $index\" refresh=\"ctrl.refreshLista" + type.getSimpleName() + "($select.search)\" refresh-delay=\"0\">\n"
                + "                {{" + nomeAtributo.toLowerCase() + "." + nomePrimeiroAtributo + "}}\n"
                + "            </gumga:select:choices>\n"
                + "        </gumga:select>\n"
                + "\n"
                + "\n"
                + "        <gumga:input:errors field=\"" + nomeAtributo + "\"></gumga:input:errors>\n"
                + "    </div>\n"
                + "");
    }

    public void geraListJs() throws IOException {
        File arquivoList = new File(pastaControllers + "/list.js");
        FileWriter fwList = new FileWriter(arquivoList);

        fwList.write(""
                + "define(function(require) {\n"
                + "\n"
                + "	return require('angular-class').create({\n"
                + "		$inject : [],\n"
                + "		extends : require('app-commons/controllers/basic-list-controller'),\n"
                + "		prototype : {\n"
                + "\n"
                + "			initialize : function() {\n"
                + "				// Inicialização do controller\n"
                + "			}\n"
                + "\n"
                + "			// Demais métodos do controller\n"
                + "\n"
                + "		}\n"
                + "	});\n"
                + "});\n"
                + "");

        fwList.close();
    }

    public void geraFormJs() throws SecurityException, IOException {
        File arquivoForm = new File(pastaControllers + "/form.js");
        FileWriter fwForm = new FileWriter(arquivoForm);
        List<Field> atributosAddress = new ArrayList<>();
        for (Field at : Util.getTodosAtributos(classeEntidade)) {
            if (at.getType().equals(GumgaAddress.class)) {
                atributosAddress.add(at);
            }
        }

        fwForm.write(""
                + "define(function(require) {\n"
                + "\n"
                + "	return require('angular-class').create({\n\n");

        String injetar = "";
        for (Class tipo : dependenciasManyToOne) {
            injetar += ("'" + tipo.getSimpleName() + "Service',");
        }
        for (Class tipo : dependenciasManyToMany) {
            injetar += ("'" + tipo.getSimpleName() + "Service',");
        }
        for (Class tipo : dependenciasOneToMany) {
            injetar += ("'" + tipo.getSimpleName() + "Service',");
        }

        if (!injetar.isEmpty()) {
            fwForm.write("$inject: [" + injetar + "],");

        }

        fwForm.write("\n"
                + "		extends : require('app-commons/controllers/basic-form-controller'),\n"
                + "		prototype : {\n"
                + "\n"
                + "			initialize : function() {\n"
                + "				// Inicialização do controller\n");

        for (Class tipo : dependenciasManyToOne) {
            fwForm.write("this.$scope.lista" + tipo.getSimpleName() + " = [];\n");
        }

        for (Class tipo : dependenciasManyToMany) {
            fwForm.write(""
                    + "                this.$scope." + tipo.getSimpleName() + " = [];\n"
                    + "                this.refresh" + tipo.getSimpleName() + "();\n"
                    + "\n");
        }

        for (Field atributo : Util.getTodosAtributos(classeEntidade)) {
            if (atributo.isAnnotationPresent(OneToMany.class)) {
                fwForm.write(""
                        + "                this.$scope.entity." + atributo.getName() + " = this.$scope.entity." + atributo.getName() + " || [];\n"
                        + "");
            }
        }

        if (!atributosAddress.isEmpty()) {
            fwForm.write(""
                    + "                this.$scope.entity = this.entity;\n"
                    + "                this.$scope.allUF = ['AC', 'AL', 'AM', 'AP', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 'MG', 'MS', 'MT', 'PA', 'PB', 'PE', 'PI', 'PR',\n"
                    + "                    'RJ', 'RN', 'RR', 'RS', 'SC', 'SE', 'SP', 'TO'];\n"
                    + "\n"
                    + "                this.$scope.allLogradouro = ['Outros', 'Aeroporto', 'Alameda', 'Área', 'Avenida', 'Campo', 'Chácara', 'Colônia', 'Condomínio', 'Conjunto', 'Distrito',\n"
                    + "                    'Esplanada', 'Estação', 'Estrada', 'Favela', 'Fazenda', 'Feira', 'Jardim', 'Ladeira', 'Largo', 'Lago', 'Lagoa', 'Loteamento', 'Núcleo', 'Parque', 'Passarela', 'Pátio', 'Praça',\n"
                    + "                    'Quadra', 'Recanto', 'Residencial', 'Rodovia', 'Rua', 'Setor', 'Sítio', 'Travessa', 'Trevo', 'Trecho', 'Vale', 'Vereda', 'Via', 'Viaduto', 'Viela', 'Via'];\n"
                    + "                this.$scope.pais = ['Brasil'];\n"
                    + "\n");
        }
        fwForm.write("		},\n"
                + "	\n"
                + "			// Demais métodos do controller\n");

        for (Field atributo : Util.getTodosAtributos(classeEntidade)) {
            if (atributo.isAnnotationPresent(ManyToMany.class)) {

                fwForm.write(""
                        + "            refresh" + Util.getTipoGenerico(atributo).getSimpleName() + ": function () {\n"
                        + "                var $scope = this.$scope;\n"
                        + "                this." + Util.getTipoGenerico(atributo).getSimpleName() + "Service.search($scope." + Util.primeiraMinuscula(Util.getTipoGenerico(atributo).getSimpleName()) + "Pesquisa, ['" + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + "']).then(function (result) {\n"
                        + "                    $scope.lista" + Util.getTipoGenerico(atributo).getSimpleName() + Util.primeiraMaiuscula(atributo.getName()) + "= result.values;\n"
                        + "                });\n"
                        + "            },\n");

                fwForm.write(""
                        + "            add" + Util.primeiraMaiuscula(atributo.getName()) + ": function (objeto) {\n"
                        + "                var index = this.indexOf" + Util.primeiraMaiuscula(atributo.getName()) + "(objeto);\n"
                        + "                this.$scope.entity." + atributo.getName() + " = this.$scope.entity." + atributo.getName() + " || [];\n"
                        + "                this.$scope.entity." + atributo.getName() + ".push(objeto);\n"
                        + "                this.$scope.entity.lista" + Util.getTipoGenerico(atributo).getSimpleName() + Util.primeiraMaiuscula(atributo.getName()) + ".splice(index,1)\n"
                        + "            },\n"
                        + "            remove" + Util.primeiraMaiuscula(atributo.getName()) + ": function (objeto) {\n"
                        + "                var index = this.indexOf" + Util.primeiraMaiuscula(atributo.getName()) + "(objeto);\n"
                        + "                this.$scope.entity." + atributo.getName() + ".splice(index, 1);\n"
                        + "            },\n"
                        + "            indexOf" + Util.primeiraMaiuscula(atributo.getName()) + ": function (objeto) {\n"
                        + "                this.$scope.entity." + atributo.getName() + " = this.$scope.entity." + atributo.getName() + " || [];\n"
                        + "                var lista = this.$scope.entity." + atributo.getName() + ";\n"
                        + "                for (var i = 0; i < lista.length; i++) {\n"
                        + "                    if (lista[i].id == objeto.id) {\n"
                        + "                        return i;\n"
                        + "                    }\n"
                        + "                }\n"
                        + "                return -1;\n"
                        + "            },\n"
                        + "            contains" + Util.primeiraMaiuscula(atributo.getName()) + ": function (" + atributo.getName() + ") {\n"
                        + "                return this.indexOf" + Util.primeiraMaiuscula(atributo.getName()) + "(" + atributo.getName() + ") >= 0;\n"
                        + "            },"
                        + "");
            }
        }

        if (!atributosAddress.isEmpty()) {
            for (Field at : atributosAddress) {
                fwForm.write(""
                        + ""
                        + "            " + at.getName() + "UpdateAddress: function () {\n"
                        + "                var escopo = this.$scope;\n"
                        + "                this.$scope.urlWithCep = 'http://cep.republicavirtual.com.br/web_cep.php?cep=' + this.entity." + at.getName() + ".cep + '&formato=jsonp';\n"
                        + "                this.$http.get(this.$scope.urlWithCep)\n"
                        + "                        .success(function (data) {\n"
                        + "                            escopo.entity." + at.getName() + ".localidade = data.cidade;\n"
                        + "                            escopo.entity." + at.getName() + ".bairro = data.bairro;\n"
                        + "                            escopo.entity." + at.getName() + ".uf = data.uf;\n"
                        + "                            escopo.entity." + at.getName() + ".tipoLogradouro = data.tipo_logradouro;\n"
                        + "                            escopo.entity." + at.getName() + ".logradouro = data.logradouro;\n"
                        + "                            escopo.entity." + at.getName() + ".pais = 'Brasil';\n"
                        + "                        })\n"
                        + "                        \n"
                        + "            },\n"
                        + "            \n");
            }
        }

        for (Class tipo : dependenciasManyToOne) {

            fwForm.write(""
                    + "            refreshLista" + tipo.getSimpleName() + ": function (pesquisa) {\n"
                    + "                var $scope = this.$scope;\n"
                    + "\n"
                    + "                if (pesquisa.length == 0) {\n"
                    + "                    pesquisa = \"%\";\n"
                    + "                }\n"
                    + "\n"
                    + "                this." + tipo.getSimpleName() + "Service.search(pesquisa, ['" + Util.primeiroAtributo(tipo).getName() + "']).then(function (result) {\n"
                    + "                    $scope.lista" + tipo.getSimpleName() + " = result.values;\n"
                    + "                });\n"
                    + "            },\n");
        }
        fwForm.write(""
                + "        }\n"
                + "    });\n"
                + "});"
                + "");

        fwForm.close();
    }

    public void geraServiceJs() throws IOException {
        File arquivoService = new File(pastaScripts + "/service.js");
        FileWriter fwService = new FileWriter(arquivoService);

        fwService.write(""
                + "define([\n"
                + "		'gumga-class',\n"
                + "		'gumga/services/basic-crud-service',\n"
                + "         'app/locations'\n"
                + "	], function(GumgaClass, BasicCrudService,API) {\n"
                + "\n"
                + "	\n"
                + "	function " + nomeEntidade + "Service($http, $q) {\n"
                + "		" + nomeEntidade + "Service.super.constructor.call(this, $http, $q, API.location+\"" + nomeEntidade.toLowerCase() + "\");\n"
                + "	}\n"
                + "\n"
                + "	return GumgaClass.create({\n"
                + "		constructor : " + nomeEntidade + "Service,\n"
                + "		extends : BasicCrudService\n"
                + "	});\n"
                + "	\n"
                + "});\n"
                + "");

        fwService.close();
    }

    public void geraModuleJs() throws IOException {
        File arquivoModule = new File(pastaScripts + "/module.js");
        FileWriter fwModule = new FileWriter(arquivoModule);

        fwModule.write(""
                + "define(function(require) {\n"
                + "	\n"
                + "	require('gumga-components');\n"
                + "	require('app-commons/modules/crud-module').constant('baseTemplateURL', '" + nomeEntidade.toLowerCase() + "');\n"
                + "	\n"
                + "	return require('angular')\n"
                + "		.module('app." + nomeEntidade.toLowerCase() + "', [\"app.base.crud\", 'gumga.components'])\n"
                + "		\n"
                + "		.service('EntityService', require('app/" + nomeEntidade.toLowerCase() + "/service'))\n\n");

        for (Class tipo : dependenciasManyToOne) {
            fwModule.write(".service(\"" + tipo.getSimpleName() + "Service\", require('app/" + tipo.getSimpleName().toLowerCase() + "/service'))\n");
        }
        for (Class tipo : dependenciasManyToMany) {
            fwModule.write(".service(\"" + tipo.getSimpleName() + "Service\", require('app/" + tipo.getSimpleName().toLowerCase() + "/service'))\n");
        }
        for (Class tipo : dependenciasOneToMany) {
            fwModule.write(".service(\"" + tipo.getSimpleName() + "Service\", require('app/" + tipo.getSimpleName().toLowerCase() + "/service'))\n");
        }

        fwModule.write(""
                + "		\n"
                + "		.controller(\"ListController\", require('app/" + nomeEntidade.toLowerCase() + "/controllers/list'))\n"
                + "		.controller(\"FormController\", require('app/" + nomeEntidade.toLowerCase() + "/controllers/form'));\n"
                + "	\n"
                + "});\n"
                + "");

        fwModule.close();
    }

    private void adicionaAoMenu() {
        try {
            File arquivoMenu = new File(pastaResources + "/menu.config");
            FileWriter fwMenu = new FileWriter(arquivoMenu, true);
            fwMenu.write("\n" + nomeEntidade + " { url=\"" + nomeEntidade.toLowerCase() + "\" id=\"" + nomeEntidade.toLowerCase() + "\" }\n");
            fwMenu.close();

        } catch (Exception ex) {
            getLog().error(ex);
        }

    }

    private void geraModal(Field atributo) throws IOException {
        File arquivoModal = new File(pastaJSP + "/" + atributo.getName() + "_modal.jsp");
        FileWriter fwBase = new FileWriter(arquivoModal);
        fwBase.write(""
                + "<%@ page language=\"java\" contentType=\"text/html; charset=UTF-8\" pageEncoding=\"UTF-8\"%>\n"
                + "<%@ taglib uri=\"http://java.sun.com/jsp/jstl/core\" prefix=\"c\" %>\n"
                + "<%@ taglib uri=\"http://gumga.com.br/jsp/tags\" prefix=\"g\" %>\n"
                + "\n"
                + "<div class=\"modal-body\">\n"
                + "    <form name=\"itemForm\" gumga-form-errors gumga-ng-model-errors>\n"
                + "        <div class=\"form-group\" ng-class=\"{'has-error' : itemForm.valor.$invalid}\">\n"
                + "            <label class=\"control-label\">Horas</label>\n"
                + "            <input type=\"text\" class=\"form-control\" name=\"horas\" required=\"true\" placeholder=\"Informe o numero de horas gasto na tarefa...\" ng-model=\"ctrl.entity.horas\" gumga-number gumga-integer>\n"
                + "        </div>\n"
                + "        <div class=\"form-group\" ng-class=\"{'has-error' : itemForm.valor.$invalid}\">\n"
                + "            <label class=\"control-label\">Serviços</label>\n"
                + "            <ui:select ng-model=\"ctrl.entity.servico\">\n"
                + "                <ui:select:match placeholder=\"Selecione um serviço...\">{{$select.selected.nome}}</ui:select:match>\n"
                + "                <ui:select:choices repeat=\"servico in ctrl.servicos track by $index\"\n"
                + "                                   refresh=\"ctrl.refreshServicos($select.search)\"\n"
                + "                                   refresh-delay=\"0\">\n"
                + "                    <div>{{servico.nome}}</div>\n"
                + "                </ui:select:choices>\n"
                + "            </ui:select>\n"
                + "        </div>\n"
                + "    </form>\n"
                + "</div>\n"
                + "<div class=\"modal-footer\">\n"
                + "    <button class=\"btn btn-primary\" ng-click=\"$close(ctrl.entity)\">Selecionar</button>\n"
                + "    <button class=\"btn btn-default\" ng-click=\"$dismiss('close')\">Cancelar</button>\n"
                + "</div>"
                + "");
        fwBase.close();

    }

}
