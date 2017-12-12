${entityName}FormController.$inject = ['${entityName}Service', '$state', 'entity', '$scope', 'gumgaController'${dependenciesInject}, '$gmdAlert'];

function ${entityName}FormController(${entityName}Service, $state, entity, $scope, gumgaController${dependenciesParam},$gmdAlert) {

	gumgaController.createRestMethods($scope, ${entityName}Service, '${entityNameLowerCase}');

	<#list dependenciesEnums as enum>
	${enum}
	</#list>

	<#list dependenciesManyTo as d>
	gumgaController.createRestMethods($scope, ${d.name}Service, '${d.type}');
	$scope.${d.type}.methods.search('${d.nameGettterAndSetter}','');

	$scope.${d.type}Config = {};
	</#list>

	<#list attributesNotStatic as attribute>
	$scope.${attribute}Options=[];
	</#list>

	$scope.${entityNameLowerCase}.data = angular.copy(entity.data) || {};
	<#list attributes as attribute>
	$scope.${attribute.nameGettterAndSetter}.data.${attribute.name} = ($scope.${attribute.nameGettterAndSetter}.data.${attribute.name} == undefined || $scope.${attribute.nameGettterAndSetter}.data.${attribute.name} == "") ? new Date() : new Date($scope.${attribute.nameGettterAndSetter}.data.${attribute.name});
	$scope.open${attribute.name} = function() {
		$scope.${attribute.type} = !$scope.${attribute.type};
	};

	</#list>
	<#list oneToManys as oneToMany>
	$scope.${entityNameLowerCase}.data.${oneToMany} = $scope.${entityNameLowerCase}.data.${oneToMany} || [];

	$scope.${oneToMany}Config = {
		ngModel: '${entityNameLowerCase}.data.${oneToMany}',
		options: {
			type: 'array',
			message: 'Its not array',
			empty: {
				value: false,
				message: 'Is Empty'
			}
		}
	};
	</#list>

	$scope.${entityNameLowerCase}.on('putSuccess',function(data) {
		$gmdAlert.success('Sucesso!', 'Seu registro foi adicionado!', 3000);
		if($scope.shouldContinue) {
			$scope.${entityNameLowerCase}.data = angular.copy(entity.data) || {};
		} else {
			$state.go('${entityNameLowerCase}.list');
		}

	});
}

module.exports = ${entityName}FormController;
