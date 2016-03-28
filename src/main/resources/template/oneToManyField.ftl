
	<div class="full-width-without-padding">
	    <label for="${attribute.fieldName}" gumga-translate-tag="${attribute.entitySimpleNameLowerCase}.${attribute.fieldName}"></label>
	
		<gumga-one-to-many
		        children="${attribute.entitySimpleNameLowerCase}.data.${attribute.fieldName}"
		        template-url="app/modules/${attribute.declaringClassSimpleNameLowerCase}/views/modal${attribute.typeGenericSimpleNameOfField}.html"
		        displayable-property="${attribute.firstAttributeTypeGenericNameLowerCase}"
		        controller="Modal${attribute.typeGenericSimpleNameOfField}Controller">
		</gumga-one-to-many>
	</div>
	