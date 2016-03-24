
	<div gumga-form-class="${attribute.fieldName}">
		<label gumga-translate-tag="${attribute.entitySimpleNameLowerCase}.${attribute.fieldName}">${attribute.fieldName}</label>
		<textarea ng-model="${attribute.entitySimpleNameLowerCase}.data.${attribute.fieldName}.value" ${attribute.required} class="form-control" placeholder="Digite ${attribute.tag}" rows="4" cols="50"></textarea>\n
	</div>
	