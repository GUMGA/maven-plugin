	<#--<div gumga-form-class="${attribute.fieldName}">-->
		<label class="control-label"
			   gumga-translate-tag="${attribute.entitySimpleNameLowerCase}.${attribute.fieldName}">${attribute.fieldName}</label>
		<gumga-date
                ng-model="${attribute.entitySimpleNameLowerCase}.data.${attribute.fieldName}"
                name="${attribute.fieldName}">
		</gumga-date>
	<#--</div>-->