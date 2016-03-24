<form name="${entityName}Form" gumga-form novalidate>
    <#include "generateFields.ftl">
    <gumga-errors label="Lista de erros" title="Lista de erros"></gumga-errors>
    <gumga-form-buttons
            back="${entityNameLowerCase}.list"
            submit="${entityNameLowerCase}.methods.put(${entityNameLowerCase}.data)"
            position="right"
            valid="${entityName}Form.$valid"
            confirm-dirty="true"
            continue="continue">
    </gumga-form-buttons>    
</form>