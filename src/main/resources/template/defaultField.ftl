	<div gumga-form-class="${attribute.fieldName}">
		<label gumga-translate-tag="${attribute.entitySimpleNameLowerCase}.${attribute.fieldName}">${attribute.fieldName}</label>
		<input gumga-error type="text" name="${attribute.fieldName}" ng-model="${attribute.entitySimpleNameLowerCase}.data.${attribute.fieldName}" ${attribute.required} class="form-control" />
	</div>