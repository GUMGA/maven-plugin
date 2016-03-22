<#list values as value>
	${value.value}
</#list>
   
    private final String description; 
    
    ${enumName}(String description) { 
       this.description = description; 
    } 
    
    public String getDescription() {    
        return description; 
    } 
    
    public String toString() {
        return description; 
    }