package ${package};
import gumga.framework.domain.GumgaModel; //TODO RETIRAR OS IMPORTS DESNECESSÁRIOS
import gumga.framework.domain.GumgaMultitenancy;
import java.io.Serializable;
import java.util.*;
import java.math.BigDecimal;
import javax.persistence.*;
import javax.validation.constraints.*;
import gumga.framework.domain.domains.*;
import org.hibernate.annotations.Columns;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.envers.Audited;
import com.fasterxml.jackson.annotation.JsonIgnore;

@GumgaMultitenancy
@SequenceGenerator(name = GumgaModel.SEQ_NAME, sequenceName = "SEQ_${entityName}")
//@Indexed
@Audited
@Entity
public class ${entityName} extends ${superClass} {

<#if "GumgaModel<Long>" == "${superClass}">
    @Version
    private Integer version;
</#if>
<#include "attributes.ftl">

	public ${entityName}() {
	}
<#include "generatorGettersAndSetters.ftl">
}