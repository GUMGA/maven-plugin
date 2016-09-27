define([], function() {

  Modal${entityName}Controller.$inject = ['$scope', 'gumgaController', '$uibModal', '$uibModalInstance', 'entity'${injectManyToOne}];

  function Modal${entityName}Controller($scope, gumgaController, $uibModal, $uibModalInstance, entity${injectControllerManyToOne}) {
  
  	   $scope.${entity} = {};
  	   $scope.${entity}.data = angular.copy(entity) || {};
  	   
  	  <#list attributesOneToMany as attr>
  	  	$scope.${attr.nameGettterAndSetter}.data.${attr.name} = $scope.${attr.nameGettterAndSetter}.data.${attr.name} || [];
  	  </#list>
  
  	  <#list dpManyToOne as dp>

      gumgaController.createRestMethods($scope, ${dp.simpleName}Service, '${dp.simpleName?lower_case}');
      $scope.${dp.simpleName?lower_case}.methods.search('','')

	  </#list>
  

  
 	  $scope.ok = function (obj) {
          $uibModalInstance.close(obj);
      };

      $scope.cancel = function () {
          if($scope.Modal.$dirty) {
              var modal = $uibModal.open( {
                  template:
                  '<div>'+
                  '   <section class="modal-body">' +
                  '       <h4>Deseja sair sem salvar as alterações?</h4>' +
                  '   </section>'+
                  '   <div class="modal-footer">'+
                  '       <button class="btn btn-default" ng-click="handleClose(false)">Não</button>' +
                  '       <button class="btn btn-default" ng-click="handleClose(true)">Sim</button>' +
                  '   </div>'+
                  '</div>',
                  backdrop: false,
                  keyboard: false,
                  size: 'md',
                  controller: function($scope, $uibModalInstance) {
                      $scope.handleClose = function(_boolean) {
                          _boolean ? $uibModalInstance.close(true) : $uibModalInstance.close(false);
                      }
                  }
              });

              modal.result.then(function(_boolean) {
                  if(_boolean){
                      $uibModalInstance.dismiss('cancel');
                  }
              })
              return 0
          }
          $uibModalInstance.dismiss('cancel');
      };

  };
  return Modal${entityName}Controller;
});



