<div class="row">
    <div class="col-md-10 col-md-offset-1">

        <div class="row">

            <div class="col-sm-7 col-sm-offset-5">
                <gumga-query use-gquery="true"
                             search="${entityNameLowerCase}.methods.searchWithGQuery(field,param)"
                             advanced-search="${entityNameLowerCase}.methods.searchWithGQuery(param)"
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
            <div class="col-sm-12">
                <gumga-list sort="${entityNameLowerCase}.methods.sort(field, dir)"
                            class="table-hover table-condensed"
                            data="${entityNameLowerCase}.data"
                            page-align="flex-end"
                            page-position="bottom"
                            configuration="tableConfig"
                            page-model="page"
                            page-size="${entityNameLowerCase}.pageSize"
                            count="${entityNameLowerCase}.count"
                            on-page-change="${entityNameLowerCase}.methods.searchWithGQuery(${entityNameLowerCase}.lastGQuery, page)">
                </gumga-list>
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
    </ul>
</gmd-fab>