
	<div gumga-form-class="${attribute.fieldName}">
	    <label gumga-translate-tag="${attribute.entitySimpleNameLowerCase}.${attribute.fieldName}">"${attribute.fieldName}</label>
	    <gumga-upload attribute="${attribute.entityNameLowerCase}.data.${attribute.fieldName}"
	                upload-method="${attribute.entityNameLowerCase.methods.postImage(image)"
	                delete-method="${attribute.entityNameLowerCase.methods.deleteImage(image)">
	    </gumga-upload>
	</div>
