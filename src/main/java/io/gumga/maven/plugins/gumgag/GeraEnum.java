package io.gumga.maven.plugins.gumgag;

import java.awt.Event;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import io.gumga.freemarker.ConfigurationFreeMarker;
import io.gumga.freemarker.TemplateFreeMarker;
import io.gumga.freemarker.ValueEnum;

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
	private String valoresPossiveis;

	@Parameter(property = "rich", defaultValue = "false")
	private String rich;

	@Parameter(property = "multi", defaultValue = "false")
	private String multi;

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
		String pastaEnum = Util.windowsSafe(project.getCompileSourceRoots().get(0)) + "/".concat(nomePacote.replaceAll("\\.", "/"));

		getLog().info("Iniciando plugin Gerador de Enum GUMGA ");
		getLog().info("Gerando " + nomePacote + "." + nomeEnum);

		File f = new File(pastaEnum);
		f.mkdirs();
		try {
			ConfigurationFreeMarker config = new ConfigurationFreeMarker();
			TemplateFreeMarker template = new TemplateFreeMarker("enum.ftl", config);
			template.add("package", nomePacote);
			template.add("enumName", nomeEnum);
			template.add("simpleValue", false);
			template.add("simpleValueAndDescription", false);
			template.add("multValue", false);
			template.add("multValueAndDescription", false);

			List<ValueEnum> values = new ArrayList<>();
			if (rich.equals("false") && multi.equals("false")) {
				generateSimpleValue(template, values);
			} else if (rich.equals("true") && multi.equals("false")) {
				generateSimpleValueAndDescription(template, values);
			} else if (multi.equals("true") && rich.equals("false")) {
				generateMultValue(template, values);
			} else if (multi.equals("true") && rich.equals("true")) {
				generateMultValueAndDescription(template, values);
			}

			template.add("values", values);
			template.generateTemplate(pastaEnum + "/" + nomeEnum + ".java");

		} catch (Exception ex) {
			getLog().error(ex);
		}
	}

	private void generateMultValueAndDescription(TemplateFreeMarker template, List<ValueEnum> values) {
		template.add("multValueAndDescription", true);
		String[] value = this.valoresPossiveis.split(",");
		int x = 1;

		String[] valuesPossible = this.valoresPossiveis.split(",");
		for (int i = 0; i < valuesPossible.length; i++) {
			if (valuesPossible[i].contains(":")) {
				String[] descriptions = valuesPossible[i].split(":");

				if (i == (valuesPossible.length - 1)) {
					values.add(new ValueEnum(descriptions[0] + "(\"" + descriptions[1] + "\"" + ", " + x + ");"));
				} else {
					values.add(new ValueEnum(descriptions[0] + "(\"" + descriptions[1] + "\"" + ", " + x + "),"));
				}
			} else if (i == (valuesPossible.length - 1)) {
				values.add(new ValueEnum(valuesPossible[i] + "(\"" + "" + "\"" + ", " + x + ");"));
			} else {
				values.add(new ValueEnum(valuesPossible[i] + "(\"" + "" + "\"" + ", " + x + "),"));
			}
			x *= 2;
		}
	}

	private void generateMultValue(TemplateFreeMarker template, List<ValueEnum> values) {
		template.add("multValue", true);
		String[] valuesPossible = this.valoresPossiveis.split(",");
		int x = 1;
		for (int i = 0; i < valuesPossible.length; i++) {
			if (!valuesPossible[i].trim().contains(":")) {
				if (i == (valuesPossible.length - 1)) {
					values.add(new ValueEnum(valuesPossible[i] + "(" + x + ");"));
				} else {
					values.add(new ValueEnum(valuesPossible[i] + "(" + x + "),"));
				}
			} else {
				String[] parts = valuesPossible[i].split(":");
				if (i == (valuesPossible.length - 1)) {
					values.add(new ValueEnum(parts[0] + "(" + x + ");"));
				} else {
					values.add(new ValueEnum(parts[0] + "(" + x + "),"));
				}
			}
			x *= 2;
		}
	}

	private void generateSimpleValueAndDescription(TemplateFreeMarker template, List<ValueEnum> values) {
		template.add("simpleValueAndDescription", true);
		String[] valuesPossible = this.valoresPossiveis.split(",");
		for (int i = 0; i < valuesPossible.length; i++) {
			if (valuesPossible[i].contains(":")) {
				String[] descriptions = valuesPossible[i].split(":");

				if (i == (valuesPossible.length - 1)) {
					values.add(new ValueEnum(descriptions[0] + "(\"" + descriptions[1] + "\");"));
				} else {
					values.add(new ValueEnum(descriptions[0] + "(\"" + descriptions[1] + "\"),"));
				}
			} else {
				values.add(new ValueEnum(valuesPossible[i] + "(\"" + "" + "\"),"));
			}
		}
	}

	private void generateSimpleValue(TemplateFreeMarker template, List<ValueEnum> values) {
		template.add("simpleValue", true);
		String[] valuesPossible = this.valoresPossiveis.split(",");

		for (int i = 0; i < valuesPossible.length; i++) {

			if (i == (valuesPossible.length - 1)) {
				values.add(new ValueEnum(valuesPossible[i] + ";"));
			} else {
				values.add(new ValueEnum(valuesPossible[i] + ","));
			}

		}
	}

}