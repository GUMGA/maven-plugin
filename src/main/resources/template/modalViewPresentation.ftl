<form name="Modal" gumga-form>
	<div class="modal-header">
		<h3 class="modal-title" gumga-translate-tag="${entityNameLowerCase}.title"></h3>
	</div>
	<div class="modal-body" style="overflow: auto">
		<#include "generateFields.ftl">
	</div>
	<div class="clearfix"></div>
	<div class="modal-footer">
		<button type="button" class="btn gmd raised btn-primary" ng-click="ok(${entityName}.data)" ng-disabled="Modal.$invalid">OK</button>
		<button type="button" class="btn gmd raised btn-warning" ng-click="cancel()">Cancel</button>
	</div>
</form>