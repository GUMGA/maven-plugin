	<div gumga-form-class="${attribute.fieldName}">
		<label gumga-translate-tag="${attribute.entitySimpleNameLowerCase}.${attribute.fieldName}">${attribute.fieldName}</label>
		<p class="input-group">
			<input type="text" name="${attribute.fieldName}" class="form-control" ${attribute.required} uib-datepicker-popup="dd/MM/yyyy" ng-model="${attribute.entitySimpleNameLowerCase}.data.${attribute.fieldName}" is-open="${attribute.opened}" ng-click="${attribute.opened} = !${attribute.opened}" close-text="Close" />
			<span class="input-group-btn">
				<button type="button" class="btn btn-default" ng-click="open${attribute.fieldName}()">
					<i class="glyphicon glyphicon-calendar"></i>
				</button>
			</span>
		</p>
	</div>