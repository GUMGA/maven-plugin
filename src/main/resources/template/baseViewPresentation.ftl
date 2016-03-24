<gumga-nav 
    multi-entity="true" 
    put-url="http://www.gumga.com.br/security-api/publicoperations/token/" 
    title="${entityName}" 
    state="login.log">
</gumga-nav>
<gumga-menu 
    menu-url="gumga-menu.json" 
    keys-url="keys.json"  
    image="resources/images/gumga.png">
</gumga-menu>
<div class="gumga-container">
    <h3 style="margin-top: 0" gumga-translate-tag="${entityNameLowerCase}.title"></h3>
        <div class="col-md-12" ui-view></div>
</div>