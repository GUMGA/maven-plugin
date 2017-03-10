	<div gumga-form-class="${attribute.fieldName}">
		<label gumga-translate-tag="${attribute.entitySimpleNameLowerCase}.${attribute.fieldName}">${attribute.fieldName}</label>
		<select class="form-control" gumga-error name="${attribute.fieldName}" ng-model="${attribute.entitySimpleNameLowerCase}.data.${attribute.fieldName}">
			<option  ng-selected="value.value == ${attribute.entitySimpleNameLowerCase}.data.${attribute.fieldName}"  value="{{value.value}}" ng-repeat="value in value${attribute.fieldSimpleNameLowerCase}">{{value.label}}</option>
		</select>
	</div>'