
	<div gumga-form-class="${attribute.fieldName}">
		<label gumga-translate-tag="${attribute.entitySimpleNameLowerCase}.${attribute.fieldName}">${attribute.fieldName}</label>
		<input style="width:15px" gumga-error type="checkbox" ${attribute.required} name="${attribute.fieldName}" ng-model="${attribute.entitySimpleNameLowerCase}.data.${attribute.fieldName}.value" class="form-control" />
	</div>
	