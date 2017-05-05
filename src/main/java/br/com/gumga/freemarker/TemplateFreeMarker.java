package br.com.gumga.freemarker;

import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class TemplateFreeMarker {

	private Map<String, Object> data;
	private Template template;
	
	public TemplateFreeMarker(String path, ConfigurationFreeMarker config) {
		this.data = new HashMap<>();
		this.loadTemplate(path, config);
	}

	public void add(String key, Object value) {
		this.data.put(key, value);
	}	

	public void generateTemplate() {
		try {
			Writer console = new OutputStreamWriter(System.out);
			this.template.process(data, console);
			console.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TemplateException e) {
			e.printStackTrace();
		}		
	}
	
	public void generateTemplate(String fileName) {
		try{
			Writer file = new FileWriter(new File(fileName));
			this.template.process(data, file);
			file.flush();
			file.close();
		} catch(Exception e) {
			System.out.println(e);
		}		
	}
	
	private void loadTemplate(String path, ConfigurationFreeMarker config) {
		try {
			this.template = config.getConfiguration().getTemplate(path);
		} catch (TemplateNotFoundException e) {
			e.printStackTrace();
		} catch (MalformedTemplateNameException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
