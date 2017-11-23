package ${packageName};

import ${packageBase}.application.service.${entityName}Service;
import ${completeNameEntity};
import io.gumga.application.GumgaService;
import io.gumga.presentation.GumgaAPI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMethod;
import io.gumga.presentation.RestResponse;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.validation.BindingResult;
import io.gumga.application.GumgaTempFileService;
import io.gumga.domain.domains.GumgaImage;
import io.gumga.presentation.GumgaAPI;
import org.springframework.web.bind.annotation.RequestMapping;
import java.io.IOException;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("${uriBase}")
@Transactional
public class ${entityName}API extends GumgaAPI<${entityName}, String> {

<#if "${gumgaImage?c}" == "true">
@Autowired
private GumgaTempFileService gumgaTempFileService;
</#if>

@Autowired
public ${entityName}API(GumgaService<${entityName}, String> service) {
    super(service);
}

<#list attributes as attribute>
@Override
public ${entityName} load(@PathVariable String id) {
    return ((${entityName}Service)service).load${entityName}Fat(id);
}

</#list>
<#list gumgaImages as gumgaImage>
@RequestMapping(method = RequestMethod.POST, value = "/${gumgaImage.name}")
public String ${gumgaImage.nameGettterAndSetter}Upload(@RequestParam MultipartFile ${gumgaImage.name}) throws IOException {
    System.out.println("UPLOAD foto");
    GumgaImage gi = new GumgaImage();
    gi.setBytes(${gumgaImage.name}.getBytes());
    gi.setMimeType(${gumgaImage.name}.getContentType());
    gi.setName(${gumgaImage.name}.getName());
    gi.setSize(${gumgaImage.name}.getSize());
    String fileName = gumgaTempFileService.create(gi);
    return fileName;
}

@RequestMapping(method = RequestMethod.DELETE, value = "/${gumgaImage.name}")
public String ${gumgaImage.nameGettterAndSetter}Delete(String fileName) {
    return gumgaTempFileService.delete(fileName);
}

@RequestMapping(method = RequestMethod.GET, value = "/${gumgaImage.name}/{fileName}")
public byte[] ${gumgaImage.nameGettterAndSetter}Get(@PathVariable(value = "fileName") String fileName) {
    return gumgaTempFileService.find(fileName).getBytes();
}
</#list>

<#if "${gumgaImage?c}" == "true">
@Transactional
@RequestMapping(method = RequestMethod.POST)
public RestResponse<${entityName}> save(@RequestBody @Valid ${entityName} obj, BindingResult result) {
    <#list gumgaImages as gumgaImage>
    if (obj.get${gumgaImage.nameGettterAndSetter}() != null) {
    obj.set${gumgaImage.nameGettterAndSetter}((GumgaImage) gumgaTempFileService.find(obj.get${gumgaImage.nameGettterAndSetter}().getName()));
    }
    </#list>
return super.save(obj, result);
}

@Override
@Transactional
@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = "application/json")
public RestResponse<${entityName}> update(String id, @RequestBody @Valid ${entityName} obj, BindingResult result) {
    <#list gumgaImages as gumgaImage>
    if (obj.getFoto()!= null) {
        if ("null".equals(obj.get${gumgaImage.nameGettterAndSetter}().getName())) {
            obj.set${gumgaImage.nameGettterAndSetter}(null);
        }else if (obj.get${gumgaImage.nameGettterAndSetter}().getSize() == 0) {
            obj.set${gumgaImage.nameGettterAndSetter}((GumgaImage) gumgaTempFileService.find(obj.get${gumgaImage.nameGettterAndSetter}().getName()));
        }
    }
    </#list>
return super.update(id, obj, result);
}
</#if>
}