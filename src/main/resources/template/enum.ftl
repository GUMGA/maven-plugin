package ${package};

public enum ${enumName} {

<#if "${simpleValue?c}" == "true">
<#include "simpleValue.ftl">
</#if>
<#if "${simpleValueAndDescription?c}" == "true">
<#include "simpleValueAndDescription.ftl">
</#if>
<#if "${multValue?c}" == "true">
<#include "multValue.ftl">
</#if>
<#if "${multValueAndDescription?c}" == "true">
<#include "multValueAndDescription.ftl">
</#if>

}