<div class="row">
    <div class="col-md-10 col-md-offset-1">
        <div class="panel gmd">
            <div class="panel-heading"><label class="control-label" gumga-translate-tag="${entityNameLowerCase}"></label>
            </div>
            <div class="panel-body">
                <div class="row">

                    <div class="col-sm-5">
                        <a ui-sref="${entityNameLowerCase}.insert" class="btn gmd raised btn-primary">
                            <i class="glyphicon glyphicon-plus"></i>Novo
                        </a>

                        <button type="button" class="btn gmd raised btn-danger"
                                ng-click="${entityNameLowerCase}.methods.delete(selectedValues)"
                                gumga-confirm="Deseja remover?">
                            <i class="glyphicon glyphicon-trash"></i>Remover
                        </button>
                    </div>

                    <div class="col-sm-7">
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
                        <gumga-list sort="${entityNameLowerCase}.methods.sort(field, dir)"
                                    class="table-striped table-condensed"
                                    data="${entityNameLowerCase}.data"
                                    configuration="tableConfig">
                        </gumga-list>
                    </div>
                </div>

                <div class="row">
                    <div class="col-sm-12">
                        <ul uib-pagination
                            ng-model="page"
                            max-size="10"
                            boundary-links="true"
                            previous-text="‹"
                            next-Text="›"
                            first-text="«"
                            last-text="»"
                            items-per-page="${entityNameLowerCase}.pageSize"
                            total-items="${entityNameLowerCase}.count"
                            ng-change="${entityNameLowerCase}.methods.get(page)">
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>