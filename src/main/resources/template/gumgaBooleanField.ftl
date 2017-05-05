
	<#--<div gumga-form-class="${attribute.fieldName}">-->
    <div class="checkbox">
        <label>
            <input
					type="checkbox"
                    name="${attribute.fieldName}"
					class="gmd"
                    ng-model="${attribute.entitySimpleNameLowerCase}.data.${attribute.fieldName}.value">
            <span class="box"></span>
            <label class="control-label"
				   gumga-translate-tag="${attribute.entitySimpleNameLowerCase}.${attribute.fieldName}">${attribute.fieldName}</label>
        </label>
    </div>
	<#--</div>-->
	