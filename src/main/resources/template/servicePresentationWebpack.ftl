${entityName}Service.$inject = ['GumgaRest'];

function ${entityName}Service(GumgaRest) {
	var Service = new GumgaRest(APILocation.apiLocation + '/api/${entityNameLowerCase}');

	return Service;
}

module.exports = ${entityName}Service;
