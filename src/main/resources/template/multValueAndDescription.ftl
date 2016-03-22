<#list values as value>
	${value.value}
</#list>
 
	private final String description;
	
	private final int multValue; 
	
	${enumName}(String description, int multValue) { 
	    this.description = description;
	    this.multValue = multValue; 
	}
	
	public String getDescription() {
	    return description; 
	}
	
	public int getMultValue() {
	    return multValue; 
	}
	
	public String toString() {
	    return description; 
	}