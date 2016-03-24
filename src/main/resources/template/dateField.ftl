	<div gumga-form-class="${attribute.fieldName}">
		<label gumga-translate-tag="${attribute.entitySimpleNameLowerCase}.${attribute.fieldName}">${attribute.fieldName}</label>
		<input type="text" name="${attribute.fieldName}" class="form-control" ${attribute.required} datepicker-popup="fullDate" ng-model="${attribute.entitySimpleNameLowerCase}.data.${attribute.fieldName}" is-open="${attribute.opened}" ng-click="${attribute.opened} = !${attribute.opened}" close-text="Close" />
	</div>