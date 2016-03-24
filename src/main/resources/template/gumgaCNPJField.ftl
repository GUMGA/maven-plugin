
	<div gumga-form-class="${attribute.fieldName}">
	    <input id="${attribute.fieldName}"
	            gumga-mask="99.999.999/9999-99"
	            gumga-error type="text" name="${attribute.fieldName}"
	            ${attribute.required}
	            ng-model="${attribute.entitySimpleNameLowerCase}.data.${attribute.fieldName}.value"
	            class="form-control"/>
	</div>
	