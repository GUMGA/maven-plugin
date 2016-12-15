
	<div gumga-form-class="${attribute.fieldName}">
		<label gumga-translate-tag="${attribute.entitySimpleNameLowerCase}.${attribute.fieldName}">${attribute.fieldName}</label>
	    <gumga-many-to-one
	            name="${attribute.fieldName}"
	            value="${attribute.entitySimpleNameLowerCase}.data.${attribute.fieldName}"
	            search-method="${attribute.fieldSimpleNameLowerCase}.methods.asyncSearch('${attribute.firstAttributeOfField}', param)"
	            field="${attribute.firstAttributeOfField}"
	            authorize-add="false"
              display-info="false"
	            post-method="${attribute.fieldSimpleNameLowerCase}.methods.asyncPost(value, '${attribute.firstAttributeOfField}')">
	    </gumga-many-to-one>
	</div>

