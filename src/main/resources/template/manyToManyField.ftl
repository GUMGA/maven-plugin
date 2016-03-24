
    <div class="col-md-6">
        <label for="${attribute.fieldName}" gumga-translate-tag="${attribute.typeGenericSimpleNameOfFieldLowerCase}.title"></label>
    </div>
    
    <div class="col-md-6">
        <label for="${attribute.entitySimpleNameLowerCase}.${attribute.fieldName}" gumga-translate-tag="${attribute.entitySimpleNameLowerCase}.${attribute.fieldName}"></label>
    </div>

	<div class="full-width-without-padding">
	    <gumga-many-to-many 
	            left-list="${attribute.typeGenericSimpleNameOfFieldLowerCase}.data" 
	            right-list="${attribute.entitySimpleNameLowerCase}.data.${attribute.fieldName}" 
	            left-search="${attribute.typeGenericSimpleNameOfFieldLowerCase}.methods.advancedSearch('${attribute.firstAttributeTypeGeneric}', param)"
	            filter-parameters="${attribute.firstAttributeTypeGeneric}"
	            post-method="${attribute.typeGenericSimpleNameOfFieldLowerCase}.methods.save(value)"
	            authorize-add="true">
	        <left-field>{{$value.${attribute.firstAttributeTypeGeneric}}}</left-field>
	        <right-field>{{$value.${attribute.firstAttributeTypeGeneric}}}</right-field>
	    </gumga-many-to-many>
	</div>
	
	