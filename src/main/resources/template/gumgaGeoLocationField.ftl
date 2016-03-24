
	<div class="row">
		<div class="col-md-12">
			<label gumga-translate-tag="${attribute.entitySimpleNameLowerCase}.${attribute.fieldName}">${attribute.fieldName}</label>
			<div gumga-form-class="${attribute.fieldName}latitude">
				<label gumga-translate-tag="gumga.latitude">Latitude</label>
				<input gumga-error type="text" name="${attribute.fieldName}latitude" ${attribute.required} ng-model="${attribute.entitySimpleNameLowerCase}.data.${attribute.fieldName}.latitude" class="form-control"/>
			</div>
			<div gumga-form-class="${attribute.fieldName}longitude">
				<label gumga-translate-tag="gumga.longitude">Longitude</label>
				<input gumga-error type="text" name="${attribute.fieldName}longitude" ${attribute.required} ng-model="${attribute.entitySimpleNameLowerCase}.data.${attribute.fieldName}.longitude" class="form-control" />
			</div>
			<a class="btn btn-default" ng-href="http://maps.google.com/maps?q={{${attribute.entitySimpleNameLowerCase}.data.${attribute.fieldName}.latitude + ',' + ${attribute.entitySimpleNameLowerCase}.data.${attribute.fieldName}.longitude}}" target="_blank"> <p class="glyphicon glyphicon-globe"></p> GOOGLE MAPS</a>
		</div>
	</div>
	