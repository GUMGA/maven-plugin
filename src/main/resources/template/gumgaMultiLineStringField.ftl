
	<#--<div gumga-form-class="${attribute.fieldName}">-->
		<label  class="control-label"
				gumga-translate-tag="${attribute.entitySimpleNameLowerCase}.${attribute.fieldName}">${attribute.fieldName}</label>
		<textarea
				class="form-control gmd"
				ng-model="${attribute.entitySimpleNameLowerCase}.data.${attribute.fieldName}.value"
				${attribute.required}
				placeholder="Digite ${attribute.tag}"
				rows="4"
				cols="50">
		</textarea>
	<#--</div>-->
	