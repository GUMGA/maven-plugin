
	<#--<div gumga-form-class="${attribute.fieldName}">-->
	<gmd-input>
		<input
				class="form-control gmd"
				type="email"
				name="${attribute.fieldName}"
				gumga-error
				ng-model="${attribute.entitySimpleNameLowerCase}.data.${attribute.fieldName}.value"
				${attribute.required}/>
		<span class="bar"></span>
		<label
				class="control-label"
				gumga-translate-tag="${attribute.entitySimpleNameLowerCase}.${attribute.fieldName}">${attribute.fieldName}</label>
	</gmd-input>
	<#--</div>-->
