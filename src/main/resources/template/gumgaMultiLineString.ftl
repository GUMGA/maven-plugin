	<#--<div gumga-form-class="${attribute.fieldName}">-->
		<label  class="control-label"
				gumga-translate-tag="${attribute.entitySimpleNameLowerCase}.${attribute.fieldName}">${attribute.fieldName}</label>
		<textarea
				type="text"
                class="form-control gmd"
                gumga-error
				name="${attribute.fieldName}"
				ng-model="${attribute.entitySimpleNameLowerCase}.data.${attribute.fieldName}.value"
				${attribute.required}>
		</textarea>
	<#--</div>-->