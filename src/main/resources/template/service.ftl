package ${package};

import io.gumga.application.GumgaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import org.hibernate.Hibernate;

import ${packageRepository};
import ${packageEntity};

<#list imports as import>
${import}
</#list>

@Service
public class ${serviceName}Service extends GumgaService<${serviceName}, Long> {

private final ${serviceName}Repository repository;

@Autowired
public ${serviceName}Service(${serviceName}Repository repository) {
super(repository);
this.repository = repository;
}

<#if "${attributesToMany?c}" == "true">
@Transactional
public ${serviceName} load${serviceName}Fat(Long id) {
${serviceName} obj = view(id);

	<#list hibernate01 as h1>
    Hibernate.initialize(obj.get${h1.nameGettterAndSetter}());
	</#list>

	<#list hibernate02 as h2>
    for(${h2.name} subObj:obj.get${h2.type}()) {
    Hibernate.initialize(subObj.get${h2.nameGettterAndSetter}());
    }
	</#list>

return obj;
}
</#if>
}