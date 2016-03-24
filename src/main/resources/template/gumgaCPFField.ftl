
	<div gumga-form-class="${attribute.fieldName}">
	    <label gumga-translate-tag="${attribute.entitySimpleNameLowerCase}.${attribute.fieldName}">"${attribute.fieldName}</label>
	    <input id="${attribute.fieldName}"
	            gumga-mask="999.999.999-99"
	            gumga-error type="text" name="${attribute.fieldName}"
	            ${attribute.required}
	            ng-model="${attribute.entitySimpleNameLowerCase}.data.${attribute.fieldName}.value"
	            class="form-control"/>
	</div>
	