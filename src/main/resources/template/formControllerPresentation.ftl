define([], function() {


 	${entityName}FormController.$inject = ['${entityName}Service', '$state', 'entity', '$scope', 'gumgaController'${dependenciesInject}];

 	function ${entityName}FormController(${entityName}Service, $state, entity, $scope, gumgaController${dependenciesParam}) {

    	gumgaController.createRestMethods($scope, ${entityName}Service, '${entityNameLowerCase}');

		<#list dependenciesEnums as enum>
		${enum}
		</#list>

	    <#list dependenciesManyTo as d>
	    gumgaController.createRestMethods($scope, ${d.name}Service, '${d.type}');
	    $scope.${d.type}.methods.search('${d.nameGettterAndSetter}','');    
	    </#list>

	    <#list attributesNotStatic as attribute>
	    $scope.${attribute}Options=[];
	    </#list>
    
    	$scope.${entityNameLowerCase}.data = entity.data || {};
		<#list attributes as attribute>
		$scope.${attribute.nameGettterAndSetter}.data.${attribute.name} = ($scope.${attribute.nameGettterAndSetter}.data.${attribute.name} == undefined || $scope.${attribute.nameGettterAndSetter}.data.${attribute.name} == "") ? new Date() : new Date($scope.${attribute.nameGettterAndSetter}.data.${attribute.name});
		
		</#list>      
		<#list oneToManys as oneToMany>
		$scope.${entityNameLowerCase}.data.${oneToMany} = $scope.${entityNameLowerCase}.data.${oneToMany} || [];
		</#list>
		$scope.continue = {};
	
		$scope.${entityNameLowerCase}.on('putSuccess',function(data){
			$state.go('${entityNameLowerCase}.list');
		});
 	}
	
	return ${entityName}FormController;   
});