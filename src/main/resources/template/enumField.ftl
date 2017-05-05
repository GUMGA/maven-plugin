	<#--<div gumga-form-class="${attribute.fieldName}">-->
    <gmd-select
			ng-model="${attribute.entitySimpleNameLowerCase}.data.${attribute.fieldName}"
			placeholder="{{'${attribute.fieldName}'|gumgaTranslate:'${attribute.entitySimpleNameLowerCase}'}}">
        <gmd-option
				ng-repeat="data in ${attribute.fieldSimpleNameLowerCase}"
				ng-value="data.value"
				ng-label="data.label">
            {{data.label}}
        </gmd-option>
    </gmd-select>
	<#--</div>-->