<#list values as value>
	${value.value}
</#list>
   
	private final int multValue;
	
	${enumName} (int multValue) {
	    this.multValue = multValue; 
	}
	
	public int getMultValue() {
	    return multValue;
	}