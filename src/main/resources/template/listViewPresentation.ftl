	<div class="row">
	
	    <div class="col-md-5">
	      <a ui-sref="${entityNameLowerCase}.insert" class="btn btn-primary">
	        <i class="glyphicon glyphicon-plus"></i>Novo
	      </a>
	
	      <button type="button" class="btn btn-danger" ng-click="${entityNameLowerCase}.methods.delete(selectedValues)" gumga-confirm="Deseja remover?">
	        <i class="glyphicon glyphicon-trash"></i>Remover
	      </button>
	    </div>
	
	    <div class="col-md-7">
	        <gumga-query search="${entityNameLowerCase}.methods.search(field,param)" advanced-search="${entityNameLowerCase}.methods.advancedSearch(param)" saved-filters="${entityNameLowerCase}.methods.getQuery(page)">
	        	<#list attributesSearchField as attribute>
	            <search-field label="${attribute.name}" field="${attribute.nameGettterAndSetter}"></search-field>
	            </#list>
	            <#list attributesAdvancedSearchField as attribute>
	            <advanced-search-field  type="${attribute.type}"  label="${attribute.name}" field="${attribute.nameGettterAndSetter}"></advanced-search-field>
	            </#list>
	        </gumga-query>
	    </div>
	</div>
	
	<div class="full-width-without-padding">
	    <gumga-list sort="${entityNameLowerCase}.methods.sort(field, dir)"
	           class="table-striped table-condensed"
	            data="${entityNameLowerCase}.data"
	            configuration="tableConfig">
	    </gumga-list>
	</div>

	<uib-pagination ng-model="page"
	            max-size="10"
	            boundary-links="true"
	            previous-text="‹"
	            next-Text="›"
	            first-text="«"
	            last-text="»"
	            items-per-page="${entityNameLowerCase}.pageSize"
	            total-items="${entityNameLowerCase}.count"
	            ng-change="${entityNameLowerCase}.methods.get(page)">
	</uib-pagination>