<#list attributes as attribute>
	<#switch attribute.type>
		<#case "manyToOne">
		
		<#include "manyToOneField.ftl">
		<#break>
		<#case "oneToOne">
		
		<#include "oneToOneField.ftl">
		<#break>
		<#case "gumgaEmail">
		
		<#include "gumgaEmail.ftl">
		<#break>
		<#case "manyToMany">
		
		<#include "manyToManyField.ftl">
		<#break>
		<#case "oneToMany">
		
		<#include "oneToManyField.ftl">
		<#break>
		<#case "gumgaCustomFields">
		
		<#include "gumgaCustomFieldsField.ftl">
		<#break>
		<#case "gumgaAddress">
		
		<#include "gumgaAddress.ftl">
		<#break>
		<#case "gumgaBarCode">
		
		<#include "gumgaBarCodeField.ftl">
		<#break>
		<#case "gumgaCEP">
		
		<#include "gumgaCEPField.ftl">
		<#break>
		<#case "gumgaCNPJ">
		
		<#include "gumgaCNPJField.ftl">
		<#break>
		<#case "gumgaCPF">
		
		<#include "gumgaCPFField.ftl">
		<#break>
		<#case "gumgaIP4">
		
		<#include "gumgaIp4Field.ftl">
		<#break>				
		<#case "gumgaIP6">
		
		<#include "gumgaIp6Field.ftl">
		<#break>
		<#case "gumgaImage">
		
		<#include "gumgaImageField.ftl">
		<#break>
		<#case "gumgaMoney">
		
		<#include "gumgaMoneyField.ftl">
		<#break>
		<#case "gumgaPhoneNumber">
		
		<#include "gumgaPhoneNumberField.ftl">
		<#break>		
		<#case "gumgaGeoLocation">
		
		<#include "gumgaGeoLocationField.ftl">
		<#break>
		<#case "gumgaBoolean">
		
		<#include "gumgaBooleanField.ftl">
		<#break>						
		<#case "date">
		
		<#include "dateField.ftl">
		<#break>
		<#case "enum">
		
		<#include "enumField.ftl">
		<#break>												
		<#default>
		
		<#include "defaultField.ftl">
	</#switch>
</#list>