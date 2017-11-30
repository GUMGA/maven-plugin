<div class="row">
    <div class="col-md-10 col-md-offset-1">
        <div class="panel gmd">
            <div class="panel-body">
                <div class="row">

                    <div class="col-sm-7 col-sm-offset-5">
                        <gumga-query search="${entityNameLowerCase}.methods.search(field,param)"
                                     advanced-search="${entityNameLowerCase}.methods.advancedSearch(param)"
                                     saved-filters="${entityNameLowerCase}.methods.getQuery(page)">
                        <#list attributesSearchField as attribute>
                            <search-field label="${attribute.name}"
                                          field="${attribute.nameGettterAndSetter}"></search-field>
                        </#list>
                        <#list attributesAdvancedSearchField as attribute>
                            <advanced-search-field type="${attribute.type}" label="${attribute.name}"
                                                   field="${attribute.nameGettterAndSetter}"></advanced-search-field>
                        </#list>
                        </gumga-query>
                    </div>
                </div>

                <br>
                <div class="row">
                    <div class="col-sm-6">
                        <gumga-query-action
                                entity="${entityNameLowerCase}"
                                selected="selectedValues"
                                query="query"
                                beyond="beyond"
                                actions="actions"
                                on-action="action(queryaction)">
                        </gumga-query-action>
                    </div>
                </div>
                <br>

                <div class="row">
                    <div class="col-sm-12">
                        <gumga-list class="table-striped table-condensed"
                                    sort="${entityNameLowerCase}.methods.sort(field, dir)"
                                    data="${entityNameLowerCase}.data"
                                    configuration="tableConfig"
                                    page-size="${entityNameLowerCase}.pageSize"
                                    page-position="bottom"
                                    page-align="flex-end"
                                    count="${entityNameLowerCase}.count"
                                    page-model="page"
                                    on-page-change="${entityNameLowerCase}.methods.get(page, pageSize)">
                        </gumga-list>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<gmd-fab force-click="false" fixed opened="selectedValues.length > 0">
    <button ng-if="selectedValues.length == 0" class="btn-primary gmd-ripple" ui-sref="${entityNameLowerCase}.insert">
        <span class="material-icons">add</span>
    </button>
    <button ng-if="selectedValues.length > 0" class="btn-warning gmd-ripple">
        <span class="material-icons">view_headline</span>
    </button>
    <ul ng-class="{'disabled-fab': selectedValues.length <= 0}">
        <li class="btn-danger" ng-click="${entityNameLowerCase}.methods.delete(selectedValues)">
            <span class="visible">Remover</span>
            <i class="material-icons gmd-ripple">delete</i>
        </li>
        <li class="btn-primary" ui-sref="${entityNameLowerCase}.insert">
            <span class="visible">Novo</span>
            <i class="material-icons gmd-ripple">add</i>
        </li>
    </ul>
</gmd-fab>