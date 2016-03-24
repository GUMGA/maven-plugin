define(['app/apiLocations'], function(APILocation) {

	${entityName}Service.$inject = ['GumgaRest'];

	function ${entityName}Service(GumgaRest) {
    	var Service = new GumgaRest(APILocation.apiLocation + '/api/${entityNameLowerCase}');

    	return Service;
    }

  	return ${entityName}Service;
});